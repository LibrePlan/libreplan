/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.joda.time.LocalDate;
import org.libreplan.web.I18nHelper;
import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkoss.ganttz.util.Interval;


/**
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Abstract class with the common functionality for the earned value
 *         chart.
 */
public abstract class EarnedValueChartFiller extends ChartFiller {

    public static boolean includes(Interval interval, LocalDate date) {
        LocalDate start = interval.getStart();
        LocalDate end = interval.getFinish();
        return start.compareTo(date) <= 0 && date.compareTo(end) < 0;
    }

    protected Map<EarnedValueType, SortedMap<LocalDate, BigDecimal>> indicators = new HashMap<EarnedValueType, SortedMap<LocalDate, BigDecimal>>();

    private Interval indicatorsInterval;

    protected Plotinfo createPlotInfo(SortedMap<LocalDate, BigDecimal> map,
            Interval interval, String lineColor) {
        Plotinfo plotInfo = createPlotinfo(map, interval, true);
        plotInfo.setLineColor(lineColor);
        return plotInfo;
    }

    public void calculateValues(Interval interval) {
        this.indicatorsInterval = interval;
        // BCWS
        calculateBudgetedCostWorkScheduled(interval);
        // ACWP
        calculateActualCostWorkPerformed(interval);
        // BCWP
        calculateBudgetedCostWorkPerformed(interval);

        // CV
        calculateCostVariance();
        // SV
        calculateScheduleVariance();
        // BAC
        calculateBudgetAtCompletion();
        // EAC
        calculateEstimateAtCompletion();
        // VAC
        calculateVarianceAtCompletion();
        // ETC
        calculateEstimatedToComplete();
        // CPI
        calculateCostPerformanceIndex();
        // SPI
        calculateSchedulePerformanceIndex();
    }

    protected abstract void calculateBudgetedCostWorkScheduled(Interval interval);

    protected abstract void calculateActualCostWorkPerformed(Interval interval);

    protected abstract void calculateBudgetedCostWorkPerformed(Interval interval);

    protected abstract void calculateCostVariance();

    protected abstract void calculateScheduleVariance();

    protected abstract void calculateBudgetAtCompletion();

    protected abstract void calculateEstimateAtCompletion();

    protected abstract void calculateVarianceAtCompletion();

    protected abstract void calculateEstimatedToComplete();

    protected abstract void calculateSchedulePerformanceIndex();

    protected abstract void calculateCostPerformanceIndex();

    protected abstract Set<EarnedValueType> getSelectedIndicators();

    public SortedMap<LocalDate, BigDecimal> getIndicator(EarnedValueType indicator) {
        return indicators.get(indicator);
    }

    public BigDecimal getIndicator(EarnedValueType indicator, LocalDate date) {
        return indicators.get(indicator).get(date);
    }

    public void setIndicator(EarnedValueType type, SortedMap<LocalDate, BigDecimal> values) {
        indicators.put(type, values);
    }

    public void setIndicatorInInterval(EarnedValueType type,
            Interval interval, SortedMap<LocalDate, BigDecimal> values) {
        addZeroBeforeTheFirstValue(values);
        indicators.put(type, calculatedValueForEveryDay(values, interval));
    }

    protected void addZeroBeforeTheFirstValue(
            SortedMap<LocalDate, BigDecimal> map) {
        if (!map.isEmpty()) {
            map.put(map.firstKey().minusDays(1), BigDecimal.ZERO);
        }
    }

    @Override
    public void fillChart(Timeplot chart, Interval interval, Integer size) {
        chart.getChildren().clear();
        chart.invalidate();
        resetMinimumAndMaximumValueForChart();

        calculateValues(interval);

        List<Plotinfo> plotinfos = new ArrayList<Plotinfo>();
        for (EarnedValueType indicator : getSelectedIndicators()) {
            Plotinfo plotinfo = createPlotInfo(indicators.get(indicator),
                    interval, indicator.getColor());
            plotinfos.add(plotinfo);
        }

        if (plotinfos.isEmpty()) {
            // If user doesn't select any indicator, it is needed to create
            // a default Plotinfo in order to avoid errors on Timemplot
            plotinfos.add(new Plotinfo());
        }

        ValueGeometry valueGeometry = getValueGeometry();
        TimeGeometry timeGeometry = getTimeGeometry(interval);

        for (Plotinfo plotinfo : plotinfos) {
            appendPlotinfo(chart, plotinfo, valueGeometry, timeGeometry);
        }

        chart.setWidth(size + "px");
        chart.setHeight("150px");
    }

    public Interval getIndicatorsDefinitionInterval() {
        return indicatorsInterval;
    }

    /**
     * Will try to use today if possible
     * @return Today if there are values defined for that date. The last day in
     *         the interval otherwise
     */
    public LocalDate initialDateForIndicatorValues() {
        Interval chartInterval = getIndicatorsDefinitionInterval();
        LocalDate today = new LocalDate();
        return includes(chartInterval, today) ? today : chartInterval
                .getFinish().minusDays(1);
    }

    /**
     *
     * @author Manuel Rego Casasnovas <mrego@igalia.com>
     *
     */
    public enum EarnedValueType {

        BCWS(_("BCWS"), _("Budgeted Cost Work Scheduled"), "#0000FF"), ACWP(
                _("ACWP"), _("Actual Cost Work Performed"), "#FF0000"), BCWP(
                _("BCWP"), _("Budgeted Cost Work Performed"), "#00FF00"), CV(
                _("CV"), _("Cost Variance"), "#FF8800"), SV(_("SV"),
                _("Schedule Variance"), "#00FFFF"), BAC(_("BAC"),
                _("Budget At Completion"), "#FF00FF"), EAC(_("EAC"),
                _("Estimate At Completion"), "#880000"), VAC(_("VAC"),
                _("Variance At Completion"), "#000088"), ETC(_("ETC"),
                _("Estimate To Complete"), "#008800"), CPI(_("CPI"),
                _("Cost Performance Index"), "#888800"), SPI(_("SPI"),
                _("Schedule Performance Index"), "#008888")
        ;

        /**
         * Forces to mark the string as needing translation
         */
        private static String _(String string) {
            return string;
        }

        private String acronym;
        private String name;
        private String color;

        private EarnedValueType(String acronym, String name, String color) {
            this.acronym = acronym;
            this.name = name;
            this.color = color;
        }

        public String getAcronym() {
            return I18nHelper._(acronym);
        }

        public String getName() {
            return I18nHelper._(name);
        }

        public String getColor() {
            return color;
        }
    }

}