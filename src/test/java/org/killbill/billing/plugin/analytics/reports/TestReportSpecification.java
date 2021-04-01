/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
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

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

public class TestReportSpecification extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testParserInclusions() throws Exception {
        final String rawReportName = "payments_per_day^filter:currency=AUD^filter:currency=EUR^dimension:currency^dimension:state^metric:amount^metric:fee";
        final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
        Assert.assertEquals(reportSpecification.getReportName(), "payments_per_day");

        Assert.assertEquals(reportSpecification.getDimensions(), ImmutableList.<String>of("currency", "state"));
        Assert.assertEquals(reportSpecification.getMetrics(), ImmutableList.<String>of("amount", "fee"));

        Assert.assertEquals(reportSpecification.getFilterExpression().toString(), "(currency=AUD | currency=EUR)");
    }

    @Test(groups = "fast")
    public void testParserExclusions() throws Exception {
        final String rawReportName = "payments_per_day^filter:currency!=AUD^filter:currency!=EUR";
        final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
        Assert.assertEquals(reportSpecification.getReportName(), "payments_per_day");

        Assert.assertTrue(reportSpecification.getDimensions().isEmpty());
        Assert.assertTrue(reportSpecification.getMetrics().isEmpty());

        Assert.assertEquals(reportSpecification.getFilterExpression().toString(), "(currency!=AUD | currency!=EUR)");
    }

    @Test(groups = "fast")
    public void testParserWithSpecialCharacters() throws Exception {
        final String rawReportName = "payments_per_day^filter:plugin_name=killbill-paypal-express";
        final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
        Assert.assertEquals(reportSpecification.getReportName(), "payments_per_day");

        Assert.assertTrue(reportSpecification.getDimensions().isEmpty());
        Assert.assertTrue(reportSpecification.getMetrics().isEmpty());

        Assert.assertEquals(reportSpecification.getFilterExpression().toString(), "plugin_name=killbill-paypal-express");
    }

    @Test(groups = "fast")
    public void testParserComplexFilter() throws Exception {
        final String rawReportName = "payments_per_day(With a custom title, which supports sp3cial ch@racter$!)^" +
                                     "filter:(currency=USD&state!=ERRORED)|(currency=EUR&state=PROCESSED)^" +
                                     "filter:name~'John Doe'&age>=35|name!~Fred&age<24";
        final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
        Assert.assertEquals(reportSpecification.getReportName(), "payments_per_day");
        Assert.assertEquals(reportSpecification.getLegend(), "With a custom title, which supports sp3cial ch@racter$!");

        Assert.assertTrue(reportSpecification.getDimensions().isEmpty());
        Assert.assertTrue(reportSpecification.getMetrics().isEmpty());

        Assert.assertEquals(reportSpecification.getFilterExpression().toString(), "(" +
                                                                                  "((age<24 & name!~Fred) | (age>=35 & name~'John Doe')) " +
                                                                                  "| " +
                                                                                  "((currency=EUR & state=PROCESSED) | (currency=USD & state!=ERRORED))" +
                                                                                  ")");
    }
}
