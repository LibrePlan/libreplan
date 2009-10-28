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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;

/**
 * Computes aggregate values on a set{@link ResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AggregateOfResourceAllocations {

    private Set<ResourceAllocation<?>> resourceAllocations;

    public AggregateOfResourceAllocations(
            Collection<? extends ResourceAllocation<?>> allocations) {
        Validate.notNull(allocations);
        Validate.noNullElements(allocations);
        this.resourceAllocations = new HashSet<ResourceAllocation<?>>(
                allocations);
    }

    public int getTotalHours() {
        int sum = 0;
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            sum += resourceAllocation.getAssignedHours();
        }
        return sum;
    }

    public Map<ResourceAllocation<?>, ResourcesPerDay> getResourcesPerDay() {
        HashMap<ResourceAllocation<?>, ResourcesPerDay> result = new HashMap<ResourceAllocation<?>, ResourcesPerDay>();
        for (ResourceAllocation<?> r : resourceAllocations) {
            result.put(r, r.getResourcesPerDay());
        }
        return result;
    }

    public boolean isEmpty() {
        return resourceAllocations.isEmpty();
    }

    public List<ResourceAllocation<?>> getAllocationsSortedByStartDate() {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>(
                resourceAllocations);
        return ResourceAllocation.sortedByStartDate(result);
    }

    public int hoursBetween(LocalDate startDate, LocalDate endDate) {
        int sum = 0;
        for (ResourceAllocation<?> r : resourceAllocations) {
            sum += r.getAssignedHours(startDate, endDate);
        }
        return sum;
    }

    public LocalDate getStart() {
        if(isEmpty()){
            throw new IllegalStateException("the aggregate is empty");
        }
        return getAllocationsSortedByStartDate().get(0).getStartDate();
    }

    public LocalDate getEnd(){
        if(isEmpty()){
            throw new IllegalStateException("the aggregate is empty");
        }
        LocalDate result = null;
        for (ResourceAllocation<?> allocation : resourceAllocations) {
            result = bigger(allocation.getEndDate(), result);
        }
        return result;
    }

    private LocalDate bigger(LocalDate one, LocalDate other) {
        if (one == null) {
            return other;
        }
        if (other == null) {
            return one;
        }
        return one.compareTo(other) > 0 ? one : other;
    }

    public List<SpecificResourceAllocation> getSpecificAllocations() {
        return ResourceAllocation.getOfType(SpecificResourceAllocation.class,
                resourceAllocations);
    }

    public List<GenericResourceAllocation> getGenericAllocations() {
        return ResourceAllocation.getOfType(GenericResourceAllocation.class,
                resourceAllocations);
    }

}
