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

package org.navalplanner.ws.calendars.impl;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.business.calendars.entities.CalendarException;
import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.calendars.entities.Capacity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.ws.calendars.api.BaseCalendarDTO;
import org.navalplanner.ws.calendars.api.CalendarDataDTO;
import org.navalplanner.ws.calendars.api.CalendarExceptionDTO;
import org.navalplanner.ws.calendars.api.HoursPerDayDTO;
import org.navalplanner.ws.common.impl.DateConverter;

/**
 * Converter from/to {@link BaseCalendar} related entities to/from DTOs.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
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

        String parent = null;
        if (baseCalendar.getParent() != null) {
            parent = baseCalendar.getParent().getCode();
        }

        return new BaseCalendarDTO(baseCalendar.getCode(), baseCalendar
                .getName(), parent, calendarExceptionDTOs, calendarDataDTOs);
    }

    private final static CalendarExceptionDTO toDTO(
            CalendarException calendarException) {
        XMLGregorianCalendar date = DateConverter
                .toXMLGregorianCalendar(calendarException.getDate());
        int hours = calendarException.getDuration().getHours();
        String code = calendarException.getType().getCode();
        return new CalendarExceptionDTO(calendarException.getCode(), date,
                hours, code);
    }

    private final static CalendarDataDTO toDTO(CalendarData calendarData) {
        List<HoursPerDayDTO> hoursPerDayDTOs = new ArrayList<HoursPerDayDTO>();
        Days[] days = CalendarData.Days.values();
        for (Integer day : calendarData.getHoursPerDay().keySet()) {
            String dayName = days[day].name();
            Integer hours = calendarData.getHoursPerDay().get(day);
            hoursPerDayDTOs.add(new HoursPerDayDTO(dayName, hours));
        }

        XMLGregorianCalendar expiringDate = (calendarData.getExpiringDate() != null) ? DateConverter
                .toXMLGregorianCalendar(calendarData
                    .getExpiringDate())
                : null;
        String parentCalendar = (calendarData.getParent() != null) ? calendarData
                .getParent().getCode()
                : null;

        return new CalendarDataDTO(calendarData.getCode(), hoursPerDayDTOs,
                expiringDate, parentCalendar);
    }

    public final static BaseCalendar toEntity(BaseCalendarDTO baseCalendarDTO) {

        Set<CalendarException> exceptions = new HashSet<CalendarException>();
        if (baseCalendarDTO.calendarExceptions != null) {
            for (CalendarExceptionDTO exceptionDTO : baseCalendarDTO.calendarExceptions) {
                exceptions.add(toEntity(exceptionDTO));
            }
        }

        List<CalendarData> calendarDataVersions = new ArrayList<CalendarData>();
        if (baseCalendarDTO.calendarDatas != null) {
            for (CalendarDataDTO calendarDataDTO : baseCalendarDTO.calendarDatas) {
                calendarDataVersions.add(toEntity(calendarDataDTO));
            }
            calendarDataVersions = getVersionsOrderedByExpiringDate(calendarDataVersions);
        }

        BaseCalendar parent = findBaseCalendarParent(baseCalendarDTO.parent);

        try {
        return BaseCalendar.createUnvalidated(baseCalendarDTO.code,
                baseCalendarDTO.name, parent, exceptions, calendarDataVersions);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(_(e.getMessage()));
        }
    }

    public final static CalendarException toEntity(
            CalendarExceptionDTO calendarExceptionDTO) {

        LocalDate date = null;
        if (calendarExceptionDTO.date != null) {
            date = DateConverter.toLocalDate(calendarExceptionDTO.date);
        }

        CalendarExceptionType type = findCalendarExceptionType(calendarExceptionDTO.calendarExceptionTypeCode);

        return CalendarException.create(calendarExceptionDTO.code, date,
                EffortDuration.hours(calendarExceptionDTO.hours), type);
    }

    public final static CalendarData toEntity(CalendarDataDTO calendarDataDTO) {
        LocalDate expiringDate = null;
        if (calendarDataDTO.expiringDate != null) {
            expiringDate = DateConverter
                    .toLocalDate(calendarDataDTO.expiringDate);
        }

        BaseCalendar parent = findBaseCalendarParent(calendarDataDTO.parentCalendar);

        CalendarData calendarData = CalendarData.createUnvalidated(
                calendarDataDTO.code, expiringDate, parent);

        Map<Integer, Capacity> capacitiesPerDays = getCapacitiesPerDays(calendarDataDTO.hoursPerDays);
        try {
            calendarData.updateCapacitiesPerDay(capacitiesPerDays);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(_(e.getMessage()));
        }

        return calendarData;
    }

    public final static void update(BaseCalendar baseCalendar,
            BaseCalendarDTO baseCalendarDTO) {

        if (baseCalendarDTO.calendarExceptions != null) {
            for (CalendarExceptionDTO exceptionDTO : baseCalendarDTO.calendarExceptions) {

                if (StringUtils.isBlank(exceptionDTO.code)) {
                    throw new ValidationException(
                            _("missing code in a calendar exception"));
                }

                if (exceptionDTO.date == null) {
                    throw new ValidationException(
                            _("missing date in a calendar exception"));
                }
                // find by code
                try {
                    CalendarException exception = baseCalendar
                            .getCalendarExceptionByCode(exceptionDTO.code);
                    update(exception, exceptionDTO);
                } catch (InstanceNotFoundException e) {
                    // find by date
                    CalendarException exception = baseCalendar
                            .getOwnExceptionDay(DateConverter
                                    .toLocalDate(exceptionDTO.date));
                    if (exception != null) {
                        throw new ValidationException(
                                _("exception date already exists"));
                    } else {
                        try {
                            baseCalendar
                                    .addExceptionDay(toEntity(exceptionDTO));
                        } catch (IllegalArgumentException o) {
                            throw new ValidationException(_(o.getMessage()));
                        }
                    }
                }
            }
        }

        if (baseCalendarDTO.calendarDatas != null) {

            for (CalendarDataDTO calendarDataDTO : baseCalendarDTO.calendarDatas) {

                if (StringUtils.isBlank(calendarDataDTO.code)) {
                    throw new ValidationException(
                            _("missing code in a calendar data version"));
                }

                // find by code
                try {
                    CalendarData version = baseCalendar
                            .getCalendarDataByCode(calendarDataDTO.code);
                    update(version, calendarDataDTO);
                } catch (InstanceNotFoundException e) {
                    try {
                        baseCalendar.addNewVersion(toEntity(calendarDataDTO));
                    } catch (IllegalArgumentException o) {
                        throw new ValidationException(_(o.getMessage()));
                    }
                }
            }

        }

        BaseCalendar parent = null;
        if (!StringUtils.isBlank(baseCalendarDTO.parent)) {
            try {
                parent = Registry.getBaseCalendarDAO().findByCode(
                        baseCalendarDTO.parent);
            } catch (InstanceNotFoundException e) {
                throw new ValidationException(
                        _("The base calendar parent not found"));
            }
        }

        baseCalendar.updateUnvalidated(baseCalendarDTO.name, parent);

    }

    public final static void update(CalendarException exception,
            CalendarExceptionDTO calendarExceptionDTO) {

        LocalDate date = null;
        if (calendarExceptionDTO.date != null) {
            date = DateConverter.toLocalDate(calendarExceptionDTO.date);
        }

        CalendarExceptionType type = findCalendarExceptionType(calendarExceptionDTO.calendarExceptionTypeCode);

        exception.updateUnvalidated(date, calendarExceptionDTO.hours, type);
    }

    public final static void update(CalendarData calendarData,
            CalendarDataDTO calendarDataDTO) {

        LocalDate expiringDate = null;
        if (calendarDataDTO.expiringDate != null) {
            expiringDate = DateConverter
                    .toLocalDate(calendarDataDTO.expiringDate);
        }

        BaseCalendar parent = findBaseCalendarParent(calendarDataDTO.parentCalendar);

        Map<Integer, Capacity> capacitiesPerDays = getCapacitiesPerDays(calendarDataDTO.hoursPerDays);
        try {
            calendarData.updateCapacitiesPerDay(capacitiesPerDays);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(_(e.getMessage()));
        }

        calendarData.updateUnvalidated(expiringDate, parent);

    }

    private static Map<Integer, Capacity> getCapacitiesPerDays(
            List<HoursPerDayDTO> hoursPerDayDTOs) {
        Map<Integer, Capacity> result = new HashMap<Integer, Capacity>();
        if (hoursPerDayDTOs != null) {
            for (HoursPerDayDTO hoursPerDayDTO : hoursPerDayDTOs) {
                try {
                    Integer day = CalendarData.Days.valueOf(hoursPerDayDTO.day)
                        .ordinal();
                    Capacity capacity = Capacity.create(
                            EffortDuration.hours(hoursPerDayDTO.hours))
                            .overAssignableWithoutLimit(true);
                    result.put(day, capacity);
                } catch (IllegalArgumentException e) {
                    throw new ValidationException(_("a day is not valid"));
                } catch(NullPointerException e){
                    throw new ValidationException(_("a day is empty"));
                }
            }
        }
        return result;
    }

    private static BaseCalendar findBaseCalendarParent(String parentCode) {
        if (StringUtils.isBlank(parentCode)) {
            return null;
        }

        try {
            return Registry.getBaseCalendarDAO().findByCode(parentCode);
        } catch (InstanceNotFoundException e) {
            throw new ValidationException(
                    _("The base calendar parent not found"));
        }
    }

    private static CalendarExceptionType findCalendarExceptionType(
            String typeCode) {
        if (StringUtils.isBlank(typeCode)) {
            return null;
        }

        try {
            return Registry.getCalendarExceptionTypeDAO().findByCode(typeCode);
        } catch (InstanceNotFoundException e) {
            throw new ValidationException(
                    _("The calendar exception type not found"));
        }
    }

    private static final List<CalendarData> getVersionsOrderedByExpiringDate(
            List<CalendarData> versions) {

        Collections.sort(versions, new Comparator<CalendarData>() {

            @Override
            public int compare(CalendarData o1, CalendarData o2) {
                if (o1.getExpiringDate() == null) {
                    return 1;
                }
                if (o2.getExpiringDate() == null) {
                    return -1;
                }
                return o1.getExpiringDate().compareTo(o2.getExpiringDate());
            }
        });
        return versions;
    }

}