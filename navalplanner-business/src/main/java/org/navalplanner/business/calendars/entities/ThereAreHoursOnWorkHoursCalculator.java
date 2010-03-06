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

import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.EndOfTime;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.FixedPoint;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.Interval;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.StartOfTime;
import org.navalplanner.business.planner.entities.ResourcesPerDay;

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
    public static boolean thereAreHoursOn(IWorkHours workHours,
            AvailabilityTimeLine availability,
            ResourcesPerDay resourcesPerDay, int hoursToAllocate) {
        if (hoursToAllocate == 0) {
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
                || thereAreHoursOn(workHours, hoursToAllocate, resourcesPerDay,
                        validPeriods);

    }

    private static Interval getLast(List<Interval> validPeriods) {
        return validPeriods.get(validPeriods.size() - 1);
    }

    private static boolean thereAreHoursOn(IWorkHours workHours,
            int hoursToAllocate,
            ResourcesPerDay resourcesPerDay, List<Interval> validPeriods) {
        int sum = 0;
        for (Interval each : validPeriods) {
            FixedPoint start = (FixedPoint) each.getStart();
            FixedPoint end = (FixedPoint) each.getEnd();
            int pending = hoursToAllocate - sum;
            sum += sumHoursUntil(workHours, pending, resourcesPerDay, start
                    .getDate(), end
                    .getDate());
            if (sum >= hoursToAllocate) {
                return true;
            }
        }
        return false;
    }

    private static int sumHoursUntil(IWorkHours workHours, int maximum,
            ResourcesPerDay resourcesPerDay,
            LocalDate start, LocalDate end) {
        int result = 0;
        int days = org.joda.time.Days.daysBetween(start, end).getDays();
        for (int i = 0; i < days; i++) {
            LocalDate current = start.plusDays(i);
            result += workHours.toHours(current, resourcesPerDay);
            if (result >= maximum) {
                return result;
            }
        }
        return result;
    }

}
