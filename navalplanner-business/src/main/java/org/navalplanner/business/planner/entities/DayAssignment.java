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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.Resource;

public abstract class DayAssignment extends BaseEntity {

    public static int sum(Collection<? extends DayAssignment> assignments) {
        int result = 0;
        for (DayAssignment dayAssignment : assignments) {
            result += dayAssignment.getHours();
        }
        return result;
    }

    public static <T extends DayAssignment> Map<Resource, List<T>> byResourceAndOrdered(
            Collection<T> assignments) {
        Map<Resource, List<T>> result = new HashMap<Resource, List<T>>();
        for (T assignment : assignments) {
            Resource resource = assignment.getResource();
            if (!result.containsKey(resource)) {
                result.put(resource, new ArrayList<T>());
            }
            result.get(resource).add(assignment);
        }
        for (Entry<Resource, List<T>> entry : result.entrySet()) {
            Collections.sort(entry.getValue(), byDayComparator());
        }
        return result;
    }

    public static <T extends DayAssignment> Map<LocalDate, List<T>> byDay(
            Collection<? extends T> assignments) {
        Map<LocalDate, List<T>> result = new HashMap<LocalDate, List<T>>();
        for (T t : assignments) {
            LocalDate day = t.getDay();
            if (!result.containsKey(day)) {
                result.put(day, new ArrayList<T>());
            }
            result.get(day).add(t);
        }
        return result;
    }

    @Min(0)
    private int hours;

    @NotNull
    private LocalDate day;

    @NotNull
    private Resource resource;

    protected DayAssignment() {

    }

    protected DayAssignment(LocalDate day, int hours, Resource resource) {
        Validate.notNull(day);
        Validate.isTrue(hours >= 0);
        Validate.notNull(resource);
        this.day = day;
        this.hours = hours;
        this.resource = resource;
    }

    public int getHours() {
        return hours;
    }

    public Resource getResource() {
        return resource;
    }

    public LocalDate getDay() {
        return day;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public static Comparator<DayAssignment> byDayComparator() {
        return new Comparator<DayAssignment>() {

            @Override
            public int compare(DayAssignment assignment1,
                    DayAssignment assignment2) {
                return assignment1.getDay().compareTo(assignment2.getDay());
            }
        };
    }

    public static <T extends DayAssignment> List<T> orderedByDay(
            Collection<T> dayAssignments) {
        List<T> result = new ArrayList<T>(dayAssignments);
        Collections.sort(result, byDayComparator());
        return result;
    }

    public boolean isAssignedTo(Resource resource) {
        return this.resource.equals(resource);
    }

    public boolean includedIn(LocalDate startInclusive, LocalDate endExclusive) {
        return day.compareTo(startInclusive) >= 0
                && day.compareTo(endExclusive) < 0;
    }

}
