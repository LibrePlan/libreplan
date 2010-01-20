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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.DerivedAllocationGenerator.IWorkerFinder;
import org.navalplanner.business.planner.entities.allocationalgorithms.HoursModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;

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

    private Integer daysDuration;

    private final IWorkerFinder workersFinder;

    private AllocationRowsHandler(Task task, List<AllocationRow> initialRows,
            IWorkerFinder workersFinder) {
        this.task = task;
        this.workersFinder = workersFinder;
        this.currentRows = new ArrayList<AllocationRow>(initialRows);
        this.calculatedValue = task.getCalculatedValue();
        this.daysDuration = task.getDaysDuration();
    }

    public void addSpecificResourceAllocationFor(List<Resource> resource) {
        List<Resource> alreadyPresent = new ArrayList<Resource>();
        for (Resource each : resource) {
            if (alreadyExistsAllocationFor(each)) {
                alreadyPresent.add(each);
            } else {
                currentRows.add(SpecificAllocationRow.forResource(each));
                formBinder.newAllocationAdded();
            }
        }
        if (!alreadyPresent.isEmpty()) {
            formBinder.markRepeatedResources(alreadyPresent);
        }
    }

    public void addGeneric(Set<Criterion> criterions,
            Collection<? extends Resource> resourcesMatched) {
        addGeneric(criterions, resourcesMatched, null);
    }

    public void addGeneric(Set<Criterion> criterions,
            Collection<? extends Resource> resourcesMatched, Integer hours) {
        if (resourcesMatched.isEmpty()) {
            formBinder.markNoWorkersMatchedByCriterions(criterions);
        } else {
            GenericAllocationRow genericAllocationRow = GenericAllocationRow
                    .create(criterions, resourcesMatched);
            if (hours != null) {
                genericAllocationRow.setHoursToInput(hours);
            }
            if (alreadyExistsAllocationFor(criterions)) {
                formBinder.markThereisAlreadyAssignmentWith(criterions);
            } else {
                currentRows.add(genericAllocationRow);
                formBinder.newAllocationAdded();
            }
        }
    }

    public List<AllocationRow> getCurrentRows() {
        return currentRows;
    }

    private boolean alreadyExistsAllocationFor(Resource resource) {
        return !getAllocationsFor(resource).isEmpty();
    }

    private boolean alreadyExistsAllocationFor(Set<Criterion> criterions) {
        List<GenericAllocationRow> generic = AllocationRow
                .getGeneric(getCurrentRows());
        for (GenericAllocationRow each : generic) {
            if (each.hasSameCriterions(criterions)) {
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
    }

    public Set<ResourceAllocation<?>> getAllocationsRequestedToRemove() {
        return requestedToRemove;
    }

    public void checkInvalidValues() {
        if (calculatedValue != CalculatedValue.NUMBER_OF_HOURS
                && !currentRows.isEmpty() && formBinder.getAssignedHours() <= 0) {
            formBinder.markAssignedHoursMustBePositive();
        }
        if (calculatedValue != CalculatedValue.END_DATE
                && formBinder.getAllocationEnd().isBefore(
                new LocalDate(task.getStartDate()))) {
            formBinder.markEndDateMustBeAfterStartDate();
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
                calculateEndDateAllocation();
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
                calculatedValue, currentRows);
        daysDuration = result.getDaysDuration();
        AllocationRow.loadDataFromLast(currentRows);
        return result;
    }

    private void calculateNumberOfHoursAllocation() {
        List<ResourcesPerDayModification> allocations = AllocationRow
                .createAndAssociate(task, currentRows);
        ResourceAllocation.allocating(allocations).allocateUntil(
                formBinder.getAllocationEnd());
    }

    private void calculateEndDateAllocation() {
        List<ResourcesPerDayModification> allocations = AllocationRow
                .createAndAssociate(task, currentRows);
        ResourceAllocation.allocating(allocations).untilAllocating(
                formBinder.getAssignedHours());
    }

    private void calculateResourcesPerDayAllocation() {
        List<HoursModification> hours = AllocationRow
                .createHoursModificationsAndAssociate(task, currentRows);
        ResourceAllocation.allocatingHours(hours).allocateUntil(
                formBinder.getAllocationEnd());
    }

    private void createDerived() {
        List<ResourceAllocation<?>> lastFrom = AllocationRow
                .getTemporalFrom(currentRows);
        for (ResourceAllocation<?> each : lastFrom) {
            each.createDerived(workersFinder);
        }
    }

    public FormBinder createFormBinder(
            IResourceAllocationModel resourceAllocationModel) {
        if (formBinder != null) {
            throw new IllegalStateException(
                    "there is already a binder associated with this object");
        }
        formBinder = new FormBinder(this, resourceAllocationModel);
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
        return AllocationResult.createCurrent(task);
    }


    public Task getTask() {
        return task;
    }

    public Integer getDaysDuration() {
        return daysDuration;
    }

    public Set<Resource> getAllocationResources() {
        Set<Resource> result = new HashSet<Resource>();
        for (AllocationRow each : currentRows) {
            result.addAll(each.getAssociatedResources());
        }
        return result;
    }

    public Date getEnd() {
        LocalDate start = getStartDate();
        return toDate(start.plusDays(getDaysDuration()));
    }

    public LocalDate getStartDate() {
        return new LocalDate(task.getStartDate());
    }

    private Date toDate(LocalDate date) {
        return date.toDateTimeAtStartOfDay().toDate();
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