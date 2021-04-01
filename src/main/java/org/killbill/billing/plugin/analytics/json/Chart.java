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

import org.killbill.billing.plugin.analytics.reports.configuration.ReportsConfigurationModelDao.ReportType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Chart {

    private final ReportType type;
    private final String title;
    private final List<DataMarker> data;

    private final int ordering;

    @JsonCreator
    public Chart(@JsonProperty("type") final ReportType type,
                 @JsonProperty("title") final String title,
                 @JsonProperty("data") final List<DataMarker> data) {
        this.type = type;
        this.title = title;
        this.data = data;
        this.ordering = -1;
    }

    public Chart(final ReportType type,
                 final String title,
                 final int ordering,
                 final List<DataMarker> data) {
        this.type = type;
        this.title = title;
        this.ordering = ordering;
        this.data = data;
    }

    public ReportType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public List<DataMarker> getData() {
        return data;
    }

    @JsonIgnore
    public int getOrdering() {
        return ordering;
    }
}
