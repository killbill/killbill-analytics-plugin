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

import java.util.UUID;

import org.killbill.billing.ObjectType;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessTagModelDao;

public class BusinessTag extends BusinessEntityBase {

    private final ObjectType objectType;
    private final UUID objectId;
    private final String name;

    private BusinessTag(final ObjectType objectType, final BusinessTagModelDao businessTagModelDao, final UUID objectId) {
        super(businessTagModelDao.getCreatedDate(),
              businessTagModelDao.getCreatedBy(),
              businessTagModelDao.getCreatedReasonCode(),
              businessTagModelDao.getCreatedComments(),
              businessTagModelDao.getAccountId(),
              businessTagModelDao.getAccountName(),
              businessTagModelDao.getAccountExternalKey(),
              businessTagModelDao.getReportGroup());
        this.objectType = objectType;
        this.name = businessTagModelDao.getName();
        this.objectId = objectId;
    }

    public static BusinessTag create(final BusinessTagModelDao businessTagModelDao) {
        if (businessTagModelDao instanceof BusinessAccountTagModelDao) {
            return new BusinessTag(ObjectType.ACCOUNT, businessTagModelDao, businessTagModelDao.getAccountId());
        } else if (businessTagModelDao instanceof BusinessBundleTagModelDao) {
            return new BusinessTag(ObjectType.BUNDLE, businessTagModelDao, ((BusinessBundleTagModelDao) businessTagModelDao).getBundleId());
        } else if (businessTagModelDao instanceof BusinessInvoiceTagModelDao) {
            return new BusinessTag(ObjectType.INVOICE, businessTagModelDao, ((BusinessInvoiceTagModelDao) businessTagModelDao).getInvoiceId());
        } else if (businessTagModelDao instanceof BusinessInvoicePaymentTagModelDao) {
            return new BusinessTag(ObjectType.INVOICE_PAYMENT, businessTagModelDao, ((BusinessInvoicePaymentTagModelDao) businessTagModelDao).getInvoicePaymentId());
        } else {
            return null;
        }
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getName() {
        return name;
    }

    public UUID getObjectId() {
        return objectId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessTag{");
        sb.append("objectType=").append(objectType);
        sb.append(", objectId=").append(objectId);
        sb.append(", name='").append(name).append('\'');
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

        final BusinessTag that = (BusinessTag) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (objectId != null ? !objectId.equals(that.objectId) : that.objectId != null) {
            return false;
        }
        if (objectType != that.objectType) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (objectType != null ? objectType.hashCode() : 0);
        result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
