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

package org.killbill.billing.plugin.analytics.api;

import java.math.BigDecimal;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;

public class BusinessInvoiceItem extends BusinessEntityBase {

    private final UUID itemId;
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
    private final String itemType;
    private final String itemSource;
    private final UUID bundleId;
    private final String bundleExternalKey;
    private final String productName;
    private final String productType;
    private final String productCategory;
    private final String slug;
    private final String usageName;
    private final String phase;
    private final String billingPeriod;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal amount;
    private final BigDecimal convertedAmount;
    private final String currency;
    private final UUID linkedItemId;
    private final String convertedCurrency;

    public BusinessInvoiceItem(final BusinessInvoiceItemBaseModelDao businessInvoiceItemBaseModelDao) {
        super(businessInvoiceItemBaseModelDao.getCreatedDate(),
              businessInvoiceItemBaseModelDao.getCreatedBy(),
              businessInvoiceItemBaseModelDao.getCreatedReasonCode(),
              businessInvoiceItemBaseModelDao.getCreatedComments(),
              businessInvoiceItemBaseModelDao.getAccountId(),
              businessInvoiceItemBaseModelDao.getAccountName(),
              businessInvoiceItemBaseModelDao.getAccountExternalKey(),
              businessInvoiceItemBaseModelDao.getReportGroup());
        this.itemId = businessInvoiceItemBaseModelDao.getItemId();
        this.invoiceId = businessInvoiceItemBaseModelDao.getInvoiceId();
        this.invoiceNumber = businessInvoiceItemBaseModelDao.getInvoiceNumber();
        this.invoiceCreatedDate = businessInvoiceItemBaseModelDao.getInvoiceCreatedDate();
        this.invoiceDate = businessInvoiceItemBaseModelDao.getInvoiceDate();
        this.invoiceTargetDate = businessInvoiceItemBaseModelDao.getInvoiceTargetDate();
        this.invoiceCurrency = businessInvoiceItemBaseModelDao.getInvoiceCurrency();
        this.invoiceBalance = businessInvoiceItemBaseModelDao.getInvoiceBalance();
        this.convertedInvoiceBalance = businessInvoiceItemBaseModelDao.getConvertedInvoiceBalance();
        this.invoiceAmountPaid = businessInvoiceItemBaseModelDao.getInvoiceAmountPaid();
        this.convertedInvoiceAmountPaid = businessInvoiceItemBaseModelDao.getConvertedInvoiceAmountPaid();
        this.invoiceAmountCharged = businessInvoiceItemBaseModelDao.getInvoiceAmountCharged();
        this.convertedInvoiceAmountCharged = businessInvoiceItemBaseModelDao.getConvertedInvoiceAmountCharged();
        this.invoiceOriginalAmountCharged = businessInvoiceItemBaseModelDao.getInvoiceOriginalAmountCharged();
        this.convertedInvoiceOriginalAmountCharged = businessInvoiceItemBaseModelDao.getConvertedInvoiceOriginalAmountCharged();
        this.invoiceAmountCredited = businessInvoiceItemBaseModelDao.getInvoiceAmountCredited();
        this.convertedInvoiceAmountCredited = businessInvoiceItemBaseModelDao.getConvertedInvoiceAmountCredited();
        this.invoiceAmountRefunded = businessInvoiceItemBaseModelDao.getInvoiceAmountRefunded();
        this.convertedInvoiceAmountRefunded = businessInvoiceItemBaseModelDao.getConvertedInvoiceAmountRefunded();
        this.itemType = businessInvoiceItemBaseModelDao.getItemType();
        this.itemSource = businessInvoiceItemBaseModelDao.getItemSource();
        this.bundleId = businessInvoiceItemBaseModelDao.getBundleId();
        this.bundleExternalKey = businessInvoiceItemBaseModelDao.getBundleExternalKey();
        this.productName = businessInvoiceItemBaseModelDao.getProductName();
        this.productType = businessInvoiceItemBaseModelDao.getProductType();
        this.productCategory = businessInvoiceItemBaseModelDao.getProductCategory();
        this.slug = businessInvoiceItemBaseModelDao.getSlug();
        this.usageName = businessInvoiceItemBaseModelDao.getUsageName();
        this.phase = businessInvoiceItemBaseModelDao.getPhase();
        this.billingPeriod = businessInvoiceItemBaseModelDao.getBillingPeriod();
        this.startDate = businessInvoiceItemBaseModelDao.getStartDate();
        this.endDate = businessInvoiceItemBaseModelDao.getEndDate();
        this.amount = businessInvoiceItemBaseModelDao.getAmount();
        this.convertedAmount = businessInvoiceItemBaseModelDao.getConvertedAmount();
        this.currency = businessInvoiceItemBaseModelDao.getCurrency();
        this.linkedItemId = businessInvoiceItemBaseModelDao.getLinkedItemId();
        this.convertedCurrency = businessInvoiceItemBaseModelDao.getConvertedCurrency();
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
        final StringBuilder sb = new StringBuilder("BusinessInvoiceItem{");
        sb.append("itemId=").append(itemId);
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

        final BusinessInvoiceItem that = (BusinessInvoiceItem) o;

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
        if (invoiceNumber != null ? !invoiceNumber.equals(that.invoiceNumber) : that.invoiceNumber != null) {
            return false;
        }
        if (invoiceOriginalAmountCharged != null ? !(invoiceOriginalAmountCharged.compareTo(that.invoiceOriginalAmountCharged) == 0) : that.invoiceOriginalAmountCharged != null) {
            return false;
        }
        if (invoiceTargetDate != null ? invoiceTargetDate.compareTo(that.invoiceTargetDate) != 0 : that.invoiceTargetDate != null) {
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
        if (slug != null ? !slug.equals(that.slug) : that.slug != null) {
            return false;
        }
        if (startDate != null ? startDate.compareTo(that.startDate) != 0 : that.startDate != null) {
            return false;
        }
        if (usageName != null ? !usageName.equals(that.usageName) : that.usageName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (itemId != null ? itemId.hashCode() : 0);
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
