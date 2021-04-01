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

package org.killbill.billing.plugin.analytics.api;

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestBusinessInvoiceItem extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        final BusinessInvoiceItemBaseModelDao invoiceItemBaseModelDao = BusinessInvoiceItemBaseModelDao.create(account,
                                                                                                               accountRecordId,
                                                                                                               invoice,
                                                                                                               invoiceItem,
                                                                                                               itemSource,
                                                                                                               false,
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
        final BusinessInvoiceItem businessInvoiceItem = new BusinessInvoiceItem(invoiceItemBaseModelDao);
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
        Assert.assertEquals(businessInvoiceItem.getRawInvoiceBalance().compareTo(invoiceItemBaseModelDao.getRawInvoiceBalance()), 0);
        Assert.assertEquals(businessInvoiceItem.getConvertedRawInvoiceBalance().compareTo(invoiceItemBaseModelDao.getConvertedRawInvoiceBalance()), 0);
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
        Assert.assertFalse(businessInvoiceItem.isInvoiceWrittenOff());
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
