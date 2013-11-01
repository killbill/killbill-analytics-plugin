/*
 * Copyright 2010-2013 Ning, Inc.
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

package com.ning.billing.osgi.bundles.analytics.dao.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.ning.billing.account.api.Account;
import com.ning.billing.clock.Clock;
import com.ning.billing.entitlement.api.SubscriptionBundle;
import com.ning.billing.entitlement.api.SubscriptionEvent;
import com.ning.billing.osgi.bundles.analytics.AnalyticsRefreshException;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessSubscription;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessSubscriptionEvent;
import com.ning.billing.osgi.bundles.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import com.ning.billing.osgi.bundles.analytics.utils.CurrencyConverter;
import com.ning.billing.util.audit.AuditLog;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillLogService;

import com.google.common.annotations.VisibleForTesting;

public class BusinessSubscriptionTransitionFactory extends BusinessFactoryBase {

    // See EntitlementService
    public static final String ENTITLEMENT_SERVICE_NAME = "entitlement-service";
    // See DefaultSubscriptionBundleTimeline
    public static final String BILLING_SERVICE_NAME = "billing-service";
    public static final String ENTITLEMENT_BILLING_SERVICE_NAME = "entitlement+billing-service";

    public BusinessSubscriptionTransitionFactory(final OSGIKillbillLogService logService,
                                                 final OSGIKillbillAPI osgiKillbillAPI,
                                                 final OSGIKillbillDataSource osgiKillbillDataSource,
                                                 final Clock clock) {
        super(logService, osgiKillbillAPI, osgiKillbillDataSource, clock);
    }

    public Collection<BusinessSubscriptionTransitionModelDao> createBusinessSubscriptionTransitions(final UUID accountId,
                                                                                                    final Long accountRecordId,
                                                                                                    final Long tenantRecordId,
                                                                                                    final CallContext context) throws AnalyticsRefreshException {
        final Account account = getAccount(accountId, context);
        final ReportGroup reportGroup = getReportGroup(account.getId(), context);
        final CurrencyConverter currencyConverter = getCurrencyConverter();

        final List<SubscriptionBundle> bundles = getSubscriptionBundlesForAccount(account.getId(), context);

        final Collection<BusinessSubscriptionTransitionModelDao> bsts = new LinkedList<BusinessSubscriptionTransitionModelDao>();
        for (final SubscriptionBundle bundle : bundles) {
            bsts.addAll(buildTransitionsForBundle(account, bundle, currencyConverter, accountRecordId, tenantRecordId, reportGroup, context));
        }

        return bsts;
    }

    private Collection<BusinessSubscriptionTransitionModelDao> buildTransitionsForBundle(final Account account,
                                                                                         final SubscriptionBundle bundle,
                                                                                         final CurrencyConverter currencyConverter,
                                                                                         final Long accountRecordId,
                                                                                         final Long tenantRecordId,
                                                                                         @Nullable final ReportGroup reportGroup,
                                                                                         final CallContext context) throws AnalyticsRefreshException {
        final List<SubscriptionEvent> transitions = bundle.getTimeline().getSubscriptionEvents();
        return buildTransitionsForBundle(account, bundle, transitions, currencyConverter, accountRecordId, tenantRecordId, reportGroup, context);
    }

    @VisibleForTesting
    Collection<BusinessSubscriptionTransitionModelDao> buildTransitionsForBundle(final Account account,
                                                                                 final SubscriptionBundle bundle,
                                                                                 final List<SubscriptionEvent> transitions,
                                                                                 final CurrencyConverter currencyConverter,
                                                                                 final Long accountRecordId,
                                                                                 final Long tenantRecordId,
                                                                                 @Nullable final ReportGroup reportGroup,
                                                                                 final CallContext context) throws AnalyticsRefreshException {
        final List<BusinessSubscriptionTransitionModelDao> bsts = new LinkedList<BusinessSubscriptionTransitionModelDao>();
        final Map<String, Map<UUID, List<BusinessSubscriptionTransitionModelDao>>> bstsPerServicePerSubscription = new HashMap<String, Map<UUID, List<BusinessSubscriptionTransitionModelDao>>>();
        final Map<String, Map<UUID, BusinessSubscription>> prevSubscriptionPerServicePerSubscription = new HashMap<String, Map<UUID, BusinessSubscription>>();

        // Ordered for us by entitlement
        for (final SubscriptionEvent transition : transitions) {
            final BusinessSubscription nextSubscription;
            // Special service, will be multiplexed. These events are only for PHASE/CHANGE: since Kill Bill doesn't support different
            // entitlement and billing dates for PHASE/CHANGE, we will only see one event in the subscription bundle timeline stream
            // but we want two in Analytics for convenience. All other events will have their own entitlement and billing events
            // already (coming either from subscription base or blocking states).
            if (ENTITLEMENT_BILLING_SERVICE_NAME.equals(transition.getServiceName())) {
                nextSubscription = getBusinessSubscriptionFromTransition(account, transition, ENTITLEMENT_SERVICE_NAME, currencyConverter);
            } else {
                nextSubscription = getBusinessSubscriptionFromTransition(account, transition, currencyConverter);
            }
            createBusinessSubscriptionTransition(transition, bsts, bstsPerServicePerSubscription, prevSubscriptionPerServicePerSubscription, nextSubscription, account, bundle, currencyConverter, accountRecordId, tenantRecordId, reportGroup, context);

            // Multiplex these events
            if (transition.getServiceName().equals(ENTITLEMENT_BILLING_SERVICE_NAME)) {
                final BusinessSubscription nextNextSubscription = getBusinessSubscriptionFromTransition(account, transition, BILLING_SERVICE_NAME, currencyConverter);
                createBusinessSubscriptionTransition(transition, bsts, bstsPerServicePerSubscription, prevSubscriptionPerServicePerSubscription, nextNextSubscription, account, bundle, currencyConverter, accountRecordId, tenantRecordId, reportGroup, context);
            }
        }

        // We can now fix the next end date (the last next_end date will be set by the catalog by using the phase name)
        final Map<String, Map<UUID, Iterator<BusinessSubscriptionTransitionModelDao>>> iteratorPerServicePerSubscription = new HashMap<String, Map<UUID, Iterator<BusinessSubscriptionTransitionModelDao>>>();
        for (final BusinessSubscriptionTransitionModelDao bst : bsts) {
            Map<UUID, Iterator<BusinessSubscriptionTransitionModelDao>> iteratorPerSubscription = iteratorPerServicePerSubscription.get(bst.getNextService());
            if (iteratorPerSubscription == null) {
                iteratorPerSubscription = new HashMap<UUID, Iterator<BusinessSubscriptionTransitionModelDao>>();
                iteratorPerServicePerSubscription.put(bst.getNextService(), iteratorPerSubscription);
            }

            Iterator<BusinessSubscriptionTransitionModelDao> bstIterator = iteratorPerSubscription.get(bst.getSubscriptionId());
            if (bstIterator == null) {
                bstIterator = bstsPerServicePerSubscription.get(bst.getNextService()).get(bst.getSubscriptionId()).iterator();
                // Skip the first one
                bstIterator.next();
                iteratorPerSubscription.put(bst.getSubscriptionId(), bstIterator);
            }

            if (bstIterator.hasNext()) {
                final BusinessSubscriptionTransitionModelDao nextBstPerService = bstIterator.next();
                bst.setNextEndDate(nextBstPerService.getNextStartDate());
            }
        }

        return bsts;
    }

    private void createBusinessSubscriptionTransition(final SubscriptionEvent transition,
                                                      final Collection<BusinessSubscriptionTransitionModelDao> bsts,
                                                      final Map<String, Map<UUID, List<BusinessSubscriptionTransitionModelDao>>> bstsPerServicePerSubscription,
                                                      final Map<String, Map<UUID, BusinessSubscription>> prevSubscriptionPerServicePerSubscription,
                                                      final BusinessSubscription nextSubscription,
                                                      final Account account,
                                                      final SubscriptionBundle bundle,
                                                      final CurrencyConverter currencyConverter,
                                                      final Long accountRecordId,
                                                      final Long tenantRecordId,
                                                      @Nullable final ReportGroup reportGroup,
                                                      final CallContext context) throws AnalyticsRefreshException {
        Map<UUID, BusinessSubscription> prevSubscriptionPerSubscription = prevSubscriptionPerServicePerSubscription.get(nextSubscription.getService());
        if (prevSubscriptionPerSubscription == null) {
            prevSubscriptionPerSubscription = new HashMap<UUID, BusinessSubscription>();
            prevSubscriptionPerServicePerSubscription.put(nextSubscription.getService(), prevSubscriptionPerSubscription);
        }

        final BusinessSubscriptionTransitionModelDao bst = createBusinessSubscriptionTransition(account,
                                                                                                bundle,
                                                                                                transition,
                                                                                                prevSubscriptionPerSubscription.get(transition.getEntitlementId()),
                                                                                                nextSubscription,
                                                                                                currencyConverter,
                                                                                                accountRecordId,
                                                                                                tenantRecordId,
                                                                                                reportGroup,
                                                                                                context);
        bsts.add(bst);

        Map<UUID, List<BusinessSubscriptionTransitionModelDao>> bstsPerSubscription = bstsPerServicePerSubscription.get(nextSubscription.getService());
        if (bstsPerSubscription == null) {
            bstsPerSubscription = new HashMap<UUID, List<BusinessSubscriptionTransitionModelDao>>();
            bstsPerServicePerSubscription.put(nextSubscription.getService(), bstsPerSubscription);
        }
        if (bstsPerSubscription.get(transition.getEntitlementId()) == null) {
            bstsPerSubscription.put(transition.getEntitlementId(), new LinkedList<BusinessSubscriptionTransitionModelDao>());
        }
        bstsPerSubscription.get(transition.getEntitlementId()).add(bst);

        prevSubscriptionPerSubscription.put(transition.getEntitlementId(), nextSubscription);
    }

    private BusinessSubscriptionTransitionModelDao createBusinessSubscriptionTransition(final Account account,
                                                                                        final SubscriptionBundle subscriptionBundle,
                                                                                        final SubscriptionEvent subscriptionTransition,
                                                                                        @Nullable final BusinessSubscription prevNextSubscription,
                                                                                        final BusinessSubscription nextSubscription,
                                                                                        final CurrencyConverter currencyConverter,
                                                                                        final Long accountRecordId,
                                                                                        final Long tenantRecordId,
                                                                                        @Nullable final ReportGroup reportGroup,
                                                                                        final CallContext context) throws AnalyticsRefreshException {
        final BusinessSubscriptionEvent businessEvent = BusinessSubscriptionEvent.fromTransition(subscriptionTransition);
        final Long subscriptionEventRecordId = getSubscriptionEventRecordId(subscriptionTransition.getId(), subscriptionTransition.getSubscriptionEventType().getObjectType(), context);
        final AuditLog creationAuditLog = getSubscriptionEventCreationAuditLog(subscriptionTransition.getId(), subscriptionTransition.getSubscriptionEventType().getObjectType(), context);

        return new BusinessSubscriptionTransitionModelDao(account,
                                                          accountRecordId,
                                                          subscriptionBundle,
                                                          subscriptionTransition,
                                                          subscriptionEventRecordId,
                                                          businessEvent,
                                                          prevNextSubscription,
                                                          nextSubscription,
                                                          currencyConverter,
                                                          creationAuditLog,
                                                          tenantRecordId,
                                                          reportGroup);
    }

    private BusinessSubscription getBusinessSubscriptionFromTransition(final Account account,
                                                                       final SubscriptionEvent subscriptionTransition,
                                                                       final CurrencyConverter currencyConverter) {
        return getBusinessSubscriptionFromTransition(account, subscriptionTransition, subscriptionTransition.getServiceName(), currencyConverter);
    }

    private BusinessSubscription getBusinessSubscriptionFromTransition(final Account account,
                                                                       final SubscriptionEvent subscriptionTransition,
                                                                       final String serviceName,
                                                                       final CurrencyConverter currencyConverter) {
        return new BusinessSubscription(subscriptionTransition.getNextPlan(),
                                        subscriptionTransition.getNextPhase(),
                                        subscriptionTransition.getNextPriceList(),
                                        account.getCurrency(),
                                        subscriptionTransition.getEffectiveDate(),
                                        // We don't record the blockedBilling/blockedEntitlement values
                                        // as they are implicitly reflected in the subscription transition event name
                                        // Note: we don't have information on blocked changes though
                                        serviceName,
                                        subscriptionTransition.getServiceStateName(),
                                        currencyConverter);
    }
}
