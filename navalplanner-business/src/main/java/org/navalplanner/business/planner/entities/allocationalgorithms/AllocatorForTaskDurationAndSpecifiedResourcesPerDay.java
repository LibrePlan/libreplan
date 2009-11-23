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

    private List<AllocationBeingModified> allocations;

    public AllocatorForTaskDurationAndSpecifiedResourcesPerDay(
            List<AllocationBeingModified> allocations) {
        this.allocations = allocations;
    }

    public void allocateOnTaskLength() {
        for (AllocationBeingModified allocation : allocations) {
            doAllocationForFixedTask(allocation);
        }
    }

    private void doAllocationForFixedTask(
            AllocationBeingModified allocationBeingModified) {
        ResourceAllocation<?> allocation = allocationBeingModified
                .getBeingModified();
        ResourcesPerDay resourcesPerDay = allocationBeingModified.getGoal();
        if (allocation instanceof GenericResourceAllocation) {
            doAllocation((GenericResourceAllocation) allocation,
                    resourcesPerDay, allocationBeingModified.getResources());
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
            ResourcesPerDay resourcesPerDay, List<Resource> resources) {
        generic.forResources(resources).allocate(resourcesPerDay);
    }

}
