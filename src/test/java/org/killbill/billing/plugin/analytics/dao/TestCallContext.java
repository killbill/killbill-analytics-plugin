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

package org.killbill.billing.plugin.analytics.dao;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.CallOrigin;
import org.killbill.billing.util.callcontext.UserType;

public class TestCallContext implements CallContext {

    private final UUID userToken = UUID.randomUUID();
    private final String userName = UUID.randomUUID().toString();
    private final String reasonCode = UUID.randomUUID().toString();
    private final String comments = UUID.randomUUID().toString();
    private final DateTime createdDate = new DateTime(2010, 2, 4, 6, 8, 10, DateTimeZone.UTC);
    private final DateTime updatedDate = new DateTime(2011, 3, 5, 7, 9, 11, DateTimeZone.UTC);
    private final UUID tenantId = UUID.randomUUID();

    @Override
    public UUID getUserToken() {
        return userToken;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public CallOrigin getCallOrigin() {
        return CallOrigin.TEST;
    }

    @Override
    public UserType getUserType() {
        return UserType.TEST;
    }

    @Override
    public String getReasonCode() {
        return reasonCode;
    }

    @Override
    public String getComments() {
        return comments;
    }

    @Override
    public DateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public DateTime getUpdatedDate() {
        return updatedDate;
    }

    @Override
    public UUID getAccountId() {
        return null;
    }

    @Override
    public UUID getTenantId() {
        return tenantId;
    }
}
