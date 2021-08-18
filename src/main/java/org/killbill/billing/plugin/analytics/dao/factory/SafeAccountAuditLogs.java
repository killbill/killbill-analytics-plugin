/*
 * Copyright 2020-2021 Equinix, Inc
 * Copyright 2014-2021 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.dao.factory;

import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.util.api.AuditLevel;
import org.killbill.billing.util.api.AuditUserApi;
import org.killbill.billing.util.audit.AccountAuditLogs;
import org.killbill.billing.util.callcontext.TenantContext;

// Encapsulate logic to fetch *all* audit logs for a given account
//
// Because we fetch one time, and then pull some state (e.g subscriptions) later on, we may not
// have all the audit logs for recently added state. In such case the client can fetch again all the audit logs.
//
class SafeAccountAuditLogs {

    private final UUID accountId;
    private final TenantContext context;
    private final OSGIKillbillAPI osgiKillbillAPI;
    private final ReadWriteLock lock;
    private volatile AccountAuditLogs accountAuditLogs;

    public SafeAccountAuditLogs(final OSGIKillbillAPI osgiKillbillAPI, final UUID accountId, final TenantContext context) {
        this.osgiKillbillAPI = osgiKillbillAPI;
        this.lock = new ReentrantReadWriteLock();
        this.accountId = accountId;
        this.context = context;
    }

    public AccountAuditLogs getAccountAuditLogs(final boolean refresh) throws AnalyticsRefreshException {
        lock.readLock().lock();
        try {
            if (accountAuditLogs == null || refresh) {
                lock.writeLock().lock();
                try {
                    accountAuditLogs = refreshAccountAuditLogs();
                } finally {
                    lock.writeLock().unlock();
                }
            }
            return accountAuditLogs;
        } finally {
            lock.readLock().unlock();
        }
    }

    private AccountAuditLogs refreshAccountAuditLogs() throws AnalyticsRefreshException {
        final AuditUserApi auditUserApi = osgiKillbillAPI.getAuditUserApi();
        if (auditUserApi == null) {
            throw new AnalyticsRefreshException("Error retrieving auditUserApi");
        }
        return auditUserApi.getAccountAuditLogs(accountId, AuditLevel.MINIMAL, context);
    }

}
