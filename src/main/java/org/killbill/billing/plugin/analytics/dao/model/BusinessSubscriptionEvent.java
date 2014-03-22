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

package org.killbill.billing.plugin.analytics.dao.model;

import javax.annotation.Nullable;

import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.Product;
import org.killbill.billing.catalog.api.ProductCategory;
import org.killbill.billing.entitlement.api.SubscriptionEvent;

/**
 * Describe an event associated with a transition between two BusinessSubscription
 */
public class BusinessSubscriptionEvent {

    private static final String CATEGORY_UNSPECIFIED = "UNSPECIFIED";

    public enum EventType {
        START_BILLING,
        PAUSE_ENTITLEMENT,
        PAUSE_BILLING,
        RESUME_ENTITLEMENT,
        RESUME_BILLING,
        STOP_ENTITLEMENT,
        STOP_BILLING,
        STATE_CHANGE,
        CHANGE,
        SYSTEM_CHANGE,
        START_ENTITLEMENT,
        ERROR
    }

    private final EventType eventType;
    // We keep the category in the event name because the prev_product_category and next_product_category fields
    // can be both NULL
    private final ProductCategory category;

    public static BusinessSubscriptionEvent valueOf(final String eventString) {
        for (final EventType possibleEventType : EventType.values()) {
            if (!eventString.startsWith(possibleEventType.toString().toUpperCase())) {
                continue;
            }

            final String categoryString = eventString.substring(possibleEventType.toString().length() + 1, eventString.length());

            if (categoryString.equals(CATEGORY_UNSPECIFIED)) {
                return new BusinessSubscriptionEvent(possibleEventType, null);
            } else {
                return new BusinessSubscriptionEvent(possibleEventType, ProductCategory.valueOf(categoryString));
            }
        }

        throw new IllegalArgumentException("Unable to parse event string: " + eventString);
    }

    private BusinessSubscriptionEvent(final EventType eventType, final ProductCategory category) {
        this.eventType = eventType;
        this.category = category;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public EventType getEventType() {
        return eventType;
    }

    public static BusinessSubscriptionEvent fromTransition(final SubscriptionEvent transition) {
        switch (transition.getSubscriptionEventType()) {
            // A subscription enters either through migration or as newly created subscription
            case START_ENTITLEMENT:
                return startEntitlement(transition.getNextPlan());
            case START_BILLING:
                return startBilling(transition.getNextPlan());
            case PAUSE_ENTITLEMENT:
                return pauseEntitlement(transition.getNextPlan());
            case PAUSE_BILLING:
                return pauseBilling(transition.getNextPlan());
            case RESUME_ENTITLEMENT:
                return resumeEntitlement(transition.getNextPlan());
            case RESUME_BILLING:
                return resumeBilling(transition.getNextPlan());
            case CHANGE:
                return subscriptionChanged(transition.getNextPlan());
            case PHASE:
                return subscriptionPhaseChanged(transition.getNextPlan());
            case STOP_ENTITLEMENT:
                return stopEntitlement(transition.getPrevPlan());
            case STOP_BILLING:
                return stopBilling(transition.getPrevPlan());
            case SERVICE_STATE_CHANGE:
                return subscriptionStateChanged(transition.getNextPlan());
            default:
                // Should never happen
                return unexpectedEvent(transition.getNextPlan());
        }
    }

    private static BusinessSubscriptionEvent startEntitlement(final Plan plan) {
        return eventFromType(EventType.START_ENTITLEMENT, plan);
    }

    private static BusinessSubscriptionEvent startBilling(final Plan plan) {
        return eventFromType(EventType.START_BILLING, plan);
    }

    private static BusinessSubscriptionEvent pauseEntitlement(final Plan plan) {
        return eventFromType(EventType.PAUSE_ENTITLEMENT, plan);
    }

    private static BusinessSubscriptionEvent pauseBilling(final Plan plan) {
        return eventFromType(EventType.PAUSE_BILLING, plan);
    }

    private static BusinessSubscriptionEvent resumeEntitlement(final Plan plan) {
        return eventFromType(EventType.RESUME_ENTITLEMENT, plan);
    }

    private static BusinessSubscriptionEvent resumeBilling(final Plan plan) {
        return eventFromType(EventType.RESUME_BILLING, plan);
    }

    private static BusinessSubscriptionEvent stopEntitlement(final Plan plan) {
        return eventFromType(EventType.STOP_ENTITLEMENT, plan);
    }

    private static BusinessSubscriptionEvent stopBilling(final Plan plan) {
        return eventFromType(EventType.STOP_BILLING, plan);
    }

    private static BusinessSubscriptionEvent subscriptionStateChanged(final Plan plan) {
        return eventFromType(EventType.STATE_CHANGE, plan);
    }

    private static BusinessSubscriptionEvent subscriptionChanged(final Plan plan) {
        return eventFromType(EventType.CHANGE, plan);
    }

    private static BusinessSubscriptionEvent subscriptionPhaseChanged(final Plan plan) {
        return eventFromType(EventType.SYSTEM_CHANGE, plan);
    }

    private static BusinessSubscriptionEvent unexpectedEvent(final Plan plan) {
        return eventFromType(EventType.ERROR, plan);
    }

    private static BusinessSubscriptionEvent eventFromType(final EventType eventType, final Plan plan) {
        final ProductCategory category = getTypeFromSubscription(plan);
        return new BusinessSubscriptionEvent(eventType, category);
    }

    private static ProductCategory getTypeFromSubscription(@Nullable final Plan plan) {
        if (plan != null && plan.getProduct() != null) {
            final Product product = plan.getProduct();
            if (product.getCatalogName() != null && product.getCategory() != null) {
                return product.getCategory();
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return eventType.toString() + "_" + (category == null ? CATEGORY_UNSPECIFIED : category.toString().toUpperCase());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final BusinessSubscriptionEvent that = (BusinessSubscriptionEvent) o;

        if (category != that.category) {
            return false;
        }
        if (eventType != null ? !eventType.equals(that.eventType) : that.eventType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventType != null ? eventType.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }
}
