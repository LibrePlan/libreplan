/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.business.planner.entities;

import static java.util.Collections.emptyList;
import static org.libreplan.business.workingday.EffortDuration.min;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Valid;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;
import org.libreplan.business.calendars.entities.ICalendar;
import org.libreplan.business.calendars.entities.SameWorkHoursEveryDay;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.entities.AggregatedHoursGroup;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.SumChargedEffort;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.planner.entities.AssignedEffortForResource.IAssignedEffortForResource;
import org.libreplan.business.planner.entities.AssignedEffortForResource.WithTheLoadOf;
import org.libreplan.business.planner.entities.DayAssignment.FilterType;
import org.libreplan.business.planner.entities.Dependency.Type;
import org.libreplan.business.planner.entities.DerivedAllocationGenerator.IWorkerFinder;
import org.libreplan.business.planner.entities.ResourceAllocation.Direction;
import org.libreplan.business.planner.entities.allocationalgorithms.AllocationModification;
import org.libreplan.business.planner.entities.allocationalgorithms.EffortModification;
import org.libreplan.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.libreplan.business.planner.entities.consolidations.Consolidation;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.util.TaskElementVisitor;
import org.libreplan.business.util.deepcopy.AfterCopy;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workingday.ResourcesPerDay;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class Task extends TaskElement implements ITaskPositionConstrained {

    private static final Log LOG = LogFactory.getLog(Task.class);

    /**
     * Maximum number of days in order to looking for calendar capacity (defined
     * to 5 years)
     */
    private static int MAX_DAYS_LOOKING_CAPACITY = 360 * 5;

    public static Task createTask(TaskSource taskSource) {
        Task task = new Task();
        OrderElement orderElement = taskSource.getOrderElement();
        orderElement.applyInitialPositionConstraintTo(task);
        Task result = create(task, taskSource);
        result.initializeDates();
        return result;
    }

    @Override
    protected void initializeDates() {
        EffortDuration workHours = EffortDuration.hours(getWorkHours());
        DurationBetweenDates duration = fromFixedDuration(workHours);

        IntraDayDate start = getIntraDayStartDate();
        if (start != null) {
            setIntraDayEndDate(duration.fromStartToEnd(start));
        } else {
            IntraDayDate end = getIntraDayEndDate();
            setIntraDayStartDate(duration.fromEndToStart(end));
        }
    }

    /**
     * Calculates end date for a task, starting from start until fulfilling
     * number of hours.
     *
     * For tasks with limiting resources it's needed to resize a task if the
     * number of hours allocated to a resource changes. In non limiting
     * resources, the task is resized because when the number of hours changes,
     * new days assignments are generated, and then the task is resized
     * accordingly.
     *
     * @param hours
     */
    public void resizeToHours(int hours) {
        Validate.isTrue(isLimiting());
        EffortDuration workHours = EffortDuration.hours(hours);
        DurationBetweenDates duration = new DurationBetweenDates(0, workHours);
        setIntraDayEndDate(duration.fromStartToEnd(getIntraDayStartDate()));
    }

    private CalculatedValue calculatedValue = CalculatedValue.END_DATE;

    private TaskStatusEnum currentStatus = null;

    private Set<ResourceAllocation<?>> resourceAllocations = new HashSet<ResourceAllocation<?>>();

    @Valid
    private Set<ResourceAllocation<?>> getResourceAlloations() {
        return new HashSet<ResourceAllocation<?>>(resourceAllocations);
    }

    @SuppressWarnings("unused")
    @AfterCopy
    private void ifLimitingAllocationRemove() {
        if (isLimiting()) {
            resourceAllocations.clear();
        }
    }

    private TaskPositionConstraint positionConstraint = new TaskPositionConstraint();

    private SubcontractedTaskData subcontractedTaskData;

    private Integer priority;

    private Consolidation consolidation;

    private Integer workableDays;

    private Direction lastAllocationDirection = Direction.FORWARD;

    /**
     * Constructor for hibernate. Do not use!
     */
    public Task() {

    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "element associated to a task must be not empty")
    private boolean theOrderElementMustBeNotNull() {
        return getOrderElement() != null;
    }


    public HoursGroup getHoursGroup() {
        return getTaskSource().getHoursGroups().iterator().next();
    }

    public Set<Criterion> getCriterions() {
        return Collections
                .unmodifiableSet(getHoursGroup().getValidCriterions());
    }

    public Integer getHoursSpecifiedAtOrder() {
        return getWorkHours();
    }

    public int getAssignedHours() {
        return AggregateOfResourceAllocations.createFromSatisfied(resourceAllocations)
                .getTotalHours();
    }

    public EffortDuration getAssignedEffort() {
        return AggregateOfResourceAllocations.createFromSatisfied(
                resourceAllocations).getTotalEffort();
    }

    private EffortDuration getTotalNonConsolidatedEffort() {
        return AggregateOfResourceAllocations
                .createFromAll(resourceAllocations).getNonConsolidatedEffort();
    }

    public int getTotalHours() {
        return (getTaskSource() != null) ? getTaskSource().getTotalHours() : 0;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public List<TaskElement> getChildren() {
        return Collections.emptyList();
    }

    public Set<ResourceAllocation<?>> getSatisfiedResourceAllocations() {
        Set<ResourceAllocation<?>> result = new HashSet<ResourceAllocation<?>>();

        if (isLimiting()) {
            result.addAll(getLimitingResourceAllocations());
        } else {
            result.addAll(ResourceAllocation
                    .getSatisfied(resourceAllocations));
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Set<ResourceAllocation<?>> getAllResourceAllocations() {
        return Collections.unmodifiableSet(resourceAllocations);
    }

    public Set<ResourceAllocation<?>> getLimitingResourceAllocations() {
        Set<ResourceAllocation<?>> result = new HashSet<ResourceAllocation<?>>();
        for (ResourceAllocation<?> each: resourceAllocations) {
            if (each.isLimiting()) {
                result.add(each);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public Set<ResourceAllocation<?>> getNonLimitingResourceAllocations() {
        Set<ResourceAllocation<?>> result = new HashSet<ResourceAllocation<?>>();
        for (ResourceAllocation<?> each: resourceAllocations) {
            if (!each.isLimiting()) {
                result.add(each);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public boolean isLimiting() {
        return !(getLimitingResourceAllocations().isEmpty());
    }

    private ResourceAllocation<?> getAssociatedLimitingResourceAllocation() {
        Set<ResourceAllocation<?>> resourceAllocations = getLimitingResourceAllocations();
        return (resourceAllocations.size() > 0) ? resourceAllocations.iterator().next() : null;
    }

    public LimitingResourceQueueElement getAssociatedLimitingResourceQueueElementIfAny() {
        if (!isLimiting()) {
            throw new IllegalStateException("this is not a limiting task");
        }
        return getAssociatedLimitingResourceAllocation()
                .getLimitingResourceQueueElement();
    }

    public boolean isLimitingAndHasDayAssignments() {
        ResourceAllocation<?> resourceAllocation = getAssociatedLimitingResourceAllocation();
        return resourceAllocation != null
                && resourceAllocation.isLimitingAndHasDayAssignments();
    }

    public void addResourceAllocation(ResourceAllocation<?> resourceAllocation) {
        addResourceAllocation(resourceAllocation, true);
    }

    public void addResourceAllocation(ResourceAllocation<?> resourceAllocation,
            boolean generateDayAssignments) {
        if (!resourceAllocation.getTask().equals(this)) {
            throw new IllegalArgumentException(
                    "the resourceAllocation's task must be this task");
        }
        resourceAllocations.add(resourceAllocation);
        if (generateDayAssignments) {
            resourceAllocation.associateAssignmentsToResource();
        }
    }

    public ResourceAllocation<?> getResourceAllocation() {
        Validate.isTrue(isLimiting());
        return resourceAllocations.isEmpty() ? null : resourceAllocations.iterator().next();
    }

    public void setResourceAllocation(ResourceAllocation<?> resourceAllocation) {
        Validate.isTrue(resourceAllocation.isLimiting());
        removeAllResourceAllocations();
        resourceAllocations.add(resourceAllocation);
    }

    public void removeResourceAllocation(
            ResourceAllocation<?> resourceAllocation) {
        resourceAllocation.detach();
        resourceAllocations.remove(resourceAllocation);
    }

    public CalculatedValue getCalculatedValue() {
        if (calculatedValue == null) {
            return CalculatedValue.END_DATE;
        }
        return calculatedValue;
    }

    public void setCalculatedValue(CalculatedValue calculatedValue) {
        Validate.notNull(calculatedValue);
        this.calculatedValue = calculatedValue;
    }

    /**
     * Checks if there isn't any {@link Worker} repeated in the {@link Set} of
     * {@link ResourceAllocation} of this {@link Task}.
     * @return <code>true</code> if the {@link Task} is valid, that means there
     *         isn't any {@link Worker} repeated.
     */
    public boolean isValidResourceAllocationWorkers() {
        Set<Long> workers = new HashSet<Long>();

        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                Resource resource = ((SpecificResourceAllocation) resourceAllocation)
                        .getResource();
                if (resource != null) {
                    if (workers.contains(resource.getId())) {
                        return false;
                    } else {
                        workers.add(resource.getId());
                    }
                }
            }
        }

        return true;
    }

    public Set<GenericResourceAllocation> getGenericResourceAllocations() {
        return new HashSet<GenericResourceAllocation>(ResourceAllocation
                .getOfType(GenericResourceAllocation.class,
                        getSatisfiedResourceAllocations()));
    }

    public Set<SpecificResourceAllocation> getSpecificResourceAllocations() {
        return new HashSet<SpecificResourceAllocation>(ResourceAllocation
                .getOfType(SpecificResourceAllocation.class,
                        getSatisfiedResourceAllocations()));
    }

    public static class ModifiedAllocation {

        public static List<ModifiedAllocation> copy(Scenario onScenario,
                Collection<ResourceAllocation<?>> resourceAllocations) {
            List<ModifiedAllocation> result = new ArrayList<ModifiedAllocation>();
            for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
                result.add(new ModifiedAllocation(resourceAllocation,
                        resourceAllocation.copy(onScenario)));
            }
            return result;
        }

        public static List<ResourceAllocation<?>> modified(
                Collection<? extends ModifiedAllocation> collection) {
            List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
            for (ModifiedAllocation modifiedAllocation : collection) {
                result.add(modifiedAllocation.getModification());
            }
            return result;
        }

        public static List<ResourceAllocation<?>> originals(
                Collection<? extends ModifiedAllocation> modifiedAllocations) {
            List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
            for (ModifiedAllocation each : modifiedAllocations) {
                result.add(each.getOriginal());
            }
            return result;
        }

        private final ResourceAllocation<?> original;

        private final ResourceAllocation<?> modification;

        public ModifiedAllocation(ResourceAllocation<?> original,
                ResourceAllocation<?> modification) {
            Validate.notNull(original);
            Validate.notNull(modification);
            this.original = original;
            this.modification = modification;
        }

        public ResourceAllocation<?> getOriginal() {
            return original;
        }

        public ResourceAllocation<?> getModification() {
            return modification;
        }

    }

    public void mergeAllocation(Scenario scenario, final IntraDayDate start,
            final IntraDayDate end, Integer newWorkableDays,
            CalculatedValue calculatedValue,
            List<ResourceAllocation<?>> newAllocations,
            List<ModifiedAllocation> modifications,
            Collection<? extends ResourceAllocation<?>> toRemove) {
        this.calculatedValue = calculatedValue;
        this.workableDays = calculatedValue == CalculatedValue.END_DATE ? null
                : newWorkableDays;
        setIntraDayStartDate(start);
        setIntraDayEndDate(end);
        for (ModifiedAllocation pair : modifications) {
            Validate.isTrue(resourceAllocations.contains(pair.getOriginal()));
            pair.getOriginal().mergeAssignmentsAndResourcesPerDay(scenario,
                    pair.getModification());
        }
        remove(toRemove);
        addAllocations(scenario, newAllocations);
    }

    private void remove(Collection<? extends ResourceAllocation<?>> toRemove) {
        for (ResourceAllocation<?> resourceAllocation : toRemove) {
            removeResourceAllocation(resourceAllocation);
        }
    }

    private void addAllocations(Scenario scenario,
            List<ResourceAllocation<?>> newAllocations) {
        for (ResourceAllocation<?> resourceAllocation : newAllocations) {
            resourceAllocation.switchToScenario(scenario);
            addResourceAllocation(resourceAllocation);
        }
    }

    public void explicityMoved(IntraDayDate startDate, IntraDayDate endDate) {
        getPositionConstraint().explicityMovedTo(startDate, endDate,
                getOrderElement().getOrder().getSchedulingMode());
    }

    public TaskPositionConstraint getPositionConstraint() {
        if (positionConstraint == null) {
            positionConstraint = new TaskPositionConstraint();
        }
        return positionConstraint;
    }

    private static class ModificationsResult<T extends AllocationModification> {

        static <T extends AllocationModification> ModificationsResult<T> create(
                List<ResourceAllocation<?>> original, List<T> canBeModified) {

            List<ResourceAllocation<?>> beingModified = AllocationModification
                    .getBeingModified(canBeModified);
            List<ResourceAllocation<?>> noLongerValid = new ArrayList<ResourceAllocation<?>>();
            for (ResourceAllocation<?> each : original) {
                if (!beingModified.contains(each)) {
                    noLongerValid.add(each);
                }
            }
            return new ModificationsResult<T>(canBeModified, noLongerValid);
        }

        private final List<T> valid;

        private final List<ResourceAllocation<?>> noLongerValid;

        private ModificationsResult(List<T> valid, List<ResourceAllocation<?>> noLongerValid) {
            this.valid = Collections.unmodifiableList(valid);
            this.noLongerValid = Collections.unmodifiableList(noLongerValid);
        }

        public List<T> getBeingModified() {
            return valid;
        }

        public List<ResourceAllocation<?>> getNoLongerValid() {
            return noLongerValid;
        }

    }

    private static class WithPotentiallyNewResources {

        protected final IResourcesSearcher searcher;

        public WithPotentiallyNewResources(IResourcesSearcher searcher) {
            Validate.notNull(searcher);
            this.searcher = searcher;
        }

        public ModificationsResult<EffortModification> getHoursModified(
                List<ResourceAllocation<?>> allocations) {
            List<EffortModification> canBeModified = EffortModification
                    .withNewResources(allocations, searcher);
            return ModificationsResult.create(allocations, canBeModified);
        }

        public ModificationsResult<ResourcesPerDayModification> getResourcesPerDayModified(
                List<ResourceAllocation<?>> allocations) {
            List<ResourcesPerDayModification> canBeModified = ResourcesPerDayModification
                    .withNewResources(allocations, searcher);
            return ModificationsResult.create(allocations, canBeModified);
        }

    }

    public void copyAssignmentsFromOneScenarioToAnother(Scenario from, Scenario to) {
        for (ResourceAllocation<?> each : getAllResourceAllocations()) {
            each.copyAssignmentsFromOneScenarioToAnother(from, to);
        }
    }

    @Override
    protected IDatesHandler createDatesHandler(final Scenario scenario,
            final IResourcesSearcher searcher) {
        return new IDatesHandler() {

            @Override
            public void moveTo(IntraDayDate newStartDate) {
                IntraDayDate previousStart = getIntraDayStartDate();
                if (previousStart.equals(newStartDate)) {
                    return;
                }
                setIntraDayEndDate(calculateEndKeepingLength(newStartDate));
                setIntraDayStartDate(newStartDate);
                doReassignment(Direction.FORWARD);
            }

            private void doReassignment(Direction direction) {
                reassign(scenario, direction, new WithPotentiallyNewResources(
                        searcher));
            }

            @Override
            public void moveEndTo(IntraDayDate newEnd) {
                if (getIntraDayEndDate().equals(newEnd)) {
                    return;
                }
                setIntraDayStartDate(calculateNewStartGivenEnd(newEnd));
                setIntraDayEndDate(newEnd);
                doReassignment(Direction.BACKWARD);
            }

            private IntraDayDate calculateNewStartGivenEnd(IntraDayDate newEnd) {
                return calculateStartKeepingLength(newEnd);
            }

            @Override
            public void resizeTo(IntraDayDate endDate) {
                if (!canBeResized() || getIntraDayEndDate().equals(endDate)) {
                    return;
                }
                setIntraDayEndDate(endDate);
                updateWorkableDays();
                doReassignment(getAllocationDirection());
            }

            private void updateWorkableDays() {
                assert calculatedValue != CalculatedValue.END_DATE;
                workableDays = getWorkableDaysBetweenDates();
            }

        };
    }

    public IntraDayDate calculateEndKeepingLength(IntraDayDate newStartDate) {
        DurationBetweenDates durationBetweenDates = getDurationBetweenDates();
        return durationBetweenDates.fromStartToEnd(newStartDate);
    }

    private IntraDayDate calculateStartKeepingLength(IntraDayDate newEnd) {
        DurationBetweenDates durationBetweenDates = getDurationBetweenDates();
        return durationBetweenDates.fromEndToStart(newEnd);
    }

    private DurationBetweenDates getDurationBetweenDates() {
        if (workableDays != null) {
            return fromFixedDuration(workableDays);
        } else {
            return fromCurrentDuration();
        }
    }

    private DurationBetweenDates fromFixedDuration(int fixedNumberOfWorkableDays) {
        return new DurationBetweenDates(fixedNumberOfWorkableDays,
                EffortDuration.zero());
    }

    private DurationBetweenDates fromCurrentDuration() {
        IntraDayDate start = getIntraDayStartDate();
        IntraDayDate end = getIntraDayEndDate();
        int calculatedWorkableDays = getWorkableDaysFrom(start.roundUp(),
                end.roundDown());
        EffortDuration extraDuration = getExtraDurationAtStart(start).plus(
                end.getEffortDuration());
        return new DurationBetweenDates(calculatedWorkableDays, extraDuration);
    }

    private EffortDuration getExtraDurationAtStart(IntraDayDate start) {
        if (start.getEffortDuration().isZero()) {
            return EffortDuration.zero();
        }
        ICalendar calendar = getNullSafeCalendar();
        EffortDuration capacity = calendar.getCapacityOn(PartialDay
                .wholeDay(start.getDate()));
        return capacity.minus(min(start.getEffortDuration(), capacity));
    }

    private ICalendar getNullSafeCalendar() {
        return getCalendar() != null ? getCalendar() : SameWorkHoursEveryDay
                .getDefaultWorkingDay();
    }

    private DurationBetweenDates fromFixedDuration(EffortDuration duration) {
        return new DurationBetweenDates(0, duration);
    }

    private class DurationBetweenDates {

        private final int numberOfWorkableDays;

        private final EffortDuration remainderDuration;

        private final ICalendar calendar;

        private DurationBetweenDates(int numberOfWorkableDays,
                EffortDuration remainderDuration) {
            this.numberOfWorkableDays = numberOfWorkableDays;
            this.remainderDuration = remainderDuration;
            this.calendar = getNullSafeCalendar();

        }

        public IntraDayDate fromStartToEnd(IntraDayDate newStartDate) {
            LocalDate resultDay = calculateEndGivenWorkableDays(
                    newStartDate.getDate(), numberOfWorkableDays);
            return plusDuration(IntraDayDate.startOfDay(resultDay),
                    remainderDuration.plus(newStartDate.getEffortDuration()));
        }

        private IntraDayDate plusDuration(IntraDayDate start,
                EffortDuration remaining) {
            IntraDayDate result = IntraDayDate.startOfDay(start.getDate());
            remaining = remaining.plus(start.getEffortDuration());
            EffortDuration originalRemaining = remaining;
            LocalDate startDate = start.getDate();
            LocalDate current = startDate;
            if (!canBeFulfilled(start, originalRemaining)) {
                return roughApproximationDueToNotFullfilingCalendar(startDate,
                        originalRemaining);
            }
            while (!remaining.isZero()) {
                EffortDuration capacity = calendar.getCapacityOn(PartialDay
                        .wholeDay(current));
                result = IntraDayDate.create(current, remaining);
                remaining = remaining.minus(min(capacity, remaining));
                current = current.plusDays(1);
                if (Days.daysBetween(startDate, current).getDays() > MAX_DAYS_LOOKING_CAPACITY) {
                    LOG.error("thereAreCapacityFor didn't detect that it didn't"
                            + " really have enough capacity to fulfill the required hours"
                            + " or this capacity is more than "
                            + MAX_DAYS_LOOKING_CAPACITY + " in the future");
                    return roughApproximationDueToNotFullfilingCalendar(
                            startDate, originalRemaining);
                }
            }
            return result;
        }

        private boolean canBeFulfilled(IntraDayDate start,
                EffortDuration originalRemaining) {
            AvailabilityTimeLine availability = AvailabilityTimeLine.allValid();
            availability.invalidUntil(start.getDate());
            return calendar.thereAreCapacityFor(availability,
                    ResourcesPerDay.amount(1), originalRemaining);
        }

        private IntraDayDate roughApproximationDueToNotFullfilingCalendar(
                LocalDate startDate,
                EffortDuration originalRemaining) {
            LOG.warn("Calendar " + calendar + " doesn't have enough capacity, "
                    + "using 8h per day to calculate end date for the task");
            return IntraDayDate.create(
                    startDate.plusDays(originalRemaining.getHours() / 8),
                    EffortDuration.zero());
        }

        public IntraDayDate fromEndToStart(IntraDayDate newEnd) {
            LocalDate resultDay = calculateStartGivenWorkableDays(
                    newEnd.getDate(), numberOfWorkableDays);
            return minusDuration(plusDuration(
                    IntraDayDate.startOfDay(resultDay),
                            newEnd.getEffortDuration()), remainderDuration);
        }

        private IntraDayDate minusDuration(IntraDayDate date,
                EffortDuration decrement) {
            IntraDayDate result = IntraDayDate.create(
                    date.getDate(),
                    date.getEffortDuration().minus(
                            min(decrement, date.getEffortDuration())));
            decrement = decrement
                    .minus(min(date.getEffortDuration(), decrement));
            LocalDate resultDay = date.getDate();
            while (!decrement.isZero()) {
                resultDay = resultDay.minusDays(1);
                EffortDuration capacity = calendar.getCapacityOn(PartialDay
                        .wholeDay(resultDay));
                result = IntraDayDate.create(resultDay,
                        capacity.minus(min(capacity, decrement)));
                decrement = decrement.minus(min(capacity, decrement));
            }
            return result;
        }
    }

    /**
     * The allocation direction in which the allocation must be done
     */
    public Direction getAllocationDirection() {
        if (lastAllocationDirection == null || hasConsolidations()) {
            return Direction.FORWARD;
        }
        return lastAllocationDirection;
    }

    public void reassignAllocationsWithNewResources(Scenario scenario,
            IResourcesSearcher searcher) {
        reassign(scenario, getAllocationDirection(),
                new WithPotentiallyNewResources(searcher));
    }

    private void reassign(Scenario onScenario, Direction direction,
            WithPotentiallyNewResources strategy) {
        try {
            this.lastAllocationDirection = direction;
            if (isLimiting()) {
                return;
            }
            List<ModifiedAllocation> copied = ModifiedAllocation.copy(onScenario,
                    getResourceAlloations());
            List<ResourceAllocation<?>> toBeModified = ModifiedAllocation
                    .modified(copied);
            if (toBeModified.isEmpty()) {
                return;
            }
            setCustomAssignedEffortForResource(copied);
            doAllocation(strategy, direction, toBeModified);
            updateDerived(copied);

            List<ResourceAllocation<?>> newAllocations = emptyList(), removedAllocations = emptyList();
            mergeAllocation(onScenario, getIntraDayStartDate(),
                    getIntraDayEndDate(), workableDays, calculatedValue,
                    newAllocations, copied, removedAllocations);
        } catch (Exception e) {
            LOG.error("reassignment for task: " + this
                    + " couldn't be completed", e);
        }
    }

    private void setCustomAssignedEffortForResource(
            List<ModifiedAllocation> modifiedAllocations) {
        List<ResourceAllocation<?>> originals = ModifiedAllocation
                .originals(modifiedAllocations);
        IAssignedEffortForResource discounting = AssignedEffortForResource
                .effortDiscounting(originals);
        List<ResourceAllocation<?>> beingModified = ModifiedAllocation
                .modified(modifiedAllocations);
        WithTheLoadOf allNewLoad = AssignedEffortForResource
                .withTheLoadOf(beingModified);
        List<GenericResourceAllocation> generic = ResourceAllocation.getOfType(
                GenericResourceAllocation.class, beingModified);
        for (GenericResourceAllocation each : generic) {
            each.setAssignedEffortForResource(AssignedEffortForResource.sum(
                    allNewLoad.withoutConsidering(each), discounting));
        }
    }

    private void doAllocation(WithPotentiallyNewResources strategy,
            Direction direction, List<ResourceAllocation<?>> toBeModified) {
        ModificationsResult<ResourcesPerDayModification> modificationsResult = strategy
                .getResourcesPerDayModified(toBeModified);
        markAsUnsatisfied(modificationsResult.getNoLongerValid());
        List<ResourcesPerDayModification> allocations = modificationsResult
                .getBeingModified();
        if (allocations.isEmpty()) {
            LOG.warn("all allocations for task " + this
                    + " have no valid data that could be used");
            return;
        }
        switch (calculatedValue) {
        case NUMBER_OF_HOURS:
            ResourceAllocation.allocating(allocations).allocateOnTaskLength();
            break;
        case END_DATE:
            IntraDayDate date = ResourceAllocation.allocating(allocations)
                    .untilAllocating(direction, getTotalNonConsolidatedEffort());
            if (direction == Direction.FORWARD) {
                setIntraDayEndDate(date);
            } else {
                setIntraDayStartDate(date);
            }
            break;
        case RESOURCES_PER_DAY:
            ModificationsResult<EffortModification> hoursModificationResult = strategy
                    .getHoursModified(toBeModified);
            markAsUnsatisfied(hoursModificationResult.getNoLongerValid());
            List<EffortModification> hoursModified = hoursModificationResult
                    .getBeingModified();
            if (hoursModified.isEmpty()) {
                LOG.warn("all allocations for task " + this + " can't be used");
                return;
            }
            ResourceAllocation.allocatingHours(hoursModified)
                              .allocateUntil(new LocalDate(getEndDate()));
            break;
        default:
            throw new RuntimeException("cant handle: " + calculatedValue);
        }

        AssignmentFunction.applyAssignmentFunctionsIfAny(toBeModified);
    }

    private void markAsUnsatisfied(
            Collection<? extends ResourceAllocation<?>> noLongerValid) {
        for (ResourceAllocation<?> each : noLongerValid) {
            each.markAsUnsatisfied();
        }
    }

    private void updateDerived(List<ModifiedAllocation> allocations) {
        for (ModifiedAllocation each : allocations) {
            ResourceAllocation<?> original = each.getOriginal();
            if (!original.getDerivedAllocations().isEmpty()) {
                IWorkerFinder workersFinder = createFromExistentDerivedAllocationsFinder(original);
                each.getModification().createDerived(workersFinder);
            }
        }
    }

    private IWorkerFinder createFromExistentDerivedAllocationsFinder(
            ResourceAllocation<?> original) {
        Set<DerivedAllocation> derivedAllocations = original
                .getDerivedAllocations();
        final Set<Worker> allWorkers = new HashSet<Worker>();
        for (DerivedAllocation each : derivedAllocations) {
            allWorkers.addAll(Resource.workers(each.getResources()));
        }
        return new IWorkerFinder() {

            @Override
            public Collection<Worker> findWorkersMatching(
                    Collection<? extends Criterion> requiredCriterions) {
                if (requiredCriterions.isEmpty()) {
                    return new ArrayList<Worker>();
                }
                Collection<Worker> result = new ArrayList<Worker>();
                for (Worker each : allWorkers) {
                    if (each.satisfiesCriterions(requiredCriterions)) {
                        result.add(each);
                    }
                }
                return result;
            }
        };
    }


    public List<AggregatedHoursGroup> getAggregatedByCriterions() {
        return getTaskSource().getAggregatedByCriterions();
    }

    public void setSubcontractedTaskData(SubcontractedTaskData subcontractedTaskData) {
        this.subcontractedTaskData = subcontractedTaskData;
    }

    @Valid
    public SubcontractedTaskData getSubcontractedTaskData() {
        return subcontractedTaskData;
    }

    public ExternalCompany getSubcontractedCompany() {
        return subcontractedTaskData.getExternalCompany();
    }

    public void removeAllSatisfiedResourceAllocations() {
        Set<ResourceAllocation<?>> resourceAllocations = getSatisfiedResourceAllocations();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            removeResourceAllocation(resourceAllocation);
        }
    }

    public void removeAllResourceAllocations() {
        for (Iterator<ResourceAllocation<?>> i = resourceAllocations.iterator(); i
                .hasNext();) {
            ResourceAllocation<?> each = i.next();
            removeResourceAllocation(each);
        }
    }

    public boolean isSubcontracted() {
        return (subcontractedTaskData != null);
    }

    public String getSubcontractionName() {
        return subcontractedTaskData.getExternalCompany().getName();
    }

    public boolean isSubcontractedAndWasAlreadySent() {
        return (subcontractedTaskData != null)
                && (!subcontractedTaskData.getState()
                        .equals(SubcontractState.PENDING_INITIAL_SEND));
    }

    public boolean hasSomeSatisfiedAllocation() {
        return !getSatisfiedResourceAllocations().isEmpty();
    }

    @Override
    protected boolean canBeResized() {
        return calculatedValue != CalculatedValue.END_DATE
                || resourceAllocations.isEmpty();
    }

    @Override
    public boolean canBeExplicitlyResized() {
        return canBeResized() && !isSubcontracted() && !isManualAnyAllocation();
    }

    @Override
    public boolean isMilestone() {
        return false;
    }

    public void removeSubcontractCommunicationDate() {
        if (subcontractedTaskData != null) {
            subcontractedTaskData.setSubcontractCommunicationDate(null);
        }
    }

    public boolean hasResourceAllocations() {
        return !resourceAllocations.isEmpty();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setConsolidation(Consolidation consolidation) {
        this.consolidation = consolidation;
    }

    @Valid
    public Consolidation getConsolidation() {
        return consolidation;
    }

    @Override
    public boolean hasLimitedResourceAllocation() {
        return !getLimitingResourceAllocations().isEmpty();

    }

    public boolean hasConsolidations() {
        return ((consolidation != null) && (!consolidation.isEmpty()));
    }

    public IntraDayDate getFirstDayNotConsolidated() {
        if (consolidation != null) {
            LocalDate until = consolidation.getConsolidatedUntil();
            if (until != null) {
                return IntraDayDate.startOfDay(until.plusDays(1));
            }
        }
        return getIntraDayStartDate();
    }

    public void updateAssignmentsConsolidatedValues() {
        for (ResourceAllocation<?> each : getAllResourceAllocations()) {
            each.updateAssignmentsConsolidatedValues();
        }
    }

    public Integer getWorkableDays() {
        if (workableDays == null) {
            return getWorkableDaysBetweenDates();
        }
        return workableDays;
    }

    public Integer getDaysBetweenDates() {
        Days daysBetween = Days.daysBetween(getStartAsLocalDate(),
                getIntraDayEndDate().asExclusiveEnd());
        return daysBetween.getDays();
    }

    public Integer getSpecifiedWorkableDays() {
        return workableDays;
    }

    private Integer getWorkableDaysBetweenDates() {
        LocalDate end = getIntraDayEndDate().asExclusiveEnd();
        return getWorkableDaysUntil(end);
    }

    public Integer getWorkableDaysUntil(LocalDate end) {
        return getWorkableDaysFrom(getStartAsLocalDate(), end);
    }

    public Integer getWorkableDaysFrom(LocalDate startInclusive,
            LocalDate endExclusive) {
        int result = 0;
        for (LocalDate current = startInclusive; current
                .compareTo(endExclusive) < 0; current = current
                .plusDays(1)) {
            if (isWorkable(current)) {
                result++;
            }
        }
        return result;
    }

    /* Older methods didn't consider until dates more recent than
     * task end date
     */
    public Integer getWorkableDaysFromLimitedByEndOfTheTask(LocalDate end) {
        return getWorkableDaysFromLimitedByEndOfTheTask(getStartAsLocalDate(), end);
    }

    public Integer getWorkableDaysFromLimitedByEndOfTheTask(LocalDate startInclusive,
            LocalDate endExclusive) {
        int result = 0;
        if(endExclusive.compareTo(this.getEndAsLocalDate()) > 0) {
            endExclusive = getIntraDayEndDate().asExclusiveEnd();
        }
        for (LocalDate current = startInclusive; current
                .compareTo(endExclusive) < 0; current = current
                .plusDays(1)) {
            if (isWorkable(current)) {
                result++;
            }
        }
        return result;
    }

    public LocalDate calculateEndGivenWorkableDays(int workableDays) {
        return calculateEndGivenWorkableDays(getIntraDayStartDate().getDate(),
                workableDays);
    }

    public LocalDate calculateStartGivenWorkableDays(int workableDays) {
        return calculateStartGivenWorkableDays(getEndAsLocalDate(),
                workableDays);
    }

    private LocalDate calculateEndGivenWorkableDays(LocalDate start,
            int workableDays) {
        LocalDate result = start;
        for (int i = 0; i < workableDays; result = result.plusDays(1)) {
            if (isWorkable(result)) {
                i++;
            }
        }
        return result;
    }

    private LocalDate calculateStartGivenWorkableDays(LocalDate end,
            int workableDays) {
        LocalDate result = end;
        for (int i = 0; i < workableDays; result = result.minusDays(1)) {
            if (isWorkable(result.minusDays(1))) {
                i++;
            }
        }
        return result;
    }

    private boolean isWorkable(LocalDate day) {
        ICalendar calendar = getCalendar();
        assert calendar != null;
        return !calendar.getCapacityOn(PartialDay.wholeDay(day)).isZero();
    }

    public static void convertOnStartInFixedDate(Task task) {
        TaskPositionConstraint taskConstraint = task.getPositionConstraint();
        if (taskConstraint.isValid(PositionConstraintType.START_IN_FIXED_DATE,
                task.getIntraDayStartDate())) {
            taskConstraint.update(PositionConstraintType.START_IN_FIXED_DATE,
                    task.getIntraDayStartDate());
        }
    }

    public boolean isManualAnyAllocation() {
        for (ResourceAllocation<?> each : resourceAllocations) {
            if (each.isManualAssignmentFunction()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isTask() {
        return true;
    }

    @Override
    public EffortDuration getTheoreticalCompletedTimeUntilDate(Date date) {
        return AggregateOfDayAssignments.createByDataRange(
                this.getDayAssignments(FilterType.KEEP_ALL),
                this.getStartDate(),
                date).getTotalTime();
    }

    public TaskStatusEnum getTaskStatus() {
        if (this.isFinished()) {
            return TaskStatusEnum.FINISHED;
        } else if (this.isInProgress()) {
            return TaskStatusEnum.IN_PROGRESS;
        } else if (this.isReadyToStart()) {
            return TaskStatusEnum.READY_TO_START;
        } else if (this.isBlocked()){
            return TaskStatusEnum.BLOCKED;
        } else {
            throw new RuntimeException("Unknown task status. You've found a bug :)");
        }
    }

    @Override
    /* If the status of the task was needed in the past was because
     * a TaskGroup needed to calculate children status, but only asked
     * if this task was FINISHED or IN_PROGRESS. Thus, there is no need
     * to cache other statutes because they only will be queried once.
     */
    public boolean isFinished() {
        if (this.currentStatus != null) {
            return this.currentStatus == TaskStatusEnum.FINISHED;
        } else {
            boolean outcome = this.advancePercentageIsOne();
            if (outcome == true) {
                this.currentStatus = TaskStatusEnum.FINISHED;
            }
            return outcome;
        }
    }

    @Override
    public boolean isInProgress() {
        if (this.currentStatus != null) {
            return this.currentStatus == TaskStatusEnum.IN_PROGRESS;
        } else {
            boolean advanceBetweenZeroAndOne = this.advancePertentageIsGreaterThanZero() &&
                    !advancePercentageIsOne();
            boolean outcome = advanceBetweenZeroAndOne || this.hasAttachedWorkReports();
            if (outcome == true) {
                this.currentStatus = TaskStatusEnum.IN_PROGRESS;
            }
            return outcome;
        }
    }

    public boolean isReadyToStart() {
        if (!this.advancePercentageIsZero() || this.hasAttachedWorkReports()) {
            return false;
        }
        Set<Dependency> dependencies = getDependenciesWithThisDestinationAndAllParents();
        for (Dependency dependency: dependencies) {
            Type dependencyType = dependency.getType();
            if (dependencyType.equals(Type.END_START)) {
                if (!dependency.getOrigin().isFinished()) {
                    return false;
                }
            } else if (dependencyType.equals(Type.START_START)) {
                if (!dependency.getOrigin().isFinished() &&
                        !dependency.getOrigin().isInProgress()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isBlocked() {
        if (!this.advancePercentageIsZero() || this.hasAttachedWorkReports()) {
            return false;
        }
        Set<Dependency> dependencies = getDependenciesWithThisDestinationAndAllParents();
        for (Dependency dependency: dependencies) {
            Type dependencyType = dependency.getType();
            if (dependencyType.equals(Type.END_START)) {
                if (!dependency.getOrigin().isFinished()) {
                    return true;
                }
            } else if (dependencyType.equals(Type.START_START)) {
                if (!dependency.getOrigin().isFinished() &&
                        !dependency.getOrigin().isInProgress()) {
                    return true;
                }
            }
        }
        return false;
     }

    private boolean advancePertentageIsGreaterThanZero() {
        return this.getAdvancePercentage().compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean advancePercentageIsZero() {
        return this.getAdvancePercentage().compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean advancePercentageIsOne() {
        return this.getAdvancePercentage().compareTo(BigDecimal.ONE) == 0;
    }

    private boolean hasAttachedWorkReports() {
        SumChargedEffort sumChargedEffort = this.getOrderElement().getSumChargedEffort();
        return sumChargedEffort != null && !sumChargedEffort.isZero();
    }

    @Override
    public void acceptVisitor(TaskElementVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void resetStatus() {
        this.currentStatus = null;
    }

    @Override
    public boolean isAnyTaskWithConstraint(PositionConstraintType type) {
        return getPositionConstraint().getConstraintType().equals(type);
    }

}
