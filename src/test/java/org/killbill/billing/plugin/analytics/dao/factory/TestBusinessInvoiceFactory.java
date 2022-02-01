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

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.entitlement.api.Subscription;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoiceItemType;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.BusinessExecutor;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao.ItemSource;
import org.killbill.billing.plugin.analytics.utils.BusinessInvoiceUtils;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class TestBusinessInvoiceFactory extends AnalyticsTestSuiteNoDB {

    private BusinessInvoiceFactory invoiceFactory;

    @Override
    @BeforeMethod(groups = "fast")
    public void setUp() throws Exception {
        super.setUp();

        final OSGIKillbillDataSource osgiKillbillDataSource = Mockito.mock(OSGIKillbillDataSource.class);

        final DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(osgiKillbillDataSource.getDataSource()).thenReturn(dataSource);

        invoiceFactory = new BusinessInvoiceFactory(BusinessExecutor.newCachedThreadPool(osgiConfigPropertiesService));
    }

    @Test(groups = "fast", description = "Regression test for an NPE in a specific scenario")
    public void testGenerateBusinessItemForAdjustmentWithTax() throws Exception {
        final UUID invoiceId = UUID.randomUUID();

        final BusinessContextFactory businessContextFactory = Mockito.mock(BusinessContextFactory.class);

        final InvoiceItem recurringItem = createInvoiceItem(invoiceId, InvoiceItemType.RECURRING, BigDecimal.TEN);

        final InvoiceItem adjustmentItem = Mockito.mock(InvoiceItem.class);
        Mockito.when(adjustmentItem.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(adjustmentItem.getInvoiceItemType()).thenReturn(InvoiceItemType.ITEM_ADJ);
        Mockito.when(adjustmentItem.getInvoiceId()).thenReturn(invoiceId);
        final UUID recurringItemId = recurringItem.getId();
        Mockito.when(adjustmentItem.getLinkedItemId()).thenReturn(recurringItemId);
        Mockito.when(adjustmentItem.getAmount()).thenReturn(BigDecimal.ONE.negate());
        Mockito.when(adjustmentItem.getCurrency()).thenReturn(Currency.EUR);

        final InvoiceItem taxItem = Mockito.mock(InvoiceItem.class);
        Mockito.when(taxItem.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(taxItem.getInvoiceItemType()).thenReturn(InvoiceItemType.TAX);
        Mockito.when(taxItem.getInvoiceId()).thenReturn(invoiceId);
        Mockito.when(taxItem.getAmount()).thenReturn(BigDecimal.ONE);
        Mockito.when(taxItem.getCurrency()).thenReturn(Currency.EUR);

        final Subscription subscription = Mockito.mock(Subscription.class);
        final UUID subscriptionId = recurringItem.getSubscriptionId();
        Mockito.when(subscription.getId()).thenReturn(subscriptionId);
        final SubscriptionBundle bundle = Mockito.mock(SubscriptionBundle.class);
        Mockito.when(bundle.getSubscriptions()).thenReturn(ImmutableList.<Subscription>of(subscription));
        final Map<UUID, SubscriptionBundle> bundles = ImmutableMap.<UUID, SubscriptionBundle>of(recurringItem.getBundleId(), bundle);

        final BusinessInvoiceItemBaseModelDao businessItem = invoiceFactory.createBusinessInvoiceItem(businessContextFactory,
                                                                                                      account,
                                                                                                      invoice,
                                                                                                      adjustmentItem,
                                                                                                      ImmutableList.<InvoiceItem>of(taxItem, recurringItem),
                                                                                                      recurringItem,
                                                                                                      false,
                                                                                                      currencyConverter,
                                                                                                      auditLog,
                                                                                                      accountRecordId,
                                                                                                      tenantRecordId,
                                                                                                      reportGroup);

        Assert.assertEquals(businessItem.getAmount().compareTo(BigDecimal.ONE.negate()), 0);
        Assert.assertEquals(businessItem.getItemType(), InvoiceItemType.ITEM_ADJ.toString());
    }

    @Test(groups = "fast")
    public void testRevenueRecognizableClassicAccountCredit() throws Exception {
        final UUID invoiceId = UUID.randomUUID();

        // Classic account credit ($10), from the perspective of the CREDIT_ADJ item
        final BusinessInvoiceItemBaseModelDao businessCreditAdjItem = invoiceFactory.createBusinessInvoiceItem(account,
                                                                                                               invoice,
                                                                                                               createInvoiceItem(invoiceId, InvoiceItemType.CREDIT_ADJ, new BigDecimal("-10")),
                                                                                                               ImmutableList.<InvoiceItem>of(createInvoiceItem(invoiceId, InvoiceItemType.CBA_ADJ, new BigDecimal("10"))),
                                                                                                               false,
                                                                                                               null,
                                                                                                               null,
                                                                                                               null,
                                                                                                               invoiceItemRecordId,
                                                                                                               currencyConverter,
                                                                                                               auditLog,
                                                                                                               accountRecordId,
                                                                                                               tenantRecordId,
                                                                                                               reportGroup);
        // We ignore these
        Assert.assertNull(businessCreditAdjItem);

        // Classic account credit ($10), from the perspective of the CBA_ADJ item
        final BusinessInvoiceItemBaseModelDao businessCreditItem = invoiceFactory.createBusinessInvoiceItem(account,
                                                                                                            invoice,
                                                                                                            createInvoiceItem(invoiceId, InvoiceItemType.CBA_ADJ, new BigDecimal("10")),
                                                                                                            ImmutableList.<InvoiceItem>of(createInvoiceItem(invoiceId, InvoiceItemType.CREDIT_ADJ, new BigDecimal("-10"))),
                                                                                                            false,
                                                                                                            null,
                                                                                                            null,
                                                                                                            null,
                                                                                                            invoiceItemRecordId,
                                                                                                            currencyConverter,
                                                                                                            auditLog,
                                                                                                            accountRecordId,
                                                                                                            tenantRecordId,
                                                                                                            reportGroup);
        // We treat these as NOT recognizable account credits
        Assert.assertEquals(businessCreditItem.getAmount().compareTo(new BigDecimal("10")), 0);
        Assert.assertEquals(businessCreditItem.getItemType(), InvoiceItemType.CBA_ADJ.toString());
        Assert.assertEquals(businessCreditItem.getItemSource(), ItemSource.user.toString());

        // Invoice adjustment, not to be mixed with credits!
        final BusinessInvoiceItemBaseModelDao businessInvoiceAdjustmentItem = invoiceFactory.createBusinessInvoiceItem(account,
                                                                                                                       invoice,
                                                                                                                       createInvoiceItem(invoiceId, InvoiceItemType.CREDIT_ADJ, new BigDecimal("-10")),
                                                                                                                       ImmutableList.<InvoiceItem>of(createInvoiceItem(invoiceId, InvoiceItemType.RECURRING, new BigDecimal("10"))),
                                                                                                                       false,
                                                                                                                       null,
                                                                                                                       null,
                                                                                                                       null,
                                                                                                                       invoiceItemRecordId,
                                                                                                                       currencyConverter,
                                                                                                                       auditLog,
                                                                                                                       accountRecordId,
                                                                                                                       tenantRecordId,
                                                                                                                       reportGroup);
        Assert.assertEquals(businessInvoiceAdjustmentItem.getAmount().compareTo(new BigDecimal("-10")), 0);
        Assert.assertEquals(businessInvoiceAdjustmentItem.getItemType(), InvoiceItemType.CREDIT_ADJ.toString());
        Assert.assertEquals(businessInvoiceAdjustmentItem.getItemSource(), ItemSource.user.toString());

        // Item adjustment
        final BusinessInvoiceItemBaseModelDao businessInvoiceItemAdjustmentItem = invoiceFactory.createBusinessInvoiceItem(account,
                                                                                                                           invoice,
                                                                                                                           createInvoiceItem(invoiceId, InvoiceItemType.ITEM_ADJ, new BigDecimal("-10")),
                                                                                                                           ImmutableList.<InvoiceItem>of(createInvoiceItem(invoiceId, InvoiceItemType.RECURRING, new BigDecimal("10"))),
                                                                                                                           false,
                                                                                                                           null,
                                                                                                                           null,
                                                                                                                           null,
                                                                                                                           invoiceItemRecordId,
                                                                                                                           currencyConverter,
                                                                                                                           auditLog,
                                                                                                                           accountRecordId,
                                                                                                                           tenantRecordId,
                                                                                                                           reportGroup);
        Assert.assertEquals(businessInvoiceItemAdjustmentItem.getAmount().compareTo(new BigDecimal("-10")), 0);
        Assert.assertEquals(businessInvoiceItemAdjustmentItem.getItemType(), InvoiceItemType.ITEM_ADJ.toString());
        Assert.assertEquals(businessInvoiceItemAdjustmentItem.getItemSource(), ItemSource.user.toString());

        // System generated account credit
        final BusinessInvoiceItemBaseModelDao businessCBAItem = invoiceFactory.createBusinessInvoiceItem(account,
                                                                                                         invoice,
                                                                                                         createInvoiceItem(invoiceId, InvoiceItemType.CBA_ADJ, new BigDecimal("10")),
                                                                                                         ImmutableList.<InvoiceItem>of(createInvoiceItem(invoiceId, InvoiceItemType.RECURRING, new BigDecimal("30")),
                                                                                                                                       createInvoiceItem(invoiceId, InvoiceItemType.REPAIR_ADJ, new BigDecimal("-30")),
                                                                                                                                       createInvoiceItem(invoiceId, InvoiceItemType.RECURRING, new BigDecimal("20"))),
                                                                                                         false,
                                                                                                         null,
                                                                                                         null,
                                                                                                         null,
                                                                                                         invoiceItemRecordId,
                                                                                                         currencyConverter,
                                                                                                         auditLog,
                                                                                                         accountRecordId,
                                                                                                         tenantRecordId,
                                                                                                         reportGroup
                                                                                                        );
        Assert.assertEquals(businessCBAItem.getAmount().compareTo(new BigDecimal("10")), 0);
        Assert.assertEquals(businessCBAItem.getItemType(), InvoiceItemType.CBA_ADJ.toString());
        Assert.assertEquals(businessCBAItem.getItemSource(), BusinessInvoiceItemBaseModelDao.DEFAULT_ITEM_SOURCE);
    }

    @Test(groups = "fast")
    public void testInvoiceAdjustment() throws Exception {
        final UUID invoiceId = UUID.randomUUID();

        Assert.assertFalse(BusinessInvoiceUtils.isInvoiceAdjustmentItem(createInvoiceItem(invoiceId, InvoiceItemType.RECURRING),
                                                                        ImmutableList.<InvoiceItem>of()));

        final InvoiceItem creditAdj = createInvoiceItem(invoiceId, InvoiceItemType.CREDIT_ADJ);

        // Account credit
        Assert.assertFalse(BusinessInvoiceUtils.isInvoiceAdjustmentItem(creditAdj,
                                                                        ImmutableList.<InvoiceItem>of(createInvoiceItem(invoiceId, InvoiceItemType.CBA_ADJ, creditAdj.getAmount().negate()))));

        Assert.assertTrue(BusinessInvoiceUtils.isInvoiceAdjustmentItem(creditAdj,
                                                                       ImmutableList.<InvoiceItem>of(createInvoiceItem(invoiceId, InvoiceItemType.CBA_ADJ, creditAdj.getAmount().negate().add(BigDecimal.ONE)))));
        Assert.assertTrue(BusinessInvoiceUtils.isInvoiceAdjustmentItem(creditAdj,
                                                                       ImmutableList.<InvoiceItem>of(createInvoiceItem(invoiceId, InvoiceItemType.RECURRING),
                                                                                                     createInvoiceItem(invoiceId, InvoiceItemType.CBA_ADJ, creditAdj.getAmount().negate()))
                                                                      ));
    }
}
