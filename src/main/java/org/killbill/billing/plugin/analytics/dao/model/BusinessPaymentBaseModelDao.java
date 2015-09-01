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

package org.killbill.billing.plugin.analytics.dao.model;

import java.math.BigDecimal;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentMethod;
import org.killbill.billing.payment.api.PaymentTransaction;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.plugin.analytics.utils.PaymentUtils;
import org.killbill.billing.util.audit.AuditLog;

import com.google.common.annotations.VisibleForTesting;

public abstract class BusinessPaymentBaseModelDao extends BusinessModelDaoBase {

    @VisibleForTesting
    static final String DEFAULT_PLUGIN_NAME = "__UNKNOWN__";

    protected static final String AUTHS_TABLE_NAME = "analytics_payment_auths";
    protected static final String CAPTURES_TABLE_NAME = "analytics_payment_captures";
    protected static final String PURCHASES_TABLE_NAME = "analytics_payment_purchases";
    protected static final String REFUNDS_TABLE_NAME = "analytics_payment_refunds";
    protected static final String CREDITS_TABLE_NAME = "analytics_payment_credits";
    protected static final String CHARGEBACKS_TABLE_NAME = "analytics_payment_chargebacks";
    protected static final String VOIDS_TABLE_NAME = "analytics_payment_voids";

    public static final String[] ALL_PAYMENTS_TABLE_NAMES = new String[]{AUTHS_TABLE_NAME, CAPTURES_TABLE_NAME, PURCHASES_TABLE_NAME, REFUNDS_TABLE_NAME, CREDITS_TABLE_NAME, CHARGEBACKS_TABLE_NAME, VOIDS_TABLE_NAME};

    private Long invoicePaymentRecordId;
    private UUID invoicePaymentId;
    private UUID invoiceId;
    private Integer invoiceNumber;
    private DateTime invoiceCreatedDate;
    private LocalDate invoiceDate;
    private LocalDate invoiceTargetDate;
    private String invoiceCurrency;
    private BigDecimal invoiceBalance;
    private BigDecimal convertedInvoiceBalance;
    private BigDecimal invoiceAmountPaid;
    private BigDecimal convertedInvoiceAmountPaid;
    private BigDecimal invoiceAmountCharged;
    private BigDecimal convertedInvoiceAmountCharged;
    private BigDecimal invoiceOriginalAmountCharged;
    private BigDecimal convertedInvoiceOriginalAmountCharged;
    private BigDecimal invoiceAmountCredited;
    private BigDecimal convertedInvoiceAmountCredited;
    private BigDecimal invoiceAmountRefunded;
    private BigDecimal convertedInvoiceAmountRefunded;
    private String invoicePaymentType;
    private UUID paymentId;
    private UUID refundId;
    private Long paymentNumber;
    private String paymentExternalKey;
    private UUID paymentTransactionId;
    private String paymentTransactionExternalKey;
    private String paymentTransactionStatus;
    private UUID linkedInvoicePaymentId;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
    private String currency;
    private String pluginName;
    private DateTime pluginCreatedDate;
    private DateTime pluginEffectiveDate;
    private String pluginStatus;
    private String pluginGatewayError;
    private String pluginGatewayErrorCode;
    private String pluginFirstReferenceId;
    private String pluginSecondReferenceId;
    private String pluginPmId;
    private Boolean pluginPmIsDefault;
    private String pluginPmType;
    private String pluginPmCcName;
    private String pluginPmCcType;
    private String pluginPmCcExpirationMonth;
    private String pluginPmCcExpirationYear;
    private String pluginPmCcLast4;
    private String pluginPmAddress1;
    private String pluginPmAddress2;
    private String pluginPmCity;
    private String pluginPmState;
    private String pluginPmZip;
    private String pluginPmCountry;
    private String convertedCurrency;

    public static BusinessPaymentBaseModelDao create(final Account account,
                                                     final Long accountRecordId,
                                                     @Nullable final Invoice invoice,
                                                     @Nullable final InvoicePayment invoicePayment,
                                                     @Nullable final Long invoicePaymentRecordId,
                                                     final Payment payment,
                                                     final PaymentTransaction paymentTransaction,
                                                     @Nullable final PaymentMethod paymentMethod,
                                                     final CurrencyConverter currencyConverter,
                                                     @Nullable final AuditLog creationAuditLog,
                                                     final Long tenantRecordId,
                                                     @Nullable final ReportGroup reportGroup) {
        if (paymentTransaction.getTransactionType().equals(TransactionType.AUTHORIZE)) {
            return new BusinessPaymentAuthModelDao(account,
                                                   accountRecordId,
                                                   invoice,
                                                   invoicePayment,
                                                   invoicePaymentRecordId,
                                                   payment,
                                                   paymentTransaction,
                                                   paymentMethod,
                                                   currencyConverter,
                                                   creationAuditLog,
                                                   tenantRecordId,
                                                   reportGroup);
        } else if (paymentTransaction.getTransactionType().equals(TransactionType.CAPTURE)) {
            return new BusinessPaymentCaptureModelDao(account,
                                                      accountRecordId,
                                                      invoice,
                                                      invoicePayment,
                                                      invoicePaymentRecordId,
                                                      payment,
                                                      paymentTransaction,
                                                      paymentMethod,
                                                      currencyConverter,
                                                      creationAuditLog,
                                                      tenantRecordId,
                                                      reportGroup);
        } else if (paymentTransaction.getTransactionType().equals(TransactionType.CHARGEBACK)) {
            return new BusinessPaymentChargebackModelDao(account,
                                                         accountRecordId,
                                                         invoice,
                                                         invoicePayment,
                                                         invoicePaymentRecordId,
                                                         payment,
                                                         paymentTransaction,
                                                         paymentMethod,
                                                         currencyConverter,
                                                         creationAuditLog,
                                                         tenantRecordId,
                                                         reportGroup);
        } else if (paymentTransaction.getTransactionType().equals(TransactionType.CREDIT)) {
            return new BusinessPaymentCreditModelDao(account,
                                                     accountRecordId,
                                                     invoice,
                                                     invoicePayment,
                                                     invoicePaymentRecordId,
                                                     payment,
                                                     paymentTransaction,
                                                     paymentMethod,
                                                     currencyConverter,
                                                     creationAuditLog,
                                                     tenantRecordId,
                                                     reportGroup);
        } else if (paymentTransaction.getTransactionType().equals(TransactionType.PURCHASE)) {
            return new BusinessPaymentPurchaseModelDao(account,
                                                       accountRecordId,
                                                       invoice,
                                                       invoicePayment,
                                                       invoicePaymentRecordId,
                                                       payment,
                                                       paymentTransaction,
                                                       paymentMethod,
                                                       currencyConverter,
                                                       creationAuditLog,
                                                       tenantRecordId,
                                                       reportGroup);
        } else if (paymentTransaction.getTransactionType().equals(TransactionType.REFUND)) {
            return new BusinessPaymentRefundModelDao(account,
                                                     accountRecordId,
                                                     invoice,
                                                     invoicePayment,
                                                     invoicePaymentRecordId,
                                                     payment,
                                                     paymentTransaction,
                                                     paymentMethod,
                                                     currencyConverter,
                                                     creationAuditLog,
                                                     tenantRecordId,
                                                     reportGroup);
        } else if (paymentTransaction.getTransactionType().equals(TransactionType.VOID)) {
            return new BusinessPaymentVoidModelDao(account,
                                                   accountRecordId,
                                                   invoice,
                                                   invoicePayment,
                                                   invoicePaymentRecordId,
                                                   payment,
                                                   paymentTransaction,
                                                   paymentMethod,
                                                   currencyConverter,
                                                   creationAuditLog,
                                                   tenantRecordId,
                                                   reportGroup);
        } else {
            throw new IllegalStateException("Unexpected transaction type: " + paymentTransaction.getTransactionType());
        }
    }

    public BusinessPaymentBaseModelDao() { /* When reading from the database */ }

    public BusinessPaymentBaseModelDao(final Long invoicePaymentRecordId,
                                       final UUID invoicePaymentId,
                                       final UUID invoiceId,
                                       final Integer invoiceNumber,
                                       final DateTime invoiceCreatedDate,
                                       final LocalDate invoiceDate,
                                       final LocalDate invoiceTargetDate,
                                       final String invoiceCurrency,
                                       final String invoicePaymentType,
                                       final UUID paymentId,
                                       final UUID refundId,
                                       final Long paymentNumber,
                                       final String paymentExternalKey,
                                       final UUID paymentTransactionId,
                                       final String paymentTransactionExternalKey,
                                       final String paymentTransactionStatus,
                                       final UUID linkedInvoicePaymentId,
                                       final BigDecimal amount,
                                       final BigDecimal convertedAmount,
                                       final String currency,
                                       final BigDecimal invoiceBalance,
                                       final BigDecimal convertedInvoiceBalance,
                                       final BigDecimal invoiceAmountPaid,
                                       final BigDecimal convertedInvoiceAmountPaid,
                                       final BigDecimal invoiceAmountCharged,
                                       final BigDecimal convertedInvoiceAmountCharged,
                                       final BigDecimal invoiceOriginalAmountCharged,
                                       final BigDecimal convertedInvoiceOriginalAmountCharged,
                                       final BigDecimal invoiceAmountCredited,
                                       final BigDecimal convertedInvoiceAmountCredited,
                                       final BigDecimal invoiceAmountRefunded,
                                       final BigDecimal convertedInvoiceAmountRefunded,
                                       final String pluginName,
                                       final DateTime pluginCreatedDate,
                                       final DateTime pluginEffectiveDate,
                                       final String pluginStatus,
                                       final String pluginGatewayError,
                                       final String pluginGatewayErrorCode,
                                       final String pluginFirstReferenceId,
                                       final String pluginSecondReferenceId,
                                       final String pluginPmId,
                                       final Boolean pluginPmIsDefault,
                                       final String pluginPmType,
                                       final String pluginPmCcName,
                                       final String pluginPmCcType,
                                       final String pluginPmCcExpirationMonth,
                                       final String pluginPmCcExpirationYear,
                                       final String pluginPmCcLast4,
                                       final String pluginPmAddress1,
                                       final String pluginPmAddress2,
                                       final String pluginPmCity,
                                       final String pluginPmState,
                                       final String pluginPmZip,
                                       final String pluginPmCountry,
                                       final String convertedCurrency,
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
        this.invoicePaymentRecordId = invoicePaymentRecordId;
        this.invoicePaymentId = invoicePaymentId;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.invoiceCreatedDate = invoiceCreatedDate;
        this.invoiceDate = invoiceDate;
        this.invoiceTargetDate = invoiceTargetDate;
        this.invoiceCurrency = invoiceCurrency;
        this.invoiceBalance = invoiceBalance;
        this.convertedInvoiceBalance = convertedInvoiceBalance;
        this.invoiceAmountPaid = invoiceAmountPaid;
        this.convertedInvoiceAmountPaid = convertedInvoiceAmountPaid;
        this.invoiceAmountCharged = invoiceAmountCharged;
        this.convertedInvoiceAmountCharged = convertedInvoiceAmountCharged;
        this.invoiceOriginalAmountCharged = invoiceOriginalAmountCharged;
        this.convertedInvoiceOriginalAmountCharged = convertedInvoiceOriginalAmountCharged;
        this.invoiceAmountCredited = invoiceAmountCredited;
        this.convertedInvoiceAmountCredited = convertedInvoiceAmountCredited;
        this.invoiceAmountRefunded = invoiceAmountRefunded;
        this.convertedInvoiceAmountRefunded = convertedInvoiceAmountRefunded;
        this.invoicePaymentType = invoicePaymentType;
        this.paymentId = paymentId;
        this.refundId = refundId;
        this.paymentNumber = paymentNumber;
        this.paymentExternalKey = paymentExternalKey;
        this.paymentTransactionId = paymentTransactionId;
        this.paymentTransactionExternalKey = paymentTransactionExternalKey;
        this.paymentTransactionStatus = paymentTransactionStatus;
        this.linkedInvoicePaymentId = linkedInvoicePaymentId;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
        this.currency = currency;
        this.pluginName = pluginName;
        this.pluginCreatedDate = pluginCreatedDate;
        this.pluginEffectiveDate = pluginEffectiveDate;
        this.pluginStatus = pluginStatus;
        this.pluginGatewayError = pluginGatewayError;
        this.pluginGatewayErrorCode = pluginGatewayErrorCode;
        this.pluginFirstReferenceId = pluginFirstReferenceId;
        this.pluginSecondReferenceId = pluginSecondReferenceId;
        this.pluginPmId = pluginPmId;
        this.pluginPmIsDefault = pluginPmIsDefault;
        this.pluginPmType = pluginPmType;
        this.pluginPmCcName = pluginPmCcName;
        this.pluginPmCcType = pluginPmCcType;
        this.pluginPmCcExpirationMonth = pluginPmCcExpirationMonth;
        this.pluginPmCcExpirationYear = pluginPmCcExpirationYear;
        this.pluginPmCcLast4 = pluginPmCcLast4;
        this.pluginPmAddress1 = pluginPmAddress1;
        this.pluginPmAddress2 = pluginPmAddress2;
        this.pluginPmCity = pluginPmCity;
        this.pluginPmState = pluginPmState;
        this.pluginPmZip = pluginPmZip;
        this.pluginPmCountry = pluginPmCountry;
        this.convertedCurrency = convertedCurrency;
    }

    protected BusinessPaymentBaseModelDao(final Account account,
                                          final Long accountRecordId,
                                          @Nullable final Invoice invoice,
                                          @Nullable final InvoicePayment invoicePayment,
                                          @Nullable final Long invoicePaymentRecordId,
                                          final Payment payment,
                                          final PaymentTransaction paymentTransaction,
                                          @Nullable final PaymentMethod paymentMethod,
                                          final CurrencyConverter currencyConverter,
                                          @Nullable final AuditLog creationAuditLog,
                                          final Long tenantRecordId,
                                          @Nullable final ReportGroup reportGroup) {
        this(account,
             accountRecordId,
             invoice,
             invoicePayment,
             invoicePaymentRecordId,
             payment,
             paymentTransaction,
             PaymentUtils.findLastPaymentTransaction(payment, TransactionType.CAPTURE, TransactionType.PURCHASE),
             PaymentUtils.findLastPaymentTransaction(payment, TransactionType.REFUND),
             paymentMethod,
             currencyConverter,
             creationAuditLog,
             tenantRecordId,
             reportGroup);
    }

    private BusinessPaymentBaseModelDao(final Account account,
                                        final Long accountRecordId,
                                        @Nullable final Invoice invoice,
                                        @Nullable final InvoicePayment invoicePayment,
                                        @Nullable final Long invoicePaymentRecordId,
                                        final Payment payment,
                                        final PaymentTransaction paymentTransaction,
                                        @Nullable final PaymentTransaction lastCaptureOrPurchase,
                                        @Nullable final PaymentTransaction lastRefund,
                                        @Nullable final PaymentMethod paymentMethod,
                                        final CurrencyConverter currencyConverter,
                                        @Nullable final AuditLog creationAuditLog,
                                        final Long tenantRecordId,
                                        @Nullable final ReportGroup reportGroup) {
        this(invoicePaymentRecordId,
             invoicePayment == null ? null : invoicePayment.getId(),
             invoice == null ? null : invoice.getId(),
             invoice == null ? null : invoice.getInvoiceNumber(),
             invoice == null ? null : invoice.getCreatedDate(),
             invoice == null ? null : invoice.getInvoiceDate(),
             invoice == null ? null : invoice.getTargetDate(),
             invoice == null || invoice.getCurrency() == null ? null : invoice.getCurrency().toString(),
             invoicePayment == null || invoicePayment.getType() == null ? null : invoicePayment.getType().toString(),
             payment.getId(),
             lastRefund != null ? lastRefund.getId() : null,
             payment.getPaymentNumber() == null ? null : payment.getPaymentNumber().longValue(),
             payment.getExternalKey(),
             paymentTransaction.getId(),
             paymentTransaction.getExternalKey(),
             paymentTransaction.getTransactionStatus() == null ? null : paymentTransaction.getTransactionStatus().name(),
             invoicePayment == null ? null : invoicePayment.getLinkedInvoicePaymentId(),
             invoicePayment == null ? paymentTransaction.getAmount() : invoicePayment.getAmount(),
             currencyConverter.getConvertedValue(invoicePayment, paymentTransaction, invoice),
             invoicePayment == null || invoicePayment.getCurrency() == null ? ( paymentTransaction == null || paymentTransaction.getCurrency() == null ? null : paymentTransaction.getCurrency().toString() ) : invoicePayment.getCurrency().toString(),
             invoice == null ? null : invoice.getBalance(),
             invoice == null ? null : currencyConverter.getConvertedValue(invoice.getBalance(), invoice),
             invoice == null ? null : invoice.getPaidAmount(),
             invoice == null ? null : currencyConverter.getConvertedValue(invoice.getPaidAmount(), invoice),
             invoice == null ? null : invoice.getChargedAmount(),
             invoice == null ? null : currencyConverter.getConvertedValue(invoice.getChargedAmount(), invoice),
             invoice == null ? null : invoice.getOriginalChargedAmount(),
             invoice == null ? null : currencyConverter.getConvertedValue(invoice.getOriginalChargedAmount(), invoice),
             invoice == null ? null : invoice.getCreditedAmount(),
             invoice == null ? null : currencyConverter.getConvertedValue(invoice.getCreditedAmount(), invoice),
             invoice == null ? null : invoice.getRefundedAmount(),
             invoice == null ? null : currencyConverter.getConvertedValue(invoice.getRefundedAmount(), invoice),
             paymentMethod != null ? paymentMethod.getPluginName() : DEFAULT_PLUGIN_NAME,
             lastRefund != null ? (lastRefund.getPaymentInfoPlugin() != null ? lastRefund.getPaymentInfoPlugin().getCreatedDate() : null) : (lastCaptureOrPurchase != null && lastCaptureOrPurchase.getPaymentInfoPlugin() != null ? lastCaptureOrPurchase.getPaymentInfoPlugin().getCreatedDate() : null),
             lastRefund != null ? (lastRefund.getPaymentInfoPlugin() != null ? lastRefund.getPaymentInfoPlugin().getEffectiveDate() : null) : (lastCaptureOrPurchase != null && lastCaptureOrPurchase.getPaymentInfoPlugin() != null ? lastCaptureOrPurchase.getPaymentInfoPlugin().getEffectiveDate() : null),
             lastRefund != null ? (lastRefund.getPaymentInfoPlugin() != null && lastRefund.getPaymentInfoPlugin().getStatus() != null ? lastRefund.getPaymentInfoPlugin().getStatus().toString() : null) : (lastCaptureOrPurchase != null && lastCaptureOrPurchase.getPaymentInfoPlugin() != null && lastCaptureOrPurchase.getPaymentInfoPlugin().getStatus() != null ? lastCaptureOrPurchase.getPaymentInfoPlugin().getStatus().toString() : null),
             lastRefund != null ? (lastRefund.getPaymentInfoPlugin() != null ? lastRefund.getPaymentInfoPlugin().getGatewayError() : null) : (lastCaptureOrPurchase != null && lastCaptureOrPurchase.getPaymentInfoPlugin() != null ? lastCaptureOrPurchase.getPaymentInfoPlugin().getGatewayError() : null),
             lastRefund != null ? (lastRefund.getPaymentInfoPlugin() != null ? lastRefund.getPaymentInfoPlugin().getGatewayErrorCode() : null) : (lastCaptureOrPurchase != null && lastCaptureOrPurchase.getPaymentInfoPlugin() != null ? lastCaptureOrPurchase.getPaymentInfoPlugin().getGatewayErrorCode() : null),
             lastRefund != null ? (lastRefund.getPaymentInfoPlugin() != null ? lastRefund.getPaymentInfoPlugin().getFirstPaymentReferenceId() : null) : (lastCaptureOrPurchase != null && lastCaptureOrPurchase.getPaymentInfoPlugin() != null ? lastCaptureOrPurchase.getPaymentInfoPlugin().getFirstPaymentReferenceId() : null),
             lastRefund != null ? (lastRefund.getPaymentInfoPlugin() != null ? lastRefund.getPaymentInfoPlugin().getSecondPaymentReferenceId() : null) : (lastCaptureOrPurchase != null && lastCaptureOrPurchase.getPaymentInfoPlugin() != null ? lastCaptureOrPurchase.getPaymentInfoPlugin().getSecondPaymentReferenceId() : null),
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? paymentMethod.getPluginDetail().getExternalPaymentMethodId() : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? paymentMethod.getPluginDetail().isDefaultPaymentMethod() : null) : null,
             // Magic from Killbill::Plugin::ActiveMerchant::PaymentPlugin
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "type") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "ccLastName") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "ccType") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "ccExpirationMonth") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "ccExpirationYear") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "ccVerificationValue") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "address1") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "address2") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "city") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "state") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "zip") : null) : null,
             paymentMethod != null ? (paymentMethod.getPluginDetail() != null ? PaymentUtils.getPropertyValue(paymentMethod.getPluginDetail().getProperties(), "country") : null) : null,
             currencyConverter.getConvertedCurrency(),
             invoicePayment == null ? paymentTransaction.getCreatedDate() : invoicePayment.getCreatedDate(),
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

    public Long getInvoicePaymentRecordId() {
        return invoicePaymentRecordId;
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
        final StringBuilder sb = new StringBuilder("BusinessInvoicePaymentBaseModelDao{");
        sb.append("invoicePaymentRecordId=").append(invoicePaymentRecordId);
        sb.append(", invoicePaymentId=").append(invoicePaymentId);
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
        sb.append(", pluginCreatedDate=").append(pluginCreatedDate);
        sb.append(", pluginEffectiveDate=").append(pluginEffectiveDate);
        sb.append(", pluginStatus='").append(pluginStatus).append('\'');
        sb.append(", pluginGatewayError='").append(pluginGatewayError).append('\'');
        sb.append(", pluginGatewayErrorCode='").append(pluginGatewayErrorCode).append('\'');
        sb.append(", pluginFirstReferenceId='").append(pluginFirstReferenceId).append('\'');
        sb.append(", pluginSecondReferenceId='").append(pluginSecondReferenceId).append('\'');
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

        final BusinessPaymentBaseModelDao that = (BusinessPaymentBaseModelDao) o;

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
        if (invoicePaymentRecordId != null ? !invoicePaymentRecordId.equals(that.invoicePaymentRecordId) : that.invoicePaymentRecordId != null) {
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
        if (pluginGatewayError != null ? !pluginGatewayError.equals(that.pluginGatewayError) : that.pluginGatewayError != null) {
            return false;
        }
        if (pluginGatewayErrorCode != null ? !pluginGatewayErrorCode.equals(that.pluginGatewayErrorCode) : that.pluginGatewayErrorCode != null) {
            return false;
        }
        if (pluginName != null ? !pluginName.equals(that.pluginName) : that.pluginName != null) {
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
        if (pluginSecondReferenceId != null ? !pluginSecondReferenceId.equals(that.pluginSecondReferenceId) : that.pluginSecondReferenceId != null) {
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
        result = 31 * result + (invoicePaymentRecordId != null ? invoicePaymentRecordId.hashCode() : 0);
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
        result = 31 * result + (pluginCreatedDate != null ? pluginCreatedDate.hashCode() : 0);
        result = 31 * result + (pluginEffectiveDate != null ? pluginEffectiveDate.hashCode() : 0);
        result = 31 * result + (pluginStatus != null ? pluginStatus.hashCode() : 0);
        result = 31 * result + (pluginGatewayError != null ? pluginGatewayError.hashCode() : 0);
        result = 31 * result + (pluginGatewayErrorCode != null ? pluginGatewayErrorCode.hashCode() : 0);
        result = 31 * result + (pluginFirstReferenceId != null ? pluginFirstReferenceId.hashCode() : 0);
        result = 31 * result + (pluginSecondReferenceId != null ? pluginSecondReferenceId.hashCode() : 0);
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
