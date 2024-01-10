/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2022 Equinix, Inc
 * Copyright 2014-2022 The Billing Project, LLC
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

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfiguration;
import org.killbill.billing.plugin.dao.PluginDao.DBEngine;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSqlReportDataExtractor extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testSimple() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where `tenant_record_id` = 1234");
    }

    @Test(groups = "fast")
    public void testDimensions() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^dimension:currency^dimension:state");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select `day`, `currency`, `state`\n" +
                                                               "from payments_per_day\n" +
                                                               "where `tenant_record_id` = 1234");
    }

    @Test(groups = "fast")
    public void testMetrics() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^metric:amount^metric:fee");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select `day`, `amount`, `fee`\n" +
                                                               "from payments_per_day\n" +
                                                               "where `tenant_record_id` = 1234");
    }

    @Test(groups = "fast")
    public void testDimensionsAndMetrics() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^dimension:currency^dimension:state^metric:amount^metric:fee");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select\n" +
                                                               "  `day`,\n" +
                                                               "  `currency`,\n" +
                                                               "  `state`,\n" +
                                                               "  `amount`,\n" +
                                                               "  `fee`\n" +
                                                               "from payments_per_day\n" +
                                                               "where `tenant_record_id` = 1234");
    }

    @Test(groups = "fast")
    public void testCaseStatement() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^dimension:currency(USD|BRL,GBP,EUR,MXN,AUD)^dimension:state^metric:amount^metric:fee");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select\n" +
                                                               "  `day`,\n" +
                                                               "  case\n" +
                                                               "    when `currency` = 'USD' then 'USD'\n" +
                                                               "    when `currency` = 'BRL' then 'BRL,GBP,EUR,MXN,AUD'\n" +
                                                               "    when `currency` = 'GBP' then 'BRL,GBP,EUR,MXN,AUD'\n" +
                                                               "    when `currency` = 'EUR' then 'BRL,GBP,EUR,MXN,AUD'\n" +
                                                               "    when `currency` = 'MXN' then 'BRL,GBP,EUR,MXN,AUD'\n" +
                                                               "    when `currency` = 'AUD' then 'BRL,GBP,EUR,MXN,AUD'\n" +
                                                               "    else 'Other'\n" +
                                                               "  end as `currency`,\n" +
                                                               "  `state`,\n" +
                                                               "  `amount`,\n" +
                                                               "  `fee`\n" +
                                                               "from payments_per_day\n" +
                                                               "where `tenant_record_id` = 1234");
    }

    @Test(groups = "fast")
    public void testCaseStatementNoOther() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^dimension:currency(USD|BRL,GBP,EUR,MXN,AUD|-)^dimension:state^metric:amount^metric:fee");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select\n" +
                                                               "  `day`,\n" +
                                                               "  case\n" +
                                                               "    when `currency` = 'USD' then 'USD'\n" +
                                                               "    when `currency` = 'BRL' then 'BRL,GBP,EUR,MXN,AUD'\n" +
                                                               "    when `currency` = 'GBP' then 'BRL,GBP,EUR,MXN,AUD'\n" +
                                                               "    when `currency` = 'EUR' then 'BRL,GBP,EUR,MXN,AUD'\n" +
                                                               "    when `currency` = 'MXN' then 'BRL,GBP,EUR,MXN,AUD'\n" +
                                                               "    when `currency` = 'AUD' then 'BRL,GBP,EUR,MXN,AUD'\n" +
                                                               "  end as `currency`,\n" +
                                                               "  `state`,\n" +
                                                               "  `amount`,\n" +
                                                               "  `fee`\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  currency in (\n" +
                                                               "    'USD', 'BRL', 'GBP', 'EUR', 'MXN', 'AUD'\n" +
                                                               "  )\n" +
                                                               "  and `tenant_record_id` = 1234\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testFilterEqual() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^filter:currency=EUR^filter:currency=BTC");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  (\n" +
                                                               "    `currency` = 'BTC'\n" +
                                                               "    or `currency` = 'EUR'\n" +
                                                               "  )\n" +
                                                               "  and `tenant_record_id` = 1234\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testFilterNotEqual() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^filter:currency!=EUR^filter:currency!=BTC");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  (\n" +
                                                               "    `currency` <> 'BTC'\n" +
                                                               "    or `currency` <> 'EUR'\n" +
                                                               "  )\n" +
                                                               "  and `tenant_record_id` = 1234\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testFilters() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^filter:currency!=EUR^filter:state=PROCESSED^filter:state=PROCESSING");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  (\n" +
                                                               "    `currency` <> 'EUR'\n" +
                                                               "    or `state` = 'PROCESSED'\n" +
                                                               "    or `state` = 'PROCESSING'\n" +
                                                               "  )\n" +
                                                               "  and `tenant_record_id` = 1234\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testAggregate() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^dimension:currency^dimension:state^metric:avg(amount)^metric:sum(fee)^metric:count(distinct amount)");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select\n" +
                                                               "  `day`,\n" +
                                                               "  `currency`,\n" +
                                                               "  `state`,\n" +
                                                               "  avg(`amount`),\n" +
                                                               "  sum(`fee`),\n" +
                                                               "  count(distinct `amount`)\n" +
                                                               "from payments_per_day\n" +
                                                               "where `tenant_record_id` = 1234\n" +
                                                               "group by `day`, `currency`, `state`");
    }

    @Test(groups = "fast")
    public void testAggregateNoDimension() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^metric:avg(amount)");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select\n" +
                                                               "  `day`,\n" +
                                                               "  avg(`amount`)\n" +
                                                               "from payments_per_day\n" +
                                                               "where `tenant_record_id` = 1234\n" +
                                                               "group by `day`");
    }

    @Test(groups = "fast")
    public void testStartDate() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day", new LocalDate(2012, 11, 10).toDateTimeAtStartOfDay(), null);
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  `day` >= '2012-11-10'\n" +
                                                               "  and `tenant_record_id` = 1234\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testEndDate() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day", null, new LocalDate(2013, 11, 10).toDateTimeAtStartOfDay());
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  `day` <= '2013-11-10'\n" +
                                                               "  and `tenant_record_id` = 1234\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testStartAndEndDate() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day", new LocalDate(2012, 11, 10).toDateTimeAtStartOfDay(), new LocalDate(2013, 11, 10).toDateTimeAtStartOfDay());
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  `day` <= '2013-11-10'\n" +
                                                               "  and `day` >= '2012-11-10'\n" +
                                                               "  and `tenant_record_id` = 1234\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testFullThing() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^filter:currency!=EUR^filter:state=PROCESSED^filter:state=PROCESSING^dimension:currency^dimension:state^metric:amount^metric:fee",
                                                                                          new LocalDate(2012, 11, 10).toDateTimeAtStartOfDay(),
                                                                                          new LocalDate(2013, 11, 10).toDateTimeAtStartOfDay());
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select\n" +
                                                               "  `day`,\n" +
                                                               "  `currency`,\n" +
                                                               "  `state`,\n" +
                                                               "  `amount`,\n" +
                                                               "  `fee`\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  (\n" +
                                                               "    `currency` <> 'EUR'\n" +
                                                               "    or `state` = 'PROCESSED'\n" +
                                                               "    or `state` = 'PROCESSING'\n" +
                                                               "  )\n" +
                                                               "  and `day` >= '2012-11-10'\n" +
                                                               "  and `day` <= '2013-11-10'\n" +
                                                               "  and `tenant_record_id` = 1234\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testFullThingWithComplicatedWhereClause() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day^filter:(currency=USD&state!=ERRORED)|(currency=EUR&currency=PROCESSED)|(name~'John Doe%'&name!~'John Does')^dimension:currency^dimension:state^metric:avg(amount)^metric:avg(fee)^metric:100*sum(fee)/amount",
                                                                                          new LocalDate(2012, 11, 10).toDateTimeAtStartOfDay(),
                                                                                          new LocalDate(2013, 11, 10).toDateTimeAtStartOfDay());
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select\n" +
                                                               "  `day`,\n" +
                                                               "  `currency`,\n" +
                                                               "  `state`,\n" +
                                                               "  avg(`amount`),\n" +
                                                               "  avg(`fee`),\n" +
                                                               "  ((100 * sum(`fee`)) / `amount`)\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  (\n" +
                                                               "    (\n" +
                                                               "      `currency` = 'EUR'\n" +
                                                               "      and `currency` = 'PROCESSED'\n" +
                                                               "    )\n" +
                                                               "    or (\n" +
                                                               "      `currency` = 'USD'\n" +
                                                               "      and `state` <> 'ERRORED'\n" +
                                                               "    )\n" +
                                                               "    or (\n" +
                                                               "      `name` not like 'John Does'\n" +
                                                               "      and `name` like 'John Doe%'\n" +
                                                               "    )\n" +
                                                               "  )\n" +
                                                               "  and `day` >= '2012-11-10'\n" +
                                                               "  and `day` <= '2013-11-10'\n" +
                                                               "  and `tenant_record_id` = 1234\n" +
                                                               ")\n" +
                                                               "group by `day`, `currency`, `state`");
    }

    @Test(groups = "fast")
    public void testRawQueryWithTemplateDisabled() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractorForRawQuery("select :current_date from X where created_at > :current_date and group = :group and tenant_record_id = TENANT_RECORD_ID",
                                                                                                     "today^variable:current_date=2017-01-01,group=testing",
                                                                                                     new LocalDate(2012, 11, 10).toDateTimeAtStartOfDay(),
                                                                                                     new LocalDate(2013, 11, 10).toDateTimeAtStartOfDay(),
                                                                                                     false);
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select null\n" +
                                                               "from X\n" +
                                                               "where (\n" +
                                                               "  created_at > null\n" +
                                                               "  and group = null\n" +
                                                               "  and tenant_record_id = 1234\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testRawQueryWithTemplate() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractorForRawQuery("select :current_date from X where created_at > :current_date and group = :group and tenant_record_id = TENANT_RECORD_ID",
                                                                                                     "today^variable:current_date=2017-01-01,group=testing",
                                                                                                     new LocalDate(2012, 11, 10).toDateTimeAtStartOfDay(),
                                                                                                     new LocalDate(2013, 11, 10).toDateTimeAtStartOfDay(),
                                                                                                     true);
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select '2017-01-01'\n" +
                                                               "from X\n" +
                                                               "where (\n" +
                                                               "  created_at > '2017-01-01'\n" +
                                                               "  and group = 'testing'\n" +
                                                               "  and tenant_record_id = 1234\n" +
                                                               ")");
    }

    private SqlReportDataExtractor buildSqlReportDataExtractor(final String rawReportName) {
        return buildSqlReportDataExtractor(rawReportName, null, null);
    }

    private SqlReportDataExtractor buildSqlReportDataExtractor(final String rawReportName,
                                                               @Nullable final DateTime startDate,
                                                               @Nullable final DateTime endDate) {
        final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
        return new SqlReportDataExtractor(reportSpecification.getReportName(), reportSpecification, startDate, endDate, DBEngine.MYSQL, 1234L);
    }

    private SqlReportDataExtractor buildSqlReportDataExtractorForRawQuery(final String sourceQuery,
                                                                          final String rawReportName,
                                                                          @Nullable final DateTime startDate,
                                                                          @Nullable final DateTime endDate,
                                                                          final boolean templatingEnabled) {
        final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
        final AnalyticsConfiguration configurable = new AnalyticsConfiguration(templatingEnabled);
        return new SqlReportDataExtractor(sourceQuery, reportSpecification, startDate, endDate, configurable, DBEngine.MYSQL, 1234L);
    }
}
