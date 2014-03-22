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

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;

import com.google.common.collect.Multimap;

public class BusinessInvoiceDao extends BusinessAnalyticsDaoBase {

    public BusinessInvoiceDao(final OSGIKillbillLogService logService, final OSGIKillbillDataSource osgiKillbillDataSource) {
        super(logService, osgiKillbillDataSource);
    }

    /**
     * Delete all invoice and invoice item records and insert the specified ones as current.
     *
     * @param bac                  current, fully populated, BusinessAccountModelDao record
     * @param businessInvoices     current, fully populated, mapping of invoice id -> BusinessInvoiceModelDao records
     * @param businessInvoiceItems current, fully populated, mapping of invoice id -> BusinessInvoiceItemBaseModelDao records
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

        // Invoice and payment details in BAC will be updated by BusinessInvoiceAndInvoicePaymentDao
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
