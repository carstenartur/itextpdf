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
 * Statistics aggregator which aggregates size of PDF documents.
 */
public class SizeOfPdfStatisticsAggregator extends AbstractStatisticsAggregator {

    private static final long MEASURE_COEFFICIENT = 1024;

    private static final long SIZE_128KB = 128 * MEASURE_COEFFICIENT;
    private static final long SIZE_1MB = MEASURE_COEFFICIENT * MEASURE_COEFFICIENT;
    private static final long SIZE_16MB = 16 * MEASURE_COEFFICIENT * MEASURE_COEFFICIENT;
    private static final long SIZE_128MB = 128 * MEASURE_COEFFICIENT * MEASURE_COEFFICIENT;

    private static final String STRING_FOR_128KB = "<128kb";
    private static final String STRING_FOR_1MB = "128kb-1mb";
    private static final String STRING_FOR_16MB = "1mb-16mb";
    private static final String STRING_FOR_128MB = "16mb-128mb";
    private static final String STRING_FOR_INF = "128mb+";

    private static final Map<Long, String> DOCUMENT_SIZES;

    // This List must be sorted.
    private static final List<Long> SORTED_UPPER_BOUNDS_OF_SIZES =
            Arrays.asList(SIZE_128KB, SIZE_1MB, SIZE_16MB, SIZE_128MB);

    static {
        Map<Long, String> temp = new HashMap<Long, String>();
        temp.put(SIZE_128KB, STRING_FOR_128KB);
        temp.put(SIZE_1MB, STRING_FOR_1MB);
        temp.put(SIZE_16MB, STRING_FOR_16MB);
        temp.put(SIZE_128MB, STRING_FOR_128MB);
        DOCUMENT_SIZES = Collections.unmodifiableMap(temp);
    }

    private final Object lock = new Object();

    private final Map<String, Long> numberOfDocuments = new LinkedHashMap<String, Long>();

    /**
     * Aggregates size of the PDF document from the provided event.
     *
     * @param event {@link SizeOfPdfStatisticsEvent} instance
     */
    @Override
    public void aggregate(AbstractStatisticsEvent event) {
        if (!(event instanceof SizeOfPdfStatisticsEvent)) {
            return;
        }
        long sizeOfPdf = ((SizeOfPdfStatisticsEvent) event).getAmountOfBytes();
        String range = STRING_FOR_INF;
        for (final long upperBound : SORTED_UPPER_BOUNDS_OF_SIZES) {
            if (sizeOfPdf <= upperBound) {
                range = DOCUMENT_SIZES.get(upperBound);
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
     * Retrieves Map where keys are ranges of document sizes and values are the amounts of such PDF documents.
     *
     * @return aggregated {@link Map}
     */
    @Override
    public Object retrieveAggregation() {
        return Collections.unmodifiableMap(numberOfDocuments);
    }

    /**
     * Merges data about amounts of ranges of document sizes from the provided aggregator into this aggregator.
     *
     * @param aggregator {@link SizeOfPdfStatisticsAggregator} from which data will be taken.
     */
    @Override
    public void merge(AbstractStatisticsAggregator aggregator) {
        if (!(aggregator instanceof SizeOfPdfStatisticsAggregator)) {
            return;
        }

        Map<String, Long> amountOfDocuments = ((SizeOfPdfStatisticsAggregator) aggregator).numberOfDocuments;
        synchronized (lock) {
            merge(this.numberOfDocuments, amountOfDocuments);
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
