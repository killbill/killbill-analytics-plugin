/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.api.user;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.currency.plugin.api.CurrencyPluginApi;
import org.killbill.billing.notification.plugin.api.ExtBusEventType;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIMetricRegistry;
import org.killbill.billing.plugin.analytics.AnalyticsJob;
import org.killbill.billing.plugin.analytics.AnalyticsJobHierarchy.Group;
import org.killbill.billing.plugin.analytics.AnalyticsListener;
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
import org.killbill.billing.plugin.analytics.api.RefreshResult;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfigurationHandler;
import org.killbill.billing.plugin.analytics.dao.AllBusinessObjectsDao;
import org.killbill.billing.plugin.analytics.dao.AnalyticsDao;
import org.killbill.billing.plugin.analytics.dao.CurrencyConversionDao;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.entity.Pagination;
import org.killbill.clock.Clock;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsUserApi {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsUserApi.class);

    private final OSGIKillbillAPI osgiKillbillAPI;
    private final OSGIConfigPropertiesService osgiConfigPropertiesService;
    private final Clock clock;
    private final AnalyticsConfigurationHandler analyticsConfigurationHandler;
    private final AnalyticsDao analyticsDao;
    private final AllBusinessObjectsDao allBusinessObjectsDao;
    private final ServiceTracker<CurrencyPluginApi, CurrencyPluginApi> currencyPluginApiServiceTracker;
    private final CurrencyConversionDao currencyConversionDao;
    private final AnalyticsListener analyticsListener;

    public AnalyticsUserApi(final OSGIKillbillAPI osgiKillbillAPI,
                            final OSGIKillbillDataSource osgiKillbillDataSource,
                            final OSGIMetricRegistry metricRegistry,
                            final OSGIConfigPropertiesService osgiConfigPropertiesService,
                            final ServiceTracker<CurrencyPluginApi, CurrencyPluginApi> currencyPluginApiServiceTracker,
                            final Executor executor,
                            final Clock clock,
                            final AnalyticsConfigurationHandler analyticsConfigurationHandler,
                            final AnalyticsListener analyticsListener) {
        this.osgiKillbillAPI = osgiKillbillAPI;
        this.osgiConfigPropertiesService = osgiConfigPropertiesService;
        this.currencyPluginApiServiceTracker = currencyPluginApiServiceTracker;
        this.clock = clock;
        this.analyticsConfigurationHandler = analyticsConfigurationHandler;
        this.analyticsDao = new AnalyticsDao(osgiKillbillAPI, osgiKillbillDataSource, metricRegistry);
        this.allBusinessObjectsDao = new AllBusinessObjectsDao(osgiKillbillDataSource, metricRegistry, executor);
        this.currencyConversionDao = new CurrencyConversionDao(osgiKillbillDataSource, metricRegistry);
        this.analyticsListener = analyticsListener;
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
        final BusinessContextFactory businessContextFactory = new BusinessContextFactory(accountId, context, currencyPluginApiServiceTracker, currencyConversionDao, osgiKillbillAPI, osgiConfigPropertiesService, clock, analyticsConfigurationHandler);
        logger.info("Starting Analytics refresh for account {}", businessContextFactory.getAccountId());
        // TODO Should we take the account lock?
        allBusinessObjectsDao.update(businessContextFactory);
        logger.info("Finished Analytics refresh for account {}", businessContextFactory.getAccountId());
    }

    public RefreshResult rebuildAnalyticsForAllAccounts(final CallContext context) {
        logger.info("Starting Analytics refresh for all accounts ");
        Pagination<Account> accounts = osgiKillbillAPI.getAccountUserApi().getAccounts(0L, 100L, context);
        logger.info("Total accounts to be refreshed {} ",accounts.getTotalNbRecords());
        RefreshResult refreshResult = rebuildAnalyticsForAccounts(accounts, new RefreshResult(accounts.getTotalNbRecords(), 0L), context);
        Long nextOffSet = accounts.getNextOffset();
        while (nextOffSet != null) {
            accounts = osgiKillbillAPI.getAccountUserApi().getAccounts(nextOffSet, 100L, context);
            refreshResult = rebuildAnalyticsForAccounts(accounts, refreshResult, context);
            nextOffSet = accounts.getNextOffset();
        }

        logger.info("Finished Analytics refresh for all accounts");
        return refreshResult;
        
    }

    private RefreshResult rebuildAnalyticsForAccounts(final Pagination<Account> accounts, RefreshResult refreshResult, final CallContext context) {
        final Iterator<Account> accountsItr = accounts.iterator();
        Long nbRefreshes = refreshResult.getNbRefreshes();
        try {
            while (accountsItr.hasNext()) {
                final UUID accountId = accountsItr.next().getId();
                final AnalyticsJob analyticsJob = new AnalyticsJob(Group.ALL, ExtBusEventType.ACCOUNT_CHANGE, ObjectType.ACCOUNT, null, accountId, context.getTenantId());
                analyticsListener.scheduleAnalyticsJob(analyticsJob, analyticsConfigurationHandler.getConfigurable(context.getTenantId()));
                nbRefreshes++;
            }
        } finally {
            try {
                accounts.close();
            } catch (final IOException e) {
                logger.error("Exception while rebuilding analytics for accounts");
            }
        }
        
        return new RefreshResult(refreshResult.getNbAccounts(),nbRefreshes);
    }

}
