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

package org.killbill.billing.plugin.analytics.api.user;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.api.BusinessAccount;
import org.killbill.billing.plugin.analytics.api.BusinessAccountTransition;
import org.killbill.billing.plugin.analytics.api.BusinessBundle;
import org.killbill.billing.plugin.analytics.api.BusinessField;
import org.killbill.billing.plugin.analytics.api.BusinessInvoice;
import org.killbill.billing.plugin.analytics.api.BusinessPayment;
import org.killbill.billing.plugin.analytics.api.BusinessSnapshot;
import org.killbill.billing.plugin.analytics.api.BusinessSubscriptionTransition;
import org.killbill.billing.plugin.analytics.api.BusinessTag;
import org.killbill.billing.plugin.analytics.dao.AllBusinessObjectsDao;
import org.killbill.billing.plugin.analytics.dao.AnalyticsDao;
import org.killbill.billing.util.api.AuditLevel;
import org.killbill.billing.util.api.AuditUserApi;
import org.killbill.billing.util.audit.AccountAuditLogs;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.clock.Clock;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;

public class AnalyticsUserApi {

    private final OSGIKillbillAPI osgiKillbillAPI;
    private final AnalyticsDao analyticsDao;
    private final AllBusinessObjectsDao allBusinessObjectsDao;

    public AnalyticsUserApi(final OSGIKillbillLogService logService,
                            final OSGIKillbillAPI osgiKillbillAPI,
                            final OSGIKillbillDataSource osgiKillbillDataSource,
                            final Executor executor,
                            final Clock clock) {
        this.osgiKillbillAPI = osgiKillbillAPI;
        this.analyticsDao = new AnalyticsDao(logService, osgiKillbillAPI, osgiKillbillDataSource);
        this.allBusinessObjectsDao = new AllBusinessObjectsDao(logService, osgiKillbillAPI, osgiKillbillDataSource, executor, clock);
    }

    public BusinessSnapshot getBusinessSnapshot(final UUID accountId, final TenantContext context) {
        // Find account
        final BusinessAccount businessAccount = analyticsDao.getAccountById(accountId, context);
        final Collection<BusinessAccountTransition> businessAccountTransitions = analyticsDao.getAccountTransitionsForAccount(accountId, context);

        // Find all bundles
        final Collection<BusinessBundle> businessBundles = analyticsDao.getBundlesForAccount(accountId, context);

        // Find all transitions for all bundles for that account
        final Collection<BusinessSubscriptionTransition> businessSubscriptionTransitions = analyticsDao.getSubscriptionTransitionsForAccount(accountId, context);

        // Find all invoices for that account
        final Collection<BusinessInvoice> businessInvoices = analyticsDao.getInvoicesForAccount(accountId, context);

        // Find all payments for that account
        final Collection<BusinessPayment> businessPayments = analyticsDao.getInvoicePaymentsForAccount(accountId, context);

        // Find all tags for that account
        final Collection<BusinessTag> businessTags = analyticsDao.getTagsForAccount(accountId, context);

        // Find all fields for that account
        final Collection<BusinessField> businessFields = analyticsDao.getFieldsForAccount(accountId, context);

        return new BusinessSnapshot(businessAccount,
                                    businessBundles,
                                    businessSubscriptionTransitions,
                                    businessInvoices,
                                    businessPayments,
                                    businessAccountTransitions,
                                    businessTags,
                                    businessFields);
    }

    public void rebuildAnalyticsForAccount(final UUID accountId, final CallContext context) throws AnalyticsRefreshException {
        final AccountAuditLogs accountAuditLogs = getAuditUserApi().getAccountAuditLogs(accountId, AuditLevel.MINIMAL, context);
        // TODO Should we take the account lock?
        allBusinessObjectsDao.update(accountId, accountAuditLogs, context);
    }

    private AuditUserApi getAuditUserApi() throws AnalyticsRefreshException {
        final AuditUserApi auditUserApi = osgiKillbillAPI.getAuditUserApi();
        if (auditUserApi == null) {
            throw new AnalyticsRefreshException("Error retrieving auditUserApi");
        }
        return auditUserApi;
    }
}
