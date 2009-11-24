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

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.AllocationBeingModified;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.resourceload.ResourceLoadModel;

/**
 * The information required for creating a {@link GenericResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GenericAllocationDTO extends AllocationDTO {

    private static GenericAllocationDTO createDefault() {
        GenericAllocationDTO result = new GenericAllocationDTO();
        result.setName(_("Generic"));
        result.setResourcesPerDay(ResourcesPerDay.amount(0));
        return result;
    }

    public static GenericAllocationDTO create(Set<Criterion> criterions,
            Collection<? extends Resource> resources) {
        Validate.isTrue(!resources.isEmpty());
        Validate.notNull(criterions);
        GenericAllocationDTO result = createDefault();
        result.criterions = criterions;
        result.resources = new ArrayList<Resource>(resources);
        result.setName(ResourceLoadModel.getName(criterions));
        return result;
    }

    public static GenericAllocationDTO from(
            GenericResourceAllocation resourceAllocation) {
        GenericAllocationDTO result = createDefault();
        result.setResourcesPerDay(resourceAllocation.getResourcesPerDay());
        result.setOrigin(resourceAllocation);
        result.criterions = resourceAllocation.getCriterions();
        result.resources = resourceAllocation.getAssociatedResources();
        result.setName(ResourceLoadModel.getName(result.criterions));
        return result;
    }

    private Set<Criterion> criterions;
    private List<Resource> resources;

    @Override
    public boolean isGeneric() {
        return true;
    }

    public static Collection<GenericAllocationDTO> toGenericAllocations(
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        ArrayList<GenericAllocationDTO> result = new ArrayList<GenericAllocationDTO>();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                result.add(from((GenericResourceAllocation) resourceAllocation));
            }
        }
        return result;
    }

    @Override
    public AllocationBeingModified toAllocationBeingModified(Task task) {
        GenericResourceAllocation genericResourceAllocation = GenericResourceAllocation
                .create(task, criterions);
        return AllocationBeingModified.create(genericResourceAllocation,
                getResourcesPerDay(), this.resources);
    }

    public boolean hasSameCriterions(Set<Criterion> criterions) {
        return this.criterions.equals(criterions);
    }
}
