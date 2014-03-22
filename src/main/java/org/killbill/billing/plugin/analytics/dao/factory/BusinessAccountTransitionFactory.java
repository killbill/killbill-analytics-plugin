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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.entitlement.api.SubscriptionEvent;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.util.audit.AccountAuditLogs;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.clock.Clock;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.killbill.osgi.libs.killbill.OSGIKillbillLogService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class BusinessAccountTransitionFactory extends BusinessFactoryBase {

    public BusinessAccountTransitionFactory(final OSGIKillbillLogService logService,
                                            final OSGIKillbillAPI osgiKillbillAPI,
                                            final OSGIKillbillDataSource osgiKillbillDataSource,
                                            final Clock clock) {
        super(logService, osgiKillbillAPI, osgiKillbillDataSource, clock);
    }

    public Collection<BusinessAccountTransitionModelDao> createBusinessAccountTransitions(final UUID accountId,
                                                                                          final AccountAuditLogs accountAuditLogs,
                                                                                          final CallContext context) throws AnalyticsRefreshException {
        final Account account = getAccount(accountId, context);

        final Iterable<SubscriptionEvent> blockingStatesOrdered = getBlockingHistory(accountId, context);
        if (!blockingStatesOrdered.iterator().hasNext()) {
            return ImmutableList.<BusinessAccountTransitionModelDao>of();
        }

        return createBusinessAccountTransitions(account, accountAuditLogs, blockingStatesOrdered, context);
    }

    @VisibleForTesting
    Collection<BusinessAccountTransitionModelDao> createBusinessAccountTransitions(final Account account,
                                                                                   final AccountAuditLogs accountAuditLogs,
                                                                                   final Iterable<SubscriptionEvent> blockingStatesOrdered,
                                                                                   final CallContext context) throws AnalyticsRefreshException {
        final Long accountRecordId = getAccountRecordId(account.getId(), context);
        final Long tenantRecordId = getTenantRecordId(context);
        final ReportGroup reportGroup = getReportGroup(account.getId(), context);

        final List<BusinessAccountTransitionModelDao> businessAccountTransitions = new LinkedList<BusinessAccountTransitionModelDao>();

        // Reverse to compute the end date of each state
        final List<SubscriptionEvent> blockingStates = Lists.reverse(ImmutableList.<SubscriptionEvent>copyOf(blockingStatesOrdered));

        // To remove duplicates
        final Set<UUID> blockingStateIdsSeen = new HashSet<UUID>();

        final Map<String, LocalDate> previousStartDatePerService = new HashMap<String, LocalDate>();
        for (final SubscriptionEvent state : blockingStates) {
            if (blockingStateIdsSeen.contains(state.getId())) {
                continue;
            } else {
                blockingStateIdsSeen.add(state.getId());
            }

            final Long blockingStateRecordId = getBlockingStateRecordId(state.getId(), context);
            final AuditLog creationAuditLog = getBlockingStateCreationAuditLog(state.getId(), accountAuditLogs);
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
