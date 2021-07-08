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

import java.util.UUID;

import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfiguration;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfigurationHandler;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAnalyticsListener extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testBlacklist() throws Exception {
        final AnalyticsListener analyticsListener = new AnalyticsListener(killbillAPI,
                                                                          killbillDataSource,
                                                                          osgiConfigPropertiesService,
                                                                          null,
                                                                          locker,
                                                                          clock,
                                                                          analyticsConfigurationHandler,
                                                                          notificationQueueService);

        // Other accounts are blacklisted
        Assert.assertFalse(analyticsListener.isAccountBlacklisted(UUID.randomUUID(), callContext.getTenantId()));

        // Blacklist
        Assert.assertTrue(analyticsListener.isAccountBlacklisted(blackListedAccountId, callContext.getTenantId()));
    }

    @Test(groups = "fast")
    public void testIgnoredGroups() throws Exception {
        final ExtBusEvent cfEvent = Mockito.mock(ExtBusEvent.class);
        Mockito.when(cfEvent.getEventType()).thenReturn(ExtBusEventType.CUSTOM_FIELD_CREATION);

        final ExtBusEvent accountEvent = Mockito.mock(ExtBusEvent.class);
        Mockito.when(accountEvent.getEventType()).thenReturn(ExtBusEventType.ACCOUNT_CREATION);

        final AnalyticsConfigurationHandler analyticsConfigurationHandler = new AnalyticsConfigurationHandler(null, AnalyticsActivator.PLUGIN_NAME, killbillAPI);
        final AnalyticsConfiguration defaultConfigurable = new AnalyticsConfiguration();
        defaultConfigurable.ignoredGroups.add("FIELDS");
        analyticsConfigurationHandler.setDefaultConfigurable(defaultConfigurable);

        final AnalyticsListener analyticsListener = new AnalyticsListener(killbillAPI,
                                                                          killbillDataSource,
                                                                          osgiConfigPropertiesService,
                                                                          null,
                                                                          locker,
                                                                          clock,
                                                                          analyticsConfigurationHandler,
                                                                          notificationQueueService);

        Assert.assertTrue(analyticsListener.shouldIgnoreEvent(new AnalyticsJob(cfEvent)));
        Assert.assertFalse(analyticsListener.shouldIgnoreEvent(new AnalyticsJob(accountEvent)));
    }


    @Test(groups = "fast", description = "https://github.com/killbill/killbill-analytics-plugin/issues/118")
    public void testJobsOverlap() throws Exception {
        final AnalyticsListener analyticsListener = new AnalyticsListener(killbillAPI,
                                                                          killbillDataSource,
                                                                          osgiConfigPropertiesService,
                                                                          null,
                                                                          locker,
                                                                          clock,
                                                                          analyticsConfigurationHandler,
                                                                          notificationQueueService);

        final UUID accountId = UUID.randomUUID();
        final UUID accountId2 = UUID.randomUUID();
        final UUID invoiceId1 = UUID.randomUUID();
        final UUID invoiceId2 = UUID.randomUUID();
        final UUID invoiceId3 = UUID.randomUUID();

        final ExtBusEvent inv1CreationEvent = Mockito.mock(ExtBusEvent.class);
        Mockito.when(inv1CreationEvent.getAccountId()).thenReturn(accountId);
        Mockito.when(inv1CreationEvent.getObjectId()).thenReturn(invoiceId1);
        Mockito.when(inv1CreationEvent.getEventType()).thenReturn(ExtBusEventType.INVOICE_CREATION);

        final ExtBusEvent inv1AdjEvent = Mockito.mock(ExtBusEvent.class);
        Mockito.when(inv1AdjEvent.getAccountId()).thenReturn(accountId);
        Mockito.when(inv1AdjEvent.getObjectId()).thenReturn(invoiceId1);
        Mockito.when(inv1AdjEvent.getEventType()).thenReturn(ExtBusEventType.INVOICE_ADJUSTMENT);

        final ExtBusEvent inv2AdjEvent = Mockito.mock(ExtBusEvent.class);
        Mockito.when(inv2AdjEvent.getAccountId()).thenReturn(accountId);
        Mockito.when(inv2AdjEvent.getObjectId()).thenReturn(invoiceId2);
        Mockito.when(inv2AdjEvent.getEventType()).thenReturn(ExtBusEventType.INVOICE_ADJUSTMENT);

        final ExtBusEvent inv3CreationEvent = Mockito.mock(ExtBusEvent.class);
        Mockito.when(inv3CreationEvent.getAccountId()).thenReturn(accountId2);
        Mockito.when(inv3CreationEvent.getObjectId()).thenReturn(invoiceId3);
        Mockito.when(inv3CreationEvent.getEventType()).thenReturn(ExtBusEventType.INVOICE_CREATION);

        // Same job
        Assert.assertTrue(analyticsListener.jobsOverlap(new AnalyticsJob(inv1CreationEvent), new AnalyticsJob(inv1CreationEvent)));
        // Different accounts
        Assert.assertFalse(analyticsListener.jobsOverlap(new AnalyticsJob(inv3CreationEvent), new AnalyticsJob(inv1CreationEvent)));
        // Adjustment event for the same invoice
        Assert.assertTrue(analyticsListener.jobsOverlap(new AnalyticsJob(inv1AdjEvent), new AnalyticsJob(inv1CreationEvent)));
        // Adjustment event for another invoice
        Assert.assertFalse(analyticsListener.jobsOverlap(new AnalyticsJob(inv2AdjEvent), new AnalyticsJob(inv1CreationEvent)));
    }
}
