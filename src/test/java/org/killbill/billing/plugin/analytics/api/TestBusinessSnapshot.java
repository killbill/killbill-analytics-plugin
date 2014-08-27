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

package org.killbill.billing.plugin.analytics.api;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentPurchaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscription;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionEvent;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import org.killbill.billing.plugin.analytics.http.ObjectMapperProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

public class TestBusinessSnapshot extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        // Account
        final BusinessAccountModelDao accountModelDao = new BusinessAccountModelDao(account,
                                                                                    accountRecordId,
                                                                                    BigDecimal.ONE,
                                                                                    invoice,
                                                                                    invoice,
                                                                                    paymentTransaction,
                                                                                    3,
                                                                                    currencyConverter,
                                                                                    auditLog,
                                                                                    tenantRecordId,
                                                                                    reportGroup);
        final BusinessAccount businessAccount = new BusinessAccount(accountModelDao);

        // Field
        final BusinessAccountFieldModelDao businessAccountFieldModelDao = new BusinessAccountFieldModelDao(account,
                                                                                                           accountRecordId,
                                                                                                           customField,
                                                                                                           fieldRecordId,
                                                                                                           auditLog,
                                                                                                           tenantRecordId,
                                                                                                           reportGroup);
        final BusinessField businessField = BusinessField.create(businessAccountFieldModelDao);

        // Invoice
        final BusinessInvoiceModelDao invoiceModelDao = new BusinessInvoiceModelDao(account,
                                                                                    accountRecordId,
                                                                                    invoice,
                                                                                    invoiceRecordId,
                                                                                    currencyConverter,
                                                                                    auditLog,
                                                                                    tenantRecordId,
                                                                                    reportGroup);
        final BusinessInvoiceItemBaseModelDao invoiceItemBaseModelDao = BusinessInvoiceItemBaseModelDao.create(account,
                                                                                                               accountRecordId,
                                                                                                               invoice,
                                                                                                               invoiceItem,
                                                                                                               itemSource,
                                                                                                               invoiceItemType,
                                                                                                               invoiceItemRecordId,
                                                                                                               secondInvoiceItemRecordId,
                                                                                                               bundle,
                                                                                                               plan,
                                                                                                               phase,
                                                                                                               currencyConverter,
                                                                                                               auditLog,
                                                                                                               tenantRecordId,
                                                                                                               reportGroup);
        final BusinessInvoice businessInvoice = new BusinessInvoice(invoiceModelDao,
                                                                    ImmutableList.<BusinessInvoiceItemBaseModelDao>of(invoiceItemBaseModelDao));

        // Invoice payment
        final BusinessPaymentBaseModelDao invoicePaymentBaseModelDao = BusinessPaymentPurchaseModelDao.create(account,
                                                                                                              accountRecordId,
                                                                                                              invoice,
                                                                                                              invoicePayment,
                                                                                                              invoicePaymentRecordId,
                                                                                                              payment,
                                                                                                              paymentTransaction,
                                                                                                              paymentMethod,
                                                                                                              currencyConverter,
                                                                                                              auditLog,
                                                                                                              tenantRecordId,
                                                                                                              reportGroup);
        final BusinessPayment businessPayment = new BusinessPayment(invoicePaymentBaseModelDao);

        // Overdue
        final LocalDate startDate = new LocalDate(2005, 2, 5);
        final LocalDate endDate = new LocalDate(2005, 6, 5);
        final BusinessAccountTransitionModelDao businessAccountTransitionModelDao = new BusinessAccountTransitionModelDao(account,
                                                                                                                          accountRecordId,
                                                                                                                          serviceName,
                                                                                                                          stateName,
                                                                                                                          startDate,
                                                                                                                          blockingStateRecordId,
                                                                                                                          endDate,
                                                                                                                          auditLog,
                                                                                                                          tenantRecordId,
                                                                                                                          reportGroup);
        final BusinessAccountTransition businessAccountTransition = new BusinessAccountTransition(businessAccountTransitionModelDao);

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
        final BusinessSubscriptionTransition businessSubscriptionTransition = new BusinessSubscriptionTransition(subscriptionTransitionModelDao);

        // Bundle
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

        // Tag
        final BusinessAccountTagModelDao businessAccountTagModelDao = new BusinessAccountTagModelDao(account,
                                                                                                     accountRecordId,
                                                                                                     tag,
                                                                                                     tagRecordId,
                                                                                                     tagDefinition,
                                                                                                     auditLog,
                                                                                                     tenantRecordId,
                                                                                                     reportGroup);
        final BusinessTag businessTag = BusinessTag.create(businessAccountTagModelDao);

        // Create the snapshot
        final BusinessSnapshot businessSnapshot = new BusinessSnapshot(businessAccount,
                                                                       ImmutableList.<BusinessBundle>of(businessBundle),
                                                                       ImmutableList.<BusinessSubscriptionTransition>of(businessSubscriptionTransition),
                                                                       ImmutableList.<BusinessInvoice>of(businessInvoice),
                                                                       ImmutableList.<BusinessPayment>of(businessPayment),
                                                                       ImmutableList.<BusinessAccountTransition>of(businessAccountTransition),
                                                                       ImmutableList.<BusinessTag>of(businessTag),
                                                                       ImmutableList.<BusinessField>of(businessField));
        Assert.assertEquals(businessSnapshot.getBusinessAccount(), businessAccount);
        Assert.assertEquals(businessSnapshot.getBusinessBundles().size(), 1);
        Assert.assertEquals(businessSnapshot.getBusinessBundles().iterator().next(), businessBundle);
        Assert.assertEquals(businessSnapshot.getBusinessSubscriptionTransitions().size(), 1);
        Assert.assertEquals(businessSnapshot.getBusinessSubscriptionTransitions().iterator().next(), businessSubscriptionTransition);
        Assert.assertEquals(businessSnapshot.getBusinessInvoices().size(), 1);
        Assert.assertEquals(businessSnapshot.getBusinessInvoices().iterator().next(), businessInvoice);
        Assert.assertEquals(businessSnapshot.getBusinessPayments().size(), 1);
        Assert.assertEquals(businessSnapshot.getBusinessPayments().iterator().next(), businessPayment);
        Assert.assertEquals(businessSnapshot.getBusinessAccountTransitions().size(), 1);
        Assert.assertEquals(businessSnapshot.getBusinessAccountTransitions().iterator().next(), businessAccountTransition);
        Assert.assertEquals(businessSnapshot.getBusinessTags().size(), 1);
        Assert.assertEquals(businessSnapshot.getBusinessTags().iterator().next(), businessTag);
        Assert.assertEquals(businessSnapshot.getBusinessFields().size(), 1);
        Assert.assertEquals(businessSnapshot.getBusinessFields().iterator().next(), businessField);

        // We check we can write it out without exception - we can't deserialize it back (no annotation)
        // but we don't care since the APIs are read-only for Analytics
        final String asJson = ObjectMapperProvider.getJsonMapper().writeValueAsString(businessSnapshot);
    }
}
