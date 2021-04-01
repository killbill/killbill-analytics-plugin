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

package org.killbill.billing.plugin.analytics.utils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.dao.model.CurrencyConversionModelDao;
import org.killbill.clock.DefaultClock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class TestCurrencyConverter extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testGetConvertedValue() throws Exception {
        final Map<String, List<CurrencyConversionModelDao>> currencyConversions = ImmutableMap.<String, List<CurrencyConversionModelDao>>of(
                "EUR", ImmutableList.<CurrencyConversionModelDao>of(
                        new CurrencyConversionModelDao("EUR", new LocalDate(2012, 7, 1), new LocalDate(2012, 7, 31), new BigDecimal("1.1"), "USD"),
                        new CurrencyConversionModelDao("EUR", new LocalDate(2012, 8, 1), new LocalDate(2012, 8, 31), new BigDecimal("1.2"), "USD"),
                        new CurrencyConversionModelDao("EUR", new LocalDate(2012, 9, 1), new LocalDate(2012, 9, 30), new BigDecimal("1.4"), "USD"),
                        // Overlap!
                        new CurrencyConversionModelDao("EUR", new LocalDate(2012, 9, 15), new LocalDate(2012, 9, 30), new BigDecimal("1.5"), "USD"),
                        new CurrencyConversionModelDao("EUR", new LocalDate(2012, 10, 1), new LocalDate(2012, 10, 31), new BigDecimal("1.6"), "USD"))
                                                                                                                                           );
        final CurrencyConverter currencyConverter = new CurrencyConverter(new DefaultClock(), "USD", currencyConversions);

        // No data for currency
        Assert.assertNull(currencyConverter.getConvertedValue(BigDecimal.TEN, "FOO", new LocalDate(2012, 9, 1)));
        // No data for date
        Assert.assertNull(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 6, 30)));
        Assert.assertNull(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 11, 1)));

        final BigDecimal expectedResult1 = new BigDecimal("11");
        Assert.assertEquals(expectedResult1.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 7, 1))), 0);
        Assert.assertEquals(expectedResult1.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 7, 15))), 0);
        Assert.assertEquals(expectedResult1.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 7, 31))), 0);

        final BigDecimal expectedResult2 = new BigDecimal("12");
        Assert.assertEquals(expectedResult2.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 8, 1))), 0);
        Assert.assertEquals(expectedResult2.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 8, 15))), 0);
        Assert.assertEquals(expectedResult2.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 8, 31))), 0);

        final BigDecimal expectedResult3 = new BigDecimal("14");
        Assert.assertEquals(expectedResult3.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 9, 1))), 0);
        Assert.assertEquals(expectedResult3.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 9, 14))), 0);
        // Test overlap behavior
        final BigDecimal expectedResult4 = new BigDecimal("15");
        Assert.assertEquals(expectedResult4.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 9, 15))), 0);
        Assert.assertEquals(expectedResult4.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 9, 30))), 0);

        final BigDecimal expectedResult5 = new BigDecimal("16");
        Assert.assertEquals(expectedResult5.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 10, 1))), 0);
        Assert.assertEquals(expectedResult5.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 10, 15))), 0);
        Assert.assertEquals(expectedResult5.compareTo(currencyConverter.getConvertedValue(BigDecimal.TEN, "EUR", new LocalDate(2012, 10, 31))), 0);
    }
}
