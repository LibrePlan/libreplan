/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import org.apache.commons.lang.Validate;
import org.hibernate.Hibernate;
import org.joda.time.LocalDate;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MonteCarloModel implements IMonteCarloModel {

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    private String CRITICAL_PATH = _("Critical path");

    private String DEFAULT_CRITICAL_PATH = CRITICAL_PATH + " 1";

    private Map<MonteCarloTask, Set<EstimationRange>> estimationRangesForTasks = new HashMap<MonteCarloTask, Set<EstimationRange>>();

    private Map<String, List<MonteCarloTask>> criticalPaths = new HashMap<String, List<MonteCarloTask>>();

    private String orderName = "";

    private List<TaskElement> orderTasks = new ArrayList<TaskElement>();

    @Override
    @Transactional(readOnly = true)
    public void setCriticalPath(Order order, List<TaskElement> tasksInCriticalPath) {
        useSchedulingDataForCurrentScenario(order);
        orderTasks.addAll(order.getAllChildrenAssociatedTaskElements());
        initializeTasks(orderTasks);

        initializeOrderName(tasksInCriticalPath);

        int i = 1;
        criticalPaths.clear();
        List<List<Task>> allCriticalPaths = buildAllPossibleCriticalPaths(sortByStartDate(onlyTasks((tasksInCriticalPath))));
        for (List<Task> path : allCriticalPaths) {
            criticalPaths.put(CRITICAL_PATH + " " + i++, toMonteCarloTaskList(path));
        }
    }

    private void useSchedulingDataForCurrentScenario(Order order) {
        orderDAO.reattach(order);
        order.useSchedulingDataFor(scenarioManager.getCurrent());
    }

    private void initializeTasks(List<TaskElement> taskElements) {
        for (TaskElement each: taskElements) {
            initializeTaskElement(each);
        }
    }

    private void initializeTaskElement(TaskElement taskElement) {
        if (taskElement == null) {
            return;
        }
        Hibernate.initialize(taskElement);
        initializeTaskElement(taskElement.getParent());
        initializeDependencies(taskElement);
    }

    private void initializeDependencies(TaskElement taskElement) {
        for (Dependency each: taskElement.getDependenciesWithThisDestination()) {
            Hibernate.initialize(each.getOrigin());
            Hibernate.initialize(each.getDestination());
        }
    }

    private void initializeOrderName(List<TaskElement> tasksInCriticalPath) {
        orderName = tasksInCriticalPath.isEmpty() ? "" : tasksInCriticalPath
                .get(0).getOrderElement().getOrder().getName();
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

    private List<Task> sortByStartDate(List<Task> tasks) {
        Collections.sort(tasks, Task.getByStartDateComparator());
        return tasks;
    }

    private List<Task> onlyTasks(List<TaskElement> tasks) {
        List<Task> result = new ArrayList<Task>();
        for (TaskElement each: tasks) {
            if (each instanceof Task) {
                result.add((Task) each);
            }
        }
        return result;
    }
    private List<List<Task>> buildAllPossibleCriticalPaths(
            List<Task> tasksInCriticalPath) {

        List<List<Task>> result = new ArrayList<List<Task>>();

        List<List<Task>> allPaths = new ArrayList<List<Task>>();
        for (Task each: tasksWithoutIncomingDependencies(tasksInCriticalPath)) {
            buildAllPossiblePaths(each, new ArrayList<Task>(), allPaths);
        }
        for (List<Task> path: allPaths) {
            if (isCriticalPath(path, tasksInCriticalPath)) {
                result.add(path);
            }
        }
        return result;
    }

    private List<Task> getDestinationsFrom(Task _task) {
        List<Task> result = new ArrayList<Task>(), tasks;

        Task task = retrieveTaskFromModel(_task);
        tasks = getDestinationsFrom(task.getDependenciesWithThisOrigin());
        if (!tasks.isEmpty()) {
            result.addAll(tasks);
        } else {
            tasks = getDestinationsFrom(task.getParent().getDependenciesWithThisOrigin());
            if (!tasks.isEmpty()) {
                result.addAll(tasks);
            }
        }
        return result;
    }

    private List<Task> getDestinationsFrom(Set<Dependency> dependencies) {
        List<Task> result = new ArrayList<Task>();
        for (Dependency each: dependencies) {
            TaskElement taskElement = each.getDestination();
            if (taskElement instanceof TaskGroup) {
                final TaskGroup taskGroup = (TaskGroup) taskElement;
                result.addAll(toTaskIfNecessary(taskGroup.getChildren()));
            }
            if (taskElement instanceof Task) {
                result.add((Task) taskElement);
            }
        }
        return result;
    }

    private Collection<? extends Task> toTaskIfNecessary(
            List<TaskElement> taskElements) {
       List<Task> result = new ArrayList<Task>();
       for (TaskElement each: taskElements) {
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

    private List<Task> tasksWithoutIncomingDependencies(List<Task> tasks) {
        List<Task> result = new ArrayList<Task>();
        for (Task each: tasks) {
            if (getIncomingDependencies(each).isEmpty()) {
                result.add(each);
            }
        }
        return result;
    }

    private List<Dependency> getIncomingDependencies(Task _task) {
        Task task = retrieveTaskFromModel(_task);
        if (task == null) {
            throw new RuntimeException(_("Couldn't find %s in model", _task.getName()));
        }
        List<Dependency> result = new ArrayList<Dependency>();
        result.addAll(task.getDependenciesWithThisDestination());
        result.addAll(task.getParent().getDependenciesWithThisDestination());
        return result;
    }

    private Task retrieveTaskFromModel(Task task) {
        for (TaskElement each: orderTasks) {
            String code = each.getOrderElement().getCode();
            if (code.equals(task.getOrderElement().getCode())) {
                return (Task) each;
            }
        }
        return null;
    }

    private boolean isCriticalPath(List<Task> path,
            List<Task> tasksInCriticalPath) {
       final List<Long> tasksInCriticalPathIds = getIds(tasksInCriticalPath);
       for (Task each: path) {
           if (!tasksInCriticalPathIds.contains(each.getId())) {
               return false;
           }
       }
       return true;
    }

    private List<Long> getIds(List<Task> tasks) {
        List<Long> result = new ArrayList<Long>();
        for (Task each: tasks) {
            result.add(each.getId());
        }
        return result;
    }

    private void buildAllPossiblePaths(Task task, List<Task> path, List<List<Task>> allPaths) {
        List<Task> destinations = getDestinationsFrom(task);
        if (destinations.size() == 0) {
            path.add(task);
            allPaths.add(path);
            return;
        }
        for (Task each: destinations) {
            List<Task> oldPath = copyPath(path);
            path.add(task);
            buildAllPossiblePaths((Task) each, path, allPaths);
            path = oldPath;
        }
    }

    private List<Task> copyPath(List<Task> path) {
        List<Task> result = new ArrayList<Task>();
        for (Task each: path) {
            result.add(each);
        }
        return result;
    }

    private List<MonteCarloTask> toMonteCarloTaskList(List<Task> path) {
        List<MonteCarloTask> result = new ArrayList<MonteCarloTask>();
        for (Task each: path) {
            result.add(MonteCarloTask.create(each));
        }
        return result;
    }

    @Override
    public Map<LocalDate, BigDecimal> calculateMonteCarlo(List<MonteCarloTask> tasks, int iterations) {
        Map<LocalDate, BigDecimal> monteCarloValues = new HashMap<LocalDate, BigDecimal>();
        adjustDurationDays(tasks);
        initializeEstimationRanges(tasks);

        Random randomGenerator = new Random((new Date()).getTime());
        // Calculate number of times a date is repeated
        for (int i = 0; i < iterations; i++) {
            LocalDate endDate = calculateEndDateFor(tasks, randomGenerator);
            BigDecimal times = monteCarloValues.get(endDate);
            times = times != null ? times.add(BigDecimal.ONE): BigDecimal.ONE;
            monteCarloValues.put(endDate, times);
        }

        // Convert number of times to probability
        for (LocalDate key: monteCarloValues.keySet()) {
            BigDecimal times = monteCarloValues.get(key);
            BigDecimal probability = times.divide(BigDecimal
                    .valueOf(iterations), 8, RoundingMode.HALF_UP);
            monteCarloValues.put(key, probability);
        }

        return monteCarloValues;
    }

    public String getOrderName() {
        return orderName;
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

    private LocalDate calculateEndDateFor(List<MonteCarloTask> tasks, Random randomGenerator) {
        Validate.notEmpty(tasks);
        BigDecimal durationDays = BigDecimal.ZERO;
        MonteCarloTask first = tasks.get(0);
        LocalDate result = first.getTask().getStartAsLocalDate();

        for (MonteCarloTask each: tasks) {
            BigDecimal randomNumber = generate(randomGenerator);
            durationDays = getDuration(each, randomNumber);
            result = result.plusDays(durationDays.intValue());
        }
        return result;
    }

    private BigDecimal generate(Random randomGenerator) {
        return BigDecimal.valueOf(randomGenerator.nextDouble());
    }

    private BigDecimal getDuration(MonteCarloTask each, BigDecimal random) {
        ESTIMATION_TYPE type = getEstimationType(each, random);
        Validate.notNull(type);
        switch(type) {
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

    private void initializeEstimationRanges(List<MonteCarloTask> tasks) {
        estimationRangesForTasks.clear();
        for (MonteCarloTask each: tasks) {
            createEstimationRangesFor(each);
        }
    }

    private void createEstimationRangesFor(MonteCarloTask task) {
        Set<EstimationRange> estimationRanges = new HashSet<EstimationRange>();
        estimationRanges.add(pessimisticRangeFor(task));
        estimationRanges.add(normalRangeFor(task));
        estimationRanges.add(optimisticRangeFor(task));
        estimationRangesForTasks.put(task, estimationRanges);
    }

    private EstimationRange optimisticRangeFor(MonteCarloTask task) {
        return new EstimationRange(
                task.getOptimisticDurationPercentageLowerLimit(),
                task.getOptimisticDurationPercentageUpperLimit(),
                ESTIMATION_TYPE.OPTIMISTIC);
    }

    private EstimationRange normalRangeFor(MonteCarloTask task) {
        return new EstimationRange(
                task.getNormalDurationPercentageLowerLimit(),
                task.getNormalDurationPercentageUpperLimit(),
                ESTIMATION_TYPE.NORMAL);
    }

    private EstimationRange pessimisticRangeFor(MonteCarloTask task) {
        return new EstimationRange(
                task.getPessimisticDurationPercentageLowerLimit(),
                task.getPessimisticDurationPercentageUpperLimit(),
                ESTIMATION_TYPE.PESSIMISTIC);
    }

    public ESTIMATION_TYPE getEstimationType(MonteCarloTask task, BigDecimal random) {
        Set<EstimationRange> estimationRanges = estimationRangesForTasks.get(task);
        for (EstimationRange each: estimationRanges) {
            if (each.contains(random)) {
                return each.getEstimationType();
            }
        }
        return null;
    }

    private class EstimationRange {
        BigDecimal min;
        BigDecimal max;
        ESTIMATION_TYPE estimationType;

        public EstimationRange(BigDecimal min, BigDecimal max,
                ESTIMATION_TYPE estimationType) {
            this.min = min;
            this.max = max;
            this.estimationType = estimationType;
        }

        public boolean contains(BigDecimal value) {
            return (value.compareTo(min) >= 0 && value.compareTo(max) <= 0);
        }

        public ESTIMATION_TYPE getEstimationType() {
            return estimationType;
        }
    }

    enum ESTIMATION_TYPE {
        PESSIMISTIC,
        NORMAL,
        OPTIMISTIC;
    }

}
