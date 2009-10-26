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
import org.zkoss.zk.ui.Executions;

/**
 * Abstract class with the basic functionality to fill the chart.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class LoadChartFiller implements ILoadChartFiller {

    private final class GraphicSpecificationCreator implements
            IServletRequestHandler {

        private final LocalDate finish;
        private final SortedMap<LocalDate, Integer> mapDayAssignments;
        private final LocalDate start;

        private GraphicSpecificationCreator(Date finish,
                SortedMap<LocalDate, Integer> mapDayAssignments, Date start) {
            this.finish = new LocalDate(finish);
            this.mapDayAssignments = mapDayAssignments;
            this.start = new LocalDate(start);
        }

        @Override
        public void handle(HttpServletRequest request,
                HttpServletResponse response) throws ServletException,
                IOException {
            PrintWriter writer = response.getWriter();
            fillValues(writer);
            writer.close();
        }

        private void fillValues(PrintWriter writer) {
            fillZeroValueFromStart(writer);
            fillInnerValues(writer, firstDay(), lastDay());
            fillZeroValueToFinish(writer);
        }

        private void fillInnerValues(PrintWriter writer, LocalDate firstDay,
                LocalDate lastDay) {
            for (LocalDate day = firstDay; day.compareTo(lastDay) <= 0; day = nextDay(day)) {
                Integer hours = getHoursForDay(day);
                printLine(writer, day, hours);
            }
        }

        private LocalDate nextDay(LocalDate date) {
            if (zoomByDay()) {
                return date.plusDays(1);
            } else {
                return date.plusWeeks(1);
            }
        }

        private LocalDate firstDay() {
            LocalDate date = mapDayAssignments.firstKey();
            return convertAsNeededByZoom(date);
        }

        private LocalDate lastDay() {
            LocalDate date = mapDayAssignments.lastKey();
            return convertAsNeededByZoom(date);
        }

        private LocalDate convertAsNeededByZoom(LocalDate date) {
            if (zoomByDay()) {
                return date;
            } else {
                return getThursdayOfThisWeek(date);
            }
        }

        private int getHoursForDay(LocalDate day) {
            return mapDayAssignments.get(day) != null ? mapDayAssignments
                    .get(day) : 0;
        }

        private void printLine(PrintWriter writer, LocalDate day, Integer hours) {
            writer.println(day.toString("yyyyMMdd") + " " + hours);
        }

        private void fillZeroValueFromStart(PrintWriter writer) {
            printLine(writer, start, 0);
            if (startIsPreviousToPreviousDayToFirstAssignment()) {
                printLine(writer, previousDayToFirstAssignment(), 0);
            }
        }

        private boolean startIsPreviousToPreviousDayToFirstAssignment() {
            return !mapDayAssignments.isEmpty()
                    && start.compareTo(previousDayToFirstAssignment()) < 0;
        }

        private LocalDate previousDayToFirstAssignment() {
            return mapDayAssignments.firstKey().minusDays(1);
        }

        private void fillZeroValueToFinish(PrintWriter writer) {
            if (finishIsPosteriorToNextDayToLastAssignment()) {
                printLine(writer, nextDayToLastAssignment(), 0);
            }
            printLine(writer, finish, 0);
        }

        private boolean finishIsPosteriorToNextDayToLastAssignment() {
            return !mapDayAssignments.isEmpty()
                    && finish.compareTo(nextDayToLastAssignment()) > 0;
        }

        private LocalDate nextDayToLastAssignment() {
            return mapDayAssignments.lastKey().plusDays(1);
        }
    }

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

    protected String getServletUri(
            final SortedMap<LocalDate, Integer> mapDayAssignments,
            final Date start, final Date finish) {
        if (mapDayAssignments.isEmpty()) {
            return "";
        }

        setMaximunValueForChartIfGreater(Collections.max(mapDayAssignments
                .values()));

        HttpServletRequest request = (HttpServletRequest) Executions
                .getCurrent().getNativeRequest();
        String uri = CallbackServlet.registerAndCreateURLFor(request,
                new GraphicSpecificationCreator(finish, mapDayAssignments,
                        start));
        return uri;
    }

    private void setMaximunValueForChartIfGreater(Integer max) {
        if (maximunValueForChart < max) {
            maximunValueForChart = max;
        }
    }

    private LocalDate getThursdayOfThisWeek(LocalDate date) {
        return date.dayOfWeek().withMinimumValue().plusDays(DAYS_TO_THURSDAY);
    }

    @Override
    public boolean zoomByDay() {
        return zoomLevel.equals(ZoomLevel.DETAIL_FIVE);
    }

    @Override
    public void setZoomLevel(ZoomLevel zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    protected void resetMaximunValueForChart() {
        this.maximunValueForChart = 0;
    }

    protected Integer getMaximunValueForChart() {
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
