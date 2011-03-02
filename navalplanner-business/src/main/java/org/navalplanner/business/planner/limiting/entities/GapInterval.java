/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia,S.L
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

package org.navalplanner.business.planner.limiting.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.CalendarAvailability;
import org.navalplanner.business.resources.entities.Resource;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 *         Represents the interval of time of a Gap.
 *
 *         It's possible for a calendar assigned to a resource to define
 *         activation periods of time. This class is used to check if a gap is
 *         activated in a period of time. The method delimitByActivationPeriods
 *         returns the interval of time in which the gap in activated according
 *         to the calendar
 *
 *
 */
public class GapInterval {

    protected DateAndHour start;

    protected DateAndHour end;

    public static GapInterval create(DateAndHour start, DateAndHour end) {
        return new GapInterval(start, end);
    }

    public static Collection<? extends Gap> gapsOn(
            List<GapInterval> intervals, Resource resource) {
        List<Gap> result = new ArrayList<Gap>();
        for (GapInterval each: intervals) {
            result.add(each.gapOn(resource));
        }
        return result;
    }

    public GapInterval(DateAndHour start, DateAndHour end) {
        this.start = start;
        this.end = end;
    }

    public List<GapInterval> delimitByActivationPeriods(
            List<CalendarAvailability> activationPeriods) {
        List<GapInterval> result = new ArrayList<GapInterval>();
        for (CalendarAvailability interval: activationPeriods) {
            GapInterval gapInterval = delimitByInterval(interval);
            if (gapInterval != null) {
                result.add(gapInterval);
            }
        }
        return result;
    }

    private GapInterval delimitByInterval(CalendarAvailability interval) {
        LocalDate start = this.start != null ? this.start.getDate() : null;
        LocalDate end = this.end != null ? this.end.getDate() : null;

        LocalDate newStart = max(start, interval.getStartDate());
        LocalDate newEnd = min(end, interval.getEndDate());
        if ((newStart == null && newEnd == null)
                || (newEnd != null && newStart.isAfter(newEnd))) {
            // The period of time is not valid, as it's not an activated period
            // of time according to calendar
            return null;
        }
        return GapInterval.create(newStart != null ? DateAndHour.from(newStart)
                : null, newEnd != null ? DateAndHour.from(newEnd) : null);
    }

    private LocalDate max(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return null;
        }
        return date1.isAfter(date2) ? date1 : date2;
    }

    private LocalDate min(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return null;
        }
        return date1.isBefore(date2) || date1.isEqual(date2) ? date1 : date2;
    }

    public Gap gapOn(Resource resource) {
        return Gap.create(resource, start, end);
    }

    public String toString() {
        return String.format("[%s, %s]", start, end);
    }

}