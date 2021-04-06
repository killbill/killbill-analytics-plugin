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

package org.killbill.billing.plugin.analytics.reports.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.jooq.ConnectionCallable;
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
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Ordering;

public class Metadata {

    private static final Logger logger = LoggerFactory.getLogger(Metadata.class);

    private static final int MAX_NUMBER_OF_DISTINCT_ITEMS_TO_FETCH = 6;
    private static final int QUERY_TIMEOUT_SECONDS = 30;

    private final Set<String> reportsTables;
    private final DSLContext context;

    private String schemaName = null;
    private final Map<String, Table> tablesCache = new ConcurrentHashMap<String, Table>();
    private final Map<String, Map<String, List<Object>>> distinctValuesCache = new ConcurrentHashMap<String, Map<String, List<Object>>>();

    public Metadata(final Set<String> reportsTables, final DataSource dataSource) {
        this(reportsTables, dataSource, SQLDialect.MYSQL);
    }

    public Metadata(final Set<String> reportsTables, final DataSource dataSource, final SQLDialect sqlDialect) {
        this.reportsTables = reportsTables;
        this.context = DSL.using(dataSource, sqlDialect);
        primeCaches();
    }

    public synchronized void clearCaches() {
        tablesCache.clear();
    }

    public synchronized TableMetadata getTable(@Nullable final String tableName) throws SQLException {
        if (tableName == null) {
            // No metadata for non-local tables
            return null;
        }
        final String schemaName = getSchemaName();
        final Table table = getTable(schemaName, tableName);
        return table == null ? null : new TableMetadata(table, distinctValuesCache.get(tableName));
    }

    private Table getTable(final String schemaName, final String tableName) throws SQLException {
        if (!tablesCache.isEmpty()) {
            return tablesCache.get(tableName);
        }

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

        for (final Table foundTable : analyticsSchema.getTables()) {
            // Skip all but reports tables (e.g. skip Kill Bill tables if the database is shared)
            if (reportsTables.contains(foundTable.getName())) {
                logger.info("Caching metadata for table {}", foundTable.getName());
                tablesCache.put(foundTable.getName(), foundTable);
                cacheDistinctValues(foundTable);
            }
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
        logger.info("Caching distinct values for column {}.{}", tableName, columnName);

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
            logger.info("Skipping column: {}", e.getLocalizedMessage());
            logger.debug("Got exception trying to cache column {}.{}", tableName, columnName, e);
        }
    }

    private String getSchemaName() {
        if (schemaName == null) {
            schemaName = context.connectionResult(new ConnectionCallable<String>() {
                @Override
                public String run(final Connection connection) throws Exception {
                    return connection.getCatalog();
                }
            });
        }

        return schemaName;
    }

    private void primeCaches() {
        final Thread t = new Thread() {
            @Override
            public void run() {
                final long startTime = System.currentTimeMillis();

                logger.info("Started priming caches...");
                try {
                    // Retrieving one table will load the full catalog
                    getTable("DoesNotMatter");

                    final long secondsToStart = (System.currentTimeMillis() - startTime) / 1000;
                    logger.info(String.format("Primed caches in %d:%02d", secondsToStart / 60, secondsToStart % 60));
                } catch (final SQLException e) {
                    // Ignored
                    logger.warn("Error while priming caches", e);
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }
}
