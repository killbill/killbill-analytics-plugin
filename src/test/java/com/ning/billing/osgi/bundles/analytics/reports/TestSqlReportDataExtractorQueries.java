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

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteWithEmbeddedDB;

public class TestSqlReportDataExtractorQueries extends AnalyticsTestSuiteWithEmbeddedDB {

    @Test(groups = "slow")
    public void testQueryGeneration() throws Exception {
        final String tableName = "payments_per_day";
        embeddedDB.executeScript(String.format("create table %s(day datetime, name varchar(100), currency varchar(10), state varchar(10), amount int, fee int);", tableName));

        final String query = "payments_per_day;" +
                             "filter:(currency=USD&state!=ERRORED)|(currency=EUR&currency=PROCESSED)|(name~'John Doe%'&name!~'John Does');" +
                             "filter:currency=BTC;" +
                             "dimension:currency(USD|BRL,GBP,EUR,MXN,AUD);" +
                             "dimension:state;" +
                             "metric:avg(amount);" +
                             "metric:avg(fee);" +
                             "metric:100*sum(fee)/amount";
        final ReportSpecification reportSpecification = new ReportSpecification(query);
        final SqlReportDataExtractor sqlReportDataExtractor = new SqlReportDataExtractor(tableName, reportSpecification, new LocalDate(2012, 10, 10), new LocalDate(2014, 11, 11));

        final List<Map<String, Object>> results = dbi.withHandle(new HandleCallback<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> withHandle(final Handle handle) throws Exception {
                return handle.select(sqlReportDataExtractor.toString());
            }
        });

        // Don't actually test the query, just make sure it got executed (no MySQL error)
        Assert.assertTrue(results.isEmpty());
    }
}
