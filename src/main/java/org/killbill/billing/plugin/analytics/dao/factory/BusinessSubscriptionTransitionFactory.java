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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.killbill.billing.account.api.Account;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.entitlement.api.SubscriptionEvent;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscription;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionEvent;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AuditLog;

import com.google.common.annotations.VisibleForTesting;

public class BusinessSubscriptionTransitionFactory {

    // See EntitlementService
    public static final String ENTITLEMENT_SERVICE_NAME = "entitlement-service";
    // See DefaultSubscriptionBundleTimeline
    public static final String BILLING_SERVICE_NAME = "billing-service";
    public static final String ENTITLEMENT_BILLING_SERVICE_NAME = "entitlement+billing-service";

    public Collection<BusinessSubscriptionTransitionModelDao> createBusinessSubscriptionTransitions(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        final Account account = businessContextFactory.getAccount();
        final Long accountRecordId = businessContextFactory.getAccountRecordId();
        final Long tenantRecordId = businessContextFactory.getTenantRecordId();
        final ReportGroup reportGroup = businessContextFactory.getReportGroup();
        final CurrencyConverter currencyConverter = businessContextFactory.getCurrencyConverter();

        final Iterable<SubscriptionBundle> bundles = businessContextFactory.getAccountBundles();

        final Collection<BusinessSubscriptionTransitionModelDao> bsts = new LinkedList<BusinessSubscriptionTransitionModelDao>();
        for (final SubscriptionBundle bundle : bundles) {
            bsts.addAll(buildTransitionsForBundle(businessContextFactory, account, bundle, currencyConverter, accountRecordId, tenantRecordId, reportGroup));
        }

        return bsts;
    }

    private Collection<BusinessSubscriptionTransitionModelDao> buildTransitionsForBundle(final BusinessContextFactory businessContextFactory,
                                                                                         final Account account,
                                                                                         final SubscriptionBundle bundle,
                                                                                         final CurrencyConverter currencyConverter,
                                                                                         final Long accountRecordId,
                                                                                         final Long tenantRecordId,
                                                                                         @Nullable final ReportGroup reportGroup) throws AnalyticsRefreshException {
        final List<SubscriptionEvent> transitions = bundle.getTimeline().getSubscriptionEvents();
        return buildTransitionsForBundle(businessContextFactory, account, bundle, transitions, currencyConverter, accountRecordId, tenantRecordId, reportGroup);
    }

    @VisibleForTesting
    Collection<BusinessSubscriptionTransitionModelDao> buildTransitionsForBundle(final BusinessContextFactory businessContextFactory,
                                                                                 final Account account,
                                                                                 final SubscriptionBundle bundle,
                                                                                 final List<SubscriptionEvent> transitions,
                                                                                 final CurrencyConverter currencyConverter,
                                                                                 final Long accountRecordId,
                                                                                 final Long tenantRecordId,
                                                                                 @Nullable final ReportGroup reportGroup) throws AnalyticsRefreshException {
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
            createBusinessSubscriptionTransition(businessContextFactory, transition, bsts, bstsPerServicePerSubscription, prevSubscriptionPerServicePerSubscription, nextSubscription, account, bundle, currencyConverter, accountRecordId, tenantRecordId, reportGroup);

            // Multiplex these events
            if (transition.getServiceName().equals(ENTITLEMENT_BILLING_SERVICE_NAME)) {
                final BusinessSubscription nextNextSubscription = getBusinessSubscriptionFromTransition(account, transition, BILLING_SERVICE_NAME, currencyConverter);
                createBusinessSubscriptionTransition(businessContextFactory, transition, bsts, bstsPerServicePerSubscription, prevSubscriptionPerServicePerSubscription, nextNextSubscription, account, bundle, currencyConverter, accountRecordId, tenantRecordId, reportGroup);
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

    private void createBusinessSubscriptionTransition(final BusinessContextFactory businessContextFactory,
                                                      final SubscriptionEvent transition,
                                                      final Collection<BusinessSubscriptionTransitionModelDao> bsts,
                                                      final Map<String, Map<UUID, List<BusinessSubscriptionTransitionModelDao>>> bstsPerServicePerSubscription,
                                                      final Map<String, Map<UUID, BusinessSubscription>> prevSubscriptionPerServicePerSubscription,
                                                      final BusinessSubscription nextSubscription,
                                                      final Account account,
                                                      final SubscriptionBundle bundle,
                                                      final CurrencyConverter currencyConverter,
                                                      final Long accountRecordId,
                                                      final Long tenantRecordId,
                                                      @Nullable final ReportGroup reportGroup) throws AnalyticsRefreshException {
        Map<UUID, BusinessSubscription> prevSubscriptionPerSubscription = prevSubscriptionPerServicePerSubscription.get(nextSubscription.getService());
        if (prevSubscriptionPerSubscription == null) {
            prevSubscriptionPerSubscription = new HashMap<UUID, BusinessSubscription>();
            prevSubscriptionPerServicePerSubscription.put(nextSubscription.getService(), prevSubscriptionPerSubscription);
        }

        final BusinessSubscriptionTransitionModelDao bst = createBusinessSubscriptionTransition(businessContextFactory,
                                                                                                account,
                                                                                                bundle,
                                                                                                transition,
                                                                                                prevSubscriptionPerSubscription.get(transition.getEntitlementId()),
                                                                                                nextSubscription,
                                                                                                currencyConverter,
                                                                                                accountRecordId,
                                                                                                tenantRecordId,
                                                                                                reportGroup
                                                                                               );
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

    private BusinessSubscriptionTransitionModelDao createBusinessSubscriptionTransition(final BusinessContextFactory businessContextFactory,
                                                                                        final Account account,
                                                                                        final SubscriptionBundle subscriptionBundle,
                                                                                        final SubscriptionEvent subscriptionTransition,
                                                                                        @Nullable final BusinessSubscription prevNextSubscription,
                                                                                        final BusinessSubscription nextSubscription,
                                                                                        final CurrencyConverter currencyConverter,
                                                                                        final Long accountRecordId,
                                                                                        final Long tenantRecordId,
                                                                                        @Nullable final ReportGroup reportGroup) throws AnalyticsRefreshException {
        final BusinessSubscriptionEvent businessEvent = BusinessSubscriptionEvent.fromTransition(subscriptionTransition);
        final Long subscriptionEventRecordId = businessContextFactory.getSubscriptionEventRecordId(subscriptionTransition.getId(), subscriptionTransition.getSubscriptionEventType().getObjectType());
        final AuditLog creationAuditLog = businessContextFactory.getSubscriptionEventCreationAuditLog(subscriptionTransition.getId(), subscriptionTransition.getSubscriptionEventType().getObjectType());

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
