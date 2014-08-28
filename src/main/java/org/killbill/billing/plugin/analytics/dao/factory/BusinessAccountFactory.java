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

import org.killbill.billing.account.api.Account;
import org.killbill.billing.catalog.api.ProductCategory;
import org.killbill.billing.entitlement.api.Entitlement.EntitlementState;
import org.killbill.billing.entitlement.api.Subscription;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentTransaction;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.plugin.analytics.utils.PaymentUtils;
import org.killbill.billing.util.audit.AuditLog;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class BusinessAccountFactory {

    public BusinessAccountModelDao createBusinessAccount(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        final Account account = businessContextFactory.getAccount();

        // Retrieve the account creation audit log
        final AuditLog creationAuditLog = businessContextFactory.getAccountCreationAuditLog();

        // Retrieve the account balance
        // Note: since we retrieve the invoices below, we could compute it ourselves and avoid fetching the invoices
        // twice, but that way the computation logic is owned by invoice
        final BigDecimal accountBalance = businessContextFactory.getAccountBalance();

        // Retrieve invoices information
        Invoice oldestUnpaidInvoice = null;
        Invoice lastInvoice = null;
        final Iterable<Invoice> invoices = businessContextFactory.getAccountInvoices();
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
        final Iterable<Payment> payments = businessContextFactory.getAccountPayments();
        final PaymentTransaction lastCaptureOrPurchaseTransaction = PaymentUtils.findLastPaymentTransaction(payments, TransactionType.CAPTURE, TransactionType.PURCHASE);

        // Retrieve bundles information
        final Iterable<SubscriptionBundle> bundles = businessContextFactory.getAccountBundles();

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
        final Long accountRecordId = businessContextFactory.getAccountRecordId();
        final Long tenantRecordId = businessContextFactory.getTenantRecordId();
        final ReportGroup reportGroup = businessContextFactory.getReportGroup();
        final CurrencyConverter converter = businessContextFactory.getCurrencyConverter();

        return new BusinessAccountModelDao(account,
                                           accountRecordId,
                                           accountBalance,
                                           oldestUnpaidInvoice,
                                           lastInvoice,
                                           lastCaptureOrPurchaseTransaction,
                                           nbActiveBundles,
                                           converter,
                                           creationAuditLog,
                                           tenantRecordId,
                                           reportGroup);
    }
}
