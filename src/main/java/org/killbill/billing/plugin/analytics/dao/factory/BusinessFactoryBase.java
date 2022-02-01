/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.killbill.billing.ErrorCode;
import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.account.api.AccountApiException;
import org.killbill.billing.account.api.AccountUserApi;
import org.killbill.billing.catalog.api.CatalogApiException;
import org.killbill.billing.catalog.api.CatalogUserApi;
import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.PlanPhase;
import org.killbill.billing.catalog.api.VersionedCatalog;
import org.killbill.billing.entitlement.api.Subscription;
import org.killbill.billing.entitlement.api.SubscriptionApi;
import org.killbill.billing.entitlement.api.SubscriptionApiException;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceApiException;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.invoice.api.InvoiceUserApi;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.api.InvoicePaymentApi;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentApi;
import org.killbill.billing.payment.api.PaymentApiException;
import org.killbill.billing.payment.api.PaymentMethod;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.CurrencyConversionDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * Wrapper around Kill Bill APIs
 * <p>
 * Note: the code is merciful in case audit logs cannot be retrieved. This is because the auditing code
 * is fairly recent, and we want this plugin to support early versions of Kill Bill (with non audited data).
 */
public abstract class BusinessFactoryBase {

    private static final String ANALYTICS_REFERENCE_CURRENCY_PROPERTY = "org.killbill.billing.plugin.analytics.referenceCurrency";
    private static final Iterable<PluginProperty> PLUGIN_PROPERTIES = ImmutableList.<PluginProperty>of();
    private static final Logger logger = LoggerFactory.getLogger(BusinessFactoryBase.class);

    protected final OSGIKillbillAPI osgiKillbillAPI;
    protected final Clock clock;

    private final String referenceCurrency;
    private final CurrencyConversionDao currencyConversionDao;

    public BusinessFactoryBase(final CurrencyConversionDao currencyConversionDao,
                               final OSGIKillbillAPI osgiKillbillAPI,
                               final OSGIConfigPropertiesService osgiConfigPropertiesService,
                               final Clock clock) {
        this.osgiKillbillAPI = osgiKillbillAPI;
        this.clock = clock;
        this.referenceCurrency = MoreObjects.firstNonNull(Strings.emptyToNull(osgiConfigPropertiesService.getString(ANALYTICS_REFERENCE_CURRENCY_PROPERTY)), "USD");
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
        } catch (final AccountApiException e) {
            logger.warn("Error retrieving account for id {}", accountId, e);
            throw new AnalyticsRefreshException(e);
        }
    }

    protected AuditLog getAccountCreationAuditLog(final UUID accountId, final SafeAccountAuditLogs safeAccountAuditLogs) throws AnalyticsRefreshException {


        return new AuditLogWithRetry(safeAccountAuditLogs).withRetry(new AuditLogHandler() {
            @Override
            public AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs) {
                final List<AuditLog> auditLogsForAccount = accountAuditLogs.getAuditLogsForAccount();
                for (final AuditLog auditLog : auditLogsForAccount) {
                    if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                        return auditLog;
                    }
                }
                return null;
            }
        },"Unable to find Account creation audit log for id {}", accountId);
    }

    protected Long getAccountRecordId(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        final Long accountRecordIdOrNull = recordIdUserApi.getRecordId(accountId, ObjectType.ACCOUNT, context);
        // Never return null, to make sure indexes can be used (see https://github.com/killbill/killbill-analytics-plugin/issues/59)
        return accountRecordIdOrNull == null ? Long.valueOf(-1L) : accountRecordIdOrNull;
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
        } catch (final SubscriptionApiException e) {
            logger.warn("Error retrieving bundles for account id {}", accountId, e);
            throw new AnalyticsRefreshException(e);
        }
    }

    protected SubscriptionBundle getSubscriptionBundle(final UUID bundleId, final TenantContext context) throws AnalyticsRefreshException {
        final SubscriptionApi subscriptionApi = getSubscriptionApi();

        try {
            return subscriptionApi.getSubscriptionBundle(bundleId, context);
        } catch (final SubscriptionApiException e) {
            logger.warn("Error retrieving bundle for id {}", bundleId, e);
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
        } catch (final SubscriptionApiException e) {
            logger.warn("Error retrieving bundles for bundle external key {}", bundleExternalKey, e);
            throw new AnalyticsRefreshException(e);
        }
    }

    protected Subscription getSubscription(final UUID subscriptionId, final TenantContext context) throws AnalyticsRefreshException {
        final SubscriptionApi subscriptionApi = getSubscriptionApi();

        try {
            return subscriptionApi.getSubscriptionForEntitlementId(subscriptionId, context);
        } catch (final SubscriptionApiException e) {
            logger.warn("Error retrieving subscription for id {}", subscriptionId, e);
            throw new AnalyticsRefreshException(e);
        }
    }

    protected Long getBundleRecordId(final UUID bundleId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(bundleId, ObjectType.BUNDLE, context);
    }

    protected AuditLog getBundleCreationAuditLog(final UUID bundleId, final SafeAccountAuditLogs safeAccountAuditLogs) throws AnalyticsRefreshException {

        return new AuditLogWithRetry(safeAccountAuditLogs).withRetry(new AuditLogHandler() {
            @Override
            public AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs) {
                final List<AuditLog> auditLogsForBundle = accountAuditLogs.getAuditLogsForBundle(bundleId);
                for (final AuditLog auditLog : auditLogsForBundle) {
                    if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                        return auditLog;
                    }
                }
                return null;
            }
        }, "Unable to find Bundle creation audit log for id {}", bundleId);

    }

    protected AuditLog getSubscriptionEventCreationAuditLog(final UUID subscriptionEventId, final ObjectType objectType, final SafeAccountAuditLogs safeAccountAuditLogs) throws AnalyticsRefreshException {
        return new AuditLogWithRetry(safeAccountAuditLogs).withRetry(new AuditLogHandler() {
            @Override
            public AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs) {
                final List<AuditLog> auditLogsForSubscriptionEvent = accountAuditLogs.getAuditLogs(objectType).getAuditLogs(subscriptionEventId);
                for (final AuditLog auditLog : auditLogsForSubscriptionEvent) {
                    if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                        return auditLog;
                    }
                }
                return null;
            }
        }, "Unable to find Subscription event creation audit log for id {}", subscriptionEventId);
    }

    protected Long getSubscriptionEventRecordId(final UUID subscriptionEventId, final ObjectType objectType, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(subscriptionEventId, objectType, context);
    }

    //
    // BLOCKING STATES
    //

    protected AuditLog getBlockingStateCreationAuditLog(final UUID blockingStateId, final SafeAccountAuditLogs safeAccountAuditLogs) throws AnalyticsRefreshException {

        return new AuditLogWithRetry(safeAccountAuditLogs).withRetry(new AuditLogHandler() {
            @Override
            public AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs) {
                final List<AuditLog> auditLogsForBlockingState = accountAuditLogs.getAuditLogsForBlockingState(blockingStateId);
                for (final AuditLog auditLog : auditLogsForBlockingState) {
                    if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                        return auditLog;
                    }
                }
                return null;
            }
        }, "Unable to find Blocking state creation audit log for id {}", blockingStateId);
    }

    protected Long getBlockingStateRecordId(final UUID blockingStateId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(blockingStateId, ObjectType.BLOCKING_STATES, context);
    }

    //
    // INVOICE
    //

    protected AuditLog getInvoiceCreationAuditLog(final UUID invoiceId, final SafeAccountAuditLogs safeAccountAuditLogs) throws AnalyticsRefreshException {

        return new AuditLogWithRetry(safeAccountAuditLogs).withRetry(new AuditLogHandler() {
            @Override
            public AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs) {
                final List<AuditLog> auditLogsForInvoice = accountAuditLogs.getAuditLogsForInvoice(invoiceId);
                for (final AuditLog auditLog : auditLogsForInvoice) {
                    if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                        return auditLog;
                    }
                }
                return null;
            }
        }, "Unable to find Invoice creation audit log for id {}", invoiceId);
    }

    protected Long getInvoiceRecordId(final UUID invoiceId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(invoiceId, ObjectType.INVOICE, context);
    }

    protected AuditLog getInvoiceItemCreationAuditLog(final UUID invoiceItemId, final SafeAccountAuditLogs safeAccountAuditLogs) throws AnalyticsRefreshException {

        return new AuditLogWithRetry(safeAccountAuditLogs).withRetry(new AuditLogHandler() {
            @Override
            public AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs) {
                final List<AuditLog> auditLogsForInvoiceItem = accountAuditLogs.getAuditLogsForInvoiceItem(invoiceItemId);
                for (final AuditLog auditLog : auditLogsForInvoiceItem) {
                    if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                        return auditLog;
                    }
                }
                return null;
            }
        }, "Unable to find Invoice item creation audit log for id {}", invoiceItemId);
    }

    protected Long getInvoiceItemRecordId(final UUID invoiceItemId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(invoiceItemId, ObjectType.INVOICE_ITEM, context);
    }

    protected Invoice getInvoice(final UUID invoiceId, final TenantContext context) throws AnalyticsRefreshException {
        final InvoiceUserApi invoiceUserApi = getInvoiceUserApi();
        try {
            return invoiceUserApi.getInvoice(invoiceId, context);
        } catch (final InvoiceApiException e) {
            logger.warn("Unable to retrieve invoice for {}", invoiceId, e);
            return null;
        }
    }

    protected Invoice getInvoiceByInvoiceItemId(final UUID invoiceItemId, final TenantContext context) throws AnalyticsRefreshException {
        final InvoiceUserApi invoiceUserApi = getInvoiceUserApi();
        try {
            return invoiceUserApi.getInvoiceByInvoiceItem(invoiceItemId, context);
        } catch (final InvoiceApiException e) {
            logger.warn("Unable to retrieve invoice for invoice item {}", invoiceItemId, e);
            return null;
        }
    }

    protected Collection<Invoice> getInvoicesByAccountId(final UUID accountId, final CallContext context) throws AnalyticsRefreshException {
        final InvoiceUserApi invoiceUserApi = getInvoiceUserApi();
        return invoiceUserApi.getInvoicesByAccount(accountId, false, false, context);
    }

    protected BigDecimal getAccountBalance(final UUID accountId, final CallContext context) throws AnalyticsRefreshException {
        final InvoiceUserApi invoiceUserApi = getInvoiceUserApi();
        return invoiceUserApi.getAccountBalance(accountId, context);
    }

    protected Plan getPlanFromInvoiceItem(final InvoiceItem invoiceItem, final VersionedCatalog catalog) throws AnalyticsRefreshException {
        try {
            // getCatalogEffectiveDate was introduced in 0.21.x
            final DateTime catalogEffectiveDate = MoreObjects.firstNonNull(invoiceItem.getCatalogEffectiveDate(), invoiceItem.getCreatedDate());
            return catalog.getVersion(catalogEffectiveDate.toDate()).findPlan(invoiceItem.getPlanName());
        } catch (final CatalogApiException e) {
            logger.warn("Unable to retrieve plan for invoice item {}", invoiceItem.getId(), e);
            return null;
        }
    }

    protected PlanPhase getPlanPhaseFromInvoiceItem(final InvoiceItem invoiceItem, final VersionedCatalog catalog) throws AnalyticsRefreshException {
        // Find the phase via the plan (same implementation logic as Catalog.findPhase, but without having to pass the subscription start date)
        final Plan plan = getPlanFromInvoiceItem(invoiceItem, catalog);
        if (plan == null) {
            return null;
        }

        try {
            return plan.findPhase(invoiceItem.getPhaseName());
        } catch (final CatalogApiException e) {
            logger.warn("Unable to retrieve phase for invoice item {}", invoiceItem.getId(), e);
            return null;
        }
    }

    //
    // CATALOG
    //

    protected VersionedCatalog getCatalog(final TenantContext context) throws AnalyticsRefreshException {
        final CatalogUserApi catalogUserApi = getCatalogUserApi();
        try {
            return catalogUserApi.getCatalog(null, context);
        } catch (final CatalogApiException e) {
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

    protected AuditLog getInvoicePaymentCreationAuditLog(final UUID invoicePaymentId, final SafeAccountAuditLogs safeAccountAuditLogs) throws AnalyticsRefreshException {

        return new AuditLogWithRetry(safeAccountAuditLogs).withRetry(new AuditLogHandler() {
            @Override
            public AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs) {
                final List<AuditLog> auditLogsForInvoicePayment = accountAuditLogs.getAuditLogsForInvoicePayment(invoicePaymentId);
                for (final AuditLog auditLog : auditLogsForInvoicePayment) {
                    if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                        return auditLog;
                    }
                }
                return null;
            }
        }, "Unable to find Invoice payment creation audit log for id {}", invoicePaymentId);
    }

    protected Long getInvoicePaymentRecordId(final UUID invoicePaymentId, final TenantContext context) throws AnalyticsRefreshException {
        final RecordIdApi recordIdUserApi = getRecordIdUserApi();
        return recordIdUserApi.getRecordId(invoicePaymentId, ObjectType.INVOICE_PAYMENT, context);
    }

    //
    // PAYMENT
    //

    protected Collection<Payment> getPaymentsWithPluginInfoByAccountId(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        Exception error;

        final PaymentApi paymentApi = getPaymentUserApi();
        try {
            return paymentApi.getAccountPayments(accountId, true, false, PLUGIN_PROPERTIES, context);
        } catch (final PaymentApiException e) {
            error = e;
            if (e.getCode() == ErrorCode.PAYMENT_NO_SUCH_PAYMENT_PLUGIN.getCode()) {
                logger.warn(e.getMessage() + ". Analytics tables will be missing plugin specific information");

                try {
                    return paymentApi.getAccountPayments(accountId, false, false, PLUGIN_PROPERTIES, context);
                } catch (final PaymentApiException e1) {
                    error = e1;
                }
            }
        } catch (final RuntimeException e) {
            // Plugin exception?
            error = e;
            try {
                final List<Payment> accountPayments = paymentApi.getAccountPayments(accountId, false, false, PLUGIN_PROPERTIES, context);
                logger.warn(e.getMessage() + ". Analytics tables will be missing plugin specific information");
                return accountPayments;
            } catch (final PaymentApiException ignored) {
            }
        }

        logger.warn("Error retrieving payments for account id {}", accountId, error);
        throw new AnalyticsRefreshException(error);
    }

    protected List<PaymentMethod> getPaymentMethodsForAccount(final UUID accountId, final TenantContext context) throws AnalyticsRefreshException {
        Exception error;

        final PaymentApi paymentApi = getPaymentUserApi();
        try {
            return paymentApi.getAccountPaymentMethods(accountId, true, true, PLUGIN_PROPERTIES, context);
        } catch (final PaymentApiException e) {
            error = e;
            if (e.getCode() == ErrorCode.PAYMENT_NO_SUCH_PAYMENT_PLUGIN.getCode() || // Plugin was uninstalled
                e.getCode() == ErrorCode.PAYMENT_GET_PAYMENT_METHODS.getCode()) { // Plugin doesn't return information on deleted payment methods
                logger.warn(e.getMessage() + ". Analytics tables will be missing plugin specific information");

                try {
                    return paymentApi.getAccountPaymentMethods(accountId, true, false, PLUGIN_PROPERTIES, context);
                } catch (final PaymentApiException e1) {
                    error = e1;
                }
            }
        } catch (final RuntimeException e) {
            // Plugin exception?
            error = e;
            try {
                final List<PaymentMethod> accountPaymentMethods = paymentApi.getAccountPaymentMethods(accountId, true, false, PLUGIN_PROPERTIES, context);
                logger.warn(e.getMessage() + ". Analytics tables will be missing plugin specific information");
                return accountPaymentMethods;
            } catch (final PaymentApiException ignored) {
            }
        }

        logger.warn("Error retrieving payment methods for account id {}", accountId, error);
        throw new AnalyticsRefreshException(error);
    }

    protected AuditLog getPaymentCreationAuditLog(final UUID paymentId, final SafeAccountAuditLogs safeAccountAuditLogs) throws AnalyticsRefreshException {

        return new AuditLogWithRetry(safeAccountAuditLogs).withRetry(new AuditLogHandler() {
            @Override
            public AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs) {
                final List<AuditLog> auditLogsForPayment = accountAuditLogs.getAuditLogsForPayment(paymentId);
                for (final AuditLog auditLog : auditLogsForPayment) {
                    if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                        return auditLog;
                    }
                }
                return null;
            }
        }, "Unable to find payment creation audit log for id {}", paymentId);
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

    protected AuditLog getFieldCreationAuditLog(final UUID fieldId, final SafeAccountAuditLogs safeAccountAuditLogs) throws AnalyticsRefreshException {
        return new AuditLogWithRetry(safeAccountAuditLogs).withRetry(new AuditLogHandler() {
            @Override
            public AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs) {
                final List<AuditLog> auditLogsForTag = accountAuditLogs.getAuditLogsForCustomField(fieldId);
                for (final AuditLog auditLog : auditLogsForTag) {
                    if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                        return auditLog;
                    }
                }
                return null;
            }
        }, "Unable to find Field creation audit log for id {}", fieldId);
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

    protected AuditLog getTagCreationAuditLog(final UUID tagId, final SafeAccountAuditLogs safeAccountAuditLogs) throws AnalyticsRefreshException {
        return new AuditLogWithRetry(safeAccountAuditLogs).withRetry(new AuditLogHandler() {
            @Override
            public AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs) {
                final List<AuditLog> auditLogsForTag = accountAuditLogs.getAuditLogsForTag(tagId);
                for (final AuditLog auditLog : auditLogsForTag) {
                    if (auditLog.getChangeType().equals(ChangeType.INSERT)) {
                        return auditLog;
                    }
                }
                return null;
            }
        }, "Unable to find Tag creation audit log for id {}", tagId);
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

    private interface AuditLogHandler {
        AuditLog getAuditLog(final AccountAuditLogs accountAuditLogs);
    }

    private static class AuditLogWithRetry {

        private final SafeAccountAuditLogs safeAccountAuditLogs;

        public AuditLogWithRetry(final SafeAccountAuditLogs safeAccountAuditLogs) {
            this.safeAccountAuditLogs = safeAccountAuditLogs;
        }

        // Fetch audit log for resource based on cached value and refresh cache if not found.
        public AuditLog withRetry(final AuditLogHandler handler, final String warnFmt, final Object... warnObjs) throws AnalyticsRefreshException {
            AuditLog result = handler.getAuditLog(safeAccountAuditLogs.getAccountAuditLogs(false));
            if (result == null) {
                result = handler.getAuditLog(safeAccountAuditLogs.getAccountAuditLogs(true));
                if (result == null) {
                    logger.warn(warnFmt, warnObjs);
                }
            }
            return result;
        }
    }
}
