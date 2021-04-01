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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.DELETE;
import org.jooby.mvc.GET;
import org.jooby.mvc.Local;
import org.jooby.mvc.PUT;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;
import org.killbill.billing.plugin.analytics.core.AnalyticsHealthcheck;
import org.killbill.billing.plugin.core.resources.PluginHealthcheck;
import org.killbill.billing.tenant.api.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Path("/healthcheck")
public class AnalyticsHealthcheckResource extends PluginHealthcheck {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsHealthcheckResource.class);

    private final AnalyticsHealthcheck healthcheck;
    private boolean inRotation = false;

    @Inject
    public AnalyticsHealthcheckResource(final AnalyticsHealthcheck healthcheck) {
        this.healthcheck = healthcheck;
    }

    @GET
    @Produces("application/json")
    public Result check(@Local @Named("killbill_tenant") final Optional<Tenant> tenant) {
        final Result check = check(healthcheck, tenant.orElse(null), null);

        if (!inRotation) {
            check.status(Status.SERVICE_UNAVAILABLE);
        }

        return check;
    }

    @DELETE
    public Result putOutOfRotation() {
        inRotation = false;
        logger.warn("Putting server out of rotation");
        return Results.with(Status.OK);
    }

    @PUT
    public Result putInRotation() {
        inRotation = true;
        logger.warn("Putting server in rotation");
        return Results.with(Status.OK);
    }
}
