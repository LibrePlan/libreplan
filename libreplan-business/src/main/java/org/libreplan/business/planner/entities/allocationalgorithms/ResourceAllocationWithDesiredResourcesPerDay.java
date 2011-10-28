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

package org.libreplan.business.planner.entities.allocationalgorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.workingday.ResourcesPerDay;

public class ResourceAllocationWithDesiredResourcesPerDay {

    public static List<ResourceAllocationWithDesiredResourcesPerDay> fromExistent(
            Collection<? extends ResourceAllocation<?>> allocations) {
        List<ResourceAllocationWithDesiredResourcesPerDay> result = new ArrayList<ResourceAllocationWithDesiredResourcesPerDay>();
        for (ResourceAllocation<?> resourceAllocation : allocations) {
            ResourcesPerDay perDay = resourceAllocation
                    .getResourcesPerDay();
            Validate.notNull(perDay);
            result.add(new ResourceAllocationWithDesiredResourcesPerDay(
                    resourceAllocation, perDay));
        }
        return result;
    }

    private final ResourceAllocation<?> resourceAllocation;

    private final ResourcesPerDay resourcesPerDay;

    public ResourceAllocationWithDesiredResourcesPerDay(
            ResourceAllocation<?> resourceAllocation,
            ResourcesPerDay resourcesPerDay) {
        this.resourceAllocation = resourceAllocation;
        this.resourcesPerDay = resourcesPerDay;
    }

    public ResourceAllocation<?> getResourceAllocation() {
        return resourceAllocation;
    }

    public ResourcesPerDay getResourcesPerDay() {
        return resourcesPerDay;
    }

    public static List<ResourceAllocation<?>> stripResourcesPerDay(
            List<ResourceAllocationWithDesiredResourcesPerDay> withResourcesPerDay) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        for (ResourceAllocationWithDesiredResourcesPerDay r : withResourcesPerDay) {
            result.add(r.getResourceAllocation());
        }
        return result;
    }
}