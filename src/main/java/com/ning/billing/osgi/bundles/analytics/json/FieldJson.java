/*
 * Copyright 2010-2013 Ning, Inc.
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

package com.ning.billing.osgi.bundles.analytics.json;

import org.jooq.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldJson {

    private final String name;
    private final String dataType;

    public FieldJson(final Field<?> field) {
        this(field.getName(), field.getDataType() == null ? null : field.getDataType().getTypeName());
    }

    public FieldJson(@JsonProperty("name") final String name,
                     @JsonProperty("dataType") final String dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FieldJson{");
        sb.append("name='").append(name).append('\'');
        sb.append(", dataType='").append(dataType).append('\'');
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
        if (name != null ? !name.equals(fieldJson.name) : fieldJson.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        return result;
    }
}
