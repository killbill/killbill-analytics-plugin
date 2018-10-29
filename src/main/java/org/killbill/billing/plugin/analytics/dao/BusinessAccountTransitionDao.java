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

import java.util.Collection;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessAccountTransitionFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;
import org.killbill.billing.util.callcontext.CallContext;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessAccountTransitionDao extends BusinessAnalyticsDaoBase {

    private static final Logger logger = LoggerFactory.getLogger(BusinessAccountTransitionDao.class);

    private final BusinessAccountTransitionFactory bosFactory;

    public BusinessAccountTransitionDao(final OSGIKillbillDataSource osgiKillbillDataSource) {
        super(osgiKillbillDataSource);
        bosFactory = new BusinessAccountTransitionFactory();
    }

    public void update(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        logger.debug("Starting rebuild of Analytics account transitions for account {}", businessContextFactory.getAccountId());

        final Collection<BusinessAccountTransitionModelDao> businessAccountTransitions = bosFactory.createBusinessAccountTransitions(businessContextFactory);

        executeInTransaction(new Transaction<Void, BusinessAnalyticsSqlDao>() {
            @Override
            public Void inTransaction(final BusinessAnalyticsSqlDao transactional, final TransactionStatus status) throws Exception {
                updateInTransaction(businessAccountTransitions, transactional, businessContextFactory.getCallContext());
                return null;
            }
        });

        logger.debug("Finished rebuild of Analytics account transitions for account {}", businessContextFactory.getAccountId());
    }

    private void updateInTransaction(final Collection<BusinessAccountTransitionModelDao> businessAccountTransitionModelDaos,
                                     final BusinessAnalyticsSqlDao transactional,
                                     final CallContext context) {
        if (businessAccountTransitionModelDaos.size() == 0) {
            return;
        }

        final BusinessAccountTransitionModelDao firstTransition = businessAccountTransitionModelDaos.iterator().next();
        transactional.deleteByAccountRecordId(firstTransition.getTableName(),
                                              firstTransition.getAccountRecordId(),
                                              firstTransition.getTenantRecordId(),
                                              context);

        for (final BusinessAccountTransitionModelDao transition : businessAccountTransitionModelDaos) {
            transactional.create(transition.getTableName(), transition, context);
        }
    }
}
