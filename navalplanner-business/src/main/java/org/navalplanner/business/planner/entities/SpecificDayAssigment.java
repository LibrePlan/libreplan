package org.navalplanner.business.planner.entities;

import org.joda.time.LocalDate;
import org.navalplanner.business.resources.entities.Resource;


/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class SpecificDayAssigment extends DayAssigment {

    private SpecificResourceAllocation specificResourceAllocation;

    public static SpecificDayAssigment create(LocalDate day, int hours,
            Resource resource) {
        return (SpecificDayAssigment) create(new SpecificDayAssigment(day,
                hours, resource));
    }

    public SpecificDayAssigment(LocalDate day, int hours, Resource resource) {
        super(day, hours, resource);
    }

    /**
     * Constructor for hibernate. DO NOT USE!
     */
    public SpecificDayAssigment() {

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
