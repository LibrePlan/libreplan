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

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.Task;
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

    private List<MonteCarloTask> criticalPath = new ArrayList<MonteCarloTask>();

    private Map<MonteCarloTask, Set<EstimationRange>> estimationRangesForTasks = new HashMap<MonteCarloTask, Set<EstimationRange>>();

    @Override
    @Transactional(readOnly=true)
    public void setCriticalPath(Order order, List<Task> criticalPath) {
        this.criticalPath.clear();
        for (Task each: sortByStartDate(criticalPath)) {
            this.criticalPath.add(MonteCarloTask.create(each));
        }
    }

    private List<Task> sortByStartDate(List<Task> tasks) {
        Collections.sort(tasks, Task.getByStartDateComparator());
        return tasks;
    }

    @Override
    public List<MonteCarloTask> getCriticalPathTasks() {
        return criticalPath;
    }

    @Override
    public Map<LocalDate, BigDecimal> calculateMonteCarlo(int iterations) {
        Map<LocalDate, BigDecimal> monteCarloValues = new HashMap<LocalDate, BigDecimal>();
        List<MonteCarloTask> tasks = copy(criticalPath);
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
        if (criticalPath.isEmpty()) {
            return "";
        }
        return criticalPath.get(0).getOrderName();
    }

    private List<MonteCarloTask> copy(List<MonteCarloTask> tasks) {
        List<MonteCarloTask> result = new ArrayList<MonteCarloTask>();
        for (MonteCarloTask each: tasks) {
            result.add(MonteCarloTask.copy(each));
        }
        return result;
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
