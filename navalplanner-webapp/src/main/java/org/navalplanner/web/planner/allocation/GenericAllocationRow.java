/*
 * This file is part of NavalPlan
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
import org.navalplanner.business.planner.entities.allocationalgorithms.HoursModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.resourceload.ResourceLoadModel;

/**
 * The information required for creating a {@link GenericResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GenericAllocationRow extends AllocationRow {

    private static GenericAllocationRow createDefault() {
        GenericAllocationRow result = new GenericAllocationRow();
        result.setName(_("Generic"));
        result.setResourcesPerDay(ResourcesPerDay.amount(0));
        return result;
    }

    public static GenericAllocationRow create(Set<Criterion> criterions,
            Collection<? extends Resource> resources) {
        Validate.isTrue(!resources.isEmpty());
        Validate.notNull(criterions);
        GenericAllocationRow result = createDefault();
        result.criterions = criterions;
        result.resources = new ArrayList<Resource>(resources);
        result.setName(ResourceLoadModel.getName(criterions));
        return result;
    }

    public static GenericAllocationRow from(
            GenericResourceAllocation resourceAllocation) {
        GenericAllocationRow result = createDefault();
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

    public static Collection<GenericAllocationRow> toGenericAllocations(
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        ArrayList<GenericAllocationRow> result = new ArrayList<GenericAllocationRow>();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                result.add(from((GenericResourceAllocation) resourceAllocation));
            }
        }
        return result;
    }

    @Override
    public ResourcesPerDayModification toResourcesPerDayModification(Task task) {
        return ResourcesPerDayModification.create(createGenericAllocation(task),
                getResourcesPerDay(), this.resources);
    }

    private GenericResourceAllocation createGenericAllocation(Task task) {
        return GenericResourceAllocation
                .create(task, criterions);
    }

    @Override
    public HoursModification toHoursModification(Task task) {
        return HoursModification.create(createGenericAllocation(task),
                getHoursFromInput(), resources);
    }

    public boolean hasSameCriterions(Set<Criterion> criterions) {
        return this.criterions.equals(criterions);
    }

    @Override
    public List<Resource> getAssociatedResources() {
        return resources;
    }
}
