/*
 * This file is part of NavalPlan
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
package org.navalplanner.business.planner.entities;

import static org.navalplanner.business.workingday.EffortDuration.seconds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ICalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.navalplanner.business.workingday.ResourcesPerDay;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class EffortDistributor {

    public interface IResourceSelector {
        boolean isSelectable(Resource resource, LocalDate day);
    }

    private static class CompoundSelector implements IResourceSelector {

        private List<IResourceSelector> selectors;

        public CompoundSelector(IResourceSelector... selectors) {
            Validate.noNullElements(selectors);
            this.selectors = Arrays.asList(selectors);
        }

        @Override
        public boolean isSelectable(Resource resource, LocalDate day) {
            for (IResourceSelector each : selectors) {
                if (!each.isSelectable(resource, day)) {
                    return false;
                }
            }
            return true;
        }

    }

    private static class OnlyCanWork implements IResourceSelector {

        @Override
        public boolean isSelectable(Resource resource, LocalDate day) {
            ResourceCalendar resourceCalendar = resource.getCalendar();
            return resourceCalendar == null || resourceCalendar.canWorkOn(day);
        }
    }

    public static class ResourceWithAssignedDuration {
        public final EffortDuration duration;

        public final Resource resource;

        private ResourceWithAssignedDuration(EffortDuration duration,
                Resource resource) {
            Validate.notNull(duration);
            Validate.notNull(resource);
            this.duration = duration;
            this.resource = resource;
        }
    }

    private static final ICalendar generateCalendarFor(Resource resource) {
        if (resource.getCalendar() != null) {
            return resource.getCalendar();
        } else {
            return SameWorkHoursEveryDay.getDefaultWorkingDay();
        }
    }

    private static int getCapacityFor(Resource resource) {
        if (resource.getCalendar() != null) {
            return resource.getCalendar().getCapacity();
        } else {
            return 1;
        }
    }

    private static class ResourceWithDerivedData {

        public static List<ResourceWithDerivedData> from(
                List<Resource> resources) {
            List<ResourceWithDerivedData> result = new ArrayList<ResourceWithDerivedData>();
            for (Resource each : resources) {
                result.add(new ResourceWithDerivedData(each));
            }
            return result;
        }

        public static List<Resource> resources(
                List<ResourceWithDerivedData> resources) {
            List<Resource> result = new ArrayList<Resource>();
            for (ResourceWithDerivedData each : resources) {
                result.add(each.resource);
            }
            return result;
        }

        public final Resource resource;

        public final int capacityUnits;

        public final ICalendar calendar;

        public ResourceWithDerivedData(Resource resource) {
            this.resource = resource;
            this.capacityUnits = getCapacityFor(resource);
            this.calendar = generateCalendarFor(resource);
        }

    }

    private final List<ResourceWithDerivedData> resources;

    private final IAssignedHoursForResource assignedHoursForResource;

    private final IResourceSelector resourceSelector;

    public EffortDistributor(List<Resource> resources,
            IAssignedHoursForResource assignedHoursForResource) {
        this(resources, assignedHoursForResource, null);
    }

    public EffortDistributor(List<Resource> resources,
            IAssignedHoursForResource assignedHoursForResource,
            IResourceSelector selector) {
        this.resources = ResourceWithDerivedData.from(resources);
        this.assignedHoursForResource = assignedHoursForResource;
        this.resourceSelector = selector != null ? new CompoundSelector(
                new OnlyCanWork(), selector) : new OnlyCanWork();
    }


    public List<ResourceWithAssignedDuration> distributeForDay(LocalDate day,
            EffortDuration totalDuration) {
        List<ResourceWithDerivedData> resourcesAssignable = resourcesAssignableAt(day);
        List<ShareSource> shares = divisionAt(resourcesAssignable, day);
        ShareDivision currentDivision = ShareSource.all(shares);
        ShareDivision newDivison = currentDivision.plus(totalDuration.getSeconds());
        int[] differences = currentDivision.to(newDivison);
        return ShareSource.durationsForEachResource(shares, differences,
                ResourceWithDerivedData.resources(resourcesAssignable));
    }

    private List<ResourceWithDerivedData> resourcesAssignableAt(LocalDate day) {
        List<ResourceWithDerivedData> result = new ArrayList<ResourceWithDerivedData>();
        for (ResourceWithDerivedData each : resources) {
            if (resourceSelector.isSelectable(each.resource, day)) {
                result.add(each);
            }
        }
        return result;
    }

    private static final ResourcesPerDay ONE = ResourcesPerDay.amount(1);

    private static class ShareSource {

        public static ShareDivision all(Collection<ShareSource> sources) {
            List<Share> shares = new ArrayList<Share>();
            for (ShareSource shareSource : sources) {
                shares.addAll(shareSource.shares);
            }
            return ShareDivision.create(shares);
        }

        public static List<ResourceWithAssignedDuration> durationsForEachResource(
                List<ShareSource> sources, int[] differencesInSeconds,
                List<Resource> resources) {
            List<ResourceWithAssignedDuration> result = new ArrayList<ResourceWithAssignedDuration>();
            int differencesIndex = 0;
            for (int i = 0; i < resources.size(); i++) {
                Resource resource = resources.get(i);
                ShareSource shareSource = sources.get(i);
                final int differencesToTake = shareSource.shares.size();
                int sum = sumDifferences(differencesInSeconds, differencesIndex,
                        differencesToTake);
                differencesIndex += differencesToTake;
                result.add(new ResourceWithAssignedDuration(seconds(sum),
                        resource));
            }
            return result;
        }

        private static int sumDifferences(int[] differences, int start,
                final int toTake) {
            int sum = 0;
            for (int i = 0; i < toTake; i++) {
                sum += differences[start + i];
            }
            return sum;
        }

        private final List<Share> shares;

        private ShareSource(List<Share> shares) {
            this.shares = shares;
        }

    }

    public List<ShareSource> divisionAt(
            List<ResourceWithDerivedData> resources, LocalDate day) {
        List<ShareSource> result = new ArrayList<ShareSource>();
        for (int i = 0; i < resources.size(); i++) {
            List<Share> shares = new ArrayList<Share>();
            Resource resource = resources.get(i).resource;
            ICalendar calendarForResource = resources.get(i).calendar;
            EffortDuration alreadyAssigned = assignedHoursForResource
                    .getAssignedDurationAt(resource, day);
            final int alreadyAssignedSeconds = alreadyAssigned.getSeconds();
            Integer capacityEachOneSeconds = calendarForResource.asDurationOn(
                    PartialDay.wholeDay(day), ONE).getSeconds();
            final int capacityUnits = resources.get(i).capacityUnits;
            assert capacityUnits >= 1;
            final int assignedForEach = alreadyAssignedSeconds / capacityUnits;
            final int remainder = alreadyAssignedSeconds % capacityUnits;
            for (int j = 0; j < capacityUnits; j++) {
                int assignedSeconds = assignedForEach + (j < remainder ? 1 : 0);
                shares.add(new Share(assignedSeconds - capacityEachOneSeconds));
            }
            result.add(new ShareSource(shares));
        }
        return result;
    }

}
