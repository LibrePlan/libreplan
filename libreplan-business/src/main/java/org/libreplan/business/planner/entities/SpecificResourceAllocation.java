/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine.FixedPoint;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine.Interval;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.calendars.entities.CombinedWorkHours;
import org.libreplan.business.calendars.entities.ICalendar;
import org.libreplan.business.common.ProportionalDistributor;
import org.libreplan.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.util.deepcopy.OnCopy;
import org.libreplan.business.util.deepcopy.Strategy;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.EffortDuration.IEffortFrom;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workingday.ResourcesPerDay;

/**
 * Represents the relation between {@link Task} and a specific {@link Worker}.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class SpecificResourceAllocation extends
        ResourceAllocation<SpecificDayAssignment> implements IAllocatable {

    public static SpecificResourceAllocation create(Task task) {
        return create(new SpecificResourceAllocation(
                task));
    }

    /**
     * Creates a {@link SpecificResourceAllocation} for a
     * {@link LimitingResourceQueueElement}
     *
     * The process of creating a specific resource allocation for a queue
     * element is different as it's necessary to assign a resource and a number
     * of resources per day without allocating day assignments
     *
     * @param resource
     * @param task
     * @return
     */
    public static SpecificResourceAllocation createForLimiting(Resource resource,
            Task task) {
        assert resource.isLimitingResource();
        SpecificResourceAllocation result = create(new SpecificResourceAllocation(
                task));
        result.setResource(resource);
        result.setResourcesPerDayToAmount(1);
        return result;
    }

    @OnCopy(Strategy.SHARE)
    private Resource resource;

    private Set<SpecificDayAssignmentsContainer> specificDayAssignmentsContainers = new HashSet<SpecificDayAssignmentsContainer>();

    @Valid
    private Set<SpecificDayAssignmentsContainer> getSpecificDayAssignmentsContainers() {
        return new HashSet<SpecificDayAssignmentsContainer>(
                specificDayAssignmentsContainers);
    }

    public static SpecificResourceAllocation createForTesting(
            ResourcesPerDay resourcesPerDay, Task task) {
        return create(new SpecificResourceAllocation(
                resourcesPerDay, task));
    }

    public SpecificResourceAllocation() {
    }

    @Override
    protected SpecificDayAssignmentsContainer retrieveOrCreateContainerFor(
            Scenario scenario) {
        SpecificDayAssignmentsContainer retrieved = retrieveContainerFor(scenario);
        if (retrieved != null) {
            return retrieved;
        }
        SpecificDayAssignmentsContainer result = SpecificDayAssignmentsContainer
                .create(this, scenario);
        specificDayAssignmentsContainers.add(result);
        return result;
    }

    @Override
    protected SpecificDayAssignmentsContainer retrieveContainerFor(
            Scenario scenario) {
        Map<Scenario, SpecificDayAssignmentsContainer> containers = containersByScenario();
        return containers.get(scenario);
    }

    private SpecificResourceAllocation(ResourcesPerDay resourcesPerDay,
            Task task) {
        super(resourcesPerDay, task);
    }

    private SpecificResourceAllocation(Task task) {
        super(task);
    }

    @NotNull
    public Resource getResource() {
        return resource;
    }

    private Map<Scenario, SpecificDayAssignmentsContainer> containersByScenario() {
        Map<Scenario, SpecificDayAssignmentsContainer> result = new HashMap<Scenario, SpecificDayAssignmentsContainer>();
        for (SpecificDayAssignmentsContainer each : specificDayAssignmentsContainers) {
            assert !result.containsKey(each);
            result.put(each.getScenario(), each);
        }
        return result;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void allocate(ResourcesPerDay resourcesPerDay) {
        Validate.notNull(resourcesPerDay);
        Validate.notNull(resource);
        new SpecificAssignmentsAllocator().allocate(resourcesPerDay);
    }

    @Override
    public IAllocateResourcesPerDay resourcesPerDayUntil(
            IntraDayDate endExclusive) {
        return new SpecificAssignmentsAllocator()
                .resourcesPerDayUntil(endExclusive);
    }

    @Override
    public IAllocateResourcesPerDay resourcesPerDayFromEndUntil(
            IntraDayDate start) {
        SpecificAssignmentsAllocator allocator = new SpecificAssignmentsAllocator();
        return allocator.resourcesPerDayFromEndUntil(start);
    }

    @Override
    public IAllocateEffortOnInterval fromStartUntil(LocalDate endExclusive) {
        return new SpecificAssignmentsAllocator().fromStartUntil(endExclusive);
    }

    @Override
    public IAllocateEffortOnInterval fromStartUntil(IntraDayDate end) {
        return new SpecificAssignmentsAllocator().fromStartUntil(end);
    }

    @Override
    public IAllocateEffortOnInterval fromEndUntil(LocalDate start) {
        return new SpecificAssignmentsAllocator().fromEndUntil(start);
    }

    @Override
    public IAllocateEffortOnInterval fromEndUntil(IntraDayDate start) {
        return new SpecificAssignmentsAllocator().fromEndUntil(start);
    }

    private final class SpecificAssignmentsAllocator extends
            AssignmentsAllocator {

        @Override
        public List<SpecificDayAssignment> distributeForDay(PartialDay day,
                EffortDuration effort) {
            return Arrays.asList(SpecificDayAssignment.create(day.getDate(),
                    effort, resource));
        }

        @Override
        protected AvailabilityTimeLine getResourcesAvailability() {
            return AvailabilityCalculator.getCalendarAvailabilityFor(resource);
        }

        @Override
        protected Capacity getCapacityAt(PartialDay day) {
            return day.limitCapacity(getAllocationCalendar()
                    .getCapacityWithOvertime(day.getDate()));
        }

    }

    public IEffortDistributor<SpecificDayAssignment> createEffortDistributor() {
        return new SpecificAssignmentsAllocator();
    }

    @Override
    public IAllocateEffortOnInterval onIntervalWithinTask(LocalDate start, LocalDate end) {
        return new SpecificAssignmentsAllocator().onIntervalWithinTask(start, end);
    }

    @Override
    public IAllocateEffortOnInterval onIntervalWithinTask(IntraDayDate start,
            IntraDayDate end) {
        return new SpecificAssignmentsAllocator().onIntervalWithinTask(start,
                end);
    }

    @Override
    public IAllocateEffortOnInterval onInterval(LocalDate startInclusive,
            LocalDate endExclusive) {
        return new SpecificAssignmentsAllocator().onInterval(startInclusive,
                endExclusive);
    }

    @Override
    public IAllocateEffortOnInterval onInterval(IntraDayDate start,
            IntraDayDate end) {
        return new SpecificAssignmentsAllocator().onInterval(start, end);
    }

    @Override
    protected ICalendar getCalendarGivenTaskCalendar(ICalendar taskCalendar) {
        return CombinedWorkHours.minOf(taskCalendar, getResource()
                .getCalendar());
    }

    @Override
    protected Class<SpecificDayAssignment> getDayAssignmentType() {
        return SpecificDayAssignment.class;
    }

    @Override
    public IAllocatable withPreviousAssociatedResources() {
        return this;
    }

    @Override
    public List<Resource> getAssociatedResources() {
        return Arrays.asList(resource);
    }

    @Override
    ResourceAllocation<SpecificDayAssignment> createCopy(Scenario scenario) {
        SpecificResourceAllocation result = create(getTask());
        result.resource = getResource();
        return result;
    }

    @Override
    public ResourcesPerDayModification withDesiredResourcesPerDay(
            ResourcesPerDay resourcesPerDay) {
        return ResourcesPerDayModification.create(this, resourcesPerDay);
    }

    @Override
    public List<Resource> querySuitableResources(
            IResourcesSearcher resourcesSearcher) {
        return Collections.singletonList(resource);
    }

    @Override
    protected void setItselfAsParentFor(SpecificDayAssignment dayAssignment) {
        dayAssignment.setSpecificResourceAllocation(this);
    }

    @Override
    public void makeAssignmentsContainersDontPoseAsTransientAnyMore() {
        for (SpecificDayAssignmentsContainer each : specificDayAssignmentsContainers) {
            each.dontPoseAsTransientObjectAnymore();
        }
    }

    @Override
    public void copyAssignments(Scenario from, Scenario to) {
        SpecificDayAssignmentsContainer fromContainer = retrieveOrCreateContainerFor(from);
        SpecificDayAssignmentsContainer toContainer = retrieveOrCreateContainerFor(to);
        toContainer.resetTo(fromContainer.getDayAssignments());
    }

    @Override
    protected void removePredecessorContainersFor(Scenario scenario) {
        Map<Scenario, SpecificDayAssignmentsContainer> byScenario = containersByScenario();
        for (Scenario each : scenario.getPredecessors()) {
            SpecificDayAssignmentsContainer container = byScenario.get(each);
            if (container != null) {
                specificDayAssignmentsContainers.remove(container);
            }
        }
    }

    @Override
    protected void removeContainersFor(Scenario scenario) {
        SpecificDayAssignmentsContainer container = containersByScenario().get(
                scenario);
        if (container != null) {
            specificDayAssignmentsContainers.remove(container);
        }
    }

    /**
     * It does an allocation using the provided {@link EffortDuration} in the
     * not consolidated part in interval from the first day not consolidated to
     * the end provided. All previous not consolidated assignments are removed.
     *
     * @param effortForNotConsolidatedPart
     * @param endExclusive
     */
    public void allocateWholeAllocationKeepingProportions(
            EffortDuration effortForNotConsolidatedPart, IntraDayDate end) {
        AllocationInterval interval = new AllocationInterval(
                getIntraDayStartDate(), end);

        List<DayAssignment> nonConsolidatedAssignments = interval
                .getNoConsolidatedAssignmentsOnInterval();
        ProportionalDistributor distributor = ProportionalDistributor
                .create(asSeconds(nonConsolidatedAssignments));

        EffortDuration[] effortsPerDay = asEfforts(distributor
                .distribute(effortForNotConsolidatedPart.getSeconds()));
        allocateTheWholeAllocation(
                interval,
                assignmentsForEfforts(nonConsolidatedAssignments, effortsPerDay));
    }

    private EffortDuration[] asEfforts(int[] secondsArray) {
        EffortDuration[] result = new EffortDuration[secondsArray.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = EffortDuration.seconds(secondsArray[i]);
        }
        return result;
    }

    private List<SpecificDayAssignment> assignmentsForEfforts(
            List<DayAssignment> assignments, EffortDuration[] newEffortsPerDay) {
        List<SpecificDayAssignment> result = new ArrayList<SpecificDayAssignment>();
        int i = 0;
        for (DayAssignment each : assignments) {
            EffortDuration durationForAssignment = newEffortsPerDay[i++];
            result.add(SpecificDayAssignment.create(each.getDay(),
                    durationForAssignment, resource));
        }
        return result;
    }

    private int[] asSeconds(List<DayAssignment> assignments) {
        int[] result = new int[assignments.size()];
        int i = 0;
        for (DayAssignment each : assignments) {
            result[i++] = each.getDuration().getSeconds();
        }
        return result;
    }

    public void overrideConsolidatedDayAssignments(
            SpecificResourceAllocation origin) {
        if (origin != null) {
            List<SpecificDayAssignment> originAssignments = origin
                    .getConsolidatedAssignments();
            resetAssignmentsTo(SpecificDayAssignment
                    .copyToAssignmentsWithoutParent(originAssignments));
        }
    }

    @Override
    public EffortDuration getAssignedEffort(Criterion criterion,
            final IntraDayDate startInclusive, final IntraDayDate endExclusive) {

        return EffortDuration.sum(
                getIntervalsRelatedWith(criterion, startInclusive.getDate(),
                        endExclusive.asExclusiveEnd()),
                new IEffortFrom<Interval>() {

                    @Override
                    public EffortDuration from(Interval each) {
                        FixedPoint intervalStart = (FixedPoint) each.getStart();
                        FixedPoint intervalEnd = (FixedPoint) each.getEnd();
                        return getAssignedDuration(
                                IntraDayDate.convert(intervalStart.getDate(), startInclusive),
                                IntraDayDate.convert(intervalEnd.getDate(), endExclusive));
                    }
                });
    }

    private List<Interval> getIntervalsRelatedWith(Criterion criterion,
            LocalDate startInclusive, LocalDate endExclusive) {
        Interval queryInterval = AvailabilityTimeLine.Interval.create(
                startInclusive, endExclusive);

        List<Interval> result = new ArrayList<Interval>();
        for (Interval each : getIntervalsThisAllocationInterferesWith(criterion)) {
            if (queryInterval.overlaps(each)) {
                result.add(queryInterval.intersect(each));
            }
        }
        return result;
    }

    private List<Interval> getIntervalsThisAllocationInterferesWith(
            Criterion criterion) {
        AvailabilityTimeLine availability = AvailabilityCalculator
                .getCriterionsAvailabilityFor(Collections.singleton(criterion),
                        resource);
        availability.invalidUntil(getStartDate());
        availability.invalidFrom(getEndDate());
        return availability.getValidPeriods();
    }

    public boolean interferesWith(Criterion criterion,
            LocalDate startInclusive, LocalDate endExclusive) {
        List<Interval> intervalsRelatedWith = getIntervalsRelatedWith(
                criterion, startInclusive, endExclusive);
        return !intervalsRelatedWith.isEmpty();
    }


}
