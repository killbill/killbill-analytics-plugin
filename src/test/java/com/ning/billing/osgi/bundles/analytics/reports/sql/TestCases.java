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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.osgi.bundles.analytics.AnalyticsTestSuiteNoDB;

public class TestCases extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testCheckRegexp() throws Exception {
        Assert.assertEquals(Cases.of("currency").toString(), "\"currency\"");
        Assert.assertEquals(Cases.of("currency(USD)").toString(), "case when \"currency\" = 'USD' then 'USD' else 'Other' end");
        Assert.assertEquals(Cases.of("currency(USD|BRL,GBP,EUR,MXN,AUD)").toString(), "case when \"currency\" = 'USD' then 'USD' when \"currency\" = 'BRL' then 'BRL,GBP,EUR,MXN,AUD' when \"currency\" = 'GBP' then 'BRL,GBP,EUR,MXN,AUD' when \"currency\" = 'EUR' then 'BRL,GBP,EUR,MXN,AUD' when \"currency\" = 'MXN' then 'BRL,GBP,EUR,MXN,AUD' when \"currency\" = 'AUD' then 'BRL,GBP,EUR,MXN,AUD' else 'Other' end");
    }
}
