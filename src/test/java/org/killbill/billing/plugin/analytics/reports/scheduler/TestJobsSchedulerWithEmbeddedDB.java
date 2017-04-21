/*
 * Copyright 2016 Groupon, Inc
 * Copyright 2016 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.reports.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteWithEmbeddedDB;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.Frequency;
import org.killbill.notificationq.api.NotificationEventWithMetadata;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import org.awaitility.Awaitility;

public class TestJobsSchedulerWithEmbeddedDB extends AnalyticsTestSuiteWithEmbeddedDB {

    private JobsScheduler jobsScheduler;

    @BeforeMethod(groups = "mysql")
    public void createScheduler() throws Exception {
        jobsScheduler = new JobsScheduler(logService, killbillDataSource, clock, notificationQueueService);
        jobsScheduler.start();
    }

    @AfterMethod(groups = "mysql")
    public void stopScheduler() throws Exception {
        if (jobsScheduler != null) {
            jobsScheduler.shutdownNow();
        }
    }

    @Test(groups = "mysql")
    public void testComputeNextRun() throws Exception {
        final ReportsConfigurationModelDao report = new ReportsConfigurationModelDao(1,
                                                                                     "testReport",
                                                                                     "Test report",
                                                                                     ReportsConfigurationModelDao.ReportType.COUNTERS,
                                                                                     "testSourceTable",
                                                                                     "testProcedure",
                                                                                     Frequency.DAILY,
                                                                                     1);
        setupProcedure(false);

        Assert.assertEquals(nbProcedureRuns(), 0);
        Assert.assertEquals(nbScheduledJobs(report), 0);

        jobsScheduler.scheduleNow(report);

        Assert.assertEquals(nbProcedureRuns(), 0);
        Assert.assertEquals(nbScheduledJobs(report), 1);

        waitForNbHistoricRuns(1);

        Assert.assertEquals(nbProcedureRuns(), 1);
        Assert.assertEquals(nbScheduledJobs(report), 1);

        cleanScheduledJobs();
        Assert.assertEquals(nbProcedureRuns(), 1);
        Assert.assertEquals(nbScheduledJobs(report), 0);

        // Simulate a failure
        setupProcedure(true);

        jobsScheduler.scheduleNow(report);

        Assert.assertEquals(nbProcedureRuns(), 1);
        Assert.assertEquals(nbScheduledJobs(report), 1);

        waitForNbHistoricRuns(2);

        Assert.assertEquals(nbProcedureRuns(), 1);
        Assert.assertEquals(nbScheduledJobs(report), 1);
    }

    private void setupProcedure(final boolean shouldFail) {
        dbi.inTransaction(new TransactionCallback<Void>() {
            @Override
            public Void inTransaction(final Handle conn, final TransactionStatus status) throws Exception {
                conn.execute("CREATE TABLE IF NOT EXISTS test_run(num int)");
                conn.execute("DROP PROCEDURE IF EXISTS testProcedure");

                final String tableName = shouldFail ? "testing_fail" : "test_run";
                conn.execute("CREATE PROCEDURE testProcedure()\n" +
                             "BEGIN\n" +
                             "    INSERT INTO " + tableName + " VALUES (1);\n" +
                             "END");
                return null;
            }
        });
    }

    private int nbProcedureRuns() {
        return dbi.inTransaction(new TransactionCallback<Integer>() {
            @Override
            public Integer inTransaction(final Handle conn, final TransactionStatus status) throws Exception {
                return conn.select("select * from test_run").size();
            }
        });
    }

    private void cleanScheduledJobs() {
        dbi.inTransaction(new TransactionCallback<Void>() {
            @Override
            public Void inTransaction(final Handle conn, final TransactionStatus status) throws Exception {
                conn.execute("truncate analytics_notifications;");
                return null;
            }
        });
    }

    private int nbScheduledJobs(final ReportsConfigurationModelDao report) {
        final AnalyticsReportJob reportJob = new AnalyticsReportJob(report);
        return Iterables.<NotificationEventWithMetadata<AnalyticsReportJob>>size(jobsScheduler.getFutureNotificationsForReportJob(reportJob, null));
    }

    private void waitForNbHistoricRuns(final int runNb) throws Exception {
        Awaitility.await().atMost(15, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return nbHistoricJobs() == runNb;
            }
        });
    }

    private int nbHistoricJobs() {
        return dbi.inTransaction(new TransactionCallback<Integer>() {
            @Override
            public Integer inTransaction(final Handle conn, final TransactionStatus status) throws Exception {
                return conn.select("select * from analytics_notifications_history").size();
            }
        });
    }
}
