package org.navalplanner.business.planner.entities;

import java.util.Arrays;
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

    private Set<SpecificDayAssigment> specificDaysAssigment = new HashSet<SpecificDayAssigment>();

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
    public List<SpecificDayAssigment> getAssignments() {
        return DayAssigment.orderedByDay(specificDaysAssigment);
    }

    private void setAssignments(List<SpecificDayAssigment> assignments) {
        this.specificDaysAssigment = new HashSet<SpecificDayAssigment>(
                assignments);
    }

    @Override
    public void allocate(ResourcesPerDay resourcesPerDay) {
        Validate.notNull(resourcesPerDay);
        Validate.notNull(resource);
        AssignmentsAllocation<SpecificDayAssigment> assignmentsAllocation = new AssignmentsAllocation<SpecificDayAssigment>() {

            @Override
            protected List<SpecificDayAssigment> distributeForDay(
                    LocalDate day, int totalHours) {
                return Arrays.asList(SpecificDayAssigment.create(day,
                        totalHours, resource));
            }

            @Override
            protected void resetAssignmentsTo(
                    List<SpecificDayAssigment> assignments) {
                setAssignments(assignments);
            }
        };

        assignmentsAllocation.allocate(resourcesPerDay);
    }
}
