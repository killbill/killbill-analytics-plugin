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

package org.killbill.billing.plugin.analytics.dao;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.invoice.api.InvoicePaymentType;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteWithEmbeddedDB;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentPurchaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscription;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionEvent;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessTagModelDao;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestBusinessAnalyticsSqlDao extends AnalyticsTestSuiteWithEmbeddedDB {

    @Test(groups = "slow")
    public void testSqlDaoForAccount() throws Exception {
        final BusinessAccountModelDao accountModelDao = new BusinessAccountModelDao(account,
                                                                                    accountRecordId,
                                                                                    new BigDecimal("1.2345"),
                                                                                    invoice,
                                                                                    invoice,
                                                                                    paymentTransaction,
                                                                                    3,
                                                                                    currencyConverter,
                                                                                    auditLog,
                                                                                    tenantRecordId,
                                                                                    reportGroup);

        // Check the record doesn't exist yet
        Assert.assertNull(analyticsSqlDao.getAccountByAccountRecordId(accountModelDao.getAccountRecordId(),
                                                                      accountModelDao.getTenantRecordId(),
                                                                      callContext));

        // Create and check we can retrieve it
        analyticsSqlDao.create(accountModelDao.getTableName(), accountModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getAccountByAccountRecordId(accountModelDao.getAccountRecordId(),
                                                                        accountModelDao.getTenantRecordId(),
                                                                        callContext), accountModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(accountModelDao.getTableName(),
                                                accountModelDao.getAccountRecordId(),
                                                accountModelDao.getTenantRecordId(),
                                                callContext);
        Assert.assertNull(analyticsSqlDao.getAccountByAccountRecordId(accountModelDao.getAccountRecordId(),
                                                                      accountModelDao.getTenantRecordId(),
                                                                      callContext));
    }

    @Test(groups = "slow")
    public void testSqlDaoForAccountField() throws Exception {
        final BusinessFieldModelDao businessFieldModelDao = new BusinessAccountFieldModelDao(account,
                                                                                             accountRecordId,
                                                                                             customField,
                                                                                             fieldRecordId,
                                                                                             auditLog,
                                                                                             tenantRecordId,
                                                                                             reportGroup);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessFieldModelDao.getTableName(), businessFieldModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessFieldModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessFieldModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForBundleField() throws Exception {
        final BusinessFieldModelDao businessFieldModelDao = new BusinessBundleFieldModelDao(account,
                                                                                            accountRecordId,
                                                                                            bundle,
                                                                                            customField,
                                                                                            fieldRecordId,
                                                                                            auditLog,
                                                                                            tenantRecordId,
                                                                                            reportGroup);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getBundleFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessFieldModelDao.getTableName(), businessFieldModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getBundleFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getBundleFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessFieldModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessFieldModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getBundleFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForInvoiceField() throws Exception {
        final BusinessFieldModelDao businessFieldModelDao = new BusinessInvoiceFieldModelDao(account,
                                                                                             accountRecordId,
                                                                                             customField,
                                                                                             fieldRecordId,
                                                                                             auditLog,
                                                                                             tenantRecordId,
                                                                                             reportGroup);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getInvoiceFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessFieldModelDao.getTableName(), businessFieldModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoiceFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getInvoiceFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessFieldModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessFieldModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoiceFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForInvoicePaymentField() throws Exception {
        final BusinessFieldModelDao businessFieldModelDao = new BusinessInvoicePaymentFieldModelDao(account,
                                                                                                    accountRecordId,
                                                                                                    customField,
                                                                                                    fieldRecordId,
                                                                                                    auditLog,
                                                                                                    tenantRecordId,
                                                                                                    reportGroup);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getInvoicePaymentFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessFieldModelDao.getTableName(), businessFieldModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoicePaymentFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getInvoicePaymentFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessFieldModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessFieldModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoicePaymentFieldsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForInvoice() throws Exception {
        final BusinessInvoiceModelDao businessInvoiceModelDao = new BusinessInvoiceModelDao(account,
                                                                                            accountRecordId,
                                                                                            invoice,
                                                                                            invoiceRecordId,
                                                                                            currencyConverter,
                                                                                            auditLog,
                                                                                            tenantRecordId,
                                                                                            reportGroup);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getInvoicesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessInvoiceModelDao.getTableName(), businessInvoiceModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoicesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getInvoicesByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessInvoiceModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessInvoiceModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoicesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForInvoiceItem() throws Exception {
        final BusinessInvoiceItemBaseModelDao businessInvoiceItemModelDao = BusinessInvoiceItemBaseModelDao.create(account,
                                                                                                                   accountRecordId,
                                                                                                                   invoice,
                                                                                                                   invoiceItem,
                                                                                                                   itemSource,
                                                                                                                   // ITEM_ADJ
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
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getInvoiceItemAdjustmentsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessInvoiceItemModelDao.getTableName(), businessInvoiceItemModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoiceItemAdjustmentsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getInvoiceItemAdjustmentsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessInvoiceItemModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessInvoiceItemModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoiceItemAdjustmentsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForPayment() throws Exception {
        final BusinessPaymentBaseModelDao businessInvoicePaymentModelDao = BusinessPaymentPurchaseModelDao.create(account,
                                                                                                                  accountRecordId,
                                                                                                                  invoice,
                                                                                                                  invoicePayment,
                                                                                                                  invoicePaymentRecordId,
                                                                                                                  paymentNoRefund,
                                                                                                                  purchaseTransaction,
                                                                                                                  paymentMethod,
                                                                                                                  currencyConverter,
                                                                                                                  auditLog,
                                                                                                                  tenantRecordId,
                                                                                                                  reportGroup,
                                                                                                                  pluginPropertiesManager);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getPaymentPurchasesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessInvoicePaymentModelDao.getTableName(), businessInvoicePaymentModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getPaymentPurchasesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getPaymentPurchasesByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessInvoicePaymentModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessInvoicePaymentModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getPaymentPurchasesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForPaymentRefund() throws Exception {
        Mockito.when(invoicePayment.getType()).thenReturn(InvoicePaymentType.REFUND);
        final BusinessPaymentBaseModelDao businessInvoicePaymentRefundModelDao = BusinessPaymentPurchaseModelDao.create(account,
                                                                                                                        accountRecordId,
                                                                                                                        invoice,
                                                                                                                        invoicePayment,
                                                                                                                        invoicePaymentRecordId,
                                                                                                                        payment,
                                                                                                                        refundTransaction,
                                                                                                                        paymentMethod,
                                                                                                                        currencyConverter,
                                                                                                                        auditLog,
                                                                                                                        tenantRecordId,
                                                                                                                        reportGroup,
                                                                                                                        pluginPropertiesManager);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getPaymentPurchasesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessInvoicePaymentRefundModelDao.getTableName(), businessInvoicePaymentRefundModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getPaymentRefundsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getPaymentRefundsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessInvoicePaymentRefundModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessInvoicePaymentRefundModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getPaymentRefundsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForOverdueStatus() throws Exception {
        final LocalDate startDate = new LocalDate(2005, 6, 5);
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
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getPaymentPurchasesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessAccountTransitionModelDao.getTableName(), businessAccountTransitionModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getAccountTransitionsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getAccountTransitionsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessAccountTransitionModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessAccountTransitionModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getAccountTransitionsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForSubscriptionTransition() throws Exception {
        final LocalDate startDate = new LocalDate(2012, 6, 5);

        final BusinessSubscriptionEvent event = BusinessSubscriptionEvent.valueOf("START_ENTITLEMENT_BASE");
        final BusinessSubscription previousSubscription = null;
        final BusinessSubscription nextSubscription = new BusinessSubscription(null, null, null, Currency.GBP, startDate, serviceName, stateName, currencyConverter);
        final BusinessSubscriptionTransitionModelDao businessSubscriptionTransitionModelDao = new BusinessSubscriptionTransitionModelDao(account,
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
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getSubscriptionTransitionsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessSubscriptionTransitionModelDao.getTableName(), businessSubscriptionTransitionModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getSubscriptionTransitionsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getSubscriptionTransitionsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessSubscriptionTransitionModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessSubscriptionTransitionModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getSubscriptionTransitionsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForBundleSummary() throws Exception {
        final LocalDate startDate = new LocalDate(2012, 6, 5);

        final BusinessSubscriptionEvent event = BusinessSubscriptionEvent.valueOf("START_ENTITLEMENT_BASE");
        final BusinessSubscription previousSubscription = null;
        final BusinessSubscription nextSubscription = new BusinessSubscription(null, null, null, Currency.GBP, startDate, serviceName, stateName, currencyConverter);
        final BusinessSubscriptionTransitionModelDao businessSubscriptionTransitionModelDao = new BusinessSubscriptionTransitionModelDao(account,
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
                                                                                        businessSubscriptionTransitionModelDao,
                                                                                        currencyConverter,
                                                                                        auditLog,
                                                                                        tenantRecordId,
                                                                                        reportGroup);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getBundlesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(bundleSummaryModelDao.getTableName(), bundleSummaryModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getBundlesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getBundlesByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), bundleSummaryModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(bundleSummaryModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getBundlesByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForAccountTag() throws Exception {
        final BusinessTagModelDao businessTagModelDao = new BusinessAccountTagModelDao(account,
                                                                                       accountRecordId,
                                                                                       tag,
                                                                                       tagRecordId,
                                                                                       tagDefinition,
                                                                                       auditLog,
                                                                                       tenantRecordId,
                                                                                       reportGroup);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getAccountTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessTagModelDao.getTableName(), businessTagModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getAccountTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getAccountTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessTagModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessTagModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getAccountTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForBundleTag() throws Exception {
        final BusinessTagModelDao businessTagModelDao = new BusinessBundleTagModelDao(account,
                                                                                      accountRecordId,
                                                                                      bundle,
                                                                                      tag,
                                                                                      tagRecordId,
                                                                                      tagDefinition,
                                                                                      auditLog,
                                                                                      tenantRecordId,
                                                                                      reportGroup);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getBundleTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessTagModelDao.getTableName(), businessTagModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getBundleTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getBundleTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessTagModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessTagModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getBundleTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForInvoiceTag() throws Exception {
        final BusinessTagModelDao businessTagModelDao = new BusinessInvoiceTagModelDao(account,
                                                                                       accountRecordId,
                                                                                       tag,
                                                                                       tagRecordId,
                                                                                       tagDefinition,
                                                                                       auditLog,
                                                                                       tenantRecordId,
                                                                                       reportGroup);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getInvoiceTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessTagModelDao.getTableName(), businessTagModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoiceTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getInvoiceTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessTagModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessTagModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoiceTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }

    @Test(groups = "slow")
    public void testSqlDaoForInvoicePaymentTag() throws Exception {
        final BusinessTagModelDao businessTagModelDao = new BusinessInvoicePaymentTagModelDao(account,
                                                                                              accountRecordId,
                                                                                              tag,
                                                                                              tagRecordId,
                                                                                              tagDefinition,
                                                                                              auditLog,
                                                                                              tenantRecordId,
                                                                                              reportGroup);
        // Check the record doesn't exist yet
        Assert.assertEquals(analyticsSqlDao.getInvoicePaymentTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);

        // Create and check we can retrieve it
        analyticsSqlDao.create(businessTagModelDao.getTableName(), businessTagModelDao, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoicePaymentTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 1);
        Assert.assertEquals(analyticsSqlDao.getInvoicePaymentTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0), businessTagModelDao);

        // Delete and verify it doesn't exist anymore
        analyticsSqlDao.deleteByAccountRecordId(businessTagModelDao.getTableName(), accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(analyticsSqlDao.getInvoicePaymentTagsByAccountRecordId(accountRecordId, tenantRecordId, callContext).size(), 0);
    }
}
