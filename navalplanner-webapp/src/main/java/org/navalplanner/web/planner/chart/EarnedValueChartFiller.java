/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.web.I18nHelper;
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

    public static <K, V> void forValuesAtSameKey(Map<K, V> a, Map<K, V> b,
            IOperation<K, V> onSameKey) {
        for (Entry<K, V> each : a.entrySet()) {
            V aValue = each.getValue();
            V bValue = b.get(each.getKey());
            onSameKey.operate(each.getKey(), aValue, bValue);
        }
    }
    public interface IOperation<K, V> {

        public void operate(K key, V a, V b);

        public void undefinedFor(K key);
    }

    protected static abstract class PreconditionChecker<K, V> implements
            IOperation<K, V> {

        private final IOperation<K, V> decorated;

        protected PreconditionChecker(IOperation<K, V> decorated) {
            this.decorated = decorated;
        }

        @Override
        public void operate(K key, V a, V b) {
            if (isOperationDefinedFor(key, a, b)) {
                decorated.operate(key, a, b);
            } else {
                decorated.undefinedFor(key);
            }
        }

        protected abstract boolean isOperationDefinedFor(K key, V a, V b);

        @Override
        public void undefinedFor(K key) {
            decorated.undefinedFor(key);
        }

    }

    public static <K, V> IOperation<K, V> notNullOperands(
            final IOperation<K, V> operation) {
        return new PreconditionChecker<K, V>(operation) {
            @Override
            protected boolean isOperationDefinedFor(K key, V a, V b) {
                return a != null && b != null;
            }
        };
    }

    public static <K> IOperation<K, BigDecimal> secondOperandNotZero(
            final IOperation<K, BigDecimal> operation) {
        return new PreconditionChecker<K, BigDecimal>(operation) {
            @Override
            protected boolean isOperationDefinedFor(K key, BigDecimal a, BigDecimal b) {
                return b.signum() != 0;
            }
        };
    }

    public static boolean includes(Interval interval, LocalDate date) {
        LocalDate start = interval.getStart();
        LocalDate end = interval.getFinish();
        return start.compareTo(date) <= 0 && date.compareTo(end) < 0;
    }
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

    protected Map<EarnedValueType, SortedMap<LocalDate, BigDecimal>> indicators = new HashMap<EarnedValueType, SortedMap<LocalDate, BigDecimal>>();
    private Interval indicatorsInterval;

    protected abstract void calculateBudgetedCostWorkScheduled(Interval interval);
    protected abstract void calculateActualCostWorkPerformed(Interval interval);
    protected abstract void calculateBudgetedCostWorkPerformed(Interval interval);

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

    public BigDecimal getIndicator(EarnedValueType indicator, LocalDate date) {
        return indicators.get(indicator).get(date);
    }

    private void calculateCostVariance() {
        // CV = BCWP - ACWP
        indicators.put(EarnedValueType.CV,
                substract(EarnedValueType.BCWP, EarnedValueType.ACWP));
    }

    private void calculateScheduleVariance() {
        // SV = BCWP - BCWS

        indicators.put(EarnedValueType.SV,
                substract(EarnedValueType.BCWP, EarnedValueType.BCWS));
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
        SortedMap<LocalDate, BigDecimal> dividend = divide(
                EarnedValueType.ACWP, EarnedValueType.BCWP,
                BigDecimal.ZERO);
        SortedMap<LocalDate, BigDecimal> bac = indicators
                .get(EarnedValueType.BAC);
        indicators.put(EarnedValueType.EAC, multiply(dividend, bac));
    }

    private static SortedMap<LocalDate, BigDecimal> multiply(
            Map<LocalDate, BigDecimal> firstFactor,
            Map<LocalDate, BigDecimal> secondFactor) {
        final SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        forValuesAtSameKey(firstFactor, secondFactor,
                multiplicationOperation(result));
        return result;
    }

    private static IOperation<LocalDate, BigDecimal> multiplicationOperation(
            final SortedMap<LocalDate, BigDecimal> result) {
        return notNullOperands(new IOperation<LocalDate, BigDecimal>() {

            @Override
            public void operate(LocalDate key, BigDecimal a,
                    BigDecimal b) {
                result.put(key, a.multiply(b));
            }

            @Override
            public void undefinedFor(LocalDate key) {
                result.put(key, BigDecimal.ZERO);
            }
        });
    }

    private void calculateVarianceAtCompletion() {
        indicators.put(EarnedValueType.VAC,
                substract(EarnedValueType.BAC, EarnedValueType.EAC));
    }

    private void calculateEstimatedToComplete() {
        // ETC = EAC - ACWP
        indicators.put(EarnedValueType.ETC,
                substract(EarnedValueType.EAC, EarnedValueType.ACWP));
    }

    private SortedMap<LocalDate, BigDecimal> substract(EarnedValueType minuend,
            EarnedValueType subtrahend) {
        return substract(indicators.get(minuend), indicators.get(subtrahend));
    }

    private static SortedMap<LocalDate, BigDecimal> substract(
            Map<LocalDate, BigDecimal> minuend,
            Map<LocalDate, BigDecimal> subtrahend) {
        final SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        forValuesAtSameKey(minuend, subtrahend, substractionOperation(result));
        return result;
    }

    private static IOperation<LocalDate, BigDecimal> substractionOperation(
            final SortedMap<LocalDate, BigDecimal> result) {
        return notNullOperands(new IOperation<LocalDate, BigDecimal>() {

            @Override
            public void operate(LocalDate key, BigDecimal minuedValue,
                    BigDecimal subtrahendValue) {
                result.put(key,
                        minuedValue.subtract(subtrahendValue));
            }

            @Override
            public void undefinedFor(LocalDate key) {
            }
        });
    }

    private void calculateCostPerformanceIndex() {
        // CPI = BCWP / ACWP
        indicators.put(EarnedValueType.CPI,
                divide(EarnedValueType.BCWP, EarnedValueType.ACWP,
                        BigDecimal.ZERO));
    }

    private void calculateSchedulePerformanceIndex() {
        // SPI = BCWP / BCWS
        indicators.put(EarnedValueType.SPI,
                divide(EarnedValueType.BCWP, EarnedValueType.BCWS,
                        BigDecimal.ZERO));
    }

    private SortedMap<LocalDate, BigDecimal> divide(EarnedValueType dividend,
            EarnedValueType divisor, BigDecimal defaultIfNotComputable) {
        Validate.notNull(indicators.get(dividend));
        Validate.notNull(indicators.get(divisor));
        return divide(indicators.get(dividend), indicators.get(divisor),
                defaultIfNotComputable);
    }

    private static SortedMap<LocalDate, BigDecimal> divide(
            Map<LocalDate, BigDecimal> dividend,
            Map<LocalDate, BigDecimal> divisor,
            final BigDecimal defaultIfNotComputable) {
        final TreeMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        forValuesAtSameKey(dividend, divisor,
                divisionOperation(result, defaultIfNotComputable));
        return result;
    }

    private static IOperation<LocalDate, BigDecimal> divisionOperation(
            final TreeMap<LocalDate, BigDecimal> result,
            final BigDecimal defaultIfNotComputable) {
        return notNullOperands(secondOperandNotZero(new IOperation<LocalDate, BigDecimal>() {

            @Override
            public void operate(LocalDate key, BigDecimal dividendValue,
                    BigDecimal divisorValue) {
                result.put(key, dividendValue.divide(divisorValue,
                        RoundingMode.DOWN));
            }

            @Override
            public void undefinedFor(LocalDate key) {
                result.put(key, defaultIfNotComputable);
            }
      }));
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
                .getFinish();
    }
    protected void addZeroBeforeTheFirstValue(
            SortedMap<LocalDate, BigDecimal> map) {
        if (!map.isEmpty()) {
            map.put(map.firstKey().minusDays(1), BigDecimal.ZERO);
        }
    }

}
