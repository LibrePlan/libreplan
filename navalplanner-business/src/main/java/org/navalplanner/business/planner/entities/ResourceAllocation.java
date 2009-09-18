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

/**
 * Resources are allocated to planner tasks.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class ResourceAllocation extends BaseEntity {

    public static class ResourceAllocationWithDesiredResourcesPerDay {

        private final ResourceAllocation resourceAllocation;

        private final ResourcesPerDay resourcesPerDay;

        private ResourceAllocationWithDesiredResourcesPerDay(ResourceAllocation resourceAllocation,
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
        assignmentFunction = assignmentFunction;
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

    protected abstract class AssignmentsAllocation<T extends DayAssignment>
            implements IAllocatable {

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

        protected abstract void resetAssignmentsTo(List<T> assignments);

        private int calculateTotalToDistribute(LocalDate day,
                ResourcesPerDay resourcesPerDay) {
            Integer workableHours = getWorkableHoursAt(day);
            return resourcesPerDay
                    .asHoursGivenResourceWorkingDayOf(workableHours);
        }

        private Integer getWorkableHoursAt(LocalDate day) {
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

        private int getDaysElapsedAt(Task task) {
            LocalDate endExclusive = new LocalDate(task.getEndDate());
            Days daysBetween = Days.daysBetween(new LocalDate(task
                    .getStartDate()), endExclusive);
            return daysBetween.getDays();
        }

        protected abstract List<T> distributeForDay(LocalDate day,
                int totalHours);

    }

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
