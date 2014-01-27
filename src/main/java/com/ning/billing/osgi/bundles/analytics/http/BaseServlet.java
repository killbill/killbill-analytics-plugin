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

package com.ning.billing.osgi.bundles.analytics.http;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.log.LogService;

import com.ning.billing.osgi.bundles.analytics.api.user.AnalyticsUserApi;
import com.ning.billing.osgi.bundles.analytics.reports.ReportsUserApi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public abstract class BaseServlet extends HttpServlet {

    protected static final String SERVER_IP = System.getProperty("com.ning.core.server.ip", "127.0.0.1");
    protected static final String SERVER_PORT = System.getProperty("com.ning.core.server.port", "8080");

    protected static final String STRING_PATTERN = "[\\w-]+";
    protected static final String UUID_PATTERN = "\\w+-\\w+-\\w+-\\w+-\\w+";
    protected static final String ANYTHING_PATTERN = ".*";

    protected static final String RESOURCE_NAME_ATTRIBUTE = "resourceName";
    protected static final String REPORT_NAME_ATTRIBUTE = "reportName";
    protected static final String KB_ACCOUNT_ID_ATTRIBUTE = "kbAccountId";

    protected static final ObjectMapper jsonMapper = ObjectMapperProvider.getJsonMapper();
    protected static final ObjectWriter csvMapper = ObjectMapperProvider.getCsvWriter();

    protected final AnalyticsUserApi analyticsUserApi;
    protected final ReportsUserApi reportsUserApi;
    protected final LogService logService;

    public BaseServlet(final AnalyticsUserApi analyticsUserApi, final ReportsUserApi reportsUserApi, final LogService logService) {
        this.analyticsUserApi = analyticsUserApi;
        this.reportsUserApi = reportsUserApi;
        this.logService = logService;
    }

    protected void setCrossSiteScriptingHeaders(final HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Request-Method", "GET");
        resp.setHeader("Access-Control-Allow-Headers", "accept, origin, content-type");
    }
}
