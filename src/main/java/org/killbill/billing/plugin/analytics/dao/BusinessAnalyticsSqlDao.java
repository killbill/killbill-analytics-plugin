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

package org.killbill.billing.plugin.analytics.dao;

import java.util.List;

import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceAdjustmentModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemAdjustmentModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemCreditModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentChargebackModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentRefundModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessModelDaoBase;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.commons.jdbi.binder.SmartBindBean;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;

public interface BusinessAnalyticsSqlDao extends Transactional<BusinessAnalyticsSqlDao> {

    // Note: the CallContext and TenantContext are not bound for now since they are not used (and createdDate would conflict)

    @SqlUpdate
    public void create(final String tableName,
                       @SmartBindBean final BusinessModelDaoBase entity,
                       final CallContext callContext);

    @SqlUpdate
    public void deleteByAccountRecordId(@Define("tableName") final String tableName,
                                        @Bind("accountRecordId") final Long accountRecordId,
                                        @Bind("tenantRecordId") final Long tenantRecordId,
                                        final CallContext callContext);

    @SqlQuery
    public BusinessAccountModelDao getAccountByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                               @Bind("tenantRecordId") final Long tenantRecordId,
                                                               final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessSubscriptionTransitionModelDao> getSubscriptionTransitionsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                                    @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                                    final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessBundleModelDao> getBundlesByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                    @Bind("tenantRecordId") final Long tenantRecordId,
                                                                    final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessAccountTransitionModelDao> getAccountTransitionsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                          @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                          final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoiceModelDao> getInvoicesByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                      @Bind("tenantRecordId") final Long tenantRecordId,
                                                                      final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoiceAdjustmentModelDao> getInvoiceAdjustmentsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                          @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                          final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoiceItemModelDao> getInvoiceItemsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                              @Bind("tenantRecordId") final Long tenantRecordId,
                                                                              final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoiceItemAdjustmentModelDao> getInvoiceItemAdjustmentsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                                  @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                                  final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoiceItemCreditModelDao> getInvoiceItemCreditsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                          @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                          final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoicePaymentModelDao> getInvoicePaymentsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                    @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                    final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoicePaymentRefundModelDao> getInvoicePaymentRefundsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                                @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                                final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoicePaymentChargebackModelDao> getInvoicePaymentChargebacksByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                                        @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                                        final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessAccountFieldModelDao> getAccountFieldsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessBundleFieldModelDao> getBundleFieldsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                              @Bind("tenantRecordId") final Long tenantRecordId,
                                                                              final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoiceFieldModelDao> getInvoiceFieldsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoicePaymentFieldModelDao> getInvoicePaymentFieldsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                              @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                              final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessAccountTagModelDao> getAccountTagsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                            @Bind("tenantRecordId") final Long tenantRecordId,
                                                                            final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessBundleTagModelDao> getBundleTagsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                          @Bind("tenantRecordId") final Long tenantRecordId,
                                                                          final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoiceTagModelDao> getInvoiceTagsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                            @Bind("tenantRecordId") final Long tenantRecordId,
                                                                            final TenantContext tenantContext);

    @SqlQuery
    public List<BusinessInvoicePaymentTagModelDao> getInvoicePaymentTagsByAccountRecordId(@Bind("accountRecordId") final Long accountRecordId,
                                                                                          @Bind("tenantRecordId") final Long tenantRecordId,
                                                                                          final TenantContext tenantContext);
}
