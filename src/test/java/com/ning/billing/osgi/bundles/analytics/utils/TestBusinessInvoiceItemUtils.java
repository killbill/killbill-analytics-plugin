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

package com.ning.billing.osgi.bundles.analytics.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.catalog.api.BillingPeriod;
import com.ning.billing.catalog.api.PhaseType;
import com.ning.billing.catalog.api.Plan;
import com.ning.billing.catalog.api.PlanPhase;
import com.ning.billing.entitlement.api.SubscriptionBundle;
import com.ning.billing.entitlement.api.SubscriptionBundleTimeline;
import com.ning.billing.entitlement.api.SubscriptionEvent;
import com.ning.billing.invoice.api.InvoiceItem;
import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteNoDB;

public class TestBusinessInvoiceItemUtils extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testComputeEndDate() throws Exception {
        final UUID subscriptionId = UUID.randomUUID();

        final Plan plan1 = Mockito.mock(Plan.class);
        Mockito.when(plan1.getName()).thenReturn(UUID.randomUUID().toString());

        final Plan plan2 = Mockito.mock(Plan.class);
        Mockito.when(plan2.getName()).thenReturn(UUID.randomUUID().toString());

        // Start with a trial
        final PlanPhase phase1 = Mockito.mock(PlanPhase.class);
        Mockito.when(phase1.getName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(phase1.getPlan()).thenReturn(plan1);
        Mockito.when(phase1.getBillingPeriod()).thenReturn(BillingPeriod.NO_BILLING_PERIOD);
        Mockito.when(phase1.getPhaseType()).thenReturn(PhaseType.TRIAL);

        final List<SubscriptionEvent> events = new LinkedList<SubscriptionEvent>();
        final SubscriptionEvent event1 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event1.getEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(event1.getEffectiveDate()).thenReturn(new LocalDate(2013, 10, 1));
        Mockito.when(event1.getNextPhase()).thenReturn(phase1);

        // Simulate a plan change during the trial
        final PlanPhase phase2 = Mockito.mock(PlanPhase.class);
        Mockito.when(phase2.getName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(phase2.getPlan()).thenReturn(plan2);
        Mockito.when(phase2.getBillingPeriod()).thenReturn(BillingPeriod.NO_BILLING_PERIOD);
        Mockito.when(phase2.getPhaseType()).thenReturn(PhaseType.TRIAL);

        final SubscriptionEvent event2 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event2.getEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(event2.getEffectiveDate()).thenReturn(new LocalDate(2013, 10, 15));
        Mockito.when(event2.getPrevPhase()).thenReturn(phase1);
        Mockito.when(event2.getNextPhase()).thenReturn(phase2);

        // Evergreen phase
        final PlanPhase phase3 = Mockito.mock(PlanPhase.class);
        Mockito.when(phase3.getName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(phase3.getPlan()).thenReturn(plan2);
        Mockito.when(phase3.getBillingPeriod()).thenReturn(BillingPeriod.MONTHLY);
        Mockito.when(phase3.getPhaseType()).thenReturn(PhaseType.EVERGREEN);

        final SubscriptionEvent event3 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event3.getEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(event3.getEffectiveDate()).thenReturn(new LocalDate(2013, 11, 1));
        Mockito.when(event3.getPrevPhase()).thenReturn(phase2);
        Mockito.when(event3.getNextPhase()).thenReturn(phase3);

        // Add an event for another subscription
        final SubscriptionEvent randomEvent = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(randomEvent.getEntitlementId()).thenReturn(UUID.randomUUID());
        Mockito.when(randomEvent.getEffectiveDate()).thenReturn(new LocalDate(2013, 10, 15));
        Mockito.when(randomEvent.getPrevPhase()).thenReturn(phase1);
        Mockito.when(randomEvent.getNextPhase()).thenReturn(phase2);

        events.add(event1);
        events.add(event2);
        events.add(randomEvent);
        events.add(event3);

        final SubscriptionBundleTimeline timeline = Mockito.mock(SubscriptionBundleTimeline.class);
        Mockito.when(timeline.getSubscriptionEvents()).thenReturn(events);

        final SubscriptionBundle bundle = Mockito.mock(SubscriptionBundle.class);
        Mockito.when(bundle.getTimeline()).thenReturn(timeline);

        // Check first trial invoice item
        final InvoiceItem item1 = Mockito.mock(InvoiceItem.class);
        Mockito.when(item1.getSubscriptionId()).thenReturn(subscriptionId);
        Mockito.when(item1.getStartDate()).thenReturn(new LocalDate(2013, 10, 1));
        Assert.assertEquals(BusinessInvoiceItemUtils.computeServicePeriodEndDate(item1, phase1, bundle).compareTo(new LocalDate(2013, 10, 15)), 0);

        // Check second trial invoice item
        final InvoiceItem item2 = Mockito.mock(InvoiceItem.class);
        Mockito.when(item2.getSubscriptionId()).thenReturn(subscriptionId);
        Mockito.when(item2.getStartDate()).thenReturn(new LocalDate(2013, 10, 15));
        Assert.assertEquals(BusinessInvoiceItemUtils.computeServicePeriodEndDate(item2, phase2, bundle).compareTo(new LocalDate(2013, 11, 1)), 0);

        // Check evergreen phase
        final InvoiceItem item3 = Mockito.mock(InvoiceItem.class);
        Mockito.when(item3.getSubscriptionId()).thenReturn(subscriptionId);
        Mockito.when(item3.getStartDate()).thenReturn(new LocalDate(2013, 11, 1));
        Mockito.when(item3.getEndDate()).thenReturn(new LocalDate(2013, 12, 1));
        Assert.assertEquals(BusinessInvoiceItemUtils.computeServicePeriodEndDate(item3, phase3, bundle).compareTo(new LocalDate(2013, 12, 1)), 0);
    }
}
