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

package org.navalplanner.business.calendars.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.daos.ICalendarDataDAO;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.workingday.EffortDuration;

/**
 * Represents the information about the calendar that can change through time.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarData extends IntegrationEntity {

    public static CalendarData create() {
        return create(new CalendarData());
    }

    public static CalendarData createUnvalidated(String code,
            LocalDate expiringDate, BaseCalendar parent) {
        CalendarData calendarData = create(new CalendarData(), code);
        calendarData.expiringDate = expiringDate;
        calendarData.parent = parent;
        return calendarData;
    }

    public void updateUnvalidated(LocalDate expiringDate, BaseCalendar parent) {
        if (expiringDate != null) {
            this.expiringDate = expiringDate;
        }
        if (parent != null) {
            this.parent = parent;
        }
    }

    public void updateHourPerDay(Map<Integer, Integer> hoursPerDay)
            throws IllegalArgumentException {
        if ((hoursPerDay != null)) {
            for (Days day : Days.values()) {
                Integer hours = hoursPerDay.get(day.ordinal());
                if (hours != null) {
                    setDurationAt(day, EffortDuration.hours(hours));
                }
            }
        }
    }

    private Map<Integer, EffortDuration> effortPerDay;

    private LocalDate expiringDate;

    private BaseCalendar parent;

    public enum Days {
        MONDAY(_("Monday")), TUESDAY(_("Tuesday")), WEDNESDAY(_("Wednesday")), THURSDAY(
                _("Thursday")), FRIDAY(_("Friday")), SATURDAY(_("Saturday")), SUNDAY(
                _("Sunday"));

        /**
         * Forces to mark the string as needing translation
         */
        private static String _(String string) {
            return string;
        }

        private String name;

        private Days(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public CalendarData() {
        effortPerDay = new HashMap<Integer, EffortDuration>();
        for (Days each : Days.values()) {
            setDurationAt(each, null);
        }
    }

    public Map<Integer, Integer> getHoursPerDay() {
        return asHours(effortPerDay);
    }

    private Map<Integer, Integer> asHours(Map<Integer, EffortDuration> efforts) {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (Entry<Integer, EffortDuration> each : efforts.entrySet()) {
            EffortDuration value = each.getValue();
            result.put(each.getKey(), value == null ? null : value.getHours());
        }
        return result;
    }

    public EffortDuration getDurationAt(Days day) {
        return effortPerDay.get(day.ordinal());
    }

    public void setDurationAt(Days day, EffortDuration duration) {
        effortPerDay.put(day.ordinal(), duration);
    }

    public boolean isDefault(Days day) {
        return getDurationAt(day) == null;
    }

    public void setDefault(Days day) {
        setDurationAt(day, null);
    }

    /**
     * The expiringDate. It is exclusive.
     */
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
        copy.effortPerDay = new HashMap<Integer, EffortDuration>(
                this.effortPerDay);
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
        return !isDefault(day) && getDurationAt(day).isZero() || isDefault(day)
                && hasParent() && getParent().onlyGivesZeroHours(day)
                || isDefault(day) && !hasParent();
    }

    private boolean hasParent() {
        return getParent() != null;
    }

    @Override
    protected ICalendarDataDAO getIntegrationEntityDAO() {
        return Registry.getCalendarDataDAO();
    }

}
