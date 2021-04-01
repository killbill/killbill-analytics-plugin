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

import java.math.BigDecimal;

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestBusinessAccount extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        final BusinessAccountModelDao accountModelDao = new BusinessAccountModelDao(account,
                                                                                    parentAccount,
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

        verifyBusinessEntityBase(businessAccount);
        Assert.assertEquals(businessAccount.getCreatedDate(), accountModelDao.getCreatedDate());
        Assert.assertEquals(businessAccount.getEmail(), accountModelDao.getEmail());
        Assert.assertEquals(businessAccount.getFirstNameLength(), accountModelDao.getFirstNameLength());
        Assert.assertEquals(businessAccount.getCurrency(), accountModelDao.getCurrency());
        Assert.assertEquals(businessAccount.getBillingCycleDayLocal(), accountModelDao.getBillingCycleDayLocal());
        Assert.assertEquals(businessAccount.getPaymentMethodId(), accountModelDao.getPaymentMethodId());
        Assert.assertEquals(businessAccount.getTimeZone(), accountModelDao.getTimeZone());
        Assert.assertEquals(businessAccount.getLocale(), accountModelDao.getLocale());
        Assert.assertEquals(businessAccount.getAddress1(), accountModelDao.getAddress1());
        Assert.assertEquals(businessAccount.getAddress2(), accountModelDao.getAddress2());
        Assert.assertEquals(businessAccount.getCompanyName(), accountModelDao.getCompanyName());
        Assert.assertEquals(businessAccount.getCity(), accountModelDao.getCity());
        Assert.assertEquals(businessAccount.getStateOrProvince(), accountModelDao.getStateOrProvince());
        Assert.assertEquals(businessAccount.getCountry(), accountModelDao.getCountry());
        Assert.assertEquals(businessAccount.getPostalCode(), accountModelDao.getPostalCode());
        Assert.assertEquals(businessAccount.getPhone(), accountModelDao.getPhone());
        Assert.assertEquals(businessAccount.getMigrated(), accountModelDao.getMigrated());
        Assert.assertEquals(businessAccount.getUpdatedDate().compareTo(accountModelDao.getUpdatedDate()), 0);
        Assert.assertEquals(businessAccount.getBalance().compareTo(accountModelDao.getBalance()), 0);
        Assert.assertEquals(businessAccount.getConvertedBalance().compareTo(accountModelDao.getConvertedBalance()), 0);
        Assert.assertEquals(businessAccount.getOldestUnpaidInvoiceDate().compareTo(accountModelDao.getOldestUnpaidInvoiceDate()), 0);
        Assert.assertEquals(businessAccount.getOldestUnpaidInvoiceBalance().compareTo(accountModelDao.getOldestUnpaidInvoiceBalance()), 0);
        Assert.assertEquals(businessAccount.getConvertedOldestUnpaidInvoiceBalance().compareTo(accountModelDao.getConvertedOldestUnpaidInvoiceBalance()), 0);
        Assert.assertEquals(businessAccount.getOldestUnpaidInvoiceCurrency(), accountModelDao.getOldestUnpaidInvoiceCurrency());
        Assert.assertEquals(businessAccount.getOldestUnpaidInvoiceId(), accountModelDao.getOldestUnpaidInvoiceId());
        Assert.assertEquals(businessAccount.getLastInvoiceDate().compareTo(accountModelDao.getLastInvoiceDate()), 0);
        Assert.assertEquals(businessAccount.getLastInvoiceBalance().compareTo(accountModelDao.getLastInvoiceBalance()), 0);
        Assert.assertEquals(businessAccount.getConvertedLastInvoiceBalance().compareTo(accountModelDao.getConvertedLastInvoiceBalance()), 0);
        Assert.assertEquals(businessAccount.getLastInvoiceCurrency(), accountModelDao.getLastInvoiceCurrency());
        Assert.assertEquals(businessAccount.getLastInvoiceId(), accountModelDao.getLastInvoiceId());
        Assert.assertEquals(businessAccount.getLastPaymentDate().compareTo(accountModelDao.getLastPaymentDate()), 0);
        Assert.assertEquals(businessAccount.getLastPaymentStatus(), accountModelDao.getLastPaymentStatus());
        Assert.assertEquals(businessAccount.getNbActiveBundles(), accountModelDao.getNbActiveBundles());
        Assert.assertEquals(businessAccount.getConvertedCurrency(), accountModelDao.getConvertedCurrency());
        Assert.assertEquals(businessAccount.getParentAccountId(), accountModelDao.getParentAccountId());
        Assert.assertEquals(businessAccount.getParentAccountName(), accountModelDao.getParentAccountName());
        Assert.assertEquals(businessAccount.getParentAccountExternalKey(), accountModelDao.getParentAccountExternalKey());
    }
}
