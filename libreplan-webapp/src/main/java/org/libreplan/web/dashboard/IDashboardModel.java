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

import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskStatusEnum;
import org.libreplan.web.dashboard.DashboardModel.Interval;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;

interface IDashboardModel {

    void setCurrentOrder(PlanningState planningState, List<TaskElement> criticalPath);

    boolean tasksAvailable();

    /* Progress KPI: "Number of tasks by status" */
    Map<TaskStatusEnum, Integer> calculateTaskStatus();

    BigDecimal getPercentageOfFinishedTasks();

    BigDecimal getPercentageOfInProgressTasks();

    BigDecimal getPercentageOfReadyToStartTasks();

    BigDecimal getPercentageOfBlockedTasks();

    /* Progress KPI: "Deadline violation" */
    BigDecimal getPercentageOfOnScheduleTasks();

    BigDecimal getPercentageOfTasksWithViolatedDeadline();

    BigDecimal getPercentageOfTasksWithNoDeadline();

    /* Progress KPI: "Global Progress of the Project" */
    BigDecimal getSpreadProgress();

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
    Map<Interval, Integer> calculateEstimationAccuracy();

    /* Time KPI: "Lead/Lag in task completion" */
    Map<Interval, Integer> calculateTaskCompletion();

    /* Resources KPI: "Overtime Ratio" */

    // (Load + Overload) / Load
    BigDecimal getOvertimeRatio();

    // Load / Capacity
    BigDecimal getAvailabilityRatio();

}
