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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.util.api.AuditLevel;
import org.killbill.billing.util.api.AuditUserApi;
import org.killbill.billing.util.audit.AccountAuditLogs;
import org.killbill.billing.util.callcontext.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Encapsulate logic to fetch *all* audit logs for a given account
//
// Because we fetch one time, and then pull some state (e.g subscriptions) later on, we may not
// have all the audit logs for recently added state. In such case the client can fetch again all the audit logs.
//
class SafeAccountAuditLogs {

    private static final long WAIT_TIME_MS = 20;
    private static final int MAX_ATTEMPTS = 10;
    // More than LOG_THRESHOLD refresh we log an entry
    private static final int LOG_THRESHOLD = 10;

    private static final Logger logger = LoggerFactory.getLogger(SafeAccountAuditLogs.class);

    private final UUID accountId;
    private final TenantContext context;
    private final OSGIKillbillAPI osgiKillbillAPI;

    private final AtomicBoolean isRefreshing;
    private volatile AccountAuditLogs accountAuditLogs;
    // Only for logging to see what's happening
    private final AtomicInteger nbRefresh;

    public SafeAccountAuditLogs(final OSGIKillbillAPI osgiKillbillAPI, final UUID accountId, final TenantContext context) {
        this.osgiKillbillAPI = osgiKillbillAPI;
        this.accountId = accountId;
        this.context = context;
        this.isRefreshing = new AtomicBoolean(false);
        this.nbRefresh = new AtomicInteger(0);
    }

    public AccountAuditLogs getAccountAuditLogs(final boolean refresh) throws AnalyticsRefreshException {
        if (accountAuditLogs == null || refresh) {
            accountAuditLogs = getAccountAuditLogsWithRetry();
        }
        if (accountAuditLogs == null) {
            throw new AnalyticsRefreshException(String.format("Failed to fetch all account audit logs for account %s", accountId));
        }

        final int refreshCnt = nbRefresh.get();
        if (refreshCnt >= LOG_THRESHOLD) {
            if (refreshCnt % LOG_THRESHOLD == 0) {
                logger.warn("Detected {} audit logs full refresh for accountId={}", refreshCnt, accountId);
            }
        }
        return accountAuditLogs;
    }

    private AccountAuditLogs getAccountAuditLogsWithRetry() throws AnalyticsRefreshException {
        int leftAttempts = MAX_ATTEMPTS;
        do {
            final boolean doRefresh = isRefreshing.compareAndSet(false, true);
            if (doRefresh) {
                try {
                    return refreshAccountAuditLogs();
                } finally {
                    nbRefresh.incrementAndGet();
                    isRefreshing.set(false);
                }
            } else {
                try {
                    Thread.sleep(WAIT_TIME_MS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AnalyticsRefreshException(e);
                }
            }
        } while (leftAttempts-- > 0);
        return null;
    }

    private AccountAuditLogs refreshAccountAuditLogs() throws AnalyticsRefreshException {
        final AuditUserApi auditUserApi = getAuditUserApi();
        return auditUserApi.getAccountAuditLogs(accountId, AuditLevel.MINIMAL, context);
    }

    public TenantContext getContext() {
        return context;
    }

    public AuditUserApi getAuditUserApi() throws AnalyticsRefreshException {
        final AuditUserApi auditUserApi = osgiKillbillAPI.getAuditUserApi();
        if (auditUserApi == null) {
            throw new AnalyticsRefreshException("Error retrieving auditUserApi");
        }
        return auditUserApi;
    }
}
