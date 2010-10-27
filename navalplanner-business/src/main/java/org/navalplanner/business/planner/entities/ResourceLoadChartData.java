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

package org.navalplanner.business.planner.entities;

import static org.navalplanner.business.workingday.EffortDuration.min;
import static org.navalplanner.business.workingday.EffortDuration.zero;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.ICalendar;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;

/**
 * This class groups the calculation of the three values needed for the
 * chart of the company global resource load. The purpose of the class is
 * having these data pre-calculated to prevent heavy algorithms being
 * run each time the chart is shown.
 * @see PredefinedDatabaseSnapshots
 * @author Jacobo Aragunde Pérez<jaragunde@igalia.com>
 *
 */
public class ResourceLoadChartData {

    private SortedMap<LocalDate, EffortDuration> load;

    private SortedMap<LocalDate, EffortDuration> overload;

    private SortedMap<LocalDate, EffortDuration> availability;

    public ResourceLoadChartData(List<DayAssignment> dayAssignments, List<Resource> resources) {
        SortedMap<LocalDate, Map<Resource, EffortDuration>> map =
            groupDurationsByDayAndResource(dayAssignments);
        this.load = calculateResourceLoadPerDate(map);
        this.overload = calculateResourceOverloadPerDate(map);
        if(load.keySet().isEmpty()) {
            this.availability = new TreeMap<LocalDate, EffortDuration>();
        }
        else {
            this.availability = calculateAvailabilityDurationByDay(
                    resources, load.firstKey(), load.lastKey());
        }

        for (LocalDate day : this.overload.keySet()) {
            EffortDuration overloadDuration = this.overload
            .get(day);
            EffortDuration maxDuration = this.availability.get(day);
            this.overload.put(day,
                    overloadDuration.plus(maxDuration));
        }
    }

    public SortedMap<LocalDate, EffortDuration> getLoad() {
        return load;
    }

    public SortedMap<LocalDate, EffortDuration> getOverload() {
        return overload;
    }

    public SortedMap<LocalDate, EffortDuration> getAvailability() {
        return availability;
    }

    private SortedMap<LocalDate, Map<Resource, EffortDuration>> groupDurationsByDayAndResource(
            List<DayAssignment> dayAssignments) {
        SortedMap<LocalDate, Map<Resource, EffortDuration>> map =
                new TreeMap<LocalDate, Map<Resource, EffortDuration>>();

        for (DayAssignment dayAssignment : dayAssignments) {
            final LocalDate day = dayAssignment.getDay();
            final EffortDuration dayAssignmentDuration = dayAssignment
                    .getDuration();
            Resource resource = dayAssignment.getResource();
            if (map.get(day) == null) {
                map.put(day, new HashMap<Resource, EffortDuration>());
            }
            Map<Resource, EffortDuration> forDay = map.get(day);
            EffortDuration previousDuration = forDay.get(resource);
            previousDuration = previousDuration != null ? previousDuration
                    : EffortDuration.zero();
            forDay.put(dayAssignment.getResource(),
                    previousDuration.plus(dayAssignmentDuration));
        }
        return map;
    }

    private SortedMap<LocalDate, EffortDuration> calculateResourceLoadPerDate(
            SortedMap<LocalDate, Map<Resource, EffortDuration>> durationsGrouped) {
        SortedMap<LocalDate, EffortDuration> map = new TreeMap<LocalDate, EffortDuration>();

        for (LocalDate date : durationsGrouped.keySet()) {
            EffortDuration result = zero();
            PartialDay day = PartialDay.wholeDay(date);
            for (Resource resource : durationsGrouped.get(date).keySet()) {
                ICalendar calendar = resource.getCalendarOrDefault();
                EffortDuration workableTime = calendar.getCapacityOn(day);
                EffortDuration assignedDuration = durationsGrouped.get(
                        day.getDate()).get(resource);
                result = result.plus(min(assignedDuration, workableTime));
            }

            map.put(date, result);
        }
        return map;
    }

    protected abstract class EffortByDayCalculator<T> {
        public SortedMap<LocalDate, EffortDuration> calculate(
                Collection<? extends T> elements) {
            SortedMap<LocalDate, EffortDuration> result = new TreeMap<LocalDate, EffortDuration>();
            if (elements.isEmpty()) {
                return result;
            }
            for (T element : elements) {
                if (included(element)) {
                    EffortDuration duration = getDurationFor(element);
                    LocalDate day = getDayFor(element);
                    EffortDuration previous = result.get(day);
                    previous = previous == null ? zero() : previous;
                    result.put(day, previous.plus(duration));
                }
            }
            return result;
        }

        protected abstract LocalDate getDayFor(T element);

        protected abstract EffortDuration getDurationFor(T element);

        protected boolean included(T each) {
            return true;
        }
    }

    private SortedMap<LocalDate, EffortDuration> calculateResourceOverloadPerDate(
            SortedMap<LocalDate, Map<Resource, EffortDuration>> dayAssignmentGrouped) {
        return new EffortByDayCalculator<Entry<LocalDate, Map<Resource, EffortDuration>>>() {

            @Override
            protected LocalDate getDayFor(
                    Entry<LocalDate, Map<Resource, EffortDuration>> element) {
                return element.getKey();
            }

            @Override
            protected EffortDuration getDurationFor(
                    Entry<LocalDate, Map<Resource, EffortDuration>> element) {
                EffortDuration result = zero();
                PartialDay day = PartialDay.wholeDay(element.getKey());
                for (Entry<Resource, EffortDuration> each : element.getValue()
                        .entrySet()) {
                    EffortDuration overlad = getOverloadAt(day,
                            each.getKey(),
                                    each.getValue());
                    result = result.plus(overlad);
                }
                return result;
            }

            private EffortDuration getOverloadAt(PartialDay day,
                    Resource resource, EffortDuration assignedDuration) {
                ICalendar calendar = resource.getCalendarOrDefault();
                EffortDuration workableDuration = calendar
                        .getCapacityOn(day);
                if (assignedDuration.compareTo(workableDuration) > 0) {
                    return assignedDuration.minus(workableDuration);
                }
                return zero();
            }
        }.calculate(dayAssignmentGrouped.entrySet());
    }

    private SortedMap<LocalDate, EffortDuration> calculateAvailabilityDurationByDay(
            final List<Resource> resources, LocalDate start, LocalDate finish) {
        return new EffortByDayCalculator<Entry<LocalDate, List<Resource>>>() {

            @Override
            protected LocalDate getDayFor(
                    Entry<LocalDate, List<Resource>> element) {
                return element.getKey();
            }

            @Override
            protected EffortDuration getDurationFor(
                    Entry<LocalDate, List<Resource>> element) {
                LocalDate day = element.getKey();
                return sumCalendarCapacitiesForDay(resources, day);
            }

        }.calculate(getResourcesByDateBetween(resources, start, finish));
    }

    protected static EffortDuration sumCalendarCapacitiesForDay(
            Collection<? extends Resource> resources, LocalDate day) {
        PartialDay wholeDay = PartialDay.wholeDay(day);
        EffortDuration sum = zero();
        for (Resource resource : resources) {
            sum = sum.plus(calendarCapacityFor(resource,
                    wholeDay));
        }
        return sum;
    }

    protected static EffortDuration calendarCapacityFor(Resource resource,
            PartialDay day) {
        return resource.getCalendarOrDefault().getCapacityOn(day);
    }

    private Set<Entry<LocalDate, List<Resource>>> getResourcesByDateBetween(
            List<Resource> resources, LocalDate start, LocalDate finish) {
        Map<LocalDate, List<Resource>> result = new HashMap<LocalDate, List<Resource>>();
        for (LocalDate date = new LocalDate(start); date.compareTo(finish) <= 0; date = date
                .plusDays(1)) {
            result.put(date, resources);
        }
        return result.entrySet();
    }
}
