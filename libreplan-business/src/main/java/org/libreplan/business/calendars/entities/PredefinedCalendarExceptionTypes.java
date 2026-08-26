/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

package org.libreplan.business.calendars.entities;

import org.libreplan.business.workingday.EffortDuration;

/**
 * Defines the default {@link CalendarExceptionType}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public enum PredefinedCalendarExceptionTypes {

    RESOURCE_HOLIDAY("RESOURCE_HOLIDAY", CalendarExceptionTypeColor.YELLOW, true,
            EffortDuration.zero(),true),
    LEAVE("LEAVE", CalendarExceptionTypeColor.MAGENTA, true,
            EffortDuration.zero(),true),
    STRIKE("STRIKE", CalendarExceptionTypeColor.PURPLE, true,
            EffortDuration.zero(),true),
    BANK_HOLIDAY("BANK_HOLIDAY", CalendarExceptionTypeColor.DEFAULT, true,
            EffortDuration.zero(),true),
    HALF_DAY_HOLIDAY("HALF_DAY_HOLIDAY", CalendarExceptionTypeColor.ORANGE, false,
            EffortDuration.hours(4),true),
    WORKING_DAY("WORKING_DAY", CalendarExceptionTypeColor.BLUE,
            false, EffortDuration.hours(8), false),
    NOT_WORKING_DAY("NOT_WORKING_DAY", CalendarExceptionTypeColor.GREEN, true,
            EffortDuration.zero(),false);

    private final String name;
    private final CalendarExceptionTypeColor color;
    private final Boolean notAssignable;
    private final EffortDuration duration;
    private final Boolean updatable;

    private PredefinedCalendarExceptionTypes(String name,
            CalendarExceptionTypeColor color, Boolean notAssignable,
            EffortDuration duration, Boolean updatable) {
        this.name = name;
        this.color = color;
        this.notAssignable = notAssignable;
        this.duration = duration;
        this.updatable = updatable;
    }

    /**
     * Builds a fresh, never-persisted {@link CalendarExceptionType} matching this predefined
     * type. Returns a new instance on every call rather than a cached one: {@link CalendarBootstrap}
     * saves the result, and a shared instance would keep the id/version it got from its first
     * save across later, independent bootstrap runs (e.g. one per test transaction).
     */
    public CalendarExceptionType getCalendarExceptionType() {
        // Using the name as code in order to be more human friendly
        CalendarExceptionType calendarExceptionType = CalendarExceptionType.create(name, name, color,
                notAssignable, updatable);
        calendarExceptionType.setDuration(duration);
        return calendarExceptionType;
    }

    public static boolean contains(CalendarExceptionType exceptionType) {
        PredefinedCalendarExceptionTypes[] predefinedExceptionTypes = PredefinedCalendarExceptionTypes.values();
        for (PredefinedCalendarExceptionTypes each: predefinedExceptionTypes) {
            if (each.getCalendarExceptionType().getName().equals(exceptionType.getName())) {
                return true;
            }
        }
        return false;
    }

}
