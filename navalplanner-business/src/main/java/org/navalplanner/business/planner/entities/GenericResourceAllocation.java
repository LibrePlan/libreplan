/*
 * This file is part of NavalPlan
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.IWorkHours;
import org.navalplanner.business.planner.entities.HoursDistributor.IResourceSelector;
import org.navalplanner.business.planner.entities.HoursDistributor.ResourceWithAssignedHours;
import org.navalplanner.business.planner.entities.allocationalgorithms.HoursModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionCompounder;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.Resource;

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
            Collection<? extends GenericResourceAllocation> genericAllocations) {
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

    private Set<Criterion> criterions = new HashSet<Criterion>();

    private Set<GenericDayAssignment> genericDayAssignments = new HashSet<GenericDayAssignment>();

    private Map<Resource, List<GenericDayAssignment>> orderedDayAssignmentsByResource = null;

    private GenericResourceAllocation(ResourcesPerDay resourcesPerDay, Task task) {
        super(resourcesPerDay, task);
    }

    public GenericResourceAllocation() {

    }

    public static GenericResourceAllocation create(Task task) {
        return create(new GenericResourceAllocation(
                task));
    }

    public static GenericResourceAllocation create(Task task,
            Collection<? extends Criterion> criterions) {
        GenericResourceAllocation result = new GenericResourceAllocation(task);
        result.criterions = new HashSet<Criterion>(criterions);
        return create(result);
    }

    private GenericResourceAllocation(Task task) {
        super(task);
        this.criterions = task.getCriterions();
    }

    public Set<GenericDayAssignment> getGenericDayAssignments() {
        return Collections.unmodifiableSet(genericDayAssignments);
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
        if (orderedDayAssignmentsByResource == null) {
            orderedDayAssignmentsByResource = DayAssignment
                    .byResourceAndOrdered(genericDayAssignments);
        }
        return orderedDayAssignmentsByResource;
    }

    private void clearFieldsCalculatedFromAssignments() {
        this.orderedDayAssignmentsByResource = null;
        this.orderedDayAssignmentsCached = null;
    }

    public Set<Criterion> getCriterions() {
        return Collections.unmodifiableSet(criterions);
    }

    private static Date toDate(LocalDate day) {
        return day.toDateTimeAtStartOfDay().toDate();
    }

    private final class ResourcesSatisfyingCriterionsSelector implements
            IResourceSelector {

        @Override
        public boolean isSelectable(Resource resource, LocalDate day) {
            ICriterion compoundCriterion = CriterionCompounder.buildAnd(
                    criterions).getResult();
            return compoundCriterion.isSatisfiedBy(resource, toDate(day),
                    toDate(day.plusDays(1)));
        }
    }

    private class GenericAllocation extends AssignmentsAllocation {


        private HoursDistributor hoursDistributor;

        public GenericAllocation(List<Resource> resources) {
            hoursDistributor = new HoursDistributor(resources,
                    getAssignedHoursForResource(),
                    new ResourcesSatisfyingCriterionsSelector());
        }

        @Override
        protected List<GenericDayAssignment> distributeForDay(LocalDate day,
                int totalHours) {
            List<GenericDayAssignment> result = new ArrayList<GenericDayAssignment>();
            for (ResourceWithAssignedHours each : hoursDistributor
                    .distributeForDay(day, totalHours)) {
                result.add(GenericDayAssignment.create(day, each.hours,
                        each.resource));
            }
            return result;
        }

    }

    private IAssignedHoursForResource assignedHoursCalculatorOverriden = null;

    public void overrideAssignedHoursForResource(
            GenericResourceAllocation allocation) {
        assignedHoursCalculatorOverriden = allocation
                .getAssignedHoursForResource();
    }

    private IAssignedHoursForResource getAssignedHoursForResource() {
        if (assignedHoursCalculatorOverriden != null) {
            return assignedHoursCalculatorOverriden;
        }
        return new AssignedHoursDiscounting(this);
    }

    @Override
    protected IWorkHours getWorkHoursGivenTaskHours(IWorkHours taskWorkHours) {
        return taskWorkHours;
    }

    public IAllocatable forResources(Collection<? extends Resource> resources) {
        return new GenericAllocation(new ArrayList<Resource>(resources));
    }

    @Override
    protected void addingAssignments(
            Collection<? extends GenericDayAssignment> assignments) {
        setParentFor(assignments);
        this.genericDayAssignments.addAll(assignments);
        clearFieldsCalculatedFromAssignments();
    }

    @Override
    protected void removingAssignments(List<? extends DayAssignment> assignments) {
        this.genericDayAssignments.removeAll(assignments);
        clearFieldsCalculatedFromAssignments();
    }

    private void setParentFor(
            Collection<? extends GenericDayAssignment> assignmentsCreated) {
        for (GenericDayAssignment genericDayAssignment : assignmentsCreated) {
            genericDayAssignment.setGenericResourceAllocation(this);
        }
    }

    private List<GenericDayAssignment> orderedDayAssignmentsCached;

    @Override
    public List<GenericDayAssignment> getAssignments() {
        if (orderedDayAssignmentsCached != null) {
            return orderedDayAssignmentsCached;
        }
        return orderedDayAssignmentsCached = DayAssignment
                .orderedByDay(genericDayAssignments);
    }

    @Override
    protected Class<GenericDayAssignment> getDayAssignmentType() {
        return GenericDayAssignment.class;
    }

    public List<DayAssignment> createAssignmentsAtDay(
            List<Resource> resources, LocalDate day,
            ResourcesPerDay resourcesPerDay, final int maxLimit) {
        final int hours = Math.min(calculateTotalToDistribute(day,
                resourcesPerDay), maxLimit);
        GenericAllocation genericAllocation = new GenericAllocation(resources);
        return new ArrayList<DayAssignment>(genericAllocation.distributeForDay(
                day, hours));
    }

    @Override
    public void mergeAssignments(ResourceAllocation<?> modifications) {
        Validate.isTrue(modifications instanceof GenericResourceAllocation);
        mergeAssignments((GenericResourceAllocation) modifications);
    }

    private void mergeAssignments(GenericResourceAllocation modifications) {
        detachAssignments();
        moveToThis(modifications.genericDayAssignments);
        clearFieldsCalculatedFromAssignments();
    }

    private void moveToThis(Set<GenericDayAssignment> assignemnts) {
        this.genericDayAssignments = GenericDayAssignment.copy(this,
                assignemnts);
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
    ResourceAllocation<GenericDayAssignment> createCopy() {
        GenericResourceAllocation allocation = create();
        allocation.genericDayAssignments = new HashSet<GenericDayAssignment>(
                this.genericDayAssignments);
        allocation.criterions = new HashSet<Criterion>(criterions);
        allocation.assignedHoursCalculatorOverriden = new AssignedHoursDiscounting(
                this);
        return allocation;
    }

    @Override
    public ResourcesPerDayModification asResourcesPerDayModification() {
        return ResourcesPerDayModification.create(this,
                getResourcesPerDay(), getAssociatedResources());
    }

    @Override
    public HoursModification asHoursModification() {
        return HoursModification.create(this, getAssignedHours(),
                getAssociatedResources());
    }

    @Override
    public ResourcesPerDayModification withDesiredResourcesPerDay(
            ResourcesPerDay resourcesPerDay) {
        return ResourcesPerDayModification.create(this, resourcesPerDay,
                getAssociatedResources());
    }

    @Override
    public List<Resource> querySuitableResources(IResourceDAO resourceDAO) {
        return resourceDAO.findSatisfyingCriterionsAtSomePoint(getCriterions());
    }

}
