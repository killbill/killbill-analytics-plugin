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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Or;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * Interprets the report filters specified by the user
 */
public class ReportSpecification {

    private static final Pattern LEGEND_PATTERN = Pattern.compile("([a-zA-Z0-9=\\s,|_-~\\?!@#$%^&*\\+\\[\\]\\\\;:‘\"<>,./]+)(\\(\\s*([a-zA-Z0-9=\\s,|_-~\\?!@#$%^&*\\+\\[\\]\\\\;:‘\"<>,./]+)\\s*\\))?");

    private static final Splitter REPORT_SPECIFICATIONS_SPLITTER = Splitter.on(Pattern.compile("\\^"))
                                                                           .trimResults()
                                                                           .omitEmptyStrings();
    private static final Splitter REPORT_SPECIFICATION_SPLITTER = Splitter.on(Pattern.compile("\\:"))
                                                                          .trimResults()
                                                                          .omitEmptyStrings();
    private static final Splitter REPORT_GROUPING_SPECIFICATION_SPLITTER = Splitter.on(Pattern.compile("\\("))
                                                                                   .trimResults()
                                                                                   .omitEmptyStrings();

    private final List<String> dimensions = new LinkedList<String>();
    private final List<String> dimensionsWithGrouping = new LinkedList<String>();
    private final List<String> metrics = new LinkedList<String>();
    private Expression<String> filterExpression = null;

    private enum ValidKeywords {
        DIMENSION,
        METRIC,
        FILTER
    }

    private final String rawReportName;

    private String reportName;
    private String legend;

    public ReportSpecification(final String rawReportName) {
        this.rawReportName = rawReportName;
        parseRawReportName();
    }

    public String getReportName() {
        return reportName;
    }

    public String getLegend() {
        return legend;
    }

    public List<String> getDimensions() {
        return dimensions;
    }

    public List<String> getDimensionsWithGrouping() {
        return dimensionsWithGrouping;
    }

    public List<String> getMetrics() {
        return metrics;
    }

    public Expression<String> getFilterExpression() {
        return filterExpression;
    }

    private void parseRawReportName() {
        // rawReportName is in the form: payments_per_day(Currency report)^filter:currency=AUD^filter:currency=EUR^dimension:currency^dimension:state^metric:amount^metric:fee
        final Iterator<String> reportIterator = REPORT_SPECIFICATIONS_SPLITTER.split(rawReportName).iterator();

        boolean isFirst = true;
        while (reportIterator.hasNext()) {
            // rawSpecification is in the form: dimension:currency
            final String rawSpecification = reportIterator.next();

            // The report name should be the first token
            if (isFirst) {
                isFirst = false;

                // Extract the alias, if present: payments_per_day(Currency report)
                final Matcher matcher = LEGEND_PATTERN.matcher(rawSpecification);
                if (!matcher.find()) {
                    reportName = rawSpecification;
                    legend = null;
                } else {
                    reportName = matcher.group(1);
                    legend = Strings.emptyToNull(matcher.group(3));
                }

                continue;
            }

            final List<String> specification = ImmutableList.<String>copyOf(REPORT_SPECIFICATION_SPLITTER.split(rawSpecification).iterator());
            if (specification.size() != 2) {
                // Be lenient
                continue;
            }
            final String keywordString = specification.get(0);
            final String value = specification.get(1);

            final ValidKeywords keyword;
            try {
                keyword = ValidKeywords.valueOf(keywordString.toUpperCase());
            } catch (final IllegalArgumentException e) {
                // Be lenient
                continue;
            }

            switch (keyword) {
                case DIMENSION:
                    dimensions.add(REPORT_GROUPING_SPECIFICATION_SPLITTER.split(value).iterator().next());
                    // value is something like: currency(USD|EUR)
                    dimensionsWithGrouping.add(value);
                    break;
                case METRIC:
                    metrics.add(value);
                    break;
                case FILTER:
                    // value is something like: (currency=USD&state!=ERRORED)|(currency=EUR&currency=PROCESSED)
                    final Expression<String> thisFilterExpression = FilterExpressionParser.parse(value);
                    if (filterExpression == null) {
                        filterExpression = thisFilterExpression;
                    } else {
                        // Multiple filter expressions are OR'ed by default
                        filterExpression = Or.of(filterExpression, thisFilterExpression);
                    }
                    break;
            }
        }
    }
}
