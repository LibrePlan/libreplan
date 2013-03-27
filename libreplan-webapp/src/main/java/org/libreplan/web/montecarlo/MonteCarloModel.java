/*
 * This file is part of LibrePlan
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

package org.libreplan.web.montecarlo;

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.Hibernate;
import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.util.LongOperationFeedback.IDesktopUpdatesEmitter;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 *         Calculates the MonteCarlo function for a list of tasks. Usually this
 *         list of tasks represents a critical path. There could be many
 *         critical paths in scheduling.
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MonteCarloModel implements IMonteCarloModel {

    @Autowired
    private ITaskElementDAO taskDAO;

    private String CRITICAL_PATH = _("Critical path");

    private String DEFAULT_CRITICAL_PATH = CRITICAL_PATH + " 1";

    private Map<String, List<MonteCarloTask>> criticalPaths = new HashMap<String, List<MonteCarloTask>>();

    private String orderName = "";

    private List<Task> tasksInCriticalPath;

    @Override
    @Transactional(readOnly = true)
    public void setCriticalPath(List<TaskElement> tasksInCriticalPath) {
        if (tasksInCriticalPath.isEmpty()) {
            return;
        }
        this.tasksInCriticalPath = onlyTasks(tasksInCriticalPath);
        if (this.tasksInCriticalPath.isEmpty()) {
            return;
        }
        Collections.sort(this.tasksInCriticalPath, Task.getByStartDateComparator());
        initializeTasksInOrder(getOrderFor(this.tasksInCriticalPath));
        initializeOrderNameFor(this.tasksInCriticalPath);
        feedCriticalPaths(this.tasksInCriticalPath);
    }

    /**
     * @param tasksInCriticalPath Cannot be null or empty
     * @return
     */
    private Order getOrderFor(List<Task> tasksInCriticalPath) {
        return tasksInCriticalPath.get(0).getOrderElement().getOrder();
    }

    private void feedCriticalPaths(List<Task> tasksInCriticalPath) {
        List<List<Task>> allCriticalPaths = buildAllPossibleCriticalPaths(tasksInCriticalPath);
        int i = 1;

        criticalPaths.clear();
        for (List<Task> path : allCriticalPaths) {
            criticalPaths.put(CRITICAL_PATH + " " + i++,
                    toMonteCarloTaskList(path));
        }
    }

    private List<List<Task>> buildAllPossibleCriticalPaths(
            List<Task> tasksInCriticalPath) {
        MonteCarloCriticalPathBuilder criticalPathBuilder = MonteCarloCriticalPathBuilder
                .create(tasksInCriticalPath);
        return criticalPathBuilder.buildAllPossibleCriticalPaths();
    }

    /**
     * Calculating all the critical paths, may need to explore other tasks that
     * are not part of the tasks that are on the critical path. So it's
     * necessary to initialize all the tasks in the order, otherwise a lazy
     * initialization may happen
     *
     * @param root
     */
    private void initializeTasksInOrder(Order root) {
        initializeTask(root);
        for (OrderElement each: root.getAllChildren()) {
            Hibernate.initialize(each);
            initializeTask(each);
        }
    }

    private void initializeTask(OrderElement orderElement) {
        TaskElement task = orderElement.getAssociatedTaskElement();
        if (task != null) {
            taskDAO.reattach(task);
            task.getCalendar();
            initializeDependenciesFor(task);
        }
    }

    private void initializeDependenciesFor(TaskElement task) {
        Set<Dependency> dependencies = task
                .getDependenciesWithThisDestination();
        Hibernate.initialize(dependencies);
        for (Dependency each : dependencies) {
            Hibernate.initialize(each.getOrigin());
            Hibernate.initialize(each.getDestination());
        }
    }

    private void initializeOrderNameFor(List<Task> tasksInCriticalPath) {
        orderName = tasksInCriticalPath.isEmpty() ? "" : getOrderFor(
                tasksInCriticalPath).getName();
    }

    @Override
    public List<String> getCriticalPathNames() {
        List<String> result = new ArrayList(criticalPaths.keySet());
        Collections.sort(result);
        return result;
    }

    @Override
    public List<MonteCarloTask> getCriticalPath(String name) {
        if (name == null || name.isEmpty()) {
            return criticalPaths.get(DEFAULT_CRITICAL_PATH);
        }
        return criticalPaths.get(name);
    }

    private List<Task> onlyTasks(List<TaskElement> tasks) {
        List<Task> result = new ArrayList<Task>();
        for (TaskElement each : tasks) {
            if (each instanceof Task) {
                result.add((Task) each);
            }
        }
        return result;
    }

    private List<MonteCarloTask> toMonteCarloTaskList(List<Task> path) {
        List<MonteCarloTask> result = new ArrayList<MonteCarloTask>();
        for (Task each : path) {
            result.add(MonteCarloTask.create(each));
        }
        return result;
    }

    @Override
    public Map<LocalDate, BigDecimal> calculateMonteCarlo(
            List<MonteCarloTask> _tasks, int iterations,
            IDesktopUpdatesEmitter<Integer> iterationProgress) {

        MonteCarloCalculation monteCarloCalculation = new MonteCarloCalculation(
                copyOf(_tasks), iterations, iterationProgress);
        Map<LocalDate, BigDecimal> monteCarloValues = monteCarloCalculation.doCalculation();

        // Convert number of times to probability
        for (LocalDate key : monteCarloValues.keySet()) {
            BigDecimal times = monteCarloValues.get(key);
            BigDecimal probability = times.divide(
                    BigDecimal.valueOf(iterations), 8, RoundingMode.HALF_UP);
            monteCarloValues.put(key, probability);
        }
        return monteCarloValues;
    }

    private List<MonteCarloTask> copyOf(List<MonteCarloTask> _tasks) {
        List<MonteCarloTask> result = new ArrayList<MonteCarloTask>();
        for (MonteCarloTask each: _tasks) {
            result.add(MonteCarloTask.copy(each));
        }
        return result;
    }

    @Override
    public String getOrderName() {
        return orderName;
    }

    private String toString(List<? extends TaskElement> tasks) {
        List<String> result = new ArrayList<String>();
        for (TaskElement each : tasks) {
            result.add(each.getName());
        }
        return StringUtils.join(result, ",");
    }

    private static class MonteCarloCalculation {

        private Map<MonteCarloTask, Set<EstimationRange>> estimationRangesForTasks = new HashMap<MonteCarloTask, Set<EstimationRange>>();

        private List<MonteCarloTask> tasks;

        private int iterations;

        private IDesktopUpdatesEmitter<Integer> iterationProgress;

        public MonteCarloCalculation(List<MonteCarloTask> tasks,
                int iterations,
                IDesktopUpdatesEmitter<Integer> iterationProgress) {
            adjustDurationDays(tasks);
            initializeEstimationRanges(tasks);
            this.tasks = tasks;
            this.iterations = iterations;
            this.iterationProgress = iterationProgress;
        }

        public Map<LocalDate, BigDecimal> doCalculation() {
            Map<LocalDate, BigDecimal> result = new HashMap<LocalDate, BigDecimal>();

            final int ONE_PERCENT = iterations / 100;
            Random randomGenerator = new Random((new Date()).getTime());
            // Calculate number of times a date is repeated
            for (int i = 0; i < iterations; i++) {
                LocalDate endDate = calculateEndDateFor(tasks, randomGenerator);
                BigDecimal times = result.get(endDate);
                times = times != null ? times.add(BigDecimal.ONE) : BigDecimal.ONE;
                result.put(endDate, times);
                if (i % ONE_PERCENT == 0) {
                    increaseProgressMeter((i * 100) / iterations);
                }
            }

            return result;
        }

        private void increaseProgressMeter(int completedPercent) {
            iterationProgress.doUpdate(completedPercent);
        }

        private LocalDate calculateEndDateFor(List<MonteCarloTask> tasks,
                Random randomGenerator) {
            Validate.notEmpty(tasks);
            BigDecimal durationDays = BigDecimal.ZERO;
            MonteCarloTask first = tasks.get(0);

            for (MonteCarloTask each : tasks) {
                BigDecimal randomNumber = generate(randomGenerator);
                durationDays = durationDays.add(getDuration(each, randomNumber));
            }
            return first.getStartDate().plusDays(durationDays.intValue());
        }

        private BigDecimal generate(Random randomGenerator) {
            return BigDecimal.valueOf(randomGenerator.nextDouble());
        }

        private BigDecimal getDuration(MonteCarloTask each, BigDecimal random) {
            ESTIMATION_TYPE type = getEstimationType(each, random);
            Validate.notNull(type);
            switch (type) {
            case PESSIMISTIC:
                BigDecimal duration = each.getPessimisticDuration();
                return duration;
            case NORMAL:
                return each.getNormalDuration();
            case OPTIMISTIC:
                return each.getOptimisticDuration();
            }
            return BigDecimal.ZERO;
        }

        private void adjustDurationDays(List<MonteCarloTask> tasks) {
            for (MonteCarloTask each : tasks) {
                each.setPessimisticDuration(MonteCarloTask
                        .calculateRealDurationFor(each,
                                each.getPessimisticDuration()));
                each.setNormalDuration(MonteCarloTask.calculateRealDurationFor(
                        each, each.getNormalDuration()));
                each.setOptimisticDuration(MonteCarloTask.calculateRealDurationFor(
                        each, each.getOptimisticDuration()));
            }
        }

        private void initializeEstimationRanges(List<MonteCarloTask> tasks) {
            estimationRangesForTasks.clear();
            for (MonteCarloTask each : tasks) {
                createEstimationRangesFor(each);
            }
        }

        private void createEstimationRangesFor(MonteCarloTask task) {
            Set<EstimationRange> estimationRanges = new HashSet<EstimationRange>();
            estimationRanges.add(EstimationRange.pessimisticRangeFor(task));
            estimationRanges.add(EstimationRange.normalRangeFor(task));
            estimationRanges.add(EstimationRange.optimisticRangeFor(task));
            estimationRangesForTasks.put(task, estimationRanges);
        }

        public ESTIMATION_TYPE getEstimationType(MonteCarloTask task,
                BigDecimal random) {
            Set<EstimationRange> estimationRanges = estimationRangesForTasks
                    .get(task);
            for (EstimationRange each : estimationRanges) {
                if (each.contains(random)) {
                    return each.getEstimationType();
                }
            }
            return null;
        }

    }

}
