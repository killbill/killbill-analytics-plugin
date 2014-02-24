/*
 * Copyright 2010-2014 Ning, Inc.
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

package com.ning.billing.osgi.bundles.analytics.reports.sql;

import org.jooq.Table;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteWithEmbeddedDB;

public class TestMetadata extends AnalyticsTestSuiteWithEmbeddedDB {

    @Test(groups = "slow")
    public void testTableRetrieval() throws Exception {
        final String tableName = "payments_per_day";
        embeddedDB.executeScript(String.format("create table %s(day datetime, name varchar(100), currency varchar(10), state varchar(10), amount int, fee int);", tableName));

        final Metadata metadata = new Metadata(embeddedDB.getDataSource(), logService);
        final Table table = metadata.getTable(tableName);

        Assert.assertEquals(table.fields().length, 6);
        Assert.assertEquals(table.fields()[0].getName(), "day");
        Assert.assertEquals(table.fields()[1].getName(), "name");
        Assert.assertEquals(table.fields()[2].getName(), "currency");
        Assert.assertEquals(table.fields()[3].getName(), "state");
        Assert.assertEquals(table.fields()[4].getName(), "amount");
        Assert.assertEquals(table.fields()[5].getName(), "fee");
    }
}
