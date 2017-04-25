/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2016 Groupon, Inc
 * Copyright 2014-2016 The Billing Project, LLC
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
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.killbill.billing.plugin.analytics.api.user.AnalyticsUserApi;
import org.killbill.billing.plugin.analytics.json.CSVNamedXYTimeSeries;
import org.killbill.billing.plugin.analytics.json.Chart;
import org.killbill.billing.plugin.analytics.json.DataMarker;
import org.killbill.billing.plugin.analytics.json.NamedXYTimeSeries;
import org.killbill.billing.plugin.analytics.json.ReportConfigurationJson;
import org.killbill.billing.plugin.analytics.json.TableDataSeries;
import org.killbill.billing.plugin.analytics.json.XY;
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;
import org.killbill.billing.plugin.analytics.reports.analysis.Smoother;
import org.killbill.billing.plugin.analytics.reports.analysis.Smoother.SmootherType;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.osgi.service.log.LogService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

// Handle /plugins/killbill-analytics/reports/<reportName>
public class ReportsServlet extends BaseServlet {

    private static final String CSV_DATA_FORMAT = "csv";
    private static final String JSON_DATA_FORMAT = "json";

    @VisibleForTesting
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = ISODateTimeFormat.basicDateTime();

    private static final String REPORTS_QUERY_NAME = "name";
    private static final String REPORTS_QUERY_START_DATE = "startDate";
    private static final String REPORTS_QUERY_END_DATE = "endDate";
    private static final String REPORTS_SMOOTHER_NAME = "smooth";
    private static final String REPORTS_DATA_FORMAT = "format";
    private static final String REPORT_QUERY_SQL_ONLY = "sqlOnly";

    public ReportsServlet(final AnalyticsUserApi analyticsUserApi, final ReportsUserApi reportsUserApi, final LogService logService) {
        super(analyticsUserApi, reportsUserApi, logService);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final TenantContext context = createCallContext(req, resp);

        final String reportName = (String) req.getAttribute(REPORT_NAME_ATTRIBUTE);
        if (reportName != null) {
            final ReportConfigurationJson reportConfigurationJson;
            try {
                reportConfigurationJson = reportsUserApi.getReportConfiguration(reportName, context);
            } catch (SQLException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
                return;
            }

            if (reportConfigurationJson == null) {
                resp.sendError(404, reportName + "");
            }
            resp.getOutputStream().write(jsonMapper.writeValueAsBytes(reportConfigurationJson));
            resp.setContentType("application/json");
        } else {
            doHandleReports(req, resp, context);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final CallContext context = createCallContext(req, resp);

        final ReportConfigurationJson reportConfigurationJson = jsonMapper.readValue(req.getInputStream(), ReportConfigurationJson.class);

        final ReportConfigurationJson existingReportConfiguration;
        try {
            existingReportConfiguration = reportsUserApi.getReportConfiguration(reportConfigurationJson.getReportName(), context);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
            return;
        }

        if (existingReportConfiguration == null) {
            reportsUserApi.createReport(reportConfigurationJson, context);
            resp.setStatus(201);
        } else {
            resp.setStatus(409);
        }

        resp.setHeader("Location", "/plugins/killbill-analytics/reports/" + reportConfigurationJson.getReportName());
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final CallContext context = createCallContext(req, resp);

        final String reportName = (String) req.getAttribute(REPORT_NAME_ATTRIBUTE);
        if (reportName == null) {
            return;
        }

        if ((Boolean) req.getAttribute(SHOULD_REFRESH)) {
            reportsUserApi.refreshReport(reportName, context);
        } else {
            final ReportConfigurationJson reportConfigurationJson = jsonMapper.readValue(req.getInputStream(), ReportConfigurationJson.class);
            reportsUserApi.updateReport(reportName, reportConfigurationJson, context);
        }
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final CallContext context = createCallContext(req, resp);

        final String reportName = (String) req.getAttribute(REPORT_NAME_ATTRIBUTE);
        if (reportName == null) {
            reportsUserApi.clearCaches(context);
        } else {
            reportsUserApi.deleteReport(reportName, context);
        }
    }

    private void doHandleReports(final HttpServletRequest req, final HttpServletResponse resp, final TenantContext context) throws ServletException, IOException {
        final String[] rawReportNames = req.getParameterValues(REPORTS_QUERY_NAME);
        if (rawReportNames == null || rawReportNames.length == 0) {
            listReports(req, resp, context);
            return;
        }

        DateTime startDate;
        if (Strings.emptyToNull(req.getParameter(REPORTS_QUERY_START_DATE)) != null) {
            try {
                startDate = DATE_FORMAT.parseLocalDate(req.getParameter(REPORTS_QUERY_START_DATE)).toDateTimeAtStartOfDay(DateTimeZone.UTC);
            } catch (final IllegalArgumentException e) {
                startDate = DATE_TIME_FORMAT.parseDateTime(req.getParameter(REPORTS_QUERY_START_DATE));
            }
        } else {
            startDate = null;
        }
        DateTime endDate;
        if (Strings.emptyToNull(req.getParameter(REPORTS_QUERY_END_DATE)) != null) {
            try {
                endDate = DATE_FORMAT.parseLocalDate(req.getParameter(REPORTS_QUERY_END_DATE)).toDateTimeAtStartOfDay(DateTimeZone.UTC);
            } catch (final IllegalArgumentException e) {
                endDate = DATE_TIME_FORMAT.parseDateTime(req.getParameter(REPORTS_QUERY_END_DATE));
            }
        } else {
            endDate = null;
        }

        final boolean sqlOnly = req.getParameter(REPORT_QUERY_SQL_ONLY) != null;

        if (sqlOnly) {
            for (final String sql : reportsUserApi.getSQLForReport(rawReportNames, startDate, endDate, context)) {
                resp.getOutputStream().write(("\n" + sql + "\n").getBytes(Charset.forName("UTF-8")));
            }
            resp.setContentType("text/plain");
        } else {
            final SmootherType smootherType = Smoother.fromString(Strings.emptyToNull(req.getParameter(REPORTS_SMOOTHER_NAME)));

            // TODO PIERRE Switch to an equivalent of StreamingOutputStream?
            final List<Chart> results = reportsUserApi.getDataForReport(rawReportNames, startDate, endDate, smootherType, context);

            final String format = MoreObjects.firstNonNull(Strings.emptyToNull(req.getParameter(REPORTS_DATA_FORMAT)), JSON_DATA_FORMAT);
            if (CSV_DATA_FORMAT.equals(format)) {
                final OutputStream out = resp.getOutputStream();
                writeAsCSV(results, out);
                resp.setContentType("text/csv");
            } else {
                resp.getOutputStream().write(jsonMapper.writeValueAsBytes(results));
                resp.setContentType("application/json");
            }
        }
    }

    private void listReports(final HttpServletRequest req, final HttpServletResponse resp, final TenantContext context) throws ServletException, IOException {
        final List<ReportConfigurationJson> reports = reportsUserApi.getReports(context);
        resp.getOutputStream().write(jsonMapper.writeValueAsBytes(reports));
        resp.setContentType("application/json");
    }

    static void writeAsCSV(final List<Chart> charts, final OutputStream out) throws IOException {
        for (Chart cur : charts) {
            switch (cur.getType()) {
                case TIMELINE:
                    for (final DataMarker marker : cur.getData()) {
                        final NamedXYTimeSeries namedXYTimeSeries = (NamedXYTimeSeries) marker;
                        for (final XY value : namedXYTimeSeries.getValues()) {
                            out.write(csvMapper.writeValueAsBytes(new CSVNamedXYTimeSeries(namedXYTimeSeries.getName(), value)));
                        }
                    }
                    break;
                case COUNTERS:
                    break;
                case TABLE:
                    for (final DataMarker marker : cur.getData()) {
                        final TableDataSeries tableDataSeries = (TableDataSeries) marker;
                        out.write(csvMapper.writeValueAsBytes(tableDataSeries.getHeader()));
                        for (final List<Object> row : tableDataSeries.getValues()) {
                            out.write(csvMapper.writeValueAsBytes(row));
                        }
                    }
                    break;
            }
        }
    }
}
