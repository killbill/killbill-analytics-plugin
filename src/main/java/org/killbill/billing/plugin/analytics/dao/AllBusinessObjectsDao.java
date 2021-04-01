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

import java.util.concurrent.Executor;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllBusinessObjectsDao {

    private static final Logger logger = LoggerFactory.getLogger(AllBusinessObjectsDao.class);

    private final BusinessSubscriptionTransitionDao bstDao;
    private final BusinessInvoiceAndPaymentDao binAndBipDao;
    private final BusinessAccountTransitionDao bosDao;
    private final BusinessFieldDao bFieldDao;
    private final BusinessTagDao bTagDao;

    public AllBusinessObjectsDao(final OSGIKillbillDataSource osgiKillbillDataSource,
                                 final Executor executor) {
        final BusinessAccountDao bacDao = new BusinessAccountDao(osgiKillbillDataSource);
        this.bstDao = new BusinessSubscriptionTransitionDao(osgiKillbillDataSource, bacDao, executor);
        this.binAndBipDao = new BusinessInvoiceAndPaymentDao(osgiKillbillDataSource, bacDao, executor);
        this.bosDao = new BusinessAccountTransitionDao(osgiKillbillDataSource);
        this.bFieldDao = new BusinessFieldDao(osgiKillbillDataSource);
        this.bTagDao = new BusinessTagDao(osgiKillbillDataSource);
    }

    // TODO: each refresh is done in a transaction - do we want to share a long running transaction across all refreshes?
    public void update(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        logger.debug("Starting rebuild of Analytics for account {}", businessContextFactory.getAccountId());

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

        logger.debug("Finished rebuild of Analytics for account {}", businessContextFactory.getAccountId());
    }
}
