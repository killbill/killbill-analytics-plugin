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

package org.killbill.billing.plugin.analytics.dao.model;

import javax.annotation.Nullable;

import org.killbill.billing.account.api.Account;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentMethod;
import org.killbill.billing.payment.api.Refund;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AuditLog;

public class BusinessInvoicePaymentModelDao extends BusinessInvoicePaymentBaseModelDao {

    public BusinessInvoicePaymentModelDao() { /* When reading from the database */ }

    public BusinessInvoicePaymentModelDao(final Account account,
                                          final Long accountRecordId,
                                          final Invoice invoice,
                                          final InvoicePayment invoicePayment,
                                          final Long invoicePaymentRecordId,
                                          final Payment payment,
                                          final Refund refund,
                                          @Nullable final PaymentMethod paymentMethod,
                                          final CurrencyConverter currencyConverter,
                                          @Nullable final AuditLog creationAuditLog,
                                          final Long tenantRecordId,
                                          @Nullable final ReportGroup reportGroup) {
        super(account,
              accountRecordId,
              invoice,
              invoicePayment,
              invoicePaymentRecordId,
              payment,
              refund,
              paymentMethod,
              currencyConverter,
              creationAuditLog,
              tenantRecordId,
              reportGroup);
    }

    @Override
    public String getTableName() {
        return INVOICE_PAYMENTS_TABLE_NAME;
    }
}
