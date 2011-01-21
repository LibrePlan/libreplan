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
package org.navalplanner.business.planner.entities.allocationalgorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;

/**
 * @author  Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class AllocationModification {

    public static List<ResourceAllocation<?>> getBeingModified(
            Collection<? extends AllocationModification> allocationModifications) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        for (AllocationModification each : allocationModifications) {
            result.add(each.getBeingModified());
        }
        return result;
    }

    /**
     * It ensures that the provided allocations have at least one associated
     * resource. A {@link AllocationModification} doesn't have associated
     * resources when creating an {@link AllocationModification} from a
     * unsatisfied generic allocation.
     */
    protected static <T extends AllocationModification> List<T> ensureNoOneWithoutAssociatedResources(
            Collection<? extends T> modifications, IResourceDAO resourceDAO) {
        List<T> result = new ArrayList<T>();
        for (T each : modifications) {
            if (each.hasNoResources()) {
                each.withNewResources(resourceDAO);
            }
            assert !each.hasNoResources();
            result.add(each);
        }
        return result;
    }

    private final ResourceAllocation<?> beingModified;

    private List<Resource> resourcesOnWhichApplyAllocation;

    protected AllocationModification(ResourceAllocation<?> beingModified, Collection<? extends Resource> resources) {
        this.beingModified = beingModified;
        this.resourcesOnWhichApplyAllocation = Collections
                .unmodifiableList(new ArrayList<Resource>(resources));
    }

    private boolean hasNoResources() {
        return resourcesOnWhichApplyAllocation.isEmpty();
    }

    protected void withNewResources(IResourceDAO resourceDAO) {
        resourcesOnWhichApplyAllocation = beingModified
                .querySuitableResources(resourceDAO);
    }

    public ResourceAllocation<?> getBeingModified() {
        return beingModified;
    }

    public List<Resource> getResources() {
        return resourcesOnWhichApplyAllocation;
    }

}