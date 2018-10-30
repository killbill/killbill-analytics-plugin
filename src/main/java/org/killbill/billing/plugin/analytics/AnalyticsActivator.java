/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2018 Groupon, Inc
 * Copyright 2014-2018 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.concurrent.Executor;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.osgi.libs.killbill.KillbillActivatorBase;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfiguration;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfigurationHandler;
import org.killbill.billing.plugin.analytics.api.user.AnalyticsUserApi;
import org.killbill.billing.plugin.analytics.core.AnalyticsHealthcheck;
import org.killbill.billing.plugin.analytics.dao.BusinessDBIProvider;
import org.killbill.billing.plugin.analytics.http.ServletRouter;
import org.killbill.billing.plugin.analytics.reports.ReportsConfiguration;
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;
import org.killbill.billing.plugin.analytics.reports.scheduler.JobsScheduler;
import org.killbill.billing.plugin.api.notification.PluginConfigurationEventHandler;
import org.killbill.bus.dao.BusEventModelDao;
import org.killbill.clock.Clock;
import org.killbill.commons.embeddeddb.EmbeddedDB;
import org.killbill.commons.jdbi.mapper.LowerToCamelBeanMapperFactory;
import org.killbill.notificationq.DefaultNotificationQueueService;
import org.killbill.notificationq.api.NotificationQueueConfig;
import org.killbill.notificationq.dao.NotificationEventModelDao;
import org.osgi.framework.BundleContext;
import org.skife.config.ConfigurationObjectFactory;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;

public class AnalyticsActivator extends KillbillActivatorBase {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsActivator.class);

    public static final String PLUGIN_NAME = "killbill-analytics";
    public static final String ANALYTICS_QUEUE_SERVICE = "AnalyticsService";

    private AnalyticsConfigurationHandler analyticsConfigurationHandler;
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
        if ("notifications".equals(config.getTableName())) {
            logger.warn("Analytics plugin mis-configured: you are probably missing the property org.killbill.notificationq.analytics.tableName=analytics_notifications");
        }
        if ("notifications_history".equals(config.getHistoryTableName())) {
            logger.warn("Analytics plugin mis-configured: you are probably missing the property org.killbill.notificationq.analytics.historyTableName=analytics_notifications_history");
        }

        final DBI dbi = BusinessDBIProvider.get(dataSource.getDataSource());
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusEventModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(NotificationEventModelDao.class));


        final DefaultNotificationQueueService notificationQueueService = new DefaultNotificationQueueService(dbi, killbillClock, config, metricRegistry);

        analyticsConfigurationHandler = new AnalyticsConfigurationHandler(PLUGIN_NAME, roOSGIkillbillAPI, logService);
        final AnalyticsConfiguration globalConfiguration = analyticsConfigurationHandler.createConfigurable(configProperties.getProperties());
        analyticsConfigurationHandler.setDefaultConfigurable(globalConfiguration);

        analyticsListener = new AnalyticsListener(roOSGIkillbillAPI, dataSource, configProperties, executor, killbillClock, analyticsConfigurationHandler, notificationQueueService);

        jobsScheduler = new JobsScheduler(dataSource, killbillClock, notificationQueueService);

        final ReportsConfiguration reportsConfiguration = new ReportsConfiguration(dataSource, jobsScheduler);

        final EmbeddedDB.DBEngine dbEngine = getDbEngine();
        final AnalyticsUserApi analyticsUserApi = new AnalyticsUserApi(roOSGIkillbillAPI, dataSource, configProperties, executor, killbillClock, analyticsConfigurationHandler);
        reportsUserApi = new ReportsUserApi(roOSGIkillbillAPI, dataSource, configProperties, dbEngine, reportsConfiguration, jobsScheduler);

        final ServletRouter servletRouter = new ServletRouter(analyticsUserApi, reportsUserApi);
        registerServlet(context, servletRouter);
        registerHandlers();
        registerHealthcheck(context, new AnalyticsHealthcheck(analyticsListener, jobsScheduler));
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

    private void registerHandlers() {
        final PluginConfigurationEventHandler configHandler = new PluginConfigurationEventHandler(analyticsConfigurationHandler);

        dispatcher.registerEventHandlers(configHandler,
                                         new OSGIKillbillEventDispatcher.OSGIFrameworkEventHandler() {
                                             @Override
                                             public void started() {
                                                 analyticsListener.start();
                                                 dispatcher.registerEventHandlers(analyticsListener);
                                                 jobsScheduler.start();
                                             }
                                         });
    }

    private void registerServlet(final BundleContext context, final HttpServlet servlet) {
        final Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, Servlet.class, servlet, props);
    }

    private void registerHealthcheck(final BundleContext context, final AnalyticsHealthcheck healthcheck) {
        final Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(OSGIPluginProperties.PLUGIN_NAME_PROP, PLUGIN_NAME);
        registrar.registerService(context, Healthcheck.class, healthcheck, props);
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
