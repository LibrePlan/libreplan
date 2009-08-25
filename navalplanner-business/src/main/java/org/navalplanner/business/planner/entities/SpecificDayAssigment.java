package org.navalplanner.business.planner.entities;


/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class SpecificDayAssigment extends DayAssigment {

    private SpecificResourceAllocation specificResourceAllocation;

    public static SpecificDayAssigment create() {
        return (SpecificDayAssigment) create(new SpecificDayAssigment());
    }

    protected SpecificDayAssigment() {

    }

    public SpecificResourceAllocation getSpecificResourceAllocation() {
        return specificResourceAllocation;
    }

    public void setSpecificResourceAllocation(
            SpecificResourceAllocation specificResourceAllocation) {
        this.specificResourceAllocation = specificResourceAllocation;
    }
}
