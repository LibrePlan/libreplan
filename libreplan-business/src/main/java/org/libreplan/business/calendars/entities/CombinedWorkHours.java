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

package org.libreplan.business.calendars.entities;

import static java.util.Arrays.asList;
import static org.libreplan.business.workingday.EffortDuration.max;
import static org.libreplan.business.workingday.EffortDuration.min;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workingday.ResourcesPerDay;

public abstract class CombinedWorkHours implements ICalendar {

    private final List<ICalendar> calendars;

    public CombinedWorkHours(Collection<? extends ICalendar> calendars) {
        Validate.notNull(calendars);
        Validate.isTrue(!calendars.isEmpty());
        this.calendars = notNull(calendars);
        Validate.isTrue(!this.calendars.isEmpty(),
                "there should be at least one workHours not null");
    }

    private static List<ICalendar> notNull(
            Collection<? extends ICalendar> calendars) {
        List<ICalendar> result = new ArrayList<ICalendar>();
        for (ICalendar each : calendars) {
            if (each != null) {
                result.add(each);
            }
        }
        return result;
    }

    public static CombinedWorkHours minOf(ICalendar... workHours) {
        Validate.notNull(workHours);
        return new Min(asList(workHours));
    }

    public static CombinedWorkHours maxOf(ICalendar... workHours) {
        return maxOf(asList(workHours));
    }

    public static CombinedWorkHours maxOf(
            Collection<? extends ICalendar> calendars) {
        return new Max(calendars);
    }

    @Override
    public EffortDuration getCapacityOn(PartialDay day) {
        EffortDuration current = null;
        for (ICalendar workHour : calendars) {
            current = current == null ? workHour.getCapacityOn(day)
                    : updateCapacity(current, workHour.getCapacityOn(day));
        }
        return current;
    }

    @Override
    public EffortDuration asDurationOn(PartialDay day, ResourcesPerDay amount) {
        EffortDuration result = null;
        for (ICalendar each : calendars) {
            result = result == null ? each.asDurationOn(day, amount)
                    : updateDuration(result, each.asDurationOn(day, amount));
        }
        return result;
    }

    @Override
    public Capacity getCapacityWithOvertime(LocalDate day) {
        Capacity result = null;
        for (ICalendar each : calendars) {
            Capacity current = each.getCapacityWithOvertime(day);
            result = result == null ? current : updateCapacity(result, current);
        }
        return result;
    }

    @Override
    public AvailabilityTimeLine getAvailability() {
        AvailabilityTimeLine result = AvailabilityTimeLine.allValid();
        for (ICalendar each : calendars) {
            result = compoundAvailability(result, each.getAvailability());
        }
        return result;
    }

    protected abstract AvailabilityTimeLine compoundAvailability(
            AvailabilityTimeLine accumulated, AvailabilityTimeLine each);

    protected abstract EffortDuration updateDuration(EffortDuration current,
            EffortDuration each);

    protected abstract EffortDuration updateCapacity(EffortDuration current,
            EffortDuration each);

    protected abstract Capacity updateCapacity(Capacity a, Capacity current);

    @Override
    public boolean thereAreCapacityFor(AvailabilityTimeLine availability,
            ResourcesPerDay resourcesPerDay, EffortDuration durationToAllocate) {
        return ThereAreHoursOnWorkHoursCalculator.thereIsAvailableCapacityFor(
                this, availability, resourcesPerDay, durationToAllocate)
                .thereIsCapacityAvailable();
    }
}

class Min extends CombinedWorkHours {

    public Min(List<ICalendar> calendars) {
        super(calendars);
    }

    @Override
    protected EffortDuration updateCapacity(EffortDuration current,
            EffortDuration each) {
        return Collections.min(asList(current, each));
    }

    @Override
    protected EffortDuration updateDuration(EffortDuration current,
            EffortDuration each) {
        return min(current, each);
    }

    @Override
    protected AvailabilityTimeLine compoundAvailability(
            AvailabilityTimeLine accumulated, AvailabilityTimeLine each) {
        return accumulated.and(each);
    }

    @Override
    protected Capacity updateCapacity(Capacity accumulated, Capacity current) {
        return Capacity.min(accumulated, current);
    }

}

class Max extends CombinedWorkHours {

    public Max(Collection<? extends ICalendar> calendars) {
        super(calendars);
    }

    @Override
    protected EffortDuration updateCapacity(EffortDuration current,
            EffortDuration each) {
        return Collections.max(asList(current, each));
    }

    @Override
    protected EffortDuration updateDuration(EffortDuration current,
            EffortDuration each) {
        return max(current, each);
    }

    @Override
    protected AvailabilityTimeLine compoundAvailability(
            AvailabilityTimeLine accumulated, AvailabilityTimeLine each) {
        return accumulated.or(each);
    }

    @Override
    protected Capacity updateCapacity(Capacity accumulated, Capacity current) {
        return Capacity.max(accumulated, current);
    }
}
