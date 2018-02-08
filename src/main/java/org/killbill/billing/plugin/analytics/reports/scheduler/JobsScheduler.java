/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2018 Groupon, Inc
 * Copyright 2014-2018 The Billing Project, LLC
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

import java.io.IOException;
import java.sql.Connection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillLogService;
import org.killbill.billing.plugin.analytics.dao.BusinessDBIProvider;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.Frequency;
import org.killbill.clock.Clock;
import org.killbill.commons.concurrent.Executors;
import org.killbill.notificationq.DefaultNotificationQueueService;
import org.killbill.notificationq.api.NotificationEvent;
import org.killbill.notificationq.api.NotificationEventWithMetadata;
import org.killbill.notificationq.api.NotificationQueue;
import org.killbill.notificationq.api.NotificationQueueService.NotificationQueueAlreadyExists;
import org.killbill.notificationq.api.NotificationQueueService.NotificationQueueHandler;
import org.osgi.service.log.LogService;
import org.skife.jdbi.v2.Call;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import static org.killbill.billing.plugin.analytics.AnalyticsActivator.ANALYTICS_QUEUE_SERVICE;

public class JobsScheduler {

    // Current version of the jobs in the notification queue
    // This is useful to retrieve all currently scheduled ones
    private static final Long JOBS_SCHEDULER_VERSION = 1L;

    private static final Ordering<AnalyticsReportJob> ANALYTICS_REPORT_JOB_ORDERING = Ordering.from(new Comparator<AnalyticsReportJob>() {
        @Override
        public int compare(AnalyticsReportJob o1, AnalyticsReportJob o2) {
            return o1.getRecordId().compareTo(o2.getRecordId());
        }
    });

    private final OSGIKillbillLogService logService;
    private final IDBI dbi;
    private final Clock clock;
    private final NotificationQueue jobQueue;

    private ExecutorService proceduresService;

    public JobsScheduler(final OSGIKillbillLogService logService,
                         final OSGIKillbillDataSource osgiKillbillDataSource,
                         final Clock clock,
                         final DefaultNotificationQueueService notificationQueueService) throws NotificationQueueAlreadyExists {
        this.logService = logService;
        this.clock = clock;

        dbi = BusinessDBIProvider.get(osgiKillbillDataSource.getDataSource());
        final NotificationQueueHandler notificationQueueHandler = new NotificationQueueHandler() {

            @Override
            public void handleReadyNotification(final NotificationEvent eventJson, final DateTime eventDateTime, final UUID userToken, final Long searchKey1, final Long searchKey2) {
                if (eventJson == null || !(eventJson instanceof AnalyticsReportJob)) {
                    logService.log(LogService.LOG_ERROR, "Analytics report service received an unexpected event: " + eventJson);
                    return;
                }

                final AnalyticsReportJob job = (AnalyticsReportJob) eventJson;
                logService.log(LogService.LOG_INFO, "Starting job for " + job.getReportName());

                try {
                    callStoredProcedure(job.getRefreshProcedureName());
                } finally {
                    schedule(job, null);
                    logService.log(LogService.LOG_INFO, "Ending job for " + job.getReportName());
                }
            }
        };
        jobQueue = notificationQueueService.createNotificationQueue(ANALYTICS_QUEUE_SERVICE,
                                                                    "reports-jobs",
                                                                    notificationQueueHandler);
    }

    public synchronized void start() {
        proceduresService = Executors.newCachedThreadPool("proceduresService");
        jobQueue.startQueue();
    }

    public synchronized void shutdownNow() {
        if (proceduresService != null) {
            proceduresService.shutdownNow();
            proceduresService = null;
        }
        jobQueue.stopQueue();
    }

    public void scheduleNow(final ReportsConfigurationModelDao report) {
        final AnalyticsReportJob eventJson = new AnalyticsReportJob(report);
        schedule(eventJson, clock.getUTCNow(), null);
    }

    public void schedule(final ReportsConfigurationModelDao report, final Connection connection) {
        final AnalyticsReportJob eventJson = new AnalyticsReportJob(report);
        schedule(eventJson, connection);
    }

    public void unSchedule(final ReportsConfigurationModelDao report, final Connection connection) {
        final AnalyticsReportJob eventJson = new AnalyticsReportJob(report);
        final Iterator<NotificationEventWithMetadata<AnalyticsReportJob>> iterator = getFutureNotificationsForReportJob(eventJson, connection).iterator();
        try {
            while (iterator.hasNext()) {
                final NotificationEventWithMetadata<AnalyticsReportJob> notification = iterator.next();
                jobQueue.removeNotificationFromTransaction(connection, notification.getRecordId());
            }
        } finally {
            // Go through all results to close the connection
            while (iterator.hasNext()) {
                iterator.next();
            }
        }
    }

    public List<AnalyticsReportJob> schedules() {
        final List<AnalyticsReportJob> schedules = new LinkedList<AnalyticsReportJob>();
        final Iterator<NotificationEventWithMetadata<AnalyticsReportJob>> iterator = getFutureNotifications(null).iterator();
        try {
            while (iterator.hasNext()) {
                final NotificationEventWithMetadata<AnalyticsReportJob> notification = iterator.next();
                schedules.add(notification.getEvent());
            }
        } finally {
            // Go through all results to close the connection
            while (iterator.hasNext()) {
                iterator.next();
            }
        }
        return ANALYTICS_REPORT_JOB_ORDERING.immutableSortedCopy(schedules);
    }

    private Iterable<NotificationEventWithMetadata<AnalyticsReportJob>> getFutureNotifications(@Nullable Connection connection) {
        if (connection == null) {
            return jobQueue.getFutureNotificationForSearchKey2(null, JOBS_SCHEDULER_VERSION);
        } else {
            return jobQueue.getFutureNotificationFromTransactionForSearchKey2(null, JOBS_SCHEDULER_VERSION, connection);
        }
    }

    @VisibleForTesting
    Iterable<NotificationEventWithMetadata<AnalyticsReportJob>> getFutureNotificationsForReportJob(final AnalyticsReportJob reportJob, @Nullable Connection connection) {
        final Integer eventJsonRecordId = reportJob.getRecordId();
        if (eventJsonRecordId != null) {
            // Fast search path
            if (connection == null) {
                return jobQueue.getFutureNotificationForSearchKeys(Long.valueOf(eventJsonRecordId), JOBS_SCHEDULER_VERSION);
            } else {
                return jobQueue.getFutureNotificationFromTransactionForSearchKeys(Long.valueOf(eventJsonRecordId), JOBS_SCHEDULER_VERSION, connection);
            }
        } else {
            // Slow search path
            return Iterables.<NotificationEventWithMetadata<AnalyticsReportJob>>filter(getFutureNotifications(connection),
                                                                                       new Predicate<NotificationEventWithMetadata<AnalyticsReportJob>>() {
                                                                                           @Override
                                                                                           public boolean apply(final NotificationEventWithMetadata<AnalyticsReportJob> existingJob) {
                                                                                               return existingJob.getEvent().equalsNoRecordId(reportJob);
                                                                                           }
                                                                                       }
                                                                                      );
        }
    }

    private void schedule(final AnalyticsReportJob eventJson, @Nullable final Connection connection) {
        // Verify we don't already have a job for that report
        // This will go through all results to close the connection
        final int nbFutureNotifications = Iterables.<NotificationEventWithMetadata<AnalyticsReportJob>>size(getFutureNotificationsForReportJob(eventJson, connection));
        if (nbFutureNotifications > 0) {
            logService.log(LogService.LOG_DEBUG, "Skipping already present job for report " + eventJson.toString());
            return;
        }

        final DateTime nextRun = computeNextRun(eventJson);
        logService.log(LogService.LOG_INFO, "Next run for report " + eventJson.getReportName() + " will be at " + nextRun);
        schedule(eventJson, nextRun, connection);
    }

    private void schedule(final AnalyticsReportJob eventJson, final DateTime nextRun, @Nullable final Connection connection) {
        try {
            if (connection == null) {
                jobQueue.recordFutureNotification(nextRun, eventJson, UUID.randomUUID(), Long.valueOf(eventJson.getRecordId()), JOBS_SCHEDULER_VERSION);
            } else {
                jobQueue.recordFutureNotificationFromTransaction(connection, nextRun, eventJson, UUID.randomUUID(), Long.valueOf(eventJson.getRecordId()), JOBS_SCHEDULER_VERSION);
            }
        } catch (IOException e) {
            logService.log(LogService.LOG_WARNING, "Unable to record notification for report " + eventJson.toString());
        }
    }

    @VisibleForTesting
    DateTime computeNextRun(final AnalyticsReportJob report) {

        final DateTime now = clock.getUTCNow();

        if (Frequency.HOURLY.equals(report.getRefreshFrequency())) {
            // 5' past the hour (fixed to avoid drifts)
            return now.plusHours(1).withMinuteOfHour(5).withSecondOfMinute(0).withMillisOfSecond(0);
        } else if (Frequency.DAILY.equals(report.getRefreshFrequency())) {
            // 6am GMT by default
            final Integer hourOfTheDayGMT = MoreObjects.firstNonNull(report.getRefreshHourOfDayGmt(), 6);
            final DateTime boundaryTime = now.withHourOfDay(hourOfTheDayGMT).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            return now.compareTo(boundaryTime) >= 0 ? boundaryTime.plusDays(1) : boundaryTime;
        } else {
            // Run now
            return now;
        }
    }

    private void callStoredProcedure(final String storedProcedureName) {
        if (Strings.isNullOrEmpty(storedProcedureName)) {
            return;
        }

        Preconditions.checkState(proceduresService != null, "proceduresService isn't started yet");

        // Execute the refresh in the background, to avoid having other notifications threads "steal" the IN_PROCESSING entry
        proceduresService.execute(new Runnable() {
            @Override
            public void run() {
                Handle handle = null;
                try {
                    handle = dbi.open();
                    final Call call = handle.createCall("call " + storedProcedureName);
                    call.invoke();
                } finally {
                    if (handle != null) {
                        handle.close();
                    }
                }
            }
        });
    }
}
