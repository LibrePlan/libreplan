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
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;
import static org.libreplan.web.test.ws.common.Util.getUniqueName;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.CalendarData;
import org.libreplan.business.calendars.entities.CalendarException;
import org.libreplan.business.calendars.entities.CalendarExceptionType;
import org.libreplan.business.calendars.entities.CalendarExceptionTypeColor;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.IConfigurationBootstrap;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.web.test.ws.calendarexceptiontypes.api.CalendarExceptionTypeServiceTest;
import org.libreplan.ws.calendars.api.BaseCalendarDTO;
import org.libreplan.ws.calendars.api.BaseCalendarListDTO;
import org.libreplan.ws.calendars.api.CalendarDataDTO;
import org.libreplan.ws.calendars.api.CalendarExceptionDTO;
import org.libreplan.ws.calendars.api.HoursPerDayDTO;
import org.libreplan.ws.calendars.api.ICalendarService;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.impl.DateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Collections;

/**
 * Tests for <code>ICalendarService</code>.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE, WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
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

    private CalendarExceptionType addedExceptionType;

    @BeforeTransaction
    public void ensureOneExceptionType() {
        addedExceptionType = transactionService.runOnTransaction(new IOnTransaction<CalendarExceptionType>() {
            @Override
            public CalendarExceptionType execute() {
                CalendarExceptionType result;
                result = CalendarExceptionType.create("name", CalendarExceptionTypeColor.DEFAULT, false);
                result.setCode("TypeCode_A");
                calendarExceptionTypeDAO.save(result);
                return result;
            }
        });

        addedExceptionType.dontPoseAsTransientObjectAnymore();
    }


    /**
     * It removes added {@link CalendarExceptionType} to avoid problems in other tests.
     * The associated calendar exceptions are also removed, so the {@link CalendarExceptionType} can be removed without error.
     *
     * More concretely, it was causing problem in {@link CalendarExceptionTypeServiceTest} if it was executed after this test.
     */
    @AfterTransaction
    public void removeAddedExceptionType() {
        transactionService.runOnTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                try {
                    removeAssociatedCalendarData();
                    removeAddedType();
                    return null;
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @SuppressWarnings("unchecked")
            private void removeAssociatedCalendarData() {
                Session s = sessionFactory.getCurrentSession();
                Query query = s.createQuery("from CalendarException e where e.type = :type").setParameter("type", addedExceptionType);
                List<CalendarException> found = query.list();
                for (CalendarException each : found) {
                    s.delete(each);
                }
            }

            private void removeAddedType() throws InstanceNotFoundException {
                calendarExceptionTypeDAO.remove(addedExceptionType.getId());
            }
        });
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
    @Transactional
    public void testAddValidBaseCalendar() throws InstanceNotFoundException {

        /* Build valid base calendar "bc1" (5 constraint violations) */

        /* Build a calendar exception */
        CalendarExceptionDTO exceptionDTO_1 =
                new CalendarExceptionDTO(getUniqueName(), toXml(getValidDate(0)), 7, addedExceptionType.getCode());

        CalendarExceptionDTO exceptionDTO_2 =
                new CalendarExceptionDTO(getUniqueName(), toXml(getValidDate(1)), 7, addedExceptionType.getCode());

        List<CalendarExceptionDTO> calendarExceptions = new ArrayList<>();
        calendarExceptions.add(exceptionDTO_1);
        calendarExceptions.add(exceptionDTO_2);

        /* Build a calendar data */
        HoursPerDayDTO hoursPerDayDTO_1 = new HoursPerDayDTO(CalendarData.Days.FRIDAY.name(), 4);
        HoursPerDayDTO hoursPerDayDTO_2 = new HoursPerDayDTO(CalendarData.Days.TUESDAY.name(), 4);
        List<HoursPerDayDTO> listHoursPerDayDTO = new ArrayList<>();
        listHoursPerDayDTO.add(hoursPerDayDTO_1);
        listHoursPerDayDTO.add(hoursPerDayDTO_2);

        /* missing code, date, hoursPerDays and parent */
        CalendarDataDTO dataDTO_1 = new CalendarDataDTO(null, null, null);

        CalendarDataDTO dataDTO_2 =
                new CalendarDataDTO("codeData", listHoursPerDayDTO, toXml(getValidDate(4)), getDefaultCalendar().getCode());

        List<CalendarDataDTO> calendarData = new ArrayList<>();
        calendarData.add(dataDTO_1);
        calendarData.add(dataDTO_2);

        /* Build Base Calendar list */
        BaseCalendarDTO bc1 = new BaseCalendarDTO(
                getUniqueName(), getUniqueName(),
                null, calendarExceptions,
                Collections.singletonList(new CalendarDataDTO(
                        Collections.singletonList(new HoursPerDayDTO(CalendarData.Days.MONDAY.name(), 8)), null, null)));

        String codeBaseCalendar = getUniqueName();
        BaseCalendarDTO bc2 = new BaseCalendarDTO(codeBaseCalendar, getUniqueName(), null, null, calendarData);

        BaseCalendarListDTO baseCalendars = createBaseCalendarListDTO(bc1, bc2);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
                calendarService.addBaseCalendars(baseCalendars).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(), instanceConstraintViolationsList.size() == 0);

        BaseCalendar baseCalendar = baseCalendarDAO.findByCode(codeBaseCalendar);
        assertTrue(baseCalendar.getExceptions().isEmpty());
        assertTrue(baseCalendar.getCalendarDataVersions().size() == 2);

        CalendarData data = baseCalendar.getCalendarDataByCode("codeData");
        assertEquals(Capacity.create(EffortDuration.hours(4)).overAssignableWithoutLimit(),
                data.getCapacityOn(CalendarData.Days.FRIDAY));

        assertEquals(Capacity.create(EffortDuration.hours(4)).overAssignableWithoutLimit(true),
                data.getCapacityOn(CalendarData.Days.TUESDAY));
    }

    @Test
    @Transactional
    public void testAddInvalidBaseCalendar() throws InstanceNotFoundException {
        /* Build valid base calendar "bc1" (5 constraint violations) */

        /* Build two calendar exception with the same date */
        CalendarExceptionDTO exceptionDTO_1 =
                new CalendarExceptionDTO(getUniqueName(), toXml(getValidDate(0)), 7, addedExceptionType.getCode());

        CalendarExceptionDTO exceptionDTO_2 =
                new CalendarExceptionDTO(getUniqueName(), toXml(getValidDate(0)), 7, addedExceptionType.getCode());

        /* Build two calendar exception with the past date */
        CalendarExceptionDTO exceptionDTO_3 =
                new CalendarExceptionDTO(getUniqueName(), toXml(getInvalidDate()), 7, addedExceptionType.getCode());

        /* Build two calendar exception with the invalid type */
        CalendarExceptionDTO exceptionDTO_4 =
                new CalendarExceptionDTO(getUniqueName(), toXml(getInvalidDate()), 7, "InvalidType");

        List<CalendarExceptionDTO> calendarExceptions = new ArrayList<>();
        calendarExceptions.add(exceptionDTO_1);
        calendarExceptions.add(exceptionDTO_2);
        calendarExceptions.add(exceptionDTO_3);
        calendarExceptions.add(exceptionDTO_4);

        /* Build Base Calendar list */
        BaseCalendarDTO bc1 =
                new BaseCalendarDTO(getUniqueName(), getUniqueName(), null, calendarExceptions, new ArrayList<>());

        BaseCalendarListDTO baseCalendars = createBaseCalendarListDTO(bc1);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
                calendarService.addBaseCalendars(baseCalendars).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(), instanceConstraintViolationsList.size() == 1);

    }

    @Test
    @Transactional
    public void testAddInvalidCalendarData() {
        /* Build a calendar data */
        HoursPerDayDTO hoursPerDayDTO_1 = new HoursPerDayDTO("XXX", 4);
        List<HoursPerDayDTO> listHoursPerDayDTO = new ArrayList<>();
        listHoursPerDayDTO.add(hoursPerDayDTO_1);

        /* Missing code,date, hoursPerDays and parent */
        CalendarDataDTO dataDTO_2 =
                new CalendarDataDTO("codeData_2", listHoursPerDayDTO, toXml(getInvalidDate()), getDefaultCalendar().getCode());

        List<CalendarDataDTO> calendarData = new ArrayList<>();
        calendarData.add(dataDTO_2);

        String codeBaseCalendar = getUniqueName();
        BaseCalendarDTO bc2 = new BaseCalendarDTO(codeBaseCalendar, getUniqueName(), null, null, calendarData);

        BaseCalendarListDTO baseCalendars = createBaseCalendarListDTO(bc2);

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
                calendarService.addBaseCalendars(baseCalendars).instanceConstraintViolationsList;

        assertTrue(instanceConstraintViolationsList.toString(), instanceConstraintViolationsList.size() == 1);
    }

    private BaseCalendarListDTO createBaseCalendarListDTO(BaseCalendarDTO... calendarDTOs) {

        List<BaseCalendarDTO> baseCalendarList = new ArrayList<>();

        for (BaseCalendarDTO c : calendarDTOs) {
            baseCalendarList.add(c);
        }

        return new BaseCalendarListDTO(baseCalendarList);

    }

    private BaseCalendar getDefaultCalendar() {

        IOnTransaction<BaseCalendar> find = new IOnTransaction<BaseCalendar>() {
            @Override
            public BaseCalendar execute() {
                BaseCalendar defaultCalendar = configurationDAO.getConfiguration().getDefaultCalendar();
                defaultCalendar.getCode();
                return defaultCalendar;
            }
        };

        return transactionService.runOnAnotherTransaction(find);

    }

}
