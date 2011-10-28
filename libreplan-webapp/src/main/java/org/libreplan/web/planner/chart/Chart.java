/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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
package org.libreplan.web.planner.chart;

import org.apache.commons.lang.Validate;
import org.zkforge.timeplot.Timeplot;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class Chart {

    private final Timeplot timeplot;
    private final IChartFiller filler;
    private final TimeTracker timeTracker;

    public Chart(Timeplot timeplot, IChartFiller filler,
            TimeTracker timeTracker) {
        Validate.notNull(filler);
        Validate.notNull(timeTracker);
        Validate.notNull(timeplot);
        this.timeplot = timeplot;
        this.filler = filler;
        this.timeTracker = timeTracker;
    }

    public void fillChart() {
        filler.fillChart(timeplot, timeTracker.getRealInterval(), timeTracker.getHorizontalSize());
    }

    public void setZoomLevel(ZoomLevel zoomLevel) {
        if (zoomLevel != null) {
            filler.setZoomLevel(zoomLevel);
        }
    }

}
