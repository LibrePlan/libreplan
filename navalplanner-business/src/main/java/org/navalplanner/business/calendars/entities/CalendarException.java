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

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.daos.ICalendarExceptionDAO;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.Granularity;

/**
 * Represents an exceptional day that has a different number of hours. For
 * example, a bank holiday.
 *
 * It is used for the {@link BaseCalendar}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarException extends IntegrationEntity {

    public static CalendarException create(Date date, Integer hours,
            CalendarExceptionType type) {
        return create(new CalendarException(new LocalDate(date), hours, type));
    }

    public static CalendarException create(LocalDate date, Integer hours,
            CalendarExceptionType type) {
        return create(new CalendarException(date, hours, type));
    }

    public static CalendarException create(String code, LocalDate date,
            Integer hours, CalendarExceptionType type) {
        return create(new CalendarException(date, hours, type), code);
    }

    private static EffortDuration fromHours(Integer hours) {
        return hours == null ? null : EffortDuration.elapsing(hours,
                Granularity.HOURS);
    }

    public void updateUnvalidated(LocalDate date, Integer hours,
            CalendarExceptionType type) {
        if (date != null) {
            this.date = date;
        }

        if (hours != null) {
            this.duration = fromHours(hours);
        }

        if (type != null) {
            this.type = type;
        }
    }

    private LocalDate date;

    private EffortDuration duration;

    private CalendarExceptionType type;

    /**
     * Constructor for hibernate. Do not use!
     */
    public CalendarException() {

    }

    private CalendarException(LocalDate date, Integer hours,
            CalendarExceptionType type) {
        this.date = date;
        this.duration = fromHours(hours);
        this.type = type;
    }

    @NotNull
    public LocalDate getDate() {
        return date;
    }

    @NotNull
    public Integer getHours() {
        return duration != null ? duration.getHours() : 0;
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
