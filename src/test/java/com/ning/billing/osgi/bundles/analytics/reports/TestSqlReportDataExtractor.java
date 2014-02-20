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

import javax.annotation.Nullable;

import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSqlReportDataExtractor {

    @Test(groups = "fast")
    public void testSimple() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day");
    }

    @Test(groups = "fast")
    public void testDimensions() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day;dimension:currency;dimension:state");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select \n" +
                                                               "  `day`, \n" +
                                                               "  `currency`, \n" +
                                                               "  `state`\n" +
                                                               "from payments_per_day");
    }

    @Test(groups = "fast")
    public void testMetrics() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day;metric:amount;metric:fee");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select \n" +
                                                               "  `day`, \n" +
                                                               "  `amount`, \n" +
                                                               "  `fee`\n" +
                                                               "from payments_per_day");
    }

    @Test(groups = "fast")
    public void testDimensionsAndMetrics() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day;dimension:currency;dimension:state;metric:amount;metric:fee");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select \n" +
                                                               "  `day`, \n" +
                                                               "  `currency`, \n" +
                                                               "  `state`, \n" +
                                                               "  `amount`, \n" +
                                                               "  `fee`\n" +
                                                               "from payments_per_day");
    }

    @Test(groups = "fast")
    public void testFilterEqual() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day;filter:currency=EUR;filter:currency=BTC");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  `currency` = 'BTC'\n" +
                                                               "  or `currency` = 'EUR'\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testFilterNotEqual() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day;filter:currency!=EUR;filter:currency!=BTC");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  `currency` <> 'BTC'\n" +
                                                               "  or `currency` <> 'EUR'\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testFilters() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day;filter:currency!=EUR;filter:state=PROCESSED;filter:state=PROCESSING");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  `currency` <> 'EUR'\n" +
                                                               "  or `state` = 'PROCESSED'\n" +
                                                               "  or `state` = 'PROCESSING'\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testAggregate() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day;dimension:currency;dimension:state;metric:avg(amount);metric:sum(fee);metric:count(distinct amount)");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select \n" +
                                                               "  `day`, \n" +
                                                               "  `currency`, \n" +
                                                               "  `state`, \n" +
                                                               "  avg(`amount`), \n" +
                                                               "  sum(`fee`), \n" +
                                                               "  count(distinct `amount`)\n" +
                                                               "from payments_per_day\n" +
                                                               "group by \n" +
                                                               "  `day`, \n" +
                                                               "  `currency`, \n" +
                                                               "  `state`");
    }

    @Test(groups = "fast")
    public void testAggregateNoDimension() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day;metric:avg(amount)");
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select \n" +
                                                               "  `day`, \n" +
                                                               "  avg(`amount`)\n" +
                                                               "from payments_per_day\n" +
                                                               "group by `day`");
    }

    @Test(groups = "fast")
    public void testStartDate() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day", new LocalDate(2012, 11, 10), null);
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where `day` >= '2012-11-10'");
    }

    @Test(groups = "fast")
    public void testEndDate() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day", null, new LocalDate(2013, 11, 10));
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where `day` <= '2013-11-10'");
    }

    @Test(groups = "fast")
    public void testStartAndEndDate() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day", new LocalDate(2012, 11, 10), new LocalDate(2013, 11, 10));
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select *\n" +
                                                               "from payments_per_day\n" +
                                                               "where (\n" +
                                                               "  `day` <= '2013-11-10'\n" +
                                                               "  and `day` >= '2012-11-10'\n" +
                                                               ")");
    }

    @Test(groups = "fast")
    public void testFullThing() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day;filter:currency!=EUR;filter:state=PROCESSED;filter:state=PROCESSING;dimension:currency;dimension:state;metric:amount;metric:fee",
                                                                                          new LocalDate(2012, 11, 10),
                                                                                          new LocalDate(2013, 11, 10));
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select \n" +
                                                               "  `day`, \n" +
                                                               "  `currency`, \n" +
                                                               "  `state`, \n" +
                                                               "  `amount`, \n" +
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
                                                               ")");
    }

    @Test(groups = "fast")
    public void testFullThingWithComplicatedWhereClause() throws Exception {
        final SqlReportDataExtractor sqlReportDataExtractor = buildSqlReportDataExtractor("payments_per_day;filter:(currency=USD&state!=ERRORED)|(currency=EUR&currency=PROCESSED)|(name~'John Doe%'&name!~'John Does');dimension:currency;dimension:state;metric:avg(amount);metric:avg(fee)",
                                                                                          new LocalDate(2012, 11, 10),
                                                                                          new LocalDate(2013, 11, 10));
        Assert.assertEquals(sqlReportDataExtractor.toString(), "select \n" +
                                                               "  `day`, \n" +
                                                               "  `currency`, \n" +
                                                               "  `state`, \n" +
                                                               "  avg(`amount`), \n" +
                                                               "  avg(`fee`)\n" +
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
                                                               ")\n" +
                                                               "group by \n" +
                                                               "  `day`, \n" +
                                                               "  `currency`, \n" +
                                                               "  `state`");
    }

    private SqlReportDataExtractor buildSqlReportDataExtractor(final String rawReportName) {
        return buildSqlReportDataExtractor(rawReportName, null, null);
    }

    private SqlReportDataExtractor buildSqlReportDataExtractor(final String rawReportName, @Nullable final LocalDate startDate, @Nullable final LocalDate endDate) {
        final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
        return new SqlReportDataExtractor(reportSpecification.getReportName(), reportSpecification, startDate, endDate);
    }
}
