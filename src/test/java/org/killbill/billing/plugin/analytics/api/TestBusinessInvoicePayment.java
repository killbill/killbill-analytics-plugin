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
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentModelDao;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestBusinessInvoicePayment extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        final BusinessInvoicePaymentBaseModelDao invoicePaymentBaseModelDao = BusinessInvoicePaymentModelDao.create(account,
                                                                                                                    accountRecordId,
                                                                                                                    invoice,
                                                                                                                    invoicePayment,
                                                                                                                    invoicePaymentRecordId,
                                                                                                                    payment,
                                                                                                                    refund,
                                                                                                                    paymentMethod,
                                                                                                                    currencyConverter,
                                                                                                                    auditLog,
                                                                                                                    tenantRecordId,
                                                                                                                    reportGroup);
        final BusinessInvoicePayment businessInvoicePayment = new BusinessInvoicePayment(invoicePaymentBaseModelDao);
        verifyBusinessEntityBase(businessInvoicePayment);
        Assert.assertEquals(businessInvoicePayment.getCreatedDate(), invoicePaymentBaseModelDao.getCreatedDate());
        Assert.assertEquals(businessInvoicePayment.getInvoicePaymentId(), invoicePaymentBaseModelDao.getInvoicePaymentId());
        Assert.assertEquals(businessInvoicePayment.getInvoiceId(), invoicePaymentBaseModelDao.getInvoiceId());
        Assert.assertEquals(businessInvoicePayment.getInvoiceNumber(), invoicePaymentBaseModelDao.getInvoiceNumber());
        Assert.assertEquals(businessInvoicePayment.getInvoiceCreatedDate(), invoicePaymentBaseModelDao.getInvoiceCreatedDate());
        Assert.assertEquals(businessInvoicePayment.getInvoiceDate(), invoicePaymentBaseModelDao.getInvoiceDate());
        Assert.assertEquals(businessInvoicePayment.getInvoiceTargetDate(), invoicePaymentBaseModelDao.getInvoiceTargetDate());
        Assert.assertEquals(businessInvoicePayment.getInvoiceCurrency(), invoicePaymentBaseModelDao.getInvoiceCurrency());
        Assert.assertEquals(businessInvoicePayment.getInvoiceBalance().compareTo(invoicePaymentBaseModelDao.getInvoiceBalance()), 0);
        Assert.assertEquals(businessInvoicePayment.getConvertedInvoiceBalance().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceBalance()), 0);
        Assert.assertEquals(businessInvoicePayment.getInvoiceAmountPaid().compareTo(invoicePaymentBaseModelDao.getInvoiceAmountPaid()), 0);
        Assert.assertEquals(businessInvoicePayment.getConvertedInvoiceAmountPaid().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceAmountPaid()), 0);
        Assert.assertEquals(businessInvoicePayment.getInvoiceAmountCharged().compareTo(invoicePaymentBaseModelDao.getInvoiceAmountCharged()), 0);
        Assert.assertEquals(businessInvoicePayment.getConvertedInvoiceAmountCharged().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceAmountCharged()), 0);
        Assert.assertEquals(businessInvoicePayment.getInvoiceOriginalAmountCharged().compareTo(invoicePaymentBaseModelDao.getInvoiceOriginalAmountCharged()), 0);
        Assert.assertEquals(businessInvoicePayment.getConvertedInvoiceOriginalAmountCharged().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceOriginalAmountCharged()), 0);
        Assert.assertEquals(businessInvoicePayment.getInvoiceAmountCredited().compareTo(invoicePaymentBaseModelDao.getInvoiceAmountCredited()), 0);
        Assert.assertEquals(businessInvoicePayment.getConvertedInvoiceAmountCredited().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceAmountCredited()), 0);
        Assert.assertEquals(businessInvoicePayment.getInvoiceAmountRefunded().compareTo(invoicePaymentBaseModelDao.getInvoiceAmountRefunded()), 0);
        Assert.assertEquals(businessInvoicePayment.getConvertedInvoiceAmountRefunded().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceAmountRefunded()), 0);
        Assert.assertEquals(businessInvoicePayment.getInvoicePaymentType(), invoicePaymentBaseModelDao.getInvoicePaymentType());
        Assert.assertEquals(businessInvoicePayment.getPaymentNumber(), invoicePaymentBaseModelDao.getPaymentNumber());
        Assert.assertEquals(businessInvoicePayment.getLinkedInvoicePaymentId(), invoicePaymentBaseModelDao.getLinkedInvoicePaymentId());
        Assert.assertEquals(businessInvoicePayment.getAmount().compareTo(invoicePaymentBaseModelDao.getAmount()), 0);
        Assert.assertEquals(businessInvoicePayment.getConvertedAmount().compareTo(invoicePaymentBaseModelDao.getConvertedAmount()), 0);
        Assert.assertEquals(businessInvoicePayment.getCurrency(), invoicePaymentBaseModelDao.getCurrency());
        Assert.assertEquals(businessInvoicePayment.getConvertedCurrency(), invoicePaymentBaseModelDao.getConvertedCurrency());
    }
}
