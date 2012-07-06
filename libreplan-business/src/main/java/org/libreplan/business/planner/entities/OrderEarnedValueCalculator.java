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
import java.math.RoundingMode;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino García <dpino@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderEarnedValueCalculator extends EarnedValueCalculator implements IOrderEarnedValueCalculator {

    @Autowired
    private ICostCalculator hoursCostCalculator;

    @Transactional(readOnly = true)
    @Override
    public BigDecimal getActualCostWorkPerformedAt(Order order, LocalDate date) {
        SortedMap<LocalDate, BigDecimal> actualCost = calculateActualCostWorkPerformed(order);
        BigDecimal result = actualCost.get(date);
        return (result != null) ? result : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    @Override
    public SortedMap<LocalDate, BigDecimal> calculateActualCostWorkPerformed(
            Order order) {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        for (TaskElement taskElement : getAllTaskElements(order)) {
            if (taskElement instanceof Task) {
                addCost(result, getWorkReportCost((Task) taskElement));
            }
        }
        return accumulateResult(result);
    }

    private SortedMap<LocalDate, BigDecimal> accumulateResult(
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

    private void addCost(SortedMap<LocalDate, BigDecimal> currentCost,
            SortedMap<LocalDate, BigDecimal> additionalCost) {
        for (LocalDate day : additionalCost.keySet()) {
            if (!currentCost.containsKey(day)) {
                currentCost.put(day, BigDecimal.ZERO);
            }
            currentCost.put(day,
                    currentCost.get(day).add(additionalCost.get(day)));
        }
    }

    private List<TaskElement> getAllTaskElements(Order order) {
        List<TaskElement> result = order.getAllChildrenAssociatedTaskElements();
        result.add(order.getAssociatedTaskElement());
        return result;
    }

    private SortedMap<LocalDate, BigDecimal> getWorkReportCost(Task taskElement) {
        return hoursCostCalculator.getWorkReportCost(taskElement);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBudgetAtCompletion(Order order) {
		SortedMap<LocalDate, BigDecimal> budgedtedCost = calculateBudgetedCostWorkScheduled(order);
		return !budgedtedCost.isEmpty() ? budgedtedCost.get(budgedtedCost
				.lastKey()) : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public SortedMap<LocalDate, BigDecimal> calculateBudgetedCostWorkScheduled(
            Order order) {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        for (TaskElement taskElement : getAllTaskElements(order)) {
            if (taskElement instanceof Task) {
                addCost(result, getEstimatedCost((Task) taskElement));
            }
        }
        return accumulateResult(result);
    }

    private SortedMap<LocalDate, BigDecimal> getEstimatedCost(Task task) {
        return hoursCostCalculator.getEstimatedCost(task);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBudgetedCostWorkPerformedAt(Order order, LocalDate date) {
        SortedMap<LocalDate, BigDecimal> budgetedCost = calculateBudgetedCostWorkPerformed(order);
        BigDecimal result = budgetedCost.get(date);
        return (result != null) ? result : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public SortedMap<LocalDate, BigDecimal> calculateBudgetedCostWorkPerformed(
            Order order) {
        SortedMap<LocalDate, BigDecimal> estimatedCost = new TreeMap<LocalDate, BigDecimal>();
        for (TaskElement taskElement : getAllTaskElements(order)) {
            if (taskElement instanceof Task) {
                addCost(estimatedCost, getAdvanceCost((Task) taskElement));
            }
        }
        return accumulateResult(estimatedCost);
    }

    private SortedMap<LocalDate, BigDecimal> getAdvanceCost(Task task) {
        return hoursCostCalculator.getAdvanceCost(task);
    }

    @Override
    public BigDecimal getCostPerformanceIndex(BigDecimal budgetedCost,
            BigDecimal actualCost) {
        if (BigDecimal.ZERO.compareTo(actualCost) == 0) {
            return BigDecimal.ZERO;
        }
        return asPercentage(budgetedCost.divide(actualCost,
                RoundingMode.HALF_UP));
    }

    private BigDecimal asPercentage(BigDecimal value) {
        return value.multiply(BigDecimal.valueOf(100)).setScale(2);
    }

    @Override
    public BigDecimal getCostVariance(BigDecimal budgetedCost,
            BigDecimal actualCost) {
        return budgetedCost.subtract(actualCost);
    }

    @Override
    public BigDecimal getEstimateAtCompletion(BigDecimal budgetAtCompletion,
            BigDecimal costPerformanceIndex) {
        if (BigDecimal.ZERO.compareTo(costPerformanceIndex) == 0) {
            return BigDecimal.ZERO;
        }
        return asPercentage(budgetAtCompletion.divide(costPerformanceIndex));
    }

}
