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

package org.killbill.billing.plugin.analytics.dao.factory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

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
import org.killbill.billing.util.audit.AccountAuditLogs;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.clock.Clock;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;

public class BusinessPaymentFactory extends BusinessFactoryBase {

    public BusinessPaymentFactory(final OSGIKillbillLogService logService,
                                  final OSGIKillbillAPI osgiKillbillAPI,
                                  final OSGIKillbillDataSource osgiKillbillDataSource,
                                  final Clock clock) {
        super(logService, osgiKillbillAPI, osgiKillbillDataSource, clock);
    }

    public Collection<BusinessPaymentBaseModelDao> createBusinessPayments(final UUID accountId,
                                                                          final AccountAuditLogs accountAuditLogs,
                                                                          final CallContext context) throws AnalyticsRefreshException {
        final Account account = getAccount(accountId, context);
        final Long accountRecordId = getAccountRecordId(account.getId(), context);
        final Long tenantRecordId = getTenantRecordId(context);
        final ReportGroup reportGroup = getReportGroup(account.getId(), context);
        final CurrencyConverter currencyConverter = getCurrencyConverter();

        // Optimize invoice lookups by fetching all invoices at once
        final Collection<Invoice> invoicesForAccount = getInvoicesByAccountId(account.getId(), context);
        final Map<UUID, Invoice> invoices = new LinkedHashMap<UUID, Invoice>();
        for (final Invoice invoice : invoicesForAccount) {
            invoices.put(invoice.getId(), invoice);
        }

        final Collection<Payment> paymentsForAccount = getPaymentsByAccountId(accountId, context);

        // Optimize invoice payment lookups by fetching all invoice payments at once
        final Map<UUID, List<InvoicePayment>> allInvoicePaymentsByPaymentId = getAccountInvoicePayments(paymentsForAccount, context);

        final Collection<BusinessPaymentBaseModelDao> businessPayments = new LinkedList<BusinessPaymentBaseModelDao>();
        for (final Payment payment : paymentsForAccount) {
            final List<InvoicePayment> invoicePaymentsForPayment = allInvoicePaymentsByPaymentId.get(payment.getId());
            // TODO - assume one for now
            final InvoicePayment invoicePaymentForPayment = invoicePaymentsForPayment == null || invoicePaymentsForPayment.isEmpty() ? null : invoicePaymentsForPayment.get(0);

            final Collection<BusinessPaymentBaseModelDao> createdBusinessPayments = createBusinessPayment(account,
                                                                                                          invoicePaymentForPayment,
                                                                                                          invoices,
                                                                                                          currencyConverter,
                                                                                                          accountAuditLogs,
                                                                                                          accountRecordId,
                                                                                                          tenantRecordId,
                                                                                                          reportGroup,
                                                                                                          context);
            businessPayments.addAll(createdBusinessPayments);
        }

        return businessPayments;
    }

    private Collection<BusinessPaymentBaseModelDao> createBusinessPayment(final Account account,
                                                                          final InvoicePayment invoicePayment,
                                                                          final Map<UUID, Invoice> invoices,
                                                                          final CurrencyConverter currencyConverter,
                                                                          final AccountAuditLogs accountAuditLogs,
                                                                          final Long accountRecordId,
                                                                          final Long tenantRecordId,
                                                                          @Nullable final ReportGroup reportGroup,
                                                                          final CallContext context) throws AnalyticsRefreshException {
        final Long invoicePaymentRecordId = getInvoicePaymentRecordId(invoicePayment.getId(), context);

        final Payment payment = getPaymentWithPluginInfo(invoicePayment.getPaymentId(), context);

        final Invoice invoice = invoices.get(invoicePayment.getInvoiceId());
        final PaymentMethod paymentMethod = getPaymentMethod(payment.getPaymentMethodId(), context);
        final AuditLog creationAuditLog = getInvoicePaymentCreationAuditLog(invoicePayment.getId(), accountAuditLogs);

        final List<BusinessPaymentBaseModelDao> businessPayments = new LinkedList<BusinessPaymentBaseModelDao>();
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
                                                                                                   reportGroup);
            if (businessPayment != null) {
                businessPayments.add(businessPayment);
            }
        }

        return businessPayments;
    }
}
