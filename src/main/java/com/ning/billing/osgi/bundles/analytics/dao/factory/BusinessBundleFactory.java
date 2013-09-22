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

package com.ning.billing.osgi.bundles.analytics.dao.factory;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

import org.joda.time.LocalDate;

import com.ning.billing.account.api.Account;
import com.ning.billing.catalog.api.ProductCategory;
import com.ning.billing.clock.Clock;
import com.ning.billing.entitlement.api.Subscription;
import com.ning.billing.entitlement.api.SubscriptionBundle;
import com.ning.billing.osgi.bundles.analytics.AnalyticsRefreshException;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessBundleModelDao;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import com.ning.billing.osgi.bundles.analytics.utils.CurrencyConverter;
import com.ning.billing.util.audit.AuditLog;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillLogService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class BusinessBundleFactory extends BusinessFactoryBase {

    private final Executor executor;

    public BusinessBundleFactory(final OSGIKillbillLogService logService,
                                 final OSGIKillbillAPI osgiKillbillAPI,
                                 final OSGIKillbillDataSource osgiKillbillDataSource,
                                 final Executor executor,
                                 final Clock clock) {
        super(logService, osgiKillbillAPI, osgiKillbillDataSource, clock);
        this.executor = executor;
    }

    public Collection<BusinessBundleModelDao> createBusinessBundles(final UUID accountId,
                                                                    final Long accountRecordId,
                                                                    // Correctly ordered
                                                                    final Collection<BusinessSubscriptionTransitionModelDao> sortedBsts,
                                                                    final Long tenantRecordId,
                                                                    final CallContext context) throws AnalyticsRefreshException {
        final Account account = getAccount(accountId, context);
        final ReportGroup reportGroup = getReportGroup(account.getId(), context);

        // Lookup once all SubscriptionBundle for that account (this avoids expensive lookups for each bundle)
        final Set<UUID> baseSubscriptionIds = new HashSet<UUID>();
        final Map<UUID, SubscriptionBundle> bundles = new LinkedHashMap<UUID, SubscriptionBundle>();
        final List<SubscriptionBundle> bundlesForAccount = getSubscriptionBundlesForAccount(account.getId(), context);
        for (final SubscriptionBundle bundle : bundlesForAccount) {
            for (final Subscription subscription : bundle.getSubscriptions()) {
                baseSubscriptionIds.add(subscription.getBaseEntitlementId());
                bundles.put(bundle.getId(), bundle);
            }
        }

        final Map<UUID, Integer> rankForBundle = new LinkedHashMap<UUID, Integer>();
        final Map<UUID, BusinessSubscriptionTransitionModelDao> bstForBundle = new LinkedHashMap<UUID, BusinessSubscriptionTransitionModelDao>();
        filterBstsForBasePlans(sortedBsts, baseSubscriptionIds, rankForBundle, bstForBundle);

        // We fetch the bundles in parallel as these can be very large on a per account basis (@see BusinessSubscriptionTransitionFactory)
        final CompletionService<BusinessBundleModelDao> completionService = new ExecutorCompletionService<BusinessBundleModelDao>(executor);
        final Collection<BusinessBundleModelDao> bbss = new LinkedList<BusinessBundleModelDao>();
        for (final BusinessSubscriptionTransitionModelDao bst : bstForBundle.values()) {
            completionService.submit(new Callable<BusinessBundleModelDao>() {
                @Override
                public BusinessBundleModelDao call() throws Exception {
                    return buildBBS(account,
                                    accountRecordId,
                                    bundles,
                                    bst,
                                    rankForBundle.get(bst.getBundleId()),
                                    tenantRecordId,
                                    reportGroup,
                                    context);
                }
            });
        }
        for (final BusinessSubscriptionTransitionModelDao ignored : bstForBundle.values()) {
            try {
                bbss.add(completionService.take().get());
            } catch (InterruptedException e) {
                throw new AnalyticsRefreshException(e);
            } catch (ExecutionException e) {
                throw new AnalyticsRefreshException(e);
            }
        }

        return bbss;
    }

    @VisibleForTesting
    void filterBstsForBasePlans(final Collection<BusinessSubscriptionTransitionModelDao> sortedBundlesBst, final Set<UUID> baseSubscriptionIds, final Map<UUID, Integer> rankForBundle, final Map<UUID, BusinessSubscriptionTransitionModelDao> bstForBundle) {
        UUID lastBundleId = null;
        Integer lastBundleRank = 0;
        for (final BusinessSubscriptionTransitionModelDao bst : sortedBundlesBst) {
            if (!baseSubscriptionIds.contains(bst.getSubscriptionId())) {
                continue;
            }

            // Note that sortedBundlesBst is not ordered bundle by bundle, i.e. we may have:
            // bundleId1 CREATE, bundleId2 CREATE, bundleId1 PHASE, bundleId3 CREATE bundleId2 PHASE
            if (lastBundleId == null || (!lastBundleId.equals(bst.getBundleId()) && rankForBundle.get(bst.getBundleId()) == null)) {
                lastBundleRank++;
                lastBundleId = bst.getBundleId();
                rankForBundle.put(lastBundleId, lastBundleRank);
            }

            // We want the last entry to get the current state
            bstForBundle.put(bst.getBundleId(), bst);
        }
    }

    private BusinessBundleModelDao buildBBS(final Account account,
                                            final Long accountRecordId,
                                            final Map<UUID, SubscriptionBundle> bundles,
                                            final BusinessSubscriptionTransitionModelDao bst,
                                            final Integer bundleAccountRank,
                                            final Long tenantRecordId,
                                            final ReportGroup reportGroup,
                                            final CallContext context) throws AnalyticsRefreshException {
        final SubscriptionBundle bundle = bundles.get(bst.getBundleId());
        final Long bundleRecordId = getBundleRecordId(bundle.getId(), context);
        final AuditLog creationAuditLog = getBundleCreationAuditLog(bundle.getId(), context);
        final CurrencyConverter currencyConverter = getCurrencyConverter();

        LocalDate chargedThroughDate = null;
        final Optional<Subscription> base = Iterables.tryFind(bundle.getSubscriptions(),
                                                              new Predicate<Subscription>() {
                                                                  @Override
                                                                  public boolean apply(final Subscription subscription) {
                                                                      return ProductCategory.BASE.equals(subscription.getLastActiveProductCategory());
                                                                  }
                                                              });
        if (base.isPresent()) {
            chargedThroughDate = base.get().getChargedThroughDate();
        }

        return new BusinessBundleModelDao(account,
                                          accountRecordId,
                                          bundle,
                                          bundleRecordId,
                                          bundleAccountRank,
                                          chargedThroughDate,
                                          bst,
                                          currencyConverter,
                                          creationAuditLog,
                                          tenantRecordId,
                                          reportGroup);
    }
}
