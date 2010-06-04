/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.planner.entities.allocationalgorithms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.Task;

public abstract class AllocatorForSpecifiedResourcesPerDayAndHours {

    private final Task task;

    private List<ResourcesPerDayModification> allocations;

    private Map<ResourcesPerDayModification, List<DayAssignment>> resultAssignments = new HashMap<ResourcesPerDayModification, List<DayAssignment>>();

    public AllocatorForSpecifiedResourcesPerDayAndHours(Task task,
            List<ResourcesPerDayModification> allocations) {
        this.task = task;
        this.allocations = allocations;
        initializeResultsMap();
    }

    private void initializeResultsMap() {
        for (ResourcesPerDayModification r : allocations) {
            resultAssignments.put(r, new ArrayList<DayAssignment>());
        }
    }

    public LocalDate untilAllocating(int hoursToAllocate) {
        LocalDate taskStart = LocalDate.fromDateFields(task.getStartDate());
        LocalDate start = (task.getFirstDayNotConsolidated().compareTo(
                taskStart) >= 0) ? task.getFirstDayNotConsolidated()
                : taskStart;
        int i = 0;
        int maxDaysElapsed = 0;
        for (HoursPerAllocation each : hoursPerAllocation(start,
                hoursToAllocate)) {
            int daysElapsedForCurrent = untilAllocating(start, each.allocation,
                    each.hours);
            maxDaysElapsed = Math.max(maxDaysElapsed, daysElapsedForCurrent);
            i++;
        }
        setAssignmentsForEachAllocation();
        return start.plusDays(maxDaysElapsed);
    }

    private List<HoursPerAllocation> hoursPerAllocation(LocalDate start,
            int toBeAssigned) {
        return new HoursPerAllocationCalculator(allocations)
                .calculateHoursPerAllocation(start, toBeAssigned);
    }

    private int untilAllocating(LocalDate start,
            ResourcesPerDayModification resourcesPerDayModification,
            Integer hoursToAllocate) {
        int hoursRemaining = hoursToAllocate;
        int day = 0;
        while (hoursRemaining > 0) {
            LocalDate current = start.plusDays(day);
            int taken = assignForDay(resourcesPerDayModification, current,
                    hoursRemaining);
            hoursRemaining = hoursRemaining - taken;
            day++;
        }
        return day;
    }

    private void setAssignmentsForEachAllocation() {
        for (Entry<ResourcesPerDayModification, List<DayAssignment>> entry : resultAssignments
                .entrySet()) {
            ResourceAllocation<?> allocation = entry.getKey()
                    .getBeingModified();
            ResourcesPerDay resourcesPerDay = entry.getKey()
                    .getGoal();
            List<DayAssignment> value = entry.getValue();
            setNewDataForAllocation(allocation, resourcesPerDay, value);
        }
    }

    protected abstract void setNewDataForAllocation(
            ResourceAllocation<?> allocation, ResourcesPerDay resourcesPerDay,
            List<DayAssignment> dayAssignments);

    protected abstract List<DayAssignment> createAssignmentsAtDay(
            ResourcesPerDayModification allocation, LocalDate day, Integer limit);

    protected abstract boolean thereAreAvailableHoursFrom(LocalDate start,
            ResourcesPerDayModification resourcesPerDayModification,
            int hoursToAllocate);

    protected abstract void markUnsatisfied(ResourceAllocation<?> beingModified);

    private int assignForDay(
            ResourcesPerDayModification resourcesPerDayModification,
            LocalDate day, int remaining) {
        List<DayAssignment> newAssignments = createAssignmentsAtDay(
                resourcesPerDayModification, day, remaining);
        resultAssignments.get(resourcesPerDayModification).addAll(
                newAssignments);
        return DayAssignment.sum(newAssignments);
    }

    private static class HoursPerAllocation {
        final int hours;

        final ResourcesPerDayModification allocation;

        private HoursPerAllocation(int hours,
                ResourcesPerDayModification allocation) {
            this.hours = hours;
            this.allocation = allocation;
        }

        public static List<HoursPerAllocation> wrap(
                List<ResourcesPerDayModification> allocations,
                List<Integer> hours) {
            Validate.isTrue(hours.size() == allocations.size());
            int i = 0;
            List<HoursPerAllocation> result = new ArrayList<HoursPerAllocation>();
            for(i = 0; i < allocations.size(); i++){
                result.add(new HoursPerAllocation(hours.get(i), allocations
                        .get(i)));
            }
            return result;
        }
    }

    private class HoursPerAllocationCalculator {
        private List<ResourcesPerDayModification> allocations;

        private HoursPerAllocationCalculator(
                List<ResourcesPerDayModification> allocations) {
            this.allocations = new ArrayList<ResourcesPerDayModification>(
                    allocations);
        }

        public List<HoursPerAllocation> calculateHoursPerAllocation(
                LocalDate start, int toAssign) {
            do {
                List<Integer> hours = calculateHours(toAssign);
                List<HoursPerAllocation> result = HoursPerAllocation.wrap(
                        allocations, hours);
                List<ResourcesPerDayModification> unsatisfied = getUnsatisfied(
                        start, result);
                if (unsatisfied.isEmpty()) {
                    return result;
                }
                for (ResourcesPerDayModification each : unsatisfied) {
                    markUnsatisfied(each.getBeingModified());
                }
                allocations.removeAll(unsatisfied);
            } while (!allocations.isEmpty());
            return Collections.emptyList();
        }

        private List<ResourcesPerDayModification> getUnsatisfied(
                LocalDate start, List<HoursPerAllocation> hoursPerAllocations) {
            List<ResourcesPerDayModification> cannotSatisfy = new ArrayList<ResourcesPerDayModification>();
            for (HoursPerAllocation each : hoursPerAllocations) {
                if (!thereAreAvailableHoursFrom(start, each.allocation,
                        each.hours)) {
                    cannotSatisfy.add(each.allocation);
                }
            }
            return cannotSatisfy;
        }

        private List<Integer> calculateHours(int toAssign) {
            BigDecimal[] limits = new BigDecimal[allocations.size()];
            BigDecimal sumAll = sumAll();
            for (int i = 0; i < limits.length; i++) {
                BigDecimal amount = allocations.get(i).getGoal().getAmount();
                limits[i] = amount.divide(sumAll, RoundingMode.DOWN).multiply(
                        new BigDecimal(toAssign));
            }
            final int remainder = toAssign - sumIntegerParts(limits);
            return distributeRemainder(limits, remainder);
        }

        private List<Integer> distributeRemainder(BigDecimal[] decimals,
                final int remainder) {
            for (int i = 0; i < remainder; i++) {
                int position = positionOfBiggestDecimalPart(decimals);
                decimals[position] = new BigDecimal(decimals[position]
                        .intValue() + 1);
            }
            return asIntegers(decimals);
        }

        private List<Integer> asIntegers(BigDecimal[] decimals) {
            Integer[] result = new Integer[decimals.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = decimals[i].intValue();
            }
            return Arrays.asList(result);
        }

        private int positionOfBiggestDecimalPart(BigDecimal[] decimals) {
            int result = 0;
            BigDecimal currentBiggestDecimalPart = new BigDecimal(0);
            for (int i = 0; i < decimals.length; i++) {
                BigDecimal fractionalPart = decimals[i]
                        .subtract(new BigDecimal(decimals[i].intValue()));
                if (currentBiggestDecimalPart.compareTo(fractionalPart) < 0) {
                    currentBiggestDecimalPart = fractionalPart;
                    result = i;
                }
            }
            return result;
        }

        private int sumIntegerParts(BigDecimal[] decimals) {
            int sum = 0;
            for (BigDecimal decimal : decimals) {
                sum += decimal.intValue();
            }
            return sum;
        }

        private BigDecimal sumAll() {
            BigDecimal result = new BigDecimal(0);
            for (ResourcesPerDayModification r : allocations) {
                result = result.add(r.getGoal().getAmount());
            }
            return result;
        }

    }

}
