/*
 * Copyright 2010-2013 Ning, Inc.
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

package com.ning.billing.osgi.bundles.analytics.utils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;

import com.ning.billing.account.api.Account;
import com.ning.billing.clock.Clock;
import com.ning.billing.invoice.api.Invoice;
import com.ning.billing.invoice.api.InvoiceItem;
import com.ning.billing.invoice.api.InvoicePayment;
import com.ning.billing.osgi.bundles.analytics.dao.model.CurrencyConversionModelDao;

public class CurrencyConverter {

    private final Clock clock;
    private final String referenceCurrency;

    // Map original currency -> currency conversions over time
    // TODO PIERRE Better representation (heap?)
    private final Map<String, List<CurrencyConversionModelDao>> currencyConversions;

    public CurrencyConverter(final Clock clock, final Map<String, List<CurrencyConversionModelDao>> currencyConversions) {
        this.clock = clock;
        this.currencyConversions = currencyConversions;

        // We expect the currency conversions to be for the same reference currency - we don't check it though
        String commonReferenceCurrency = null;
        if (currencyConversions.values().iterator().hasNext()) {
            final List<CurrencyConversionModelDao> firstCurrencyConversion = currencyConversions.values().iterator().next();
            if (firstCurrencyConversion.iterator().hasNext()) {
                commonReferenceCurrency = firstCurrencyConversion.iterator().next().getReferenceCurrency();
            }
        }
        this.referenceCurrency = commonReferenceCurrency;
    }

    public BigDecimal getConvertedValue(@Nullable final BigDecimal value,
                                        @Nullable final String currency,
                                        @Nullable final LocalDate effectiveDate) {
        // Optimization
        if (referenceCurrency == null || referenceCurrency.equals(currency)) {
            return value;
        }

        if (value == null || currency == null || effectiveDate == null || currencyConversions.get(currency) == null) {
            return null;
        }

        CurrencyConversionModelDao currencyConversionCandidate = null;
        for (final CurrencyConversionModelDao currencyConversionModelDao : currencyConversions.get(currency)) {
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

    public String getConvertedCurrency() {
        return referenceCurrency;
    }

    public BigDecimal getConvertedValue(@Nullable final BigDecimal value, @Nullable final Account account) {
        if (value == null || account == null || account.getCurrency() == null) {
            return null;
        }
        return getConvertedValue(value, account.getCurrency().toString(), clock.getUTCToday());
    }

    public BigDecimal getConvertedValue(@Nullable final Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        return getConvertedValue(invoice.getBalance(), invoice);
    }

    public BigDecimal getConvertedValue(@Nullable final BigDecimal value, @Nullable final Invoice invoice) {
        if (value == null || invoice == null || invoice.getCurrency() == null) {
            return null;
        }
        return getConvertedValue(value, invoice.getCurrency().toString(), invoice.getInvoiceDate());
    }

    public BigDecimal getConvertedValue(@Nullable final InvoiceItem invoiceItem, final Invoice invoice) {
        if (invoiceItem == null || invoiceItem.getCurrency() == null) {
            return null;
        }
        // Use the invoice date as the effective date for consistency - invoice item creation date could also be a candidate
        return getConvertedValue(invoiceItem.getAmount(), invoiceItem.getCurrency().toString(), invoice.getInvoiceDate());
    }

    public BigDecimal getConvertedValue(@Nullable final InvoicePayment invoicePayment, final Invoice invoice) {
        if (invoicePayment == null || invoicePayment.getCurrency() == null) {
            return null;
        }
        // Use the invoice date as the effective date for consistency - invoice payment payment date could also be a candidate
        return getConvertedValue(invoicePayment.getAmount(), invoicePayment.getCurrency().toString(), invoice.getInvoiceDate());
    }
}
