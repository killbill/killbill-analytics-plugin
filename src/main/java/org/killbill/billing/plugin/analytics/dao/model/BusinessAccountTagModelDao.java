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

package org.killbill.billing.plugin.analytics.dao.model;

import javax.annotation.Nullable;

import org.killbill.billing.account.api.Account;
import org.killbill.billing.util.audit.AuditLog;
import org.killbill.billing.util.tag.Tag;
import org.killbill.billing.util.tag.TagDefinition;

public class BusinessAccountTagModelDao extends BusinessTagModelDao {

    public BusinessAccountTagModelDao() { /* When reading from the database */ }

    public BusinessAccountTagModelDao(final Account account,
                                      final Long accountRecordId,
                                      final Tag tag,
                                      final Long tagRecordId,
                                      final TagDefinition tagDefinition,
                                      @Nullable final AuditLog creationAuditLog,
                                      final Long tenantRecordId,
                                      @Nullable final ReportGroup reportGroup) {
        super(account,
              accountRecordId,
              tag,
              tagRecordId,
              tagDefinition,
              creationAuditLog,
              tenantRecordId,
              reportGroup);
    }

    @Override
    public String getTableName() {
        return ACCOUNT_TAGS_TABLE_NAME;
    }
}
