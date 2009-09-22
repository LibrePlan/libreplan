package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourceAllocationWithDesiredResourcesPerDay;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

public class ResourceAllocationsBeingEdited {

    private final List<AllocationDTO> currentAllocations;

    private final Set<ResourceAllocation<?>> requestedToRemove = new HashSet<ResourceAllocation<?>>();

    private IResourceDAO resourceDAO;

    public ResourceAllocationsBeingEdited(
            List<AllocationDTO> initialAllocations, IResourceDAO resourceDAO) {
        this.resourceDAO = resourceDAO;
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

    public List<ResourceAllocationWithDesiredResourcesPerDay> asResourceAllocationsFor(
            Task task) {
        List<ResourceAllocationWithDesiredResourcesPerDay> result = new ArrayList<ResourceAllocationWithDesiredResourcesPerDay>();
        for (AllocationDTO allocation : currentAllocations) {
            result
                    .add(createOrModify(allocation, task)
                            .withDesiredResourcesPerDay(
                    allocation.getResourcesPerDay()));
        }
        return result;
    }

    private ResourceAllocation<?> createOrModify(AllocationDTO allocation,
            Task task) {
        if (allocation.isModifying()) {
            return reloadResourceIfNeeded(allocation.getOrigin());
        } else {
            ResourceAllocation<?> result = createAllocation(allocation, task);
            task.addResourceAllocation(result);
            return result;
        }
    }

    private ResourceAllocation<?> reloadResourceIfNeeded(
            ResourceAllocation<?> origin) {
        if (origin instanceof SpecificResourceAllocation) {
            SpecificResourceAllocation specific = (SpecificResourceAllocation) origin;
            specific.setResource(getFromDB(specific.getResource()));
        }
        return origin;
    }

    private Resource getFromDB(Resource resource) {
        return resourceDAO.findExistingEntity(resource.getId());
    }

    private ResourceAllocation<?> createAllocation(AllocationDTO allocation,
            Task task) {
        if (allocation instanceof SpecificAllocationDTO) {
            SpecificAllocationDTO specific = (SpecificAllocationDTO) allocation;
            return createSpecific(specific.getResource(), task);
        } else {
            return GenericResourceAllocation.create(task);
        }
    }

    private ResourceAllocation<?> createSpecific(Resource resource, Task task) {
        resource = getFromDB(resource);
        SpecificResourceAllocation result = SpecificResourceAllocation
                .create(task);
        result.setResource(resource);
        return result;
    }

}
