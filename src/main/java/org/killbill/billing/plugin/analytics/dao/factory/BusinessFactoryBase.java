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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.killbill.billing.ErrorCode;
import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.account.api.AccountApiException;
import org.killbill.billing.account.api.AccountUserApi;
import org.killbill.billing.catalog.api.Catalog;
import org.killbill.billing.catalog.api.CatalogApiException;
import org.killbill.billing.catalog.api.CatalogUserApi;
import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.PlanPhase;
import org.killbill.billing.entitlement.api.SubscriptionApi;
import org.killbill.billing.entitlement.api.SubscriptionApiException;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.entitlement.api.SubscriptionEvent;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.invoice.api.InvoicePaymentApi;
import org.killbill.billing.invoice.api.InvoiceUserApi;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillLogService;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentApi;
import org.killbill.billing.payment.api.PaymentApiException;
import org.killbill.billing.payment.api.PaymentMethod;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.CurrencyConversionDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.api.AuditLevel;
import org.killbill.billing.util.api.AuditUserApi;
import org.killbill.billing.util.api.CustomFieldUserApi;
import org.killbill.billing.util.api.RecordIdApi;
import org.killbill.billing.util.api.TagUserApi;
import org.killbill.billing.util.audit.AccountAuditLogs;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.audit.ChangeType;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.customfield.CustomField;
import org.killbill.billing.util.tag.ControlTagType;
import org.killbill.billing.util.tag.Tag;
import org.killbill.billing.util.tag.TagDefinition;
import org.killbill.clock.Clock;
import org.osgi.service.log.LogService;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Wrapper around Kill Bill APIs
 * <p>
 * Note: the code is merciful in case audit logs cannot be retrieved. This is because the auditing code
 * is fairly recent, and we want this plugin to support early versions of Kill Bill (with non audited data).
 */
public abstract class BusinessFactoryBase {

    private static final String ANALYTICS_REFERENCE_CURRENCY_PROPERTY = "org.killbill.billing.plugin.analytics.referenceCurrency";
    private static final Iterable<PluginProperty> PLUGIN_PROPERTIES = ImmutableList.<PluginProperty>of();

    protected final OSGIKillbillLogService logService;
    protected final OSGIKillbillAPI osgiKillbillAPI;
    protected final Clock clock;

    private final String referenceCurrency;
    private final CurrencyConversionDao currencyConversionDao;

    public BusinessFactoryBase(final CurrencyConversionDao currencyConversionDao,
                               final OSGIKillbillLogService logService,
                               final OSGIKillbillAPI osgiKillbillAPI,
                               final OSGIConfigPropertiesService osgiConfigPropertiesService,
                               final Clock clock) {
        this.logService = logService;
        this.osgiKillbillAPI = osgiKillbillAPI;
        this.clock = clock;
        this.referenceCurrency = Objects.firstNonNull(Strings.emptyToNull(osgiConfigPropertiesService.getString(ANALYTICS_REFERENCE_CURRENCY_PROPERTY)), "USD");
        this.currencyConversionDao = currencyConversionDao;
    }

    //
    // UTILS
    //

    protected CurrencyConverter getCurrencyConverter() {
        return new CurrencyConverter(clock, referenceCurrency, currencyConversionDao.getCurrencyConversions(referenceCurrency));
    }

    //
    // TENANT
    //

    private static final long INTERNAL_TENANT_RECORD_ID = 0L;

    protected Long getTenantRecordId(final TenantContext context) throws AnalyticsRefreshException {
        // See convention in InternalCallContextFactory
        if (context.getTenantId() == null) {
            return INTERNAL_TENANT_RECORD_ID;
        } else {
            final RecordIdApi recordIdUserApi = getRecordIdUserApi();
            return recordIdUserApi.getRecordId(context.getTenantId(), ObjectType.TENANT, context);
        }
    }

    //
    // ACCOUNT
    //

    protected Account getAccount(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        final AccountUserApi accountUserApi = getAccountUserApi();

        try {
            return accountUserApi.getAccountById(accountId, context);
        } catch (AccountApiException e) {
            logService.log(LogService.LOG_WARNING, "Error retrieving account for id " + accountId, e);
            throw new AnalyticsRefreshException(e);
        }
    }

    protected AccountAuditLogs getAccountAuditLogs(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        final AuditUserApi auditUserApi = getAuditUserApi();
        return auditUserApi.getAccountAuditLogs(accountId, AuditLevel.MINIMAL, context);
    }

    protected AuditLog getAccountCreationAuditLog(final UUID accountId, final AccountAuditLogs accountAuditLogs) throws AnalyticsRefreshException {
        final List<AuditLog> auditLogsForAccount = accountAuditLogs.getAuditLogsForAccount();
        for (final AuditLog auditLog : auditLogsForAccount) {
            if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                return auditLog;
            }
        }

        logService.log(LogService.LOG_WARNING, "Unable to find Account creation audit log for id " + accountId);
        return null;
    }

    protected Long getAccountRecordId(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        final Long accountRecordIdOrNull = recordIdUserApi.getRecordId(accountId, ObjectType.ACCOUNT, context);
        // Never return null, to make sure indexes can be used (see https://github.com/killbill/killbill-analytics-plugin/issues/59)
        return accountRecordIdOrNull == null ? -1L : accountRecordIdOrNull;
    }

    protected ReportGroup getReportGroup(final Iterable<Tag> accountTags) throws AnalyticsRefreshException {
        boolean isTestAccount = false;
        boolean isPartnerAccount = false;

        for (final Tag tag : accountTags) {
            if (!ObjectType.ACCOUNT.equals(tag.getObjectType())) {
                continue;
            }

            if (ControlTagType.TEST.getId().equals(tag.getTagDefinitionId())) {
                isTestAccount = true;
            } else if (ControlTagType.PARTNER.getId().equals(tag.getTagDefinitionId())) {
                isPartnerAccount = true;
            }
        }

        // Test group has precedence
        if (isTestAccount) {
            return ReportGroup.test;
        } else if (isPartnerAccount) {
            return ReportGroup.partner;
        } else {
            return null;
        }
    }

    //
    // SUBSCRIPTION
    //

    protected List<SubscriptionBundle> getSubscriptionBundlesForAccount(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        final SubscriptionApi subscriptionApi = getSubscriptionApi();

        try {
            return subscriptionApi.getSubscriptionBundlesForAccountId(accountId, context);
        } catch (SubscriptionApiException e) {
            logService.log(LogService.LOG_WARNING, "Error retrieving bundles for account id " + accountId, e);
            throw new AnalyticsRefreshException(e);
        }
    }

    protected SubscriptionBundle getLatestSubscriptionBundleForExternalKey(final String bundleExternalKey, final TenantContext context) throws AnalyticsRefreshException {
        final SubscriptionApi subscriptionApi = getSubscriptionApi();

        try {
            final List<SubscriptionBundle> bundles = subscriptionApi.getSubscriptionBundlesForExternalKey(bundleExternalKey, context);
            if (bundles.size() == 0) {
                throw new AnalyticsRefreshException("Unable to retrieve latest bundle for bundle external key " + bundleExternalKey);
            }
            return bundles.get(bundles.size() - 1);
        } catch (SubscriptionApiException e) {
            logService.log(LogService.LOG_WARNING, "Error retrieving bundles for bundle external key " + bundleExternalKey, e);
            throw new AnalyticsRefreshException(e);
        }
    }

    protected Long getBundleRecordId(final UUID bundleId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(bundleId, ObjectType.BUNDLE, context);
    }

    protected AuditLog getBundleCreationAuditLog(final UUID bundleId, final AccountAuditLogs accountAuditLogs) throws AnalyticsRefreshException {
        final List<AuditLog> auditLogsForBundle = accountAuditLogs.getAuditLogsForBundle(bundleId);
        for (final AuditLog auditLog : auditLogsForBundle) {
            if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                return auditLog;
            }
        }

        logService.log(LogService.LOG_WARNING, "Unable to find Bundle creation audit log for id " + bundleId);
        return null;
    }

    protected AuditLog getSubscriptionEventCreationAuditLog(final UUID subscriptionEventId, final ObjectType objectType, final AccountAuditLogs accountAuditLogs) throws AnalyticsRefreshException {
        final List<AuditLog> auditLogsForSubscriptionEvent = accountAuditLogs.getAuditLogs(objectType).getAuditLogs(subscriptionEventId);
        for (final AuditLog auditLog : auditLogsForSubscriptionEvent) {
            if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                return auditLog;
            }
        }

        logService.log(LogService.LOG_WARNING, "Unable to find Subscription event creation audit log for id " + subscriptionEventId);
        return null;
    }

    protected Long getSubscriptionEventRecordId(final UUID subscriptionEventId, final ObjectType objectType, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(subscriptionEventId, objectType, context);
    }

    //
    // OVERDUE
    //

    protected Iterable<SubscriptionEvent> getBlockingHistory(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        final List<SubscriptionBundle> bundles = getSubscriptionBundlesForAccount(accountId, context);

        // Find all subscription events for that account
        final Iterable<SubscriptionEvent> subscriptionEvents = Iterables.<SubscriptionEvent>concat(Iterables.<SubscriptionBundle, List<SubscriptionEvent>>transform(bundles,
                                                                                                                                                                    new Function<SubscriptionBundle, List<SubscriptionEvent>>() {
                                                                                                                                                                        @Override
                                                                                                                                                                        public List<SubscriptionEvent> apply(final SubscriptionBundle bundle) {
                                                                                                                                                                            return bundle.getTimeline().getSubscriptionEvents();
                                                                                                                                                                        }
                                                                                                                                                                    }
                                                                                                                                                                   ));

        // Filter all service state changes
        return Iterables.<SubscriptionEvent>filter(subscriptionEvents,
                                                   new Predicate<SubscriptionEvent>() {
                                                       @Override
                                                       public boolean apply(final SubscriptionEvent event) {
                                                           return event.getSubscriptionEventType() != null &&
                                                                  // We want events coming from the blocking states table...
                                                                  ObjectType.BLOCKING_STATES.equals(event.getSubscriptionEventType().getObjectType()) &&
                                                                  // ...that are for any service but entitlement
                                                                  !BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME.equals(event.getServiceName());
                                                       }
                                                   }
                                                  );
    }

    //
    // BLOCKING STATES
    //

    protected AuditLog getBlockingStateCreationAuditLog(final UUID blockingStateId, final AccountAuditLogs accountAuditLogs) throws AnalyticsRefreshException {
        final List<AuditLog> auditLogsForBlockingState = accountAuditLogs.getAuditLogsForBlockingState(blockingStateId);
        for (final AuditLog auditLog : auditLogsForBlockingState) {
            if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                return auditLog;
            }
        }

        logService.log(LogService.LOG_WARNING, "Unable to find Blocking state creation audit log for id " + blockingStateId);
        return null;
    }

    protected Long getBlockingStateRecordId(final UUID blockingStateId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(blockingStateId, ObjectType.BLOCKING_STATES, context);
    }

    //
    // INVOICE
    //

    protected AuditLog getInvoiceCreationAuditLog(final UUID invoiceId, final AccountAuditLogs accountAuditLogs) throws AnalyticsRefreshException {
        final List<AuditLog> auditLogsForInvoice = accountAuditLogs.getAuditLogsForInvoice(invoiceId);
        for (final AuditLog auditLog : auditLogsForInvoice) {
            if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                return auditLog;
            }
        }

        logService.log(LogService.LOG_WARNING, "Unable to find Invoice creation audit log for id " + invoiceId);
        return null;
    }

    protected Long getInvoiceRecordId(final UUID invoiceId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(invoiceId, ObjectType.INVOICE, context);
    }

    protected AuditLog getInvoiceItemCreationAuditLog(final UUID invoiceItemId, final AccountAuditLogs accountAuditLogs) throws AnalyticsRefreshException {
        final List<AuditLog> auditLogsForInvoiceItem = accountAuditLogs.getAuditLogsForInvoiceItem(invoiceItemId);
        for (final AuditLog auditLog : auditLogsForInvoiceItem) {
            if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                return auditLog;
            }
        }

        logService.log(LogService.LOG_WARNING, "Unable to find Invoice item creation audit log for id " + invoiceItemId);
        return null;
    }

    protected Long getInvoiceItemRecordId(final UUID invoiceItemId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(invoiceItemId, ObjectType.INVOICE_ITEM, context);
    }

    protected Collection<Invoice> getInvoicesByAccountId(final UUID accountId, final CallContext context) throws AnalyticsRefreshException {
        final InvoiceUserApi invoiceUserApi = getInvoiceUserApi();
        return invoiceUserApi.getInvoicesByAccount(accountId, false, context);
    }

    protected BigDecimal getAccountBalance(final UUID accountId, final CallContext context) throws AnalyticsRefreshException {
        final InvoiceUserApi invoiceUserApi = getInvoiceUserApi();
        return invoiceUserApi.getAccountBalance(accountId, context);
    }

    protected Plan getPlanFromInvoiceItem(final InvoiceItem invoiceItem, final TenantContext context) throws AnalyticsRefreshException {
        try {
            final Catalog catalog = getCatalog(context);
            return catalog.findPlan(invoiceItem.getPlanName(), invoiceItem.getStartDate().toDateTimeAtStartOfDay());
        } catch (CatalogApiException e) {
            logService.log(LogService.LOG_INFO, "Unable to retrieve plan for invoice item " + invoiceItem.getId(), e);
            return null;
        }
    }

    protected PlanPhase getPlanPhaseFromInvoiceItem(final InvoiceItem invoiceItem, final LocalDate subscriptionStartDate, final TenantContext context) throws AnalyticsRefreshException {
        try {
            final Catalog catalog = getCatalog(context);
            // TODO - Inaccurate timing
            return catalog.findPhase(invoiceItem.getPhaseName(), invoiceItem.getStartDate().toDateTimeAtStartOfDay(), subscriptionStartDate.toDateTimeAtStartOfDay());
        } catch (CatalogApiException e) {
            logService.log(LogService.LOG_INFO, "Unable to retrieve phase for invoice item " + invoiceItem.getId(), e);
            return null;
        }
    }

    //
    // CATALOG
    //

    protected Catalog getCatalog(final TenantContext context) throws AnalyticsRefreshException {
        final CatalogUserApi catalogUserApi = getCatalogUserApi();
        try {
            return catalogUserApi.getCatalog(null, context);
        } catch (CatalogApiException e) {
           throw new AnalyticsRefreshException(e);
        }
    }

    //
    // INVOICE PAYMENT
    //

    protected Map<UUID, List<InvoicePayment>> getAccountInvoicePayments(final Iterable<Payment> payments, final TenantContext context) throws AnalyticsRefreshException {
        final InvoicePaymentApi invoicePaymentApi = getInvoicePaymentUserApi();

        final Map<UUID, List<InvoicePayment>> allInvoicePaymentsByPaymentId = new HashMap<UUID, List<InvoicePayment>>();
        for (final Payment payment : payments) {
            // Retrieve all invoice payment types (including refunds and chargebacks) for that payment
            allInvoicePaymentsByPaymentId.put(payment.getId(), invoicePaymentApi.getInvoicePayments(payment.getId(), context));
        }

        return allInvoicePaymentsByPaymentId;
    }

    protected AuditLog getInvoicePaymentCreationAuditLog(final UUID invoicePaymentId, final AccountAuditLogs accountAuditLogs) throws AnalyticsRefreshException {
        final List<AuditLog> auditLogsForInvoicePayment = accountAuditLogs.getAuditLogsForInvoicePayment(invoicePaymentId);
        for (final AuditLog auditLog : auditLogsForInvoicePayment) {
            if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                return auditLog;
            }
        }

        logService.log(LogService.LOG_WARNING, "Unable to find Invoice payment creation audit log for id " + invoicePaymentId);
        return null;
    }

    protected Long getInvoicePaymentRecordId(final UUID invoicePaymentId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(invoicePaymentId, ObjectType.INVOICE_PAYMENT, context);
    }

    //
    // PAYMENT
    //

    protected Collection<Payment> getPaymentsWithPluginInfoByAccountId(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        PaymentApiException error;

        final PaymentApi paymentApi = getPaymentUserApi();
        try {
            return paymentApi.getAccountPayments(accountId, true, false, PLUGIN_PROPERTIES, context);
        } catch (PaymentApiException e) {
            error = e;
            if (e.getCode() == ErrorCode.PAYMENT_NO_SUCH_PAYMENT_PLUGIN.getCode()) {
                logService.log(LogService.LOG_WARNING, e.getMessage() + ". Analytics tables will be missing plugin specific information");

                try {
                    return paymentApi.getAccountPayments(accountId, false, false, PLUGIN_PROPERTIES, context);
                } catch (PaymentApiException e1) {
                    error = e1;
                }
            }
        }

        logService.log(LogService.LOG_WARNING, "Error retrieving payments for account id " + accountId, error);
        throw new AnalyticsRefreshException(error);
    }

    protected List<PaymentMethod> getPaymentMethodsForAccount(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        PaymentApiException error;

        final PaymentApi paymentApi = getPaymentUserApi();
        try {
            // Try to get all payment methods, with plugin information
            // TODO this will not return deleted payment methods
            return paymentApi.getAccountPaymentMethods(accountId, true, PLUGIN_PROPERTIES, context);
        } catch (PaymentApiException e) {
            error = e;
            if (e.getCode() == ErrorCode.PAYMENT_NO_SUCH_PAYMENT_PLUGIN.getCode()) {
                logService.log(LogService.LOG_WARNING, e.getMessage() + ". Analytics tables will be missing plugin specific information");

                try {
                    return paymentApi.getAccountPaymentMethods(accountId, false, PLUGIN_PROPERTIES, context);
                } catch (PaymentApiException e1) {
                    error = e1;
                }
            }
        }

        logService.log(LogService.LOG_WARNING, "Error retrieving payment methods for account id " + accountId, error);
        throw new AnalyticsRefreshException(error);
    }

    protected AuditLog getPaymentCreationAuditLog(final UUID paymentId, final AccountAuditLogs accountAuditLogs) throws AnalyticsRefreshException {
        final List<AuditLog> auditLogsForPayment = accountAuditLogs.getAuditLogsForPayment(paymentId);
        for (final AuditLog auditLog : auditLogsForPayment) {
            if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                return auditLog;
            }
        }

        logService.log(LogService.LOG_WARNING, "Unable to find payment creation audit log for id " + paymentId);
        return null;
    }

    protected Long getPaymentRecordId(final UUID paymentId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(paymentId, ObjectType.PAYMENT, context);
    }

    //
    // FIELD
    //

    protected Collection<CustomField> getFieldsForAccount(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        final CustomFieldUserApi tagUserApi = getCustomFieldUserApi();
        return tagUserApi.getCustomFieldsForAccount(accountId, context);
    }

    protected AuditLog getFieldCreationAuditLog(final UUID fieldId, final AccountAuditLogs accountAuditLogs) throws AnalyticsRefreshException {
        final List<AuditLog> auditLogsForTag = accountAuditLogs.getAuditLogsForCustomField(fieldId);
        for (final AuditLog auditLog : auditLogsForTag) {
            if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                return auditLog;
            }
        }

        logService.log(LogService.LOG_WARNING, "Unable to find Field creation audit log for id " + fieldId);
        return null;
    }

    protected Long getFieldRecordId(final UUID fieldId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(fieldId, ObjectType.CUSTOM_FIELD, context);
    }

    //
    // TAG
    //

    protected Collection<Tag> getTagsForAccount(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        final TagUserApi tagUserApi = getTagUserApi();
        return tagUserApi.getTagsForAccount(accountId, false, context);
    }

    protected List<TagDefinition> getTagDefinitions(final TenantContext context) throws AnalyticsRefreshException {
        final TagUserApi tagUserApi = getTagUserApi();
        return tagUserApi.getTagDefinitions(context);
    }

    protected AuditLog getTagCreationAuditLog(final UUID tagId, final AccountAuditLogs accountAuditLogs) throws AnalyticsRefreshException {
        final List<AuditLog> auditLogsForTag = accountAuditLogs.getAuditLogsForTag(tagId);
        for (final AuditLog auditLog : auditLogsForTag) {
            if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                return auditLog;
            }
        }

        logService.log(LogService.LOG_WARNING, "Unable to find Tag creation audit log for id " + tagId);
        return null;
    }

    protected Long getTagRecordId(final UUID tagId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(tagId, ObjectType.TAG, context);
    }

    //
    // APIs
    //

    private AccountUserApi getAccountUserApi() throws AnalyticsRefreshException {
        final AccountUserApi accountUserApi = osgiKillbillAPI.getAccountUserApi();
        if (accountUserApi == null) {
            throw new AnalyticsRefreshException("Error retrieving accountUserApi");
        }
        return accountUserApi;
    }

    private SubscriptionApi getSubscriptionApi() throws AnalyticsRefreshException {
        final SubscriptionApi subscriptionApi = osgiKillbillAPI.getSubscriptionApi();
        if (subscriptionApi == null) {
            throw new AnalyticsRefreshException("Error retrieving subscriptionApi");
        }
        return subscriptionApi;
    }

    private InvoiceUserApi getInvoiceUserApi() throws AnalyticsRefreshException {
        final InvoiceUserApi invoiceUserApi = osgiKillbillAPI.getInvoiceUserApi();
        if (invoiceUserApi == null) {
            throw new AnalyticsRefreshException("Error retrieving invoiceUserApi");
        }
        return invoiceUserApi;
    }

    private CatalogUserApi getCatalogUserApi() throws AnalyticsRefreshException {
        final CatalogUserApi catalogUserApi = osgiKillbillAPI.getCatalogUserApi();
        if (catalogUserApi == null) {
            throw new AnalyticsRefreshException("Error retrieving catalogUserApi");
        }
        return catalogUserApi;
    }

    private PaymentApi getPaymentUserApi() throws AnalyticsRefreshException {
        final PaymentApi paymentApi = osgiKillbillAPI.getPaymentApi();
        if (paymentApi == null) {
            throw new AnalyticsRefreshException("Error retrieving paymentApi");
        }
        return paymentApi;
    }

    private InvoicePaymentApi getInvoicePaymentUserApi() throws AnalyticsRefreshException {
        final InvoicePaymentApi invoicePaymentApi = osgiKillbillAPI.getInvoicePaymentApi();
        if (invoicePaymentApi == null) {
            throw new AnalyticsRefreshException("Error retrieving invoicePaymentApi");
        }
        return invoicePaymentApi;
    }

    private CustomFieldUserApi getCustomFieldUserApi() throws AnalyticsRefreshException {
        final CustomFieldUserApi fieldUserApi = osgiKillbillAPI.getCustomFieldUserApi();
        if (fieldUserApi == null) {
            throw new AnalyticsRefreshException("Error retrieving fieldUserApi");
        }
        return fieldUserApi;
    }

    private TagUserApi getTagUserApi() throws AnalyticsRefreshException {
        final TagUserApi tagUserApi = osgiKillbillAPI.getTagUserApi();
        if (tagUserApi == null) {
            throw new AnalyticsRefreshException("Error retrieving tagUserApi");
        }
        return tagUserApi;
    }

    private RecordIdApi getRecordIdUserApi() throws AnalyticsRefreshException {
        final RecordIdApi recordIdApi = osgiKillbillAPI.getRecordIdApi();
        if (recordIdApi == null) {
            throw new AnalyticsRefreshException("Error retrieving recordIdApi");
        }
        return recordIdApi;
    }

    private AuditUserApi getAuditUserApi() throws AnalyticsRefreshException {
        final AuditUserApi auditUserApi = osgiKillbillAPI.getAuditUserApi();
        if (auditUserApi == null) {
            throw new AnalyticsRefreshException("Error retrieving auditUserApi");
        }
        return auditUserApi;
    }
}
