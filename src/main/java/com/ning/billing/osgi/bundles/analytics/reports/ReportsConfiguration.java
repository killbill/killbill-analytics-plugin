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

package com.ning.billing.osgi.bundles.analytics.reports;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.skife.jdbi.v2.DBI;

import com.ning.billing.osgi.bundles.analytics.dao.BusinessDBIProvider;
import com.ning.billing.osgi.bundles.analytics.reports.configuration.ReportsConfigurationModelDao;
import com.ning.billing.osgi.bundles.analytics.reports.configuration.ReportsConfigurationSqlDao;
import com.ning.billing.osgi.bundles.analytics.reports.scheduler.JobsScheduler;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillDataSource;

public class ReportsConfiguration {

    private final ReportsConfigurationSqlDao sqlDao;
    private final JobsScheduler scheduler;

    public ReportsConfiguration(final OSGIKillbillDataSource osgiKillbillDataSource, final JobsScheduler scheduler) {
        final DBI dbi = BusinessDBIProvider.get(osgiKillbillDataSource.getDataSource());
        this.sqlDao = dbi.onDemand(ReportsConfigurationSqlDao.class);
        this.scheduler = scheduler;
    }

    public void initialize() {
        final List<ReportsConfigurationModelDao> reports = sqlDao.getAllReportsConfigurations();
        for (final ReportsConfigurationModelDao report : reports) {
            if (report.getRefreshFrequency() != null && report.getRefreshProcedureName() != null) {
                scheduler.schedule(report);
            }
        }
    }

    public void createReportConfiguration(final ReportsConfigurationModelDao report) {
        sqlDao.addReportConfiguration(report);
    }

    public void updateReportConfiguration(final ReportsConfigurationModelDao report) {
        sqlDao.updateReportConfiguration(report);
    }

    public void deleteReportConfiguration(final String reportName) {
        sqlDao.deleteReportConfiguration(reportName);
    }

    public Map<String, ReportsConfigurationModelDao> getAllReportConfigurations() {
        final Map<String, ReportsConfigurationModelDao> reports = new LinkedHashMap<String, ReportsConfigurationModelDao>();

        final List<ReportsConfigurationModelDao> reportsConfigurationModelDaos = sqlDao.getAllReportsConfigurations();
        for (final ReportsConfigurationModelDao report : reportsConfigurationModelDaos) {
            reports.put(report.getReportName(), report);
        }

        return reports;
    }

    public ReportsConfigurationModelDao getReportConfigurationForReport(final String reportName) {
        return sqlDao.getReportConfigurationForReport(reportName);
    }
}
