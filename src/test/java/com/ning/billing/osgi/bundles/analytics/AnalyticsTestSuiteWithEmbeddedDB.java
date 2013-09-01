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

package com.ning.billing.osgi.bundles.analytics;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.skife.config.ConfigurationObjectFactory;
import org.skife.jdbi.v2.DBI;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.ning.billing.clock.Clock;
import com.ning.billing.clock.DefaultClock;
import com.ning.billing.commons.embeddeddb.mysql.MySQLEmbeddedDB;
import com.ning.billing.notificationq.DefaultNotificationQueueService;
import com.ning.billing.notificationq.api.NotificationQueueConfig;
import com.ning.billing.osgi.bundles.analytics.dao.BusinessAnalyticsSqlDao;
import com.ning.billing.osgi.bundles.analytics.dao.BusinessDBIProvider;
import com.ning.billing.osgi.bundles.analytics.dao.model.CurrencyConversionModelDao;
import com.ning.billing.osgi.bundles.analytics.utils.CurrencyConverter;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillDataSource;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;

public abstract class AnalyticsTestSuiteWithEmbeddedDB extends AnalyticsTestSuiteNoDB {

    protected MySQLEmbeddedDB embeddedDB;
    protected DBI dbi;
    protected BusinessAnalyticsSqlDao analyticsSqlDao;
    protected DefaultNotificationQueueService notificationQueueService;

    protected final Clock clock = new DefaultClock();
    protected final CurrencyConverter currencyConverter = new CurrencyConverter(clock, ImmutableMap.<String, List<CurrencyConversionModelDao>>of());
    protected final Properties properties = new Properties();

    @BeforeClass(groups = "slow")
    public void setUpClass() throws Exception {
        embeddedDB = new MySQLEmbeddedDB();
        embeddedDB.initialize();
        embeddedDB.start();

        final String ddl = toString(Resources.getResource("com/ning/billing/osgi/bundles/analytics/ddl.sql").openStream());
        embeddedDB.executeScript(ddl);
        embeddedDB.refreshTableNames();
    }

    @BeforeMethod(groups = "slow")
    @Override
    public void setUp() throws Exception {
        super.setUp();

        killbillDataSource = new AnalyticsOSGIKillbillDataSource();

        embeddedDB.cleanupAllTables();

        dbi = BusinessDBIProvider.get(embeddedDB.getDataSource());
        analyticsSqlDao = dbi.onDemand(BusinessAnalyticsSqlDao.class);

        properties.setProperty("killbill.billing.notificationq.analytics.tableName", "analytics_notifications");
        properties.setProperty("killbill.billing.notificationq.analytics.historyTableName", "analytics_notifications_history");

        final NotificationQueueConfig config = new ConfigurationObjectFactory(properties).buildWithReplacements(NotificationQueueConfig.class,
                                                                                                                ImmutableMap.<String, String>of("instanceName", "analytics"));
        notificationQueueService = new DefaultNotificationQueueService(dbi, clock, config, new MetricRegistry());
    }

    @AfterClass(groups = "slow")
    public void tearDown() throws Exception {
        embeddedDB.stop();
    }

    public static String toString(final InputStream stream) throws IOException {
        final InputSupplier<InputStream> inputSupplier = new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return stream;
            }
        };

        return CharStreams.toString(CharStreams.newReaderSupplier(inputSupplier, Charsets.UTF_8));
    }

    private final class AnalyticsOSGIKillbillDataSource extends OSGIKillbillDataSource {

        public AnalyticsOSGIKillbillDataSource() {
            super(Mockito.mock(BundleContext.class));
        }

        @Override
        public DataSource getDataSource() {
            try {
                return embeddedDB.getDataSource();
            } catch (IOException e) {
                Assert.fail(e.toString(), e);
                return null;
            }
        }
    }
}
