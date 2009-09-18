package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.IWorkHours;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;

/**
 * Represents the relation between {@link Task} and a generic {@link Resource}.
 * @author Diego Pino García <dpino@igalia.com>
 */
public class GenericResourceAllocation extends
        ResourceAllocation<GenericDayAssignment> {

    private Set<Criterion> criterions;

    private Set<GenericDayAssignment> genericDayAssignments = new HashSet<GenericDayAssignment>();

    private Map<Resource, List<GenericDayAssignment>> orderedDayAssignmentsByResource = null;

    public static GenericResourceAllocation create() {
        return (GenericResourceAllocation) create(new GenericResourceAllocation());
    }

    public static GenericResourceAllocation createForTesting(
            ResourcesPerDay resourcesPerDay, Task task) {
        return (GenericResourceAllocation) create(new GenericResourceAllocation(
                resourcesPerDay, task));
    }

    private GenericResourceAllocation(ResourcesPerDay resourcesPerDay, Task task) {
        super(resourcesPerDay, task);
    }

    public GenericResourceAllocation() {

    }

    public static GenericResourceAllocation create(Task task) {
        return (GenericResourceAllocation) create(new GenericResourceAllocation(
                task));
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
        if (list == null)
            return Collections.emptyList();
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
    }

    public Set<Criterion> getCriterions() {
        return Collections.unmodifiableSet(criterions);
    }

    private class GenericAllocation extends AssignmentsAllocation {

        private final List<Resource> resources;

        private final List<IWorkHours> workHours;

        public GenericAllocation(List<Resource> resources) {
            this.resources = resources;
            this.workHours = new ArrayList<IWorkHours>();
            for (Resource resource : resources) {
                this.workHours.add(generateWorkHoursFor(resource));
            }
        }

        private final IWorkHours generateWorkHoursFor(Resource resource) {
            if (resource.getCalendar() != null) {
                return resource.getCalendar();
            } else {
                return SameWorkHoursEveryDay.getDefaultWorkingDay();
            }
        }

        @Override
        protected List<GenericDayAssignment> distributeForDay(LocalDate day,
                int totalHours) {
            List<GenericDayAssignment> result = new ArrayList<GenericDayAssignment>();
            List<Share> shares = currentSharesFor(day);
            ShareDivision currentDivision = ShareDivision.create(shares);
            ShareDivision newDivison = currentDivision.plus(totalHours);
            int[] differences = currentDivision.to(newDivison);
            for (int i = 0; i < differences.length; i++) {
                assert differences[i] >= 0;
                GenericDayAssignment dayAssignment = GenericDayAssignment
                        .create(day, differences[i], resources.get(i));
                result.add(dayAssignment);
            }
            return result;
        }

        private List<Share> currentSharesFor(LocalDate day) {
            List<Share> shares = new ArrayList<Share>();
            for (int i = 0; i < resources.size(); i++) {
                Resource resource = resources.get(i);
                IWorkHours workHoursForResource = workHours.get(i);
                int alreadyAssignedHours = resource.getAssignedHours(day);
                Integer workableHours = workHoursForResource
                        .getWorkableHours(day);
                // a resource would have a zero share if all it's hours for a
                // given day are filled
                shares.add(new Share(alreadyAssignedHours - workableHours));
            }
            return shares;
        }

    }

    public IAllocatable forResources(Collection<? extends Resource> resources) {
        return new GenericAllocation(new ArrayList<Resource>(resources));
    }

    @Override
    protected void resetAssignmentsTo(
            List<GenericDayAssignment> assignmentsCreated) {
        this.genericDayAssignments = new HashSet<GenericDayAssignment>(
                assignmentsCreated);
        setParentFor(assignmentsCreated);
        clearFieldsCalculatedFromAssignments();
    }

    private void setParentFor(List<GenericDayAssignment> assignmentsCreated) {
        for (GenericDayAssignment genericDayAssignment : assignmentsCreated) {
            genericDayAssignment.setGenericResourceAllocation(this);
        }
    }

    @Override
    public List<? extends DayAssignment> getAssignments() {
        return DayAssignment.orderedByDay(genericDayAssignments);
    }

}
