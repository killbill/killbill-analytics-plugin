/*
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

package org.killbill.billing.plugin.analytics.dao.model;

import javax.annotation.Nullable;

import org.killbill.billing.account.api.Account;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentMethod;
import org.killbill.billing.payment.api.PaymentTransaction;
import org.killbill.billing.plugin.analytics.dao.factory.PluginPropertiesManager;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AuditLog;

public class BusinessPaymentAuthModelDao extends BusinessPaymentBaseModelDao {

    public BusinessPaymentAuthModelDao() { /* When reading from the database */ }

    public BusinessPaymentAuthModelDao(final Account account,
                                       final Long accountRecordId,
                                       @Nullable final Invoice invoice,
                                       @Nullable final InvoicePayment invoicePayment,
                                       @Nullable final Long invoicePaymentRecordId,
                                       final Payment payment,
                                       final PaymentTransaction paymentTransaction,
                                       @Nullable final PaymentMethod paymentMethod,
                                       final CurrencyConverter currencyConverter,
                                       @Nullable final AuditLog creationAuditLog,
                                       final Long tenantRecordId,
                                       @Nullable final ReportGroup reportGroup,
                                       final PluginPropertiesManager pluginPropertiesManager) {
        super(account,
              accountRecordId,
              invoice,
              invoicePayment,
              invoicePaymentRecordId,
              payment,
              paymentTransaction,
              paymentMethod,
              currencyConverter,
              creationAuditLog,
              tenantRecordId,
              reportGroup,
              pluginPropertiesManager);
    }

    @Override
    public String getTableName() {
        return AUTHS_TABLE_NAME;
    }
}
