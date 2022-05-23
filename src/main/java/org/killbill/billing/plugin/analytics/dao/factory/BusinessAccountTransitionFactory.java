/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2022 Equinix, Inc
 * Copyright 2014-2022 The Billing Project, LLC
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.entitlement.api.SubscriptionEvent;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.util.audit.AuditLog;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class BusinessAccountTransitionFactory {

    public Collection<BusinessAccountTransitionModelDao> createBusinessAccountTransitions(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        final Iterable<SubscriptionEvent> blockingStatesOrdered = businessContextFactory.getAccountBlockingStates();
        if (!blockingStatesOrdered.iterator().hasNext()) {
            return ImmutableList.<BusinessAccountTransitionModelDao>of();
        }

        return createBusinessAccountTransitions(businessContextFactory, blockingStatesOrdered);
    }

    @VisibleForTesting
    Collection<BusinessAccountTransitionModelDao> createBusinessAccountTransitions(final BusinessContextFactory businessContextFactory,
                                                                                   final Iterable<SubscriptionEvent> blockingStatesOrdered) throws AnalyticsRefreshException {
        final Account account = businessContextFactory.getAccount();
        final Long accountRecordId = businessContextFactory.getAccountRecordId();
        final Long tenantRecordId = businessContextFactory.getTenantRecordId();
        final ReportGroup reportGroup = businessContextFactory.getReportGroup();

        final List<BusinessAccountTransitionModelDao> businessAccountTransitions = new LinkedList<BusinessAccountTransitionModelDao>();

        // Reverse to compute the end date of each state
        final List<SubscriptionEvent> blockingStates = Lists.reverse(ImmutableList.<SubscriptionEvent>copyOf(blockingStatesOrdered));

        // To remove duplicates
        final Set<UUID> blockingStateIdsSeen = new HashSet<UUID>();

        final Map<String, DateTime> previousStartDatePerService = new HashMap<>();
        for (final SubscriptionEvent state : blockingStates) {
            if (blockingStateIdsSeen.contains(state.getId())) {
                continue;
            } else {
                blockingStateIdsSeen.add(state.getId());
            }

            final Long blockingStateRecordId = businessContextFactory.getBlockingStateRecordId(state.getId());
            final AuditLog creationAuditLog = businessContextFactory.getBlockingStateCreationAuditLog(state.getId());
            // TODO We're missing information about block billing, etc. Maybe capture it in an event name?
            final BusinessAccountTransitionModelDao accountTransition = new BusinessAccountTransitionModelDao(account,
                                                                                                              accountRecordId,
                                                                                                              state.getServiceName(),
                                                                                                              state.getServiceStateName(),
                                                                                                              state.getEffectiveDate(),
                                                                                                              blockingStateRecordId,
                                                                                                              previousStartDatePerService.get(state.getServiceName()),
                                                                                                              creationAuditLog,
                                                                                                              tenantRecordId,
                                                                                                              reportGroup);
            businessAccountTransitions.add(accountTransition);
            previousStartDatePerService.put(state.getServiceName(), state.getEffectiveDate());
        }

        // Reverse again to store the events chronologically
        return Lists.<BusinessAccountTransitionModelDao>reverse(businessAccountTransitions);
    }
}
