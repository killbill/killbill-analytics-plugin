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
import org.killbill.billing.plugin.analytics.dao.AllBusinessObjectsDao;
import org.killbill.billing.plugin.analytics.dao.BusinessAccountDao;
import org.killbill.billing.plugin.analytics.dao.BusinessAccountTransitionDao;
import org.killbill.billing.plugin.analytics.dao.BusinessFieldDao;
import org.killbill.billing.plugin.analytics.dao.BusinessInvoiceAndPaymentDao;
import org.killbill.billing.plugin.analytics.dao.BusinessSubscriptionTransitionDao;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.util.api.RecordIdApi;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.CallOrigin;
import org.killbill.billing.util.callcontext.UserType;
import org.killbill.clock.Clock;
import org.killbill.killbill.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillEventDispatcher.OSGIKillbillEventHandler;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;
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

public class AnalyticsListener implements OSGIKillbillEventHandler {

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
    private final OSGIKillbillDataSource osgiKillbillDataSource;
    private final OSGIConfigPropertiesService osgiConfigPropertiesService;
    private final BusinessSubscriptionTransitionDao bstDao;
    private final BusinessInvoiceAndPaymentDao binAndBipDao;
    private final BusinessAccountTransitionDao bosDao;
    private final BusinessFieldDao bFieldDao;
    private final AllBusinessObjectsDao allBusinessObjectsDao;
    private final NotificationQueue jobQueue;
    private final Clock clock;

    public AnalyticsListener(final OSGIKillbillLogService logService,
                             final OSGIKillbillAPI osgiKillbillAPI,
                             final OSGIKillbillDataSource osgiKillbillDataSource,
                             final OSGIConfigPropertiesService osgiConfigPropertiesService,
                             final Executor executor,
                             final Clock clock,
                             final DefaultNotificationQueueService notificationQueueService) throws NotificationQueueAlreadyExists {
        this.logService = logService;
        this.osgiKillbillAPI = osgiKillbillAPI;
        this.osgiKillbillDataSource = osgiKillbillDataSource;
        this.osgiConfigPropertiesService = osgiConfigPropertiesService;
        this.clock = clock;

        final String refreshDelayMaybeNull = Strings.emptyToNull(osgiConfigPropertiesService.getString(ANALYTICS_REFRESH_DELAY_PROPERTY));
        this.refreshDelaySeconds = refreshDelayMaybeNull == null ? 10 : Integer.valueOf(refreshDelayMaybeNull);

        final BusinessAccountDao bacDao = new BusinessAccountDao(logService, osgiKillbillDataSource);
        this.bstDao = new BusinessSubscriptionTransitionDao(logService, osgiKillbillDataSource, bacDao, executor);
        this.binAndBipDao = new BusinessInvoiceAndPaymentDao(logService, osgiKillbillDataSource, bacDao, executor);
        this.bosDao = new BusinessAccountTransitionDao(logService, osgiKillbillDataSource);
        this.bFieldDao = new BusinessFieldDao(logService, osgiKillbillDataSource);
        this.allBusinessObjectsDao = new AllBusinessObjectsDao(logService, osgiKillbillAPI, osgiKillbillDataSource, executor, clock);

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

        if (accountRecordId != null) {
            // Verify if we don't have a notification for that type and account already.
            // If we do, no need to insert another one since we will do a full refresh anyways
            final List<NotificationEventWithMetadata<AnalyticsJob>> futureNotificationForSearchKeys = jobQueue.getFutureNotificationForSearchKeys(accountRecordId, tenantRecordId);
            if (Iterables.<NotificationEventWithMetadata<AnalyticsJob>>tryFind(futureNotificationForSearchKeys,
                                                                               new Predicate<NotificationEventWithMetadata<AnalyticsJob>>() {
                                                                                   @Override
                                                                                   public boolean apply(final NotificationEventWithMetadata<AnalyticsJob> notificationEvent) {
                                                                                       return notificationEvent.getEvent().equals(job);
                                                                                   }
                                                                               }
                                                                              ).isPresent()) {
                logService.log(LogService.LOG_DEBUG, "Skipping already present notification for event " + killbillEvent.toString());
                return;
            }
        }

        try {
            jobQueue.recordFutureNotification(computeFutureNotificationTime(), job, UUID.randomUUID(), accountRecordId, tenantRecordId);
        } catch (IOException e) {
            logService.log(LogService.LOG_WARNING, "Unable to record notification for event " + killbillEvent.toString());
        }
    }

    private void handleAnalyticsJob(final AnalyticsJob job) throws AnalyticsRefreshException {
        if (job.getEventType() == null) {
            return;
        }

        final CallContext callContext = new AnalyticsCallContext(job, clock);
        final BusinessContextFactory businessContextFactory = new BusinessContextFactory(job.getAccountId(), callContext, logService, osgiKillbillAPI, osgiKillbillDataSource, osgiConfigPropertiesService, clock);

        logService.log(LogService.LOG_INFO, "Refreshing Analytics data for account " + businessContextFactory.getAccountId());
        switch (job.getEventType()) {
            case ACCOUNT_CREATION:
            case ACCOUNT_CHANGE:
                // Note: account information is denormalized across all tables, we pretty much
                // have to refresh all objects
                allBusinessObjectsDao.update(businessContextFactory);
                break;
            case SUBSCRIPTION_CREATION:
            case SUBSCRIPTION_CHANGE:
            case SUBSCRIPTION_CANCEL:
            case SUBSCRIPTION_PHASE:
            case SUBSCRIPTION_UNCANCEL:
                bstDao.update(businessContextFactory);
                break;
            case OVERDUE_CHANGE:
                bosDao.update(businessContextFactory);
                break;
            case INVOICE_CREATION:
            case INVOICE_ADJUSTMENT:
                binAndBipDao.update(businessContextFactory);
                break;
            case PAYMENT_SUCCESS:
            case PAYMENT_FAILED:
                binAndBipDao.update(businessContextFactory);
                break;
            case TAG_CREATION:
            case TAG_DELETION:
                // Note: tags determine the report group. Since it is denormalized across all tables, we pretty much
                // have to refresh all objects
                allBusinessObjectsDao.update(businessContextFactory);
                break;
            case CUSTOM_FIELD_CREATION:
            case CUSTOM_FIELD_DELETION:
                bFieldDao.update(businessContextFactory);
                break;
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
        public UUID getTenantId() {
            return job.getTenantId();
        }
    }

    @VisibleForTesting
    NotificationQueue getJobQueue() {
        return jobQueue;
    }
}
