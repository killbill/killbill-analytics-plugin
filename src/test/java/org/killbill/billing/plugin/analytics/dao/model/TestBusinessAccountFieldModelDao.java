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

package org.killbill.billing.plugin.analytics.dao.model;

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestBusinessAccountFieldModelDao extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConstructor() throws Exception {
        final BusinessAccountFieldModelDao accountFieldModelDao = new BusinessAccountFieldModelDao(account,
                                                                                                   accountRecordId,
                                                                                                   customField,
                                                                                                   fieldRecordId,
                                                                                                   auditLog,
                                                                                                   tenantRecordId,
                                                                                                   reportGroup);
        verifyBusinessModelDaoBase(accountFieldModelDao, accountRecordId, tenantRecordId);
        Assert.assertEquals(accountFieldModelDao.getCreatedDate(), customField.getCreatedDate());
        Assert.assertEquals(accountFieldModelDao.getCustomFieldRecordId(), fieldRecordId);
        Assert.assertEquals(accountFieldModelDao.getName(), customField.getFieldName());
        Assert.assertEquals(accountFieldModelDao.getValue(), customField.getFieldValue());
    }
}
