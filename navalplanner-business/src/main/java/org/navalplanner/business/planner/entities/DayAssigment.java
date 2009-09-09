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

public abstract class DayAssigment extends BaseEntity {

    public static <T extends DayAssigment> Map<Resource, List<T>> byResourceAndOrdered(
            Collection<T> assigments) {
        Map<Resource, List<T>> result = new HashMap<Resource, List<T>>();
        for (T assigment : assigments) {
            Resource resource = assigment.getResource();
            if (!result.containsKey(resource)) {
                result.put(resource, new ArrayList<T>());
            }
            result.get(resource).add(assigment);
        }
        for (Entry<Resource, List<T>> entry : result.entrySet()) {
            Collections.sort(entry.getValue(), byDayComparator());
        }
        return result;
    }

    @Min(0)
    private int hours;

    @NotNull
    private LocalDate day;

    @NotNull
    private Resource resource;

    protected DayAssigment() {

    }

    protected DayAssigment(LocalDate day, int hours, Resource resource) {
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

    public static Comparator<DayAssigment> byDayComparator() {
        return new Comparator<DayAssigment>() {

            @Override
            public int compare(DayAssigment assigment1, DayAssigment assigment2) {
                return assigment1.getDay().compareTo(assigment2.getDay());
            }
        };
    }

}
