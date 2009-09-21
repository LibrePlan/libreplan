package org.navalplanner.business.planner.entities.allocationalgorithms;

import java.util.List;

import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.resources.entities.Resource;

public class AllocatorForTaskDurationAndSpecifiedResourcesPerDay {

    private List<ResourceAllocationWithDesiredResourcesPerDay> allocations;

    private List<Resource> resources;

    public AllocatorForTaskDurationAndSpecifiedResourcesPerDay(
            List<ResourceAllocationWithDesiredResourcesPerDay> allocations,
            List<Resource> resources) {
        this.allocations = allocations;
        this.resources = resources;
    }

    public void allocateOnTaskLength() {
        for (ResourceAllocationWithDesiredResourcesPerDay allocation : allocations) {
            doAllocationForFixedTask(allocation.getResourceAllocation(),
                    allocation.getResourcesPerDay());
        }
    }

    private void doAllocationForFixedTask(ResourceAllocation<?> allocation,
            ResourcesPerDay resourcesPerDay) {
        if (allocation instanceof GenericResourceAllocation) {
            doAllocation((GenericResourceAllocation) allocation,
                    resourcesPerDay);
        } else {
            SpecificResourceAllocation specific = (SpecificResourceAllocation) allocation;
            doAllocation(specific, resourcesPerDay);
        }
    }

    private void doAllocation(SpecificResourceAllocation specific,
            ResourcesPerDay resourcesPerDay) {
        specific.allocate(resourcesPerDay);
    }

    private void doAllocation(GenericResourceAllocation generic,
            ResourcesPerDay resourcesPerDay) {
        generic.forResources(resources).allocate(resourcesPerDay);
    }

}
