/*
 * Copyright 2016 Groupon, Inc
 * Copyright 2016 The Billing Project, LLC
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

import java.util.Map;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;

public class AnalyticsConfiguration {

    private final Properties properties;

    // Sane defaults (keys used by most official plugins)
    private final Map<Integer, String> defaultPluginPropertyKeys = ImmutableMap.<Integer, String>of(1, "processorResponse",
                                                                                                    2, "avsResultCode",
                                                                                                    3, "cvvResultCode",
                                                                                                    4, "payment_processor_account_id",
                                                                                                    5, "paymentMethod");

    public AnalyticsConfiguration(final Properties properties) {
        this.properties = properties;
    }

    public String getPluginPropertyKey(final int position, final String pluginName) {
        final String propertyKey = String.format("org.killbill.billing.plugin.analytics.pluginPropertyKey.%s.%s", pluginName, position);
        final String pluginPropertyKey = properties.getProperty(propertyKey);
        return pluginPropertyKey != null ? pluginPropertyKey : defaultPluginPropertyKeys.get(position);
    }
}
