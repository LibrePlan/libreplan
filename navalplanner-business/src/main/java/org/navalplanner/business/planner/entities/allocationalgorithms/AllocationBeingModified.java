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

package org.navalplanner.business.planner.entities.allocationalgorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;

public class AllocationBeingModified {

    public static List<AllocationBeingModified> fromExistent(
            Collection<? extends ResourceAllocation<?>> allocations) {
        List<AllocationBeingModified> result = new ArrayList<AllocationBeingModified>();
        for (ResourceAllocation<?> resourceAllocation : allocations) {
            ResourcesPerDay perDay = resourceAllocation
                    .getResourcesPerDay();
            Validate.notNull(perDay);
            result.add(new AllocationBeingModified(
                    resourceAllocation, perDay));
        }
        return result;
    }

    private final ResourceAllocation<?> beingModified;

    private final ResourcesPerDay goal;

    public AllocationBeingModified(
            ResourceAllocation<?> resourceAllocation,
            ResourcesPerDay resourcesPerDay) {
        this.beingModified = resourceAllocation;
        this.goal = resourcesPerDay;
    }

    public ResourceAllocation<?> getBeingModified() {
        return beingModified;
    }

    public ResourcesPerDay getGoal() {
        return goal;
    }

    public static List<ResourceAllocation<?>> stripResourcesPerDay(
            Collection<AllocationBeingModified> withResourcesPerDay) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        for (AllocationBeingModified r : withResourcesPerDay) {
            result.add(r.getBeingModified());
        }
        return result;
    }
}