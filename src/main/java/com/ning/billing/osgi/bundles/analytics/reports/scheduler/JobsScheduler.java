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

package com.ning.billing.osgi.bundles.analytics.reports.scheduler;

import java.io.IOException;
import java.util.UUID;

import org.joda.time.DateTime;
import org.osgi.service.log.LogService;
import org.skife.jdbi.v2.Call;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;

import com.ning.billing.clock.Clock;
import com.ning.billing.notificationq.DefaultNotificationQueueService;
import com.ning.billing.notificationq.api.NotificationEvent;
import com.ning.billing.notificationq.api.NotificationEventWithMetadata;
import com.ning.billing.notificationq.api.NotificationQueue;
import com.ning.billing.notificationq.api.NotificationQueueService.NotificationQueueAlreadyExists;
import com.ning.billing.notificationq.api.NotificationQueueService.NotificationQueueHandler;
import com.ning.billing.osgi.bundles.analytics.dao.BusinessDBIProvider;
import com.ning.billing.osgi.bundles.analytics.reports.configuration.ReportsConfigurationModelDao;
import com.ning.billing.osgi.bundles.analytics.reports.configuration.ReportsConfigurationModelDao.Frequency;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillLogService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.ning.billing.osgi.bundles.analytics.AnalyticsActivator.ANALYTICS_QUEUE_SERVICE;

public class JobsScheduler {

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
                    schedule(job);
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
        schedule(eventJson, clock.getUTCNow());
    }

    public void schedule(final ReportsConfigurationModelDao report) {
        final AnalyticsReportJob eventJson = new AnalyticsReportJob(report);
        schedule(eventJson);
    }

    private void schedule(final AnalyticsReportJob eventJson) {
        // Verify we don't already have a job for that report
        if (Iterables.<NotificationEventWithMetadata<AnalyticsReportJob>>tryFind(jobQueue.getFutureNotificationForSearchKey1(AnalyticsReportJob.class, Long.valueOf(eventJson.getRecordId())),
                                                                                 new Predicate<NotificationEventWithMetadata<AnalyticsReportJob>>() {
                                                                                     @Override
                                                                                     public boolean apply(final NotificationEventWithMetadata<AnalyticsReportJob> notificationEvent) {
                                                                                         return notificationEvent.getEvent().equals(eventJson);
                                                                                     }
                                                                                 }).isPresent()) {
            logService.log(LogService.LOG_DEBUG, "Skipping already present job for report " + eventJson.toString());
            return;
        }

        final DateTime nextRun = computeNextRun(eventJson);
        logService.log(LogService.LOG_INFO, "Next run for report " + eventJson.getReportName() + " will be at " + nextRun);
        schedule(eventJson, nextRun);
    }

    private void schedule(final AnalyticsReportJob eventJson, final DateTime nextRun) {
        try {
            jobQueue.recordFutureNotification(nextRun, eventJson, UUID.randomUUID(), Long.valueOf(eventJson.getRecordId()), null);
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
            final Call call = handle.createCall(storedProcedureName);
            call.invoke();
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
    }
}
