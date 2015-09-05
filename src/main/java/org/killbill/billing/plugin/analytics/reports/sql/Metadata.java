/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.reports.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Meta;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;
import org.osgi.service.log.LogService;

import com.google.common.collect.Ordering;

public class Metadata {

    private static final int MAX_NUMBER_OF_DISTINCT_ITEMS_TO_FETCH = 6;
    private static final int QUERY_TIMEOUT_SECONDS = 30;

    private final Set<String> reportsTables;
    private final KludgeDataSourceConnectionProvider connectionProvider;
    private final DSLContext context;
    private final OSGIKillbillLogService logService;

    private String schemaName = null;
    private final Map<String, Table> tablesCache = new ConcurrentHashMap<String, Table>();
    private final Map<String, Map<String, List<Object>>> distinctValuesCache = new ConcurrentHashMap<String, Map<String, List<Object>>>();

    public Metadata(final Set<String> reportsTables, final DataSource dataSource, final OSGIKillbillLogService logService) {
        this(reportsTables, dataSource, SQLDialect.MYSQL, logService);
    }

    public Metadata(final Set<String> reportsTables, final DataSource dataSource, final SQLDialect sqlDialect, final OSGIKillbillLogService logService) {
        this.reportsTables = reportsTables;
        this.connectionProvider = new KludgeDataSourceConnectionProvider(dataSource);
        this.context = DSL.using(connectionProvider, sqlDialect);
        this.logService = logService;
        primeCaches();
    }

    public synchronized void clearCaches() {
        tablesCache.clear();
    }

    public synchronized TableMetadata getTable(final String tableName) throws SQLException {
        final String schemaName = getSchemaName();
        final Table table = getTable(schemaName, tableName);
        return table == null ? null : new TableMetadata(table, distinctValuesCache.get(tableName));
    }

    private synchronized Table getTable(final String schemaName, final String tableName) throws SQLException {
        if (!tablesCache.isEmpty()) {
            return tablesCache.get(tableName);
        }

        try {
            // Open the connection
            final Meta meta = context.meta();

            Schema analyticsSchema = null;
            for (final Schema schema : meta.getSchemas()) {
                if (schemaName.equalsIgnoreCase(schema.getName())) {
                    analyticsSchema = schema;
                    break;
                }
            }

            if (analyticsSchema == null) {
                return null;
            }

            // We still need the connection alive here!
            for (final Table foundTable : analyticsSchema.getTables()) {
                // Skip all but reports tables (e.g. skip Kill Bill tables if the database is shared)
                if (reportsTables.contains(foundTable.getName())) {
                    logService.log(LogService.LOG_INFO, "Caching metadata for table " + foundTable.getName());
                    tablesCache.put(foundTable.getName(), foundTable);
                    cacheDistinctValues(foundTable);
                }
            }
        } finally {
            connectionProvider.releaseAll();
        }

        return tablesCache.get(tableName);
    }

    private void cacheDistinctValues(final Table table) {
        for (final Field field : table.recordType().fields()) {
            // Skip columns we likely don't want to group/filter on
            if (!ReportsUserApi.DAY_COLUMN_NAME.equals(field.getName()) &&
                !ReportsUserApi.COUNT_COLUMN_NAME.equals(field.getName())) {
                cacheDistinctValues(field.getName(), table.getName());
            }
        }
    }

    private void cacheDistinctValues(final String columnName, final String tableName) {
        logService.log(LogService.LOG_INFO, "Caching distinct values for column " + tableName + "." + columnName);

        if (distinctValuesCache.get(tableName) == null) {
            distinctValuesCache.put(tableName, new ConcurrentHashMap<String, List<Object>>());
        }

        try {
            final Result<?> results = context.selectDistinct(DSL.fieldByName(columnName))
                                             .from(tableName)
                                             .limit(MAX_NUMBER_OF_DISTINCT_ITEMS_TO_FETCH + 1)
                                             .queryTimeout(QUERY_TIMEOUT_SECONDS)
                                             .fetch();

            // If we have too many values, ignore
            if (results.size() <= MAX_NUMBER_OF_DISTINCT_ITEMS_TO_FETCH) {
                if (distinctValuesCache.get(tableName).get(columnName) == null) {
                    distinctValuesCache.get(tableName).put(columnName, new LinkedList<Object>());
                }

                // Sort results in-memory
                final List<? extends Record> sortedResults = Ordering.from(new Comparator<Record>() {
                    @Override
                    public int compare(final Record r1, final Record r2) {
                        if (r1 == null || r1.getValue(0) == null) {
                            return -1;
                        } else if (r2 == null || r2.getValue(0) == null) {
                            return 1;
                        } else {
                            return r1.getValue(0).toString().compareTo(r2.getValue(0).toString());
                        }
                    }
                }).immutableSortedCopy(results);

                for (final Record result : sortedResults) {
                    distinctValuesCache.get(tableName).get(columnName).add(result.getValue(0));
                }
            }
        } catch (final DataAccessException e) {
            // Maybe com.mysql.jdbc.exceptions.MySQLTimeoutException?
            logService.log(LogService.LOG_INFO, "Skipping column: " + e.getLocalizedMessage());
            logService.log(LogService.LOG_DEBUG, "Got exception trying to cache column " + tableName + "." + columnName, e);
        } finally {
            connectionProvider.releaseLast();
        }
    }

    private String getSchemaName() throws SQLException {
        if (schemaName == null) {
            try {
                final Connection connection = connectionProvider.acquire();
                schemaName = connection.getCatalog();
            } finally {
                connectionProvider.releaseAll();
            }
        }

        return schemaName;
    }

    // TODO Work around a bug in jOOQ where the connection is closed too early
    private static class KludgeDataSourceConnectionProvider extends DataSourceConnectionProvider {

        private final List<Connection> connections = new LinkedList<Connection>();

        public KludgeDataSourceConnectionProvider(final DataSource dataSource) {
            super(dataSource);
        }

        @Override
        public void release(final Connection connection) {
            connections.add(connection);
            // No-op, we'll do it ourselves
        }

        public void releaseLast() {
            final int lastIndex = connections.size() - 1;
            releaseConnection(connections.get(lastIndex));
            connections.remove(lastIndex);
        }

        public void releaseAll() {
            final Iterator<Connection> iterator = connections.iterator();

            while (iterator.hasNext()) {
                releaseConnection(iterator.next());
                iterator.remove();
            }
        }

        private void releaseConnection(Connection connection) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DataAccessException("Error closing connection " + connection, e);
            }
        }
    }

    private void primeCaches() {
        final Thread t = new Thread() {
            @Override
            public void run() {
                final long startTime = System.currentTimeMillis();

                logService.log(LogService.LOG_INFO, "Started priming caches...");
                try {
                    // Retrieving one table will load the full catalog
                    getTable("DoesNotMatter");

                    final long secondsToStart = (System.currentTimeMillis() - startTime) / 1000;
                    logService.log(LogService.LOG_INFO, String.format("Primed caches in %d:%02d", secondsToStart / 60, secondsToStart % 60));
                } catch (final SQLException e) {
                    // Ignored
                    logService.log(LogService.LOG_WARNING, "Error while priming caches", e);
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }
}
