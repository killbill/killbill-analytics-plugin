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

package com.ning.billing.osgi.bundles.analytics;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.osgi.service.log.LogService;
import org.skife.config.ConfigurationObjectFactory;
import org.skife.jdbi.v2.DBI;

import com.ning.billing.ObjectType;
import com.ning.billing.clock.Clock;
import com.ning.billing.clock.DefaultClock;
import com.ning.billing.notification.plugin.api.ExtBusEvent;
import com.ning.billing.notificationq.DefaultNotificationQueueService;
import com.ning.billing.notificationq.api.NotificationEvent;
import com.ning.billing.notificationq.api.NotificationEventWithMetadata;
import com.ning.billing.notificationq.api.NotificationQueue;
import com.ning.billing.notificationq.api.NotificationQueueConfig;
import com.ning.billing.notificationq.api.NotificationQueueService.NotificationQueueAlreadyExists;
import com.ning.billing.notificationq.api.NotificationQueueService.NotificationQueueHandler;
import com.ning.billing.osgi.bundles.analytics.dao.AllBusinessObjectsDao;
import com.ning.billing.osgi.bundles.analytics.dao.BusinessAccountDao;
import com.ning.billing.osgi.bundles.analytics.dao.BusinessDBIProvider;
import com.ning.billing.osgi.bundles.analytics.dao.BusinessFieldDao;
import com.ning.billing.osgi.bundles.analytics.dao.BusinessInvoiceAndInvoicePaymentDao;
import com.ning.billing.osgi.bundles.analytics.dao.BusinessOverdueStatusDao;
import com.ning.billing.osgi.bundles.analytics.dao.BusinessSubscriptionTransitionDao;
import com.ning.billing.util.api.RecordIdApi;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.callcontext.CallOrigin;
import com.ning.billing.util.callcontext.UserType;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillEventDispatcher.OSGIKillbillEventHandler;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillLogService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class AnalyticsListener implements OSGIKillbillEventHandler {

    // List of account ids to ignore
    static final String ANALYTICS_ACCOUNTS_BLACKLIST_PROPERTY = "killbill.osgi.analytics.blacklist";
    private static final Splitter BLACKLIST_SPLITTER = Splitter.on(',')
                                                               .trimResults()
                                                               .omitEmptyStrings();
    private final Iterable<String> accountsBlacklist;

    private final LogService logService;
    private final OSGIKillbillAPI osgiKillbillAPI;
    private final BusinessSubscriptionTransitionDao bstDao;
    private final BusinessInvoiceAndInvoicePaymentDao binAndBipDao;
    private final BusinessOverdueStatusDao bosDao;
    private final BusinessFieldDao bFieldDao;
    private final AllBusinessObjectsDao allBusinessObjectsDao;
    private final NotificationQueue jobQueue;

    private final Clock clock = new DefaultClock();

    public AnalyticsListener(final OSGIKillbillLogService logService,
                             final OSGIKillbillAPI osgiKillbillAPI,
                             final OSGIKillbillDataSource osgiKillbillDataSource,
                             final Executor executor) throws NotificationQueueAlreadyExists {
        this(logService, osgiKillbillAPI, osgiKillbillDataSource, executor, System.getProperties());
    }

    AnalyticsListener(final OSGIKillbillLogService logService,
                      final OSGIKillbillAPI osgiKillbillAPI,
                      final OSGIKillbillDataSource osgiKillbillDataSource,
                      final Executor executor,
                      final Properties properties) throws NotificationQueueAlreadyExists {
        this.logService = logService;
        this.osgiKillbillAPI = osgiKillbillAPI;

        final BusinessAccountDao bacDao = new BusinessAccountDao(logService, osgiKillbillAPI, osgiKillbillDataSource);
        this.bstDao = new BusinessSubscriptionTransitionDao(logService, osgiKillbillAPI, osgiKillbillDataSource, bacDao, executor);
        this.binAndBipDao = new BusinessInvoiceAndInvoicePaymentDao(logService, osgiKillbillAPI, osgiKillbillDataSource, bacDao, executor);
        this.bosDao = new BusinessOverdueStatusDao(logService, osgiKillbillAPI, osgiKillbillDataSource, executor);
        this.bFieldDao = new BusinessFieldDao(logService, osgiKillbillAPI, osgiKillbillDataSource);
        this.allBusinessObjectsDao = new AllBusinessObjectsDao(logService, osgiKillbillAPI, osgiKillbillDataSource, executor);

        final NotificationQueueConfig config = new ConfigurationObjectFactory(properties).build(NotificationQueueConfig.class);
        final DBI dbi = BusinessDBIProvider.get(osgiKillbillDataSource.getDataSource());
        final DefaultNotificationQueueService notificationQueueService = new DefaultNotificationQueueService(dbi, clock, config);
        final NotificationQueueHandler notificationQueueHandler = new NotificationQueueHandler() {

            @Override
            public void handleReadyNotification(final NotificationEvent eventJson, final DateTime eventDateTime, final UUID userToken, final Long searchKey1, final Long searchKey2) {
                if (eventJson == null || !(eventJson instanceof AnalyticsJob)) {
                    logService.log(LogService.LOG_ERROR, "Analytics service received an unexpected event: " + eventJson);
                    return;
                }

                final AnalyticsJob job = (AnalyticsJob) eventJson;
                try {
                    handleAnalyticsJob(job);
                } catch (AnalyticsRefreshException e) {
                    logService.log(LogService.LOG_ERROR, "Unable to process event", e);
                }
            }
        };
        jobQueue = notificationQueueService.createNotificationQueue("AnalyticsService",
                                                                    "refresh-queue",
                                                                    notificationQueueHandler);
        accountsBlacklist = BLACKLIST_SPLITTER.split(properties.getProperty(ANALYTICS_ACCOUNTS_BLACKLIST_PROPERTY, ""));
    }

    public void start() {
        jobQueue.startQueue();
    }

    public void shutdownNow() {
        jobQueue.stopQueue();
    }

    @Override
    public void handleKillbillEvent(final ExtBusEvent killbillEvent) {
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

        if (accountRecordId != null) {
            // Verify if we don't have a notification for that type and account already.
            // If we do, no need to insert another one since we will do a full refresh anyways
            if (Iterables.<NotificationEventWithMetadata<AnalyticsJob>>tryFind(jobQueue.getFutureNotificationForSearchKey1(AnalyticsJob.class, accountRecordId),
                                                                               new Predicate<NotificationEventWithMetadata<AnalyticsJob>>() {
                                                                                   @Override
                                                                                   public boolean apply(final NotificationEventWithMetadata<AnalyticsJob> notificationEvent) {
                                                                                       return notificationEvent.getEvent().equals(job);
                                                                                   }
                                                                               }).isPresent()) {
                logService.log(LogService.LOG_DEBUG, "Skipping already present notification for event " + killbillEvent.toString());
                return;
            }
        }

        try {
            jobQueue.recordFutureNotification(clock.getUTCNow(), job, UUID.randomUUID(), accountRecordId, tenantRecordId);
        } catch (IOException e) {
            logService.log(LogService.LOG_WARNING, "Unable to record notification for event " + killbillEvent.toString());
        }
    }

    private void handleAnalyticsJob(final AnalyticsJob job) throws AnalyticsRefreshException {
        if (job.getEventType() == null) {
            return;
        }

        final CallContext callContext = new AnalyticsCallContext(job, clock);
        switch (job.getEventType()) {
            case ACCOUNT_CREATION:
            case ACCOUNT_CHANGE:
                // Note: account information is denormalized across all tables, we pretty much
                // have to refresh all objects
                allBusinessObjectsDao.update(job.getAccountId(), callContext);
                break;
            case SUBSCRIPTION_CREATION:
            case SUBSCRIPTION_CHANGE:
            case SUBSCRIPTION_CANCEL:
            case SUBSCRIPTION_PHASE:
            case SUBSCRIPTION_UNCANCEL:
                bstDao.update(job.getAccountId(), callContext);
                break;
            case OVERDUE_CHANGE:
                bosDao.update(job.getAccountId(), callContext);
                break;
            case INVOICE_CREATION:
            case INVOICE_ADJUSTMENT:
                binAndBipDao.update(job.getAccountId(), callContext);
                break;
            case PAYMENT_SUCCESS:
            case PAYMENT_FAILED:
                binAndBipDao.update(job.getAccountId(), callContext);
                break;
            case TAG_CREATION:
            case TAG_DELETION:
                // Note: tags determine the report group. Since it is denormalized across all tables, we pretty much
                // have to refresh all objects
                allBusinessObjectsDao.update(job.getAccountId(), callContext);
                break;
            case CUSTOM_FIELD_CREATION:
            case CUSTOM_FIELD_DELETION:
                bFieldDao.update(job.getAccountId(), callContext);
                break;
            default:
                break;
        }
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
        public UUID getTenantId() {
            return job.getTenantId();
        }
    }

    @VisibleForTesting
    NotificationQueue getJobQueue() {
        return jobQueue;
    }
}
