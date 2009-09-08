package org.navalplanner.business.planner.entities;

import org.joda.time.LocalDate;
import org.navalplanner.business.resources.entities.Resource;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class GenericDayAssigment extends DayAssigment {

    private GenericResourceAllocation genericResourceAllocation;

    public static GenericDayAssigment create(LocalDate day, int hours,
            Resource resource) {
        return (GenericDayAssigment) create(new GenericDayAssigment(day, hours,
                resource));
    }

    private GenericDayAssigment(LocalDate day, int hours, Resource resource) {
        super(day, hours, resource);
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public GenericDayAssigment() {

    }

    public GenericResourceAllocation getGenericResourceAllocation() {
        return genericResourceAllocation;
    }

    public void setGenericResourceAllocation(
            GenericResourceAllocation genericResourceAllocation) {
        if (this.genericResourceAllocation != null)
            throw new IllegalStateException(
                    "the allocation cannot be changed once it has been set");
        this.genericResourceAllocation = genericResourceAllocation;
    }

}
