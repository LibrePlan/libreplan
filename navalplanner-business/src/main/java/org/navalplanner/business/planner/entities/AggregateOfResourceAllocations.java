/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.workingday.IntraDayDate;
import org.navalplanner.business.workingday.ResourcesPerDay;

/**
 * Computes aggregate values on a set{@link ResourceAllocation}.
 * <p>
 * It only contains satisfied resource allocations
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AggregateOfResourceAllocations {

    public static AggregateOfResourceAllocations createFromSatisfied(
            Collection<? extends ResourceAllocation<?>> allocations) {
        return new AggregateOfResourceAllocations(
                ResourceAllocation.getSatisfied(allocations));
    }

    public static AggregateOfResourceAllocations createFromAll(
            Collection<? extends ResourceAllocation<?>> allocations) {
        return new AggregateOfResourceAllocations(allocations);
    }

    private Set<ResourceAllocation<?>> resourceAllocations;

    private AggregateOfResourceAllocations(
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

    public int getIntendedHours() {
        int sum = 0;
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            sum += resourceAllocation.getIntendedHours();
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

    private LocalDate getStartAsLocalDate() {
        IntraDayDate start = getStart();
        return start != null ? start.getDate() : null;
    }

    public IntraDayDate getStart() {
        if (isEmpty()) {
            throw new IllegalStateException("the aggregate is empty");
        }
        return getAllocationsSortedByStartDate().get(0).getIntraDayStartDate();
    }

    private LocalDate getEndAsLocalDate() {
        IntraDayDate end = getEnd();
        return end != null ? end.getDate() : null;
    }

    /**
     * Calculates the latest end of all the allocations of this aggregate
     *
     * @return
     * @throws IllegalStateException
     *             if the aggregate is empty
     */
    public IntraDayDate getEnd() {
        if (isEmpty()) {
            throw new IllegalStateException("the aggregate is empty");
        }
        IntraDayDate result = null;
        for (ResourceAllocation<?> allocation : resourceAllocations) {
            result = bigger(allocation.getIntraDayEndDate(), result);
        }
        return result;
    }

    public Integer getDaysDuration() {
        return Days.daysBetween(getStartAsLocalDate(), getEndAsLocalDate())
                .getDays();
    }

    private IntraDayDate bigger(IntraDayDate one, IntraDayDate other) {
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
