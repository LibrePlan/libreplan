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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.chart.ContiguousDaysLine;
import org.libreplan.business.planner.chart.ContiguousDaysLine.OnDay;
import org.libreplan.business.planner.entities.IOrderResourceLoadCalculator;
import org.libreplan.business.planner.entities.TaskDeadlineViolationStatusEnum;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.TaskStatusEnum;
import org.libreplan.business.planner.entities.visitors.AccumulateTasksDeadlineStatusVisitor;
import org.libreplan.business.planner.entities.visitors.AccumulateTasksStatusVisitor;
import org.libreplan.business.planner.entities.visitors.CalculateFinishedTasksEstimationDeviationVisitor;
import org.libreplan.business.planner.entities.visitors.CalculateFinishedTasksLagInCompletionVisitor;
import org.libreplan.business.planner.entities.visitors.ResetTasksStatusVisitor;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Nacho Barrientos <nacho@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Model for UI operations related to Order Dashboard View
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DashboardModel implements IDashboardModel {

    @Autowired
    private IOrderResourceLoadCalculator resourceLoadCalculator;

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
    private List<TaskElement> criticalPath;
    private Integer taskCount = null;

    private final Map<TaskStatusEnum, BigDecimal> taskStatusStats;
    private final Map<TaskDeadlineViolationStatusEnum, BigDecimal> taskDeadlineViolationStatusStats;
    private BigDecimal marginWithDeadLine;
    private Integer absoluteMarginWithDeadLine;

    public DashboardModel() {
        taskStatusStats = new EnumMap<TaskStatusEnum, BigDecimal>(
                TaskStatusEnum.class);
        taskDeadlineViolationStatusStats = new EnumMap<TaskDeadlineViolationStatusEnum, BigDecimal>(
                TaskDeadlineViolationStatusEnum.class);
    }

    @Override
    public void setCurrentOrder(PlanningState planningState, List<TaskElement> criticalPath) {
        final Order order = planningState.getOrder();

        resourceLoadCalculator.setOrder(order,
                planningState.getAssignmentsCalculator());
        this.currentOrder = order;
        this.criticalPath = criticalPath;
        this.taskCount = null;
        if (tasksAvailable()) {
            this.calculateGlobalProgress();
            this.calculateTaskStatusStatistics();
            this.calculateTaskViolationStatusStatistics();
            this.calculateAbsoluteMarginWithDeadLine();
            this.calculateMarginWithDeadLine();
        }
    }

    /* Progress KPI: "Number of tasks by status" */
    @Override
    public BigDecimal getPercentageOfFinishedTasks() {
        return taskStatusStats.get(TaskStatusEnum.FINISHED);
    }

    @Override
    public BigDecimal getPercentageOfInProgressTasks() {
        return taskStatusStats.get(TaskStatusEnum.IN_PROGRESS);
    }

    @Override
    public BigDecimal getPercentageOfReadyToStartTasks() {
        return taskStatusStats.get(TaskStatusEnum.READY_TO_START);
    }

    @Override
    public BigDecimal getPercentageOfBlockedTasks() {
        return taskStatusStats.get(TaskStatusEnum.BLOCKED);
    }

    /* Progress KPI: "Deadline violation" */
    @Override
    public BigDecimal getPercentageOfOnScheduleTasks() {
        return taskDeadlineViolationStatusStats
                .get(TaskDeadlineViolationStatusEnum.ON_SCHEDULE);
    }

    @Override
    public BigDecimal getPercentageOfTasksWithViolatedDeadline() {
        return taskDeadlineViolationStatusStats
                .get(TaskDeadlineViolationStatusEnum.DEADLINE_VIOLATED);
    }

    @Override
    public BigDecimal getPercentageOfTasksWithNoDeadline() {
        return taskDeadlineViolationStatusStats
                .get(TaskDeadlineViolationStatusEnum.NO_DEADLINE);
    }

    /* Progress KPI: "Global Progress of the Project" */
    private void calculateGlobalProgress() {
        TaskGroup rootTask = getRootTask();
        if (rootTask == null) {
            throw new RuntimeException("Root task is null");
        }
        rootTask.updateCriticalPathProgress(criticalPath);
    }

    @Override
    public BigDecimal getSpreadProgress() {
        return asPercentage(getRootTask().getAdvancePercentage());
    }

    private BigDecimal asPercentage(BigDecimal value) {
        return value != null ? value.multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getAdvancePercentageByHours() {
        return asPercentage(getRootTask().getProgressAllByNumHours());
    }

    @Override
    public BigDecimal getExpectedAdvancePercentageByHours() {
        return asPercentage(getRootTask()
                .getTheoreticalProgressByNumHoursForAllTasksUntilNow());
    }

    @Override
    public BigDecimal getCriticalPathProgressByNumHours() {
        return asPercentage(getRootTask().getCriticalPathProgressByNumHours());
    }

    @Override
    public BigDecimal getExpectedCriticalPathProgressByNumHours() {
        return asPercentage(getRootTask()
                .getTheoreticalProgressByNumHoursForCriticalPathUntilNow());
    }

    @Override
    public BigDecimal getCriticalPathProgressByDuration() {
        return asPercentage(getRootTask().getCriticalPathProgressByDuration());
    }

    @Override
    public BigDecimal getExpectedCriticalPathProgressByDuration() {
        return asPercentage(getRootTask()
                .getTheoreticalProgressByDurationForCriticalPathUntilNow());
    }

    /* Time KPI: Margin with deadline */
    @Override
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
        TaskGroup rootTask = getRootTask();
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

    @Override
    public Integer getAbsoluteMarginWithDeadLine() {
        return absoluteMarginWithDeadLine;
    }

    private void calculateAbsoluteMarginWithDeadLine() {
        TaskElement rootTask = getRootTask();
        Date deadline = currentOrder.getDeadline();

        if (rootTask == null) {
            throw new RuntimeException("Root task is null");
        }
        if (deadline == null) {
            this.absoluteMarginWithDeadLine = null;
            return;
        }
        absoluteMarginWithDeadLine = daysBetween(rootTask.getEndAsLocalDate(),
                LocalDate.fromDateFields(deadline));
    }

    private int daysBetween(LocalDate start, LocalDate end) {
        return Days.daysBetween(start, end).getDays();
    }

    /**
     * Calculates the task completation deviations for the current order
     *
     * All the deviations are groups in Interval.MAX_INTERVALS intervals of
     * equal size. If the order contains just one single task then, the upper
     * limit will be the deviation of the task + 3, and the lower limit will be
     * deviation of the task - 2
     *
     * Each {@link Interval} contains the number of tasks that fit in that
     * interval
     *
     * @return
     */
    @Override
    public Map<Interval, Integer> calculateTaskCompletion() {
        Map<Interval, Integer> result = new LinkedHashMap<Interval, Integer>();
        Double max, min;

        // Get deviations of finished tasks, calculate max, min and delta
        List<Double> deviations = getTaskLagDeviations();
        if (deviations.isEmpty()) {
            max = Double.valueOf(3);
            min = Double.valueOf(-2);
        } else if (deviations.size() == 1) {
            max = deviations.get(0).doubleValue() + 3;
            min = deviations.get(0).doubleValue() - 2;
        } else {
            max = Collections.max(deviations);
            min = Collections.min(deviations);
        }
        double delta = (max - min) / Interval.MAX_INTERVALS;

        // Create MAX_INTERVALS
        double from = min;
        for (int i = 0; i < Interval.MAX_INTERVALS; i++) {
            result.put(Interval.create(from, from + delta), Integer.valueOf(0));
            from = from + delta;
        }

        // Construct map with number of tasks for each interval
        final Set<Interval> intervals = result.keySet();
        for (Double each : deviations) {
            Interval interval = Interval.containingValue(intervals, each);
            if (interval != null) {
                Integer value = result.get(interval);
                result.put(interval, value + 1);
            }
        }
        return result;
    }

    private List<Double> getTaskLagDeviations() {
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        CalculateFinishedTasksLagInCompletionVisitor visitor = new CalculateFinishedTasksLagInCompletionVisitor();
        TaskElement rootTask = getRootTask();
        rootTask.acceptVisitor(visitor);
        return visitor.getDeviations();
    }

    /**
     * Calculates the estimation accuracy deviations for the current order
     *
     * All the deviations are groups in Interval.MAX_INTERVALS intervals of
     * equal size. If the order contains just one single task then, the upper
     * limit will be the deviation of the task + 30, and the lower limit will be
     * deviation of the task - 20
     *
     * Each {@link Interval} contains the number of tasks that fit in that
     * interval
     *
     * @return
     */
    @Override
    public Map<Interval, Integer> calculateEstimationAccuracy() {
        Map<Interval, Integer> result = new LinkedHashMap<Interval, Integer>();
        Double max, min;

        // Get deviations of finished tasks, calculate max, min and delta
        List<Double> deviations = getEstimationAccuracyDeviations();
        if (deviations.isEmpty()) {
            max = Double.valueOf(30);
            min = Double.valueOf(-20);
        } else if (deviations.size() == 1) {
            max = deviations.get(0).doubleValue() + 30;
            min = deviations.get(0).doubleValue() - 20;
        } else {
            max = Collections.max(deviations);
            min = Collections.min(deviations);
        }
        double delta = (max - min) / Interval.MAX_INTERVALS;

        // Create MAX_INTERVALS
        double from = min;
        for (int i = 0; i < Interval.MAX_INTERVALS; i++) {
            result.put(Interval.create(from, from + delta), Integer.valueOf(0));
            from = from + delta;
        }

        // Construct map with number of tasks for each interval
        final Set<Interval> intervals = result.keySet();
        for (Double each : deviations) {
            Interval interval = Interval.containingValue(intervals, each);
            if (interval != null) {
                Integer value = result.get(interval);
                result.put(interval, value + 1);
            }
        }
        return result;
    }

    private List<Double> getEstimationAccuracyDeviations() {
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        CalculateFinishedTasksEstimationDeviationVisitor visitor = new CalculateFinishedTasksEstimationDeviationVisitor();
        TaskElement rootTask = getRootTask();
        rootTask.acceptVisitor(visitor);
        return visitor.getDeviations();
    }

    /**
     *
     * @author Diego Pino García<dpino@igalia.com>
     *
     */
    static class Interval {

        public static final double MAX_INTERVALS = 6;

        private double min;

        private double max;

        private Interval() {

        }

        public static Interval create(double min, double max) {
            return new Interval(min, max);
        }

        private Interval(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public static Interval copy(Interval interval) {
            return new Interval(interval.min, interval.max);
        }

        public static Interval containingValue(Collection<Interval> intervals,
                Double value) {
            for (Interval each : intervals) {
                if (each.includes(value)) {
                    return each;
                }
            }
            return null;
        }

        private boolean includes(double value) {
            return (value >= min) && (value <= max);
        }

        @Override
        public String toString() {
            return String.format("[%d, %d]", (int) Math.ceil(min),
                    (int) Math.ceil(max));
        }

    }

    @Override
    public Map<TaskStatusEnum, Integer> calculateTaskStatus() {
        AccumulateTasksStatusVisitor visitor = new AccumulateTasksStatusVisitor();
        TaskElement rootTask = getRootTask();
        if (this.getRootTask() == null) {
            throw new RuntimeException("Root task is null");
        }
        resetTasksStatusInGraph();
        rootTask.acceptVisitor(visitor);
        return visitor.getTaskStatusData();
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
        Map<TaskDeadlineViolationStatusEnum, Integer> count = visitor
                .getTaskDeadlineViolationStatusData();
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

    private TaskGroup getRootTask() {
        return currentOrder.getAssociatedTaskElement();
    }

    private void resetTasksStatusInGraph() {
        ResetTasksStatusVisitor visitor = new ResetTasksStatusVisitor();
        getRootTask().acceptVisitor(visitor);
    }

    private int countTasksInAResultMap(Map<? extends Object, Integer> map) {
        /*
         * It's only needed to count the number of tasks once each time setOrder
         * is called.
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

    @Override
    public boolean tasksAvailable() {
        return getRootTask() != null;
    }

    @Override
    public BigDecimal getOvertimeRatio() {
        EffortDuration totalLoad = sumAll(resourceLoadCalculator.getAllLoad());
        EffortDuration overload = sumAll(resourceLoadCalculator
                .getAllOverload());
        return overload.dividedByAndResultAsBigDecimal(totalLoad).setScale(2,
                RoundingMode.HALF_UP);
    }

    private EffortDuration sumAll(
            ContiguousDaysLine<EffortDuration> contiguousDays) {
        EffortDuration result = EffortDuration.zero();
        Iterator<OnDay<EffortDuration>> iterator = contiguousDays
                .iterator();
        while (iterator.hasNext()) {
            OnDay<EffortDuration> value = iterator.next();
            EffortDuration effort = value.getValue();
            result = EffortDuration.sum(result, effort);
        }
        return result;
    }

    @Override
    public BigDecimal getAvailabilityRatio() {
        EffortDuration totalLoad = sumAll(resourceLoadCalculator.getAllLoad());
        EffortDuration overload = sumAll(resourceLoadCalculator
                .getAllOverload());
        EffortDuration load = totalLoad.minus(overload);

        EffortDuration capacity = sumAll(resourceLoadCalculator
                .getMaxCapacityOnResources());
        return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP).subtract(
                load.dividedByAndResultAsBigDecimal(capacity));
    }

}
