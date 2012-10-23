/*
 * This file is part of LibrePlan
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

package org.libreplan.web.test.ws.basecalendars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.ws.common.Util.getUniqueName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.CalendarData;
import org.libreplan.business.calendars.entities.CalendarExceptionType;
import org.libreplan.business.calendars.entities.CalendarExceptionTypeColor;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.IConfigurationBootstrap;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.ws.calendars.api.BaseCalendarDTO;
import org.libreplan.ws.calendars.api.BaseCalendarListDTO;
import org.libreplan.ws.calendars.api.CalendarDataDTO;
import org.libreplan.ws.calendars.api.CalendarExceptionDTO;
import org.libreplan.ws.calendars.api.HoursPerDayDTO;
import org.libreplan.ws.calendars.api.ICalendarService;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.impl.DateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for <code>ICalendarService</code>.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE })
@Transactional

public class BaseCalendarServiceTest {

    @Autowired
    private ICalendarService calendarService;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private ICalendarExceptionTypeDAO calendarExceptionTypeDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IConfigurationBootstrap configurationBootstrap;

    private final String typeCode = "TypeCode_A";

    @Before
    public void loadConfiguration() {

        IOnTransaction<Void> load = new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                configurationBootstrap.loadRequiredData();
                return null;
            }
        };

        transactionService.runOnAnotherTransaction(load);

    }

    @Test
    @Rollback(false)
    // FIXME: This exception type is kept in DB and it may cause problems in
    // other tests, for example in
    // CalendarExceptionTypeTest.exportExceptionTypes(), depending on the
    // execution order. We must ensure that
    public void givenCalendarExceptionTypeStored() {
        CalendarExceptionType calendarExceptionType = CalendarExceptionType
                .create("name", CalendarExceptionTypeColor.DEFAULT, false);
        calendarExceptionType.setCode(typeCode);

        calendarExceptionTypeDAO.save(calendarExceptionType);
        calendarExceptionTypeDAO.flush();
        sessionFactory.getCurrentSession().evict(calendarExceptionType);
        calendarExceptionType.dontPoseAsTransientObjectAnymore();

    }

    private Date getValidDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, day);

        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        calendar.set(year, month, date);
        return calendar.getTime();
    }

    private Date getInvalidDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) - 1;
        int year = calendar.get(Calendar.YEAR);

        calendar.set(year, month, date);
        return calendar.getTime();
    }

    private XMLGregorianCalendar toXml(Date date) {
        return DateConverter.toXMLGregorianCalendar(date);
    }

    @Test
    // FIXME: when this test finishes, the two new calendar exceptions are not
    // being removed. This is a problem to delete the calendar exception type
    // and causes conflicts in other tests, for example in
    // CalendarExceptionTypeTest.exportExceptionTypes()
    public void testAddValidBaseCalendar() throws InstanceNotFoundException {

        /* Build valid base calendar "bc1" (5 constraint violations). */
        /* Build a calendar exception */
        CalendarExceptionDTO exceptionDTO_1 = new CalendarExceptionDTO(
                getUniqueName(), toXml(getValidDate(0)), new Integer(7),
                typeCode);

        CalendarExceptionDTO exceptionDTO_2 = new CalendarExceptionDTO(
                getUniqueName(), toXml(getValidDate(1)), new Integer(7),
                typeCode);

        List<CalendarExceptionDTO> calendarExceptions = new ArrayList<CalendarExceptionDTO>();
        calendarExceptions.add(exceptionDTO_1);
        calendarExceptions.add(exceptionDTO_2);

        /* Build a calendar data */
        HoursPerDayDTO hoursPerDayDTO_1 = new HoursPerDayDTO(CalendarData.Days.FRIDAY.name(), new Integer(4));
        HoursPerDayDTO hoursPerDayDTO_2 = new HoursPerDayDTO(CalendarData.Days.TUESDAY.name(), new Integer(4));
        List<HoursPerDayDTO> listHoursPerDayDTO = new ArrayList<HoursPerDayDTO>();
        listHoursPerDayDTO.add(hoursPerDayDTO_1);
        listHoursPerDayDTO.add(hoursPerDayDTO_2);

        /* missing code,date, hoursPerDays and parent */
        CalendarDataDTO dataDTO_1 = new CalendarDataDTO(null, null, null);
        CalendarDataDTO dataDTO_2 = new CalendarDataDTO("codeData",
                listHoursPerDayDTO, toXml(getValidDate(4)),
                getDefaultCalendar()
                        .getCode());

        List<CalendarDataDTO> calendarDatas = new ArrayList<CalendarDataDTO>();
        calendarDatas.add(dataDTO_1);
        calendarDatas.add(dataDTO_2);

        /* Build Base Calendar list. */
        BaseCalendarDTO bc1 = new BaseCalendarDTO(getUniqueName(),
                getUniqueName(), null, calendarExceptions, Arrays
                        .asList(new CalendarDataDTO(Arrays
                                .asList(new HoursPerDayDTO(
                                        CalendarData.Days.MONDAY.name(),
                                        new Integer(8))), null, null)));

        String codeBaseCalendar = getUniqueName();
        BaseCalendarDTO bc2 = new BaseCalendarDTO(codeBaseCalendar,
                getUniqueName(), null, null, calendarDatas);

        BaseCalendarListDTO baseCalendars = createBaseCalendarListDTO(bc1, bc2);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = calendarService
                .addBaseCalendars(baseCalendars).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 0);

        BaseCalendar baseCalendar = baseCalendarDAO
                .findByCode(codeBaseCalendar);
        assertTrue(baseCalendar.getExceptions().isEmpty());
        assertTrue(baseCalendar.getCalendarDataVersions().size() == 2);

        CalendarData data = baseCalendar.getCalendarDataByCode("codeData");
        assertEquals(Capacity.create(EffortDuration.hours(4))
                .overAssignableWithoutLimit(),
                data.getCapacityOn(CalendarData.Days.FRIDAY));
        assertEquals(Capacity.create(EffortDuration.hours(4))
                .overAssignableWithoutLimit(true),
                data.getCapacityOn(CalendarData.Days.TUESDAY));
    }

    @Test
    public void testAddInvalidBaseCalendar() throws InstanceNotFoundException {
        /* Build valid base calendar "bc1" (5 constraint violations). */
        /* Build two calendar exception with the same date */
        CalendarExceptionDTO exceptionDTO_1 = new CalendarExceptionDTO(
                getUniqueName(), toXml(getValidDate(0)), new Integer(7),
                typeCode);

        CalendarExceptionDTO exceptionDTO_2 = new CalendarExceptionDTO(
                getUniqueName(), toXml(getValidDate(0)), new Integer(7),
                typeCode);

        /* Build two calendar exception with the past date */
        CalendarExceptionDTO exceptionDTO_3 = new CalendarExceptionDTO(
                getUniqueName(), toXml(getInvalidDate()), new Integer(7),
                typeCode);

        /* Build two calendar exception with the invalid type */
        CalendarExceptionDTO exceptionDTO_4 = new CalendarExceptionDTO(
                getUniqueName(), toXml(getInvalidDate()), new Integer(7),
                "InvalidType");

        List<CalendarExceptionDTO> calendarExceptions = new ArrayList<CalendarExceptionDTO>();
        calendarExceptions.add(exceptionDTO_1);
        calendarExceptions.add(exceptionDTO_2);
        calendarExceptions.add(exceptionDTO_3);
        calendarExceptions.add(exceptionDTO_4);

        /* Build Base Calendar list. */
        BaseCalendarDTO bc1 = new BaseCalendarDTO(getUniqueName(),
                getUniqueName(), null, calendarExceptions,
                new ArrayList<CalendarDataDTO>());

        BaseCalendarListDTO baseCalendars = createBaseCalendarListDTO(bc1);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = calendarService
                .addBaseCalendars(baseCalendars).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);

    }

    @Test
    public void testAddInvalidCalendarData() {
        /* Build a calendar data */
        HoursPerDayDTO hoursPerDayDTO_1 = new HoursPerDayDTO("XXX",
                new Integer(4));
        List<HoursPerDayDTO> listHoursPerDayDTO = new ArrayList<HoursPerDayDTO>();
        listHoursPerDayDTO.add(hoursPerDayDTO_1);

        /* missing code,date, hoursPerDays and parent */
        CalendarDataDTO dataDTO_2 = new CalendarDataDTO("codeData_2",
                listHoursPerDayDTO, toXml(getInvalidDate()),
                getDefaultCalendar()
                        .getCode());

        List<CalendarDataDTO> calendarDatas = new ArrayList<CalendarDataDTO>();
        calendarDatas.add(dataDTO_2);

        String codeBaseCalendar = getUniqueName();
        BaseCalendarDTO bc2 = new BaseCalendarDTO(codeBaseCalendar,
                getUniqueName(), null, null, calendarDatas);

        BaseCalendarListDTO baseCalendars = createBaseCalendarListDTO(bc2);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = calendarService
                .addBaseCalendars(baseCalendars).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(),
                instanceConstraintViolationsList.size() == 1);
    }

    private BaseCalendarListDTO createBaseCalendarListDTO(
            BaseCalendarDTO... calendarDTOs) {

        List<BaseCalendarDTO> baseCalendarList = new ArrayList<BaseCalendarDTO>();

        for (BaseCalendarDTO c : calendarDTOs) {
            baseCalendarList.add(c);
        }

        return new BaseCalendarListDTO(baseCalendarList);

    }

    private BaseCalendar getDefaultCalendar() {

        IOnTransaction<BaseCalendar> find = new IOnTransaction<BaseCalendar>() {

            @Override
            public BaseCalendar execute() {
                BaseCalendar defaultCalendar = configurationDAO
                        .getConfiguration().getDefaultCalendar();
                defaultCalendar.getCode();
                return defaultCalendar;
            }
        };

        return transactionService.runOnAnotherTransaction(find);

    }

}
