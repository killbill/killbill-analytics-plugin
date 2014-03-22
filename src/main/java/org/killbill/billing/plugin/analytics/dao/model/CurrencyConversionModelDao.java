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

public class CurrencyConversionModelDao {

    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal referenceRate;
    private String referenceCurrency;

    public CurrencyConversionModelDao() { /* When reading from the database */ }

    public CurrencyConversionModelDao(final String currency, final LocalDate startDate, final LocalDate endDate,
                                      final BigDecimal referenceRate, final String referenceCurrency) {
        this.currency = currency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.referenceRate = referenceRate;
        this.referenceCurrency = referenceCurrency;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal getReferenceRate() {
        return referenceRate;
    }

    public String getReferenceCurrency() {
        return referenceCurrency;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CurrencyConversionModelDao{");
        sb.append("currency='").append(currency).append('\'');
        sb.append(", startDate=").append(startDate);
        sb.append(", endDate=").append(endDate);
        sb.append(", referenceRate=").append(referenceRate);
        sb.append(", referenceCurrency='").append(referenceCurrency).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CurrencyConversionModelDao that = (CurrencyConversionModelDao) o;

        if (currency != null ? !currency.equals(that.currency) : that.currency != null) {
            return false;
        }
        if (endDate != null ? endDate.compareTo(that.endDate) != 0 : that.endDate != null) {
            return false;
        }
        if (referenceCurrency != null ? !referenceCurrency.equals(that.referenceCurrency) : that.referenceCurrency != null) {
            return false;
        }
        if (referenceRate != null ? referenceRate.compareTo(that.referenceRate) != 0 : that.referenceRate != null) {
            return false;
        }
        if (startDate != null ? startDate.compareTo(that.startDate) != 0 : that.startDate != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = currency != null ? currency.hashCode() : 0;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (referenceRate != null ? referenceRate.hashCode() : 0);
        result = 31 * result + (referenceCurrency != null ? referenceCurrency.hashCode() : 0);
        return result;
    }
}
