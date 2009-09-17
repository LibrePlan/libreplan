package org.navalplanner.business.planner.entities;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Represents the relation between {@link Task} and a specific {@link Worker}.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class SpecificResourceAllocation extends ResourceAllocation implements
        IAllocatable {

    public static SpecificResourceAllocation create(Task task) {
        return (SpecificResourceAllocation) create(new SpecificResourceAllocation(
                task));
    }

    @NotNull
    private Resource resource;

    private Set<SpecificDayAssignment> specificDaysAssignment = new HashSet<SpecificDayAssignment>();

    public static SpecificResourceAllocation createForTesting(
            ResourcesPerDay resourcesPerDay, Task task) {
        return (SpecificResourceAllocation) create(new SpecificResourceAllocation(
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

    @Override
    public List<SpecificDayAssignment> getAssignments() {
        return DayAssignment.orderedByDay(specificDaysAssignment);
    }

    private void setAssignments(List<SpecificDayAssignment> assignments) {
        this.specificDaysAssignment = new HashSet<SpecificDayAssignment>(
                assignments);
        setParentFor(specificDaysAssignment);
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
        AssignmentsAllocation<SpecificDayAssignment> assignmentsAllocation = new AssignmentsAllocation<SpecificDayAssignment>() {

            @Override
            protected List<SpecificDayAssignment> distributeForDay(
                    LocalDate day, int totalHours) {
                return Arrays.asList(SpecificDayAssignment.create(day,
                        totalHours, resource));
            }

            @Override
            protected void resetAssignmentsTo(
                    List<SpecificDayAssignment> assignments) {
                setAssignments(assignments);
            }
        };

        assignmentsAllocation.allocate(resourcesPerDay);
    }
}
