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

import org.killbill.billing.account.api.Account;
import org.killbill.billing.catalog.api.Plan;
import org.killbill.billing.catalog.api.PlanPhase;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceItem;
import org.killbill.billing.plugin.analytics.utils.CurrencyConverter;
import org.killbill.billing.util.audit.AuditLog;

public class BusinessInvoiceItemAdjustmentModelDao extends BusinessInvoiceItemBaseModelDao {

    public BusinessInvoiceItemAdjustmentModelDao() { /* When reading from the database */ }

    public BusinessInvoiceItemAdjustmentModelDao(final Account account,
                                                 final Long accountRecordId,
                                                 final Invoice invoice,
                                                 final InvoiceItem invoiceItem,
                                                 @Nullable final ItemSource itemSource,
                                                 final Long invoiceItemRecordId,
                                                 final Long secondInvoiceItemRecordId,
                                                 @Nullable final SubscriptionBundle bundle,
                                                 @Nullable final Plan plan,
                                                 @Nullable final PlanPhase planPhase,
                                                 final CurrencyConverter currencyConverter,
                                                 @Nullable final AuditLog creationAuditLog,
                                                 final Long tenantRecordId,
                                                 @Nullable final ReportGroup reportGroup) {
        super(account,
              accountRecordId,
              invoice,
              invoiceItem,
              itemSource,
              invoiceItemRecordId,
              secondInvoiceItemRecordId,
              bundle,
              plan,
              planPhase,
              currencyConverter,
              creationAuditLog,
              tenantRecordId,
              reportGroup);
    }

    @Override
    public String getTableName() {
        return INVOICE_ITEM_ADJUSTMENTS_TABLE_NAME;
    }

    @Override
    public BusinessInvoiceItemType getBusinessInvoiceItemType() {
        return BusinessInvoiceItemType.INVOICE_ITEM_ADJUSTMENT;
    }
}
