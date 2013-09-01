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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

public class BusinessSubscriptionTransitionFactory extends BusinessFactoryBase {

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
        final Collection<BusinessSubscriptionTransitionModelDao> bsts = new LinkedList<BusinessSubscriptionTransitionModelDao>();

        final List<SubscriptionEvent> transitions = bundle.getTimeline().getSubscriptionEvents();

        BusinessSubscription prevNextSubscription = null;

        // Ordered for us by entitlement
        for (final SubscriptionEvent transition : transitions) {
            final BusinessSubscription nextSubscription = getBusinessSubscriptionFromTransition(account, transition, currencyConverter);
            final BusinessSubscriptionTransitionModelDao bst = createBusinessSubscriptionTransition(account,
                                                                                                    accountRecordId,
                                                                                                    bundle,
                                                                                                    transition,
                                                                                                    prevNextSubscription,
                                                                                                    nextSubscription,
                                                                                                    currencyConverter,
                                                                                                    tenantRecordId,
                                                                                                    reportGroup,
                                                                                                    context);
            if (bst != null) {
                bsts.add(bst);
                prevNextSubscription = nextSubscription;
            }
        }

        // We can now fix the next end date (the last next_end date will be set by the catalog by using the phase name)
        final Iterator<BusinessSubscriptionTransitionModelDao> bstIterator = bsts.iterator();
        if (bstIterator.hasNext()) {
            BusinessSubscriptionTransitionModelDao prevBst = bstIterator.next();

            while (bstIterator.hasNext()) {
                final BusinessSubscriptionTransitionModelDao nextBst = bstIterator.next();
                prevBst.setNextEndDate(nextBst.getNextStartDate());
                prevBst = nextBst;
            }
        }

        return bsts;
    }

    private BusinessSubscriptionTransitionModelDao createBusinessSubscriptionTransition(final Account account,
                                                                                        final Long accountRecordId,
                                                                                        final SubscriptionBundle subscriptionBundle,
                                                                                        final SubscriptionEvent subscriptionTransition,
                                                                                        @Nullable final BusinessSubscription prevNextSubscription,
                                                                                        final BusinessSubscription nextSubscription,
                                                                                        final CurrencyConverter currencyConverter,
                                                                                        final Long tenantRecordId,
                                                                                        @Nullable final ReportGroup reportGroup,
                                                                                        final CallContext context) throws AnalyticsRefreshException {
        final BusinessSubscriptionEvent businessEvent = BusinessSubscriptionEvent.fromTransition(subscriptionTransition);
        if (businessEvent == null) {
            return null;
        }

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
        return new BusinessSubscription(subscriptionTransition.getNextPlan(),
                                        subscriptionTransition.getNextPhase(),
                                        subscriptionTransition.getNextPriceList(),
                                        account.getCurrency(),
                                        subscriptionTransition.getEffectiveDate(),
                                        // We don't record the blockedBilling/blockedEntitlement values
                                        // as they are implicitly reflected in the subscription transition event name
                                        // Note: we don't have information on blocked changes though
                                        subscriptionTransition.getServiceName(),
                                        subscriptionTransition.getServiceStateName(),
                                        currencyConverter);
    }
}
