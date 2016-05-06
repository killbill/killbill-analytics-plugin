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

import java.util.concurrent.Executor;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillLogService;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.clock.Clock;
import org.osgi.service.log.LogService;

public class AllBusinessObjectsDao {

    private final LogService logService;
    private final BusinessSubscriptionTransitionDao bstDao;
    private final BusinessInvoiceAndPaymentDao binAndBipDao;
    private final BusinessAccountTransitionDao bosDao;
    private final BusinessFieldDao bFieldDao;
    private final BusinessTagDao bTagDao;

    public AllBusinessObjectsDao(final OSGIKillbillLogService logService,
                                 final OSGIKillbillAPI osgiKillbillAPI,
                                 final OSGIKillbillDataSource osgiKillbillDataSource,
                                 final Executor executor,
                                 final Clock clock) {
        this.logService = logService;

        final BusinessAccountDao bacDao = new BusinessAccountDao(logService, osgiKillbillDataSource);
        this.bstDao = new BusinessSubscriptionTransitionDao(logService, osgiKillbillDataSource, bacDao, executor);
        this.binAndBipDao = new BusinessInvoiceAndPaymentDao(logService, osgiKillbillDataSource, bacDao, executor);
        this.bosDao = new BusinessAccountTransitionDao(logService, osgiKillbillDataSource);
        this.bFieldDao = new BusinessFieldDao(logService, osgiKillbillDataSource);
        this.bTagDao = new BusinessTagDao(logService, osgiKillbillDataSource);
    }

    // TODO: each refresh is done in a transaction - do we want to share a long running transaction across all refreshes?
    public void update(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        logService.log(LogService.LOG_DEBUG, "Starting rebuild of Analytics for account " + businessContextFactory.getAccountId());

        // Refresh invoices and payments. This will automatically trigger a refresh of account
        binAndBipDao.update(businessContextFactory);

        // Refresh subscription transitions
        bstDao.update(businessContextFactory);

        // Refresh tags
        bTagDao.update(businessContextFactory);

        // Refresh fields
        bFieldDao.update(businessContextFactory);

        // Refresh account transitions
        bosDao.update(businessContextFactory);

        logService.log(LogService.LOG_DEBUG, "Finished rebuild of Analytics for account " + businessContextFactory.getAccountId());
    }
}
