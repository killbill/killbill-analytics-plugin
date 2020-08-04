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

package org.killbill.billing.plugin.analytics.reports.sql;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jooq.Case;
import org.jooq.CaseConditionStep;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public abstract class Cases {

    private static final Pattern MAGIC_REGEXP = Pattern.compile("([a-zA-Z0-9_]+)(\\(\\s*([a-zA-Z0-9=\\s,|_-]+)\\s*\\))?");

    private static final Splitter GROUPS_SPLITTER = Splitter.on(Pattern.compile("\\|"))
                                                            .trimResults()
                                                            .omitEmptyStrings();
    private static final Splitter VALUES_IN_GROUP_SPLITTER = Splitter.on(Pattern.compile("\\,"))
                                                                     .trimResults()
                                                                     .omitEmptyStrings();
    private static final String VALUES_IN_GROUP_ALIAS_TOKEN = "=";
    private static final String SKIP_OTHER_TOKEN = "-";
    private static final String OTHER = "Other";

    // For grouping, input is in the form: currency(USD|BRL,GBP,EUR,MXN,AUD)
    public static FieldWithMetadata of(final String input) {
        final Matcher matcher = MAGIC_REGEXP.matcher(input);
        if (!matcher.find()) {
            // Shouldn't happen?
            return null;
        }

        final Field<Object> column = DSL.fieldByName(matcher.group(1));
        final String grouping = matcher.group(3);

        if (grouping == null) {
            return new FieldWithMetadata(column, null);
        } else {
            final Iterable<String> columnGroups = GROUPS_SPLITTER.split(grouping);
            return buildCaseStatementForColumn(column, columnGroups);
        }
    }

    private static FieldWithMetadata buildCaseStatementForColumn(final Field<Object> column, final Iterable<String> columnGroups) {
        final List<String> allAcceptableValues = new LinkedList<String>();

        boolean withOther = true;
        Case decode = DSL.decode();
        CaseConditionStep caseConditionStep = null;
        for (final String columnGroupWithAlias : columnGroups) {
            final String[] columnGroupAndAlias = columnGroupWithAlias.split(VALUES_IN_GROUP_ALIAS_TOKEN);
            final String columnGroup = columnGroupAndAlias[0];

            final List<String> columnValues = ImmutableList.<String>copyOf(VALUES_IN_GROUP_SPLITTER.split(columnGroup));
            // Append '-' as a sign to skip the Other group: currency(USD|BRL,GBP,EUR,MXN,AUD|-)
            if (columnValues.size() == 1 && SKIP_OTHER_TOKEN.equals(columnValues.get(0))) {
                withOther = false;
                continue;
            }

            final String columnGroupAlias = columnGroupAndAlias.length == 1 ? columnGroup : columnGroupAndAlias[1];
            for (final String columnValue : columnValues) {
                allAcceptableValues.add(columnValue);
                final Condition condition = column.eq(columnValue);
                caseConditionStep = caseConditionStep == null ? decode.when(condition, columnGroupAlias)
                                                              : caseConditionStep.when(condition, columnGroupAlias);
            }
        }

        final Field field = withOther ? caseConditionStep.otherwise(OTHER) : caseConditionStep;
        final Condition condition = withOther ? null : DSL.field(column.getName()).in(allAcceptableValues.toArray());

        return new FieldWithMetadata(field.as(column.getName()), condition);
    }


    public static final class FieldWithMetadata {

        private final Field<Object> field;
        private final Condition condition;

        private FieldWithMetadata(final Field<Object> field, final Condition condition) {
            this.field = field;
            this.condition = condition;
        }

        public Field<Object> getField() {
            return field;
        }

        public Condition getCondition() {
            return condition;
        }

        @Override
        public String toString() {
            return field.toString();
        }
    }
}
