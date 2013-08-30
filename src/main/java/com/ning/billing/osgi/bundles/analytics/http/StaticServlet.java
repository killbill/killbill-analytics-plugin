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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.log.LogService;

import com.ning.billing.osgi.bundles.analytics.api.user.AnalyticsUserApi;
import com.ning.billing.osgi.bundles.analytics.reports.ReportsUserApi;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Resources;

// Handle /plugins/killbill-analytics/static/<resourceName>
public class StaticServlet extends BaseServlet {

    public StaticServlet(final AnalyticsUserApi analyticsUserApi, final ReportsUserApi reportsUserApi, final LogService logService) {
        super(analyticsUserApi, reportsUserApi, logService);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String resourceName = (String) req.getAttribute(RESOURCE_NAME_ATTRIBUTE);
        if (resourceName == null) {
            return;
        }

        doHandleStaticResource(resourceName, resp);
    }

    private void doHandleStaticResource(final String resourceName, final HttpServletResponse resp) throws IOException {
        final URL resourceUrl = Resources.getResource("static/" + resourceName);

        final String[] parts = resourceName.split("/");
        if (parts.length > 2) {
            if (parts[1].equals("javascript")) {
                resp.setContentType("application/javascript");
            } else if (parts[1].equals("styles")) {
                resp.setContentType("text/css");
            }
            Resources.copy(resourceUrl, resp.getOutputStream());
        } else {
            final String out = rewriteStaticResource(resourceUrl);
            resp.getOutputStream().write(out.getBytes());
            resp.setContentType("text/html");
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @VisibleForTesting
    static String rewriteStaticResource(final URL resourceUrl) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Resources.copy(resourceUrl, out);
        final String inputHtml = new String(out.toByteArray());

        final String tmp1 = inputHtml.replace("$VAR_SERVER", "\"" + SERVER_IP + "\"");
        return tmp1.replace("$VAR_PORT", "\"" + SERVER_PORT + "\"");
    }
}
