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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.GET;
import org.jooby.mvc.Header;
import org.jooby.mvc.Local;
import org.jooby.mvc.PUT;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillClock;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.api.BusinessSnapshot;
import org.killbill.billing.plugin.analytics.api.user.AnalyticsUserApi;
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;
import org.killbill.billing.plugin.api.PluginTenantContext;
import org.killbill.billing.plugin.core.resources.ExceptionResponse;
import org.killbill.billing.tenant.api.Tenant;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
// Handle /plugins/killbill-analytics/<accountId>
@Path("/{accountId}")
public class AnalyticsAccountResource extends BaseResource {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsAccountResource.class);

    @Inject
    public AnalyticsAccountResource(final AnalyticsUserApi analyticsUserApi, final ReportsUserApi reportsUserApi, final OSGIKillbillClock osgiKillbillClock) {
        super(analyticsUserApi, reportsUserApi, osgiKillbillClock);
    }

    @GET
    @Produces("application/json")
    public Result doGet(@Named("accountId") final UUID accountId,
                        @Local @Named("killbill_tenant") final Tenant tenant) {
        final TenantContext context = new PluginTenantContext(accountId, tenant.getId());

        final BusinessSnapshot businessSnapshot = analyticsUserApi.getBusinessSnapshot(accountId, context);
        return Results.with(businessSnapshot, Status.OK);
    }

    @PUT
    public Result doPut(@Named("accountId") final UUID accountId,
                        @Header(HDR_CREATED_BY) final Optional<String> createdBy,
                        @Header(HDR_REASON) final Optional<String> reason,
                        @Header(HDR_COMMENT) final Optional<String> comment,
                        @Local @Named("killbill_tenant") final Tenant tenant) {
        final CallContext context = createCallContext(createdBy, reason, comment, accountId, tenant);

        try {
            analyticsUserApi.rebuildAnalyticsForAccount(accountId, context);
            return Results.with(Status.NO_CONTENT);
        } catch (final AnalyticsRefreshException e) {
            logger.error("Error refreshing account {}", accountId, e);
            return Results.with(new ExceptionResponse(e, true), Status.SERVER_ERROR);
        }
    }
}
