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

package com.ning.billing.osgi.bundles.analytics.dao;

import java.util.Collection;
import java.util.UUID;

import org.osgi.service.log.LogService;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;

import com.ning.billing.clock.Clock;
import com.ning.billing.osgi.bundles.analytics.AnalyticsRefreshException;
import com.ning.billing.osgi.bundles.analytics.dao.factory.BusinessAccountTransitionFactory;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessAccountTransitionModelDao;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillLogService;

public class BusinessAccountTransitionDao extends BusinessAnalyticsDaoBase {

    private final LogService logService;
    private final BusinessAccountTransitionFactory bosFactory;

    public BusinessAccountTransitionDao(final OSGIKillbillLogService logService,
                                        final OSGIKillbillAPI osgiKillbillAPI,
                                        final OSGIKillbillDataSource osgiKillbillDataSource,
                                        final Clock clock) {
        super(logService, osgiKillbillDataSource);
        this.logService = logService;
        bosFactory = new BusinessAccountTransitionFactory(logService, osgiKillbillAPI, osgiKillbillDataSource, clock);
    }

    public void update(final UUID accountId, final CallContext context) throws AnalyticsRefreshException {
        logService.log(LogService.LOG_INFO, "Starting rebuild of Analytics account transitions for account " + accountId);

        final Collection<BusinessAccountTransitionModelDao> businessAccountTransitions = bosFactory.createBusinessAccountTransitions(accountId, context);

        sqlDao.inTransaction(new Transaction<Void, BusinessAnalyticsSqlDao>() {
            @Override
            public Void inTransaction(final BusinessAnalyticsSqlDao transactional, final TransactionStatus status) throws Exception {
                updateInTransaction(businessAccountTransitions, transactional, context);
                return null;
            }
        });

        logService.log(LogService.LOG_INFO, "Finished rebuild of Analytics account transitions for account " + accountId);
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
