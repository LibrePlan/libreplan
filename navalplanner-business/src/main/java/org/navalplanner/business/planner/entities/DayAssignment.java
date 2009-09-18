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

    public static int sum(Collection<? extends DayAssignment> assigments) {
        int result = 0;
        for (DayAssignment dayAssignment : assigments) {
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
            Collection<T> assignments) {
        Map<LocalDate, List<T>> result = new HashMap<LocalDate, List<T>>();
        for (T t : assignments) {
            LocalDate day = t.getDay();
            if (!result.containsKey(day)){
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
            public int compare(DayAssignment assignment1, DayAssignment assignment2) {
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

}
