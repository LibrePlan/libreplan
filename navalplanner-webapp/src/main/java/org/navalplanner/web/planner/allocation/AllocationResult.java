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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.Task.ModifiedAllocation;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourceAllocationWithDesiredResourcesPerDay;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class AllocationResult {

    private static Map<ResourceAllocation<?>, ResourceAllocation<?>> translation(
            Map<ResourceAllocationWithDesiredResourcesPerDay, ResourceAllocation<?>> fromDetachedToAttached) {
        Map<ResourceAllocation<?>, ResourceAllocation<?>> result = new HashMap<ResourceAllocation<?>, ResourceAllocation<?>>();
        for (Entry<ResourceAllocationWithDesiredResourcesPerDay, ResourceAllocation<?>> entry : fromDetachedToAttached
                .entrySet()) {
            result
                    .put(entry.getKey().getResourceAllocation(), entry
                            .getValue());
        }
        return result;
    }

    private final AggregateOfResourceAllocations aggregate;

    private final Integer daysDuration;

    private final Map<ResourceAllocation<?>, ResourceAllocation<?>> fromDetachedAllocationToAttached;

    private final CalculatedValue calculatedValue;

    private List<ResourceAllocation<?>> allSortedByStartDate;

    private final Task task;

    AllocationResult(
            Task task,
            CalculatedValue calculatedValue,
            AggregateOfResourceAllocations aggregate,
            Integer daysDuration,
            Map<ResourceAllocationWithDesiredResourcesPerDay, ResourceAllocation<?>> fromDetachedAllocationToAttached) {
        Validate.notNull(daysDuration);
        Validate.notNull(aggregate);
        Validate.notNull(calculatedValue);
        Validate.notNull(task);
        this.task = task;
        this.calculatedValue = calculatedValue;
        this.aggregate = aggregate;
        this.daysDuration = daysDuration;
        this.fromDetachedAllocationToAttached = translation(fromDetachedAllocationToAttached);
    }

    public AggregateOfResourceAllocations getAggregate() {
        return aggregate;
    }

    public Integer getDaysDuration() {
        return daysDuration;
    }

    public List<ResourceAllocation<?>> getNew() {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        for (Entry<ResourceAllocation<?>, ResourceAllocation<?>> entry : fromDetachedAllocationToAttached
                .entrySet()) {
            if (entry.getValue() == null) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public List<Task.ModifiedAllocation> getModified() {
        List<ModifiedAllocation> result = new ArrayList<ModifiedAllocation>();
        for (Entry<ResourceAllocation<?>, ResourceAllocation<?>> entry : fromDetachedAllocationToAttached
                .entrySet()) {
            if (entry.getValue() != null) {
                result.add(new ModifiedAllocation(entry.getValue(), entry
                        .getKey()));
            }
        }
        return result;
    }

    public CalculatedValue getCalculatedValue() {
        return calculatedValue;
    }

    public void applyTo(Task task) {
        task.mergeAllocation(getCalculatedValue(), getDaysDuration(), getNew(),
                getModified());
    }

    public List<ResourceAllocation<?>> getAllSortedByStartDate() {
        if (allSortedByStartDate != null) {
            return allSortedByStartDate;
        }
        return allSortedByStartDate = aggregate
                .getAllocationsSortedByStartDate();
    }

    public Task getTask() {
        return task;
    }

    public List<GenericResourceAllocation> getGenericAllocations() {
        return onlyGeneric(getAllSortedByStartDate());
    }

    private List<GenericResourceAllocation> onlyGeneric(
            List<ResourceAllocation<?>> allocations) {
        List<GenericResourceAllocation> result = new ArrayList<GenericResourceAllocation>();
        for (ResourceAllocation<?> resourceAllocation : allocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                result.add((GenericResourceAllocation) resourceAllocation);
            }
        }
        return result;
    }

    public List<SpecificResourceAllocation> getSpecificAllocations() {
        return onlySpecific(getAllSortedByStartDate());
    }

    private List<SpecificResourceAllocation> onlySpecific(
            List<ResourceAllocation<?>> allocations) {
        List<SpecificResourceAllocation> result = new ArrayList<SpecificResourceAllocation>();
        for (ResourceAllocation<?> r : allocations) {
            if (r instanceof SpecificResourceAllocation) {
                result.add((SpecificResourceAllocation) r);
            }
        }
        return result;
    }

    public LocalDate getStart() {
        return new LocalDate(task.getStartDate().getTime());
    }

}
