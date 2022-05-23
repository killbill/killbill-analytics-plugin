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

package org.killbill.billing.plugin.analytics.dao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIMetricRegistry;
import org.killbill.billing.plugin.analytics.dao.model.CurrencyConversionModelDao;
import org.skife.jdbi.v2.DBI;

public class CurrencyConversionDao {

    private final CurrencyConversionSqlDao sqlDao;

    public CurrencyConversionDao(final OSGIKillbillDataSource osgiKillbillDataSource,
                                 final OSGIMetricRegistry metricRegistry) {
        final DBI dbi = BusinessDBIProvider.get(osgiKillbillDataSource.getDataSource(), metricRegistry.getMetricRegistry());
        this.sqlDao = dbi.onDemand(CurrencyConversionSqlDao.class);
    }

    // Return a list of conversion rates over time per currency
    public Map<String, List<CurrencyConversionModelDao>> getCurrencyConversions(final String referenceCurrency) {
        final Map<String, List<CurrencyConversionModelDao>> currencyConversions = new HashMap<String, List<CurrencyConversionModelDao>>();
        for (final CurrencyConversionModelDao currencyConversionModelDao : sqlDao.getCurrencyConversions(referenceCurrency)) {
            if (currencyConversions.get(currencyConversionModelDao.getCurrency()) == null) {
                currencyConversions.put(currencyConversionModelDao.getCurrency(), new LinkedList<CurrencyConversionModelDao>());
            }
            currencyConversions.get(currencyConversionModelDao.getCurrency()).add(currencyConversionModelDao);
        }
        return currencyConversions;
    }

    public CurrencyConversionModelDao getCurrencyConversion(final String referenceCurrency, final String currency, final DateTime effectiveDate) {
        return getCurrencyConversion(referenceCurrency, currency, effectiveDate.toLocalDate());
    }

    public CurrencyConversionModelDao getCurrencyConversion(final String referenceCurrency, final String currency, final LocalDate effectiveDate) {
        final CurrencyConversionModelDao currencyConversion = sqlDao.getCurrencyConversionForCurrencyAndDate(referenceCurrency, currency, effectiveDate);
        if (currencyConversion == null) {
            return new CurrencyConversionModelDao(currency, effectiveDate, effectiveDate, null, referenceCurrency);
        } else {
            return currencyConversion;
        }
    }

    public void addCurrencyConversion(final String currency, final LocalDate startDate, final LocalDate endDate,
                                      final BigDecimal referenceRate, final String referenceCurrency) {
        sqlDao.addCurrencyConversion(currency, startDate, endDate, referenceRate, referenceCurrency);
    }
}
