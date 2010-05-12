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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;
import org.navalplanner.business.calendars.entities.CombinedWorkHours;
import org.navalplanner.business.calendars.entities.IWorkHours;
import org.navalplanner.business.planner.entities.allocationalgorithms.HoursModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

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
    public static SpecificResourceAllocation create(Resource resource,
            Task task) {
        assert resource.isLimitingResource();
        SpecificResourceAllocation result = create(new SpecificResourceAllocation(
                task));
        result.setResource(resource);
        result.setResourcesPerDay(ResourcesPerDay.amount(1));
        return result;
    }

    @NotNull
    private Resource resource;

    private Set<SpecificDayAssignment> specificDaysAssignment = new HashSet<SpecificDayAssignment>();

    public static SpecificResourceAllocation createForTesting(
            ResourcesPerDay resourcesPerDay, Task task) {
        return create(new SpecificResourceAllocation(
                resourcesPerDay, task));
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public SpecificResourceAllocation() {

    }

    private SpecificResourceAllocation(ResourcesPerDay resourcesPerDay,
            Task task) {
        super(resourcesPerDay, task);
    }

    private SpecificResourceAllocation(Task task) {
        super(task);
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    private List<SpecificDayAssignment> assignmentsOrderedCached;
    @Override
    public List<SpecificDayAssignment> getAssignments() {
        if (assignmentsOrderedCached != null) {
            return assignmentsOrderedCached;
        }
        return assignmentsOrderedCached = DayAssignment
                .orderedByDay(specificDaysAssignment);
    }

    private List<SpecificDayAssignment> getDayAssignmentsByConsolidated(
            boolean consolidated) {
        List<SpecificDayAssignment> result = new ArrayList<SpecificDayAssignment>();
        for (SpecificDayAssignment day : getAssignments()) {
            if (day.isConsolidated() == consolidated) {
                result.add(day);
            }
        }
        return result;
    }

    public List<SpecificDayAssignment> getNonConsolidatedAssignments() {
        return getDayAssignmentsByConsolidated(false);
    }

    public List<SpecificDayAssignment> getConsolidatedAssignments() {
        return getDayAssignmentsByConsolidated(true);
    }

    @Override
    protected void addingAssignments(
            Collection<? extends SpecificDayAssignment> assignments) {
        setParentFor(assignments);
        this.specificDaysAssignment.addAll(assignments);
        clearFieldsCalculatedFromAssignments();
    }

    @Override
    protected void removingAssignments(List<? extends DayAssignment> assignments) {
        this.specificDaysAssignment.removeAll(assignments);
        clearFieldsCalculatedFromAssignments();
    }

    private void setParentFor(
            Collection<? extends SpecificDayAssignment> assignments) {
        for (SpecificDayAssignment specificDayAssignment : assignments) {
            specificDayAssignment.setSpecificResourceAllocation(this);
        }
    }

    @Override
    public void allocate(ResourcesPerDay resourcesPerDay) {
        Validate.notNull(resourcesPerDay);
        Validate.notNull(resource);
        new SpecificAssignmentsAllocation().allocate(resourcesPerDay);
    }

    @Override
    public IAllocateResourcesPerDay until(LocalDate endExclusive) {
        return new SpecificAssignmentsAllocation().until(endExclusive);
    }

    @Override
    public IAllocateHoursOnInterval fromStartUntil(LocalDate endExclusive) {
        return new SpecificAssignmentsAllocation().fromStartUntil(endExclusive);
    }

    private final class SpecificAssignmentsAllocation extends
            AssignmentsAllocation {
        @Override
        protected List<SpecificDayAssignment> distributeForDay(
                LocalDate day, int totalHours) {
            return Arrays.asList(SpecificDayAssignment.create(day,
                    totalHours, resource));
        }

        @Override
        protected AvailabilityTimeLine getResourcesAvailability() {
            return AvailabilityCalculator.getCalendarAvailabilityFor(resource);
        }
    }

    @Override
    public IAllocateHoursOnInterval onInterval(LocalDate start, LocalDate end) {
        return new SpecificAssignmentsAllocation().onInterval(start, end);
    }

    @Override
    protected IWorkHours getWorkHoursGivenTaskHours(IWorkHours taskWorkHours) {
        return CombinedWorkHours.minOf(taskWorkHours, getResource()
                .getCalendar());
    }

    @Override
    protected Class<SpecificDayAssignment> getDayAssignmentType() {
        return SpecificDayAssignment.class;
    }

    public List<DayAssignment> createAssignmentsAtDay(LocalDate day,
            ResourcesPerDay resourcesPerDay, int limit) {
        int hours = calculateTotalToDistribute(day, resourcesPerDay);
        SpecificDayAssignment specific = SpecificDayAssignment.create(day, Math
                .min(limit, hours), resource);
        List<DayAssignment> result = new ArrayList<DayAssignment>();
        result.add(specific);
        return result;
    }

    @Override
    public void mergeAssignments(ResourceAllocation<?> modifications) {
        Validate.isTrue(modifications instanceof SpecificResourceAllocation);
        mergeAssignments((SpecificResourceAllocation) modifications);
    }

    private void mergeAssignments(SpecificResourceAllocation modifications) {
        detachAssignments();
        this.specificDaysAssignment = SpecificDayAssignment.copy(this,
                modifications.specificDaysAssignment);
        clearFieldsCalculatedFromAssignments();
    }

    private void clearFieldsCalculatedFromAssignments() {
        assignmentsOrderedCached = null;
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
    ResourceAllocation<SpecificDayAssignment> createCopy() {
        SpecificResourceAllocation result = create(getTask());
        result.specificDaysAssignment = new HashSet<SpecificDayAssignment>(
                this.specificDaysAssignment);
        result.resource = getResource();
        return result;
    }

    @Override
    public ResourcesPerDayModification asResourcesPerDayModification() {
        return ResourcesPerDayModification.create(this, getResourcesPerDay());
    }

    @Override
    public HoursModification asHoursModification() {
        return HoursModification.create(this, getAssignedHours());
    }

    @Override
    public ResourcesPerDayModification withDesiredResourcesPerDay(
            ResourcesPerDay resourcesPerDay) {
        return ResourcesPerDayModification.create(this, resourcesPerDay);
    }

    @Override
    public List<Resource> querySuitableResources(IResourceDAO resourceDAO) {
        return Collections.singletonList(resource);
    }

}
