/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2021 Equinix, Inc
 * Copyright 2014-2021 The Billing Project, LLC
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Body;
import org.jooby.mvc.Consumes;
import org.jooby.mvc.DELETE;
import org.jooby.mvc.GET;
import org.jooby.mvc.Header;
import org.jooby.mvc.Local;
import org.jooby.mvc.POST;
import org.jooby.mvc.PUT;
import org.jooby.mvc.Path;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillClock;
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
import org.killbill.billing.plugin.api.PluginTenantContext;
import org.killbill.billing.plugin.core.resources.ExceptionResponse;
import org.killbill.billing.tenant.api.Tenant;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Singleton
// Handle /plugins/killbill-analytics/reports
@Path("/reports")
public class ReportsResource extends BaseResource {

    @VisibleForTesting
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final ObjectWriter csvMapper = ObjectMapperProvider.getCsvWriter();

    private static final String CSV_DATA_FORMAT = "csv";
    private static final String JSON_DATA_FORMAT = "json";
    private static final DateTimeFormatter DATE_TIME_FORMAT = ISODateTimeFormat.basicDateTime();

    private static final String REPORTS_QUERY_NAME = "name";
    private static final String REPORTS_QUERY_START_DATE = "startDate";
    private static final String REPORTS_QUERY_END_DATE = "endDate";
    private static final String REPORTS_SMOOTHER_NAME = "smooth";
    private static final String REPORTS_DATA_FORMAT = "format";
    private static final String REPORT_QUERY_SQL_ONLY = "sqlOnly";

    @Inject
    public ReportsResource(final AnalyticsUserApi analyticsUserApi, final ReportsUserApi reportsUserApi, final OSGIKillbillClock osgiKillbillClock) {
        super(analyticsUserApi, reportsUserApi, osgiKillbillClock);
    }

    @VisibleForTesting
    static void writeAsCSV(final Iterable<Chart> charts, final OutputStream out) throws IOException {
        for (final Chart cur : charts) {
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
                            // Workaround for https://github.com/FasterXML/jackson-dataformats-text/issues/10
                            final List<Object> withoutNulls = Lists.<Object, Object>transform(row, new Function<Object, Object>() {
                                @Override
                                public Object apply(final Object input) {
                                    return (input == null) ? "" : input;
                                }
                            });
                            out.write(csvMapper.writeValueAsBytes(withoutNulls));
                        }
                    }
                    break;
            }
        }
    }

    @GET
    public Result doGet(@Named(REPORTS_QUERY_NAME) final Optional<List<String>> rawReportNames,
                        @Named(REPORTS_QUERY_START_DATE) final Optional<String> startDateStr,
                        @Named(REPORTS_QUERY_END_DATE) final Optional<String> endDateStr,
                        @Named(REPORTS_SMOOTHER_NAME) final Optional<String> smoother,
                        @Named(REPORTS_DATA_FORMAT) final Optional<String> formatter,
                        @Named(REPORT_QUERY_SQL_ONLY) final Optional<Boolean> sqlOnly,
                        @Local @Named("killbill_tenant") final Tenant tenant) throws IOException {
        final TenantContext context = new PluginTenantContext(null, tenant.getId());

        if (!rawReportNames.isPresent()) {
            final List<ReportConfigurationJson> reports = reportsUserApi.getReports(context);
            return Results.with(reports, Status.OK).header("Content-Type", "application/json");
        }

        // TODO I'm sure Jooby could do that for us?
        DateTime startDate;
        if (startDateStr.isPresent() && !startDateStr.get().isEmpty()) {
            try {
                startDate = DATE_FORMAT.parseLocalDate(startDateStr.get()).toDateTimeAtStartOfDay(DateTimeZone.UTC);
            } catch (final IllegalArgumentException e) {
                startDate = DATE_TIME_FORMAT.parseDateTime(startDateStr.get());
            }
        } else {
            startDate = null;
        }
        DateTime endDate;
        if (endDateStr.isPresent() && !endDateStr.get().isEmpty()) {
            try {
                endDate = DATE_FORMAT.parseLocalDate(endDateStr.get()).toDateTimeAtStartOfDay(DateTimeZone.UTC);
            } catch (final IllegalArgumentException e) {
                endDate = DATE_TIME_FORMAT.parseDateTime(endDateStr.get());
            }
        } else {
            endDate = null;
        }

        if (sqlOnly.orElse(false)) {
            final OutputStream out = new ByteArrayOutputStream();
            for (final String sql : reportsUserApi.getSQLForReport(rawReportNames.get(),
                                                                   startDate,
                                                                   endDate,
                                                                   context)) {
            	out.write(("\n" + sql + "\n").getBytes(Charset.forName("UTF-8")));
            }
            return Results.with(out, Status.OK).header("Content-Type", "text/plain");
        } else {
            final SmootherType smootherType = Smoother.fromString(smoother.orElse(null));

            // TODO PIERRE Switch to an equivalent of StreamingOutputStream?
            final List<Chart> results = reportsUserApi.getDataForReport(rawReportNames.get(),
                                                                        startDate,
                                                                        endDate,
                                                                        smootherType,
                                                                        context);

            final String format = formatter.orElse(JSON_DATA_FORMAT);
            if (CSV_DATA_FORMAT.equals(format)) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                writeAsCSV(results, out);
                return Results.with(out.toString(StandardCharsets.UTF_8.name()), Status.OK).header("Content-Type", "text/csv; charset=utf-8");
            } else {
                return Results.with(results, Status.OK).header("Content-Type", "application/json");
            }
        }
    }

    @GET
    @Path("/{reportName}")
    public Result doGet(@Named("reportName") final String reportName,
                        @Local @Named("killbill_tenant") final Tenant tenant) {
        final TenantContext context = new PluginTenantContext(null, tenant.getId());

        final ReportConfigurationJson reportConfigurationJson;
        try {
            reportConfigurationJson = reportsUserApi.getReportConfiguration(reportName, context);
        } catch (final SQLException e) {
            return Results.with(new ExceptionResponse(e, true), Status.SERVER_ERROR);
        }

        if (reportConfigurationJson == null) {
            return Results.with(Status.NOT_FOUND);
        } else {
            return Results.with(reportConfigurationJson, Status.OK);
        }
    }

    @POST
    @Consumes("application/json")
    public Result doPost(@Body final ReportConfigurationJson reportConfigurationJson,
                         @Header(HDR_CREATED_BY) final Optional<String> createdBy,
                         @Header(HDR_REASON) final Optional<String> reason,
                         @Header(HDR_COMMENT) final Optional<String> comment,
                         @Local @Named("killbill_tenant") final Tenant tenant) {
        final CallContext context = createCallContext(createdBy, reason, comment, null, tenant);

        final ReportConfigurationJson existingReportConfiguration;
        try {
            existingReportConfiguration = reportsUserApi.getReportConfiguration(reportConfigurationJson.getReportName(), context);
        } catch (final SQLException e) {
            return Results.with(new ExceptionResponse(e, true), Status.SERVER_ERROR);
        }

        final String location = "/plugins/killbill-analytics/reports/" + reportConfigurationJson.getReportName();
        if (existingReportConfiguration == null) {
            reportsUserApi.createReport(reportConfigurationJson, context);
            return Results.with(Status.CREATED).header("Location", location);
        } else {
            return Results.with(Status.CONFLICT).header("Location", location);
        }
    }

    @PUT
    @Path("/{reportName}")
    @Consumes("application/json")
    public Result doPut(@Named("reportName") final String reportName,
                        @Named("shouldRefresh") final Optional<Boolean> shouldRefresh,
                        @Body final Optional<ReportConfigurationJson> reportConfigurationJson,
                        @Header(HDR_CREATED_BY) final Optional<String> createdBy,
                        @Header(HDR_REASON) final Optional<String> reason,
                        @Header(HDR_COMMENT) final Optional<String> comment,
                        @Local @Named("killbill_tenant") final Tenant tenant) {
        final CallContext context = createCallContext(createdBy, reason, comment, null, tenant);

        if (Boolean.TRUE.equals(shouldRefresh.orElse(Boolean.FALSE))) {
            reportsUserApi.refreshReport(reportName, context);
        } else {
            reportsUserApi.updateReport(reportName, reportConfigurationJson.orElse(null), context);
        }

        return Results.ok();
    }

    @DELETE
    public Result doDelete(@Header(HDR_CREATED_BY) final Optional<String> createdBy,
                           @Header(HDR_REASON) final Optional<String> reason,
                           @Header(HDR_COMMENT) final Optional<String> comment,
                           @Local @Named("killbill_tenant") final Tenant tenant) {
        final CallContext context = createCallContext(createdBy, reason, comment, null, tenant);

        reportsUserApi.clearCaches(context);

        return Results.ok();
    }

    @DELETE
    @Path("/{reportName}")
    public Result doDelete(@Named("reportName") final String reportName,
                           @Header(HDR_CREATED_BY) final Optional<String> createdBy,
                           @Header(HDR_REASON) final Optional<String> reason,
                           @Header(HDR_COMMENT) final Optional<String> comment,
                           @Local @Named("killbill_tenant") final Tenant tenant) {
        final CallContext context = createCallContext(createdBy, reason, comment, null, tenant);

        reportsUserApi.deleteReport(reportName, context);

        return Results.ok();
    }
}
