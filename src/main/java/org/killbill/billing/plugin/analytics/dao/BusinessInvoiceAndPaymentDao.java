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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIMetricRegistry;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessAccountFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessInvoiceFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessPaymentFactory;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentBaseModelDao;
import org.killbill.billing.util.callcontext.CallContext;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Wrapper around BusinessInvoiceDao and BusinessPaymentDao.
 * <p>
 * These two should always be updated together as invoice and payment information is denormalized across
 * bot sets of tables.
 */
public class BusinessInvoiceAndPaymentDao extends BusinessAnalyticsDaoBase {

    private static final Logger logger = LoggerFactory.getLogger(BusinessInvoiceAndPaymentDao.class);

    private final BusinessAccountDao businessAccountDao;
    private final BusinessInvoiceDao businessInvoiceDao;
    private final BusinessPaymentDao businessPaymentDao;
    private final BusinessAccountFactory bacFactory;
    private final BusinessInvoiceFactory binFactory;
    private final BusinessPaymentFactory bipFactory;

    public BusinessInvoiceAndPaymentDao(final OSGIKillbillDataSource osgiKillbillDataSource,
                                        final OSGIMetricRegistry metricRegistry,
                                        final BusinessAccountDao businessAccountDao,
                                        final Executor executor) {
        super(osgiKillbillDataSource, metricRegistry);
        this.businessAccountDao = businessAccountDao;
        this.businessInvoiceDao = new BusinessInvoiceDao(osgiKillbillDataSource, metricRegistry, businessAccountDao, executor);
        this.businessPaymentDao = new BusinessPaymentDao(osgiKillbillDataSource, metricRegistry);
        bacFactory = new BusinessAccountFactory();
        binFactory = new BusinessInvoiceFactory(executor);
        bipFactory = new BusinessPaymentFactory();
    }

    public void update(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        logger.debug("Starting rebuild of Analytics invoices and payments for account {}", businessContextFactory.getAccountId());

        // Recompute the account record
        final BusinessAccountModelDao bac = bacFactory.createBusinessAccount(businessContextFactory);

        // Recompute invoice, invoice items and invoice payments records
        final Map<UUID, BusinessInvoiceModelDao> invoices = new HashMap<UUID, BusinessInvoiceModelDao>();
        final Multimap<UUID, BusinessInvoiceItemBaseModelDao> invoiceItems = ArrayListMultimap.<UUID, BusinessInvoiceItemBaseModelDao>create();
        final Multimap<UUID, BusinessPaymentBaseModelDao> invoicePayments = ArrayListMultimap.<UUID, BusinessPaymentBaseModelDao>create();
        createBusinessPojos(businessContextFactory, invoices, invoiceItems, invoicePayments);

        // Delete and recreate all items in the transaction
        executeInTransaction(new Transaction<Void, BusinessAnalyticsSqlDao>() {
            @Override
            public Void inTransaction(final BusinessAnalyticsSqlDao transactional, final TransactionStatus status) throws Exception {
                updateInTransaction(bac, invoices, invoiceItems, invoicePayments, transactional, businessContextFactory.getCallContext());
                return null;
            }
        });

        logger.debug("Finished rebuild of Analytics invoices and payments for account {}", businessContextFactory.getAccountId());
    }

    private void createBusinessPojos(final BusinessContextFactory businessContextFactory,
                                     final Map<UUID, BusinessInvoiceModelDao> invoices,
                                     final Multimap<UUID, BusinessInvoiceItemBaseModelDao> invoiceItems,
                                     final Multimap<UUID, BusinessPaymentBaseModelDao> invoicePayments) throws AnalyticsRefreshException {
        // Recompute all invoices and invoice items
        final Map<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>> businessInvoices = binFactory.createBusinessInvoicesAndInvoiceItems(businessContextFactory);

        // Recompute all payments
        final Collection<BusinessPaymentBaseModelDao> businessInvoicePayments = bipFactory.createBusinessPayments(businessContextFactory);

        // Transform the results
        for (final Entry<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>> entry : businessInvoices.entrySet()) {
            invoices.put(entry.getKey().getInvoiceId(), entry.getKey());
            invoiceItems.get(entry.getKey().getInvoiceId()).addAll(entry.getValue());
        }
        for (final BusinessPaymentBaseModelDao businessInvoicePayment : businessInvoicePayments) {
            invoicePayments.get(businessInvoicePayment.getInvoiceId()).add(businessInvoicePayment);
        }
    }

    /**
     * Refresh the records. This does not perform any logic but simply deletes existing records and inserts the current ones.
     *
     * @param bac             current, fully populated, BusinessAccountModelDao record
     * @param invoices        current, fully populated, mapping of invoice id -> BusinessInvoiceModelDao records
     * @param invoiceItems    current, fully populated, mapping of invoice id -> BusinessInvoiceItemBaseModelDao records
     * @param invoicePayments current, fully populated, mapping of invoice id -> BusinessInvoicePaymentBaseModelDao records
     * @param transactional   current transaction
     * @param context         call context
     */
    private void updateInTransaction(final BusinessAccountModelDao bac,
                                     final Map<UUID, BusinessInvoiceModelDao> invoices,
                                     final Multimap<UUID, BusinessInvoiceItemBaseModelDao> invoiceItems,
                                     final Multimap<UUID, BusinessPaymentBaseModelDao> invoicePayments,
                                     final BusinessAnalyticsSqlDao transactional,
                                     final CallContext context) {
        // Update invoice and invoice items tables
        businessInvoiceDao.updateInTransaction(bac, invoices, invoiceItems, transactional, context);

        // Update payment tables
        businessPaymentDao.updateInTransaction(bac, Iterables.<BusinessPaymentBaseModelDao>concat(invoicePayments.values()), transactional, context);

        // Update denormalized invoice and payment details in BAC
        businessAccountDao.updateInTransaction(bac, transactional, context);
    }
}
