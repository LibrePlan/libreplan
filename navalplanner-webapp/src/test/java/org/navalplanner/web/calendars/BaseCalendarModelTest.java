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

package org.navalplanner.web.calendars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.workingday.EffortDuration.hours;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.business.calendars.entities.Capacity;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workingday.EffortDuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link BaseCalendarModel}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class BaseCalendarModelTest {

    @Autowired
    @Qualifier("main")
    private IBaseCalendarModel baseCalendarModel;

    @Test
    public void testCreateAndSave() {
        int previous = baseCalendarModel.getBaseCalendars().size();
        baseCalendarModel.initCreate();
        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendar();
        baseCalendar.setName("Test");
        setHours(baseCalendar, 8);
        baseCalendarModel.confirmSave();
        assertThat(baseCalendarModel.getBaseCalendars().size(),
                equalTo(previous + 1));
        assertThat(baseCalendarModel.getBaseCalendars().get(previous).getId(),
                equalTo(baseCalendar.getId()));
        assertThat(baseCalendarModel.getBaseCalendars().get(previous)
                .getDurationAt(new LocalDate(), Days.MONDAY), equalTo(hours(8)));
    }

    private void setHours(BaseCalendar baseCalendar, Integer hours) {
        EffortDuration hoursDuration = EffortDuration.hours(hours);
        for (Days each : Days.values()) {
            baseCalendar.setCapacityAt(each, Capacity.create(hoursDuration)
                    .overAssignableWithoutLimit(true));
        }
    }

    @Test
    public void testEditAndSave() throws ValidationException {
        int previous = baseCalendarModel.getBaseCalendars().size();
        saveOneCalendar();

        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendars().get(
                previous);
        baseCalendarModel.initEdit(baseCalendar);
        setHours(baseCalendarModel.getBaseCalendar(), 4);

        baseCalendarModel.confirmSave();

        assertThat(baseCalendarModel.getBaseCalendars().size(),
                equalTo(previous + 1));
        assertThat(baseCalendarModel.getBaseCalendars().get(previous).getId(),
                equalTo(baseCalendar.getId()));
        assertThat(baseCalendarModel.getBaseCalendars().get(previous)
                .getDurationAt(new LocalDate(), Days.MONDAY), equalTo(hours(4)));
    }

    @Test
    public void testEditAndNewVersion() {
        int previous = baseCalendarModel.getBaseCalendars().size();
        saveOneCalendar();

        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendars().get(
                previous);
        baseCalendarModel.initEdit(baseCalendar);
        LocalDate date = new LocalDate().plusWeeks(1);
        baseCalendarModel.createNewVersion(date);
        setHours(baseCalendarModel.getBaseCalendar(), 4);
        baseCalendarModel.confirmSave();

        assertThat(baseCalendarModel.getBaseCalendars().size(),
                equalTo(previous + 1));
        assertThat(baseCalendarModel.getBaseCalendars().get(previous)
                .getDurationAt(date, Days.MONDAY), equalTo(hours(4)));
        assertThat(baseCalendarModel.getBaseCalendars().get(previous)
                .getCalendarDataVersions().size(), equalTo(2));
    }

    private void saveOneCalendar() {
        baseCalendarModel.initCreate();
        baseCalendarModel.getBaseCalendar().setName("Test");
        setHours(baseCalendarModel.getBaseCalendar(), 8);
        baseCalendarModel.confirmSave();
    }

    @Test
    public void testRemove() {
        int previous = baseCalendarModel.getBaseCalendars().size();
        saveOneCalendar();

        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendars().get(
                previous);
        baseCalendarModel.initRemove(baseCalendar);
        baseCalendarModel.confirmRemove(baseCalendar);
        assertThat(baseCalendarModel.getBaseCalendars().size(),
                equalTo(previous));
    }

    @Test
    public void testPossibleParentCalendars() throws ValidationException {
        int previous = baseCalendarModel.getPossibleParentCalendars().size();

        baseCalendarModel.initCreate();
        baseCalendarModel.getBaseCalendar().setName("Test");
        setHours(baseCalendarModel.getBaseCalendar(), 8);
        BaseCalendar parent = baseCalendarModel.getBaseCalendar();
        baseCalendarModel.createNewVersion(new LocalDate().plusMonths(1));
        BaseCalendar parentNewVersion = baseCalendarModel.getBaseCalendar();
        baseCalendarModel.confirmSave();

        baseCalendarModel.initCreateDerived(parent);
        BaseCalendar child = baseCalendarModel.getBaseCalendar();
        baseCalendarModel.getBaseCalendar().setName("Derived");
        baseCalendarModel.confirmSave();

        baseCalendarModel.initEdit(child);
        List<BaseCalendar> possibleParentCalendars = baseCalendarModel
                .getPossibleParentCalendars();

        assertThat(possibleParentCalendars.size(), equalTo(previous + 1));
        assertThat(possibleParentCalendars.get(previous).getId(),
                equalTo(parentNewVersion.getId()));
        assertThat(
                possibleParentCalendars.get(previous)
                .getCalendarDataVersions()
                .size(), equalTo(2));
    }

}
