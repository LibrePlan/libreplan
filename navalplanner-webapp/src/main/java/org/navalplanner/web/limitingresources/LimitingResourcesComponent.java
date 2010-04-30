/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galic
 *                    ia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.limitingresources;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zul.Div;
import org.zkoss.zul.impl.XulElement;

/**
 * This class wraps ResourceLoad data inside an specific HTML Div component.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class LimitingResourcesComponent extends XulElement {

    public static LimitingResourcesComponent create(TimeTracker timeTracker,
            LimitingResourceQueue limitingResourceQueue) {
        return new LimitingResourcesComponent(timeTracker,
                limitingResourceQueue);
    }

    private final LimitingResourceQueue limitingResourceQueue;
    private final TimeTracker timeTracker;
    private transient IZoomLevelChangedListener zoomChangedListener;

    private LimitingResourcesComponent(final TimeTracker timeTracker,
            final LimitingResourceQueue limitingResourceQueue) {
        this.limitingResourceQueue = limitingResourceQueue;
        this.timeTracker = timeTracker;
        createChildren(limitingResourceQueue, timeTracker.getMapper());
        zoomChangedListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                getChildren().clear();
                createChildren(limitingResourceQueue, timeTracker.getMapper());
                invalidate();
            }
        };
        this.timeTracker.addZoomListener(zoomChangedListener);
    }

    private void createChildren(LimitingResourceQueue limitingResourceQueue,
            IDatesMapper mapper) {
        List<Div> divs = createDivsForQueueElements(mapper,
                limitingResourceQueue.getLimitingResourceQueueElements());
        if (divs != null) {
            for (Div div : divs) {
                appendChild(div);
            }
        }
    }

    public String getResourceName() {
        return limitingResourceQueue.getResource().getName();
    }

    private static List<Div> createDivsForQueueElements(
            IDatesMapper datesMapper,
            Set<LimitingResourceQueueElement> list) {
        List<Div> result = new ArrayList<Div>();

        for (LimitingResourceQueueElement queueElement : list) {
            validateQueueElement(queueElement);
            result.add(createDivForQueueElement(datesMapper, queueElement));
        }

        // FIX: adding static elements
        result.add(createFakeDivForQueueElement(datesMapper));

        return result;
    }

    private static void validateQueueElement(
            LimitingResourceQueueElement queueElement) {
        if ((queueElement.getStartDate() == null)
                || (queueElement.getStartDate() == null)) {
            throw new ValidationException(_("Invalid queue element"));
        }
    }

    private static Div createFakeDivForQueueElement(IDatesMapper datesMapper) {
        Div result = new Div();
        result.setClass("queue-element");

        result.setTooltiptext("Tooltip");

        result.setLeft("200px");
        result.setWidth("200px");
        return result;
    }

    private static Div createDivForQueueElement(IDatesMapper datesMapper,
            LimitingResourceQueueElement loadPeriod) {
        Div result = new Div();
        result.setClass("queue-element");

        result.setTooltiptext("Tooltip");

        result.setLeft(forCSS(getStartPixels(datesMapper, loadPeriod)));
        result.setWidth(forCSS(getWidthPixels(datesMapper, loadPeriod)));
        return result;
    }

    private static int getWidthPixels(IDatesMapper datesMapper,
            LimitingResourceQueueElement loadPeriod) {
        LocalDate start = loadPeriod.getStartDate();
        LocalDate end = loadPeriod.getEndDate();
        return datesMapper
                .toPixels(toMilliseconds(end) - toMilliseconds(start));
    }

    private static long toMilliseconds(LocalDate localDate) {
        return localDate.toDateMidnight().getMillis();
    }

    private static String forCSS(int pixels) {
        return String.format("%dpx", pixels);
    }

    private static int getStartPixels(IDatesMapper datesMapper,
            LimitingResourceQueueElement queueElement) {
        return datesMapper.toPixels(queueElement.getStartDate().toDateMidnight()
                .toDate());
    }

}