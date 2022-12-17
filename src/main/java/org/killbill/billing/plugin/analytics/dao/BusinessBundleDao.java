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

package org.killbill.billing.plugin.analytics.dao;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIMetricRegistry;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleModelDao;
import org.killbill.billing.util.callcontext.CallContext;

public class BusinessBundleDao extends BusinessAnalyticsDaoBase {

    public BusinessBundleDao(final OSGIKillbillDataSource osgiKillbillDataSource,
                             final OSGIMetricRegistry metricRegistry) {
        super(osgiKillbillDataSource, metricRegistry);
    }

    public void updateInTransaction(final Iterable<BusinessBundleModelDao> bbss,
                                    final Long tenantRecordId,
                                    final BusinessAnalyticsSqlDao transactional,
                                    final CallContext context) {
        for (final BusinessBundleModelDao bbs : bbss) {
            // Delete by bundle to support partial refreshes
            transactional.deleteByBundleId(BusinessBundleModelDao.BUNDLES_TABLE_NAME,
                                           bbs.getBundleId(),
                                           tenantRecordId,
                                           context);

            transactional.create(bbs.getTableName(), bbs, context);
        }

        // The update of summary columns in BAC will be done via BST
    }
}
