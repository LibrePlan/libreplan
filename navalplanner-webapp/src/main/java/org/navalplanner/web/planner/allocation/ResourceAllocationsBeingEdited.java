package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourceAllocationWithDesiredResourcesPerDay;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

public class ResourceAllocationsBeingEdited {

    public static ResourceAllocationsBeingEdited noTaskModifying(Task task,
            List<AllocationDTO> initialAllocations, IResourceDAO resourceDAO) {
        return new ResourceAllocationsBeingEdited(task, initialAllocations,
                resourceDAO, false);
    }

    private final List<AllocationDTO> currentAllocations;

    private final Set<ResourceAllocation<?>> requestedToRemove = new HashSet<ResourceAllocation<?>>();

    private IResourceDAO resourceDAO;

    private final Task task;

    private final boolean modifyTask;

    private FormBinder formBinder = null;

    private CalculatedValue calculatedValue;

    private ResourceAllocationsBeingEdited(Task task,
            List<AllocationDTO> initialAllocations, IResourceDAO resourceDAO,
            boolean modifyTask) {
        this.task = task;
        this.resourceDAO = resourceDAO;
        this.modifyTask = modifyTask;
        this.currentAllocations = new ArrayList<AllocationDTO>(
                initialAllocations);
        this.calculatedValue = getCurrentCalculatedValue(task);
    }

    private CalculatedValue getCurrentCalculatedValue(Task task) {
        return task.isFixedDuration() ? CalculatedValue.NUMBER_OF_HOURS
                : CalculatedValue.END_DATE;
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

    public List<ResourceAllocationWithDesiredResourcesPerDay> asResourceAllocations() {
        List<ResourceAllocationWithDesiredResourcesPerDay> result = new ArrayList<ResourceAllocationWithDesiredResourcesPerDay>();
        for (AllocationDTO allocation : currentAllocations) {
            result.add(createOrModify(allocation).withDesiredResourcesPerDay(
                    allocation.getResourcesPerDay()));
        }
        return result;
    }

    private List<ResourceAllocation<?>> stripResourcesPerDay(
            List<ResourceAllocationWithDesiredResourcesPerDay> withResourcesPerDay) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        for (ResourceAllocationWithDesiredResourcesPerDay r : withResourcesPerDay) {
            result.add(r.getResourceAllocation());
        }
        return result;
    }

    private ResourceAllocation<?> createOrModify(AllocationDTO allocation) {
        if (!modifyTask) {
            return createAllocation(allocation);
        }
        if (allocation.isModifying()) {
            return reloadResourceIfNeeded(allocation.getOrigin());
        } else {
            ResourceAllocation<?> result = createAllocation(allocation);
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

    private ResourceAllocation<?> createAllocation(AllocationDTO allocation) {
        if (allocation instanceof SpecificAllocationDTO) {
            SpecificAllocationDTO specific = (SpecificAllocationDTO) allocation;
            return createSpecific(specific.getResource());
        } else {
            return GenericResourceAllocation.create(task);
        }
    }

    private ResourceAllocation<?> createSpecific(Resource resource) {
        SpecificResourceAllocation result = SpecificResourceAllocation
                .create(task);
        result.setResource(resource);
        return result;
    }

    public ResourceAllocationsBeingEdited taskModifying() {
        return new ResourceAllocationsBeingEdited(task, currentAllocations,
                resourceDAO, true);
    }

    public FormBinder createFormBinder() {
        if (formBinder != null)
            throw new IllegalStateException(
                    "there is already a binder associated with this object");
        formBinder = new FormBinder(this);
        return formBinder;
    }

    public CalculatedValue getCalculatedValue() {
        return this.calculatedValue;
    }

    public void setCalculatedValue(CalculatedValue calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    public AggregateOfResourceAllocations getInitialAggregate() {
        return new AggregateOfResourceAllocations(task.getResourceAllocations());
    }


}
