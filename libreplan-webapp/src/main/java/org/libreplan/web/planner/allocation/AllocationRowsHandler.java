/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.web.planner.allocation;

import static org.libreplan.business.workingday.EffortDuration.hours;
import static org.libreplan.business.workingday.EffortDuration.zero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.CapacityResult;
import org.libreplan.business.common.Flagged;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.planner.entities.AssignmentFunction;
import org.libreplan.business.planner.entities.CalculatedValue;
import org.libreplan.business.planner.entities.DerivedAllocationGenerator.IWorkerFinder;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation.AllocationsSpecified.INotFulfilledReceiver;
import org.libreplan.business.planner.entities.ResourceAllocation.Direction;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.allocationalgorithms.AllocationModification;
import org.libreplan.business.planner.entities.allocationalgorithms.EffortModification;
import org.libreplan.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.EffortDuration.IEffortFrom;
import org.libreplan.business.workingday.IntraDayDate;

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
                        .forResource(getCalculatedValue(), each);
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

    public boolean addGeneric(ResourceEnum resourceType,
            Collection<? extends Criterion> criteria,
            Collection<? extends Resource> resourcesMatched, Integer hours) {
        if (resourcesMatched.isEmpty()) {
            formBinder.markNoResourcesMatchedByCriterions(resourceType,
                    criteria);
            return false;
        } else {
            GenericAllocationRow genericAllocationRow = GenericAllocationRow
                    .create(getCalculatedValue(), resourceType, criteria,
                            resourcesMatched);
            if (hours != null) {
                genericAllocationRow.setEffortToInput(hours(hours));
            } else {
                setupInitialHours(genericAllocationRow);
            }
            if (alreadyExistsAllocationFor(resourceType, criteria)) {
                formBinder.markThereisAlreadyAssignmentWith(resourceType,
                        criteria);
                return false;
            } else {
                currentRows.add(genericAllocationRow);
                formBinder.newAllocationAdded();
                return true;
            }
        }
    }

    private void setupInitialHours(AllocationRow allocationRow) {
        EffortDuration effortCalculated = calculateEffort(allocationRow);
        if (effortCalculated != null) {
            allocationRow.setEffortToInput(effortCalculated);
        }
    }

    private EffortDuration calculateEffort(AllocationRow allocationRow) {
        ResourceEnum type = allocationRow.getType();
        if (notStillExistAnotherAllocation(type)) {
            return calculateEffortByCalculatedValue(type);
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

    private EffortDuration calculateEffortByCalculatedValue(ResourceEnum type) {
        switch (calculatedValue) {
            case NUMBER_OF_HOURS:
                break;
            case END_DATE:
            case RESOURCES_PER_DAY:
            return calculateEffortByResourceType(type);
        }
        return null;
    }

    private EffortDuration calculateEffortByResourceType(final ResourceEnum type) {
        return EffortDuration.sum(task.getTaskSource().getHoursGroups(),
                new IEffortFrom<HoursGroup>() {

                    @Override
                    public EffortDuration from(HoursGroup each) {
                        if (type.equals(each.getResourceType())) {
                            return hours(each.getWorkingHours());
                        }
                        return zero();
                    }
                });
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
                && !currentRows.isEmpty()
                && formBinder.getAssignedEffort().compareTo(zero()) <= 0) {
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

    public enum Warnings {
        SOME_GOALS_NOT_FULFILLED;
    }

    public Flagged<AllocationResult, Warnings> doAllocation() {
        checkInvalidValues();
        if (!currentRows.isEmpty()) {
            List<? extends AllocationModification> modificationsDone;
            modificationsDone = doSuitableAllocation();

            AllocationRow.loadDataFromLast(currentRows, modificationsDone);

            createDerived();
            AllocationResult result = createResult();
            if (AllocationModification.allFullfiled(AllocationModification
                    .ofType(EffortModification.class, modificationsDone))) {
                return Flagged.justValue(result);
            } else {
                return Flagged.withFlags(result,
                        Warnings.SOME_GOALS_NOT_FULFILLED);
            }
        }
        return Flagged.justValue(createResult());
    }

    private AllocationResult createResult() {
        return AllocationResult.create(task,
                calculatedValue, currentRows, getWorkableDaysIfApplyable());
    }

    private List<? extends AllocationModification> doSuitableAllocation() {
        List<? extends AllocationModification> allocationModifications;
        switch (calculatedValue) {
        case NUMBER_OF_HOURS:
            allocationModifications = calculateNumberOfHoursAllocation();
            break;
        case END_DATE:
            allocationModifications = calculateEndDateOrStartDateAllocation();
            break;
        case RESOURCES_PER_DAY:
            allocationModifications = calculateResourcesPerDayAllocation();
            break;
        default:
            throw new RuntimeException("cant handle: " + calculatedValue);
        }

        AssignmentFunction.applyAssignmentFunctionsIfAny(AllocationModification
                .getBeingModified(allocationModifications));
        return allocationModifications;
    }

    private List<ResourcesPerDayModification> calculateNumberOfHoursAllocation() {
        List<ResourcesPerDayModification> allocations = AllocationRow
                .createAndAssociate(task, currentRows, requestedToRemove);
        if (isForwardsAllocation()) {
            ResourceAllocation.allocating(allocations).allocateUntil(
                    formBinder.getAllocationEnd());
        } else {
            ResourceAllocation.allocating(allocations).allocateFromEndUntil(
                    formBinder.getAllocationStart());
        }
        return allocations;
    }

    public boolean isForwardsAllocation() {
        return Direction.FORWARD.equals(task.getAllocationDirection());
    }

    private List<ResourcesPerDayModification> calculateEndDateOrStartDateAllocation() {
        List<ResourcesPerDayModification> allocations = AllocationRow
                .createAndAssociate(task, currentRows, requestedToRemove);
        ResourceAllocation.allocating(allocations).untilAllocating(
                task.getAllocationDirection(),
                formBinder.getAssignedEffort(),
                notFullfiledReceiver());
        return allocations;
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

    private List<EffortModification> calculateResourcesPerDayAllocation() {
        List<EffortModification> hours = AllocationRow
                .createHoursModificationsAndAssociate(task, currentRows,
                        requestedToRemove);
        if (isForwardsAllocation()) {
            ResourceAllocation.allocatingHours(hours).allocateUntil(
                    IntraDayDate.startOfDay(formBinder.getAllocationEnd()));
        } else {
            ResourceAllocation.allocatingHours(hours).allocateFromEndUntil(
                    IntraDayDate.startOfDay(formBinder.getAllocationStart()));
        }
        return hours;
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
        for (ResourceAllocation<?> each : requestedToRemove) {
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