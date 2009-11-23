/*
 * This file is part of ###PROJECT_NAME###
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Resource;

public abstract class AllocatorForSpecifiedResourcesPerDayAndHours {

    private final Task task;

    private List<AllocationBeingModified> allocations;

    private Map<AllocationBeingModified, List<DayAssignment>> resultAssignments = new HashMap<AllocationBeingModified, List<DayAssignment>>();

    public AllocatorForSpecifiedResourcesPerDayAndHours(Task task,
            List<AllocationBeingModified> allocations) {
        this.task = task;
        this.allocations = allocations;
        initializeResultsMap();
    }

    private void initializeResultsMap() {
        for (AllocationBeingModified r : allocations) {
            resultAssignments.put(r, new ArrayList<DayAssignment>());
        }
    }

    public LocalDate untilAllocating(int hoursToAllocate) {
        int hoursRemaining = hoursToAllocate;
        LocalDate start = new LocalDate(task.getStartDate().getTime());
        int day = 0;
        while (hoursRemaining > 0) {
            LocalDate current = start.plusDays(day);
            int taken = assignForDay(current, hoursRemaining);
            hoursRemaining = hoursRemaining - taken;
            day++;
        }
        setAssignmentsForEachAllocation();
        return start.plusDays(day);
    }

    private void setAssignmentsForEachAllocation() {
        for (Entry<AllocationBeingModified, List<DayAssignment>> entry : resultAssignments
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
            ResourceAllocation<?> resourceAllocation, List<Resource> resources,
            LocalDate day, ResourcesPerDay resourcesPerDay, Integer limit);

    private int assignForDay(LocalDate day, int toBeAssigned) {
        int i = 0;
        int total = 0;
        List<Integer> maxPerAllocations = calculateLimits(toBeAssigned);
        for (AllocationBeingModified withResourcesPerDay : allocations) {
            ResourceAllocation<?> resourceAllocation = withResourcesPerDay
                    .getBeingModified();
            ResourcesPerDay resourcesPerDay = withResourcesPerDay
                    .getGoal();
            List<DayAssignment> assignments = createAssignmentsAtDay(
                    resourceAllocation, withResourcesPerDay.getResources(),
                    day,
                    resourcesPerDay, maxPerAllocations.get(i));
            resultAssignments.get(withResourcesPerDay).addAll(assignments);
            total += DayAssignment.sum(assignments);
            i++;
        }
        return total;
    }

    private List<Integer> calculateLimits(int toBeAssigned) {
        BigDecimal[] limits = new BigDecimal[allocations.size()];
        BigDecimal sumAll = sumAll();
        for (int i = 0; i < limits.length; i++) {
            BigDecimal amount = allocations.get(i).getGoal()
                    .getAmount();
            limits[i] = amount.divide(sumAll, RoundingMode.DOWN).multiply(
                    new BigDecimal(toBeAssigned));
        }
        final int remainder = toBeAssigned - sumIntegerParts(limits);
        return distributeRemainder(limits, remainder);
    }

    private List<Integer> distributeRemainder(BigDecimal[] decimals,
            final int remainder) {
        for (int i = 0; i < remainder; i++) {
            int position = positionOfBiggestDecimalPart(decimals);
            decimals[position] = new BigDecimal(
                    decimals[position].intValue() + 1);
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
            BigDecimal fractionalPart = decimals[i].subtract(new BigDecimal(
                    decimals[i].intValue()));
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
        for (AllocationBeingModified r : allocations) {
            result = result.add(r.getGoal().getAmount());
        }
        return result;
    }

}
