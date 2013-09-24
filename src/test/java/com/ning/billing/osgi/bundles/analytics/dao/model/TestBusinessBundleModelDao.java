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

import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.catalog.api.Currency;
import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteNoDB;

public class TestBusinessBundleModelDao extends AnalyticsTestSuiteNoDB {

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

        final BusinessBundleModelDao bundleSummaryModelDao = new BusinessBundleModelDao(account,
                                                                                        accountRecordId,
                                                                                        bundle,
                                                                                        bundleRecordId,
                                                                                        3,
                                                                                        true,
                                                                                        new LocalDate(2013, 10, 1),
                                                                                        subscriptionTransitionModelDao,
                                                                                        currencyConverter,
                                                                                        auditLog,
                                                                                        tenantRecordId,
                                                                                        reportGroup);
        verifyBusinessModelDaoBase(bundleSummaryModelDao, accountRecordId, tenantRecordId);
        Assert.assertEquals(bundleSummaryModelDao.getBundleRecordId(), bundleRecordId);
        Assert.assertEquals(bundleSummaryModelDao.getBundleId(), bundle.getId());
        Assert.assertEquals(bundleSummaryModelDao.getBundleExternalKey(), bundle.getExternalKey());
        Assert.assertEquals(bundleSummaryModelDao.getSubscriptionId(), subscriptionTransition.getEntitlementId());
        Assert.assertEquals(bundleSummaryModelDao.getBundleAccountRank(), (Integer) 3);
        Assert.assertTrue(bundleSummaryModelDao.getLatestForBundleExternalKey());
        Assert.assertEquals(bundleSummaryModelDao.getChargedThroughDate().compareTo(new LocalDate(2013, 10, 1)), 0);
        Assert.assertEquals(bundleSummaryModelDao.getCurrentProductName(), subscriptionTransitionModelDao.getNextProductName());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentProductType(), subscriptionTransitionModelDao.getNextProductType());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentProductCategory(), subscriptionTransitionModelDao.getNextProductCategory());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentSlug(), subscriptionTransitionModelDao.getNextSlug());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentPhase(), subscriptionTransitionModelDao.getNextPhase());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentBillingPeriod(), subscriptionTransitionModelDao.getNextBillingPeriod());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentPrice(), subscriptionTransitionModelDao.getNextPrice());
        Assert.assertEquals(bundleSummaryModelDao.getConvertedCurrentPrice(), subscriptionTransitionModelDao.getConvertedNextPrice());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentPriceList(), subscriptionTransitionModelDao.getNextPriceList());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentMrr(), subscriptionTransitionModelDao.getNextMrr());
        Assert.assertEquals(bundleSummaryModelDao.getConvertedCurrentMrr(), subscriptionTransitionModelDao.getConvertedNextMrr());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentCurrency(), subscriptionTransitionModelDao.getNextCurrency());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentBusinessActive(), subscriptionTransitionModelDao.getNextBusinessActive());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentStartDate(), subscriptionTransitionModelDao.getNextStartDate());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentEndDate(), subscriptionTransitionModelDao.getNextEndDate());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentService(), subscriptionTransitionModelDao.getNextService());
        Assert.assertEquals(bundleSummaryModelDao.getCurrentState(), subscriptionTransitionModelDao.getNextState());
    }
}
