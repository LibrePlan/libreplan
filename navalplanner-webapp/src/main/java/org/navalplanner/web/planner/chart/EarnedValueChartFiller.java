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

package org.navalplanner.web.planner.chart;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkoss.ganttz.util.Interval;


/**
 * Abstract class with the common functionality for the earned value chart.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class EarnedValueChartFiller extends ChartFiller {

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

        private String acronym;
        private String name;
        private String color;

        private EarnedValueType(String acronym, String name, String color) {
            this.acronym = acronym;
            this.name = name;
            this.color = color;
        }

        public String getAcronym() {
            return acronym;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }
    }

    protected Map<EarnedValueType, SortedMap<LocalDate, BigDecimal>> indicators = new HashMap<EarnedValueType, SortedMap<LocalDate, BigDecimal>>();

    protected abstract void calculateBudgetedCostWorkScheduled(Interval interval);
    protected abstract void calculateActualCostWorkPerformed(Interval interval);
    protected abstract void calculateBudgetedCostWorkPerformed(Interval interval);

    protected Plotinfo createPlotInfo(SortedMap<LocalDate, BigDecimal> map,
            Interval interval, String lineColor) {
        Plotinfo plotInfo = createPlotinfo(map, interval);
        plotInfo.setLineColor(lineColor);
        return plotInfo;
    }

    protected void calculateValues(Interval interval) {
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

    private void calculateCostVariance() {
        // CV = BCWP - ACWP
        SortedMap<LocalDate, BigDecimal> cv = new TreeMap<LocalDate, BigDecimal>();
        SortedMap<LocalDate, BigDecimal> bcwp = indicators
                .get(EarnedValueType.BCWP);
        SortedMap<LocalDate, BigDecimal> acwp = indicators
                .get(EarnedValueType.ACWP);

        for (LocalDate day : bcwp.keySet()) {
            cv.put(day, bcwp.get(day).subtract(acwp.get(day)));
        }

        indicators.put(EarnedValueType.CV, cv);
    }

    private void calculateScheduleVariance() {
        // SV = BCWP - BCWS
        SortedMap<LocalDate, BigDecimal> sv = new TreeMap<LocalDate, BigDecimal>();
        SortedMap<LocalDate, BigDecimal> bcwp = indicators
                .get(EarnedValueType.BCWP);
        SortedMap<LocalDate, BigDecimal> bcws = indicators
                .get(EarnedValueType.BCWS);

        for (LocalDate day : bcwp.keySet()) {
            sv.put(day, bcwp.get(day).subtract(bcws.get(day)));
        }

        indicators.put(EarnedValueType.SV, sv);
    }

    private void calculateBudgetAtCompletion() {
        // BAC = max (BCWS)
        SortedMap<LocalDate, BigDecimal> bac = new TreeMap<LocalDate, BigDecimal>();
        SortedMap<LocalDate, BigDecimal> bcws = indicators
                .get(EarnedValueType.BCWS);

        BigDecimal value = Collections.max(bcws.values());
        for (LocalDate day : bcws.keySet()) {
            bac.put(day, value);
        }

        indicators.put(EarnedValueType.BAC, bac);
    }

    private void calculateEstimateAtCompletion() {
        // EAC = (ACWP/BCWP) * BAC
        SortedMap<LocalDate, BigDecimal> eac = new TreeMap<LocalDate, BigDecimal>();
        SortedMap<LocalDate, BigDecimal> acwp = indicators
                .get(EarnedValueType.ACWP);
        SortedMap<LocalDate, BigDecimal> bcwp = indicators
                .get(EarnedValueType.BCWP);
        SortedMap<LocalDate, BigDecimal> bac = indicators
                .get(EarnedValueType.BAC);

        for (LocalDate day : acwp.keySet()) {
            BigDecimal value = BigDecimal.ZERO;
            if (bcwp.get(day).compareTo(BigDecimal.ZERO) != 0) {
                value = acwp.get(day).divide(bcwp.get(day), RoundingMode.DOWN)
                        .multiply(bac.get(day));
            }
            eac.put(day, value);
        }

        indicators.put(EarnedValueType.EAC, eac);
    }

    private void calculateVarianceAtCompletion() {
        // VAC = BAC - EAC
        SortedMap<LocalDate, BigDecimal> vac = new TreeMap<LocalDate, BigDecimal>();
        SortedMap<LocalDate, BigDecimal> bac = indicators
                .get(EarnedValueType.BAC);
        SortedMap<LocalDate, BigDecimal> eac = indicators
                .get(EarnedValueType.EAC);

        for (LocalDate day : bac.keySet()) {
            vac.put(day, bac.get(day).subtract(eac.get(day)));
        }

        indicators.put(EarnedValueType.VAC, vac);
    }

    private void calculateEstimatedToComplete() {
        // ETC = EAC - ACWP
        SortedMap<LocalDate, BigDecimal> etc = new TreeMap<LocalDate, BigDecimal>();
        SortedMap<LocalDate, BigDecimal> eac = indicators
                .get(EarnedValueType.EAC);
        SortedMap<LocalDate, BigDecimal> acwp = indicators
                .get(EarnedValueType.ACWP);

        for (LocalDate day : eac.keySet()) {
            etc.put(day, eac.get(day).subtract(acwp.get(day)));
        }

        indicators.put(EarnedValueType.ETC, etc);
    }

    private void calculateCostPerformanceIndex() {
        // CPI = BCWP / ACWP
        SortedMap<LocalDate, BigDecimal> cpi = new TreeMap<LocalDate, BigDecimal>();
        SortedMap<LocalDate, BigDecimal> bcwp = indicators
                .get(EarnedValueType.BCWP);
        SortedMap<LocalDate, BigDecimal> acwp = indicators
                .get(EarnedValueType.ACWP);

        for (LocalDate day : bcwp.keySet()) {
            BigDecimal value = BigDecimal.ZERO;
            if (acwp.get(day).compareTo(BigDecimal.ZERO) != 0) {
                value = bcwp.get(day).divide(acwp.get(day), RoundingMode.DOWN);
            }
            cpi.put(day, value);
        }

        indicators.put(EarnedValueType.CPI, cpi);
    }

    private void calculateSchedulePerformanceIndex() {
        // SPI = BCWP / BCWS
        SortedMap<LocalDate, BigDecimal> spi = new TreeMap<LocalDate, BigDecimal>();
        SortedMap<LocalDate, BigDecimal> bcwp = indicators
                .get(EarnedValueType.BCWP);
        SortedMap<LocalDate, BigDecimal> bcws = indicators
                .get(EarnedValueType.BCWS);

        for (LocalDate day : bcwp.keySet()) {
            BigDecimal value = BigDecimal.ZERO;
            if (bcws.get(day).compareTo(BigDecimal.ZERO) != 0) {
                value = bcwp.get(day).divide(bcws.get(day), RoundingMode.DOWN);
            }
            spi.put(day, value);
        }

        indicators.put(EarnedValueType.SPI, spi);
    }

    protected abstract Set<EarnedValueType> getSelectedIndicators();

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
        chart.setHeight("100px");
    }

}
