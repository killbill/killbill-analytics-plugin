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
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;
import org.jooq.AggregateFunction;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;

import com.ning.billing.osgi.bundles.analytics.reports.sql.Aggregates;
import com.ning.billing.osgi.bundles.analytics.reports.sql.Cases;
import com.ning.billing.osgi.bundles.analytics.reports.sql.Filters;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Variable;
import com.google.common.base.Function;
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

    private Collection<Field<Object>> dimensions = ImmutableList.<Field<Object>>of();
    private Collection<Field<Object>> metrics = ImmutableList.<Field<Object>>of();
    private Expression<String> filters = null;
    private boolean shouldGroupBy = false;

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

        setup();
    }

    @Override
    public String toString() {
        // Generate "select *" if no dimension or metric is precised
        final SelectSelectStep<? extends Record> initialSelect = dimensions.size() == 1 && metrics.isEmpty() ? context.select()
                                                                                                             : context.select(dimensions)
                                                                                                                      .select(metrics);

        SelectConditionStep<? extends Record> statement = initialSelect.from(tableName)
                                                                       .where();


        if (filters != null) {
            statement = statement.and(Filters.of(filters));
        }

        if (shouldGroupBy) {
            return statement.groupBy(dimensions)
                            .getSQL();
        } else {
            return statement.getSQL();
        }
    }

    private void setup() {
        setupDimensions();
        setupMetrics();
        setupFilters();
    }

    private void setupDimensions() {
        dimensions = new LinkedList<Field<Object>>();

        // Add the special "day" column if needed
        if (!reportSpecification.getDimensions().contains(DAY_COLUMN_NAME)) {
            dimensions.add(stringToField(DAY_COLUMN_NAME));
        }

        // Add all other dimensions, potential building case statements as we go
        for (final String dimensionWithGrouping : reportSpecification.getDimensionsWithGrouping()) {
            dimensions.add(Cases.of(dimensionWithGrouping));
        }
    }

    private void setupMetrics() {
        metrics = stringsToFields(reportSpecification.getMetrics());
    }

    private void setupFilters() {
        filters = reportSpecification.getFilterExpression();

        // Deal with dates (as yet another, specific, filter)
        if (startDate != null) {
            final Variable<String> dateCheck = Variable.of(String.format("%s>=%s", DAY_COLUMN_NAME, startDate));
            filters = filters == null ? dateCheck : And.of(filters, dateCheck);
        }
        if (endDate != null) {
            final Variable<String> dateCheck = Variable.of(String.format("%s<=%s", DAY_COLUMN_NAME, endDate));
            filters = filters == null ? dateCheck : And.of(filters, dateCheck);
        }
    }

    private Collection<Field<Object>> stringsToFields(final List<String> columns) {
        return Collections2.transform(columns,
                                      new Function<String, Field<Object>>() {
                                          @Override
                                          public Field<Object> apply(final String columnName) {
                                              return stringToField(columnName);
                                          }
                                      });
    }

    private Field<Object> stringToField(final String columnName) {
        final AggregateFunction<?> candidateField = Aggregates.of(columnName);
        if (candidateField != null) {
            shouldGroupBy = true;
            return (Field<Object>) candidateField;
        } else {
            return DSL.fieldByName(columnName);
        }
    }
}
