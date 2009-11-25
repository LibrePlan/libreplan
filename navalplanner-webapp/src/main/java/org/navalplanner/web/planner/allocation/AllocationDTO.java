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
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.AllocationBeingModified;
import org.navalplanner.business.resources.entities.Resource;

/**
 * The information that must be introduced to create a
 * {@link ResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class AllocationDTO {

    public static List<GenericAllocationDTO> getGeneric(
            Collection<? extends AllocationDTO> all) {
        List<GenericAllocationDTO> result = new ArrayList<GenericAllocationDTO>();
        for (AllocationDTO dto : all) {
            if (dto.isGeneric()) {
                result.add((GenericAllocationDTO) dto);
            }
        }
        return result;
    }

    public static List<AllocationDTO> toDTOs(
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        List<AllocationDTO> result = new ArrayList<AllocationDTO>();
        result.addAll(GenericAllocationDTO
                .toGenericAllocations(resourceAllocations));
        result.addAll(SpecificAllocationDTO
                .toSpecificAllocations(resourceAllocations));
        return result;
    }

    private ResourceAllocation<?> origin;

    private String name;

    private ResourcesPerDay resourcesPerDay;

    public abstract AllocationBeingModified toAllocationBeingModified(
            Task task);

    public boolean isCreating() {
        return origin == null;
    }

    public boolean isModifying() {
        return origin != null;
    }

    public ResourceAllocation<?> getOrigin() {
        return origin;
    }

    protected void setOrigin(ResourceAllocation<?> allocation) {
        this.origin = allocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourcesPerDay getResourcesPerDay() {
        return this.resourcesPerDay;
    }

    public void setResourcesPerDay(ResourcesPerDay resourcesPerDay) {
        this.resourcesPerDay = resourcesPerDay;
    }

    public abstract boolean isGeneric();

    public boolean isEmptyResourcesPerDay() {
        return getResourcesPerDay().isZero();
    }

    public abstract List<Resource> getAssociatedResources();

}
