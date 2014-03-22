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

package org.killbill.billing.plugin.analytics.dao.factory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.killbill.billing.account.api.Account;
import org.killbill.billing.catalog.api.ProductCategory;
import org.killbill.billing.entitlement.api.Entitlement.EntitlementState;
import org.killbill.billing.entitlement.api.Subscription;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AccountAuditLogs;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.clock.Clock;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class BusinessAccountFactory extends BusinessFactoryBase {

    public BusinessAccountFactory(final OSGIKillbillLogService logService,
                                  final OSGIKillbillAPI osgiKillbillAPI,
                                  final OSGIKillbillDataSource osgiKillbillDataSource,
                                  final Clock clock) {
        super(logService, osgiKillbillAPI, osgiKillbillDataSource, clock);
    }

    public BusinessAccountModelDao createBusinessAccount(final UUID accountId,
                                                         final AccountAuditLogs accountAuditLogs,
                                                         final CallContext context) throws AnalyticsRefreshException {
        final Account account = getAccount(accountId, context);

        // Retrieve the account creation audit log
        final AuditLog creationAuditLog = getAccountCreationAuditLog(account.getId(), accountAuditLogs);

        // Retrieve the account balance
        // Note: since we retrieve the invoices below, we could compute it ourselves and avoid fetching the invoices
        // twice, but that way the computation logic is owned by invoice
        final BigDecimal accountBalance = getAccountBalance(account.getId(), context);

        // Retrieve invoices information
        Invoice oldestUnpaidInvoice = null;
        Invoice lastInvoice = null;
        final Collection<Invoice> invoices = getInvoicesByAccountId(account.getId(), context);
        for (final Invoice invoice : invoices) {
            if (BigDecimal.ZERO.compareTo(invoice.getBalance()) < 0 &&
                (oldestUnpaidInvoice == null || invoice.getInvoiceDate().isBefore(oldestUnpaidInvoice.getInvoiceDate()))) {
                oldestUnpaidInvoice = invoice;
            }
            if (lastInvoice == null || invoice.getInvoiceDate().isAfter(lastInvoice.getInvoiceDate())) {
                lastInvoice = invoice;
            }
        }

        // Retrieve payments information
        Payment lastPayment = null;
        final Collection<Payment> payments = getPaymentsByAccountId(account.getId(), context);
        for (final Payment payment : payments) {
            if (lastPayment == null || payment.getEffectiveDate().isAfter(lastPayment.getEffectiveDate())) {
                lastPayment = payment;
            }
        }

        final List<SubscriptionBundle> bundles = getSubscriptionBundlesForAccount(account.getId(), context);

        final int nbActiveBundles = Iterables.size(Iterables.<SubscriptionBundle>filter(bundles,
                                                                                        new Predicate<SubscriptionBundle>() {
                                                                                            @Override
                                                                                            public boolean apply(final SubscriptionBundle bundle) {
                                                                                                return Iterables.size(Iterables.<Subscription>filter(bundle.getSubscriptions(),
                                                                                                                                                     new Predicate<Subscription>() {
                                                                                                                                                         @Override
                                                                                                                                                         public boolean apply(final Subscription subscription) {
                                                                                                                                                             // Bundle is active iff its base entitlement is not cancelled
                                                                                                                                                             return ProductCategory.BASE.equals(subscription.getLastActiveProductCategory()) &&
                                                                                                                                                                    !subscription.getState().equals(EntitlementState.CANCELLED);
                                                                                                                                                         }
                                                                                                                                                     }
                                                                                                                                                    )) > 0;
                                                                                            }
                                                                                        }
                                                                                       ));
        final Long accountRecordId = getAccountRecordId(account.getId(), context);
        final Long tenantRecordId = getTenantRecordId(context);
        final ReportGroup reportGroup = getReportGroup(account.getId(), context);
        final CurrencyConverter converter = getCurrencyConverter();

        return new BusinessAccountModelDao(account,
                                           accountRecordId,
                                           accountBalance,
                                           oldestUnpaidInvoice,
                                           lastInvoice,
                                           lastPayment,
                                           nbActiveBundles,
                                           converter,
                                           creationAuditLog,
                                           tenantRecordId,
                                           reportGroup);
    }
}
