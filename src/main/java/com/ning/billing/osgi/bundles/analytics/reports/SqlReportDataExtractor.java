/*
 * Copyright 2010-2014 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.osgi.bundles.analytics.reports;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Literal;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

public class SqlReportDataExtractor {

    // Part of the public API
    private static final String DAY_COLUMN_NAME = "day";

    private final String tableName;
    private final ReportSpecification reportSpecification;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final DSLContext context;

    public SqlReportDataExtractor(final String tableName,
                                  final ReportSpecification reportSpecification) {
        this(tableName, reportSpecification, null, null);
    }

    public SqlReportDataExtractor(final String tableName,
                                  final ReportSpecification reportSpecification,
                                  @Nullable final LocalDate startDate,
                                  @Nullable final LocalDate endDate) {
        this(tableName, reportSpecification, startDate, endDate, SQLDialect.MYSQL);
    }

    public SqlReportDataExtractor(final String tableName,
                                  final ReportSpecification reportSpecification,
                                  @Nullable final LocalDate startDate,
                                  @Nullable final LocalDate endDate,
                                  final SQLDialect sqlDialect) {
        this.tableName = tableName;
        this.reportSpecification = reportSpecification;
        this.startDate = startDate;
        this.endDate = endDate;

        final Settings settings = new Settings();
        settings.withStatementType(StatementType.STATIC_STATEMENT);
        settings.withRenderFormatted(true);
        this.context = DSL.using(sqlDialect, settings);
    }

    private enum SqlMapping {
        // Order matters! See below
        GE(">="),
        LE("<="),
        GT(">"),
        LT("<"),
        NE("!="),
        EQ("="),
        NOT_LIKE("!~"),
        LIKE("~");

        private final String representation;
        private final Splitter splitter;

        SqlMapping(final String representation) {
            this.representation = representation;
            this.splitter = Splitter.on(representation).omitEmptyStrings().trimResults().limit(2);
        }

        public String getRepresentation() {
            return representation;
        }

        public Splitter getSplitter() {
            return splitter;
        }

    }

    @VisibleForTesting
    static Condition buildConditionFromVariable(final Variable<String> input) {
        SqlMapping sqlOp = null;
        String column = null;
        String expression = null;
        for (final SqlMapping sqlMapping : SqlMapping.values()) {
            final List<String> parts = ImmutableList.<String>copyOf(sqlMapping.getSplitter().split(input.getValue()));
            if (parts.size() != 2) {
                continue;
            }

            // Take the first match in case of equality (see above)
            if (column == null || parts.get(0).length() < column.length()) {
                column = parts.get(0);
                expression = parts.get(1);
                sqlOp = sqlMapping;
            }
        }

        if (sqlOp != null) {
            // Un-quote the expression if needed (to avoid double quoting)
            expression = expression.replaceFirst("^[\"']", "");
            expression = expression.replaceFirst("[\"']$", "");

            final Field<Object> field = DSL.fieldByName(column);
            switch (sqlOp) {
                case EQ:
                    return field.eq(expression);
                case NE:
                    return field.ne(expression);
                case GT:
                    return field.gt(expression);
                case LT:
                    return field.lt(expression);
                case GE:
                    return field.ge(expression);
                case LE:
                    return field.le(expression);
                case LIKE:
                    return field.like(expression);
                case NOT_LIKE:
                    return field.notLike(expression);
                default:
                    throw new IllegalStateException("Unknown operation " + sqlOp);
            }
        } else {
            return DSL.trueCondition();
        }
    }

    @VisibleForTesting
    static Condition buildConditionFromExpression(final Expression<String> expression) {
        if (expression instanceof And) {
            Condition condition = null;
            for (final Expression<String> childExpression : ((And<String>) expression).getChildren()) {
                final Condition newCondition = buildConditionFromExpression(childExpression);
                condition = condition == null ? newCondition : condition.and(newCondition);
            }
            return condition;
        } else if (expression instanceof Literal) {
            final boolean value = ((Literal) expression).getValue();
            if (value) {
                return DSL.trueCondition();
            } else {
                return DSL.falseCondition();
            }
        } else if (expression instanceof Not) {
            return DSL.trueCondition().andNot(buildConditionFromExpression(((Not<String>) expression).getE()));
        } else if (expression instanceof Or) {
            Condition condition = null;
            for (final Expression<String> childExpression : ((Or<String>) expression).getChildren()) {
                final Condition newCondition = buildConditionFromExpression(childExpression);
                condition = condition == null ? newCondition : condition.or(newCondition);
            }
            return condition;
        } else if (expression instanceof Variable) {
            return buildConditionFromVariable((Variable<String>) expression);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        // Generate "select *" if no dimension or metric is precised
        final SelectSelectStep<? extends Record> initialSelect = reportSpecification.getDimensions().isEmpty() && reportSpecification.getMetrics().isEmpty() ? context.select()
                                                                                                                                                             : context.select(DSL.fieldByName(DAY_COLUMN_NAME));

        SelectConditionStep<Record> statement = initialSelect.select(stringsToFields(reportSpecification.getDimensions()))
                                                             .select(stringsToFields(reportSpecification.getMetrics()))
                                                             .from(tableName)
                                                             .where();

        // Deal with filters
        Expression<String> filterExpression = reportSpecification.getFilterExpression();

        // Deal with dates (as yet another, specific, filter)
        if (startDate != null) {
            final Variable<String> dateCheck = Variable.of(String.format("%s>=%s", DAY_COLUMN_NAME, startDate));
            filterExpression = filterExpression == null ? dateCheck : And.of(filterExpression, dateCheck);
        }
        if (endDate != null) {
            final Variable<String> dateCheck = Variable.of(String.format("%s<=%s", DAY_COLUMN_NAME, endDate));
            filterExpression = filterExpression == null ? dateCheck : And.of(filterExpression, dateCheck);
        }

        if (filterExpression != null) {
            statement = statement.and(buildConditionFromExpression(filterExpression));
        }

        return statement.getSQL();
    }

    private Collection<Field<?>> stringsToFields(final List<String> columns) {
        return Collections2.transform(columns,
                                      new Function<String, Field<?>>() {
                                          @Override
                                          public Field<?> apply(final String columnName) {
                                              return DSL.fieldByName(columnName);
                                          }
                                      });
    }
}
