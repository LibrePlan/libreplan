package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;

/**
 * Represents the relation between {@link Task} and a generic {@link Resource}.
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class GenericResourceAllocation extends ResourceAllocation {

    private Set<Criterion> criterions;

    private Set<GenericDayAssigment> genericDayAssigments = new HashSet<GenericDayAssigment>();

    public static GenericResourceAllocation create() {
        return (GenericResourceAllocation) create(new GenericResourceAllocation());
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

    public Set<GenericDayAssigment> getGenericDayAssigments() {
        return Collections.unmodifiableSet(genericDayAssigments);
    }

    public List<GenericDayAssigment> getOrderedAssigmentsFor(Resource resource) {
        return Collections.unmodifiableList(getOrderedAssignmentsFor().get(
                resource));
    }

    private Map<Resource, List<GenericDayAssigment>> getOrderedAssignmentsFor() {
        if (orderedDayAssignmentsByResource == null) {
            orderedDayAssignmentsByResource = DayAssigment
                    .byResourceAndOrdered(genericDayAssigments);
        }
        return orderedDayAssignmentsByResource;
    }

    private void clearFieldsCalculatedFromAssignments() {
        this.orderedDayAssignmentsByResource = null;
    }

    public Set<Criterion> getCriterions() {
        return Collections.unmodifiableSet(criterions);
    }
}
