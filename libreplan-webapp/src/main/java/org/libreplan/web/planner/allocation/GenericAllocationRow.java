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

package org.libreplan.web.planner.allocation;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.libreplan.business.planner.entities.CalculatedValue;
import org.libreplan.business.planner.entities.GenericResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.allocationalgorithms.EffortModification;
import org.libreplan.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.resources.entities.ResourceType;

/**
 * The information required for creating a {@link GenericResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GenericAllocationRow extends AllocationRow {

    private static GenericAllocationRow initializeDefault(
            GenericAllocationRow result, ResourceEnum resourceType) {
        Validate.notNull(resourceType);
        result.setName(_("Generic"));
        result.resourceType = resourceType;
        return result;
    }

    public static GenericAllocationRow create(CalculatedValue calculatedValue,
            ResourceEnum resourceType,
            Collection<? extends Criterion> criterions,
            Collection<? extends Resource> resources) {

        GenericAllocationRow result = new GenericAllocationRow(calculatedValue);
        Validate.isTrue(!resources.isEmpty());
        Validate.notNull(criterions);
        initializeDefault(result, resourceType);
        result.criterions = new HashSet<Criterion>(criterions);
        result.resources = new ArrayList<Resource>(resources);
        result.setName(Criterion.getCaptionFor(resourceType, criterions));
        return result;
    }

    public static GenericAllocationRow from(
            GenericResourceAllocation resourceAllocation,
            IResourcesSearcher searchModel) {
        GenericAllocationRow result = initializeDefault(
                new GenericAllocationRow(resourceAllocation),
                resourceAllocation.getResourceType());

        ResourceType type = resourceAllocation.isLimiting() ?
                ResourceType.LIMITING_RESOURCE :
                ResourceType.NON_LIMITING_RESOURCE;

        result.criterions = resourceAllocation.getCriterions();
        result.resources = new ArrayList<Resource>(searchModel
                .searchBy(resourceAllocation.getResourceType())
                .byCriteria(resourceAllocation.getCriterions())
                .byResourceType(type).execute());
        result.setName(Criterion
                .getCaptionFor(resourceAllocation));
        return result;
    }

    private ResourceEnum resourceType;
    private Set<Criterion> criterions;
    private List<Resource> resources;

    private GenericAllocationRow(CalculatedValue calculatedValue) {
        super(calculatedValue);
    }

    private GenericAllocationRow(GenericResourceAllocation origin) {
        super(origin);
    }

    @Override
    public boolean isGeneric() {
        return true;
    }

    public static Collection<GenericAllocationRow> toGenericAllocations(
            Collection<? extends ResourceAllocation<?>> resourceAllocations,
            IResourcesSearcher searchModel) {
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
                .create(newGeneric, getResourcesPerDayEditedValue(), this.resources);
    }

    private GenericResourceAllocation createGenericAllocation(Task task) {
        GenericResourceAllocation result = GenericResourceAllocation.create(
                task, resourceType, criterions);
        GenericResourceAllocation origin = (GenericResourceAllocation) getOrigin();
        if (origin != null) {
            result.overrideConsolidatedDayAssignments(origin);
            result.setAssignmentFunctionWithoutApply(origin.getAssignmentFunction());
        }
        return result;
    }

    @Override
    public EffortModification toHoursModification(Task task) {
        return EffortModification.create(createGenericAllocation(task),
                getEffortFromInput(), resources);
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
