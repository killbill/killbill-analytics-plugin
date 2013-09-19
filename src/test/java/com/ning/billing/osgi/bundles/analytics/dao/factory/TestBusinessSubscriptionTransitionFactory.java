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

package com.ning.billing.osgi.bundles.analytics.dao.factory;

import java.util.LinkedList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.entitlement.api.SubscriptionEvent;
import com.ning.billing.entitlement.api.SubscriptionEventType;
import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteNoDB;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessSubscriptionTransitionModelDao;

import com.google.common.collect.ImmutableList;

public class TestBusinessSubscriptionTransitionFactory extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testRespectPrevPerService() throws Exception {
        final BusinessSubscriptionTransitionFactory factory = new BusinessSubscriptionTransitionFactory(logService, killbillAPI, killbillDataSource, clock);

        final List<SubscriptionEvent> events = new LinkedList<SubscriptionEvent>();
        // Start entitlement
        final SubscriptionEvent event1 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event1.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_ENTITLEMENT);
        Mockito.when(event1.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1));
        Mockito.when(event1.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        events.add(event1);
        // Start billing
        final SubscriptionEvent event2 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event2.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_BILLING);
        Mockito.when(event2.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1));
        Mockito.when(event2.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        events.add(event2);
        // Random service
        final SubscriptionEvent event3 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event3.getSubscriptionEventType()).thenReturn(SubscriptionEventType.SERVICE_STATE_CHANGE);
        Mockito.when(event3.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1));
        Mockito.when(event3.getServiceName()).thenReturn("service-A");
        events.add(event3);
        // Change
        final SubscriptionEvent event4 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event4.getSubscriptionEventType()).thenReturn(SubscriptionEventType.CHANGE);
        Mockito.when(event4.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 15));
        Mockito.when(event4.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_BILLING_SERVICE_NAME);
        events.add(event4);
        // Random service
        final SubscriptionEvent event5 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event5.getSubscriptionEventType()).thenReturn(SubscriptionEventType.SERVICE_STATE_CHANGE);
        Mockito.when(event5.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 20));
        Mockito.when(event5.getServiceName()).thenReturn("service-A");
        events.add(event5);
        // Cancel entitlement
        final SubscriptionEvent event6 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event6.getSubscriptionEventType()).thenReturn(SubscriptionEventType.STOP_ENTITLEMENT);
        Mockito.when(event6.getEffectiveDate()).thenReturn(new LocalDate(2012, 6, 1));
        Mockito.when(event6.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        events.add(event6);
        // Cancel billing
        final SubscriptionEvent event7 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event7.getSubscriptionEventType()).thenReturn(SubscriptionEventType.STOP_BILLING);
        Mockito.when(event7.getEffectiveDate()).thenReturn(new LocalDate(2012, 6, 1));
        Mockito.when(event7.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        events.add(event7);

        final List<BusinessSubscriptionTransitionModelDao> result = ImmutableList.<BusinessSubscriptionTransitionModelDao>copyOf(factory.buildTransitionsForBundle(account, bundle, events, currencyConverter, accountRecordId, tenantRecordId, ReportGroup.test, callContext));
        Assert.assertNull(result.get(0).getPrevStartDate());
        Assert.assertNull(result.get(0).getPrevService());
        Assert.assertEquals(result.get(0).getNextStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(0).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(0).getNextEndDate(), new LocalDate(2012, 5, 15));
        Assert.assertNull(result.get(1).getPrevStartDate());
        Assert.assertNull(result.get(1).getPrevService());
        Assert.assertEquals(result.get(1).getNextStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(1).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(1).getNextEndDate(), new LocalDate(2012, 5, 15));
        Assert.assertNull(result.get(2).getPrevStartDate());
        Assert.assertNull(result.get(2).getPrevService());
        Assert.assertEquals(result.get(2).getNextStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(2).getNextService(), "service-A");
        Assert.assertEquals(result.get(2).getNextEndDate(), new LocalDate(2012, 5, 20));

        Assert.assertEquals(result.get(3).getPrevStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(3).getPrevService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(3).getNextStartDate(), new LocalDate(2012, 5, 15));
        Assert.assertEquals(result.get(3).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(3).getNextEndDate(), new LocalDate(2012, 6, 1));
        Assert.assertEquals(result.get(4).getPrevStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(4).getPrevService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(4).getNextStartDate(), new LocalDate(2012, 5, 15));
        Assert.assertEquals(result.get(4).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(4).getNextEndDate(), new LocalDate(2012, 6, 1));
        Assert.assertEquals(result.get(5).getPrevStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(5).getPrevService(), "service-A");
        Assert.assertEquals(result.get(5).getNextStartDate(), new LocalDate(2012, 5, 20));
        Assert.assertEquals(result.get(5).getNextService(), "service-A");
        Assert.assertNull(result.get(5).getNextEndDate());

        Assert.assertEquals(result.get(6).getPrevStartDate(), new LocalDate(2012, 5, 15));
        Assert.assertEquals(result.get(6).getPrevService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(6).getNextStartDate(), new LocalDate(2012, 6, 1));
        Assert.assertEquals(result.get(6).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertNull(result.get(6).getNextEndDate());
        Assert.assertEquals(result.get(7).getPrevStartDate(), new LocalDate(2012, 5, 15));
        Assert.assertEquals(result.get(7).getPrevService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(7).getNextStartDate(), new LocalDate(2012, 6, 1));
        Assert.assertEquals(result.get(7).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertNull(result.get(7).getNextEndDate());
    }
}
