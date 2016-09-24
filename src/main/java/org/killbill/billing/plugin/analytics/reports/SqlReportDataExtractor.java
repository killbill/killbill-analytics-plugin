/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2016 Groupon, Inc
 * Copyright 2014-2016 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
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

package org.killbill.billing.plugin.analytics.reports;

import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSelectStep;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;
import org.killbill.billing.plugin.analytics.reports.sql.Cases;
import org.killbill.billing.plugin.analytics.reports.sql.Filters;
import org.killbill.billing.plugin.analytics.reports.sql.MetricExpressionParser;
import org.killbill.commons.embeddeddb.EmbeddedDB;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Variable;
import com.google.common.collect.ImmutableList;

import static org.killbill.billing.plugin.analytics.reports.ReportsUserApi.DAY_COLUMN_NAME;
import static org.killbill.billing.plugin.analytics.reports.ReportsUserApi.TS_COLUMN_NAME;

public class SqlReportDataExtractor {

    private final String tableName;
    private final ReportSpecification reportSpecification;
    private final DateTime startDate;
    private final DateTime endDate;
    private final DSLContext context;
    private final Long tenantRecordId;

    private Collection<Field<Object>> dimensions = ImmutableList.<Field<Object>>of();
    private Collection<Field<Object>> metrics = ImmutableList.<Field<Object>>of();
    private Expression<String> filters = null;
    private Condition condition = null;
    private boolean shouldGroupBy = false;

    public SqlReportDataExtractor(final String tableName,
                                  final ReportSpecification reportSpecification,
                                  @Nullable final DateTime startDate,
                                  @Nullable final DateTime endDate,
                                  final EmbeddedDB.DBEngine dbEngine,
                                  final Long tenantRecordId) {
        this(tableName, reportSpecification, startDate, endDate, SQLDialectFromDBEngine(dbEngine), tenantRecordId);
    }

    public SqlReportDataExtractor(final String tableName,
                                  final ReportSpecification reportSpecification,
                                  @Nullable final DateTime startDate,
                                  @Nullable final DateTime endDate,
                                  final SQLDialect sqlDialect,
                                  final Long tenantRecordId) {
        this.tableName = tableName;
        this.reportSpecification = reportSpecification;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tenantRecordId = tenantRecordId;

        final Settings settings = new Settings();
        settings.withStatementType(StatementType.STATIC_STATEMENT);
        settings.withRenderFormatted(true);
        if (SQLDialect.H2.equals(sqlDialect)) {
            settings.withRenderNameStyle(RenderNameStyle.AS_IS);
        }
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
        if (condition != null) {
            statement = statement.and(condition);
        }

        statement.and(DSL.fieldByName("tenant_record_id").eq(tenantRecordId));

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
        if (!reportSpecification.getDimensions().contains(DAY_COLUMN_NAME) && !reportSpecification.getDimensions().contains(TS_COLUMN_NAME)) {
            dimensions.add(DSL.fieldByName(DAY_COLUMN_NAME));
        }

        // Add all other dimensions, potential building case statements as we go
        for (final String dimensionWithGrouping : reportSpecification.getDimensionsWithGrouping()) {
            final Cases.FieldWithMetadata fieldWithMetadata = Cases.of(dimensionWithGrouping);
            dimensions.add(fieldWithMetadata.getField());

            if (fieldWithMetadata.getCondition() != null) {
                condition = condition == null ? fieldWithMetadata.getCondition() : condition.and(fieldWithMetadata.getCondition());
            }
        }
    }

    private void setupMetrics() {
        metrics = new LinkedList<Field<Object>>();
        for (final String metric : reportSpecification.getMetrics()) {
            final MetricExpressionParser.FieldWithMetadata fieldWithMetadata = MetricExpressionParser.parse(metric);
            metrics.add(fieldWithMetadata.getField());
            shouldGroupBy = shouldGroupBy || fieldWithMetadata.hasAggregateFunction();
        }
    }

    private void setupFilters() {
        filters = reportSpecification.getFilterExpression();

        // Deal with dates (as yet another, specific, filter)
        if (startDate != null) {
            final Variable<String> dateCheck;
            if (!reportSpecification.getDimensions().contains(TS_COLUMN_NAME) && startDate.compareTo(startDate.toLocalDate().toDateTimeAtStartOfDay(DateTimeZone.UTC)) == 0) {
                dateCheck = Variable.of(String.format("%s>=%s", DAY_COLUMN_NAME, startDate.toLocalDate()));
            } else {
                dateCheck = Variable.of(String.format("%s>=%s", TS_COLUMN_NAME, startDate));
            }
            filters = filters == null ? dateCheck : And.of(filters, dateCheck);
        }
        if (endDate != null) {
            final Variable<String> dateCheck;
            if (!reportSpecification.getDimensions().contains(TS_COLUMN_NAME) && endDate.compareTo(endDate.toLocalDate().toDateTimeAtStartOfDay(DateTimeZone.UTC)) == 0) {
                dateCheck = Variable.of(String.format("%s<=%s", DAY_COLUMN_NAME, endDate.toLocalDate()));
            } else {
                dateCheck = Variable.of(String.format("%s<=%s", TS_COLUMN_NAME, endDate));
            }
            filters = filters == null ? dateCheck : And.of(filters, dateCheck);
        }
    }

    private static SQLDialect SQLDialectFromDBEngine(final EmbeddedDB.DBEngine dbEngine) {
        switch (dbEngine) {
            case H2:
                return SQLDialect.H2;
            case MYSQL:
                return SQLDialect.MARIADB;
            case POSTGRESQL:
                return SQLDialect.POSTGRES;
            default:
                throw new IllegalArgumentException("Unsupported DB engine: " + dbEngine);
        }
    }
}
