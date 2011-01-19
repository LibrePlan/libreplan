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

package org.navalplanner.ws.calendarexceptiontypes.impl;

import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.workingday.EffortDuration;
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
        EffortDuration duration = calendarExceptionType.getDuration();
        int seconds = (duration != null) ? duration.getSeconds() : 0;
        return new CalendarExceptionTypeDTO(calendarExceptionType.getCode(),
                calendarExceptionType.getName(), calendarExceptionType
                        .getColor(), calendarExceptionType.isOverAssignableWithoutLimit(),
                seconds);
    }

    public final static CalendarExceptionType toEntity(
            CalendarExceptionTypeDTO entityDTO) {
        return CalendarExceptionType.create(entityDTO.code, entityDTO.name,
                entityDTO.color, entityDTO.overAssignable, EffortDuration
                        .seconds(entityDTO.duration));
    }

    public static void updateCalendarExceptionType(
            CalendarExceptionType entity, CalendarExceptionTypeDTO entityDTO) {
        entity.setName(entityDTO.name);
        entity.setColor(entityDTO.color);
        entity.setOverAssignable(entityDTO.overAssignable);
        entity.setDuration(EffortDuration.seconds(entityDTO.duration));
    }

}
