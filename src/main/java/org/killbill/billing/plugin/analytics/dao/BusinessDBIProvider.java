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

import javax.sql.DataSource;

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
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentFieldModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoicePaymentTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessInvoiceTagModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentAuthModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentCaptureModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentChargebackModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentCreditModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentPurchaseModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessPaymentRefundModelDao;
import org.killbill.billing.plugin.analytics.dao.model.BusinessSubscriptionTransitionModelDao;
import org.killbill.billing.plugin.analytics.dao.model.CurrencyConversionModelDao;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao;
import org.killbill.commons.jdbi.ReusableStringTemplate3StatementLocator;
import org.killbill.commons.jdbi.argument.DateTimeArgumentFactory;
import org.killbill.commons.jdbi.argument.DateTimeZoneArgumentFactory;
import org.killbill.commons.jdbi.argument.EnumArgumentFactory;
import org.killbill.commons.jdbi.argument.LocalDateArgumentFactory;
import org.killbill.commons.jdbi.argument.UUIDArgumentFactory;
import org.killbill.commons.jdbi.log.Slf4jLogging;
import org.killbill.commons.jdbi.mapper.LowerToCamelBeanMapperFactory;
import org.killbill.commons.jdbi.mapper.UUIDMapper;
import org.killbill.commons.jdbi.notification.DatabaseTransactionNotificationApi;
import org.killbill.commons.jdbi.transaction.NotificationTransactionHandler;
import org.killbill.commons.jdbi.transaction.RestartTransactionRunner;
import org.skife.jdbi.v2.Binding;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.TransactionHandler;

import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;

public class BusinessDBIProvider {

    private BusinessDBIProvider() {}

    public static DBI get(final DataSource dataSource) {
        final DBI dbi = new DBI(dataSource);

        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessAccountFieldModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessAccountModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessAccountTagModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessInvoiceAdjustmentModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessInvoiceFieldModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessInvoiceItemAdjustmentModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessInvoiceItemCreditModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessInvoiceItemModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessInvoiceModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessInvoicePaymentFieldModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessPaymentAuthModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessPaymentCaptureModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessPaymentPurchaseModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessPaymentRefundModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessPaymentCreditModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessPaymentChargebackModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessInvoicePaymentTagModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessInvoiceTagModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessAccountTransitionModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessSubscriptionTransitionModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessBundleModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessBundleFieldModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(BusinessBundleTagModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(CurrencyConversionModelDao.class));
        dbi.registerMapper(new LowerToCamelBeanMapperFactory(ReportsConfigurationModelDao.class));

        dbi.registerMapper(new UUIDMapper());

        dbi.registerArgumentFactory(new UUIDArgumentFactory());
        dbi.registerArgumentFactory(new DateTimeZoneArgumentFactory());
        dbi.registerArgumentFactory(new DateTimeArgumentFactory());
        dbi.registerArgumentFactory(new LocalDateArgumentFactory());
        dbi.registerArgumentFactory(new EnumArgumentFactory());

        dbi.setStatementLocator(new AnalyticsStatementLocator());

        dbi.setSQLLog(new Slf4jLogging());

        final DatabaseTransactionNotificationApi databaseTransactionNotificationApi = new DatabaseTransactionNotificationApi();
        final TransactionHandler notificationTransactionHandler = new NotificationTransactionHandler(databaseTransactionNotificationApi);
        dbi.setTransactionHandler(new RestartTransactionRunner(notificationTransactionHandler));

        return dbi;
    }

    private static final class AnalyticsStatementLocator extends ReusableStringTemplate3StatementLocator {

        public AnalyticsStatementLocator() {
            super(BusinessAnalyticsSqlDao.class, true, true);
        }

        @Override
        public String locate(final String name, final StatementContext ctx) throws Exception {
            // Rewrite create to createBac, createBin, createBiia, etc.
            if ("create".equals(name)) {
                final Binding binding = ctx.getBinding();
                if (binding != null) {
                    final Argument tableNameArgument = binding.forName("tableName");
                    if (tableNameArgument != null) {
                        // Lame, rely on toString (tableNameArgument will be a org.skife.jdbi.v2.StringArgument)
                        final String tableName = CharMatcher.anyOf("'").removeFrom(tableNameArgument.toString());
                        final String newQueryName = name + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);
                        return super.locate(newQueryName, ctx);
                    }
                }
            }

            // Inspired from org.skife.jdbi.v2.ClasspathStatementLocator to allow real SQL to be executed
            if (looksLikeSql(name)) {
                return name;
            } else {
                return super.locate(name, ctx);
            }
        }

        /**
         * Very basic sanity test to see if a string looks like it might be sql
         */
        public static boolean looksLikeSql(String sql) {
            final String local = left(stripStart(sql), 7).toLowerCase();
            return local.startsWith("insert ")
                   || local.startsWith("update ")
                   || local.startsWith("select ")
                   || local.startsWith("call ")
                   || local.startsWith("delete ")
                   || local.startsWith("create ")
                   || local.startsWith("alter ")
                   || local.startsWith("drop ");
        }

        // (scs) Logic copied from commons-lang3 3.1 with minor edits, per discussion on commit 023a14ade2d33bf8ccfa0f68294180455233ad52
        private static String stripStart(String str) {
            int strLen;
            if (str == null || (strLen = str.length()) == 0) {
                return "";
            }
            int start = 0;
            while (start != strLen && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
            return str.substring(start);
        }

        private static String left(String str, int len) {
            if (str == null || len < 0) {
                return "";
            }
            if (str.length() <= len) {
                return str;
            }
            return str.substring(0, len);
        }
    }
}
