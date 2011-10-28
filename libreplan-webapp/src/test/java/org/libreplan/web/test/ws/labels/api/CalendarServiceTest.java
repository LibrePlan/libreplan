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

package org.libreplan.web.test.ws.labels.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.ws.calendars.api.BaseCalendarDTO;
import org.libreplan.ws.calendars.api.BaseCalendarListDTO;
import org.libreplan.ws.calendars.api.ICalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link ICalendarService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class CalendarServiceTest {

    @Autowired
    private ICalendarService calendarService;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private BaseCalendar givenBaseCalendarStored() {
        BaseCalendar calendar = BaseCalendar.createBasicCalendar();
        calendar.setName("calendar-name");

        baseCalendarDAO.save(calendar);
        baseCalendarDAO.flush();
        sessionFactory.getCurrentSession().evict(calendar);
        calendar.dontPoseAsTransientObjectAnymore();

        return calendar;
    }

    @Test
    public void exportBaseCalendars() {
        int previous = baseCalendarDAO.getBaseCalendars().size();

        BaseCalendarListDTO baseCalendars = calendarService.getBaseCalendars();
        assertThat(baseCalendars.baseCalendars.size(), equalTo(previous));
    }

    @Test
    public void exportBaseCalendars2() {
        int previous = baseCalendarDAO.getBaseCalendars().size();

        BaseCalendar calendar = givenBaseCalendarStored();

        BaseCalendarListDTO baseCalendars = calendarService.getBaseCalendars();
        assertThat(baseCalendars.baseCalendars.size(), equalTo(previous + 1));

        BaseCalendarDTO calendarDTO = baseCalendars.baseCalendars.get(previous);
        assertThat(calendarDTO.code, equalTo(calendar.getCode()));
        assertThat(calendarDTO.name, equalTo(calendar.getName()));
    }

}
