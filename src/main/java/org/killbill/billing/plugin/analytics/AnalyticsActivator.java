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

package org.killbill.billing.plugin.analytics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.concurrent.Executor;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.osgi.libs.killbill.KillbillActivatorBase;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;
import org.killbill.billing.plugin.analytics.api.user.AnalyticsUserApi;
import org.killbill.billing.plugin.analytics.dao.BusinessDBIProvider;
import org.killbill.billing.plugin.analytics.http.ServletRouter;
import org.killbill.billing.plugin.analytics.reports.ReportsConfiguration;
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;
import org.killbill.billing.plugin.analytics.reports.scheduler.JobsScheduler;
import org.killbill.clock.Clock;
import org.killbill.commons.embeddeddb.EmbeddedDB;
import org.killbill.notificationq.DefaultNotificationQueueService;
import org.killbill.notificationq.api.NotificationQueueConfig;
import org.osgi.framework.BundleContext;
import org.skife.config.ConfigurationObjectFactory;
import org.skife.jdbi.v2.DBI;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;

public class AnalyticsActivator extends KillbillActivatorBase {

    public static final String PLUGIN_NAME = "killbill-analytics";
    public static final String ANALYTICS_QUEUE_SERVICE = "AnalyticsService";

    private AnalyticsListener analyticsListener;
    private JobsScheduler jobsScheduler;
    private ReportsUserApi reportsUserApi;

    private Clock killbillClock;

    private final MetricRegistry metricRegistry = new MetricRegistry();

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

        killbillClock = clock.getClock();

        final Executor executor = BusinessExecutor.newCachedThreadPool(configProperties);

        final NotificationQueueConfig config = new ConfigurationObjectFactory(configProperties.getProperties()).buildWithReplacements(NotificationQueueConfig.class,
                                                                                                                                      ImmutableMap.<String, String>of("instanceName", "analytics"));
        final DBI dbi = BusinessDBIProvider.get(dataSource.getDataSource());
        final DefaultNotificationQueueService notificationQueueService = new DefaultNotificationQueueService(dbi, killbillClock, config, metricRegistry);

        analyticsListener = new AnalyticsListener(logService, killbillAPI, dataSource, configProperties, executor, killbillClock, notificationQueueService);

        jobsScheduler = new JobsScheduler(logService, dataSource, killbillClock, notificationQueueService);

        final ReportsConfiguration reportsConfiguration = new ReportsConfiguration(dataSource, jobsScheduler);

        final EmbeddedDB.DBEngine dbEngine = getDbEngine();
        final AnalyticsUserApi analyticsUserApi = new AnalyticsUserApi(logService, killbillAPI, dataSource, configProperties, executor, killbillClock);
        reportsUserApi = new ReportsUserApi(logService, killbillAPI, dataSource, configProperties, dbEngine, reportsConfiguration, jobsScheduler);

        final ServletRouter servletRouter = new ServletRouter(analyticsUserApi, reportsUserApi, logService);
        registerServlet(context, servletRouter);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        if (analyticsListener != null) {
            analyticsListener.shutdownNow();
        }
        if (jobsScheduler != null) {
            jobsScheduler.shutdownNow();
        }
        if (reportsUserApi != null) {
            reportsUserApi.shutdownNow();
        }
        super.stop(context);
    }

    @Deprecated
    @Override
    public OSGIKillbillEventDispatcher.OSGIFrameworkEventHandler getOSGIFrameworkEventHandler() {
        return new OSGIKillbillEventDispatcher.OSGIFrameworkEventHandler() {
            @Override
            public void started() {
                analyticsListener.start();
                dispatcher.registerEventHandler(analyticsListener);
                jobsScheduler.start();
            }
        };
    }

    private void registerServlet(final BundleContext context, final HttpServlet servlet) {
        final Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, Servlet.class, servlet, props);
    }

    private EmbeddedDB.DBEngine getDbEngine() throws SQLException {
        Connection connection = null;
        String databaseProductName = null;
        try {
            connection = dataSource.getDataSource().getConnection();
            databaseProductName = connection.getMetaData().getDatabaseProductName();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        final EmbeddedDB.DBEngine dbEngine;
        if ("H2".equalsIgnoreCase(databaseProductName)) {
            dbEngine = EmbeddedDB.DBEngine.H2;
        } else if ("MySQL".equalsIgnoreCase(databaseProductName)) {
            dbEngine = EmbeddedDB.DBEngine.MYSQL;
        } else if ("PostgreSQL".equalsIgnoreCase(databaseProductName)) {
            dbEngine = EmbeddedDB.DBEngine.POSTGRESQL;
        } else {
            dbEngine = EmbeddedDB.DBEngine.GENERIC;
        }
        return dbEngine;
    }
}
