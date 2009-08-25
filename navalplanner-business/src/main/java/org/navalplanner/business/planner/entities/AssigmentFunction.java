package org.navalplanner.business.planner.entities;

import org.navalplanner.business.common.BaseEntity;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class AssigmentFunction extends BaseEntity {

    private ResourceAllocation resourceAllocation;

    public static AssigmentFunction create() {
        return (AssigmentFunction) create(new AssigmentFunction());
    }

    protected AssigmentFunction() {

    }

    public ResourceAllocation getResourceAllocation() {
        return resourceAllocation;
    }

    public void setResourceAllocation(ResourceAllocation resourceAllocation) {
        this.resourceAllocation = resourceAllocation;
    }
}
