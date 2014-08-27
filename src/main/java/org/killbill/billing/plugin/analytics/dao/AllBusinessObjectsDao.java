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

import java.util.UUID;
import java.util.concurrent.Executor;

import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.util.audit.AccountAuditLogs;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.clock.Clock;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;
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

        final BusinessAccountDao bacDao = new BusinessAccountDao(logService, osgiKillbillAPI, osgiKillbillDataSource, clock);
        this.bstDao = new BusinessSubscriptionTransitionDao(logService, osgiKillbillAPI, osgiKillbillDataSource, bacDao, executor, clock);
        this.binAndBipDao = new BusinessInvoiceAndPaymentDao(logService, osgiKillbillAPI, osgiKillbillDataSource, bacDao, executor, clock);
        this.bosDao = new BusinessAccountTransitionDao(logService, osgiKillbillAPI, osgiKillbillDataSource, clock);
        this.bFieldDao = new BusinessFieldDao(logService, osgiKillbillAPI, osgiKillbillDataSource, clock);
        this.bTagDao = new BusinessTagDao(logService, osgiKillbillAPI, osgiKillbillDataSource, clock);
    }

    // TODO: each refresh is done in a transaction - do we want to share a long running transaction across all refreshes?
    public void update(final UUID accountId, final AccountAuditLogs accountAuditLogs, final CallContext context) throws AnalyticsRefreshException {
        logService.log(LogService.LOG_INFO, "Starting rebuild of Analytics for account " + accountId);

        // Refresh invoices and payments. This will automatically trigger a refresh of account
        binAndBipDao.update(accountId, accountAuditLogs, context);

        // Refresh subscription transitions
        bstDao.update(accountId, accountAuditLogs, context);

        // Refresh tags
        bTagDao.update(accountId, accountAuditLogs, context);

        // Refresh fields
        bFieldDao.update(accountId, accountAuditLogs, context);

        // Refresh account transitions
        bosDao.update(accountId, accountAuditLogs, context);

        logService.log(LogService.LOG_INFO, "Finished rebuild of Analytics for account " + accountId);
    }
}
