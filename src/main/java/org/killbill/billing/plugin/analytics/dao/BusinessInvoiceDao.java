/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2019 Groupon, Inc
 * Copyright 2014-2019 The Billing Project, LLC
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessAccountFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessInvoiceFactory;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;
import org.killbill.billing.util.callcontext.CallContext;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

public class BusinessInvoiceDao extends BusinessAnalyticsDaoBase {

    private static final Logger logger = LoggerFactory.getLogger(BusinessInvoiceDao.class);

    private final BusinessAccountDao businessAccountDao;
    private final BusinessInvoiceFactory binFactory;
    private final BusinessAccountFactory bacFactory;

    public BusinessInvoiceDao(final OSGIKillbillDataSource osgiKillbillDataSource,
                              final BusinessAccountDao businessAccountDao,
                              final Executor executor) {
        super(osgiKillbillDataSource);
        this.businessAccountDao = businessAccountDao;
        this.binFactory = new BusinessInvoiceFactory(executor);
        this.bacFactory = new BusinessAccountFactory();
    }

    public void update(final UUID invoiceId, final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        logger.debug("Starting rebuild of Analytics invoiceId {} for account {}", invoiceId, businessContextFactory.getAccountId());

        // Recompute the account record
        final BusinessAccountModelDao bac = bacFactory.createBusinessAccount(businessContextFactory);

        // Recompute invoice and invoice items records
        final Map<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>> businessInvoices = binFactory.createBusinessInvoiceAndInvoiceItems(invoiceId,
                                                                                                                                                           businessContextFactory);
        Preconditions.checkArgument(businessInvoices.size() == 1, "Unexpected number of invoices: " + businessInvoices);

        // Delete and recreate all items in the transaction
        executeInTransaction(new Transaction<Void, BusinessAnalyticsSqlDao>() {
            @Override
            public Void inTransaction(final BusinessAnalyticsSqlDao transactional, final TransactionStatus status) throws Exception {
                final Entry<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>> models = businessInvoices.entrySet().iterator().next();
                updateInTransaction(bac, models.getKey(), models.getValue(), transactional, businessContextFactory.getCallContext());

                // Update denormalized invoice and payment details in BAC
                businessAccountDao.updateInTransaction(bac, transactional, businessContextFactory.getCallContext());
                return null;
            }
        });

        logger.debug("Finished rebuild of Analytics invoiceId {} for account {}", invoiceId, businessContextFactory.getAccountId());
    }

    private void updateInTransaction(final BusinessAccountModelDao bac,
                                     final BusinessInvoiceModelDao businessInvoice,
                                     final Iterable<BusinessInvoiceItemBaseModelDao> businessInvoiceItems,
                                     final BusinessAnalyticsSqlDao transactional,
                                     final CallContext context) {
        deleteInvoiceAndInvoiceItemsInTransaction(transactional, businessInvoice.getInvoiceId(), bac.getTenantRecordId(), context);

        if (businessInvoiceItems != null) {
            createInvoiceInTransaction(transactional, businessInvoice, businessInvoiceItems, context);
        }

        // Invoice and payment details in BAC will subsequently be updated
    }

    /**
     * Delete all invoice and invoice item records and insert the specified ones as current.
     *
     * @param bac                  current, fully populated, BusinessAccountModelDao record
     * @param businessInvoices     current, fully populated, mapping of invoice id to BusinessInvoiceModelDao records
     * @param businessInvoiceItems current, fully populated, mapping of invoice id to BusinessInvoiceItemBaseModelDao records
     * @param transactional        current transaction
     * @param context              call context
     */
    public void updateInTransaction(final BusinessAccountModelDao bac,
                                    final Map<UUID, BusinessInvoiceModelDao> businessInvoices,
                                    final Multimap<UUID, BusinessInvoiceItemBaseModelDao> businessInvoiceItems,
                                    final BusinessAnalyticsSqlDao transactional,
                                    final CallContext context) {
        deleteInvoicesAndInvoiceItemsForAccountInTransaction(transactional, bac.getAccountRecordId(), bac.getTenantRecordId(), context);

        for (final BusinessInvoiceModelDao businessInvoice : businessInvoices.values()) {
            final Collection<BusinessInvoiceItemBaseModelDao> invoiceItems = businessInvoiceItems.get(businessInvoice.getInvoiceId());
            if (invoiceItems != null) {
                createInvoiceInTransaction(transactional, businessInvoice, invoiceItems, context);
            }
        }

        // Invoice and payment details in BAC will subsequently be updated
    }

    private void deleteInvoiceAndInvoiceItemsInTransaction(final BusinessAnalyticsSqlDao transactional,
                                                           final UUID invoiceId,
                                                           final Long tenantRecordId,
                                                           final CallContext context) {
        // Delete all invoice items
        for (final String tableName : BusinessInvoiceItemBaseModelDao.ALL_INVOICE_ITEMS_TABLE_NAMES) {
            transactional.deleteByInvoiceId(tableName, invoiceId, tenantRecordId, context);
        }

        // Delete all invoices
        transactional.deleteByInvoiceId(BusinessInvoiceModelDao.INVOICES_TABLE_NAME, invoiceId, tenantRecordId, context);
    }

    private void deleteInvoicesAndInvoiceItemsForAccountInTransaction(final BusinessAnalyticsSqlDao transactional,
                                                                      final Long accountRecordId,
                                                                      final Long tenantRecordId,
                                                                      final CallContext context) {
        // Delete all invoice items
        for (final String tableName : BusinessInvoiceItemBaseModelDao.ALL_INVOICE_ITEMS_TABLE_NAMES) {
            transactional.deleteByAccountRecordId(tableName, accountRecordId, tenantRecordId, context);
        }

        // Delete all invoices
        transactional.deleteByAccountRecordId(BusinessInvoiceModelDao.INVOICES_TABLE_NAME, accountRecordId, tenantRecordId, context);
    }

    private void createInvoiceInTransaction(final BusinessAnalyticsSqlDao transactional,
                                            final BusinessInvoiceModelDao invoice,
                                            final Iterable<BusinessInvoiceItemBaseModelDao> invoiceItems,
                                            final CallContext context) {
        // Create the invoice
        transactional.create(invoice.getTableName(), invoice, context);

        // Add associated invoice items
        for (final BusinessInvoiceItemBaseModelDao invoiceItem : invoiceItems) {
            transactional.create(invoiceItem.getTableName(), invoiceItem, context);
        }
    }
}
