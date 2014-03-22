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

package org.killbill.billing.plugin.analytics.dao.model;

import java.util.UUID;

import javax.annotation.Nullable;

import org.killbill.billing.account.api.Account;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.customfield.CustomField;

public class BusinessBundleFieldModelDao extends BusinessFieldModelDao {

    private UUID bundleId;
    private String bundleExternalKey;

    public BusinessBundleFieldModelDao() { /* When reading from the database */ }

    public BusinessBundleFieldModelDao(final Account account,
                                       final Long accountRecordId,
                                       final SubscriptionBundle bundle,
                                       final CustomField customField,
                                       final Long customFieldRecordId,
                                       @Nullable final AuditLog creationAuditLog,
                                       final Long tenantRecordId,
                                       @Nullable final ReportGroup reportGroup) {
        super(account,
              accountRecordId,
              customField,
              customFieldRecordId,
              creationAuditLog,
              tenantRecordId,
              reportGroup);
        this.bundleId = customField.getObjectId();
        this.bundleExternalKey = bundle.getExternalKey();
    }

    @Override
    public String getTableName() {
        return BUNDLE_FIELDS_TABLE_NAME;
    }

    public UUID getBundleId() {
        return bundleId;
    }

    public String getBundleExternalKey() {
        return bundleExternalKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessBundleFieldModelDao{");
        sb.append("bundleId=").append(bundleId);
        sb.append(", bundleExternalKey='").append(bundleExternalKey).append('\'');
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

        final BusinessBundleFieldModelDao that = (BusinessBundleFieldModelDao) o;

        if (bundleExternalKey != null ? !bundleExternalKey.equals(that.bundleExternalKey) : that.bundleExternalKey != null) {
            return false;
        }
        if (bundleId != null ? !bundleId.equals(that.bundleId) : that.bundleId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (bundleId != null ? bundleId.hashCode() : 0);
        result = 31 * result + (bundleExternalKey != null ? bundleExternalKey.hashCode() : 0);
        return result;
    }
}
