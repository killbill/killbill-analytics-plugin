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

package org.killbill.billing.plugin.analytics.utils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentTransaction;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

public class PaymentUtils {

    private static final Comparator<PaymentTransaction> PAYMENT_TRANSACTION_COMPARATOR = new Comparator<PaymentTransaction>() {
        @Override
        public int compare(final PaymentTransaction paymentTransaction1, final PaymentTransaction paymentTransaction2) {
            return paymentTransaction1.getEffectiveDate().compareTo(paymentTransaction2.getEffectiveDate());
        }
    };

    public static PaymentTransaction findLastPaymentTransaction(final Payment payment, final TransactionType... transactionTypes) {
        return findLastPaymentTransaction(ImmutableList.<Payment>of(payment), transactionTypes);
    }

    public static PaymentTransaction findLastPaymentTransaction(final Iterable<Payment> payments, final TransactionType... transactionTypes) {
        final Iterable<PaymentTransaction> paymentTransactions = findPaymentTransactions(payments, transactionTypes);
        if (!paymentTransactions.iterator().hasNext()) {
            return null;
        }

        return Ordering.from(PAYMENT_TRANSACTION_COMPARATOR)
                       .immutableSortedCopy(paymentTransactions)
                       .reverse()
                       .get(0);
    }

    public static Iterable<PaymentTransaction> findPaymentTransactions(final Payment payment, final TransactionType... transactionTypes) {
        return findPaymentTransactions(ImmutableList.<Payment>of(payment), transactionTypes);
    }

    public static Iterable<PaymentTransaction> findPaymentTransactions(final Iterable<Payment> payments, final TransactionType... transactionTypes) {
        final List<PaymentTransaction> transactions = new LinkedList<PaymentTransaction>();
        for (final Payment payment : payments) {
            transactions.addAll(payment.getTransactions());
        }

        return Iterables.<PaymentTransaction>filter(transactions,
                                                    new Predicate<PaymentTransaction>() {
                                                        @Override
                                                        public boolean apply(final PaymentTransaction paymentTransaction) {
                                                            for (final TransactionType transactionType : transactionTypes) {
                                                                if (paymentTransaction.getTransactionType().equals(transactionType)) {
                                                                    return true;
                                                                }
                                                            }
                                                            return false;
                                                        }
                                                    });
    }


    public static String getPropertyValue(@Nullable final Iterable<PluginProperty> properties, final String key) {
        if (properties == null) {
            return null;
        } else {
            final PluginProperty pluginProperty = Iterables.<PluginProperty>tryFind(properties,
                                                                                    new Predicate<PluginProperty>() {
                                                                                        @Override
                                                                                        public boolean apply(@Nullable final PluginProperty pluginProperty) {
                                                                                            return pluginProperty != null && key.equals(pluginProperty.getKey());
                                                                                        }
                                                                                    }).orNull();

            return pluginProperty == null || pluginProperty.getValue() == null ? null : pluginProperty.getValue().toString();
        }
    }
}
