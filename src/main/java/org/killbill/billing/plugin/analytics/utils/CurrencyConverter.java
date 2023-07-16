/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
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

import javax.annotation.Nullable;

import org.joda.time.LocalDate;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.payment.api.PaymentTransaction;
import org.killbill.billing.plugin.analytics.dao.model.CurrencyConversionModelDao;
import org.killbill.clock.Clock;

public class CurrencyConverter {

    private final Clock clock;
    private final String referenceCurrency;
    private final CurrencyConversions currencyConversions;

    public CurrencyConverter(final Clock clock, final String referenceCurrency, final CurrencyConversions currencyConversions) {
        this.clock = clock;
        this.referenceCurrency = referenceCurrency;
        this.currencyConversions = currencyConversions;
    }

    public BigDecimal getConvertedValue(@Nullable final BigDecimal value,
                                        @Nullable final String currency,
                                        @Nullable final LocalDate effectiveDate) {
        // No data
        if (referenceCurrency == null) {
            return null;
        }

        // Optimization
        if (referenceCurrency.equals(currency)) {
            return value;
        }

        if (value == null || currency == null || effectiveDate == null) {
            return null;
        }

        return currencyConversions.getConvertedValue(value, currency, effectiveDate);
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

    public BigDecimal getConvertedValue(@Nullable final InvoicePayment invoicePayment, final PaymentTransaction transaction, @Nullable final Invoice invoice) {
        if (invoicePayment != null && invoicePayment.getAmount() != null && invoicePayment.getCurrency() != null && invoice != null) {
            // Use the invoice date as the effective date for consistency - invoice payment payment date could also be a candidate
            return getConvertedValue(invoicePayment.getAmount(), invoicePayment.getCurrency().toString(), invoice.getInvoiceDate());
        } else if (transaction.getCurrency() != null) {
            return getConvertedValue(transaction.getAmount(), transaction.getCurrency().toString(), transaction.getEffectiveDate().toLocalDate());
        } else {
            return null;
        }
    }
}
