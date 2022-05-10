/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2022 Equinix, Inc
 * Copyright 2014-2022 The Billing Project, LLC
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
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleModelDao;

public class BusinessBundle extends BusinessEntityBase {

    private final Long bundleRecordId;
    private final UUID bundleId;
    private final String bundleExternalKey;
    private final UUID subscriptionId;
    private final Integer bundleAccountRank;
    private final Boolean latestForBundleExternalKey;
    private final LocalDate chargedThroughDate;
    private final String currentProductName;
    private final String currentProductType;
    private final String currentProductCategory;
    private final String currentSlug;
    private final String currentPhase;
    private final String currentBillingPeriod;
    private final BigDecimal currentPrice;
    private final BigDecimal convertedCurrentPrice;
    private final String currentPriceList;
    private final BigDecimal currentMrr;
    private final BigDecimal convertedCurrentMrr;
    private final String currentCurrency;
    private final Boolean currentBusinessActive;
    private final DateTime currentStartDate;
    private final DateTime currentEndDate;
    private final String currentService;
    private final String currentState;
    private final String convertedCurrency;
    private final DateTime originalCreatedDate;

    public BusinessBundle(final BusinessBundleModelDao businessBundleModelDao) {
        super(businessBundleModelDao.getCreatedDate(),
              businessBundleModelDao.getCreatedBy(),
              businessBundleModelDao.getCreatedReasonCode(),
              businessBundleModelDao.getCreatedComments(),
              businessBundleModelDao.getAccountId(),
              businessBundleModelDao.getAccountName(),
              businessBundleModelDao.getAccountExternalKey(),
              businessBundleModelDao.getReportGroup());
        this.bundleRecordId = businessBundleModelDao.getBundleRecordId();
        this.bundleId = businessBundleModelDao.getBundleId();
        this.bundleExternalKey = businessBundleModelDao.getBundleExternalKey();
        this.subscriptionId = businessBundleModelDao.getSubscriptionId();
        this.bundleAccountRank = businessBundleModelDao.getBundleAccountRank();
        this.latestForBundleExternalKey = businessBundleModelDao.getLatestForBundleExternalKey();
        this.chargedThroughDate = businessBundleModelDao.getChargedThroughDate();
        this.currentProductName = businessBundleModelDao.getCurrentProductName();
        this.currentProductType = businessBundleModelDao.getCurrentProductType();
        this.currentProductCategory = businessBundleModelDao.getCurrentProductCategory();
        this.currentSlug = businessBundleModelDao.getCurrentSlug();
        this.currentPhase = businessBundleModelDao.getCurrentPhase();
        this.currentBillingPeriod = businessBundleModelDao.getCurrentBillingPeriod();
        this.currentPrice = businessBundleModelDao.getCurrentPrice();
        this.convertedCurrentPrice = businessBundleModelDao.getConvertedCurrentPrice();
        this.currentPriceList = businessBundleModelDao.getCurrentPriceList();
        this.currentMrr = businessBundleModelDao.getCurrentMrr();
        this.convertedCurrentMrr = businessBundleModelDao.getConvertedCurrentMrr();
        this.currentCurrency = businessBundleModelDao.getCurrentCurrency();
        this.currentBusinessActive = businessBundleModelDao.getCurrentBusinessActive();
        this.currentStartDate = businessBundleModelDao.getCurrentStartDate();
        this.currentEndDate = businessBundleModelDao.getCurrentEndDate();
        this.currentService = businessBundleModelDao.getCurrentService();
        this.currentState = businessBundleModelDao.getCurrentState();
        this.convertedCurrency = businessBundleModelDao.getConvertedCurrency();
        this.originalCreatedDate = businessBundleModelDao.getOriginalCreatedDate();
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

    public DateTime getCurrentStartDate() {
        return currentStartDate;
    }

    public DateTime getCurrentEndDate() {
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
        final StringBuilder sb = new StringBuilder("BusinessBundle{");
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

        final BusinessBundle that = (BusinessBundle) o;

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
        if (chargedThroughDate != null ? chargedThroughDate.compareTo(that.chargedThroughDate) != 0 : that.chargedThroughDate != null) {
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
        if (currentEndDate != null ? currentEndDate.compareTo(that.currentEndDate) != 0 : that.currentEndDate != null) {
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
        if (currentStartDate != null ? currentStartDate.compareTo(that.currentStartDate) != 0 : that.currentStartDate != null) {
            return false;
        }
        if (currentState != null ? !currentState.equals(that.currentState) : that.currentState != null) {
            return false;
        }
        if (subscriptionId != null ? !subscriptionId.equals(that.subscriptionId) : that.subscriptionId != null) {
            return false;
        }
        if (originalCreatedDate != null ? originalCreatedDate.compareTo(that.originalCreatedDate) != 0 : that.originalCreatedDate != null) {
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
