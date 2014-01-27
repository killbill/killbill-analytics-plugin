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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.log.LogService;

import com.ning.billing.osgi.bundles.analytics.api.user.AnalyticsUserApi;
import com.ning.billing.osgi.bundles.analytics.json.CSVNamedXYTimeSeries;
import com.ning.billing.osgi.bundles.analytics.json.Chart;
import com.ning.billing.osgi.bundles.analytics.json.DataMarker;
import com.ning.billing.osgi.bundles.analytics.json.NamedXYTimeSeries;
import com.ning.billing.osgi.bundles.analytics.json.ReportConfigurationJson;
import com.ning.billing.osgi.bundles.analytics.json.XY;
import com.ning.billing.osgi.bundles.analytics.reports.ReportsUserApi;
import com.ning.billing.osgi.bundles.analytics.reports.analysis.Smoother;
import com.ning.billing.osgi.bundles.analytics.reports.analysis.Smoother.SmootherType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

// Handle /plugins/killbill-analytics/reports/<reportName>
public class ReportsServlet extends BaseServlet {

    private final Pattern REPORT_REFRESH_PATTERN = Pattern.compile("(" + STRING_PATTERN + ")/refresh");

    private static final String CSV_DATA_FORMAT = "csv";
    private static final String JSON_DATA_FORMAT = "json";

    @VisibleForTesting
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final String REPORTS_QUERY_NAME = "name";
    private static final String REPORTS_QUERY_START_DATE = "startDate";
    private static final String REPORTS_QUERY_END_DATE = "endDate";
    private static final String REPORTS_SMOOTHER_NAME = "smooth";
    private static final String REPORTS_DATA_FORMAT = "format";

    public ReportsServlet(final AnalyticsUserApi analyticsUserApi, final ReportsUserApi reportsUserApi, final LogService logService) {
        super(analyticsUserApi, reportsUserApi, logService);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String reportName = (String) req.getAttribute(REPORT_NAME_ATTRIBUTE);
        if (reportName != null) {
            final ReportConfigurationJson reportConfigurationJson = reportsUserApi.getReportConfiguration(reportName);
            if (reportConfigurationJson == null) {
                resp.sendError(404, reportName + "");
            }
            resp.getOutputStream().write(jsonMapper.writeValueAsBytes(reportConfigurationJson));
            resp.setContentType("application/json");
        } else {
            doHandleReports(req, resp);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final ReportConfigurationJson reportConfigurationJson = jsonMapper.readValue(req.getInputStream(), ReportConfigurationJson.class);
        reportsUserApi.createReport(reportConfigurationJson);

        resp.setHeader("Location", "/plugins/killbill-analytics/reports/" + reportConfigurationJson.getReportName());
        resp.setStatus(201);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String reportName = (String) req.getAttribute(REPORT_NAME_ATTRIBUTE);
        if (reportName == null) {
            return;
        }

        final Matcher matcher = REPORT_REFRESH_PATTERN.matcher(reportName);
        if (matcher.matches()) {
            // reportName was actually reportName/refresh
            reportsUserApi.refreshReport(matcher.group(1));
        } else {
            final ReportConfigurationJson reportConfigurationJson = jsonMapper.readValue(req.getInputStream(), ReportConfigurationJson.class);
            reportsUserApi.updateReport(reportName, reportConfigurationJson);
        }
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String reportName = (String) req.getAttribute(REPORT_NAME_ATTRIBUTE);
        if (reportName == null) {
            return;
        }

        reportsUserApi.deleteReport(reportName);
    }

    private void doHandleReports(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String[] rawReportNames = req.getParameterValues(REPORTS_QUERY_NAME);
        if (rawReportNames == null || rawReportNames.length == 0) {
            listReports(req, resp);
            return;
        }

        final LocalDate startDate = Strings.emptyToNull(req.getParameter(REPORTS_QUERY_START_DATE)) != null ? DATE_FORMAT.parseLocalDate(req.getParameter(REPORTS_QUERY_START_DATE)) : null;
        final LocalDate endDate = Strings.emptyToNull(req.getParameter(REPORTS_QUERY_END_DATE)) != null ? DATE_FORMAT.parseLocalDate(req.getParameter(REPORTS_QUERY_END_DATE)) : null;

        final SmootherType smootherType = Smoother.fromString(Strings.emptyToNull(req.getParameter(REPORTS_SMOOTHER_NAME)));

        // TODO PIERRE Switch to an equivalent of StreamingOutputStream?
        final List<Chart> results = reportsUserApi.getDataForReport(rawReportNames, startDate, endDate, smootherType);

        final String format = Objects.firstNonNull(Strings.emptyToNull(req.getParameter(REPORTS_DATA_FORMAT)), JSON_DATA_FORMAT);
        if (CSV_DATA_FORMAT.equals(format)) {
            final OutputStream out = resp.getOutputStream();
            writeAsCSV(results, out);
            resp.setContentType("text/csv");
        } else {
            resp.getOutputStream().write(jsonMapper.writeValueAsBytes(results));
            resp.setContentType("application/json");
        }
        setCrossSiteScriptingHeaders(resp);
    }

    private void listReports(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final List<ReportConfigurationJson> reports = reportsUserApi.getReports();
        resp.getOutputStream().write(jsonMapper.writeValueAsBytes(reports));
        resp.setContentType("application/json");
        setCrossSiteScriptingHeaders(resp);
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
            }
        }
    }
}
