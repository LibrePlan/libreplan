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

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.data.limitingresource.QueueTask;
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

    private final LimitingResourceQueue loadLine;
    private final TimeTracker timeTracker;
    private transient IZoomLevelChangedListener zoomChangedListener;

    private LimitingResourcesComponent(final TimeTracker timeTracker,
            final LimitingResourceQueue limitingResourceQueue) {
        this.loadLine = limitingResourceQueue;
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
        List<Div> divs = createDivsForPeriods(mapper, limitingResourceQueue
                .getLimitingResourceQueueElements());
        for (Div div : divs) {
            appendChild(div);
        }
    }

    public String getResourceLoadName() {
        return loadLine.getResource().getName();
    }

    private static List<Div> createDivsForPeriods(IDatesMapper datesMapper,
            List<LimitingResourceQueueElement> list) {
        List<Div> result = new ArrayList<Div>();
        for (LimitingResourceQueueElement loadPeriod : list) {
            result.add(createDivForPeriod(datesMapper, loadPeriod));
        }
        return result;
    }

    private static Div createDivForPeriod(IDatesMapper datesMapper,
            LimitingResourceQueueElement loadPeriod) {
        Div result = new Div();
        result.setClass("queue element");

        result.setTooltiptext("Tooltip");

        result.setLeft(forCSS(getStartPixels(datesMapper, loadPeriod)));
        result.setWidth(forCSS(getWidthPixels(datesMapper, loadPeriod)));
        return result;
    }

    private static int getWidthPixels(IDatesMapper datesMapper,
            QueueTask loadPeriod) {
        LocalDate start = loadPeriod.getStart();
        LocalDate end = loadPeriod.getEnd();
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
            LimitingResourceQueueElement loadPeriod) {
        return datesMapper.toPixels(loadPeriod.getResourceAllocation()
                .getStartDate().toDateMidnight()
                .toDate());
    }

}