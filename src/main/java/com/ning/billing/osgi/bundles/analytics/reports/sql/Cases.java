/*
 * Copyright 2010-2014 Ning, Inc.
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

package com.ning.billing.osgi.bundles.analytics.reports.sql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jooq.Case;
import org.jooq.CaseConditionStep;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import com.google.common.base.Splitter;

public abstract class Cases {

    private static final Pattern MAGIC_REGEXP = Pattern.compile("([a-zA-Z0-9_]+)(\\(\\s*([a-zA-Z0-9,|_]+)\\s*\\))?");

    private static final Splitter GROUPS_SPLITTER = Splitter.on(Pattern.compile("\\|"))
                                                            .trimResults()
                                                            .omitEmptyStrings();
    private static final Splitter VALUES_IN_GROUP_SPLITTER = Splitter.on(Pattern.compile("\\,"))
                                                                     .trimResults()
                                                                     .omitEmptyStrings();
    private static final String OTHER = "Other";

    // For grouping, input is in the form: currency(USD|BRL,GBP,EUR,MXN,AUD)
    public static Field<Object> of(final String input) {
        final Matcher matcher = MAGIC_REGEXP.matcher(input);
        if (!matcher.find()) {
            // Shouldn't happen?
            return null;
        }

        final Field<Object> column = DSL.fieldByName(matcher.group(1));
        final String grouping = matcher.group(3);

        if (grouping == null) {
            return column;
        } else {
            final Iterable<String> columnGroups = GROUPS_SPLITTER.split(grouping);
            return buildCaseStatementForColumn(column, columnGroups).as(column.getName());
        }
    }

    private static Field<Object> buildCaseStatementForColumn(final Field<Object> column, final Iterable<String> columnGroups) {
        Case decode = DSL.decode();
        CaseConditionStep caseConditionStep = null;
        for (final String columnGroup : columnGroups) {
            for (final String columnValue : VALUES_IN_GROUP_SPLITTER.split(columnGroup)) {
                final Condition condition = column.eq(columnValue);
                caseConditionStep = caseConditionStep == null ? decode.when(condition, columnGroup)
                                                              : caseConditionStep.when(condition, columnGroup);
            }
        }

        return caseConditionStep.otherwise(OTHER);
    }
}
