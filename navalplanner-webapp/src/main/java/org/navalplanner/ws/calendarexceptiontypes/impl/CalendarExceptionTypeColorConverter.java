/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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

package org.navalplanner.ws.calendarexceptiontypes.impl;

import static org.navalplanner.web.I18nHelper._;

import java.util.HashMap;
import java.util.Map;

import org.navalplanner.business.calendars.entities.CalendarExceptionTypeColor;
import org.navalplanner.ws.calendarexceptiontypes.api.CalendarExceptionTypeColorDTO;

/**
 * Converter from/to {@link CalendarExceptionTypeColor} entities to/from DTOs.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class CalendarExceptionTypeColorConverter {

    private final static Map<CalendarExceptionTypeColor, CalendarExceptionTypeColorDTO> calendarExceptionTypeColorToDTO = new HashMap<CalendarExceptionTypeColor, CalendarExceptionTypeColorDTO>();

    private final static Map<CalendarExceptionTypeColorDTO, CalendarExceptionTypeColor> calendarExceptionTypeColorFromDTO = new HashMap<CalendarExceptionTypeColorDTO, CalendarExceptionTypeColor>();

    static {

        calendarExceptionTypeColorFromDTO.put(
                CalendarExceptionTypeColorDTO.RED,
                CalendarExceptionTypeColor.DEFAULT);

        calendarExceptionTypeColorToDTO.put(CalendarExceptionTypeColor.DEFAULT,
                CalendarExceptionTypeColorDTO.DEFAULT);
        calendarExceptionTypeColorFromDTO.put(
                CalendarExceptionTypeColorDTO.DEFAULT,
                CalendarExceptionTypeColor.DEFAULT);

        calendarExceptionTypeColorToDTO.put(CalendarExceptionTypeColor.GREEN,
                CalendarExceptionTypeColorDTO.GREEN);
        calendarExceptionTypeColorFromDTO.put(
                CalendarExceptionTypeColorDTO.GREEN,
                CalendarExceptionTypeColor.GREEN);

        calendarExceptionTypeColorToDTO.put(CalendarExceptionTypeColor.BLUE,
                CalendarExceptionTypeColorDTO.BLUE);
        calendarExceptionTypeColorFromDTO.put(
                CalendarExceptionTypeColorDTO.BLUE,
                CalendarExceptionTypeColor.BLUE);

    }

    public final static CalendarExceptionTypeColorDTO toDTO(
            CalendarExceptionTypeColor resource) {
        CalendarExceptionTypeColorDTO value = calendarExceptionTypeColorToDTO
                .get(resource);

        if (value == null) {
            throw new RuntimeException(_("Unable to convert {0} "
                    + "value to {1} type", resource.toString(),
                    CalendarExceptionTypeColorDTO.class.getName()));
        } else {
            return value;
        }
    }

    /**
     * It returns <code>null</code> if the parameter is <code>null</code>.
     */
    public final static CalendarExceptionTypeColor toEntity(
            CalendarExceptionTypeColorDTO resource) {
        if (resource == null) {
            return null;
        }

        CalendarExceptionTypeColor value = calendarExceptionTypeColorFromDTO
                .get(resource);

        if (value == null) {
            throw new RuntimeException(_("Unable to convert value to {0} type",
                    CalendarExceptionTypeColor.class.getName()));
        } else {
            return value;
        }
    }

}
