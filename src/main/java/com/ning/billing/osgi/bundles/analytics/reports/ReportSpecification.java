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

package com.ning.billing.osgi.bundles.analytics.reports;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

/**
 * Interprets the report filters specified by the user
 */
public class ReportSpecification {

    private static final Splitter REPORT_FILTERS_SPLITTER = Splitter.on(Pattern.compile("\\|"))
                                                                    .trimResults()
                                                                    .omitEmptyStrings();
    private static final Splitter FILTER_EXCLUDE_SPLITTER = Splitter.on(Pattern.compile("\\!\\="))
                                                                    .trimResults()
                                                                    .omitEmptyStrings();
    private static final Splitter FILTER_INCLUDE_SPLITTER = Splitter.on(Pattern.compile("\\="))
                                                                    .trimResults()
                                                                    .omitEmptyStrings();
    private final Multimap<String, String> exclusions = HashMultimap.create();
    private final Multimap<String, String> inclusions = HashMultimap.create();

    private final String rawReportName;

    private String reportName;

    public ReportSpecification(final String rawReportName) {
        this.rawReportName = rawReportName;
        parseRawReportName();
    }

    public String getReportName() {
        return reportName;
    }

    // return true if the value should not be graphed
    public boolean isFiltered(final String column, final String value) {
        return isExcluded(column, value) || !isIncluded(column, value);
    }

    private boolean isExcluded(final String column, final String value) {
        return exclusions.get(column).contains(value);
    }

    private boolean isIncluded(final String column, final String value) {
        // Return true if no inclusion
        return inclusions.get(column).size() == 0 || inclusions.get(column).contains(value);
    }

    private void parseRawReportName() {
        // rawReportName is in the form payments_per_day|currency=AUD|currency=EUR or payments_per_day|currency!=AUD|currency!=EUR
        final Iterator<String> reportIterator = REPORT_FILTERS_SPLITTER.split(rawReportName).iterator();

        boolean isFirst = true;
        while (reportIterator.hasNext()) {
            final String piece = reportIterator.next();

            if (isFirst) {
                reportName = piece;
            } else {
                final List<String> exclusion = ImmutableList.<String>copyOf(FILTER_EXCLUDE_SPLITTER.split(piece).iterator());
                final List<String> inclusion = ImmutableList.<String>copyOf(FILTER_INCLUDE_SPLITTER.split(piece).iterator());

                // Exclusions first
                if (exclusion.size() == 2) {
                    final String columnName = exclusion.get(0);
                    final String value = exclusion.get(1);
                    exclusions.put(columnName, value);
                } else if (inclusion.size() == 2) {
                    final String columnName = inclusion.get(0);
                    final String value = inclusion.get(1);
                    inclusions.put(columnName, value);
                } else if (exclusions.size() != 0 || inclusions.size() != 0) {
                    // Be lenient?
                    //throw new IllegalArgumentException();
                }
            }

            isFirst = false;
        }
    }
}
