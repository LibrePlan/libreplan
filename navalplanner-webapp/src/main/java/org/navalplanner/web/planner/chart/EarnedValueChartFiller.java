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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.zkforge.timeplot.Plotinfo;
import org.zkoss.ganttz.util.Interval;


/**
 * Abstract class with the common functionality for the earned value chart.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class EarnedValueChartFiller extends ChartFiller {

    protected SortedMap<LocalDate, BigDecimal> bcws;
    protected SortedMap<LocalDate, BigDecimal> acwp;
    protected SortedMap<LocalDate, BigDecimal> bcwp;
    protected SortedMap<LocalDate, BigDecimal> cv;
    protected SortedMap<LocalDate, BigDecimal> sv;
    protected SortedMap<LocalDate, BigDecimal> bac;
    protected SortedMap<LocalDate, BigDecimal> eac;
    protected SortedMap<LocalDate, BigDecimal> vac;
    protected SortedMap<LocalDate, BigDecimal> etc;
    protected SortedMap<LocalDate, BigDecimal> cpi;
    protected SortedMap<LocalDate, BigDecimal> spi;

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
        cv = new TreeMap<LocalDate, BigDecimal>();
        for (LocalDate day : bcwp.keySet()) {
            cv.put(day, bcwp.get(day).subtract(acwp.get(day)));
        }
    }

    private void calculateScheduleVariance() {
        // SV = BCWP - BCWS
        sv = new TreeMap<LocalDate, BigDecimal>();
        for (LocalDate day : bcwp.keySet()) {
            sv.put(day, bcwp.get(day).subtract(bcws.get(day)));
        }
    }

    private void calculateBudgetAtCompletion() {
        // BAC = max (BCWS)
        bac = new TreeMap<LocalDate, BigDecimal>();
        BigDecimal value = Collections.max(bcws.values());
        for (LocalDate day : bcws.keySet()) {
            bac.put(day, value);
        }
    }

    private void calculateEstimateAtCompletion() {
        // EAC = (ACWP/BCWP) * BAC
        eac = new TreeMap<LocalDate, BigDecimal>();
        for (LocalDate day : acwp.keySet()) {
            BigDecimal value = BigDecimal.ZERO;
            if (bcwp.get(day).compareTo(BigDecimal.ZERO) != 0) {
                value = acwp.get(day).divide(bcwp.get(day), RoundingMode.DOWN)
                        .multiply(bac.get(day));
            }
            eac.put(day, value);
        }
    }

    private void calculateVarianceAtCompletion() {
        // VAC = BAC - EAC
        vac = new TreeMap<LocalDate, BigDecimal>();
        for (LocalDate day : bac.keySet()) {
            vac.put(day, bac.get(day).subtract(eac.get(day)));
        }
    }

    private void calculateEstimatedToComplete() {
        // ETC = EAC - ACWP
        etc = new TreeMap<LocalDate, BigDecimal>();
        for (LocalDate day : eac.keySet()) {
            etc.put(day, eac.get(day).subtract(acwp.get(day)));
        }
    }

    private void calculateCostPerformanceIndex() {
        // CPI = BCWP / ACWP
        cpi = new TreeMap<LocalDate, BigDecimal>();
        for (LocalDate day : bcwp.keySet()) {
            BigDecimal value = BigDecimal.ZERO;
            if (acwp.get(day).compareTo(BigDecimal.ZERO) != 0) {
                value = bcwp.get(day).divide(acwp.get(day), RoundingMode.DOWN);
            }
            cpi.put(day, value);
        }
    }

    private void calculateSchedulePerformanceIndex() {
        // SPI = BCWP / BCWS
        spi = new TreeMap<LocalDate, BigDecimal>();
        for (LocalDate day : bcwp.keySet()) {
            BigDecimal value = BigDecimal.ZERO;
            if (bcws.get(day).compareTo(BigDecimal.ZERO) != 0) {
                value = bcwp.get(day).divide(bcws.get(day), RoundingMode.DOWN);
            }
            spi.put(day, value);
        }
    }

}
