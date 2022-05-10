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

package org.killbill.billing.plugin.analytics.dao.model;

import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.util.audit.AuditLog;

public class BusinessAccountTransitionModelDao extends BusinessModelDaoBase {

    private static final String ACCOUNT_TRANSITIONS_TABLE_NAME = "analytics_account_transitions";
    private Long blockingStateRecordId;
    private String service;
    private String state;
    private DateTime startDate;
    private DateTime endDate;

    public BusinessAccountTransitionModelDao() { /* When reading from the database */ }

    public BusinessAccountTransitionModelDao(final Long blockingStateRecordId,
                                             final String service,
                                             final String state,
                                             final DateTime startDate,
                                             final DateTime endDate,
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
        this.blockingStateRecordId = blockingStateRecordId;
        this.service = service;
        this.state = state;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public BusinessAccountTransitionModelDao(final Account account,
                                             final Long accountRecordId,
                                             final String service,
                                             final String state,
                                             final DateTime startDate,
                                             final Long blockingStateRecordId,
                                             final DateTime endDate,
                                             @Nullable final AuditLog creationAuditLog,
                                             final Long tenantRecordId,
                                             @Nullable final ReportGroup reportGroup) {
        this(blockingStateRecordId,
             service,
             state,
             startDate,
             endDate,
             creationAuditLog != null ? creationAuditLog.getCreatedDate() : null,
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
        return ACCOUNT_TRANSITIONS_TABLE_NAME;
    }

    public Long getBlockingStateRecordId() {
        return blockingStateRecordId;
    }

    public String getService() {
        return service;
    }

    public String getState() {
        return state;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessAccountTransitionModelDao{");
        sb.append("blockingStateRecordId=").append(blockingStateRecordId);
        sb.append(", service='").append(service).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append(", startDate=").append(startDate);
        sb.append(", endDate=").append(endDate);
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

        final BusinessAccountTransitionModelDao that = (BusinessAccountTransitionModelDao) o;

        if (blockingStateRecordId != null ? !blockingStateRecordId.equals(that.blockingStateRecordId) : that.blockingStateRecordId != null) {
            return false;
        }
        if (endDate != null ? (endDate.compareTo(that.endDate) != 0) : that.endDate != null) {
            return false;
        }
        if (startDate != null ? (startDate.compareTo(that.startDate) != 0) : that.startDate != null) {
            return false;
        }
        if (service != null ? !service.equals(that.service) : that.service != null) {
            return false;
        }
        if (state != null ? !state.equals(that.state) : that.state != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (blockingStateRecordId != null ? blockingStateRecordId.hashCode() : 0);
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }
}
