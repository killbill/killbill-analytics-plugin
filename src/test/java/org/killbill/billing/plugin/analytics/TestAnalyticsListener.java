/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2019 Groupon, Inc
 * Copyright 2014-2019 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics;

import java.util.Properties;
import java.util.UUID;

import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestAnalyticsListener extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testBlacklist() throws Exception {
        final AnalyticsListener analyticsListener = new AnalyticsListener(killbillAPI,
                                                                          killbillDataSource,
                                                                          osgiConfigPropertiesService,
                                                                          null,
                                                                          locker,
                                                                          clock,
                                                                          analyticsConfigurationHandler,
                                                                          notificationQueueService);

        // Other accounts are blacklisted
        Assert.assertFalse(analyticsListener.isAccountBlacklisted(UUID.randomUUID()));

        // Blacklist
        Assert.assertTrue(analyticsListener.isAccountBlacklisted(blackListedAccountId));
    }

    @Test(groups = "fast")
    public void testIgnoredGroups() throws Exception {
        final ExtBusEvent cfEvent = Mockito.mock(ExtBusEvent.class);
        Mockito.when(cfEvent.getEventType()).thenReturn(ExtBusEventType.CUSTOM_FIELD_CREATION);

        final ExtBusEvent accountEvent = Mockito.mock(ExtBusEvent.class);
        Mockito.when(accountEvent.getEventType()).thenReturn(ExtBusEventType.ACCOUNT_CREATION);

        final Properties properties = new Properties();
        properties.setProperty(AnalyticsListener.ANALYTICS_IGNORED_GROUPS_PROPERTY, "FIELDS");
        final OSGIConfigPropertiesService osgiConfigPropertiesService = Mockito.mock(OSGIConfigPropertiesService.class);
        Mockito.when(osgiConfigPropertiesService.getProperties()).thenReturn(properties);
        Mockito.when(osgiConfigPropertiesService.getString(Mockito.<String>any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return properties.getProperty((String) invocation.getArguments()[0]);
            }
        });

        final AnalyticsListener analyticsListener = new AnalyticsListener(killbillAPI,
                                                                          killbillDataSource,
                                                                          osgiConfigPropertiesService,
                                                                          null,
                                                                          locker,
                                                                          clock,
                                                                          analyticsConfigurationHandler,
                                                                          notificationQueueService);

        Assert.assertTrue(analyticsListener.shouldIgnoreEvent(new AnalyticsJob(cfEvent)));
        Assert.assertFalse(analyticsListener.shouldIgnoreEvent(new AnalyticsJob(accountEvent)));
    }
}
