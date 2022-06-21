/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2021 Equinix, Inc
 * Copyright 2014-2021 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.account.api.AccountUserApi;
import org.killbill.billing.catalog.api.BillingPeriod;
import org.killbill.billing.catalog.api.CatalogUserApi;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.catalog.api.InternationalPrice;
import org.killbill.billing.catalog.api.PhaseType;
import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.PlanPhase;
import org.killbill.billing.catalog.api.PriceList;
import org.killbill.billing.catalog.api.Product;
import org.killbill.billing.catalog.api.ProductCategory;
import org.killbill.billing.catalog.api.Recurring;
import org.killbill.billing.catalog.api.StaticCatalog;
import org.killbill.billing.catalog.api.VersionedCatalog;
import org.killbill.billing.entitlement.api.Subscription;
import org.killbill.billing.entitlement.api.SubscriptionApi;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.entitlement.api.SubscriptionEvent;
import org.killbill.billing.entitlement.api.SubscriptionEventType;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.invoice.api.InvoiceItemType;
import org.killbill.billing.invoice.api.InvoicePayment;
import org.killbill.billing.invoice.api.InvoicePaymentType;
import org.killbill.billing.invoice.api.InvoiceStatus;
import org.killbill.billing.invoice.api.InvoiceUserApi;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.payment.api.InvoicePaymentApi;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentApi;
import org.killbill.billing.payment.api.PaymentMethod;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PaymentTransaction;
import org.killbill.billing.payment.api.TransactionStatus;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.plugin.analytics.api.BusinessEntityBase;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfiguration;
import org.killbill.billing.plugin.analytics.api.core.AnalyticsConfigurationHandler;
import org.killbill.billing.plugin.analytics.dao.CurrencyConversionDao;
import org.killbill.billing.plugin.analytics.dao.TestCallContext;
import org.killbill.billing.plugin.analytics.dao.factory.BusinessContextFactory;
import org.killbill.billing.plugin.analytics.dao.factory.PluginPropertiesManager;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao.BusinessInvoiceItemType;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao.ItemSource;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.tenant.api.TenantUserApi;
import org.killbill.billing.util.api.AuditLevel;
import org.killbill.billing.util.api.AuditUserApi;
import org.killbill.billing.util.api.CustomFieldUserApi;
import org.killbill.billing.util.api.RecordIdApi;
import org.killbill.billing.util.api.TagUserApi;
import org.killbill.billing.util.audit.AccountAuditLogs;
import org.killbill.billing.util.audit.AccountAuditLogsForObjectType;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.audit.ChangeType;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.customfield.CustomField;
import org.killbill.billing.util.entity.Pagination;
import org.killbill.billing.util.tag.Tag;
import org.killbill.billing.util.tag.TagDefinition;
import org.killbill.clock.ClockMock;
import org.killbill.commons.locker.GlobalLocker;
import org.killbill.commons.locker.memory.MemoryGlobalLocker;
import org.killbill.notificationq.DefaultNotificationQueueService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import com.google.common.collect.ImmutableList;

public abstract class AnalyticsTestSuiteNoDB {

    private static final DateTime INVOICE_CREATED_DATE = new DateTime(2016, 1, 22, 10, 56, 53, DateTimeZone.UTC);
    protected final Logger logger = LoggerFactory.getLogger(AnalyticsTestSuiteNoDB.class);

    protected final GlobalLocker locker = new MemoryGlobalLocker();

    protected final Long accountRecordId = 1L;
    protected final Long subscriptionEventRecordId = 2L;
    protected final Long invoiceRecordId = 3L;
    protected final Long invoiceItemRecordId = 4L;
    protected final Long secondInvoiceItemRecordId = 24L;
    protected final Long invoicePaymentRecordId = 5L;
    protected final Long blockingStateRecordId = 6L;
    protected final Long fieldRecordId = 7L;
    protected final Long tagRecordId = 8L;
    protected final Long tenantRecordId = 9L;
    protected final Long bundleRecordId = 10L;

    protected final ReportGroup reportGroup = ReportGroup.partner;
    protected final PluginPropertiesManager pluginPropertiesManager = new PluginPropertiesManager(new AnalyticsConfiguration());
    protected final BusinessInvoiceItemType invoiceItemType = BusinessInvoiceItemType.INVOICE_ITEM_ADJUSTMENT;
    protected final ItemSource itemSource = ItemSource.user;
    protected final ClockMock clock = new ClockMock();
    protected final CurrencyConverter currencyConverter = Mockito.mock(CurrencyConverter.class);
    protected final CurrencyConversionDao currencyConversionDao = Mockito.mock(CurrencyConversionDao.class);

    protected DefaultNotificationQueueService notificationQueueService = Mockito.mock(DefaultNotificationQueueService.class);

    protected final String serviceName = UUID.randomUUID().toString();
    protected final String stateName = UUID.randomUUID().toString();

    protected final UUID blackListedAccountId = UUID.randomUUID();

    protected Account account;
    protected Account parentAccount;
    protected SubscriptionBundle bundle;
    protected Plan plan;
    protected PlanPhase phase;
    protected PriceList priceList;
    protected SubscriptionEvent subscriptionTransition;
    protected Invoice invoice;
    protected InvoiceItem invoiceItem;
    protected InvoiceItem linkedInvoiceItem;
    protected InvoicePayment invoicePayment;
    protected PaymentMethod paymentMethod;
    protected Payment payment;
    protected Payment paymentNoRefund;
    protected PaymentTransaction paymentTransaction;
    protected PaymentTransaction purchaseTransaction;
    protected PaymentTransaction refundTransaction;
    protected CustomField customField;
    protected Tag tag;
    protected TagDefinition tagDefinition;
    protected AccountAuditLogs accountAuditLogs;
    protected AuditLog auditLog;
    protected CallContext callContext;
    protected AnalyticsConfigurationHandler analyticsConfigurationHandler;
    protected OSGIKillbillAPI killbillAPI;
    protected OSGIKillbillDataSource killbillDataSource;
    protected OSGIConfigPropertiesService osgiConfigPropertiesService;
    protected ExecutorService executor;
    protected BusinessContextFactory businessContextFactory;

    protected void verifyBusinessEntityBase(final BusinessEntityBase businessEntityBase) {
        Assert.assertEquals(businessEntityBase.getCreatedBy(), auditLog.getUserName());
        Assert.assertEquals(businessEntityBase.getCreatedReasonCode(), auditLog.getReasonCode());
        Assert.assertEquals(businessEntityBase.getCreatedComments(), auditLog.getComment());
        Assert.assertEquals(businessEntityBase.getAccountId(), account.getId());
        Assert.assertEquals(businessEntityBase.getAccountName(), account.getName());
        Assert.assertEquals(businessEntityBase.getAccountExternalKey(), account.getExternalKey());
        Assert.assertEquals(businessEntityBase.getReportGroup(), reportGroup.toString());
    }

    protected void verifyBusinessModelDaoBase(final BusinessModelDaoBase businessModelDaoBase,
                                              final Long accountRecordId,
                                              final Long tenantRecordId) {
        Assert.assertEquals(businessModelDaoBase.getCreatedBy(), auditLog.getUserName());
        Assert.assertEquals(businessModelDaoBase.getCreatedReasonCode(), auditLog.getReasonCode());
        Assert.assertEquals(businessModelDaoBase.getCreatedComments(), auditLog.getComment());
        Assert.assertEquals(businessModelDaoBase.getAccountId(), account.getId());
        Assert.assertEquals(businessModelDaoBase.getAccountName(), account.getName());
        Assert.assertEquals(businessModelDaoBase.getAccountExternalKey(), account.getExternalKey());
        Assert.assertEquals(businessModelDaoBase.getAccountRecordId(), accountRecordId);
        Assert.assertEquals(businessModelDaoBase.getTenantRecordId(), tenantRecordId);
        Assert.assertEquals(businessModelDaoBase.getReportGroup(), reportGroup.toString());
    }

    protected InvoiceItem createInvoiceItem(final UUID invoiceId, final InvoiceItemType type) {
        return createInvoiceItem(invoiceId, type, BigDecimal.TEN);
    }

    protected InvoiceItem createInvoiceItem(final UUID invoiceId, final InvoiceItemType type, final BigDecimal amount) {
        return createInvoiceItem(invoiceId, type, UUID.randomUUID(), new LocalDate(2013, 1, 2), new LocalDate(2013, 2, 5), amount, null);
    }

    protected InvoiceItem createInvoiceItem(final UUID invoiceId,
                                            final InvoiceItemType invoiceItemType,
                                            final UUID subscriptionId,
                                            final LocalDate startDate,
                                            final LocalDate endDate,
                                            final BigDecimal amount,
                                            @Nullable final UUID linkedItemId) {
        final UUID invoiceItemId = UUID.randomUUID();
        return createInvoiceItem(invoiceItemId,
                                 invoiceId,
                                 invoiceItemType,
                                 subscriptionId,
                                 startDate,
                                 endDate,
                                 amount,
                                 linkedItemId);
    }

    protected InvoiceItem createInvoiceItem(final UUID invoiceItemId,
                                            final UUID invoiceId,
                                            final InvoiceItemType invoiceItemType,
                                            final UUID subscriptionId,
                                            final LocalDate startDate,
                                            final LocalDate endDate,
                                            final BigDecimal amount,
                                            @Nullable final UUID linkedItemId) {
        final InvoiceItem invoiceItem = Mockito.mock(InvoiceItem.class);
        Mockito.when(invoiceItem.getId()).thenReturn(invoiceItemId);
        Mockito.when(invoiceItem.getInvoiceItemType()).thenReturn(invoiceItemType);
        Mockito.when(invoiceItem.getInvoiceId()).thenReturn(invoiceId);
        Mockito.when(invoiceItem.getAccountId()).thenReturn(UUID.randomUUID());
        Mockito.when(invoiceItem.getStartDate()).thenReturn(startDate);
        Mockito.when(invoiceItem.getEndDate()).thenReturn(endDate);
        Mockito.when(invoiceItem.getAmount()).thenReturn(amount);
        Mockito.when(invoiceItem.getCurrency()).thenReturn(Currency.EUR);
        Mockito.when(invoiceItem.getDescription()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(invoiceItem.getBundleId()).thenReturn(UUID.randomUUID());
        Mockito.when(invoiceItem.getSubscriptionId()).thenReturn(subscriptionId);
        Mockito.when(invoiceItem.getPlanName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(invoiceItem.getPhaseName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(invoiceItem.getUsageName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(invoiceItem.getRate()).thenReturn(new BigDecimal("1203"));
        Mockito.when(invoiceItem.getLinkedItemId()).thenReturn(linkedItemId);
        Mockito.when(invoiceItem.getCreatedDate()).thenReturn(INVOICE_CREATED_DATE);

        return invoiceItem;
    }

    @BeforeMethod(groups = "fast")
    public void setUp() throws Exception {
        Mockito.when(currencyConverter.getConvertedCurrency()).thenReturn("USD");
        Mockito.when(currencyConverter.getConvertedValue(Mockito.<BigDecimal>any(), Mockito.anyString(), Mockito.<LocalDate>any())).thenReturn(BigDecimal.TEN);
        Mockito.when(currencyConverter.getConvertedValue(Mockito.<BigDecimal>any(), Mockito.<Account>any())).thenReturn(BigDecimal.TEN);
        Mockito.when(currencyConverter.getConvertedValue(Mockito.<Invoice>any())).thenReturn(BigDecimal.TEN);
        Mockito.when(currencyConverter.getConvertedValue(Mockito.<BigDecimal>any(), Mockito.<Invoice>any())).thenReturn(BigDecimal.TEN);
        Mockito.when(currencyConverter.getConvertedValue(Mockito.<InvoiceItem>any(), Mockito.<Invoice>any())).thenReturn(BigDecimal.TEN);
        Mockito.when(currencyConverter.getConvertedValue(Mockito.<InvoicePayment>any(), Mockito.<PaymentTransaction>any(), Mockito.<Invoice>any())).thenReturn(BigDecimal.TEN);

        account = Mockito.mock(Account.class);
        Mockito.when(account.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(account.getExternalKey()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(account.getName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(account.getFirstNameLength()).thenReturn(4);
        Mockito.when(account.getEmail()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(account.getBillCycleDayLocal()).thenReturn(2);
        Mockito.when(account.getCurrency()).thenReturn(Currency.BRL);
        Mockito.when(account.getPaymentMethodId()).thenReturn(UUID.randomUUID());
        Mockito.when(account.getTimeZone()).thenReturn(DateTimeZone.forID("Europe/London"));
        Mockito.when(account.getLocale()).thenReturn(UUID.randomUUID().toString().substring(0, 5));
        Mockito.when(account.getAddress1()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(account.getAddress2()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(account.getCompanyName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(account.getCity()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(account.getStateOrProvince()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(account.getPostalCode()).thenReturn(UUID.randomUUID().toString().substring(0, 16));
        Mockito.when(account.getCountry()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(account.getPhone()).thenReturn(UUID.randomUUID().toString().substring(0, 25));
        Mockito.when(account.isMigrated()).thenReturn(true);
        Mockito.when(account.getCreatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 47, DateTimeZone.UTC));
        Mockito.when(account.getUpdatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 48, DateTimeZone.UTC));
        final UUID accountId = account.getId();

        parentAccount = Mockito.mock(Account.class);
        Mockito.when(parentAccount.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(parentAccount.getName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(parentAccount.getExternalKey()).thenReturn(UUID.randomUUID().toString());
        final UUID parentAccountId = parentAccount.getId();

        Mockito.when(account.getParentAccountId()).thenReturn(parentAccountId);

        bundle = Mockito.mock(SubscriptionBundle.class);
        Mockito.when(bundle.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(bundle.getAccountId()).thenReturn(accountId);
        Mockito.when(bundle.getExternalKey()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(bundle.getCreatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 48, DateTimeZone.UTC));
        final UUID bundleId = bundle.getId();

        final UUID subscriptionId = UUID.randomUUID();
        final Subscription subscription = Mockito.mock(Subscription.class);
        Mockito.when(subscription.getBaseEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(subscription.getId()).thenReturn(subscriptionId);
        Mockito.when(bundle.getSubscriptions()).thenReturn(ImmutableList.<Subscription>of(subscription));

        final Product product = Mockito.mock(Product.class);
        Mockito.when(product.getName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(product.getCategory()).thenReturn(ProductCategory.STANDALONE);
        Mockito.when(product.getCatalogName()).thenReturn(UUID.randomUUID().toString());

        plan = Mockito.mock(Plan.class);
        Mockito.when(plan.getProduct()).thenReturn(product);
        Mockito.when(plan.getName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(plan.getRecurringBillingPeriod()).thenReturn(BillingPeriod.QUARTERLY);
        Mockito.when(plan.getEffectiveDateForExistingSubscriptions()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 59, DateTimeZone.UTC).toDate());
        final String planName = plan.getName();

        phase = Mockito.mock(PlanPhase.class);
        final Recurring recurring = Mockito.mock(Recurring.class);
        Mockito.when(recurring.getBillingPeriod()).thenReturn(BillingPeriod.QUARTERLY);
        Mockito.when(phase.getRecurring()).thenReturn(recurring);
        Mockito.when(phase.getName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(phase.getPhaseType()).thenReturn(PhaseType.DISCOUNT);

        final InternationalPrice internationalPrice = Mockito.mock(InternationalPrice.class);
        Mockito.when(recurring.getRecurringPrice()).thenReturn(internationalPrice);
        Mockito.when(internationalPrice.getPrice(Mockito.<Currency>any())).thenReturn(BigDecimal.TEN);
        final String phaseName = phase.getName();

        priceList = Mockito.mock(PriceList.class);
        Mockito.when(priceList.getName()).thenReturn(UUID.randomUUID().toString());

        subscriptionTransition = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(subscriptionTransition.getEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(subscriptionTransition.getServiceName()).thenReturn(serviceName);
        Mockito.when(subscriptionTransition.getServiceStateName()).thenReturn(stateName);
        Mockito.when(subscriptionTransition.getNextPlan()).thenReturn(plan);
        Mockito.when(subscriptionTransition.getNextPhase()).thenReturn(phase);
        Mockito.when(subscriptionTransition.getNextPriceList()).thenReturn(priceList);
        Mockito.when(subscriptionTransition.getEffectiveDate()).thenReturn(new LocalDate(2010, 1, 2));
        Mockito.when(subscriptionTransition.getEffectiveDate()).thenReturn(new LocalDate(2011, 2, 3));
        Mockito.when(subscriptionTransition.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_ENTITLEMENT);
        Mockito.when(subscriptionTransition.getId()).thenReturn(UUID.randomUUID());
        final UUID nextEventId = subscriptionTransition.getId();

        invoiceItem = Mockito.mock(InvoiceItem.class);
        Mockito.when(invoiceItem.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(invoiceItem.getInvoiceItemType()).thenReturn(InvoiceItemType.EXTERNAL_CHARGE);
        Mockito.when(invoiceItem.getInvoiceId()).thenReturn(UUID.randomUUID());
        Mockito.when(invoiceItem.getAccountId()).thenReturn(UUID.randomUUID());
        Mockito.when(invoiceItem.getStartDate()).thenReturn(new LocalDate(1999, 9, 9));
        Mockito.when(invoiceItem.getEndDate()).thenReturn(new LocalDate(2048, 1, 1));
        Mockito.when(invoiceItem.getAmount()).thenReturn(new BigDecimal("12000"));
        Mockito.when(invoiceItem.getCurrency()).thenReturn(Currency.EUR);
        Mockito.when(invoiceItem.getDescription()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(invoiceItem.getBundleId()).thenReturn(bundleId);
        Mockito.when(invoiceItem.getSubscriptionId()).thenReturn(subscriptionId);
        Mockito.when(invoiceItem.getPlanName()).thenReturn(planName);
        Mockito.when(invoiceItem.getPhaseName()).thenReturn(phaseName);
        Mockito.when(invoiceItem.getRate()).thenReturn(new BigDecimal("1203"));
        Mockito.when(invoiceItem.getLinkedItemId()).thenReturn(UUID.randomUUID());
        Mockito.when(invoiceItem.getCreatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 51, DateTimeZone.UTC));
        final UUID invoiceItemId = invoiceItem.getId();

        linkedInvoiceItem = createInvoiceItem(invoiceItem.getLinkedItemId(),
                                              invoiceItem.getInvoiceId(),
                                              InvoiceItemType.TAX,
                                              invoiceItem.getSubscriptionId(),
                                              null,
                                              null,
                                              BigDecimal.ONE,
                                              null);

        final UUID invoiceId = invoiceItem.getInvoiceId();
        final UUID paymentId = UUID.randomUUID();

        invoicePayment = Mockito.mock(InvoicePayment.class);
        Mockito.when(invoicePayment.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(invoicePayment.getPaymentId()).thenReturn(paymentId);
        Mockito.when(invoicePayment.getType()).thenReturn(InvoicePaymentType.ATTEMPT);
        Mockito.when(invoicePayment.getInvoiceId()).thenReturn(invoiceId);
        Mockito.when(invoicePayment.getPaymentDate()).thenReturn(new DateTime(2003, 4, 12, 3, 34, 52, DateTimeZone.UTC));
        Mockito.when(invoicePayment.getAmount()).thenReturn(BigDecimal.ONE);
        Mockito.when(invoicePayment.getCurrency()).thenReturn(Currency.MXN);
        Mockito.when(invoicePayment.getLinkedInvoicePaymentId()).thenReturn(UUID.randomUUID());
        Mockito.when(invoicePayment.getPaymentCookieId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(invoicePayment.getCreatedDate()).thenReturn(INVOICE_CREATED_DATE);
        final UUID invoicePaymentId = invoicePayment.getId();

        invoice = Mockito.mock(Invoice.class);
        Mockito.when(invoice.getId()).thenReturn(invoiceId);
        Mockito.when(invoice.getInvoiceItems()).thenReturn(ImmutableList.<InvoiceItem>of(invoiceItem, linkedInvoiceItem));
        Mockito.when(invoice.getNumberOfItems()).thenReturn(2);
        Mockito.when(invoice.getPayments()).thenReturn(ImmutableList.<InvoicePayment>of(invoicePayment));
        Mockito.when(invoice.getNumberOfPayments()).thenReturn(1);
        Mockito.when(invoice.getAccountId()).thenReturn(accountId);
        Mockito.when(invoice.getInvoiceNumber()).thenReturn(42);
        Mockito.when(invoice.getInvoiceDate()).thenReturn(new LocalDate(1954, 12, 1));
        Mockito.when(invoice.getTargetDate()).thenReturn(new LocalDate(2017, 3, 4));
        Mockito.when(invoice.getCurrency()).thenReturn(Currency.AUD);
        Mockito.when(invoice.getPaidAmount()).thenReturn(BigDecimal.ZERO);
        Mockito.when(invoice.getOriginalChargedAmount()).thenReturn(new BigDecimal("1922"));
        Mockito.when(invoice.getChargedAmount()).thenReturn(new BigDecimal("100293"));
        Mockito.when(invoice.getCreditedAmount()).thenReturn(new BigDecimal("283"));
        Mockito.when(invoice.getRefundedAmount()).thenReturn(new BigDecimal("384"));
        final BigDecimal balance = new BigDecimal("12001");
        Mockito.when(invoice.getBalance()).thenReturn(balance);
        Mockito.when(invoice.isMigrationInvoice()).thenReturn(false);
        Mockito.when(invoice.getCreatedDate()).thenReturn(INVOICE_CREATED_DATE);
        Mockito.when(invoice.getStatus()).thenReturn(InvoiceStatus.COMMITTED);

        final PaymentMethodPlugin paymentMethodPlugin = Mockito.mock(PaymentMethodPlugin.class);
        Mockito.when(paymentMethodPlugin.getExternalPaymentMethodId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethodPlugin.isDefaultPaymentMethod()).thenReturn(true);

        paymentMethod = Mockito.mock(PaymentMethod.class);
        Mockito.when(paymentMethod.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(paymentMethod.getAccountId()).thenReturn(accountId);
        Mockito.when(paymentMethod.isActive()).thenReturn(true);
        Mockito.when(paymentMethod.getPluginName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentMethod.getPluginDetail()).thenReturn(paymentMethodPlugin);
        Mockito.when(paymentMethod.getCreatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 55, DateTimeZone.UTC));
        final UUID paymentMethodId = paymentMethod.getId();

        paymentTransaction = Mockito.mock(PaymentTransaction.class);
        Mockito.when(paymentTransaction.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(paymentTransaction.getTransactionType()).thenReturn(TransactionType.CAPTURE);
        Mockito.when(paymentTransaction.getAmount()).thenReturn(new BigDecimal("199999"));
        Mockito.when(paymentTransaction.getCurrency()).thenReturn(Currency.USD);
        Mockito.when(paymentTransaction.getEffectiveDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 56, DateTimeZone.UTC));
        Mockito.when(paymentTransaction.getExternalKey()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(paymentTransaction.getTransactionStatus()).thenReturn(TransactionStatus.SUCCESS);

        purchaseTransaction = Mockito.mock(PaymentTransaction.class);
        Mockito.when(purchaseTransaction.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(purchaseTransaction.getTransactionType()).thenReturn(TransactionType.PURCHASE);
        Mockito.when(purchaseTransaction.getAmount()).thenReturn(new BigDecimal("199999"));
        Mockito.when(purchaseTransaction.getCurrency()).thenReturn(Currency.USD);
        Mockito.when(purchaseTransaction.getEffectiveDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 56, DateTimeZone.UTC));
        Mockito.when(purchaseTransaction.getExternalKey()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(purchaseTransaction.getTransactionStatus()).thenReturn(TransactionStatus.SUCCESS);

        refundTransaction = Mockito.mock(PaymentTransaction.class);
        Mockito.when(refundTransaction.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(refundTransaction.getTransactionType()).thenReturn(TransactionType.REFUND);
        Mockito.when(refundTransaction.getAmount()).thenReturn(new BigDecimal("199998"));
        Mockito.when(refundTransaction.getCurrency()).thenReturn(Currency.USD);
        Mockito.when(refundTransaction.getEffectiveDate()).thenReturn(new DateTime(2016, 1, 23, 10, 56, 56, DateTimeZone.UTC));
        Mockito.when(refundTransaction.getExternalKey()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(refundTransaction.getTransactionStatus()).thenReturn(TransactionStatus.PENDING);

        payment = Mockito.mock(Payment.class);
        Mockito.when(payment.getId()).thenReturn(paymentId);
        Mockito.when(payment.getAccountId()).thenReturn(accountId);
        Mockito.when(payment.getPaymentMethodId()).thenReturn(paymentMethodId);
        Mockito.when(payment.getPaymentNumber()).thenReturn(1);
        Mockito.when(payment.getCapturedAmount()).thenReturn(new BigDecimal("199999"));
        Mockito.when(payment.getRefundedAmount()).thenReturn(new BigDecimal("199998"));
        Mockito.when(payment.getCurrency()).thenReturn(Currency.USD);
        Mockito.when(payment.getTransactions()).thenReturn(ImmutableList.<PaymentTransaction>of(paymentTransaction, refundTransaction));
        Mockito.when(payment.getCreatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 56, DateTimeZone.UTC));

        paymentNoRefund = Mockito.mock(Payment.class);
        Mockito.when(paymentNoRefund.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(paymentNoRefund.getAccountId()).thenReturn(accountId);
        Mockito.when(paymentNoRefund.getPaymentMethodId()).thenReturn(paymentMethodId);
        Mockito.when(paymentNoRefund.getPaymentNumber()).thenReturn(1);
        Mockito.when(paymentNoRefund.getCapturedAmount()).thenReturn(new BigDecimal("199999"));
        Mockito.when(paymentNoRefund.getCurrency()).thenReturn(Currency.USD);
        Mockito.when(paymentNoRefund.getTransactions()).thenReturn(ImmutableList.<PaymentTransaction>of(paymentTransaction));
        Mockito.when(paymentNoRefund.getCreatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 56, DateTimeZone.UTC));

        customField = Mockito.mock(CustomField.class);
        Mockito.when(customField.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(customField.getObjectId()).thenReturn(UUID.randomUUID());
        Mockito.when(customField.getObjectType()).thenReturn(ObjectType.ACCOUNT);
        Mockito.when(customField.getFieldName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(customField.getFieldValue()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(customField.getCreatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 57, DateTimeZone.UTC));
        final UUID fieldId = customField.getId();

        tag = Mockito.mock(Tag.class);
        Mockito.when(tag.getObjectId()).thenReturn(UUID.randomUUID());
        Mockito.when(tag.getObjectType()).thenReturn(ObjectType.ACCOUNT);
        Mockito.when(tag.getTagDefinitionId()).thenReturn(UUID.randomUUID());
        Mockito.when(tag.getCreatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 58, DateTimeZone.UTC));
        final UUID tagId = tag.getId();

        tagDefinition = Mockito.mock(TagDefinition.class);
        Mockito.when(tagDefinition.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(tagDefinition.getName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(tagDefinition.getDescription()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(tagDefinition.isControlTag()).thenReturn(false);
        Mockito.when(tagDefinition.getApplicableObjectTypes()).thenReturn(ImmutableList.<ObjectType>of(ObjectType.INVOICE));
        Mockito.when(tagDefinition.getCreatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 59, DateTimeZone.UTC));

        auditLog = Mockito.mock(AuditLog.class);
        Mockito.when(auditLog.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(auditLog.getChangeType()).thenReturn(ChangeType.INSERT);
        Mockito.when(auditLog.getUserName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(auditLog.getCreatedDate()).thenReturn(new DateTime(2012, 12, 31, 23, 59, 59, DateTimeZone.UTC));
        Mockito.when(auditLog.getReasonCode()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(auditLog.getUserToken()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(auditLog.getComment()).thenReturn(UUID.randomUUID().toString());

        accountAuditLogs = Mockito.mock(AccountAuditLogs.class);
        Mockito.when(accountAuditLogs.getAuditLogsForAccount()).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogsForBundle(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogsForSubscription(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogsForSubscriptionEvent(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogsForBlockingState(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogsForInvoice(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogsForInvoiceItem(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogsForInvoicePayment(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogsForPayment(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogsForTag(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogsForCustomField(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        final AccountAuditLogsForObjectType accountAuditLogsForObjectType = Mockito.mock(AccountAuditLogsForObjectType.class);
        Mockito.when(accountAuditLogsForObjectType.getAuditLogs(Mockito.<UUID>any())).thenReturn(ImmutableList.<AuditLog>of(auditLog));
        Mockito.when(accountAuditLogs.getAuditLogs(Mockito.<ObjectType>any())).thenReturn(accountAuditLogsForObjectType);

        // Real class for the binding to work with JDBI
        callContext = new TestCallContext();
        final UUID tenantId = callContext.getTenantId();

        final RecordIdApi recordIdApi = Mockito.mock(RecordIdApi.class);
        Mockito.when(recordIdApi.getRecordId(Mockito.eq(accountId), Mockito.eq(ObjectType.ACCOUNT), Mockito.<TenantContext>any())).thenReturn(accountRecordId);
        Mockito.when(recordIdApi.getRecordId(Mockito.eq(nextEventId), Mockito.eq(ObjectType.SUBSCRIPTION_EVENT), Mockito.<TenantContext>any())).thenReturn(subscriptionEventRecordId);
        Mockito.when(recordIdApi.getRecordId(Mockito.eq(invoiceId), Mockito.eq(ObjectType.INVOICE), Mockito.<TenantContext>any())).thenReturn(invoiceRecordId);
        Mockito.when(recordIdApi.getRecordId(Mockito.eq(invoiceItemId), Mockito.eq(ObjectType.INVOICE_ITEM), Mockito.<TenantContext>any())).thenReturn(invoiceItemRecordId);
        Mockito.when(recordIdApi.getRecordId(Mockito.eq(invoicePaymentId), Mockito.eq(ObjectType.INVOICE_PAYMENT), Mockito.<TenantContext>any())).thenReturn(invoicePaymentRecordId);
        Mockito.when(recordIdApi.getRecordId(Mockito.eq(fieldId), Mockito.eq(ObjectType.CUSTOM_FIELD), Mockito.<TenantContext>any())).thenReturn(fieldRecordId);
        Mockito.when(recordIdApi.getRecordId(Mockito.eq(tagId), Mockito.eq(ObjectType.TAG), Mockito.<TenantContext>any())).thenReturn(tagRecordId);
        Mockito.when(recordIdApi.getRecordId(Mockito.eq(tenantId), Mockito.eq(ObjectType.TENANT), Mockito.<TenantContext>any())).thenReturn(tenantRecordId);

        killbillAPI = Mockito.mock(OSGIKillbillAPI.class);
        final AccountUserApi accountUserApi = Mockito.mock(AccountUserApi.class);
        Mockito.when(accountUserApi.getAccountById(Mockito.<UUID>any(), Mockito.<TenantContext>any())).thenReturn(account);
        final TagUserApi tagUserApi = Mockito.mock(TagUserApi.class);

        final CustomFieldUserApi customFieldUserApi = Mockito.mock(CustomFieldUserApi.class);
        Mockito.when(customFieldUserApi.getCustomFieldsForAccount(Mockito.<UUID>any(), Mockito.<TenantContext>any())).thenReturn(ImmutableList.<CustomField>of(customField));

        final AuditUserApi auditUserApi = Mockito.mock(AuditUserApi.class);
        Mockito.when(auditUserApi.getAccountAuditLogs(Mockito.<UUID>any(), Mockito.<AuditLevel>any(), Mockito.<TenantContext>any())).thenReturn(accountAuditLogs);
        Mockito.when(auditUserApi.getAccountAuditLogs(Mockito.<UUID>any(), Mockito.<ObjectType>any(), Mockito.<AuditLevel>any(), Mockito.<TenantContext>any())).thenReturn(accountAuditLogsForObjectType);
        Mockito.when(auditUserApi.getAuditLogs(Mockito.<UUID>any(), Mockito.<ObjectType>any(), Mockito.<AuditLevel>any(), Mockito.<TenantContext>any())).thenReturn(ImmutableList.<AuditLog>of());

        final SubscriptionApi subscriptionApi = Mockito.mock(SubscriptionApi.class);
        Mockito.when(subscriptionApi.getSubscriptionBundlesForAccountId(Mockito.<UUID>any(), Mockito.<TenantContext>any())).thenReturn(ImmutableList.<SubscriptionBundle>of(bundle));
        Mockito.when(subscriptionApi.getSubscriptionBundlesForExternalKey(Mockito.<String>any(), Mockito.<TenantContext>any())).thenReturn(ImmutableList.<SubscriptionBundle>of(bundle));

        final InvoiceUserApi invoiceApi = Mockito.mock(InvoiceUserApi.class);
        Mockito.when(invoiceApi.getAccountBalance(Mockito.eq(account.getId()),
                                                  Mockito.any(TenantContext.class)))
               .thenReturn(balance);
        //noinspection unchecked
        final Pagination<Invoice> accountInvoicePages = Mockito.mock(Pagination.class);
        Mockito.when(accountInvoicePages.iterator()).thenReturn(ImmutableList.of(invoice).iterator());
        Mockito.when(invoiceApi.searchInvoices(Mockito.eq(account.getId().toString()),
                                               Mockito.anyLong(),
                                               Mockito.anyLong(),
                                               Mockito.any(TenantContext.class)))
               .thenReturn(accountInvoicePages);
        Mockito.when(invoiceApi.getInvoicesByAccount(Mockito.eq(account.getId()),
                                                     Mockito.anyBoolean(),
                                                     Mockito.anyBoolean(),
                                                     Mockito.any(TenantContext.class)))
               .thenReturn(ImmutableList.of(invoice));
        Mockito.when(invoiceApi.getInvoice(Mockito.eq(invoice.getId()),
                                           Mockito.any(TenantContext.class)))
               .thenReturn(invoice);
        Mockito.when(invoiceApi.getInvoiceByInvoiceItem(Mockito.eq(invoiceItem.getId()),
                                                        Mockito.any(TenantContext.class)))
               .thenReturn(invoice);
        Mockito.when(invoiceApi.getInvoiceByInvoiceItem(Mockito.eq(linkedInvoiceItem.getId()),
                                                        Mockito.any(TenantContext.class)))
               .thenReturn(invoice);

        final PaymentApi paymentApi = Mockito.mock(PaymentApi.class);
        //noinspection unchecked
        Mockito.when(paymentApi.getAccountPayments(Mockito.eq(account.getId()),
                                                   Mockito.anyBoolean(),
                                                   Mockito.anyBoolean(),
                                                   Mockito.any(Iterable.class),
                                                   Mockito.any(TenantContext.class)))
               .thenReturn(ImmutableList.of(payment));
        //noinspection unchecked
        Mockito.when(paymentApi.getAccountPaymentMethods(Mockito.eq(account.getId()),
                                                         Mockito.anyBoolean(),
                                                         Mockito.anyBoolean(),
                                                         Mockito.any(Iterable.class),
                                                         Mockito.any(TenantContext.class)))
               .thenReturn(ImmutableList.of(paymentMethod));

        final InvoicePaymentApi invoicePaymentApi = Mockito.mock(InvoicePaymentApi.class);
        Mockito.when(invoicePaymentApi.getInvoicePayments(Mockito.eq(payment.getId()),
                                                          Mockito.any(TenantContext.class)))
               .thenReturn(ImmutableList.<InvoicePayment>of(invoicePayment));

        final CatalogUserApi catalogUserApi = Mockito.mock(CatalogUserApi.class);
        final VersionedCatalog versionedCatalog = Mockito.mock(VersionedCatalog.class);
        //noinspection unchecked
        Mockito.when(catalogUserApi.getCatalog(Mockito.any(),
                                               Mockito.any(TenantContext.class)))
               .thenReturn(versionedCatalog);
        final StaticCatalog catalog = Mockito.mock(StaticCatalog.class);
        Mockito.when(versionedCatalog.getVersion(Mockito.any(Date.class))).thenReturn(catalog);
        Mockito.when(catalog.findPlan(Mockito.eq(plan.getName()))).thenReturn(plan);

        Mockito.when(tagUserApi.getTagsForObject(Mockito.<UUID>any(), Mockito.<ObjectType>any(), Mockito.anyBoolean(), Mockito.<TenantContext>any())).thenReturn(ImmutableList.<Tag>of());

        final TenantUserApi tenantUserApi = Mockito.mock(TenantUserApi.class);

        Mockito.when(killbillAPI.getAccountUserApi()).thenReturn(accountUserApi);
        Mockito.when(killbillAPI.getSubscriptionApi()).thenReturn(subscriptionApi);
        Mockito.when(killbillAPI.getInvoiceUserApi()).thenReturn(invoiceApi);
        Mockito.when(killbillAPI.getPaymentApi()).thenReturn(paymentApi);
        Mockito.when(killbillAPI.getInvoicePaymentApi()).thenReturn(invoicePaymentApi);
        Mockito.when(killbillAPI.getRecordIdApi()).thenReturn(recordIdApi);
        Mockito.when(killbillAPI.getTagUserApi()).thenReturn(tagUserApi);
        Mockito.when(killbillAPI.getCustomFieldUserApi()).thenReturn(customFieldUserApi);
        Mockito.when(killbillAPI.getAuditUserApi()).thenReturn(auditUserApi);
        Mockito.when(killbillAPI.getCatalogUserApi()).thenReturn(catalogUserApi);
        Mockito.when(killbillAPI.getTenantUserApi()).thenReturn(tenantUserApi);

        killbillDataSource = Mockito.mock(OSGIKillbillDataSource.class);
        final DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(killbillDataSource.getDataSource()).thenReturn(dataSource);

        final Properties properties = System.getProperties();
        properties.setProperty("org.killbill.notificationq.analytics.tableName", "analytics_notifications");
        properties.setProperty("org.killbill.notificationq.analytics.historyTableName", "analytics_notifications_history");

        osgiConfigPropertiesService = Mockito.mock(OSGIConfigPropertiesService.class);
        Mockito.when(osgiConfigPropertiesService.getProperties()).thenReturn(properties);
        Mockito.when(osgiConfigPropertiesService.getString(Mockito.<String>any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) {
                return properties.getProperty((String) invocation.getArguments()[0]);
            }
        });

        executor = BusinessExecutor.newCachedThreadPool(osgiConfigPropertiesService);

        analyticsConfigurationHandler = new AnalyticsConfigurationHandler(null, AnalyticsActivator.PLUGIN_NAME, killbillAPI);
        final AnalyticsConfiguration defaultConfigurable = new AnalyticsConfiguration();
        defaultConfigurable.blacklist.add(UUID.randomUUID().toString());
        defaultConfigurable.blacklist.add(blackListedAccountId.toString());
        analyticsConfigurationHandler.setDefaultConfigurable(defaultConfigurable);

        businessContextFactory = new BusinessContextFactory(account.getId(),
                                                            callContext,
                                                            currencyConversionDao,
                                                            killbillAPI,
                                                            osgiConfigPropertiesService,
                                                            clock,
                                                            analyticsConfigurationHandler);
    }
}
