/*
 * Copyright 2015 Groupon, Inc
 * Copyright 2015 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.http;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.CallOrigin;
import org.killbill.billing.util.callcontext.UserType;

public class AnalyticsApiCallContext implements CallContext {

    private final String createdBy;
    private final String reason;
    private final String comment;
    private final UUID tenantId;
    private final DateTime now;

    public AnalyticsApiCallContext(final String createdBy,
                                   final String reason,
                                   final String comment,
                                   final UUID tenantId) {
        this.createdBy = createdBy;
        this.reason = reason;
        this.comment = comment;
        this.tenantId = tenantId;

        this.now = new DateTime(DateTimeZone.UTC);
    }

    @Override
    public UUID getUserToken() {
        return UUID.randomUUID();
    }

    @Override
    public String getUserName() {
        return createdBy;
    }

    @Override
    public CallOrigin getCallOrigin() {
        return CallOrigin.EXTERNAL;
    }

    @Override
    public UserType getUserType() {
        return UserType.ADMIN;
    }

    @Override
    public String getReasonCode() {
        return reason;
    }

    @Override
    public String getComments() {
        return comment;
    }

    @Override
    public DateTime getCreatedDate() {
        return now;
    }

    @Override
    public DateTime getUpdatedDate() {
        return now;
    }

    @Override
    public UUID getTenantId() {
        return tenantId;
    }
}
