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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CounterChart implements DataMarker {

    private final String label;
    private final Float value;

    @JsonCreator
    public CounterChart(@JsonProperty("label") final String label,
                        @JsonProperty("value") final Float value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public Float getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CounterChart)) {
            return false;
        }

        final CounterChart counters = (CounterChart) o;

        if (label != null ? !label.equals(counters.label) : counters.label != null) {
            return false;
        }
        if (value != null ? !value.equals(counters.value) : counters.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
