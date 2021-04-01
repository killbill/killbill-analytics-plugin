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

import org.joda.time.LocalDate;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;

public class BusinessAccountTransition extends BusinessEntityBase {

    private final String service;
    private final String state;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public BusinessAccountTransition(final BusinessAccountTransitionModelDao businessAccountTransitionModelDao) {
        super(businessAccountTransitionModelDao.getCreatedDate(),
              businessAccountTransitionModelDao.getCreatedBy(),
              businessAccountTransitionModelDao.getCreatedReasonCode(),
              businessAccountTransitionModelDao.getCreatedComments(),
              businessAccountTransitionModelDao.getAccountId(),
              businessAccountTransitionModelDao.getAccountName(),
              businessAccountTransitionModelDao.getAccountExternalKey(),
              businessAccountTransitionModelDao.getReportGroup());
        this.service = businessAccountTransitionModelDao.getService();
        this.state = businessAccountTransitionModelDao.getState();
        this.startDate = businessAccountTransitionModelDao.getStartDate();
        this.endDate = businessAccountTransitionModelDao.getEndDate();
    }

    public String getService() {
        return service;
    }

    public String getState() {
        return state;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessAccountTransition{");
        sb.append("service='").append(service).append('\'');
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

        final BusinessAccountTransition that = (BusinessAccountTransition) o;

        if (endDate != null ? endDate.compareTo(that.endDate) != 0 : that.endDate != null) {
            return false;
        }
        if (startDate != null ? startDate.compareTo(that.startDate) != 0 : that.startDate != null) {
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
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        return result;
    }
}
