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

package org.killbill.billing.plugin.analytics.reports.sql;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jooq.Table;

public class TableMetadata {

    private final Table table;
    private final Map<String, List<Object>> distinctValues;

    public TableMetadata(final Table table, @Nullable final Map<String, List<Object>> distinctValues) {
        this.table = table;
        this.distinctValues = distinctValues;
    }

    public Table getTable() {
        return table;
    }

    public Map<String, List<Object>> getDistinctValues() {
        return distinctValues;
    }
}
