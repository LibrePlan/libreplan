package org.navalplanner.business.planner.entities;

import org.joda.time.LocalDate;
import org.navalplanner.business.resources.entities.Resource;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class GenericDayAssignment extends DayAssignment {

    private GenericResourceAllocation genericResourceAllocation;

    public static GenericDayAssignment create(LocalDate day, int hours,
            Resource resource) {
        return (GenericDayAssignment) create(new GenericDayAssignment(day, hours,
                resource));
    }

    private GenericDayAssignment(LocalDate day, int hours, Resource resource) {
        super(day, hours, resource);
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public GenericDayAssignment() {

    }

    public GenericResourceAllocation getGenericResourceAllocation() {
        return genericResourceAllocation;
    }

    protected void setGenericResourceAllocation(
            GenericResourceAllocation genericResourceAllocation) {
        if (this.genericResourceAllocation != null)
            throw new IllegalStateException(
                    "the allocation cannot be changed once it has been set");
        this.genericResourceAllocation = genericResourceAllocation;
    }

}
