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

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.killbill.billing.plugin.analytics.reports.KillBillArithmeticExprLexer;
import org.killbill.billing.plugin.analytics.reports.KillBillArithmeticExprParser;

import com.google.common.base.MoreObjects;

public class MetricExpressionParser {

    public static FieldWithMetadata parse(final String expression) {
        try {
            final ANTLRStringStream input = new ANTLRStringStream(expression);
            final TokenStream tokens = new CommonTokenStream(new KillBillArithmeticExprLexer(input));

            final KillBillArithmeticExprParser parser = new KillBillArithmeticExprParser(tokens);
            final KillBillArithmeticExprParser.expression_return ret = parser.expression();

            final CommonTree ast = (CommonTree) ret.getTree();
            return parse(ast);
        } catch (RecognitionException e) {
            throw new IllegalStateException("Recognition exception is never thrown, only declared.");
        }
    }

    private static FieldWithMetadata parse(final Tree tree) {
        if (tree.getType() == KillBillArithmeticExprParser.NUMBER) {
            final Field field = DSL.val(tree.getText(), SQLDataType.NUMERIC);
            return new FieldWithMetadata(field, false);
        } else if (tree.getType() == KillBillArithmeticExprParser.COLUMNNAME) {
            final Field function = Aggregates.of(tree.getText());
            final Field column = DSL.fieldByName(tree.getText());
            final Field field = MoreObjects.firstNonNull(function, column);
            return new FieldWithMetadata(field, function != null);
        } else {
            boolean hasAggregateFunction = false;
            Field field = null;
            for (int i = 0; i < tree.getChildCount(); i++) {
                final FieldWithMetadata parsed = parse(tree.getChild(i));
                hasAggregateFunction = hasAggregateFunction || parsed.hasAggregateFunction();
                final Field childValue = parsed.getField();
                field = field == null ? childValue : parse(tree, field, childValue);
            }
            return new FieldWithMetadata(field, hasAggregateFunction);
        }
    }

    private static Field parse(final Tree tree, final Field field, final Field childValue) {
        if (tree.getType() == KillBillArithmeticExprParser.PLUS) {
            return field.add(childValue);
        } else if (tree.getType() == KillBillArithmeticExprParser.MINUS) {
            return field.minus(childValue);
        } else if (tree.getType() == KillBillArithmeticExprParser.MULT) {
            return field.multiply(childValue);
        } else if (tree.getType() == KillBillArithmeticExprParser.DIV) {
            return field.divide(childValue);
        } else if (tree.getType() == KillBillArithmeticExprParser.MOD) {
            return field.mod(childValue);
        } else {
            throw new RuntimeException("Unrecognized! " + tree.getType() + " " + tree.getText());
        }
    }

    public static final class FieldWithMetadata {

        private final Field field;
        private final boolean hasAggregateFunction;

        public FieldWithMetadata(final Field field, final boolean hasAggregateFunction) {
            this.field = field;
            this.hasAggregateFunction = hasAggregateFunction;
        }

        public Field getField() {
            return field;
        }

        public boolean hasAggregateFunction() {
            return hasAggregateFunction;
        }

        @Override
        public String toString() {
            return field.toString();
        }
    }
}
