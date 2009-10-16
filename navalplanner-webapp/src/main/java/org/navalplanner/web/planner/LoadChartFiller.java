/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.planner;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.navalplanner.web.servlets.CallbackServlet;
import org.navalplanner.web.servlets.CallbackServlet.IServletRequestHandler;
import org.zkforge.timeplot.Timeplot;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;

/**
 * Abstract class with the basic functionality to fill the chart.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class LoadChartFiller implements ILoadChartFiller {

    /**
     * Number of days to Thursday since the beginning of the week. In order to
     * calculate the middle of a week.
     */
    private final static int DAYS_TO_THURSDAY = 3;

    private ZoomLevel zoomLevel = ZoomLevel.DETAIL_ONE;

    private Integer maximunValueForChart = 0;

    @Override
    public abstract void fillChart(Timeplot chart, Interval interval,
            Integer size);

    @Override
    public String getServletUri(
            final SortedMap<LocalDate, Integer> mapDayAssignments,
            final Date start, final Date finish) {
        if (mapDayAssignments.isEmpty()) {
            return "";
        }

        setMaximunValueForChartIfGreater(Collections.max(mapDayAssignments
                .values()));

        String uri = CallbackServlet
                .registerAndCreateURLFor(new IServletRequestHandler() {

                    @Override
                    public void handle(HttpServletRequest request,
                            HttpServletResponse response)
                            throws ServletException, IOException {
                        PrintWriter writer = response.getWriter();

                        fillZeroValueFromStart(writer, start, mapDayAssignments);

                        LocalDate firstDay = firstDay(mapDayAssignments);
                        LocalDate lastDay = lastDay(mapDayAssignments);

                        for (LocalDate day = firstDay; day.compareTo(lastDay) <= 0; day = nextDay(day)) {
                            Integer hours = mapDayAssignments.get(day) != null ? mapDayAssignments
                                    .get(day)
                                    : 0;
                            printLine(writer, day, hours);
                        }

                        fillZeroValueToFinish(writer, finish, mapDayAssignments);

                        writer.close();
                    }
                });
        return uri;
    }

    private void setMaximunValueForChartIfGreater(Integer max) {
        if (maximunValueForChart < max) {
            maximunValueForChart = max;
        }
    }

    private LocalDate firstDay(SortedMap<LocalDate, Integer> mapDayAssignments) {
        LocalDate date = mapDayAssignments.firstKey();
        if (zoomByDay()) {
            return date;
        } else {
            return getThursdayOfThisWeek(date);
        }
    }

    private LocalDate lastDay(SortedMap<LocalDate, Integer> mapDayAssignments) {
        LocalDate date = mapDayAssignments.lastKey();
        if (zoomByDay()) {
            return date;
        } else {
            return getThursdayOfThisWeek(date);
        }
    }

    private LocalDate getThursdayOfThisWeek(LocalDate date) {
        return date.dayOfWeek().withMinimumValue().plusDays(DAYS_TO_THURSDAY);
    }

    private LocalDate nextDay(LocalDate date) {
        if (zoomByDay()) {
            return date;
        } else {
            return date.plusWeeks(1);
        }
    }

    @Override
    public boolean zoomByDay() {
        return zoomLevel.equals(ZoomLevel.DETAIL_FIVE);
    }

    private void fillZeroValueFromStart(PrintWriter writer, Date start,
            SortedMap<LocalDate, Integer> mapDayAssignments) {
        LocalDate day = new LocalDate(start);
        if (mapDayAssignments.isEmpty()) {
            printLine(writer, day, 0);
        } else if (day.compareTo(mapDayAssignments.firstKey()) < 0) {
            printLine(writer, day, 0);
            if (!day.equals(mapDayAssignments.firstKey().minusDays(1))) {
                printLine(writer, mapDayAssignments.firstKey().minusDays(1), 0);
            }
        }
    }

    private void fillZeroValueToFinish(PrintWriter writer, Date finish,
            SortedMap<LocalDate, Integer> mapDayAssignments) {
        LocalDate day = new LocalDate(finish);
        if (mapDayAssignments.isEmpty()) {
            printLine(writer, day, 0);
        } else if (day.compareTo(mapDayAssignments.lastKey()) > 0) {
            if (!day.equals(mapDayAssignments.lastKey().plusDays(1))) {
                printLine(writer, mapDayAssignments.lastKey().plusDays(1), 0);
            }
            printLine(writer, day, 0);
        }
    }

    private void printLine(PrintWriter writer, LocalDate day, Integer hours) {
        writer.println(day.toString("yyyyMMdd") + " " + hours);
    }

    @Override
    public void setZoomLevel(ZoomLevel zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    @Override
    public void resetMaximunValueForChart() {
        this.maximunValueForChart = 0;
    }

    @Override
    public Integer getMaximunValueForChart() {
        return maximunValueForChart;
    }

    @Override
    public SortedMap<LocalDate, Integer> groupByWeek(
            SortedMap<LocalDate, Integer> map) {
        SortedMap<LocalDate, Integer> result = new TreeMap<LocalDate, Integer>();

        for (LocalDate day : map.keySet()) {
            LocalDate key = getThursdayOfThisWeek(day);

            if (result.get(key) == null) {
                result.put(key, map.get(day));
            } else {
                result.put(key, result.get(key) + map.get(day));
            }
        }

        for (LocalDate day : result.keySet()) {
            result.put(day, result.get(day) / 7);
        }

        return result;
    }

}
