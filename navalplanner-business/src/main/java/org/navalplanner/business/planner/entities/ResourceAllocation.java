package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.Resource;

/**
 * Resources are allocated to planner tasks.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class ResourceAllocation<T extends DayAssignment> extends
        BaseEntity {

    public static class ResourceAllocationWithDesiredResourcesPerDay {

        private final ResourceAllocation<?> resourceAllocation;

        private final ResourcesPerDay resourcesPerDay;

        public ResourceAllocationWithDesiredResourcesPerDay(
                ResourceAllocation<?> resourceAllocation,
                ResourcesPerDay resourcesPerDay) {
            this.resourceAllocation = resourceAllocation;
            this.resourcesPerDay = resourcesPerDay;
        }

        public ResourceAllocation<?> getResourceAllocation() {
            return resourceAllocation;
        }

        public ResourcesPerDay getResourcesPerDay() {
            return resourcesPerDay;
        }
    }

    public static AllocationsCurried allocating(
            List<ResourceAllocationWithDesiredResourcesPerDay> resourceAllocations) {
        return new AllocationsCurried(resourceAllocations);
    }

    public static class AllocationsCurried {

        private final List<ResourceAllocationWithDesiredResourcesPerDay> resourceAllocations;

        private final Task task;

        public AllocationsCurried(
                List<ResourceAllocationWithDesiredResourcesPerDay> resourceAllocations) {
            Validate.notNull(resourceAllocations);
            Validate.notEmpty(resourceAllocations);
            Validate.noNullElements(resourceAllocations);
            checkNoOneHasNullTask(resourceAllocations);
            checkAllHaveSameTask(resourceAllocations);
            this.resourceAllocations = resourceAllocations;
            this.task = resourceAllocations.get(0).getResourceAllocation()
                    .getTask();
        }

        private static void checkNoOneHasNullTask(
                List<ResourceAllocationWithDesiredResourcesPerDay> allocations) {
            for (ResourceAllocationWithDesiredResourcesPerDay resourceAllocationWithDesiredResourcesPerDay : allocations) {
                if (resourceAllocationWithDesiredResourcesPerDay
                        .getResourceAllocation().getTask() == null)
                    throw new IllegalArgumentException(
                            "all allocations must have task");
            }
        }

        private static void checkAllHaveSameTask(
                List<ResourceAllocationWithDesiredResourcesPerDay> resourceAllocations) {
            Task task = null;
            for (ResourceAllocationWithDesiredResourcesPerDay r : resourceAllocations) {
                if (task == null) {
                    task = r.getResourceAllocation().getTask();
                }
                if (!task.equals(r.getResourceAllocation().getTask())) {
                    throw new IllegalArgumentException(
                            "all allocations must belong to the same task");
                }
            }
        }

        public AllocationsAndResourcesCurried withResources(
                List<Resource> resources) {
            return new AllocationsAndResourcesCurried(task, resources,
                    resourceAllocations);
        }
    }

    public static class AllocationsAndResourcesCurried {
        private List<Resource> resources;

        private List<ResourceAllocationWithDesiredResourcesPerDay> allocations;

        private final Task task;

        private Map<ResourceAllocationWithDesiredResourcesPerDay, List<DayAssignment>> resultAssignments = new HashMap<ResourceAllocationWithDesiredResourcesPerDay, List<DayAssignment>>();

        public AllocationsAndResourcesCurried(Task task,
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
                allocation.setResourcesPerDay(resourcesPerDay);
                allocation.resetGenericAssignmentsTo(entry.getValue());
            }
        }

        private int assignForDay(LocalDate day, int toBeAssigned) {
            int i = 0;
            int total = 0;
            List<Integer> maxPerAllocations = calculateLimits(toBeAssigned);
            for (ResourceAllocationWithDesiredResourcesPerDay withResourcesPerDay : allocations) {
                ResourceAllocation<?> resourceAllocation = withResourcesPerDay
                        .getResourceAllocation();
                ResourcesPerDay resourcesPerDay = withResourcesPerDay
                        .getResourcesPerDay();
                List<DayAssignment> assigments = resourceAllocation
                        .createAssignmentsAtDay(resources, day,
                                resourcesPerDay, maxPerAllocations.get(i));
                resultAssignments.get(withResourcesPerDay).addAll(assigments);
                total += DayAssignment.sum(assigments);
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
            for (ResourceAllocationWithDesiredResourcesPerDay r : allocations) {
                result = result.add(r.getResourcesPerDay().getAmount());
            }
            return result;
        }

        public void allocateOnTaskLength() {
            for (ResourceAllocationWithDesiredResourcesPerDay allocation : allocations) {
                doAllocationForFixedTask(allocation.getResourceAllocation(),
                        allocation.getResourcesPerDay());
            }
        }

        private void doAllocationForFixedTask(ResourceAllocation<?> allocation,
                ResourcesPerDay resourcesPerDay) {
            if (allocation instanceof GenericResourceAllocation) {
                doAllocation((GenericResourceAllocation) allocation,
                        resourcesPerDay);
            } else {
                SpecificResourceAllocation specific = (SpecificResourceAllocation) allocation;
                doAllocation(specific, resourcesPerDay);
            }
        }

        private void doAllocation(SpecificResourceAllocation specific,
                ResourcesPerDay resourcesPerDay) {
            specific.allocate(resourcesPerDay);
        }

        private void doAllocation(GenericResourceAllocation generic,
                ResourcesPerDay resourcesPerDay) {
            generic.forResources(resources).allocate(
                    resourcesPerDay);
        }
    }

    @NotNull
    private Task task;

    private AssignmentFunction assignmentFunction;

    @NotNull
    private ResourcesPerDay resourcesPerDay;

    /**
     * Constructor for hibernate. Do not use!
     */
    public ResourceAllocation() {

    }

    protected abstract List<DayAssignment> createAssignmentsAtDay(
            List<Resource> resources, LocalDate day,
            ResourcesPerDay resourcesPerDay, int limit);

    protected void setResourcesPerDay(ResourcesPerDay resourcesPerDay) {
        Validate.notNull(resourcesPerDay);
        this.resourcesPerDay = resourcesPerDay;
    }

    public ResourceAllocation(Task task) {
        this(task, null);
    }

    public ResourceAllocation(Task task, AssignmentFunction assignmentFunction) {
        Validate.notNull(task);
        this.task = task;
        this.assignmentFunction = assignmentFunction;
    }

    protected ResourceAllocation(ResourcesPerDay resourcesPerDay, Task task) {
        this(task);
        Validate.notNull(resourcesPerDay);
        this.resourcesPerDay = resourcesPerDay;
    }

    public Task getTask() {
        return task;
    }

    public ResourceAllocationWithDesiredResourcesPerDay withDesiredResourcesPerDay(
            ResourcesPerDay resourcesPerDay) {
        return new ResourceAllocationWithDesiredResourcesPerDay(this,
                resourcesPerDay);
    }

    protected abstract class AssignmentsAllocation implements IAllocatable {

        @Override
        public final void allocate(ResourcesPerDay resourcesPerDay) {
            Task task = getTask();
            LocalDate startInclusive = new LocalDate(task.getStartDate());
            List<T> assignmentsCreated = new ArrayList<T>();
            for (int i = 0; i < getDaysElapsedAt(task); i++) {
                LocalDate day = startInclusive.plusDays(i);
                int totalForDay = calculateTotalToDistribute(day,
                        resourcesPerDay);
                assignmentsCreated.addAll(distributeForDay(day, totalForDay));
            }
            setResourcesPerDay(resourcesPerDay);
            resetAssignmentsTo(assignmentsCreated);
        }

        private int getDaysElapsedAt(Task task) {
            LocalDate endExclusive = new LocalDate(task.getEndDate());
            Days daysBetween = Days.daysBetween(new LocalDate(task
                    .getStartDate()), endExclusive);
            return daysBetween.getDays();
        }

        protected abstract List<T> distributeForDay(LocalDate day,
                int totalHours);

    }

    protected abstract void resetAssignmentsTo(List<T> assignments);

    final int calculateTotalToDistribute(LocalDate day,
            ResourcesPerDay resourcesPerDay) {
        Integer workableHours = getWorkableHoursAt(day);
        return resourcesPerDay.asHoursGivenResourceWorkingDayOf(workableHours);
    }

    final Integer getWorkableHoursAt(LocalDate day) {
        if (getTaskCalendar() == null) {
            return SameWorkHoursEveryDay.getDefaultWorkingDay()
                    .getWorkableHours(day);
        } else {
            return getTaskCalendar().getWorkableHours(day);
        }
    }

    protected final BaseCalendar getTaskCalendar() {
        return getTask().getCalendar();
    }

    private void resetGenericAssignmentsTo(List<DayAssignment> assignments) {
        resetAssignmentsTo(cast(assignments));
    }

    private List<T> cast(List<DayAssignment> value) {
        List<T> result = new ArrayList<T>();
        for (DayAssignment dayAssignment : value) {
            result.add(getDayAssignmentType().cast(dayAssignment));
        }
        return result;
    }

    protected abstract Class<T> getDayAssignmentType();

    public AssignmentFunction getAssignmentFunction() {
        return assignmentFunction;
    }

    public int getAssignedHours() {
        int total = 0;
        for (DayAssignment dayAssignment : getAssignments()) {
            total += dayAssignment.getHours();
        }
        return total;
    }

    public abstract List<? extends DayAssignment> getAssignments();

    public ResourcesPerDay getResourcesPerDay() {
        return resourcesPerDay;
    }

}
