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

import static org.navalplanner.business.workingday.EffortDuration.zero;

import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.EndOfTime;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.FixedPoint;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.Interval;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.StartOfTime;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.ResourcesPerDay;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ThereAreHoursOnWorkHoursCalculator {

    // Not instantiable
    private ThereAreHoursOnWorkHoursCalculator() {
    }

    /**
     * Caculates if there are enough hours
     */
    public static boolean thereIsAvailableCapacityFor(IWorkHours workHours,
            AvailabilityTimeLine availability,
            ResourcesPerDay resourcesPerDay, EffortDuration effortToAllocate) {
        if (effortToAllocate.isZero()) {
            return true;
        }
        if (resourcesPerDay.isZero()) {
            return false;
        }
        AvailabilityTimeLine realAvailability = workHours.getAvailability()
                .and(availability);
        List<Interval> validPeriods = realAvailability.getValidPeriods();
        if (validPeriods.isEmpty()) {
            return false;
        }

        Interval last = getLast(validPeriods);
        Interval first = validPeriods.get(0);
        final boolean isOpenEnded = last.getEnd().equals(EndOfTime.create())
                || first.getStart().equals(StartOfTime.create());

        return isOpenEnded
                || thereAreCapacityOn(workHours, effortToAllocate,
                        resourcesPerDay,
                        validPeriods);

    }

    private static Interval getLast(List<Interval> validPeriods) {
        return validPeriods.get(validPeriods.size() - 1);
    }

    private static boolean thereAreCapacityOn(IWorkHours workHours,
            EffortDuration effortToAllocate,
            ResourcesPerDay resourcesPerDay, List<Interval> validPeriods) {
        EffortDuration sum = zero();
        for (Interval each : validPeriods) {
            FixedPoint start = (FixedPoint) each.getStart();
            FixedPoint end = (FixedPoint) each.getEnd();
            EffortDuration pending = effortToAllocate.minus(sum);
            sum = sum.plus(sumDurationUntil(workHours, pending,
                    resourcesPerDay, start.getDate(), end.getDate()));
            if (sum.compareTo(effortToAllocate) >= 0) {
                return true;
            }
        }
        return false;
    }

    private static EffortDuration sumDurationUntil(IWorkHours workHours,
            EffortDuration maximum,
            ResourcesPerDay resourcesPerDay,
            LocalDate start, LocalDate end) {
        EffortDuration result = zero();
        int days = org.joda.time.Days.daysBetween(start, end).getDays();
        for (int i = 0; i < days; i++) {
            LocalDate current = start.plusDays(i);
            result = result.plus(workHours.asDurationOn(current, resourcesPerDay));
            if (result.compareTo(maximum) >= 0) {
                return maximum;
            }
        }
        return result;
    }

}
