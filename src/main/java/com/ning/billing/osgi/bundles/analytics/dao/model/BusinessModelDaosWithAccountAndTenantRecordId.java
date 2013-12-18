/*
 * Copyright 2010-2013 Ning, Inc.
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

package com.ning.billing.osgi.bundles.analytics.dao.model;

import java.util.Collection;

public class BusinessModelDaosWithAccountAndTenantRecordId<T extends BusinessModelDaoBase> {

    private final Long accountRecordId;
    private final Long tenantRecordId;
    private final Collection<T> businessModelDaos;

    public BusinessModelDaosWithAccountAndTenantRecordId(final Long accountRecordId, final Long tenantRecordId, final Collection<T> businessModelDaos) {
        this.accountRecordId = accountRecordId;
        this.tenantRecordId = tenantRecordId;
        this.businessModelDaos = businessModelDaos;
    }

    public Long getAccountRecordId() {
        return accountRecordId;
    }

    public Long getTenantRecordId() {
        return tenantRecordId;
    }

    public Collection<T> getBusinessModelDaos() {
        return businessModelDaos;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessModelDaosWithAccountAndTenantRecordId{");
        sb.append("accountRecordId=").append(accountRecordId);
        sb.append(", tenantRecordId=").append(tenantRecordId);
        sb.append(", businessModelDaos=").append(businessModelDaos);
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

        final BusinessModelDaosWithAccountAndTenantRecordId that = (BusinessModelDaosWithAccountAndTenantRecordId) o;

        if (accountRecordId != null ? !accountRecordId.equals(that.accountRecordId) : that.accountRecordId != null) {
            return false;
        }
        if (businessModelDaos != null ? !businessModelDaos.equals(that.businessModelDaos) : that.businessModelDaos != null) {
            return false;
        }
        if (tenantRecordId != null ? !tenantRecordId.equals(that.tenantRecordId) : that.tenantRecordId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = accountRecordId != null ? accountRecordId.hashCode() : 0;
        result = 31 * result + (tenantRecordId != null ? tenantRecordId.hashCode() : 0);
        result = 31 * result + (businessModelDaos != null ? businessModelDaos.hashCode() : 0);
        return result;
    }
}
