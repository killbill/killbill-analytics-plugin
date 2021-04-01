/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.reports.configuration;

import javax.annotation.Nullable;

import org.killbill.billing.plugin.analytics.json.ReportConfigurationJson;

import com.google.common.base.MoreObjects;

public class ReportsConfigurationModelDao {

    public static enum Frequency {
        HOURLY,
        DAILY
    }

    public static enum ReportType {
        TIMELINE,
        COUNTERS,
        TABLE
    }

    // Used as a search key in the notification queue
    private Integer recordId;
    private String reportName;
    private String reportPrettyName;
    private ReportType reportType;
    private String sourceTableName;
    private String refreshProcedureName;
    private Frequency refreshFrequency;
    private Integer refreshHourOfDayGmt;

    public ReportsConfigurationModelDao() { /* When reading from the database */ }

    public ReportsConfigurationModelDao(final ReportConfigurationJson reportConfigurationJson) {
        this(reportConfigurationJson.getRecordId(),
             reportConfigurationJson.getReportName(),
             reportConfigurationJson.getReportPrettyName(),
             reportConfigurationJson.getReportType(),
             reportConfigurationJson.getSourceTableName(),
             reportConfigurationJson.getRefreshProcedureName(),
             reportConfigurationJson.getRefreshFrequency(),
             reportConfigurationJson.getRefreshHourOfDayGmt());
    }

    public ReportsConfigurationModelDao(final ReportConfigurationJson reportConfigurationJson, final ReportsConfigurationModelDao currentReportsConfigurationModelDao) {
        this(currentReportsConfigurationModelDao.getRecordId(), // Never override
             currentReportsConfigurationModelDao.getReportName(), // Never override
             reportConfigurationJson.getReportPrettyName() != null ? reportConfigurationJson.getReportPrettyName() : currentReportsConfigurationModelDao.getReportPrettyName(),
             currentReportsConfigurationModelDao.getReportType(),
             MoreObjects.firstNonNull(reportConfigurationJson.getSourceTableName(), currentReportsConfigurationModelDao.getSourceTableName()),
             reportConfigurationJson.getRefreshProcedureName() != null ? reportConfigurationJson.getRefreshProcedureName() : currentReportsConfigurationModelDao.getRefreshProcedureName(),
             reportConfigurationJson.getRefreshFrequency() != null ? reportConfigurationJson.getRefreshFrequency() : currentReportsConfigurationModelDao.getRefreshFrequency(),
             reportConfigurationJson.getRefreshHourOfDayGmt() != null ? reportConfigurationJson.getRefreshHourOfDayGmt() : currentReportsConfigurationModelDao.getRefreshHourOfDayGmt());
    }

    public ReportsConfigurationModelDao(final String reportName, final String reportPrettyName, final ReportType type, final String sourceTableName,
                                        final String refreshProcedureName, final Frequency refreshFrequency, final Integer refreshHourOfDayGmt) {
        this(null, reportName, reportPrettyName, type, sourceTableName, refreshProcedureName, refreshFrequency, refreshHourOfDayGmt);
    }

    public ReportsConfigurationModelDao(@Nullable final Integer recordId, final String reportName, final String reportPrettyName, final ReportType type, final String sourceTableName,
                                        final String refreshProcedureName, final Frequency refreshFrequency, final Integer refreshHourOfDayGmt) {
        this.recordId = recordId;
        this.reportName = reportName;
        this.reportPrettyName = reportPrettyName;
        this.reportType = MoreObjects.firstNonNull(type, ReportType.TIMELINE);
        this.sourceTableName = sourceTableName;
        this.refreshProcedureName = refreshProcedureName;
        this.refreshFrequency = refreshFrequency;
        this.refreshHourOfDayGmt = refreshHourOfDayGmt;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public String getReportName() {
        return reportName;
    }

    public String getReportPrettyName() {
        return reportPrettyName;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public String getRefreshProcedureName() {
        return refreshProcedureName;
    }

    public Frequency getRefreshFrequency() {
        return refreshFrequency;
    }

    public Integer getRefreshHourOfDayGmt() {
        return refreshHourOfDayGmt;
    }

    public ReportType getReportType() {
        return reportType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReportsConfigurationModelDao{");
        sb.append("recordId=").append(recordId);
        sb.append(", reportName='").append(reportName).append('\'');
        sb.append(", reportPrettyName='").append(reportPrettyName).append('\'');
        sb.append(", sourceTableName='").append(sourceTableName).append('\'');
        sb.append(", reportType='").append(reportType).append('\'');
        sb.append(", refreshProcedureName='").append(refreshProcedureName).append('\'');
        sb.append(", refreshFrequency=").append(refreshFrequency);
        sb.append(", refreshHourOfDayGmt=").append(refreshHourOfDayGmt);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ReportsConfigurationModelDao that = (ReportsConfigurationModelDao) o;

        if (recordId != null ? !recordId.equals(that.recordId) : that.recordId != null) {
            return false;
        }

        return equalsNoRecordId(that);
    }

    public boolean equalsNoRecordId(final ReportsConfigurationModelDao that) {
        if (refreshFrequency != that.refreshFrequency) {
            return false;
        }
        if (refreshHourOfDayGmt != null ? !refreshHourOfDayGmt.equals(that.refreshHourOfDayGmt) : that.refreshHourOfDayGmt != null) {
            return false;
        }
        if (refreshProcedureName != null ? !refreshProcedureName.equals(that.refreshProcedureName) : that.refreshProcedureName != null) {
            return false;
        }
        if (reportName != null ? !reportName.equals(that.reportName) : that.reportName != null) {
            return false;
        }
        if (reportPrettyName != null ? !reportPrettyName.equals(that.reportPrettyName) : that.reportPrettyName != null) {
            return false;
        }
        if (sourceTableName != null ? !sourceTableName.equals(that.sourceTableName) : that.sourceTableName != null) {
            return false;
        }
        if (reportType != null ? !reportType.equals(that.reportType) : that.reportType != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = recordId != null ? recordId.hashCode() : 0;
        result = 31 * result + (reportName != null ? reportName.hashCode() : 0);
        result = 31 * result + (reportPrettyName != null ? reportPrettyName.hashCode() : 0);
        result = 31 * result + (sourceTableName != null ? sourceTableName.hashCode() : 0);
        result = 31 * result + (reportType != null ? reportType.hashCode() : 0);
        result = 31 * result + (refreshProcedureName != null ? refreshProcedureName.hashCode() : 0);
        result = 31 * result + (refreshFrequency != null ? refreshFrequency.hashCode() : 0);
        result = 31 * result + (refreshHourOfDayGmt != null ? refreshHourOfDayGmt.hashCode() : 0);
        return result;
    }
}
