/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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


/**
 * Defines the default {@link CalendarExceptionType}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public enum PredefinedCalendarExceptionTypes {

    HOLIDAY("HOLIDAY", CalendarExceptionTypeColor.DEFAULT, true),
    SICK_LEAVE("SICK_LEAVE", CalendarExceptionTypeColor.DEFAULT, true),
    LEAVE("LEAVE", CalendarExceptionTypeColor.DEFAULT, true),
    STRIKE("STRIKE", CalendarExceptionTypeColor.DEFAULT, true),
    BANK_HOLIDAY("BANK_HOLIDAY", CalendarExceptionTypeColor.DEFAULT, true),
    WORKABLE_BANK_HOLIDAY("WORKABLE_BANK_HOLIDAY", CalendarExceptionTypeColor.DEFAULT, false);

    private CalendarExceptionType calendarExceptionType;

    private PredefinedCalendarExceptionTypes(String name,
            CalendarExceptionTypeColor color, Boolean notAssignable) {
        // Using the name as code in order to be more human friendly
        calendarExceptionType = CalendarExceptionType.create(name, name, color,
                notAssignable);
    }

    public CalendarExceptionType getCalendarExceptionType() {
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
