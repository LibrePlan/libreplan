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

package org.navalplanner.ws.calendars.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.CalendarException;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.ws.calendars.api.BaseCalendarDTO;
import org.navalplanner.ws.calendars.api.CalendarDataDTO;
import org.navalplanner.ws.calendars.api.CalendarExceptionDTO;
import org.navalplanner.ws.calendars.api.HoursPerDayDTO;

/**
 * Converter from/to {@link BaseCalendar} related entities to/from DTOs.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public final class CalendarConverter {

    private CalendarConverter() {
    }

    public final static BaseCalendarDTO toDTO(BaseCalendar baseCalendar) {
        List<CalendarExceptionDTO> calendarExceptionDTOs = new ArrayList<CalendarExceptionDTO>();
        for (CalendarException calendarException : baseCalendar.getExceptions()) {
            calendarExceptionDTOs.add(toDTO(calendarException));
        }

        List<CalendarDataDTO> calendarDataDTOs = new ArrayList<CalendarDataDTO>();
        for (CalendarData calendarData : baseCalendar.getCalendarDataVersions()) {
            calendarDataDTOs.add(toDTO(calendarData));
        }

        return new BaseCalendarDTO(baseCalendar.getCode(), baseCalendar
                .getName(), calendarExceptionDTOs, calendarDataDTOs);
    }

    private final static CalendarExceptionDTO toDTO(
            CalendarException calendarException) {
        return new CalendarExceptionDTO(calendarException.getCode(),
                calendarException.getDate().toDateTimeAtStartOfDay().toDate(),
                calendarException.getHours(), calendarException.getType()
                        .getName());
    }

    private final static CalendarDataDTO toDTO(CalendarData calendarData) {
        List<HoursPerDayDTO> hoursPerDayDTOs = new ArrayList<HoursPerDayDTO>();
        Days[] days = CalendarData.Days.values();
        for (Integer day : calendarData.getHoursPerDay().keySet()) {
            String dayName = days[day].name();
            Integer hours = calendarData.getHoursPerDay().get(day);
            hoursPerDayDTOs.add(new HoursPerDayDTO(dayName, hours));
        }

        Date expiringDate = (calendarData.getExpiringDate() != null) ? calendarData
                .getExpiringDate().toDateTimeAtStartOfDay().toDate()
                : null;
        String parentCalendar = (calendarData.getParent() != null) ? calendarData
                .getParent().getCode()
                : null;

        return new CalendarDataDTO(hoursPerDayDTOs, expiringDate,
                parentCalendar);
    }

}