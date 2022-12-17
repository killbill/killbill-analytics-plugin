/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2022 Equinix, Inc
 * Copyright 2014-2022 The Billing Project, LLC
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

import java.util.List;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.impl.DSL;
import org.killbill.billing.plugin.analytics.reports.ReportsUserApi;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Literal;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public abstract class Filters {

    public static Condition of(final Expression<String> expression) {
        return buildConditionFromExpression(expression);
    }

    private enum SqlMapping {
        // Order matters! See below
        GE(">="),
        LE("<="),
        GT(">"),
        LT("<"),
        NE("!="),
        EQ("="),
        NOT_LIKE("!~"),
        LIKE("~");

        private final String representation;
        private final Splitter splitter;

        SqlMapping(final String representation) {
            this.representation = representation;
            this.splitter = Splitter.on(representation).omitEmptyStrings().trimResults().limit(2);
        }

        public String getRepresentation() {
            return representation;
        }

        public Splitter getSplitter() {
            return splitter;
        }
    }

    private static Condition buildConditionFromVariable(final Variable<String> input) {
        SqlMapping sqlOp = null;
        String column = null;
        String expression = null;
        for (final SqlMapping sqlMapping : SqlMapping.values()) {
            final List<String> parts = ImmutableList.<String>copyOf(sqlMapping.getSplitter().split(input.getValue()));
            if (parts.size() != 2) {
                continue;
            }

            // Take the first match in case of equality (see above)
            if (column == null || parts.get(0).length() < column.length()) {
                column = parts.get(0);
                expression = parts.get(1);
                sqlOp = sqlMapping;
            }
        }

        if (sqlOp != null) {
            // Un-quote the expression if needed (to avoid double quoting)
            expression = expression.replaceFirst("^[\"']", "");
            expression = expression.replaceFirst("[\"']$", "");

            Name name = DSL.name(column);
            if (ReportsUserApi.DAY_COLUMN_NAME.equals(column)) {
                // Reserved keyword
                name = name.quotedName();
            }

            final Field<Object> field = DSL.field(name);
            switch (sqlOp) {
                case EQ:
                    return field.eq(expression);
                case NE:
                    return field.ne(expression);
                case GT:
                    return field.gt(expression);
                case LT:
                    return field.lt(expression);
                case GE:
                    return field.ge(expression);
                case LE:
                    return field.le(expression);
                case LIKE:
                    return field.like(expression);
                case NOT_LIKE:
                    return field.notLike(expression);
                default:
                    throw new IllegalStateException("Unknown operation " + sqlOp);
            }
        } else {
            return DSL.trueCondition();
        }
    }

    @VisibleForTesting
    static Condition buildConditionFromExpression(final Expression<String> expression) {
        if (expression instanceof And) {
            Condition condition = null;
            for (final Expression<String> childExpression : ((And<String>) expression).getChildren()) {
                final Condition newCondition = buildConditionFromExpression(childExpression);
                condition = condition == null ? newCondition : condition.and(newCondition);
            }
            return condition;
        } else if (expression instanceof Literal) {
            final boolean value = ((Literal) expression).getValue();
            if (value) {
                return DSL.trueCondition();
            } else {
                return DSL.falseCondition();
            }
        } else if (expression instanceof Not) {
            return DSL.trueCondition().andNot(buildConditionFromExpression(((Not<String>) expression).getE()));
        } else if (expression instanceof Or) {
            Condition condition = null;
            for (final Expression<String> childExpression : ((Or<String>) expression).getChildren()) {
                final Condition newCondition = buildConditionFromExpression(childExpression);
                condition = condition == null ? newCondition : condition.or(newCondition);
            }
            return condition;
        } else if (expression instanceof Variable) {
            return buildConditionFromVariable((Variable<String>) expression);
        } else {
            throw new IllegalStateException();
        }
    }
}
