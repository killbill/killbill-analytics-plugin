/*
 * Copyright 2010-2013 Ning, Inc.
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

package com.ning.billing.osgi.bundles.analytics.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.log.LogService;

import com.ning.billing.osgi.bundles.analytics.AnalyticsRefreshException;
import com.ning.billing.osgi.bundles.analytics.api.BusinessSnapshot;
import com.ning.billing.osgi.bundles.analytics.api.user.AnalyticsUserApi;
import com.ning.billing.osgi.bundles.analytics.json.NamedXYTimeSeries;
import com.ning.billing.osgi.bundles.analytics.reports.ReportsUserApi;
import com.ning.billing.osgi.bundles.analytics.reports.analysis.Smoother;
import com.ning.billing.osgi.bundles.analytics.reports.analysis.Smoother.SmootherType;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.callcontext.CallOrigin;
import com.ning.billing.util.callcontext.UserType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.io.Resources;

public class AnalyticsServlet extends HttpServlet {

    public static final String SERVER_IP = System.getProperty("com.ning.core.server.ip", "127.0.0.1");
    public static final String SERVER_PORT = System.getProperty("com.ning.core.server.port", "8080");
    public static DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final String QUERY_TENANT_ID = "tenantId";
    private static final String HDR_CREATED_BY = "X-Killbill-CreatedBy";
    private static final String HDR_REASON = "X-Killbill-Reason";
    private static final String HDR_COMMENT = "X-Killbill-Comment";

    private final static String STATIC_RESOURCES = "static";

    private final static String QUERY_START_DATE = "startDate";
    private final static String QUERY_END_DATE = "endDate";

    private static final String REPORTS = "reports";
    private static final String REPORTS_QUERY_NAME = "name";
    private static final String REPORTS_SMOOTHER_NAME = "smooth";

    private static final ObjectMapper mapper = ObjectMapperProvider.get();

    private final AnalyticsUserApi analyticsUserApi;
    private final ReportsUserApi reportsUserApi;
    private final LogService logService;

    public AnalyticsServlet(final AnalyticsUserApi analyticsUserApi, final ReportsUserApi reportsUserApi, final LogService logService) {
        this.analyticsUserApi = analyticsUserApi;
        this.reportsUserApi = reportsUserApi;
        this.logService = logService;
    }

    @Override
    protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        setCrossSiteScriptingHeaders(resp);
        super.doOptions(req, resp);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String uriOperationInfo = extractUriOperationInfo(req);
        if (uriOperationInfo.startsWith(STATIC_RESOURCES)) {
            doHandleStaticResource(uriOperationInfo, resp);
        } else if (uriOperationInfo.startsWith(REPORTS)) {
            doHandleReports(req, resp);
        } else {
            final UUID kbAccountId = getKbAccountId(req, resp);
            final CallContext context = createCallContext(req, resp);

            final BusinessSnapshot businessSnapshot = analyticsUserApi.getBusinessSnapshot(kbAccountId, context);
            resp.getOutputStream().write(mapper.writeValueAsBytes(businessSnapshot));
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final UUID kbAccountId = getKbAccountId(req, resp);
        final CallContext context = createCallContext(req, resp);

        try {
            analyticsUserApi.rebuildAnalyticsForAccount(kbAccountId, context);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (AnalyticsRefreshException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private CallContext createCallContext(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final String createdBy = Objects.firstNonNull(req.getHeader(HDR_CREATED_BY), req.getRemoteAddr());
        final String reason = req.getHeader(HDR_REASON);
        final String comment = Objects.firstNonNull(req.getHeader(HDR_COMMENT), req.getRequestURI());

        final String tenantIdString = req.getParameter(QUERY_TENANT_ID);

        UUID tenantId = null;
        if (tenantIdString != null) {
            try {
                tenantId = UUID.fromString(tenantIdString);
            } catch (final IllegalArgumentException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID for tenant id: " + tenantIdString);
                return null;
            }
        }
        return new AnalyticsApiCallContext(createdBy, reason, comment, tenantId);
    }

    private String extractUriOperationInfo(final HttpServletRequest req) throws ServletException {
        logService.log(LogService.LOG_INFO, "extractUriOperationInfo :" + req.getPathInfo());
        return req.getPathInfo().substring(1, req.getPathInfo().length());
    }

    private void doHandleReports(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String[] rawReportNames = req.getParameterValues(REPORTS_QUERY_NAME);
        if (rawReportNames == null || rawReportNames.length == 0) {
            resp.sendError(404);
            return;
        }

        final LocalDate startDate = Strings.emptyToNull(req.getParameter(QUERY_START_DATE)) != null ? DATE_FORMAT.parseLocalDate(req.getParameter(QUERY_START_DATE)) : null;
        final LocalDate endDate = Strings.emptyToNull(req.getParameter(QUERY_END_DATE)) != null ? DATE_FORMAT.parseLocalDate(req.getParameter(QUERY_END_DATE)) : null;

        final SmootherType smootherType = Smoother.fromString(Strings.emptyToNull(req.getParameter(REPORTS_SMOOTHER_NAME)));

        // TODO PIERRE Switch to an equivalent of StreamingOutputStream?
        final List<NamedXYTimeSeries> result = reportsUserApi.getTimeSeriesDataForReport(rawReportNames, startDate, endDate, smootherType);

        resp.getOutputStream().write(mapper.writeValueAsBytes(result));
        resp.setContentType("application/json");
        setCrossSiteScriptingHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCrossSiteScriptingHeaders(final HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", String.format("http://%s:%s", SERVER_IP, SERVER_PORT));
        resp.setHeader("Access-Control-Request-Method", "GET");
        resp.setHeader("Access-Control-Allow-Headers", "accept, origin, content-type");
    }

    private void doHandleStaticResource(final String resourceName, final HttpServletResponse resp) throws IOException {
        final URL resourceUrl = Resources.getResource(resourceName);

        final String[] parts = resourceName.split("/");
        if (parts.length > 2) {
            if (parts[1].equals("javascript")) {
                resp.setContentType("application/javascript");
            } else if (parts[1].equals("styles")) {
                resp.setContentType("text/css");
            }
            Resources.copy(resourceUrl, resp.getOutputStream());
        } else {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Resources.copy(resourceUrl, out);
            String inputHtml = new String(out.toByteArray());

            String tmp1 = inputHtml.replace("$VAR_SERVER", "\"" + SERVER_IP + "\"");
            String tmp2 = tmp1.replace("$VAR_PORT", "\"" + SERVER_PORT + "\"");
            resp.getOutputStream().write(tmp2.getBytes());
            resp.setContentType("text/html");
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private UUID getKbAccountId(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final String kbAccountIdString;
        try {
            kbAccountIdString = req.getPathInfo().substring(1, req.getPathInfo().length());
        } catch (final StringIndexOutOfBoundsException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Badly formed kb account id in request: " + req.getPathInfo());
            return null;
        }

        final UUID kbAccountId;
        try {
            kbAccountId = UUID.fromString(kbAccountIdString);
        } catch (final IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid UUID for kb account id: " + kbAccountIdString);
            return null;
        }

        return kbAccountId;
    }

    private static final class AnalyticsApiCallContext implements CallContext {

        private final String createdBy;
        private final String reason;
        private final String comment;
        private final UUID tenantId;
        private final DateTime now;

        private AnalyticsApiCallContext(final String createdBy,
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
}
