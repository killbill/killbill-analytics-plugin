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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sql.DataSource;

import org.killbill.billing.plugin.analytics.dao.BusinessAnalyticsSqlDao;
import org.killbill.billing.plugin.analytics.dao.BusinessDBIProvider;
import org.killbill.billing.plugin.analytics.dao.model.CurrencyConversionModelDao;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.clock.Clock;
import org.killbill.clock.DefaultClock;
import org.killbill.commons.embeddeddb.mysql.MySQLEmbeddedDB;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.notificationq.DefaultNotificationQueueService;
import org.killbill.notificationq.api.NotificationQueueConfig;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.skife.config.ConfigurationObjectFactory;
import org.skife.jdbi.v2.DBI;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

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

    @BeforeClass(groups = "slow")
    public void setUpClass() throws Exception {
        embeddedDB = new MySQLEmbeddedDB();
        embeddedDB.initialize();
        embeddedDB.start();

        final String ddl = toString(Resources.getResource("org/killbill/billing/plugin/analytics/ddl.sql").openStream());
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

        final NotificationQueueConfig config = new ConfigurationObjectFactory(osgiConfigPropertiesService.getProperties()).buildWithReplacements(NotificationQueueConfig.class,
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
