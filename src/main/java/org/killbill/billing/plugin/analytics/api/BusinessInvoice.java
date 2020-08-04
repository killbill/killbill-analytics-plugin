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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;

public class BusinessInvoice extends BusinessEntityBase {

    private final UUID invoiceId;
    private final Integer invoiceNumber;
    private final LocalDate invoiceDate;
    private final LocalDate targetDate;
    private final String currency;
    private final BigDecimal rawBalance;
    private final BigDecimal convertedRawBalance;
    private final BigDecimal balance;
    private final BigDecimal convertedBalance;
    private final BigDecimal amountPaid;
    private final BigDecimal convertedAmountPaid;
    private final BigDecimal amountCharged;
    private final BigDecimal convertedAmountCharged;
    private final BigDecimal originalAmountCharged;
    private final BigDecimal convertedOriginalAmountCharged;
    private final BigDecimal amountCredited;
    private final BigDecimal convertedAmountCredited;
    private final BigDecimal amountRefunded;
    private final BigDecimal convertedAmountRefunded;
    private final boolean writtenOff;
    private final String convertedCurrency;
    private final List<BusinessInvoiceItem> invoiceItems = new LinkedList<BusinessInvoiceItem>();

    public BusinessInvoice(final BusinessInvoiceModelDao businessInvoiceModelDao,
                           final Collection<BusinessInvoiceItemBaseModelDao> businessInvoiceItemModelDaos) {
        super(businessInvoiceModelDao.getCreatedDate(),
              businessInvoiceModelDao.getCreatedBy(),
              businessInvoiceModelDao.getCreatedReasonCode(),
              businessInvoiceModelDao.getCreatedComments(),
              businessInvoiceModelDao.getAccountId(),
              businessInvoiceModelDao.getAccountName(),
              businessInvoiceModelDao.getAccountExternalKey(),
              businessInvoiceModelDao.getReportGroup());
        this.invoiceId = businessInvoiceModelDao.getInvoiceId();
        this.invoiceNumber = businessInvoiceModelDao.getInvoiceNumber();
        this.invoiceDate = businessInvoiceModelDao.getInvoiceDate();
        this.targetDate = businessInvoiceModelDao.getTargetDate();
        this.currency = businessInvoiceModelDao.getCurrency();
        this.rawBalance = businessInvoiceModelDao.getRawBalance();
        this.convertedRawBalance = businessInvoiceModelDao.getConvertedRawBalance();
        this.balance = businessInvoiceModelDao.getBalance();
        this.convertedBalance = businessInvoiceModelDao.getConvertedBalance();
        this.amountPaid = businessInvoiceModelDao.getAmountPaid();
        this.convertedAmountPaid = businessInvoiceModelDao.getConvertedAmountPaid();
        this.amountCharged = businessInvoiceModelDao.getAmountCharged();
        this.convertedAmountCharged = businessInvoiceModelDao.getConvertedAmountCharged();
        this.originalAmountCharged = businessInvoiceModelDao.getOriginalAmountCharged();
        this.convertedOriginalAmountCharged = businessInvoiceModelDao.getConvertedOriginalAmountCharged();
        this.amountCredited = businessInvoiceModelDao.getAmountCredited();
        this.convertedAmountCredited = businessInvoiceModelDao.getConvertedAmountCredited();
        this.amountRefunded = businessInvoiceModelDao.getAmountRefunded();
        this.convertedAmountRefunded = businessInvoiceModelDao.getConvertedAmountRefunded();
        this.convertedCurrency = businessInvoiceModelDao.getConvertedCurrency();
        this.writtenOff = businessInvoiceModelDao.isWrittenOff();
        for (final BusinessInvoiceItemBaseModelDao businessInvoiceItemModelDao : businessInvoiceItemModelDaos) {
            invoiceItems.add(new BusinessInvoiceItem(businessInvoiceItemModelDao));
        }
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getRawBalance() {
        return rawBalance;
    }

    public BigDecimal getConvertedRawBalance() {
        return convertedRawBalance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getConvertedBalance() {
        return convertedBalance;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getConvertedAmountPaid() {
        return convertedAmountPaid;
    }

    public BigDecimal getAmountCharged() {
        return amountCharged;
    }

    public BigDecimal getConvertedAmountCharged() {
        return convertedAmountCharged;
    }

    public BigDecimal getOriginalAmountCharged() {
        return originalAmountCharged;
    }

    public BigDecimal getConvertedOriginalAmountCharged() {
        return convertedOriginalAmountCharged;
    }

    public BigDecimal getAmountCredited() {
        return amountCredited;
    }

    public BigDecimal getConvertedAmountCredited() {
        return convertedAmountCredited;
    }

    public BigDecimal getAmountRefunded() {
        return amountRefunded;
    }

    public BigDecimal getConvertedAmountRefunded() {
        return convertedAmountRefunded;
    }

    public boolean isWrittenOff() {
        return writtenOff;
    }

    public String getConvertedCurrency() {
        return convertedCurrency;
    }

    public List<BusinessInvoiceItem> getInvoiceItems() {
        return invoiceItems;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessInvoice{");
        sb.append("invoiceId=").append(invoiceId);
        sb.append(", invoiceNumber=").append(invoiceNumber);
        sb.append(", invoiceDate=").append(invoiceDate);
        sb.append(", targetDate=").append(targetDate);
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", rawBalance=").append(rawBalance);
        sb.append(", convertedRawBalance=").append(convertedRawBalance);
        sb.append(", balance=").append(balance);
        sb.append(", convertedBalance=").append(convertedBalance);
        sb.append(", amountPaid=").append(amountPaid);
        sb.append(", convertedAmountPaid=").append(convertedAmountPaid);
        sb.append(", amountCharged=").append(amountCharged);
        sb.append(", convertedAmountCharged=").append(convertedAmountCharged);
        sb.append(", originalAmountCharged=").append(originalAmountCharged);
        sb.append(", convertedOriginalAmountCharged=").append(convertedOriginalAmountCharged);
        sb.append(", amountCredited=").append(amountCredited);
        sb.append(", convertedAmountCredited=").append(convertedAmountCredited);
        sb.append(", amountRefunded=").append(amountRefunded);
        sb.append(", convertedAmountRefunded=").append(convertedAmountRefunded);
        sb.append(", writtenOff=").append(writtenOff);
        sb.append(", convertedCurrency='").append(convertedCurrency).append('\'');
        sb.append(", invoiceItems=").append(invoiceItems);
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
        if (!super.equals(o)) {
            return false;
        }

        final BusinessInvoice that = (BusinessInvoice) o;

        if (amountCharged != null ? !(amountCharged.compareTo(that.amountCharged) == 0) : that.amountCharged != null) {
            return false;
        }
        if (amountCredited != null ? !(amountCredited.compareTo(that.amountCredited) == 0) : that.amountCredited != null) {
            return false;
        }
        if (amountPaid != null ? !(amountPaid.compareTo(that.amountPaid) == 0) : that.amountPaid != null) {
            return false;
        }
        if (amountRefunded != null ? !(amountRefunded.compareTo(that.amountRefunded) == 0) : that.amountRefunded != null) {
            return false;
        }
        if (balance != null ? !(balance.compareTo(that.balance) == 0) : that.balance != null) {
            return false;
        }
        if (convertedAmountCharged != null ? !(convertedAmountCharged.compareTo(that.convertedAmountCharged) == 0) : that.convertedAmountCharged != null) {
            return false;
        }
        if (convertedAmountCredited != null ? !(convertedAmountCredited.compareTo(that.convertedAmountCredited) == 0) : that.convertedAmountCredited != null) {
            return false;
        }
        if (convertedAmountPaid != null ? !(convertedAmountPaid.compareTo(that.convertedAmountPaid) == 0) : that.convertedAmountPaid != null) {
            return false;
        }
        if (convertedAmountRefunded != null ? !(convertedAmountRefunded.compareTo(that.convertedAmountRefunded) == 0) : that.convertedAmountRefunded != null) {
            return false;
        }
        if (convertedBalance != null ? !(convertedBalance.compareTo(that.convertedBalance) == 0) : that.convertedBalance != null) {
            return false;
        }
        if (convertedCurrency != null ? !convertedCurrency.equals(that.convertedCurrency) : that.convertedCurrency != null) {
            return false;
        }
        if (convertedOriginalAmountCharged != null ? !(convertedOriginalAmountCharged.compareTo(that.convertedOriginalAmountCharged) == 0) : that.convertedOriginalAmountCharged != null) {
            return false;
        }
        if (convertedRawBalance != null ? !(convertedRawBalance.compareTo(that.convertedRawBalance) == 0) : that.convertedRawBalance != null) {
            return false;
        }
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) {
            return false;
        }
        if (invoiceDate != null ? invoiceDate.compareTo(that.invoiceDate) != 0 : that.invoiceDate != null) {
            return false;
        }
        if (invoiceId != null ? !invoiceId.equals(that.invoiceId) : that.invoiceId != null) {
            return false;
        }
        if (invoiceItems != null ? !invoiceItems.equals(that.invoiceItems) : that.invoiceItems != null) {
            return false;
        }
        if (invoiceNumber != null ? !invoiceNumber.equals(that.invoiceNumber) : that.invoiceNumber != null) {
            return false;
        }
        if (originalAmountCharged != null ? !(originalAmountCharged.compareTo(that.originalAmountCharged) == 0) : that.originalAmountCharged != null) {
            return false;
        }
        if (rawBalance != null ? !(rawBalance.compareTo(that.rawBalance) == 0) : that.rawBalance != null) {
            return false;
        }
        if (targetDate != null ? targetDate.compareTo(that.targetDate) != 0 : that.targetDate != null) {
            return false;
        }
        if (writtenOff != that.writtenOff) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (invoiceId != null ? invoiceId.hashCode() : 0);
        result = 31 * result + (invoiceNumber != null ? invoiceNumber.hashCode() : 0);
        result = 31 * result + (invoiceDate != null ? invoiceDate.hashCode() : 0);
        result = 31 * result + (targetDate != null ? targetDate.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (rawBalance != null ? rawBalance.hashCode() : 0);
        result = 31 * result + (convertedRawBalance != null ? convertedRawBalance.hashCode() : 0);
        result = 31 * result + (balance != null ? balance.hashCode() : 0);
        result = 31 * result + (convertedBalance != null ? convertedBalance.hashCode() : 0);
        result = 31 * result + (amountPaid != null ? amountPaid.hashCode() : 0);
        result = 31 * result + (convertedAmountPaid != null ? convertedAmountPaid.hashCode() : 0);
        result = 31 * result + (amountCharged != null ? amountCharged.hashCode() : 0);
        result = 31 * result + (convertedAmountCharged != null ? convertedAmountCharged.hashCode() : 0);
        result = 31 * result + (originalAmountCharged != null ? originalAmountCharged.hashCode() : 0);
        result = 31 * result + (convertedOriginalAmountCharged != null ? convertedOriginalAmountCharged.hashCode() : 0);
        result = 31 * result + (amountCredited != null ? amountCredited.hashCode() : 0);
        result = 31 * result + (convertedAmountCredited != null ? convertedAmountCredited.hashCode() : 0);
        result = 31 * result + (amountRefunded != null ? amountRefunded.hashCode() : 0);
        result = 31 * result + (convertedAmountRefunded != null ? convertedAmountRefunded.hashCode() : 0);
        result = 31 * result + (convertedCurrency != null ? convertedCurrency.hashCode() : 0);
        result = 31 * result + (invoiceItems != null ? invoiceItems.hashCode() : 0);
        result = 31 * result + (writtenOff ? 1 : 0);
        return result;
    }
}
