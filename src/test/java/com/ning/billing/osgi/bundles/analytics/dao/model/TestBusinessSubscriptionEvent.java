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

package com.ning.billing.osgi.bundles.analytics.dao.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.catalog.api.ProductCategory;
import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteNoDB;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessSubscriptionEvent.EventType;

public class TestBusinessSubscriptionEvent extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testValueOf() throws Exception {
        BusinessSubscriptionEvent event;

        event = BusinessSubscriptionEvent.valueOf("START_ENTITLEMENT_ADD_ON");
        Assert.assertEquals(event.getEventType(), EventType.START_ENTITLEMENT);
        Assert.assertEquals(event.getCategory(), ProductCategory.ADD_ON);

        event = BusinessSubscriptionEvent.valueOf("STOP_ENTITLEMENT_BASE");
        Assert.assertEquals(event.getEventType(), EventType.STOP_ENTITLEMENT);
        Assert.assertEquals(event.getCategory(), ProductCategory.BASE);
    }

    @Test(groups = "fast")
    public void testFromSubscription() throws Exception {
        final BusinessSubscriptionEvent event = BusinessSubscriptionEvent.fromTransition(subscriptionTransition);
        Assert.assertEquals(event.getEventType(), EventType.START_ENTITLEMENT);
        Assert.assertEquals(event.getCategory(), subscriptionTransition.getNextPlan().getProduct().getCategory());
        Assert.assertEquals(event.toString(), "START_ENTITLEMENT_" + plan.getProduct().getCategory().toString());
    }
}
