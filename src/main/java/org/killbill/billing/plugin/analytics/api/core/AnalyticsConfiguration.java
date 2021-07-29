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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
public class AnalyticsConfiguration {

    // Sane defaults (keys used by most official plugins)
    private final Map<Integer, String> defaultPluginPropertyKeys = ImmutableMap.<Integer, String>of(1, "processorResponse",
                                                                                                    2, "avsResultCode",
                                                                                                    3, "cvvResultCode",
                                                                                                    4, "payment_processor_account_id",
                                                                                                    5, "paymentMethod");

    // List of account ids to ignore
    public List<String> blacklist = new LinkedList<String>();
    // Groups to ignore for refresh, see https://github.com/killbill/killbill-analytics-plugin/issues/87
    public List<String> ignoredGroups = new LinkedList<String>();
    // Delay, in seconds, before starting to refresh data after an event is received. For workflows with lots of successive events
    // for a given account (e.g. create account, add payment method, create payment), this makes sure we have the latest state
    // when starting the refresh (since only the first event will trigger the refresh, all others are ignored).
    public Integer refreshDelaySeconds = 10;
    // How many retries to get the lock
    public Integer lockAttemptRetries = 100;

    public Map<String, Map<Integer, String>> pluginPropertyKeys = new HashMap<String, Map<Integer, String>>();
    public Map<String, Map<String, String>> databases = new HashMap<String, Map<String, String>>();

    public String getPluginPropertyKey(final int position, final String pluginName) {
        if (pluginPropertyKeys.get(pluginName) == null || pluginPropertyKeys.get(pluginName).get(position) == null) {
            return defaultPluginPropertyKeys.get(position);
        }

        return pluginPropertyKeys.get(pluginName).get(position);
    }
}
