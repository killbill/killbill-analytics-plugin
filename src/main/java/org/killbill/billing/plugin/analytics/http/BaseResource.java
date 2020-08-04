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

package org.killbill.billing.plugin.analytics.http;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillClock;
import org.killbill.billing.plugin.analytics.AnalyticsActivator;
import org.killbill.billing.plugin.analytics.api.user.AnalyticsUserApi;
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;
import org.killbill.billing.plugin.api.PluginCallContext;
import org.killbill.billing.tenant.api.Tenant;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.CallOrigin;
import org.killbill.billing.util.callcontext.UserType;

public abstract class BaseResource {

    protected static final String HDR_CREATED_BY = "X-Killbill-CreatedBy";
    protected static final String HDR_REASON = "X-Killbill-Reason";
    protected static final String HDR_COMMENT = "X-Killbill-Comment";

    protected final AnalyticsUserApi analyticsUserApi;
    protected final ReportsUserApi reportsUserApi;
    private final OSGIKillbillClock osgiKillbillClock;

    public BaseResource(final AnalyticsUserApi analyticsUserApi,
                        final ReportsUserApi reportsUserApi,
                        final OSGIKillbillClock osgiKillbillClock) {
        this.analyticsUserApi = analyticsUserApi;
        this.reportsUserApi = reportsUserApi;
        this.osgiKillbillClock = osgiKillbillClock;
    }

    protected CallContext createCallContext(final Optional<String> createdBy,
                                            final Optional<String> reason,
                                            final Optional<String> comment,
                                            @Nullable final UUID accountId,
                                            final Tenant tenant) {
        final DateTime utcNow = osgiKillbillClock.getClock().getUTCNow();
        return new PluginCallContext(UUID.randomUUID(),
                                     createdBy.orElse(AnalyticsActivator.PLUGIN_NAME),
                                     CallOrigin.EXTERNAL,
                                     UserType.SYSTEM,
                                     reason.orElse(null),
                                     comment.orElse(null),
                                     utcNow,
                                     utcNow,
                                     accountId,
                                     tenant.getId());
    }
}
