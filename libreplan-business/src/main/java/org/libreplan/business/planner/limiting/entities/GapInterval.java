/*
 * This file is part of LibrePlan
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

package org.libreplan.business.planner.limiting.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.libreplan.business.calendars.entities.CalendarAvailability;
import org.libreplan.business.resources.entities.Resource;

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
        DateAndHour intervalStart = DateAndHour.from(interval.getStartDate());
        DateAndHour intervalEnd = DateAndHour.from(interval.getEndDate());

        DateAndHour newStart = (start == null || intervalStart == null) ? null
                : DateAndHour.max(start, intervalStart);
        DateAndHour newEnd = (end == null || intervalEnd == null) ? null
                : DateAndHour.min(end, intervalStart);
        if ((newStart == null && newEnd == null)
                || (newEnd != null && newStart.isAfter(newEnd))) {
            // The period of time is not valid, as it's not an activated period
            // of time according to calendar
            return null;
        }
        return GapInterval.create(newStart, newEnd);
    }

    public Gap gapOn(Resource resource) {
        return Gap.create(resource, start, end);
    }

    public String toString() {
        return String.format("[%s, %s]", start, end);
    }

}