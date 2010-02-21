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
package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.IWorkHours;
import org.navalplanner.business.calendars.entities.SameWorkHoursEveryDay;
import org.navalplanner.business.resources.entities.Resource;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class HoursDistributor {

    public static class ResourceWithAssignedHours {
        public final Integer hours;

        public final Resource resource;

        private ResourceWithAssignedHours(Integer hours, Resource resource) {
            this.hours = hours;
            this.resource = resource;
        }
    }

    private static final IWorkHours generateWorkHoursFor(Resource resource) {
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

        public final IWorkHours workHours;

        public ResourceWithDerivedData(Resource resource) {
            this.resource = resource;
            this.capacityUnits = getCapacityFor(resource);
            this.workHours = generateWorkHoursFor(resource);
        }

    }

    private final List<ResourceWithDerivedData> resources;

    private final IAssignedHoursForResource assignedHoursForResource;

    public HoursDistributor(List<Resource> resources,
            IAssignedHoursForResource assignedHoursForResource) {
        this.resources = ResourceWithDerivedData.from(resources);
        this.assignedHoursForResource = assignedHoursForResource;
    }


    public List<ResourceWithAssignedHours> distributeForDay(LocalDate day,
            int totalHours) {
        List<ShareSource> shares = divisionAt(resources, day);
        ShareDivision currentDivision = ShareSource.all(shares);
        ShareDivision newDivison = currentDivision.plus(totalHours);
        int[] differences = currentDivision.to(newDivison);
        return ShareSource.hoursForEachResource(shares, differences,
                ResourceWithDerivedData.resources(resources));
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

        public static List<ResourceWithAssignedHours> hoursForEachResource(
                List<ShareSource> sources, int[] differences,
                List<Resource> resources) {
            List<ResourceWithAssignedHours> result = new ArrayList<ResourceWithAssignedHours>();
            int differencesIndex = 0;
            for (int i = 0; i < resources.size(); i++) {
                Resource resource = resources.get(i);
                ShareSource shareSource = sources.get(i);
                final int differencesToTake = shareSource.shares.size();
                int sum = sumDifferences(differences, differencesIndex,
                        differencesToTake);
                differencesIndex += differencesToTake;
                result.add(new ResourceWithAssignedHours(sum, resource));
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
            IWorkHours workHoursForResource = resources.get(i).workHours;
            int alreadyAssignedHours = assignedHoursForResource
                    .getAssignedHoursAt(resource, day);
            Integer capacityEachOne = workHoursForResource.toHours(day, ONE);
            final int capacityUnits = resources.get(i).capacityUnits;
            assert capacityUnits >= 1;
            final int assignedForEach = alreadyAssignedHours / capacityUnits;
            final int remainder = alreadyAssignedHours % capacityUnits;
            for (int j = 0; j < capacityUnits; j++) {
                int assigned = assignedForEach + (j < remainder ? 1 : 0);
                shares.add(new Share(assigned - capacityEachOne));
            }
            result.add(new ShareSource(shares));
        }
        return result;
    }

}
