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

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.daos.ICalendarExceptionDAO;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.workingday.EffortDuration;

/**
 * Represents an exceptional day that has a different number of hours. For
 * example, a bank holiday.
 *
 * It is used for the {@link BaseCalendar}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarException extends IntegrationEntity {

    public static CalendarException create(Date date, EffortDuration duration,
            CalendarExceptionType type) {
        return create(new CalendarException(new LocalDate(date), from(duration,
                type), type));
    }

    public static CalendarException create(LocalDate date,
            EffortDuration duration, CalendarExceptionType type) {
        return create(new CalendarException(date, from(duration, type), type));
    }

    public static CalendarException create(LocalDate date, Capacity capacity,
            CalendarExceptionType type) {
        return create(new CalendarException(date, capacity, type));
    }

    public static CalendarException create(String code, LocalDate date,
            EffortDuration duration, CalendarExceptionType type) {
        return create(new CalendarException(date, from(duration, type), type),
                code);
    }

    public static CalendarException create(String code, LocalDate date,
            Capacity capacity, CalendarExceptionType type) {
        return create(new CalendarException(date, capacity, type), code);
    }

    private static Capacity from(EffortDuration duration,
            CalendarExceptionType type) {
        return type.getCapacity().withStandardEffort(duration);
    }

    private static EffortDuration fromHours(Integer hours) {
        return hours == null ? null : EffortDuration.hours(hours);
    }

    public void updateUnvalidated(LocalDate date, Integer hours,
            CalendarExceptionType type) {
        if (date != null) {
            this.date = date;
        }

        if (hours != null) {
            this.capacity.withStandardEffort(fromHours(hours));
        }

        if (type != null) {
            this.type = type;
        }
    }

    private LocalDate date;

    private Capacity capacity;

    private CalendarExceptionType type;

    /**
     * Constructor for hibernate. Do not use!
     */
    public CalendarException() {

    }

    private CalendarException(LocalDate date, Capacity capacity,
            CalendarExceptionType type) {
        Validate.notNull(capacity);
        this.date = date;
        this.capacity = capacity;
        this.type = type;
    }

    @NotNull
    public LocalDate getDate() {
        return date;
    }

    public EffortDuration getDuration() {
        return capacity.getStandardEffort();
    }

    @NotNull
    public Capacity getCapacity() {
        return capacity;
    }

    @NotNull
    public CalendarExceptionType getType() {
        return type;
    }

    @Override
    protected ICalendarExceptionDAO getIntegrationEntityDAO() {
        return Registry.getCalendarExceptionDAO();
    }

}
