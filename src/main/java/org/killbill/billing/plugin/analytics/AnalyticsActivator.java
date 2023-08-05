/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2022 Equinix, Inc
 * Copyright 2014-2022 The Billing Project, LLC
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

import java.util.Hashtable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

import org.killbill.billing.currency.plugin.api.CurrencyPluginApi;
import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.osgi.api.OSGIPluginProperties;
import org.killbill.billing.osgi.libs.killbill.KillbillActivatorBase;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfiguration;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfigurationHandler;
import org.killbill.billing.plugin.analytics.api.user.AnalyticsUserApi;
import org.killbill.billing.plugin.analytics.core.AnalyticsHealthcheck;
import org.killbill.billing.plugin.analytics.dao.BusinessDBIProvider;
import org.killbill.billing.plugin.analytics.http.AnalyticsAccountResource;
import org.killbill.billing.plugin.analytics.http.AnalyticsHealthcheckResource;
import org.killbill.billing.plugin.analytics.http.ReportsResource;
import org.killbill.billing.plugin.analytics.reports.ReportsConfiguration;
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;
import org.killbill.billing.plugin.analytics.reports.scheduler.JobsScheduler;
import org.killbill.billing.plugin.api.notification.PluginConfigurationEventHandler;
import org.killbill.billing.plugin.core.config.PluginEnvironmentConfig;
import org.killbill.billing.plugin.core.resources.jooby.PluginApp;
import org.killbill.billing.plugin.core.resources.jooby.PluginAppBuilder;
import org.killbill.billing.plugin.dao.PluginDao;
import org.killbill.billing.plugin.dao.PluginDao.DBEngine;
import org.killbill.bus.dao.BusEventModelDao;
import org.killbill.clock.Clock;
import org.killbill.commons.jdbi.mapper.LowerToCamelBeanMapperFactory;
import org.killbill.commons.locker.GlobalLocker;
import org.killbill.commons.locker.memory.MemoryGlobalLocker;
import org.killbill.commons.locker.mysql.MySqlGlobalLocker;
import org.killbill.commons.locker.postgresql.PostgreSQLGlobalLocker;
import org.killbill.notificationq.DefaultNotificationQueueService;
import org.killbill.notificationq.api.NotificationQueueConfig;
import org.killbill.notificationq.dao.NotificationEventModelDao;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.skife.config.ConfigurationObjectFactory;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.ImmutableMap;

public class AnalyticsActivator extends KillbillActivatorBase {

    public static final String PLUGIN_NAME = "killbill-analytics";
    public static final String ANALYTICS_QUEUE_SERVICE = "AnalyticsService";

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsActivator.class);

    private AnalyticsConfigurationHandler analyticsConfigurationHandler;
    private ServiceTracker<CurrencyPluginApi, CurrencyPluginApi> currencyPluginApiServiceTracker;
    private AnalyticsListener analyticsListener;
    private JobsScheduler jobsScheduler;
    private ReportsUserApi reportsUserApi;
    private Clock killbillClock;

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

        final DBI dbi = BusinessDBIProvider.get(dataSource.getDataSource(), metricRegistry.getMetricRegistry());
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusEventModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(NotificationEventModelDao.class));

        final DefaultNotificationQueueService notificationQueueService = new DefaultNotificationQueueService(dbi, killbillClock, config, metricRegistry.getMetricRegistry());

        final String region = PluginEnvironmentConfig.getRegion(configProperties.getProperties());

        analyticsConfigurationHandler = new AnalyticsConfigurationHandler(region, PLUGIN_NAME, roOSGIkillbillAPI);
        analyticsConfigurationHandler.setDefaultConfigurable(new AnalyticsConfiguration());

        // Timeout defines how long to sleep between retries to get the lock
        final long lockSleepMilliSeconds = Long.parseLong(configProperties.getProperties().getProperty("org.killbill.analytics.lockSleepMilliSeconds", "100"));

        final DBEngine dbEngine = PluginDao.getDBEngine(dataSource.getDataSource());
        final GlobalLocker locker;
        switch (dbEngine) {
            case MYSQL:
                locker = new MySqlGlobalLocker(dataSource.getDataSource(), lockSleepMilliSeconds, TimeUnit.MILLISECONDS);
                break;
            case POSTGRESQL:
                locker = new PostgreSQLGlobalLocker(dataSource.getDataSource(), lockSleepMilliSeconds, TimeUnit.MILLISECONDS);
                break;
            case GENERIC:
            case H2:
            default:
                locker = new MemoryGlobalLocker();
                break;
        }

        currencyPluginApiServiceTracker = new ServiceTracker<>(context, CurrencyPluginApi.class, null);
        currencyPluginApiServiceTracker.open();

        analyticsListener = new AnalyticsListener(roOSGIkillbillAPI,
                                                  dataSource,
                                                  metricRegistry,
                                                  configProperties,
                                                  currencyPluginApiServiceTracker,
                                                  executor,
                                                  locker,
                                                  killbillClock,
                                                  analyticsConfigurationHandler,
                                                  notificationQueueService);

        jobsScheduler = new JobsScheduler(dataSource, metricRegistry, killbillClock, notificationQueueService);

        final ReportsConfiguration reportsConfiguration = new ReportsConfiguration(dataSource, metricRegistry, jobsScheduler);

        final AnalyticsUserApi analyticsUserApi = new AnalyticsUserApi(roOSGIkillbillAPI, dataSource, metricRegistry, configProperties, currencyPluginApiServiceTracker, executor, killbillClock, analyticsConfigurationHandler, analyticsListener);
        reportsUserApi = new ReportsUserApi(roOSGIkillbillAPI, dataSource, metricRegistry, configProperties, dbEngine, reportsConfiguration, jobsScheduler, analyticsConfigurationHandler);

        final AnalyticsHealthcheck healthcheck = new AnalyticsHealthcheck(analyticsListener, jobsScheduler);
        registerHealthcheck(context, healthcheck);

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);

        final PluginApp pluginApp = new PluginAppBuilder(PLUGIN_NAME,
                                                         killbillAPI,
                                                         dataSource,
                                                         clock,
                                                         configProperties).withRouteClass(AnalyticsHealthcheckResource.class)
                                                                          .withRouteClass(ReportsResource.class)
                                                                          .withRouteClass(AnalyticsAccountResource.class) // Needs to be last (to avoid matching /healthcheck or /reports)!
                                                                          .withService(analyticsUserApi)
                                                                          .withService(reportsUserApi)
                                                                          .withService(clock)
                                                                          .withService(healthcheck)
                                                                          .withObjectMapper(objectMapper)
                                                                          .build();
        final HttpServlet httpServlet = PluginApp.createServlet(pluginApp);
        registerServlet(context, httpServlet);

        registerHandlers();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        if (jobsScheduler != null) {
            jobsScheduler.shutdownNow();
        }
        if (currencyPluginApiServiceTracker != null) {
            currencyPluginApiServiceTracker.close();
        }
        if (analyticsListener != null) {
            // Little bit of subtlety here, which is queue implementation dependent: only the second time
            // the queue is asked to stop that it will actually go through the shutdown sequence
            if (!analyticsListener.shutdownNow()) {
                logger.warn("Timed out while shutting down Analytics notifications queue: IN_PROCESSING entries might be left behind");
            }
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
}
