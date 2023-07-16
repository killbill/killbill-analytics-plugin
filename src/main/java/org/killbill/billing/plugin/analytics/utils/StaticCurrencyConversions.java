/*
 * Copyright 2020-2023 Equinix, Inc
 * Copyright 2014-2023 The Billing Project, LLC
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
import org.killbill.billing.plugin.analytics.dao.model.CurrencyConversionModelDao;

public class StaticCurrencyConversions implements CurrencyConversions {

    // Map original currency -> currency conversions over time
    // TODO PIERRE Better representation (heap?)
    private final Map<String, List<CurrencyConversionModelDao>> currencyConversions;

    public StaticCurrencyConversions(final Map<String, List<CurrencyConversionModelDao>> currencyConversions) {
        this.currencyConversions = currencyConversions;
    }

    @Override
    public BigDecimal getConvertedValue(final BigDecimal value, final String currency, final LocalDate effectiveDate) {
        CurrencyConversionModelDao currencyConversionCandidate = null;
        final List<CurrencyConversionModelDao> currencyConversionModelDaos = currencyConversions.get(currency);
        if (currencyConversionModelDaos == null) {
            return null;
        }

        for (final CurrencyConversionModelDao currencyConversionModelDao : currencyConversionModelDaos) {
            if (!effectiveDate.isBefore(currencyConversionModelDao.getStartDate()) &&
                !effectiveDate.isAfter(currencyConversionModelDao.getEndDate()) &&
                // In case of overlapping ranges, use the narrowest one
                (currencyConversionCandidate == null || currencyConversionModelDao.getStartDate().isAfter(currencyConversionCandidate.getStartDate()))) {
                currencyConversionCandidate = currencyConversionModelDao;
            }
        }

        if (currencyConversionCandidate == null) {
            return null;
        } else {
            return value.multiply(currencyConversionCandidate.getReferenceRate());
        }
    }
}
