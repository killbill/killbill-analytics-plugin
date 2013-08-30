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

package com.ning.billing.osgi.bundles.analytics.api;

import org.joda.time.LocalDate;

import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessOverdueStatusModelDao;

public class BusinessOverdueStatus extends BusinessEntityBase {

    private final String status;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public BusinessOverdueStatus(final BusinessOverdueStatusModelDao businessOverdueStatusModelDao) {
        super(businessOverdueStatusModelDao.getCreatedDate(),
              businessOverdueStatusModelDao.getCreatedBy(),
              businessOverdueStatusModelDao.getCreatedReasonCode(),
              businessOverdueStatusModelDao.getCreatedComments(),
              businessOverdueStatusModelDao.getAccountId(),
              businessOverdueStatusModelDao.getAccountName(),
              businessOverdueStatusModelDao.getAccountExternalKey(),
              businessOverdueStatusModelDao.getReportGroup());
        this.status = businessOverdueStatusModelDao.getStatus();
        this.startDate = businessOverdueStatusModelDao.getStartDate();
        this.endDate = businessOverdueStatusModelDao.getEndDate();
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessOverdueStatus{");
        sb.append("status='").append(status).append('\'');
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

        final BusinessOverdueStatus that = (BusinessOverdueStatus) o;

        if (endDate != null ? endDate.compareTo(that.endDate) != 0 : that.endDate != null) {
            return false;
        }
        if (startDate != null ? startDate.compareTo(that.startDate) != 0 : that.startDate != null) {
            return false;
        }
        if (status != null ? !status.equals(that.status) : that.status != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }
}
