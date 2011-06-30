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

package org.navalplanner.web.montecarlo;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
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
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
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
 *         A big chunk of code goes for determining all the possible critical
 *         paths, departing from a list of elements that contains all the tasks
 *         which are in the critical path. The algorithm determines first all
 *         the possible starting tasks and navigates them forward until reaching
 *         an end.
 *
 *         Navigating from a task to a taskgroup is a bit cumbersome. The
 *         algorithm considers than when there is a link between a task a
 *         taskgroup, the task is connected with all the taskgroup's children
 *         which have: a) no incoming dependencies b) has no incoming
 *         dependencies from a task that it's not a children of that taskgroup.
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

    /**
     * Constructs all possible paths starting from those tasks in the critical
     * path have no incoming dependencies or have incoming dependencies to other
     * tasks not in the critical path.
     *
     * Once all possible path were constructed, filter only those paths which
     * all their tasks are in the list of tasks in the critical path
     *
     * @param tasksInCriticalPath
     * @return
     */
    private List<List<Task>> buildAllPossibleCriticalPaths(
            List<Task> tasksInCriticalPath) {

        List<List<Task>> result = new ArrayList<List<Task>>();
        List<List<Task>> allPaths = new ArrayList<List<Task>>();

        for (Task each : getStartingTasks(tasksInCriticalPath)) {
            buildAllPossiblePaths(each, new ArrayList<Task>(), allPaths);
        }
        for (List<Task> path : allPaths) {
            if (isCriticalPath(path)) {
                result.add(path);
            }
        }
        return result;
    }

    /**
     * Returns the list of starting tasks
     *
     * A task is a starting task if a) has no incoming dependencies b) the only
     * incoming dependencies that it has are to other tasks which are no tasks in
     * the critical path
     *
     * @param tasks
     * @return
     */
    private List<Task> getStartingTasks(List<Task> tasks) {
        List<Task> result = new ArrayList<Task>();
        for (Task each : tasks) {
            List<Task> origins = getOriginsFrom(each);
            if (onlyTasksInCriticalPath(origins).isEmpty()) {
                result.add(each);
            }
        }
        return result;
    }

    private List<Task> getOriginsFrom(Task task) {
        List<Task> result = new ArrayList<Task>(), tasks;

        tasks = getOriginsFrom(task.getDependenciesWithThisDestination());
        if (!tasks.isEmpty()) {
            result.addAll(tasks);
        } else {
            tasks = getOriginsFrom(task.getParent()
                    .getDependenciesWithThisDestination());
            if (!tasks.isEmpty()) {
                result.addAll(tasks);
            }
        }
        return result;
    }

    private List<Task> getOriginsFrom(Set<Dependency> dependencies) {
        List<Task> result = new ArrayList<Task>();
        for (Dependency each : dependencies) {
            TaskElement taskElement = each.getOrigin();
            if (taskElement instanceof TaskGroup) {
                final TaskGroup taskGroup = (TaskGroup) taskElement;
                List<TaskElement> children = onlyTasksInCriticalPath(taskGroup
                        .getChildren());
                result.addAll(toTaskIfNecessary(children));
            }
            if (taskElement instanceof Task) {
                result.add((Task) taskElement);
            }
        }
        return result;
    }

    private List<TaskElement> onlyTasksInCriticalPath(
            List<? extends TaskElement> tasks) {
        return onlyTasksInGroup(tasks, tasksInCriticalPath);
    }

    private List<TaskElement> onlyTasksInGroup(
            List<? extends TaskElement> tasks, List<? extends TaskElement> group) {
        List<TaskElement> result = new ArrayList<TaskElement>();
        for (TaskElement each : tasks) {
            if (inTaskList(each, group)) {
                result.add(each);
            }
        }
        return result;
    }

    private boolean inTaskList(TaskElement task, List<? extends TaskElement> tasks) {
        for (TaskElement each : tasks) {
            if (isSameTask(task, each)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameTask(TaskElement task1, TaskElement task2) {
        return task1.getOrderElement().getCode()
                .equals(task2.getOrderElement().getCode());
    }

    private boolean isCriticalPath(List<Task> path) {
        for (Task each : path) {
            if (!inTaskList(each, tasksInCriticalPath)) {
                return false;
            }
        }
        return true;
    }

    private void buildAllPossiblePaths(Task task, List<Task> path,
            List<List<Task>> allPaths) {
        List<Task> destinations = getDestinationsFrom(task);
        if (destinations.size() == 0) {
            path.add(task);
            allPaths.add(path);
            return;
        }
        for (Task each : destinations) {
            List<Task> oldPath = copyPath(path);
            path.add(task);
            buildAllPossiblePaths((Task) each, path, allPaths);
            path = oldPath;
        }
    }

    private List<Task> copyPath(List<Task> path) {
        List<Task> result = new ArrayList<Task>();
        for (Task each : path) {
            result.add(each);
        }
        return result;
    }

    private List<Task> getDestinationsFrom(Task task) {
        List<Task> result = new ArrayList<Task>(), tasks;

        tasks = getDestinationsFrom(task.getDependenciesWithThisOrigin());
        if (!tasks.isEmpty()) {
            result.addAll(tasks);
        } else {
            tasks = getDestinationsFrom(task.getParent()
                    .getDependenciesWithThisOrigin());
            if (!tasks.isEmpty()) {
                result.addAll(tasks);
            }
        }
        return result;
    }

    private List<Task> getDestinationsFrom(Set<Dependency> dependencies) {
        List<Task> result = new ArrayList<Task>();
        for (Dependency each : dependencies) {
            TaskElement taskElement = each.getDestination();
            if (taskElement instanceof TaskGroup) {
                final TaskGroup taskGroup = (TaskGroup) taskElement;
                List<TaskElement> tasks = onlyTasksWithIncomingDependenciesToOtherTasksNotInThisGroup(taskGroup
                        .getChildren());
                result.addAll(toTaskIfNecessary(tasks));
            }
            if (taskElement instanceof Task) {
                result.add((Task) taskElement);
            }
        }
        return result;
    }

    private List<TaskElement> onlyTasksWithIncomingDependenciesToOtherTasksNotInThisGroup(
            List<TaskElement> tasks) {
        List<TaskElement> result = new ArrayList<TaskElement>();
        for (TaskElement each : tasks) {
            Set<Dependency> dependencies = each
                    .getDependenciesWithThisDestination();
            if (dependencies.isEmpty()) {
                result.add(each);
                continue;
            }
            for (Dependency dependency : dependencies) {
                TaskElement origin = dependency.getOrigin();
                if (origin instanceof Task && !inTaskList(origin, tasks)) {
                    result.add(origin);
                }
            }
        }
        return result;
    }

    private Collection<? extends Task> toTaskIfNecessary(
            List<TaskElement> taskElements) {
        List<Task> result = new ArrayList<Task>();
        for (TaskElement each : taskElements) {
            if (each instanceof TaskGroup) {
                final TaskGroup taskGroup = (TaskGroup) each;
                result.addAll(toTaskIfNecessary(taskGroup.getChildren()));
            }
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
