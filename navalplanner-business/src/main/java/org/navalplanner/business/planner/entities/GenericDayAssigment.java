package org.navalplanner.business.planner.entities;

import java.util.HashSet;
import java.util.Set;

import org.navalplanner.business.resources.entities.Criterion;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class GenericDayAssigment extends DayAssigment {

    private GenericResourceAllocation genericResourceAllocation;

    private Set<Criterion> criterions = new HashSet<Criterion>();

    public static GenericDayAssigment create() {
        return (GenericDayAssigment) create(new GenericDayAssigment());
    }

    protected GenericDayAssigment() {

    }

    public Set<Criterion> getCriterions() {
        return criterions;
    }

    public void setCriterions(Set<Criterion> criterions) {
        this.criterions = criterions;
    }

    public GenericResourceAllocation getGenericResourceAllocation() {
        return genericResourceAllocation;
    }

    public void setGenericResourceAllocation(
            GenericResourceAllocation genericResourceAllocation) {
        this.genericResourceAllocation = genericResourceAllocation;
    }

}
