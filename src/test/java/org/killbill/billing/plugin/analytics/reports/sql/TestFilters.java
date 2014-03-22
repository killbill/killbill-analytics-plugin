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

package org.killbill.billing.plugin.analytics.reports.sql;

import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.reports.ReportSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.bpodgursky.jbool_expressions.Variable;

public class TestFilters extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testConditionFromVariableBuilder() throws Exception {
        Assert.assertEquals(Filters.of(Variable.of("currency=USD")).toString(), "\"currency\" = 'USD'");
        Assert.assertEquals(Filters.of(Variable.of("status=state=PROCESSED")).toString(), "\"status\" = 'state=PROCESSED'");
        Assert.assertEquals(Filters.of(Variable.of("status!=state=PROCESSED")).toString(), "\"status\" <> 'state=PROCESSED'");
    }

    @Test(groups = "fast")
    public void testConditionFromExpressionBuilder() throws Exception {
        final ReportSpecification reportSpecification1 = new ReportSpecification("payments_per_day^filter:currency=AUD^filter:currency=EUR");
        Assert.assertEquals(Filters.buildConditionFromExpression(reportSpecification1.getFilterExpression()).toString(), "(\"currency\" = 'AUD' or \"currency\" = 'EUR')");

        final ReportSpecification reportSpecification2 = new ReportSpecification("payments_per_day^filter:currency!=AUD^filter:currency!=EUR");
        Assert.assertEquals(Filters.buildConditionFromExpression(reportSpecification2.getFilterExpression()).toString(), "(\"currency\" <> 'AUD' or \"currency\" <> 'EUR')");

        final ReportSpecification reportSpecification3 = new ReportSpecification("payments_per_day^" +
                                                                                 "filter:(currency=USD&state!=ERRORED)|(currency=EUR&state=PROCESSED)^" +
                                                                                 "filter:name~'John Doe'&age>=35|name!~Fred&age<24");
        Assert.assertEquals(Filters.buildConditionFromExpression(reportSpecification3.getFilterExpression()).toString(), "(" +
                                                                                                                         "(\"age\" < '24' and \"name\" not like 'Fred') " +
                                                                                                                         "or (\"age\" >= '35' and \"name\" like 'John Doe') " +
                                                                                                                         "or (\"currency\" = 'EUR' and \"state\" = 'PROCESSED') " +
                                                                                                                         "or (\"currency\" = 'USD' and \"state\" <> 'ERRORED')" +
                                                                                                                         ")");
    }
}
