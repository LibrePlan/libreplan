/*
 * This file is part of ###PROJECT_NAME###
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

package org.zkoss.ganttz.resourceload;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.data.resourceload.LoadPeriod;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zul.Div;
import org.zkoss.zul.impl.XulElement;

/**
 * This class wraps ResourceLoad data inside an specific HTML Div component.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class ResourceLoadComponent extends XulElement {

    public static ResourceLoadComponent create(TimeTracker timeTracker,
            LoadTimeLine loadLine) {
        return new ResourceLoadComponent(timeTracker, loadLine);
    }

    private final LoadTimeLine loadLine;
    private final TimeTracker timeTracker;
    private transient IZoomLevelChangedListener zoomChangedListener;

    private ResourceLoadComponent(final TimeTracker timeTracker,
            final LoadTimeLine loadLine) {
        this.loadLine = loadLine;
        this.timeTracker = timeTracker;
        createChildren(loadLine, timeTracker.getMapper());
        zoomChangedListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                getChildren().clear();
                createChildren(loadLine, timeTracker.getMapper());
                invalidate();
            }
        };
        this.timeTracker.addZoomListener(zoomChangedListener);
    }

    private void createChildren(LoadTimeLine loadLine, IDatesMapper mapper) {
        List<Div> divs = createDivsForPeriods(mapper, loadLine.getLoadPeriods());
        for (Div div : divs) {
            appendChild(div);
        }
    }

    public String getResourceLoadName() {
        return loadLine.getConceptName();
    }

    private static List<Div> createDivsForPeriods(IDatesMapper datesMapper,
            List<LoadPeriod> loadPeriods) {
        List<Div> result = new ArrayList<Div>();
        for (LoadPeriod loadPeriod : loadPeriods) {
            result.add(createDivForPeriod(datesMapper, loadPeriod));
        }
        return result;
    }

    private static Div createDivForPeriod(IDatesMapper datesMapper,
            LoadPeriod loadPeriod) {
        Div result = new Div();
        result.setClass(String.format("taskassignmentinterval %s", loadPeriod
                .getLoadLevel().getCategory()));
        result.setTooltiptext("Load: "
                + loadPeriod.getLoadLevel().getPercentage() + "%");

        result.setLeft(forCSS(getStartPixels(datesMapper, loadPeriod)));
        result.setWidth(forCSS(getWidthPixels(datesMapper, loadPeriod)));
        return result;
    }

    private static int getWidthPixels(IDatesMapper datesMapper,
            LoadPeriod loadPeriod) {
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
            LoadPeriod loadPeriod) {
        return datesMapper.toPixels(loadPeriod.getStart().toDateMidnight()
                .toDate());
    }

}