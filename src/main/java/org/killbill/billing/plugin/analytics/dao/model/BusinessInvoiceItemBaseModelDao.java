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

package org.killbill.billing.plugin.analytics.dao.model;

import java.math.BigDecimal;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.PlanPhase;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.plugin.analytics.utils.BusinessInvoiceItemUtils;
import org.killbill.billing.plugin.analytics.utils.BusinessInvoiceUtils;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AuditLog;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class BusinessInvoiceItemBaseModelDao extends BusinessModelDaoBase {

    protected static final String INVOICE_ADJUSTMENTS_TABLE_NAME = "analytics_invoice_adjustments";
    protected static final String INVOICE_ITEMS_TABLE_NAME = "analytics_invoice_items";
    protected static final String INVOICE_ITEM_ADJUSTMENTS_TABLE_NAME = "analytics_invoice_item_adjustments";
    protected static final String ACCOUNT_CREDITS_TABLE_NAME = "analytics_invoice_credits";

    @SuppressFBWarnings("MS_MUTABLE_ARRAY")
    public static final String[] ALL_INVOICE_ITEMS_TABLE_NAMES = new String[]{INVOICE_ADJUSTMENTS_TABLE_NAME, INVOICE_ITEMS_TABLE_NAME, INVOICE_ITEM_ADJUSTMENTS_TABLE_NAME, ACCOUNT_CREDITS_TABLE_NAME};

    private Long invoiceItemRecordId;
    private Long secondInvoiceItemRecordId;
    private UUID itemId;
    private UUID invoiceId;
    private Integer invoiceNumber;
    private DateTime invoiceCreatedDate;
    private LocalDate invoiceDate;
    private LocalDate invoiceTargetDate;
    private String invoiceCurrency;
    private BigDecimal rawInvoiceBalance;
    private BigDecimal convertedRawInvoiceBalance;
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
    private boolean invoiceWrittenOff;
    private String itemType;
    private String itemSource;
    private UUID bundleId;
    private String bundleExternalKey;
    private String productName;
    private String productType;
    private String productCategory;
    private String slug;
    private String usageName;
    private String phase;
    private String billingPeriod;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
    private String currency;
    private UUID linkedItemId;
    private String convertedCurrency;

    public enum BusinessInvoiceItemType {
        INVOICE_ADJUSTMENT,
        INVOICE_ITEM_ADJUSTMENT,
        ACCOUNT_CREDIT,
        CHARGE
    }

    // See ddl.sql
    @VisibleForTesting
    public static final String DEFAULT_ITEM_SOURCE = "system";

    // See ddl.sql
    public enum ItemSource {
        user
    }

    public abstract BusinessInvoiceItemType getBusinessInvoiceItemType();

    public static BusinessInvoiceItemBaseModelDao create(final Account account,
                                                         final Long accountRecordId,
                                                         final Invoice invoice,
                                                         final InvoiceItem invoiceItem,
                                                         @Nullable final ItemSource itemSource,
                                                         final boolean invoiceWrittenOff,
                                                         final BusinessInvoiceItemType businessInvoiceItemType,
                                                         final Long invoiceItemRecordId,
                                                         final Long secondInvoiceItemRecordId,
                                                         @Nullable final SubscriptionBundle bundle,
                                                         @Nullable final Plan plan,
                                                         @Nullable final PlanPhase planPhase,
                                                         final CurrencyConverter currencyConverter,
                                                         @Nullable final AuditLog creationAuditLog,
                                                         final Long tenantRecordId,
                                                         @Nullable final ReportGroup reportGroup) {
        if (BusinessInvoiceItemType.INVOICE_ADJUSTMENT.equals(businessInvoiceItemType)) {
            return new BusinessInvoiceAdjustmentModelDao(account,
                                                         accountRecordId,
                                                         invoice,
                                                         invoiceItem,
                                                         itemSource,
                                                         invoiceWrittenOff,
                                                         invoiceItemRecordId,
                                                         secondInvoiceItemRecordId,
                                                         bundle,
                                                         plan,
                                                         planPhase,
                                                         currencyConverter,
                                                         creationAuditLog,
                                                         tenantRecordId,
                                                         reportGroup);
        } else if (BusinessInvoiceItemType.CHARGE.equals(businessInvoiceItemType)) {
            return new BusinessInvoiceItemModelDao(account,
                                                   accountRecordId,
                                                   invoice,
                                                   invoiceItem,
                                                   itemSource,
                                                   invoiceWrittenOff,
                                                   invoiceItemRecordId,
                                                   secondInvoiceItemRecordId,
                                                   bundle,
                                                   plan,
                                                   planPhase,
                                                   currencyConverter,
                                                   creationAuditLog,
                                                   tenantRecordId,
                                                   reportGroup);
        } else if (BusinessInvoiceItemType.INVOICE_ITEM_ADJUSTMENT.equals(businessInvoiceItemType)) {
            return new BusinessInvoiceItemAdjustmentModelDao(account,
                                                             accountRecordId,
                                                             invoice,
                                                             invoiceItem,
                                                             itemSource,
                                                             invoiceWrittenOff,
                                                             invoiceItemRecordId,
                                                             secondInvoiceItemRecordId,
                                                             bundle,
                                                             plan,
                                                             planPhase,
                                                             currencyConverter,
                                                             creationAuditLog,
                                                             tenantRecordId,
                                                             reportGroup);
        } else if (BusinessInvoiceItemType.ACCOUNT_CREDIT.equals(businessInvoiceItemType)) {
            return new BusinessInvoiceItemCreditModelDao(account,
                                                         accountRecordId,
                                                         invoice,
                                                         invoiceItem,
                                                         itemSource,
                                                         invoiceWrittenOff,
                                                         invoiceItemRecordId,
                                                         secondInvoiceItemRecordId,
                                                         bundle,
                                                         plan,
                                                         planPhase,
                                                         currencyConverter,
                                                         creationAuditLog,
                                                         tenantRecordId,
                                                         reportGroup);
        } else {
            // We don't care
            return null;
        }
    }

    public BusinessInvoiceItemBaseModelDao() { /* When reading from the database */ }

    public BusinessInvoiceItemBaseModelDao(final Long invoiceItemRecordId,
                                           final Long secondInvoiceItemRecordId,
                                           final UUID itemId,
                                           final UUID invoiceId,
                                           final Integer invoiceNumber,
                                           final DateTime invoiceCreatedDate,
                                           final LocalDate invoiceDate,
                                           final LocalDate invoiceTargetDate,
                                           final String invoiceCurrency,
                                           final BigDecimal rawInvoiceBalance,
                                           final BigDecimal convertedRawInvoiceBalance,
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
                                           final boolean invoiceWrittenOff,
                                           final String itemType,
                                           @Nullable final ItemSource itemSource,
                                           final UUID bundleId,
                                           final String bundleExternalKey,
                                           final String productName,
                                           final String productType,
                                           final String productCategory,
                                           final String slug,
                                           final String usageName,
                                           final String phase,
                                           final String billingPeriod,
                                           final LocalDate startDate,
                                           final LocalDate endDate,
                                           final BigDecimal amount,
                                           final BigDecimal convertedAmount,
                                           final String currency,
                                           final UUID linkedItemId,
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
        this.invoiceItemRecordId = invoiceItemRecordId;
        this.secondInvoiceItemRecordId = secondInvoiceItemRecordId;
        this.itemId = itemId;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.invoiceCreatedDate = invoiceCreatedDate;
        this.invoiceDate = invoiceDate;
        this.invoiceTargetDate = invoiceTargetDate;
        this.invoiceCurrency = invoiceCurrency;
        this.rawInvoiceBalance = rawInvoiceBalance;
        this.convertedRawInvoiceBalance = convertedRawInvoiceBalance;
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
        this.invoiceWrittenOff = invoiceWrittenOff;
        this.itemType = itemType;
        this.itemSource = itemSource == null ? DEFAULT_ITEM_SOURCE : itemSource.toString();
        this.bundleId = bundleId;
        this.bundleExternalKey = bundleExternalKey;
        this.productName = productName;
        this.productType = productType;
        this.productCategory = productCategory;
        this.slug = slug;
        this.usageName = usageName;
        this.phase = phase;
        this.billingPeriod = billingPeriod;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
        this.currency = currency;
        this.linkedItemId = linkedItemId;
        this.convertedCurrency = convertedCurrency;
    }

    public BusinessInvoiceItemBaseModelDao(final Account account,
                                           final Long accountRecordId,
                                           final Invoice invoice,
                                           final InvoiceItem invoiceItem,
                                           @Nullable final ItemSource itemSource,
                                           final boolean invoiceWrittenOff,
                                           final Long invoiceItemRecordId,
                                           final Long secondInvoiceItemRecordId,
                                           @Nullable final SubscriptionBundle bundle,
                                           @Nullable final Plan plan,
                                           @Nullable final PlanPhase planPhase,
                                           final CurrencyConverter currencyConverter,
                                           @Nullable final AuditLog creationAuditLog,
                                           final Long tenantRecordId,
                                           @Nullable final ReportGroup reportGroup) {
        this(invoiceItemRecordId,
             secondInvoiceItemRecordId,
             invoiceItem.getId(),
             invoice.getId(),
             invoice.getInvoiceNumber(),
             invoice.getCreatedDate(),
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
             invoiceWrittenOff,
             invoiceItem.getInvoiceItemType().toString(),
             itemSource,
             bundle == null ? null : bundle.getId(),
             bundle == null ? null : bundle.getExternalKey(),
             (plan != null && plan.getProduct() != null) ? plan.getProduct().getName() : null,
             (plan != null && plan.getProduct() != null) ? plan.getProduct().getCatalogName() : null,
             (plan != null && plan.getProduct().getCategory() != null) ? plan.getProduct().getCategory().toString() : null,
             planPhase != null ? planPhase.getName() : null,
             invoiceItem.getUsageName(),
             (planPhase != null && planPhase.getPhaseType() != null) ? planPhase.getPhaseType().toString() : null,
             (planPhase != null && planPhase.getRecurring() != null && planPhase.getRecurring().getBillingPeriod() != null) ? planPhase.getRecurring().getBillingPeriod().toString() : null,
             invoiceItem.getStartDate(),
             /* Populate end date for fixed items for convenience (null in invoice_items table) */
             BusinessInvoiceItemUtils.computeServicePeriodEndDate(invoiceItem, planPhase, bundle),
             invoiceItem.getAmount(),
             currencyConverter.getConvertedValue(invoiceItem, invoice),
             invoiceItem.getCurrency() == null ? null : invoiceItem.getCurrency().toString(),
             invoiceItem.getLinkedItemId(),
             currencyConverter.getConvertedCurrency(),
             invoiceItem.getCreatedDate(),
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

    public Long getInvoiceItemRecordId() {
        return invoiceItemRecordId;
    }

    public Long getSecondInvoiceItemRecordId() {
        return secondInvoiceItemRecordId;
    }

    public UUID getItemId() {
        return itemId;
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

    public BigDecimal getRawInvoiceBalance() {
        return rawInvoiceBalance;
    }

    public BigDecimal getConvertedRawInvoiceBalance() {
        return convertedRawInvoiceBalance;
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

    public boolean isInvoiceWrittenOff() {
        return invoiceWrittenOff;
    }

    public String getItemType() {
        return itemType;
    }

    public String getItemSource() {
        return itemSource;
    }

    public UUID getBundleId() {
        return bundleId;
    }

    public String getBundleExternalKey() {
        return bundleExternalKey;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductType() {
        return productType;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public String getSlug() {
        return slug;
    }

    public String getUsageName() {
        return usageName;
    }

    public String getPhase() {
        return phase;
    }

    public String getBillingPeriod() {
        return billingPeriod;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
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

    public UUID getLinkedItemId() {
        return linkedItemId;
    }

    public String getConvertedCurrency() {
        return convertedCurrency;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessInvoiceItemBaseModelDao{");
        sb.append("invoiceItemRecordId=").append(invoiceItemRecordId);
        sb.append(", secondInvoiceItemRecordId=").append(secondInvoiceItemRecordId);
        sb.append(", itemId=").append(itemId);
        sb.append(", invoiceId=").append(invoiceId);
        sb.append(", invoiceNumber=").append(invoiceNumber);
        sb.append(", invoiceCreatedDate=").append(invoiceCreatedDate);
        sb.append(", invoiceDate=").append(invoiceDate);
        sb.append(", invoiceTargetDate=").append(invoiceTargetDate);
        sb.append(", invoiceCurrency='").append(invoiceCurrency).append('\'');
        sb.append(", rawInvoiceBalance=").append(rawInvoiceBalance);
        sb.append(", convertedRawInvoiceBalance=").append(convertedRawInvoiceBalance);
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
        sb.append(", invoiceWrittenOff='").append(invoiceWrittenOff).append('\'');
        sb.append(", itemType='").append(itemType).append('\'');
        sb.append(", itemSource='").append(itemSource).append('\'');
        sb.append(", bundleId=").append(bundleId);
        sb.append(", bundleExternalKey='").append(bundleExternalKey).append('\'');
        sb.append(", productName='").append(productName).append('\'');
        sb.append(", productType='").append(productType).append('\'');
        sb.append(", productCategory='").append(productCategory).append('\'');
        sb.append(", slug='").append(slug).append('\'');
        sb.append(", usageName='").append(usageName).append('\'');
        sb.append(", phase='").append(phase).append('\'');
        sb.append(", billingPeriod='").append(billingPeriod).append('\'');
        sb.append(", startDate=").append(startDate);
        sb.append(", endDate=").append(endDate);
        sb.append(", amount=").append(amount);
        sb.append(", convertedAmount=").append(convertedAmount);
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", linkedItemId=").append(linkedItemId);
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

        final BusinessInvoiceItemBaseModelDao that = (BusinessInvoiceItemBaseModelDao) o;

        if (amount != null ? !(amount.compareTo(that.amount) == 0) : that.amount != null) {
            return false;
        }
        if (billingPeriod != null ? !billingPeriod.equals(that.billingPeriod) : that.billingPeriod != null) {
            return false;
        }
        if (bundleExternalKey != null ? !bundleExternalKey.equals(that.bundleExternalKey) : that.bundleExternalKey != null) {
            return false;
        }
        if (bundleId != null ? !bundleId.equals(that.bundleId) : that.bundleId != null) {
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
        if (convertedRawInvoiceBalance != null ? !(convertedRawInvoiceBalance.compareTo(that.convertedRawInvoiceBalance) == 0) : that.convertedRawInvoiceBalance != null) {
            return false;
        }
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) {
            return false;
        }
        if (endDate != null ? endDate.compareTo(that.endDate) != 0 : that.endDate != null) {
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
        if (invoiceItemRecordId != null ? !invoiceItemRecordId.equals(that.invoiceItemRecordId) : that.invoiceItemRecordId != null) {
            return false;
        }
        if (invoiceNumber != null ? !invoiceNumber.equals(that.invoiceNumber) : that.invoiceNumber != null) {
            return false;
        }
        if (invoiceOriginalAmountCharged != null ? !(invoiceOriginalAmountCharged.compareTo(that.invoiceOriginalAmountCharged) == 0) : that.invoiceOriginalAmountCharged != null) {
            return false;
        }
        if (invoiceTargetDate != null ? invoiceTargetDate.compareTo(that.invoiceTargetDate) != 0 : that.invoiceTargetDate != null) {
            return false;
        }
        if (invoiceWrittenOff != that.invoiceWrittenOff) {
            return false;
        }
        if (itemId != null ? !itemId.equals(that.itemId) : that.itemId != null) {
            return false;
        }
        if (itemSource != null ? !itemSource.equals(that.itemSource) : that.itemSource != null) {
            return false;
        }
        if (itemType != null ? !itemType.equals(that.itemType) : that.itemType != null) {
            return false;
        }
        if (linkedItemId != null ? !linkedItemId.equals(that.linkedItemId) : that.linkedItemId != null) {
            return false;
        }
        if (phase != null ? !phase.equals(that.phase) : that.phase != null) {
            return false;
        }
        if (productCategory != null ? !productCategory.equals(that.productCategory) : that.productCategory != null) {
            return false;
        }
        if (productName != null ? !productName.equals(that.productName) : that.productName != null) {
            return false;
        }
        if (productType != null ? !productType.equals(that.productType) : that.productType != null) {
            return false;
        }
        if (rawInvoiceBalance != null ? !(rawInvoiceBalance.compareTo(that.rawInvoiceBalance) == 0) : that.rawInvoiceBalance != null) {
            return false;
        }
        if (secondInvoiceItemRecordId != null ? !secondInvoiceItemRecordId.equals(that.secondInvoiceItemRecordId) : that.secondInvoiceItemRecordId != null) {
            return false;
        }
        if (slug != null ? !slug.equals(that.slug) : that.slug != null) {
            return false;
        }
        if (startDate != null ? startDate.compareTo(that.startDate) != 0 : that.startDate != null) {
            return false;
        }
        if(usageName != null ? !usageName.equals(that.usageName) : that.usageName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (invoiceItemRecordId != null ? invoiceItemRecordId.hashCode() : 0);
        result = 31 * result + (secondInvoiceItemRecordId != null ? secondInvoiceItemRecordId.hashCode() : 0);
        result = 31 * result + (itemId != null ? itemId.hashCode() : 0);
        result = 31 * result + (invoiceId != null ? invoiceId.hashCode() : 0);
        result = 31 * result + (invoiceNumber != null ? invoiceNumber.hashCode() : 0);
        result = 31 * result + (invoiceCreatedDate != null ? invoiceCreatedDate.hashCode() : 0);
        result = 31 * result + (invoiceDate != null ? invoiceDate.hashCode() : 0);
        result = 31 * result + (invoiceTargetDate != null ? invoiceTargetDate.hashCode() : 0);
        result = 31 * result + (invoiceCurrency != null ? invoiceCurrency.hashCode() : 0);
        result = 31 * result + (rawInvoiceBalance != null ? rawInvoiceBalance.hashCode() : 0);
        result = 31 * result + (convertedRawInvoiceBalance != null ? convertedRawInvoiceBalance.hashCode() : 0);
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
        result = 31 * result + (invoiceWrittenOff ? 1 : 0);
        result = 31 * result + (itemType != null ? itemType.hashCode() : 0);
        result = 31 * result + (itemSource != null ? itemSource.hashCode() : 0);
        result = 31 * result + (bundleId != null ? bundleId.hashCode() : 0);
        result = 31 * result + (bundleExternalKey != null ? bundleExternalKey.hashCode() : 0);
        result = 31 * result + (productName != null ? productName.hashCode() : 0);
        result = 31 * result + (productType != null ? productType.hashCode() : 0);
        result = 31 * result + (productCategory != null ? productCategory.hashCode() : 0);
        result = 31 * result + (slug != null ? slug.hashCode() : 0);
        result = 31 * result + (usageName != null ? usageName.hashCode() : 0);
        result = 31 * result + (phase != null ? phase.hashCode() : 0);
        result = 31 * result + (billingPeriod != null ? billingPeriod.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (convertedAmount != null ? convertedAmount.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (linkedItemId != null ? linkedItemId.hashCode() : 0);
        result = 31 * result + (convertedCurrency != null ? convertedCurrency.hashCode() : 0);
        return result;
    }
}
