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
import org.killbill.billing.entitlement.api.BlockingState;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class TestBusinessAccountTransitionFactory extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testRespectPrevPerService() throws Exception {
        final BusinessAccountTransitionFactory factory = new BusinessAccountTransitionFactory();

        final List<BlockingState> events = new LinkedList<BlockingState>();
        final BlockingState event1 = Mockito.mock(BlockingState.class);
        Mockito.when(event1.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(event1.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1).toDateTimeAtStartOfDay());
        Mockito.when(event1.getService()).thenReturn("service-A");
        events.add(event1);
        final BlockingState event2 = Mockito.mock(BlockingState.class);
        Mockito.when(event2.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(event2.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 2).toDateTimeAtStartOfDay());
        Mockito.when(event2.getService()).thenReturn("service-B");
        events.add(event2);
        final BlockingState event3 = Mockito.mock(BlockingState.class);
        Mockito.when(event3.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(event3.getEffectiveDate()).thenReturn(new LocalDate(2012, 6, 1).toDateTimeAtStartOfDay());
        Mockito.when(event3.getService()).thenReturn("service-A");
        events.add(event3);
        final BlockingState event4 = Mockito.mock(BlockingState.class);
        Mockito.when(event4.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(event4.getEffectiveDate()).thenReturn(new LocalDate(2012, 6, 2).toDateTimeAtStartOfDay());
        Mockito.when(event4.getService()).thenReturn("service-B");
        events.add(event4);

        final BusinessContextFactory businessContextFactory = new BusinessContextFactory(account.getId(), callContext, currencyConversionDao, killbillAPI, osgiConfigPropertiesService, clock, analyticsConfigurationHandler);
        final List<BusinessAccountTransitionModelDao> result = ImmutableList.<BusinessAccountTransitionModelDao>copyOf(factory.createBusinessAccountTransitions(businessContextFactory, Lists.reverse(events)));
        Assert.assertEquals(result.get(0).getService(), "service-A");
        Assert.assertEquals(result.get(0).getStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(0).getEndDate(), new LocalDate(2012, 6, 1));
        Assert.assertEquals(result.get(1).getService(), "service-B");
        Assert.assertEquals(result.get(1).getStartDate(), new LocalDate(2012, 5, 2));
        Assert.assertEquals(result.get(1).getEndDate(), new LocalDate(2012, 6, 2));
        Assert.assertEquals(result.get(2).getService(), "service-A");
        Assert.assertEquals(result.get(2).getStartDate(), new LocalDate(2012, 6, 1));
        Assert.assertNull(result.get(2).getEndDate());
        Assert.assertEquals(result.get(3).getService(), "service-B");
        Assert.assertEquals(result.get(3).getStartDate(), new LocalDate(2012, 6, 2));
        Assert.assertNull(result.get(3).getEndDate());
    }
}
