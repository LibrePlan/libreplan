/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.planner.chart;

import static org.navalplanner.business.planner.chart.ContiguousDaysLine.compound;
import static org.navalplanner.business.planner.chart.ContiguousDaysLine.sum;
import static org.navalplanner.business.planner.chart.ContiguousDaysLine.toSortedMap;
import static org.navalplanner.business.workingday.EffortDuration.min;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.hibernate.notification.PredefinedDatabaseSnapshots;
import org.navalplanner.business.planner.chart.ContiguousDaysLine.IValueTransformer;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.IEffortFrom;
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
public class ResourceLoadChartData implements ILoadChartData {

    private SortedMap<LocalDate, EffortDuration> load;

    private SortedMap<LocalDate, EffortDuration> overload;

    private SortedMap<LocalDate, EffortDuration> availability;

    public ResourceLoadChartData(List<DayAssignment> dayAssignments, List<Resource> resources) {
        this(dayAssignments, resources, null, null);
    }

    public ResourceLoadChartData(List<DayAssignment> dayAssignments,
            List<Resource> resources, LocalDate startInclusive,
            LocalDate endExclusive) {

        ContiguousDaysLine<List<DayAssignment>> assignments = ContiguousDaysLine
                .byDay(dayAssignments);
        if (startInclusive != null && endExclusive != null) {
            assignments = assignments.subInterval(startInclusive, endExclusive);
        }
        ContiguousDaysLine<EffortDuration> load = assignments
                .transform(extractLoad());

        ContiguousDaysLine<EffortDuration> overload = assignments
                .transform(extractOverload());

        ContiguousDaysLine<EffortDuration> availabilityOnAllResources = assignments
                .transform(extractAvailabilityOnAllResources(resources));

        this.load = toSortedMap(ContiguousDaysLine.min(load,
                availabilityOnAllResources));
        this.overload = toSortedMap(sum(overload, availabilityOnAllResources));
        this.availability = toSortedMap(availabilityOnAllResources);
    }

    public static IValueTransformer<List<DayAssignment>, EffortDuration> extractOverload() {
        return compound(effortByResource(), calculateOverload());
    }

    private static IValueTransformer<List<DayAssignment>, Map<Resource, EffortDuration>> effortByResource() {
        return new IValueTransformer<List<DayAssignment>, Map<Resource, EffortDuration>>() {

            @Override
            public Map<Resource, EffortDuration> transform(LocalDate day,
                    List<DayAssignment> previousValue) {
                Map<Resource, List<DayAssignment>> byResource = DayAssignment
                        .byResource(previousValue);
                Map<Resource, EffortDuration> result = new HashMap<Resource, EffortDuration>();
                for (Entry<Resource, List<DayAssignment>> each : byResource
                        .entrySet()) {
                    result.put(each.getKey(),
                            DayAssignment.sum(each.getValue()));
                }
                return result;
            }
        };
    }

    public static IValueTransformer<Map<Resource, EffortDuration>, EffortDuration> calculateOverload() {
        return new IValueTransformer<Map<Resource, EffortDuration>, EffortDuration>() {

            @Override
            public EffortDuration transform(LocalDate day,
                    Map<Resource, EffortDuration> previousValue) {

                final PartialDay wholeDay = PartialDay.wholeDay(day);
                return EffortDuration.sum(previousValue.entrySet(),
                        new IEffortFrom<Entry<Resource, EffortDuration>>() {

                            @Override
                            public EffortDuration from(
                                    Entry<Resource, EffortDuration> each) {
                                EffortDuration capacity = calendarCapacityFor(
                                        each.getKey(), wholeDay);
                                EffortDuration assigned = each.getValue();
                                return assigned.minus(min(capacity, assigned));
                            }
                        });
            }
        };
    }

    public static IValueTransformer<List<DayAssignment>, EffortDuration> extractLoad() {
        return new IValueTransformer<List<DayAssignment>, EffortDuration>() {

            @Override
            public EffortDuration transform(LocalDate day,
                    List<DayAssignment> previousValue) {
                return DayAssignment.sum(previousValue);
            }
        };
    }

    public static IValueTransformer<List<DayAssignment>, EffortDuration> extractAvailabilityOnAssignedResources() {
        return new IValueTransformer<List<DayAssignment>, EffortDuration>() {

            @Override
            public EffortDuration transform(LocalDate day,
                    List<DayAssignment> previousValue) {
                Set<Resource> resources = getResources(previousValue);
                return sumCalendarCapacitiesForDay(resources, day);
            }

            private Set<Resource> getResources(List<DayAssignment> assignments) {
                Set<Resource> resources = new HashSet<Resource>();
                for (DayAssignment dayAssignment : assignments) {
                    resources.add(dayAssignment.getResource());
                }
                return resources;
            }
        };
    }

    private IValueTransformer<List<DayAssignment>, EffortDuration> extractAvailabilityOnAllResources(
            final List<Resource> resources) {
        return new IValueTransformer<List<DayAssignment>, EffortDuration>() {

            @Override
            public EffortDuration transform(LocalDate day,
                    List<DayAssignment> previousValue) {
                return sumCalendarCapacitiesForDay(resources, day);
            }
        };
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

    public ILoadChartData on(final LocalDate startInclusive,
            final LocalDate endExclusive) {

        final ResourceLoadChartData original = ResourceLoadChartData.this;
        if (startInclusive == null && endExclusive == null) {
            return original;
        }
        return new ILoadChartData() {

            @Override
            public SortedMap<LocalDate, EffortDuration> getOverload() {
                return filter(original.getOverload());
            }

            @Override
            public SortedMap<LocalDate, EffortDuration> getLoad() {
                return filter(original.getLoad());
            }

            @Override
            public SortedMap<LocalDate, EffortDuration> getAvailability() {
                return filter(original.getAvailability());
            }

            private SortedMap<LocalDate, EffortDuration> filter(
                    SortedMap<LocalDate, EffortDuration> map) {
                if (startInclusive != null) {
                    return map.tailMap(startInclusive);
                }
                if (endExclusive != null) {
                    return map.headMap(endExclusive);
                }
                return map.subMap(startInclusive, endExclusive);
            }
        };
    }

    private static EffortDuration sumCalendarCapacitiesForDay(
            Collection<? extends Resource> resources, LocalDate day) {

        final PartialDay wholeDay = PartialDay.wholeDay(day);

        return EffortDuration.sum(resources, new IEffortFrom<Resource>() {
            @Override
            public EffortDuration from(Resource each) {
                return calendarCapacityFor(each, wholeDay);
            }
        });
    }

    protected static EffortDuration calendarCapacityFor(Resource resource,
            PartialDay day) {
        return resource.getCalendarOrDefault().getCapacityOn(day);
    }

}
