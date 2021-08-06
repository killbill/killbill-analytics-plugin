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

package org.killbill.billing.plugin.analytics;

import org.killbill.billing.notification.plugin.api.ExtBusEventType;

public abstract class AnalyticsJobHierarchy {

    public static Group fromEventType(final ExtBusEventType extBusEventType) {
        switch (extBusEventType) {
            // Account information is denormalized across all tables
            case ACCOUNT_CREATION:
            case ACCOUNT_CHANGE:
            // Tags determine the report group (denormalized across all tables)
            case TAG_CREATION:
            case TAG_DELETION:
                return Group.ALL;
            case BUNDLE_PAUSE:
            case BUNDLE_RESUME:
            case SUBSCRIPTION_CREATION:
            case SUBSCRIPTION_CHANGE:
            case SUBSCRIPTION_CANCEL:
            case SUBSCRIPTION_PHASE:
            case SUBSCRIPTION_UNCANCEL:
                return Group.SUBSCRIPTIONS;
            case OVERDUE_CHANGE:
                return Group.OVERDUE;
            case INVOICE_CREATION:
            case INVOICE_ADJUSTMENT:
                return Group.INVOICES;
            case PAYMENT_SUCCESS:
            case PAYMENT_FAILED:
                return Group.INVOICE_AND_PAYMENTS;
            case CUSTOM_FIELD_CREATION:
            case CUSTOM_FIELD_DELETION:
                return Group.FIELDS;
            default:
                return Group.OTHER;
        }
    }

    public enum Group {
        ALL,
        FIELDS,
        INVOICES,
        INVOICE_AND_PAYMENTS,
        OVERDUE,
        OTHER,
        SUBSCRIPTIONS
    }
}
