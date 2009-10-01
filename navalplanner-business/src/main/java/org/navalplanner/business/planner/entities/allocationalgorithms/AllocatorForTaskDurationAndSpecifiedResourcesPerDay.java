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

import java.util.List;

import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.resources.entities.Resource;

public class AllocatorForTaskDurationAndSpecifiedResourcesPerDay {

    private List<ResourceAllocationWithDesiredResourcesPerDay> allocations;

    private List<Resource> resources;

    public AllocatorForTaskDurationAndSpecifiedResourcesPerDay(
            List<ResourceAllocationWithDesiredResourcesPerDay> allocations,
            List<Resource> resources) {
        this.allocations = allocations;
        this.resources = resources;
    }

    public void allocateOnTaskLength() {
        for (ResourceAllocationWithDesiredResourcesPerDay allocation : allocations) {
            doAllocationForFixedTask(allocation.getResourceAllocation(),
                    allocation.getResourcesPerDay());
        }
    }

    private void doAllocationForFixedTask(ResourceAllocation<?> allocation,
            ResourcesPerDay resourcesPerDay) {
        if (allocation instanceof GenericResourceAllocation) {
            doAllocation((GenericResourceAllocation) allocation,
                    resourcesPerDay);
        } else {
            SpecificResourceAllocation specific = (SpecificResourceAllocation) allocation;
            doAllocation(specific, resourcesPerDay);
        }
    }

    private void doAllocation(SpecificResourceAllocation specific,
            ResourcesPerDay resourcesPerDay) {
        specific.allocate(resourcesPerDay);
    }

    private void doAllocation(GenericResourceAllocation generic,
            ResourcesPerDay resourcesPerDay) {
        generic.forResources(resources).allocate(resourcesPerDay);
    }

}
