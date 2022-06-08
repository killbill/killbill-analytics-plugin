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

package org.killbill.billing.plugin.analytics.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.killbill.billing.ObjectType;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessAccountFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessBundleFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessSubscriptionTransitionFactory;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import org.killbill.billing.util.callcontext.CallContext;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessSubscriptionTransitionDao extends BusinessAnalyticsDaoBase {

    private static final Logger logger = LoggerFactory.getLogger(BusinessSubscriptionTransitionDao.class);

    private final BusinessAccountDao businessAccountDao;
    private final BusinessBundleDao businessBundleDao;
    private final BusinessAccountFactory bacFactory;
    private final BusinessBundleFactory bbsFactory;
    private final BusinessSubscriptionTransitionFactory bstFactory;

    public BusinessSubscriptionTransitionDao(final OSGIKillbillDataSource osgiKillbillDataSource,
                                             final BusinessAccountDao businessAccountDao,
                                             final Executor executor) {
        super(osgiKillbillDataSource);
        this.businessAccountDao = businessAccountDao;
        this.businessBundleDao = new BusinessBundleDao(osgiKillbillDataSource);
        bacFactory = new BusinessAccountFactory();
        bbsFactory = new BusinessBundleFactory(executor);
        bstFactory = new BusinessSubscriptionTransitionFactory();
    }

    public void update(final UUID objectId, final ObjectType objectType, final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        final UUID bundleId;
        if (objectType == ObjectType.BUNDLE) {
            bundleId = objectId;
        } else if (objectType == ObjectType.SUBSCRIPTION) {
            bundleId = businessContextFactory.getSubscription(objectId).getBundleId();
        } else {
            logger.warn("Unexpected objectType={} for objectId={}", objectType, objectId);
            return;
        }

        logger.debug("Starting rebuild of Analytics bundleId {} for account {}", bundleId, businessContextFactory.getAccountId());

        // Recompute all subscription transition records for that bundle
        logger.debug("Starting rebuild of Analytics bundle transitions of bundleId {} for account {}", bundleId, businessContextFactory.getAccountId());
        final Collection<BusinessSubscriptionTransitionModelDao> bsts = bstFactory.createBusinessSubscriptionTransitions(bundleId, businessContextFactory);
        logger.debug("Finished rebuild of Analytics bundle transitions of bundleId {} for account {}", bundleId, businessContextFactory.getAccountId());

        update(bsts, true, businessContextFactory);

        logger.debug("Finished rebuild of Analytics bundleId {} for account {}", bundleId, businessContextFactory.getAccountId());
    }

    public void update(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        logger.debug("Starting rebuild of Analytics subscriptions for account {}", businessContextFactory.getAccountId());

        // Recompute all subscription transition records
        final Collection<BusinessSubscriptionTransitionModelDao> bsts = bstFactory.createBusinessSubscriptionTransitions(businessContextFactory);

        update(bsts, false, businessContextFactory);

        logger.debug("Finished rebuild of Analytics subscriptions for account {}", businessContextFactory.getAccountId());
    }

    private void update(final Iterable<BusinessSubscriptionTransitionModelDao> bsts,
                        final boolean partialRefresh,
                        final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        // Recompute the account record
        logger.debug("Starting rebuild of Analytics account {}", businessContextFactory.getAccountId());
        final BusinessAccountModelDao bac = bacFactory.createBusinessAccount(businessContextFactory);
        logger.debug("Finished rebuild of Analytics account {}", businessContextFactory.getAccountId());

        // Recompute the bundle summary records
        logger.debug("Starting rebuild of Analytics bundle summary for account {}", businessContextFactory.getAccountId());
        final Collection<BusinessBundleModelDao> bbss = bbsFactory.createBusinessBundles(partialRefresh, businessContextFactory, bsts);
        logger.debug("Finished rebuild of Analytics bundle summary for account {}", businessContextFactory.getAccountId());

        executeInTransaction(new Transaction<Void, BusinessAnalyticsSqlDao>() {
            @Override
            public Void inTransaction(final BusinessAnalyticsSqlDao transactional, final TransactionStatus status) throws Exception {
                updateInTransaction(bac, bbss, bsts, transactional, businessContextFactory.getCallContext());
                return null;
            }
        });
    }

    private void updateInTransaction(final BusinessAccountModelDao bac,
                                     final Iterable<BusinessBundleModelDao> bbss,
                                     final Iterable<BusinessSubscriptionTransitionModelDao> bsts,
                                     final BusinessAnalyticsSqlDao transactional,
                                     final CallContext context) {
        final Collection<UUID> deletedByBundleId = new HashSet<UUID>();

        // Update the subscription transitions
        for (final BusinessSubscriptionTransitionModelDao bst : bsts) {
            if (!deletedByBundleId.contains(bst.getBundleId())) {
                // Delete by bundle to support partial refreshes
                transactional.deleteByBundleId(BusinessSubscriptionTransitionModelDao.SUBSCRIPTION_TABLE_NAME,
                                               bst.getBundleId(),
                                               bac.getTenantRecordId(),
                                               context);
                deletedByBundleId.add(bst.getBundleId());
            }

            transactional.create(bst.getTableName(), bst, context);
        }

        // Update the summary table per bundle
        businessBundleDao.updateInTransaction(bbss,
                                              bac.getTenantRecordId(),
                                              transactional,
                                              context);

        // Update BAC
        businessAccountDao.updateInTransaction(bac, transactional, context);
    }
}
