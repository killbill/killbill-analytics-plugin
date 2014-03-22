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

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

public class TestBusinessInvoice extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
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
        verifyBusinessEntityBase(businessInvoice);
        Assert.assertEquals(businessInvoice.getCreatedDate(), invoiceModelDao.getCreatedDate());
        Assert.assertEquals(businessInvoice.getInvoiceId(), invoiceModelDao.getInvoiceId());
        Assert.assertEquals(businessInvoice.getInvoiceNumber(), invoiceModelDao.getInvoiceNumber());
        Assert.assertEquals(businessInvoice.getInvoiceDate().compareTo(invoiceModelDao.getInvoiceDate()), 0);
        Assert.assertEquals(businessInvoice.getTargetDate().compareTo(invoiceModelDao.getTargetDate()), 0);
        Assert.assertEquals(businessInvoice.getCurrency(), invoiceModelDao.getCurrency());
        Assert.assertEquals(businessInvoice.getBalance().compareTo(invoiceModelDao.getBalance()), 0);
        Assert.assertEquals(businessInvoice.getConvertedBalance().compareTo(invoiceModelDao.getConvertedBalance()), 0);
        Assert.assertEquals(businessInvoice.getAmountPaid().compareTo(invoiceModelDao.getAmountPaid()), 0);
        Assert.assertEquals(businessInvoice.getConvertedAmountPaid().compareTo(invoiceModelDao.getConvertedAmountPaid()), 0);
        Assert.assertEquals(businessInvoice.getAmountCharged().compareTo(invoiceModelDao.getAmountCharged()), 0);
        Assert.assertEquals(businessInvoice.getConvertedAmountCharged().compareTo(invoiceModelDao.getConvertedAmountCharged()), 0);
        Assert.assertEquals(businessInvoice.getOriginalAmountCharged().compareTo(invoiceModelDao.getOriginalAmountCharged()), 0);
        Assert.assertEquals(businessInvoice.getConvertedOriginalAmountCharged().compareTo(invoiceModelDao.getConvertedOriginalAmountCharged()), 0);
        Assert.assertEquals(businessInvoice.getAmountCredited().compareTo(invoiceModelDao.getAmountCredited()), 0);
        Assert.assertEquals(businessInvoice.getConvertedAmountCredited().compareTo(invoiceModelDao.getConvertedAmountCredited()), 0);
        Assert.assertEquals(businessInvoice.getAmountRefunded().compareTo(invoiceModelDao.getAmountRefunded()), 0);
        Assert.assertEquals(businessInvoice.getConvertedAmountRefunded().compareTo(invoiceModelDao.getConvertedAmountRefunded()), 0);
        Assert.assertEquals(businessInvoice.getConvertedCurrency(), invoiceModelDao.getConvertedCurrency());

        Assert.assertEquals(businessInvoice.getInvoiceItems().size(), 1);

        final BusinessInvoiceItem businessInvoiceItem = businessInvoice.getInvoiceItems().get(0);
        verifyBusinessEntityBase(businessInvoiceItem);
        Assert.assertEquals(businessInvoiceItem.getCreatedDate().compareTo(invoiceItemBaseModelDao.getCreatedDate()), 0);
        Assert.assertEquals(businessInvoiceItem.getItemId(), invoiceItemBaseModelDao.getItemId());
        Assert.assertEquals(businessInvoiceItem.getInvoiceId(), invoiceItemBaseModelDao.getInvoiceId());
        Assert.assertEquals(businessInvoiceItem.getInvoiceNumber(), invoiceItemBaseModelDao.getInvoiceNumber());
        Assert.assertEquals(businessInvoiceItem.getInvoiceCreatedDate().compareTo(invoiceItemBaseModelDao.getInvoiceCreatedDate()), 0);
        Assert.assertEquals(businessInvoiceItem.getInvoiceDate().compareTo(invoiceItemBaseModelDao.getInvoiceDate()), 0);
        Assert.assertEquals(businessInvoiceItem.getInvoiceTargetDate().compareTo(invoiceItemBaseModelDao.getInvoiceTargetDate()), 0);
        Assert.assertEquals(businessInvoiceItem.getInvoiceCurrency(), invoiceItemBaseModelDao.getInvoiceCurrency());
        Assert.assertEquals(businessInvoiceItem.getInvoiceBalance().compareTo(invoiceItemBaseModelDao.getInvoiceBalance()), 0);
        Assert.assertEquals(businessInvoiceItem.getConvertedInvoiceBalance().compareTo(invoiceItemBaseModelDao.getConvertedInvoiceBalance()), 0);
        Assert.assertEquals(businessInvoiceItem.getInvoiceAmountPaid().compareTo(invoiceItemBaseModelDao.getInvoiceAmountPaid()), 0);
        Assert.assertEquals(businessInvoiceItem.getConvertedInvoiceAmountPaid().compareTo(invoiceItemBaseModelDao.getConvertedInvoiceAmountPaid()), 0);
        Assert.assertEquals(businessInvoiceItem.getInvoiceAmountCharged().compareTo(invoiceItemBaseModelDao.getInvoiceAmountCharged()), 0);
        Assert.assertEquals(businessInvoiceItem.getConvertedInvoiceAmountCharged().compareTo(invoiceItemBaseModelDao.getConvertedInvoiceAmountCharged()), 0);
        Assert.assertEquals(businessInvoiceItem.getInvoiceOriginalAmountCharged().compareTo(invoiceItemBaseModelDao.getInvoiceOriginalAmountCharged()), 0);
        Assert.assertEquals(businessInvoiceItem.getConvertedInvoiceOriginalAmountCharged().compareTo(invoiceItemBaseModelDao.getConvertedInvoiceOriginalAmountCharged()), 0);
        Assert.assertEquals(businessInvoiceItem.getInvoiceAmountCredited().compareTo(invoiceItemBaseModelDao.getInvoiceAmountCredited()), 0);
        Assert.assertEquals(businessInvoiceItem.getConvertedInvoiceAmountCredited().compareTo(invoiceItemBaseModelDao.getConvertedInvoiceAmountCredited()), 0);
        Assert.assertEquals(businessInvoiceItem.getInvoiceAmountRefunded().compareTo(invoiceItemBaseModelDao.getInvoiceAmountRefunded()), 0);
        Assert.assertEquals(businessInvoiceItem.getConvertedInvoiceAmountRefunded().compareTo(invoiceItemBaseModelDao.getConvertedInvoiceAmountRefunded()), 0);
        Assert.assertEquals(businessInvoiceItem.getItemType(), invoiceItemBaseModelDao.getItemType());
        Assert.assertEquals(businessInvoiceItem.getItemSource(), invoiceItemBaseModelDao.getItemSource());
        Assert.assertEquals(businessInvoiceItem.getBundleId(), invoiceItemBaseModelDao.getBundleId());
        Assert.assertEquals(businessInvoiceItem.getBundleExternalKey(), invoiceItemBaseModelDao.getBundleExternalKey());
        Assert.assertEquals(businessInvoiceItem.getProductName(), invoiceItemBaseModelDao.getProductName());
        Assert.assertEquals(businessInvoiceItem.getProductType(), invoiceItemBaseModelDao.getProductType());
        Assert.assertEquals(businessInvoiceItem.getProductCategory(), invoiceItemBaseModelDao.getProductCategory());
        Assert.assertEquals(businessInvoiceItem.getSlug(), invoiceItemBaseModelDao.getSlug());
        Assert.assertEquals(businessInvoiceItem.getPhase(), invoiceItemBaseModelDao.getPhase());
        Assert.assertEquals(businessInvoiceItem.getBillingPeriod(), invoiceItemBaseModelDao.getBillingPeriod());
        Assert.assertEquals(businessInvoiceItem.getStartDate().compareTo(invoiceItemBaseModelDao.getStartDate()), 0);
        Assert.assertEquals(businessInvoiceItem.getEndDate().compareTo(invoiceItemBaseModelDao.getEndDate()), 0);
        Assert.assertEquals(businessInvoiceItem.getAmount().compareTo(invoiceItemBaseModelDao.getAmount()), 0);
        Assert.assertEquals(businessInvoiceItem.getConvertedAmount().compareTo(invoiceItemBaseModelDao.getConvertedAmount()), 0);
        Assert.assertEquals(businessInvoiceItem.getCurrency(), invoiceItemBaseModelDao.getCurrency());
        Assert.assertEquals(businessInvoiceItem.getLinkedItemId(), invoiceItemBaseModelDao.getLinkedItemId());
        Assert.assertEquals(businessInvoiceItem.getConvertedCurrency(), invoiceItemBaseModelDao.getConvertedCurrency());
    }
}
