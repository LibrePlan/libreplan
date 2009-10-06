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

package org.navalplanner.business.planner.entities;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Task extends TaskElement {

    public static Task createTask(HoursGroup hoursGroup) {
        Task task = new Task(hoursGroup);
        task.setNewObject(true);
        return task;
    }

    @NotNull
    private HoursGroup hoursGroup;

    private CalculatedValue calculatedValue = CalculatedValue.END_DATE;

    private Set<ResourceAllocation<?>> resourceAllocations = new HashSet<ResourceAllocation<?>>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public Task() {

    }

    private Task(HoursGroup hoursGroup) {
        Validate.notNull(hoursGroup);
        this.hoursGroup = hoursGroup;
    }

    public HoursGroup getHoursGroup() {
        return this.hoursGroup;
    }

    public Set<Criterion> getCriterions() {
        return Collections.unmodifiableSet(this.hoursGroup.getCriterions());
    }

    public Integer getHoursSpecifiedAtOrder() {
        return hoursGroup.getWorkingHours();
    }

    public int getAssignedHours() {
        return new AggregateOfResourceAllocations(resourceAllocations)
                .getTotalHours();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public List<TaskElement> getChildren() {
        throw new UnsupportedOperationException();
    }

    public Set<ResourceAllocation<?>> getResourceAllocations() {
        return Collections.unmodifiableSet(resourceAllocations);
    }

    public void addResourceAllocation(ResourceAllocation<?> resourceAllocation) {
        if (!resourceAllocation.getTask().equals(this)) {
            throw new IllegalArgumentException(
                    "the resourceAllocation's task must be this task");
        }
        resourceAllocations.add(resourceAllocation);
    }

    public void removeResourceAllocation(
            ResourceAllocation<?> resourceAllocation) {
        resourceAllocations.remove(resourceAllocation);
    }

    public CalculatedValue getCalculatedValue() {
        if (calculatedValue == null) {
            return CalculatedValue.END_DATE;
        }
        return calculatedValue;
    }

    public void setCalculatedValue(CalculatedValue calculatedValue) {
        Validate.notNull(calculatedValue);
        this.calculatedValue = calculatedValue;
    }

    public void setDaysDuration(Integer duration) {
        Validate.notNull(duration);
        Validate.isTrue(duration >= 0);
        DateTime endDate = toDateTime(getStartDate()).plusDays(duration);
        setEndDate(endDate.toDate());
    }

    public Integer getDaysDuration() {
        Days daysBetween = Days.daysBetween(toDateTime(getStartDate()),
                toDateTime(getEndDate()));
        return daysBetween.getDays();
    }

    private DateTime toDateTime(Date startDate) {
        return new DateTime(startDate.getTime());
    }

    /**
     * Checks if there isn't any {@link Worker} repeated in the {@link Set} of
     * {@link ResourceAllocation} of this {@link Task}.
     * @return <code>true</code> if the {@link Task} is valid, that means there
     *         isn't any {@link Worker} repeated.
     */
    public boolean isValidResourceAllocationWorkers() {
        Set<Long> workers = new HashSet<Long>();

        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                Resource resource = ((SpecificResourceAllocation) resourceAllocation)
                        .getResource();
                if (resource != null) {
                    if (workers.contains(resource.getId())) {
                        return false;
                    } else {
                        workers.add(resource.getId());
                    }
                }
            }
        }

        return true;
    }

    @Override
    public Integer defaultWorkHours() {
        return hoursGroup.getWorkingHours();
    }

    public TaskGroup split(int... shares) {
        int totalSumOfHours = sum(shares);
        if (totalSumOfHours != getWorkHours())
            throw new IllegalArgumentException(
                    "the shares don't sum up the work hours");
        TaskGroup result = TaskGroup.create();
        result.copyPropertiesFrom(this);
        result.shareOfHours = this.shareOfHours;
        copyParenTo(result);
        for (int i = 0; i < shares.length; i++) {
            Task task = Task.createTask(hoursGroup);
            task.copyPropertiesFrom(this);
            result.addTaskElement(task);
            task.shareOfHours = shares[i];
        }
        copyDependenciesTo(result);
        return result;
    }

    private int sum(int[] shares) {
        int result = 0;
        for (int share : shares) {
            result += share;
        }
        return result;
    }

    public Set<GenericResourceAllocation> getGenericResourceAllocations() {
        Set<GenericResourceAllocation> result = new HashSet<GenericResourceAllocation>();

        Set<ResourceAllocation<?>> resourceAllocations = getResourceAllocations();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                result.add((GenericResourceAllocation) resourceAllocation);
            }
        }

        return result;
    }

    public Set<SpecificResourceAllocation> getSpecificResourceAllocations() {
        Set<SpecificResourceAllocation> result = new HashSet<SpecificResourceAllocation>();

        Set<ResourceAllocation<?>> resourceAllocations = getResourceAllocations();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                result.add((SpecificResourceAllocation) resourceAllocation);
            }
        }

        return result;
    }

    public void mergeAllocation(CalculatedValue calculatedValue,
            Integer daysDuration, List<ResourceAllocation<?>> newAllocations,
            Map<ResourceAllocation<?>, ResourceAllocation<?>> modified) {
        this.calculatedValue = calculatedValue;
        setDaysDuration(daysDuration);
        addAllocations(newAllocations);
        for (Entry<ResourceAllocation<?>, ResourceAllocation<?>> entry : modified
                .entrySet()) {
            ResourceAllocation<?> existent = entry.getValue();
            Validate.isTrue(resourceAllocations.contains(existent));
            ResourceAllocation<?> modifications = entry.getKey();
            existent.mergeAssignmentsAndResourcesPerDay(modifications);
        }
    }

    private void addAllocations(List<ResourceAllocation<?>> newAllocations) {
        for (ResourceAllocation<?> resourceAllocation : newAllocations) {
            addResourceAllocation(resourceAllocation);
        }
    }

}
