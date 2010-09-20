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
package org.navalplanner.web.planner.limiting.allocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.web.common.components.NewAllocationSelector.AllocationType;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class LimitingAllocationRow {

    public static final int DEFAULT_PRIORITY = 5;

    private ResourceAllocation<?> resourceAllocation;

    private int hours = 0;

    private int priority = DEFAULT_PRIORITY;

    private Task task;

    private Collection<? extends Resource> resources;

    public LimitingAllocationRow() {

    }

    public static List<LimitingAllocationRow> toRows(Task task) {
        List<LimitingAllocationRow> result = new ArrayList<LimitingAllocationRow>();
        for (ResourceAllocation<?> each : task.getLimitingResourceAllocations()) {
            result.add(new LimitingAllocationRow(each, task));
        }
        return result;
    }

    public static LimitingAllocationRow create(Resource resource, Task task) {
        return new LimitingAllocationRow(SpecificResourceAllocation.createForLimiting(resource, task), task);
    }

    private LimitingAllocationRow(ResourceAllocation<?> resourceAllocation,
            Task task) {
        init(resourceAllocation, task);
    }

    private void init(ResourceAllocation<?> resourceAllocation,
            Task task) {
        initializeIntentedTotalHoursIfNeeded(resourceAllocation, task);
        this.resourceAllocation = resourceAllocation;
        this.task = task;
        this.hours = resourceAllocation.getIntendedTotalHours();
        this.priority = task.getPriority();
    }

    /**
     * Sets resourceAllocation.intentedTotalHours to task.totalHours if null
     *
     * @param resourceAllocation
     * @param task
     */
    private void initializeIntentedTotalHoursIfNeeded(
            ResourceAllocation<?> resourceAllocation, Task task) {
        Integer intentedTotalHours = resourceAllocation.getIntendedTotalHours();
        if (intentedTotalHours == null) {
            resourceAllocation.setIntendedTotalHours(task.getTotalHours());
        }
    }

    public static LimitingAllocationRow create(ResourceEnum resourceType,
            Collection<? extends Criterion> criteria,
            Collection<? extends Resource> resources, Task task, int priority) {
        LimitingAllocationRow result = new LimitingAllocationRow(
                GenericResourceAllocation.create(task, resourceType, criteria),
                task, priority);
        result.setResources(resources);
        return result;
    }

    private void setResources(Collection<? extends Resource> resources) {
        this.resources = resources;
    }

    public static LimitingAllocationRow create(Resource resource, Task task,
            int priority) {
        return new LimitingAllocationRow(SpecificResourceAllocation.createForLimiting(
                resource, task), task, priority);
    }

    private LimitingAllocationRow(ResourceAllocation<?> resourceAllocation,
            Task task, int priority) {
        task.setPriority(priority);
        init(resourceAllocation, task);
    }

    public AllocationType getAllocationType() {
        return (resourceAllocation instanceof SpecificResourceAllocation) ? AllocationType.SPECIFIC
                : AllocationType.GENERIC_WORKERS;
    }

    public String getAllocationTypeStr() {
        return getAllocationType().toString();
    }

    public String getAllocation() {
        final AllocationType type = getAllocationType();
        if (AllocationType.GENERIC_WORKERS.equals(type)) {
            final GenericResourceAllocation generic = (GenericResourceAllocation) resourceAllocation;
            return Criterion.getCaptionForCriterionsFrom(generic);
        }
        if (AllocationType.SPECIFIC.equals(type)) {
            return formatResources(resourceAllocation.getAssociatedResources());
        }
        return "";
    }

    private String formatResources(List<Resource> resources) {
        List<String> resourcesNames = new ArrayList<String>();
        for (Resource each: resources) {
            resourcesNames.add(each.getName());
        }
        return StringUtils.join(resourcesNames, ",");
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
        updateHours(resourceAllocation, hours);
    }

    private void updateHours(ResourceAllocation<?> resourceAllocation, final int hours) {
        if (resourceAllocation != null) {
            resourceAllocation.setIntendedTotalHours(hours);
        }
    }

    public int getPriority() {
        return priority;
    }

    public String getPriorityStr() {
        return (new Integer(getPriority()).toString());
    }

    public void setPriorityStr(String priority) {
        this.priority = toNumber(priority);
        task.setPriority(this.priority);
    }

    private int toNumber(String str) {
        if (NumberUtils.isNumber(str)) {
            int result = NumberUtils.toInt(str);
            return (result >= 1 && result <= 10) ? result : 1;
        }
        return 1;
    }

    public ResourceAllocation<?> getResourceAllocation() {
        return resourceAllocation;
    }

    public Set<Long> getResourcesIds() {
        Set<Long> result = new HashSet<Long>();
        if (resources != null) {
            for (Resource each: resources) {
                result.add(each.getId());
            }
        }
        return result;
    }

    public Set<Long> getCriteriaIds() {
        Set<Long> result = new HashSet<Long>();

        if (resourceAllocation instanceof GenericResourceAllocation) {
            GenericResourceAllocation generic = (GenericResourceAllocation) resourceAllocation;
            for (Criterion each: generic.getCriterions()) {
                result.add(each.getId());
            }
        }
        return result;
    }

    public boolean isSpecific() {
        return resourceAllocation != null
                && resourceAllocation instanceof SpecificResourceAllocation;
    }

    public boolean isGeneric() {
        return resourceAllocation != null
                && resourceAllocation instanceof GenericResourceAllocation;
    }

    public boolean hasDayAssignments() {
        return resourceAllocation.hasAssignments();
    }

}
