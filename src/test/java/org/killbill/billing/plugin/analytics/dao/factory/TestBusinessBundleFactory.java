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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.Product;
import org.killbill.billing.catalog.api.ProductCategory;
import org.killbill.billing.entitlement.api.Subscription;
import org.killbill.billing.entitlement.api.SubscriptionApi;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.entitlement.api.SubscriptionEvent;
import org.killbill.billing.entitlement.api.SubscriptionEventType;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.BusinessExecutor;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscription;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionEvent;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import org.killbill.billing.util.callcontext.TenantContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

public class TestBusinessBundleFactory extends AnalyticsTestSuiteNoDB {

    private BusinessBundleFactory bundleFactory;
    private BusinessSubscriptionTransitionFactory subscriptionFactory;
    private BusinessContextFactory businessContextFactory;

    @Override
    @BeforeMethod(groups = "fast")
    public void setUp() throws Exception {
        super.setUp();

        final OSGIKillbillDataSource osgiKillbillDataSource = Mockito.mock(OSGIKillbillDataSource.class);

        final DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(osgiKillbillDataSource.getDataSource()).thenReturn(dataSource);

        bundleFactory = new BusinessBundleFactory(BusinessExecutor.newCachedThreadPool(osgiConfigPropertiesService));

        final UUID subscriptionId = UUID.randomUUID();
        final Subscription subscription = Mockito.mock(Subscription.class);
        Mockito.when(subscription.getBaseEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(subscription.getId()).thenReturn(subscriptionId);

        Mockito.when(bundle.getSubscriptions()).thenReturn(ImmutableList.<Subscription>of(subscription));

        final SubscriptionApi subscriptionApi = killbillAPI.getSubscriptionApi();
        Mockito.when(subscriptionApi.getSubscriptionBundlesForAccountId(Mockito.<UUID>any(), Mockito.<TenantContext>any())).thenReturn(ImmutableList.<SubscriptionBundle>of(bundle));
        Mockito.when(subscriptionApi.getSubscriptionBundlesForExternalKey(Mockito.<String>any(), Mockito.<TenantContext>any())).thenReturn(ImmutableList.<SubscriptionBundle>of(bundle));

        businessContextFactory = new BusinessContextFactory(account.getId(), callContext, currencyConversionDao, killbillAPI, osgiConfigPropertiesService, clock, analyticsConfigurationHandler);
        subscriptionFactory = new BusinessSubscriptionTransitionFactory();
    }

    @Test(groups = "fast", description = "https://github.com/killbill/killbill-analytics-plugin/issues/89")
    public void testBundleStarted() throws AnalyticsRefreshException {
        final UUID subscriptionId = bundle.getSubscriptions().get(0).getId();

        final Product product = Mockito.mock(Product.class);
        Mockito.when(product.getCatalogName()).thenReturn("NotSureWhyItsNeeded");
        Mockito.when(product.getCategory()).thenReturn(ProductCategory.BASE);
        final Plan plan = Mockito.mock(Plan.class);
        Mockito.when(plan.getProduct()).thenReturn(product);

        final List<SubscriptionEvent> events = new LinkedList<SubscriptionEvent>();
        // Start entitlement
        final SubscriptionEvent event1 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event1.getEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(event1.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_ENTITLEMENT);
        Mockito.when(event1.getNextPlan()).thenReturn(plan);
        Mockito.when(event1.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1));
        Mockito.when(event1.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        events.add(event1);
        // Start billing
        final SubscriptionEvent event2 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event2.getEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(event2.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_BILLING);
        Mockito.when(event2.getNextPlan()).thenReturn(plan);
        Mockito.when(event2.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1));
        Mockito.when(event2.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        events.add(event2);

        final List<BusinessSubscriptionTransitionModelDao> result = ImmutableList.<BusinessSubscriptionTransitionModelDao>copyOf(subscriptionFactory.buildTransitionsForBundle(businessContextFactory, account, bundle, events, currencyConverter, accountRecordId, tenantRecordId, ReportGroup.test));
        Assert.assertEquals(result.size(), 2);

        Assert.assertEquals(result.get(0).getEvent(), "START_ENTITLEMENT_BASE");
        Assert.assertEquals(result.get(0).getSubscriptionId(), subscriptionId);
        Assert.assertNull(result.get(0).getPrevStartDate());
        Assert.assertNull(result.get(0).getPrevService());
        Assert.assertEquals(result.get(0).getNextStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(0).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertNull(result.get(0).getNextEndDate());

        Assert.assertEquals(result.get(1).getEvent(), "START_BILLING_BASE");
        Assert.assertEquals(result.get(1).getSubscriptionId(), subscriptionId);
        Assert.assertNull(result.get(1).getPrevStartDate());
        Assert.assertNull(result.get(1).getPrevService());
        Assert.assertEquals(result.get(1).getNextStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(1).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertNull(result.get(1).getNextEndDate());

        final Collection<BusinessBundleModelDao> bundles = bundleFactory.createBusinessBundles(false, businessContextFactory, result);
        Assert.assertEquals(bundles.size(), 1);
        final BusinessBundleModelDao bBundle = bundles.iterator().next();
        Assert.assertEquals(bBundle.getCurrentStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertNull(bBundle.getCurrentEndDate());
    }

    @Test(groups = "fast", description = "https://github.com/killbill/killbill-analytics-plugin/issues/89")
    public void testBundleCancelled() throws AnalyticsRefreshException {
        final UUID subscriptionId = bundle.getSubscriptions().get(0).getId();

        final Product product = Mockito.mock(Product.class);
        Mockito.when(product.getCatalogName()).thenReturn("NotSureWhyItsNeeded");
        Mockito.when(product.getCategory()).thenReturn(ProductCategory.BASE);
        final Plan plan = Mockito.mock(Plan.class);
        Mockito.when(plan.getProduct()).thenReturn(product);

        final List<SubscriptionEvent> events = new LinkedList<SubscriptionEvent>();
        // Start entitlement
        final SubscriptionEvent event1 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event1.getEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(event1.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_ENTITLEMENT);
        Mockito.when(event1.getNextPlan()).thenReturn(plan);
        Mockito.when(event1.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1));
        Mockito.when(event1.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        events.add(event1);
        // Start billing
        final SubscriptionEvent event2 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event2.getEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(event2.getSubscriptionEventType()).thenReturn(SubscriptionEventType.START_BILLING);
        Mockito.when(event2.getNextPlan()).thenReturn(plan);
        Mockito.when(event2.getEffectiveDate()).thenReturn(new LocalDate(2012, 5, 1));
        Mockito.when(event2.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        events.add(event2);
        // Cancel entitlement
        final SubscriptionEvent event3 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event3.getEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(event3.getSubscriptionEventType()).thenReturn(SubscriptionEventType.STOP_ENTITLEMENT);
        Mockito.when(event3.getPrevPlan()).thenReturn(plan);
        Mockito.when(event3.getEffectiveDate()).thenReturn(new LocalDate(2012, 6, 1));
        Mockito.when(event3.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        events.add(event3);
        // Cancel billing
        final SubscriptionEvent event4 = Mockito.mock(SubscriptionEvent.class);
        Mockito.when(event4.getEntitlementId()).thenReturn(subscriptionId);
        Mockito.when(event4.getSubscriptionEventType()).thenReturn(SubscriptionEventType.STOP_BILLING);
        Mockito.when(event4.getPrevPlan()).thenReturn(plan);
        Mockito.when(event4.getEffectiveDate()).thenReturn(new LocalDate(2012, 6, 1));
        Mockito.when(event4.getServiceName()).thenReturn(BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        events.add(event4);

        final List<BusinessSubscriptionTransitionModelDao> result = ImmutableList.<BusinessSubscriptionTransitionModelDao>copyOf(subscriptionFactory.buildTransitionsForBundle(businessContextFactory, account, bundle, events, currencyConverter, accountRecordId, tenantRecordId, ReportGroup.test));
        Assert.assertEquals(result.size(), 4);

        Assert.assertEquals(result.get(0).getEvent(), "START_ENTITLEMENT_BASE");
        Assert.assertEquals(result.get(0).getSubscriptionId(), subscriptionId);
        Assert.assertNull(result.get(0).getPrevStartDate());
        Assert.assertNull(result.get(0).getPrevService());
        Assert.assertEquals(result.get(0).getNextStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(0).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(0).getNextEndDate(), new LocalDate(2012, 6, 1));

        Assert.assertEquals(result.get(1).getEvent(), "START_BILLING_BASE");
        Assert.assertEquals(result.get(1).getSubscriptionId(), subscriptionId);
        Assert.assertNull(result.get(1).getPrevStartDate());
        Assert.assertNull(result.get(1).getPrevService());
        Assert.assertEquals(result.get(1).getNextStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(1).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(1).getNextEndDate(), new LocalDate(2012, 6, 1));

        Assert.assertEquals(result.get(2).getEvent(), "STOP_ENTITLEMENT_BASE");
        Assert.assertEquals(result.get(2).getPrevStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(2).getPrevService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertEquals(result.get(2).getNextStartDate(), new LocalDate(2012, 6, 1));
        Assert.assertEquals(result.get(2).getNextService(), BusinessSubscriptionTransitionFactory.ENTITLEMENT_SERVICE_NAME);
        Assert.assertNull(result.get(2).getNextEndDate());

        Assert.assertEquals(result.get(3).getEvent(), "STOP_BILLING_BASE");
        Assert.assertEquals(result.get(3).getPrevStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(result.get(3).getPrevService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertEquals(result.get(3).getNextStartDate(), new LocalDate(2012, 6, 1));
        Assert.assertEquals(result.get(3).getNextService(), BusinessSubscriptionTransitionFactory.BILLING_SERVICE_NAME);
        Assert.assertNull(result.get(3).getNextEndDate());

        final Collection<BusinessBundleModelDao> bundles = bundleFactory.createBusinessBundles(false, businessContextFactory, result);
        Assert.assertEquals(bundles.size(), 1);
        final BusinessBundleModelDao bBundle = bundles.iterator().next();
        Assert.assertEquals(bBundle.getCurrentStartDate(), new LocalDate(2012, 5, 1));
        Assert.assertEquals(bBundle.getCurrentEndDate(), new LocalDate(2012, 6, 1));
    }

    @Test(groups = "fast")
    public void testFilterBsts() throws Exception {
        final UUID bundleId1 = UUID.randomUUID();
        final UUID subscriptionId1 = UUID.randomUUID();
        final LocalDate bundle1StartDate = new LocalDate(2012, 1, 1);
        final LocalDate bundle1PhaseDate = new LocalDate(2012, 2, 2);
        final UUID bundleId2 = UUID.randomUUID();
        final UUID subscriptionId2 = UUID.randomUUID();
        final LocalDate bundle2StartDate = new LocalDate(2012, 2, 1);
        final LocalDate bundle2PhaseDate = new LocalDate(2012, 3, 2);
        final UUID bundleId3 = UUID.randomUUID();
        final UUID subscriptionId3 = UUID.randomUUID();
        final LocalDate bundle3StartDate = new LocalDate(2012, 3, 1);

        // Real order is: bundleId1 START_ENTITLEMENT_BASE, bundleId2 START_ENTITLEMENT_BASE, bundleId1 SYSTEM_CHANGE_BASE, bundleId3 START_ENTITLEMENT_BASE bundleId2 SYSTEM_CHANGE_BASE
        final Collection<BusinessSubscriptionTransitionModelDao> bsts = ImmutableList.<BusinessSubscriptionTransitionModelDao>of(
                createBst(bundleId1, subscriptionId1, "START_ENTITLEMENT_BASE", bundle1StartDate),
                createBst(bundleId1, subscriptionId1, "SYSTEM_CHANGE_BASE", bundle1PhaseDate),
                createBst(bundleId2, subscriptionId2, "START_ENTITLEMENT_BASE", bundle2StartDate),
                createBst(bundleId2, subscriptionId2, "SYSTEM_CHANGE_BASE", bundle2PhaseDate),
                createBst(bundleId3, subscriptionId3, "START_ENTITLEMENT_BASE", bundle3StartDate),
                createBst(UUID.randomUUID(), UUID.randomUUID(), "START_ENTITLEMENT_ADD_ON", new LocalDate(DateTimeZone.UTC))
                                                                                                                                );

        final Map<UUID, Integer> rankForBundle = new LinkedHashMap<UUID, Integer>();
        final Map<UUID, BusinessSubscriptionTransitionModelDao> bstForBundle = new LinkedHashMap<UUID, BusinessSubscriptionTransitionModelDao>();
        bundleFactory.filterBstsForBasePlans(bsts, rankForBundle, bstForBundle);

        final List<BusinessSubscriptionTransitionModelDao> filteredBsts = ImmutableList.<BusinessSubscriptionTransitionModelDao>copyOf(bstForBundle.values());
        Assert.assertEquals(filteredBsts.size(), 3);

        Assert.assertEquals(filteredBsts.get(0).getBundleId(), bundleId1);
        Assert.assertEquals(filteredBsts.get(0).getNextStartDate(), bundle1PhaseDate);
        Assert.assertEquals(filteredBsts.get(1).getBundleId(), bundleId2);
        Assert.assertEquals(filteredBsts.get(1).getNextStartDate(), bundle2PhaseDate);
        Assert.assertEquals(filteredBsts.get(2).getBundleId(), bundleId3);
        Assert.assertEquals(filteredBsts.get(2).getNextStartDate(), bundle3StartDate);
    }

    private BusinessSubscriptionTransitionModelDao createBst(final UUID bundleId, final UUID subscriptionId, final String eventString, final LocalDate startDate) {
        final SubscriptionBundle bundle = Mockito.mock(SubscriptionBundle.class);
        Mockito.when(bundle.getId()).thenReturn(bundleId);

        final BusinessSubscriptionEvent event = BusinessSubscriptionEvent.valueOf(eventString);
        final BusinessSubscription previousSubscription = null; // We don't look at it

        final Product product = Mockito.mock(Product.class);
        Mockito.when(product.getCategory()).thenReturn(event.getCategory());
        final Plan plan = Mockito.mock(Plan.class);
        Mockito.when(plan.getProduct()).thenReturn(product);
        final BusinessSubscription nextSubscription = new BusinessSubscription(plan,
                                                                               null,
                                                                               null,
                                                                               Currency.GBP,
                                                                               startDate,
                                                                               serviceName,
                                                                               stateName,
                                                                               currencyConverter);

        Mockito.when(subscriptionTransition.getEntitlementId()).thenReturn(subscriptionId);
        return new BusinessSubscriptionTransitionModelDao(account,
                                                          accountRecordId,
                                                          bundle,
                                                          subscriptionTransition,
                                                          subscriptionEventRecordId,
                                                          event,
                                                          previousSubscription,
                                                          nextSubscription,
                                                          currencyConverter,
                                                          auditLog,
                                                          tenantRecordId,
                                                          reportGroup);
    }
}
