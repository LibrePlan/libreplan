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

package org.navalplanner.business.calendars.entities;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.ResourcesPerDay;

public abstract class CombinedWorkHours implements IWorkHours {

    private final List<IWorkHours> workHours;

    public CombinedWorkHours(List<IWorkHours> workHours) {
        Validate.notNull(workHours);
        Validate.noNullElements(workHours);
        Validate.isTrue(!workHours.isEmpty());
        this.workHours = workHours;
    }

    public static CombinedWorkHours minOf(IWorkHours... workHours) {
        Validate.notNull(workHours);
        return new Min(Arrays.asList(workHours));
    }

    @Override
    public Integer getCapacityAt(LocalDate date) {
        Integer current = null;
        for (IWorkHours workHour : workHours) {
            current = current == null ? capacity(workHour, date)
                    : updateCapacity(current, workHour, date);
        }
        return current;
    }

    @Override
    public Integer toHours(LocalDate day, ResourcesPerDay amount) {
        Integer current = null;
        for (IWorkHours each : workHours) {
            current = current == null ? initialHours(each, day, amount)
                    : updateHours(current, each, day, amount);
        }
        return current;
    }

    protected abstract Integer updateHours(Integer current,
            IWorkHours workHours, LocalDate day, ResourcesPerDay amount);

    protected abstract Integer initialHours(IWorkHours workHours,
            LocalDate day, ResourcesPerDay amount);

    protected abstract Integer capacity(IWorkHours workHour, LocalDate date);

    protected abstract Integer updateCapacity(Integer current,
            IWorkHours workHour, LocalDate date);

    @Override
    public boolean thereAreHoursOn(AvailabilityTimeLine availability,
            ResourcesPerDay resourcesPerDay, int hoursToAllocate) {
        for (IWorkHours each : workHours) {
            if (!each.thereAreHoursOn(availability, resourcesPerDay,
                    hoursToAllocate)) {
                return false;
            }
        }
        return true;
    }
}

class Min extends CombinedWorkHours {

    public Min(List<IWorkHours> workHours) {
        super(workHours);
    }

    @Override
    protected Integer updateCapacity(Integer current, IWorkHours workHour,
            LocalDate date) {
        return Math.min(current, workHour.getCapacityAt(date));
    }

    @Override
    protected Integer capacity(IWorkHours workHour, LocalDate date) {
        return workHour.getCapacityAt(date);
    }

    @Override
    protected Integer initialHours(IWorkHours workHours, LocalDate day,
            ResourcesPerDay amount) {
        return workHours.toHours(day, amount);
    }

    @Override
    protected Integer updateHours(Integer current, IWorkHours workHours,
            LocalDate day, ResourcesPerDay amount) {
        return Math.min(workHours.toHours(day, amount), current);
    }

}
