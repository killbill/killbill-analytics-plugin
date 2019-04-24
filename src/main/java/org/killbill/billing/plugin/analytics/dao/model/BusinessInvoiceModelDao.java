/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2019 Groupon, Inc
 * Copyright 2014-2019 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.dao.model;

import java.math.BigDecimal;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.plugin.analytics.utils.BusinessInvoiceUtils;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AuditLog;

public class BusinessInvoiceModelDao extends BusinessModelDaoBase {

    public static final String INVOICES_TABLE_NAME = "analytics_invoices";

    private Long invoiceRecordId;
    private UUID invoiceId;
    private Integer invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate targetDate;
    private String currency;
    private BigDecimal rawBalance;
    private BigDecimal convertedRawBalance;
    private BigDecimal balance;
    private BigDecimal convertedBalance;
    private BigDecimal amountPaid;
    private BigDecimal convertedAmountPaid;
    private BigDecimal amountCharged;
    private BigDecimal convertedAmountCharged;
    private BigDecimal originalAmountCharged;
    private BigDecimal convertedOriginalAmountCharged;
    private BigDecimal amountCredited;
    private BigDecimal convertedAmountCredited;
    private BigDecimal amountRefunded;
    private BigDecimal convertedAmountRefunded;
    private String convertedCurrency;
    private boolean writtenOff;

    public BusinessInvoiceModelDao() { /* When reading from the database */ }

    public BusinessInvoiceModelDao(final Long invoiceRecordId,
                                   final UUID invoiceId,
                                   final Integer invoiceNumber,
                                   final LocalDate invoiceDate,
                                   final LocalDate targetDate,
                                   final String currency,
                                   final BigDecimal rawBalance,
                                   final BigDecimal convertedRawBalance,
                                   final BigDecimal balance,
                                   final BigDecimal convertedBalance,
                                   final BigDecimal amountPaid,
                                   final BigDecimal convertedAmountPaid,
                                   final BigDecimal amountCharged,
                                   final BigDecimal convertedAmountCharged,
                                   final BigDecimal originalAmountCharged,
                                   final BigDecimal convertedOriginalAmountCharged,
                                   final BigDecimal amountCredited,
                                   final BigDecimal convertedAmountCredited,
                                   final BigDecimal amountRefunded,
                                   final BigDecimal convertedAmountRefunded,
                                   final String convertedCurrency,
                                   final boolean writtenOff,
                                   final DateTime createdDate,
                                   final String createdBy,
                                   final String createdReasonCode,
                                   final String createdComments,
                                   final UUID accountId,
                                   final String accountName,
                                   final String accountExternalKey,
                                   final Long accountRecordId,
                                   final Long tenantRecordId,
                                   @Nullable final ReportGroup reportGroup) {
        super(createdDate,
              createdBy,
              createdReasonCode,
              createdComments,
              accountId,
              accountName,
              accountExternalKey,
              accountRecordId,
              tenantRecordId,
              reportGroup);
        this.invoiceRecordId = invoiceRecordId;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.targetDate = targetDate;
        this.currency = currency;
        this.rawBalance = rawBalance;
        this.convertedRawBalance = convertedRawBalance;
        this.balance = balance;
        this.convertedBalance = convertedBalance;
        this.amountPaid = amountPaid;
        this.convertedAmountPaid = convertedAmountPaid;
        this.amountCharged = amountCharged;
        this.convertedAmountCharged = convertedAmountCharged;
        this.originalAmountCharged = originalAmountCharged;
        this.convertedOriginalAmountCharged = convertedOriginalAmountCharged;
        this.amountCredited = amountCredited;
        this.convertedAmountCredited = convertedAmountCredited;
        this.amountRefunded = amountRefunded;
        this.convertedAmountRefunded = convertedAmountRefunded;
        this.convertedCurrency = convertedCurrency;
        this.writtenOff = writtenOff;
    }

    public BusinessInvoiceModelDao(final Account account,
                                   final Long accountRecordId,
                                   final Invoice invoice,
                                   final boolean writtenOff,
                                   final Long invoiceRecordId,
                                   final CurrencyConverter currencyConverter,
                                   @Nullable final AuditLog creationAuditLog,
                                   final Long tenantRecordId,
                                   @Nullable final ReportGroup reportGroup) {
        this(invoiceRecordId,
             invoice.getId(),
             invoice.getInvoiceNumber(),
             invoice.getInvoiceDate(),
             invoice.getTargetDate(),
             invoice.getCurrency() == null ? null : invoice.getCurrency().toString(),
             BusinessInvoiceUtils.computeRawInvoiceBalance(invoice.getCurrency(), invoice.getInvoiceItems(), invoice.getPayments()),
             currencyConverter.getConvertedValue(BusinessInvoiceUtils.computeRawInvoiceBalance(invoice.getCurrency(), invoice.getInvoiceItems(), invoice.getPayments()), invoice),
             invoice.getBalance(),
             currencyConverter.getConvertedValue(invoice.getBalance(), invoice),
             invoice.getPaidAmount(),
             currencyConverter.getConvertedValue(invoice.getPaidAmount(), invoice),
             invoice.getChargedAmount(),
             currencyConverter.getConvertedValue(invoice.getChargedAmount(), invoice),
             invoice.getOriginalChargedAmount(),
             currencyConverter.getConvertedValue(invoice.getOriginalChargedAmount(), invoice),
             invoice.getCreditedAmount(),
             currencyConverter.getConvertedValue(invoice.getCreditedAmount(), invoice),
             invoice.getRefundedAmount(),
             currencyConverter.getConvertedValue(invoice.getRefundedAmount(), invoice),
             currencyConverter.getConvertedCurrency(),
             writtenOff,
             invoice.getCreatedDate(),
             creationAuditLog != null ? creationAuditLog.getUserName() : null,
             creationAuditLog != null ? creationAuditLog.getReasonCode() : null,
             creationAuditLog != null ? creationAuditLog.getComment() : null,
             account.getId(),
             account.getName(),
             account.getExternalKey(),
             accountRecordId,
             tenantRecordId,
             reportGroup);
    }

    @Override
    public String getTableName() {
        return INVOICES_TABLE_NAME;
    }

    public Long getInvoiceRecordId() {
        return invoiceRecordId;
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

    public String getConvertedCurrency() {
        return convertedCurrency;
    }

    public boolean isWrittenOff() {
        return writtenOff;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessInvoiceModelDao{");
        sb.append("invoiceRecordId=").append(invoiceRecordId);
        sb.append(", invoiceId=").append(invoiceId);
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
        sb.append(", convertedCurrency='").append(convertedCurrency).append('\'');
        sb.append(", writtenOff='").append(writtenOff).append('\'');
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

        final BusinessInvoiceModelDao that = (BusinessInvoiceModelDao) o;

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
        if (invoiceNumber != null ? !invoiceNumber.equals(that.invoiceNumber) : that.invoiceNumber != null) {
            return false;
        }
        if (invoiceRecordId != null ? !invoiceRecordId.equals(that.invoiceRecordId) : that.invoiceRecordId != null) {
            return false;
        }
        if (originalAmountCharged != null ? !(originalAmountCharged.compareTo(that.originalAmountCharged) == 0) : that.originalAmountCharged != null) {
            return false;
        }
        if (rawBalance != null ? rawBalance.compareTo(that.rawBalance) != 0 : that.rawBalance != null) {
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
        result = 31 * result + (invoiceRecordId != null ? invoiceRecordId.hashCode() : 0);
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
        result = 31 * result + (writtenOff ? 1 : 0);
        return result;
    }
}
