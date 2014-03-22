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

import java.util.Collection;

public class BusinessSnapshot {

    private final BusinessAccount businessAccount;
    private final Collection<BusinessBundle> businessBundles;
    private final Collection<BusinessSubscriptionTransition> businessSubscriptionTransitions;
    private final Collection<BusinessInvoice> businessInvoices;
    private final Collection<BusinessInvoicePayment> businessInvoicePayments;
    private final Collection<BusinessAccountTransition> businessAccountTransitions;
    private final Collection<BusinessTag> businessTags;
    private final Collection<BusinessField> businessFields;

    public BusinessSnapshot(final BusinessAccount businessAccount,
                            final Collection<BusinessBundle> businessBundles,
                            final Collection<BusinessSubscriptionTransition> businessSubscriptionTransitions,
                            final Collection<BusinessInvoice> businessInvoices,
                            final Collection<BusinessInvoicePayment> businessInvoicePayments,
                            final Collection<BusinessAccountTransition> businessAccountTransitions,
                            final Collection<BusinessTag> businessTags,
                            final Collection<BusinessField> businessFields) {
        this.businessAccount = businessAccount;
        this.businessBundles = businessBundles;
        this.businessSubscriptionTransitions = businessSubscriptionTransitions;
        this.businessInvoices = businessInvoices;
        this.businessInvoicePayments = businessInvoicePayments;
        this.businessAccountTransitions = businessAccountTransitions;
        this.businessTags = businessTags;
        this.businessFields = businessFields;
    }

    public BusinessAccount getBusinessAccount() {
        return businessAccount;
    }

    public Collection<BusinessBundle> getBusinessBundles() {
        return businessBundles;
    }

    public Collection<BusinessSubscriptionTransition> getBusinessSubscriptionTransitions() {
        return businessSubscriptionTransitions;
    }

    public Collection<BusinessInvoice> getBusinessInvoices() {
        return businessInvoices;
    }

    public Collection<BusinessInvoicePayment> getBusinessInvoicePayments() {
        return businessInvoicePayments;
    }

    public Collection<BusinessAccountTransition> getBusinessAccountTransitions() {
        return businessAccountTransitions;
    }

    public Collection<BusinessTag> getBusinessTags() {
        return businessTags;
    }

    public Collection<BusinessField> getBusinessFields() {
        return businessFields;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessSnapshot{");
        sb.append("businessAccount=").append(businessAccount);
        sb.append(", businessBundles=").append(businessBundles);
        sb.append(", businessSubscriptionTransitions=").append(businessSubscriptionTransitions);
        sb.append(", businessInvoices=").append(businessInvoices);
        sb.append(", businessInvoicePayments=").append(businessInvoicePayments);
        sb.append(", businessAccountTransitions=").append(businessAccountTransitions);
        sb.append(", businessTags=").append(businessTags);
        sb.append(", businessFields=").append(businessFields);
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

        final BusinessSnapshot that = (BusinessSnapshot) o;

        if (businessAccount != null ? !businessAccount.equals(that.businessAccount) : that.businessAccount != null) {
            return false;
        }
        if (businessAccountTransitions != null ? !businessAccountTransitions.equals(that.businessAccountTransitions) : that.businessAccountTransitions != null) {
            return false;
        }
        if (businessBundles != null ? !businessBundles.equals(that.businessBundles) : that.businessBundles != null) {
            return false;
        }
        if (businessFields != null ? !businessFields.equals(that.businessFields) : that.businessFields != null) {
            return false;
        }
        if (businessInvoicePayments != null ? !businessInvoicePayments.equals(that.businessInvoicePayments) : that.businessInvoicePayments != null) {
            return false;
        }
        if (businessInvoices != null ? !businessInvoices.equals(that.businessInvoices) : that.businessInvoices != null) {
            return false;
        }
        if (businessSubscriptionTransitions != null ? !businessSubscriptionTransitions.equals(that.businessSubscriptionTransitions) : that.businessSubscriptionTransitions != null) {
            return false;
        }
        if (businessTags != null ? !businessTags.equals(that.businessTags) : that.businessTags != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = businessAccount != null ? businessAccount.hashCode() : 0;
        result = 31 * result + (businessBundles != null ? businessBundles.hashCode() : 0);
        result = 31 * result + (businessSubscriptionTransitions != null ? businessSubscriptionTransitions.hashCode() : 0);
        result = 31 * result + (businessInvoices != null ? businessInvoices.hashCode() : 0);
        result = 31 * result + (businessInvoicePayments != null ? businessInvoicePayments.hashCode() : 0);
        result = 31 * result + (businessAccountTransitions != null ? businessAccountTransitions.hashCode() : 0);
        result = 31 * result + (businessTags != null ? businessTags.hashCode() : 0);
        result = 31 * result + (businessFields != null ? businessFields.hashCode() : 0);
        return result;
    }
}
