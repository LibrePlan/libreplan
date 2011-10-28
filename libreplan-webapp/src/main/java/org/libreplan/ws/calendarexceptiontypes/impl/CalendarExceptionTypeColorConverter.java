/*
 * This file is part of LibrePlan
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

package org.libreplan.ws.calendarexceptiontypes.impl;

import static org.libreplan.web.I18nHelper._;

import java.util.HashMap;
import java.util.Map;

import org.libreplan.business.calendars.entities.CalendarExceptionTypeColor;
import org.libreplan.ws.calendarexceptiontypes.api.CalendarExceptionTypeColorDTO;

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

        addEquivalence(CalendarExceptionTypeColor.DEFAULT,
                CalendarExceptionTypeColorDTO.DEFAULT);

        addEquivalence(CalendarExceptionTypeColor.GREEN,
                CalendarExceptionTypeColorDTO.GREEN);

        addEquivalence(CalendarExceptionTypeColor.BLUE,
                CalendarExceptionTypeColorDTO.BLUE);

        addEquivalence(CalendarExceptionTypeColor.MAGENTA,
                CalendarExceptionTypeColorDTO.MAGENTA);

        addEquivalence(CalendarExceptionTypeColor.CYAN,
                CalendarExceptionTypeColorDTO.CYAN);

        addEquivalence(CalendarExceptionTypeColor.YELLOW,
                CalendarExceptionTypeColorDTO.YELLOW);

        addEquivalence(CalendarExceptionTypeColor.ORANGE,
                CalendarExceptionTypeColorDTO.ORANGE);

        addEquivalence(CalendarExceptionTypeColor.BLACK,
                CalendarExceptionTypeColorDTO.BLACK);

    }

    private static void addEquivalence(CalendarExceptionTypeColor origin,
            CalendarExceptionTypeColorDTO destination) {
        calendarExceptionTypeColorToDTO.put(origin, destination);
        calendarExceptionTypeColorFromDTO.put(destination, origin);
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
