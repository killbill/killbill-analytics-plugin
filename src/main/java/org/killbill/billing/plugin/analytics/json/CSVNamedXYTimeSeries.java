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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"name", "x", "y"})
public class CSVNamedXYTimeSeries implements DataMarker {

    private final String name;
    private final String x;
    private final Float y;

    public CSVNamedXYTimeSeries(final String name, final XY value) {
        this.name = name;
        this.x = value.getX();
        this.y = value.getY();
    }

    public String getName() {
        return name;
    }

    public String getX() {
        return x;
    }

    public Float getY() {
        return y;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CSVNamedXYTimeSeries{");
        sb.append("name='").append(name).append('\'');
        sb.append(", x='").append(x).append('\'');
        sb.append(", y=").append(y);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CSVNamedXYTimeSeries that = (CSVNamedXYTimeSeries) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (x != null ? !x.equals(that.x) : that.x != null) {
            return false;
        }
        if (y != null ? !y.equals(that.y) : that.y != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (x != null ? x.hashCode() : 0);
        result = 31 * result + (y != null ? y.hashCode() : 0);
        return result;
    }
}
