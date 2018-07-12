/*
 * Copyright 2014-2018 Groupon, Inc
 * Copyright 2014-2018 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.core;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.plugin.analytics.AnalyticsListener;
import org.killbill.billing.plugin.analytics.reports.scheduler.JobsScheduler;
import org.killbill.billing.tenant.api.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsHealthcheck implements Healthcheck {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsHealthcheck.class);

    private final AnalyticsListener analyticsListener;
    private final JobsScheduler jobsScheduler;

    public AnalyticsHealthcheck(final AnalyticsListener analyticsListener, final JobsScheduler jobsScheduler) {
        this.analyticsListener = analyticsListener;
        this.jobsScheduler = jobsScheduler;
    }

    @Override
    public HealthStatus getHealthStatus(@Nullable final Tenant tenant, @Nullable final Map properties) {
        final boolean analyticsListenerStarted = analyticsListener.isStarted();
        final boolean jobsSchedulerStarted = jobsScheduler.isStarted();

        final boolean healthy = analyticsListenerStarted && jobsSchedulerStarted;

        final Map details = new HashMap();
        details.put("AnalyticsListener", analyticsListenerStarted);
        details.put("JobsScheduler", jobsSchedulerStarted);

        return new HealthStatus(healthy, details);
    }
}
