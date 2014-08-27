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
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentPurchaseModelDao;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestBusinessPayment extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
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
        verifyBusinessEntityBase(businessPayment);
        Assert.assertEquals(businessPayment.getCreatedDate(), invoicePaymentBaseModelDao.getCreatedDate());
        Assert.assertEquals(businessPayment.getInvoicePaymentId(), invoicePaymentBaseModelDao.getInvoicePaymentId());
        Assert.assertEquals(businessPayment.getInvoiceId(), invoicePaymentBaseModelDao.getInvoiceId());
        Assert.assertEquals(businessPayment.getInvoiceNumber(), invoicePaymentBaseModelDao.getInvoiceNumber());
        Assert.assertEquals(businessPayment.getInvoiceCreatedDate(), invoicePaymentBaseModelDao.getInvoiceCreatedDate());
        Assert.assertEquals(businessPayment.getInvoiceDate(), invoicePaymentBaseModelDao.getInvoiceDate());
        Assert.assertEquals(businessPayment.getInvoiceTargetDate(), invoicePaymentBaseModelDao.getInvoiceTargetDate());
        Assert.assertEquals(businessPayment.getInvoiceCurrency(), invoicePaymentBaseModelDao.getInvoiceCurrency());
        Assert.assertEquals(businessPayment.getInvoiceBalance().compareTo(invoicePaymentBaseModelDao.getInvoiceBalance()), 0);
        Assert.assertEquals(businessPayment.getConvertedInvoiceBalance().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceBalance()), 0);
        Assert.assertEquals(businessPayment.getInvoiceAmountPaid().compareTo(invoicePaymentBaseModelDao.getInvoiceAmountPaid()), 0);
        Assert.assertEquals(businessPayment.getConvertedInvoiceAmountPaid().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceAmountPaid()), 0);
        Assert.assertEquals(businessPayment.getInvoiceAmountCharged().compareTo(invoicePaymentBaseModelDao.getInvoiceAmountCharged()), 0);
        Assert.assertEquals(businessPayment.getConvertedInvoiceAmountCharged().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceAmountCharged()), 0);
        Assert.assertEquals(businessPayment.getInvoiceOriginalAmountCharged().compareTo(invoicePaymentBaseModelDao.getInvoiceOriginalAmountCharged()), 0);
        Assert.assertEquals(businessPayment.getConvertedInvoiceOriginalAmountCharged().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceOriginalAmountCharged()), 0);
        Assert.assertEquals(businessPayment.getInvoiceAmountCredited().compareTo(invoicePaymentBaseModelDao.getInvoiceAmountCredited()), 0);
        Assert.assertEquals(businessPayment.getConvertedInvoiceAmountCredited().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceAmountCredited()), 0);
        Assert.assertEquals(businessPayment.getInvoiceAmountRefunded().compareTo(invoicePaymentBaseModelDao.getInvoiceAmountRefunded()), 0);
        Assert.assertEquals(businessPayment.getConvertedInvoiceAmountRefunded().compareTo(invoicePaymentBaseModelDao.getConvertedInvoiceAmountRefunded()), 0);
        Assert.assertEquals(businessPayment.getInvoicePaymentType(), invoicePaymentBaseModelDao.getInvoicePaymentType());
        Assert.assertEquals(businessPayment.getPaymentNumber(), invoicePaymentBaseModelDao.getPaymentNumber());
        Assert.assertEquals(businessPayment.getLinkedInvoicePaymentId(), invoicePaymentBaseModelDao.getLinkedInvoicePaymentId());
        Assert.assertEquals(businessPayment.getAmount().compareTo(invoicePaymentBaseModelDao.getAmount()), 0);
        Assert.assertEquals(businessPayment.getConvertedAmount().compareTo(invoicePaymentBaseModelDao.getConvertedAmount()), 0);
        Assert.assertEquals(businessPayment.getCurrency(), invoicePaymentBaseModelDao.getCurrency());
        Assert.assertEquals(businessPayment.getConvertedCurrency(), invoicePaymentBaseModelDao.getConvertedCurrency());
    }
}
