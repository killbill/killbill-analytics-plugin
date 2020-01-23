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

package org.killbill.billing.plugin.analytics.utils;

import java.math.BigDecimal;
import java.util.Collection;

import javax.annotation.Nullable;

import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoiceItemType;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.invoice.api.InvoicePaymentType;
import org.killbill.billing.plugin.util.KillBillMoney;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

/**
 * Utilities to manipulate invoice and invoice items (mostly copied over from Kill Bill).
 */
public class BusinessInvoiceUtils {

    public static boolean isRevenueRecognizable(final InvoiceItem invoiceItem, final Collection<InvoiceItem> otherInvoiceItems) {
        // All items are recognizable except user generated credit (CBA_ADJ and CREDIT_ADJ on their own invoice)
        return !(InvoiceItemType.CBA_ADJ.equals(invoiceItem.getInvoiceItemType()) &&
                 (otherInvoiceItems.size() == 1 &&
                  InvoiceItemType.CREDIT_ADJ.equals(otherInvoiceItems.iterator().next().getInvoiceItemType()) &&
                  otherInvoiceItems.iterator().next().getInvoiceId().equals(invoiceItem.getInvoiceId()) &&
                  otherInvoiceItems.iterator().next().getAmount().compareTo(invoiceItem.getAmount().negate()) == 0));
    }

    // Invoice adjustments
    public static boolean isInvoiceAdjustmentItem(final InvoiceItem invoiceItem, final Iterable<InvoiceItem> otherInvoiceItems) {
        // Invoice level credit, i.e. credit adj, but NOT on its on own invoice
        // Note: the negative credit adj items (internal generation of account level credits) doesn't figure in analytics
        return (InvoiceItemType.CREDIT_ADJ.equals(invoiceItem.getInvoiceItemType()) &&
                !(Iterables.size(otherInvoiceItems) == 1 &&
                  InvoiceItemType.CBA_ADJ.equals(otherInvoiceItems.iterator().next().getInvoiceItemType()) &&
                  otherInvoiceItems.iterator().next().getInvoiceId().equals(invoiceItem.getInvoiceId()) &&
                  otherInvoiceItems.iterator().next().getAmount().compareTo(invoiceItem.getAmount().negate()) == 0));
    }

    // Item adjustments
    public static boolean isInvoiceItemAdjustmentItem(final InvoiceItem invoiceItem) {
        return InvoiceItemType.ITEM_ADJ.equals(invoiceItem.getInvoiceItemType()) || InvoiceItemType.REPAIR_ADJ.equals(invoiceItem.getInvoiceItemType());
    }

    // Account credits, gained or consumed
    public static boolean isAccountCreditItem(final InvoiceItem invoiceItem) {
        return InvoiceItemType.CBA_ADJ.equals(invoiceItem.getInvoiceItemType());
    }

    // Item from Parent Invoice
    private static boolean isParentSummaryItem(final InvoiceItem invoiceItem) {
        return InvoiceItemType.PARENT_SUMMARY.equals(invoiceItem.getInvoiceItemType());
    }

    // Regular line item (charges)
    public static boolean isCharge(final InvoiceItem invoiceItem) {
        return InvoiceItemType.TAX.equals(invoiceItem.getInvoiceItemType()) ||
               InvoiceItemType.EXTERNAL_CHARGE.equals(invoiceItem.getInvoiceItemType()) ||
               InvoiceItemType.FIXED.equals(invoiceItem.getInvoiceItemType()) ||
               InvoiceItemType.USAGE.equals(invoiceItem.getInvoiceItemType()) ||
               InvoiceItemType.RECURRING.equals(invoiceItem.getInvoiceItemType());
    }

    public static BigDecimal computeRawInvoiceBalance(final Currency currency,
                                                      @Nullable final Collection<InvoiceItem> invoiceItems,
                                                      @Nullable final Collection<InvoicePayment> invoicePayments) {

        final BigDecimal amountPaid = computeInvoiceAmountPaid(currency, invoicePayments)
                .add(computeInvoiceAmountRefunded(currency, invoicePayments));

        final BigDecimal chargedAmount = computeInvoiceAmountCharged(currency, invoiceItems)
                .add(computeInvoiceAmountCredited(currency, invoiceItems))
                .add(computeInvoiceAmountAdjustedForAccountCredit(currency, invoiceItems));

        final BigDecimal invoiceBalance = chargedAmount.add(amountPaid.negate());

        return KillBillMoney.of(invoiceBalance, currency);
    }

    // Snowflake for the CREDIT_ADJ on its own invoice
    private static BigDecimal computeInvoiceAmountAdjustedForAccountCredit(final Currency currency, final Iterable<InvoiceItem> invoiceItems) {
        BigDecimal amountAdjusted = BigDecimal.ZERO;
        if (invoiceItems == null || !invoiceItems.iterator().hasNext()) {
            return KillBillMoney.of(amountAdjusted, currency);
        }

        for (final InvoiceItem invoiceItem : invoiceItems) {
            final Iterable<InvoiceItem> otherInvoiceItems = Iterables.filter(invoiceItems, new Predicate<InvoiceItem>() {
                @Override
                public boolean apply(final InvoiceItem input) {
                    return !input.getId().equals(invoiceItem.getId());
                }
            });

            if (InvoiceItemType.CREDIT_ADJ.equals(invoiceItem.getInvoiceItemType()) &&
                (Iterables.size(otherInvoiceItems) == 1 &&
                 InvoiceItemType.CBA_ADJ.equals(otherInvoiceItems.iterator().next().getInvoiceItemType()) &&
                 otherInvoiceItems.iterator().next().getInvoiceId().equals(invoiceItem.getInvoiceId()) &&
                 otherInvoiceItems.iterator().next().getAmount().compareTo(invoiceItem.getAmount().negate()) == 0)) {
                amountAdjusted = amountAdjusted.add(invoiceItem.getAmount());
            }
        }

        return KillBillMoney.of(amountAdjusted, currency);
    }

    private static BigDecimal computeInvoiceAmountCharged(final Currency currency, @Nullable final Collection<InvoiceItem> invoiceItems) {
        BigDecimal amountCharged = BigDecimal.ZERO;
        if (invoiceItems == null || !invoiceItems.iterator().hasNext()) {
            return KillBillMoney.of(amountCharged, currency);
        }

        for (final InvoiceItem invoiceItem : invoiceItems) {
            final Collection<InvoiceItem> otherInvoiceItems = Collections2.<InvoiceItem>filter(invoiceItems, new Predicate<InvoiceItem>() {
                @Override
                public boolean apply(final InvoiceItem input) {
                    return !input.getId().equals(invoiceItem.getId());
                }
            });

            if (isCharge(invoiceItem) ||
                isInvoiceAdjustmentItem(invoiceItem, otherInvoiceItems) ||
                isInvoiceItemAdjustmentItem(invoiceItem) ||
                isParentSummaryItem(invoiceItem)) {
                amountCharged = amountCharged.add(invoiceItem.getAmount());
            }
        }

        return KillBillMoney.of(amountCharged, currency);
    }

    private static BigDecimal computeInvoiceAmountCredited(final Currency currency, @Nullable final Iterable<InvoiceItem> invoiceItems) {
        BigDecimal amountCredited = BigDecimal.ZERO;
        if (invoiceItems == null || !invoiceItems.iterator().hasNext()) {
            return KillBillMoney.of(amountCredited, currency);
        }

        for (final InvoiceItem invoiceItem : invoiceItems) {
            if (isAccountCreditItem(invoiceItem)) {
                amountCredited = amountCredited.add(invoiceItem.getAmount());
            }
        }

        return KillBillMoney.of(amountCredited, currency);
    }

    private static BigDecimal computeInvoiceAmountPaid(final Currency currency, @Nullable final Iterable<InvoicePayment> invoicePayments) {
        BigDecimal amountPaid = BigDecimal.ZERO;
        if (invoicePayments == null || !invoicePayments.iterator().hasNext()) {
            return KillBillMoney.of(amountPaid, currency);
        }

        for (final InvoicePayment invoicePayment : invoicePayments) {
            if (!invoicePayment.isSuccess()) {
                continue;
            }
            if (InvoicePaymentType.ATTEMPT.equals(invoicePayment.getType())) {
                amountPaid = amountPaid.add(invoicePayment.getAmount());
            }
        }

        return KillBillMoney.of(amountPaid, currency);
    }

    private static BigDecimal computeInvoiceAmountRefunded(final Currency currency, @Nullable final Iterable<InvoicePayment> invoicePayments) {
        BigDecimal amountRefunded = BigDecimal.ZERO;
        if (invoicePayments == null || !invoicePayments.iterator().hasNext()) {
            return KillBillMoney.of(amountRefunded, currency);
        }

        for (final InvoicePayment invoicePayment : invoicePayments) {
            if (invoicePayment.isSuccess() == null || !invoicePayment.isSuccess()) {
                continue;
            }
            if (InvoicePaymentType.REFUND.equals(invoicePayment.getType()) ||
                InvoicePaymentType.CHARGED_BACK.equals(invoicePayment.getType())) {
                amountRefunded = amountRefunded.add(invoicePayment.getAmount());
            }
        }

        return KillBillMoney.of(amountRefunded, currency);
    }
}
