/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014 Groupon, Inc
 * Copyright 2014 The Billing Project, LLC
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

import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentBaseModelDao;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;

public class BusinessPaymentDao extends BusinessAnalyticsDaoBase {

    public BusinessPaymentDao(final OSGIKillbillLogService logService, final OSGIKillbillDataSource osgiKillbillDataSource) {
        super(logService, osgiKillbillDataSource);
    }

    /**
     * Delete all invoice payment records and insert the specified ones as current.
     *
     * @param bac                     current, fully populated, BusinessAccountModelDao record
     * @param businessInvoicePayments current, fully populated, mapping of invoice id to BusinessInvoicePaymentBaseModelDao records
     * @param transactional           current transaction
     * @param context                 call context
     */
    public void updateInTransaction(final BusinessAccountModelDao bac,
                                    final Iterable<BusinessPaymentBaseModelDao> businessInvoicePayments,
                                    final BusinessAnalyticsSqlDao transactional,
                                    final CallContext context) {
        for (final String tableName : BusinessPaymentBaseModelDao.ALL_PAYMENTS_TABLE_NAMES) {
            transactional.deleteByAccountRecordId(tableName, bac.getAccountRecordId(), bac.getTenantRecordId(), context);
        }

        for (final BusinessPaymentBaseModelDao invoicePayment : businessInvoicePayments) {
            transactional.create(invoicePayment.getTableName(), invoicePayment, context);
        }

        // Invoice and payment details in BAC will be updated by BusinessInvoiceAndInvoicePaymentDao
    }
}
