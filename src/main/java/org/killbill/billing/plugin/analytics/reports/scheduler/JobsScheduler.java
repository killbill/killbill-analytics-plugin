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

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Ordering;
import org.joda.time.DateTime;
import org.killbill.billing.plugin.analytics.dao.BusinessDBIProvider;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.Frequency;
import org.killbill.clock.Clock;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;
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
import org.skife.jdbi.v2.sqlobject.mixins.Transmogrifier;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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

    public void start() {
        jobQueue.startQueue();
    }

    public void shutdownNow() {
        jobQueue.stopQueue();
    }

    public void scheduleNow(final ReportsConfigurationModelDao report) {
        final AnalyticsReportJob eventJson = new AnalyticsReportJob(report);
        schedule(eventJson, clock.getUTCNow(), null);
    }

    public void schedule(final ReportsConfigurationModelDao report, final Transmogrifier transmogrifier) {
        final AnalyticsReportJob eventJson = new AnalyticsReportJob(report);
        schedule(eventJson, transmogrifier);
    }

    public void unSchedule(final ReportsConfigurationModelDao report, final Transmogrifier transmogrifier) {
        final AnalyticsReportJob eventJson = new AnalyticsReportJob(report);
        for (final NotificationEventWithMetadata<AnalyticsReportJob> notification : getFutureNotificationsForReportJob(eventJson, transmogrifier)) {
            jobQueue.removeNotificationFromTransaction(transmogrifier, notification.getRecordId());
        }
    }

    public List<AnalyticsReportJob> schedules() {
        final List<AnalyticsReportJob> schedules = new LinkedList<AnalyticsReportJob>();
        for (final NotificationEventWithMetadata<AnalyticsReportJob> notification : getFutureNotifications(null)) {
            schedules.add(notification.getEvent());
        }
        return ANALYTICS_REPORT_JOB_ORDERING.immutableSortedCopy(schedules);
    }

    private List<NotificationEventWithMetadata<AnalyticsReportJob>> getFutureNotifications(@Nullable Transmogrifier transmogrifier) {
        if (transmogrifier == null) {
            return jobQueue.getFutureNotificationForSearchKey2(AnalyticsReportJob.class, JOBS_SCHEDULER_VERSION);
        } else {
            return jobQueue.getFutureNotificationFromTransactionForSearchKey2(AnalyticsReportJob.class, JOBS_SCHEDULER_VERSION, transmogrifier);
        }
    }

    private Iterable<NotificationEventWithMetadata<AnalyticsReportJob>> getFutureNotificationsForReportJob(final AnalyticsReportJob reportJob, @Nullable Transmogrifier transmogrifier) {
        final Integer eventJsonRecordId = reportJob.getRecordId();
        if (eventJsonRecordId != null) {
            // Fast search path
            if (transmogrifier == null) {
                return jobQueue.getFutureNotificationForSearchKey1(AnalyticsReportJob.class, Long.valueOf(eventJsonRecordId));
            } else {
                return jobQueue.getFutureNotificationFromTransactionForSearchKey1(AnalyticsReportJob.class, Long.valueOf(eventJsonRecordId), transmogrifier);
            }
        } else {
            // Slow search path
            return Iterables.<NotificationEventWithMetadata<AnalyticsReportJob>>filter(getFutureNotifications(transmogrifier),
                                                                                       new Predicate<NotificationEventWithMetadata<AnalyticsReportJob>>() {
                                                                                           @Override
                                                                                           public boolean apply(final NotificationEventWithMetadata<AnalyticsReportJob> existingJob) {
                                                                                               return existingJob.getEvent().equalsNoRecordId(reportJob);
                                                                                           }
                                                                                       }
                                                                                      );
        }
    }

    private void schedule(final AnalyticsReportJob eventJson, @Nullable final Transmogrifier transmogrifier) {
        // Verify we don't already have a job for that report
        if (getFutureNotificationsForReportJob(eventJson, transmogrifier).iterator().hasNext()) {
            logService.log(LogService.LOG_DEBUG, "Skipping already present job for report " + eventJson.toString());
            return;
        }

        final DateTime nextRun = computeNextRun(eventJson);
        logService.log(LogService.LOG_INFO, "Next run for report " + eventJson.getReportName() + " will be at " + nextRun);
        schedule(eventJson, nextRun, transmogrifier);
    }

    private void schedule(final AnalyticsReportJob eventJson, final DateTime nextRun, @Nullable final Transmogrifier transmogrifier) {
        try {
            if (transmogrifier == null) {
                jobQueue.recordFutureNotification(nextRun, eventJson, UUID.randomUUID(), Long.valueOf(eventJson.getRecordId()), JOBS_SCHEDULER_VERSION);
            } else {
                jobQueue.recordFutureNotificationFromTransaction(transmogrifier, nextRun, eventJson, UUID.randomUUID(), Long.valueOf(eventJson.getRecordId()), JOBS_SCHEDULER_VERSION);
            }
        } catch (IOException e) {
            logService.log(LogService.LOG_WARNING, "Unable to record notification for report " + eventJson.toString());
        }
    }

    @VisibleForTesting
    DateTime computeNextRun(final AnalyticsReportJob report) {
        if (Frequency.HOURLY.equals(report.getRefreshFrequency())) {
            // 5' past the hour (fixed to avoid drifts)
            return clock.getUTCNow().plusHours(1).withMinuteOfHour(5).withSecondOfMinute(0).withMillisOfSecond(0);
        } else if (Frequency.DAILY.equals(report.getRefreshFrequency())) {
            // 6am GMT by default
            final Integer hourOfTheDayGMT = Objects.firstNonNull(report.getRefreshHourOfDayGmt(), 6);
            if (clock.getUTCNow().getHourOfDay() > hourOfTheDayGMT) {
                // We missed it for today
                return clock.getUTCNow().plusDays(1).withHourOfDay(hourOfTheDayGMT).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else {
                return clock.getUTCNow().withHourOfDay(hourOfTheDayGMT).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }
        } else {
            // Run now
            return clock.getUTCNow();
        }
    }

    private void callStoredProcedure(final String storedProcedureName) {
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
}
