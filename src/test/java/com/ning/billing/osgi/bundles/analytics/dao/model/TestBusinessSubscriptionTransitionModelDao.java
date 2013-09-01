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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.catalog.api.Currency;
import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteNoDB;

public class TestBusinessSubscriptionTransitionModelDao extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        final LocalDate startDate = new LocalDate(2012, 6, 5);

        final BusinessSubscriptionEvent event = BusinessSubscriptionEvent.valueOf("START_ENTITLEMENT_BASE");
        final BusinessSubscription previousSubscription = null;
        final BusinessSubscription nextSubscription = new BusinessSubscription(null, null, null, Currency.GBP, startDate, serviceName, stateName, currencyConverter);
        final BusinessSubscriptionTransitionModelDao subscriptionTransitionModelDao = new BusinessSubscriptionTransitionModelDao(account,
                                                                                                                                 accountRecordId,
                                                                                                                                 bundle,
                                                                                                                                 subscriptionTransition,
                                                                                                                                 subscriptionEventRecordId,
                                                                                                                                 event,
                                                                                                                                 previousSubscription,
                                                                                                                                 nextSubscription,
                                                                                                                                 currencyConverter,
                                                                                                                                 auditLog,
                                                                                                                                 tenantRecordId,
                                                                                                                                 reportGroup);
        verifyBusinessModelDaoBase(subscriptionTransitionModelDao, accountRecordId, tenantRecordId);
        Assert.assertEquals(subscriptionTransitionModelDao.getCreatedDate(), auditLog.getCreatedDate());
        Assert.assertEquals(subscriptionTransitionModelDao.getSubscriptionEventRecordId(), subscriptionEventRecordId);
        Assert.assertEquals(subscriptionTransitionModelDao.getBundleId(), bundle.getId());
        Assert.assertEquals(subscriptionTransitionModelDao.getBundleExternalKey(), bundle.getExternalKey());
        Assert.assertEquals(subscriptionTransitionModelDao.getSubscriptionId(), subscriptionTransition.getEntitlementId());
        Assert.assertEquals(subscriptionTransitionModelDao.getRequestedTimestamp(), subscriptionTransition.getRequestedDate());
        Assert.assertEquals(subscriptionTransitionModelDao.getEvent(), event.toString());

        Assert.assertNull(subscriptionTransitionModelDao.getPrevProductName());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevProductType());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevProductCategory());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevSlug());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevPhase());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevBillingPeriod());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevPrice());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevPriceList());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevMrr());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevCurrency());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevBusinessActive());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevStartDate());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevService());
        Assert.assertNull(subscriptionTransitionModelDao.getPrevState());

        Assert.assertEquals(subscriptionTransitionModelDao.getNextProductName(), nextSubscription.getProductName());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextProductType(), nextSubscription.getProductType());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextProductCategory(), nextSubscription.getProductCategory());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextSlug(), nextSubscription.getSlug());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextPhase(), nextSubscription.getPhase());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextBillingPeriod(), nextSubscription.getBillingPeriod());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextPrice(), nextSubscription.getPrice());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextPriceList(), nextSubscription.getPriceList());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextMrr(), nextSubscription.getMrr());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextCurrency(), nextSubscription.getCurrency());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextBusinessActive(), nextSubscription.getBusinessActive());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextStartDate(), nextSubscription.getStartDate());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextEndDate(), nextSubscription.getEndDate());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextService(), nextSubscription.getService());
        Assert.assertEquals(subscriptionTransitionModelDao.getNextState(), nextSubscription.getState());
    }
}
