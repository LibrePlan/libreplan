/*
 * This file is part of LibrePlan
 *
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

package org.libreplan.web.dashboard;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.Order;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
interface ICostStatusModel {

    // Actual Cost Work Performed (ACWP)
    BigDecimal getActualCostWorkPerformedAt(LocalDate date);

    // Budget At Completion (BAC)
    BigDecimal getBudgetAtCompletion();

    // Budgeted Cost Work Performed (BCWP)
    BigDecimal getBudgetedCostWorkPerformedAt(LocalDate date);

    // Cost Performance Index (CPI)
    BigDecimal getCostPerformanceIndex(BigDecimal budgetedCost,
            BigDecimal actualCost);

    // Cost Variance (CV)
    BigDecimal getCostVariance(BigDecimal budgetedCost, BigDecimal actualCost);

    // Estimate at Completion (EAC)
    BigDecimal getEstimateAtCompletion(BigDecimal budgetAtCompletion,
            BigDecimal costPerformanceIndex);

    // Variance at Completion (VAC)
    BigDecimal getVarianceAtCompletion(BigDecimal budgetAtCompletion,
            BigDecimal estimateAtCompletion);

    // Estimate to Complete (ETC)
    BigDecimal getEstimateToComplete(BigDecimal estimateAtCompletion,
            BigDecimal actualCost);

    void setCurrentOrder(Order order);

}