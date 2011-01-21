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

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class HoursModification extends AllocationModification {

    private static class OnGenericAllocation extends HoursModification {

        private final GenericResourceAllocation genericAllocation;

        private OnGenericAllocation(GenericResourceAllocation beingModified,
                Collection<? extends Resource> resources, int hours) {
            super(beingModified, resources, hours);
            genericAllocation = beingModified;
        }

        @Override
        public void allocateUntil(LocalDate end) {
            genericAllocation.forResources(getResources())
                             .fromStartUntil(end)
                             .allocateHours(getHours());
        }

        @Override
        public void allocateFromEndUntil(LocalDate start) {
            genericAllocation.forResources(getResources())
                             .fromEndUntil(start)
                             .allocateHours(getHours());
        }
    }

    private static class OnSpecificAllocation extends HoursModification {

        private final SpecificResourceAllocation specific;

        private OnSpecificAllocation(SpecificResourceAllocation beingModified,
                Collection<? extends Resource> resources, int hours) {
            super(beingModified, resources, hours);
            specific = beingModified;
        }

        @Override
        public void allocateUntil(LocalDate end) {
            specific.fromStartUntil(end)
                    .allocateHours(getHours());
        }

        @Override
        public void allocateFromEndUntil(LocalDate start) {
            specific.fromEndUntil(start)
                    .allocateHours(getHours());
        }
    }

    public static HoursModification create(
            GenericResourceAllocation resourceAllocation, int hours,
            List<Resource> resources) {
        return new OnGenericAllocation(resourceAllocation, resources, hours);
    }

    public static HoursModification create(
            SpecificResourceAllocation resourceAllocation, int hours) {
        return new OnSpecificAllocation(resourceAllocation, Collections
                .singletonList(resourceAllocation.getResource()), hours);
    }

    public static List<HoursModification> fromExistent(
            Collection<? extends ResourceAllocation<?>> allocations,
            IResourceDAO resourceDAO) {
        List<HoursModification> result = new ArrayList<HoursModification>();
        for (ResourceAllocation<?> resourceAllocation : allocations) {
            result.add(resourceAllocation.asHoursModification());
        }
        return ensureNoOneWithoutAssociatedResources(result, resourceDAO);
    }

    public static List<HoursModification> withNewResources(
            List<ResourceAllocation<?>> allocations, IResourceDAO resourceDAO) {
        List<HoursModification> result = fromExistent(allocations, resourceDAO);
        for (HoursModification each : result) {
            each.withNewResources(resourceDAO);
        }
        return result;
    }

    private final int hours;

    private HoursModification(ResourceAllocation<?> beingModified,
            Collection<? extends Resource> resources, int hours) {
        super(beingModified, resources);
        Validate.isTrue(hours >= 0);
        this.hours = hours;
    }

    protected LocalDate getTaskStart() {
        return new LocalDate(getBeingModified().getTask().getStartDate());
    }

    public abstract void allocateUntil(LocalDate end);

    public abstract void allocateFromEndUntil(LocalDate start);

    public int getHours() {
        return hours;
    }

}
