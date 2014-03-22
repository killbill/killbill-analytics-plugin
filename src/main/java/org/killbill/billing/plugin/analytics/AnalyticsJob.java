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

package org.killbill.billing.plugin.analytics;

import java.util.UUID;

import org.killbill.billing.ObjectType;
import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;
import org.killbill.notificationq.api.NotificationEvent;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalyticsJob implements NotificationEvent {

    private final ExtBusEventType eventType;
    private final ObjectType objectType;
    private final UUID objectId;
    private final UUID accountId;
    private final UUID tenantId;

    public AnalyticsJob(final ExtBusEvent extBusEvent) {
        this(extBusEvent.getEventType(),
             extBusEvent.getObjectType(),
             extBusEvent.getObjectId(),
             extBusEvent.getAccountId(),
             extBusEvent.getTenantId());
    }

    public AnalyticsJob(@JsonProperty("eventType") final ExtBusEventType eventType,
                        @JsonProperty("objectType") final ObjectType objectType,
                        @JsonProperty("objectId") final UUID objectId,
                        @JsonProperty("accountId") final UUID accountId,
                        @JsonProperty("tenantId") final UUID tenantId) {
        this.eventType = eventType;
        this.objectType = objectType;
        this.objectId = objectId;
        this.accountId = accountId;
        this.tenantId = tenantId;
    }

    public ExtBusEventType getEventType() {
        return eventType;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public UUID getObjectId() {
        return objectId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AnalyticsJob{");
        sb.append("eventType=").append(eventType);
        sb.append(", objectType=").append(objectType);
        sb.append(", objectId=").append(objectId);
        sb.append(", accountId=").append(accountId);
        sb.append(", tenantId=").append(tenantId);
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

        final AnalyticsJob job = (AnalyticsJob) o;

        if (accountId != null ? !accountId.equals(job.accountId) : job.accountId != null) {
            return false;
        }
        if (eventType != job.eventType) {
            return false;
        }
        if (objectId != null ? !objectId.equals(job.objectId) : job.objectId != null) {
            return false;
        }
        if (objectType != job.objectType) {
            return false;
        }
        if (tenantId != null ? !tenantId.equals(job.tenantId) : job.tenantId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventType != null ? eventType.hashCode() : 0;
        result = 31 * result + (objectType != null ? objectType.hashCode() : 0);
        result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        return result;
    }
}
