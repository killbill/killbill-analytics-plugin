/*
 * Copyright 2020-2021 Equinix, Inc
 * Copyright 2014-2021 The Billing Project, LLC
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

package org.killbill.billing.plugin.analytics.reports.databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.skife.jdbi.v2.DBI;

import com.google.common.base.Preconditions;
import io.trino.jdbc.TrinoDriver;

public class Trino {

    static {
        try {
            DriverManager.registerDriver(new TrinoDriver());
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private final String name;
    private final String url;

    public Trino(final String name,
                 final Map<String, String> configuration) {
        Preconditions.checkArgument(configuration != null && "trino".equals(configuration.get("type")), "Expected trino, got " + (configuration == null ? "null" : configuration.get("type")));
        Preconditions.checkNotNull(configuration.get("url"), "Missing trino url");
        this.name = name;
        this.url = configuration.get("url");
    }

    public DBI getDBI() {
        return new DBI(url);
    }
}
