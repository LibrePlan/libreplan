/*
 * This file is part of LibrePlan
 *
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

package org.libreplan.web.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskStatusEnum;
import org.libreplan.web.dashboard.DashboardModel.Interval;

interface IDashboardModel {

    void setCurrentOrder(Order order);

    boolean tasksAvailable();

    /* Progress KPI: "Number of tasks by status" */
    BigDecimal getPercentageOfFinishedTasks();

    BigDecimal getPercentageOfInProgressTasks();

    BigDecimal getPercentageOfReadyToStartTasks();

    BigDecimal getPercentageOfBlockedTasks();

    /* Progress KPI: "Deadline violation" */
    BigDecimal getPercentageOfOnScheduleTasks();

    BigDecimal getPercentageOfTasksWithViolatedDeadline();

    BigDecimal getPercentageOfTasksWithNoDeadline();

    /* Progress KPI: "Global Progress of the Project" */
    BigDecimal getAdvancePercentageByHours();

    BigDecimal getExpectedAdvancePercentageByHours();

    BigDecimal getCriticalPathProgressByNumHours();

    BigDecimal getExpectedCriticalPathProgressByNumHours();

    BigDecimal getCriticalPathProgressByDuration();

    BigDecimal getExpectedCriticalPathProgressByDuration();

    /* Time KPI: "Margin with deadline" */
    BigDecimal getMarginWithDeadLine();

    Integer getAbsoluteMarginWithDeadLine();

    /* Time KPI: "Estimation accuracy" */
    List<Double> getFinishedTasksEstimationAccuracyHistogram();

    /* Time KPI: "Lead/Lag in task completion" */
    List<Double> getLagInTaskCompletionHistogram();

    Map<TaskStatusEnum, Integer> calculateTaskStatus();

    Map<Interval, Integer> calculateTaskCompletion();

    /* Resources KPI: "Overtime Ratio" */
    Map<Interval, Integer> calculateEstimationAccuracy();

    // (Load + Overload) / Load
    BigDecimal getOvertimeRatio();

}
