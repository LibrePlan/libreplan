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

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.SortedMap;

import org.joda.time.LocalDate;
import org.libreplan.business.planner.chart.ILoadChartData;
import org.libreplan.business.workingday.EffortDuration;
import org.zkoss.ganttz.util.Interval;

public abstract class StandardLoadChartFiller extends LoadChartFiller {

    @Override
    protected ChartSeries[] getPlotInfo(Interval interval) {
        final ILoadChartData data = getDataOn(interval);

        ChartSeries plotInfoLoad = createPlotinfoFromDurations(getLoad(data));
        plotInfoLoad.setFillColor(COLOR_ASSIGNED_LOAD);
        plotInfoLoad.setLineWidth(0);

        ChartSeries plotInfoMax = createPlotinfoFromDurations(getCalendarMaximumAvailability(data));
        plotInfoMax.setLineColor(COLOR_CAPABILITY_LINE);
        plotInfoMax.setFillColor("#FFFFFF");
        plotInfoMax.setLineWidth(2);

        ChartSeries plotInfoOverload = createPlotinfoFromDurations(getOverload(data));
        plotInfoOverload.setFillColor(COLOR_OVERLOAD);
        plotInfoOverload.setLineWidth(0);

        return new ChartSeries[] { plotInfoOverload, plotInfoMax, plotInfoLoad };
    }

    protected abstract ILoadChartData getDataOn(Interval interval);

    protected LocalDate getStart(LocalDate explicitlySpecifiedStart, Interval interval) {
        return explicitlySpecifiedStart == null
                ? interval.getStart()
                : Collections.max(asList(explicitlySpecifiedStart, interval.getStart()));
    }

    @SuppressWarnings("unchecked")
    protected LocalDate getEnd(LocalDate explicitlySpecifiedEnd, Interval interval) {
        return explicitlySpecifiedEnd == null
                ? interval.getFinish()
                : Collections.min(asList(explicitlySpecifiedEnd, interval.getFinish()));
    }

    private SortedMap<LocalDate, EffortDuration> getLoad(ILoadChartData data) {
        return groupAsNeededByZoom(data.getLoad());
    }

    private SortedMap<LocalDate, EffortDuration> getOverload(ILoadChartData data) {
        return groupAsNeededByZoom(data.getOverload());
    }

    private SortedMap<LocalDate, EffortDuration> getCalendarMaximumAvailability(ILoadChartData data) {
        return groupAsNeededByZoom(data.getAvailability());
    }

}
