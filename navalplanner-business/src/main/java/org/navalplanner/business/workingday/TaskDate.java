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
package org.navalplanner.business.workingday;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.NotNull;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * @author Óscar González Fernández
 *
 */
public class TaskDate implements Comparable<TaskDate> {

    public static TaskDate min(TaskDate... dates) {
        Validate.noNullElements(dates);
        return Collections.min(Arrays.asList(dates));
    }

    public static TaskDate max(TaskDate... dates) {
        Validate.noNullElements(dates);
        return Collections.max(Arrays.asList(dates));
    }

    public static TaskDate create(LocalDate date, EffortDuration effortDuration) {
        return new TaskDate(date, effortDuration);
    }

    private LocalDate date;

    private EffortDuration effortDuration;

    /**
     * Default constructor for hibernate do not use!
     */
    public TaskDate() {
    }

    private TaskDate(LocalDate date, EffortDuration effortDuration) {
        Validate.notNull(date);
        Validate.notNull(effortDuration);
        this.date = date;
        this.effortDuration = effortDuration;
    }

    @NotNull
    public LocalDate getDate() {
        return date;
    }

    public EffortDuration getEffortDuration() {
        return effortDuration == null ? EffortDuration.zero() : effortDuration;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaskDate) {
            TaskDate other = (TaskDate) obj;
            return this.date.equals(other.date)
                    && this.getEffortDuration().equals(
                            other.getEffortDuration());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.date)
                .append(this.getEffortDuration()).toHashCode();
    }

    public boolean areSameDay(Date date) {
        return areSameDay(LocalDate.fromDateFields(date));
    }

    public boolean areSameDay(LocalDate date) {
        return this.date.equals(date);
    }

    public DateTime toDateTimeAtStartOfDay() {
        return this.date.toDateTimeAtStartOfDay();
    }

    @Override
    public int compareTo(TaskDate other) {
        int result = date.compareTo(other.date);
        if (result == 0) {
            result = effortDuration.compareTo(other.effortDuration);
        }
        return result;
    }

}
