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

package org.killbill.billing.plugin.analytics.dao.factory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.killbill.billing.account.api.Account;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentMethod;
import org.killbill.billing.payment.api.PaymentTransaction;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentBaseModelDao;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AuditLog;

import com.google.common.collect.Iterables;

public class BusinessPaymentFactory {

    public Collection<BusinessPaymentBaseModelDao> createBusinessPayments(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        final Iterable<Payment> paymentsForAccount = businessContextFactory.getAccountPayments();
        final Collection<BusinessPaymentBaseModelDao> businessPayments = new LinkedList<BusinessPaymentBaseModelDao>();
        if (Iterables.<Payment>isEmpty(paymentsForAccount)) {
            return businessPayments;
        }

        final Account account = businessContextFactory.getAccount();
        final Long accountRecordId = businessContextFactory.getAccountRecordId();
        final Long tenantRecordId = businessContextFactory.getTenantRecordId();
        final ReportGroup reportGroup = businessContextFactory.getReportGroup();
        final CurrencyConverter currencyConverter = businessContextFactory.getCurrencyConverter();
        final PluginPropertiesManager pluginPropertiesManager = businessContextFactory.getPluginPropertiesManager();

        // Optimize invoice lookups by fetching all invoices at once
        final Iterable<Invoice> invoicesForAccount = businessContextFactory.getAccountInvoices();
        final Map<UUID, Invoice> invoices = new LinkedHashMap<UUID, Invoice>();
        for (final Invoice invoice : invoicesForAccount) {
            invoices.put(invoice.getId(), invoice);
        }

        // Optimize invoice payment lookups by fetching all invoice payments at once
        final Map<UUID, List<InvoicePayment>> allInvoicePaymentsByPaymentId = businessContextFactory.getAccountInvoicePayments();

        for (final Payment payment : paymentsForAccount) {
            final List<InvoicePayment> invoicePaymentsForPayment = allInvoicePaymentsByPaymentId.get(payment.getId());
            // TODO - we will remove invoicePayment information from payment tables, we only care about the associated invoice id
            final InvoicePayment invoicePayment = invoicePaymentsForPayment == null || invoicePaymentsForPayment.isEmpty() ? null : invoicePaymentsForPayment.get(0);
            final Long invoicePaymentRecordId = 0L;
            final Invoice invoice = invoicePayment == null ? null : invoices.get(invoicePayment.getInvoiceId());

            final PaymentMethod paymentMethod = businessContextFactory.getPaymentMethod(payment.getPaymentMethodId());
            final AuditLog creationAuditLog = businessContextFactory.getPaymentCreationAuditLog(payment.getId());

            for (final PaymentTransaction paymentTransaction : payment.getTransactions()) {
                final BusinessPaymentBaseModelDao businessPayment = BusinessPaymentBaseModelDao.create(account,
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
                if (businessPayment != null) {
                    businessPayments.add(businessPayment);
                }
            }
        }

        return businessPayments;
    }
}
