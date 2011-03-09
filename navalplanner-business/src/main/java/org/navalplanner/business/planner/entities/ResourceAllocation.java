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

package org.navalplanner.business.planner.entities;

import static org.navalplanner.business.workingday.EffortDuration.hours;
import static org.navalplanner.business.workingday.EffortDuration.seconds;
import static org.navalplanner.business.workingday.EffortDuration.zero;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CombinedWorkHours;
import org.navalplanner.business.calendars.entities.ICalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator;
import org.navalplanner.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.CapacityResult;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.planner.entities.DerivedAllocationGenerator.IWorkerFinder;
import org.navalplanner.business.planner.entities.allocationalgorithms.AllocatorForTaskDurationAndSpecifiedResourcesPerDay;
import org.navalplanner.business.planner.entities.allocationalgorithms.HoursModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.UntilFillingHoursAllocator;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.MachineWorkersConfigurationUnit;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.navalplanner.business.workingday.ResourcesPerDay;

/**
 * Resources are allocated to planner tasks.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class ResourceAllocation<T extends DayAssignment> extends
        BaseEntity {

    public static <T extends ResourceAllocation<?>> List<T> getSatisfied(
            Collection<T> resourceAllocations) {
        Validate.notNull(resourceAllocations);
        Validate.noNullElements(resourceAllocations);
        List<T> result = new ArrayList<T>();
        for (T each : resourceAllocations) {
            if (each.isSatisfied()) {
                result.add(each);
            }
        }
        return result;
    }

    public static <T extends ResourceAllocation<?>> List<T> getOfType(
            Class<T> type,
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        List<T> result = new ArrayList<T>();
        for (ResourceAllocation<?> allocation : resourceAllocations) {
            if (type.isInstance(allocation)) {
                result.add(type.cast(allocation));
            }
        }
        return result;
    }

    public static <R extends ResourceAllocation<?>> Map<Resource, List<R>> byResource(
            Collection<? extends R> allocations) {
        Map<Resource, List<R>> result = new HashMap<Resource, List<R>>();
        for (R resourceAllocation : allocations) {
            for (Resource resource : resourceAllocation
                    .getAssociatedResources()) {
                if (!result.containsKey(resource)) {
                    result.put(resource, new ArrayList<R>());
                }
                result.get(resource).add(resourceAllocation);
            }
        }
        return result;
    }

    public static <R extends ResourceAllocation<?>> List<R> sortedByStartDate(
            Collection<R> allocations) {
        List<R> result = new ArrayList<R>(allocations);
        Collections.sort(result, byStartDateComparator());
        return result;
    }

    public static <R extends ResourceAllocation<?>> Map<Task, List<R>> byTask(
            List<? extends R> allocations) {
        Map<Task, List<R>> result = new HashMap<Task, List<R>>();
        for (R resourceAllocation : allocations) {
            if (resourceAllocation.getTask() != null) {
                Task task = resourceAllocation.getTask();
                initializeIfNeeded(result, task);
                result.get(task).add(resourceAllocation);
            }
        }
        return result;
    }

    private static <E extends ResourceAllocation<?>> void initializeIfNeeded(
            Map<Task, List<E>> result, Task task) {
        if (!result.containsKey(task)) {
            result.put(task, new ArrayList<E>());
        }
    }

    private static Comparator<ResourceAllocation<?>> byStartDateComparator() {
        return new Comparator<ResourceAllocation<?>>() {

            @Override
            public int compare(ResourceAllocation<?> o1,
                    ResourceAllocation<?> o2) {
                if (o1.getIntraDayStartDate() == null) {
                    return -1;
                }
                if (o2.getIntraDayStartDate() == null) {
                    return 1;
                }
                return o1.getIntraDayStartDate().compareTo(
                        o2.getIntraDayStartDate());
            }
        };
    }

    public enum Direction {
        FORWARD {
            @Override
            public IntraDayDate getDateFromWhichToAllocate(Task task) {
                return IntraDayDate.max(task.getFirstDayNotConsolidated(),
                        task.getIntraDayStartDate());
            }

            @Override
            void limitAvailabilityOn(AvailabilityTimeLine availability,
                    IntraDayDate dateFromWhichToAllocate) {
                availability.invalidUntil(dateFromWhichToAllocate
                        .asExclusiveEnd());
            }
        },
        BACKWARD {
            @Override
            public IntraDayDate getDateFromWhichToAllocate(Task task) {
                return task.getIntraDayEndDate();
            }

            @Override
            void limitAvailabilityOn(AvailabilityTimeLine availability,
                    IntraDayDate dateFromWhichToAllocate) {
                availability.invalidFrom(dateFromWhichToAllocate.getDate());
            }
        };

        public abstract IntraDayDate getDateFromWhichToAllocate(Task task);

        abstract void limitAvailabilityOn(AvailabilityTimeLine availability,
                IntraDayDate dateFromWhichToAllocate);

    }

    public static AllocationsSpecified allocating(
            List<ResourcesPerDayModification> resourceAllocations) {
        return new AllocationsSpecified(resourceAllocations);
    }

    /**
     * Needed for doing fluent interface calls:
     * <ul>
     * <li>
     * {@link ResourceAllocation#allocating(List)}.
     * {@link AllocationsSpecified#untilAllocating(int) untiAllocating(int)}</li>
     * <li> {@link ResourceAllocation#allocating(List)}.
     * {@link AllocationsSpecified#allocateOnTaskLength() allocateOnTaskLength}</li>
     * <li>
     * {@link ResourceAllocation#allocating(List)}.
     * {@link AllocationsSpecified#allocateUntil(LocalDate)
     * allocateUntil(LocalDate)}</li>
     * </ul>
     *
     */
    public static class AllocationsSpecified {

        private final List<ResourcesPerDayModification> allocations;

        private final Task task;

        public AllocationsSpecified(
                List<ResourcesPerDayModification> resourceAllocations) {
            Validate.notNull(resourceAllocations);
            Validate.notEmpty(resourceAllocations);
            Validate.noNullElements(resourceAllocations);
            checkNoOneHasNullTask(resourceAllocations);
            checkAllHaveSameTask(resourceAllocations);
            checkNoAllocationWithZeroResourcesPerDay(resourceAllocations);
            this.allocations = resourceAllocations;
            this.task = resourceAllocations.get(0).getBeingModified()
                    .getTask();
        }

        private static void checkNoAllocationWithZeroResourcesPerDay(
                List<ResourcesPerDayModification> allocations) {
            for (ResourcesPerDayModification r : allocations) {
                if (isZero(r.getGoal().getAmount())) {
                    throw new IllegalArgumentException(
                            "all resources per day must be no zero");
                }
            }
        }

        public static boolean isZero(BigDecimal amount) {
            return amount.movePointRight(amount.scale()).intValue() == 0;
        }

        private static void checkNoOneHasNullTask(
                List<ResourcesPerDayModification> allocations) {
            for (ResourcesPerDayModification resourcesPerDayModification : allocations) {
                if (resourcesPerDayModification
                        .getBeingModified().getTask() == null) {
                    throw new IllegalArgumentException(
                            "all allocations must have task");
                }
            }
        }

        private static void checkAllHaveSameTask(
                List<ResourcesPerDayModification> resourceAllocations) {
            Task task = null;
            for (ResourcesPerDayModification r : resourceAllocations) {
                if (task == null) {
                    task = r.getBeingModified().getTask();
                }
                if (!task.equals(r.getBeingModified().getTask())) {
                    throw new IllegalArgumentException(
                            "all allocations must belong to the same task");
                }
            }
        }

        public interface INotFulfilledReceiver {
            public void cantFulfill(
                    ResourcesPerDayModification allocationAttempt,
                    CapacityResult capacityResult);
        }

        public IntraDayDate untilAllocating(int hoursToAllocate) {
            return untilAllocating(Direction.FORWARD, hoursToAllocate);
        }

        public IntraDayDate untilAllocating(Direction direction,
                int hoursToAllocate) {
            return untilAllocating(direction, hoursToAllocate, doNothing());
        }

        private static INotFulfilledReceiver doNothing() {
            return new INotFulfilledReceiver() {
                @Override
                public void cantFulfill(
                        ResourcesPerDayModification allocationAttempt,
                        CapacityResult capacityResult) {
                }
            };
        }

        public IntraDayDate untilAllocating(int hoursToAllocate,
                final INotFulfilledReceiver receiver) {
            return untilAllocating(Direction.FORWARD, hoursToAllocate, receiver);
        }

        public IntraDayDate untilAllocating(Direction direction,
                int hoursToAllocate, final INotFulfilledReceiver receiver) {
            UntilFillingHoursAllocator allocator = new UntilFillingHoursAllocator(
                    direction,
                    task, allocations) {

                @Override
                protected List<DayAssignment> createAssignmentsAtDay(
                        ResourcesPerDayModification allocation, PartialDay day,
                        EffortDuration limit) {
                    return allocation.createAssignmentsAtDay(day, limit);
                }

                @Override
                protected <T extends DayAssignment> void setNewDataForAllocation(
                        ResourceAllocation<T> allocation,
                        IntraDayDate resultDate,
                        ResourcesPerDay resourcesPerDay, List<T> dayAssignments) {
                    Task task = AllocationsSpecified.this.task;
                    if (isForwardScheduling()) {
                        allocation.resetAllAllocationAssignmentsTo(
                                dayAssignments,
                                task.getIntraDayStartDate(), resultDate);
                    } else {
                        allocation.resetAllAllocationAssignmentsTo(
                                dayAssignments,
                                resultDate, task.getIntraDayEndDate());
                    }
                    allocation.updateResourcesPerDay();
                }

                @Override
                protected CapacityResult thereAreAvailableHoursFrom(
                        IntraDayDate dateFromWhichToAllocate,
                        ResourcesPerDayModification resourcesPerDayModification,
                        EffortDuration effortToAllocate) {
                    ICalendar calendar = getCalendar(resourcesPerDayModification);
                    ResourcesPerDay resourcesPerDay = resourcesPerDayModification
                            .getGoal();
                    AvailabilityTimeLine availability = resourcesPerDayModification
                            .getAvailability();
                    getDirection().limitAvailabilityOn(availability,
                            dateFromWhichToAllocate);
                    return ThereAreHoursOnWorkHoursCalculator
                            .thereIsAvailableCapacityFor(calendar,
                                    availability, resourcesPerDay,
                                    effortToAllocate);
                }

                private CombinedWorkHours getCalendar(
                        ResourcesPerDayModification resourcesPerDayModification) {
                    return CombinedWorkHours.minOf(resourcesPerDayModification
                            .getBeingModified().getTaskCalendar(),
                            resourcesPerDayModification.getResourcesCalendar());
                }

                @Override
                protected void markUnsatisfied(
                        ResourcesPerDayModification allocationAttempt,
                        CapacityResult capacityResult) {
                    allocationAttempt.getBeingModified().markAsUnsatisfied();
                    receiver.cantFulfill(allocationAttempt, capacityResult);
                }

            };
            IntraDayDate result = allocator
                    .untilAllocating(hours(hoursToAllocate));
            if (result == null) {
                // allocation could not be done
                return direction == Direction.FORWARD ? task
                        .getIntraDayEndDate() : task.getIntraDayStartDate();
            }
            return result;
        }

        public void allocateOnTaskLength() {
            AllocatorForTaskDurationAndSpecifiedResourcesPerDay allocator = new AllocatorForTaskDurationAndSpecifiedResourcesPerDay(
                    allocations);
            allocator.allocateOnTaskLength();
        }

        public void allocateUntil(LocalDate endExclusive) {
            AllocatorForTaskDurationAndSpecifiedResourcesPerDay allocator = new AllocatorForTaskDurationAndSpecifiedResourcesPerDay(
                    allocations);
            allocator.allocateUntil(endExclusive);
        }

        public void allocateFromEndUntil(LocalDate start) {
            AllocatorForTaskDurationAndSpecifiedResourcesPerDay allocator = new AllocatorForTaskDurationAndSpecifiedResourcesPerDay(
                    allocations);
            allocator.allocateFromEndUntil(start);
        }
    }

    public static HoursAllocationSpecified allocatingHours(
            List<HoursModification> hoursModifications) {
        return new HoursAllocationSpecified(hoursModifications);
    }

    /**
     * Needed for doing fluent interface calls:
     * <ul>
     * <li>
     * {@link ResourceAllocation#allocatingHours(List)}.
     * {@link HoursAllocationSpecified#allocateUntil(LocalDate)
     * allocateUntil(LocalDate)}</li>
     * <li>
     * {@link ResourceAllocation#allocatingHours(List)}.
     * {@link HoursAllocationSpecified#allocate() allocate()}</li>
     * </li>
     * </ul>
     *
     */
    public static class HoursAllocationSpecified {

        private final List<HoursModification> hoursModifications;

        private Task task;

        public HoursAllocationSpecified(List<HoursModification> hoursModifications) {
            Validate.noNullElements(hoursModifications);
            Validate.isTrue(!hoursModifications.isEmpty());
            this.hoursModifications = hoursModifications;
            this.task = hoursModifications.get(0).getBeingModified().getTask();
            Validate.notNull(task);
        }

        public void allocate() {
            allocateUntil(new LocalDate(task.getEndDate()));
        }

        public void allocateUntil(LocalDate end) {
            Validate.notNull(end);
            Validate.isTrue(!end.isBefore(new LocalDate(task.getStartDate())));
            for (HoursModification each : hoursModifications) {
                each.allocateUntil(end);
            }
        }

        public void allocateFromEndUntil(LocalDate start) {
            Validate.notNull(start);
            Validate.isTrue(start.isBefore(task.getEndAsLocalDate()));
            for (HoursModification each : hoursModifications) {
                each.allocateFromEndUntil(start);
            }

        }

    }

    private Task task;

    private AssignmentFunction assignmentFunction;

    @OnCopy(Strategy.SHARE)
    private ResourcesPerDay resourcesPerDay;

    private Integer intendedTotalHours;

    private Set<DerivedAllocation> derivedAllocations = new HashSet<DerivedAllocation>();

    @OnCopy(Strategy.SHARE_COLLECTION_ELEMENTS)
    private Set<LimitingResourceQueueElement> limitingResourceQueueElements = new HashSet<LimitingResourceQueueElement>();

    private int originalTotalAssignment = 0;

    private IOnDayAssignmentRemoval dayAssignmenteRemoval = new DoNothing();

    public interface IOnDayAssignmentRemoval {

        public void onRemoval(ResourceAllocation<?> allocation,
                DayAssignment assignment);
    }

    public static class DoNothing implements IOnDayAssignmentRemoval {

        @Override
        public void onRemoval(
                ResourceAllocation<?> allocation, DayAssignment assignment) {
        }
    }

    public static class DetachDayAssignmentOnRemoval implements
            IOnDayAssignmentRemoval {

        @Override
        public void onRemoval(ResourceAllocation<?> allocation,
                DayAssignment assignment) {
            assignment.detach();
        }
    }

    public void setOnDayAssignmentRemoval(
            IOnDayAssignmentRemoval dayAssignmentRemoval) {
        Validate.notNull(dayAssignmentRemoval);
        this.dayAssignmenteRemoval = dayAssignmentRemoval;
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public ResourceAllocation() {
        this.assignmentsState = buildFromDBState();
    }

    /**
     * Returns the associated resources from the day assignments of this
     * {@link ResourceAllocation}.
     * @return the associated resources with no repeated elements
     */
    public abstract List<Resource> getAssociatedResources();

    public void switchToScenario(Scenario scenario) {
        Validate.notNull(scenario);
        assignmentsState = assignmentsState.switchTo(scenario);
        switchDerivedAllocationsTo(scenario);
    }

    private void switchDerivedAllocationsTo(Scenario scenario) {
        for (DerivedAllocation each : derivedAllocations) {
            each.useScenario(scenario);
        }
    }

    protected void updateResourcesPerDay() {
        ResourcesPerDay resourcesPerDay = calculateResourcesPerDayFromAssignments(getAssignments());
        if (resourcesPerDay == null) {
            this.resourcesPerDay = ResourcesPerDay.amount(0);
        } else {
            this.resourcesPerDay = resourcesPerDay;
        }
    }

    protected void setResourcesPerDayToAmount(int amount) {
        this.resourcesPerDay = ResourcesPerDay.amount(amount);
    }

    public ResourceAllocation(Task task) {
        this(task, null);
    }

    public ResourceAllocation(Task task, AssignmentFunction assignmentFunction) {
        Validate.notNull(task);
        this.task = task;
        this.assignmentFunction = assignmentFunction;
        this.assignmentsState = buildInitialTransientState();
    }

    protected ResourceAllocation(ResourcesPerDay resourcesPerDay, Task task) {
        this(task);
        Validate.notNull(resourcesPerDay);
        this.resourcesPerDay = resourcesPerDay;
    }

    @NotNull
    public Task getTask() {
        return task;
    }

    private void updateOriginalTotalAssigment() {
        if (!isSatisfied()) {
            return;
        }
        if ((task.getConsolidation() == null)
                || (task.getConsolidation().getConsolidatedValues().isEmpty())) {
            originalTotalAssignment = getNonConsolidatedHours();
        } else {
            BigDecimal lastConslidation = task.getConsolidation()
                    .getConsolidatedValues().last().getValue();
            BigDecimal unconsolitedPercentage = BigDecimal.ONE
                    .subtract(lastConslidation.setScale(2).divide(
                            new BigDecimal(100), RoundingMode.DOWN));
            if (unconsolitedPercentage.setScale(2).equals(
                    BigDecimal.ZERO.setScale(2))) {
                originalTotalAssignment = getConsolidatedHours();
            } else {
                originalTotalAssignment = new BigDecimal(
                        getNonConsolidatedHours()).divide(
                        unconsolitedPercentage, RoundingMode.DOWN).intValue();
            }
        }
    }

    @Min(0)
    public int getOriginalTotalAssigment() {
        return originalTotalAssignment;
    }

    public abstract ResourcesPerDayModification withDesiredResourcesPerDay(
            ResourcesPerDay resourcesPerDay);

    public abstract ResourcesPerDayModification asResourcesPerDayModification();

    public abstract HoursModification asHoursModification();

    public abstract IAllocatable withPreviousAssociatedResources();

    protected abstract class AssignmentsAllocator implements IAllocatable {

        @Override
        public final void allocate(ResourcesPerDay resourcesPerDay) {
            Task currentTask = getTask();
            List<T> assignmentsCreated = createAssignments(resourcesPerDay,
                    currentTask.getIntraDayStartDate(),
                    currentTask.getIntraDayEndDate());
            resetAssignmentsTo(assignmentsCreated);
            updateResourcesPerDay();
        }

        private List<T> createAssignments(ResourcesPerDay resourcesPerDay,
                IntraDayDate startInclusive, IntraDayDate endExclusive) {
            List<T> assignmentsCreated = new ArrayList<T>();
            for (PartialDay day : getDays(startInclusive, endExclusive)) {
                EffortDuration durationForDay = calculateTotalToDistribute(day,
                        resourcesPerDay);
                assignmentsCreated.addAll(distributeForDay(day.getDate(),
                        durationForDay));
            }
            return assignmentsCreated;
        }

        @Override
        public IAllocateResourcesPerDay resourcesPerDayUntil(final LocalDate endExclusive) {
            IntraDayDate startInclusive = getStartSpecifiedByTask();
            IntraDayDate end = IntraDayDate.startOfDay(endExclusive);
            return new AllocateResourcesPerDayOnInterval(startInclusive, end);
        }

        @Override
        public IAllocateResourcesPerDay resourcesPerDayFromEndUntil(
                LocalDate start) {
            IntraDayDate startInclusive = IntraDayDate.max(
                    IntraDayDate.startOfDay(start), getStartSpecifiedByTask());
            IntraDayDate endDate = task.getIntraDayEndDate();
            return new AllocateResourcesPerDayOnInterval(startInclusive,
                    endDate);
        }

        private List<PartialDay> getDays(IntraDayDate startInclusive,
                IntraDayDate endExclusive) {
            Validate.notNull(startInclusive);
            Validate.notNull(endExclusive);
            Validate.isTrue(startInclusive.compareTo(endExclusive) <= 0,
                    "the end must be equal or posterior than start");
            return IntraDayDate.toList(startInclusive.daysUntil(endExclusive));
        }

        private final class AllocateResourcesPerDayOnInterval implements
                IAllocateResourcesPerDay {

            private final IntraDayDate startInclusive;

            private final IntraDayDate endExclusive;

            private AllocateResourcesPerDayOnInterval(
                    IntraDayDate startInclusive, IntraDayDate endExclusive) {
                this.startInclusive = startInclusive;
                this.endExclusive = endExclusive;
            }

            @Override
            public void allocate(ResourcesPerDay resourcesPerDay) {
                List<T> assignmentsCreated = createAssignments(resourcesPerDay,
                        startInclusive, endExclusive);
                resetAllAllocationAssignmentsTo(assignmentsCreated,
                        startInclusive,
                        endExclusive);
                updateResourcesPerDay();
            }
        }

        private class AllocateHoursOnInterval implements
                IAllocateHoursOnInterval {

            private final LocalDate start;
            private final LocalDate end;

            AllocateHoursOnInterval(LocalDate start, LocalDate end) {
                checkStartBeforeOrEqualEnd(start, end);
                this.start = start;
                this.end = end;
            }


            public void allocateHours(int hours) {
                allocateSubintervalWithinTaskBounds(start, end, hours(hours));
            }
        }

        private void checkStartBeforeOrEqualEnd(LocalDate start, LocalDate end) {
            Validate.isTrue(start.compareTo(end) <= 0,
                    "the end must be equal or posterior than start");
        }

        @Override
        public IAllocateHoursOnInterval onIntervalWithinTask(LocalDate start,
                LocalDate end) {
            return new AllocateHoursOnInterval(start, end);
        }

        @Override
        public IAllocateHoursOnInterval onInterval(
                final LocalDate startInclusive, final LocalDate endExclusive) {
            checkStartBeforeOrEqualEnd(startInclusive, endExclusive);
            return new IAllocateHoursOnInterval() {

                @Override
                public void allocateHours(int hours) {
                    allocateInterval(startInclusive, endExclusive, hours);
                }

            };
        }

        @Override
        public IAllocateHoursOnInterval fromStartUntil(final LocalDate end) {
            return new IAllocateHoursOnInterval() {

                @Override
                public void allocateHours(int hours) {
                    allocateTheWholeAllocation(getStartSpecifiedByTask(),
                            IntraDayDate.startOfDay(end), hours(hours));
                }
            };
        }

        @Override
        public IAllocateHoursOnInterval fromEndUntil(final LocalDate start) {
            return new IAllocateHoursOnInterval() {

                @Override
                public void allocateHours(int hours) {
                    allocateTheWholeAllocation(IntraDayDate.startOfDay(start),
                            task.getIntraDayEndDate(), hours(hours));
                }
            };
        }

        private void allocateTheWholeAllocation(IntraDayDate startInclusive,
                IntraDayDate endExclusive, EffortDuration durationToAssign) {
            AllocationInterval interval = new AllocationInterval(
                    startInclusive, endExclusive);
            List<T> assignmentsCreated = createAssignments(interval,
                    durationToAssign);
            resetAllAllocationAssignmentsTo(assignmentsCreated,
                    interval.getStartInclusive(), interval.getEndExclusive());
            updateResourcesPerDay();
        }

        private void allocateSubintervalWithinTaskBounds(
                LocalDate startInclusive, LocalDate endExclusive,
                EffortDuration durationToAssign) {
            AllocationIntervalInsideTask interval = new AllocationIntervalInsideTask(
                    startInclusive, endExclusive);
            List<T> assignmentsCreated = createAssignments(interval,
                    durationToAssign);
            resetAssigmentsForInterval(interval, assignmentsCreated);
        }

        private void allocateInterval(LocalDate startInclusive,
                LocalDate endExclusive, int hours) {
            AllocationInterval interval = new AllocationInterval(
                    startInclusive, endExclusive);
            List<T> assignmentsCreated = createAssignments(interval,
                    hours(hours));
            resetAssigmentsFittingAllocationDatesToResultingAssignments(
                    interval, assignmentsCreated);
        }

        protected abstract AvailabilityTimeLine getResourcesAvailability();

        private List<T> createAssignments(AllocationInterval interval,
                EffortDuration durationToAssign) {
            List<T> assignmentsCreated = new ArrayList<T>();
            AvailabilityTimeLine availability = getAvailability();

            List<PartialDay> days = getDays(interval.getStartInclusive(),
                    interval.getEndExclusive());
            EffortDuration[] durationsEachDay = secondsDistribution(
                    availability, days, durationToAssign);
            int i = 0;
            for (PartialDay day : days) {
                // if all days are not available, it would try to assign
                // them anyway, preventing it with a check
                if (availability.isValid(day.getDate())) {
                    assignmentsCreated.addAll(distributeForDay(day.getDate(),
                            durationsEachDay[i]));
                }
                i++;
            }
            return onlyNonZeroHours(assignmentsCreated);
        }

        private AvailabilityTimeLine getAvailability() {
            AvailabilityTimeLine resourcesAvailability = getResourcesAvailability();
            BaseCalendar taskCalendar = getTask().getCalendar();
            if (taskCalendar != null) {
                return taskCalendar.getAvailability()
                        .and(resourcesAvailability);
            } else {
                return resourcesAvailability;
            }
        }

        private List<T> onlyNonZeroHours(List<T> assignmentsCreated) {
            List<T> result = new ArrayList<T>();
            for (T each : assignmentsCreated) {
                if (!each.getDuration().isZero()) {
                    result.add(each);
                }
            }
            return result;
        }

        private EffortDuration[] secondsDistribution(
                AvailabilityTimeLine availability, List<PartialDay> days,
                EffortDuration duration) {
            List<Share> shares = new ArrayList<Share>();
            for (PartialDay each : days) {
                shares.add(getShareAt(each, availability));
            }
            ShareDivision original = ShareDivision.create(shares);
            ShareDivision newShare = original.plus(duration.getSeconds());
            return fromSecondsToDurations(original.to(newShare));
        }

        private EffortDuration[] fromSecondsToDurations(int[] seconds) {
            EffortDuration[] result = new EffortDuration[seconds.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = seconds(seconds[i]);
            }
            return result;
        }

        private Share getShareAt(PartialDay day,
                AvailabilityTimeLine availability) {
            if (availability.isValid(day.getDate())) {
                EffortDuration capacityAtDay = getAllocationCalendar()
                        .getCapacityOn(day);
                return new Share(-capacityAtDay.getSeconds());
            } else {
                return new Share(Integer.MAX_VALUE);
            }
        }

        protected abstract List<T> distributeForDay(LocalDate day,
                EffortDuration effort);

    }

    private void markAsUnsatisfied() {
        removingAssignments(getAssignments());
        assert isUnsatisfied();
    }

    public boolean isLimiting() {
        return getLimitingResourceQueueElement() != null;
    }

    public boolean isLimitingAndHasDayAssignments() {
        return isLimiting() && hasAssignments();
    }

    public boolean isSatisfied() {
        return hasAssignments();
    }

    public boolean isUnsatisfied() {
        return !isSatisfied();
    }

    public void copyAssignmentsFromOneScenarioToAnother(Scenario from, Scenario to){
        copyAssignments(from, to);
        for (DerivedAllocation each : derivedAllocations) {
            each.copyAssignments(from, to);
        }
    }

    protected abstract void copyAssignments(Scenario from, Scenario to);

    protected void resetAssignmentsTo(List<T> assignments) {
        resetAllAllocationAssignmentsTo(assignments,
                task.getIntraDayStartDate(),
                task.getIntraDayEndDate());
    }

    protected void resetAllAllocationAssignmentsTo(List<T> assignments,
            IntraDayDate intraDayStart,
            IntraDayDate intraDayEnd) {
        removingAssignments(withoutConsolidated(getAssignments()));
        addingAssignments(assignments);
        updateOriginalTotalAssigment();
        getDayAssignmentsState().setIntraDayStart(intraDayStart);
        getDayAssignmentsState().setIntraDayEnd(intraDayEnd);
    }

    class AllocationInterval {

        private final IntraDayDate start;

        private final IntraDayDate end;

        private AllocationInterval(IntraDayDate start, IntraDayDate end) {
            IntraDayDate startConsideringConsolidated = task
                    .hasConsolidations() ? IntraDayDate
                    .max(task.getFirstDayNotConsolidated(), start) : start;

            this.start = IntraDayDate.min(startConsideringConsolidated, end);
            this.end = IntraDayDate.max(this.start, end);
        }

        public AllocationInterval(LocalDate startInclusive,
                LocalDate endExclusive) {
            this(IntraDayDate.startOfDay(startInclusive), IntraDayDate
                    .startOfDay(endExclusive));
        }

        public IntraDayDate getStartInclusive() {
            return this.start;
        }

        public IntraDayDate getEndExclusive() {
            return this.end;
        }

        public List<DayAssignment> getAssignmentsOnInterval() {
            return getAssignments(this.start.getDate(),
                    this.end.asExclusiveEnd());
        }

    }

    class AllocationIntervalInsideTask extends AllocationInterval {

        AllocationIntervalInsideTask(LocalDate startInclusive,
                LocalDate endExclusive) {
            this(IntraDayDate.startOfDay(startInclusive), IntraDayDate
                    .startOfDay(endExclusive));
        }

        AllocationIntervalInsideTask(IntraDayDate startInclusive,
                IntraDayDate endExclusive) {
            super(IntraDayDate.max(startInclusive, getTask()
                    .getFirstDayNotConsolidated()), IntraDayDate.min(
                    endExclusive, task.getIntraDayEndDate()));
        }
    }

    protected void resetAssigmentsForInterval(
            AllocationIntervalInsideTask interval,
            List<T> assignmentsCreated) {
        IntraDayDate originalStart = getIntraDayStartDate();
        IntraDayDate originalEnd = getIntraDayEndDate();

        updateAssignments(interval, assignmentsCreated);

        // The resource allocation cannot grow beyond the start of the task.
        // This
        // is guaranteed by IntervalInsideTask. It also cannot shrink from the
        // original size, this is guaranteed by originalStart
        getDayAssignmentsState().setIntraDayStart(
                IntraDayDate.min(originalStart, interval.getStartInclusive()));

        // The resource allocation cannot grow beyond the end of the task. This
        // is guaranteed by IntervalInsideTask. It also cannot shrink from the
        // original size, this is guaranteed by originalEnd
        getDayAssignmentsState().setIntraDayEnd(
                IntraDayDate.max(originalEnd, interval.getEndExclusive()));
    }

    private void updateAssignments(AllocationInterval interval,
            List<T> assignmentsCreated) {
        removingAssignments(withoutConsolidated(interval
                .getAssignmentsOnInterval()));
        addingAssignments(assignmentsCreated);
        updateOriginalTotalAssigment();
        updateResourcesPerDay();
    }

    private void resetAssigmentsFittingAllocationDatesToResultingAssignments(
            AllocationInterval interval, List<T> assignmentsCreated) {
        updateAssignments(interval, assignmentsCreated);

        LocalDate startConsideringAssignments = getStartConsideringAssignments();
        IntraDayDate start = IntraDayDate
                .startOfDay(startConsideringAssignments);
        if (interval.getStartInclusive()
                .areSameDay(startConsideringAssignments)) {
            start = interval.getStartInclusive();
        }
        getDayAssignmentsState().setIntraDayStart(start);

        LocalDate endConsideringAssignments = getEndDateGiven(getAssignments());
        IntraDayDate end = IntraDayDate.startOfDay(endConsideringAssignments);
        if (interval.getEndExclusive().areSameDay(endConsideringAssignments)) {
            end = interval.getEndExclusive();
        }
        getDayAssignmentsState().setIntraDayEnd(end);
    }

    private static <T extends DayAssignment> List<T> withoutConsolidated(
            List<? extends T> assignments) {
        List<T> result = new ArrayList<T>();
        for (T each : assignments) {
            if (!each.isConsolidated()) {
                result.add(each);
            }
        }
        return result;
    }

    protected final void addingAssignments(Collection<? extends T> assignments) {
        getDayAssignmentsState().addingAssignments(assignments);
    }

    public void removeLimitingDayAssignments() {
        allocateLimitingDayAssignments(Collections.<T>emptyList());
    }

    @SuppressWarnings("unchecked")
    public void allocateLimitingDayAssignments(List<? extends DayAssignment> assignments) {
        assert isLimiting();
        resetAssignmentsTo((List<T>) assignments);
    }

    private void removingAssignments(
            List<? extends DayAssignment> assignments) {
        getDayAssignmentsState().removingAssignments(assignments);
    }

    final EffortDuration calculateTotalToDistribute(PartialDay day,
            ResourcesPerDay resourcesPerDay) {
        return getAllocationCalendar().asDurationOn(day, resourcesPerDay);
    }

    public ResourcesPerDay calculateResourcesPerDayFromAssignments() {
        return calculateResourcesPerDayFromAssignments(getAssignments());
    }

    private ResourcesPerDay calculateResourcesPerDayFromAssignments(
            Collection<? extends T> assignments) {
        Map<LocalDate, List<T>> byDay = DayAssignment.byDay(assignments);
        EffortDuration sumTotalEffort = zero();
        EffortDuration sumWorkableEffort = zero();
        final ResourcesPerDay ONE_RESOURCE_PER_DAY = ResourcesPerDay.amount(1);
        for (Entry<LocalDate, List<T>> entry : byDay.entrySet()) {
            LocalDate dayDate = entry.getKey();
            PartialDay day = dayFor(dayDate);
            EffortDuration incrementWorkable = getAllocationCalendar()
                    .asDurationOn(day, ONE_RESOURCE_PER_DAY);
            sumWorkableEffort = sumWorkableEffort.plus(incrementWorkable);
            sumTotalEffort = sumTotalEffort.plus(getAssignedDuration(entry
                    .getValue()));
        }
        if (sumWorkableEffort.equals(zero())) {
            return ResourcesPerDay.amount(0);
        }
        return ResourcesPerDay.calculateFrom(sumTotalEffort, sumWorkableEffort);
    }

    private PartialDay dayFor(LocalDate dayDate) {
        IntraDayDate startDate = startFor(dayDate);

        IntraDayDate intraDayEnd = getDayAssignmentsState()
                .getIntraDayEnd();
        if (intraDayEnd != null && dayDate.equals(intraDayEnd.getDate())) {
            return new PartialDay(startDate, intraDayEnd);
        }
        return new PartialDay(startDate, startDate.nextDayAtStart());
    }

    private IntraDayDate startFor(LocalDate dayDate) {
        IntraDayDate start = getIntraDayStartDate();
        if (start.getDate().equals(dayDate)) {
            return start;
        } else {
            return IntraDayDate.startOfDay(dayDate);
        }
    }

    public ICalendar getAllocationCalendar() {
        return getCalendarGivenTaskCalendar(getTaskCalendar());
    }

    private ICalendar getTaskCalendar() {
        if (getTask().getCalendar() == null) {
            return SameWorkHoursEveryDay.getDefaultWorkingDay();
        } else {
            return getTask().getCalendar();
        }
    }

    protected abstract ICalendar getCalendarGivenTaskCalendar(
            ICalendar taskCalendar);

    protected abstract Class<T> getDayAssignmentType();

    public ResourceAllocation<T> copy(Scenario scenario) {
        Validate.notNull(scenario);
        ResourceAllocation<T> copy = createCopy(scenario);
        copy.assignmentsState = copy.toTransientStateWithInitial(
                getUnorderedFor(scenario), getIntraDayStartDateFor(scenario),
                getIntraDayEndFor(scenario));
        copy.resourcesPerDay = resourcesPerDay;
        copy.originalTotalAssignment = originalTotalAssignment;
        copy.task = task;
        copy.assignmentFunction = assignmentFunction;
        return copy;
    }

    private DayAssignmentsState toTransientStateWithInitial(
            Collection<? extends T> initialAssignments, IntraDayDate start,
            IntraDayDate end) {
        TransientState result = new TransientState(initialAssignments);
        result.setIntraDayStart(start);
        result.setIntraDayEnd(end);
        return result;
    }

    private Set<T> getUnorderedFor(Scenario scenario) {
        IDayAssignmentsContainer<T> container = retrieveContainerFor(scenario);
        if (container == null) {
            return new HashSet<T>();
        }
        return container.getDayAssignments();
    }

    private IntraDayDate getIntraDayStartDateFor(Scenario scenario) {
        IDayAssignmentsContainer<T> container = retrieveContainerFor(scenario);
        if (container == null) {
            return null;
        }
        return container.getIntraDayStart();
    }

    private IntraDayDate getIntraDayEndFor(Scenario scenario) {
        IDayAssignmentsContainer<T> container = retrieveContainerFor(scenario);
        if (container == null) {
            return null;
        }
        return container.getIntraDayEnd();
    }

    abstract ResourceAllocation<T> createCopy(Scenario scenario);

    public AssignmentFunction getAssignmentFunction() {
        return assignmentFunction;
    }

    public void setAssignmentFunction(AssignmentFunction assignmentFunction) {
        this.assignmentFunction = assignmentFunction;
        if (this.assignmentFunction != null) {
            this.assignmentFunction.applyTo(this);
        }
    }

    private void setWithoutApply(AssignmentFunction assignmentFunction) {
        this.assignmentFunction = assignmentFunction;
    }

    public int getAssignedHours() {
        return DayAssignment.sum(getAssignments()).roundToHours();
    }

    protected int getIntendedHours() {
        return originalTotalAssignment;
    }

    @OnCopy(Strategy.IGNORE)
    private DayAssignmentsState assignmentsState;

    protected DayAssignmentsState getDayAssignmentsState() {
        return assignmentsState;
    }

    private TransientState buildInitialTransientState() {
        return new TransientState(new HashSet<T>());
    }

    private DayAssignmentsState buildFromDBState() {
        return new NoExplicitlySpecifiedScenario();
    }

    abstract class DayAssignmentsState {

        private List<T> dayAssignmentsOrdered = null;

        protected List<T> getOrderedDayAssignments() {
            if (dayAssignmentsOrdered == null) {
                dayAssignmentsOrdered = DayAssignment
                        .orderedByDay(getUnorderedAssignments());
            }
            return dayAssignmentsOrdered;
        }

        /**
         * It can be null. It allows to mark that the allocation is started in a
         * point within a day instead of the start of the day
         */
        abstract IntraDayDate getIntraDayStart();

        /**
         * Set a new intraDayStart.
         *
         * @param intraDayStart
         *            it can be <code>null</code>
         * @see getIntraDayStart
         */
        public abstract void setIntraDayStart(IntraDayDate intraDayStart);


        /**
         * It can be null. It allows to mark that the allocation is finished in
         * a point within a day instead of taking the whole day
         */
        abstract IntraDayDate getIntraDayEnd();

        /**
         * Set a new intraDayEnd.
         *
         * @param intraDayEnd
         *            it can be <code>null</code>
         * @see getIntraDayEnd
         */
        public abstract void setIntraDayEnd(IntraDayDate intraDayEnd);

        protected abstract Collection<T> getUnorderedAssignments();

        protected void addingAssignments(Collection<? extends T> assignments) {
            setParentFor(assignments);
            addAssignments(assignments);
            clearCachedData();
        }

        protected void clearCachedData() {
            dayAssignmentsOrdered = null;
        }

        private void setParentFor(Collection<? extends T> assignments) {
            for (T each : assignments) {
                setItselfAsParentFor(each);
            }
        }

        protected void removingAssignments(
                List<? extends DayAssignment> assignments){
            removeAssignments(assignments);
            clearCachedData();
            for (DayAssignment each : assignments) {
                dayAssignmenteRemoval.onRemoval(ResourceAllocation.this, each);
            }
        }

        protected abstract void removeAssignments(
                List<? extends DayAssignment> assignments);

        protected abstract void addAssignments(
                Collection<? extends T> assignments);

        @SuppressWarnings("unchecked")
        public void mergeAssignments(ResourceAllocation<?> modification) {
            detachAssignments();
            resetTo(((ResourceAllocation<T>) modification).getAssignments());
            clearCachedData();
        }

        protected abstract void resetTo(Collection<T> assignmentsCopied);

        void detachAssignments() {
            for (DayAssignment each : getUnorderedAssignments()) {
                each.detach();
            }
        }

        final protected DayAssignmentsState switchTo(Scenario scenario) {
            DayAssignmentsState result = explicitlySpecifiedState(scenario);
            copyTransientPropertiesIfAppropiateTo(result);
            return result;
        }

        /**
         * Override if necessary to do extra actions
         */
        protected void copyTransientPropertiesIfAppropiateTo(
                DayAssignmentsState newStateForScenario) {
        }
    }

    protected abstract void setItselfAsParentFor(T dayAssignment);

    private class TransientState extends DayAssignmentsState {

        private final Set<T> assignments;

        private IntraDayDate intraDayStart;

        private IntraDayDate intraDayEnd;

        TransientState(Collection<? extends T> assignments) {
            this.assignments = new HashSet<T>(assignments);
        }

        @Override
        final protected Collection<T> getUnorderedAssignments() {
            return assignments;
        }

        @Override
        final protected void removeAssignments(
                List<? extends DayAssignment> assignments) {
            this.assignments.removeAll(assignments);
        }

        @Override
        final protected void addAssignments(Collection<? extends T> assignments) {
            this.assignments.addAll(assignments);
        }

        @Override
        final protected void resetTo(Collection<T> assignments) {
            this.assignments.clear();
            this.assignments.addAll(assignments);
        }

        @Override
        public IntraDayDate getIntraDayStart() {
            return intraDayStart;
        }

        @Override
        public void setIntraDayStart(IntraDayDate intraDayStart) {
            this.intraDayStart = intraDayStart;
        }

        @Override
        final IntraDayDate getIntraDayEnd() {
            return intraDayEnd;
        }

        @Override
        public final void setIntraDayEnd(IntraDayDate intraDayEnd) {
            this.intraDayEnd = intraDayEnd;
        }

        protected void copyTransientPropertiesIfAppropiateTo(
                DayAssignmentsState newStateForScenario) {
            newStateForScenario.resetTo(getUnorderedAssignments());
            newStateForScenario.setIntraDayStart(getIntraDayStart());
            newStateForScenario.setIntraDayEnd(getIntraDayEnd());
        };

    }

    private DayAssignmentsState explicitlySpecifiedState(Scenario scenario) {
        IDayAssignmentsContainer<T> container;
        container = retrieveOrCreateContainerFor(scenario);
        return new ExplicitlySpecifiedScenarioState(container);
    }

    protected abstract IDayAssignmentsContainer<T> retrieveContainerFor(
            Scenario scenario);

    protected abstract IDayAssignmentsContainer<T> retrieveOrCreateContainerFor(
            Scenario scenario);
    /**
     * It uses the current scenario retrieved from {@link IScenarioManager} in
     * order to return the assignments for that scenario. This state doesn't
     * allow to update the current assignments for that scenario.<br />
     * Note that this implementation doesn't work well if the current scenario
     * is changed since the assignments are cached and the assignments for the
     * previous one would be returned<br />
     */
    private class NoExplicitlySpecifiedScenario extends
            DayAssignmentsState {

        @Override
        protected final void removeAssignments(
                List<? extends DayAssignment> assignments) {
            modificationsNotAllowed();
        }

        @Override
        protected final void addAssignments(Collection<? extends T> assignments) {
            modificationsNotAllowed();
        }

        @Override
        final void detachAssignments() {
            modificationsNotAllowed();
        }

        @Override
        protected final void resetTo(Collection<T> assignmentsCopied) {
            modificationsNotAllowed();
        }

        private void modificationsNotAllowed() {
            throw new IllegalStateException(
                    "modifications to assignments can't be done "
                            + "if the scenario on which to work on is not explicitly specified");
        }

        @Override
        protected Collection<T> getUnorderedAssignments() {
            Scenario scenario = currentScenario();
            return retrieveOrCreateContainerFor(scenario).getDayAssignments();
        }

        private Scenario currentScenario() {
            return Registry.getScenarioManager().getCurrent();
        }

        @Override
        IntraDayDate getIntraDayStart() {
            return retrieveContainerFor(currentScenario()).getIntraDayStart();
        }

        @Override
        IntraDayDate getIntraDayEnd() {
            return retrieveOrCreateContainerFor(currentScenario())
                    .getIntraDayEnd();
        }

        @Override
        public void setIntraDayEnd(IntraDayDate intraDayEnd) {
            modificationsNotAllowed();
        }

        @Override
        public void setIntraDayStart(IntraDayDate intraDayStart) {
            modificationsNotAllowed();
        }

    }

    private class ExplicitlySpecifiedScenarioState extends
            DayAssignmentsState {

        private final IDayAssignmentsContainer<T> container;

        ExplicitlySpecifiedScenarioState(IDayAssignmentsContainer<T> container) {
            Validate.notNull(container);
            this.container = container;
        }

        @Override
        protected void addAssignments(Collection<? extends T> assignments) {
            container.addAll(assignments);
        }

        @Override
        protected Collection<T> getUnorderedAssignments() {
            return container.getDayAssignments();
        }

        @Override
        protected void removeAssignments(
                List<? extends DayAssignment> assignments) {
            container.removeAll(assignments);
        }

        @Override
        protected void resetTo(Collection<T> assignmentsCopied) {
            container.resetTo(assignmentsCopied);
        }

        @Override
        IntraDayDate getIntraDayStart() {
            return container.getIntraDayStart();
        }

        @Override
        public void setIntraDayStart(IntraDayDate intraDayStart) {
            container.setIntraDayStart(intraDayStart);
        }

        @Override
        IntraDayDate getIntraDayEnd() {
            return container.getIntraDayEnd();
        }

        @Override
        public void setIntraDayEnd(IntraDayDate intraDayEnd) {
            container.setIntraDayEnd(intraDayEnd);
        }

    }

    public int getConsolidatedHours() {
        return DayAssignment.sum(getConsolidatedAssignments()).roundToHours();
    }

    public int getNonConsolidatedHours() {
        return DayAssignment.sum(getNonConsolidatedAssignments())
                .roundToHours();
    }

    /**
     * @return a list of {@link DayAssignment} ordered by date
     */
    public final List<T> getAssignments() {
        return getDayAssignmentsState().getOrderedDayAssignments();
    }

    public List<T> getNonConsolidatedAssignments() {
        return getDayAssignmentsByConsolidated(false);
    }

    public List<T> getConsolidatedAssignments() {
        return getDayAssignmentsByConsolidated(true);
    }

    private List<T> getDayAssignmentsByConsolidated(
            boolean consolidated) {
        List<T> result = new ArrayList<T>();
        for (T day : getAssignments()) {
            if (day.isConsolidated() == consolidated) {
                result.add(day);
            }
        }
        return result;
    }

    public ResourcesPerDay getNonConsolidatedResourcePerDay() {
        return calculateResourcesPerDayFromAssignments(getNonConsolidatedAssignments());
    }

    /**
     * Returns the last valid specified resources per day
     */
    protected ResourcesPerDay getIntendedResourcesPerDay() {
        return getResourcesPerDay();
    }

    public ResourcesPerDay getConsolidatedResourcePerDay() {
        return calculateResourcesPerDayFromAssignments(getConsolidatedAssignments());
    }

    // just called for validation purposes. It must be public, otherwise if it's
    // a proxy the call is not intercepted.
    @NotNull
    public ResourcesPerDay getRawResourcesPerDay() {
        return resourcesPerDay;
    }

    public ResourcesPerDay getResourcesPerDay() {
        if (resourcesPerDay == null) {
            return ResourcesPerDay.amount(0);
        }
        return resourcesPerDay;
    }

    public void createDerived(IWorkerFinder finder) {
        final List<? extends DayAssignment> assignments = getAssignments();
        List<DerivedAllocation> result = new ArrayList<DerivedAllocation>();
        List<Machine> machines = Resource.machines(getAssociatedResources());
        for (Machine machine : machines) {
            for (MachineWorkersConfigurationUnit each : machine
                    .getConfigurationUnits()) {
                result.add(DerivedAllocationGenerator.generate(this, finder,
                        each,
                        assignments));
            }
        }
        resetDerivedAllocationsTo(result);
    }

    /**
     * Resets the derived allocations
     */
    private void resetDerivedAllocationsTo(
            Collection<DerivedAllocation> derivedAllocations) {
        // avoiding error: A collection with cascade="all-delete-orphan" was no
        // longer referenced by the owning entity instance
        this.derivedAllocations.clear();
        this.derivedAllocations.addAll(derivedAllocations);
    }

    public Set<DerivedAllocation> getDerivedAllocations() {
        return Collections.unmodifiableSet(derivedAllocations);
    }

    public LocalDate getStartConsideringAssignments() {
        List<? extends DayAssignment> assignments = getAssignments();
        if (assignments.isEmpty()) {
            return getStartDate();
        }
        return assignments.get(0).getDay();
    }

    public LocalDate getStartDate() {
        IntraDayDate start = getIntraDayStartDate();
        return start != null ? start.getDate() : null;
    }

    private IntraDayDate getStartSpecifiedByTask() {
        IntraDayDate taskStart = task.getIntraDayStartDate();
        IntraDayDate firstDayNotConsolidated = getTask()
                .getFirstDayNotConsolidated();
        return IntraDayDate.max(taskStart, firstDayNotConsolidated);
    }

    public IntraDayDate getIntraDayStartDate() {
        IntraDayDate intraDayStart = getDayAssignmentsState()
                .getIntraDayStart();
        if (intraDayStart != null) {
            return intraDayStart;
        }
        return task.getIntraDayStartDate();
    }

    public LocalDate getEndDate() {
        IntraDayDate intraDayEndDate = getIntraDayEndDate();
        return intraDayEndDate != null ? intraDayEndDate.asExclusiveEnd()
                : null;
    }

    public IntraDayDate getIntraDayEndDate() {
        IntraDayDate intraDayEnd = getDayAssignmentsState().getIntraDayEnd();
        if (intraDayEnd != null) {
            return intraDayEnd;
        }

        LocalDate l = getEndDateGiven(getAssignments());
        if (l == null) {
            return task.getIntraDayEndDate();
        }
        return IntraDayDate.startOfDay(l);
    }

    private LocalDate getEndDateGiven(
            List<? extends DayAssignment> assignments) {
        if (assignments.isEmpty()) {
            return null;
        }
        DayAssignment lastAssignment = assignments.get(assignments.size() - 1);
        return lastAssignment.getDay().plusDays(1);
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
        return getAssignedEffort(resource, start, endExclusive).roundToHours();
    }

    public EffortDuration getAssignedEffort(final Resource resource,
            LocalDate start, LocalDate endExclusive) {
        return getAssignedDuration(
                filter(getAssignments(start, endExclusive),
                        new PredicateOnDayAssignment() {
                            @Override
                            public boolean satisfiedBy(
                                    DayAssignment dayAssignment) {
                                return dayAssignment.isAssignedTo(resource);
                            }
                }));
    }

    public List<DayAssignment> getAssignments(LocalDate start,
            LocalDate endExclusive) {
        return new ArrayList<DayAssignment>(DayAssignment.getAtInterval(
                getAssignments(), start, endExclusive));
    }

    public int getAssignedHours(LocalDate start, LocalDate endExclusive) {
        return getAssignedDuration(start, endExclusive).roundToHours();
    }

    public abstract EffortDuration getAssignedEffort(ICriterion criterion, LocalDate start,
            LocalDate endExclusive);

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

    protected EffortDuration getAssignedDuration(LocalDate startInclusive,
            LocalDate endExclusive) {
        return getAssignedDuration(getAssignments(startInclusive, endExclusive));
    }

    private EffortDuration getAssignedDuration(
            List<? extends DayAssignment> assignments) {
        EffortDuration result = zero();
        for (DayAssignment dayAssignment : assignments) {
            result = result.plus(dayAssignment.getDuration());
        }
        return result;
    }

    public void mergeAssignmentsAndResourcesPerDay(Scenario scenario,
            ResourceAllocation<?> modifications) {
        if (modifications == this) {
            return;
        }
        switchToScenario(scenario);
        mergeAssignments(modifications);
        if (modifications.isSatisfied()) {
            updateOriginalTotalAssigment();
            updateResourcesPerDay();
        }
        setWithoutApply(modifications.getAssignmentFunction());
        mergeDerivedAllocations(scenario, modifications.getDerivedAllocations());
    }

    private void mergeDerivedAllocations(Scenario scenario,
            Set<DerivedAllocation> derivedAllocations) {
        Map<MachineWorkersConfigurationUnit, DerivedAllocation> newMap = DerivedAllocation
                .byConfigurationUnit(derivedAllocations);
        Map<MachineWorkersConfigurationUnit, DerivedAllocation> currentMap = DerivedAllocation
                .byConfigurationUnit(getDerivedAllocations());
        for (Entry<MachineWorkersConfigurationUnit, DerivedAllocation> entry : newMap
                .entrySet()) {
            final MachineWorkersConfigurationUnit key = entry.getKey();
            final DerivedAllocation modification = entry.getValue();
            DerivedAllocation current = currentMap.get(key);
            if (current == null) {
                DerivedAllocation derived = modification.asDerivedFrom(this);
                derived.useScenario(scenario);
                currentMap.put(key, derived);
            } else {
                current.useScenario(scenario);
                current.resetAssignmentsTo(modification.getAssignments());
            }
        }
        resetDerivedAllocationsTo(currentMap.values());
    }

    final void mergeAssignments(ResourceAllocation<?> modifications) {
        getDayAssignmentsState().mergeAssignments(modifications);
        getDayAssignmentsState().setIntraDayStart(
                modifications.getDayAssignmentsState().getIntraDayStart());
        getDayAssignmentsState().setIntraDayEnd(
                modifications.getDayAssignmentsState().getIntraDayEnd());
    }

    public void detach() {
        getDayAssignmentsState().detachAssignments();
    }

    void associateAssignmentsToResource() {
        for (DayAssignment dayAssignment : getAssignments()) {
            dayAssignment.associateToResource();
        }
    }

    public boolean hasAssignments() {
        return !getAssignments().isEmpty();
    }

    public LimitingResourceQueueElement getLimitingResourceQueueElement() {
        return (!limitingResourceQueueElements.isEmpty()) ? (LimitingResourceQueueElement) limitingResourceQueueElements.iterator().next() : null;
    }

    public void setLimitingResourceQueueElement(LimitingResourceQueueElement element) {
        limitingResourceQueueElements.clear();
        if (element != null) {
            element.setResourceAllocation(this);
            limitingResourceQueueElements.add(element);
        }
    }

    public Integer getIntendedTotalHours() {
        return intendedTotalHours;
    }

    public void setIntendedTotalHours(Integer intendedTotalHours) {
        this.intendedTotalHours = intendedTotalHours;
    }

    /**
     * Do a query to recover a list of resources that are suitable for this
     * allocation. For a {@link SpecificResourceAllocation} returns the current
     * resource. For a {@link GenericResourceAllocation} returns the resources
     * that currently match this allocation criterions
     * @return a list of resources that are proper for this allocation
     */
    public abstract List<Resource> querySuitableResources(IResourceDAO resourceDAO);

    public abstract void makeAssignmentsContainersDontPoseAsTransientAnyMore();

    public void removePredecessorsDayAssignmentsFor(Scenario scenario) {
        for (DerivedAllocation each : getDerivedAllocations()) {
            each.removePredecessorContainersFor(scenario);
        }
        removePredecessorContainersFor(scenario);
    }

    protected abstract void removePredecessorContainersFor(Scenario scenario);

    public void removeDayAssigmentsFor(Scenario scenario) {
        for (DerivedAllocation each : getDerivedAllocations()) {
            each.removeContainersFor(scenario);
        }
        removeContainersFor(scenario);
    }

    protected abstract void removeContainersFor(Scenario scenario);

}
