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

import static java.util.Collections.emptyList;
import static org.navalplanner.business.workingday.EffortDuration.min;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Valid;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ICalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.orders.entities.AggregatedHoursGroup;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.entities.DerivedAllocationGenerator.IWorkerFinder;
import org.navalplanner.business.planner.entities.ResourceAllocation.Direction;
import org.navalplanner.business.planner.entities.allocationalgorithms.HoursModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.planner.entities.consolidations.Consolidation;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.util.deepcopy.AfterCopy;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Task extends TaskElement implements ITaskLeafConstraint {

    public static Task createTask(TaskSource taskSource) {
        Task task = new Task();
        OrderElement orderElement = taskSource.getOrderElement();
        orderElement.applyStartConstraintIfNeededTo(task);
        Task result = create(task, taskSource);
        result.initializeEndDate();
        return result;
    }

    @Override
    protected void initializeEndDate() {
        EffortDuration workHours = EffortDuration.hours(getWorkHours());
        EffortDuration effortStandardPerDay = EffortDuration.hours(8);

        int daysElapsed = workHours.divideBy(effortStandardPerDay);
        EffortDuration remainder = workHours.remainderFor(effortStandardPerDay);

        IntraDayDate start = getIntraDayStartDate();
        IntraDayDate newEnd = IntraDayDate.create(
                start.getDate().plusDays(daysElapsed), start
                        .getEffortDuration().plus(remainder));
        setIntraDayEndDate(newEnd);
    }

    private CalculatedValue calculatedValue = CalculatedValue.END_DATE;

    private Set<ResourceAllocation<?>> resourceAllocations = new HashSet<ResourceAllocation<?>>();

    @Valid
    @SuppressWarnings("unused")
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

    private TaskStartConstraint startConstraint = new TaskStartConstraint();

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
    @AssertTrue(message = "order element associated to a task must be not null")
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
        return new AggregateOfResourceAllocations(resourceAllocations)
                .getTotalHours();
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
        throw new UnsupportedOperationException();
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

    private boolean isLimitingAndNotAssignedYet() {
        return isLimiting() && !isLimitingAndHasDayAssignments();
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
                Collection<ModifiedAllocation> collection) {
            List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
            for (ModifiedAllocation modifiedAllocation : collection) {
                result.add(modifiedAllocation.getModification());
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

    public void explicityMoved(LocalDate date) {
        getStartConstraint().explicityMovedTo(date);
    }

    public TaskStartConstraint getStartConstraint() {
        if (startConstraint == null) {
            startConstraint = new TaskStartConstraint();
        }
        return startConstraint;
    }

    private static abstract class AllocationModificationStrategy {

        public abstract List<ResourcesPerDayModification> getResourcesPerDayModified(
                List<ResourceAllocation<?>> allocations);

        public abstract List<HoursModification> getHoursModified(
                List<ResourceAllocation<?>> allocations);

    }

    private static class WithTheSameHoursAndResourcesPerDay extends
            AllocationModificationStrategy {

        @Override
        public List<HoursModification> getHoursModified(
                List<ResourceAllocation<?>> allocations) {
            return HoursModification.fromExistent(allocations);
        }

        @Override
        public List<ResourcesPerDayModification> getResourcesPerDayModified(
                List<ResourceAllocation<?>> allocations) {
            return ResourcesPerDayModification.fromExistent(allocations);
        }

    }

    private static class WithAnotherResources extends
            AllocationModificationStrategy {
        private final IResourceDAO resourceDAO;

        WithAnotherResources(IResourceDAO resourceDAO) {
            this.resourceDAO = resourceDAO;
        }

        @Override
        public List<HoursModification> getHoursModified(
                List<ResourceAllocation<?>> allocations) {
            return HoursModification.withNewResources(allocations, resourceDAO);
        }

        @Override
        public List<ResourcesPerDayModification> getResourcesPerDayModified(
                List<ResourceAllocation<?>> allocations) {
            return ResourcesPerDayModification.withNewResources(allocations,
                    resourceDAO);
        }
    }

    public void copyAssignmentsFromOneScenarioToAnother(Scenario from, Scenario to) {
        for (ResourceAllocation<?> each : getAllResourceAllocations()) {
            each.copyAssignmentsFromOneScenarioToAnother(from, to);
        }
    }

    @Override
    protected IDatesHandler createDatesHandler(final Scenario scenario) {
        return new IDatesHandler() {

            @Override
            public void moveTo(IntraDayDate newStartDate) {
                IntraDayDate previousStart = getIntraDayStartDate();
                if (previousStart.equals(newStartDate)) {
                    return;
                }
                if (calculatedValue != CalculatedValue.END_DATE
                        || getSatisfiedResourceAllocations().isEmpty()
                        || isLimitingAndNotAssignedYet()) {
                    setIntraDayEndDate(calculateEndKeepingLength(newStartDate));
                }
                setIntraDayStartDate(newStartDate);
                doReassignment(Direction.FORWARD);
            }

            private void doReassignment(Direction direction) {
                reassign(scenario, direction,
                        new WithTheSameHoursAndResourcesPerDay());
            }

            @Override
            public void moveEndTo(IntraDayDate newEnd) {
                if (getIntraDayEndDate().equals(newEnd)) {
                    return;
                }
                if (calculatedValue != CalculatedValue.END_DATE
                        || getSatisfiedResourceAllocations().isEmpty()) {
                    setIntraDayStartDate(calculateNewStartGivenEnd(newEnd));
                }
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
                doReassignment(getLastAllocationDirection());
            }

            private void updateWorkableDays() {
                assert calculatedValue != CalculatedValue.END_DATE;
                workableDays = getWorkableDaysBetweenDates();
            }

        };
    }

    protected abstract class DatesHandlerForAllocatable implements
            IDatesHandler {

        protected final Scenario scenario;

        public DatesHandlerForAllocatable(Scenario scenario) {
            Validate.notNull(scenario);
            this.scenario = scenario;
        }



        protected abstract IntraDayDate calculateNewEndGiven(
                IntraDayDate newStartDate);

        protected abstract void moveAllocations();

        // default implementation meant to be override
        protected void updateWorkableDays() {
        }
    }

    private IntraDayDate calculateEndKeepingLength(IntraDayDate newStartDate) {
        DurationBetweenDates durationBetweenDates = getDurationBetweenDates();
        return durationBetweenDates.fromStartToEnd(newStartDate);
    }

    private IntraDayDate calculateStartKeepingLength(IntraDayDate newEnd) {
        DurationBetweenDates durationBetweenDates = getDurationBetweenDates();
        return durationBetweenDates.fromEndToStart(newEnd);
    }

    private DurationBetweenDates getDurationBetweenDates() {
        if (workableDays != null) {
            return new DurationBetweenDates(workableDays);
        } else {
            Integer calculatedWorkableDays = getWorkableDaysUntil(getEndAsLocalDate());
            return new DurationBetweenDates(calculatedWorkableDays);
        }
    }

    private class DurationBetweenDates {

        private final int numberOfWorkableDays;

        private final EffortDuration remainderDuration;

        private final ICalendar calendar;

        public DurationBetweenDates(int numberOfWorkableDays) {
            this.calendar = getCalendar() != null ? getCalendar()
                    : SameWorkHoursEveryDay.getDefaultWorkingDay();
            this.numberOfWorkableDays = numberOfWorkableDays;

            IntraDayDate start = getIntraDayStartDate();
            IntraDayDate end = getIntraDayEndDate();

            if (start.getEffortDuration().compareTo(end.getEffortDuration()) <= 0) {
                this.remainderDuration = end.getEffortDuration().minus(
                        start.getEffortDuration());
            } else {
                EffortDuration capacity = calendar.getCapacityOn(PartialDay
                        .wholeDay(start.getDate()));
                this.remainderDuration = end.getEffortDuration()
                        .plus(capacity.minus(min(start.getEffortDuration(),
                                capacity)));
            }
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
            LocalDate current = start.getDate();
            while (!remaining.isZero()) {
                EffortDuration capacity = calendar.getCapacityOn(PartialDay
                        .wholeDay(current));
                result = IntraDayDate.create(current, remaining);
                remaining = remaining.minus(min(capacity, remaining));
                current = current.plusDays(1);
            }
            return result;
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
                decrement = decrement.minus(min(capacity, decrement));
                result = IntraDayDate.create(resultDay,
                        capacity.minus(min(capacity, decrement)));
            }
            return result;
        }
    }

    public Direction getLastAllocationDirection() {
        if (lastAllocationDirection == null) {
            return Direction.FORWARD;
        }
        return lastAllocationDirection;
    }

    public void reassignAllocationsWithNewResources(Scenario scenario,
            IResourceDAO resourceDAO) {
        reassign(scenario, getLastAllocationDirection(),
                new WithAnotherResources(resourceDAO));
    }

    private void reassign(Scenario onScenario, Direction direction,
            AllocationModificationStrategy strategy) {
        this.lastAllocationDirection = direction;
        if (isLimiting()) {
            return;
        }
        List<ModifiedAllocation> copied = ModifiedAllocation.copy(onScenario,
                getSatisfiedResourceAllocations());
        List<ResourceAllocation<?>> toBeModified = ModifiedAllocation
                .modified(copied);
        if (toBeModified.isEmpty()) {
            return;
        }
        doAllocation(strategy, direction, toBeModified);
        updateDerived(copied);

        List<ResourceAllocation<?>> newAllocations = emptyList(),
        modifiedAllocations = emptyList();
        mergeAllocation(onScenario, getIntraDayStartDate(),
                getIntraDayEndDate(), workableDays, calculatedValue,
                newAllocations, copied, modifiedAllocations);
    }

    private void doAllocation(AllocationModificationStrategy strategy,
            Direction direction, List<ResourceAllocation<?>> toBeModified) {
        List<ResourcesPerDayModification> allocations = strategy
                .getResourcesPerDayModified(toBeModified);
        switch (calculatedValue) {
        case NUMBER_OF_HOURS:
            ResourceAllocation.allocating(allocations).allocateOnTaskLength();
            break;
        case END_DATE:
            IntraDayDate date = ResourceAllocation.allocating(allocations)
                    .untilAllocating(direction, getAssignedHours());
            if (direction == Direction.FORWARD) {
                setIntraDayEndDate(date);
            } else {
                setIntraDayStartDate(date);
            }
            break;
        case RESOURCES_PER_DAY:
            List<HoursModification> hoursModified = strategy
                    .getHoursModified(toBeModified);
            ResourceAllocation.allocatingHours(hoursModified)
                              .allocateUntil(new LocalDate(getEndDate()));
            break;
        default:
            throw new RuntimeException("cant handle: " + calculatedValue);
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
        return canBeResized() && !isSubcontracted();
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
        int result = 0;
        LocalDate start = getStartAsLocalDate();
        for (LocalDate current = start; current.compareTo(end) < 0; current = current
                .plusDays(1)) {
            if (isWorkable(current)) {
                result++;
            }
        }
        return result;
    }

    public LocalDate calculateEndGivenWorkableDays(int workableDays) {
        LocalDate result = getIntraDayStartDate().getDate();
        return calculateEndGivenWorkableDays(result, workableDays);
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

}
