package org.navalplanner.business.planner.entities.allocationalgorithms;

import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;

public class ResourceAllocationWithDesiredResourcesPerDay {

    private final ResourceAllocation<?> resourceAllocation;

    private final ResourcesPerDay resourcesPerDay;

    public ResourceAllocationWithDesiredResourcesPerDay(
            ResourceAllocation<?> resourceAllocation,
            ResourcesPerDay resourcesPerDay) {
        this.resourceAllocation = resourceAllocation;
        this.resourcesPerDay = resourcesPerDay;
    }

    public ResourceAllocation<?> getResourceAllocation() {
        return resourceAllocation;
    }

    public ResourcesPerDay getResourcesPerDay() {
        return resourcesPerDay;
    }
}