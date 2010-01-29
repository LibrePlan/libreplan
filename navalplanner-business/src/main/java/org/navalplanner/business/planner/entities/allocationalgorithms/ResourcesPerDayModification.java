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

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;

public abstract class ResourcesPerDayModification extends
        AllocationModification {

    private static class OnGenericAllocation extends
            ResourcesPerDayModification {
        private final GenericResourceAllocation genericAllocation;

        OnGenericAllocation(
                GenericResourceAllocation resourceAllocation,
                ResourcesPerDay resourcesPerDay,
                Collection<? extends Resource> resources) {
            super(resourceAllocation, resourcesPerDay, resources);
            Validate.isTrue(!resources.isEmpty());
            this.genericAllocation = resourceAllocation;
        }

        @Override
        public void applyAllocationOnAllTaskLength() {
            genericAllocation.forResources(getResources()).allocate(getGoal());
        }

        @Override
        public void applyAllocationUntil(LocalDate endExclusive) {
            genericAllocation.forResources(getResources()).until(endExclusive)
                    .allocate(getGoal());
        }

        @Override
        public List<DayAssignment> createAssignmentsAtDay(LocalDate day,
                int limit) {
            return genericAllocation.createAssignmentsAtDay(getResources(),
                    day, getGoal(), limit);
        }
    }

    private static class OnSpecificAllocation extends
            ResourcesPerDayModification {

        private final SpecificResourceAllocation resourceAllocation;

        OnSpecificAllocation(
                SpecificResourceAllocation resourceAllocation,
                ResourcesPerDay resourcesPerDay,
                Collection<? extends Resource> resources) {
            super(resourceAllocation, resourcesPerDay, resources);
            this.resourceAllocation = resourceAllocation;
        }

        @Override
        public void applyAllocationOnAllTaskLength() {
            resourceAllocation.allocate(getGoal());
        }

        @Override
        public void applyAllocationUntil(LocalDate endExclusive) {
            resourceAllocation.until(endExclusive).allocate(getGoal());
        }

        @Override
        public List<DayAssignment> createAssignmentsAtDay(LocalDate day,
                int limit) {
            return resourceAllocation.createAssignmentsAtDay(day, getGoal(),
                    limit);
        }
    }

    public static ResourcesPerDayModification create(
            GenericResourceAllocation resourceAllocation,
            ResourcesPerDay resourcesPerDay, List<Resource> resources) {
        return new OnGenericAllocation(resourceAllocation,
                resourcesPerDay, resources);
    }

    public static List<ResourcesPerDayModification> withNewResources(
            List<ResourceAllocation<?>> allocations, IResourceDAO resourceDAO) {
        List<ResourcesPerDayModification> result = fromExistent(allocations);
        for (ResourcesPerDayModification each : result) {
            each.withNewResources(resourceDAO);
        }
        return result;
    }

    public static ResourcesPerDayModification create(
            SpecificResourceAllocation resourceAllocation,
            ResourcesPerDay resourcesPerDay) {
        return new OnSpecificAllocation(resourceAllocation,
                resourcesPerDay, Collections.singletonList(resourceAllocation
                        .getResource()));
    }

    public static List<ResourcesPerDayModification> fromExistent(
            Collection<? extends ResourceAllocation<?>> allocations) {
        List<ResourcesPerDayModification> result = new ArrayList<ResourcesPerDayModification>();
        for (ResourceAllocation<?> resourceAllocation : allocations) {
            Validate.isTrue(resourceAllocation.hasAssignments());
            ResourcesPerDay perDay = resourceAllocation
                    .getResourcesPerDay();
            Validate.notNull(perDay);
            result.add(resourceAllocation.asResourcesPerDayModification());
        }
        return result;
    }

    private final ResourcesPerDay goal;

    private ResourcesPerDayModification(
            ResourceAllocation<?> resourceAllocation,
            ResourcesPerDay resourcesPerDay,
            Collection<? extends Resource> resources) {
        super(resourceAllocation, resources);
        this.goal = resourcesPerDay;
    }

    public ResourcesPerDay getGoal() {
        return goal;
    }

    public abstract void applyAllocationOnAllTaskLength();

    public abstract void applyAllocationUntil(LocalDate endExclusive);

    public abstract List<DayAssignment> createAssignmentsAtDay(LocalDate day,
            int limit);



}
