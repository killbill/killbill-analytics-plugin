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
import org.killbill.billing.plugin.analytics.dao.factory.BusinessAccountFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.util.callcontext.CallContext;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessAccountDao extends BusinessAnalyticsDaoBase {

    private static final Logger logger = LoggerFactory.getLogger(BusinessAccountDao.class);

    private final BusinessAccountFactory bacFactory;

    public BusinessAccountDao(final OSGIKillbillDataSource osgiKillbillDataSource) {
        super(osgiKillbillDataSource);
        bacFactory = new BusinessAccountFactory();
    }

    public void update(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        logger.debug("Starting rebuild of Analytics account for account {}", businessContextFactory.getAccountId());

        // Recompute the account record
        final BusinessAccountModelDao bac = bacFactory.createBusinessAccount(businessContextFactory);

        executeInTransaction(new Transaction<Void, BusinessAnalyticsSqlDao>() {
            @Override
            public Void inTransaction(final BusinessAnalyticsSqlDao transactional, final TransactionStatus status) throws Exception {
                updateInTransaction(bac, transactional, businessContextFactory.getCallContext());
                return null;
            }
        });

        logger.debug("Finished rebuild of Analytics account for account {}", businessContextFactory.getAccountId());
    }

    // Note: computing the BusinessAccountModelDao object is fairly expensive, hence should be done outside of the transaction
    public void updateInTransaction(final BusinessAccountModelDao bac,
                                    final BusinessAnalyticsSqlDao transactional,
                                    final CallContext context) {
        transactional.deleteByAccountRecordId(bac.getTableName(), bac.getAccountRecordId(), bac.getTenantRecordId(), context);
        transactional.create(bac.getTableName(), bac, context);
    }
}
