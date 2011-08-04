/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.navalplanner.web.planner.chart;

import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.util.Clients;

public abstract class LoadChartFiller extends ChartFiller {

    public static final String COLOR_CAPABILITY_LINE = "#000000"; // Black
    public static final String COLOR_ASSIGNED_LOAD = "#98D471"; // Green
    public static final String COLOR_OVERLOAD = "#FF5A11"; // Red

    @Override
    public void fillChart(Timeplot chart, Interval interval, Integer size) {
        chart.getChildren().clear();
        chart.invalidate();

        if (getOptionalJavascriptCall() != null) {
            Clients.evalJavaScript(getOptionalJavascriptCall());
        }
        resetMinimumAndMaximumValueForChart();

        ValueGeometry valueGeometry = getValueGeometry();
        TimeGeometry timeGeometry = getTimeGeometry(interval);
        Plotinfo[] plotInfos = getPlotInfos(interval);
        for (Plotinfo each : plotInfos) {
            appendPlotinfo(chart, each, valueGeometry, timeGeometry);
        }
        chart.setWidth(size + "px");
        chart.setHeight("150px");
    }


    protected abstract String getOptionalJavascriptCall();

    /**
     * The order must be from the topmost one to the lowest one.
     *
     * @param interval
     * @return the {@link Plotinfo plot infos} to show
     */
    protected abstract Plotinfo[] getPlotInfos(Interval interval);

}
