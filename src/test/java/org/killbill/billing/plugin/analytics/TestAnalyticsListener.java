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

package org.killbill.billing.plugin.analytics;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAnalyticsListener extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testBlacklist() throws Exception {
        AnalyticsListener analyticsListener = new AnalyticsListener(logService, killbillAPI, killbillDataSource, osgiConfigPropertiesService, null, clock, notificationQueueService);

        // No account is blacklisted
        Assert.assertFalse(analyticsListener.isAccountBlacklisted(UUID.randomUUID()));

        analyticsListener = new AnalyticsListener(logService, killbillAPI, killbillDataSource, osgiConfigPropertiesService, null, clock, notificationQueueService);

        // Other accounts are blacklisted
        Assert.assertFalse(analyticsListener.isAccountBlacklisted(UUID.randomUUID()));

        // Blacklist
        Assert.assertTrue(analyticsListener.isAccountBlacklisted(blackListedAccountId));
    }
}
