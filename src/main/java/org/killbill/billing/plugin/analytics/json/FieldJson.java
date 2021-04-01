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

import javax.annotation.Nullable;

import org.jooq.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldJson {

    private final String name;
    private final String dataType;
    private final List<Object> distinctValues;

    public FieldJson(final Field<?> field, @Nullable final List<Object> distinctValues) {
        this(field.getName(), field.getDataType().getTypeName(), distinctValues);
    }

    public FieldJson(@JsonProperty("name") final String name,
                     @JsonProperty("dataType") final String dataType,
                     @JsonProperty("distinctValues") final List<Object> distinctValues) {
        this.name = name;
        this.dataType = dataType;
        this.distinctValues = distinctValues;
    }

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public List<Object> getDistinctValues() {
        return distinctValues;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FieldJson{");
        sb.append("name='").append(name).append('\'');
        sb.append(", dataType='").append(dataType).append('\'');
        sb.append(", distinctValues=").append(distinctValues);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldJson fieldJson = (FieldJson) o;

        if (dataType != null ? !dataType.equals(fieldJson.dataType) : fieldJson.dataType != null) {
            return false;
        }
        if (distinctValues != null ? !distinctValues.equals(fieldJson.distinctValues) : fieldJson.distinctValues != null) {
            return false;
        }
        if (name != null ? !name.equals(fieldJson.name) : fieldJson.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        result = 31 * result + (distinctValues != null ? distinctValues.hashCode() : 0);
        return result;
    }
}
