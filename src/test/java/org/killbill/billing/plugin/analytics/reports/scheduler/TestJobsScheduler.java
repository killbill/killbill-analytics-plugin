/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.reports.scheduler;

import org.joda.time.DateTime;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.Frequency;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestJobsScheduler extends AnalyticsTestSuiteNoDB {

    private JobsScheduler jobsScheduler;

    @BeforeMethod(groups = "fast")
    public void createScheduler() throws Exception {
        jobsScheduler = new JobsScheduler(logService, killbillDataSource, clock, notificationQueueService);
    }

    @Test(groups = "fast")
    public void testComputeNextRun() throws Exception {
        clock.setTime(new DateTime(2012, 10, 5, 18, 33, 46));
        Assert.assertEquals(computeNextRun(Frequency.DAILY, null).compareTo(new DateTime(2012, 10, 6, 6, 0, 0)), 0);
        Assert.assertEquals(computeNextRun(Frequency.DAILY, 20).compareTo(new DateTime(2012, 10, 5, 20, 0, 0)), 0);
        // Will still run on that day
        Assert.assertEquals(computeNextRun(Frequency.DAILY, 18).compareTo(new DateTime(2012, 10, 5, 18, 0, 0)), 0);
        Assert.assertEquals(computeNextRun(Frequency.DAILY, 17).compareTo(new DateTime(2012, 10, 6, 17, 0, 0)), 0);
        Assert.assertEquals(computeNextRun(Frequency.HOURLY, 1).compareTo(new DateTime(2012, 10, 5, 19, 5, 0)), 0);
        Assert.assertEquals(computeNextRun(Frequency.HOURLY, null).compareTo(new DateTime(2012, 10, 5, 19, 5, 0)), 0);
    }

    private DateTime computeNextRun(final Frequency frequency, final Integer refreshHourOfDayGmt) {
        return jobsScheduler.computeNextRun(new AnalyticsReportJob(null, null, null, null, null, frequency, refreshHourOfDayGmt));
    }
}
