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

package org.navalplanner.ws.calendarexceptiontypes.impl;

import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.ws.calendarexceptiontypes.api.CalendarExceptionTypeDTO;

/**
 * Converter from/to {@link CalendarExceptionType} related entities to/from
 * DTOs.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public final class CalendarExceptionTypeConverter {

    private CalendarExceptionTypeConverter() {
    }

    public final static CalendarExceptionTypeDTO toDTO(
            CalendarExceptionType calendarExceptionType) {
        return new CalendarExceptionTypeDTO(calendarExceptionType.getCode(),
                calendarExceptionType.getName(), calendarExceptionType
                        .getColor(), calendarExceptionType.isOverAssignable());
    }

}
