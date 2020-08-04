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

package org.killbill.billing.plugin.analytics.utils;

import javax.annotation.Nullable;

import org.joda.time.LocalDate;
import org.killbill.billing.catalog.api.PlanPhase;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.entitlement.api.SubscriptionEvent;
import org.killbill.billing.invoice.api.InvoiceItem;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class BusinessInvoiceItemUtils {

    // Try to compute the service period end date for fixed items
    public static LocalDate computeServicePeriodEndDate(final InvoiceItem invoiceItem,
                                                        @Nullable final PlanPhase planPhase,
                                                        @Nullable final SubscriptionBundle bundle) {
        if (invoiceItem.getEndDate() != null) {
            return invoiceItem.getEndDate();
        } else if (planPhase == null || bundle == null) {
            return null;
        } else {
            final Optional<SubscriptionEvent> nextEvent = Iterables.<SubscriptionEvent>tryFind(bundle.getTimeline().getSubscriptionEvents(),
                                                                                               new Predicate<SubscriptionEvent>() {
                                                                                                   SubscriptionEvent subscriptionEventForInvoiceItem = null;

                                                                                                   @Override
                                                                                                   public boolean apply(final SubscriptionEvent input) {
                                                                                                       if (input == null) {
                                                                                                           return false;
                                                                                                       }

                                                                                                       if (input.getEntitlementId().equals(invoiceItem.getSubscriptionId()) &&
                                                                                                           // planPhase can't be null here
                                                                                                           planPhase.equals(input.getNextPhase()) &&
                                                                                                           input.getEffectiveDate().compareTo(invoiceItem.getStartDate()) == 0) {
                                                                                                           subscriptionEventForInvoiceItem = input;
                                                                                                           return false;
                                                                                                       }

                                                                                                       return subscriptionEventForInvoiceItem != null &&
                                                                                                              input.getEntitlementId().equals(invoiceItem.getSubscriptionId()) &&
                                                                                                              // planPhase can't be null here (prev phase can be for the first event)
                                                                                                              planPhase.equals(input.getPrevPhase());
                                                                                                   }
                                                                                               }
                                                                                              );
            if (nextEvent.isPresent()) {
                return nextEvent.get().getEffectiveDate();
            } else {
                return null;
            }
        }
    }
}
