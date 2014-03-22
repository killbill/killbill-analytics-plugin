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

import org.joda.time.LocalDate;
import org.joda.time.chrono.BuddhistChronology;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestCurrencyConversionModelDao extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testEquals() throws Exception {
        final CurrencyConversionModelDao currencyConversion = new CurrencyConversionModelDao("EUR",
                                                                                             new LocalDate(2012, 7, 1),
                                                                                             new LocalDate(2012, 8, 1),
                                                                                             BigDecimal.TEN,
                                                                                             "USD");
        final CurrencyConversionModelDao currencyConversion2 = new CurrencyConversionModelDao("EUR",
                                                                                              new LocalDate(2012, 7, 1, BuddhistChronology.getInstance()),
                                                                                              new LocalDate(2012, 8, 1, BuddhistChronology.getInstance()),
                                                                                              new BigDecimal("10"),
                                                                                              "USD");
        Assert.assertEquals(currencyConversion2, currencyConversion);
    }
}
