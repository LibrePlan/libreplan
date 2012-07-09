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
import org.libreplan.business.planner.entities.IOrderEarnedValueCalculator;
import org.libreplan.web.planner.order.OrderPlanningModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino García <ltilve@igalia.com>
 *
 *         Model for UI operations related to CostStatus in Dashboard view
 *
 *         FIXME: This Model contains several operations for calculating 'Earned
 *         Value' measures related with cost. The code for calculating the basic
 *         measures: BCWP, ACWP and BCWS is copied from
 *         {@link OrderPlanningModel}. At this moment this code cannot be reused
 *         as it's coupled with the logic for displaying the 'Earned Value'
 *         chart. We may consider to refactor this code in the future.
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CostStatusModel implements ICostStatusModel {

    @Autowired
    private IOrderEarnedValueCalculator earnedValueCalculator;

    private Order order;

    public CostStatusModel() {

    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getActualCostWorkPerformedAt(LocalDate date) {
        return earnedValueCalculator.getActualCostWorkPerformedAt(order, date);
    }

    @Override
    public BigDecimal getCostPerformanceIndex(BigDecimal budgetedCost,
            BigDecimal actualCost) {
        return earnedValueCalculator.getCostPerformanceIndex(budgetedCost,
                actualCost);
    }

    @Override
    public BigDecimal getCostVariance(BigDecimal budgetedCost,
            BigDecimal actualCost) {
        return earnedValueCalculator.getCostVariance(budgetedCost, actualCost);
    }

    @Override
    public BigDecimal getEstimateAtCompletion(BigDecimal budgetAtCompletion,
            BigDecimal costPerformanceIndex) {
        return earnedValueCalculator.getEstimateAtCompletion(
                budgetAtCompletion, costPerformanceIndex);
    }

    @Override
    public BigDecimal getVarianceAtCompletion(BigDecimal budgetAtCompletion,
            BigDecimal estimateAtCompletion) {
        return budgetAtCompletion.subtract(estimateAtCompletion);
    }

    @Override
    public void setCurrentOrder(Order order) {
        this.order = order;
    }

    @Override
    public BigDecimal getBudgetAtCompletion() {
        return earnedValueCalculator.getBudgetAtCompletion(order);
    }

    @Override
    public BigDecimal getBudgetedCostWorkPerformedAt(LocalDate date) {
        return earnedValueCalculator
                .getBudgetedCostWorkPerformedAt(order, date);
    }

    @Override
    public BigDecimal getEstimateToComplete(BigDecimal estimateAtCompletion,
            BigDecimal actualCost) {
        return earnedValueCalculator.getEstimateToComplete(
                estimateAtCompletion, actualCost);
    }

}