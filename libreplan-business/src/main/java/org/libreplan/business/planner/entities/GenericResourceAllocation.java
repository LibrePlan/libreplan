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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.calendars.entities.ICalendar;
import org.libreplan.business.planner.entities.AssignedEffortForResource.IAssignedEffortForResource;
import org.libreplan.business.planner.entities.EffortDistributor.IResourceSelector;
import org.libreplan.business.planner.entities.EffortDistributor.ResourceWithAssignedDuration;
import org.libreplan.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionCompounder;
import org.libreplan.business.resources.entities.ICriterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.util.deepcopy.OnCopy;
import org.libreplan.business.util.deepcopy.Strategy;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workingday.ResourcesPerDay;

/**
 * Represents the relation between {@link Task} and a generic {@link Resource}.
 * @author Diego Pino García <dpino@igalia.com>
 */
public class GenericResourceAllocation extends
        ResourceAllocation<GenericDayAssignment> {

    public static GenericResourceAllocation create() {
        return create(new GenericResourceAllocation());
    }

    public static GenericResourceAllocation createForTesting(
            ResourcesPerDay resourcesPerDay, Task task) {
        return create(new GenericResourceAllocation(
                resourcesPerDay, task));
    }

    public static Map<Set<Criterion>, List<GenericResourceAllocation>> byCriterions(
            Collection<GenericResourceAllocation> genericAllocations) {
        Map<Set<Criterion>, List<GenericResourceAllocation>> result = new HashMap<Set<Criterion>, List<GenericResourceAllocation>>();
        for (GenericResourceAllocation genericResourceAllocation : genericAllocations) {
            Set<Criterion> criterions = genericResourceAllocation.getCriterions();
            if(! result.containsKey(criterions)){
                result.put(criterions, new ArrayList<GenericResourceAllocation>());
            }
            result.get(criterions).add(genericResourceAllocation);
        }
        return result;
    }

    @OnCopy(Strategy.SHARE_COLLECTION_ELEMENTS)
    private Set<Criterion> criterions = new HashSet<Criterion>();

    @OnCopy(Strategy.SHARE)
    private ResourceEnum resourceType;

    private Set<GenericDayAssignmentsContainer> genericDayAssignmentsContainers = new HashSet<GenericDayAssignmentsContainer>();

    @Valid
    @SuppressWarnings("unused")
    private Set<GenericDayAssignmentsContainer> getGenericDayAssignmentsContainers() {
        return new HashSet<GenericDayAssignmentsContainer>(
                genericDayAssignmentsContainers);
    }

    private GenericResourceAllocation(ResourcesPerDay resourcesPerDay, Task task) {
        super(resourcesPerDay, task);
    }

    /**
     * Constructor for Hibernate. DO NOT USE!
     */
    public GenericResourceAllocation() {
    }

    public static GenericResourceAllocation create(Task task) {
        return create(new GenericResourceAllocation(
                task));
    }

    public static GenericResourceAllocation create(Task task,
            Collection<? extends Criterion> criterions) {
        return create(task, inferType(criterions), criterions);
    }

    public static ResourceEnum inferType(
            Collection<? extends Criterion> criterions) {
        if (criterions.isEmpty()) {
            return ResourceEnum.WORKER;
        }
        Criterion first = criterions.iterator().next();
        return first.getType().getResource();
    }

    public static GenericResourceAllocation create(Task task,
            ResourceEnum resourceType, Collection<? extends Criterion> criterions) {
        Validate.notNull(resourceType);
        GenericResourceAllocation result = new GenericResourceAllocation(task);
        result.criterions = new HashSet<Criterion>(criterions);
        result.resourceType = resourceType;
        result.setResourcesPerDayToAmount(1);
        return create(result);
    }

    private GenericResourceAllocation(Task task) {
        super(task);
        this.criterions = task.getCriterions();
    }

    @Override
    protected GenericDayAssignmentsContainer retrieveOrCreateContainerFor(
            Scenario scenario) {
        GenericDayAssignmentsContainer retrieved = retrieveContainerFor(scenario);
        if (retrieved != null) {
            return retrieved;
        }
        GenericDayAssignmentsContainer result = GenericDayAssignmentsContainer
                .create(this, scenario);
        genericDayAssignmentsContainers.add(result);
        return result;
    }

    @Override
    protected GenericDayAssignmentsContainer retrieveContainerFor(
            Scenario scenario) {
        Map<Scenario, GenericDayAssignmentsContainer> containers = containersByScenario();
        return containers.get(scenario);
    }

    private Map<Scenario, GenericDayAssignmentsContainer> containersByScenario() {
        Map<Scenario, GenericDayAssignmentsContainer> result = new HashMap<Scenario, GenericDayAssignmentsContainer>();
        for (GenericDayAssignmentsContainer each : genericDayAssignmentsContainers) {
            assert !result.containsKey(each);
            result.put(each.getScenario(), each);
        }
        return result;
    }

    public List<GenericDayAssignment> getOrderedAssignmentsFor(Resource resource) {
        List<GenericDayAssignment> list = getOrderedAssignmentsFor().get(
                resource);
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    private Map<Resource, List<GenericDayAssignment>> getOrderedAssignmentsFor() {
        return DayAssignment.byResourceAndOrdered(getDayAssignmentsState()
                .getUnorderedAssignments());
    }

    public Set<Criterion> getCriterions() {
        return Collections.unmodifiableSet(criterions);
    }

    private final class ResourcesSatisfyingCriterionsSelector implements
            IResourceSelector {

        @Override
        public boolean isSelectable(Resource resource, LocalDate day) {
            ICriterion compoundCriterion = CriterionCompounder.buildAnd(
                    criterions).getResult();
            return compoundCriterion.isSatisfiedBy(resource, day);
        }
    }

    private class GenericAllocation extends AssignmentsAllocator {

        private EffortDistributor hoursDistributor;
        private final List<Resource> resources;

        public GenericAllocation(List<Resource> resources) {
            this.resources = resources;
            hoursDistributor = new EffortDistributor(resources,
                    getAssignedEffortForResource(),
                    new ResourcesSatisfyingCriterionsSelector());
        }

        @Override
        public List<GenericDayAssignment> distributeForDay(PartialDay day,
                EffortDuration effort) {
            List<GenericDayAssignment> result = new ArrayList<GenericDayAssignment>();
            for (ResourceWithAssignedDuration each : hoursDistributor
                    .distributeForDay(day, effort)) {
                result.add(GenericDayAssignment.create(day.getDate(),
                        each.duration, each.resource));
            }
            return result;
        }

        @Override
        protected AvailabilityTimeLine getResourcesAvailability() {
            return AvailabilityCalculator.buildSumOfAvailabilitiesFor(
                    getCriterions(), resources);
        }

        @Override
        protected Capacity getCapacityAt(PartialDay day) {
            return hoursDistributor.getCapacityAt(day);
        }

    }

    private IAssignedEffortForResource assignedEffortCalculatorOverriden = null;

    public void discountAssignedHoursForResourceFrom(
            Collection<? extends ResourceAllocation<?>> allocations) {
        assignedEffortCalculatorOverriden = AssignedEffortForResource
                .discount(allocations);
    }

    private IAssignedEffortForResource getAssignedEffortForResource() {
        if (assignedEffortCalculatorOverriden != null) {
            return assignedEffortCalculatorOverriden;
        }
        return AssignedEffortForResource.discount(Collections
                .singletonList(this));
    }

    @Override
    protected ICalendar getCalendarGivenTaskCalendar(ICalendar taskCalendar) {
        return taskCalendar;
    }

    public IAllocatable forResources(Collection<? extends Resource> resources) {
        return new GenericAllocation(new ArrayList<Resource>(resources));
    }

    @Override
    protected void setItselfAsParentFor(GenericDayAssignment dayAssignment) {
        dayAssignment.setGenericResourceAllocation(this);
    }

    @Override
    protected Class<GenericDayAssignment> getDayAssignmentType() {
        return GenericDayAssignment.class;
    }

    @Override
    public List<Resource> getAssociatedResources() {
        return new ArrayList<Resource>(DayAssignment
                .getAllResources(getAssignments()));
    }

    @Override
    public IAllocatable withPreviousAssociatedResources() {
        return forResources(getAssociatedResources());
    }

    @Override
    ResourceAllocation<GenericDayAssignment> createCopy(Scenario scenario) {
        GenericResourceAllocation allocation = create();
        allocation.criterions = new HashSet<Criterion>(criterions);
        return allocation;
    }

    @Override
    public ResourcesPerDayModification withDesiredResourcesPerDay(
            ResourcesPerDay resourcesPerDay) {
        return ResourcesPerDayModification.create(this, resourcesPerDay,
                getAssociatedResources());
    }

    @Override
    public List<Resource> querySuitableResources(
            IResourcesSearcher resourcesSearcher) {
        return resourcesSearcher.searchBoth().byCriteria(getCriterions())
                .execute();
    }

    public static Map<Criterion, List<GenericResourceAllocation>> byCriterion(
            List<GenericResourceAllocation> generics) {
        Map<Criterion, List<GenericResourceAllocation>> result = new HashMap<Criterion, List<GenericResourceAllocation>>();
        for (GenericResourceAllocation genericResourceAllocation : generics) {
            Set<Criterion> criterions = genericResourceAllocation
                    .getCriterions();
            for (Criterion criterion : criterions) {
                if (!result.containsKey(criterion)) {
                    result.put(criterion,
                            new ArrayList<GenericResourceAllocation>());
                }
                result.get(criterion).add(genericResourceAllocation);
            }
        }
        return result;
    }

    @Override
    public void makeAssignmentsContainersDontPoseAsTransientAnyMore() {
        for (GenericDayAssignmentsContainer each : genericDayAssignmentsContainers) {
            each.dontPoseAsTransientObjectAnymore();
        }
    }

    @Override
    public void copyAssignments(Scenario from, Scenario to) {
        GenericDayAssignmentsContainer fromContainer = retrieveOrCreateContainerFor(from);
        GenericDayAssignmentsContainer toContainer = retrieveOrCreateContainerFor(to);
        toContainer.resetTo(fromContainer.getDayAssignments());
    }

    @Override
    protected void removePredecessorContainersFor(Scenario scenario) {
        Map<Scenario, GenericDayAssignmentsContainer> byScenario = containersByScenario();
        for (Scenario each : scenario.getPredecessors()) {
            GenericDayAssignmentsContainer container = byScenario.get(each);
            if (container != null) {
                genericDayAssignmentsContainers.remove(container);
            }
        }
    }

    @Override
    protected void removeContainersFor(Scenario scenario) {
        GenericDayAssignmentsContainer container = containersByScenario().get(
                scenario);
        if (container != null) {
            genericDayAssignmentsContainers.remove(container);
        }
    }

    public void overrideConsolidatedDayAssignments(
            GenericResourceAllocation origin) {
        if (origin != null) {
            List<GenericDayAssignment> originAssignments = origin
                    .getConsolidatedAssignments();
            resetAssignmentsTo(GenericDayAssignment
                    .copyToAssignmentsWithoutParent(originAssignments));
        }
    }

    public ResourceEnum getResourceType() {
        return resourceType != null ? resourceType : inferType(criterions);
    }

    @Override
    public EffortDuration getAssignedEffort(Criterion criterion,
            IntraDayDate startInclusive, IntraDayDate endExclusive) {
        return super.getAssignedDuration(startInclusive, endExclusive);
    }

    public IEffortDistributor<GenericDayAssignment> createEffortDistributor(
            List<Resource> resources) {
        return new GenericAllocation(resources);
    }

}
