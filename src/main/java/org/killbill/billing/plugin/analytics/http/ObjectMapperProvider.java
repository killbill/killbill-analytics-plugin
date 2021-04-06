/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
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

package org.killbill.billing.plugin.analytics.http;

import org.killbill.billing.plugin.analytics.json.CSVNamedXYTimeSeries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class ObjectMapperProvider {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final CsvMapper _csvMapper = new CsvMapper();
    private static final CsvSchema namedXYTimeSeriesCsvSchema = _csvMapper.schemaFor(CSVNamedXYTimeSeries.class);
    private static ObjectWriter csvWriter;

    static {
        jsonMapper.registerModule(new JodaModule());
        jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        _csvMapper.registerModule(new JodaModule());
        _csvMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        _csvMapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        csvWriter = _csvMapper.writer(namedXYTimeSeriesCsvSchema);
    }

    private ObjectMapperProvider() {}

    public static ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    public static ObjectWriter getCsvWriter() {
        return csvWriter;
    }
}
