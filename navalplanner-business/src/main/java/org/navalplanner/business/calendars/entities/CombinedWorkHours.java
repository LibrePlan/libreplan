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

package org.navalplanner.business.calendars.entities;

import static java.util.Arrays.asList;
import static org.navalplanner.business.workingday.EffortDuration.max;
import static org.navalplanner.business.workingday.EffortDuration.min;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.ResourcesPerDay;

public abstract class CombinedWorkHours implements IWorkHours {

    private final List<IWorkHours> workHours;

    public CombinedWorkHours(Collection<? extends IWorkHours> workHours) {
        Validate.notNull(workHours);
        Validate.isTrue(!workHours.isEmpty());
        this.workHours = notNull(workHours);
        Validate.isTrue(!this.workHours.isEmpty(),
                "there should be at least one workHours not null");
    }

    private static List<IWorkHours> notNull(
            Collection<? extends IWorkHours> workHours) {
        List<IWorkHours> result = new ArrayList<IWorkHours>();
        for (IWorkHours each : workHours) {
            if (each != null) {
                result.add(each);
            }
        }
        return result;
    }

    public static CombinedWorkHours minOf(IWorkHours... workHours) {
        Validate.notNull(workHours);
        return new Min(asList(workHours));
    }

    public static CombinedWorkHours maxOf(IWorkHours... workHours) {
        return maxOf(asList(workHours));
    }

    public static CombinedWorkHours maxOf(
            Collection<? extends IWorkHours> workHours) {
        return new Max(workHours);
    }

    @Override
    public EffortDuration getCapacityDurationAt(LocalDate date) {
        EffortDuration current = null;
        for (IWorkHours workHour : workHours) {
            current = current == null ? workHour.getCapacityDurationAt(date)
                    : updateCapacity(current,
                            workHour.getCapacityDurationAt(date));
        }
        return current;
    }

    @Override
    public EffortDuration asDurationOn(LocalDate day, ResourcesPerDay amount) {
        EffortDuration result = null;
        for (IWorkHours each : workHours) {
            result = result == null ? each.asDurationOn(day, amount)
                    : updateDuration(result, each.asDurationOn(day, amount));
        }
        return result;
    }

    @Override
    public AvailabilityTimeLine getAvailability() {
        AvailabilityTimeLine result = AvailabilityTimeLine.allValid();
        for (IWorkHours each : workHours) {
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

    @Override
    public boolean thereAreCapacityFor(AvailabilityTimeLine availability,
            ResourcesPerDay resourcesPerDay, EffortDuration durationToAllocate) {
        return ThereAreHoursOnWorkHoursCalculator.thereIsAvailableCapacityFor(this,
                availability, resourcesPerDay, durationToAllocate);
    }
}

class Min extends CombinedWorkHours {

    public Min(List<IWorkHours> workHours) {
        super(workHours);
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

}

class Max extends CombinedWorkHours {

    public Max(Collection<? extends IWorkHours> workHours) {
        super(workHours);
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
}
