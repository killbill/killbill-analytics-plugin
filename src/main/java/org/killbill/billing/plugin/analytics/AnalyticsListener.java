/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2021 Equinix, Inc
 * Copyright 2014-2021 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.killbill.billing.ObjectType;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceApiException;
import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;
import org.killbill.billing.plugin.analytics.AnalyticsJobHierarchy.Group;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfigurationHandler;
import org.killbill.billing.plugin.analytics.dao.AllBusinessObjectsDao;
import org.killbill.billing.plugin.analytics.dao.BusinessAccountDao;
import org.killbill.billing.plugin.analytics.dao.BusinessAccountTransitionDao;
import org.killbill.billing.plugin.analytics.dao.BusinessFieldDao;
import org.killbill.billing.plugin.analytics.dao.BusinessInvoiceAndPaymentDao;
import org.killbill.billing.plugin.analytics.dao.BusinessInvoiceDao;
import org.killbill.billing.plugin.analytics.dao.BusinessSubscriptionTransitionDao;
import org.killbill.billing.plugin.analytics.dao.CurrencyConversionDao;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.plugin.api.PluginTenantContext;
import org.killbill.billing.util.api.RecordIdApi;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.CallOrigin;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.callcontext.UserType;
import org.killbill.clock.Clock;
import org.killbill.commons.locker.GlobalLock;
import org.killbill.commons.locker.GlobalLocker;
import org.killbill.commons.locker.LockFailedException;
import org.killbill.notificationq.DefaultNotificationQueueService;
import org.killbill.notificationq.api.NotificationEvent;
import org.killbill.notificationq.api.NotificationEventWithMetadata;
import org.killbill.notificationq.api.NotificationQueue;
import org.killbill.notificationq.api.NotificationQueueService.NotificationQueueAlreadyExists;
import org.killbill.notificationq.api.NotificationQueueService.NotificationQueueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import static org.killbill.billing.notification.plugin.api.ExtBusEventType.PAYMENT_SUCCESS;
import static org.killbill.billing.plugin.analytics.AnalyticsActivator.ANALYTICS_QUEUE_SERVICE;

public class AnalyticsListener implements OSGIKillbillEventDispatcher.OSGIKillbillEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsListener.class);

    private final OSGIKillbillAPI osgiKillbillAPI;
    private final OSGIConfigPropertiesService osgiConfigPropertiesService;
    private final BusinessSubscriptionTransitionDao bstDao;
    private final BusinessInvoiceDao binDao;
    private final BusinessInvoiceAndPaymentDao binAndBipDao;
    private final BusinessAccountTransitionDao bosDao;
    private final BusinessFieldDao bFieldDao;
    private final AllBusinessObjectsDao allBusinessObjectsDao;
    private final CurrencyConversionDao currencyConversionDao;
    private final NotificationQueue jobQueue;
    private final GlobalLocker locker;
    private final Clock clock;
    private final AnalyticsConfigurationHandler analyticsConfigurationHandler;

    public AnalyticsListener(final OSGIKillbillAPI osgiKillbillAPI,
                             final OSGIKillbillDataSource osgiKillbillDataSource,
                             final OSGIConfigPropertiesService osgiConfigPropertiesService,
                             final Executor executor,
                             final GlobalLocker locker,
                             final Clock clock,
                             final AnalyticsConfigurationHandler analyticsConfigurationHandler,
                             final DefaultNotificationQueueService notificationQueueService) throws NotificationQueueAlreadyExists {
        this.osgiKillbillAPI = osgiKillbillAPI;
        this.osgiConfigPropertiesService = osgiConfigPropertiesService;
        this.locker = locker;
        this.clock = clock;
        this.analyticsConfigurationHandler = analyticsConfigurationHandler;

        final BusinessAccountDao bacDao = new BusinessAccountDao(osgiKillbillDataSource);
        this.bstDao = new BusinessSubscriptionTransitionDao(osgiKillbillDataSource, bacDao, executor);
        this.binDao = new BusinessInvoiceDao(osgiKillbillDataSource, bacDao, executor);
        this.binAndBipDao = new BusinessInvoiceAndPaymentDao(osgiKillbillDataSource, bacDao, executor);
        this.bosDao = new BusinessAccountTransitionDao(osgiKillbillDataSource);
        this.bFieldDao = new BusinessFieldDao(osgiKillbillDataSource);
        this.allBusinessObjectsDao = new AllBusinessObjectsDao(osgiKillbillDataSource, executor);
        this.currencyConversionDao = new CurrencyConversionDao(osgiKillbillDataSource);

        final NotificationQueueHandler notificationQueueHandler = new NotificationQueueHandler() {

            @Override
            public void handleReadyNotification(final NotificationEvent eventJson, final DateTime eventDateTime, final UUID futureUserToken, final Long searchKey1, final Long searchKey2) {
                if (eventJson == null || !(eventJson instanceof AnalyticsJob)) {
                    logger.error("Analytics service received an unexpected event: {}", eventJson);
                    return;
                }

                final AnalyticsJob job = (AnalyticsJob) eventJson;

                // We need to check again if there is a duplicate because it's possible that 2 events were processed at the same time in handleKillbillEvent (e.g. ACCOUNT_CREATION and ACCOUNT_CHANGE)
                if (!shouldRun(job, futureUserToken, searchKey1, searchKey2)) {
                    logger.debug("Skipping already present notification for job {}", job);
                    return;
                }

                try {
                    handleAnalyticsJob(job);
                } catch (final AnalyticsRefreshException e) {
                    logger.error("Unable to process event", e);
                }
            }
        };
        jobQueue = notificationQueueService.createNotificationQueue(ANALYTICS_QUEUE_SERVICE,
                                                                    "refresh-queue",
                                                                    notificationQueueHandler);
    }

    public void start() {
        jobQueue.startQueue();
    }

    public void shutdownNow() {
        jobQueue.stopQueue();
    }

    public boolean isStarted() {
        return jobQueue.isStarted();
    }

    @Override
    public void handleKillbillEvent(final ExtBusEvent killbillEvent) {
        // Ignore non account-specific events (e.g. TENANT_CONFIG_CHANGE)
        if (killbillEvent.getAccountId() == null) {
            return;
        }

        // Don't mirror accounts in the blacklist
        if (isAccountBlacklisted(killbillEvent.getAccountId(), killbillEvent.getTenantId())) {
            return;
        }

        AnalyticsJob job = new AnalyticsJob(killbillEvent);
        if (AnalyticsJobHierarchy.fromEventType(job) == Group.INVOICES) {
            try {
                final TenantContext tenantContext = new PluginTenantContext(killbillEvent.getAccountId(), killbillEvent.getTenantId());
                final Invoice invoice = osgiKillbillAPI.getInvoiceUserApi().getInvoice(killbillEvent.getObjectId(), tenantContext);
                if (BigDecimal.ZERO.compareTo(invoice.getCreditedAmount()) != 0 && invoice.getNumberOfPayments() > 0) {
                    // The invoice has payments and CBA was updated: payment rows must be updated
                    // See https://github.com/killbill/killbill-analytics-plugin/issues/105
                    job = new AnalyticsJob(PAYMENT_SUCCESS,
                                           killbillEvent.getObjectType(),
                                           killbillEvent.getObjectId(),
                                           killbillEvent.getAccountId(),
                                           killbillEvent.getTenantId());
                }
            } catch (final InvoiceApiException e) {
                logger.warn("Unable to retrieve InvoiceUserApi for event {}, payment data might be stale", killbillEvent);
            }
        }

        // Events we don't care about
        if (shouldIgnoreEvent(job)) {
            return;
        }

        Long accountRecordId = null;
        Long tenantRecordId = null;
        final RecordIdApi recordIdApi = osgiKillbillAPI.getRecordIdApi();
        if (recordIdApi == null) {
            logger.warn("Unable to retrieve the recordIdApi");
        } else {
            final CallContext callContext = new AnalyticsCallContext(job, clock);
            accountRecordId = osgiKillbillAPI.getRecordIdApi().getRecordId(killbillEvent.getAccountId(), ObjectType.ACCOUNT, callContext);
            tenantRecordId = osgiKillbillAPI.getRecordIdApi().getRecordId(killbillEvent.getTenantId(), ObjectType.TENANT, callContext);
        }

        // We check for duplicates here to avoid triggering useless refreshes. Note that because multiple bus_ext_events threads
        // are calling handleKillbillEvent in parallel, there is a small chance that this check will miss some, so we will check again
        // before processing the job (see handleReadyNotification above)
        if (accountRecordId != null && futureOverlappingJobAlreadyScheduled(job, accountRecordId, tenantRecordId)) {
            logger.debug("Skipping already present notification for event {}", killbillEvent.toString());
            return;
        }

        try {
            jobQueue.recordFutureNotification(computeFutureNotificationTime(killbillEvent.getTenantId()), job, UUID.randomUUID(), accountRecordId, tenantRecordId);
        } catch (final IOException e) {
            logger.warn("Unable to record notification for event {}", killbillEvent);
        }
    }

    // Is there already a future notification overlapping this new job?
    private boolean futureOverlappingJobAlreadyScheduled(final AnalyticsJob newJob, final Long accountRecordId, final Long tenantRecordId) {
        // We don't look at IN_PROCESSING notifications here, as we want to make sure the latest state is refreshed
        final Iterable<NotificationEventWithMetadata<AnalyticsJob>> futureNotificationForSearchKeys = jobQueue.getFutureNotificationForSearchKeys(accountRecordId, tenantRecordId);
        final Iterator<NotificationEventWithMetadata<AnalyticsJob>> iterator = futureNotificationForSearchKeys.iterator();
        try {
            final Iterator<NotificationEventWithMetadata<AnalyticsJob>> scheduledOverlappingJobs = findScheduledOverlappingJobs(newJob, iterator);
            return scheduledOverlappingJobs.hasNext();
        } finally {
            // Go through all results to close the connection
            while (iterator.hasNext()) {
                iterator.next();
            }
        }
    }

    // Should this IN_PROCESSING job actually run?
    private boolean shouldRun(final AnalyticsJob inProcessingJob, final UUID existingJobUserToken, final Long accountRecordId, final Long tenantRecordId) {
        final Iterable<NotificationEventWithMetadata<AnalyticsJob>> futureNotificationForSearchKeys = jobQueue.getFutureOrInProcessingNotificationForSearchKeys(accountRecordId, tenantRecordId);
        final Iterator<NotificationEventWithMetadata<AnalyticsJob>> jobsIterator = futureNotificationForSearchKeys.iterator();
        final Iterator<NotificationEventWithMetadata<AnalyticsJob>> iterator = findScheduledOverlappingJobs(inProcessingJob, jobsIterator);

        try {
            NotificationEventWithMetadata runningJobToRun = null;
            while (iterator.hasNext()) {
                final NotificationEventWithMetadata<AnalyticsJob> runningJob = iterator.next();
                if (runningJobToRun == null || runningJob.getRecordId() > runningJobToRun.getRecordId()) {
                    runningJobToRun = runningJob;
                }
            }

            return runningJobToRun == null || runningJobToRun.getFutureUserToken().equals(existingJobUserToken);
        } finally {
            // Go through all results to close the connection
            while (iterator.hasNext()) {
                iterator.next();
            }
        }
    }

    private Iterator<NotificationEventWithMetadata<AnalyticsJob>> findScheduledOverlappingJobs(final AnalyticsJob job, final Iterator<NotificationEventWithMetadata<AnalyticsJob>> existingScheduledJobs) {
        return Iterators.<NotificationEventWithMetadata<AnalyticsJob>>filter(existingScheduledJobs,
                                                                             new Predicate<NotificationEventWithMetadata<AnalyticsJob>>() {
                                                                                 @Override
                                                                                 public boolean apply(final NotificationEventWithMetadata<AnalyticsJob> notificationEvent) {
                                                                                     if (notificationEvent == null) {
                                                                                         return false;
                                                                                     }
                                                                                     return jobsOverlap(job, notificationEvent.getEvent());
                                                                                 }
                                                                             }
                                                                            );
    }

    // Does the specified existing notification overlap with this job?
    @VisibleForTesting
    boolean jobsOverlap(final AnalyticsJob job, final AnalyticsJob existingJob) {
        final AnalyticsJobHierarchy.Group existingHierarchyGroup = AnalyticsJobHierarchy.fromEventType(existingJob);

        if (!existingJob.getAccountId().equals(job.getAccountId())) {
            // Jobs for different accounts, they cannot overlap
            return false;
        } else if (Group.ALL.equals(existingHierarchyGroup)) {
            // A full refresh is already scheduled, the new job overlaps
            return true;
        } else if (existingHierarchyGroup.equals(AnalyticsJobHierarchy.fromEventType(job)) &&
                   job.getObjectId() != null && existingJob.getObjectId() != null && job.getObjectId().equals(existingJob.getObjectId())) {
            // A refresh for the same group and object is already scheduled
            return true;
        } else {
            return false;
        }
    }

    private void handleAnalyticsJob(final AnalyticsJob job) throws AnalyticsRefreshException {
        GlobalLock lock = null;
        try {
            lock = locker.lockWithNumberOfTries("ANALYTICS_REFRESH", job.getAccountId().toString(), analyticsConfigurationHandler.getConfigurable(job.getTenantId()).lockAttemptRetries);
            handleAnalyticsJobWithLock(job);
        } catch (final LockFailedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock != null) {
                lock.release();
            }
        }
    }

    private void handleAnalyticsJobWithLock(final AnalyticsJob job) throws AnalyticsRefreshException {
        if (job.getEventType() == null) {
            return;
        }

        final CallContext callContext = new AnalyticsCallContext(job, clock);
        final BusinessContextFactory businessContextFactory = new BusinessContextFactory(job.getAccountId(), callContext, currencyConversionDao, osgiKillbillAPI, osgiConfigPropertiesService, clock, analyticsConfigurationHandler);

        final Group group = AnalyticsJobHierarchy.fromEventType(job);
        logger.info("Starting {} Analytics refresh for account {}", group, businessContextFactory.getAccountId());
        switch (group) {
            case ALL:
                allBusinessObjectsDao.update(businessContextFactory);
                break;
            case SUBSCRIPTIONS:
                bstDao.update(businessContextFactory);
                break;
            case OVERDUE:
                bosDao.update(businessContextFactory);
                break;
            case INVOICES:
                binDao.update(job.getObjectId(), businessContextFactory);
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
        logger.info("Finished Analytics refresh for account {}", businessContextFactory.getAccountId());
    }

    private DateTime computeFutureNotificationTime(final UUID tenantId) {
        return clock.getUTCNow().plusSeconds(analyticsConfigurationHandler.getConfigurable(tenantId).refreshDelaySeconds);
    }

    @VisibleForTesting
    protected boolean isAccountBlacklisted(@Nullable final UUID accountId, final UUID tenantId) {
        return accountId != null && Iterables.find(analyticsConfigurationHandler.getConfigurable(tenantId).blacklist, Predicates.<String>equalTo(accountId.toString()), null) != null;
    }

    @VisibleForTesting
    protected boolean shouldIgnoreEvent(final AnalyticsJob job) {
        final Iterable<Group> ignoredGroups = Iterables.<String, Group>transform(analyticsConfigurationHandler.getConfigurable(job.getTenantId()).ignoredGroups,
                                                                                 new Function<String, Group>() {
                                                                                     @Override
                                                                                     public Group apply(final String input) {
                                                                                         return input == null ? null : Group.valueOf(input.toUpperCase());
                                                                                     }
                                                                                 });
        return Iterables.find(ignoredGroups, Predicates.<Group>equalTo(AnalyticsJobHierarchy.fromEventType(job)), null) != null;
    }

    @VisibleForTesting
    NotificationQueue getJobQueue() {
        return jobQueue;
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
}
