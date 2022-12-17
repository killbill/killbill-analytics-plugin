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

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIMetricRegistry;
import org.killbill.billing.plugin.analytics.dao.BusinessDBIProvider;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationSqlDao;
import org.killbill.billing.plugin.analytics.reports.scheduler.JobsScheduler;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

// TODO Make reports configurable per tenant
public class ReportsConfiguration {

    private final JobsScheduler scheduler;
    private final DBI dbi;

    public ReportsConfiguration(final OSGIKillbillDataSource osgiKillbillDataSource,
                                final OSGIMetricRegistry metricRegistry,
                                final JobsScheduler scheduler) {
        this.dbi = BusinessDBIProvider.get(osgiKillbillDataSource.getDataSource(), metricRegistry.getMetricRegistry());
        this.scheduler = scheduler;
    }

    public void createReportConfiguration(final ReportsConfigurationModelDao report, final Long tenantRecordId) {
        executeWithConnectionAndTransaction(new ReportsConfigurationQueryCallback<Void>() {
            @Override
            public Void executeCallback(Connection connection, ReportsConfigurationSqlDao transactional) {
                transactional.addReportConfiguration(report);

                if (report.getRefreshFrequency() != null && report.getRefreshProcedureName() != null) {
                    // Re-read the record to optimize the schedule creation path
                    final ReportsConfigurationModelDao reportWithRecordId = transactional.getReportConfigurationForReport(report.getReportName());
                    scheduler.schedule(reportWithRecordId, connection);
                }
                return null;
            }
        });
    }

    public void updateReportConfiguration(final ReportsConfigurationModelDao report, final Long tenantRecordId) {
        executeWithConnectionAndTransaction(new ReportsConfigurationQueryCallback<Void>() {
            @Override
            public Void executeCallback(Connection connection, ReportsConfigurationSqlDao transactional) {
                transactional.updateReportConfiguration(report);

                if (report.getRefreshFrequency() != null && report.getRefreshProcedureName() != null) {
                    // Re-read the record to optimize the schedule creation path
                    final ReportsConfigurationModelDao reportWithRecordId = transactional.getReportConfigurationForReport(report.getReportName());
                    scheduler.unSchedule(reportWithRecordId, connection);
                    scheduler.schedule(reportWithRecordId, connection);
                }
                return null;
            }
        });
    }

    public void deleteReportConfiguration(final String reportName, final Long tenantRecordId) {
        executeWithConnectionAndTransaction(new ReportsConfigurationQueryCallback<Void>() {
            @Override
            public Void executeCallback(Connection connection, ReportsConfigurationSqlDao transactional) {
                // Re-read the record to optimize the schedule deletion path
                final ReportsConfigurationModelDao reportsConfigurationModelDao = transactional.getReportConfigurationForReport(reportName);

                // Make deletion idempotent
                if (reportsConfigurationModelDao != null) {
                    transactional.deleteReportConfiguration(reportName);
                    scheduler.unSchedule(reportsConfigurationModelDao, connection);
                }

                return null;
            }
        });
    }

    public Map<String, ReportsConfigurationModelDao> getAllReportConfigurations(final Long tenantRecordId) {
        final Map<String, ReportsConfigurationModelDao> reports = new LinkedHashMap<String, ReportsConfigurationModelDao>();
        final List<ReportsConfigurationModelDao> reportsConfigurationModelDaos = executeWithConnectionAndTransaction(new ReportsConfigurationQueryCallback<List<ReportsConfigurationModelDao>>() {
            @Override
            public List<ReportsConfigurationModelDao> executeCallback(Connection connection, ReportsConfigurationSqlDao transactional) {
                return transactional.getAllReportsConfigurations();
            }
        });
        for (final ReportsConfigurationModelDao report : reportsConfigurationModelDaos) {
            reports.put(report.getReportName(), report);
        }

        return reports;
    }

    public ReportsConfigurationModelDao getReportConfigurationForReport(final String reportName, final Long tenantRecordId) {
        return executeWithConnectionAndTransaction(new ReportsConfigurationQueryCallback<ReportsConfigurationModelDao>() {
            @Override
            public ReportsConfigurationModelDao executeCallback(Connection connection, ReportsConfigurationSqlDao transactional) {
                return transactional.getReportConfigurationForReport(reportName);
            }
        });
    }

    private interface ReportsConfigurationQueryCallback<Result> {
        public Result executeCallback(final Connection connection, final ReportsConfigurationSqlDao transactional);
    }

    private <Result> Result executeWithConnectionAndTransaction(final ReportsConfigurationQueryCallback<Result> callback) {
        return dbi.withHandle(new HandleCallback<Result>() {
            @Override
            public Result withHandle(Handle handle) throws Exception {
                final Connection connection  = handle.getConnection();
                final ReportsConfigurationSqlDao transactional = handle.attach(ReportsConfigurationSqlDao.class);
                return callback.executeCallback(connection, transactional);
            }
        });
    };
}
