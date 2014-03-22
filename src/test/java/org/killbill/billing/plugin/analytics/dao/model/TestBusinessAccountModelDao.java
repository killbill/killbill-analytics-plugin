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

package org.killbill.billing.plugin.analytics.dao.model;

import java.math.BigDecimal;

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestBusinessAccountModelDao extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructorWithNulls() throws Exception {
        final BusinessAccountModelDao accountModelDao = new BusinessAccountModelDao(account,
                                                                                    accountRecordId,
                                                                                    BigDecimal.ONE,
                                                                                    null,
                                                                                    null,
                                                                                    null,
                                                                                    3,
                                                                                    currencyConverter,
                                                                                    auditLog,
                                                                                    tenantRecordId,
                                                                                    reportGroup);
        verifyAccountFields(accountModelDao);
        Assert.assertEquals(accountModelDao.getBalance(), BigDecimal.ONE);
        Assert.assertNull(accountModelDao.getOldestUnpaidInvoiceDate());
        Assert.assertNull(accountModelDao.getOldestUnpaidInvoiceBalance());
        Assert.assertNull(accountModelDao.getOldestUnpaidInvoiceCurrency());
        Assert.assertNull(accountModelDao.getOldestUnpaidInvoiceId());
        Assert.assertNull(accountModelDao.getLastInvoiceDate());
        Assert.assertNull(accountModelDao.getLastInvoiceBalance());
        Assert.assertNull(accountModelDao.getLastInvoiceCurrency());
        Assert.assertNull(accountModelDao.getLastInvoiceId());
        Assert.assertNull(accountModelDao.getLastPaymentDate());
        Assert.assertNull(accountModelDao.getLastPaymentStatus());
    }

    @Test(groups = "fast")
    public void testConstructorWithoutNulls() throws Exception {
        final BusinessAccountModelDao accountModelDao = new BusinessAccountModelDao(account,
                                                                                    accountRecordId,
                                                                                    BigDecimal.ONE,
                                                                                    invoice,
                                                                                    invoice,
                                                                                    payment,
                                                                                    3,
                                                                                    currencyConverter,
                                                                                    auditLog,
                                                                                    tenantRecordId,
                                                                                    reportGroup);
        verifyAccountFields(accountModelDao);
        Assert.assertEquals(accountModelDao.getBalance(), BigDecimal.ONE);
        Assert.assertEquals(accountModelDao.getOldestUnpaidInvoiceDate().compareTo(invoice.getInvoiceDate()), 0);
        Assert.assertEquals(accountModelDao.getOldestUnpaidInvoiceBalance().compareTo(invoice.getBalance()), 0);
        Assert.assertEquals(accountModelDao.getOldestUnpaidInvoiceCurrency(), invoice.getCurrency().toString());
        Assert.assertEquals(accountModelDao.getOldestUnpaidInvoiceId(), invoice.getId());
        Assert.assertEquals(accountModelDao.getLastInvoiceBalance().compareTo(invoice.getBalance()), 0);
        Assert.assertEquals(accountModelDao.getLastInvoiceCurrency(), invoice.getCurrency().toString());
        Assert.assertEquals(accountModelDao.getLastInvoiceDate().compareTo(invoice.getInvoiceDate()), 0);
        Assert.assertEquals(accountModelDao.getLastInvoiceId(), invoice.getId());
        Assert.assertEquals(accountModelDao.getLastPaymentDate(), payment.getEffectiveDate());
        Assert.assertEquals(accountModelDao.getLastPaymentStatus(), payment.getPaymentStatus().toString());
    }

    private void verifyAccountFields(final BusinessAccountModelDao accountModelDao) {
        verifyBusinessModelDaoBase(accountModelDao, accountRecordId, tenantRecordId);
        Assert.assertEquals(accountModelDao.getCreatedDate(), account.getCreatedDate());
        Assert.assertEquals(accountModelDao.getUpdatedDate(), account.getUpdatedDate());
        Assert.assertEquals(accountModelDao.getEmail(), account.getEmail());
        Assert.assertEquals(accountModelDao.getFirstNameLength(), account.getFirstNameLength());
        Assert.assertEquals(accountModelDao.getCurrency(), account.getCurrency().toString());
        Assert.assertEquals(accountModelDao.getBillingCycleDayLocal(), account.getBillCycleDayLocal());
        Assert.assertEquals(accountModelDao.getPaymentMethodId(), account.getPaymentMethodId());
        Assert.assertEquals(accountModelDao.getTimeZone(), account.getTimeZone().toString());
        Assert.assertEquals(accountModelDao.getLocale(), account.getLocale());
        Assert.assertEquals(accountModelDao.getAddress1(), account.getAddress1());
        Assert.assertEquals(accountModelDao.getAddress2(), account.getAddress2());
        Assert.assertEquals(accountModelDao.getCompanyName(), account.getCompanyName());
        Assert.assertEquals(accountModelDao.getCity(), account.getCity());
        Assert.assertEquals(accountModelDao.getStateOrProvince(), account.getStateOrProvince());
        Assert.assertEquals(accountModelDao.getCountry(), account.getCountry());
        Assert.assertEquals(accountModelDao.getPostalCode(), account.getPostalCode());
        Assert.assertEquals(accountModelDao.getPhone(), account.getPhone());
        Assert.assertEquals(accountModelDao.getMigrated(), account.isMigrated());
        Assert.assertEquals(accountModelDao.getNotifiedForInvoices(), account.isNotifiedForInvoices());
        Assert.assertEquals(accountModelDao.getNbActiveBundles(), (Integer) 3);
    }
}
