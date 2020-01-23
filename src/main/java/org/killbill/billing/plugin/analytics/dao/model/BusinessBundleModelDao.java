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
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AuditLog;

public class BusinessBundleModelDao extends BusinessModelDaoBase {

    public static final String BUNDLES_TABLE_NAME = "analytics_bundles";

    private Long bundleRecordId;
    private UUID bundleId;
    private String bundleExternalKey;
    private UUID subscriptionId;
    private Integer bundleAccountRank;
    private Boolean latestForBundleExternalKey;
    private LocalDate chargedThroughDate;
    private String currentProductName;
    private String currentProductType;
    private String currentProductCategory;
    private String currentSlug;
    private String currentPhase;
    private String currentBillingPeriod;
    private BigDecimal currentPrice;
    private BigDecimal convertedCurrentPrice;
    private String currentPriceList;
    private BigDecimal currentMrr;
    private BigDecimal convertedCurrentMrr;
    private String currentCurrency;
    private Boolean currentBusinessActive;
    private LocalDate currentStartDate;
    private LocalDate currentEndDate;
    private String currentService;
    private String currentState;
    private String convertedCurrency;
    private DateTime originalCreatedDate;

    public BusinessBundleModelDao() { /* When reading from the database */ }

    public BusinessBundleModelDao(final Long bundleRecordId,
                                  final UUID bundleId,
                                  final String bundleExternalKey,
                                  final UUID subscriptionId,
                                  final Integer bundleAccountRank,
                                  final Boolean latestForBundleExternalKey,
                                  final LocalDate chargedThroughDate,
                                  final BusinessSubscriptionTransitionModelDao bst,
                                  final String convertedCurrency,
                                  final DateTime originalCreatedDate,
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
        this.bundleRecordId = bundleRecordId;
        this.bundleId = bundleId;
        this.bundleExternalKey = bundleExternalKey;
        this.subscriptionId = subscriptionId;
        this.bundleAccountRank = bundleAccountRank;
        this.latestForBundleExternalKey = latestForBundleExternalKey;
        this.chargedThroughDate = chargedThroughDate;
        this.currentProductName = bst.getNextProductName();
        this.currentProductType = bst.getNextProductType();
        this.currentProductCategory = bst.getNextProductCategory();
        this.currentSlug = bst.getNextSlug();
        this.currentPhase = bst.getNextPhase();
        this.currentBillingPeriod = bst.getNextBillingPeriod();
        this.currentPrice = bst.getNextPrice();
        this.convertedCurrentPrice = bst.getConvertedNextPrice();
        this.currentPriceList = bst.getNextPriceList();
        this.currentMrr = bst.getNextMrr();
        this.convertedCurrentMrr = bst.getConvertedNextMrr();
        this.currentCurrency = bst.getNextCurrency();
        this.currentBusinessActive = bst.getNextBusinessActive();
        if (bst.getEvent() != null &&
            (bst.getEvent().startsWith("STOP_ENTITLEMENT") || bst.getEvent().startsWith("STOP_BILLING"))) {
            this.currentStartDate = bst.getPrevStartDate();
            this.currentEndDate = bst.getRequestedTimestamp();
        } else {
            this.currentStartDate = bst.getNextStartDate();
            this.currentEndDate = bst.getNextEndDate();
        }
        this.currentService = bst.getNextService();
        this.currentState = bst.getNextState();
        this.convertedCurrency = convertedCurrency;
        this.originalCreatedDate = originalCreatedDate;
    }

    public BusinessBundleModelDao(final Account account,
                                  final Long accountRecordId,
                                  final SubscriptionBundle bundle,
                                  final Long bundleRecordId,
                                  final Integer bundleAccountRank,
                                  final Boolean latestForBundleExternalKey,
                                  final LocalDate chargedThroughDate,
                                  final BusinessSubscriptionTransitionModelDao bst,
                                  final CurrencyConverter currencyConverter,
                                  @Nullable final AuditLog creationAuditLog,
                                  final Long tenantRecordId,
                                  @Nullable final ReportGroup reportGroup) {
        this(bundleRecordId,
             bundle.getId(),
             bundle.getExternalKey(),
             bst.getSubscriptionId(),
             bundleAccountRank,
             latestForBundleExternalKey,
             chargedThroughDate,
             bst,
             currencyConverter.getConvertedCurrency(),
             bundle.getOriginalCreatedDate(),
             bundle.getCreatedDate(),
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
        return BUNDLES_TABLE_NAME;
    }

    public Long getBundleRecordId() {
        return bundleRecordId;
    }

    public UUID getBundleId() {
        return bundleId;
    }

    public String getBundleExternalKey() {
        return bundleExternalKey;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public Integer getBundleAccountRank() {
        return bundleAccountRank;
    }

    public Boolean getLatestForBundleExternalKey() {
        return latestForBundleExternalKey;
    }

    public LocalDate getChargedThroughDate() {
        return chargedThroughDate;
    }

    public String getCurrentProductName() {
        return currentProductName;
    }

    public String getCurrentProductType() {
        return currentProductType;
    }

    public String getCurrentProductCategory() {
        return currentProductCategory;
    }

    public String getCurrentSlug() {
        return currentSlug;
    }

    public String getCurrentPhase() {
        return currentPhase;
    }

    public String getCurrentBillingPeriod() {
        return currentBillingPeriod;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getConvertedCurrentPrice() {
        return convertedCurrentPrice;
    }

    public String getCurrentPriceList() {
        return currentPriceList;
    }

    public BigDecimal getCurrentMrr() {
        return currentMrr;
    }

    public BigDecimal getConvertedCurrentMrr() {
        return convertedCurrentMrr;
    }

    public String getCurrentCurrency() {
        return currentCurrency;
    }

    public Boolean getCurrentBusinessActive() {
        return currentBusinessActive;
    }

    public LocalDate getCurrentStartDate() {
        return currentStartDate;
    }

    public LocalDate getCurrentEndDate() {
        return currentEndDate;
    }

    public String getCurrentService() {
        return currentService;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getConvertedCurrency() {
        return convertedCurrency;
    }

    public DateTime getOriginalCreatedDate() {
        return originalCreatedDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessBundleModelDao{");
        sb.append("bundleRecordId=").append(bundleRecordId);
        sb.append(", bundleId=").append(bundleId);
        sb.append(", bundleExternalKey='").append(bundleExternalKey).append('\'');
        sb.append(", subscriptionId=").append(subscriptionId);
        sb.append(", bundleAccountRank=").append(bundleAccountRank);
        sb.append(", latestForBundleExternalKey=").append(latestForBundleExternalKey);
        sb.append(", chargedThroughDate=").append(chargedThroughDate);
        sb.append(", currentProductName='").append(currentProductName).append('\'');
        sb.append(", currentProductType='").append(currentProductType).append('\'');
        sb.append(", currentProductCategory='").append(currentProductCategory).append('\'');
        sb.append(", currentSlug='").append(currentSlug).append('\'');
        sb.append(", currentPhase='").append(currentPhase).append('\'');
        sb.append(", currentBillingPeriod='").append(currentBillingPeriod).append('\'');
        sb.append(", currentPrice=").append(currentPrice);
        sb.append(", convertedCurrentPrice=").append(convertedCurrentPrice);
        sb.append(", currentPriceList='").append(currentPriceList).append('\'');
        sb.append(", currentMrr=").append(currentMrr);
        sb.append(", convertedCurrentMrr=").append(convertedCurrentMrr);
        sb.append(", currentCurrency='").append(currentCurrency).append('\'');
        sb.append(", currentBusinessActive=").append(currentBusinessActive);
        sb.append(", currentStartDate=").append(currentStartDate);
        sb.append(", currentEndDate=").append(currentEndDate);
        sb.append(", currentService='").append(currentService).append('\'');
        sb.append(", currentState='").append(currentState).append('\'');
        sb.append(", convertedCurrency='").append(convertedCurrency).append('\'');
        sb.append(", originalCreatedDate='").append(originalCreatedDate).append('\'');
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

        final BusinessBundleModelDao that = (BusinessBundleModelDao) o;

        if (bundleAccountRank != null ? !bundleAccountRank.equals(that.bundleAccountRank) : that.bundleAccountRank != null) {
            return false;
        }
        if (latestForBundleExternalKey != null ? !latestForBundleExternalKey.equals(that.latestForBundleExternalKey) : that.latestForBundleExternalKey != null) {
            return false;
        }
        if (bundleExternalKey != null ? !bundleExternalKey.equals(that.bundleExternalKey) : that.bundleExternalKey != null) {
            return false;
        }
        if (bundleId != null ? !bundleId.equals(that.bundleId) : that.bundleId != null) {
            return false;
        }
        if (bundleRecordId != null ? !bundleRecordId.equals(that.bundleRecordId) : that.bundleRecordId != null) {
            return false;
        }
        if (chargedThroughDate != null ? !chargedThroughDate.equals(that.chargedThroughDate) : that.chargedThroughDate != null) {
            return false;
        }
        if (convertedCurrency != null ? !convertedCurrency.equals(that.convertedCurrency) : that.convertedCurrency != null) {
            return false;
        }
        if (convertedCurrentMrr != null ? !(convertedCurrentMrr.compareTo(that.convertedCurrentMrr) == 0) : that.convertedCurrentMrr != null) {
            return false;
        }
        if (convertedCurrentPrice != null ? !(convertedCurrentPrice.compareTo(that.convertedCurrentPrice) == 0) : that.convertedCurrentPrice != null) {
            return false;
        }
        if (currentBillingPeriod != null ? !currentBillingPeriod.equals(that.currentBillingPeriod) : that.currentBillingPeriod != null) {
            return false;
        }
        if (currentBusinessActive != null ? !currentBusinessActive.equals(that.currentBusinessActive) : that.currentBusinessActive != null) {
            return false;
        }
        if (currentCurrency != null ? !currentCurrency.equals(that.currentCurrency) : that.currentCurrency != null) {
            return false;
        }
        if (currentEndDate != null ? (currentEndDate.compareTo(that.currentEndDate) != 0) : that.currentEndDate != null) {
            return false;
        }
        if (currentMrr != null ? !(currentMrr.compareTo(that.currentMrr) == 0) : that.currentMrr != null) {
            return false;
        }
        if (currentPhase != null ? !currentPhase.equals(that.currentPhase) : that.currentPhase != null) {
            return false;
        }
        if (currentPrice != null ? !(currentPrice.compareTo(that.currentPrice) == 0) : that.currentPrice != null) {
            return false;
        }
        if (currentPriceList != null ? !currentPriceList.equals(that.currentPriceList) : that.currentPriceList != null) {
            return false;
        }
        if (currentProductCategory != null ? !currentProductCategory.equals(that.currentProductCategory) : that.currentProductCategory != null) {
            return false;
        }
        if (currentProductName != null ? !currentProductName.equals(that.currentProductName) : that.currentProductName != null) {
            return false;
        }
        if (currentProductType != null ? !currentProductType.equals(that.currentProductType) : that.currentProductType != null) {
            return false;
        }
        if (currentService != null ? !currentService.equals(that.currentService) : that.currentService != null) {
            return false;
        }
        if (currentSlug != null ? !currentSlug.equals(that.currentSlug) : that.currentSlug != null) {
            return false;
        }
        if (currentStartDate != null ? (currentStartDate.compareTo(that.currentStartDate) != 0) : that.currentStartDate != null) {
            return false;
        }
        if (currentState != null ? !currentState.equals(that.currentState) : that.currentState != null) {
            return false;
        }
        if (subscriptionId != null ? !subscriptionId.equals(that.subscriptionId) : that.subscriptionId != null) {
            return false;
        }
        if (originalCreatedDate != null ? (originalCreatedDate.compareTo(that.originalCreatedDate) != 0) : that.originalCreatedDate != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (bundleRecordId != null ? bundleRecordId.hashCode() : 0);
        result = 31 * result + (bundleId != null ? bundleId.hashCode() : 0);
        result = 31 * result + (bundleExternalKey != null ? bundleExternalKey.hashCode() : 0);
        result = 31 * result + (subscriptionId != null ? subscriptionId.hashCode() : 0);
        result = 31 * result + (bundleAccountRank != null ? bundleAccountRank.hashCode() : 0);
        result = 31 * result + (latestForBundleExternalKey != null ? latestForBundleExternalKey.hashCode() : 0);
        result = 31 * result + (chargedThroughDate != null ? chargedThroughDate.hashCode() : 0);
        result = 31 * result + (currentProductName != null ? currentProductName.hashCode() : 0);
        result = 31 * result + (currentProductType != null ? currentProductType.hashCode() : 0);
        result = 31 * result + (currentProductCategory != null ? currentProductCategory.hashCode() : 0);
        result = 31 * result + (currentSlug != null ? currentSlug.hashCode() : 0);
        result = 31 * result + (currentPhase != null ? currentPhase.hashCode() : 0);
        result = 31 * result + (currentBillingPeriod != null ? currentBillingPeriod.hashCode() : 0);
        result = 31 * result + (currentPrice != null ? currentPrice.hashCode() : 0);
        result = 31 * result + (convertedCurrentPrice != null ? convertedCurrentPrice.hashCode() : 0);
        result = 31 * result + (currentPriceList != null ? currentPriceList.hashCode() : 0);
        result = 31 * result + (currentMrr != null ? currentMrr.hashCode() : 0);
        result = 31 * result + (convertedCurrentMrr != null ? convertedCurrentMrr.hashCode() : 0);
        result = 31 * result + (currentCurrency != null ? currentCurrency.hashCode() : 0);
        result = 31 * result + (currentBusinessActive != null ? currentBusinessActive.hashCode() : 0);
        result = 31 * result + (currentStartDate != null ? currentStartDate.hashCode() : 0);
        result = 31 * result + (currentEndDate != null ? currentEndDate.hashCode() : 0);
        result = 31 * result + (currentService != null ? currentService.hashCode() : 0);
        result = 31 * result + (currentState != null ? currentState.hashCode() : 0);
        result = 31 * result + (convertedCurrency != null ? convertedCurrency.hashCode() : 0);
        result = 31 * result + (originalCreatedDate != null ? originalCreatedDate.hashCode() : 0);
        return result;
    }
}
