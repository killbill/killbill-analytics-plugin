/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2021 Equinix, Inc
 * Copyright 2014-2021 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceApiException;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoiceItemType;
import org.killbill.billing.invoice.api.InvoiceStatus;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteWithEmbeddedDB;
import org.killbill.billing.plugin.analytics.api.BusinessInvoice;
import org.killbill.billing.plugin.analytics.api.BusinessSnapshot;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;
import org.killbill.billing.util.callcontext.TenantContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

public class TestBusinessInvoiceDao extends AnalyticsTestSuiteWithEmbeddedDB {

    private BusinessInvoiceDao businessInvoiceDao;
    private BusinessInvoiceAndPaymentDao businessInvoiceAndPaymentDao;

    @BeforeMethod(groups = "slow")
    public void set2Up() {
        final BusinessAccountDao businessAccountDao = new BusinessAccountDao(killbillDataSource);
        businessInvoiceDao = new BusinessInvoiceDao(killbillDataSource,
                                                    businessAccountDao,
                                                    executor);
        businessInvoiceAndPaymentDao = new BusinessInvoiceAndPaymentDao(killbillDataSource,
                                                                        businessAccountDao,
                                                                        executor);
    }

    @Test(groups = "slow")
    public void testUpdate() throws AnalyticsRefreshException {
        final BusinessSnapshot businessSnapshot1 = analyticsUserApi.getBusinessSnapshot(account.getId(), callContext);
        Assert.assertNull(businessSnapshot1.getBusinessAccount());
        Assert.assertEquals(businessSnapshot1.getBusinessInvoices().size(), 0);

        // Refresh that one invoice
        businessInvoiceDao.update(invoice.getId(), businessContextFactory);

        final BusinessSnapshot businessSnapshot2 = analyticsUserApi.getBusinessSnapshot(account.getId(), callContext);
        Assert.assertEquals(businessSnapshot2.getBusinessAccount().getAccountId(), account.getId());
        Assert.assertEquals(businessSnapshot2.getBusinessAccount().getLastInvoiceId(), invoice.getId());
        Assert.assertEquals(businessSnapshot2.getBusinessInvoices().size(), 1);
        final BusinessInvoice businessInvoice21 = businessSnapshot2.getBusinessInvoices().iterator().next();
        Assert.assertEquals(businessInvoice21.getInvoiceItems().size(), 2);
        Assert.assertTrue("EXTERNAL_CHARGE".equals(businessInvoice21.getInvoiceItems().get(0).getItemType()) && "TAX".equals(businessInvoice21.getInvoiceItems().get(1).getItemType()) ||
                          "EXTERNAL_CHARGE".equals(businessInvoice21.getInvoiceItems().get(1).getItemType()) && "TAX".equals(businessInvoice21.getInvoiceItems().get(0).getItemType()));

        final Long bacRecordId1 = analyticsSqlDao.getAccountByAccountRecordId(accountRecordId, tenantRecordId, callContext).getRecordId();
        final Long binRecordId1 = analyticsSqlDao.getInvoicesByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0).getRecordId();
        final List<BusinessInvoiceItemModelDao> invoiceItemsModelDao1 = analyticsSqlDao.getInvoiceItemsByAccountRecordId(accountRecordId, tenantRecordId, callContext);
        final Long biiRecordId11 = invoiceItemsModelDao1.get(0).getRecordId();
        final Long biiRecordId12 = invoiceItemsModelDao1.get(1).getRecordId();

        // Full invoices and payments refresh
        businessInvoiceAndPaymentDao.update(businessContextFactory);

        // Verify the state is the same
        final BusinessSnapshot businessSnapshot3 = analyticsUserApi.getBusinessSnapshot(account.getId(), callContext);
        Assert.assertEquals(businessSnapshot3.getBusinessAccount(), businessSnapshot2.getBusinessAccount());
        Assert.assertEquals(businessSnapshot3.getBusinessInvoices().size(), 1);
        Assert.assertEqualsNoOrder(businessSnapshot3.getBusinessInvoices().iterator().next().getInvoiceItems().toArray(), businessInvoice21.getInvoiceItems().toArray());

        // Verify the rows have been recreated
        final Long bacRecordId2 = analyticsSqlDao.getAccountByAccountRecordId(accountRecordId, tenantRecordId, callContext).getRecordId();
        final Long binRecordId2 = analyticsSqlDao.getInvoicesByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0).getRecordId();
        final List<BusinessInvoiceItemModelDao> invoiceItemsModelDao2 = analyticsSqlDao.getInvoiceItemsByAccountRecordId(accountRecordId, tenantRecordId, callContext);
        final Long biiRecordId21 = invoiceItemsModelDao2.get(0).getRecordId();
        final Long biiRecordId22 = invoiceItemsModelDao2.get(1).getRecordId();
        Assert.assertTrue(bacRecordId2 > bacRecordId1);
        Assert.assertTrue(binRecordId2 > binRecordId1);
        Assert.assertTrue(biiRecordId21 > Math.max(biiRecordId11, biiRecordId12));
        Assert.assertTrue(biiRecordId22 > Math.max(biiRecordId11, biiRecordId12));

        // Add a new invoice
        final UUID invoiceId2 = UUID.randomUUID();
        final InvoiceItem newInvoiceItem = createInvoiceItem(invoiceId2, InvoiceItemType.RECURRING);
        final Invoice invoice2 = Mockito.mock(Invoice.class);
        Mockito.when(invoice2.getId()).thenReturn(invoiceId2);
        Mockito.when(invoice2.getInvoiceItems()).thenReturn(ImmutableList.<InvoiceItem>of(newInvoiceItem));
        Mockito.when(invoice2.getNumberOfItems()).thenReturn(1);
        Mockito.when(invoice2.getBalance()).thenReturn(BigDecimal.ZERO);
        Mockito.when(invoice2.getCurrency()).thenReturn(Currency.EUR);
        Mockito.when(invoice2.getStatus()).thenReturn(InvoiceStatus.COMMITTED);
        final LocalDate firstInvoiceDate = invoice.getInvoiceDate();
        Mockito.when(invoice2.getInvoiceDate()).thenReturn(firstInvoiceDate.plusDays(1));
        Mockito.when(killbillAPI.getInvoiceUserApi().getInvoicesByAccount(Mockito.eq(account.getId()),
                                                                          Mockito.anyBoolean(),
                                                                          Mockito.anyBoolean(),
                                                                          Mockito.any(TenantContext.class)))
               .thenReturn(ImmutableList.of(invoice, invoice2));
        // Re-create the context to clear caches
        businessContextFactory = new BusinessContextFactory(account.getId(),
                                                            callContext,
                                                            currencyConversionDao,
                                                            killbillAPI,
                                                            osgiConfigPropertiesService,
                                                            clock,
                                                            analyticsConfigurationHandler);

        // Refresh that new invoice
        businessInvoiceDao.update(invoiceId2, businessContextFactory);

        // Verify the state
        final BusinessSnapshot businessSnapshot4 = analyticsUserApi.getBusinessSnapshot(account.getId(), callContext);
        Assert.assertEquals(businessSnapshot4.getBusinessAccount().getAccountId(), account.getId());
        Assert.assertEquals(businessSnapshot4.getBusinessAccount().getLastInvoiceId(), invoiceId2);
        Assert.assertEquals(businessSnapshot4.getBusinessInvoices().size(), 2);
        for (final BusinessInvoice businessInvoice : businessSnapshot4.getBusinessInvoices()) {
            if (businessInvoice.getInvoiceId().equals(invoice.getId())) {
                Assert.assertEquals(businessInvoice.getInvoiceItems().size(), 2);
                Assert.assertTrue("EXTERNAL_CHARGE".equals(businessInvoice.getInvoiceItems().get(0).getItemType()) && "TAX".equals(businessInvoice.getInvoiceItems().get(1).getItemType()) ||
                                  "EXTERNAL_CHARGE".equals(businessInvoice.getInvoiceItems().get(1).getItemType()) && "TAX".equals(businessInvoice.getInvoiceItems().get(0).getItemType()));
            } else {
                Assert.assertEquals(businessInvoice.getInvoiceId(), invoiceId2);
                Assert.assertEquals(businessInvoice.getInvoiceItems().size(), 1);
                Assert.assertEquals(businessInvoice.getInvoiceItems().get(0).getItemType(), "RECURRING");
            }
        }

        // Verify only the rows associated with that new invoice have changed
        final Long bacRecordId3 = analyticsSqlDao.getAccountByAccountRecordId(accountRecordId, tenantRecordId, callContext).getRecordId();
        Assert.assertTrue(bacRecordId3 > bacRecordId2);
        final List<BusinessInvoiceModelDao> invoicesByAccountRecordId3 = analyticsSqlDao.getInvoicesByAccountRecordId(accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(invoicesByAccountRecordId3.size(), 2);
        for (final BusinessInvoiceModelDao businessInvoiceModelDao : invoicesByAccountRecordId3) {
            if (businessInvoiceModelDao.getInvoiceId().equals(invoice.getId())) {
                // Row hasn't changed
                Assert.assertEquals(businessInvoiceModelDao.getRecordId(), binRecordId2);
            } else {
                Assert.assertTrue(businessInvoiceModelDao.getRecordId() > binRecordId2);
            }
        }
        final List<BusinessInvoiceItemModelDao> invoiceItemsByAccountRecordId3 = analyticsSqlDao.getInvoiceItemsByAccountRecordId(accountRecordId, tenantRecordId, callContext);
        Assert.assertEquals(invoiceItemsByAccountRecordId3.size(), 3);
        for (final BusinessInvoiceItemModelDao businessInvoiceItemModelDao : invoiceItemsByAccountRecordId3) {
            if (businessInvoiceItemModelDao.getItemId().equals(invoiceItem.getId()) || businessInvoiceItemModelDao.getItemId().equals(linkedInvoiceItem.getId())) {
                // Row hasn't changed
                Assert.assertTrue(businessInvoiceItemModelDao.getRecordId().equals(biiRecordId21) || businessInvoiceItemModelDao.getRecordId().equals(biiRecordId22));
            } else {
                Assert.assertEquals(businessInvoiceItemModelDao.getItemId(), newInvoiceItem.getId());
                Assert.assertTrue(businessInvoiceItemModelDao.getRecordId() > Math.max(biiRecordId11, biiRecordId12));
            }
        }
    }

    @Test(groups = "slow")
    public void testUpdateToVoid() throws AnalyticsRefreshException, InvoiceApiException {
        final BusinessSnapshot businessSnapshot1 = analyticsUserApi.getBusinessSnapshot(account.getId(), callContext);
        Assert.assertNull(businessSnapshot1.getBusinessAccount());
        Assert.assertEquals(businessSnapshot1.getBusinessInvoices().size(), 0);

        // Refresh that one invoice
        businessInvoiceDao.update(invoice.getId(), businessContextFactory);

        final BusinessSnapshot businessSnapshot2 = analyticsUserApi.getBusinessSnapshot(account.getId(), callContext);
        Assert.assertEquals(businessSnapshot2.getBusinessAccount().getAccountId(), account.getId());
        Assert.assertEquals(businessSnapshot2.getBusinessAccount().getLastInvoiceId(), invoice.getId());
        Assert.assertEquals(businessSnapshot2.getBusinessInvoices().size(), 1);
        final BusinessInvoice businessInvoice21 = businessSnapshot2.getBusinessInvoices().iterator().next();
        Assert.assertEquals(businessInvoice21.getInvoiceItems().size(), 2);
        Assert.assertTrue("EXTERNAL_CHARGE".equals(businessInvoice21.getInvoiceItems().get(0).getItemType()) && "TAX".equals(businessInvoice21.getInvoiceItems().get(1).getItemType()) ||
                          "EXTERNAL_CHARGE".equals(businessInvoice21.getInvoiceItems().get(1).getItemType()) && "TAX".equals(businessInvoice21.getInvoiceItems().get(0).getItemType()));

        final Long bacRecordId1 = analyticsSqlDao.getAccountByAccountRecordId(accountRecordId, tenantRecordId, callContext).getRecordId();
        final Long binRecordId1 = analyticsSqlDao.getInvoicesByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0).getRecordId();
        final List<BusinessInvoiceItemModelDao> invoiceItemsModelDao1 = analyticsSqlDao.getInvoiceItemsByAccountRecordId(accountRecordId, tenantRecordId, callContext);
        final Long biiRecordId11 = invoiceItemsModelDao1.get(0).getRecordId();
        final Long biiRecordId12 = invoiceItemsModelDao1.get(1).getRecordId();

        // Full invoices and payments refresh
        businessInvoiceAndPaymentDao.update(businessContextFactory);

        // Verify the state is the same
        final BusinessSnapshot businessSnapshot3 = analyticsUserApi.getBusinessSnapshot(account.getId(), callContext);
        Assert.assertEquals(businessSnapshot3.getBusinessAccount(), businessSnapshot2.getBusinessAccount());
        Assert.assertEquals(businessSnapshot3.getBusinessInvoices().size(), 1);
        Assert.assertEqualsNoOrder(businessSnapshot3.getBusinessInvoices().iterator().next().getInvoiceItems().toArray(), businessInvoice21.getInvoiceItems().toArray());

        // Verify the rows have been recreated
        final Long bacRecordId2 = analyticsSqlDao.getAccountByAccountRecordId(accountRecordId, tenantRecordId, callContext).getRecordId();
        final Long binRecordId2 = analyticsSqlDao.getInvoicesByAccountRecordId(accountRecordId, tenantRecordId, callContext).get(0).getRecordId();
        final List<BusinessInvoiceItemModelDao> invoiceItemsModelDao2 = analyticsSqlDao.getInvoiceItemsByAccountRecordId(accountRecordId, tenantRecordId, callContext);
        final Long biiRecordId21 = invoiceItemsModelDao2.get(0).getRecordId();
        final Long biiRecordId22 = invoiceItemsModelDao2.get(1).getRecordId();
        Assert.assertTrue(bacRecordId2 > bacRecordId1);
        Assert.assertTrue(binRecordId2 > binRecordId1);
        Assert.assertTrue(biiRecordId21 > Math.max(biiRecordId11, biiRecordId12));
        Assert.assertTrue(biiRecordId22 > Math.max(biiRecordId11, biiRecordId12));

        // Void the invoice
        Mockito.when(invoice.getStatus()).thenReturn(InvoiceStatus.VOID);
        Mockito.when(killbillAPI.getInvoiceUserApi().getInvoice(Mockito.eq(invoice.getId()),
                                                                Mockito.any(TenantContext.class)))
               .thenReturn(invoice);
        Mockito.when(killbillAPI.getInvoiceUserApi().getInvoicesByAccount(Mockito.eq(account.getId()),
                                                                          Mockito.anyBoolean(),
                                                                          Mockito.eq(false),
                                                                          Mockito.any(TenantContext.class)))
               .thenReturn(ImmutableList.of());

        // Re-create the context to clear caches
        businessContextFactory = new BusinessContextFactory(account.getId(),
                                                            callContext,
                                                            currencyConversionDao,
                                                            killbillAPI,
                                                            osgiConfigPropertiesService,
                                                            clock,
                                                            analyticsConfigurationHandler);

        // Refresh that one invoice
        businessInvoiceDao.update(invoice.getId(), businessContextFactory);

        final BusinessSnapshot businessSnapshot4 = analyticsUserApi.getBusinessSnapshot(account.getId(), callContext);
        Assert.assertEquals(businessSnapshot4.getBusinessAccount().getAccountId(), account.getId());
        Assert.assertNull(businessSnapshot4.getBusinessAccount().getLastInvoiceId());
        Assert.assertEquals(businessSnapshot4.getBusinessInvoices().size(), 0);
    }
}
