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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.navalplanner.business.planner.entities.allocationalgorithms.AllocationModification;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class AllocationResult {

    private static Map<ResourceAllocation<?>, ResourceAllocation<?>> translation(
            Map<? extends AllocationModification, ResourceAllocation<?>> fromDetachedToAttached) {
        Map<ResourceAllocation<?>, ResourceAllocation<?>> result = new HashMap<ResourceAllocation<?>, ResourceAllocation<?>>();
        for (Entry<? extends AllocationModification, ResourceAllocation<?>> entry : fromDetachedToAttached
                .entrySet()) {
            result
                    .put(entry.getKey().getBeingModified(), entry
                            .getValue());
        }
        return result;
    }

    private static List<Task.ModifiedAllocation> calculateModified(
            Map<ResourceAllocation<?>, ResourceAllocation<?>> fromDetachedAllocationToAttached) {
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

    private static List<ResourceAllocation<?>> calculateNew(
            Map<ResourceAllocation<?>, ResourceAllocation<?>> fromDetachedToAttached) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        for (Entry<ResourceAllocation<?>, ResourceAllocation<?>> entry : fromDetachedToAttached
                .entrySet()) {
            if (entry.getValue() == null) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public static AllocationResult create(
            Task task,
            CalculatedValue calculatedValue,
            AggregateOfResourceAllocations aggregate,
            Map<? extends AllocationModification, ResourceAllocation<?>> fromDetachedAllocationToAttached) {
        Map<ResourceAllocation<?>, ResourceAllocation<?>> translation = translation(fromDetachedAllocationToAttached);
        return new AllocationResult(task, calculatedValue, aggregate,
                calculateNew(translation), calculateModified(translation));
    }

    public static AllocationResult createCurrent(Task task) {
        Set<ResourceAllocation<?>> resourceAllocations = task
                .getResourceAllocations();
        List<ModifiedAllocation> modifiedAllocations = ModifiedAllocation
                .copy(resourceAllocations);
        AggregateOfResourceAllocations aggregate = new AggregateOfResourceAllocations(
                ModifiedAllocation.modified(modifiedAllocations));
        return new AllocationResult(task, task.getCalculatedValue(), aggregate,
                Collections.<ResourceAllocation<?>> emptyList(),
                modifiedAllocations);

    }

    private final AggregateOfResourceAllocations aggregate;

    private final Integer daysDuration;

    private final CalculatedValue calculatedValue;

    private final Task task;

    private final List<ResourceAllocation<?>> newAllocations;

    private final List<Task.ModifiedAllocation> modified;

    private AllocationResult(
            Task task,
            CalculatedValue calculatedValue,
            AggregateOfResourceAllocations aggregate,
            List<ResourceAllocation<?>> newAllocations,
            List<Task.ModifiedAllocation> modified) {
        Validate.notNull(aggregate);
        Validate.notNull(calculatedValue);
        Validate.notNull(task);
        this.task = task;
        this.calculatedValue = calculatedValue;
        this.aggregate = aggregate;
        this.daysDuration = aggregate.isEmpty() ? task.getDaysDuration()
                : aggregate.getDaysDuration();
        this.newAllocations = newAllocations;
        this.modified = modified;
    }

    public AggregateOfResourceAllocations getAggregate() {
        return aggregate;
    }

    public Integer getDaysDuration() {
        return daysDuration;
    }

    public List<ResourceAllocation<?>> getNew() {
        return newAllocations;
    }

    public List<Task.ModifiedAllocation> getModified() {
        return modified;
    }

    public CalculatedValue getCalculatedValue() {
        return calculatedValue;
    }

    public void applyTo(Task task) {
        List<ModifiedAllocation> modified = getModified();
        task.mergeAllocation(getCalculatedValue(), aggregate, getNew(),
                modified, getNotModified(originals(modified)));
    }

    private List<ResourceAllocation<?>> originals(
            List<ModifiedAllocation> modified) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        for (ModifiedAllocation modifiedAllocation : modified) {
            result.add(modifiedAllocation.getOriginal());
        }
        return result;
    }

    private Set<ResourceAllocation<?>> getNotModified(
            List<ResourceAllocation<?>> modified) {
        Set<ResourceAllocation<?>> all = new HashSet<ResourceAllocation<?>>(
                task.getResourceAllocations());
        all.removeAll(modified);
        return all;
    }

    public List<ResourceAllocation<?>> getAllSortedByStartDate() {
        return aggregate.getAllocationsSortedByStartDate();
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
