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

package org.killbill.billing.plugin.analytics.api.user;

import java.math.BigDecimal;

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteWithEmbeddedDB;
import org.killbill.billing.plugin.analytics.BusinessExecutor;
import org.killbill.billing.plugin.analytics.api.BusinessAccount;
import org.killbill.billing.plugin.analytics.api.BusinessSnapshot;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDefaultAnalyticsUserApi extends AnalyticsTestSuiteWithEmbeddedDB {

    @Test(groups = "slow")
    public void testAccountSnapshot() throws Exception {
        final BusinessAccountModelDao accountModelDao = new BusinessAccountModelDao(account,
                                                                                    parentAccount,
                                                                                    accountRecordId,
                                                                                    BigDecimal.ONE,
                                                                                    invoice,
                                                                                    invoice,
                                                                                    paymentTransaction,
                                                                                    3,
                                                                                    currencyConverter,
                                                                                    auditLog,
                                                                                    tenantRecordId,
                                                                                    reportGroup);
        analyticsSqlDao.create(accountModelDao.getTableName(), accountModelDao, callContext);

        final AnalyticsUserApi analyticsUserApi = new AnalyticsUserApi(logService, killbillAPI, killbillDataSource, osgiConfigPropertiesService, BusinessExecutor.newCachedThreadPool(osgiConfigPropertiesService), clock, analyticsConfigurationHandler);
        final BusinessSnapshot businessSnapshot = analyticsUserApi.getBusinessSnapshot(account.getId(), callContext);
        Assert.assertEquals(businessSnapshot.getBusinessAccount(), new BusinessAccount(accountModelDao));
    }
}
