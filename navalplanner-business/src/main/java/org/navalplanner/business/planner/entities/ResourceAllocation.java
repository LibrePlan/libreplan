package org.navalplanner.business.planner.entities;


import java.util.ArrayList;
import java.util.List;

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
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class ResourceAllocation<T extends DayAssignment> extends
        BaseEntity {

    public static class ResourceAllocationWithDesiredResourcesPerDay {

        private final ResourceAllocation resourceAllocation;

        private final ResourcesPerDay resourcesPerDay;

        public ResourceAllocationWithDesiredResourcesPerDay(
                ResourceAllocation resourceAllocation,
                ResourcesPerDay resourcesPerDay) {
            this.resourceAllocation = resourceAllocation;
            this.resourcesPerDay = resourcesPerDay;
        }

        public ResourceAllocation getResourceAllocation() {
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

        public AllocationsCurried(
                List<ResourceAllocationWithDesiredResourcesPerDay> resourceAllocations) {
            Validate.notNull(resourceAllocations);
            Validate.notEmpty(resourceAllocations);
            Validate.noNullElements(resourceAllocations);
            checkNoOneHasNullTask(resourceAllocations);
            checkAllHaveSameTask(resourceAllocations);
            this.resourceAllocations = resourceAllocations;
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
            return new AllocationsAndResourcesCurried(resources,
                    resourceAllocations);
        }
    }

    public static class AllocationsAndResourcesCurried {
        private List<Resource> resources;

        private List<ResourceAllocationWithDesiredResourcesPerDay> allocations;

        public AllocationsAndResourcesCurried(List<Resource> resources,
                List<ResourceAllocationWithDesiredResourcesPerDay> allocations) {
            this.resources = resources;
            this.allocations = allocations;
        }

        public LocalDate untilAllocating(int hoursToAllocate) {
            throw new RuntimeException(
                    "TODO: implement allocation for variable length tasks");
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
        return new ResourceAllocationWithDesiredResourcesPerDay(this, resourcesPerDay);
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
