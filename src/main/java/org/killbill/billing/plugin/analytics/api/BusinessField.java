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
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentMethodFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessTransactionFieldModelDao;

public class BusinessField extends BusinessEntityBase {

    private final ObjectType objectType;
    private final UUID objectId;
    private final String name;
    private final String value;

    private BusinessField(final ObjectType objectType, final BusinessFieldModelDao businessFieldModelDao, final UUID objectId) {
        super(businessFieldModelDao.getCreatedDate(),
              businessFieldModelDao.getCreatedBy(),
              businessFieldModelDao.getCreatedReasonCode(),
              businessFieldModelDao.getCreatedComments(),
              businessFieldModelDao.getAccountId(),
              businessFieldModelDao.getAccountName(),
              businessFieldModelDao.getAccountExternalKey(),
              businessFieldModelDao.getReportGroup());
        this.objectType = objectType;
        this.name = businessFieldModelDao.getName();
        this.value = businessFieldModelDao.getValue();
        this.objectId = objectId;
    }

    public static BusinessField create(final BusinessFieldModelDao businessFieldModelDao) {
        if (businessFieldModelDao instanceof BusinessAccountFieldModelDao) {
            return new BusinessField(ObjectType.ACCOUNT, businessFieldModelDao, businessFieldModelDao.getAccountId());
        } else if (businessFieldModelDao instanceof BusinessBundleFieldModelDao) {
            return new BusinessField(ObjectType.BUNDLE, businessFieldModelDao, ((BusinessBundleFieldModelDao) businessFieldModelDao).getBundleId());
        } else if (businessFieldModelDao instanceof BusinessInvoiceFieldModelDao) {
            return new BusinessField(ObjectType.INVOICE, businessFieldModelDao, ((BusinessInvoiceFieldModelDao) businessFieldModelDao).getInvoiceId());
        } else if (businessFieldModelDao instanceof BusinessInvoicePaymentFieldModelDao) {
            return new BusinessField(ObjectType.INVOICE_PAYMENT, businessFieldModelDao, ((BusinessInvoicePaymentFieldModelDao) businessFieldModelDao).getInvoicePaymentId());
        } else if (businessFieldModelDao instanceof BusinessPaymentFieldModelDao) {
            return new BusinessField(ObjectType.PAYMENT, businessFieldModelDao, ((BusinessPaymentFieldModelDao) businessFieldModelDao).getPaymentId());
        } else if (businessFieldModelDao instanceof BusinessPaymentMethodFieldModelDao) {
            return new BusinessField(ObjectType.PAYMENT_METHOD, businessFieldModelDao, ((BusinessPaymentMethodFieldModelDao) businessFieldModelDao).getPaymentMethodId());
        } else if (businessFieldModelDao instanceof BusinessTransactionFieldModelDao) {
            return new BusinessField(ObjectType.TRANSACTION, businessFieldModelDao, ((BusinessTransactionFieldModelDao) businessFieldModelDao).getTransactionId());
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

    public String getValue() {
        return value;
    }

    public UUID getObjectId() {
        return objectId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BusinessField{");
        sb.append("objectType=").append(objectType);
        sb.append(", objectId=").append(objectId);
        sb.append(", name='").append(name).append('\'');
        sb.append(", value='").append(value).append('\'');
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

        final BusinessField that = (BusinessField) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (objectId != null ? !objectId.equals(that.objectId) : that.objectId != null) {
            return false;
        }
        if (objectType != that.objectType) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
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
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
