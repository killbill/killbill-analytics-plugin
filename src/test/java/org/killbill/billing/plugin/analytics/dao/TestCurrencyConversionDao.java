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
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteWithEmbeddedDB;
import org.killbill.billing.plugin.analytics.dao.model.CurrencyConversionModelDao;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestCurrencyConversionDao extends AnalyticsTestSuiteWithEmbeddedDB {

    @Test(groups = "slow")
    public void testLookup() throws Exception {
        final CurrencyConversionDao dao = new CurrencyConversionDao(logService, killbillDataSource);

        final LocalDate effectiveDate = new LocalDate(2012, 1, 1);
        final CurrencyConversionModelDao notFound = dao.getCurrencyConversion("USD", "EUR", effectiveDate);
        Assert.assertEquals(notFound.getCurrency(), "EUR");
        Assert.assertEquals(notFound.getStartDate(), effectiveDate);
        Assert.assertEquals(notFound.getEndDate(), effectiveDate);
        Assert.assertNull(notFound.getReferenceRate());
        Assert.assertEquals(notFound.getReferenceCurrency(), "USD");

        dao.addCurrencyConversion("EUR", new LocalDate(2013, 5, 1), new LocalDate(2013, 7, 1), new BigDecimal("1.2983"), "USD");

        final CurrencyConversionModelDao found = dao.getCurrencyConversion("USD", "EUR", new LocalDate(2013, 5, 5));
        Assert.assertEquals(found.getCurrency(), "EUR");
        Assert.assertEquals(found.getStartDate(), new LocalDate(2013, 5, 1));
        Assert.assertEquals(found.getEndDate(), new LocalDate(2013, 7, 1));
        Assert.assertEquals(found.getReferenceRate(), new BigDecimal("1.2983"));
        Assert.assertEquals(found.getReferenceCurrency(), "USD");
    }
}
