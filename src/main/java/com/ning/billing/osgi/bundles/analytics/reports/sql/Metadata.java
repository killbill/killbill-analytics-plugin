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

package com.ning.billing.osgi.bundles.analytics.reports.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.Meta;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.osgi.service.log.LogService;

import com.ning.killbill.osgi.libs.killbill.OSGIKillbillLogService;

public class Metadata {

    private final KludgeDataSourceConnectionProvider connectionProvider;
    private final DSLContext context;
    private final OSGIKillbillLogService logService;

    private String schemaName = null;
    private final Map<String, Table> tablesCache = new ConcurrentHashMap<String, Table>();

    public Metadata(final DataSource dataSource, final OSGIKillbillLogService logService) {
        this(dataSource, SQLDialect.MYSQL, logService);
    }

    public Metadata(final DataSource dataSource, final SQLDialect sqlDialect, final OSGIKillbillLogService logService) {
        this.connectionProvider = new KludgeDataSourceConnectionProvider(dataSource);
        this.context = DSL.using(connectionProvider, sqlDialect);
        this.logService = logService;
        primeCaches();
    }

    public synchronized void clearCaches() {
        tablesCache.clear();
    }

    public synchronized Table getTable(final String tableName) throws SQLException {
        final String schemaName = getSchemaName();
        return getTable(schemaName, tableName);
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
                if (schemaName.equals(schema.getName())) {
                    analyticsSchema = schema;
                    break;
                }
            }

            if (analyticsSchema == null) {
                return null;
            }

            // We still need the connection alive here!
            for (final Table foundTable : analyticsSchema.getTables()) {
                tablesCache.put(foundTable.getName(), foundTable);
            }
        } finally {
            connectionProvider.releaseAll();
        }

        return tablesCache.get(tableName);
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

        public void releaseAll() {
            final Iterator<Connection> iterator = connections.iterator();

            while (iterator.hasNext()) {
                Connection connection = null;
                try {
                    connection = iterator.next();
                    connection.close();
                    iterator.remove();
                } catch (SQLException e) {
                    throw new DataAccessException("Error closing connection " + connection, e);
                }
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
        t.run();
    }
}
