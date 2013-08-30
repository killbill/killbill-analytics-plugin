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

package com.ning.billing.osgi.bundles.analytics.reports.configuration;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteNoDB;
import com.ning.billing.osgi.bundles.analytics.reports.configuration.ReportsConfigurationModelDao.Frequency;

public class TestReportsConfigurationModelDao extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testGetters() throws Exception {
        final String reportName = UUID.randomUUID().toString();
        final String reportPrettyName = UUID.randomUUID().toString();
        final String sourceTableName = UUID.randomUUID().toString();
        final String refreshProcedureName = UUID.randomUUID().toString();
        final Frequency daily = Frequency.DAILY;
        final int refreshHourOfDayGmt = 12;
        final ReportsConfigurationModelDao reportsConfiguration = new ReportsConfigurationModelDao(reportName,
                                                                                                   reportPrettyName,
                                                                                                   sourceTableName,
                                                                                                   refreshProcedureName,
                                                                                                   daily,
                                                                                                   refreshHourOfDayGmt);
        Assert.assertEquals(reportsConfiguration.getReportName(), reportName);
        Assert.assertEquals(reportsConfiguration.getReportPrettyName(), reportPrettyName);
        Assert.assertEquals(reportsConfiguration.getSourceTableName(), sourceTableName);
        Assert.assertEquals(reportsConfiguration.getRefreshProcedureName(), refreshProcedureName);
        Assert.assertEquals(reportsConfiguration.getRefreshFrequency(), daily);
        Assert.assertEquals(reportsConfiguration.getRefreshHourOfDayGmt(), (Integer) refreshHourOfDayGmt);
    }
}
