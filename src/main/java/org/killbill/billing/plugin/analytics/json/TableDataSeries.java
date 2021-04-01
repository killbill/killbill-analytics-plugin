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

package org.killbill.billing.plugin.analytics.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TableDataSeries implements DataMarker {

    private final String name;
    private final List<String> header;
    private final List<List<Object>> values;

    @JsonCreator
    public TableDataSeries(@JsonProperty("name") final String name,
                           @JsonProperty("header") final List<String> header,
                           @JsonProperty("values") final List<List<Object>> values) {
        this.name = name;
        this.header = header;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<String> getHeader() {
        return header;
    }

    public List<List<Object>> getValues() {
        return values;
    }
}
