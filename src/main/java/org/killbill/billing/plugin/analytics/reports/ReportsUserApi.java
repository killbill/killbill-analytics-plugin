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

package org.killbill.billing.plugin.analytics.reports;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.killbill.billing.ObjectType;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.plugin.analytics.BusinessExecutor;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfigurationHandler;
import org.killbill.billing.plugin.analytics.dao.BusinessDBIProvider;
import org.killbill.billing.plugin.analytics.json.Chart;
import org.killbill.billing.plugin.analytics.json.CounterChart;
import org.killbill.billing.plugin.analytics.json.DataMarker;
import org.killbill.billing.plugin.analytics.json.NamedXYTimeSeries;
import org.killbill.billing.plugin.analytics.json.ReportConfigurationJson;
import org.killbill.billing.plugin.analytics.json.TableDataSeries;
import org.killbill.billing.plugin.analytics.json.XY;
import org.killbill.billing.plugin.analytics.reports.analysis.Smoother;
import org.killbill.billing.plugin.analytics.reports.analysis.Smoother.SmootherType;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.ReportType;
import org.killbill.billing.plugin.analytics.reports.scheduler.JobsScheduler;
import org.killbill.billing.plugin.analytics.reports.sql.Metadata;
import org.killbill.billing.plugin.dao.PluginDao.DBEngine;
import org.killbill.billing.util.api.RecordIdApi;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

public class ReportsUserApi {

    private static final Logger logger = LoggerFactory.getLogger(ReportsUserApi.class);

    private static final String ANALYTICS_REPORTS_NB_THREADS_PROPERTY = "org.killbill.billing.plugin.analytics.dashboard.nbThreads";

    // Part of the public API
    public static final String DAY_COLUMN_NAME = "day";
    public static final String TS_COLUMN_NAME = "ts";
    public static final String LABEL = "label";
    public static final String COUNT_COLUMN_NAME = "count";

    private final OSGIKillbillAPI killbillAPI;
    private final QueryEngine queryEngine;
    private final DBEngine dbEngine;
    private final ExecutorService dbiThreadsExecutor;
    private final ReportsConfiguration reportsConfiguration;
    private final JobsScheduler jobsScheduler;
    private final Metadata sqlMetadata;

    public ReportsUserApi(final OSGIKillbillAPI killbillAPI,
                          final OSGIKillbillDataSource osgiKillbillDataSource,
                          final OSGIConfigPropertiesService osgiConfigPropertiesService,
                          final DBEngine dbEngine,
                          final ReportsConfiguration reportsConfiguration,
                          final JobsScheduler jobsScheduler,
                          final AnalyticsConfigurationHandler analyticsConfigurationHandler) {
        this.killbillAPI = killbillAPI;
        this.dbEngine = dbEngine;
        this.reportsConfiguration = reportsConfiguration;
        this.jobsScheduler = jobsScheduler;
        queryEngine = new QueryEngine(BusinessDBIProvider.get(osgiKillbillDataSource.getDataSource()), analyticsConfigurationHandler);

        final String nbThreadsMaybeNull = Strings.emptyToNull(osgiConfigPropertiesService.getString(ANALYTICS_REPORTS_NB_THREADS_PROPERTY));
        this.dbiThreadsExecutor = BusinessExecutor.newCachedThreadPool(nbThreadsMaybeNull == null ? Integer.valueOf(10) : Integer.valueOf(nbThreadsMaybeNull), "osgi-analytics-dashboard");

        this.sqlMetadata = new Metadata(Sets.<String>newHashSet(Iterables.transform(reportsConfiguration.getAllReportConfigurations(null).values(),
                                                                                    new Function<ReportsConfigurationModelDao, String>() {
                                                                                        @Override
                                                                                        public String apply(final ReportsConfigurationModelDao reportConfiguration) {
                                                                                            return reportConfiguration == null ? null : reportConfiguration.getSourceTableName();
                                                                                        }
                                                                                    }
                                                                                   )),
                                        osgiKillbillDataSource.getDataSource());
    }

    public void shutdownNow() {
        dbiThreadsExecutor.shutdownNow();
    }

    // TODO Cache per tenant
    public void clearCaches(final CallContext context) {
        sqlMetadata.clearCaches();
    }

    public ReportConfigurationJson getReportConfiguration(final String reportName, final TenantContext context) throws SQLException {
        final Long tenantRecordId = getTenantRecordId(context);
        final ReportsConfigurationModelDao reportsConfigurationModelDao = reportsConfiguration.getReportConfigurationForReport(reportName, tenantRecordId);
        if (reportsConfigurationModelDao != null) {
            return new ReportConfigurationJson(reportsConfigurationModelDao, sqlMetadata.getTable(reportsConfigurationModelDao.getSourceTableName()));
        } else {
            return null;
        }
    }

    public void createReport(final ReportConfigurationJson reportConfigurationJson, final CallContext context) {
        final Long tenantRecordId = getTenantRecordId(context);
        final ReportsConfigurationModelDao reportsConfigurationModelDao = new ReportsConfigurationModelDao(reportConfigurationJson);
        reportsConfiguration.createReportConfiguration(reportsConfigurationModelDao, tenantRecordId);
    }

    public void updateReport(final String reportName, final ReportConfigurationJson reportConfigurationJson, final CallContext context) {
        final Long tenantRecordId = getTenantRecordId(context);
        final ReportsConfigurationModelDao currentReportsConfigurationModelDao = reportsConfiguration.getReportConfigurationForReport(reportName, tenantRecordId);
        final ReportsConfigurationModelDao reportsConfigurationModelDao = new ReportsConfigurationModelDao(reportConfigurationJson, currentReportsConfigurationModelDao);
        reportsConfiguration.updateReportConfiguration(reportsConfigurationModelDao, tenantRecordId);
    }

    public void deleteReport(final String reportName, final CallContext context) {
        final Long tenantRecordId = getTenantRecordId(context);
        reportsConfiguration.deleteReportConfiguration(reportName, tenantRecordId);
    }

    public void refreshReport(final String reportName, final CallContext context) {
        final Long tenantRecordId = getTenantRecordId(context);
        final ReportsConfigurationModelDao reportsConfigurationModelDao = reportsConfiguration.getReportConfigurationForReport(reportName, tenantRecordId);
        jobsScheduler.scheduleNow(reportsConfigurationModelDao);
    }

    public List<ReportConfigurationJson> getReports(final TenantContext context) {
        final Long tenantRecordId = getTenantRecordId(context);
        final List<ReportsConfigurationModelDao> reports = Ordering.natural()
                                                                   .nullsLast()
                                                                   .onResultOf(new Function<ReportsConfigurationModelDao, String>() {
                                                                       @Override
                                                                       public String apply(final ReportsConfigurationModelDao input) {
                                                                           return input == null ? null : input.getReportPrettyName();
                                                                       }
                                                                   })
                                                                   .immutableSortedCopy(reportsConfiguration.getAllReportConfigurations(tenantRecordId).values());

        return Lists.<ReportsConfigurationModelDao, ReportConfigurationJson>transform(reports,
                                                                                      new Function<ReportsConfigurationModelDao, ReportConfigurationJson>() {
                                                                                          @Override
                                                                                          public ReportConfigurationJson apply(final ReportsConfigurationModelDao input) {
                                                                                              try {
                                                                                                  return input == null ? null : new ReportConfigurationJson(input, sqlMetadata.getTable(input.getSourceTableName()));
                                                                                              } catch (final SQLException e) {
                                                                                                  throw new RuntimeException(e);
                                                                                              }
                                                                                          }
                                                                                      }
                                                                                     );
    }

    // Useful for testing
    public List<String> getSQLForReport(final Iterable<String> rawReportNames,
                                        @Nullable final DateTime startDate,
                                        @Nullable final DateTime endDate,
                                        final TenantContext context) {
        final Long tenantRecordId = getTenantRecordId(context);

        final List<String> sqlQueries = new LinkedList<String>();
        for (final String rawReportName : rawReportNames) {
            final ReportSpecification reportSpecification = new ReportSpecification(rawReportName);
            final ReportsConfigurationModelDao reportConfigurationForReport = reportsConfiguration.getReportConfigurationForReport(reportSpecification.getReportName(), tenantRecordId);
            if (reportConfigurationForReport != null) {
                if (reportConfigurationForReport.getSourceTableName() != null) {
                    final SqlReportDataExtractor sqlReportDataExtractor = new SqlReportDataExtractor(reportConfigurationForReport.getSourceTableName(),
                                                                                                     reportSpecification,
                                                                                                     startDate,
                                                                                                     endDate,
                                                                                                     dbEngine,
                                                                                                     tenantRecordId);

                    sqlQueries.add(sqlReportDataExtractor.toString());
                } else {
                    final SqlReportDataExtractor sqlReportDataExtractor = new SqlReportDataExtractor(reportConfigurationForReport.getSourceQuery(),
                                                                                                     startDate,
                                                                                                     endDate,
                                                                                                     tenantRecordId);
                    sqlQueries.add(sqlReportDataExtractor.toString());
                }
            }
        }

        return sqlQueries;
    }

    public List<Chart> getDataForReport(final Iterable<String> rawReportNames,
                                        @Nullable final DateTime startDate,
                                        @Nullable final DateTime endDate,
                                        @Nullable final SmootherType smootherType,
                                        final TenantContext context) {
        final Long tenantRecordId = getTenantRecordId(context);

        final List<Chart> result = new LinkedList<Chart>();
        final Map<String, Map<String, List<XY>>> timeSeriesData = new ConcurrentHashMap<String, Map<String, List<XY>>>();

        // Parse the reports
        final List<ReportSpecification> reportSpecifications = new ArrayList<ReportSpecification>();
        for (final String rawReportName : rawReportNames) {
            reportSpecifications.add(new ReportSpecification(rawReportName));
        }

        // Fetch the latest reports configurations
        final Map<String, ReportsConfigurationModelDao> reportsConfigurations = reportsConfiguration.getAllReportConfigurations(tenantRecordId);

        final List<Future> jobs = new LinkedList<Future>();
        for (final ReportSpecification reportSpecification : reportSpecifications) {
            final String reportName = reportSpecification.getReportName();
            final ReportsConfigurationModelDao reportConfiguration = getReportConfiguration(reportName, reportsConfigurations);
            final String tableName = reportConfiguration.getSourceTableName();
            final String prettyName = reportConfiguration.getReportPrettyName();
            final ReportType reportType = reportConfiguration.getReportType();

            jobs.add(dbiThreadsExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    switch (reportType) {
                        case COUNTERS:
                            List<DataMarker> counters = queryEngine.getCountersData(reportConfiguration, startDate, endDate, tenantRecordId, context.getTenantId());
                            result.add(new Chart(ReportType.COUNTERS, prettyName, counters));
                            break;

                        case TIMELINE:
                            final Map<String, List<XY>> data = queryEngine.getTimeSeriesData(reportConfiguration, reportSpecification, dbEngine, startDate, endDate, tenantRecordId, context.getTenantId());
                            timeSeriesData.put(reportName, data);
                            break;

                        case TABLE:
                            List<DataMarker> tables = queryEngine.getTablesData(reportConfiguration, startDate, endDate, tenantRecordId, context.getTenantId());
                            result.add(new Chart(ReportType.TABLE, prettyName, tables));
                            break;

                        default:
                            throw new RuntimeException("Unknown reportType " + reportType);
                    }
                }
            }));
        }
        waitForJobCompletion(jobs);

        //
        // Normalization and smoothing of time series if needed
        //
        if (!timeSeriesData.isEmpty()) {
            normalizeAndSortXValues(timeSeriesData, startDate, endDate);
            if (smootherType != null) {
                final Smoother smoother = smootherType.createSmoother(timeSeriesData);
                smoother.smooth();
                result.addAll(buildNamedXYTimeSeries(smoother.getDataForReports(), reportsConfigurations));
            } else {
                result.addAll(buildNamedXYTimeSeries(timeSeriesData, reportsConfigurations));
            }
        }

        return result;
    }

    private List<Chart> buildNamedXYTimeSeries(final Map<String, Map<String, List<XY>>> dataForReports, final Map<String, ReportsConfigurationModelDao> reportsConfigurations) {
        final List<Chart> results = new LinkedList<Chart>();
        final List<DataMarker> timeSeries = new LinkedList<DataMarker>();
        for (final Entry<String, Map<String, List<XY>>> entry : dataForReports.entrySet()) {
            final ReportsConfigurationModelDao reportConfiguration = getReportConfiguration(entry.getKey(), reportsConfigurations);

            // Sort the pivots by name for a consistent display in the dashboard
            for (final String timeSeriesName : Ordering.natural().sortedCopy(entry.getValue().keySet())) {
                final List<XY> dataForReport = entry.getValue().get(timeSeriesName);
                timeSeries.add(new NamedXYTimeSeries(timeSeriesName, dataForReport));
            }
            results.add(new Chart(ReportType.TIMELINE, reportConfiguration.getReportPrettyName(), timeSeries));
        }
        return results;
    }

    // TODO PIERRE Naive implementation
    private void normalizeAndSortXValues(final Map<String, Map<String, List<XY>>> dataForReports, @Nullable final DateTime startDate, @Nullable final DateTime endDate) {
        DateTime minDate = null;
        if (startDate != null) {
            minDate = startDate;
        }

        DateTime maxDate = null;
        if (endDate != null) {
            maxDate = endDate;
        }

        // If no min and/or max was specified, infer them from the data
        if (minDate == null || maxDate == null) {
            for (final Map<String, List<XY>> dataForReport : dataForReports.values()) {
                for (final List<XY> dataForPivot : dataForReport.values()) {
                    for (final XY xy : dataForPivot) {
                        if (minDate == null || xy.getxDate().isBefore(minDate)) {
                            minDate = xy.getxDate();
                        }
                        if (maxDate == null || xy.getxDate().isAfter(maxDate)) {
                            maxDate = xy.getxDate();
                        }
                    }
                }
            }
        }

        if (minDate == null || maxDate == null) {
            throw new IllegalStateException(String.format("minDate and maxDate shouldn't be null! minDate=%s, maxDate=%s, dataForReports=%s", minDate, maxDate, dataForReports));
        }

        // Add 0 for missing days
        DateTime curDate = minDate;
        while (!curDate.isAfter(maxDate)) {
            for (final Map<String, List<XY>> dataForReport : dataForReports.values()) {
                for (final List<XY> dataForPivot : dataForReport.values()) {
                    addMissingValueForDateIfNeeded(curDate, dataForPivot);
                }
            }
            curDate = curDate.plusDays(1);
        }

        // Sort the data for the dashboard
        for (final Entry<String, Map<String, List<XY>>> entry : dataForReports.entrySet()) {
            for (final Entry<String, List<XY>> subEntry : entry.getValue().entrySet()) {
                Collections.sort(subEntry.getValue(),
                                 new Comparator<XY>() {
                                     @Override
                                     public int compare(final XY o1, final XY o2) {
                                         return o1.getxDate().compareTo(o2.getxDate());
                                     }
                                 }
                                );
            }
        }
    }

    private void addMissingValueForDateIfNeeded(final DateTime curDate, final List<XY> dataForPivot) {
        final XY valueForCurrentDate = Iterables.tryFind(dataForPivot, new Predicate<XY>() {
            @Override
            public boolean apply(final XY xy) {
                return xy != null && xy.getxDate().compareTo(curDate) == 0;
            }
        }).orNull();

        if (valueForCurrentDate == null) {
            dataForPivot.add(new XY(curDate, (float) 0));
        }
    }

    private ReportsConfigurationModelDao getReportConfiguration(final String reportName, final Map<String, ReportsConfigurationModelDao> reportsConfigurations) {
        final ReportsConfigurationModelDao reportConfiguration = reportsConfigurations.get(reportName);
        if (reportConfiguration == null) {
            throw new IllegalArgumentException("Report " + reportName + " is not configured");
        }
        return reportConfiguration;
    }

    private void waitForJobCompletion(final List<Future> jobs) {
        for (final Future job : jobs) {
            try {
                job.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Long getTenantRecordId(final TenantContext context) {
        // See convention in InternalCallContextFactory
        if (context.getTenantId() == null) {
            return 0L;
        } else {
            final RecordIdApi recordIdApi = killbillAPI.getRecordIdApi();
            return recordIdApi.getRecordId(context.getTenantId(), ObjectType.TENANT, context);
        }
    }
}
