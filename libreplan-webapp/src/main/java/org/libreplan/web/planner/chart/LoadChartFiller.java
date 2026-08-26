/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

import java.util.Arrays;

import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;

public abstract class LoadChartFiller extends ChartFiller {

    /** Black */
    public static final String COLOR_CAPABILITY_LINE = "#000000";

    /** Green */
    public static final String COLOR_ASSIGNED_LOAD = "#98D471";

    /** Red */
    public static final String COLOR_OVERLOAD = "#FF5A11";

    @Override
    public void fillChart(Div chart, Interval interval, Integer size) {
        if (getOptionalJavascriptCall() != null) {
            Clients.evalJavaScript(getOptionalJavascriptCall());
        }

        // Capacity/load/overload are constant for a given day, not gradually changing within it
        // - render as a step (flat-topped, no diagonal transition between days), not a straight-
        // line interpolation. See ChartFiller.renderChart(..., boolean stepped).
        renderChart(chart, Arrays.asList(getPlotInfo(interval)), interval, size, true);
    }


    protected abstract String getOptionalJavascriptCall();

    /**
     * The order must be from the topmost one to the lowest one.
     *
     * @param interval
     * @return the {@link ChartSeries series} to show
     */
    protected abstract ChartSeries[] getPlotInfo(Interval interval);

}
