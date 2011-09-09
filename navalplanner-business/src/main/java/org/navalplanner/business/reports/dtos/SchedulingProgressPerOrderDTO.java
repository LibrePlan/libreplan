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

package org.navalplanner.business.reports.dtos;

import static org.navalplanner.business.reports.dtos.WorkingArrangementsPerOrderDTO.removeAfterDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.entities.WorkReportLine;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class SchedulingProgressPerOrderDTO {

    private IOrderDAO orderDAO;

    private IWorkReportLineDAO workReportLineDAO;

    private String orderName;

    private Integer estimatedHours;

    private Integer totalPlannedHours;

    private Integer partialPlannedHours;

    private BigDecimal realHours;

    private BigDecimal averageProgress;

    private Double imputedProgress;

    private Double plannedProgress;

    private BigDecimal costDifference;

    private BigDecimal planningDifference;

    private BigDecimal ratioCostDifference;

    private BigDecimal ratioPlanningDifference;

    private Boolean advanceTypeDoesNotApply = Boolean.FALSE;

    private Boolean appliedSpreadAdvanceType = Boolean.FALSE;

    private SchedulingProgressPerOrderDTO() {
        workReportLineDAO = Registry.getWorkReportLineDAO();
        orderDAO = Registry.getOrderDAO();
    }

    public SchedulingProgressPerOrderDTO(Order order, final List<Task> tasks,
            AdvanceType advanceType, LocalDate date) {
        this();
        this.orderName = order.getName();

        // Get average progress
        averageProgress = getFilterAdvanceTypePercentage(order, advanceType,
                date);
        if (averageProgress == null) {
            advanceTypeDoesNotApply = true;
            appliedSpreadAdvanceType = false;
            averageProgress = new BigDecimal(0);
        }

        // Fill DTO

        this.estimatedHours = getHoursSpecifiedAtOrder(tasks);
        this.totalPlannedHours = calculatePlannedHours(tasks, null);

        // Hours on time calculations
        this.partialPlannedHours = calculatePlannedHours(tasks, date);
        this.realHours = calculateRealHours(order, date);

        // Progress calculations
        this.imputedProgress = (totalPlannedHours != 0) ? new Double(
                realHours.doubleValue()
                / totalPlannedHours.doubleValue()) : new Double(0);
        this.plannedProgress = (totalPlannedHours != 0) ? new Double(
                partialPlannedHours / totalPlannedHours.doubleValue())
                : new Double(0);

        // Differences calculations
        this.costDifference = calculateCostDifference(averageProgress,
                new BigDecimal(totalPlannedHours), realHours);
        this.planningDifference = calculatePlanningDifference(averageProgress,
                new BigDecimal(totalPlannedHours), new BigDecimal(
                        partialPlannedHours));
        this.ratioCostDifference = calculateRatioCostDifference(
                averageProgress, imputedProgress);
        this.ratioPlanningDifference = calculateRatioPlanningDifference(
                averageProgress, plannedProgress);
    }

    private BigDecimal getFilterAdvanceTypePercentage(Order order,
            AdvanceType type, LocalDate date) {
        final BigDecimal result;
        if (type != null) {
            result = order.getAdvancePercentage(type, date);
            if (result != null) {
                return result;
            }
        }
        if (type != null) {
            advanceTypeDoesNotApply = true;
            appliedSpreadAdvanceType = true;
        }
        final DirectAdvanceAssignment directAdvanceAssignment = order
                .getReportGlobalAdvanceAssignment();
        return (directAdvanceAssignment != null) ? directAdvanceAssignment
                    .getAdvancePercentage(date) : null;
    }

    private Integer getHoursSpecifiedAtOrder(List<Task> tasks) {
        int result = 0;

        for (Task each: tasks) {
            result += each.getHoursSpecifiedAtOrder();
        }
        return result;
    }

    public Integer calculatePlannedHours(List<Task> tasks, LocalDate date) {
        int result = 0;

        for (Task each: tasks) {
            result += calculatePlannedHours(each, date);
        }
        return result;
    }

    public Integer calculatePlannedHours(Task task, LocalDate date) {
        final List<DayAssignment> dayAssignments = task.getDayAssignments();
        return DayAssignment.sum(removeAfterDate(dayAssignments, date))
                .roundToHours();
    }

    public BigDecimal calculateRealHours(Order order, LocalDate date) {
        BigDecimal result = BigDecimal.ZERO;

        final List<WorkReportLine> workReportLines = workReportLineDAO
                .findByOrderElementAndChildren(order);

        for (WorkReportLine workReportLine : workReportLines) {
            final LocalDate workReportLineDate = new LocalDate(workReportLine.getDate());
            if (date == null || workReportLineDate.compareTo(date) <= 0) {
                result = result.add(workReportLine.getEffort()
                        .toHoursAsDecimalWithScale(2));
            }
        }
        return result;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public Integer getTotalPlannedHours() {
        return totalPlannedHours;
    }

    public Integer getPartialPlannedHours() {
        return partialPlannedHours;
    }

    public BigDecimal getRealHours() {
        return realHours;
    }

    public BigDecimal getAverageProgress() {
        return averageProgress;
    }

    public Double getImputedProgress() {
        return imputedProgress;
    }

    public Double getPlannedProgress() {
        return plannedProgress;
    }

    public String getOrderName() {
        return orderName;
    }

    public BigDecimal calculateCostDifference(BigDecimal averageProgress,
            BigDecimal totalPlannedHours, BigDecimal realHours) {
        BigDecimal result = averageProgress;
        result = result.multiply(totalPlannedHours);
        return result.subtract(realHours);
    }

    public BigDecimal calculatePlanningDifference(BigDecimal averageProgress,
            BigDecimal totalPlannedHours, BigDecimal partialPlannedHours) {
        BigDecimal result = averageProgress;
        result = result.multiply(totalPlannedHours);
        return result.subtract(partialPlannedHours);
    }

    public BigDecimal calculateRatioCostDifference(BigDecimal averageProgress, Double imputedProgress) {
        if (imputedProgress.doubleValue() == 0) {
            return new BigDecimal(0);
        }
        return averageProgress.divide(new BigDecimal(imputedProgress), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateRatioPlanningDifference(BigDecimal averageProgress, Double plannedProgress) {
        if (plannedProgress.doubleValue() == 0) {
            return new BigDecimal(0);
        }
        return averageProgress.divide(new BigDecimal(plannedProgress), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getCostDifference() {
        return costDifference;
    }

    public BigDecimal getPlanningDifference() {
        return planningDifference;
    }

    public BigDecimal getRatioCostDifference() {
        return ratioCostDifference;
    }

    public BigDecimal getRatioPlanningDifference() {
        return ratioPlanningDifference;
    }

    public Boolean getAdvanceTypeDoesNotApply() {
        return advanceTypeDoesNotApply;
    }

    public Boolean getAppliedSpreadAdvanceType() {
        return appliedSpreadAdvanceType;
    }
}
