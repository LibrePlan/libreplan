/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.business.planner.entities;

import java.math.BigDecimal;
import java.util.SortedMap;

import org.joda.time.LocalDate;

/**
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Utility class for calculating all 'Earned Value' indicators
 */
public interface IEarnedValueCalculator {

    // CV = BCWP - ACWP
    SortedMap<LocalDate, BigDecimal> calculateCostVariance(
            SortedMap<LocalDate, BigDecimal> bcwp,
            SortedMap<LocalDate, BigDecimal> acwp);

    // SV = BCWP - BCWS
    SortedMap<LocalDate, BigDecimal> calculateScheduleVariance(
            SortedMap<LocalDate, BigDecimal> bcwp,
            SortedMap<LocalDate, BigDecimal> bcws);

    // BAC = max (BCWS)
    SortedMap<LocalDate, BigDecimal> calculateBudgetAtCompletion(
            SortedMap<LocalDate, BigDecimal> bcws);

    // LibrePlan Audiovisual (formula changed)
    // EAC = (ACWP/BCWS) * BAC
    SortedMap<LocalDate, BigDecimal> calculateEstimateAtCompletion(
            SortedMap<LocalDate, BigDecimal> acwp,
            SortedMap<LocalDate, BigDecimal> bcws,
            SortedMap<LocalDate, BigDecimal> bac);

    // VAC = BAC - EAC
    SortedMap<LocalDate, BigDecimal> calculateVarianceAtCompletion(
            SortedMap<LocalDate, BigDecimal> bac,
            SortedMap<LocalDate, BigDecimal> eac);

    // ETC = EAC - ACWP
    SortedMap<LocalDate, BigDecimal> calculateEstimatedToComplete(
            SortedMap<LocalDate, BigDecimal> eac,
            SortedMap<LocalDate, BigDecimal> acwp);

    // SPI = BCWP / BCWS
    SortedMap<LocalDate, BigDecimal> calculateSchedulePerformanceIndex(
            SortedMap<LocalDate, BigDecimal> bcwp,
            SortedMap<LocalDate, BigDecimal> bcws);

    // LibrePlan Audiovisual (formula changed)
    // CPI = BCWS / ACWP
    SortedMap<LocalDate, BigDecimal> calculateCostPerformanceIndex(
            SortedMap<LocalDate, BigDecimal> bcws,
            SortedMap<LocalDate, BigDecimal> acwp);

}
