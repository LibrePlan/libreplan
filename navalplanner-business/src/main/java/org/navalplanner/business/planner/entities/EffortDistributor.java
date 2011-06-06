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

import static org.navalplanner.business.workingday.EffortDuration.min;
import static org.navalplanner.business.workingday.EffortDuration.seconds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.Capacity;
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

        public static EffortDuration sumDurations(
                List<ResourceWithAssignedDuration> withoutOvertime) {
            EffortDuration result = EffortDuration.zero();
            for (ResourceWithAssignedDuration each : withoutOvertime) {
                result = result.plus(each.duration);
            }
            return result;
        }

        static List<Resource> resources(
                Collection<? extends ResourceWithAssignedDuration> collection) {
            List<Resource> result = new ArrayList<Resource>();
            for (ResourceWithAssignedDuration each : collection) {
                result.add(each.resource);
            }
            return result;
        }

        static Map<Resource, ResourceWithAssignedDuration> byResource(
                Collection<? extends ResourceWithAssignedDuration> durations) {
            Map<Resource, ResourceWithAssignedDuration> result = new HashMap<Resource, ResourceWithAssignedDuration>();
            for (ResourceWithAssignedDuration each : durations) {
                result.put(each.resource, each);
            }
            return result;
        }

        public static IAssignedEffortForResource sumAssignedEffort(
                List<ResourceWithAssignedDuration> durations,
                final IAssignedEffortForResource assignedEffortForResource) {
            final Map<Resource, ResourceWithAssignedDuration> byResource = byResource(durations);
            return new IAssignedEffortForResource() {

                @Override
                public EffortDuration getAssignedDurationAt(Resource resource,
                        LocalDate day) {
                    EffortDuration previouslyAssigned = assignedEffortForResource
                            .getAssignedDurationAt(resource, day);
                    ResourceWithAssignedDuration withDuration = byResource
                            .get(resource);
                    if (withDuration != null) {
                        return previouslyAssigned.plus(withDuration.duration);
                    }
                    return previouslyAssigned;
                }
            };
        }

        public static List<ResourceWithAssignedDuration> join(
                Collection<? extends ResourceWithAssignedDuration> a,
                Collection<ResourceWithAssignedDuration> b) {
            Map<Resource, ResourceWithAssignedDuration> result = byResource(a);
            Map<Resource, ResourceWithAssignedDuration> byResource = byResource(b);
            for (Entry<Resource, ResourceWithAssignedDuration> each : byResource
                    .entrySet()) {
                Resource key = each.getKey();
                ResourceWithAssignedDuration value = each.getValue();
                if (result.containsKey(key)) {
                    result.put(key, result.get(key).plus(value));
                } else {
                    result.put(key, value);
                }
            }
            return new ArrayList<ResourceWithAssignedDuration>(result.values());
        }

        ResourceWithAssignedDuration plus(ResourceWithAssignedDuration value) {
            return new ResourceWithAssignedDuration(
                    this.duration.plus(value.duration), resource);
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

        ResourceWithAvailableCapacity withAvailableCapacityOn(PartialDay day,
                IAssignedEffortForResource assignedEffort) {
            EffortDuration allCapacityForDay = calendar
                    .getCapacityOn(PartialDay.wholeDay(day.getDate()));
            EffortDuration capacity = calendar.getCapacityOn(day);

            EffortDuration capacityForAlreadyAssigned = allCapacityForDay
                    .minus(capacity);

            EffortDuration assigned = assignedEffort.getAssignedDurationAt(
                    resource, day.getDate());
            EffortDuration assignedInterfering = assigned.minus(min(assigned,
                    capacityForAlreadyAssigned));

            EffortDuration available = capacity.minus(min(assignedInterfering,
                    capacity));
            return new ResourceWithAvailableCapacity(resource, available);
        }

    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    private static class ResourceWithAvailableCapacity implements
            Comparable<ResourceWithAvailableCapacity> {

        private final Resource resource;

        private final EffortDuration available;

        public ResourceWithAvailableCapacity(Resource resource,
                EffortDuration available) {
            Validate.notNull(resource);
            Validate.notNull(available);
            this.resource = resource;
            this.available = available;
        }

        public ResourceWithAssignedDuration doBiggestAssignationPossible(
                EffortDuration remaining) {
            return new ResourceWithAssignedDuration(EffortDuration.min(
                    remaining, available), resource);
        }

        @Override
        public int compareTo(ResourceWithAvailableCapacity o) {
            return available.compareTo(o.available);
        }

        @SuppressWarnings("unchecked")
        static Comparator<ResourceWithAvailableCapacity> getComparatorConsidering(
                final Set<Resource> lastResourcesUsed) {
            return ComparatorUtils.chainedComparator(
                    new Comparator<ResourceWithAvailableCapacity>() {

                        @Override
                        public int compare(ResourceWithAvailableCapacity o1,
                                ResourceWithAvailableCapacity o2) {
                            boolean resource1Used = lastResourcesUsed
                                    .contains(o1.resource);
                            boolean resource2Used = lastResourcesUsed
                                    .contains(o2.resource);
                            return asInt(resource1Used) - asInt(resource2Used);
                        }

                        int asInt(boolean b) {
                            return b ? 1 : 0;
                        }

                    }, ComparatorUtils.naturalComparator());
        }

    }

    private final List<ResourceWithDerivedData> resources;

    private final IAssignedEffortForResource assignedEffortForResource;

    private final IResourceSelector resourceSelector;

    private Set<Resource> resourcesAlreadyPicked = new HashSet<Resource>();

    public EffortDistributor(List<Resource> resources,
            IAssignedEffortForResource assignedHoursForResource) {
        this(resources, assignedHoursForResource, null);
    }

    public EffortDistributor(List<Resource> resources,
            IAssignedEffortForResource assignedEffortForResource,
            IResourceSelector selector) {
        this.resources = ResourceWithDerivedData.from(resources);
        this.assignedEffortForResource = assignedEffortForResource;
        this.resourceSelector = selector != null ? new CompoundSelector(
                new OnlyCanWork(), selector) : new OnlyCanWork();
    }


    public List<ResourceWithAssignedDuration> distributeForDay(PartialDay day,
            EffortDuration totalDuration) {
        return withCaptureOfResourcesPicked(distributeForDay_(day,
                totalDuration));
    }

    private List<ResourceWithAssignedDuration> withCaptureOfResourcesPicked(
            List<ResourceWithAssignedDuration> result) {
        resourcesAlreadyPicked.addAll(ResourceWithAssignedDuration
                .resources(result));
        return result;
    }

    private List<ResourceWithAssignedDuration> distributeForDay_(
            PartialDay day, EffortDuration totalDuration) {
        List<ResourceWithDerivedData> resourcesAssignable = resourcesAssignableAt(day
                .getDate());
        List<ResourceWithAssignedDuration> withoutOvertime = assignAllPossibleWithoutOvertime(
                day, totalDuration, resourcesAssignable);
        EffortDuration remaining = totalDuration
                .minus(ResourceWithAssignedDuration
                        .sumDurations(withoutOvertime));
        if (remaining.isZero()) {
            return withoutOvertime;
        }
        List<ResourceWithAssignedDuration> withOvertime = distributeInOvertimeForDayRemainingEffort(
                day.getDate(), remaining,
                ResourceWithAssignedDuration.sumAssignedEffort(withoutOvertime,
                        assignedEffortForResource),
                resourcesAssignable);
        return ResourceWithAssignedDuration
                .join(withoutOvertime, withOvertime);
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

    private List<ResourceWithAssignedDuration> assignAllPossibleWithoutOvertime(
            PartialDay day, EffortDuration totalDuration,
            List<ResourceWithDerivedData> resourcesAssignable) {

        List<ResourceWithAvailableCapacity> fromMoreToLessCapacity = resourcesFromMoreDesirableToLess(
                resourcesAssignable, day);
        EffortDuration remaining = totalDuration;
        List<ResourceWithAssignedDuration> result = new ArrayList<ResourceWithAssignedDuration>();
        for (ResourceWithAvailableCapacity each : fromMoreToLessCapacity) {
            if (!each.available.isZero()) {
                ResourceWithAssignedDuration r = each
                        .doBiggestAssignationPossible(remaining);
                remaining = remaining.minus(r.duration);
                if (!r.duration.isZero()) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    private List<ResourceWithAvailableCapacity> resourcesFromMoreDesirableToLess(
            List<ResourceWithDerivedData> resourcesAssignable, PartialDay day) {
        List<ResourceWithAvailableCapacity> result = new ArrayList<ResourceWithAvailableCapacity>();
        for (ResourceWithDerivedData each : resourcesAssignable) {
            result.add(each.withAvailableCapacityOn(day,
                    assignedEffortForResource));
        }
        Collections.sort(result, Collections
                .reverseOrder(ResourceWithAvailableCapacity
                        .getComparatorConsidering(resourcesAlreadyPicked)));
        return result;
    }

    private List<ResourceWithAssignedDuration> distributeInOvertimeForDayRemainingEffort(
            LocalDate day, EffortDuration remainingDuration,
            IAssignedEffortForResource assignedEffortForEachResource,
            List<ResourceWithDerivedData> assignableResources) {
        List<ResourceWithAssignedDuration> remainingDistribution = suppressOverAssignedBeyondAvailableCapacity(
                day,
                assignedEffortForEachResource,
                distributeRemaining(day, remainingDuration,
                        assignedEffortForEachResource, assignableResources));

        EffortDuration durationDistributed = ResourceWithAssignedDuration
                .sumDurations(remainingDistribution);
        EffortDuration newRemaining = remainingDuration
                .minus(durationDistributed);
        assert newRemaining.compareTo(EffortDuration.zero()) >= 0;
        if (newRemaining.isZero()) {
            return remainingDistribution;
        }
        IAssignedEffortForResource newEffortForEachResource = ResourceWithAssignedDuration.sumAssignedEffort(
                remainingDistribution, assignedEffortForEachResource);

        List<ResourceWithDerivedData> resourcesWithAvailableOvertime = withAvailableCapacity(day, newEffortForEachResource, assignableResources);
        if (resourcesWithAvailableOvertime.isEmpty()) {
            return remainingDistribution;
        }
        return ResourceWithAssignedDuration.join(
                remainingDistribution,
                distributeInOvertimeForDayRemainingEffort(day, newRemaining,
                        newEffortForEachResource,
                        resourcesWithAvailableOvertime));
    }

    private List<ResourceWithAssignedDuration> suppressOverAssignedBeyondAvailableCapacity(
            LocalDate date,
            IAssignedEffortForResource assignedEffortForEachResource,
            List<ResourceWithAssignedDuration> resources) {
        List<ResourceWithAssignedDuration> result = new ArrayList<ResourceWithAssignedDuration>();
        for (ResourceWithAssignedDuration each : resources) {
            Resource resource = each.resource;
            ICalendar calendar = generateCalendarFor(resource);
            Capacity capacityWithOvertime = calendar
                    .getCapacityWithOvertime(date);
            if (capacityWithOvertime.isOverAssignableWithoutLimit()) {
                result.add(each);
            } else {
                EffortDuration durationCanBeAdded = calculateDurationCanBeAdded(
                        assignedEffortForEachResource.getAssignedDurationAt(
                                resource, date), capacityWithOvertime,
                        each.duration);
                if (!durationCanBeAdded.isZero()) {
                    result.add(new ResourceWithAssignedDuration(
                            durationCanBeAdded, resource));
                }
            }
        }
        return result;
    }

    private EffortDuration calculateDurationCanBeAdded(
            EffortDuration alreadyAssigned, Capacity capacityWithOvertime,
            EffortDuration newAddition) {
        EffortDuration maximum = capacityWithOvertime.getStandardEffort().plus(
                capacityWithOvertime.getAllowedExtraEffort());
        if (alreadyAssigned.compareTo(maximum) >= 0) {
            return EffortDuration.zero();
        } else {
            return EffortDuration.min(newAddition,
                    maximum.minus(alreadyAssigned));
        }
    }

    private List<ResourceWithDerivedData> withAvailableCapacity(LocalDate date,
            IAssignedEffortForResource assignedEffortForEachResource,
            List<ResourceWithDerivedData> assignableResources) {
        List<ResourceWithDerivedData> result = new ArrayList<ResourceWithDerivedData>();
        for (ResourceWithDerivedData each : assignableResources) {
            Capacity capacity = each.calendar.getCapacityWithOvertime(date);
            EffortDuration assignedEffort = assignedEffortForEachResource
                    .getAssignedDurationAt(each.resource, date);
            if (capacity.hasSpareSpaceForMoreAllocations(assignedEffort)) {
                result.add(each);
            }
        }
        return result;
    }

    private List<ResourceWithAssignedDuration> distributeRemaining(
            LocalDate date, EffortDuration remainingDuration,
            IAssignedEffortForResource assignedEffortForEachResource,
            List<ResourceWithDerivedData> resourcesWithAvailableOvertime) {
        List<ShareSource> shares = divisionAt(resourcesWithAvailableOvertime,
                assignedEffortForEachResource, date);
        ShareDivision currentDivision = ShareSource.all(shares);
        ShareDivision newDivison = currentDivision.plus(remainingDuration
                .getSeconds());
        int[] differences = currentDivision.to(newDivison);
        return ShareSource.durationsForEachResource(shares, differences,
                ResourceWithDerivedData
                        .resources(resourcesWithAvailableOvertime));
    }

    private List<ShareSource> divisionAt(
            List<ResourceWithDerivedData> resources,
            IAssignedEffortForResource assignedEffortForEachResource,
            LocalDate date) {
        List<ShareSource> result = new ArrayList<ShareSource>();
        for (int i = 0; i < resources.size(); i++) {
            List<Share> shares = new ArrayList<Share>();
            Resource resource = resources.get(i).resource;
            ICalendar calendarForResource = resources.get(i).calendar;
            EffortDuration alreadyAssigned = assignedEffortForEachResource
                    .getAssignedDurationAt(resource, date);
            final int alreadyAssignedSeconds = alreadyAssigned.getSeconds();
            Integer capacityEachOneSeconds = calendarForResource.asDurationOn(
                    PartialDay.wholeDay(date), ONE).getSeconds();
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
                ResourceWithAssignedDuration withAssignedDuration = new ResourceWithAssignedDuration(
                        seconds(sum), resource);
                if (!withAssignedDuration.duration.isZero()) {
                    result.add(withAssignedDuration);
                }
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

}
