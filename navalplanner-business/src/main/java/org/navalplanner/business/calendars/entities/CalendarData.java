/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.calendars.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;

/**
 * Represents the information about the calendar that can change through time.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarData extends BaseEntity {

    public static CalendarData create() {
        CalendarData calendarData = new CalendarData();
        calendarData.setNewObject(true);
        return calendarData;
    }

    private Map<Integer, Integer> hoursPerDay;

    private LocalDate expiringDate;

    private BaseCalendar parent;

    public enum Days {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public CalendarData() {
        hoursPerDay = new HashMap<Integer, Integer>();
        setHoursForDay(Days.MONDAY, null);
        setHoursForDay(Days.TUESDAY, null);
        setHoursForDay(Days.WEDNESDAY, null);
        setHoursForDay(Days.THURSDAY, null);
        setHoursForDay(Days.FRIDAY, null);
        setHoursForDay(Days.SATURDAY, null);
        setHoursForDay(Days.SUNDAY, null);
    }

    public Map<Integer, Integer> getHoursPerDay() {
        return hoursPerDay;
    }

    public Integer getHours(Days day) {
        return getHoursForDay(day);
    }

    public void setHours(Days day, Integer hours)
            throws IllegalArgumentException {
        setHoursForDay(day, hours);
    }

    private void setHoursForDay(Days day, Integer hours)
            throws IllegalArgumentException {
        if ((hours != null) && (hours < 0)) {
            throw new IllegalArgumentException(
                    "The number of hours for a day can not be negative");
        }
        hoursPerDay.put(day.ordinal(), hours);
    }

    private Integer getHoursForDay(Days day) {
        return hoursPerDay.get(day.ordinal());
    }

    public boolean isDefault(Days day) {
        return (getHoursForDay(day) == null);
    }

    public void setDefault(Days day) {
        setHoursForDay(day, null);
    }

    public LocalDate getExpiringDate() {
        return expiringDate;
    }

    public void setExpiringDate(Date expiringDate) {
        setExpiringDate(new LocalDate(expiringDate));
    }

    public void setExpiringDate(LocalDate expiringDate) {
        this.expiringDate = expiringDate;
    }

    public CalendarData copy() {
        CalendarData copy = create();

        copy.hoursPerDay = new HashMap<Integer, Integer>(this.hoursPerDay);
        copy.expiringDate = this.expiringDate;
        copy.parent = this.parent;

        return copy;
    }

    public BaseCalendar getParent() {
        return parent;
    }

    public void setParent(BaseCalendar parent) {
        this.parent = parent;
    }

    public void removeExpiringDate() {
        this.expiringDate = null;
    }

    public boolean isPosteriorTo(LocalDate date) {
        return expiringDate == null || expiringDate.compareTo(date) > 0;
    }

    boolean isEmpty() {
        for (Days each : Days.values()) {
            if (!isEmptyFor(each)) {
                return false;
            }
        }
        return true;
    }

    boolean isEmptyFor(Days day) {
        Integer hours = getHours(day);
        if (!isDefault(day) && hours > 0) {
            return false;
        } else if (isDefault(day) && getParent() != null
                && !parent.onlyGivesZeroHours(day)) {
            return false;
        }
        return true;
    }

}
