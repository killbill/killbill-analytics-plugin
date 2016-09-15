/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2015 Groupon, Inc
 * Copyright 2014-2015 The Billing Project, LLC
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
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentBaseModelDao;

public class BusinessPayment extends BusinessEntityBase {

    private final UUID invoicePaymentId;
    private final UUID invoiceId;
    private final Integer invoiceNumber;
    private final DateTime invoiceCreatedDate;
    private final LocalDate invoiceDate;
    private final LocalDate invoiceTargetDate;
    private final String invoiceCurrency;
    private final BigDecimal invoiceBalance;
    private final BigDecimal convertedInvoiceBalance;
    private final BigDecimal invoiceAmountPaid;
    private final BigDecimal convertedInvoiceAmountPaid;
    private final BigDecimal invoiceAmountCharged;
    private final BigDecimal convertedInvoiceAmountCharged;
    private final BigDecimal invoiceOriginalAmountCharged;
    private final BigDecimal convertedInvoiceOriginalAmountCharged;
    private final BigDecimal invoiceAmountCredited;
    private final BigDecimal convertedInvoiceAmountCredited;
    private final BigDecimal invoiceAmountRefunded;
    private final BigDecimal convertedInvoiceAmountRefunded;
    private final String invoicePaymentType;
    private final UUID paymentId;
    private final UUID refundId;
    private final Long paymentNumber;
    private final String paymentExternalKey;
    private final UUID paymentTransactionId;
    private final String paymentTransactionExternalKey;
    private final String paymentTransactionStatus;
    private final UUID linkedInvoicePaymentId;
    private final BigDecimal amount;
    private final BigDecimal convertedAmount;
    private final String currency;
    private final String pluginName;
    private final UUID paymentMethodId;
    private final String paymentMethodExternalKey;
    private final DateTime pluginCreatedDate;
    private final DateTime pluginEffectiveDate;
    private final String pluginStatus;
    private final String pluginGatewayError;
    private final String pluginGatewayErrorCode;
    private final String pluginFirstReferenceId;
    private final String pluginSecondReferenceId;
    private String pluginProperty1;
    private String pluginProperty2;
    private String pluginProperty3;
    private String pluginProperty4;
    private String pluginProperty5;
    private final String pluginPmId;
    private final Boolean pluginPmIsDefault;
    private final String pluginPmType;
    private final String pluginPmCcName;
    private final String pluginPmCcType;
    private final String pluginPmCcExpirationMonth;
    private final String pluginPmCcExpirationYear;
    private final String pluginPmCcLast4;
    private final String pluginPmAddress1;
    private final String pluginPmAddress2;
    private final String pluginPmCity;
    private final String pluginPmState;
    private final String pluginPmZip;
    private final String pluginPmCountry;
    private final String convertedCurrency;

    public BusinessPayment(final BusinessPaymentBaseModelDao businessPaymentBaseModelDao) {
        super(businessPaymentBaseModelDao.getCreatedDate(),
              businessPaymentBaseModelDao.getCreatedBy(),
              businessPaymentBaseModelDao.getCreatedReasonCode(),
              businessPaymentBaseModelDao.getCreatedComments(),
              businessPaymentBaseModelDao.getAccountId(),
              businessPaymentBaseModelDao.getAccountName(),
              businessPaymentBaseModelDao.getAccountExternalKey(),
              businessPaymentBaseModelDao.getReportGroup());
        this.invoicePaymentId = businessPaymentBaseModelDao.getInvoicePaymentId();
        this.invoiceId = businessPaymentBaseModelDao.getInvoiceId();
        this.invoiceNumber = businessPaymentBaseModelDao.getInvoiceNumber();
        this.invoiceCreatedDate = businessPaymentBaseModelDao.getInvoiceCreatedDate();
        this.invoiceDate = businessPaymentBaseModelDao.getInvoiceDate();
        this.invoiceTargetDate = businessPaymentBaseModelDao.getInvoiceTargetDate();
        this.invoiceCurrency = businessPaymentBaseModelDao.getInvoiceCurrency();
        this.invoiceBalance = businessPaymentBaseModelDao.getInvoiceBalance();
        this.convertedInvoiceBalance = businessPaymentBaseModelDao.getConvertedInvoiceBalance();
        this.invoiceAmountPaid = businessPaymentBaseModelDao.getInvoiceAmountPaid();
        this.convertedInvoiceAmountPaid = businessPaymentBaseModelDao.getConvertedInvoiceAmountPaid();
        this.invoiceAmountCharged = businessPaymentBaseModelDao.getInvoiceAmountCharged();
        this.convertedInvoiceAmountCharged = businessPaymentBaseModelDao.getConvertedInvoiceAmountCharged();
        this.invoiceOriginalAmountCharged = businessPaymentBaseModelDao.getInvoiceOriginalAmountCharged();
        this.convertedInvoiceOriginalAmountCharged = businessPaymentBaseModelDao.getConvertedInvoiceOriginalAmountCharged();
        this.invoiceAmountCredited = businessPaymentBaseModelDao.getInvoiceAmountCredited();
        this.convertedInvoiceAmountCredited = businessPaymentBaseModelDao.getConvertedInvoiceAmountCredited();
        this.invoiceAmountRefunded = businessPaymentBaseModelDao.getInvoiceAmountRefunded();
        this.convertedInvoiceAmountRefunded = businessPaymentBaseModelDao.getConvertedInvoiceAmountRefunded();
        this.invoicePaymentType = businessPaymentBaseModelDao.getInvoicePaymentType();
        this.paymentId = businessPaymentBaseModelDao.getPaymentId();
        this.refundId = businessPaymentBaseModelDao.getRefundId();
        this.paymentNumber = businessPaymentBaseModelDao.getPaymentNumber();
        this.paymentExternalKey = businessPaymentBaseModelDao.getPaymentExternalKey();
        this.paymentTransactionId = businessPaymentBaseModelDao.getPaymentTransactionId();
        this.paymentTransactionExternalKey = businessPaymentBaseModelDao.getPaymentTransactionExternalKey();
        this.paymentTransactionStatus = businessPaymentBaseModelDao.getPaymentTransactionStatus();
        this.linkedInvoicePaymentId = businessPaymentBaseModelDao.getLinkedInvoicePaymentId();
        this.amount = businessPaymentBaseModelDao.getAmount();
        this.convertedAmount = businessPaymentBaseModelDao.getConvertedAmount();
        this.currency = businessPaymentBaseModelDao.getCurrency();
        this.pluginName = businessPaymentBaseModelDao.getPluginName();
        this.paymentMethodId = businessPaymentBaseModelDao.getPaymentMethodId();
        this.paymentMethodExternalKey = businessPaymentBaseModelDao.getPaymentMethodExternalKey();
        this.pluginCreatedDate = businessPaymentBaseModelDao.getPluginCreatedDate();
        this.pluginEffectiveDate = businessPaymentBaseModelDao.getPluginEffectiveDate();
        this.pluginStatus = businessPaymentBaseModelDao.getPluginStatus();
        this.pluginGatewayError = businessPaymentBaseModelDao.getPluginGatewayError();
        this.pluginGatewayErrorCode = businessPaymentBaseModelDao.getPluginGatewayErrorCode();
        this.pluginFirstReferenceId = businessPaymentBaseModelDao.getPluginFirstReferenceId();
        this.pluginSecondReferenceId = businessPaymentBaseModelDao.getPluginSecondReferenceId();
        this.pluginProperty1 = businessPaymentBaseModelDao.getPluginProperty1();
        this.pluginProperty2 = businessPaymentBaseModelDao.getPluginProperty2();
        this.pluginProperty3 = businessPaymentBaseModelDao.getPluginProperty3();
        this.pluginProperty4 = businessPaymentBaseModelDao.getPluginProperty4();
        this.pluginProperty5 = businessPaymentBaseModelDao.getPluginProperty5();
        this.pluginPmId = businessPaymentBaseModelDao.getPluginPmId();
        this.pluginPmIsDefault = businessPaymentBaseModelDao.getPluginPmIsDefault();
        this.pluginPmType = businessPaymentBaseModelDao.getPluginPmType();
        this.pluginPmCcName = businessPaymentBaseModelDao.getPluginPmCcName();
        this.pluginPmCcType = businessPaymentBaseModelDao.getPluginPmCcType();
        this.pluginPmCcExpirationMonth = businessPaymentBaseModelDao.getPluginPmCcExpirationMonth();
        this.pluginPmCcExpirationYear = businessPaymentBaseModelDao.getPluginPmCcExpirationYear();
        this.pluginPmCcLast4 = businessPaymentBaseModelDao.getPluginPmCcLast4();
        this.pluginPmAddress1 = businessPaymentBaseModelDao.getPluginPmAddress1();
        this.pluginPmAddress2 = businessPaymentBaseModelDao.getPluginPmAddress2();
        this.pluginPmCity = businessPaymentBaseModelDao.getPluginPmCity();
        this.pluginPmState = businessPaymentBaseModelDao.getPluginPmState();
        this.pluginPmZip = businessPaymentBaseModelDao.getPluginPmZip();
        this.pluginPmCountry = businessPaymentBaseModelDao.getPluginPmCountry();
        this.convertedCurrency = businessPaymentBaseModelDao.getConvertedCurrency();
    }

    public UUID getInvoicePaymentId() {
        return invoicePaymentId;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public DateTime getInvoiceCreatedDate() {
        return invoiceCreatedDate;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public LocalDate getInvoiceTargetDate() {
        return invoiceTargetDate;
    }

    public String getInvoiceCurrency() {
        return invoiceCurrency;
    }

    public BigDecimal getInvoiceBalance() {
        return invoiceBalance;
    }

    public BigDecimal getConvertedInvoiceBalance() {
        return convertedInvoiceBalance;
    }

    public BigDecimal getInvoiceAmountPaid() {
        return invoiceAmountPaid;
    }

    public BigDecimal getConvertedInvoiceAmountPaid() {
        return convertedInvoiceAmountPaid;
    }

    public BigDecimal getInvoiceAmountCharged() {
        return invoiceAmountCharged;
    }

    public BigDecimal getConvertedInvoiceAmountCharged() {
        return convertedInvoiceAmountCharged;
    }

    public BigDecimal getInvoiceOriginalAmountCharged() {
        return invoiceOriginalAmountCharged;
    }

    public BigDecimal getConvertedInvoiceOriginalAmountCharged() {
        return convertedInvoiceOriginalAmountCharged;
    }

    public BigDecimal getInvoiceAmountCredited() {
        return invoiceAmountCredited;
    }

    public BigDecimal getConvertedInvoiceAmountCredited() {
        return convertedInvoiceAmountCredited;
    }

    public BigDecimal getInvoiceAmountRefunded() {
        return invoiceAmountRefunded;
    }

    public BigDecimal getConvertedInvoiceAmountRefunded() {
        return convertedInvoiceAmountRefunded;
    }

    public String getInvoicePaymentType() {
        return invoicePaymentType;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getRefundId() {
        return refundId;
    }

    public Long getPaymentNumber() {
        return paymentNumber;
    }

    public String getPaymentExternalKey() {
        return paymentExternalKey;
    }

    public UUID getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public String getPaymentTransactionExternalKey() {
        return paymentTransactionExternalKey;
    }

    public String getPaymentTransactionStatus() {
        return paymentTransactionStatus;
    }

    public UUID getLinkedInvoicePaymentId() {
        return linkedInvoicePaymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPluginName() {
        return pluginName;
    }

    public UUID getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getPaymentMethodExternalKey() {
        return paymentMethodExternalKey;
    }

    public DateTime getPluginCreatedDate() {
        return pluginCreatedDate;
    }

    public DateTime getPluginEffectiveDate() {
        return pluginEffectiveDate;
    }

    public String getPluginStatus() {
        return pluginStatus;
    }

    public String getPluginGatewayError() {
        return pluginGatewayError;
    }

    public String getPluginGatewayErrorCode() {
        return pluginGatewayErrorCode;
    }

    public String getPluginFirstReferenceId() {
        return pluginFirstReferenceId;
    }

    public String getPluginSecondReferenceId() {
        return pluginSecondReferenceId;
    }

    public String getPluginProperty1() {
        return pluginProperty1;
    }

    public String getPluginProperty2() {
        return pluginProperty2;
    }

    public String getPluginProperty3() {
        return pluginProperty3;
    }

    public String getPluginProperty4() {
        return pluginProperty4;
    }

    public String getPluginProperty5() {
        return pluginProperty5;
    }

    public String getPluginPmId() {
        return pluginPmId;
    }

    public Boolean getPluginPmIsDefault() {
        return pluginPmIsDefault;
    }

    public String getPluginPmType() {
        return pluginPmType;
    }

    public String getPluginPmCcName() {
        return pluginPmCcName;
    }

    public String getPluginPmCcType() {
        return pluginPmCcType;
    }

    public String getPluginPmCcExpirationMonth() {
        return pluginPmCcExpirationMonth;
    }

    public String getPluginPmCcExpirationYear() {
        return pluginPmCcExpirationYear;
    }

    public String getPluginPmCcLast4() {
        return pluginPmCcLast4;
    }

    public String getPluginPmAddress1() {
        return pluginPmAddress1;
    }

    public String getPluginPmAddress2() {
        return pluginPmAddress2;
    }

    public String getPluginPmCity() {
        return pluginPmCity;
    }

    public String getPluginPmState() {
        return pluginPmState;
    }

    public String getPluginPmZip() {
        return pluginPmZip;
    }

    public String getPluginPmCountry() {
        return pluginPmCountry;
    }

    public String getConvertedCurrency() {
        return convertedCurrency;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessInvoicePayment{");
        sb.append("invoicePaymentId=").append(invoicePaymentId);
        sb.append(", invoiceId=").append(invoiceId);
        sb.append(", invoiceNumber=").append(invoiceNumber);
        sb.append(", invoiceCreatedDate=").append(invoiceCreatedDate);
        sb.append(", invoiceDate=").append(invoiceDate);
        sb.append(", invoiceTargetDate=").append(invoiceTargetDate);
        sb.append(", invoiceCurrency='").append(invoiceCurrency).append('\'');
        sb.append(", invoiceBalance=").append(invoiceBalance);
        sb.append(", convertedInvoiceBalance=").append(convertedInvoiceBalance);
        sb.append(", invoiceAmountPaid=").append(invoiceAmountPaid);
        sb.append(", convertedInvoiceAmountPaid=").append(convertedInvoiceAmountPaid);
        sb.append(", invoiceAmountCharged=").append(invoiceAmountCharged);
        sb.append(", convertedInvoiceAmountCharged=").append(convertedInvoiceAmountCharged);
        sb.append(", invoiceOriginalAmountCharged=").append(invoiceOriginalAmountCharged);
        sb.append(", convertedInvoiceOriginalAmountCharged=").append(convertedInvoiceOriginalAmountCharged);
        sb.append(", invoiceAmountCredited=").append(invoiceAmountCredited);
        sb.append(", convertedInvoiceAmountCredited=").append(convertedInvoiceAmountCredited);
        sb.append(", invoiceAmountRefunded=").append(invoiceAmountRefunded);
        sb.append(", convertedInvoiceAmountRefunded=").append(convertedInvoiceAmountRefunded);
        sb.append(", invoicePaymentType='").append(invoicePaymentType).append('\'');
        sb.append(", paymentId=").append(paymentId);
        sb.append(", refundId=").append(refundId);
        sb.append(", paymentNumber=").append(paymentNumber);
        sb.append(", paymentExternalKey='").append(paymentExternalKey).append('\'');
        sb.append(", paymentTransactionId=").append(paymentTransactionId);
        sb.append(", paymentTransactionExternalKey='").append(paymentTransactionExternalKey).append('\'');
        sb.append(", paymentTransactionStatus='").append(paymentTransactionStatus).append('\'');
        sb.append(", linkedInvoicePaymentId=").append(linkedInvoicePaymentId);
        sb.append(", amount=").append(amount);
        sb.append(", convertedAmount=").append(convertedAmount);
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", pluginName='").append(pluginName).append('\'');
        sb.append(", paymentMethodId='").append(paymentMethodId).append('\'');
        sb.append(", paymentMethodExternalKey='").append(paymentMethodExternalKey).append('\'');
        sb.append(", pluginCreatedDate=").append(pluginCreatedDate);
        sb.append(", pluginEffectiveDate=").append(pluginEffectiveDate);
        sb.append(", pluginStatus='").append(pluginStatus).append('\'');
        sb.append(", pluginGatewayError='").append(pluginGatewayError).append('\'');
        sb.append(", pluginGatewayErrorCode='").append(pluginGatewayErrorCode).append('\'');
        sb.append(", pluginFirstReferenceId='").append(pluginFirstReferenceId).append('\'');
        sb.append(", pluginSecondReferenceId='").append(pluginSecondReferenceId).append('\'');
        sb.append(", pluginProperty1='").append(pluginProperty1).append('\'');
        sb.append(", pluginProperty2='").append(pluginProperty2).append('\'');
        sb.append(", pluginProperty3='").append(pluginProperty3).append('\'');
        sb.append(", pluginProperty4='").append(pluginProperty4).append('\'');
        sb.append(", pluginProperty5='").append(pluginProperty5).append('\'');
        sb.append(", pluginPmId='").append(pluginPmId).append('\'');
        sb.append(", pluginPmIsDefault=").append(pluginPmIsDefault);
        sb.append(", pluginPmType='").append(pluginPmType).append('\'');
        sb.append(", pluginPmCcName='").append(pluginPmCcName).append('\'');
        sb.append(", pluginPmCcType='").append(pluginPmCcType).append('\'');
        sb.append(", pluginPmCcExpirationMonth='").append(pluginPmCcExpirationMonth).append('\'');
        sb.append(", pluginPmCcExpirationYear='").append(pluginPmCcExpirationYear).append('\'');
        sb.append(", pluginPmCcLast4='").append(pluginPmCcLast4).append('\'');
        sb.append(", pluginPmAddress1='").append(pluginPmAddress1).append('\'');
        sb.append(", pluginPmAddress2='").append(pluginPmAddress2).append('\'');
        sb.append(", pluginPmCity='").append(pluginPmCity).append('\'');
        sb.append(", pluginPmState='").append(pluginPmState).append('\'');
        sb.append(", pluginPmZip='").append(pluginPmZip).append('\'');
        sb.append(", pluginPmCountry='").append(pluginPmCountry).append('\'');
        sb.append(", convertedCurrency='").append(convertedCurrency).append('\'');
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

        final BusinessPayment that = (BusinessPayment) o;

        if (amount != null ? !(amount.compareTo(that.amount) == 0) : that.amount != null) {
            return false;
        }
        if (convertedAmount != null ? !(convertedAmount.compareTo(that.convertedAmount) == 0) : that.convertedAmount != null) {
            return false;
        }
        if (convertedCurrency != null ? !convertedCurrency.equals(that.convertedCurrency) : that.convertedCurrency != null) {
            return false;
        }
        if (convertedInvoiceAmountCharged != null ? !(convertedInvoiceAmountCharged.compareTo(that.convertedInvoiceAmountCharged) == 0) : that.convertedInvoiceAmountCharged != null) {
            return false;
        }
        if (convertedInvoiceAmountCredited != null ? !(convertedInvoiceAmountCredited.compareTo(that.convertedInvoiceAmountCredited) == 0) : that.convertedInvoiceAmountCredited != null) {
            return false;
        }
        if (convertedInvoiceAmountPaid != null ? !(convertedInvoiceAmountPaid.compareTo(that.convertedInvoiceAmountPaid) == 0) : that.convertedInvoiceAmountPaid != null) {
            return false;
        }
        if (convertedInvoiceAmountRefunded != null ? !(convertedInvoiceAmountRefunded.compareTo(that.convertedInvoiceAmountRefunded) == 0) : that.convertedInvoiceAmountRefunded != null) {
            return false;
        }
        if (convertedInvoiceBalance != null ? !(convertedInvoiceBalance.compareTo(that.convertedInvoiceBalance) == 0) : that.convertedInvoiceBalance != null) {
            return false;
        }
        if (convertedInvoiceOriginalAmountCharged != null ? !(convertedInvoiceOriginalAmountCharged.compareTo(that.convertedInvoiceOriginalAmountCharged) == 0) : that.convertedInvoiceOriginalAmountCharged != null) {
            return false;
        }
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) {
            return false;
        }
        if (invoiceAmountCharged != null ? !(invoiceAmountCharged.compareTo(that.invoiceAmountCharged) == 0) : that.invoiceAmountCharged != null) {
            return false;
        }
        if (invoiceAmountCredited != null ? !(invoiceAmountCredited.compareTo(that.invoiceAmountCredited) == 0) : that.invoiceAmountCredited != null) {
            return false;
        }
        if (invoiceAmountPaid != null ? !(invoiceAmountPaid.compareTo(that.invoiceAmountPaid) == 0) : that.invoiceAmountPaid != null) {
            return false;
        }
        if (invoiceAmountRefunded != null ? !(invoiceAmountRefunded.compareTo(that.invoiceAmountRefunded) == 0) : that.invoiceAmountRefunded != null) {
            return false;
        }
        if (invoiceBalance != null ? !(invoiceBalance.compareTo(that.invoiceBalance) == 0) : that.invoiceBalance != null) {
            return false;
        }
        if (invoiceCreatedDate != null ? invoiceCreatedDate.compareTo(that.invoiceCreatedDate) != 0 : that.invoiceCreatedDate != null) {
            return false;
        }
        if (invoiceCurrency != null ? !invoiceCurrency.equals(that.invoiceCurrency) : that.invoiceCurrency != null) {
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
        if (invoiceOriginalAmountCharged != null ? !(invoiceOriginalAmountCharged.compareTo(that.invoiceOriginalAmountCharged) == 0) : that.invoiceOriginalAmountCharged != null) {
            return false;
        }
        if (invoicePaymentId != null ? !invoicePaymentId.equals(that.invoicePaymentId) : that.invoicePaymentId != null) {
            return false;
        }
        if (invoicePaymentType != null ? !invoicePaymentType.equals(that.invoicePaymentType) : that.invoicePaymentType != null) {
            return false;
        }
        if (invoiceTargetDate != null ? invoiceTargetDate.compareTo(that.invoiceTargetDate) != 0 : that.invoiceTargetDate != null) {
            return false;
        }
        if (linkedInvoicePaymentId != null ? !linkedInvoicePaymentId.equals(that.linkedInvoicePaymentId) : that.linkedInvoicePaymentId != null) {
            return false;
        }
        if (paymentId != null ? !paymentId.equals(that.paymentId) : that.paymentId != null) {
            return false;
        }
        if (paymentNumber != null ? !paymentNumber.equals(that.paymentNumber) : that.paymentNumber != null) {
            return false;
        }
        if (paymentExternalKey != null ? !paymentExternalKey.equals(that.paymentExternalKey) : that.paymentExternalKey != null) {
            return false;
        }
        if (paymentTransactionId != null ? !paymentTransactionId.equals(that.paymentTransactionId) : that.paymentTransactionId != null) {
            return false;
        }
        if (paymentTransactionExternalKey != null ? !paymentTransactionExternalKey.equals(that.paymentTransactionExternalKey) : that.paymentTransactionExternalKey != null) {
            return false;
        }
        if (paymentTransactionStatus != null ? !paymentTransactionStatus.equals(that.paymentTransactionStatus) : that.paymentTransactionStatus != null) {
            return false;
        }
        if (pluginCreatedDate != null ? pluginCreatedDate.compareTo(that.pluginCreatedDate) != 0 : that.pluginCreatedDate != null) {
            return false;
        }
        if (pluginEffectiveDate != null ? pluginEffectiveDate.compareTo(that.pluginEffectiveDate) != 0 : that.pluginEffectiveDate != null) {
            return false;
        }
        if (pluginFirstReferenceId != null ? !pluginFirstReferenceId.equals(that.pluginFirstReferenceId) : that.pluginFirstReferenceId != null) {
            return false;
        }
        if (pluginSecondReferenceId != null ? !pluginSecondReferenceId.equals(that.pluginSecondReferenceId) : that.pluginSecondReferenceId != null) {
            return false;
        }
        if (pluginProperty1 != null ? !pluginProperty1.equals(that.pluginProperty1) : that.pluginProperty1 != null) {
            return false;
        }
        if (pluginProperty2 != null ? !pluginProperty2.equals(that.pluginProperty2) : that.pluginProperty2 != null) {
            return false;
        }
        if (pluginProperty3 != null ? !pluginProperty3.equals(that.pluginProperty3) : that.pluginProperty3 != null) {
            return false;
        }
        if (pluginProperty4 != null ? !pluginProperty4.equals(that.pluginProperty4) : that.pluginProperty4 != null) {
            return false;
        }
        if (pluginProperty5 != null ? !pluginProperty5.equals(that.pluginProperty5) : that.pluginProperty5 != null) {
            return false;
        }
        if (pluginGatewayError != null ? !pluginGatewayError.equals(that.pluginGatewayError) : that.pluginGatewayError != null) {
            return false;
        }
        if (pluginGatewayErrorCode != null ? !pluginGatewayErrorCode.equals(that.pluginGatewayErrorCode) : that.pluginGatewayErrorCode != null) {
            return false;
        }
        if (pluginName != null ? !pluginName.equals(that.pluginName) : that.pluginName != null) {
            return false;
        }
        if (paymentMethodId != null ? !paymentMethodId.equals(that.paymentMethodId) : that.paymentMethodId != null) {
            return false;
        }
        if (paymentMethodExternalKey != null ? !paymentMethodExternalKey.equals(that.paymentMethodExternalKey) : that.paymentMethodExternalKey != null) {
            return false;
        }
        if (pluginPmAddress1 != null ? !pluginPmAddress1.equals(that.pluginPmAddress1) : that.pluginPmAddress1 != null) {
            return false;
        }
        if (pluginPmAddress2 != null ? !pluginPmAddress2.equals(that.pluginPmAddress2) : that.pluginPmAddress2 != null) {
            return false;
        }
        if (pluginPmCcExpirationMonth != null ? !pluginPmCcExpirationMonth.equals(that.pluginPmCcExpirationMonth) : that.pluginPmCcExpirationMonth != null) {
            return false;
        }
        if (pluginPmCcExpirationYear != null ? !pluginPmCcExpirationYear.equals(that.pluginPmCcExpirationYear) : that.pluginPmCcExpirationYear != null) {
            return false;
        }
        if (pluginPmCcLast4 != null ? !pluginPmCcLast4.equals(that.pluginPmCcLast4) : that.pluginPmCcLast4 != null) {
            return false;
        }
        if (pluginPmCcName != null ? !pluginPmCcName.equals(that.pluginPmCcName) : that.pluginPmCcName != null) {
            return false;
        }
        if (pluginPmCcType != null ? !pluginPmCcType.equals(that.pluginPmCcType) : that.pluginPmCcType != null) {
            return false;
        }
        if (pluginPmCity != null ? !pluginPmCity.equals(that.pluginPmCity) : that.pluginPmCity != null) {
            return false;
        }
        if (pluginPmCountry != null ? !pluginPmCountry.equals(that.pluginPmCountry) : that.pluginPmCountry != null) {
            return false;
        }
        if (pluginPmId != null ? !pluginPmId.equals(that.pluginPmId) : that.pluginPmId != null) {
            return false;
        }
        if (pluginPmIsDefault != null ? !pluginPmIsDefault.equals(that.pluginPmIsDefault) : that.pluginPmIsDefault != null) {
            return false;
        }
        if (pluginPmState != null ? !pluginPmState.equals(that.pluginPmState) : that.pluginPmState != null) {
            return false;
        }
        if (pluginPmType != null ? !pluginPmType.equals(that.pluginPmType) : that.pluginPmType != null) {
            return false;
        }
        if (pluginPmZip != null ? !pluginPmZip.equals(that.pluginPmZip) : that.pluginPmZip != null) {
            return false;
        }
        if (pluginStatus != null ? !pluginStatus.equals(that.pluginStatus) : that.pluginStatus != null) {
            return false;
        }
        if (refundId != null ? !refundId.equals(that.refundId) : that.refundId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (invoicePaymentId != null ? invoicePaymentId.hashCode() : 0);
        result = 31 * result + (invoiceId != null ? invoiceId.hashCode() : 0);
        result = 31 * result + (invoiceNumber != null ? invoiceNumber.hashCode() : 0);
        result = 31 * result + (invoiceCreatedDate != null ? invoiceCreatedDate.hashCode() : 0);
        result = 31 * result + (invoiceDate != null ? invoiceDate.hashCode() : 0);
        result = 31 * result + (invoiceTargetDate != null ? invoiceTargetDate.hashCode() : 0);
        result = 31 * result + (invoiceCurrency != null ? invoiceCurrency.hashCode() : 0);
        result = 31 * result + (invoiceBalance != null ? invoiceBalance.hashCode() : 0);
        result = 31 * result + (convertedInvoiceBalance != null ? convertedInvoiceBalance.hashCode() : 0);
        result = 31 * result + (invoiceAmountPaid != null ? invoiceAmountPaid.hashCode() : 0);
        result = 31 * result + (convertedInvoiceAmountPaid != null ? convertedInvoiceAmountPaid.hashCode() : 0);
        result = 31 * result + (invoiceAmountCharged != null ? invoiceAmountCharged.hashCode() : 0);
        result = 31 * result + (convertedInvoiceAmountCharged != null ? convertedInvoiceAmountCharged.hashCode() : 0);
        result = 31 * result + (invoiceOriginalAmountCharged != null ? invoiceOriginalAmountCharged.hashCode() : 0);
        result = 31 * result + (convertedInvoiceOriginalAmountCharged != null ? convertedInvoiceOriginalAmountCharged.hashCode() : 0);
        result = 31 * result + (invoiceAmountCredited != null ? invoiceAmountCredited.hashCode() : 0);
        result = 31 * result + (convertedInvoiceAmountCredited != null ? convertedInvoiceAmountCredited.hashCode() : 0);
        result = 31 * result + (invoiceAmountRefunded != null ? invoiceAmountRefunded.hashCode() : 0);
        result = 31 * result + (convertedInvoiceAmountRefunded != null ? convertedInvoiceAmountRefunded.hashCode() : 0);
        result = 31 * result + (invoicePaymentType != null ? invoicePaymentType.hashCode() : 0);
        result = 31 * result + (paymentId != null ? paymentId.hashCode() : 0);
        result = 31 * result + (refundId != null ? refundId.hashCode() : 0);
        result = 31 * result + (paymentNumber != null ? paymentNumber.hashCode() : 0);
        result = 31 * result + (paymentExternalKey != null ? paymentExternalKey.hashCode() : 0);
        result = 31 * result + (paymentTransactionId != null ? paymentTransactionId.hashCode() : 0);
        result = 31 * result + (paymentTransactionExternalKey != null ? paymentTransactionExternalKey.hashCode() : 0);
        result = 31 * result + (paymentTransactionStatus != null ? paymentTransactionStatus.hashCode() : 0);
        result = 31 * result + (linkedInvoicePaymentId != null ? linkedInvoicePaymentId.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (convertedAmount != null ? convertedAmount.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (pluginName != null ? pluginName.hashCode() : 0);
        result = 31 * result + (paymentMethodId != null ? paymentMethodId.hashCode() : 0);
        result = 31 * result + (paymentMethodExternalKey != null ? paymentMethodExternalKey.hashCode() : 0);
        result = 31 * result + (pluginCreatedDate != null ? pluginCreatedDate.hashCode() : 0);
        result = 31 * result + (pluginEffectiveDate != null ? pluginEffectiveDate.hashCode() : 0);
        result = 31 * result + (pluginStatus != null ? pluginStatus.hashCode() : 0);
        result = 31 * result + (pluginGatewayError != null ? pluginGatewayError.hashCode() : 0);
        result = 31 * result + (pluginGatewayErrorCode != null ? pluginGatewayErrorCode.hashCode() : 0);
        result = 31 * result + (pluginFirstReferenceId != null ? pluginFirstReferenceId.hashCode() : 0);
        result = 31 * result + (pluginSecondReferenceId != null ? pluginSecondReferenceId.hashCode() : 0);
        result = 31 * result + (pluginProperty1 != null ? pluginProperty1.hashCode() : 0);
        result = 31 * result + (pluginProperty2 != null ? pluginProperty2.hashCode() : 0);
        result = 31 * result + (pluginProperty3 != null ? pluginProperty3.hashCode() : 0);
        result = 31 * result + (pluginProperty4 != null ? pluginProperty4.hashCode() : 0);
        result = 31 * result + (pluginProperty5 != null ? pluginProperty5.hashCode() : 0);
        result = 31 * result + (pluginPmId != null ? pluginPmId.hashCode() : 0);
        result = 31 * result + (pluginPmIsDefault != null ? pluginPmIsDefault.hashCode() : 0);
        result = 31 * result + (pluginPmType != null ? pluginPmType.hashCode() : 0);
        result = 31 * result + (pluginPmCcName != null ? pluginPmCcName.hashCode() : 0);
        result = 31 * result + (pluginPmCcType != null ? pluginPmCcType.hashCode() : 0);
        result = 31 * result + (pluginPmCcExpirationMonth != null ? pluginPmCcExpirationMonth.hashCode() : 0);
        result = 31 * result + (pluginPmCcExpirationYear != null ? pluginPmCcExpirationYear.hashCode() : 0);
        result = 31 * result + (pluginPmCcLast4 != null ? pluginPmCcLast4.hashCode() : 0);
        result = 31 * result + (pluginPmAddress1 != null ? pluginPmAddress1.hashCode() : 0);
        result = 31 * result + (pluginPmAddress2 != null ? pluginPmAddress2.hashCode() : 0);
        result = 31 * result + (pluginPmCity != null ? pluginPmCity.hashCode() : 0);
        result = 31 * result + (pluginPmState != null ? pluginPmState.hashCode() : 0);
        result = 31 * result + (pluginPmZip != null ? pluginPmZip.hashCode() : 0);
        result = 31 * result + (pluginPmCountry != null ? pluginPmCountry.hashCode() : 0);
        result = 31 * result + (convertedCurrency != null ? convertedCurrency.hashCode() : 0);
        return result;
    }
}
