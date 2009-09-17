package org.navalplanner.business.planner.entities;

import org.joda.time.LocalDate;
import org.navalplanner.business.resources.entities.Resource;


/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class SpecificDayAssignment extends DayAssignment {

    private SpecificResourceAllocation specificResourceAllocation;

    public static SpecificDayAssignment create(LocalDate day, int hours,
            Resource resource) {
        return (SpecificDayAssignment) create(new SpecificDayAssignment(day,
                hours, resource));
    }

    public SpecificDayAssignment(LocalDate day, int hours, Resource resource) {
        super(day, hours, resource);
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public SpecificDayAssignment() {

    }

    public SpecificResourceAllocation getSpecificResourceAllocation() {
        return specificResourceAllocation;
    }

    public void setSpecificResourceAllocation(
            SpecificResourceAllocation specificResourceAllocation) {
        if (this.specificResourceAllocation != null)
            throw new IllegalStateException(
                    "the allocation cannot be changed once it has been set");
        this.specificResourceAllocation = specificResourceAllocation;
    }
}
