/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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

public class ResourceAllocationsBeingEdited {

    public static ResourceAllocationsBeingEdited noTaskModifying(Task task,
            List<AllocationDTO> initialAllocations, IResourceDAO resourceDAO,
            List<Resource> resourcesBeingEdited) {
        return new ResourceAllocationsBeingEdited(task, initialAllocations,
                resourcesBeingEdited);
    }

    private final List<AllocationDTO> currentAllocations;

    private final Set<ResourceAllocation<?>> requestedToRemove = new HashSet<ResourceAllocation<?>>();

    private final Task task;

    private FormBinder formBinder = null;

    private CalculatedValue calculatedValue;

    private Integer daysDuration;

    private final List<Resource> resourcesMatchingCriterions;

    private ResourceAllocationsBeingEdited(Task task,
            List<AllocationDTO> initialAllocations, List<Resource> resourcesMatchingCriterions) {
        this.task = task;
        this.resourcesMatchingCriterions = resourcesMatchingCriterions;
        this.currentAllocations = new ArrayList<AllocationDTO>(
                initialAllocations);
        this.calculatedValue = task.getCalculatedValue();
        this.daysDuration = task.getDaysDuration();
    }

    public void addSpecificResourceAllocationFor(Resource resource) {
        if (alreadyExistsAllocationFor(resource)) {
            throw new IllegalArgumentException(_(
                    "{0} already assigned to resource allocation list",
                    resource.getDescription()));
        }
        SpecificAllocationDTO allocation = SpecificAllocationDTO
                .forResource(resource);
        currentAllocations.add(allocation);
    }

    public List<AllocationDTO> getCurrentAllocations() {
        return new ArrayList<AllocationDTO>(currentAllocations);
    }

    private boolean alreadyExistsAllocationFor(Resource resource) {
        return !getAllocationsFor(resource).isEmpty();
    }

    private List<SpecificAllocationDTO> getAllocationsFor(Resource resource) {
        List<SpecificAllocationDTO> found = SpecificAllocationDTO
                .withResource(SpecificAllocationDTO
                        .getSpecific(currentAllocations), resource);
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

    private Map<AllocationDTO, ResourceAllocation<?>> allocationsWithTheirRelatedAllocationsOnTask() {
        Map<AllocationDTO, ResourceAllocation<?>> result = new HashMap<AllocationDTO, ResourceAllocation<?>>();
        for (AllocationDTO dto : withoutZeroResourcesPerDayAllocations(currentAllocations)) {
            result.put(dto, dto.getOrigin());
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
        if (thereIsNoEmptyGenericResourceAllocation()
                && noWorkersForCriterion()) {
            formBinder.markNoWorkersMatchedByCriterion(getGenericAllocation());
        }
        if (thereIsJustOneEmptyGenericResourceAllocation()) {
            formBinder
                    .markGenericAllocationMustBeNoZeroOrMoreAllocations(getGenericAllocation());
        }
        if (formBinder.getCalculatedValue() != CalculatedValue.NUMBER_OF_HOURS
                && formBinder.getAssignedHours() == 0) {
            formBinder.markAssignedHoursMustBePositive();
        }
        if (!thereIsLeastOneNoEmptyAllocation()) {
            formBinder.markThereMustBeAtLeastOneNoEmptyAllocation();
        }
    }

    private boolean noWorkersForCriterion() {
        return resourcesMatchingCriterions.isEmpty();
    }

    private boolean thereIsNoEmptyGenericResourceAllocation() {
        return !getGenericAllocation().isEmptyResourcesPerDay();
    }

    private GenericAllocationDTO getGenericAllocation() {
        return (GenericAllocationDTO) currentAllocations
                .get(0);
    }

    private boolean thereIsLeastOneNoEmptyAllocation() {
        for (AllocationDTO allocationDTO : currentAllocations) {
            if (!allocationDTO.isEmptyResourcesPerDay()) {
                return true;
            }
        }
        return false;
    }

    private boolean thereIsJustOneEmptyGenericResourceAllocation() {
        return currentAllocations.size() == 1
                && getGenericAllocation().isGeneric()
                && getGenericAllocation().isEmptyResourcesPerDay();
    }

    public AllocationResult doAllocation() {
        checkInvalidValues();
        Map<ResourceAllocationWithDesiredResourcesPerDay, ResourceAllocation<?>> fromDetachedToAttached = getAllocationsWithRelationshipsToOriginal();
        List<ResourceAllocationWithDesiredResourcesPerDay> allocations = asList(fromDetachedToAttached);
        switch (calculatedValue) {
        case NUMBER_OF_HOURS:
            ResourceAllocation.allocating(allocations)
                    .withResources(
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
        return new AllocationResult(calculatedValue,
                new AggregateOfResourceAllocations(
                stripResourcesPerDay(allocations)), daysDuration,
                fromDetachedToAttached);
    }

    private Map<ResourceAllocationWithDesiredResourcesPerDay, ResourceAllocation<?>> getAllocationsWithRelationshipsToOriginal() {
        Map<AllocationDTO, ResourceAllocation<?>> allocationsWithTheirRelatedAllocationsOnTask = allocationsWithTheirRelatedAllocationsOnTask();
        Map<ResourceAllocationWithDesiredResourcesPerDay, ResourceAllocation<?>> fromDetachedToAttached = instantiate(allocationsWithTheirRelatedAllocationsOnTask);
        return fromDetachedToAttached;
    }

    private List<ResourceAllocationWithDesiredResourcesPerDay> asList(
            Map<ResourceAllocationWithDesiredResourcesPerDay, ResourceAllocation<?>> map) {
        return new ArrayList<ResourceAllocationWithDesiredResourcesPerDay>(
                map.keySet());
    }

    private Map<ResourceAllocationWithDesiredResourcesPerDay, ResourceAllocation<?>> instantiate(
            Map<AllocationDTO, ResourceAllocation<?>> allocationsWithTheirRelatedAllocationsOnTask) {
        Map<ResourceAllocationWithDesiredResourcesPerDay, ResourceAllocation<?>> result = new HashMap<ResourceAllocationWithDesiredResourcesPerDay, ResourceAllocation<?>>();
        for (Entry<AllocationDTO, ResourceAllocation<?>> entry : allocationsWithTheirRelatedAllocationsOnTask
                .entrySet()) {
            AllocationDTO key = entry.getKey();
            result.put(instantiate(key), entry.getValue());
        }
        return result;
    }

    private ResourceAllocationWithDesiredResourcesPerDay instantiate(
            AllocationDTO key) {
        return new ResourceAllocationWithDesiredResourcesPerDay(
                createAllocation(key), key
                        .getResourcesPerDay());
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
        this.daysDuration = task.getDaysDuration();
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