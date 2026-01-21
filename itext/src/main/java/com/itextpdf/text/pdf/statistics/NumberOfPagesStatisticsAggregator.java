/*
 *
 * This file is part of the iText (R) project.
    Copyright (c) 1998-2026 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf.statistics;

import com.itextpdf.commons.actions.AbstractStatisticsAggregator;
import com.itextpdf.commons.actions.AbstractStatisticsEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Statistics aggregator which aggregates number of pages in PDF documents.
 */
public class NumberOfPagesStatisticsAggregator extends AbstractStatisticsAggregator {

    private static final int ONE = 1;
    private static final int TEN = 10;
    private static final int HUNDRED = 100;
    private static final int THOUSAND = 1000;

    private static final String STRING_FOR_ONE_PAGE = "1";
    private static final String STRING_FOR_TEN_PAGES = "2-10";
    private static final String STRING_FOR_HUNDRED_PAGES = "11-100";
    private static final String STRING_FOR_THOUSAND_PAGES = "101-1000";
    private static final String STRING_FOR_INF = "1001+";

    private static final Map<Integer, String> NUMBERS_OF_PAGES;

    // This List must be sorted.
    private static final List<Integer> SORTED_UPPER_BOUNDS_OF_PAGES = Arrays.asList(ONE, TEN, HUNDRED, THOUSAND);

    static {
        Map<Integer, String> temp = new HashMap<Integer, String>();
        temp.put(ONE, STRING_FOR_ONE_PAGE);
        temp.put(TEN, STRING_FOR_TEN_PAGES);
        temp.put(HUNDRED, STRING_FOR_HUNDRED_PAGES);
        temp.put(THOUSAND, STRING_FOR_THOUSAND_PAGES);
        NUMBERS_OF_PAGES = Collections.unmodifiableMap(temp);
    }

    private final Object lock = new Object();

    private final Map<String, Long> numberOfDocuments = new LinkedHashMap<String, Long>();

    /**
     * Aggregates number of pages from the provided event.
     *
     * @param event {@link NumberOfPagesStatisticsEvent} instance
     */
    @Override
    public void aggregate(AbstractStatisticsEvent event) {
        if (!(event instanceof NumberOfPagesStatisticsEvent)) {
            return;
        }
        int numberOfPages = ((NumberOfPagesStatisticsEvent) event).getNumberOfPages();
        String range = STRING_FOR_INF;
        for (final int upperBound : SORTED_UPPER_BOUNDS_OF_PAGES) {
            if (numberOfPages <= upperBound) {
                range = NUMBERS_OF_PAGES.get(upperBound);
                break;
            }
        }
        synchronized (lock) {
            Long documentsOfThisRange = numberOfDocuments.get(range);
            Long currentValue = documentsOfThisRange == null ? 1L : (documentsOfThisRange.longValue() + 1L);
            numberOfDocuments.put(range, currentValue);
        }
    }

    /**
     * Retrieves Map where keys are ranges of pages and values are the amounts of such PDF documents.
     *
     * @return aggregated {@link Map}
     */
    @Override
    public Object retrieveAggregation() {
        return Collections.unmodifiableMap(numberOfDocuments);
    }

    /**
     * Merges data about amounts of ranges of pages from the provided aggregator into this aggregator.
     *
     * @param aggregator {@link NumberOfPagesStatisticsAggregator} from which data will be taken.
     */
    @Override
    public void merge(AbstractStatisticsAggregator aggregator) {
        if (!(aggregator instanceof NumberOfPagesStatisticsAggregator)) {
            return;
        }

        Map<String, Long> numberOfDocuments = ((NumberOfPagesStatisticsAggregator) aggregator).numberOfDocuments;
        synchronized (lock) {
            merge(this.numberOfDocuments, numberOfDocuments);
        }
    }

    // Copied from com.itextpdf.commons.utils.MapUtil.merge, but Java 5 doesn't support lambdas
    private static void merge(Map<String, Long> destination, Map<String, Long> source) {
        if (destination == source) {
            return;
        }
        for (Map.Entry<String, Long> entry : source.entrySet()) {
            Long value = destination.get(entry.getKey());
            if (value == null) {
                destination.put(entry.getKey(), entry.getValue());
            } else {
                if (entry.getValue() == null) {
                    destination.put(entry.getKey(), value);
                } else {
                    destination.put(entry.getKey(), value + entry.getValue());
                }
            }
        }
    }
}
