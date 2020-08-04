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

package org.killbill.billing.plugin.analytics.dao.model;

import java.util.UUID;

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestBusinessPaymentModelDao extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructorWithNullPaymentMethod() throws Exception {
        final BusinessPaymentPurchaseModelDao invoicePaymentModelDao = new BusinessPaymentPurchaseModelDao(account,
                                                                                                           accountRecordId,
                                                                                                           invoice,
                                                                                                           invoicePayment,
                                                                                                           invoicePaymentRecordId,
                                                                                                           payment,
                                                                                                           paymentTransaction,
                                                                                                           null,
                                                                                                           currencyConverter,
                                                                                                           auditLog,
                                                                                                           tenantRecordId,
                                                                                                           reportGroup,
                                                                                                           pluginPropertiesManager);
        verifyCommonFields(invoicePaymentModelDao, payment.getId());
        Assert.assertEquals(invoicePaymentModelDao.getPluginName(), BusinessPaymentBaseModelDao.DEFAULT_PLUGIN_NAME);
        Assert.assertNull(invoicePaymentModelDao.getPluginCreatedDate());
        Assert.assertNull(invoicePaymentModelDao.getPluginEffectiveDate());
        Assert.assertNull(invoicePaymentModelDao.getPluginStatus());
        Assert.assertNull(invoicePaymentModelDao.getPluginGatewayError());
        Assert.assertNull(invoicePaymentModelDao.getPluginGatewayErrorCode());
        Assert.assertNull(invoicePaymentModelDao.getPluginFirstReferenceId());
        Assert.assertNull(invoicePaymentModelDao.getPluginSecondReferenceId());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmId());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmIsDefault());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmType());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmCcName());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmCcType());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmCcExpirationMonth());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmCcExpirationYear());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmCcLast4());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmAddress1());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmAddress2());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmCity());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmState());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmZip());
        Assert.assertNull(invoicePaymentModelDao.getPluginPmCountry());
    }

    @Test(groups = "fast")
    public void testConstructorWithNullRefund() throws Exception {
        final BusinessPaymentPurchaseModelDao invoicePaymentModelDao = new BusinessPaymentPurchaseModelDao(account,
                                                                                                           accountRecordId,
                                                                                                           invoice,
                                                                                                           invoicePayment,
                                                                                                           invoicePaymentRecordId,
                                                                                                           paymentNoRefund,
                                                                                                           paymentTransaction,
                                                                                                           paymentMethod,
                                                                                                           currencyConverter,
                                                                                                           auditLog,
                                                                                                           tenantRecordId,
                                                                                                           reportGroup,
                                                                                                           pluginPropertiesManager);
        verifyCommonFields(invoicePaymentModelDao, paymentNoRefund.getId());
    }

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        final BusinessPaymentPurchaseModelDao invoicePaymentModelDao = new BusinessPaymentPurchaseModelDao(account,
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
                                                                                                           reportGroup,
                                                                                                           pluginPropertiesManager);
        verifyCommonFields(invoicePaymentModelDao, payment.getId());
    }

    @Test(groups = "fast")
    public void testConstructorWithNullInvoicePayment() throws Exception {
        invoicePayment = null;
        final BusinessPaymentPurchaseModelDao invoicePaymentModelDao = new BusinessPaymentPurchaseModelDao(account,
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
                                                                                                           reportGroup,
                                                                                                           pluginPropertiesManager);
        verifyCommonFields(invoicePaymentModelDao, payment.getId());
    }


    private void verifyCommonFields(final BusinessPaymentPurchaseModelDao invoicePaymentModelDao, final UUID paymentId) {
        verifyBusinessModelDaoBase(invoicePaymentModelDao, accountRecordId, tenantRecordId);
        Assert.assertEquals(invoicePaymentModelDao.getInvoicePaymentRecordId(), invoicePaymentRecordId);
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceId(), invoice.getId());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceNumber(), invoice.getInvoiceNumber());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceCreatedDate(), invoice.getCreatedDate());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceDate(), invoice.getInvoiceDate());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceTargetDate(), invoice.getTargetDate());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceCurrency(), invoice.getCurrency().toString());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceBalance(), invoice.getBalance());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceAmountPaid(), invoice.getPaidAmount());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceAmountCharged(), invoice.getChargedAmount());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceOriginalAmountCharged(), invoice.getOriginalChargedAmount());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceAmountCredited(), invoice.getCreditedAmount());
        Assert.assertEquals(invoicePaymentModelDao.getInvoiceAmountRefunded(), invoice.getRefundedAmount());
        Assert.assertEquals(invoicePaymentModelDao.getPaymentNumber(), (Long) payment.getPaymentNumber().longValue());
        Assert.assertEquals(invoicePaymentModelDao.getPaymentId(), paymentId);
        Assert.assertEquals(invoicePaymentModelDao.getPaymentExternalKey(), payment.getExternalKey());
        Assert.assertEquals(invoicePaymentModelDao.getPaymentTransactionId(), paymentTransaction.getId());
        Assert.assertEquals(invoicePaymentModelDao.getPaymentTransactionExternalKey(), paymentTransaction.getExternalKey());
        if (invoicePayment != null) {
            verifyCommonFieldsFromInvoicePayments(invoicePaymentModelDao);
        } else {
            verifyCommonFieldsFromNullInvoicePayments(invoicePaymentModelDao);
        }
    }

    private void verifyCommonFieldsFromInvoicePayments(final BusinessPaymentPurchaseModelDao invoicePaymentModelDao) {
        Assert.assertEquals(invoicePaymentModelDao.getCreatedDate(), invoicePayment.getCreatedDate());
        Assert.assertEquals(invoicePaymentModelDao.getInvoicePaymentId(), invoicePayment.getId());
        Assert.assertEquals(invoicePaymentModelDao.getInvoicePaymentType(), invoicePayment.getType().toString());
        Assert.assertEquals(invoicePaymentModelDao.getLinkedInvoicePaymentId(), invoicePayment.getLinkedInvoicePaymentId());
        Assert.assertEquals(invoicePaymentModelDao.getAmount(), invoicePayment.getAmount());
        Assert.assertEquals(invoicePaymentModelDao.getCurrency(), invoicePayment.getCurrency().toString());
    }

    private void verifyCommonFieldsFromNullInvoicePayments(final BusinessPaymentPurchaseModelDao invoicePaymentModelDao) {
        Assert.assertEquals(invoicePaymentModelDao.getCreatedDate(), paymentTransaction.getCreatedDate());
        Assert.assertEquals(invoicePaymentModelDao.getAmount(), paymentTransaction.getAmount());
        Assert.assertEquals(invoicePaymentModelDao.getCurrency(), paymentTransaction.getCurrency().toString());
    }

}
