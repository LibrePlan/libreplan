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

package org.navalplanner.web.test.ws.calendarexceptiontypes.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.ws.calendarexceptiontypes.api.CalendarExceptionTypeDTO;
import org.navalplanner.ws.calendarexceptiontypes.api.CalendarExceptionTypeListDTO;
import org.navalplanner.ws.calendarexceptiontypes.api.ICalendarExceptionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link ICalendarExceptionTypeService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class CalendarExceptionTypeServiceTest {

    @Autowired
    private ICalendarExceptionTypeService calendarExceptionTypeService;

    @Autowired
    private ICalendarExceptionTypeDAO calendarExceptionTypeDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private CalendarExceptionType givenCalendarExceptionTypeStored() {
        CalendarExceptionType calendarExceptionType = CalendarExceptionType
                .create("name", "color", false);

        calendarExceptionTypeDAO.save(calendarExceptionType);
        calendarExceptionTypeDAO.flush();
        sessionFactory.getCurrentSession().evict(calendarExceptionType);
        calendarExceptionType.dontPoseAsTransientObjectAnymore();

        return calendarExceptionType;
    }

    @Test
    public void exportLabelTypes() {
        CalendarExceptionTypeListDTO list = calendarExceptionTypeService
                .getCalendarExceptionType();
        assertTrue(list.calendarExceptionTypes.isEmpty());
    }

    @Test
    public void exportLabelTypes2() {
        CalendarExceptionType calendarExceptionType = givenCalendarExceptionTypeStored();

        CalendarExceptionTypeListDTO list = calendarExceptionTypeService
                .getCalendarExceptionType();
        assertThat(list.calendarExceptionTypes.size(), equalTo(1));

        CalendarExceptionTypeDTO calendarExceptionTypeDTO = list.calendarExceptionTypes
                .get(0);
        assertThat(calendarExceptionTypeDTO.code, equalTo(calendarExceptionType
                .getCode()));
        assertThat(calendarExceptionTypeDTO.name, equalTo(calendarExceptionType
                .getName()));
        assertThat(calendarExceptionTypeDTO.color,
                equalTo(calendarExceptionType.getColor()));
        assertThat(calendarExceptionTypeDTO.overAssignable,
                equalTo(calendarExceptionType.isOverAssignableWithoutLimit()));
    }

}
