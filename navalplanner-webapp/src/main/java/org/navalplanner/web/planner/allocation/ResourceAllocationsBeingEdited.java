package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.Days;
import org.joda.time.LocalDate;
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
            List<AllocationDTO> initialAllocations, IResourceDAO resourceDAO,
            List<Resource> resourcesBeingEdited) {
        return new ResourceAllocationsBeingEdited(task, initialAllocations,
                resourceDAO, resourcesBeingEdited, false);
    }

    private final List<AllocationDTO> currentAllocations;

    private final Set<ResourceAllocation<?>> requestedToRemove = new HashSet<ResourceAllocation<?>>();

    private IResourceDAO resourceDAO;

    private final Task task;

    private final boolean modifyTask;

    private FormBinder formBinder = null;

    private CalculatedValue calculatedValue;

    private Integer daysDuration;

    private final List<Resource> resourcesMatchingCriterions;

    private ResourceAllocationsBeingEdited(Task task,
            List<AllocationDTO> initialAllocations, IResourceDAO resourceDAO,
            List<Resource> resourcesMatchingCriterions,
            boolean modifyTask) {
        this.task = task;
        this.resourceDAO = resourceDAO;
        this.resourcesMatchingCriterions = resourcesMatchingCriterions;
        this.modifyTask = modifyTask;
        this.currentAllocations = new ArrayList<AllocationDTO>(
                initialAllocations);
        this.calculatedValue = task.getCalculatedValue();
        this.daysDuration = task.getDaysDuration();
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
        for (AllocationDTO allocation : withoutZeroResourcesPerDayAllocations(currentAllocations)) {
            result.add(createOrModify(allocation).withDesiredResourcesPerDay(
                    allocation.getResourcesPerDay()));
        }
        return result;
    }


    private List<AllocationDTO> withoutZeroResourcesPerDayAllocations(
            List<AllocationDTO> allocations) {
        List<AllocationDTO> result = new ArrayList<AllocationDTO>();
        for (AllocationDTO allocationDTO : allocations) {
            if (!allocationDTO.isEmptyResourcesPerDay()) {
                result.add(allocationDTO);
            }
        }
        return result;
    }

    public void checkInvalidValues() {
        if (thereIsJustOneEmptyGenericResourceAllocation()) {
            formBinder
                    .markGenericAllocationMustBeNoZeroOrMoreAllocations(currentAllocations
                            .get(0));
        }
        if (formBinder.getCalculatedValue() != CalculatedValue.NUMBER_OF_HOURS
                && formBinder.getAssignedHours() == 0) {
            formBinder.markAssignedHoursMustBePositive();
        }
    }

    private boolean thereIsJustOneEmptyGenericResourceAllocation() {
        return currentAllocations.size() == 1
                && currentAllocations.get(0).isGeneric()
                && currentAllocations.get(0).isEmptyResourcesPerDay();
    }

    public AggregateOfResourceAllocations doAllocation() {
        checkInvalidValues();
        List<ResourceAllocationWithDesiredResourcesPerDay> allocations = asResourceAllocations();
        switch (calculatedValue) {
        case NUMBER_OF_HOURS:
            ResourceAllocation.allocating(allocations).withResources(
                    resourcesMatchingCriterions).allocateOnTaskLength();
            daysDuration = task.getDaysDuration();
            break;
        case END_DATE:
            LocalDate end = ResourceAllocation.allocating(allocations)
                    .withResources(resourcesMatchingCriterions)
                    .untilAllocating(formBinder.getAssignedHours());
            daysDuration = from(task.getStartDate(), end);
            break;
        default:
            throw new RuntimeException("cant handle: " + calculatedValue);
        }
        return new AggregateOfResourceAllocations(stripResourcesPerDay(allocations));
    }

    private Integer from(Date startDate, LocalDate end) {
        LocalDate start = new LocalDate(startDate.getTime());
        return Days.daysBetween(start, end).getDays();
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
        ResourceAllocationsBeingEdited result = new ResourceAllocationsBeingEdited(
                task, currentAllocations,
                resourceDAO, resourcesMatchingCriterions, true);
        result.formBinder = this.formBinder;
        result.calculatedValue = this.calculatedValue;
        return result;
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

    public Task getTask() {
        return task;
    }

    public Integer getDaysDuration() {
        return daysDuration;
    }

}
