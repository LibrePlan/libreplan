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
package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
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

    private final List<Resource> resources;

    private final List<IWorkHours> workHours;

    public HoursDistributor(List<Resource> resources) {
        this.resources = resources;
        this.workHours = new ArrayList<IWorkHours>();
        for (Resource resource : resources) {
            this.workHours.add(generateWorkHoursFor(resource));
        }
    }

    private final IWorkHours generateWorkHoursFor(Resource resource) {
        if (resource.getCalendar() != null) {
            return resource.getCalendar();
        } else {
            return SameWorkHoursEveryDay.getDefaultWorkingDay();
        }
    }

    public List<ResourceWithAssignedHours> distributeForDay(LocalDate day,
            int totalHours) {
        List<ResourceWithAssignedHours> result = new ArrayList<ResourceWithAssignedHours>();
        List<Share> shares = currentSharesFor(day);
        ShareDivision currentDivision = ShareDivision.create(shares);
        ShareDivision newDivison = currentDivision.plus(totalHours);
        int[] differences = currentDivision.to(newDivison);
        for (int i = 0; i < differences.length; i++) {
            assert differences[i] >= 0;
            result.add(new ResourceWithAssignedHours(differences[i], resources
                    .get(i)));
        }
        return result;
    }

    public List<Share> currentSharesFor(LocalDate day) {
        List<Share> shares = new ArrayList<Share>();
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            IWorkHours workHoursForResource = workHours.get(i);
            int alreadyAssignedHours = resource.getAssignedHours(day);
            Integer workableHours = workHoursForResource.getWorkableHours(day);
            // a resource would have a zero share if all it's hours for a
            // given day are filled
            shares.add(new Share(alreadyAssignedHours - workableHours));
        }
        return shares;
    }

}
