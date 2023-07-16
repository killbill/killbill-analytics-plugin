/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2023 Equinix, Inc
 * Copyright 2014-2023 The Billing Project, LLC
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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.PlanPhase;
import org.killbill.billing.catalog.api.VersionedCatalog;
import org.killbill.billing.currency.plugin.api.CurrencyPluginApi;
import org.killbill.billing.entitlement.api.BlockingState;
import org.killbill.billing.entitlement.api.Subscription;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentMethod;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfiguration;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfigurationHandler;
import org.killbill.billing.plugin.analytics.dao.CurrencyConversionDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.customfield.CustomField;
import org.killbill.billing.util.entity.Pagination;
import org.killbill.billing.util.tag.Tag;
import org.killbill.billing.util.tag.TagDefinition;
import org.killbill.clock.Clock;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class BusinessContextFactory extends BusinessFactoryBase {

    private static final Logger logger = LoggerFactory.getLogger(BusinessContextFactory.class);

    private static final Map<UUID, AuditLog> accountCreationAuditLogs = new FixedSizeMap<UUID, AuditLog>();
    private static final Map<UUID, AuditLog> bundleCreationAuditLogs = new FixedSizeMap<UUID, AuditLog>();
    private static final Map<UUID, AuditLog> subscriptionEventCreationAuditLogs = new FixedSizeMap<UUID, AuditLog>();
    private static final Map<UUID, AuditLog> blockingStateCreationAuditLogs = new FixedSizeMap<UUID, AuditLog>();
    private static final Map<UUID, AuditLog> invoiceCreationAuditLogs = new FixedSizeMap<UUID, AuditLog>();
    private static final Map<UUID, AuditLog> invoiceItemCreationAuditLogs = new FixedSizeMap<UUID, AuditLog>();
    private static final Map<UUID, AuditLog> invoicePaymentCreationAuditLogs = new FixedSizeMap<UUID, AuditLog>();
    private static final Map<UUID, AuditLog> paymentCreationAuditLogs = new FixedSizeMap<UUID, AuditLog>();
    private static final Map<UUID, AuditLog> tagCreationAuditLogs = new FixedSizeMap<UUID, AuditLog>();
    private static final Map<UUID, AuditLog> customFieldCreationAuditLogs = new FixedSizeMap<UUID, AuditLog>();

    private final Long accountRecordId;
    private final Long tenantRecordId;
    private final SafeAccountAuditLogs safeAccountAuditLogs;
    private final ReportGroup reportGroup;
    private final AnalyticsConfigurationHandler analyticsConfigurationHandler;

    private volatile PluginPropertiesManager pluginPropertiesManager;
    private volatile CurrencyConverter currencyConverter;
    private volatile Account account;
    private volatile Account parentAccount;
    private volatile BigDecimal accountBalance;
    // Relatively cheap lookups (assuming low cardinality), should be done by account_record_id
    private volatile Iterable<SubscriptionBundle> accountBundles;
    private volatile Iterable<BlockingState> accountBlockingStates;
    private volatile Map<UUID, Invoice> invoices = new HashMap<UUID, Invoice>();
    private volatile Map<UUID, Invoice> invoicesByInvoiceItem = new HashMap<UUID, Invoice>();
    private volatile Iterable<Invoice> accountInvoices;
    private volatile Map<UUID, List<InvoicePayment>> accountInvoicePayments;
    private volatile Iterable<Payment> accountPayments;
    private volatile Map<UUID, PaymentMethod> accountPaymentMethods;
    private volatile Iterable<Tag> accountTags;
    private volatile Iterable<CustomField> accountCustomFields;
    // Cheap lookups (should be in Ehcache)
    private volatile Map<UUID, Long> bundleRecordIds = new HashMap<UUID, Long>();
    private volatile Map<UUID, Long> subscriptionEventRecordIds = new HashMap<UUID, Long>();
    private volatile Map<UUID, Long> blockingStateRecordIds = new HashMap<UUID, Long>();
    private volatile Map<UUID, Long> invoiceRecordIds = new HashMap<UUID, Long>();
    private volatile Map<UUID, Long> invoiceItemRecordIds = new HashMap<UUID, Long>();
    private volatile Map<UUID, Long> invoicePaymentRecordIds = new HashMap<UUID, Long>();
    private volatile Map<UUID, Long> paymentRecordIds = new HashMap<UUID, Long>();
    private volatile Map<UUID, Long> tagRecordIds = new HashMap<UUID, Long>();
    private volatile Map<UUID, Long> customFieldRecordIds = new HashMap<UUID, Long>();
    // Others
    private volatile Map<UUID, SubscriptionBundle> cachedBundles = new HashMap<UUID, SubscriptionBundle>();
    private volatile Map<UUID, Subscription> subscriptions = new HashMap<UUID, Subscription>();
    private volatile Map<String, SubscriptionBundle> latestSubscriptionBundleForExternalKeys = new HashMap<String, SubscriptionBundle>();
    private volatile Map<UUID, TagDefinition> tagDefinitions = new HashMap<UUID, TagDefinition>();
    private volatile VersionedCatalog catalog;

    public BusinessContextFactory(final UUID accountId,
                                  final CallContext callContext,
                                  final ServiceTracker<CurrencyPluginApi, CurrencyPluginApi> currencyPluginApiServiceTracker,
                                  final CurrencyConversionDao currencyConversionDao,
                                  final OSGIKillbillAPI osgiKillbillAPI,
                                  final OSGIConfigPropertiesService osgiConfigPropertiesService,
                                  final Clock clock,
                                  final AnalyticsConfigurationHandler analyticsConfigurationHandler) throws AnalyticsRefreshException {
        super(accountId, callContext, currencyPluginApiServiceTracker, currencyConversionDao, osgiKillbillAPI, osgiConfigPropertiesService, clock, analyticsConfigurationHandler);
        this.analyticsConfigurationHandler = analyticsConfigurationHandler;

        // Always needed
        this.accountRecordId = getAccountRecordId(accountId, callContext);
        this.safeAccountAuditLogs = new SafeAccountAuditLogs(osgiKillbillAPI, accountId, callContext);
        this.tenantRecordId = getTenantRecordId(callContext);
        this.reportGroup = getReportGroup(getAccountTags());
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Long getAccountRecordId() {
        return accountRecordId;
    }

    public Long getTenantRecordId() {
        return tenantRecordId;
    }

    public CallContext getCallContext() {
        return callContext;
    }

    public ReportGroup getReportGroup() {
        return reportGroup;
    }

    public PluginPropertiesManager getPluginPropertiesManager() {
        if (pluginPropertiesManager == null) {
            synchronized (this) {
                if (pluginPropertiesManager == null) {
                    final AnalyticsConfiguration analyticsConfiguration = analyticsConfigurationHandler.getConfigurable(callContext.getTenantId());
                    pluginPropertiesManager = new PluginPropertiesManager(analyticsConfiguration);
                }
            }
        }
        return pluginPropertiesManager;
    }

    @Override
    public CurrencyConverter getCurrencyConverter() {
        if (currencyConverter == null) {
            synchronized (this) {
                if (currencyConverter == null) {
                    currencyConverter = super.getCurrencyConverter();
                }
            }
        }
        return currencyConverter;
    }

    public Account getAccount() throws AnalyticsRefreshException {
        if (account == null) {
            synchronized (this) {
                if (account == null) {
                    account = getAccount(accountId, callContext);
                }
            }
        }
        return account;
    }

    public Account getParentAccount() throws AnalyticsRefreshException {
        if (account != null && account.getParentAccountId() != null && parentAccount == null) {
            synchronized (this) {
                if (account != null && account.getParentAccountId() != null && parentAccount == null) {
                    parentAccount = getAccount(account.getParentAccountId(), callContext);
                }
            }
        }
        return parentAccount;
    }

    public BigDecimal getAccountBalance() throws AnalyticsRefreshException {
        if (accountBalance == null) {
            synchronized (this) {
                if (accountBalance == null) {
                    accountBalance = getAccountBalance(accountId, callContext);
                }
            }
        }
        return accountBalance;
    }

    public Iterable<SubscriptionBundle> getAccountBundles() throws AnalyticsRefreshException {
        if (accountBundles == null) {
            synchronized (this) {
                if (accountBundles == null) {
                    accountBundles = getSubscriptionBundlesForAccount(accountId, callContext);
                    for (final SubscriptionBundle subscriptionBundle : accountBundles) {
                        cachedBundles.put(subscriptionBundle.getId(), subscriptionBundle);

                        // Pre-populate latestSubscriptionBundleForExternalKeys cache to avoid calling getLatestSubscriptionBundleForExternalKey for each bundle
                        if (latestSubscriptionBundleForExternalKeys.get(subscriptionBundle.getExternalKey()) == null ||
                            latestSubscriptionBundleForExternalKeys.get(subscriptionBundle.getExternalKey()).getCreatedDate().compareTo(subscriptionBundle.getCreatedDate()) > 0) {
                            latestSubscriptionBundleForExternalKeys.put(subscriptionBundle.getExternalKey(), subscriptionBundle);
                        }
                    }
                }
            }
        }
        return accountBundles;
    }

    public Iterable<BlockingState> getAccountBlockingStates() throws AnalyticsRefreshException {
        if (accountBlockingStates == null) {
            synchronized (this) {
                if (accountBlockingStates == null) {
                    // Filter all service state changes
                    accountBlockingStates = Iterables.<BlockingState>filter(getBlockingStatesForAccount(accountId, callContext),
                                                                            new Predicate<BlockingState>() {
                                                                                @Override
                                                                                public boolean apply(final BlockingState state) {
                                                                                    return state != null &&
                                                                                           // We want events coming from the blocking states table that are for any service but entitlement
                                                                                           !BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME.equals(state.getService());
                                                                                }
                                                                            }
                                                                           );
                }
            }
        }
        return accountBlockingStates;
    }

    public Invoice getInvoice(final UUID invoiceId) throws AnalyticsRefreshException {
        if (invoices.get(invoiceId) == null) {
            synchronized (this) {
                if (invoices.get(invoiceId) == null) {
                    final Invoice invoice = getInvoice(invoiceId, callContext);
                    invoices.put(invoiceId, invoice);

                    for (final InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                        invoicesByInvoiceItem.put(invoiceItem.getId(), invoice);
                    }
                }
            }
        }
        return invoices.get(invoiceId);
    }

    public Invoice getInvoiceByInvoiceItemId(final UUID invoiceItemId) throws AnalyticsRefreshException {
        if (invoicesByInvoiceItem.get(invoiceItemId) == null) {
            synchronized (this) {
                if (invoicesByInvoiceItem.get(invoiceItemId) == null) {
                    final Invoice invoice = getInvoiceByInvoiceItemId(invoiceItemId, callContext);
                    invoicesByInvoiceItem.put(invoiceItemId, invoice);

                    invoices.put(invoice.getId(), invoice);
                }
            }
        }
        return invoicesByInvoiceItem.get(invoiceItemId);
    }

    public Iterable<Invoice> getAccountInvoices() throws AnalyticsRefreshException {
        if (accountInvoices == null) {
            synchronized (this) {
                if (accountInvoices == null) {
                    accountInvoices = getInvoicesByAccountId(accountId, callContext);

                    if (invoicesByInvoiceItem == null) {
                        invoicesByInvoiceItem = new HashMap<UUID, Invoice>();
                    }
                    for (final Invoice invoice : accountInvoices) {
                        for (final InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                            invoicesByInvoiceItem.put(invoiceItem.getId(), invoice);
                        }
                    }

                    if (invoices == null) {
                        invoices = new HashMap<UUID, Invoice>();
                    }
                    for (final Invoice invoice : accountInvoices) {
                        invoices.put(invoice.getId(), invoice);
                    }
                }
            }
        }
        return accountInvoices;
    }

    public Invoice getLastInvoice() throws AnalyticsRefreshException {
        Invoice lastShallowInvoice = null;
        Pagination<Invoice> shallowInvoicesByAccountId = null;
        try {
            shallowInvoicesByAccountId = getShallowInvoicesByAccountId(accountId, callContext);
            for (final Invoice shallowInvoice : shallowInvoicesByAccountId) {
                if (lastShallowInvoice == null || shallowInvoice.getInvoiceDate().isAfter(lastShallowInvoice.getInvoiceDate())) {
                    lastShallowInvoice = shallowInvoice;
                }
            }
        } finally {
            try {
                if (shallowInvoicesByAccountId != null) {
                    shallowInvoicesByAccountId.close();
                }
            } catch (final IOException e) {
                // Not much we can do...
                logger.warn("Unable to close Pagination object", e);
            }
        }
        return lastShallowInvoice == null ? null : getInvoice(lastShallowInvoice.getId());
    }

    public Map<UUID, List<InvoicePayment>> getAccountInvoicePayments() throws AnalyticsRefreshException {
        if (accountInvoicePayments == null) {
            synchronized (this) {
                if (accountInvoicePayments == null) {
                    accountInvoicePayments = getAccountInvoicePayments(getAccountPayments(), callContext);
                }
            }
        }
        return accountInvoicePayments;
    }

    public Iterable<Payment> getAccountPayments() throws AnalyticsRefreshException {
        if (accountPayments == null) {
            synchronized (this) {
                if (accountPayments == null) {
                    accountPayments = getPaymentsWithPluginInfoByAccountId(accountId, callContext);
                }
            }
        }
        return accountPayments;
    }

    public PaymentMethod getPaymentMethod(final UUID paymentMethodId) throws AnalyticsRefreshException {
        if (accountPaymentMethods == null) {
            synchronized (this) {
                if (accountPaymentMethods == null) {
                    final Map<UUID, PaymentMethod> accountPaymentMethodsCopy = new HashMap<UUID, PaymentMethod>();
                    for (final PaymentMethod paymentMethod : getPaymentMethodsForAccount(accountId, callContext)) {
                        accountPaymentMethodsCopy.put(paymentMethod.getId(), paymentMethod);
                    }
                    accountPaymentMethods = accountPaymentMethodsCopy;
                }
            }
        }
        return accountPaymentMethods.get(paymentMethodId);
    }

    public Iterable<Tag> getAccountTags() throws AnalyticsRefreshException {
        if (accountTags == null) {
            synchronized (this) {
                if (accountTags == null) {
                    accountTags = getTagsForAccount(accountId, callContext);
                }
            }
        }
        return accountTags;
    }

    public Iterable<CustomField> getAccountCustomFields() throws AnalyticsRefreshException {
        if (accountCustomFields == null) {
            synchronized (this) {
                if (accountCustomFields == null) {
                    accountCustomFields = getFieldsForAccount(accountId, callContext);
                }
            }
        }
        return accountCustomFields;
    }

    public AuditLog getAccountCreationAuditLog() throws AnalyticsRefreshException {
        if (accountCreationAuditLogs.get(accountId) == null) {
            synchronized (this) {
                if (accountCreationAuditLogs.get(accountId) == null) {
                    accountCreationAuditLogs.put(accountId, getAccountCreationAuditLog(accountId, safeAccountAuditLogs));
                }
            }
        }
        return accountCreationAuditLogs.get(accountId);
    }

    public AuditLog getBundleCreationAuditLog(final UUID bundleId) throws AnalyticsRefreshException {
        if (bundleCreationAuditLogs.get(bundleId) == null) {
            synchronized (this) {
                if (bundleCreationAuditLogs.get(bundleId) == null) {
                    bundleCreationAuditLogs.put(bundleId, getBundleCreationAuditLog(bundleId, safeAccountAuditLogs));
                }
            }
        }
        return bundleCreationAuditLogs.get(bundleId);
    }

    public AuditLog getSubscriptionEventCreationAuditLog(final UUID subscriptionEventId, final ObjectType objectType) throws AnalyticsRefreshException {
        if (subscriptionEventCreationAuditLogs.get(subscriptionEventId) == null) {
            synchronized (this) {
                if (subscriptionEventCreationAuditLogs.get(subscriptionEventId) == null) {
                    subscriptionEventCreationAuditLogs.put(subscriptionEventId, getSubscriptionEventCreationAuditLog(subscriptionEventId, objectType, safeAccountAuditLogs));
                }
            }
        }
        return subscriptionEventCreationAuditLogs.get(subscriptionEventId);
    }

    public AuditLog getBlockingStateCreationAuditLog(final UUID blockingStateId) throws AnalyticsRefreshException {
        if (blockingStateCreationAuditLogs.get(blockingStateId) == null) {
            synchronized (this) {
                if (blockingStateCreationAuditLogs.get(blockingStateId) == null) {
                    blockingStateCreationAuditLogs.put(blockingStateId, getBlockingStateCreationAuditLog(blockingStateId, safeAccountAuditLogs));
                }
            }
        }
        return blockingStateCreationAuditLogs.get(blockingStateId);
    }

    public AuditLog getInvoiceCreationAuditLog(final UUID invoiceId) throws AnalyticsRefreshException {
        if (invoiceCreationAuditLogs.get(invoiceId) == null) {
            synchronized (this) {
                if (invoiceCreationAuditLogs.get(invoiceId) == null) {
                    invoiceCreationAuditLogs.put(invoiceId, getInvoiceCreationAuditLog(invoiceId, safeAccountAuditLogs));
                }
            }
        }
        return invoiceCreationAuditLogs.get(invoiceId);
    }

    public AuditLog getInvoiceItemCreationAuditLog(final UUID invoiceItemId) throws AnalyticsRefreshException {
        if (invoiceItemCreationAuditLogs.get(invoiceItemId) == null) {
            synchronized (this) {
                if (invoiceItemCreationAuditLogs.get(invoiceItemId) == null) {
                    invoiceItemCreationAuditLogs.put(invoiceItemId, getInvoiceItemCreationAuditLog(invoiceItemId, safeAccountAuditLogs));
                }
            }
        }
        return invoiceItemCreationAuditLogs.get(invoiceItemId);
    }

    public AuditLog getInvoicePaymentCreationAuditLog(final UUID invoicePaymentId) throws AnalyticsRefreshException {
        if (invoicePaymentCreationAuditLogs.get(invoicePaymentId) == null) {
            synchronized (this) {
                if (invoicePaymentCreationAuditLogs.get(invoicePaymentId) == null) {
                    invoicePaymentCreationAuditLogs.put(invoicePaymentId, getInvoicePaymentCreationAuditLog(invoicePaymentId, safeAccountAuditLogs));
                }
            }
        }
        return invoicePaymentCreationAuditLogs.get(invoicePaymentId);
    }

    public AuditLog getPaymentCreationAuditLog(final UUID paymentId) throws AnalyticsRefreshException {
        if (paymentCreationAuditLogs.get(paymentId) == null) {
            synchronized (this) {
                if (paymentCreationAuditLogs.get(paymentId) == null) {
                    paymentCreationAuditLogs.put(paymentId, getPaymentCreationAuditLog(paymentId, safeAccountAuditLogs));
                }
            }
        }
        return paymentCreationAuditLogs.get(paymentId);
    }

    public AuditLog getTagCreationAuditLog(final UUID tagId) throws AnalyticsRefreshException {
        if (tagCreationAuditLogs.get(tagId) == null) {
            synchronized (this) {
                if (tagCreationAuditLogs.get(tagId) == null) {
                    tagCreationAuditLogs.put(tagId, getTagCreationAuditLog(tagId, safeAccountAuditLogs));
                }
            }
        }
        return tagCreationAuditLogs.get(tagId);
    }

    public AuditLog getCustomFieldCreationAuditLog(final UUID customFieldId) throws AnalyticsRefreshException {
        if (customFieldCreationAuditLogs.get(customFieldId) == null) {
            synchronized (this) {
                if (customFieldCreationAuditLogs.get(customFieldId) == null) {
                    customFieldCreationAuditLogs.put(customFieldId, getFieldCreationAuditLog(customFieldId, safeAccountAuditLogs));
                }
            }
        }
        return customFieldCreationAuditLogs.get(customFieldId);
    }

    public Long getBundleRecordId(final UUID bundleId) throws AnalyticsRefreshException {
        if (bundleRecordIds.get(bundleId) == null) {
            synchronized (this) {
                if (bundleRecordIds.get(bundleId) == null) {
                    bundleRecordIds.put(bundleId, getBundleRecordId(bundleId, callContext));
                }
            }
        }
        return bundleRecordIds.get(bundleId);
    }

    public Long getSubscriptionEventRecordId(final UUID subscriptionEventId, final ObjectType objectType) throws AnalyticsRefreshException {
        if (subscriptionEventRecordIds.get(subscriptionEventId) == null) {
            synchronized (this) {
                if (subscriptionEventRecordIds.get(subscriptionEventId) == null) {
                    subscriptionEventRecordIds.put(subscriptionEventId, getSubscriptionEventRecordId(subscriptionEventId, objectType, callContext));
                }
            }
        }
        return subscriptionEventRecordIds.get(subscriptionEventId);
    }

    public Long getBlockingStateRecordId(final UUID blockingStateId) throws AnalyticsRefreshException {
        if (blockingStateRecordIds.get(blockingStateId) == null) {
            synchronized (this) {
                if (blockingStateRecordIds.get(blockingStateId) == null) {
                    blockingStateRecordIds.put(blockingStateId, getBlockingStateRecordId(blockingStateId, callContext));
                }
            }
        }
        return blockingStateRecordIds.get(blockingStateId);
    }

    public Long getInvoiceRecordId(final UUID invoiceId) throws AnalyticsRefreshException {
        if (invoiceRecordIds.get(invoiceId) == null) {
            synchronized (this) {
                if (invoiceRecordIds.get(invoiceId) == null) {
                    invoiceRecordIds.put(invoiceId, getInvoiceRecordId(invoiceId, callContext));
                }
            }
        }
        return invoiceRecordIds.get(invoiceId);
    }

    public Long getInvoiceItemRecordId(final UUID invoiceItemId) throws AnalyticsRefreshException {
        if (invoiceItemRecordIds.get(invoiceItemId) == null) {
            synchronized (this) {
                if (invoiceItemRecordIds.get(invoiceItemId) == null) {
                    invoiceItemRecordIds.put(invoiceItemId, getInvoiceItemRecordId(invoiceItemId, callContext));
                }
            }
        }
        return invoiceItemRecordIds.get(invoiceItemId);
    }

    public Long getInvoicePaymentRecordId(final UUID invoicePaymentId) throws AnalyticsRefreshException {
        if (invoicePaymentRecordIds.get(invoicePaymentId) == null) {
            synchronized (this) {
                if (invoicePaymentRecordIds.get(invoicePaymentId) == null) {
                    invoicePaymentRecordIds.put(invoicePaymentId, getInvoicePaymentRecordId(invoicePaymentId, callContext));
                }
            }
        }
        return invoicePaymentRecordIds.get(invoicePaymentId);
    }

    public Long getPaymentRecordId(final UUID paymentId) throws AnalyticsRefreshException {
        if (paymentRecordIds.get(paymentId) == null) {
            synchronized (this) {
                if (paymentRecordIds.get(paymentId) == null) {
                    paymentRecordIds.put(paymentId, getPaymentRecordId(paymentId, callContext));
                }
            }
        }
        return paymentRecordIds.get(paymentId);
    }

    public Long getTagRecordId(final UUID tagId) throws AnalyticsRefreshException {
        if (tagRecordIds.get(tagId) == null) {
            synchronized (this) {
                if (tagRecordIds.get(tagId) == null) {
                    tagRecordIds.put(tagId, getTagRecordId(tagId, callContext));
                }
            }
        }
        return tagRecordIds.get(tagId);
    }

    public Long getCustomFieldRecordId(final UUID customFieldId) throws AnalyticsRefreshException {
        if (customFieldRecordIds.get(customFieldId) == null) {
            synchronized (this) {
                if (customFieldRecordIds.get(customFieldId) == null) {
                    customFieldRecordIds.put(customFieldId, getFieldRecordId(customFieldId, callContext));
                }
            }
        }
        return customFieldRecordIds.get(customFieldId);
    }

    public SubscriptionBundle getSubscriptionBundle(final UUID bundleId) throws AnalyticsRefreshException {
        if (cachedBundles.get(bundleId) == null) {
            synchronized (this) {
                if (cachedBundles.get(bundleId) == null) {
                    if (highCardinalityAccount()) {
                        // Avoid per account query
                        cachedBundles.put(bundleId, getSubscriptionBundle(bundleId, callContext));
                    } else {
                        // Populate the accountBundles cache, which will cache the second cachedBundles one
                        getAccountBundles();
                    }
                }
            }
        }
        return cachedBundles.get(bundleId);
    }

    public Subscription getSubscription(final UUID subscriptionId) throws AnalyticsRefreshException {
        if (subscriptions.get(subscriptionId) == null) {
            synchronized (this) {
                if (subscriptions.get(subscriptionId) == null) {
                    subscriptions.put(subscriptionId, getSubscription(subscriptionId, callContext));
                }
            }
        }
        return subscriptions.get(subscriptionId);
    }

    public SubscriptionBundle getLatestSubscriptionBundleForExternalKey(final String externalKey) throws AnalyticsRefreshException {
        if (latestSubscriptionBundleForExternalKeys.get(externalKey) == null) {
            synchronized (this) {
                if (latestSubscriptionBundleForExternalKeys.get(externalKey) == null) {
                    latestSubscriptionBundleForExternalKeys.put(externalKey, getLatestSubscriptionBundleForExternalKey(externalKey, callContext));
                }
            }
        }
        return latestSubscriptionBundleForExternalKeys.get(externalKey);
    }

    public TagDefinition getTagDefinition(final UUID tagDefinitionId) throws AnalyticsRefreshException {
        if (tagDefinitions.isEmpty()) {
            synchronized (this) {
                if (tagDefinitions.isEmpty()) {
                    tagDefinitions = new HashMap<UUID, TagDefinition>();
                    for (final TagDefinition tagDefinition : getTagDefinitions(callContext)) {
                        tagDefinitions.put(tagDefinition.getId(), tagDefinition);
                    }
                }
            }
        }
        return tagDefinitions.get(tagDefinitionId);
    }

    // Simple pass-through

    public Plan getPlanFromInvoiceItem(final InvoiceItem invoiceItem) throws AnalyticsRefreshException {
        return getPlanFromInvoiceItem(invoiceItem, getCatalog());
    }

    public PlanPhase getPlanPhaseFromInvoiceItem(final InvoiceItem invoiceItem) throws AnalyticsRefreshException {
        return getPlanPhaseFromInvoiceItem(invoiceItem, getCatalog());
    }

    private VersionedCatalog getCatalog() throws AnalyticsRefreshException {
        if (catalog == null) {
            synchronized (this) {
                if (catalog == null) {
                    catalog = getCatalog(callContext);
                }
            }
        }
        return catalog;
    }
}
