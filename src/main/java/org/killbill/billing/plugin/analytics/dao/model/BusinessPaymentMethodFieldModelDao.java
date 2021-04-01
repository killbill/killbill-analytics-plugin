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

import java.util.UUID;

import javax.annotation.Nullable;

import org.killbill.billing.account.api.Account;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.customfield.CustomField;

public class BusinessPaymentMethodFieldModelDao extends BusinessFieldModelDao {

    private UUID paymentMethodId;

    public BusinessPaymentMethodFieldModelDao() { /* When reading from the database */ }

    public BusinessPaymentMethodFieldModelDao(final Account account,
                                              final Long accountRecordId,
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
        this.paymentMethodId = customField.getObjectId();
    }

    @Override
    public String getTableName() {
        return PAYMENT_METHOD_FIELDS_TABLE_NAME;
    }

    public UUID getPaymentMethodId() {
        return paymentMethodId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessTransactionFieldModelDao{");
        sb.append("paymentMethodId=").append(paymentMethodId);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BusinessPaymentMethodFieldModelDao that = (BusinessPaymentMethodFieldModelDao) o;

        return paymentMethodId != null ? paymentMethodId.equals(that.paymentMethodId) : that.paymentMethodId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (paymentMethodId != null ? paymentMethodId.hashCode() : 0);
        return result;
    }
}
