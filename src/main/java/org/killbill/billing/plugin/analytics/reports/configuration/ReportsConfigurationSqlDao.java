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

package org.killbill.billing.plugin.analytics.reports.configuration;

import java.util.List;

import org.killbill.commons.jdbi.binder.SmartBindBean;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import org.skife.jdbi.v2.sqlobject.mixins.Transmogrifier;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

@UseStringTemplate3StatementLocator
public interface ReportsConfigurationSqlDao extends Transmogrifier, Transactional<ReportsConfigurationSqlDao> {

    @SqlQuery
    public List<ReportsConfigurationModelDao> getAllReportsConfigurations();

    @SqlQuery
    public ReportsConfigurationModelDao getReportConfigurationForReport(@Bind("reportName") final String reportName);

    @SqlUpdate
    public void addReportConfiguration(@SmartBindBean final ReportsConfigurationModelDao reportsConfigurationModelDao);

    @SqlUpdate
    void updateReportConfiguration(@SmartBindBean final ReportsConfigurationModelDao report);

    @SqlUpdate
    void deleteReportConfiguration(@Bind("reportName") final String reportName);
}
