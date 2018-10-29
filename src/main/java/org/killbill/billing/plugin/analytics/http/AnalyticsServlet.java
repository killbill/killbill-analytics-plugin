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

package org.killbill.billing.plugin.analytics.http;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.api.BusinessSnapshot;
import org.killbill.billing.plugin.analytics.api.user.AnalyticsUserApi;
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;
import org.killbill.billing.util.callcontext.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Handle /plugins/killbill-analytics/<kbAccountId>
public class AnalyticsServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsServlet.class);

    public AnalyticsServlet(final AnalyticsUserApi analyticsUserApi, final ReportsUserApi reportsUserApi) {
        super(analyticsUserApi, reportsUserApi);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final UUID kbAccountId = (UUID) req.getAttribute(KB_ACCOUNT_ID_ATTRIBUTE);
        if (kbAccountId == null) {
            return;
        }

        final CallContext context = createCallContext(req, resp);

        final BusinessSnapshot businessSnapshot = analyticsUserApi.getBusinessSnapshot(kbAccountId, context);
        resp.getOutputStream().write(jsonMapper.writeValueAsBytes(businessSnapshot));
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final UUID kbAccountId = (UUID) req.getAttribute(KB_ACCOUNT_ID_ATTRIBUTE);
        if (kbAccountId == null) {
            return;
        }

        final CallContext context = createCallContext(req, resp);

        try {
            analyticsUserApi.rebuildAnalyticsForAccount(kbAccountId, context);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (final AnalyticsRefreshException e) {
            logger.error("Error refreshing account {}", kbAccountId, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
