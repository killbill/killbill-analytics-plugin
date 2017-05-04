/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2015 Groupon, Inc
 * Copyright 2014-2015 The Billing Project, LLC
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

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.killbill.billing.plugin.analytics.api.user.AnalyticsUserApi;
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;
import org.killbill.billing.tenant.api.Tenant;
import org.killbill.billing.util.callcontext.CallContext;
import org.osgi.service.log.LogService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.MoreObjects;

public abstract class BaseServlet extends HttpServlet {

    protected static final String STRING_PATTERN = "[\\w-]+";
    protected static final String UUID_PATTERN = "\\w+-\\w+-\\w+-\\w+-\\w+";
    protected static final String ANYTHING_PATTERN = ".*";

    protected static final String RESOURCE_NAME_ATTRIBUTE = "resourceName";
    protected static final String REPORT_NAME_ATTRIBUTE = "reportName";
    protected static final String SHOULD_REFRESH = "shouldRefresh";
    protected static final String KB_ACCOUNT_ID_ATTRIBUTE = "kbAccountId";

    protected static final String HDR_CREATED_BY = "X-Killbill-CreatedBy";
    protected static final String HDR_REASON = "X-Killbill-Reason";
    protected static final String HDR_COMMENT = "X-Killbill-Comment";

    protected static final ObjectMapper jsonMapper = ObjectMapperProvider.getJsonMapper();
    protected static final ObjectWriter csvMapper = ObjectMapperProvider.getCsvWriter();

    protected final AnalyticsUserApi analyticsUserApi;
    protected final ReportsUserApi reportsUserApi;
    protected final LogService logService;

    public BaseServlet(final AnalyticsUserApi analyticsUserApi, final ReportsUserApi reportsUserApi, final LogService logService) {
        this.analyticsUserApi = analyticsUserApi;
        this.reportsUserApi = reportsUserApi;
        this.logService = logService;
    }

    protected void setCrossSiteScriptingHeaders(final HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Request-Method", "GET");
        resp.setHeader("Access-Control-Allow-Headers", "accept, origin, content-type");
    }

    protected CallContext createCallContext(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final String createdBy = MoreObjects.firstNonNull(req.getHeader(HDR_CREATED_BY), req.getRemoteAddr());
        final String reason = req.getHeader(HDR_REASON);
        final String comment = MoreObjects.firstNonNull(req.getHeader(HDR_COMMENT), req.getRequestURI());

        // Set by the TenantFilter
        final Tenant tenant = (Tenant) req.getAttribute("killbill_tenant");
        final UUID kbAccountId = (UUID) req.getAttribute(KB_ACCOUNT_ID_ATTRIBUTE);

        UUID tenantId = null;
        if (tenant != null) {
            tenantId = tenant.getId();
        }

        return new AnalyticsApiCallContext(createdBy, reason, comment, tenantId, kbAccountId);
    }
}
