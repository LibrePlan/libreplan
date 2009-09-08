package org.navalplanner.business.planner.entities;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.Resource;

public abstract class DayAssigment extends BaseEntity {

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

}
