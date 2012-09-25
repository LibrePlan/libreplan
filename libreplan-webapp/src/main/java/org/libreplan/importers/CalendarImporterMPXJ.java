/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.importers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.mpxj.reader.ProjectReader;
import net.sf.mpxj.reader.ProjectReaderUtility;

import org.joda.time.LocalDate;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.CalendarData;
import org.libreplan.business.calendars.entities.CalendarException;
import org.libreplan.business.calendars.entities.CalendarExceptionType;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.common.daos.IEntitySequenceDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.importers.CalendarDayHoursDTO.CalendarTypeDayDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
*
* Has all the methods needed to successfully import calendar data of external project files
* into Libreplan using MPXJ.
*
* @author Alba Carro Pérez <alba.carro@gmail.com>
*/
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional
public class CalendarImporterMPXJ implements ICalendarImporter {

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private IEntitySequenceDAO entitySequenceDAO;

    @Autowired
    private ICalendarExceptionTypeDAO calendarExceptionTypeDAO;

    /**
     * Makes a list of {@link CalendarDTO} from a InputStream.
     *
     * @param file
     *            InputStream to extract data from.
     * @return List<CalendarDTO> with the calendar data that we want to import.
     */
    @Override
    public List<CalendarDTO> getCalendarDTOs(InputStream file, String filename) {

        try {

            ProjectReader reader = ProjectReaderUtility
                    .getProjectReader(filename);

            return MPXJProjectFileConversor.convertCalendars(reader.read(file));

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

    /**
     * Makes a list of {@link BaseCalendar} from a list of {@link CalendarDTO}.
     *
     * @param calendarDTOs
     *            List of CalendarDTO to extract data from.
     * @return List<BaseCalendar> with all the calendars that we want.
     * @throws InstanceNotFoundException
     */
    @Override
    public List<BaseCalendar> getBaseCalendars(List<CalendarDTO> calendarDTOs)
            throws InstanceNotFoundException {
        List<BaseCalendar> baseCalendars = new ArrayList<BaseCalendar>();

        for (CalendarDTO calendarDTO : calendarDTOs) {

            if (calendarDTO.parent == null) {

                baseCalendars.add(toBaseCalendar(calendarDTO, null));

            } else {

                BaseCalendar parent = findBaseCalendarParent(baseCalendars,
                        calendarDTO.parent);

                if (parent != null) {

                    baseCalendars.add(toBaseCalendar(calendarDTO, parent));

                } else {

                    throw new ValidationException("Parent calendar not found");

                }

            }
        }

        return baseCalendars;
    }

    /**
     * Private Method
     *
     * Search for a {@link BaseCalendar} that has this name.
     *
     * @param baseCalendars
     *            List<BaseCalendar> to search into.
     * @param name
     *            Search condition
     * @return BaseCalendar with the name equal to the search condition.
     */
    private BaseCalendar findBaseCalendarParent(
            List<BaseCalendar> baseCalendars, String name) {

        for (BaseCalendar baseCalendar:  baseCalendars){

            if (baseCalendar.getName() == name){

                return baseCalendar;

            }
        }

        return null;
    }

    /**
     * Private Method
     *
     * Makes a {@link BaseCalendar} from a {@link CalendarDTO}.
     *
     * @param calendarDTO
     *            CalendarDTO to extract data from.
     * @return BaseCalendar with the calendar that we want.
     * @throws InstanceNotFoundException
     */
    private BaseCalendar toBaseCalendar(CalendarDTO calendarDTO,
            BaseCalendar parent) throws InstanceNotFoundException {

        String code = getCode(EntityNameEnum.CALENDAR);

        String name = validateName(calendarDTO.name);

        Set<CalendarException> calendarExceptions = getCalendarExceptions(calendarDTO.calendarExceptions);

        List<CalendarData> calendarDatas = getCalendarDatas(
                calendarDTO.calendarWeeks, parent);

        BaseCalendar baseCalendar = BaseCalendar.createUnvalidated(code, name,
                parent, calendarExceptions, calendarDatas);

        baseCalendar.setCodeAutogenerated(true);

        baseCalendar.setName(name);

        if (parent != null) {
            baseCalendar.setParent(parent);
        }

        baseCalendar.generateCalendarExceptionCodes(entitySequenceDAO
                .getNumberOfDigitsCode(EntityNameEnum.CALENDAR));

        return baseCalendar;
    }

    /**
     * Private method.
     *
     * Calculate the next code for the entity
     *
     * @param entity
     *         EntityNameEnum Entity
     * @return String new code.
     */
    private String getCode(EntityNameEnum entity) {

        String code = entitySequenceDAO.getNextEntityCode(entity);

        if (code == null) {
            throw new ConcurrentModificationException(
                    "Could not retrieve Code. Please, try again later");
        }

        return code;
    }

    /**
     * Private method
     *
     * Makes a list of {@link CalendarData} from a list of {@link CalendarWeekDTO}.
     *
     * @param calendarWeeks
     *            List of CalendarWeekDTO to extract data from.
     * @param parent
     *            BaseCalendar parent of all the calendarWeeks.
     * @return List<CalendarData> with all the CalendarData that we want.
     */
    private List<CalendarData> getCalendarDatas(
            List<CalendarWeekDTO> calendarWeeks, BaseCalendar parent) {

        List<CalendarData> calendarDatas = new ArrayList<CalendarData>();

        for (CalendarWeekDTO calendarWeekDTO : calendarWeeks) {

            calendarDatas.add(toCalendarData(calendarWeekDTO, parent));
        }

        return calendarDatas;
    }

    /**
     * Private method
     *
     * Makes a list of {@link CalendarException} from a list of {@link CalendarExceptionTO}.
     *
     * @param calendarExceptions
     *            List of CalendarExceptionDTO to extract data from.
     * @return List<CalendarException> with all the CalendarException that we want.
     * @throws InstanceNotFoundException
     */
    private Set<CalendarException> getCalendarExceptions(
            List<CalendarExceptionDTO> calendarExceptionDTOs)
            throws InstanceNotFoundException {

        Set<CalendarException> calendarExceptions = new HashSet<CalendarException>();

        for (CalendarExceptionDTO calendarExceptionDTO : calendarExceptionDTOs) {

            calendarExceptions.add(toCalendarException(calendarExceptionDTO));
        }

        return calendarExceptions;
    }

    /**
     * Private method
     *
     * Makes a {@link CalendarException} from a {@link CalendarExceptionTO}.
     *
     * @param calendarException
     *            CalendarExceptionDTO to extract data from.
     * @return CalendarException with the CalendarException that we want.
     * @throws InstanceNotFoundException
     */
    private CalendarException toCalendarException(
            CalendarExceptionDTO calendarExceptionDTO)
            throws InstanceNotFoundException {

        LocalDate date = null;

        if (calendarExceptionDTO.date != null) {
            date = LocalDate.fromDateFields(calendarExceptionDTO.date);
        }

        CalendarExceptionType calendarExceptionType;

        if(calendarExceptionDTO.working){

            calendarExceptionType = calendarExceptionTypeDAO.findUniqueByName("WORKING_DAY");

        } else {

            calendarExceptionType = calendarExceptionTypeDAO.findUniqueByName("NOT_WORKING_DAY");

        }

        return CalendarException.create(date,
                EffortDuration.hours(calendarExceptionDTO.hours).plus(
                        EffortDuration.minutes(calendarExceptionDTO.minutes)),
                calendarExceptionType);
    }

    /**
     * Private method
     *
     * Validate if a calendar name is not in use.
     * If it is throws an exception.
     * It not return the same name.
     *
     * @param name
     *            String with the name to validate.
     * @return String with the valid name.
     */
    @Transactional
    private String validateName(String name) {

        List<BaseCalendar> calendars = baseCalendarDAO.findByName(name);

        if (calendars.size() == 0) {
            return name;
        } else {
            throw new RuntimeException("Calendar name  already in use");
        }

    }

    /**
     * Private method
     *
     * Makes a {@link CalendarData} from a {@link CalendarWeekDTO}.
     * @param parent
     *            BaseCalendar parent of this workingWeek
     * @param calendarWeek
     *            CalendarWeekDTO to extract data from.
     * @return CalendarData with the CalendarData that we want.
     */
    private CalendarData toCalendarData(CalendarWeekDTO workingWeek,
            BaseCalendar parent) {

        LocalDate expiringDate = null;
        if (workingWeek.endDate != null) {
            expiringDate = LocalDate.fromDateFields(workingWeek.endDate);
        }

        LocalDate startDate = null;
        if (workingWeek.startDate != null) {
            expiringDate = LocalDate.fromDateFields(workingWeek.startDate);
        }

        CalendarData calendarData = CalendarData.create();

        calendarData.setExpiringDate(expiringDate);

        if (parent != null) {

            calendarData.setParent(parent);

        }

        calendarData.setCodeAutogenerated(true);

        Map<Integer, Capacity> capacitiesPerDays = getCapacitiesPerDays(
                workingWeek.hoursPerDays, parent);
        try {
            calendarData.updateCapacitiesPerDay(capacitiesPerDays);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }

        return calendarData;
    }

    /**
     * Private method
     *
     * Makes a map of capacitis with a list of {@link CalendarDayHoursDTO}.
     *
     * @param hoursPerDays
     *            List<CalendarDayHoursDTO> to extract data from.
     * @param parent
     *            BaseCalendar parent of the CalendarDayHoursDTO.
     * @return Map<Integer, Capacity>  with the data that we want.
     */
    private Map<Integer, Capacity> getCapacitiesPerDays(
            List<CalendarDayHoursDTO> hoursPerDays, BaseCalendar parent) {

        Map<Integer, Capacity> result = new HashMap<Integer, Capacity>();

        if (hoursPerDays != null) {
            for (CalendarDayHoursDTO hoursPerDayDTO : hoursPerDays) {
                try {

                    if (hoursPerDayDTO.type == CalendarTypeDayDTO.DEFAULT) {
                        continue;
                    }

                    Integer day = CalendarData.Days.valueOf(
                            hoursPerDayDTO.day.toString()).ordinal();

                    Capacity capacity = Capacity.zero();

                    if (hoursPerDayDTO.type == CalendarTypeDayDTO.WORKING) {
                        capacity = Capacity.create(
                                EffortDuration.hours(hoursPerDayDTO.hours))
                                .overAssignableWithoutLimit();
                    } else if (hoursPerDayDTO.type == CalendarTypeDayDTO.NOT_WORKING) {
                        capacity = Capacity.create(EffortDuration
                                .hours(hoursPerDayDTO.hours));
                    }

                    result.put(day, capacity);

                } catch (IllegalArgumentException e) {

                    throw new ValidationException("a day is not valid");

                } catch (NullPointerException e) {

                    throw new ValidationException("a day is null");
                }
            }
        }

        return result;
    }

    /**
     * Saves a list of {@link BaseCalendar} that has all the calendar data that
     * we want to store in the database.
     *
     * @param Order
     *            Order with the data.
     *
     * @param TaskGroup
     *            TaskGroup with the data.
     */
    @Override
    @Transactional
    public void storeBaseCalendars(List<BaseCalendar> baseCalendars) {

        for (BaseCalendar baseCalendar : baseCalendars) {
            baseCalendarDAO.save(baseCalendar);
        }

    }
}
