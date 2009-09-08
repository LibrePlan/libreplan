package org.navalplanner.business.planner.entities;

import java.util.Collections;
import java.util.HashSet;
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

    public Set<Criterion> getCriterions() {
        return Collections.unmodifiableSet(criterions);
    }
}
