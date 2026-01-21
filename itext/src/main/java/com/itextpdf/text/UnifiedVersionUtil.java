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
package com.itextpdf.text;

import com.itextpdf.commons.actions.AbstractITextConfigurationEvent;
import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.commons.actions.confirmations.ConfirmEvent;
import com.itextpdf.commons.actions.producer.ProducerBuilder;
import com.itextpdf.text.actions.data.IText5ProductData;
import com.itextpdf.text.actions.events.IText5ProductEvent;
import com.itextpdf.text.pdf.statistics.NumberOfPagesStatisticsEvent;
import com.itextpdf.text.pdf.statistics.SizeOfPdfStatisticsEvent;

import java.util.ArrayList;
import java.util.List;

final class UnifiedVersionUtil {
    private static final UnifiedVersionEvent UNIFIED_VERSION_EVENT =  new UnifiedVersionEvent();

    private UnifiedVersionUtil() {
        // empty constructor
    }

    static void onEventUsage() {
        IText5ProductEvent processPdfUnifiedEvent = IText5ProductEvent.createProcessPdfEvent();

        EventManager.getInstance().onEvent(processPdfUnifiedEvent);
        EventManager.getInstance().onEvent(new ConfirmEvent(processPdfUnifiedEvent));
    }

    static void onEventStatistic(long amountOfBytes, int numberOfPages) {
        EventManager.getInstance().onEvent(new SizeOfPdfStatisticsEvent(amountOfBytes, IText5ProductData.getInstance()));
        EventManager.getInstance().onEvent(new NumberOfPagesStatisticsEvent(numberOfPages, IText5ProductData.getInstance()));
    }

    static String getProducer(String oldProducer) {
        IText5ProductEvent processPdfUnifiedEvent = IText5ProductEvent.createProcessPdfEvent();
        List<IText5ProductEvent> unifiedEvents = new ArrayList<IText5ProductEvent>();
        unifiedEvents.add(processPdfUnifiedEvent);
        return ProducerBuilder.modifyProducer(unifiedEvents, oldProducer);
    }

    static boolean isAGPLVersion() {
        // returns false if unified license has been loaded, otherwise true
        return UNIFIED_VERSION_EVENT.isAGPLVersion();
    }

    private static class UnifiedVersionEvent extends AbstractITextConfigurationEvent {
        @Override
        protected void doAction() {
            throw new IllegalStateException("Configuration events for util internal purposes are not expected to be sent");
        }

        boolean isAGPLVersion() {
            return getActiveProcessor(IText5ProductData.getInstance().getProductName()) == null;
        }
    }
}
