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

    private List<Resource> resources;

    private List<ResourceAllocationWithDesiredResourcesPerDay> allocations;

    private Map<ResourceAllocationWithDesiredResourcesPerDay, List<DayAssignment>> resultAssignments = new HashMap<ResourceAllocationWithDesiredResourcesPerDay, List<DayAssignment>>();

    public AllocatorForSpecifiedResourcesPerDayAndHours(Task task,
            List<Resource> resources,
            List<ResourceAllocationWithDesiredResourcesPerDay> allocations) {
        this.task = task;
        this.resources = resources;
        this.allocations = allocations;
        initializeResultsMap();
    }

    private void initializeResultsMap() {
        for (ResourceAllocationWithDesiredResourcesPerDay r : allocations) {
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
        for (Entry<ResourceAllocationWithDesiredResourcesPerDay, List<DayAssignment>> entry : resultAssignments
                .entrySet()) {
            ResourceAllocation<?> allocation = entry.getKey()
                    .getResourceAllocation();
            ResourcesPerDay resourcesPerDay = entry.getKey()
                    .getResourcesPerDay();
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
        for (ResourceAllocationWithDesiredResourcesPerDay withResourcesPerDay : allocations) {
            ResourceAllocation<?> resourceAllocation = withResourcesPerDay
                    .getResourceAllocation();
            ResourcesPerDay resourcesPerDay = withResourcesPerDay
                    .getResourcesPerDay();
            List<DayAssignment> assignments = createAssignmentsAtDay(
                    resourceAllocation, resources, day,
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
            BigDecimal amount = allocations.get(i).getResourcesPerDay()
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
        for (ResourceAllocationWithDesiredResourcesPerDay r : allocations) {
            result = result.add(r.getResourcesPerDay().getAmount());
        }
        return result;
    }

}
