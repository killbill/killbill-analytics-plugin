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

package org.killbill.billing.plugin.analytics.json;

import org.joda.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class XY {

    private final String x;
    private final Float y;

    private final LocalDate xDate;

    @JsonCreator
    public XY(@JsonProperty("x") final String x, @JsonProperty("y") final Float y) {
        this.x = x;
        this.y = y;
        this.xDate = new LocalDate(x);
    }

    public XY(final String x, final Integer y) {
        this(x, new Float(y.doubleValue()));
    }

    public XY(final LocalDate xDate, final Float y) {
        this.y = y;
        this.xDate = xDate;
        this.x = xDate.toString();
    }

    public String getX() {
        return x;
    }

    @JsonIgnore
    public LocalDate getxDate() {
        return xDate;
    }

    public Float getY() {
        return y;
    }
}
