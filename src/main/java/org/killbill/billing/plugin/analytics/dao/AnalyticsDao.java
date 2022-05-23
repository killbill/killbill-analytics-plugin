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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.killbill.billing.ObjectType;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillDataSource;
import org.killbill.billing.osgi.libs.killbill.OSGIMetricRegistry;
import org.killbill.billing.plugin.analytics.api.BusinessAccount;
import org.killbill.billing.plugin.analytics.api.BusinessAccountTransition;
import org.killbill.billing.plugin.analytics.api.BusinessBundle;
import org.killbill.billing.plugin.analytics.api.BusinessField;
import org.killbill.billing.plugin.analytics.api.BusinessInvoice;
import org.killbill.billing.plugin.analytics.api.BusinessPayment;
import org.killbill.billing.plugin.analytics.api.BusinessSubscriptionTransition;
import org.killbill.billing.plugin.analytics.api.BusinessTag;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessAccountTransitionModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessBundleModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceItemBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentBaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessTagModelDao;
import org.killbill.billing.util.api.RecordIdApi;
import org.killbill.billing.util.callcontext.TenantContext;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class AnalyticsDao extends BusinessAnalyticsDaoBase {

    private final OSGIKillbillAPI osgiKillbillAPI;

    public AnalyticsDao(final OSGIKillbillAPI osgiKillbillAPI,
                        final OSGIKillbillDataSource osgiKillbillDataSource,
                        final OSGIMetricRegistry metricRegistry) {
        super(osgiKillbillDataSource, metricRegistry);
        this.osgiKillbillAPI = osgiKillbillAPI;
    }

    public BusinessAccount getAccountById(final UUID accountId, final TenantContext context) {
        final Long accountRecordId = getAccountRecordId(accountId, context);
        final Long tenantRecordId = getTenantRecordId(context);

        final BusinessAccountModelDao businessAccountModelDao = sqlDao.getAccountByAccountRecordId(accountRecordId, tenantRecordId, context);
        if (businessAccountModelDao == null) {
            return null;
        } else {
            return new BusinessAccount(businessAccountModelDao);
        }
    }

    public Collection<BusinessBundle> getBundlesForAccount(final UUID accountId, final TenantContext context) {
        final Long accountRecordId = getAccountRecordId(accountId, context);
        final Long tenantRecordId = getTenantRecordId(context);

        final List<BusinessBundleModelDao> businessBundleModelDaos = sqlDao.getBundlesByAccountRecordId(accountRecordId, tenantRecordId, context);
        return Lists.transform(businessBundleModelDaos, new Function<BusinessBundleModelDao, BusinessBundle>() {
            @Override
            public BusinessBundle apply(final BusinessBundleModelDao input) {
                return input == null ? null : new BusinessBundle(input);
            }
        });
    }

    public Collection<BusinessSubscriptionTransition> getSubscriptionTransitionsForAccount(final UUID accountId, final TenantContext context) {
        final Long accountRecordId = getAccountRecordId(accountId, context);
        final Long tenantRecordId = getTenantRecordId(context);

        final List<BusinessSubscriptionTransitionModelDao> businessSubscriptionTransitionModelDaos = sqlDao.getSubscriptionTransitionsByAccountRecordId(accountRecordId, tenantRecordId, context);
        return Lists.transform(businessSubscriptionTransitionModelDaos, new Function<BusinessSubscriptionTransitionModelDao, BusinessSubscriptionTransition>() {
            @Override
            public BusinessSubscriptionTransition apply(final BusinessSubscriptionTransitionModelDao input) {
                return input == null ? null : new BusinessSubscriptionTransition(input);
            }
        });
    }

    public Collection<BusinessAccountTransition> getAccountTransitionsForAccount(final UUID accountId, final TenantContext context) {
        final Long accountRecordId = getAccountRecordId(accountId, context);
        final Long tenantRecordId = getTenantRecordId(context);

        final List<BusinessAccountTransitionModelDao> businessAccountTransitionModelDaos = sqlDao.getAccountTransitionsByAccountRecordId(accountRecordId, tenantRecordId, context);
        return Lists.transform(businessAccountTransitionModelDaos, new Function<BusinessAccountTransitionModelDao, BusinessAccountTransition>() {
            @Override
            public BusinessAccountTransition apply(final BusinessAccountTransitionModelDao input) {
                return input == null ? null : new BusinessAccountTransition(input);
            }
        });
    }

    public Collection<BusinessInvoice> getInvoicesForAccount(final UUID accountId, final TenantContext context) {
        final Long accountRecordId = getAccountRecordId(accountId, context);
        final Long tenantRecordId = getTenantRecordId(context);

        final List<BusinessInvoiceItemBaseModelDao> businessInvoiceItemModelDaos = new ArrayList<BusinessInvoiceItemBaseModelDao>();
        businessInvoiceItemModelDaos.addAll(sqlDao.getInvoiceAdjustmentsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessInvoiceItemModelDaos.addAll(sqlDao.getInvoiceItemsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessInvoiceItemModelDaos.addAll(sqlDao.getInvoiceItemAdjustmentsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessInvoiceItemModelDaos.addAll(sqlDao.getInvoiceItemCreditsByAccountRecordId(accountRecordId, tenantRecordId, context));

        final Map<UUID, List<BusinessInvoiceItemBaseModelDao>> itemsPerInvoice = new LinkedHashMap<UUID, List<BusinessInvoiceItemBaseModelDao>>();
        for (final BusinessInvoiceItemBaseModelDao businessInvoiceModelDao : businessInvoiceItemModelDaos) {
            if (itemsPerInvoice.get(businessInvoiceModelDao.getInvoiceId()) == null) {
                itemsPerInvoice.put(businessInvoiceModelDao.getInvoiceId(), new LinkedList<BusinessInvoiceItemBaseModelDao>());
            }
            itemsPerInvoice.get(businessInvoiceModelDao.getInvoiceId()).add(businessInvoiceModelDao);
        }

        final List<BusinessInvoiceModelDao> businessInvoiceModelDaos = sqlDao.getInvoicesByAccountRecordId(accountRecordId, tenantRecordId, context);
        return Lists.transform(businessInvoiceModelDaos, new Function<BusinessInvoiceModelDao, BusinessInvoice>() {
            @Override
            public BusinessInvoice apply(final BusinessInvoiceModelDao input) {
                return input == null ? null : new BusinessInvoice(input, MoreObjects.firstNonNull(itemsPerInvoice.get(input.getInvoiceId()), ImmutableList.<BusinessInvoiceItemBaseModelDao>of()));
            }
        });
    }

    public Collection<BusinessPayment> getInvoicePaymentsForAccount(final UUID accountId, final TenantContext context) {
        final Long accountRecordId = getAccountRecordId(accountId, context);
        final Long tenantRecordId = getTenantRecordId(context);

        final List<BusinessPaymentBaseModelDao> businessInvoicePaymentModelDaos = new ArrayList<BusinessPaymentBaseModelDao>();
        businessInvoicePaymentModelDaos.addAll(sqlDao.getPaymentAuthsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessInvoicePaymentModelDaos.addAll(sqlDao.getPaymentCapturesByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessInvoicePaymentModelDaos.addAll(sqlDao.getPaymentPurchasesByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessInvoicePaymentModelDaos.addAll(sqlDao.getPaymentRefundsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessInvoicePaymentModelDaos.addAll(sqlDao.getPaymentCreditsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessInvoicePaymentModelDaos.addAll(sqlDao.getPaymentChargebacksByAccountRecordId(accountRecordId, tenantRecordId, context));

        return Lists.transform(businessInvoicePaymentModelDaos, new Function<BusinessPaymentBaseModelDao, BusinessPayment>() {
            @Override
            public BusinessPayment apply(final BusinessPaymentBaseModelDao input) {
                return input == null ? null : new BusinessPayment(input);
            }
        });
    }

    public Collection<BusinessField> getFieldsForAccount(final UUID accountId, final TenantContext context) {
        final Long accountRecordId = getAccountRecordId(accountId, context);
        final Long tenantRecordId = getTenantRecordId(context);

        final List<BusinessFieldModelDao> businessFieldModelDaos = new LinkedList<BusinessFieldModelDao>();
        businessFieldModelDaos.addAll(sqlDao.getAccountFieldsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessFieldModelDaos.addAll(sqlDao.getBundleFieldsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessFieldModelDaos.addAll(sqlDao.getInvoiceFieldsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessFieldModelDaos.addAll(sqlDao.getInvoicePaymentFieldsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessFieldModelDaos.addAll(sqlDao.getPaymentFieldsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessFieldModelDaos.addAll(sqlDao.getPaymentMethodFieldsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessFieldModelDaos.addAll(sqlDao.getTransactionFieldsByAccountRecordId(accountRecordId, tenantRecordId, context));

        return Lists.transform(businessFieldModelDaos, new Function<BusinessFieldModelDao, BusinessField>() {
            @Override
            public BusinessField apply(final BusinessFieldModelDao input) {
                return input == null ? null : BusinessField.create(input);
            }
        });
    }

    public Collection<BusinessTag> getTagsForAccount(final UUID accountId, final TenantContext context) {
        final Long accountRecordId = getAccountRecordId(accountId, context);
        final Long tenantRecordId = getTenantRecordId(context);

        final List<BusinessTagModelDao> businessTagModelDaos = new LinkedList<BusinessTagModelDao>();
        businessTagModelDaos.addAll(sqlDao.getAccountTagsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessTagModelDaos.addAll(sqlDao.getBundleTagsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessTagModelDaos.addAll(sqlDao.getInvoiceTagsByAccountRecordId(accountRecordId, tenantRecordId, context));
        businessTagModelDaos.addAll(sqlDao.getInvoicePaymentTagsByAccountRecordId(accountRecordId, tenantRecordId, context));

        return Lists.transform(businessTagModelDaos, new Function<BusinessTagModelDao, BusinessTag>() {
            @Override
            public BusinessTag apply(final BusinessTagModelDao input) {
                return input == null ? null : BusinessTag.create(input);
            }
        });
    }

    private Long getAccountRecordId(final UUID accountId, final TenantContext context) {
        final RecordIdApi recordIdApi = osgiKillbillAPI.getRecordIdApi();
        if (recordIdApi == null) {
            return -1L;
        } else {
            final Long accountRecordIdOrNull = recordIdApi.getRecordId(accountId, ObjectType.ACCOUNT, context);
            // Never return null, to make sure indexes can be used (see https://github.com/killbill/killbill-analytics-plugin/issues/59)
            return accountRecordIdOrNull == null ? Long.valueOf(-1L) : accountRecordIdOrNull;
        }
    }

    private Long getTenantRecordId(final TenantContext context) {
        final RecordIdApi recordIdApi = osgiKillbillAPI.getRecordIdApi();
        if (recordIdApi == null) {
            // Be safe
            return -1L;
        } else {
            return (context.getTenantId() == null) ? null : recordIdApi.getRecordId(context.getTenantId(), ObjectType.TENANT, context);
        }
    }
}
