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
package org.libreplan.business.planner.entities.allocationalgorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.planner.entities.GenericResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class EffortModification extends AllocationModification {

    private static class OnGenericAllocation extends EffortModification {

        private final GenericResourceAllocation genericAllocation;

        private OnGenericAllocation(GenericResourceAllocation beingModified,
                Collection<? extends Resource> resources, EffortDuration effort) {
            super(beingModified, resources, effort);
            genericAllocation = beingModified;
        }

        @Override
        public void allocateUntil(IntraDayDate end) {
            genericAllocation.forResources(getResources())
                             .fromStartUntil(end)
                             .allocate(getEffort());
        }

        @Override
        public void allocateFromEndUntil(IntraDayDate start) {
            genericAllocation.forResources(getResources())
                             .fromEndUntil(start)
                             .allocate(getEffort());
        }

        @Override
        public boolean isSpecific() {
            return false;
        }
    }

    private static class OnSpecificAllocation extends EffortModification {

        private final SpecificResourceAllocation specific;

        private OnSpecificAllocation(SpecificResourceAllocation beingModified,
                Collection<? extends Resource> resources, EffortDuration effort) {
            super(beingModified, resources, effort);
            specific = beingModified;
        }

        @Override
        public void allocateUntil(IntraDayDate end) {
            specific.fromStartUntil(end).allocate(getEffort());
        }

        @Override
        public void allocateFromEndUntil(IntraDayDate start) {
            specific.fromEndUntil(start).allocate(getEffort());
        }

        @Override
        public boolean isSpecific() {
            return true;
        }
    }

    public static EffortModification create(
            GenericResourceAllocation resourceAllocation,
            EffortDuration effort,
            List<Resource> resources) {
        return new OnGenericAllocation(resourceAllocation, resources, effort);
    }

    public static EffortModification create(
            SpecificResourceAllocation resourceAllocation, EffortDuration effort) {
        return new OnSpecificAllocation(resourceAllocation,
                Collections.singletonList(resourceAllocation.getResource()),
                effort);
    }

    public static List<EffortModification> fromExistent(
            Collection<? extends ResourceAllocation<?>> allocations,
            IResourcesSearcher searcher) {
        List<EffortModification> result = new ArrayList<EffortModification>();
        for (ResourceAllocation<?> resourceAllocation : allocations) {
            result.add(resourceAllocation.asHoursModification());
        }
        return ensureNoOneWithoutAssociatedResources(result, searcher);
    }

    public static List<EffortModification> withNewResources(
            List<ResourceAllocation<?>> allocations, IResourcesSearcher searcher) {
        List<EffortModification> result = fromExistent(allocations, searcher);
        for (EffortModification each : result) {
            each.withNewResources(searcher);
        }
        return ensureNoOneWithoutAssociatedResources(result, searcher);
    }

    private final EffortDuration effort;

    private EffortModification(ResourceAllocation<?> beingModified,
            Collection<? extends Resource> resources, EffortDuration effort) {
        super(beingModified, resources);
        Validate.notNull(effort);
        this.effort = effort;
    }

    protected LocalDate getTaskStart() {
        return new LocalDate(getBeingModified().getTask().getStartDate());
    }

    public abstract void allocateUntil(IntraDayDate end);

    public abstract void allocateFromEndUntil(IntraDayDate start);

    @Override
    public boolean satisfiesModificationRequested() {
        return effort.compareTo(getBeingModified().getNonConsolidatedEffort()) == 0;
    }

    public EffortDuration getEffort() {
        return effort;
    }

}
