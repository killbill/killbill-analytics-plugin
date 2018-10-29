/*
 * Copyright 2010-2014 Ning, Inc.
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

package org.killbill.billing.plugin.analytics.dao;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessFieldFactory;
import org.killbill.billing.plugin.analytics.dao.model.BusinessFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaosWithAccountAndTenantRecordId;
import org.killbill.billing.util.callcontext.CallContext;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessFieldDao extends BusinessAnalyticsDaoBase {

    private static final Logger logger = LoggerFactory.getLogger(BusinessAnalyticsDaoBase.class);

    private final BusinessFieldFactory bFieldFactory;

    public BusinessFieldDao(final OSGIKillbillDataSource osgiKillbillDataSource) {
        super(osgiKillbillDataSource);
        bFieldFactory = new BusinessFieldFactory();
    }

    public void update(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        logger.debug("Starting rebuild of Analytics custom fields for account {}", businessContextFactory.getAccountId());

        final BusinessModelDaosWithAccountAndTenantRecordId<BusinessFieldModelDao> fieldModelDaos = bFieldFactory.createBusinessFields(businessContextFactory);

        executeInTransaction(new Transaction<Void, BusinessAnalyticsSqlDao>() {
            @Override
            public Void inTransaction(final BusinessAnalyticsSqlDao transactional, final TransactionStatus status) throws Exception {
                updateInTransaction(fieldModelDaos, transactional, businessContextFactory.getCallContext());
                return null;
            }
        });

        logger.debug("Finished rebuild of Analytics custom fields for account {}", businessContextFactory.getAccountId());
    }

    private void updateInTransaction(final BusinessModelDaosWithAccountAndTenantRecordId<BusinessFieldModelDao> fieldModelDaos,
                                     final BusinessAnalyticsSqlDao transactional,
                                     final CallContext context) {
        for (final String tableName : BusinessFieldModelDao.ALL_FIELDS_TABLE_NAMES) {
            transactional.deleteByAccountRecordId(tableName,
                                                  fieldModelDaos.getAccountRecordId(),
                                                  fieldModelDaos.getTenantRecordId(),
                                                  context);
        }

        for (final BusinessFieldModelDao fieldModelDao : fieldModelDaos.getBusinessModelDaos()) {
            transactional.create(fieldModelDao.getTableName(), fieldModelDao, context);
        }
    }
}
