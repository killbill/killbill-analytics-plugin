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

package org.killbill.billing.plugin.analytics.dao.factory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.PlanPhase;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.entitlement.api.SubscriptionEvent;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillLogService;
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
    private BigDecimal accountBalance;
    // Relatively cheap lookups, should be done by account_record_id
    private Iterable<SubscriptionBundle> accountBundles;
    private Iterable<SubscriptionEvent> accountBlockingStates;
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

    public BusinessContextFactory(final UUID accountId,
                                  final CallContext callContext,
                                  final CurrencyConversionDao currencyConversionDao,
                                  final OSGIKillbillLogService logService,
                                  final OSGIKillbillAPI osgiKillbillAPI,
                                  final OSGIConfigPropertiesService osgiConfigPropertiesService,
                                  final Clock clock,
                                  final AnalyticsConfigurationHandler analyticsConfigurationHandler) throws AnalyticsRefreshException {
        super(currencyConversionDao, logService, osgiKillbillAPI, osgiConfigPropertiesService, clock);
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

    public synchronized PluginPropertiesManager getPluginPropertiesManager() {
        if (pluginPropertiesManager == null) {
            final AnalyticsConfiguration analyticsConfiguration = analyticsConfigurationHandler.getConfigurable(callContext.getTenantId());
            pluginPropertiesManager = new PluginPropertiesManager(analyticsConfiguration);
        }
        return pluginPropertiesManager;
    }

    @Override
    public synchronized CurrencyConverter getCurrencyConverter() {
        if (currencyConverter == null) {
            currencyConverter = super.getCurrencyConverter();
        }
        return currencyConverter;
    }

    public synchronized Account getAccount() throws AnalyticsRefreshException {
        if (account == null) {
            account = getAccount(accountId, callContext);
        }
        return account;
    }

    public synchronized BigDecimal getAccountBalance() throws AnalyticsRefreshException {
        if (accountBalance == null) {
            accountBalance = getAccountBalance(accountId, callContext);
        }
        return accountBalance;
    }

    public synchronized Iterable<SubscriptionBundle> getAccountBundles() throws AnalyticsRefreshException {
        if (accountBundles == null) {
            accountBundles = getSubscriptionBundlesForAccount(accountId, callContext);
        }
        return accountBundles;
    }

    public synchronized Iterable<SubscriptionEvent> getAccountBlockingStates() throws AnalyticsRefreshException {
        if (accountBlockingStates == null) {
            accountBlockingStates = getBlockingHistory(accountId, callContext);
        }
        return accountBlockingStates;
    }

    public synchronized Iterable<Invoice> getAccountInvoices() throws AnalyticsRefreshException {
        if (accountInvoices == null) {
            accountInvoices = getInvoicesByAccountId(accountId, callContext);
        }
        return accountInvoices;
    }

    public synchronized Map<UUID, List<InvoicePayment>> getAccountInvoicePayments() throws AnalyticsRefreshException {
        if (accountInvoicePayments == null) {
            accountInvoicePayments = getAccountInvoicePayments(getAccountPayments(), callContext);
        }
        return accountInvoicePayments;
    }

    public synchronized Iterable<Payment> getAccountPayments() throws AnalyticsRefreshException {
        if (accountPayments == null) {
            accountPayments = getPaymentsWithPluginInfoByAccountId(accountId, callContext);
        }
        return accountPayments;
    }

    public synchronized PaymentMethod getPaymentMethod(final UUID paymentMethodId) throws AnalyticsRefreshException {
        if (accountPaymentMethods == null) {
            accountPaymentMethods = new HashMap<UUID, PaymentMethod>();
            for (final PaymentMethod paymentMethod : getPaymentMethodsForAccount(accountId, callContext)) {
                accountPaymentMethods.put(paymentMethod.getId(), paymentMethod);
            }
        }
        return accountPaymentMethods.get(paymentMethodId);
    }

    public synchronized Iterable<Tag> getAccountTags() throws AnalyticsRefreshException {
        if (accountTags == null) {
            accountTags = getTagsForAccount(accountId, callContext);
        }
        return accountTags;
    }

    public synchronized Iterable<CustomField> getAccountCustomFields() throws AnalyticsRefreshException {
        if (accountCustomFields == null) {
            accountCustomFields = getFieldsForAccount(accountId, callContext);
        }
        return accountCustomFields;
    }

    public synchronized AuditLog getAccountCreationAuditLog() throws AnalyticsRefreshException {
        if (accountCreationAuditLog == null) {
            accountCreationAuditLog = getAccountCreationAuditLog(accountId, accountAuditLogs);
        }
        return accountCreationAuditLog;
    }

    public synchronized AuditLog getBundleCreationAuditLog(final UUID bundleId) throws AnalyticsRefreshException {
        if (bundleCreationAuditLogs.get(bundleId) == null) {
            bundleCreationAuditLogs.put(bundleId, getBundleCreationAuditLog(bundleId, accountAuditLogs));
        }
        return bundleCreationAuditLogs.get(bundleId);
    }

    public synchronized AuditLog getSubscriptionEventCreationAuditLog(final UUID subscriptionEventId, final ObjectType objectType) throws AnalyticsRefreshException {
        if (subscriptionEventCreationAuditLogs.get(subscriptionEventId) == null) {
            subscriptionEventCreationAuditLogs.put(subscriptionEventId, getSubscriptionEventCreationAuditLog(subscriptionEventId, objectType, accountAuditLogs));
        }
        return subscriptionEventCreationAuditLogs.get(subscriptionEventId);
    }

    public synchronized AuditLog getBlockingStateCreationAuditLog(final UUID blockingStateId) throws AnalyticsRefreshException {
        if (blockingStateCreationAuditLogs.get(blockingStateId) == null) {
            blockingStateCreationAuditLogs.put(blockingStateId, getBlockingStateCreationAuditLog(blockingStateId, accountAuditLogs));
        }
        return blockingStateCreationAuditLogs.get(blockingStateId);
    }

    public synchronized AuditLog getInvoiceCreationAuditLog(final UUID invoiceId) throws AnalyticsRefreshException {
        if (invoiceCreationAuditLogs.get(invoiceId) == null) {
            invoiceCreationAuditLogs.put(invoiceId, getInvoiceCreationAuditLog(invoiceId, accountAuditLogs));
        }
        return invoiceCreationAuditLogs.get(invoiceId);
    }

    public synchronized AuditLog getInvoiceItemCreationAuditLog(final UUID invoiceItemId) throws AnalyticsRefreshException {
        if (invoiceItemCreationAuditLogs.get(invoiceItemId) == null) {
            invoiceItemCreationAuditLogs.put(invoiceItemId, getInvoiceItemCreationAuditLog(invoiceItemId, accountAuditLogs));
        }
        return invoiceItemCreationAuditLogs.get(invoiceItemId);
    }

    public synchronized AuditLog getInvoicePaymentCreationAuditLog(final UUID invoicePaymentId) throws AnalyticsRefreshException {
        if (invoicePaymentCreationAuditLogs.get(invoicePaymentId) == null) {
            invoicePaymentCreationAuditLogs.put(invoicePaymentId, getInvoicePaymentCreationAuditLog(invoicePaymentId, accountAuditLogs));
        }
        return invoicePaymentCreationAuditLogs.get(invoicePaymentId);
    }

    public synchronized AuditLog getPaymentCreationAuditLog(final UUID paymentId) throws AnalyticsRefreshException {
        if (paymentCreationAuditLogs.get(paymentId) == null) {
            paymentCreationAuditLogs.put(paymentId, getPaymentCreationAuditLog(paymentId, accountAuditLogs));
        }
        return paymentCreationAuditLogs.get(paymentId);
    }

    public synchronized AuditLog getTagCreationAuditLog(final UUID tagId) throws AnalyticsRefreshException {
        if (tagCreationAuditLogs.get(tagId) == null) {
            tagCreationAuditLogs.put(tagId, getTagCreationAuditLog(tagId, accountAuditLogs));
        }
        return tagCreationAuditLogs.get(tagId);
    }

    public synchronized AuditLog getCustomFieldCreationAuditLog(final UUID customFieldId) throws AnalyticsRefreshException {
        if (customFieldCreationAuditLogs.get(customFieldId) == null) {
            customFieldCreationAuditLogs.put(customFieldId, getFieldCreationAuditLog(customFieldId, accountAuditLogs));
        }
        return customFieldCreationAuditLogs.get(customFieldId);
    }

    public synchronized Long getBundleRecordId(final UUID bundleId) throws AnalyticsRefreshException {
        if (bundleRecordIds.get(bundleId) == null) {
            bundleRecordIds.put(bundleId, getBundleRecordId(bundleId, callContext));
        }
        return bundleRecordIds.get(bundleId);
    }

    public synchronized Long getSubscriptionEventRecordId(final UUID subscriptionEventId, final ObjectType objectType) throws AnalyticsRefreshException {
        if (subscriptionEventRecordIds.get(subscriptionEventId) == null) {
            subscriptionEventRecordIds.put(subscriptionEventId, getSubscriptionEventRecordId(subscriptionEventId, objectType, callContext));
        }
        return subscriptionEventRecordIds.get(subscriptionEventId);
    }

    public synchronized Long getBlockingStateRecordId(final UUID blockingStateId) throws AnalyticsRefreshException {
        if (blockingStateRecordIds.get(blockingStateId) == null) {
            blockingStateRecordIds.put(blockingStateId, getBlockingStateRecordId(blockingStateId, callContext));
        }
        return blockingStateRecordIds.get(blockingStateId);
    }

    public synchronized Long getInvoiceRecordId(final UUID invoiceId) throws AnalyticsRefreshException {
        if (invoiceRecordIds.get(invoiceId) == null) {
            invoiceRecordIds.put(invoiceId, getInvoiceRecordId(invoiceId, callContext));
        }
        return invoiceRecordIds.get(invoiceId);
    }

    public synchronized Long getInvoiceItemRecordId(final UUID invoiceItemId) throws AnalyticsRefreshException {
        if (invoiceItemRecordIds.get(invoiceItemId) == null) {
            invoiceItemRecordIds.put(invoiceItemId, getInvoiceItemRecordId(invoiceItemId, callContext));
        }
        return invoiceItemRecordIds.get(invoiceItemId);
    }

    public synchronized Long getInvoicePaymentRecordId(final UUID invoicePaymentId) throws AnalyticsRefreshException {
        if (invoicePaymentRecordIds.get(invoicePaymentId) == null) {
            invoicePaymentRecordIds.put(invoicePaymentId, getInvoicePaymentRecordId(invoicePaymentId, callContext));
        }
        return invoicePaymentRecordIds.get(invoicePaymentId);
    }

    public synchronized Long getPaymentRecordId(final UUID paymentId) throws AnalyticsRefreshException {
        if (paymentRecordIds.get(paymentId) == null) {
            paymentRecordIds.put(paymentId, getPaymentRecordId(paymentId, callContext));
        }
        return paymentRecordIds.get(paymentId);
    }

    public synchronized Long getTagRecordId(final UUID tagId) throws AnalyticsRefreshException {
        if (tagRecordIds.get(tagId) == null) {
            tagRecordIds.put(tagId, getTagRecordId(tagId, callContext));
        }
        return tagRecordIds.get(tagId);
    }

    public synchronized Long getCustomFieldRecordId(final UUID customFieldId) throws AnalyticsRefreshException {
        if (customFieldRecordIds.get(customFieldId) == null) {
            customFieldRecordIds.put(customFieldId, getFieldRecordId(customFieldId, callContext));
        }
        return customFieldRecordIds.get(customFieldId);
    }

    public synchronized SubscriptionBundle getLatestSubscriptionBundleForExternalKey(final String externalKey) throws AnalyticsRefreshException {
        if (latestSubscriptionBundleForExternalKeys.get(externalKey) == null) {
            latestSubscriptionBundleForExternalKeys.put(externalKey, getLatestSubscriptionBundleForExternalKey(externalKey, callContext));
        }
        return latestSubscriptionBundleForExternalKeys.get(externalKey);
    }

    public synchronized TagDefinition getTagDefinition(final UUID tagDefinitionId) throws AnalyticsRefreshException {
        if (tagDefinitions.isEmpty()) {
            tagDefinitions = new HashMap<UUID, TagDefinition>();
            for (final TagDefinition tagDefinition : getTagDefinitions(callContext)) {
                tagDefinitions.put(tagDefinition.getId(), tagDefinition);
            }
        }
        return tagDefinitions.get(tagDefinitionId);
    }

    // Simple pass-through

    public Plan getPlanFromInvoiceItem(final InvoiceItem invoiceItem) throws AnalyticsRefreshException {
        return getPlanFromInvoiceItem(invoiceItem, callContext);
    }

    public PlanPhase getPlanPhaseFromInvoiceItem(final InvoiceItem invoiceItem, final LocalDate subscriptionStartDate) throws AnalyticsRefreshException {
        return getPlanPhaseFromInvoiceItem(invoiceItem, subscriptionStartDate, callContext);
    }
}
