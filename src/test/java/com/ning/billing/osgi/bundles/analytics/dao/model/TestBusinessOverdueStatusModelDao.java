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

package com.ning.billing.osgi.bundles.analytics.dao.model;

import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteNoDB;

public class TestBusinessOverdueStatusModelDao extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testEquals() throws Exception {
        final LocalDate startDate = new LocalDate(2012, 6, 21);
        final LocalDate endDate = new LocalDate(2012, 7, 21);
        final BusinessOverdueStatusModelDao overdueStatusModelDao = new BusinessOverdueStatusModelDao(account,
                                                                                                      accountRecordId,
                                                                                                      blockingStateName,
                                                                                                      startDate,
                                                                                                      blockingStateRecordId,
                                                                                                      endDate,
                                                                                                      auditLog,
                                                                                                      tenantRecordId,
                                                                                                      reportGroup);
        verifyBusinessModelDaoBase(overdueStatusModelDao, accountRecordId, tenantRecordId);
        Assert.assertEquals(overdueStatusModelDao.getCreatedDate(), auditLog.getCreatedDate());
        Assert.assertEquals(overdueStatusModelDao.getBlockingStateRecordId(), blockingStateRecordId);
        Assert.assertEquals(overdueStatusModelDao.getStatus(), blockingStateName);
        Assert.assertEquals(overdueStatusModelDao.getStartDate(), startDate);
        Assert.assertEquals(overdueStatusModelDao.getEndDate(), endDate);
    }
}
