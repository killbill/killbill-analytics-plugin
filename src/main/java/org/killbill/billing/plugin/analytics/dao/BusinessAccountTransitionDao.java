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

package org.killbill.billing.plugin.analytics.dao;

import java.util.Collection;

import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessAccountTransitionFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;
import org.osgi.service.log.LogService;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;

public class BusinessAccountTransitionDao extends BusinessAnalyticsDaoBase {

    private final LogService logService;
    private final BusinessAccountTransitionFactory bosFactory;

    public BusinessAccountTransitionDao(final OSGIKillbillLogService logService,
                                        final OSGIKillbillDataSource osgiKillbillDataSource) {
        super(logService, osgiKillbillDataSource);
        this.logService = logService;
        bosFactory = new BusinessAccountTransitionFactory();
    }

    public void update(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        logService.log(LogService.LOG_INFO, "Starting rebuild of Analytics account transitions for account " + businessContextFactory.getAccountId());

        final Collection<BusinessAccountTransitionModelDao> businessAccountTransitions = bosFactory.createBusinessAccountTransitions(businessContextFactory);

        executeInTransaction(new Transaction<Void, BusinessAnalyticsSqlDao>() {
            @Override
            public Void inTransaction(final BusinessAnalyticsSqlDao transactional, final TransactionStatus status) throws Exception {
                updateInTransaction(businessAccountTransitions, transactional, businessContextFactory.getCallContext());
                return null;
            }
        });

        logService.log(LogService.LOG_INFO, "Finished rebuild of Analytics account transitions for account " + businessContextFactory.getAccountId());
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
