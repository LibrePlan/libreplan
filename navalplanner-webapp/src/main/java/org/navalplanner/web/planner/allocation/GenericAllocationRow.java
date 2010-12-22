/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.allocationalgorithms.HoursModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.resources.entities.ResourceType;
import org.navalplanner.business.workingday.ResourcesPerDay;
import org.navalplanner.web.resources.search.IResourceSearchModel;

/**
 * The information required for creating a {@link GenericResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GenericAllocationRow extends AllocationRow {

    private static GenericAllocationRow createDefault(ResourceEnum resourceType) {
        Validate.notNull(resourceType);
        GenericAllocationRow result = new GenericAllocationRow();
        result.setName(_("Generic"));
        result.setNonConsolidatedResourcesPerDay(ResourcesPerDay.amount(0));
        result.resourceType = resourceType;
        return result;
    }

    public static GenericAllocationRow create(ResourceEnum resourceType,
            Collection<? extends Criterion> criterions,
            Collection<? extends Resource> resources) {
        Validate.isTrue(!resources.isEmpty());
        Validate.notNull(criterions);
        GenericAllocationRow result = createDefault(resourceType);
        result.criterions = new HashSet<Criterion>(criterions);
        result.resources = new ArrayList<Resource>(resources);
        result.setName(Criterion.getCaptionFor(resourceType, criterions));
        return result;
    }

    public static GenericAllocationRow from(
            GenericResourceAllocation resourceAllocation,
            IResourceSearchModel searchModel) {
        GenericAllocationRow result = createDefault(resourceAllocation
                .getResourceType());
        result.setOrigin(resourceAllocation);

        result.setNonConsolidatedResourcesPerDay(resourceAllocation
                .getNonConsolidatedResourcePerDay());

        ResourceType type = resourceAllocation.isLimiting() ?
                ResourceType.LIMITING_RESOURCE :
                ResourceType.NON_LIMITING_RESOURCE;

        result.criterions = resourceAllocation.getCriterions();
        result.resources = new ArrayList<Resource>(searchModel
                .searchBy(resourceAllocation.getResourceType())
                .byCriteria(resourceAllocation.getCriterions())
                .byResourceType(type).execute());
        result.setName(Criterion
                .getCaptionForCriterionsFrom(resourceAllocation));
        return result;
    }

    private ResourceEnum resourceType;
    private Set<Criterion> criterions;
    private List<Resource> resources;

    @Override
    public boolean isGeneric() {
        return true;
    }

    public static Collection<GenericAllocationRow> toGenericAllocations(
            Collection<? extends ResourceAllocation<?>> resourceAllocations,
            IResourceSearchModel searchModel) {
        ArrayList<GenericAllocationRow> result = new ArrayList<GenericAllocationRow>();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                result.add(from((GenericResourceAllocation) resourceAllocation,
                        searchModel));
            }
        }
        return result;
    }

    @Override
    public ResourcesPerDayModification toResourcesPerDayModification(Task task) {
        GenericResourceAllocation newGeneric = createGenericAllocation(task);
        return ResourcesPerDayModification
                .create(newGeneric, getNonConsolidatedResourcesPerDay(), this.resources);
    }

    private GenericResourceAllocation createGenericAllocation(Task task) {
        GenericResourceAllocation result = GenericResourceAllocation.create(
                task, resourceType, criterions);
        GenericResourceAllocation origin = (GenericResourceAllocation) getOrigin();
        if (origin != null) {
            result.overrideAssignedHoursForResource(origin);
            result.overrideConsolidatedDayAssignments(origin);
        }
        return result;
    }

    @Override
    public HoursModification toHoursModification(Task task) {
        return HoursModification.create(createGenericAllocation(task),
                getHoursFromInput(), resources);
    }

    public boolean hasSameCriterionsAndType(Set<Criterion> criterions,
            ResourceEnum resourceType) {
        return this.resourceType == resourceType
                && this.criterions.equals(criterions);
    }

    @Override
    public List<Resource> getAssociatedResources() {
        return resources;
    }

    public Set<Criterion> getCriterions() {
        return Collections.unmodifiableSet(criterions);
    }

    @Override
    public ResourceEnum getType() {
        return resourceType;
    }
}
