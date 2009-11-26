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

package org.navalplanner.web.planner.loadchart;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.servlets.CallbackServlet;
import org.navalplanner.web.servlets.CallbackServlet.IServletRequestHandler;
import org.zkforge.timeplot.Timeplot;
import org.zkforge.timeplot.geometry.DefaultTimeGeometry;
import org.zkforge.timeplot.geometry.DefaultValueGeometry;
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.Executions;

/**
 * Abstract class with the basic functionality to fill the chart.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class LoadChartFiller implements ILoadChartFiller {

    protected abstract class HoursByDayCalculator<T> {
        public SortedMap<LocalDate, Integer> calculate(
                Collection<? extends T> elements) {
            SortedMap<LocalDate, Integer> result = new TreeMap<LocalDate, Integer>();
            if (elements.isEmpty()) {
                return result;
            }
            for (T element : elements) {
                if (included(element)) {
                    int hours = getHoursFor(element);
                    LocalDate day = getDayFor(element);
                    if (!result.containsKey(day)) {
                        result.put(day, 0);
                    }
                    result.put(day, result.get(day) + hours);
                }
            }
            return convertAsNeededByZoom(result);
        }

        protected abstract LocalDate getDayFor(T element);

        protected abstract int getHoursFor(T element);

        protected boolean included(T each) {
            return true;
        }
    }

    protected class DefaultDayAssignmentCalculator extends
            HoursByDayCalculator<DayAssignment> {
        public DefaultDayAssignmentCalculator() {
        }

        @Override
        protected LocalDate getDayFor(DayAssignment element) {
            return element.getDay();
        }

        @Override
        protected int getHoursFor(DayAssignment element) {
            return element.getHours();
        }
    }

    protected final int sumHoursForDay(
            Collection<? extends Resource> resources,
            LocalDate day) {
        int sum = 0;
        for (Resource resource : resources) {
            sum += hoursFor(resource, day);
        }
        return sum;
    }

    private int hoursFor(Resource resource, LocalDate day) {
        int result = 0;
        ResourceCalendar calendar = resource.getCalendar();
        if (calendar != null) {
            result += calendar.getWorkableHours(day);
        } else {
            result += SameWorkHoursEveryDay.getDefaultWorkingDay()
                    .getWorkableHours(day);
        }
        return result;
    }

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
            if (isZoomByDay()) {
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
            if (isZoomByDay()) {
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

    private boolean isZoomByDay() {
        return zoomLevel.equals(ZoomLevel.DETAIL_FIVE);
    }

    protected void resetMaximunValueForChart() {
        this.maximunValueForChart = 0;
    }

    protected Integer getMaximunValueForChart() {
        return maximunValueForChart;
    }

    protected SortedMap<LocalDate, Integer> groupByWeek(
            SortedMap<LocalDate, Integer> map) {
        SortedMap<LocalDate, Integer> result = new TreeMap<LocalDate, Integer>();
        for (Entry<LocalDate, Integer> entry : map.entrySet()) {
            LocalDate day = entry.getKey();
            LocalDate key = getThursdayOfThisWeek(day);
            Integer hours = entry.getValue() == null ? 0 : entry.getValue();
            if (result.get(key) == null) {
                result.put(key, hours);
            } else {
                result.put(key, result.get(key) + hours);
            }
        }
        for (Entry<LocalDate, Integer> entry : result.entrySet()) {
            LocalDate day = entry.getKey();
            result.put(entry.getKey(), result.get(day) / 7);
        }
        return result;
    }

    protected SortedMap<LocalDate, Integer> convertAsNeededByZoom(
            SortedMap<LocalDate, Integer> map) {
        if (isZoomByDay()) {
            return map;
        } else {
            return groupByWeek(map);
        }
    }

    @Override
    public TimeGeometry getTimeGeometry(Interval interval) {
        LocalDate start = new LocalDate(interval.getStart());
        LocalDate finish = new LocalDate(interval.getFinish());

        TimeGeometry timeGeometry = new DefaultTimeGeometry();

        if (!isZoomByDay()) {
            start = getThursdayOfThisWeek(start);
            finish = getThursdayOfThisWeek(finish);
        }

        String min = start.toString("yyyyMMdd");
        String max = finish.toString("yyyyMMdd");

        timeGeometry.setMin(Integer.valueOf(min));
        timeGeometry.setMax(Integer.valueOf(max));

        return timeGeometry;
    }

    @Override
    public ValueGeometry getValueGeometry(Integer maximum) {
        DefaultValueGeometry valueGeometry = new DefaultValueGeometry();
        valueGeometry.setMin(0);
        valueGeometry.setMax(maximum);
        valueGeometry.setGridColor("#000000");
        valueGeometry.setAxisLabelsPlacement("left");

        return valueGeometry;
    }

    @Override
    public SortedMap<LocalDate, Map<Resource, Integer>> groupDayAssignmentsByDayAndResource(
            List<DayAssignment> dayAssignments) {
        SortedMap<LocalDate, Map<Resource, Integer>> map = new TreeMap<LocalDate, Map<Resource, Integer>>();

        for (DayAssignment dayAssignment : dayAssignments) {
            LocalDate day = dayAssignment.getDay();
            if (map.get(day) == null) {
                HashMap<Resource, Integer> resourcesMap = new HashMap<Resource, Integer>();
                resourcesMap.put(dayAssignment.getResource(), dayAssignment
                        .getHours());
                map.put(day, resourcesMap);
            } else {
                if (map.get(day).get(dayAssignment.getResource()) == null) {
                    map.get(day).put(dayAssignment.getResource(),
                            dayAssignment.getHours());
                } else {
                    Integer hours = map.get(day).get(
                            dayAssignment.getResource());
                    hours += dayAssignment.getHours();
                    map.get(day).put(dayAssignment.getResource(), hours);
                }
            }
        }

        return map;
    }

    @Override
    public void addCost(SortedMap<LocalDate, BigDecimal> currentCost,
            SortedMap<LocalDate, BigDecimal> additionalCost) {
        for (LocalDate day : additionalCost.keySet()) {
            if (!currentCost.containsKey(day)) {
                currentCost.put(day, BigDecimal.ZERO);
            }
            currentCost.put(day, currentCost.get(day).add(
                    additionalCost.get(day)));
        }
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> accumulateResult(
            SortedMap<LocalDate, BigDecimal> map) {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        if (map.isEmpty()) {
            return result;
        }

        BigDecimal accumulatedResult = BigDecimal.ZERO;
        for (LocalDate day : map.keySet()) {
            BigDecimal value = map.get(day);
            accumulatedResult = accumulatedResult.add(value);
            result.put(day, accumulatedResult);
        }

        return result;
    }

}
