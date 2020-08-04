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
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;

public class BusinessAccount extends BusinessEntityBase {

    private final String email;
    private final Integer firstNameLength;
    private final String currency;
    private final Integer billingCycleDayLocal;
    private final UUID paymentMethodId;
    private final String timeZone;
    private final String locale;
    private final String address1;
    private final String address2;
    private final String companyName;
    private final String city;
    private final String stateOrProvince;
    private final String country;
    private final String postalCode;
    private final String phone;
    private final Boolean isMigrated;
    private final DateTime updatedDate;
    private final BigDecimal balance;
    private final BigDecimal convertedBalance;
    private final LocalDate oldestUnpaidInvoiceDate;
    private final BigDecimal oldestUnpaidInvoiceBalance;
    private final BigDecimal convertedOldestUnpaidInvoiceBalance;
    private final String oldestUnpaidInvoiceCurrency;
    private final UUID oldestUnpaidInvoiceId;
    private final LocalDate lastInvoiceDate;
    private final BigDecimal lastInvoiceBalance;
    private final BigDecimal convertedLastInvoiceBalance;
    private final String lastInvoiceCurrency;
    private final UUID lastInvoiceId;
    private final DateTime lastPaymentDate;
    private final String lastPaymentStatus;
    private final Integer nbActiveBundles;
    private final String convertedCurrency;
    private final UUID parentAccountId;
    private final String parentAccountName;
    private final String parentAccountExternalKey;

    public BusinessAccount(final BusinessAccountModelDao businessAccountModelDao) {
        super(businessAccountModelDao.getCreatedDate(),
              businessAccountModelDao.getCreatedBy(),
              businessAccountModelDao.getCreatedReasonCode(),
              businessAccountModelDao.getCreatedComments(),
              businessAccountModelDao.getAccountId(),
              businessAccountModelDao.getAccountName(),
              businessAccountModelDao.getAccountExternalKey(),
              businessAccountModelDao.getReportGroup());
        this.email = businessAccountModelDao.getEmail();
        this.firstNameLength = businessAccountModelDao.getFirstNameLength();
        this.currency = businessAccountModelDao.getCurrency();
        this.billingCycleDayLocal = businessAccountModelDao.getBillingCycleDayLocal();
        this.paymentMethodId = businessAccountModelDao.getPaymentMethodId();
        this.timeZone = businessAccountModelDao.getTimeZone();
        this.locale = businessAccountModelDao.getLocale();
        this.address1 = businessAccountModelDao.getAddress1();
        this.address2 = businessAccountModelDao.getAddress2();
        this.companyName = businessAccountModelDao.getCompanyName();
        this.city = businessAccountModelDao.getCity();
        this.stateOrProvince = businessAccountModelDao.getStateOrProvince();
        this.country = businessAccountModelDao.getCountry();
        this.postalCode = businessAccountModelDao.getPostalCode();
        this.phone = businessAccountModelDao.getPhone();
        this.isMigrated = businessAccountModelDao.getMigrated();
        this.updatedDate = businessAccountModelDao.getUpdatedDate();
        this.balance = businessAccountModelDao.getBalance();
        this.convertedBalance = businessAccountModelDao.getConvertedBalance();
        this.oldestUnpaidInvoiceDate = businessAccountModelDao.getOldestUnpaidInvoiceDate();
        this.oldestUnpaidInvoiceBalance = businessAccountModelDao.getOldestUnpaidInvoiceBalance();
        this.convertedOldestUnpaidInvoiceBalance = businessAccountModelDao.getConvertedOldestUnpaidInvoiceBalance();
        this.oldestUnpaidInvoiceCurrency = businessAccountModelDao.getOldestUnpaidInvoiceCurrency();
        this.oldestUnpaidInvoiceId = businessAccountModelDao.getOldestUnpaidInvoiceId();
        this.lastInvoiceDate = businessAccountModelDao.getLastInvoiceDate();
        this.lastInvoiceBalance = businessAccountModelDao.getLastInvoiceBalance();
        this.convertedLastInvoiceBalance = businessAccountModelDao.getConvertedLastInvoiceBalance();
        this.lastInvoiceCurrency = businessAccountModelDao.getLastInvoiceCurrency();
        this.lastInvoiceId = businessAccountModelDao.getLastInvoiceId();
        this.lastPaymentDate = businessAccountModelDao.getLastPaymentDate();
        this.lastPaymentStatus = businessAccountModelDao.getLastPaymentStatus();
        this.nbActiveBundles = businessAccountModelDao.getNbActiveBundles();
        this.convertedCurrency = businessAccountModelDao.getConvertedCurrency();
        this.parentAccountId = businessAccountModelDao.getParentAccountId();
        this.parentAccountName = businessAccountModelDao.getParentAccountName();
        this.parentAccountExternalKey = businessAccountModelDao.getParentAccountExternalKey();
    }

    public String getEmail() {
        return email;
    }

    public Integer getFirstNameLength() {
        return firstNameLength;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getBillingCycleDayLocal() {
        return billingCycleDayLocal;
    }

    public UUID getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getLocale() {
        return locale;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCity() {
        return city;
    }

    public String getStateOrProvince() {
        return stateOrProvince;
    }

    public String getCountry() {
        return country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public Boolean getMigrated() {
        return isMigrated;
    }

    public DateTime getUpdatedDate() {
        return updatedDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getConvertedBalance() {
        return convertedBalance;
    }

    public LocalDate getOldestUnpaidInvoiceDate() {
        return oldestUnpaidInvoiceDate;
    }

    public BigDecimal getOldestUnpaidInvoiceBalance() {
        return oldestUnpaidInvoiceBalance;
    }

    public BigDecimal getConvertedOldestUnpaidInvoiceBalance() {
        return convertedOldestUnpaidInvoiceBalance;
    }

    public String getOldestUnpaidInvoiceCurrency() {
        return oldestUnpaidInvoiceCurrency;
    }

    public UUID getOldestUnpaidInvoiceId() {
        return oldestUnpaidInvoiceId;
    }

    public LocalDate getLastInvoiceDate() {
        return lastInvoiceDate;
    }

    public BigDecimal getLastInvoiceBalance() {
        return lastInvoiceBalance;
    }

    public BigDecimal getConvertedLastInvoiceBalance() {
        return convertedLastInvoiceBalance;
    }

    public String getLastInvoiceCurrency() {
        return lastInvoiceCurrency;
    }

    public UUID getLastInvoiceId() {
        return lastInvoiceId;
    }

    public DateTime getLastPaymentDate() {
        return lastPaymentDate;
    }

    public String getLastPaymentStatus() {
        return lastPaymentStatus;
    }

    public Integer getNbActiveBundles() {
        return nbActiveBundles;
    }

    public String getConvertedCurrency() {
        return convertedCurrency;
    }

    public UUID getParentAccountId() {
        return parentAccountId;
    }

    public String getParentAccountName() {
        return parentAccountName;
    }

    public String getParentAccountExternalKey() {
        return parentAccountExternalKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessAccount{");
        sb.append("email='").append(email).append('\'');
        sb.append(", firstNameLength=").append(firstNameLength);
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", billingCycleDayLocal=").append(billingCycleDayLocal);
        sb.append(", paymentMethodId=").append(paymentMethodId);
        sb.append(", timeZone='").append(timeZone).append('\'');
        sb.append(", locale='").append(locale).append('\'');
        sb.append(", address1='").append(address1).append('\'');
        sb.append(", address2='").append(address2).append('\'');
        sb.append(", companyName='").append(companyName).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", stateOrProvince='").append(stateOrProvince).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append(", postalCode='").append(postalCode).append('\'');
        sb.append(", phone='").append(phone).append('\'');
        sb.append(", isMigrated=").append(isMigrated);
        sb.append(", updatedDate=").append(updatedDate);
        sb.append(", balance=").append(balance);
        sb.append(", convertedBalance=").append(convertedBalance);
        sb.append(", oldestUnpaidInvoiceDate=").append(oldestUnpaidInvoiceDate);
        sb.append(", oldestUnpaidInvoiceBalance=").append(oldestUnpaidInvoiceBalance);
        sb.append(", convertedOldestUnpaidInvoiceBalance=").append(convertedOldestUnpaidInvoiceBalance);
        sb.append(", oldestUnpaidInvoiceCurrency='").append(oldestUnpaidInvoiceCurrency).append('\'');
        sb.append(", oldestUnpaidInvoiceId=").append(oldestUnpaidInvoiceId);
        sb.append(", lastInvoiceDate=").append(lastInvoiceDate);
        sb.append(", lastInvoiceBalance=").append(lastInvoiceBalance);
        sb.append(", convertedLastInvoiceBalance=").append(convertedLastInvoiceBalance);
        sb.append(", lastInvoiceCurrency='").append(lastInvoiceCurrency).append('\'');
        sb.append(", lastInvoiceId=").append(lastInvoiceId);
        sb.append(", lastPaymentDate=").append(lastPaymentDate);
        sb.append(", lastPaymentStatus='").append(lastPaymentStatus).append('\'');
        sb.append(", nbActiveBundles=").append(nbActiveBundles);
        sb.append(", convertedCurrency='").append(convertedCurrency).append('\'');
        sb.append(", parentAccountId=").append(parentAccountId);
        sb.append(", parentAccountName='").append(parentAccountName).append('\'');
        sb.append(", parentAccountExternalKey='").append(parentAccountExternalKey).append('\'');
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

        final BusinessAccount that = (BusinessAccount) o;

        if (address1 != null ? !address1.equals(that.address1) : that.address1 != null) {
            return false;
        }
        if (address2 != null ? !address2.equals(that.address2) : that.address2 != null) {
            return false;
        }
        if (balance != null ? !(balance.compareTo(that.balance) == 0) : that.balance != null) {
            return false;
        }
        if (billingCycleDayLocal != null ? !billingCycleDayLocal.equals(that.billingCycleDayLocal) : that.billingCycleDayLocal != null) {
            return false;
        }
        if (city != null ? !city.equals(that.city) : that.city != null) {
            return false;
        }
        if (companyName != null ? !companyName.equals(that.companyName) : that.companyName != null) {
            return false;
        }
        if (convertedBalance != null ? !(convertedBalance.compareTo(that.convertedBalance) == 0) : that.convertedBalance != null) {
            return false;
        }
        if (convertedCurrency != null ? !convertedCurrency.equals(that.convertedCurrency) : that.convertedCurrency != null) {
            return false;
        }
        if (convertedLastInvoiceBalance != null ? !(convertedLastInvoiceBalance.compareTo(that.convertedLastInvoiceBalance) == 0) : that.convertedLastInvoiceBalance != null) {
            return false;
        }
        if (convertedOldestUnpaidInvoiceBalance != null ? !(convertedOldestUnpaidInvoiceBalance.compareTo(that.convertedOldestUnpaidInvoiceBalance) == 0) : that.convertedOldestUnpaidInvoiceBalance != null) {
            return false;
        }
        if (country != null ? !country.equals(that.country) : that.country != null) {
            return false;
        }
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) {
            return false;
        }
        if (email != null ? !email.equals(that.email) : that.email != null) {
            return false;
        }
        if (firstNameLength != null ? !firstNameLength.equals(that.firstNameLength) : that.firstNameLength != null) {
            return false;
        }
        if (isMigrated != null ? !isMigrated.equals(that.isMigrated) : that.isMigrated != null) {
            return false;
        }
        if (lastInvoiceBalance != null ? !(lastInvoiceBalance.compareTo(that.lastInvoiceBalance) == 0) : that.lastInvoiceBalance != null) {
            return false;
        }
        if (lastInvoiceCurrency != null ? !lastInvoiceCurrency.equals(that.lastInvoiceCurrency) : that.lastInvoiceCurrency != null) {
            return false;
        }
        if (lastInvoiceDate != null ? lastInvoiceDate.compareTo(that.lastInvoiceDate) != 0 : that.lastInvoiceDate != null) {
            return false;
        }
        if (lastInvoiceId != null ? !lastInvoiceId.equals(that.lastInvoiceId) : that.lastInvoiceId != null) {
            return false;
        }
        if (lastPaymentDate != null ? lastPaymentDate.compareTo(that.lastPaymentDate) != 0 : that.lastPaymentDate != null) {
            return false;
        }
        if (lastPaymentStatus != null ? !lastPaymentStatus.equals(that.lastPaymentStatus) : that.lastPaymentStatus != null) {
            return false;
        }
        if (locale != null ? !locale.equals(that.locale) : that.locale != null) {
            return false;
        }
        if (nbActiveBundles != null ? !nbActiveBundles.equals(that.nbActiveBundles) : that.nbActiveBundles != null) {
            return false;
        }
        if (oldestUnpaidInvoiceBalance != null ? !(oldestUnpaidInvoiceBalance.compareTo(that.oldestUnpaidInvoiceBalance) == 0) : that.oldestUnpaidInvoiceBalance != null) {
            return false;
        }
        if (oldestUnpaidInvoiceCurrency != null ? !oldestUnpaidInvoiceCurrency.equals(that.oldestUnpaidInvoiceCurrency) : that.oldestUnpaidInvoiceCurrency != null) {
            return false;
        }
        if (oldestUnpaidInvoiceDate != null ? oldestUnpaidInvoiceDate.compareTo(that.oldestUnpaidInvoiceDate) != 0 : that.oldestUnpaidInvoiceDate != null) {
            return false;
        }
        if (oldestUnpaidInvoiceId != null ? !oldestUnpaidInvoiceId.equals(that.oldestUnpaidInvoiceId) : that.oldestUnpaidInvoiceId != null) {
            return false;
        }
        if (parentAccountExternalKey != null ? !parentAccountExternalKey.equals(that.parentAccountExternalKey) : that.parentAccountExternalKey != null) {
            return false;
        }
        if (parentAccountId != null ? !parentAccountId.equals(that.parentAccountId) : that.parentAccountId != null) {
            return false;
        }
        if (parentAccountName != null ? !parentAccountName.equals(that.parentAccountName) : that.parentAccountName != null) {
            return false;
        }
        if (paymentMethodId != null ? !paymentMethodId.equals(that.paymentMethodId) : that.paymentMethodId != null) {
            return false;
        }
        if (phone != null ? !phone.equals(that.phone) : that.phone != null) {
            return false;
        }
        if (postalCode != null ? !postalCode.equals(that.postalCode) : that.postalCode != null) {
            return false;
        }
        if (stateOrProvince != null ? !stateOrProvince.equals(that.stateOrProvince) : that.stateOrProvince != null) {
            return false;
        }
        if (timeZone != null ? !timeZone.equals(that.timeZone) : that.timeZone != null) {
            return false;
        }
        if (updatedDate != null ? updatedDate.compareTo(that.updatedDate) != 0 : that.updatedDate != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (firstNameLength != null ? firstNameLength.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (billingCycleDayLocal != null ? billingCycleDayLocal.hashCode() : 0);
        result = 31 * result + (paymentMethodId != null ? paymentMethodId.hashCode() : 0);
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (address1 != null ? address1.hashCode() : 0);
        result = 31 * result + (address2 != null ? address2.hashCode() : 0);
        result = 31 * result + (companyName != null ? companyName.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (stateOrProvince != null ? stateOrProvince.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (isMigrated != null ? isMigrated.hashCode() : 0);
        result = 31 * result + (updatedDate != null ? updatedDate.hashCode() : 0);
        result = 31 * result + (balance != null ? balance.hashCode() : 0);
        result = 31 * result + (convertedBalance != null ? convertedBalance.hashCode() : 0);
        result = 31 * result + (oldestUnpaidInvoiceDate != null ? oldestUnpaidInvoiceDate.hashCode() : 0);
        result = 31 * result + (oldestUnpaidInvoiceBalance != null ? oldestUnpaidInvoiceBalance.hashCode() : 0);
        result = 31 * result + (convertedOldestUnpaidInvoiceBalance != null ? convertedOldestUnpaidInvoiceBalance.hashCode() : 0);
        result = 31 * result + (oldestUnpaidInvoiceCurrency != null ? oldestUnpaidInvoiceCurrency.hashCode() : 0);
        result = 31 * result + (oldestUnpaidInvoiceId != null ? oldestUnpaidInvoiceId.hashCode() : 0);
        result = 31 * result + (lastInvoiceDate != null ? lastInvoiceDate.hashCode() : 0);
        result = 31 * result + (lastInvoiceBalance != null ? lastInvoiceBalance.hashCode() : 0);
        result = 31 * result + (convertedLastInvoiceBalance != null ? convertedLastInvoiceBalance.hashCode() : 0);
        result = 31 * result + (lastInvoiceCurrency != null ? lastInvoiceCurrency.hashCode() : 0);
        result = 31 * result + (lastInvoiceId != null ? lastInvoiceId.hashCode() : 0);
        result = 31 * result + (lastPaymentDate != null ? lastPaymentDate.hashCode() : 0);
        result = 31 * result + (lastPaymentStatus != null ? lastPaymentStatus.hashCode() : 0);
        result = 31 * result + (nbActiveBundles != null ? nbActiveBundles.hashCode() : 0);
        result = 31 * result + (convertedCurrency != null ? convertedCurrency.hashCode() : 0);
        result = 31 * result + (parentAccountId != null ? parentAccountId.hashCode() : 0);
        result = 31 * result + (parentAccountName != null ? parentAccountName.hashCode() : 0);
        result = 31 * result + (parentAccountExternalKey != null ? parentAccountExternalKey.hashCode() : 0);
        return result;
    }
}
