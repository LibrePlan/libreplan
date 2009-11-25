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

import java.util.ArrayList;
import java.util.Collection;
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
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.AllocationBeingModified;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;

public class ResourceAllocationsBeingEdited {

    public static AllocationResult createInitialAllocation(Task task) {
        Set<ResourceAllocation<?>> resourceAllocations = task
                .getResourceAllocations();
        Map<AllocationBeingModified, ResourceAllocation<?>> forModification = forModification(resourceAllocations);
        AggregateOfResourceAllocations aggregate = new AggregateOfResourceAllocations(
                AllocationBeingModified.stripResourcesPerDay(forModification
                        .keySet()));
        return new AllocationResult(task, task.getCalculatedValue(), aggregate,
                task.getDaysDuration(), forModification);
    }

    private static Map<AllocationBeingModified, ResourceAllocation<?>> forModification(
            Collection<ResourceAllocation<?>> resourceAllocations) {
        Map<AllocationBeingModified, ResourceAllocation<?>> result = new HashMap<AllocationBeingModified, ResourceAllocation<?>>();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            result.put(resourceAllocation.copy().asAllocationBeingModified(),
                    resourceAllocation);
        }
        return result;
    }

    public static ResourceAllocationsBeingEdited create(Task task,
            List<AllocationDTO> initialAllocations, IResourceDAO resourceDAO) {
        return new ResourceAllocationsBeingEdited(task, initialAllocations);
    }

    private final List<AllocationDTO> currentAllocations;

    private final Set<ResourceAllocation<?>> requestedToRemove = new HashSet<ResourceAllocation<?>>();

    private final Task task;

    private FormBinder formBinder = null;

    private CalculatedValue calculatedValue;

    private Integer daysDuration;

    private ResourceAllocationsBeingEdited(Task task,
            List<AllocationDTO> initialAllocations) {
        this.task = task;
        this.currentAllocations = new ArrayList<AllocationDTO>(
                initialAllocations);
        this.calculatedValue = task.getCalculatedValue();
        this.daysDuration = task.getDaysDuration();
    }

    public void addSpecificResourceAllocationFor(List<Resource> resource) {
        List<Resource> alreadyPresent = new ArrayList<Resource>();
        for (Resource each : resource) {
            if (alreadyExistsAllocationFor(each)) {
                alreadyPresent.add(each);
            } else {
                currentAllocations.add(SpecificAllocationDTO.forResource(each));
                formBinder.newAllocationAdded();
            }
        }
        if (!alreadyPresent.isEmpty()) {
            formBinder.markRepeatedResources(alreadyPresent);
        }
    }

    public void addGeneric(Set<Criterion> criterions,
            Collection<? extends Resource> resourcesMatched) {
        if (resourcesMatched.isEmpty()) {
            formBinder.markNoWorkersMatchedByCriterions(criterions);
        } else {
            GenericAllocationDTO genericAllocationDTO = GenericAllocationDTO
                    .create(criterions, resourcesMatched);
            if (alreadyExistsAllocationFor(criterions)) {
                formBinder.markThereisAlreadyAssignmentWith(criterions);
            } else {
                currentAllocations.add(genericAllocationDTO);
                formBinder.newAllocationAdded();
            }
        }
    }

    public List<AllocationDTO> getCurrentAllocations() {
        return new ArrayList<AllocationDTO>(currentAllocations);
    }

    private boolean alreadyExistsAllocationFor(Resource resource) {
        return !getAllocationsFor(resource).isEmpty();
    }

    private boolean alreadyExistsAllocationFor(Set<Criterion> criterions) {
        List<GenericAllocationDTO> generic = AllocationDTO
                .getGeneric(getCurrentAllocations());
        for (GenericAllocationDTO each : generic) {
            if (each.hasSameCriterions(criterions)) {
                return true;
            }
        }
        return false;
    }

    private List<SpecificAllocationDTO> getAllocationsFor(Resource resource) {
        List<SpecificAllocationDTO> found = SpecificAllocationDTO
                .withResource(SpecificAllocationDTO
                        .getSpecific(currentAllocations), resource);
        return found;
    }

    public void remove(AllocationDTO allocation) {
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
        if (formBinder.getCalculatedValue() != CalculatedValue.NUMBER_OF_HOURS
                && formBinder.getAssignedHours() <= 0) {
            formBinder.markAssignedHoursMustBePositive();
        }
    }

    public AllocationResult doAllocation() {
        checkInvalidValues();
        Map<AllocationBeingModified, ResourceAllocation<?>> fromDetachedToAttached = getAllocationsWithRelationshipsToOriginal();
        List<AllocationBeingModified> allocations = asList(fromDetachedToAttached);
        if (!allocations.isEmpty()) {
            switch (calculatedValue) {
            case NUMBER_OF_HOURS:
                ResourceAllocation.allocating(allocations)
                        .allocateOnTaskLength();
                daysDuration = task.getDaysDuration();
                break;
            case END_DATE:
                LocalDate end = ResourceAllocation.allocating(allocations)
                        .untilAllocating(formBinder.getAssignedHours());
                daysDuration = from(task.getStartDate(), end);
                break;
            default:
                throw new RuntimeException("cant handle: " + calculatedValue);
            }
        }
        return new AllocationResult(task, calculatedValue,
                new AggregateOfResourceAllocations(
                AllocationBeingModified.stripResourcesPerDay(allocations)), daysDuration,
                fromDetachedToAttached);
    }

    private Map<AllocationBeingModified, ResourceAllocation<?>> getAllocationsWithRelationshipsToOriginal() {
        Map<AllocationDTO, ResourceAllocation<?>> allocationsWithTheirRelatedAllocationsOnTask = allocationsWithTheirRelatedAllocationsOnTask();
        Map<AllocationBeingModified, ResourceAllocation<?>> fromDetachedToAttached = instantiate(allocationsWithTheirRelatedAllocationsOnTask);
        return fromDetachedToAttached;
    }

    private List<AllocationBeingModified> asList(
            Map<AllocationBeingModified, ResourceAllocation<?>> map) {
        return new ArrayList<AllocationBeingModified>(
                map.keySet());
    }

    private Map<AllocationBeingModified, ResourceAllocation<?>> instantiate(
            Map<AllocationDTO, ResourceAllocation<?>> allocationsWithTheirRelatedAllocationsOnTask) {
        Map<AllocationBeingModified, ResourceAllocation<?>> result = new HashMap<AllocationBeingModified, ResourceAllocation<?>>();
        for (Entry<AllocationDTO, ResourceAllocation<?>> entry : allocationsWithTheirRelatedAllocationsOnTask
                .entrySet()) {
            AllocationDTO key = entry.getKey();
            result.put(instantiate(key), entry.getValue());
        }
        return result;
    }

    private AllocationBeingModified instantiate(
            AllocationDTO key) {
        return key.toAllocationBeingModified(task);
    }

    private Integer from(Date startDate, LocalDate end) {
        LocalDate start = new LocalDate(startDate.getTime());
        return Days.daysBetween(start, end).getDays();
    }

    public FormBinder createFormBinder() {
        if (formBinder != null) {
            throw new IllegalStateException(
                    "there is already a binder associated with this object");
        }
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

    public AllocationResult getInitialAllocation() {
        return createInitialAllocation(task);
    }


    public Task getTask() {
        return task;
    }

    public Integer getDaysDuration() {
        return daysDuration;
    }

}