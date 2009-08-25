package org.navalplanner.business.planner.entities;

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

    Set<Criterion> criterions;

    Set<GenericDayAssigment> genericDayAssigments;

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
    }

    public Set<GenericDayAssigment> getGenericDayAssigments() {
        return genericDayAssigments;
    }

    public void setGenericDayAssigments(
            Set<GenericDayAssigment> genericDayAssigments) {
        this.genericDayAssigments = genericDayAssigments;
    }

    public Set<Criterion> getCriterions() {
        return criterions;
    }

    public void setCriterions(Set<Criterion> criterions) {
        this.criterions = criterions;
    }
}
