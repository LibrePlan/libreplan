package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.resources.entities.Worker;

public class ResourceAllocationsBeingEdited {

    private final List<AllocationDTO> currentAllocations;

    private final Set<ResourceAllocation<?>> requestedToRemove = new HashSet<ResourceAllocation<?>>();

    public ResourceAllocationsBeingEdited(List<AllocationDTO> initialAllocations) {
        this.currentAllocations = new ArrayList<AllocationDTO>(
                initialAllocations);
    }

    public void addSpecificResorceAllocationFor(Worker worker) {
        if (alreadyExistsAllocationFor(worker)) {
            throw new IllegalArgumentException(_(
                    "{0} already assigned to resource allocation list", worker
                            .getName()));
        }
        SpecificAllocationDTO allocation = SpecificAllocationDTO
                .forResource(worker);
        currentAllocations.add(allocation);
    }

    public List<AllocationDTO> getCurrentAllocations() {
        return new ArrayList<AllocationDTO>(currentAllocations);
    }

    private boolean alreadyExistsAllocationFor(Worker worker) {
        return !getAllocationsFor(worker).isEmpty();
    }

    private List<SpecificAllocationDTO> getAllocationsFor(Worker worker) {
        List<SpecificAllocationDTO> found = SpecificAllocationDTO.withResource(
                SpecificAllocationDTO.getSpecific(currentAllocations), worker);
        return found;
    }

    public void remove(SpecificAllocationDTO allocation) {
        currentAllocations.remove(allocation);
        if (allocation.isModifying()) {
            requestedToRemove.add(allocation.getOrigin());
        }
    }

    public Set<ResourceAllocation<?>> getAllocationsRequestedToRemove() {
        return requestedToRemove;
    }

}
