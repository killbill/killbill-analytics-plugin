/*
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfiguration;
import org.killbill.billing.plugin.analytics.json.CounterChart;
import org.killbill.billing.plugin.analytics.json.DataMarker;
import org.killbill.billing.plugin.analytics.json.TableDataSeries;
import org.killbill.billing.plugin.analytics.json.XY;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.billing.plugin.analytics.reports.databases.Trino;
import org.killbill.billing.plugin.dao.PluginDao.DBEngine;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.tweak.HandleCallback;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import static org.killbill.billing.plugin.analytics.reports.ReportsUserApi.COUNT_COLUMN_NAME;
import static org.killbill.billing.plugin.analytics.reports.ReportsUserApi.DAY_COLUMN_NAME;
import static org.killbill.billing.plugin.analytics.reports.ReportsUserApi.LABEL;
import static org.killbill.billing.plugin.analytics.reports.ReportsUserApi.TS_COLUMN_NAME;

public class QueryEngine {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S");

    private final DBI dbi;

    public QueryEngine(final DBI dbi) {
        this.dbi = dbi;
    }

    public List<DataMarker> getCountersData(final ReportsConfigurationModelDao reportsConfigurationModelDao,
                                            final ReportSpecification reportSpecification,
                                            final DBEngine kbDbEngine,
                                            @Nullable final DateTime startDate,
                                            @Nullable final DateTime endDate,
                                            final AnalyticsConfiguration analyticsConfiguration,
                                            final Long tenantRecordId) {
        final DBI dbi = getDBI(reportsConfigurationModelDao, analyticsConfiguration);
        final DBEngine dbEngine = getDbEngine(reportsConfigurationModelDao, kbDbEngine);

        final String query;
        if (reportsConfigurationModelDao.getSourceTableName() != null) {
            query = "select * from " + reportsConfigurationModelDao.getSourceTableName() + " where tenant_record_id = " + tenantRecordId;
        } else {
            final SqlReportDataExtractor sqlReportDataExtractor = new SqlReportDataExtractor(reportsConfigurationModelDao.getSourceQuery(),
                                                                                             reportSpecification,
                                                                                             startDate,
                                                                                             endDate,
                                                                                             analyticsConfiguration,
                                                                                             dbEngine,
                                                                                             tenantRecordId);
            query = sqlReportDataExtractor.toString();
        }
        return getCountersData(dbi, query);
    }

    public List<DataMarker> getTablesData(final ReportsConfigurationModelDao reportsConfigurationModelDao,
                                          final ReportSpecification reportSpecification,
                                          final DBEngine kbDbEngine,
                                          @Nullable final DateTime startDate,
                                          @Nullable final DateTime endDate,
                                          final AnalyticsConfiguration analyticsConfiguration,
                                          final Long tenantRecordId) {
        final DBI dbi = getDBI(reportsConfigurationModelDao, analyticsConfiguration);
        final DBEngine dbEngine = getDbEngine(reportsConfigurationModelDao, kbDbEngine);

        final String seriesName;
        final String fallBackHeadersQuery;
        final String query;
        if (reportsConfigurationModelDao.getSourceTableName() != null) {
            seriesName = reportsConfigurationModelDao.getSourceTableName();
            fallBackHeadersQuery = "select column_name from information_schema.columns where table_schema = schema() and table_name = '" + reportsConfigurationModelDao.getSourceTableName() + "' order by ordinal_position";
            query = "select * from " + reportsConfigurationModelDao.getSourceTableName() + " where tenant_record_id = " + tenantRecordId;
        } else {
            seriesName = reportsConfigurationModelDao.getSourceName();
            fallBackHeadersQuery = null;
            final SqlReportDataExtractor sqlReportDataExtractor = new SqlReportDataExtractor(reportsConfigurationModelDao.getSourceQuery(),
                                                                                             reportSpecification,
                                                                                             startDate,
                                                                                             endDate,
                                                                                             analyticsConfiguration,
                                                                                             dbEngine,
                                                                                             tenantRecordId);
            query = sqlReportDataExtractor.toString();
        }
        return getTablesData(dbi,
                             seriesName,
                             fallBackHeadersQuery,
                             query);
    }

    public Map<String, List<XY>> getTimeSeriesData(final ReportsConfigurationModelDao reportsConfigurationModelDao,
                                                   final ReportSpecification reportSpecification,
                                                   final DBEngine kbDbEngine,
                                                   @Nullable final DateTime startDate,
                                                   @Nullable final DateTime endDate,
                                                   final Long tenantRecordId,
                                                   final AnalyticsConfiguration analyticsConfiguration) {
        final DBI dbi = getDBI(reportsConfigurationModelDao, analyticsConfiguration);
        final DBEngine dbEngine = getDbEngine(reportsConfigurationModelDao, kbDbEngine);

        final String query;
        if (reportsConfigurationModelDao.getSourceTableName() != null) {
            final SqlReportDataExtractor sqlReportDataExtractor = new SqlReportDataExtractor(reportsConfigurationModelDao.getSourceTableName(),
                                                                                             reportSpecification,
                                                                                             startDate,
                                                                                             endDate,
                                                                                             dbEngine,
                                                                                             tenantRecordId);
            query = sqlReportDataExtractor.toString();
        } else {
            final SqlReportDataExtractor sqlReportDataExtractor = new SqlReportDataExtractor(reportsConfigurationModelDao.getSourceQuery(),
                                                                                             reportSpecification,
                                                                                             startDate,
                                                                                             endDate,
                                                                                             analyticsConfiguration,
                                                                                             dbEngine,
                                                                                             tenantRecordId);
            query = sqlReportDataExtractor.toString();
        }
        return getTimeSeriesData(dbi, reportSpecification, query);
    }

    private List<DataMarker> getCountersData(final IDBI dbi,
                                             final String query) {
        return dbi.withHandle(new HandleCallback<List<DataMarker>>() {
            @Override
            public List<DataMarker> withHandle(final Handle handle) {
                final List<Map<String, Object>> results = handle.select(query);
                if (results.isEmpty()) {
                    return Collections.emptyList();
                }

                final List<DataMarker> counters = new LinkedList<DataMarker>();
                for (final Map<String, Object> row : results) {
                    final Object labelObject = row.get(LABEL);
                    final Object countObject = row.get(COUNT_COLUMN_NAME);
                    if (labelObject == null || countObject == null) {
                        continue;
                    }

                    final String label = labelObject.toString();
                    final Float value = Float.valueOf(countObject.toString());

                    final DataMarker counter = new CounterChart(label, value);
                    counters.add(counter);
                }
                return counters;
            }
        });
    }

    private List<DataMarker> getTablesData(final IDBI dbi,
                                           final String seriesName,
                                           @Nullable final String fallBackHeadersQuery,
                                           final String query) {
        return dbi.withHandle(new HandleCallback<List<DataMarker>>() {
            @Override
            public List<DataMarker> withHandle(final Handle handle) {
                final List<Map<String, Object>> results = handle.select(query);
                if (results.isEmpty()) {
                    return Collections.emptyList();
                }

                final List<String> header = new LinkedList<String>();

                // Try to find the column names to keep the ordering
                final String headersQuery = String.format("%s limit 0", query);
                try (final Connection connection = handle.getConnection();
                     final Statement statement = connection.createStatement();
                     final ResultSet rs = statement.executeQuery(headersQuery)) {
                    final ResultSetMetaData rsmd = rs.getMetaData();
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        header.add(String.valueOf(rsmd.getColumnLabel(i)));
                    }
                } catch (final SQLException e) {
                    if (fallBackHeadersQuery != null) {
                        final List<Map<String, Object>> schemaResults = handle.select(fallBackHeadersQuery);
                        for (final Map<String, Object> row : schemaResults) {
                            header.add(String.valueOf(row.get("column_name")));
                        }
                    } else {
                        // TODO: how can we do better?
                        final Map<String, Object> firstRow = Iterators.getNext(results.iterator(), null);
                        assert firstRow != null;
                        header.addAll(firstRow.keySet());
                    }
                }

                final List<List<Object>> values = new LinkedList<List<Object>>();
                for (final Map<String, Object> row : results) {
                    final List<Object> tableRow = new LinkedList<Object>();
                    for (final String headerName : header) {
                        tableRow.add(row.get(headerName));
                    }
                    values.add(tableRow);
                }
                return ImmutableList.<DataMarker>of(new TableDataSeries(seriesName, header, values));
            }
        });
    }

    private Map<String, List<XY>> getTimeSeriesData(final IDBI dbi,
                                                    final ReportSpecification reportSpecification,
                                                    final String query) {
        return dbi.withHandle(new HandleCallback<Map<String, List<XY>>>() {
            @Override
            public Map<String, List<XY>> withHandle(final Handle handle) {
                final List<Map<String, Object>> results = handle.select(query);
                if (results.isEmpty()) {
                    return Collections.emptyMap();
                }

                final Map<String, List<XY>> timeSeries = new LinkedHashMap<String, List<XY>>();
                for (final Map<String, Object> row : results) {
                    // Day
                    Object dateObject = row.get(DAY_COLUMN_NAME);
                    if (dateObject == null) {
                        // Timestamp
                        dateObject = row.get(TS_COLUMN_NAME);
                        if (dateObject == null) {
                            continue;
                        }
                        dateObject = DATE_TIME_FORMATTER.parseDateTime(dateObject.toString()).toString();
                    }
                    final String date = dateObject.toString();

                    final String legendWithDimensions = createLegendWithDimensionsForSeries(row, reportSpecification);
                    for (final Entry<String, Object> entry : row.entrySet()) {
                        if (isMetric(entry.getKey(), reportSpecification)) {
                            // Create a unique name for that result set
                            final String seriesName = MoreObjects.firstNonNull(reportSpecification.getLegend(), entry.getKey()) + (legendWithDimensions == null ? "" : (": " + legendWithDimensions));
                            if (timeSeries.get(seriesName) == null) {
                                timeSeries.put(seriesName, new LinkedList<XY>());
                            }

                            final Object value = entry.getValue();
                            final Float valueAsFloat = value == null ? Float.valueOf(0f) : Float.valueOf(value.toString());
                            timeSeries.get(seriesName).add(new XY(date, valueAsFloat));
                        }
                    }
                }

                return timeSeries;
            }
        });
    }

    private String createLegendWithDimensionsForSeries(final Map<String, Object> row, final ReportSpecification reportSpecification) {
        int i = 0;
        final StringBuilder seriesNameBuilder = new StringBuilder();
        for (final Entry<String, Object> entry : row.entrySet()) {
            if (shouldUseColumnAsDimensionMultiplexer(entry.getKey(), reportSpecification)) {
                if (i > 0) {
                    seriesNameBuilder.append(" :: ");
                }
                seriesNameBuilder.append(entry.getValue() == null ? "NULL" : entry.getValue().toString());
                i++;
            }
        }

        if (i == 0) {
            return null;
        } else {
            return seriesNameBuilder.toString();
        }
    }

    private boolean shouldUseColumnAsDimensionMultiplexer(final String column, final ReportSpecification reportSpecification) {
        // Don't multiplex the day column
        if (DAY_COLUMN_NAME.equals(column)) {
            return false;
        }

        if (reportSpecification.getDimensions().isEmpty()) {
            if (reportSpecification.getMetrics().isEmpty()) {
                // If no dimension and metric are specified, assume all columns are dimensions except the "count" one
                return !COUNT_COLUMN_NAME.equals(column);
            } else {
                // Otherwise, all non-metric columns are dimensions
                return !reportSpecification.getMetrics().contains(column);
            }
        } else {
            return reportSpecification.getDimensions().contains(column);
        }
    }

    private boolean isMetric(final String column, final ReportSpecification reportSpecification) {
        if (reportSpecification.getMetrics().isEmpty()) {
            if (reportSpecification.getDimensions().isEmpty()) {
                // If no dimension and metric are specified, assume the only metric is the "count" one
                return COUNT_COLUMN_NAME.equals(column);
            } else {
                // Otherwise, all non-dimension columns are metrics
                return !DAY_COLUMN_NAME.equals(column) && !reportSpecification.getDimensions().contains(column);
            }
        } else {
            return reportSpecification.getMetrics().contains(column);
        }
    }

    private DBI getDBI(final ReportsConfigurationModelDao reportsConfigurationModelDao, final AnalyticsConfiguration analyticsConfiguration) {
        if (reportsConfigurationModelDao.getSourceName() != null) {
            Preconditions.checkArgument(reportsConfigurationModelDao.getSourceTableName() != null || reportsConfigurationModelDao.getSourceQuery() != null,
                                        "sourceTableName or sourceQuery must be defined: " + reportsConfigurationModelDao);
            Preconditions.checkArgument((reportsConfigurationModelDao.getSourceTableName() == null && reportsConfigurationModelDao.getSourceQuery() != null) ||
                                        (reportsConfigurationModelDao.getSourceTableName() != null && reportsConfigurationModelDao.getSourceQuery() == null),
                                        "sourceTableName or sourceQuery must be defined: " + reportsConfigurationModelDao);

            final Map<String, String> configuration = analyticsConfiguration.databases.get(reportsConfigurationModelDao.getSourceName());
            Preconditions.checkNotNull(configuration, "Missing database configuration for " + reportsConfigurationModelDao.getSourceName());
            // We only support Trino for direct queries today
            Preconditions.checkArgument("trino".equals(configuration.get("type")), "Database %s is not yet supported", configuration.get("type"));

            final Trino trino = new Trino(reportsConfigurationModelDao.getSourceName(), configuration);
            return trino.getDBI();
        } else {
            Preconditions.checkNotNull(reportsConfigurationModelDao.getSourceTableName(), "sourceTableName must be defined: " + reportsConfigurationModelDao);
            return dbi;
        }
    }

    private DBEngine getDbEngine(final ReportsConfigurationModelDao reportsConfigurationModelDao, final DBEngine kbDbEngine) {
        // TODO If a sourceName is specified, assume it's Trino for now (see also getDBI above)
        // Make Presto/Trino look like Postgres (https://github.com/jOOQ/jOOQ/issues/5414, https://github.com/jOOQ/jOOQ/issues/11485)
        return reportsConfigurationModelDao.getSourceName() != null ? DBEngine.POSTGRESQL : kbDbEngine;
    }
}
