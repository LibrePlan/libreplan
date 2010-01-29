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

    private final ResourceAllocation<?> beingModified;

    private List<Resource> resourcesOnWhichApplyAllocation;

    protected AllocationModification(ResourceAllocation<?> beingModified, Collection<? extends Resource> resources) {
        this.beingModified = beingModified;
        this.resourcesOnWhichApplyAllocation = Collections
                .unmodifiableList(new ArrayList<Resource>(resources));
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