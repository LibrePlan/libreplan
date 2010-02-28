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

package org.navalplanner.business.planner.entities.allocationalgorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
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

        @Override
        public AvailabilityTimeLine getAvailability() {
            List<Resource> resources = getResources();
            AvailabilityTimeLine result = AvailabilityTimeLine
                    .createAllInvalid();
            for (Resource each : resources) {
                result = result.or(buildAvailabilityFor(each));
            }
            return result;
        }

        private AvailabilityTimeLine buildAvailabilityFor(Resource each) {
            AvailabilityTimeLine result = AvailabilityTimeLine.allValid();
            result = result.and(getCalendarAvailabilityFor(each));
            return result.and(getCriterionsAvailabilityFor(each));
        }

        private AvailabilityTimeLine getCriterionsAvailabilityFor(
                Resource resource) {
            Set<Criterion> criterions = genericAllocation.getCriterions();
            AvailabilityTimeLine result = AvailabilityTimeLine.allValid();
            for (Criterion each : criterions) {
                result = result.and(buildTimeline(resource.query().from(each)
                        .result()));
            }
            return result;
        }

        private static AvailabilityTimeLine buildTimeline(
                List<CriterionSatisfaction> satisfactions) {
            AvailabilityTimeLine result = AvailabilityTimeLine.allValid();
            LocalDate previousEnd = null;
            for (CriterionSatisfaction each : satisfactions) {
                LocalDate startDate = asLocal(each.getStartDate());
                assert startDate != null : "satisfactions start date is not null";
                if (previousEnd == null) {
                    result.invalidUntil(startDate);
                } else {
                    result.invalidAt(previousEnd, startDate);
                }
                previousEnd = asLocal(each.getEndDate());
                if (previousEnd == null) {
                    break;
                }
            }
            if (previousEnd != null) {
                result.invalidFrom(previousEnd);
            }
            return result;
        }

        private static LocalDate asLocal(Date date) {
            return date != null ? LocalDate.fromDateFields(date) : null;
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

        @Override
        public AvailabilityTimeLine getAvailability() {
            Resource resource = getAssociatedResource();
            return getCalendarAvailabilityFor(resource);
        }

        private Resource getAssociatedResource() {
            return getResources().get(0);
        }
    }

    protected static AvailabilityTimeLine getCalendarAvailabilityFor(
            Resource resource) {
        ResourceCalendar resourceCalendar = resource.getCalendar();
        return resourceCalendar != null ? resourceCalendar.getAvailability()
                : AvailabilityTimeLine.allValid();
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

    public abstract AvailabilityTimeLine getAvailability();

}
