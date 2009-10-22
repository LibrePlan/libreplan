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

package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
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

    public static <R extends ResourceAllocation<?>> List<R> sortedByStartDate(
            Collection<R> allocations) {
        List<R> result = new ArrayList<R>(allocations);
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

        public AllocationsAndResourcesCurried withExistentResources() {
            return new AllocationsAndResourcesCurried(task,
                    getResourcesFrom(ResourceAllocationWithDesiredResourcesPerDay
                            .stripResourcesPerDay(resourceAllocations)),
                    resourceAllocations);
        }

        private List<Resource> getResourcesFrom(
                List<ResourceAllocation<?>> allocations) {
            Set<Resource> resources = new HashSet<Resource>();
            for (ResourceAllocation<?> resourceAllocation : allocations) {
                resources.addAll(resourceAllocation.getAssociatedResources());
            }
            return new ArrayList<Resource>(resources);
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

    public abstract List<Resource> getAssociatedResources();

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

    public abstract IAllocatable withPreviousAssociatedResources();

    protected abstract class AssignmentsAllocation implements IAllocatable {

        @Override
        public final void allocate(ResourcesPerDay resourcesPerDay) {
            Task task = getTask();
            LocalDate startInclusive = new LocalDate(task.getStartDate());
            List<T> assignmentsCreated = new ArrayList<T>();
            LocalDate endExclusive = new LocalDate(task.getEndDate());
            for (LocalDate day : getDays(startInclusive, endExclusive)) {
                int totalForDay = calculateTotalToDistribute(day,
                        resourcesPerDay);
                assignmentsCreated.addAll(distributeForDay(day, totalForDay));
            }
            setResourcesPerDay(resourcesPerDay);
            resetAssignmentsTo(assignmentsCreated);
        }

        private List<LocalDate> getDays(LocalDate startInclusive,
                LocalDate endExclusive) {
            List<LocalDate> result = new ArrayList<LocalDate>();
            LocalDate current = startInclusive;
            do {
                result.add(current);
                current = current.plusDays(1);
            } while (current.compareTo(endExclusive) < 0);
            return result;
        }

        private class AllocateHoursOnInterval implements
                IAllocateHoursOnInterval {

            private final LocalDate start;
            private final LocalDate end;

            AllocateHoursOnInterval(LocalDate start, LocalDate end) {
                Validate.isTrue(start.compareTo(end) <= 0,
                        "the end must be equal or posterior than start");
                this.start = start;
                this.end = end;
            }

            public void allocateHours(int hours) {
                allocate(start, end, hours);
            }
        }

        @Override
        public IAllocateHoursOnInterval onInterval(LocalDate start,
                LocalDate end) {
            return new AllocateHoursOnInterval(start, end);
        }

        private void allocate(LocalDate startInclusive, LocalDate endExclusive,
                int hours) {
            Validate.isTrue(hours >= 0);
            Validate.isTrue(startInclusive.compareTo(endExclusive) <= 0,
                    "the end must be equal or posterior than start");
            List<T> assignmentsCreated = new ArrayList<T>();
            if (hours > 0) {
                List<LocalDate> days = getDays(startInclusive, endExclusive);
                int[] hoursEachDay = hoursDistribution(days, hours);
                int i = 0;
                for (LocalDate day : getDays(startInclusive, endExclusive)) {
                    assignmentsCreated.addAll(distributeForDay(day,
                            hoursEachDay[i++]));
                }
            }
            removingAssignments(getAssignments(startInclusive, endExclusive));
            addingAssignments(assignmentsCreated);
            setResourcesPerDay(calculateResourcesPerDayFromAssignments());
        }

        private int[] hoursDistribution(List<LocalDate> days, int hoursToSum) {
            List<Share> shares = new ArrayList<Share>();
            for (LocalDate day : days) {
                shares.add(new Share(-getWorkHoursPerDay()
                        .getWorkableHours(day)));
            }
            ShareDivision original = ShareDivision.create(shares);
            ShareDivision newShare = original.plus(hoursToSum);
            return original.to(newShare);
        }

        protected abstract List<T> distributeForDay(LocalDate day,
                int totalHours);

    }

    private void resetAssignmentsTo(List<T> assignments) {
        removingAssignments(getAssignments());
        addingAssignments(assignments);
    }

    protected abstract void addingAssignments(
            Collection<? extends T> assignments);

    protected abstract void removingAssignments(
            List<? extends DayAssignment> assignments);

    final int calculateTotalToDistribute(LocalDate day,
            ResourcesPerDay resourcesPerDay) {
        Integer workableHours = getWorkHoursPerDay().getWorkableHours(day);
        return resourcesPerDay.asHoursGivenResourceWorkingDayOf(workableHours);
    }

    private ResourcesPerDay calculateResourcesPerDayFromAssignments() {
        Map<LocalDate, List<DayAssignment>> byDay = DayAssignment
                .byDay(getAssignments());
        int sumTotalHours = 0;
        int sumWorkableHours = 0;
        for (Entry<LocalDate, List<DayAssignment>> entry : byDay.entrySet()) {
            sumWorkableHours += getWorkHoursPerDay().getWorkableHours(
                    entry.getKey());
            sumTotalHours += getAssignedHours(entry.getValue());
        }
        if (sumWorkableHours == 0) {
            return ResourcesPerDay.amount(0);
        }
        return ResourcesPerDay.calculateFrom(
                sumTotalHours, sumWorkableHours);
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

    ResourceAllocation<T> copy() {
        ResourceAllocation<T> copy = createCopy();
        copy.resourcesPerDay = resourcesPerDay;
        copy.task = task;
        copy.assignmentFunction = assignmentFunction;
        return copy;
    }

    abstract ResourceAllocation<T> createCopy();

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
        return assignments.get(assignments.size() - 1).getDay().plusDays(1);
    }

    public boolean isAlreadyFinishedBy(LocalDate date) {
        if (getEndDate() == null) {
            return false;
        }
        return getEndDate().compareTo(date) <= 0;
    }

    private interface PredicateOnDayAssignment {
        boolean satisfiedBy(DayAssignment dayAssignment);
    }


    public int getAssignedHours(final Resource resource, LocalDate start,
            LocalDate endExclusive) {
        return getAssignedHours(filter(getAssignments(start, endExclusive),new PredicateOnDayAssignment() {

            @Override
            public boolean satisfiedBy(DayAssignment dayAssignment) {
                return dayAssignment.isAssignedTo(resource);
            }
                }));
    }

    public List<DayAssignment> getAssignments(LocalDate start,
            LocalDate endExclusive) {
        List<DayAssignment> result = new ArrayList<DayAssignment>();
        for (DayAssignment dayAssignment : getAssignments()) {
            if (dayAssignment.getDay().compareTo(endExclusive) >= 0) {
                break;
            }
            if (dayAssignment.includedIn(start, endExclusive)) {
                result.add(dayAssignment);
            }
        }
        return result;
    }


    public int getAssignedHours(LocalDate start, LocalDate endExclusive) {
        return getAssignedHours(getAssignments(start, endExclusive));
    }

    private List<DayAssignment> filter(List<DayAssignment> assignments,
            PredicateOnDayAssignment predicate) {
        List<DayAssignment> result = new ArrayList<DayAssignment>();
        for (DayAssignment dayAssignment : assignments) {
            if (predicate.satisfiedBy(dayAssignment)) {
                result.add(dayAssignment);
            }
        }
        return result;
    }

    private int getAssignedHours(List<DayAssignment> assignments) {
        int sum = 0;
        for (DayAssignment dayAssignment : assignments) {
            sum += dayAssignment.getHours();
        }
        return sum;
    }

    public abstract void mergeAssignmentsAndResourcesPerDay(ResourceAllocation<?> modifications);

    void detachAssignments() {
        for (DayAssignment dayAssignment : getAssignments()) {
            dayAssignment.detach();
        }
    }

    void detach() {
        detachAssignments();
    }

    void associateAssignmentsToResource() {
        for (DayAssignment dayAssignment : getAssignments()) {
            dayAssignment.associateToResource();
        }
    }

}
