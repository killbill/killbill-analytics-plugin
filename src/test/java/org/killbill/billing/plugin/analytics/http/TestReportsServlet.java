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

package org.killbill.billing.plugin.analytics.http;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.killbill.billing.plugin.analytics.AnalyticsTestSuiteNoDB;
import org.killbill.billing.plugin.analytics.json.Chart;
import org.killbill.billing.plugin.analytics.json.DataMarker;
import org.killbill.billing.plugin.analytics.json.NamedXYTimeSeries;
import org.killbill.billing.plugin.analytics.json.XY;
import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.ReportType;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;

public class TestReportsServlet extends AnalyticsTestSuiteNoDB {

    private final ObjectMapper jsonMapper = ObjectMapperProvider.getJsonMapper();

    @Test(groups = "fast")
    public void testDateFormatter() {
        final LocalDate localDate = ReportsResource.DATE_FORMAT.parseLocalDate("2013-01-07");
        Assert.assertEquals(localDate.getYear(), 2013);
        Assert.assertEquals(localDate.getMonthOfYear(), 1);
        Assert.assertEquals(localDate.getDayOfMonth(), 7);
    }

    @Test(groups = "fast")
    public void testSimpleSerialization() throws Exception {
        final List<DataMarker> res = new ArrayList<DataMarker>();

        final List<XY> xys1 = new ArrayList<XY>();
        xys1.add(new XY("2013-01-01", 11));
        xys1.add(new XY("2013-01-02", 7));
        xys1.add(new XY("2013-01-03", 34));
        final NamedXYTimeSeries serie1 = new NamedXYTimeSeries("serie1", xys1);
        res.add(serie1);

        final List<XY> xys2 = new ArrayList<XY>();
        xys2.add(new XY("2013-01-01", 12));
        xys2.add(new XY("2013-01-02", 5));
        xys2.add(new XY("2013-01-03", 3));
        final NamedXYTimeSeries serie2 = new NamedXYTimeSeries("serie2", xys2);
        res.add(serie2);

        final Writer jsonWriter = new StringWriter();
        jsonMapper.writeValue(jsonWriter, res);
        Assert.assertEquals(jsonWriter.toString(),
                            "[{\"name\":\"serie1\",\"values\":[{\"x\":\"2013-01-01\",\"y\":11.0},{\"x\":\"2013-01-02\",\"y\":7.0},{\"x\":\"2013-01-03\",\"y\":34.0}]},{\"name\":\"serie2\",\"values\":[{\"x\":\"2013-01-01\",\"y\":12.0},{\"x\":\"2013-01-02\",\"y\":5.0},{\"x\":\"2013-01-03\",\"y\":3.0}]}]");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        ReportsResource.writeAsCSV(Collections.singletonList(new Chart(ReportType.TIMELINE, "foo", res)), out);
        Assert.assertEquals(out.toString(StandardCharsets.UTF_8.name()),
                            "serie1,2013-01-01,11.0\n" +
                            "serie1,2013-01-02,7.0\n" +
                            "serie1,2013-01-03,34.0\n" +
                            "serie2,2013-01-01,12.0\n" +
                            "serie2,2013-01-02,5.0\n" +
                            "serie2,2013-01-03,3.0\n"
                           );
    }


    @Test(groups = "fast")
    public void testDeserializationReserialization() throws Exception {
        final String json = "[" +
                            "{\"name\":\"ultimate\"," +
                            "\"values\":[" +
                            "{\"x\":\"2013-01-01\",\"y\":11.0}," +
                            "{\"x\":\"2013-01-02\",\"y\":37.0}," +
                            "{\"x\":\"2013-01-03\",\"y\":16.0}," +
                            "{\"x\":\"2013-01-04\",\"y\":29.0}," +
                            "{\"x\":\"2013-01-05\",\"y\":40.0}," +
                            "{\"x\":\"2013-01-06\",\"y\":3.0}," +
                            "{\"x\":\"2013-01-07\",\"y\":4.0}," +
                            "{\"x\":\"2013-01-08\",\"y\":39.0}," +
                            "{\"x\":\"2013-01-09\",\"y\":34.0}," +
                            "{\"x\":\"2013-01-10\",\"y\":31.0}," +
                            "{\"x\":\"2013-01-11\",\"y\":20.0}," +
                            "{\"x\":\"2013-01-12\",\"y\":28.0}," +
                            "{\"x\":\"2013-01-13\",\"y\":19.0}," +
                            "{\"x\":\"2013-01-14\",\"y\":15.0}," +
                            "{\"x\":\"2013-01-15\",\"y\":31.0}," +
                            "{\"x\":\"2013-01-16\",\"y\":16.0}," +
                            "{\"x\":\"2013-01-17\",\"y\":40.0}," +
                            "{\"x\":\"2013-01-18\",\"y\":29.0}," +
                            "{\"x\":\"2013-01-19\",\"y\":31.0}," +
                            "{\"x\":\"2013-01-20\",\"y\":11.0}," +
                            "{\"x\":\"2013-01-21\",\"y\":36.0}," +
                            "{\"x\":\"2013-01-22\",\"y\":18.0}," +
                            "{\"x\":\"2013-01-23\",\"y\":12.0}," +
                            "{\"x\":\"2013-01-24\",\"y\":23.0}," +
                            "{\"x\":\"2013-01-25\",\"y\":32.0}," +
                            "{\"x\":\"2013-01-26\",\"y\":27.0}," +
                            "{\"x\":\"2013-01-27\",\"y\":33.0}," +
                            "{\"x\":\"2013-01-28\",\"y\":34.0}," +
                            "{\"x\":\"2013-01-29\",\"y\":5.0}," +
                            "{\"x\":\"2013-01-30\",\"y\":7.0}," +
                            "{\"x\":\"2013-01-31\",\"y\":13.0}," +
                            "{\"x\":\"2013-02-01\",\"y\":10.0}," +
                            "{\"x\":\"2013-02-02\",\"y\":43.0}," +
                            "{\"x\":\"2013-02-03\",\"y\":15.0}," +
                            "{\"x\":\"2013-02-04\",\"y\":38.0}," +
                            "{\"x\":\"2013-02-05\",\"y\":34.0}," +
                            "{\"x\":\"2013-02-06\",\"y\":38.0}," +
                            "{\"x\":\"2013-02-07\",\"y\":26.0}," +
                            "{\"x\":\"2013-02-08\",\"y\":27.0}," +
                            "{\"x\":\"2013-02-09\",\"y\":1.0}," +
                            "{\"x\":\"2013-02-10\",\"y\":12.0}," +
                            "{\"x\":\"2013-02-11\",\"y\":28.0}," +
                            "{\"x\":\"2013-02-12\",\"y\":10.0}," +
                            "{\"x\":\"2013-02-13\",\"y\":27.0}" +
                            "]" +
                            "}" +
                            "]";

        final List<NamedXYTimeSeries> obj = jsonMapper.readValue(json.getBytes(StandardCharsets.UTF_8), new TypeReference<List<NamedXYTimeSeries>>() {});

        final Writer writer = new StringWriter();
        jsonMapper.writeValue(writer, obj);

        Assert.assertEquals(writer.toString(), json);
    }
}
