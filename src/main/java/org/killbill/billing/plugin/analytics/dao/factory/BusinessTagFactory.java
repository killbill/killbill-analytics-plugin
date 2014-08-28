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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.killbill.billing.ObjectType;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.entitlement.api.SubscriptionBundle;
import org.killbill.billing.plugin.analytics.AnalyticsRefreshException;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase.ReportGroup;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaosWithAccountAndTenantRecordId;
import org.killbill.billing.plugin.analytics.dao.model.BusinessTagModelDao;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.tag.Tag;
import org.killbill.billing.util.tag.TagDefinition;

public class BusinessTagFactory {

    public BusinessModelDaosWithAccountAndTenantRecordId<BusinessTagModelDao> createBusinessTags(final BusinessContextFactory businessContextFactory) throws AnalyticsRefreshException {
        final Account account = businessContextFactory.getAccount();

        final Long accountRecordId = businessContextFactory.getAccountRecordId();
        final Long tenantRecordId = businessContextFactory.getTenantRecordId();
        final ReportGroup reportGroup = businessContextFactory.getReportGroup();

        final Iterable<Tag> tags = businessContextFactory.getAccountTags();

        // Lookup once all SubscriptionBundle for that account (optimized call, should be faster in case an account has a lot
        // of tagged bundles)
        final Iterable<SubscriptionBundle> bundlesForAccount = businessContextFactory.getAccountBundles();
        final Map<UUID, SubscriptionBundle> bundles = new LinkedHashMap<UUID, SubscriptionBundle>();
        for (final SubscriptionBundle bundle : bundlesForAccount) {
            bundles.put(bundle.getId(), bundle);
        }

        final Collection<BusinessTagModelDao> tagModelDaos = new LinkedList<BusinessTagModelDao>();
        // We process tags sequentially: in practice, an account will be associated with a dozen tags at most
        for (final Tag tag : tags) {
            final Long tagRecordId = businessContextFactory.getTagRecordId(tag.getId());
            final TagDefinition tagDefinition = businessContextFactory.getTagDefinition(tag.getTagDefinitionId());
            final AuditLog creationAuditLog = businessContextFactory.getTagCreationAuditLog(tag.getId());

            SubscriptionBundle bundle = null;
            if (ObjectType.BUNDLE.equals(tag.getObjectType())) {
                bundle = bundles.get(tag.getObjectId());
            }
            final BusinessTagModelDao tagModelDao = BusinessTagModelDao.create(account,
                                                                               accountRecordId,
                                                                               bundle,
                                                                               tag,
                                                                               tagRecordId,
                                                                               tagDefinition,
                                                                               creationAuditLog,
                                                                               tenantRecordId,
                                                                               reportGroup);
            tagModelDaos.add(tagModelDao);
        }

        return new BusinessModelDaosWithAccountAndTenantRecordId<BusinessTagModelDao>(accountRecordId, tenantRecordId, tagModelDaos);
    }
}
