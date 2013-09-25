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

package com.ning.billing.osgi.bundles.analytics.api;

import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.catalog.api.Currency;
import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteNoDB;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessBundleModelDao;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessSubscription;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessSubscriptionEvent;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessSubscriptionTransitionModelDao;

public class TestBusinessBundle extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        final BusinessSubscriptionEvent event = BusinessSubscriptionEvent.valueOf("START_ENTITLEMENT_BASE");
        final BusinessSubscription previousSubscription = null;
        final BusinessSubscription nextSubscription = new BusinessSubscription(null, phase, priceList, Currency.GBP, new LocalDate(2010, 1, 1), serviceName, stateName, currencyConverter);
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

        final BusinessBundleModelDao bundleModelDao = new BusinessBundleModelDao(account,
                                                                                 accountRecordId,
                                                                                 bundle,
                                                                                 bundleRecordId,
                                                                                 1,
                                                                                 true,
                                                                                 new LocalDate(2013, 10, 1),
                                                                                 subscriptionTransitionModelDao,
                                                                                 currencyConverter,
                                                                                 auditLog,
                                                                                 tenantRecordId,
                                                                                 reportGroup);
        final BusinessBundle businessBundle = new BusinessBundle(bundleModelDao);

        verifyBusinessEntityBase(businessBundle);
        Assert.assertEquals(businessBundle.getBundleRecordId(), bundleModelDao.getBundleRecordId());
        Assert.assertEquals(businessBundle.getBundleId(), bundleModelDao.getBundleId());
        Assert.assertEquals(businessBundle.getBundleExternalKey(), bundleModelDao.getBundleExternalKey());
        Assert.assertEquals(businessBundle.getSubscriptionId(), bundleModelDao.getSubscriptionId());
        Assert.assertEquals(businessBundle.getBundleAccountRank(), bundleModelDao.getBundleAccountRank());
        Assert.assertEquals(businessBundle.getLatestForBundleExternalKey(), bundleModelDao.getLatestForBundleExternalKey());
        Assert.assertEquals(businessBundle.getChargedThroughDate().compareTo(bundleModelDao.getChargedThroughDate()), 0);
        Assert.assertEquals(businessBundle.getCurrentProductName(), bundleModelDao.getCurrentProductName());
        Assert.assertEquals(businessBundle.getCurrentProductType(), bundleModelDao.getCurrentProductType());
        Assert.assertEquals(businessBundle.getCurrentProductCategory(), bundleModelDao.getCurrentProductCategory());
        Assert.assertEquals(businessBundle.getCurrentSlug(), bundleModelDao.getCurrentSlug());
        Assert.assertEquals(businessBundle.getCurrentPhase(), bundleModelDao.getCurrentPhase());
        Assert.assertEquals(businessBundle.getCurrentBillingPeriod(), bundleModelDao.getCurrentBillingPeriod());
        Assert.assertEquals(businessBundle.getCurrentPrice().compareTo(bundleModelDao.getCurrentPrice()), 0);
        Assert.assertEquals(businessBundle.getConvertedCurrentPrice().compareTo(bundleModelDao.getConvertedCurrentPrice()), 0);
        Assert.assertEquals(businessBundle.getCurrentPriceList(), bundleModelDao.getCurrentPriceList());
        Assert.assertEquals(businessBundle.getCurrentMrr().compareTo(bundleModelDao.getCurrentMrr()), 0);
        Assert.assertEquals(businessBundle.getConvertedCurrentMrr().compareTo(bundleModelDao.getConvertedCurrentMrr()), 0);
        Assert.assertEquals(businessBundle.getCurrentCurrency(), bundleModelDao.getCurrentCurrency());
        Assert.assertEquals(businessBundle.getCurrentBusinessActive(), bundleModelDao.getCurrentBusinessActive());
        Assert.assertEquals(businessBundle.getCurrentStartDate().compareTo(bundleModelDao.getCurrentStartDate()), 0);
        Assert.assertNull(businessBundle.getCurrentEndDate());
        Assert.assertEquals(businessBundle.getCurrentService(), bundleModelDao.getCurrentService());
        Assert.assertEquals(businessBundle.getCurrentState(), bundleModelDao.getCurrentState());
        Assert.assertEquals(businessBundle.getConvertedCurrency(), bundleModelDao.getConvertedCurrency());
        Assert.assertEquals(businessBundle.getOriginalCreatedDate(), bundleModelDao.getOriginalCreatedDate());
    }
}
