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
import java.math.MathContext;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.TaskStatusEnum;
import org.libreplan.business.planner.entities.visitors.AccumulateTasksStatusVisitor;
import org.libreplan.business.planner.entities.visitors.ResetTasksStatusVisitor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Model for UI operations related to Order Dashboard View
 * @author Nacho Barrientos <nacho@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DashboardModel {

    private Order currentOrder;

    private Map<TaskStatusEnum, BigDecimal> taskStatusStats;

    public DashboardModel() {
        taskStatusStats = new EnumMap<TaskStatusEnum, BigDecimal>(TaskStatusEnum.class);
    }

    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
        this.calculateTaskStatusStatistics();
    }

    /* Progress KPI: "Number of tasks by status" */
    public BigDecimal getPercentageOfFinishedTasks() {
        return taskStatusStats.get(TaskStatusEnum.FINISHED);
    }

    public BigDecimal getPercentageOfInProgressTasks() {
        return taskStatusStats.get(TaskStatusEnum.IN_PROGRESS);
    }

    public BigDecimal getPercentageOfReadyToStartTasks() {
        return taskStatusStats.get(TaskStatusEnum.READY_TO_START);
    }

    public BigDecimal getPercentageOfBlockedTasks() {
        return taskStatusStats.get(TaskStatusEnum.BLOCKED);
    }

    /* Progress KPI: "Global Progress of the Project" */
    public BigDecimal getAdvancePercentageByHours(){
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if(rootAsTaskGroup == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratio = rootAsTaskGroup.getProgressAllByNumHours();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    public BigDecimal getTheoreticalAdvancePercentageByHoursUntilNow(){
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if(rootAsTaskGroup == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratio = rootAsTaskGroup.getTheoreticalProgressByNumHoursForAllTasksUntilNow();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    public BigDecimal getCriticalPathProgressByNumHours() {
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if(rootAsTaskGroup == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratio = rootAsTaskGroup.getCriticalPathProgressByNumHours();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    public BigDecimal getTheoreticalProgressByNumHoursForCriticalPathUntilNow() {
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if(rootAsTaskGroup == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratio = rootAsTaskGroup.getTheoreticalProgressByNumHoursForCriticalPathUntilNow();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    public BigDecimal getCriticalPathProgressByDuration() {
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if(rootAsTaskGroup == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratio = rootAsTaskGroup.getCriticalPathProgressByDuration();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    public BigDecimal getTheoreticalProgressByDurationForCriticalPathUntilNow() {
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if(rootAsTaskGroup == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal ratio = rootAsTaskGroup.getTheoreticalProgressByDurationForCriticalPathUntilNow();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    private void calculateTaskStatusStatistics() {
        AccumulateTasksStatusVisitor visitor = new AccumulateTasksStatusVisitor();
        TaskElement rootTask = getRootTask();
        if(rootTask != null) {
            resetTasksStatusInGraph();
            rootTask.acceptVisitor(visitor);
        }
        Map<TaskStatusEnum, Integer> count = visitor.getTaskStatusData();
        int totalTasks = this.countTasksInAResultMap(count);

        for (Map.Entry<TaskStatusEnum, Integer> entry : count.entrySet()) {
            BigDecimal percentage;
            if(totalTasks == 0){
                percentage = BigDecimal.ZERO;

            } else {
                percentage = new BigDecimal(100*(entry.getValue()/(1.0*totalTasks)),
                        MathContext.DECIMAL32);
            }
            taskStatusStats.put(entry.getKey(), percentage);
        }
    }

    private TaskElement getRootTask(){
        return currentOrder.getAssociatedTaskElement();
    }

    private void resetTasksStatusInGraph() {
        ResetTasksStatusVisitor visitor = new ResetTasksStatusVisitor();
        getRootTask().acceptVisitor(visitor);
    }

    private int countTasksInAResultMap(Map<? extends Object, Integer> map) {
        int sum = 0;
        for (Object count: map.values()) {
            sum += (Integer)count;
        }
        return sum;
    }

}
