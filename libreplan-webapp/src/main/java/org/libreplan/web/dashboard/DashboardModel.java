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
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskDeadlineViolationStatusEnum;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.TaskStatusEnum;
import org.libreplan.business.planner.entities.visitors.AccumulateTasksDeadlineStatusVisitor;
import org.libreplan.business.planner.entities.visitors.AccumulateTasksStatusVisitor;
import org.libreplan.business.planner.entities.visitors.CalculateFinishedTasksEstimationDeviationVisitor;
import org.libreplan.business.planner.entities.visitors.CalculateFinishedTasksLagInCompletionVisitor;
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
public class DashboardModel implements IDashboardModel {

    /* Parameters */
    public final static int EA_STRETCHES_PERCENTAGE_STEP = 10;
    public final static int EA_STRETCHES_MIN_VALUE = -100;
    public final static int EA_STRETCHES_MAX_VALUE = 150;
    public final static int LTC_NUMBER_OF_INTERVALS = 10;

    /* To be calculated */
    public static double LTC_STRETCHES_STEP = 0;
    public static double LTC_STRETCHES_MIN_VALUE = 0;
    public static double LTC_STRETCHES_MAX_VALUE = 0;


    private Order currentOrder;
    private Integer taskCount = null;

    private Map<TaskStatusEnum, BigDecimal> taskStatusStats;
    private Map<TaskDeadlineViolationStatusEnum, BigDecimal> taskDeadlineViolationStatusStats;
    private List<Double> taskEstimationAccuracyHistogram;
    private BigDecimal marginWithDeadLine;
    private List<Double> lagInTaskCompletionHistogram;

    public DashboardModel() {
        taskStatusStats = new EnumMap<TaskStatusEnum, BigDecimal>(
                TaskStatusEnum.class);
        taskDeadlineViolationStatusStats = new EnumMap<TaskDeadlineViolationStatusEnum, BigDecimal>(
                TaskDeadlineViolationStatusEnum.class);
    }

    public void setCurrentOrder(Order order) {
        this.currentOrder = order;
        this.taskCount = null;
        if(tasksAvailable()) {
            this.calculateTaskStatusStatistics();
            this.calculateTaskViolationStatusStatistics();
            this.calculateMarginWithDeadLine();
            this.calculateFinishedTasksEstimationAccuracyHistogram();
            this.calculateLagInTaskCompletionHistogram();
        }
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

    /* Progress KPI: "Deadline violation" */
    public BigDecimal getPercentageOfOnScheduleTasks() {
        return taskDeadlineViolationStatusStats.get(TaskDeadlineViolationStatusEnum.ON_SCHEDULE);
    }

    public BigDecimal getPercentageOfTasksWithViolatedDeadline() {
        return taskDeadlineViolationStatusStats.get(TaskDeadlineViolationStatusEnum.DEADLINE_VIOLATED);
    }

    public BigDecimal getPercentageOfTasksWithNoDeadline() {
        return taskDeadlineViolationStatusStats.get(TaskDeadlineViolationStatusEnum.NO_DEADLINE);
    }

    /* Progress KPI: "Global Progress of the Project" */
    public BigDecimal getAdvancePercentageByHours(){
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        BigDecimal ratio = rootAsTaskGroup.getProgressAllByNumHours();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    public BigDecimal getTheoreticalAdvancePercentageByHoursUntilNow(){
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        BigDecimal ratio = rootAsTaskGroup.getTheoreticalProgressByNumHoursForAllTasksUntilNow();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    public BigDecimal getCriticalPathProgressByNumHours() {
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        BigDecimal ratio = rootAsTaskGroup.getCriticalPathProgressByNumHours();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    public BigDecimal getTheoreticalProgressByNumHoursForCriticalPathUntilNow() {
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        BigDecimal ratio = rootAsTaskGroup.getTheoreticalProgressByNumHoursForCriticalPathUntilNow();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    public BigDecimal getCriticalPathProgressByDuration() {
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        BigDecimal ratio = rootAsTaskGroup.getCriticalPathProgressByDuration();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    public BigDecimal getTheoreticalProgressByDurationForCriticalPathUntilNow() {
        TaskGroup rootAsTaskGroup = (TaskGroup)getRootTask();
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        BigDecimal ratio = rootAsTaskGroup.getTheoreticalProgressByDurationForCriticalPathUntilNow();
        return ratio.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN);
    }

    /* Time KPI: Margin with deadline */
    public BigDecimal getMarginWithDeadLine() {
        return this.marginWithDeadLine;
    }

    private void calculateMarginWithDeadLine() {
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        if (this.currentOrder.getDeadline() == null) {
            this.marginWithDeadLine = null;
            return;
        }
        TaskElement rootTask = getRootTask();
        Days orderDuration = Days.daysBetween(rootTask.getStartAsLocalDate(),
                rootTask.getEndAsLocalDate());

        LocalDate deadLineAsLocalDate = LocalDate.fromDateFields(currentOrder
                .getDeadline());
        Days deadlineOffset = Days.daysBetween(rootTask.getEndAsLocalDate(),
                deadLineAsLocalDate);

        BigDecimal outcome = new BigDecimal(deadlineOffset.getDays(),
                MathContext.DECIMAL32);
        this.marginWithDeadLine = outcome.divide(
                new BigDecimal(orderDuration.getDays()), 8,
                BigDecimal.ROUND_HALF_EVEN);
    }

    /* Time KPI: Estimation accuracy */
    public List<Double> getFinishedTasksEstimationAccuracyHistogram() {
        return this.taskEstimationAccuracyHistogram;
    }

    private void calculateFinishedTasksEstimationAccuracyHistogram() {
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        CalculateFinishedTasksEstimationDeviationVisitor visitor =
                new CalculateFinishedTasksEstimationDeviationVisitor();
        TaskElement rootTask = getRootTask();
        rootTask.acceptVisitor(visitor);
        List<Double> deviations = visitor.getDeviations();

        // [-100, -90), [-90, -80), ..., [190, 200), [200, inf)
        this.taskEstimationAccuracyHistogram = createHistogram(
                EA_STRETCHES_MIN_VALUE,
                EA_STRETCHES_MAX_VALUE,
                EA_STRETCHES_PERCENTAGE_STEP,
                deviations);
    }

    /* Time KPI: Lead/Lag in task completion */
    public List<Double> getLagInTaskCompletionHistogram() {
        return this.lagInTaskCompletionHistogram;
    }

    private void calculateLagInTaskCompletionHistogram() {
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        CalculateFinishedTasksLagInCompletionVisitor visitor =
                new CalculateFinishedTasksLagInCompletionVisitor();
        TaskElement rootTask = getRootTask();
        rootTask.acceptVisitor(visitor);
        List<Double> deviations = visitor.getDeviations();

        LTC_STRETCHES_MIN_VALUE = Collections.min(deviations);
        LTC_STRETCHES_MAX_VALUE = Collections.max(deviations);
        LTC_STRETCHES_STEP = (LTC_STRETCHES_MAX_VALUE - LTC_STRETCHES_MIN_VALUE)
                /LTC_NUMBER_OF_INTERVALS;
        this.lagInTaskCompletionHistogram = createHistogram(
                LTC_STRETCHES_MIN_VALUE,
                LTC_STRETCHES_MAX_VALUE,
                LTC_STRETCHES_STEP,
                deviations);
    }

    private List<Double> createHistogram(double lowBound, double highBound,
            double intervalStep, List<Double> values) {
        double variableRange = highBound - lowBound;
        /* TODO: What if highBound == lowBound? */
        int numberOfClasses = (int)(variableRange/intervalStep);
        int[] classes = new int[numberOfClasses+1];

        for(Double value: values) {
            int index;
            if (value >= highBound) {
                index = numberOfClasses;
            } else {
                index = (int)(numberOfClasses *
                    (((value.doubleValue() - lowBound))/variableRange));
            }
            classes[index]++;
        }

        List<Double> histogram = new ArrayList<Double>();
        int numberOfConsideredTasks = values.size();
        for (int numberOfElementsInClass: classes) {
            Double relativeCount = new Double(0.0);
            if (numberOfConsideredTasks > 0) {
                relativeCount = new Double(1.0*numberOfElementsInClass/
                        numberOfConsideredTasks);
            }
            histogram.add(relativeCount);
        }
        return histogram;
    }

    private void calculateTaskStatusStatistics() {
        AccumulateTasksStatusVisitor visitor = new AccumulateTasksStatusVisitor();
        TaskElement rootTask = getRootTask();
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        resetTasksStatusInGraph();
        rootTask.acceptVisitor(visitor);
        Map<TaskStatusEnum, Integer> count = visitor.getTaskStatusData();
        mapAbsoluteValuesToPercentages(count, taskStatusStats);
    }

    private void calculateTaskViolationStatusStatistics() {
        AccumulateTasksDeadlineStatusVisitor visitor = new AccumulateTasksDeadlineStatusVisitor();
        TaskElement rootTask = getRootTask();
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        rootTask.acceptVisitor(visitor);
        Map<TaskDeadlineViolationStatusEnum, Integer> count = visitor.getTaskDeadlineViolationStatusData();
        mapAbsoluteValuesToPercentages(count, taskDeadlineViolationStatusStats);
    }

    private <T> void mapAbsoluteValuesToPercentages(Map<T, Integer> source,
            Map<T, BigDecimal> dest) {
        int totalTasks = countTasksInAResultMap(source);
        for (Map.Entry<T, Integer> entry : source.entrySet()) {
            BigDecimal percentage;
            if (totalTasks == 0) {
                percentage = BigDecimal.ZERO;

            } else {
                percentage = new BigDecimal(
                        100 * (entry.getValue() / (1.0 * totalTasks)),
                        MathContext.DECIMAL32);
            }
            dest.put(entry.getKey(), percentage);
        }
    }

    private TaskElement getRootTask() {
        return currentOrder.getAssociatedTaskElement();
    }

    private void resetTasksStatusInGraph() {
        ResetTasksStatusVisitor visitor = new ResetTasksStatusVisitor();
        getRootTask().acceptVisitor(visitor);
    }

    private int countTasksInAResultMap(Map<? extends Object, Integer> map) {
        /* It's only needed to count the number of tasks once
         * each time setOrder is called.
         */
        if (this.taskCount != null) {
            return this.taskCount.intValue();
        }
        int sum = 0;
        for (Object count : map.values()) {
            sum += (Integer) count;
        }
        this.taskCount = new Integer(sum);
        return sum;
    }

    public boolean tasksAvailable() {
        return getRootTask() != null;
    }

}
