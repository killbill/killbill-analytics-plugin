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

import java.util.UUID;

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteWithEmbeddedDB;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.Frequency;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.ReportType;
import org.killbill.billing.plugin.analytics.reports.scheduler.AnalyticsReportJob;
import org.killbill.billing.plugin.analytics.reports.scheduler.JobsScheduler;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestReportsConfiguration extends AnalyticsTestSuiteWithEmbeddedDB {

    private final Long tenantRecordId = 1234L;

    @Test(groups = "slow")
    public void testCrud() throws Exception {
        final JobsScheduler jobsScheduler = new JobsScheduler(killbillDataSource, clock, notificationQueueService);
        final ReportsConfiguration reportsConfiguration = new ReportsConfiguration(killbillDataSource, jobsScheduler);

        // Verify initial state
        Assert.assertEquals(reportsConfiguration.getAllReportConfigurations(tenantRecordId).keySet().size(), 0);
        Assert.assertEquals(jobsScheduler.schedules().size(), 0);

        final ReportsConfigurationModelDao report1 = createReportConfiguration();
        final ReportsConfigurationModelDao report2 = createReportConfiguration();

        // Create the first report
        reportsConfiguration.createReportConfiguration(report1, tenantRecordId);
        Assert.assertEquals(reportsConfiguration.getAllReportConfigurations(tenantRecordId).keySet().size(), 1);
        Assert.assertTrue(reportsConfiguration.getAllReportConfigurations(tenantRecordId).get(report1.getReportName()).equalsNoRecordId(report1));
        Assert.assertTrue(reportsConfiguration.getReportConfigurationForReport(report1.getReportName(), tenantRecordId).equalsNoRecordId(report1));

        final AnalyticsReportJob reportJob1 = new AnalyticsReportJob(reportsConfiguration.getAllReportConfigurations(tenantRecordId).get(report1.getReportName()));
        Assert.assertEquals(jobsScheduler.schedules().size(), 1);
        Assert.assertEquals(jobsScheduler.schedules().get(0), reportJob1);

        // Create the second one
        reportsConfiguration.createReportConfiguration(report2, tenantRecordId);
        Assert.assertEquals(reportsConfiguration.getAllReportConfigurations(tenantRecordId).keySet().size(), 2);
        Assert.assertTrue(reportsConfiguration.getAllReportConfigurations(tenantRecordId).get(report1.getReportName()).equalsNoRecordId(report1));
        Assert.assertTrue(reportsConfiguration.getAllReportConfigurations(tenantRecordId).get(report2.getReportName()).equalsNoRecordId(report2));
        Assert.assertTrue(reportsConfiguration.getReportConfigurationForReport(report1.getReportName(), tenantRecordId).equalsNoRecordId(report1));
        Assert.assertTrue(reportsConfiguration.getReportConfigurationForReport(report2.getReportName(), tenantRecordId).equalsNoRecordId(report2));

        final AnalyticsReportJob reportJob2 = new AnalyticsReportJob(reportsConfiguration.getAllReportConfigurations(tenantRecordId).get(report2.getReportName()));
        Assert.assertEquals(jobsScheduler.schedules().size(), 2);
        Assert.assertEquals(jobsScheduler.schedules().get(0), reportJob1);
        Assert.assertEquals(jobsScheduler.schedules().get(1), reportJob2);

        // Update the first one
        final ReportsConfigurationModelDao updatedReport1 = new ReportsConfigurationModelDao(report1.getReportName(),
                                                                                             report1.getReportPrettyName(),
                                                                                             ReportType.TIMELINE,
                                                                                             report1.getSourceTableName(),
                                                                                             report1.getRefreshProcedureName(),
                                                                                             Frequency.HOURLY,
                                                                                             report1.getRefreshHourOfDayGmt());
        reportsConfiguration.updateReportConfiguration(updatedReport1, tenantRecordId);
        Assert.assertEquals(reportsConfiguration.getAllReportConfigurations(tenantRecordId).keySet().size(), 2);
        Assert.assertTrue(reportsConfiguration.getAllReportConfigurations(tenantRecordId).get(report1.getReportName()).equalsNoRecordId(updatedReport1));
        Assert.assertTrue(reportsConfiguration.getAllReportConfigurations(tenantRecordId).get(report2.getReportName()).equalsNoRecordId(report2));
        Assert.assertTrue(reportsConfiguration.getReportConfigurationForReport(report1.getReportName(), tenantRecordId).equalsNoRecordId(updatedReport1));
        Assert.assertTrue(reportsConfiguration.getReportConfigurationForReport(report2.getReportName(), tenantRecordId).equalsNoRecordId(report2));
        Assert.assertEquals(jobsScheduler.schedules().size(), 2);
        Assert.assertEquals(jobsScheduler.schedules().get(0).getReportName(), report1.getReportName());
        Assert.assertEquals(jobsScheduler.schedules().get(0).getRefreshFrequency(), Frequency.HOURLY);
        Assert.assertEquals(jobsScheduler.schedules().get(1), reportJob2);

        // Delete the second one
        reportsConfiguration.deleteReportConfiguration(report2.getReportName(), tenantRecordId);
        Assert.assertEquals(reportsConfiguration.getAllReportConfigurations(tenantRecordId).keySet().size(), 1);
        Assert.assertTrue(reportsConfiguration.getAllReportConfigurations(tenantRecordId).get(report1.getReportName()).equalsNoRecordId(updatedReport1));
        Assert.assertTrue(reportsConfiguration.getReportConfigurationForReport(report1.getReportName(), tenantRecordId).equalsNoRecordId(updatedReport1));
        Assert.assertEquals(jobsScheduler.schedules().size(), 1);
        Assert.assertEquals(jobsScheduler.schedules().get(0).getReportName(), report1.getReportName());
    }

    private ReportsConfigurationModelDao createReportConfiguration() {
        final String reportName = UUID.randomUUID().toString();
        final String reportPrettyName = UUID.randomUUID().toString();
        final String sourceTableName = UUID.randomUUID().toString();
        final String refreshProcedureName = UUID.randomUUID().toString();
        final Frequency daily = Frequency.DAILY;
        final int refreshHourOfDayGmt = 12;
        return new ReportsConfigurationModelDao(reportName,
                                                reportPrettyName,
                                                ReportType.TIMELINE,
                                                sourceTableName,
                                                refreshProcedureName,
                                                daily,
                                                refreshHourOfDayGmt);
    }
}
