/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import static org.libreplan.business.workingday.EffortDuration.zero;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;

/**
 * Abstract class with the basic functionality to fill the chart.
 *
 * Renders via Chart.js (see common/js/libreplan-chart.js): each fillChart() implementation builds
 * a list of {@link ChartSeries}, then calls {@link #renderChart(Div, List, Interval, Integer)},
 * which serializes them to JSON and pushes them to the client with
 * {@link Clients#evalJavaScript(String)}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class ChartFiller implements IChartFiller {

    protected abstract class EffortByDayCalculator<T> {
        public SortedMap<LocalDate, EffortDuration> calculate(Collection<? extends T> elements) {
            SortedMap<LocalDate, EffortDuration> result = new TreeMap<LocalDate, EffortDuration>();
            if ( elements.isEmpty() ) {
                return result;
            }
            for (T element : elements) {
                if ( included(element) ) {
                    EffortDuration duration = getDurationFor(element);
                    LocalDate day = getDayFor(element);
                    EffortDuration previous = result.get(day);
                    previous = previous == null ? zero() : previous;
                    result.put(day, previous.plus(duration));
                }
            }
            return groupAsNeededByZoom(result);
        }

        protected abstract LocalDate getDayFor(T element);

        protected abstract EffortDuration getDurationFor(T element);

        protected boolean included(T each) {
            return true;
        }
    }

    protected static EffortDuration sumCalendarCapacitiesForDay(
            Collection<? extends Resource> resources, LocalDate day) {
        PartialDay wholeDay = PartialDay.wholeDay(day);
        EffortDuration sum = zero();
        for (Resource resource : resources) {
            sum = sum.plus(calendarCapacityFor(resource,
                    wholeDay));
        }
        return sum;
    }

    protected static EffortDuration calendarCapacityFor(Resource resource,
            PartialDay day) {
        return resource.getCalendarOrDefault().getCapacityOn(day);
    }

    /**
     * Number of days to Thursday since the beginning of the week. In order to
     * calculate the middle of a week.
     */
    private final static int DAYS_TO_THURSDAY = 3;

    /**
     * Matches the natural height of the chart's legend cell (the "Overload / Total capability /
     * Assigned load" swatches next to it) so the chart fills its row instead of leaving blank
     * space below - the legend, not the chart, is what determines the row's height. Public so the
     * Tabbox wrapping these charts (CompanyPlanningModel/OrderPlanningModel/ResourceLoadController)
     * can size itself off the same number instead of an independent, easily-stale hardcoded value.
     */
    public final static int CHART_HEIGHT_PX = 190;

    private ZoomLevel zoomLevel = ZoomLevel.DETAIL_ONE;

    @Override
    public abstract void fillChart(Div chart, Interval interval, Integer size);

    private static LocalDate getThursdayOfThisWeek(LocalDate date) {
        return date.dayOfWeek().withMinimumValue().plusDays(DAYS_TO_THURSDAY);
    }

    private boolean isZoomByDayOrWeek() {
        return (zoomLevel.equals(ZoomLevel.DETAIL_FIVE) || zoomLevel
                .equals(ZoomLevel.DETAIL_FOUR));
    }

    protected SortedMap<LocalDate, BigDecimal> groupByWeek(
            SortedMap<LocalDate, BigDecimal> map) {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        for (Entry<LocalDate, BigDecimal> entry : map.entrySet()) {
            LocalDate day = entry.getKey();
            LocalDate key = getThursdayOfThisWeek(day);
            BigDecimal hours = entry.getValue() == null ? BigDecimal.ZERO
                    : entry.getValue();
            if (result.get(key) == null) {
                result.put(key, hours);
            } else {
                result.put(key, result.get(key).add(hours));
            }
        }
        for (Entry<LocalDate, BigDecimal> entry : result.entrySet()) {
            LocalDate day = entry.getKey();
            result.put(entry.getKey(), result.get(day).setScale(2).divide(
                    new BigDecimal(7), RoundingMode.DOWN));
        }
        return result;
    }

    protected SortedMap<LocalDate, EffortDuration> groupAsNeededByZoom(
            SortedMap<LocalDate, EffortDuration> map) {
        if (isZoomByDayOrWeek()) {
            return map;
        }
        return groupByWeekDurations(map);
    }

    protected SortedMap<LocalDate, EffortDuration> groupByWeekDurations(
            SortedMap<LocalDate, EffortDuration> map) {
        return average(accumulatePerWeek(map));
    }

    private static SortedMap<LocalDate, EffortDuration> accumulatePerWeek(
            SortedMap<LocalDate, EffortDuration> map) {
        SortedMap<LocalDate, EffortDuration> result = new TreeMap<LocalDate, EffortDuration>();
        for (Entry<LocalDate, EffortDuration> each : map.entrySet()) {
            LocalDate centerOfWeek = getThursdayOfThisWeek(each.getKey());
            EffortDuration accumulated = result.get(centerOfWeek);
            accumulated = accumulated == null ? zero() : accumulated;
            result.put(centerOfWeek, accumulated.plus(each.getValue()));
        }
        return result;
    }

    private static SortedMap<LocalDate, EffortDuration> average(
            SortedMap<LocalDate, EffortDuration> accumulatedPerWeek) {
        SortedMap<LocalDate, EffortDuration> result = new TreeMap<LocalDate, EffortDuration>();
        for (Entry<LocalDate, EffortDuration> each : accumulatedPerWeek
                .entrySet()) {
            result.put(each.getKey(), each.getValue().divideBy(7));
        }
        return result;
    }

    protected SortedMap<LocalDate, Map<Resource, EffortDuration>> groupDurationsByDayAndResource(
            List<DayAssignment> dayAssignments) {
        SortedMap<LocalDate, Map<Resource, EffortDuration>> map = new TreeMap<LocalDate, Map<Resource, EffortDuration>>();

        for (DayAssignment dayAssignment : dayAssignments) {
            final LocalDate day = dayAssignment.getDay();
            final EffortDuration dayAssignmentDuration = dayAssignment
                    .getDuration();
            Resource resource = dayAssignment.getResource();
            if (map.get(day) == null) {
                map.put(day, new HashMap<Resource, EffortDuration>());
            }
            Map<Resource, EffortDuration> forDay = map.get(day);
            EffortDuration previousDuration = forDay.get(resource);
            previousDuration = previousDuration != null ? previousDuration
                    : EffortDuration.zero();
            forDay.put(dayAssignment.getResource(),
                    previousDuration.plus(dayAssignmentDuration));
        }
        return map;
    }

    protected void addCost(SortedMap<LocalDate, BigDecimal> currentCost,
            SortedMap<LocalDate, BigDecimal> additionalCost) {
        for (LocalDate day : additionalCost.keySet()) {
            if (!currentCost.containsKey(day)) {
                currentCost.put(day, BigDecimal.ZERO);
            }
            currentCost.put(day, currentCost.get(day).add(
                    additionalCost.get(day)));
        }
    }

    protected SortedMap<LocalDate, BigDecimal> accumulateResult(
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

    protected SortedMap<LocalDate, BigDecimal> convertToBigDecimal(
            SortedMap<LocalDate, Integer> map) {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

        for (LocalDate day : map.keySet()) {
            BigDecimal value = new BigDecimal(map.get(day));
            result.put(day, value);
        }

        return result;
    }

    protected SortedMap<LocalDate, BigDecimal> calculatedValueForEveryDay(
            SortedMap<LocalDate, BigDecimal> values, Interval interval) {
        return calculatedValueForEveryDay(values, interval.getStart(),
                interval.getFinish());
    }

    protected SortedMap<LocalDate, BigDecimal> calculatedValueForEveryDay(
            SortedMap<LocalDate, BigDecimal> map, Date start, Date finish) {
        return calculatedValueForEveryDay(map, new LocalDate(start),
                new LocalDate(finish));
    }

    protected SortedMap<LocalDate, BigDecimal> calculatedValueForEveryDay(
            SortedMap<LocalDate, BigDecimal> map, LocalDate start,
            LocalDate finish) {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

        LocalDate previousDay = start;
        BigDecimal previousValue = BigDecimal.ZERO;

        for (LocalDate day : map.keySet()) {
            BigDecimal value = map.get(day);
            fillValues(result, previousDay, day, previousValue, value);

            previousDay = day;
            previousValue = value;
        }

        if (previousDay.compareTo(finish) < 0) {
            fillValues(result, previousDay, finish, previousValue,
                    previousValue);
        }

        return result;
    }

    private void fillValues(SortedMap<LocalDate, BigDecimal> map,
            LocalDate firstDay, LocalDate lastDay, BigDecimal firstValue,
            BigDecimal lastValue) {

        Integer days = Days.daysBetween(firstDay, lastDay).getDays();
        if (days > 0) {
            BigDecimal ammount = lastValue.subtract(firstValue);
            BigDecimal ammountPerDay = ammount.setScale(2, RoundingMode.DOWN).divide(
                    new BigDecimal(days), RoundingMode.DOWN);

            BigDecimal value = firstValue.setScale(2, RoundingMode.DOWN);
            for (LocalDate day = firstDay; day.compareTo(lastDay) <= 0; day = day
                    .plusDays(1)) {
                map.put(day, value);
                value = value.add(ammountPerDay);
            }
        }
    }

    protected ChartSeries createPlotinfoFromDurations(SortedMap<LocalDate, EffortDuration> map) {
        return createPlotinfo(toHoursDecimal(map));
    }

    public static <K> SortedMap<K, BigDecimal> toHoursDecimal(
            Map<K, EffortDuration> map) {
        SortedMap<K, BigDecimal> result = new TreeMap<K, BigDecimal>();
        for (Entry<K, EffortDuration> each : map.entrySet()) {
            result.put(each.getKey(), each.getValue()
                    .toHoursAsDecimalWithScale(2));
        }
        return result;
    }

    protected ChartSeries createPlotinfo(SortedMap<LocalDate, BigDecimal> map) {
        return new ChartSeries(map);
    }

    /**
     * Serializes the given series to JSON and pushes them to the client-side Chart.js instance
     * mounted on {@code chartDiv} (see common/js/libreplan-chart.js). Every series is resolved
     * against the same shared day/week calendar spanning {@code interval} (missing days default
     * to zero, matching the old chart's edge zero-padding), so a single "labels" axis works for
     * all of them regardless of how sparse each series's underlying data is.
     */
    protected void renderChart(Div chartDiv, List<ChartSeries> seriesList, Interval interval, Integer size) {
        renderChart(chartDiv, seriesList, interval, size, false);
    }

    /**
     * @param stepped
     *            Whether each series holds its value flat across the gap to the next data point
     *            instead of interpolating a straight line between them - correct for a chart
     *            like the load chart, where the underlying quantity (capacity/load on a given
     *            day) is genuinely constant for that whole day and only jumps at day boundaries,
     *            not something that changes gradually within a day. The Earned Value chart's
     *            series (BCWS, ACWP, ...) are real continuously-evolving cumulative metrics, so
     *            they keep the default straight-line interpolation instead.
     */
    protected void renderChart(
            Div chartDiv, List<ChartSeries> seriesList, Interval interval, Integer size, boolean stepped) {

        List<LocalDate> calendar = computeDateRange(interval);

        List<String> labels = new ArrayList<String>();
        for (LocalDate day : calendar) {
            labels.add(day.toString());
        }

        List<Map<String, Object>> series = new ArrayList<Map<String, Object>>();
        for (ChartSeries each : seriesList) {
            Map<String, Object> serialized = new LinkedHashMap<String, Object>();
            serialized.put("label", each.getLabel());
            serialized.put("lineColor", each.getLineColor());
            serialized.put("fillColor", each.getFillColor());
            serialized.put("lineWidth", each.getLineWidth());

            List<BigDecimal> data = new ArrayList<BigDecimal>();
            for (LocalDate day : calendar) {
                BigDecimal value = each.getPoints().get(day);
                data.add(value != null ? value : BigDecimal.ZERO);
            }
            serialized.put("data", data);

            series.add(serialized);
        }

        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("labels", labels);
        payload.put("series", series);
        payload.put("width", size);
        payload.put("height", CHART_HEIGHT_PX);
        payload.put("stepped", stepped);

        chartDiv.setWidth(size + "px");
        chartDiv.setHeight(CHART_HEIGHT_PX + "px");

        try {
            String json = new ObjectMapper().writeValueAsString(payload);
            Clients.evalJavaScript("LibreplanChart.render('" + chartDiv.getUuid() + "', " + json + ")");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not serialize chart data", e);
        }
    }

    private List<LocalDate> computeDateRange(Interval interval) {
        LocalDate start = new LocalDate(interval.getStart());
        LocalDate finish = new LocalDate(interval.getFinish());

        if (!isZoomByDayOrWeek()) {
            start = getThursdayOfThisWeek(start);
            finish = getThursdayOfThisWeek(finish);
        }

        List<LocalDate> result = new ArrayList<LocalDate>();
        for (LocalDate day = start; day.compareTo(finish) <= 0; day = nextDay(day)) {
            result.add(day);
        }
        return result;
    }

    private LocalDate nextDay(LocalDate date) {
        return isZoomByDayOrWeek() ? date.plusDays(1) : date.plusWeeks(1);
    }

    @Override
    public void setZoomLevel(ZoomLevel zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

}
