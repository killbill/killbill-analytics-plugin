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

package org.killbill.billing.plugin.analytics.api.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.killbill.billing.plugin.analytics.AnalyticsActivator;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class AnalyticsConfiguration {

    // List of account ids to ignore
    public final List<String> blacklist;
    // Groups to ignore for refresh, see https://github.com/killbill/killbill-analytics-plugin/issues/87
    public final List<String> ignoredGroups;
    // Delay, in seconds, before starting to refresh data after an event is received. For workflows with lots of successive events
    // for a given account (e.g. create account, add payment method, create payment), this makes sure we have the latest state
    // when starting the refresh (since only the first event will trigger the refresh, all others are ignored).
    public final Integer refreshDelaySeconds;
    // How many retries to get the lock
    public final Integer lockAttemptRetries;
    // If the lock is taken, how long until the job is rescheduled
    public final Integer rescheduleIntervalOnLockSeconds;
    // Whether to trigger full refreshes each time
    public final boolean enablePartialRefreshes;
    // List of account ids with a high cardinality (where queries by account_record_id is a bad idea)
    public final List<String> highCardinalityAccounts;
    // Sane defaults (keys used by most official plugins)
    private final Map<Integer, String> defaultPluginPropertyKeys = ImmutableMap.<Integer, String>of(1, "processorResponse",
                                                                                                    2, "avsResultCode",
                                                                                                    3, "cvvResultCode",
                                                                                                    4, "payment_processor_account_id",
                                                                                                    5, "paymentMethod");
    // Whether to allow template variables in raw SQL queries.
    // Note! This could be prone to SQL injection and should only be enabled in trusted environments.
    public final boolean enableTemplateVariables;
    public Map<String, Map<Integer, String>> pluginPropertyKeys = new HashMap<String, Map<Integer, String>>();
    public Map<String, Map<String, String>> databases = new HashMap<String, Map<String, String>>();

    public AnalyticsConfiguration() {
        this (new LinkedList<>(), new LinkedList<>(), 10, 100, 10, true, false, new LinkedList<>());
    }

    public AnalyticsConfiguration(final boolean enableTemplateVariables) {
        this(new LinkedList<>(), new LinkedList<>(), 10, 100, 10, true, enableTemplateVariables, new LinkedList<>());
    }

    public AnalyticsConfiguration(final List<String> blacklist, final List<String> ignoredGroups, final int refreshDelaySeconds, final int lockAttemptRetries, final int rescheduleIntervalOnLockSeconds, final boolean enablePartialRefreshes, final boolean enableTemplateVariables, final List<String> highCardinalityAccounts) {
        this.blacklist = blacklist;
        this.ignoredGroups = ignoredGroups;
        this.refreshDelaySeconds = refreshDelaySeconds;
        this.lockAttemptRetries = lockAttemptRetries;
        this.rescheduleIntervalOnLockSeconds = rescheduleIntervalOnLockSeconds;
        this.enablePartialRefreshes = enablePartialRefreshes;
        this.enableTemplateVariables = enableTemplateVariables;
        this.highCardinalityAccounts = highCardinalityAccounts;
    }

    public AnalyticsConfiguration(final Properties properties) {  //CTOR used only for default configuration from global properties if available

        final String blackList = properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "blacklist");
        this.blacklist = blackList != null && !blackList.isEmpty() ? Arrays.asList(blackList.split(",")) : new LinkedList<>();

        final String ignoredGroups = properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "ignoredGroups");
        this.ignoredGroups = ignoredGroups != null && !ignoredGroups.isEmpty() ? Arrays.asList(ignoredGroups.split(",")) : new LinkedList<>();

        final String highCardinalityAccounts = properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "highCardinalityAccounts");
        this.highCardinalityAccounts = highCardinalityAccounts != null && !highCardinalityAccounts.isEmpty() ? Arrays.asList(highCardinalityAccounts.split(",")) : new LinkedList<>();

        this.refreshDelaySeconds = properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "refreshDelaySeconds") != null ? Integer.parseInt(properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "refreshDelaySeconds")) : 10;
        this.lockAttemptRetries = properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "lockAttemptRetries") != null ? Integer.parseInt(properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "lockAttemptRetries")) : 100;
        this.rescheduleIntervalOnLockSeconds = properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "rescheduleIntervalOnLockSeconds") != null ? Integer.parseInt(properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "rescheduleIntervalOnLockSeconds")) : 10;
        this.enablePartialRefreshes = properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "enablePartialRefreshes") == null || Boolean.parseBoolean(properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "enablePartialRefreshes"));
        this.enableTemplateVariables = properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "enableTemplateVariables") != null && Boolean.parseBoolean(properties.getProperty(AnalyticsActivator.PROPERTY_PREFIX + "enableTemplateVariables"));

    }

    public String getPluginPropertyKey(final int position, final String pluginName) {
        if (pluginPropertyKeys.get(pluginName) == null || pluginPropertyKeys.get(pluginName).get(position) == null) {
            return defaultPluginPropertyKeys.get(position);
        }

        return pluginPropertyKeys.get(pluginName).get(position);
    }
}
