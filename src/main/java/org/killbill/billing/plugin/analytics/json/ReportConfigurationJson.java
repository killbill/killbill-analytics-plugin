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

package org.killbill.billing.plugin.analytics.json;

import javax.annotation.Nullable;

import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.Frequency;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.ReportType;
import org.killbill.billing.plugin.analytics.reports.sql.TableMetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReportConfigurationJson {

    private final Integer recordId;
    private final String reportName;
    private final String reportPrettyName;
    private final ReportType reportType;
    private final String sourceTableName;
    private final String refreshProcedureName;
    private final Frequency refreshFrequency;
    private final Integer refreshHourOfDayGmt;
    private final SchemaJson schema;

    public ReportConfigurationJson(final ReportsConfigurationModelDao reportsConfigurationModelDao, @Nullable final TableMetadata table) {
        this(reportsConfigurationModelDao.getRecordId(),
             reportsConfigurationModelDao.getReportName(),
             reportsConfigurationModelDao.getReportPrettyName(),
             reportsConfigurationModelDao.getReportType(),
             reportsConfigurationModelDao.getSourceTableName(),
             reportsConfigurationModelDao.getRefreshProcedureName(),
             reportsConfigurationModelDao.getRefreshFrequency(),
             reportsConfigurationModelDao.getRefreshHourOfDayGmt(),
             new SchemaJson(table));
    }

    public ReportConfigurationJson(@JsonProperty("recordId") final Integer recordId,
                                   @JsonProperty("reportName") final String reportName,
                                   @JsonProperty("reportPrettyName") final String reportPrettyName,
                                   @JsonProperty("reportType") final ReportType reportType,
                                   @JsonProperty("sourceTableName") final String sourceTableName,
                                   @JsonProperty("refreshProcedureName") final String refreshProcedureName,
                                   @JsonProperty("refreshFrequency") final Frequency refreshFrequency,
                                   @JsonProperty("refreshHourOfDayGmt") final Integer refreshHourOfDayGmt,
                                   @JsonProperty("schema") final SchemaJson schema) {
        this.recordId = recordId;
        this.reportName = reportName;
        this.reportPrettyName = reportPrettyName;
        this.reportType = reportType;
        this.sourceTableName = sourceTableName;
        this.refreshProcedureName = refreshProcedureName;
        this.refreshFrequency = refreshFrequency;
        this.refreshHourOfDayGmt = refreshHourOfDayGmt;
        this.schema = schema;
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

    public SchemaJson getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReportConfigurationJson{");
        sb.append("recordId=").append(recordId);
        sb.append(", reportName='").append(reportName).append('\'');
        sb.append(", reportPrettyName='").append(reportPrettyName).append('\'');
        sb.append(", reportType=").append(reportType);
        sb.append(", sourceTableName='").append(sourceTableName).append('\'');
        sb.append(", refreshProcedureName='").append(refreshProcedureName).append('\'');
        sb.append(", refreshFrequency=").append(refreshFrequency);
        sb.append(", refreshHourOfDayGmt=").append(refreshHourOfDayGmt);
        sb.append(", schema=").append(schema);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReportConfigurationJson that = (ReportConfigurationJson) o;

        if (recordId != null ? !recordId.equals(that.recordId) : that.recordId != null) {
            return false;
        }
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
        if (reportType != that.reportType) {
            return false;
        }
        if (schema != null ? !schema.equals(that.schema) : that.schema != null) {
            return false;
        }
        if (sourceTableName != null ? !sourceTableName.equals(that.sourceTableName) : that.sourceTableName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = recordId != null ? recordId.hashCode() : 0;
        result = 31 * result + (reportName != null ? reportName.hashCode() : 0);
        result = 31 * result + (reportPrettyName != null ? reportPrettyName.hashCode() : 0);
        result = 31 * result + (reportType != null ? reportType.hashCode() : 0);
        result = 31 * result + (sourceTableName != null ? sourceTableName.hashCode() : 0);
        result = 31 * result + (refreshProcedureName != null ? refreshProcedureName.hashCode() : 0);
        result = 31 * result + (refreshFrequency != null ? refreshFrequency.hashCode() : 0);
        result = 31 * result + (refreshHourOfDayGmt != null ? refreshHourOfDayGmt.hashCode() : 0);
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        return result;
    }
}
