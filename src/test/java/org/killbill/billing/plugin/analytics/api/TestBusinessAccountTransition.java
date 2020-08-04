/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.api;

import org.joda.time.LocalDate;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestBusinessAccountTransition extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        final LocalDate startDate = new LocalDate(2005, 2, 5);
        final LocalDate endDate = new LocalDate(2005, 6, 5);
        final BusinessAccountTransitionModelDao businessAccountTransitionModelDao = new BusinessAccountTransitionModelDao(account,
                                                                                                                          accountRecordId,
                                                                                                                          serviceName,
                                                                                                                          stateName,
                                                                                                                          startDate,
                                                                                                                          blockingStateRecordId,
                                                                                                                          endDate,
                                                                                                                          auditLog,
                                                                                                                          tenantRecordId,
                                                                                                                          reportGroup);
        final BusinessAccountTransition businessAccountTransition = new BusinessAccountTransition(businessAccountTransitionModelDao);
        verifyBusinessEntityBase(businessAccountTransition);
        Assert.assertEquals(businessAccountTransition.getCreatedDate().compareTo(businessAccountTransitionModelDao.getCreatedDate()), 0);
        Assert.assertEquals(businessAccountTransition.getService(), businessAccountTransitionModelDao.getService());
        Assert.assertEquals(businessAccountTransition.getState(), businessAccountTransitionModelDao.getState());
        Assert.assertEquals(businessAccountTransition.getStartDate().compareTo(businessAccountTransitionModelDao.getStartDate()), 0);
        Assert.assertEquals(businessAccountTransition.getEndDate().compareTo(businessAccountTransitionModelDao.getEndDate()), 0);
    }
}
