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

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jooq.AggregateFunction;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public abstract class Aggregates {

    private static final Pattern MAGIC_REGEXP = Pattern.compile("([a-z]+)\\(\\s*(distinct)?\\s*([a-zA-Z0-9_]+)\\s*\\)");

    public static AggregateFunction<?> of(final String input) {
        final Matcher matcher = MAGIC_REGEXP.matcher(input.toLowerCase());
        if (!matcher.find()) {
            // Not an aggregate?
            return null;
        }

        final SqlMapping sqlOp = SqlMapping.valueOf(matcher.group(1).toUpperCase());
        final boolean isDistinct = matcher.group(2) != null;
        // Note that the cast here is probably not correct (e.g. for count(distinct currency)),
        // but it's a workaround for jOOQ which requires Number fields for sum, avg, etc. below
        // Since we are simply generating the SQL, we don't really care what the actual column is.
        final Field<BigDecimal> field = DSL.fieldByName(SQLDataType.NUMERIC, matcher.group(3));

        return buildAggregateFunction(field, sqlOp, isDistinct);
    }

    private static AggregateFunction<?> buildAggregateFunction(final Field<? extends Number> field,
                                                               final SqlMapping sqlOp,
                                                               final boolean isDistinct) {
        switch (sqlOp) {
            case COUNT:
                return isDistinct ? DSL.countDistinct(field) : DSL.count(field);
            case MAX:
                return isDistinct ? DSL.maxDistinct(field) : DSL.max(field);
            case MIN:
                return isDistinct ? DSL.minDistinct(field) : DSL.min(field);
            case SUM:
                return isDistinct ? DSL.sumDistinct(field) : DSL.sum(field);
            case AVG:
                return isDistinct ? DSL.avgDistinct(field) : DSL.avg(field);
            case MEDIAN:
                return DSL.median(field);
            case STDDEV_POP:
                return DSL.stddevPop(field);
            case STDDEV_SAMP:
                return DSL.stddevSamp(field);
            case VAR_POP:
                return DSL.varPop(field);
            case VAR_SAMP:
                return DSL.varSamp(field);
            default:
                throw new IllegalStateException("Unknown operation " + sqlOp);
        }
    }

    private enum SqlMapping {
        COUNT("count"),
        MAX("max"),
        MIN("min"),
        SUM("sum"),
        AVG("avg"),
        MEDIAN("median"),
        STDDEV_POP("stddev_pop"),
        STDDEV_SAMP("stddev_samp"),
        VAR_POP("var_pop"),
        VAR_SAMP("var_samp");

        private final String representation;

        SqlMapping(final String representation) {
            this.representation = representation;
        }

        public String getRepresentation() {
            return representation;
        }
    }
}
