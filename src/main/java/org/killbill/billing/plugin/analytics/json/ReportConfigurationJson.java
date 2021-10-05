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

package org.killbill.billing.plugin.analytics.json;

import javax.annotation.Nullable;

import org.jooq.Field;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.Frequency;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.ReportType;
import org.killbill.billing.plugin.analytics.reports.sql.TableMetadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ReportConfigurationJson {

    private final Integer recordId;
    private final String reportName;
    private final String reportPrettyName;
    private final ReportType reportType;
    private final String sourceTableName;
    private final String sourceName;
    private final String sourceQuery;
    private final String refreshProcedureName;
    private final Frequency refreshFrequency;
    private final Integer refreshHourOfDayGmt;
    private final SchemaJson schema;
    private final SchemaJson variables;

    public ReportConfigurationJson(final ReportsConfigurationModelDao reportsConfigurationModelDao,
                                   @Nullable final TableMetadata table,
                                   @Nullable final Iterable<Field<?>> templateVariables) {
        this(reportsConfigurationModelDao.getRecordId(),
             reportsConfigurationModelDao.getReportName(),
             reportsConfigurationModelDao.getReportPrettyName(),
             reportsConfigurationModelDao.getReportType(),
             reportsConfigurationModelDao.getSourceTableName(),
             reportsConfigurationModelDao.getSourceName(),
             reportsConfigurationModelDao.getSourceQuery(),
             reportsConfigurationModelDao.getRefreshProcedureName(),
             reportsConfigurationModelDao.getRefreshFrequency(),
             reportsConfigurationModelDao.getRefreshHourOfDayGmt(),
             new SchemaJson(table),
             // Can't easily create a new constructor because of type erasure at runtime
             new SchemaJson(templateVariables == null ? ImmutableList.<FieldJson>of() : ImmutableList.<FieldJson>copyOf(Iterables.<Field<?>, FieldJson>transform(templateVariables,
                                                                                                                                                                 new Function<Field<?>, FieldJson>() {
                                                                                                                                                                     @Override
                                                                                                                                                                     public FieldJson apply(final Field<?> input) {
                                                                                                                                                                         return input == null ? null : new FieldJson(input, null);
                                                                                                                                                                     }
                                                                                                                                                                 }
                                                                                                                                                                ))));
    }

    public ReportConfigurationJson(@JsonProperty("recordId") final Integer recordId,
                                   @JsonProperty("reportName") final String reportName,
                                   @JsonProperty("reportPrettyName") final String reportPrettyName,
                                   @JsonProperty("reportType") final ReportType reportType,
                                   @JsonProperty("sourceTableName") final String sourceTableName,
                                   @JsonProperty("sourceName") final String sourceName,
                                   @JsonProperty("sourceQuery") final String sourceQuery,
                                   @JsonProperty("refreshProcedureName") final String refreshProcedureName,
                                   @JsonProperty("refreshFrequency") final Frequency refreshFrequency,
                                   @JsonProperty("refreshHourOfDayGmt") final Integer refreshHourOfDayGmt,
                                   @JsonProperty("schema") final SchemaJson schema,
                                   @JsonProperty("variables") final SchemaJson variables) {
        this.recordId = recordId;
        this.reportName = reportName;
        this.reportPrettyName = reportPrettyName;
        this.reportType = reportType;
        this.sourceTableName = sourceTableName;
        this.sourceName = sourceName;
        this.sourceQuery = sourceQuery;
        this.refreshProcedureName = refreshProcedureName;
        this.refreshFrequency = refreshFrequency;
        this.refreshHourOfDayGmt = refreshHourOfDayGmt;
        this.schema = schema;
        this.variables = variables;
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

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceQuery() {
        return sourceQuery;
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

    public SchemaJson getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReportConfigurationJson{");
        sb.append("recordId=").append(recordId);
        sb.append(", reportName='").append(reportName).append('\'');
        sb.append(", reportPrettyName='").append(reportPrettyName).append('\'');
        sb.append(", reportType=").append(reportType);
        sb.append(", sourceTableName='").append(sourceTableName).append('\'');
        sb.append(", sourceName='").append(sourceName).append('\'');
        sb.append(", sourceQuery='").append(sourceQuery).append('\'');
        sb.append(", refreshProcedureName='").append(refreshProcedureName).append('\'');
        sb.append(", refreshFrequency=").append(refreshFrequency);
        sb.append(", refreshHourOfDayGmt=").append(refreshHourOfDayGmt);
        sb.append(", schema=").append(schema);
        sb.append(", variables=").append(variables);
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

        final ReportConfigurationJson that = (ReportConfigurationJson) o;

        if (recordId != null ? !recordId.equals(that.recordId) : that.recordId != null) {
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
        if (sourceTableName != null ? !sourceTableName.equals(that.sourceTableName) : that.sourceTableName != null) {
            return false;
        }
        if (sourceName != null ? !sourceName.equals(that.sourceName) : that.sourceName != null) {
            return false;
        }
        if (sourceQuery != null ? !sourceQuery.equals(that.sourceQuery) : that.sourceQuery != null) {
            return false;
        }
        if (refreshProcedureName != null ? !refreshProcedureName.equals(that.refreshProcedureName) : that.refreshProcedureName != null) {
            return false;
        }
        if (refreshFrequency != that.refreshFrequency) {
            return false;
        }
        if (refreshHourOfDayGmt != null ? !refreshHourOfDayGmt.equals(that.refreshHourOfDayGmt) : that.refreshHourOfDayGmt != null) {
            return false;
        }
        if (schema != null ? !schema.equals(that.schema) : that.schema != null) {
            return false;
        }
        return variables != null ? variables.equals(that.variables) : that.variables == null;
    }

    @Override
    public int hashCode() {
        int result = recordId != null ? recordId.hashCode() : 0;
        result = 31 * result + (reportName != null ? reportName.hashCode() : 0);
        result = 31 * result + (reportPrettyName != null ? reportPrettyName.hashCode() : 0);
        result = 31 * result + (reportType != null ? reportType.hashCode() : 0);
        result = 31 * result + (sourceTableName != null ? sourceTableName.hashCode() : 0);
        result = 31 * result + (sourceName != null ? sourceName.hashCode() : 0);
        result = 31 * result + (sourceQuery != null ? sourceQuery.hashCode() : 0);
        result = 31 * result + (refreshProcedureName != null ? refreshProcedureName.hashCode() : 0);
        result = 31 * result + (refreshFrequency != null ? refreshFrequency.hashCode() : 0);
        result = 31 * result + (refreshHourOfDayGmt != null ? refreshHourOfDayGmt.hashCode() : 0);
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        result = 31 * result + (variables != null ? variables.hashCode() : 0);
        return result;
    }
}
