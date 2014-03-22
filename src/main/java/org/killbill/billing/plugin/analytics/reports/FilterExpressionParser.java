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

package org.killbill.billing.plugin.analytics.reports;

import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Literal;
import com.bpodgursky.jbool_expressions.Not;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;
import com.bpodgursky.jbool_expressions.parsers.IdentityMap;
import com.bpodgursky.jbool_expressions.parsers.TokenMapper;
import com.google.common.collect.Lists;

// Custom grammar to support special characters (e.g. '=')
public class FilterExpressionParser {

    public static Expression<String> parse(String expression) {
        return parse(expression, new IdentityMap());
    }

    public static <T> Expression<T> parse(String expression, TokenMapper<T> mapper) {
        try {
            final ANTLRStringStream input = new ANTLRStringStream(expression);
            final TokenStream tokens = new CommonTokenStream(new KillBillBooleanExprLexer(input));

            final KillBillBooleanExprParser parser = new KillBillBooleanExprParser(tokens);
            final KillBillBooleanExprParser.expression_return ret = parser.expression();

            final CommonTree ast = (CommonTree) ret.getTree();
            return parse(ast, mapper);
        } catch (RecognitionException e) {
            throw new IllegalStateException("Recognition exception is never thrown, only declared.");
        }
    }

    public static <T> Expression<T> parse(Tree tree, TokenMapper<T> mapper) {
        if (tree.getType() == KillBillBooleanExprParser.AND) {
            final List<Expression<T>> children = Lists.newArrayList();
            for (int i = 0; i < tree.getChildCount(); i++) {
                children.add(parse(tree.getChild(i), mapper));
            }
            return And.of(children);
        } else if (tree.getType() == KillBillBooleanExprParser.OR) {
            final List<Expression<T>> children = Lists.newArrayList();
            for (int i = 0; i < tree.getChildCount(); i++) {
                children.add(parse(tree.getChild(i), mapper));
            }
            return Or.of(children);
        } else if (tree.getType() == KillBillBooleanExprParser.NOT) {
            return Not.of(parse(tree.getChild(0), mapper));
        } else if (tree.getType() == KillBillBooleanExprParser.SQL_EXPRESSION) {
            return Variable.of(mapper.getVariable(tree.getText()));
        } else if (tree.getType() == KillBillBooleanExprParser.TRUE) {
            return Literal.getTrue();
        } else if (tree.getType() == KillBillBooleanExprParser.FALSE) {
            return Literal.getFalse();
        } else {
            throw new RuntimeException("Unrecognized! " + tree.getType() + " " + tree.getText());
        }
    }
}
