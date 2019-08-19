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

package org.killbill.billing.plugin.analytics.dao.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.PlanPhase;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoiceItemType;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao.BusinessInvoiceItemType;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao.ItemSource;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.tag.ControlTagType;
import org.killbill.billing.util.tag.Tag;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import static org.killbill.billing.plugin.analytics.utils.BusinessInvoiceUtils.isAccountCreditItem;
import static org.killbill.billing.plugin.analytics.utils.BusinessInvoiceUtils.isCharge;
import static org.killbill.billing.plugin.analytics.utils.BusinessInvoiceUtils.isInvoiceAdjustmentItem;
import static org.killbill.billing.plugin.analytics.utils.BusinessInvoiceUtils.isInvoiceItemAdjustmentItem;
import static org.killbill.billing.plugin.analytics.utils.BusinessInvoiceUtils.isRevenueRecognizable;

public class BusinessInvoiceFactory {

    private final Executor executor;

    public BusinessInvoiceFactory(final Executor executor) {
        this.executor = executor;
    }

    /**
     * Create current business invoice and invoice items.
     *
     * @return business invoice and associated invoice items to create
     */
    public Map<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>> createBusinessInvoiceAndInvoiceItems(final UUID invoiceId,
                                                                                                                          final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        // Pre-fetch these, to avoid contention on BusinessContextFactory
        final Account account = businessContextFactory.getAccount();
        final Long accountRecordId = businessContextFactory.getAccountRecordId();
        final Long tenantRecordId = businessContextFactory.getTenantRecordId();
        final ReportGroup reportGroup = businessContextFactory.getReportGroup();
        final CurrencyConverter currencyConverter = businessContextFactory.getCurrencyConverter();

        // Lookup the invoice
        final Invoice invoice = businessContextFactory.getInvoice(invoiceId);

        // Lookup all SubscriptionBundle for that account (this avoids expensive lookups for each item)
        final Iterable<SubscriptionBundle> bundlesForAccount = businessContextFactory.getAccountBundles();
        final Map<UUID, SubscriptionBundle> bundles = new LinkedHashMap<UUID, SubscriptionBundle>();
        for (final SubscriptionBundle bundle : bundlesForAccount) {
            bundles.put(bundle.getId(), bundle);
        }

        final Iterable<Tag> tags = businessContextFactory.getAccountTags();
        final Set<UUID> writtenOffInvoices = new HashSet<UUID>();
        for (final Tag cur : tags) {
            if (cur.getTagDefinitionId().equals(ControlTagType.WRITTEN_OFF.getId())) {
                writtenOffInvoices.add(cur.getObjectId());
            }
        }

        // Create the business invoice items
        final Multimap<UUID, BusinessInvoiceItemBaseModelDao> businessInvoiceItemsForInvoiceId = ArrayListMultimap.<UUID, BusinessInvoiceItemBaseModelDao>create();
        for (final InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
            final AuditLog creationAuditLog = invoiceItem.getId() != null ? businessContextFactory.getInvoiceItemCreationAuditLog(invoiceItem.getId()) : null;
            final boolean isWrittenOff = writtenOffInvoices.contains(invoiceItem.getInvoiceId());

            final Collection<InvoiceItem> otherInvoiceItems = Collections2.filter(invoice.getInvoiceItems(),
                                                                                  new Predicate<InvoiceItem>() {
                                                                                      @Override
                                                                                      public boolean apply(final InvoiceItem input) {
                                                                                          return !input.getId().equals(invoiceItem.getId());
                                                                                      }
                                                                                  }
                                                                                 );
            InvoiceItem linkedInvoiceItem = null;
            if (invoiceItem.getLinkedItemId() != null) {
                // Try to find the linked item on that invoice first
                linkedInvoiceItem = Iterables.tryFind(invoice.getInvoiceItems(),
                                                      new Predicate<InvoiceItem>() {
                                                          @Override
                                                          public boolean apply(final InvoiceItem input) {
                                                              return input.getId().equals(invoiceItem.getLinkedItemId());
                                                          }
                                                      })
                                             .orNull();
                if (linkedInvoiceItem == null) {
                    // We need to go back to the database
                    final Invoice linkedInvoice = businessContextFactory.getInvoiceByInvoiceItemId(invoiceItem.getLinkedItemId());
                    for (final InvoiceItem invoiceItemOnLinkedInvoice : linkedInvoice.getInvoiceItems()) {
                        if (invoiceItem.getLinkedItemId().equals(invoiceItemOnLinkedInvoice.getId())) {
                            linkedInvoiceItem = invoiceItemOnLinkedInvoice;
                            break;
                        }
                    }
                }
            }

            final BusinessInvoiceItemBaseModelDao businessInvoiceItemModelDao = createBusinessInvoiceItem(businessContextFactory,
                                                                                                          account,
                                                                                                          invoice,
                                                                                                          invoiceItem,
                                                                                                          otherInvoiceItems,
                                                                                                          linkedInvoiceItem,
                                                                                                          isWrittenOff,
                                                                                                          bundles,
                                                                                                          currencyConverter,
                                                                                                          creationAuditLog,
                                                                                                          accountRecordId,
                                                                                                          tenantRecordId,
                                                                                                          reportGroup);
            if (businessInvoiceItemModelDao != null) {
                businessInvoiceItemsForInvoiceId.get(businessInvoiceItemModelDao.getInvoiceId()).add(businessInvoiceItemModelDao);
            }
        }

        // Now, create the business invoice
        final Map<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>> businessRecords = new HashMap<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>>();
        createBusinessInvoice(businessRecords,
                              businessContextFactory,
                              invoice,
                              businessInvoiceItemsForInvoiceId,
                              writtenOffInvoices,
                              account,
                              currencyConverter,
                              accountRecordId,
                              tenantRecordId,
                              reportGroup);

        return businessRecords;
    }

    /**
     * Create current business invoices and invoice items.
     *
     * @return all business invoice and invoice items to create
     * @throws AnalyticsRefreshException
     */
    public Map<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>> createBusinessInvoicesAndInvoiceItems(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        // Pre-fetch these, to avoid contention on BusinessContextFactory
        final Account account = businessContextFactory.getAccount();
        final Long accountRecordId = businessContextFactory.getAccountRecordId();
        final Long tenantRecordId = businessContextFactory.getTenantRecordId();
        final ReportGroup reportGroup = businessContextFactory.getReportGroup();
        final CurrencyConverter currencyConverter = businessContextFactory.getCurrencyConverter();

        // Lookup the invoices for that account
        final Iterable<Invoice> invoices = businessContextFactory.getAccountInvoices();

        // All invoice items across all invoices for that account (we need to be able to reference items across multiple invoices)
        final Multimap<UUID, InvoiceItem> allInvoiceItems = ArrayListMultimap.<UUID, InvoiceItem>create();
        // Convenient mapping invoiceId -> invoice
        final Map<UUID, Invoice> invoiceIdToInvoiceMappings = new LinkedHashMap<UUID, Invoice>();
        for (final Invoice invoice : invoices) {
            invoiceIdToInvoiceMappings.put(invoice.getId(), invoice);
            allInvoiceItems.get(invoice.getId()).addAll(invoice.getInvoiceItems());
        }

        // Lookup once all SubscriptionBundle for that account (this avoids expensive lookups for each item)
        final Iterable<SubscriptionBundle> bundlesForAccount = businessContextFactory.getAccountBundles();
        final Map<UUID, SubscriptionBundle> bundles = new LinkedHashMap<UUID, SubscriptionBundle>();
        for (final SubscriptionBundle bundle : bundlesForAccount) {
            bundles.put(bundle.getId(), bundle);
        }

        final Iterable<Tag> tags = businessContextFactory.getAccountTags();
        final Set<UUID> writtenOffInvoices = new HashSet<UUID>();
        for (final Tag cur : tags) {
            if (cur.getTagDefinitionId().equals(ControlTagType.WRITTEN_OFF.getId())) {
                writtenOffInvoices.add(cur.getObjectId());
            }
        }

        // Create the business invoice items
        // We build them in parallel as invoice items are directly proportional to subscriptions (@see BusinessSubscriptionTransitionFactory)
        final CompletionService<BusinessInvoiceItemBaseModelDao> completionService = new ExecutorCompletionService<BusinessInvoiceItemBaseModelDao>(executor);
        final Multimap<UUID, BusinessInvoiceItemBaseModelDao> businessInvoiceItemsForInvoiceId = ArrayListMultimap.<UUID, BusinessInvoiceItemBaseModelDao>create();
        for (final InvoiceItem invoiceItem : allInvoiceItems.values()) {
            // Fetch audit logs in the main thread as AccountAuditLogs is not thread safe
            final AuditLog creationAuditLog = invoiceItem.getId() != null ? businessContextFactory.getInvoiceItemCreationAuditLog(invoiceItem.getId()) : null;

            completionService.submit(new Callable<BusinessInvoiceItemBaseModelDao>() {
                @Override
                public BusinessInvoiceItemBaseModelDao call() throws Exception {
                    final boolean isWrittenOff = writtenOffInvoices.contains(invoiceItem.getInvoiceId());
                    return createBusinessInvoiceItem(businessContextFactory,
                                                     invoiceItem,
                                                     allInvoiceItems,
                                                     invoiceIdToInvoiceMappings,
                                                     isWrittenOff,
                                                     account,
                                                     bundles,
                                                     currencyConverter,
                                                     creationAuditLog,
                                                     accountRecordId,
                                                     tenantRecordId,
                                                     reportGroup);
                }
            });
        }
        for (int i = 0; i < allInvoiceItems.values().size(); ++i) {
            try {
                final BusinessInvoiceItemBaseModelDao businessInvoiceItemModelDao = completionService.take().get();
                if (businessInvoiceItemModelDao != null) {
                    businessInvoiceItemsForInvoiceId.get(businessInvoiceItemModelDao.getInvoiceId()).add(businessInvoiceItemModelDao);
                }
            } catch (final InterruptedException e) {
                throw new AnalyticsRefreshException(e);
            } catch (final ExecutionException e) {
                throw new AnalyticsRefreshException(e);
            }
        }

        // Now, create the business invoices
        final Map<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>> businessRecords = new HashMap<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>>();
        for (final Invoice invoice : invoices) {
            createBusinessInvoice(businessRecords,
                                  businessContextFactory,
                                  invoice,
                                  businessInvoiceItemsForInvoiceId,
                                  writtenOffInvoices,
                                  account,
                                  currencyConverter,
                                  accountRecordId,
                                  tenantRecordId,
                                  reportGroup);
        }

        return businessRecords;
    }

    private void createBusinessInvoice(final Map<BusinessInvoiceModelDao, Collection<BusinessInvoiceItemBaseModelDao>> businessRecords,
                                       final BusinessContextFactory businessContextFactory,
                                       final Invoice invoice,
                                       final Multimap<UUID, BusinessInvoiceItemBaseModelDao> businessInvoiceItemsForInvoiceId,
                                       final Collection<UUID> writtenOffInvoices,
                                       final Account account,
                                       final CurrencyConverter currencyConverter,
                                       final Long accountRecordId,
                                       final Long tenantRecordId,
                                       final ReportGroup reportGroup) throws AnalyticsRefreshException {
        final Collection<BusinessInvoiceItemBaseModelDao> businessInvoiceItems = businessInvoiceItemsForInvoiceId.get(invoice.getId());
        if (businessInvoiceItems == null) {
            return;
        }

        final boolean isWrittenOff = writtenOffInvoices.contains(invoice.getId());

        final Long invoiceRecordId = businessContextFactory.getInvoiceRecordId(invoice.getId());
        final AuditLog creationAuditLog = businessContextFactory.getInvoiceCreationAuditLog(invoice.getId());

        final BusinessInvoiceModelDao businessInvoice = new BusinessInvoiceModelDao(account,
                                                                                    accountRecordId,
                                                                                    invoice,
                                                                                    isWrittenOff,
                                                                                    invoiceRecordId,
                                                                                    currencyConverter,
                                                                                    creationAuditLog,
                                                                                    tenantRecordId,
                                                                                    reportGroup);

        businessRecords.put(businessInvoice, businessInvoiceItems);
    }

    private BusinessInvoiceItemBaseModelDao createBusinessInvoiceItem(final BusinessContextFactory businessContextFactory,
                                                                      final InvoiceItem invoiceItem,
                                                                      final Multimap<UUID, InvoiceItem> allInvoiceItemsAcrossAllInvoices,
                                                                      final Map<UUID, Invoice> invoiceIdToInvoiceMappings,
                                                                      final boolean isWrittenOff,
                                                                      final Account account,
                                                                      final Map<UUID, SubscriptionBundle> bundles,
                                                                      final CurrencyConverter currencyConverter,
                                                                      final AuditLog creationAuditLog,
                                                                      final Long accountRecordId,
                                                                      final Long tenantRecordId,
                                                                      final ReportGroup reportGroup) throws AnalyticsRefreshException {
        final Invoice invoice = invoiceIdToInvoiceMappings.get(invoiceItem.getInvoiceId());
        final Collection<InvoiceItem> otherInvoiceItems = Collections2.filter(allInvoiceItemsAcrossAllInvoices.values(),
                                                                              new Predicate<InvoiceItem>() {
                                                                                  @Override
                                                                                  public boolean apply(final InvoiceItem input) {
                                                                                      return !input.getId().equals(invoiceItem.getId()) &&
                                                                                             input.getInvoiceId().equals(invoiceItem.getInvoiceId());
                                                                                  }
                                                                              }
                                                                             );
        final InvoiceItem linkedInvoiceItem = Iterables.find(allInvoiceItemsAcrossAllInvoices.values(), new Predicate<InvoiceItem>() {
            @Override
            public boolean apply(final InvoiceItem input) {
                return invoiceItem.getLinkedItemId() != null && invoiceItem.getLinkedItemId().equals(input.getId());

            }
        }, null);
        return createBusinessInvoiceItem(businessContextFactory,
                                         account,
                                         invoice,
                                         invoiceItem,
                                         otherInvoiceItems,
                                         linkedInvoiceItem,
                                         isWrittenOff,
                                         bundles,
                                         currencyConverter,
                                         creationAuditLog,
                                         accountRecordId,
                                         tenantRecordId,
                                         reportGroup);
    }

    @VisibleForTesting
    BusinessInvoiceItemBaseModelDao createBusinessInvoiceItem(final BusinessContextFactory businessContextFactory,
                                                              final Account account,
                                                              final Invoice invoice,
                                                              final InvoiceItem invoiceItem,
                                                              final Collection<InvoiceItem> otherInvoiceItems,
                                                              // For convenience, populate empty columns using the linked item
                                                              @Nullable final InvoiceItem linkedInvoiceItem,
                                                              final boolean isWrittenOff,
                                                              final Map<UUID, SubscriptionBundle> bundles,
                                                              final CurrencyConverter currencyConverter,
                                                              final AuditLog creationAuditLog,
                                                              final Long accountRecordId,
                                                              final Long tenantRecordId,
                                                              @Nullable final ReportGroup reportGroup) throws AnalyticsRefreshException {
        SubscriptionBundle bundle = null;
        // Subscription and bundle could be null for e.g. credits or adjustments
        if (invoiceItem.getBundleId() != null) {
            bundle = bundles.get(invoiceItem.getBundleId());
        }
        if (bundle == null && linkedInvoiceItem != null && linkedInvoiceItem.getBundleId() != null) {
            bundle = bundles.get(linkedInvoiceItem.getBundleId());
        }

        //
        // In order to correctly use our catalog api, we should be passing the most recent subscription transition Date,
        // but unfortunately such date is not exposed. Considering we are only using the Plan/PlanPhase to extract
        // attributes that we don't expect should be changing (e.g productName, billingPeriod, ...) and not any
        // pricing info, using a catalog version that contains the original Plan should be good enough.
        // (and certainly better than using current version and possibly finding a missing (deleted) Plan
        //
        final DateTime bundleCreatedDate = bundle != null ? bundle.getCreatedDate() : null;

        Plan plan = null;
        if (Strings.emptyToNull(invoiceItem.getPlanName()) != null) {
            plan = businessContextFactory.getPlanFromInvoiceItem(invoiceItem, bundleCreatedDate);
        }
        if (plan == null && linkedInvoiceItem != null && Strings.emptyToNull(linkedInvoiceItem.getPlanName()) != null) {
            plan = businessContextFactory.getPlanFromInvoiceItem(linkedInvoiceItem, bundleCreatedDate);
        }

        PlanPhase planPhase = null;
        if (invoiceItem.getSubscriptionId() != null && Strings.emptyToNull(invoiceItem.getPhaseName()) != null && bundle != null) {
            planPhase = businessContextFactory.getPlanPhaseFromInvoiceItem(invoiceItem, bundleCreatedDate);
        }
        if (planPhase == null && linkedInvoiceItem != null && linkedInvoiceItem.getSubscriptionId() != null && Strings.emptyToNull(linkedInvoiceItem.getPhaseName()) != null && bundle != null) {
            planPhase = businessContextFactory.getPlanPhaseFromInvoiceItem(linkedInvoiceItem, bundleCreatedDate);
        }

        final Long invoiceItemRecordId = invoiceItem.getId() != null ? businessContextFactory.getInvoiceItemRecordId(invoiceItem.getId()) : null;

        return createBusinessInvoiceItem(account,
                                         invoice,
                                         invoiceItem,
                                         otherInvoiceItems,
                                         isWrittenOff,
                                         bundle,
                                         plan,
                                         planPhase,
                                         invoiceItemRecordId,
                                         currencyConverter,
                                         creationAuditLog,
                                         accountRecordId,
                                         tenantRecordId,
                                         reportGroup);
    }

    @VisibleForTesting
    BusinessInvoiceItemBaseModelDao createBusinessInvoiceItem(final Account account,
                                                              final Invoice invoice,
                                                              final InvoiceItem invoiceItem,
                                                              final Collection<InvoiceItem> otherInvoiceItems,
                                                              final boolean isWrittenOff,
                                                              @Nullable final SubscriptionBundle bundle,
                                                              @Nullable final Plan plan,
                                                              @Nullable final PlanPhase planPhase,
                                                              final Long invoiceItemRecordId,
                                                              final CurrencyConverter currencyConverter,
                                                              final AuditLog creationAuditLog,
                                                              final Long accountRecordId,
                                                              final Long tenantRecordId,
                                                              final ReportGroup reportGroup) throws AnalyticsRefreshException {
        final BusinessInvoiceItemType businessInvoiceItemType;
        if (isCharge(invoiceItem)) {
            businessInvoiceItemType = BusinessInvoiceItemType.CHARGE;
        } else if (isAccountCreditItem(invoiceItem)) {
            businessInvoiceItemType = BusinessInvoiceItemType.ACCOUNT_CREDIT;
        } else if (isInvoiceItemAdjustmentItem(invoiceItem)) {
            businessInvoiceItemType = BusinessInvoiceItemType.INVOICE_ITEM_ADJUSTMENT;
        } else if (isInvoiceAdjustmentItem(invoiceItem, otherInvoiceItems)) {
            businessInvoiceItemType = BusinessInvoiceItemType.INVOICE_ADJUSTMENT;
        } else {
            // We don't care
            return null;
        }

        final ItemSource itemSource = getItemSource(invoiceItem, otherInvoiceItems, businessInvoiceItemType);

        // Unused for now
        final Long secondInvoiceItemRecordId = null;

        return BusinessInvoiceItemBaseModelDao.create(account,
                                                      accountRecordId,
                                                      invoice,
                                                      invoiceItem,
                                                      itemSource,
                                                      isWrittenOff,
                                                      businessInvoiceItemType,
                                                      invoiceItemRecordId,
                                                      secondInvoiceItemRecordId,
                                                      bundle,
                                                      plan,
                                                      planPhase,
                                                      currencyConverter,
                                                      creationAuditLog,
                                                      tenantRecordId,
                                                      reportGroup);
    }

    private ItemSource getItemSource(final InvoiceItem invoiceItem, final Collection<InvoiceItem> otherInvoiceItems, final BusinessInvoiceItemType businessInvoiceItemType) {
        final ItemSource itemSource;
        if (BusinessInvoiceItemType.ACCOUNT_CREDIT.equals(businessInvoiceItemType) && !isRevenueRecognizable(invoiceItem, otherInvoiceItems)) {
            // Non recognizable account credits
            itemSource = ItemSource.user;
        } else if (BusinessInvoiceItemType.INVOICE_ADJUSTMENT.equals(businessInvoiceItemType)) {
            // Invoice adjustments
            itemSource = ItemSource.user;
        } else if (BusinessInvoiceItemType.INVOICE_ITEM_ADJUSTMENT.equals(businessInvoiceItemType) && !InvoiceItemType.REPAIR_ADJ.equals(invoiceItem.getInvoiceItemType())) {
            // Item adjustments (but not repairs)
            itemSource = ItemSource.user;
        } else if (BusinessInvoiceItemType.CHARGE.equals(businessInvoiceItemType) && InvoiceItemType.EXTERNAL_CHARGE.equals(invoiceItem.getInvoiceItemType())) {
            // External charges
            itemSource = ItemSource.user;
        } else {
            // System generated item
            itemSource = null;
        }

        return itemSource;
    }
}
