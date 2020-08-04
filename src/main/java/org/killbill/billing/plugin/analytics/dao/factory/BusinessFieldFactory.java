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
import java.util.Map;
import java.util.UUID;

import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.model.BusinessFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaosWithAccountAndTenantRecordId;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.customfield.CustomField;

public class BusinessFieldFactory {

    public BusinessModelDaosWithAccountAndTenantRecordId<BusinessFieldModelDao> createBusinessFields(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        final Account account = businessContextFactory.getAccount();

        final Long accountRecordId = businessContextFactory.getAccountRecordId();
        final Long tenantRecordId = businessContextFactory.getTenantRecordId();
        final ReportGroup reportGroup = businessContextFactory.getReportGroup();

        final Iterable<CustomField> fields = businessContextFactory.getAccountCustomFields();

        // Lookup once all SubscriptionBundle for that account (optimized call, should be faster in case an account has a lot
        // of bundles with custom fields)
        final Iterable<SubscriptionBundle> bundlesForAccount = businessContextFactory.getAccountBundles();
        final Map<UUID, SubscriptionBundle> bundles = new LinkedHashMap<UUID, SubscriptionBundle>();
        for (final SubscriptionBundle bundle : bundlesForAccount) {
            bundles.put(bundle.getId(), bundle);
        }

        final Collection<BusinessFieldModelDao> fieldModelDaos = new LinkedList<BusinessFieldModelDao>();
        // We process custom fields sequentially: in practice, an account will be associated with a dozen fields at most
        for (final CustomField field : fields) {
            final Long customFieldRecordId = businessContextFactory.getCustomFieldRecordId(field.getId());
            final AuditLog creationAuditLog = businessContextFactory.getCustomFieldCreationAuditLog(field.getId());

            SubscriptionBundle bundle = null;
            if (ObjectType.BUNDLE.equals(field.getObjectType())) {
                bundle = bundles.get(field.getObjectId());
            }
            final BusinessFieldModelDao fieldModelDao = BusinessFieldModelDao.create(account,
                                                                                     accountRecordId,
                                                                                     bundle,
                                                                                     field,
                                                                                     customFieldRecordId,
                                                                                     creationAuditLog,
                                                                                     tenantRecordId,
                                                                                     reportGroup);
            if (fieldModelDao != null) {
                fieldModelDaos.add(fieldModelDao);
            }
        }

        return new BusinessModelDaosWithAccountAndTenantRecordId<BusinessFieldModelDao>(accountRecordId, tenantRecordId, fieldModelDaos);
    }
}
