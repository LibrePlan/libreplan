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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
        int i = 0;
        int maxDaysElapsed = 0;
        for (Integer each : hoursForEachAllocation(hoursToAllocate)) {
            ResourcesPerDayModification currentAllocation = allocations.get(i);
            int daysElapsedForCurrent = untilAllocating(currentAllocation, each);
            maxDaysElapsed = Math.max(maxDaysElapsed, daysElapsedForCurrent);
            i++;
        }
        setAssignmentsForEachAllocation();
        LocalDate start = LocalDate.fromDateFields(task.getStartDate());
        return start.plusDays(maxDaysElapsed);
    }

    private int untilAllocating(
            ResourcesPerDayModification resourcesPerDayModification,
            Integer hoursToAllocate) {
        int hoursRemaining = hoursToAllocate;
        LocalDate start = new LocalDate(task.getStartDate().getTime());
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

    private int assignForDay(
            ResourcesPerDayModification resourcesPerDayModification,
            LocalDate day, int remaining) {
        List<DayAssignment> newAssignments = createAssignmentsAtDay(
                resourcesPerDayModification, day, remaining);
        resultAssignments.get(resourcesPerDayModification).addAll(
                newAssignments);
        return DayAssignment.sum(newAssignments);
    }

    private List<Integer> hoursForEachAllocation(int toBeAssigned) {
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
        for (ResourcesPerDayModification r : allocations) {
            result = result.add(r.getGoal().getAmount());
        }
        return result;
    }

}
