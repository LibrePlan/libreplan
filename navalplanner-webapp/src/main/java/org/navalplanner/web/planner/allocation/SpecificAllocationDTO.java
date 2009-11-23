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
import java.util.Collection;
import java.util.List;

import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.resources.entities.Resource;

/**
 * The information required for creating a {@link SpecificResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class SpecificAllocationDTO extends AllocationDTO {

    public static List<SpecificAllocationDTO> toSpecificAllocations(
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        List<SpecificAllocationDTO> result = new ArrayList<SpecificAllocationDTO>();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                SpecificResourceAllocation specific = (SpecificResourceAllocation) resourceAllocation;
                result.add(from(specific));
            }
        }
        return result;
    }

    public static List<SpecificAllocationDTO> withResource(
            List<SpecificAllocationDTO> specific, Resource resource) {
        List<SpecificAllocationDTO> result = new ArrayList<SpecificAllocationDTO>();
        for (SpecificAllocationDTO specificAllocationDTO : specific) {
            if (areEquals(specificAllocationDTO.getResource(), resource)) {
                result.add(specificAllocationDTO);
            }
        }
        return result;
    }

    private static boolean areEquals(Resource one, Resource other) {
        if (one == other) {
            return true;
        }
        if (one == null || other == null) {
            return false;
        }
        return one.equals(other);
    }

    public static List<SpecificAllocationDTO> getSpecific(
            Collection<? extends AllocationDTO> currentAllocations) {
        List<SpecificAllocationDTO> result = new ArrayList<SpecificAllocationDTO>();
        for (AllocationDTO allocationDTO : currentAllocations) {
            if (allocationDTO instanceof SpecificAllocationDTO) {
                result.add((SpecificAllocationDTO) allocationDTO);
            }
        }
        return result;
    }

    public static SpecificAllocationDTO from(SpecificResourceAllocation specific) {
        SpecificAllocationDTO result = forResource(specific.getResource());
        result.setResourcesPerDay(specific.getResourcesPerDay());
        result.setOrigin(specific);
        return result;
    }

    public static SpecificAllocationDTO forResource(Resource resource) {
        SpecificAllocationDTO result = new SpecificAllocationDTO();
        result.setName(resource.getDescription());
        result.setResource(resource);
        result.setResourcesPerDay(ResourcesPerDay.amount(1));
        return result;
    }

    private Resource resource;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

}
