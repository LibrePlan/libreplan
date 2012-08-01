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
import org.libreplan.business.orders.entities.Order;

/**
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Utility class for calculating all 'Earned Value' indicators
 */
public interface IOrderEarnedValueCalculator extends IEarnedValueCalculator {

    // ACWP　(Actual Cost Work Performed)
    SortedMap<LocalDate, BigDecimal> calculateActualCostWorkPerformed(
            Order order);

    // BCWP (Budgeted Cost Work Performed)
    SortedMap<LocalDate, BigDecimal> calculateBudgetedCostWorkPerformed(
            Order order);

    // BCWS (Budgeted Cost Work Scheduled)
    SortedMap<LocalDate, BigDecimal> calculateBudgetedCostWorkScheduled(Order order);

    // ACWP at date
    BigDecimal getActualCostWorkPerformedAt(Order order, LocalDate date);

    // BAC (Budget at Completion)
    BigDecimal getBudgetAtCompletion(Order order);

    // BCWP at date
    BigDecimal getBudgetedCostWorkPerformedAt(Order order, LocalDate date);

    // CPI (Cost Performance Index)
    BigDecimal getCostPerformanceIndex(BigDecimal budgetedCost,
            BigDecimal actualCost);

    // CV (Cost Variance)
    BigDecimal getCostVariance(BigDecimal budgetedCost, BigDecimal actualCost);

    // EAC (Estimate At Completion)
    BigDecimal getEstimateAtCompletion(BigDecimal budgetAtCompletion,
            BigDecimal costPerformanceIndex);

    // ETC (Estimate To Complete)
    BigDecimal getEstimateToComplete(BigDecimal estimateAtCompletion,
            BigDecimal actualCostWorkPerformed);

}