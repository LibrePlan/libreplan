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

import static org.navalplanner.business.workingday.EffortDuration.zero;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.util.deepcopy.AfterCopy;
import org.navalplanner.business.util.deepcopy.OnCopy;
import org.navalplanner.business.util.deepcopy.Strategy;
import org.navalplanner.business.workingday.EffortDuration;

public abstract class DayAssignment extends BaseEntity {

    public static <T extends DayAssignment> List<T> getAtInterval(
            List<T> orderedAssignments, LocalDate startInclusive,
            LocalDate endExclusive) {
        int position = findFirstAfterOrEqual(orderedAssignments, startInclusive);
        List<T> couldBeIncluded = orderedAssignments.subList(position, Math
                .max(
                orderedAssignments.size(), position));
        List<T> result = new ArrayList<T>();
        for (T each : couldBeIncluded) {
            if (each.getDay().compareTo(endExclusive) >= 0) {
                break;
            }
            assert each.includedIn(startInclusive, endExclusive);
            result.add(each);
        }
        return result;
    }

    private static int findFirstAfterOrEqual(
            List<? extends DayAssignment> orderedAssignments, LocalDate startInclusive) {
        int start = 0;
        int end = orderedAssignments.size() - 1;
        while (start <= end) {
            int middle = start + (end - start) / 2;
            if (orderedAssignments.get(middle).getDay().compareTo(
                    startInclusive) < 0) {
                start = middle + 1;
            } else {
                end = middle - 1;
            }
        }
        return start;
    }

    public static EffortDuration sum(
            Collection<? extends DayAssignment> assignments) {
        EffortDuration result = zero();
        for (DayAssignment each : assignments) {
            result = result.plus(each.getDuration());
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

    public static Set<Resource> getAllResources(
            Collection<? extends DayAssignment> assignments) {
        Set<Resource> result = new HashSet<Resource>();
        for (DayAssignment dayAssignment : assignments) {
            result.add(dayAssignment.getResource());
        }
        return result;
    }

    public static <T extends DayAssignment> List<T> getOfType(Class<T> klass,
            Collection<? extends DayAssignment> dayAssignments) {
        List<T> result = new ArrayList<T>();
        for (DayAssignment each : dayAssignments) {
            if (klass.isInstance(each)) {
                result.add(klass.cast(each));
            }
        }
        return result;
    }

    public static List<SpecificDayAssignment> specific(
            Collection<? extends DayAssignment> dayAssignments) {
        return getOfType(SpecificDayAssignment.class, dayAssignments);
    }

    public static List<GenericDayAssignment> generic(
            Collection<? extends DayAssignment> dayAssignments) {
        return getOfType(GenericDayAssignment.class, dayAssignments);
    }

    public static <T extends DayAssignment> List<T> withScenario(
            Scenario scenario, Collection<T> dayAssignments) {
        List<T> result = new ArrayList<T>();
        for (T each : dayAssignments) {
            if (ObjectUtils.equals(each.getScenario(), scenario)) {
                result.add(each);
            }
        }
        return result;
    }

    private EffortDuration duration;

    @NotNull
    private LocalDate day;

    @NotNull
    @OnCopy(Strategy.SHARE)
    private Resource resource;

    private Boolean consolidated = false;

    protected DayAssignment() {

    }

    protected DayAssignment(LocalDate day, EffortDuration duration,
            Resource resource) {
        Validate.notNull(day);
        Validate.notNull(duration);
        Validate.notNull(resource);
        this.day = day;
        this.duration = duration;
        this.resource = resource;
    }

    /**
     * @deprecated Use {@link #getDuration()}
     */
    @Deprecated
    public int getHours() {
        return duration.getHours();
    }

    @NotNull
    public EffortDuration getDuration() {
        return duration;
    }

    public Resource getResource() {
        return resource;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setConsolidated(Boolean consolidated) {
        this.consolidated = consolidated;
    }

    public Boolean isConsolidated() {
        return consolidated == null ? false : consolidated;
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

    @AfterCopy
    protected void associateToResource() {
        getResource().addNewAssignments(Arrays.asList(this));
    }

    final void detach() {
        getResource().removeAssignments(Arrays.asList(this));
        detachFromAllocation();
    }

    protected abstract void detachFromAllocation();

    public abstract boolean belongsTo(Object allocation);

    /**
     * @return <code>null</code> if {@link DayAssignment this} day assignment
     *         still has not been explicitly associated to a {@link Scenario}
     */
    public abstract Scenario getScenario();

    public abstract DayAssignment withDuration(EffortDuration newDuration);

}
