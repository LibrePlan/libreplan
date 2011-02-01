/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.CapacityResult;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.DerivedAllocationGenerator.IWorkerFinder;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation.AllocationsSpecified.INotFulfilledReceiver;
import org.navalplanner.business.planner.entities.ResourceAllocation.Direction;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.HoursModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.scenarios.entities.Scenario;

public class AllocationRowsHandler {

    public static AllocationRowsHandler create(Task task,
            List<AllocationRow> initialAllocations, IWorkerFinder workerFinder) {
        return new AllocationRowsHandler(task, initialAllocations, workerFinder);
    }

    private final List<AllocationRow> currentRows;

    private final Set<ResourceAllocation<?>> requestedToRemove = new HashSet<ResourceAllocation<?>>();

    private final Task task;

    private FormBinder formBinder = null;

    private CalculatedValue calculatedValue;

    private final IWorkerFinder workersFinder;

    private AllocationRowsHandler(Task task, List<AllocationRow> initialRows,
            IWorkerFinder workersFinder) {
        this.task = task;
        this.workersFinder = workersFinder;
        this.currentRows = new ArrayList<AllocationRow>(initialRows);
        this.calculatedValue = task.getCalculatedValue();
    }

    public void addSpecificResourceAllocationFor(List<Resource> resource) {
        List<Resource> alreadyPresent = new ArrayList<Resource>();
        for (Resource each : resource) {
            if (alreadyExistsAllocationFor(each)) {
                alreadyPresent.add(each);
            } else {
                SpecificAllocationRow specificAllocationRow = SpecificAllocationRow
                        .forResource(each);
                setupInitialHours(specificAllocationRow);
                currentRows.add(specificAllocationRow);
                formBinder.newAllocationAdded();
            }
        }
        if (!alreadyPresent.isEmpty()) {
            formBinder.markRepeatedResources(alreadyPresent);
        }
    }

    public void addGeneric(ResourceEnum resourceType,
            Collection<? extends Criterion> criteria,
            Collection<? extends Resource> resourcesMatched) {
        addGeneric(resourceType, criteria, resourcesMatched, null);
    }

    public void addGeneric(ResourceEnum resourceType,
            Collection<? extends Criterion> criteria,
            Collection<? extends Resource> resourcesMatched, Integer hours) {
        if (resourcesMatched.isEmpty()) {
            formBinder.markNoResourcesMatchedByCriterions(resourceType,
                    criteria);
        } else {
            GenericAllocationRow genericAllocationRow = GenericAllocationRow
                    .create(resourceType, criteria, resourcesMatched);
            if (hours != null) {
                genericAllocationRow.setHoursToInput(hours);
            } else {
                setupInitialHours(genericAllocationRow);
            }
            if (alreadyExistsAllocationFor(resourceType, criteria)) {
                formBinder.markThereisAlreadyAssignmentWith(resourceType,
                        criteria);
            } else {
                currentRows.add(genericAllocationRow);
                formBinder.newAllocationAdded();
            }
        }
    }

    private void setupInitialHours(AllocationRow allocationRow) {
        Integer hoursCalculated = calculateHours(allocationRow);
        if (hoursCalculated != null) {
            allocationRow.setHoursToInput(hoursCalculated);
        }
    }

    private Integer calculateHours(AllocationRow allocationRow) {
        ResourceEnum type = allocationRow.getType();
        if (notStillExistAnotherAllocation(type)) {
            return calculateHoursByCalculatedValue(type);
        }
        return null;
    }

    private boolean notStillExistAnotherAllocation(ResourceEnum type) {
        for (AllocationRow allocation : getCurrentRows()) {
            if (allocation.getType().equals(type)) {
                return false;
            }
        }
        return true;
    }

    private Integer calculateHoursByCalculatedValue(ResourceEnum type) {
        switch (calculatedValue) {
            case NUMBER_OF_HOURS:
                break;
            case END_DATE:
            case RESOURCES_PER_DAY:
            return calculateHoursGroupByResourceType(type);
        }
        return null;
    }

    private Integer calculateHoursGroupByResourceType(ResourceEnum type) {
        int result = 0;
        for(HoursGroup hourGroup : task.getTaskSource().getHoursGroups()){
            if (type.equals(hourGroup.getResourceType())) {
                result += hourGroup.getWorkingHours();
            }
        }
        return result;
    }

    public List<AllocationRow> getCurrentRows() {
        return currentRows;
    }

    private boolean alreadyExistsAllocationFor(Resource resource) {
        return !getAllocationsFor(resource).isEmpty();
    }

    private boolean alreadyExistsAllocationFor(ResourceEnum resourceType,
            Collection<? extends Criterion> criterions) {
        Set<Criterion> criterionsSet = new HashSet<Criterion>(criterions);
        List<GenericAllocationRow> generic = AllocationRow
                .getGeneric(getCurrentRows());
        for (GenericAllocationRow each : generic) {
            if (each.hasSameCriterionsAndType(criterionsSet, resourceType)) {
                return true;
            }
        }
        return false;
    }

    private List<SpecificAllocationRow> getAllocationsFor(Resource resource) {
        List<SpecificAllocationRow> found = SpecificAllocationRow.withResource(
                SpecificAllocationRow.getSpecific(currentRows), resource);
        return found;
    }

    public void remove(AllocationRow row) {
        currentRows.remove(row);
        if (row.isModifying()) {
            requestedToRemove.add(row.getOrigin());
        }
        formBinder.rowRemoved();
    }

    public Set<ResourceAllocation<?>> getAllocationsRequestedToRemove() {
        return requestedToRemove;
    }

    public void checkInvalidValues() {
        if (calculatedValue != CalculatedValue.NUMBER_OF_HOURS
                && !currentRows.isEmpty() && formBinder.getAssignedHours() <= 0) {
            formBinder.markAssignedHoursMustBePositive();
        }
        if (calculatedValue != CalculatedValue.RESOURCES_PER_DAY) {
            List<AllocationRow> rows = getRowsWithEmptyResourcesPerDay();
            if (!rows.isEmpty()) {
                formBinder.markNoEmptyResourcesPerDay(rows);
            }
        }
    }

    private List<AllocationRow> getRowsWithEmptyResourcesPerDay() {
        List<AllocationRow> result = new ArrayList<AllocationRow>();
        for (AllocationRow each : currentRows) {
            if (each.isEmptyResourcesPerDay()) {
                result.add(each);
            }
        }
        return result;
    }

    public AllocationResult doAllocation() {
        checkInvalidValues();
        if (!currentRows.isEmpty()) {
            switch (calculatedValue) {
            case NUMBER_OF_HOURS:
                calculateNumberOfHoursAllocation();
                break;
            case END_DATE:
                calculateEndDateOrStartDateAllocation();
                break;
            case RESOURCES_PER_DAY:
                calculateResourcesPerDayAllocation();
                break;
            default:
                throw new RuntimeException("cant handle: " + calculatedValue);
            }
        }
        createDerived();
        AllocationResult result = AllocationResult.create(task,
                calculatedValue, currentRows, getWorkableDaysIfApplyable());
        AllocationRow.loadDataFromLast(currentRows);
        return result;
    }

    private void calculateNumberOfHoursAllocation() {
        List<ResourcesPerDayModification> allocations = AllocationRow
                .createAndAssociate(task, currentRows);
        if (isForwardsAllocation()) {
            ResourceAllocation.allocating(allocations).allocateUntil(
                    formBinder.getAllocationEnd());
        } else {
            ResourceAllocation.allocating(allocations).allocateFromEndUntil(
                    formBinder.getAllocationStart());
        }
    }

    public boolean isForwardsAllocation() {
        return Direction.FORWARD.equals(task.getAllocationDirection());
    }

    private void calculateEndDateOrStartDateAllocation() {
        List<ResourcesPerDayModification> allocations = AllocationRow
                .createAndAssociate(task, currentRows);
        ResourceAllocation.allocating(allocations).untilAllocating(
                task.getAllocationDirection(),
                formBinder.getAssignedHours(), notFullfiledReceiver());
    }

    private INotFulfilledReceiver notFullfiledReceiver() {
        return new INotFulfilledReceiver() {

            @Override
            public void cantFulfill(ResourcesPerDayModification attempt,
                    CapacityResult capacityResult) {
                final AllocationRow row = findRowFor(attempt.getBeingModified());
                row.markNoCapacity(attempt, capacityResult);
            }
        };
    }

    private AllocationRow findRowFor(ResourceAllocation<?> resourceAllocation) {
        return AllocationRow.find(currentRows, resourceAllocation);
    }

    private void calculateResourcesPerDayAllocation() {
        List<HoursModification> hours = AllocationRow
                .createHoursModificationsAndAssociate(task, currentRows);
        if (isForwardsAllocation()) {
            ResourceAllocation.allocatingHours(hours).allocateUntil(
                    formBinder.getAllocationEnd());
        } else {
            ResourceAllocation.allocatingHours(hours).allocateFromEndUntil(
                    formBinder.getAllocationStart());
        }
    }

    private Integer getWorkableDaysIfApplyable() {
        switch (calculatedValue) {
        case NUMBER_OF_HOURS:
        case RESOURCES_PER_DAY:
            return formBinder.getWorkableDays();
        case END_DATE:
            return null;
        default:
            throw new RuntimeException("unexpected calculatedValue: "
                    + calculatedValue);
        }
    }

    private void createDerived() {
        List<ResourceAllocation<?>> lastFrom = AllocationRow
                .getTemporalFrom(currentRows);
        for (ResourceAllocation<?> each : lastFrom) {
            each.createDerived(workersFinder);
        }
    }

    public FormBinder createFormBinder(Scenario currentScenario,
            IResourceAllocationModel resourceAllocationModel) {
        if (formBinder != null) {
            throw new IllegalStateException(
                    "there is already a binder associated with this object");
        }
        formBinder = new FormBinder(currentScenario, this,
                resourceAllocationModel);
        return formBinder;
    }

    public CalculatedValue getCalculatedValue() {
        return this.calculatedValue;
    }

    public void setCalculatedValue(CalculatedValue calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    public AllocationResult getInitialAllocation(Scenario currentScenario) {
        return AllocationResult.createCurrent(currentScenario, task);
    }


    public Task getTask() {
        return task;
    }

    public Set<Resource> getAllocationResources() {
        Set<Resource> result = new HashSet<Resource>();
        for (AllocationRow each : currentRows) {
            result.addAll(each.getAssociatedResources());
        }
        return result;
    }

    public LocalDate getStartDate() {
        return new LocalDate(task.getStartDate());
    }

    public void removeAll() {
        for (AllocationRow each : copyOfCurrentRowsToAvoidConcurrentModification()) {
            remove(each);
        }
    }

    private ArrayList<AllocationRow> copyOfCurrentRowsToAvoidConcurrentModification() {
        return new ArrayList<AllocationRow>(currentRows);
    }
}