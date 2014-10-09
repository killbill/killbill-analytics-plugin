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

import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionIsolationLevel;

public class BusinessAnalyticsDaoBase {

    protected final OSGIKillbillLogService logService;
    protected final BusinessAnalyticsSqlDao sqlDao;

    public BusinessAnalyticsDaoBase(final OSGIKillbillLogService logService, final OSGIKillbillDataSource osgiKillbillDataSource) {
        final DBI dbi = BusinessDBIProvider.get(osgiKillbillDataSource.getDataSource());
        sqlDao = dbi.onDemand(BusinessAnalyticsSqlDao.class);
        this.logService = logService;
    }

    public void executeInTransaction(final Transaction<Void, BusinessAnalyticsSqlDao> transaction) {
        // In REPEATABLE READ, every lock acquired during a transaction is held for the duration of the transaction.
        // In READ COMMITTED the locks that did not match the scan are released after the STATEMENT completes.
        // We need to make sure to use READ COMMITTED here, to avoid MySQL deadlocks under high load. This should
        // not have any impact in these transactions as we only delete & re-insert rows on a per account basis,
        // and accounts are not updated in parallel (not enforced, but we try hard not to).
        sqlDao.inTransaction(TransactionIsolationLevel.READ_COMMITTED, transaction);
    }
}
