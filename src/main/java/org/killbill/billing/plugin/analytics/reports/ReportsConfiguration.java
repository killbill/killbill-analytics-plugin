/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2015 Groupon, Inc
 * Copyright 2014-2015 The Billing Project, LLC
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.killbill.billing.plugin.analytics.dao.BusinessDBIProvider;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationSqlDao;
import org.killbill.billing.plugin.analytics.reports.scheduler.JobsScheduler;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;

// TODO Make reports configurable per tenant
public class ReportsConfiguration {

    private final ReportsConfigurationSqlDao sqlDao;
    private final JobsScheduler scheduler;

    public ReportsConfiguration(final OSGIKillbillDataSource osgiKillbillDataSource, final JobsScheduler scheduler) {
        final DBI dbi = BusinessDBIProvider.get(osgiKillbillDataSource.getDataSource());
        this.sqlDao = dbi.onDemand(ReportsConfigurationSqlDao.class);
        this.scheduler = scheduler;
    }

    public void createReportConfiguration(final ReportsConfigurationModelDao report, final Long tenantRecordId) {
        sqlDao.inTransaction(new Transaction<Void, ReportsConfigurationSqlDao>() {
            @Override
            public Void inTransaction(final ReportsConfigurationSqlDao transactional, final TransactionStatus status) throws Exception {
                transactional.addReportConfiguration(report);

                if (report.getRefreshFrequency() != null && report.getRefreshProcedureName() != null) {
                    // Re-read the record to optimize the schedule creation path
                    final ReportsConfigurationModelDao reportWithRecordId = transactional.getReportConfigurationForReport(report.getReportName());
                    scheduler.schedule(reportWithRecordId, transactional);
                }

                return null;
            }
        });
    }

    public void updateReportConfiguration(final ReportsConfigurationModelDao report, final Long tenantRecordId) {
        sqlDao.inTransaction(new Transaction<Void, ReportsConfigurationSqlDao>() {
            @Override
            public Void inTransaction(final ReportsConfigurationSqlDao transactional, final TransactionStatus status) throws Exception {
                transactional.updateReportConfiguration(report);

                if (report.getRefreshFrequency() != null && report.getRefreshProcedureName() != null) {
                    // Re-read the record to optimize the schedule creation path
                    final ReportsConfigurationModelDao reportWithRecordId = transactional.getReportConfigurationForReport(report.getReportName());
                    scheduler.unSchedule(reportWithRecordId, transactional);
                    scheduler.schedule(reportWithRecordId, transactional);
                }

                return null;
            }
        });
    }

    public void deleteReportConfiguration(final String reportName, final Long tenantRecordId) {
        sqlDao.inTransaction(new Transaction<Void, ReportsConfigurationSqlDao>() {
            @Override
            public Void inTransaction(final ReportsConfigurationSqlDao transactional, final TransactionStatus status) throws Exception {
                // Re-read the record to optimize the schedule deletion path
                final ReportsConfigurationModelDao reportsConfigurationModelDao = transactional.getReportConfigurationForReport(reportName);

                transactional.deleteReportConfiguration(reportName);
                scheduler.unSchedule(reportsConfigurationModelDao, transactional);
                return null;
            }
        });
    }

    public Map<String, ReportsConfigurationModelDao> getAllReportConfigurations(final Long tenantRecordId) {
        final Map<String, ReportsConfigurationModelDao> reports = new LinkedHashMap<String, ReportsConfigurationModelDao>();

        final List<ReportsConfigurationModelDao> reportsConfigurationModelDaos = sqlDao.getAllReportsConfigurations();
        for (final ReportsConfigurationModelDao report : reportsConfigurationModelDaos) {
            reports.put(report.getReportName(), report);
        }

        return reports;
    }

    public ReportsConfigurationModelDao getReportConfigurationForReport(final String reportName, final Long tenantRecordId) {
        return sqlDao.getReportConfigurationForReport(reportName);
    }
}
