/*
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.PlanPhase;
import org.killbill.billing.catalog.api.VersionedCatalog;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.entitlement.api.SubscriptionEvent;
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
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AccountAuditLogs;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.customfield.CustomField;
import org.killbill.billing.util.tag.Tag;
import org.killbill.billing.util.tag.TagDefinition;
import org.killbill.clock.Clock;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class BusinessContextFactory extends BusinessFactoryBase {

    private final UUID accountId;
    private final Long accountRecordId;
    private final AccountAuditLogs accountAuditLogs;
    private final Long tenantRecordId;
    private final BusinessModelDaoBase.ReportGroup reportGroup;
    private final CallContext callContext;
    private final AnalyticsConfigurationHandler analyticsConfigurationHandler;

    private PluginPropertiesManager pluginPropertiesManager;
    private CurrencyConverter currencyConverter;
    private Account account;
    private Account parentAccount;
    private BigDecimal accountBalance;
    // Relatively cheap lookups, should be done by account_record_id
    private Iterable<SubscriptionBundle> accountBundles;
    private Iterable<SubscriptionEvent> accountBlockingStates;
    private Map<UUID, Invoice> invoices = new HashMap<UUID, Invoice>();
    private Map<UUID, Invoice> invoicesByInvoiceItem = new HashMap<UUID, Invoice>();
    private Iterable<Invoice> accountInvoices;
    private Map<UUID, List<InvoicePayment>> accountInvoicePayments;
    private Iterable<Payment> accountPayments;
    private Map<UUID, PaymentMethod> accountPaymentMethods;
    private Iterable<Tag> accountTags;
    private Iterable<CustomField> accountCustomFields;
    // Cheap lookups, as all audit logs have been pre-fetched
    private AuditLog accountCreationAuditLog;
    private Map<UUID, AuditLog> bundleCreationAuditLogs = new HashMap<UUID, AuditLog>();
    private Map<UUID, AuditLog> subscriptionEventCreationAuditLogs = new HashMap<UUID, AuditLog>();
    private Map<UUID, AuditLog> blockingStateCreationAuditLogs = new HashMap<UUID, AuditLog>();
    private Map<UUID, AuditLog> invoiceCreationAuditLogs = new HashMap<UUID, AuditLog>();
    private Map<UUID, AuditLog> invoiceItemCreationAuditLogs = new HashMap<UUID, AuditLog>();
    private Map<UUID, AuditLog> invoicePaymentCreationAuditLogs = new HashMap<UUID, AuditLog>();
    private Map<UUID, AuditLog> paymentCreationAuditLogs = new HashMap<UUID, AuditLog>();
    private Map<UUID, AuditLog> tagCreationAuditLogs = new HashMap<UUID, AuditLog>();
    private Map<UUID, AuditLog> customFieldCreationAuditLogs = new HashMap<UUID, AuditLog>();
    // Cheap lookups (should be in Ehcache)
    private Map<UUID, Long> bundleRecordIds = new HashMap<UUID, Long>();
    private Map<UUID, Long> subscriptionEventRecordIds = new HashMap<UUID, Long>();
    private Map<UUID, Long> blockingStateRecordIds = new HashMap<UUID, Long>();
    private Map<UUID, Long> invoiceRecordIds = new HashMap<UUID, Long>();
    private Map<UUID, Long> invoiceItemRecordIds = new HashMap<UUID, Long>();
    private Map<UUID, Long> invoicePaymentRecordIds = new HashMap<UUID, Long>();
    private Map<UUID, Long> paymentRecordIds = new HashMap<UUID, Long>();
    private Map<UUID, Long> tagRecordIds = new HashMap<UUID, Long>();
    private Map<UUID, Long> customFieldRecordIds = new HashMap<UUID, Long>();
    // Others
    private Map<String, SubscriptionBundle> latestSubscriptionBundleForExternalKeys = new HashMap<String, SubscriptionBundle>();
    private Map<UUID, TagDefinition> tagDefinitions = new HashMap<UUID, TagDefinition>();
    private VersionedCatalog catalog;

    public BusinessContextFactory(final UUID accountId,
                                  final CallContext callContext,
                                  final CurrencyConversionDao currencyConversionDao,
                                  final OSGIKillbillAPI osgiKillbillAPI,
                                  final OSGIConfigPropertiesService osgiConfigPropertiesService,
                                  final Clock clock,
                                  final AnalyticsConfigurationHandler analyticsConfigurationHandler) throws AnalyticsRefreshException {
        super(currencyConversionDao, osgiKillbillAPI, osgiConfigPropertiesService, clock);
        this.accountId = accountId;
        this.callContext = callContext;
        this.analyticsConfigurationHandler = analyticsConfigurationHandler;

        // Always needed
        this.accountRecordId = getAccountRecordId(accountId, callContext);
        this.accountAuditLogs = getAccountAuditLogs(accountId, callContext);
        this.tenantRecordId = getTenantRecordId(callContext);
        this.reportGroup = getReportGroup(getAccountTags());
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Long getAccountRecordId() {
        return accountRecordId;
    }

    public AccountAuditLogs getAccountAuditLogs() {
        return accountAuditLogs;
    }

    public Long getTenantRecordId() {
        return tenantRecordId;
    }

    public CallContext getCallContext() {
        return callContext;
    }

    public BusinessModelDaoBase.ReportGroup getReportGroup() {
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
                }
            }
        }
        return accountBundles;
    }

    public Iterable<SubscriptionEvent> getAccountBlockingStates() throws AnalyticsRefreshException {
        if (accountBlockingStates == null) {
            synchronized (this) {
                if (accountBlockingStates == null) {
                    // Find all subscription events for that account
                    final Iterable<SubscriptionEvent> subscriptionEvents = Iterables.<SubscriptionEvent>concat(Iterables.<SubscriptionBundle, List<SubscriptionEvent>>transform(getAccountBundles(),
                                                                                                                                                                                new Function<SubscriptionBundle, List<SubscriptionEvent>>() {
                                                                                                                                                                                    @Override
                                                                                                                                                                                    public List<SubscriptionEvent> apply(final SubscriptionBundle bundle) {
                                                                                                                                                                                        return bundle.getTimeline().getSubscriptionEvents();
                                                                                                                                                                                    }
                                                                                                                                                                                }
                                                                                                                                                                               ));

                    // Filter all service state changes
                    accountBlockingStates = Iterables.<SubscriptionEvent>filter(subscriptionEvents,
                                                                                new Predicate<SubscriptionEvent>() {
                                                                                    @Override
                                                                                    public boolean apply(final SubscriptionEvent event) {
                                                                                        return event.getSubscriptionEventType() != null &&
                                                                                               // We want events coming from the blocking states table...
                                                                                               ObjectType.BLOCKING_STATES.equals(event.getSubscriptionEventType().getObjectType()) &&
                                                                                               // ...that are for any service but entitlement
                                                                                               !BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME.equals(event.getServiceName());
                                                                                    }

                                                                                    @Override
                                                                                    public boolean test(@Nullable final SubscriptionEvent input) {
                                                                                        return apply(input);
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
                    accountPaymentMethods = new HashMap<UUID, PaymentMethod>();
                    for (final PaymentMethod paymentMethod : getPaymentMethodsForAccount(accountId, callContext)) {
                        accountPaymentMethods.put(paymentMethod.getId(), paymentMethod);
                    }
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
        if (accountCreationAuditLog == null) {
            synchronized (this) {
                if (accountCreationAuditLog == null) {
                    accountCreationAuditLog = getAccountCreationAuditLog(accountId, accountAuditLogs);
                }
            }
        }
        return accountCreationAuditLog;
    }

    public AuditLog getBundleCreationAuditLog(final UUID bundleId) throws AnalyticsRefreshException {
        if (bundleCreationAuditLogs.get(bundleId) == null) {
            synchronized (this) {
                if (bundleCreationAuditLogs.get(bundleId) == null) {
                    bundleCreationAuditLogs.put(bundleId, getBundleCreationAuditLog(bundleId, accountAuditLogs));
                }
            }
        }
        return bundleCreationAuditLogs.get(bundleId);
    }

    public AuditLog getSubscriptionEventCreationAuditLog(final UUID subscriptionEventId, final ObjectType objectType) throws AnalyticsRefreshException {
        if (subscriptionEventCreationAuditLogs.get(subscriptionEventId) == null) {
            synchronized (this) {
                if (subscriptionEventCreationAuditLogs.get(subscriptionEventId) == null) {
                    subscriptionEventCreationAuditLogs.put(subscriptionEventId, getSubscriptionEventCreationAuditLog(subscriptionEventId, objectType, accountAuditLogs));
                }
            }
        }
        return subscriptionEventCreationAuditLogs.get(subscriptionEventId);
    }

    public AuditLog getBlockingStateCreationAuditLog(final UUID blockingStateId) throws AnalyticsRefreshException {
        if (blockingStateCreationAuditLogs.get(blockingStateId) == null) {
            synchronized (this) {
                if (blockingStateCreationAuditLogs.get(blockingStateId) == null) {
                    blockingStateCreationAuditLogs.put(blockingStateId, getBlockingStateCreationAuditLog(blockingStateId, accountAuditLogs));
                }
            }
        }
        return blockingStateCreationAuditLogs.get(blockingStateId);
    }

    public AuditLog getInvoiceCreationAuditLog(final UUID invoiceId) throws AnalyticsRefreshException {
        if (invoiceCreationAuditLogs.get(invoiceId) == null) {
            synchronized (this) {
                if (invoiceCreationAuditLogs.get(invoiceId) == null) {
                    invoiceCreationAuditLogs.put(invoiceId, getInvoiceCreationAuditLog(invoiceId, accountAuditLogs));
                }
            }
        }
        return invoiceCreationAuditLogs.get(invoiceId);
    }

    public AuditLog getInvoiceItemCreationAuditLog(final UUID invoiceItemId) throws AnalyticsRefreshException {
        if (invoiceItemCreationAuditLogs.get(invoiceItemId) == null) {
            synchronized (this) {
                if (invoiceItemCreationAuditLogs.get(invoiceItemId) == null) {
                    invoiceItemCreationAuditLogs.put(invoiceItemId, getInvoiceItemCreationAuditLog(invoiceItemId, accountAuditLogs));
                }
            }
        }
        return invoiceItemCreationAuditLogs.get(invoiceItemId);
    }

    public AuditLog getInvoicePaymentCreationAuditLog(final UUID invoicePaymentId) throws AnalyticsRefreshException {
        if (invoicePaymentCreationAuditLogs.get(invoicePaymentId) == null) {
            synchronized (this) {
                if (invoicePaymentCreationAuditLogs.get(invoicePaymentId) == null) {
                    invoicePaymentCreationAuditLogs.put(invoicePaymentId, getInvoicePaymentCreationAuditLog(invoicePaymentId, accountAuditLogs));
                }
            }
        }
        return invoicePaymentCreationAuditLogs.get(invoicePaymentId);
    }

    public AuditLog getPaymentCreationAuditLog(final UUID paymentId) throws AnalyticsRefreshException {
        if (paymentCreationAuditLogs.get(paymentId) == null) {
            synchronized (this) {
                if (paymentCreationAuditLogs.get(paymentId) == null) {
                    paymentCreationAuditLogs.put(paymentId, getPaymentCreationAuditLog(paymentId, accountAuditLogs));
                }
            }
        }
        return paymentCreationAuditLogs.get(paymentId);
    }

    public AuditLog getTagCreationAuditLog(final UUID tagId) throws AnalyticsRefreshException {
        if (tagCreationAuditLogs.get(tagId) == null) {
            synchronized (this) {
                if (tagCreationAuditLogs.get(tagId) == null) {
                    tagCreationAuditLogs.put(tagId, getTagCreationAuditLog(tagId, accountAuditLogs));
                }
            }
        }
        return tagCreationAuditLogs.get(tagId);
    }

    public AuditLog getCustomFieldCreationAuditLog(final UUID customFieldId) throws AnalyticsRefreshException {
        if (customFieldCreationAuditLogs.get(customFieldId) == null) {
            synchronized (this) {
                if (customFieldCreationAuditLogs.get(customFieldId) == null) {
                    customFieldCreationAuditLogs.put(customFieldId, getFieldCreationAuditLog(customFieldId, accountAuditLogs));
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
