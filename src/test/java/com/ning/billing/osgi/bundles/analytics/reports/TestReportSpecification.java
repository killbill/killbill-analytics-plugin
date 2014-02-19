/*
 * Copyright 2010-2013 Ning, Inc.
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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteNoDB;

import com.google.common.collect.ImmutableList;

public class TestReportSpecification extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testParserInclusions() throws Exception {
        final String rawReportName = "payments_per_day;filter:currency=AUD;filter:currency=EUR;dimension:currency;dimension:state;metric:amount;metric:fee";
        final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
        Assert.assertEquals(reportSpecification.getReportName(), "payments_per_day");

        Assert.assertEquals(reportSpecification.getDimensions(), ImmutableList.<String>of("currency", "state"));
        Assert.assertEquals(reportSpecification.getMetrics(), ImmutableList.<String>of("amount", "fee"));

        Assert.assertEquals(reportSpecification.getFilterExpression().toString(), "(currency=AUD | currency=EUR)");
    }

    @Test(groups = "fast")
    public void testParserExclusions() throws Exception {
        final String rawReportName = "payments_per_day;filter:currency!=AUD;filter:currency!=EUR";
        final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
        Assert.assertEquals(reportSpecification.getReportName(), "payments_per_day");

        Assert.assertTrue(reportSpecification.getDimensions().isEmpty());
        Assert.assertTrue(reportSpecification.getMetrics().isEmpty());

        Assert.assertEquals(reportSpecification.getFilterExpression().toString(), "(currency!=AUD | currency!=EUR)");
    }

    @Test(groups = "fast")
    public void testParserComplexFilter() throws Exception {
        final String rawReportName = "payments_per_day;" +
                                     "filter:(currency=USD&state!=ERRORED)|(currency=EUR&state=PROCESSED);" +
                                     "filter:name~'John Doe'&age>=35|name!~Fred&age<24";
        final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
        Assert.assertEquals(reportSpecification.getReportName(), "payments_per_day");

        Assert.assertTrue(reportSpecification.getDimensions().isEmpty());
        Assert.assertTrue(reportSpecification.getMetrics().isEmpty());

        Assert.assertEquals(reportSpecification.getFilterExpression().toString(), "(" +
                                                                                  "((age<24 & name!~Fred) | (age>=35 & name~'John Doe')) " +
                                                                                  "| " +
                                                                                  "((currency=EUR & state=PROCESSED) | (currency=USD & state!=ERRORED))" +
                                                                                  ")");
    }
}
