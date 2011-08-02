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

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.SortedMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.planner.chart.ILoadChartData;
import org.navalplanner.business.workingday.EffortDuration;
import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.util.Clients;

public abstract class LoadChartFiller extends ChartFiller {

    public static final String COLOR_CAPABILITY_LINE = "#000000"; // Black
    public static final String COLOR_ASSIGNED_LOAD_GLOBAL = "#98D471"; // Green
    public static final String COLOR_OVERLOAD_GLOBAL = "#FF5A11"; // Red

    @Override
    public void fillChart(Timeplot chart, Interval interval, Integer size) {
        chart.getChildren().clear();
        chart.invalidate();

        if (getOptionalJavascriptCall() != null) {
            Clients.evalJavaScript(getOptionalJavascriptCall());
        }
        resetMinimumAndMaximumValueForChart();

        final ILoadChartData data = getDataOn(interval);

        Plotinfo plotInfoLoad = createPlotinfoFromDurations(getLoad(data),
                interval);
        plotInfoLoad.setFillColor(COLOR_ASSIGNED_LOAD_GLOBAL);
        plotInfoLoad.setLineWidth(0);

        Plotinfo plotInfoMax = createPlotinfoFromDurations(
                getCalendarMaximumAvailability(data), interval);
        plotInfoMax.setLineColor(COLOR_CAPABILITY_LINE);
        plotInfoMax.setFillColor("#FFFFFF");
        plotInfoMax.setLineWidth(2);

        Plotinfo plotInfoOverload = createPlotinfoFromDurations(
                getOverload(data), interval);
        plotInfoOverload.setFillColor(COLOR_OVERLOAD_GLOBAL);
        plotInfoOverload.setLineWidth(0);

        ValueGeometry valueGeometry = getValueGeometry();
        TimeGeometry timeGeometry = getTimeGeometry(interval);

        appendPlotinfo(chart, plotInfoOverload, valueGeometry, timeGeometry);
        appendPlotinfo(chart, plotInfoMax, valueGeometry, timeGeometry);
        appendPlotinfo(chart, plotInfoLoad, valueGeometry, timeGeometry);

        chart.setWidth(size + "px");
        chart.setHeight("150px");
    }

    protected abstract String getOptionalJavascriptCall();

    protected abstract ILoadChartData getDataOn(Interval interval);

    protected LocalDate getStart(LocalDate explicitlySpecifiedStart,
            Interval interval) {
        if (explicitlySpecifiedStart == null) {
            return interval.getStart();
        }
        return Collections.max(asList(explicitlySpecifiedStart,
                interval.getStart()));
    }

    @SuppressWarnings("unchecked")
    protected LocalDate getEnd(LocalDate explicitlySpecifiedEnd,
            Interval interval) {
        if (explicitlySpecifiedEnd == null) {
            return interval.getFinish();
        }
        return Collections.min(asList(explicitlySpecifiedEnd,
                interval.getFinish()));
    }

    private SortedMap<LocalDate, EffortDuration> getLoad(ILoadChartData data) {
        return groupAsNeededByZoom(data.getLoad());
    }

    private SortedMap<LocalDate, EffortDuration> getOverload(ILoadChartData data) {
        return groupAsNeededByZoom(data.getOverload());
    }

    private SortedMap<LocalDate, EffortDuration> getCalendarMaximumAvailability(
            ILoadChartData data) {
        return data.getAvailability();
    }

}
