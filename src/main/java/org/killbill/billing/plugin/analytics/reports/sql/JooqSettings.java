/*
 * Copyright 2020-2022 Equinix, Inc
 * Copyright 2014-2022 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.reports.sql;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.ParseUnknownFunctions;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;
import org.killbill.billing.plugin.dao.PluginDao;
import org.killbill.billing.plugin.dao.PluginDao.DBEngine;

public abstract class JooqSettings {

    public static Settings defaults(final SQLDialect sqlDialect) {
        final Settings settings = new Settings();
        // Tell jOOQ not be too smart -- we specified and/or built the query, we know what we are doing!
        settings.withParseUnknownFunctions(ParseUnknownFunctions.IGNORE);
        settings.withStatementType(StatementType.STATIC_STATEMENT);
        settings.withRenderFormatted(true);
        if (SQLDialect.H2.equals(sqlDialect)) {
            settings.withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED);
        }
        return settings;
    }

    public static DSLContext buildDslContext(final DBEngine dbEngine) {
        final SQLDialect sqlDialect = SQLDialectFromDBEngine(dbEngine);
        return DSL.using(sqlDialect, JooqSettings.defaults(sqlDialect));
    }

    public static DSLContext buildDslContext(final DataSource dataSource) throws SQLException {
        final SQLDialect sqlDialect = SQLDialectFromDBEngine(PluginDao.getDBEngine(dataSource));
        return DSL.using(dataSource, sqlDialect, defaults(sqlDialect));
    }

    private static SQLDialect SQLDialectFromDBEngine(final DBEngine dbEngine) {
        switch (dbEngine) {
            case H2:
                return SQLDialect.H2;
            case MYSQL:
                return SQLDialect.MARIADB;
            case POSTGRESQL:
                return SQLDialect.POSTGRES;
            default:
                throw new IllegalArgumentException("Unsupported DB engine: " + dbEngine);
        }
    }
}
