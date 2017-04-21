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

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;
import org.killbill.notificationq.api.NotificationEventWithMetadata;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.awaitility.Awaitility;

import com.google.common.collect.Iterables;

public class TestAnalyticsNotificationQueue extends AnalyticsTestSuiteWithEmbeddedDB {

    @Test(groups = "slow")
    public void testSendOneEvent() throws Exception {
        final AnalyticsListener analyticsListener = new AnalyticsListener(logService, killbillAPI, killbillDataSource, osgiConfigPropertiesService, BusinessExecutor.newCachedThreadPool(osgiConfigPropertiesService), clock, analyticsConfigurationHandler, notificationQueueService);
        analyticsListener.start();

        // Verify the original state
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        final ExtBusEvent event = createExtBusEvent();
        analyticsListener.handleKillbillEvent(event);

        // Shouldn't be anything right after it
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Wait for the notification to kick in
        Awaitility.await().atMost(15, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return analyticsSqlDao.getAccountFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size() == 1;
            }
        });

        analyticsListener.shutdownNow();
    }

    @Test(groups = "slow")
    public void testVerifyNoDups() throws Exception {
        final AnalyticsListener analyticsListener = new AnalyticsListener(logService, killbillAPI, killbillDataSource, osgiConfigPropertiesService, BusinessExecutor.newCachedThreadPool(osgiConfigPropertiesService), clock, analyticsConfigurationHandler, notificationQueueService);
        // Don't start the dequeuer
        Assert.assertEquals(Iterables.<NotificationEventWithMetadata>size(analyticsListener.getJobQueue().getFutureNotificationForSearchKeys(accountRecordId, tenantRecordId)), 0);

        // Verify the original state
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Send the first event
        final ExtBusEvent firstEvent = createExtBusEvent();
        Mockito.when(firstEvent.getEventType()).thenReturn(ExtBusEventType.INVOICE_CREATION);
        analyticsListener.handleKillbillEvent(firstEvent);

        // Verify the size of the queue
        Assert.assertEquals(Iterables.<NotificationEventWithMetadata>size(analyticsListener.getJobQueue().getFutureNotificationForSearchKeys(accountRecordId, tenantRecordId)), 1);

        // Send the same event
        analyticsListener.handleKillbillEvent(firstEvent);

        // Verify the size of the queue
        Assert.assertEquals(Iterables.<NotificationEventWithMetadata>size(analyticsListener.getJobQueue().getFutureNotificationForSearchKeys(accountRecordId, tenantRecordId)), 1);

        // Now, send a different event type
        final ExtBusEvent secondEvent = createExtBusEvent();
        Mockito.when(secondEvent.getEventType()).thenReturn(ExtBusEventType.OVERDUE_CHANGE);
        analyticsListener.handleKillbillEvent(secondEvent);

        // Verify the size of the queue
        Assert.assertEquals(Iterables.<NotificationEventWithMetadata>size(analyticsListener.getJobQueue().getFutureNotificationForSearchKeys(accountRecordId, tenantRecordId)), 2);

        // Now, send a different event type, but triggering the same refresh type as the first event
        final ExtBusEvent thirdEvent = createExtBusEvent();
        Mockito.when(thirdEvent.getEventType()).thenReturn(ExtBusEventType.PAYMENT_FAILED);
        analyticsListener.handleKillbillEvent(thirdEvent);

        // Verify the size of the queue
        Assert.assertEquals(Iterables.<NotificationEventWithMetadata>size(analyticsListener.getJobQueue().getFutureNotificationForSearchKeys(accountRecordId, tenantRecordId)), 2);

        // Verify the final state
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    private ExtBusEvent createExtBusEvent() {
        final ExtBusEvent event = Mockito.mock(ExtBusEvent.class);
        final UUID accountId = account.getId();
        Mockito.when(event.getAccountId()).thenReturn(accountId);
        final UUID tenantId = callContext.getTenantId();
        Mockito.when(event.getTenantId()).thenReturn(tenantId);
        Mockito.when(event.getEventType()).thenReturn(ExtBusEventType.CUSTOM_FIELD_CREATION);
        return event;
    }
}
