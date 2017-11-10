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

package org.killbill.billing.plugin.analytics;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.killbill.billing.ObjectType;
import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillLogService;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfigurationHandler;
import org.killbill.billing.plugin.analytics.dao.AllBusinessObjectsDao;
import org.killbill.billing.plugin.analytics.dao.BusinessAccountDao;
import org.killbill.billing.plugin.analytics.dao.BusinessAccountTransitionDao;
import org.killbill.billing.plugin.analytics.dao.BusinessFieldDao;
import org.killbill.billing.plugin.analytics.dao.BusinessInvoiceAndPaymentDao;
import org.killbill.billing.plugin.analytics.dao.BusinessSubscriptionTransitionDao;
import org.killbill.billing.plugin.analytics.dao.CurrencyConversionDao;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.util.api.RecordIdApi;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.CallOrigin;
import org.killbill.billing.util.callcontext.UserType;
import org.killbill.clock.Clock;
import org.killbill.notificationq.DefaultNotificationQueueService;
import org.killbill.notificationq.api.NotificationEvent;
import org.killbill.notificationq.api.NotificationEventWithMetadata;
import org.killbill.notificationq.api.NotificationQueue;
import org.killbill.notificationq.api.NotificationQueueService.NotificationQueueAlreadyExists;
import org.killbill.notificationq.api.NotificationQueueService.NotificationQueueHandler;
import org.osgi.service.log.LogService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import static org.killbill.billing.plugin.analytics.AnalyticsActivator.ANALYTICS_QUEUE_SERVICE;

public class AnalyticsListener implements OSGIKillbillEventDispatcher.OSGIKillbillEventHandler {

    // Delay, in seconds, before starting to refresh data after an event is received. For workflows with lots of successive events
    // for a given account (e.g. create account, add payment method, create payment), this makes sure we have the latest state
    // when starting the refresh (since only the first event will trigger the refresh, all others are ignored).
    private static final String ANALYTICS_REFRESH_DELAY_PROPERTY = "org.killbill.billing.plugin.analytics.refreshDelay";

    // List of account ids to ignore
    @VisibleForTesting
    static final String ANALYTICS_ACCOUNTS_BLACKLIST_PROPERTY = "org.killbill.billing.plugin.analytics.blacklist";
    private static final Splitter BLACKLIST_SPLITTER = Splitter.on(',')
                                                               .trimResults()
                                                               .omitEmptyStrings();
    private final Iterable<String> accountsBlacklist;
    private final int refreshDelaySeconds;
    private final OSGIKillbillLogService logService;
    private final OSGIKillbillAPI osgiKillbillAPI;
    private final OSGIConfigPropertiesService osgiConfigPropertiesService;
    private final BusinessSubscriptionTransitionDao bstDao;
    private final BusinessInvoiceAndPaymentDao binAndBipDao;
    private final BusinessAccountTransitionDao bosDao;
    private final BusinessFieldDao bFieldDao;
    private final AllBusinessObjectsDao allBusinessObjectsDao;
    private final CurrencyConversionDao currencyConversionDao;
    private final NotificationQueue jobQueue;
    private final Clock clock;
    private final AnalyticsConfigurationHandler analyticsConfigurationHandler;

    public AnalyticsListener(final OSGIKillbillLogService logService,
                             final OSGIKillbillAPI osgiKillbillAPI,
                             final OSGIKillbillDataSource osgiKillbillDataSource,
                             final OSGIConfigPropertiesService osgiConfigPropertiesService,
                             final Executor executor,
                             final Clock clock,
                             AnalyticsConfigurationHandler analyticsConfigurationHandler,
                             final DefaultNotificationQueueService notificationQueueService) throws NotificationQueueAlreadyExists {
        this.logService = logService;
        this.osgiKillbillAPI = osgiKillbillAPI;
        this.osgiConfigPropertiesService = osgiConfigPropertiesService;
        this.clock = clock;
        this.analyticsConfigurationHandler = analyticsConfigurationHandler;

        final String refreshDelayMaybeNull = Strings.emptyToNull(osgiConfigPropertiesService.getString(ANALYTICS_REFRESH_DELAY_PROPERTY));
        this.refreshDelaySeconds = refreshDelayMaybeNull == null ? 10 : Integer.valueOf(refreshDelayMaybeNull);

        final BusinessAccountDao bacDao = new BusinessAccountDao(logService, osgiKillbillDataSource);
        this.bstDao = new BusinessSubscriptionTransitionDao(logService, osgiKillbillDataSource, bacDao, executor);
        this.binAndBipDao = new BusinessInvoiceAndPaymentDao(logService, osgiKillbillDataSource, bacDao, executor);
        this.bosDao = new BusinessAccountTransitionDao(logService, osgiKillbillDataSource);
        this.bFieldDao = new BusinessFieldDao(logService, osgiKillbillDataSource);
        this.allBusinessObjectsDao = new AllBusinessObjectsDao(logService, osgiKillbillAPI, osgiKillbillDataSource, executor, clock);
        this.currencyConversionDao = new CurrencyConversionDao(logService, osgiKillbillDataSource);

        final NotificationQueueHandler notificationQueueHandler = new NotificationQueueHandler() {

            @Override
            public void handleReadyNotification(final NotificationEvent eventJson, final DateTime eventDateTime, final UUID futureUserToken, final Long searchKey1, final Long searchKey2) {
                if (eventJson == null || !(eventJson instanceof AnalyticsJob)) {
                    logService.log(LogService.LOG_ERROR, "Analytics service received an unexpected event: " + eventJson);
                    return;
                }

                final AnalyticsJob job = (AnalyticsJob) eventJson;

                // We need to check again if there is a duplicate because it's possible that 2 events were processed at the same time in handleKillbillEvent (e.g. ACCOUNT_CREATION and ACCOUNT_CHANGE)
                if (!shouldRun(job, futureUserToken, searchKey1, searchKey2)) {
                    logService.log(LogService.LOG_DEBUG, "Skipping already present notification for job " + job.toString());
                    return;
                }

                try {
                    handleAnalyticsJob(job);
                } catch (AnalyticsRefreshException e) {
                    logService.log(LogService.LOG_ERROR, "Unable to process event", e);
                }
            }
        };
        jobQueue = notificationQueueService.createNotificationQueue(ANALYTICS_QUEUE_SERVICE,
                                                                    "refresh-queue",
                                                                    notificationQueueHandler);
        accountsBlacklist = BLACKLIST_SPLITTER.split(Strings.nullToEmpty(osgiConfigPropertiesService.getString(ANALYTICS_ACCOUNTS_BLACKLIST_PROPERTY)));
    }

    public void start() {
        jobQueue.startQueue();
    }

    public void shutdownNow() {
        jobQueue.stopQueue();
    }

    @Override
    public void handleKillbillEvent(final ExtBusEvent killbillEvent) {
        // Ignore non account-specific events (e.g. TENANT_CONFIG_CHANGE)
        if (killbillEvent.getAccountId() == null) {
            return;
        }

        // Don't mirror accounts in the blacklist
        if (isAccountBlacklisted(killbillEvent.getAccountId())) {
            return;
        }

        final AnalyticsJob job = new AnalyticsJob(killbillEvent);

        Long accountRecordId = null;
        Long tenantRecordId = null;
        final RecordIdApi recordIdApi = osgiKillbillAPI.getRecordIdApi();
        if (recordIdApi == null) {
            logService.log(LogService.LOG_WARNING, "Unable to retrieve the recordIdApi");
        } else {
            final CallContext callContext = new AnalyticsCallContext(job, clock);
            accountRecordId = osgiKillbillAPI.getRecordIdApi().getRecordId(killbillEvent.getAccountId(), ObjectType.ACCOUNT, callContext);
            tenantRecordId = osgiKillbillAPI.getRecordIdApi().getRecordId(killbillEvent.getTenantId(), ObjectType.TENANT, callContext);
        }

        // We check for duplicates here to avoid triggering useless refreshes. Note that because multiple bus_ext_events threads
        // are calling handleKillbillEvent in parallel, there is a small chance that this check will miss some, so we will check again
        // before processing the job (see handleReadyNotification above)
        if (accountRecordId != null && futureOverlappingJobAlreadyScheduled(job, accountRecordId, tenantRecordId)) {
            logService.log(LogService.LOG_DEBUG, "Skipping already present notification for event " + killbillEvent.toString());
            return;
        }

        try {
            jobQueue.recordFutureNotification(computeFutureNotificationTime(), job, UUID.randomUUID(), accountRecordId, tenantRecordId);
        } catch (IOException e) {
            logService.log(LogService.LOG_WARNING, "Unable to record notification for event " + killbillEvent.toString());
        }
    }

    // Is there already a future notification overlapping this new job?
    private boolean futureOverlappingJobAlreadyScheduled(final AnalyticsJob newJob, final Long accountRecordId, final Long tenantRecordId) {
        // We don't look at IN_PROCESSING notifications here, as we want to make sure the latest state is refreshed
        final Iterable<NotificationEventWithMetadata<AnalyticsJob>> futureNotificationForSearchKeys = jobQueue.getFutureNotificationForSearchKeys(accountRecordId, tenantRecordId);
        final Iterable<NotificationEventWithMetadata<AnalyticsJob>> scheduledOverlappingJobs = findScheduledOverlappingJobs(newJob, futureNotificationForSearchKeys);
        return scheduledOverlappingJobs.iterator().hasNext();
    }

    // Should this IN_PROCESSING job actually run?
    private boolean shouldRun(final AnalyticsJob inProcessingJob, final UUID existingJobUserToken, final Long accountRecordId, final Long tenantRecordId) {
        final Iterable<NotificationEventWithMetadata<AnalyticsJob>> futureNotificationForSearchKeys = jobQueue.getFutureOrInProcessingNotificationForSearchKeys(accountRecordId, tenantRecordId);
        final Iterable<NotificationEventWithMetadata<AnalyticsJob>> scheduledOverlappingJobs = findScheduledOverlappingJobs(inProcessingJob, futureNotificationForSearchKeys);

        NotificationEventWithMetadata runningJobToRun = null;
        for (final NotificationEventWithMetadata<AnalyticsJob> runningJob : scheduledOverlappingJobs) {
            if (runningJobToRun == null || runningJob.getRecordId() > runningJobToRun.getRecordId()) {
                runningJobToRun = runningJob;
            }
        }

        return runningJobToRun == null || runningJobToRun.getFutureUserToken().equals(existingJobUserToken);
    }

    private Iterable<NotificationEventWithMetadata<AnalyticsJob>> findScheduledOverlappingJobs(final AnalyticsJob job, final Iterable<NotificationEventWithMetadata<AnalyticsJob>> existingScheduledJobs) {
        return Iterables.<NotificationEventWithMetadata<AnalyticsJob>>filter(existingScheduledJobs,
                                                                             new Predicate<NotificationEventWithMetadata<AnalyticsJob>>() {
                                                                                 @Override
                                                                                 public boolean apply(final NotificationEventWithMetadata<AnalyticsJob> notificationEvent) {
                                                                                     return jobsOverlap(job, notificationEvent);
                                                                                 }
                                                                             }
                                                                            );
    }

    // Does the specified existing notification overlap with this job?
    private boolean jobsOverlap(final AnalyticsJob job, final NotificationEventWithMetadata<AnalyticsJob> notificationEventForExistingJob) {
        final AnalyticsJob existingJob = notificationEventForExistingJob.getEvent();
        final AnalyticsJobHierarchy.Group existingHierarchyGroup = AnalyticsJobHierarchy.fromEventType(existingJob.getEventType());

        return existingJob.getAccountId().equals(job.getAccountId()) &&
               (existingHierarchyGroup.equals(AnalyticsJobHierarchy.fromEventType(job.getEventType())) ||
                AnalyticsJobHierarchy.Group.ALL.equals(existingHierarchyGroup));
    }

    private void handleAnalyticsJob(final AnalyticsJob job) throws AnalyticsRefreshException {
        if (job.getEventType() == null) {
            return;
        }

        final CallContext callContext = new AnalyticsCallContext(job, clock);
        final BusinessContextFactory businessContextFactory = new BusinessContextFactory(job.getAccountId(), callContext, currencyConversionDao, osgiKillbillAPI, osgiConfigPropertiesService, clock, analyticsConfigurationHandler);

        logService.log(LogService.LOG_INFO, "Refreshing Analytics data for account " + businessContextFactory.getAccountId());
        switch (AnalyticsJobHierarchy.fromEventType(job.getEventType())) {
            case ALL:
                allBusinessObjectsDao.update(businessContextFactory);
                break;
            case SUBSCRIPTIONS:
                bstDao.update(businessContextFactory);
                break;
            case OVERDUE:
                bosDao.update(businessContextFactory);
                break;
            case INVOICE_AND_PAYMENTS:
                binAndBipDao.update(businessContextFactory);
                break;
            case FIELDS:
                bFieldDao.update(businessContextFactory);
                break;
            case OTHER:
            default:
                break;
        }
    }

    private DateTime computeFutureNotificationTime() {
        return clock.getUTCNow().plusSeconds(refreshDelaySeconds);
    }

    @VisibleForTesting
    protected boolean isAccountBlacklisted(@Nullable final UUID accountId) {
        return accountId != null && Iterables.find(accountsBlacklist, Predicates.<String>equalTo(accountId.toString()), null) != null;
    }

    private static final class AnalyticsCallContext implements CallContext {

        private static final String USER_NAME = AnalyticsListener.class.getName();

        private final AnalyticsJob job;
        private final DateTime now;

        private AnalyticsCallContext(final AnalyticsJob job, final Clock clock) {
            this.job = job;
            this.now = clock.getUTCNow();
        }

        @Override
        public UUID getUserToken() {
            return UUID.randomUUID();
        }

        @Override
        public String getUserName() {
            return USER_NAME;
        }

        @Override
        public CallOrigin getCallOrigin() {
            return CallOrigin.INTERNAL;
        }

        @Override
        public UserType getUserType() {
            return UserType.SYSTEM;
        }

        @Override
        public String getReasonCode() {
            return job.getEventType().toString();
        }

        @Override
        public String getComments() {
            return "eventType=" + job.getEventType() + ", objectType="
                   + job.getObjectType() + ", objectId=" + job.getObjectId() + ", accountId="
                   + job.getAccountId() + ", tenantId=" + job.getTenantId();
        }

        @Override
        public DateTime getCreatedDate() {
            return now;
        }

        @Override
        public DateTime getUpdatedDate() {
            return now;
        }

        @Override
        public UUID getAccountId() {
            return job.getAccountId();
        }

        @Override
        public UUID getTenantId() {
            return job.getTenantId();
        }
    }

    @VisibleForTesting
    NotificationQueue getJobQueue() {
        return jobQueue;
    }
}
