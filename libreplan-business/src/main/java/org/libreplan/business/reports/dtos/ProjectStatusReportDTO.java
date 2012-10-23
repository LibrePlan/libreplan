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

package org.libreplan.business.reports.dtos;

import java.math.BigDecimal;

import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.SumChargedEffort;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.workingday.EffortDuration;

/**
 * DTO to represent each row in the Project Status report.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class ProjectStatusReportDTO {

    private static final String EXCLAMATION_MARK = "!";

    private String code;

    private String name;

    private EffortDuration estimatedHours;

    private EffortDuration plannedHours;

    private EffortDuration imputedHours;

    private String hoursMark;

    private BigDecimal budget;

    private BigDecimal hoursCost;

    private BigDecimal expensesCost;

    private BigDecimal totalCost;

    private String costMark;

    public ProjectStatusReportDTO(EffortDuration estimatedHours,
            EffortDuration plannedHours, EffortDuration imputedHours,
            BigDecimal budget, BigDecimal hoursCost, BigDecimal expensesCost,
            BigDecimal totalCost) {
        this.estimatedHours = estimatedHours;
        this.plannedHours = plannedHours;
        this.imputedHours = imputedHours;
        this.budget = budget;
        this.hoursCost = hoursCost;
        this.expensesCost = expensesCost;
        this.totalCost = totalCost;
    }

    public ProjectStatusReportDTO(String code, String name,
            EffortDuration estimatedHours, EffortDuration plannedHours,
            EffortDuration imputedHours, BigDecimal budget,
            BigDecimal hoursCost, BigDecimal expensesCost, BigDecimal totalCost) {
        this(estimatedHours, plannedHours, imputedHours, budget, hoursCost,
                expensesCost, totalCost);
        this.code = code;
        this.name = name;
    }

    public ProjectStatusReportDTO(OrderElement orderElement,
            boolean appendProjectInName) {
        code = orderElement.getCode();

        if (appendProjectInName) {
            name = orderElement.getName() + " ("
                    + orderElement.getOrder().getName() + ")";
        } else {
            name = Util.getPrefixSpacesDependingOnDepth(orderElement)
                    + orderElement.getName();
        }

        Integer estimatedHours = orderElement.getWorkHours();
        this.estimatedHours = estimatedHours != null ? EffortDuration
                .hours(estimatedHours) : null;

        TaskSource taskSource = orderElement.getTaskSource();
        if (taskSource != null) {
            plannedHours = taskSource.getTask().getSumOfAssignedEffort();
        }

        SumChargedEffort sumChargedEffort = orderElement.getSumChargedEffort();
        if (sumChargedEffort != null) {
            imputedHours = sumChargedEffort.getTotalChargedEffort();
        }

        setBudget(orderElement.getBudget());
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getEstimatedHours() {
        return toString(estimatedHours);
    }

    public EffortDuration getEstimatedHoursAsEffortDuration() {
        return estimatedHours;
    }

    public String getPlannedHours() {
        return toString(plannedHours);
    }

    public EffortDuration getPlannedHoursAsEffortDuration() {
        return plannedHours;
    }

    public String getImputedHours() {
        return toString(imputedHours);
    }

    public EffortDuration getImputedHoursAsEffortDuration() {
        return imputedHours;
    }

    public static String toString(EffortDuration effortDuration) {
        if (effortDuration == null) {
            return null;
        }
        return effortDuration.toFormattedString();
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getHoursCost() {
        return hoursCost;
    }

    public void setHoursCost(BigDecimal hoursCost) {
        this.hoursCost = hoursCost;
    }

    public BigDecimal getExpensesCost() {
        return expensesCost;
    }

    public void setExpensesCost(BigDecimal expensesCost) {
        this.expensesCost = expensesCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getBudgetIntegerPart() {
        return Util.getIntegerPart(budget);
    }

    public BigDecimal getBudgetFractionalPart() {
        return Util.getFractionalPart(budget);
    }

    public BigDecimal getHoursCostIntegerPart() {
        return Util.getIntegerPart(hoursCost);
    }

    public BigDecimal getHoursCostFractionalPart() {
        return Util.getFractionalPart(hoursCost);
    }

    public BigDecimal getExpensesCostIntegerPart() {
        return Util.getIntegerPart(expensesCost);
    }

    public BigDecimal getExpensesCostFractionalPart() {
        return Util.getFractionalPart(expensesCost);
    }

    public BigDecimal getTotalCostIntegerPart() {
        return Util.getIntegerPart(totalCost);
    }

    public BigDecimal getTotalCostFractionalPart() {
        return Util.getFractionalPart(totalCost);
    }

    public void calculateMarks() {
        calculateHoursMark();
        calculateCostMark();
    }

    /**
     * {@link ProjectStatusReportDTO#hoursMark} will be <code>!</code> if
     * {@link ProjectStatusReportDTO#imputedHours} is bigger than
     * {@link ProjectStatusReportDTO#plannedHours}. If the task is not planned,
     * then the comparison will be done against
     * {@link ProjectStatusReportDTO#estimatedHours}, if it is zero then the
     * mark will be empty.
     */
    private void calculateHoursMark() {
        if (imputedHours != null) {
            if (plannedHours == null || plannedHours.isZero()) {
                if (estimatedHours != null && !estimatedHours.isZero()) {
                    if (imputedHours.compareTo(estimatedHours) > 0) {
                        hoursMark = EXCLAMATION_MARK;
                    }
                }
            } else {
                if (imputedHours.compareTo(plannedHours) > 0) {
                    hoursMark = EXCLAMATION_MARK;
                }
            }
        }
    }

    /**
     * {@link ProjectStatusReportDTO#costMark} will be <code>!</code> if
     * {@link ProjectStatusReportDTO#totalCost} is bigger than
     * {@link ProjectStatusReportDTO#budget}. If
     * {@link ProjectStatusReportDTO#budget} is zero then the mark will be
     * empty.
     */
    private void calculateCostMark() {
        if (totalCost != null) {
            if (budget != null && (budget.compareTo(BigDecimal.ZERO) > 0)) {
                if (totalCost.compareTo(budget) > 0) {
                    costMark = EXCLAMATION_MARK;
                }
            }
        }
    }

    public String getHoursMark() {
        return hoursMark;
    }

    public String getCostMark() {
        return costMark;
    }

}