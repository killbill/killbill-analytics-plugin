/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.dao.factory;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.killbill.billing.entitlement.api.SubscriptionEvent;
import org.killbill.billing.entitlement.api.SubscriptionEventType;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

public class TestBusinessSubscriptionTransitionFactory extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testRespectPrevPerService() throws Exception {
        final BusinessSubscriptionTransitionFactory factory = new BusinessSubscriptionTransitionFactory();

        final UUID subscriptionId1 = UUID.randomUUID();
        final UUID subscriptionId2 = UUID.randomUUID();

        final List<SubscriptionEvent> events = new LinkedList<SubscriptionEvent>();
        // Start entitlement for subscription 1
        final SubscriptionEvent event1 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event1.getEntitlementId()).thenReturn(subscriptionId1);
        Mockito.when(event1.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_ENTITLEMENT);
        Mockito.when(event1.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1));
        Mockito.when(event1.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        events.add(event1);
        // Start billing for subscription 1
        final SubscriptionEvent event2 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event2.getEntitlementId()).thenReturn(subscriptionId1);
        Mockito.when(event2.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_BILLING);
        Mockito.when(event2.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1));
        Mockito.when(event2.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        events.add(event2);
        // Start entitlement for subscription 2
        final SubscriptionEvent event12 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event12.getEntitlementId()).thenReturn(subscriptionId2);
        Mockito.when(event12.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_ENTITLEMENT);
        Mockito.when(event12.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 3));
        Mockito.when(event12.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        events.add(event12);
        // Start billing for subscription 2
        final SubscriptionEvent event22 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event22.getEntitlementId()).thenReturn(subscriptionId2);
        Mockito.when(event22.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_BILLING);
        Mockito.when(event22.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 3));
        Mockito.when(event22.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        events.add(event22);
        // Random service for subscription 1
        final SubscriptionEvent event3 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event3.getEntitlementId()).thenReturn(subscriptionId1);
        Mockito.when(event3.getSubscriptionEventType()).thenReturn(SubscriptionEventType.SERVICE_STATE_CHANGE);
        Mockito.when(event3.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1));
        Mockito.when(event3.getServiceName()).thenReturn("service-A");
        events.add(event3);
        // Change for subscription 1
        final SubscriptionEvent event4 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event4.getEntitlementId()).thenReturn(subscriptionId1);
        Mockito.when(event4.getSubscriptionEventType()).thenReturn(SubscriptionEventType.CHANGE);
        Mockito.when(event4.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 15));
        Mockito.when(event4.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_BILLING_SERVICE_NAME);
        events.add(event4);
        // Random service for subscription 1
        final SubscriptionEvent event5 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event5.getEntitlementId()).thenReturn(subscriptionId1);
        Mockito.when(event5.getSubscriptionEventType()).thenReturn(SubscriptionEventType.SERVICE_STATE_CHANGE);
        Mockito.when(event5.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 20));
        Mockito.when(event5.getServiceName()).thenReturn("service-A");
        events.add(event5);
        // Cancel entitlement for subscription 2
        final SubscriptionEvent event32 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event32.getEntitlementId()).thenReturn(subscriptionId2);
        Mockito.when(event32.getSubscriptionEventType()).thenReturn(SubscriptionEventType.STOP_ENTITLEMENT);
        Mockito.when(event32.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 25));
        Mockito.when(event32.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        events.add(event32);
        // Cancel billing for subscription 2
        final SubscriptionEvent event42 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event42.getEntitlementId()).thenReturn(subscriptionId2);
        Mockito.when(event42.getSubscriptionEventType()).thenReturn(SubscriptionEventType.STOP_BILLING);
        Mockito.when(event42.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 25));
        Mockito.when(event42.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        events.add(event42);
        // Cancel entitlement for subscription 1
        final SubscriptionEvent event6 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event6.getEntitlementId()).thenReturn(subscriptionId1);
        Mockito.when(event6.getSubscriptionEventType()).thenReturn(SubscriptionEventType.STOP_ENTITLEMENT);
        Mockito.when(event6.getEffectiveDate()).thenReturn(new LocalDate(2012, 6, 1));
        Mockito.when(event6.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        events.add(event6);
        // Cancel billing for subscription 1
        final SubscriptionEvent event7 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event7.getEntitlementId()).thenReturn(subscriptionId1);
        Mockito.when(event7.getSubscriptionEventType()).thenReturn(SubscriptionEventType.STOP_BILLING);
        Mockito.when(event7.getEffectiveDate()).thenReturn(new LocalDate(2012, 6, 1));
        Mockito.when(event7.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        events.add(event7);

        final BusinessContextFactory businessContextFactory = new BusinessContextFactory(account.getId(), callContext, currencyConversionDao, killbillAPI, osgiConfigPropertiesService, clock, analyticsConfigurationHandler);
        final List<BusinessSubscriptionTransitionModelDao> result = ImmutableList.<BusinessSubscriptionTransitionModelDao>copyOf(factory.buildTransitionsForBundle(businessContextFactory, account, bundle, events, currencyConverter, accountRecordId, tenantRecordId, ReportGroup.test));
        Assert.assertEquals(result.get(0).getEvent(), "START_ENTITLEMENT_UNSPECIFIED");
        Assert.assertEquals(result.get(0).getSubscriptionId(), subscriptionId1);
        Assert.assertNull(result.get(0).getPrevStartDate());
        Assert.assertNull(result.get(0).getPrevService());
        Assert.assertEquals(result.get(0).getNextStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(0).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(0).getNextEndDate(), new LocalDate(2012, 5, 15));

        Assert.assertEquals(result.get(1).getEvent(), "START_BILLING_UNSPECIFIED");
        Assert.assertEquals(result.get(1).getSubscriptionId(), subscriptionId1);
        Assert.assertNull(result.get(1).getPrevStartDate());
        Assert.assertNull(result.get(1).getPrevService());
        Assert.assertEquals(result.get(1).getNextStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(1).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(1).getNextEndDate(), new LocalDate(2012, 5, 15));

        Assert.assertEquals(result.get(2).getEvent(), "START_ENTITLEMENT_UNSPECIFIED");
        Assert.assertEquals(result.get(2).getSubscriptionId(), subscriptionId2);
        Assert.assertNull(result.get(2).getPrevStartDate());
        Assert.assertNull(result.get(2).getPrevService());
        Assert.assertEquals(result.get(2).getNextStartDate(), new LocalDate(2012, 5, 3));
        Assert.assertEquals(result.get(2).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(2).getNextEndDate(), new LocalDate(2012, 5, 25));

        Assert.assertEquals(result.get(3).getEvent(), "START_BILLING_UNSPECIFIED");
        Assert.assertEquals(result.get(3).getSubscriptionId(), subscriptionId2);
        Assert.assertNull(result.get(3).getPrevStartDate());
        Assert.assertNull(result.get(3).getPrevService());
        Assert.assertEquals(result.get(3).getNextStartDate(), new LocalDate(2012, 5, 3));
        Assert.assertEquals(result.get(3).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(3).getNextEndDate(), new LocalDate(2012, 5, 25));

        Assert.assertEquals(result.get(4).getEvent(), "STATE_CHANGE_UNSPECIFIED");
        Assert.assertNull(result.get(4).getPrevStartDate());
        Assert.assertNull(result.get(4).getPrevService());
        Assert.assertEquals(result.get(4).getNextStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(4).getNextService(), "service-A");
        Assert.assertEquals(result.get(4).getNextEndDate(), new LocalDate(2012, 5, 20));

        Assert.assertEquals(result.get(5).getEvent(), "CHANGE_UNSPECIFIED");
        Assert.assertEquals(result.get(5).getSubscriptionId(), subscriptionId1);
        Assert.assertEquals(result.get(5).getPrevStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(5).getPrevService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(5).getNextStartDate(), new LocalDate(2012, 5, 15));
        Assert.assertEquals(result.get(5).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(5).getNextEndDate(), new LocalDate(2012, 6, 1));

        Assert.assertEquals(result.get(6).getEvent(), "CHANGE_UNSPECIFIED");
        Assert.assertEquals(result.get(6).getSubscriptionId(), subscriptionId1);
        Assert.assertEquals(result.get(6).getPrevStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(6).getPrevService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(6).getNextStartDate(), new LocalDate(2012, 5, 15));
        Assert.assertEquals(result.get(6).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(6).getNextEndDate(), new LocalDate(2012, 6, 1));

        Assert.assertEquals(result.get(7).getEvent(), "STATE_CHANGE_UNSPECIFIED");
        Assert.assertEquals(result.get(7).getSubscriptionId(), subscriptionId1);
        Assert.assertEquals(result.get(7).getPrevStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(7).getPrevService(), "service-A");
        Assert.assertEquals(result.get(7).getNextStartDate(), new LocalDate(2012, 5, 20));
        Assert.assertEquals(result.get(7).getNextService(), "service-A");
        Assert.assertNull(result.get(7).getNextEndDate());

        Assert.assertEquals(result.get(8).getEvent(), "STOP_ENTITLEMENT_UNSPECIFIED");
        Assert.assertEquals(result.get(8).getPrevStartDate(), new LocalDate(2012, 5, 3));
        Assert.assertEquals(result.get(8).getPrevService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(8).getNextStartDate(), new LocalDate(2012, 5, 25));
        Assert.assertEquals(result.get(8).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertNull(result.get(8).getNextEndDate());

        Assert.assertEquals(result.get(9).getEvent(), "STOP_BILLING_UNSPECIFIED");
        Assert.assertEquals(result.get(9).getPrevStartDate(), new LocalDate(2012, 5, 3));
        Assert.assertEquals(result.get(9).getPrevService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(9).getNextStartDate(), new LocalDate(2012, 5, 25));
        Assert.assertEquals(result.get(9).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertNull(result.get(9).getNextEndDate());

        Assert.assertEquals(result.get(10).getEvent(), "STOP_ENTITLEMENT_UNSPECIFIED");
        Assert.assertEquals(result.get(10).getPrevStartDate(), new LocalDate(2012, 5, 15));
        Assert.assertEquals(result.get(10).getPrevService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(10).getNextStartDate(), new LocalDate(2012, 6, 1));
        Assert.assertEquals(result.get(10).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertNull(result.get(10).getNextEndDate());

        Assert.assertEquals(result.get(11).getEvent(), "STOP_BILLING_UNSPECIFIED");
        Assert.assertEquals(result.get(11).getPrevStartDate(), new LocalDate(2012, 5, 15));
        Assert.assertEquals(result.get(11).getPrevService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(11).getNextStartDate(), new LocalDate(2012, 6, 1));
        Assert.assertEquals(result.get(11).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertNull(result.get(11).getNextEndDate());
    }
}
