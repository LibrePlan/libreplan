package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.IWorkHours;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.planner.entities.allocationalgorithms.AllocatorForSpecifiedResourcesPerDayAndHours;
import org.navalplanner.business.planner.entities.allocationalgorithms.AllocatorForTaskDurationAndSpecifiedResourcesPerDay;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourceAllocationWithDesiredResourcesPerDay;
import org.navalplanner.business.resources.entities.Resource;

/**
 * Resources are allocated to planner tasks.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class ResourceAllocation<T extends DayAssignment> extends
        BaseEntity {

    public static List<ResourceAllocation<?>> sortedByStartDate(
            Collection<? extends ResourceAllocation<?>> allocations) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>(
                allocations);
        Collections.sort(result, byStartDateComparator());
        return result;
    }

    public static Map<Task, List<ResourceAllocation<?>>> byTask(
            Collection<? extends ResourceAllocation<?>> allocations) {
        Map<Task, List<ResourceAllocation<?>>> result = new HashMap<Task, List<ResourceAllocation<?>>>();
        for (ResourceAllocation<?> resourceAllocation : allocations) {
            if (resourceAllocation.getTask() != null) {
                Task task = resourceAllocation.getTask();
                initializeIfNeeded(result, task);
                result.get(task).add(resourceAllocation);
            }
        }
        return result;
    }

    private static void initializeIfNeeded(
            Map<Task, List<ResourceAllocation<?>>> result, Task task) {
        if (!result.containsKey(task)) {
            result.put(task,
                    new ArrayList<ResourceAllocation<?>>());
        }
    }

    private static Comparator<ResourceAllocation<?>> byStartDateComparator() {
        return new Comparator<ResourceAllocation<?>>() {

            @Override
            public int compare(ResourceAllocation<?> o1,
                    ResourceAllocation<?> o2) {
                return o1.getStartDate().compareTo(o2.getStartDate());
            }
        };
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
            checkNoAllocationWithZeroResourcesPerDay(resourceAllocations);
            this.resourceAllocations = resourceAllocations;
            this.task = resourceAllocations.get(0).getResourceAllocation()
                    .getTask();
        }

        private static void checkNoAllocationWithZeroResourcesPerDay(
                List<ResourceAllocationWithDesiredResourcesPerDay> allocations) {
            for (ResourceAllocationWithDesiredResourcesPerDay r : allocations) {
                if (isZero(r.getResourcesPerDay().getAmount())) {
                    throw new IllegalArgumentException(
                            "all resources per day must be no zero");
                }
            }
        }

        private static boolean isZero(BigDecimal amount) {
            return amount.movePointRight(amount.scale()).intValue() == 0;
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
            Validate.noNullElements(resources);
            return new AllocationsAndResourcesCurried(task, resources,
                    resourceAllocations);
        }
    }

    public static class AllocationsAndResourcesCurried {
        private List<Resource> resources;

        private List<ResourceAllocationWithDesiredResourcesPerDay> allocations;

        private final Task task;

        public AllocationsAndResourcesCurried(Task task,
                List<Resource> resources,
                List<ResourceAllocationWithDesiredResourcesPerDay> allocations) {
            this.task = task;
            this.resources = resources;
            this.allocations = allocations;
        }

        public LocalDate untilAllocating(int hoursToAllocate) {
            if (thereIsGenericAllocation()) {
                Validate.notEmpty(resources, "there must exist workers");
            }
            AllocatorForSpecifiedResourcesPerDayAndHours allocator = new AllocatorForSpecifiedResourcesPerDayAndHours(
                    task, resources, allocations) {

                @Override
                protected List<DayAssignment> createAssignmentsAtDay(
                        ResourceAllocation<?> resourceAllocation,
                        List<Resource> resources, LocalDate day,
                        ResourcesPerDay resourcesPerDay, Integer limit) {
                    return resourceAllocation.createAssignmentsAtDay(resources,
                            day, resourcesPerDay, limit);
                }

                @Override
                protected void setNewDataForAllocation(
                        ResourceAllocation<?> allocation,
                        ResourcesPerDay resourcesPerDay,
                        List<DayAssignment> dayAssignments) {
                    allocation.setResourcesPerDay(resourcesPerDay);
                    allocation.resetGenericAssignmentsTo(dayAssignments);
                }
            };
            return allocator.untilAllocating(hoursToAllocate);
        }

        private boolean thereIsGenericAllocation() {
            for (ResourceAllocationWithDesiredResourcesPerDay r : allocations) {
                if (r.getResourceAllocation() instanceof GenericResourceAllocation) {
                    return true;
                }
            }
            return false;
        }

        public void allocateOnTaskLength() {
            AllocatorForTaskDurationAndSpecifiedResourcesPerDay allocator = new AllocatorForTaskDurationAndSpecifiedResourcesPerDay(
                    allocations, resources);
            allocator.allocateOnTaskLength();
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
        Integer workableHours = getWorkHoursPerDay().getWorkableHours(day);
        return resourcesPerDay.asHoursGivenResourceWorkingDayOf(workableHours);
    }

    private IWorkHours getWorkHoursPerDay() {
        return getWorkHoursGivenTaskHours(getTaskWorkHoursLimit());
    }

    private IWorkHours getTaskWorkHoursLimit() {
        return new IWorkHours() {
            @Override
            public Integer getWorkableHours(LocalDate day) {
                if (getTaskCalendar() == null) {
                    return SameWorkHoursEveryDay.getDefaultWorkingDay()
                            .getWorkableHours(day);
                } else {
                    return getTaskCalendar().getWorkableHours(day);
                }
            }
        };
    }

    protected abstract IWorkHours getWorkHoursGivenTaskHours(
            IWorkHours taskWorkHours);

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

    /**
     * @return a list of {@link DayAssignment} ordered by date
     */
    public abstract List<? extends DayAssignment> getAssignments();

    public ResourcesPerDay getResourcesPerDay() {
        return resourcesPerDay;
    }

    public LocalDate getStartDate() {
        List<? extends DayAssignment> assignments = getAssignments();
        if (assignments.isEmpty()) {
            return null;
        }
        return assignments.get(0).getDay();
    }

    public LocalDate getEndDate() {
        List<? extends DayAssignment> assignments = getAssignments();
        if (assignments.isEmpty()) {
            return null;
        }
        return assignments.get(assignments.size() - 1).getDay();
    }

    public boolean isAlreadyFinishedBy(LocalDate date) {
        if (getEndDate() == null) {
            return false;
        }
        return getEndDate().compareTo(date) < 0;
    }

    public int getAssignedHours(Resource resource, LocalDate start,
            LocalDate end) {
        int sum =0;
        for (DayAssignment dayAssignment : getAssignments()) {
            if (dayAssignment.getDay().compareTo(end) >= 0) {
                break;
            }
            if (dayAssignment.includedIn(start, end)
                    && dayAssignment.isAssignedTo(resource)) {
                sum += dayAssignment.getHours();
            }
        }
        return sum;
    }

}
