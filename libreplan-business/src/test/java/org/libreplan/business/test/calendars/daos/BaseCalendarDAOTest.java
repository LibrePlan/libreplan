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

package org.libreplan.business.test.calendars.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.calendars.daos.BaseCalendarDAO;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.CalendarException;
import org.libreplan.business.calendars.entities.CalendarExceptionType;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.test.calendars.entities.BaseCalendarTest;
import org.libreplan.business.test.resources.daos.ResourceDAOTest;
import org.libreplan.business.workingday.EffortDuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link BaseCalendarDAO}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class BaseCalendarDAOTest {

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private ICalendarExceptionTypeDAO calendarExceptionTypeDAO;

    @Autowired
    private SessionFactory session;

    @Autowired
    private IResourceDAO resourceDAO;

    @Resource
    private IDataBootstrap configurationBootstrap;

    @Resource
    private IDataBootstrap calendarBootstrap;

    @Before
    public void loadRequiredData() {
        configurationBootstrap.loadRequiredData();
        calendarBootstrap.loadRequiredData();
    }

    @Test
    public void saveBasicCalendar() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);
        assertTrue(baseCalendarDAO.exists(calendar.getId()));
    }

    @Test
    public void saveBasicCalendarWithExceptionDay() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        addChristmasAsExceptionDay(calendar);

        baseCalendarDAO.save(calendar);
        assertTrue(baseCalendarDAO.exists(calendar.getId()));

        try {
            BaseCalendar savedCalendar = baseCalendarDAO.find(calendar.getId());
            assertThat(savedCalendar.getExceptions().size(), equalTo(1));
        } catch (InstanceNotFoundException e) {
            fail("It should not throw an exception");
        }
    }

    private void addChristmasAsExceptionDay(BaseCalendar calendar) {
        CalendarExceptionType type = calendarExceptionTypeDAO.list(
                CalendarExceptionType.class).get(0);
        CalendarException christmasDay = CalendarException.create(
                BaseCalendarTest.CHRISTMAS_DAY_LOCAL_DATE,
                EffortDuration.zero(), type);
        calendar.addExceptionDay(christmasDay);
    }

    @Test
    public void saveDerivedCalendar() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);

        BaseCalendar derivedCalendar = calendar.newDerivedCalendar();
        derivedCalendar.setName("derived");
        baseCalendarDAO.save(derivedCalendar);

        try {

            BaseCalendar savedCalendar = baseCalendarDAO.find(calendar.getId());
            assertFalse(savedCalendar.isDerived());

            BaseCalendar savedDerivedCalendar = baseCalendarDAO
                    .find(derivedCalendar.getId());
            assertTrue(savedDerivedCalendar.isDerived());

        } catch (InstanceNotFoundException e) {
            fail("It should not throw an exception");
        }

    }

    @Test
    public void saveNextCalendar() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);

        calendar.newVersion((new LocalDate()).plusDays(1));
        baseCalendarDAO.save(calendar);

        try {

            BaseCalendar savedCalendar = baseCalendarDAO.find(calendar.getId());
            assertThat(savedCalendar.getCalendarDataVersions().size(),
                    equalTo(2));

        } catch (InstanceNotFoundException e) {
            fail("It should not throw an exception");
        }
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void notAllowRemoveCalendarWithChildren()
            throws InstanceNotFoundException {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);
        BaseCalendar derivedCalendar = calendar.newDerivedCalendar();
        derivedCalendar.setName("Derived from " + calendar.getName());
        baseCalendarDAO.save(derivedCalendar);

        baseCalendarDAO.flush();
        session.getCurrentSession().evict(calendar);
        session.getCurrentSession().evict(derivedCalendar);

        baseCalendarDAO.remove(calendar.getId());
        baseCalendarDAO.flush();
    }

    @Test(expected = InstanceNotFoundException.class)
    public void removeVersions() throws InstanceNotFoundException {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);
        calendar.newVersion((new LocalDate()).plusDays(1));
        baseCalendarDAO.save(calendar);

        baseCalendarDAO.flush();
        session.getCurrentSession().evict(calendar);

        baseCalendarDAO.remove(calendar.getId());
        baseCalendarDAO.flush();

        baseCalendarDAO.find(calendar.getId());
    }

    @Test
    public void findChildrens() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);
        BaseCalendar derivedCalendar = calendar.newDerivedCalendar();
        derivedCalendar.setName("derived from " + calendar.getName() + " 1");
        baseCalendarDAO.save(derivedCalendar);
        BaseCalendar derivedCalendar2 = calendar.newDerivedCalendar();
        derivedCalendar2.setName("derived from " + calendar.getName() + " 2");
        baseCalendarDAO.save(derivedCalendar2);

        baseCalendarDAO.flush();
        session.getCurrentSession().evict(calendar);
        session.getCurrentSession().evict(derivedCalendar);
        session.getCurrentSession().evict(derivedCalendar2);

        calendar.dontPoseAsTransientObjectAnymore();
        derivedCalendar.dontPoseAsTransientObjectAnymore();
        derivedCalendar2.dontPoseAsTransientObjectAnymore();

        List<BaseCalendar> children = baseCalendarDAO.findByParent(calendar);
        assertThat(children.size(), equalTo(2));
        assertTrue(children.get(0).getId().equals(derivedCalendar.getId())
                || children.get(0).getId().equals(derivedCalendar2.getId()));

        children = baseCalendarDAO.findByParent(derivedCalendar);
        assertThat(children.size(), equalTo(0));

        children = baseCalendarDAO.findByParent(derivedCalendar2);
        assertThat(children.size(), equalTo(0));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void notAllowRemoveCalendarWithChildrenInOtherVersions()
            throws InstanceNotFoundException {
        BaseCalendar parent1 = BaseCalendarTest.createBasicCalendar();
        BaseCalendar parent2 = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(parent1);
        baseCalendarDAO.save(parent2);

        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        calendar.setParent(parent1);

        baseCalendarDAO.save(calendar);
        baseCalendarDAO.flush();

        assertThat(baseCalendarDAO.findByParent(parent1).get(0).getId(),
                equalTo(calendar.getId()));

        calendar.newVersion((new LocalDate())
                .plusDays(1));
        calendar.setParent(parent2);

        baseCalendarDAO.save(calendar);
        baseCalendarDAO.flush();

        assertThat(baseCalendarDAO.findByParent(parent2).get(0).getId(),
                equalTo(calendar.getId()));

        assertThat(baseCalendarDAO.findByParent(parent1).get(0).getId(),
                equalTo(calendar.getId()));

        baseCalendarDAO.remove(parent1.getId());
        baseCalendarDAO.flush();
    }

    @Test(expected = ValidationException.class)
    public void notAllowTwoCalendarsWithNullName() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        calendar.setName(null);

        baseCalendarDAO.save(calendar);
    }

    @Test(expected = ValidationException.class)
    public void notAllowTwoCalendarsWithEmptyName() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        calendar.setName("");

        baseCalendarDAO.save(calendar);
    }

    @Test
    public void findByName() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);
        baseCalendarDAO.flush();

        List<BaseCalendar> list = baseCalendarDAO.findByName(calendar);
        assertThat(list.size(), equalTo(1));
        assertThat(list.get(0).getId(), equalTo(calendar.getId()));
    }

    @Test
    public void getBaseCalendarsAndNotResourceCalendars() {
        List<BaseCalendar> baseCalendars = baseCalendarDAO.getBaseCalendars();
        int previous = baseCalendars.size();

        BaseCalendar calendar1 = BaseCalendarTest.createBasicCalendar();
        calendar1.setName("Test1");
        BaseCalendar calendar2 = BaseCalendarTest.createBasicCalendar();
        calendar1.setName("Test2");

        Worker worker = ResourceDAOTest.givenValidWorker();
        ResourceCalendar resourceCalendar = ResourceCalendar.create();
        resourceCalendar.setName("testResourceCalendar");
        BaseCalendarTest.setHoursForAllDays(resourceCalendar, 8);
        worker.setCalendar(resourceCalendar);

        baseCalendarDAO.save(calendar1);
        baseCalendarDAO.save(calendar2);
        resourceDAO.save(worker);
        baseCalendarDAO.flush();
        resourceDAO.flush();

        baseCalendars = baseCalendarDAO.getBaseCalendars();
        assertThat(baseCalendars.size(), equalTo(previous + 2));
    }

    @Test(expected = ValidationException.class)
    public void doNotAllowToSaveCalendarWithZeroHours() {
        BaseCalendar calendar = BaseCalendar.create("calendar-"
                + UUID.randomUUID());
        calendar.setName("calendar-name-" + UUID.randomUUID());
        baseCalendarDAO.save(calendar);
    }

    @Test
    public void testSaveAndRemoveCalendar() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);
        try {
            baseCalendarDAO.remove(calendar.getId());
        } catch (InstanceNotFoundException e) {

        }
        assertTrue(!baseCalendarDAO.exists(calendar.getId()));
    }

    @Test
    public void testSaveAndRemoveResourceCalendar() {
        Worker worker = ResourceDAOTest.givenValidWorker();
        ResourceCalendar resourceCalendar = ResourceCalendar.create();
        addChristmasAsExceptionDay(resourceCalendar);
        resourceCalendar.setName("testResourceCalendar");
        BaseCalendarTest.setHoursForAllDays(resourceCalendar, 8);
        worker.setCalendar(resourceCalendar);

        // Resource calendar was saved whe worker was saved
        resourceDAO.save(worker);
        resourceCalendar = worker.getCalendar();
        assertTrue(resourceCalendar.getId() != null);

        // Unset calendar from resource and save should remove calendar
        try {
            baseCalendarDAO.remove(resourceCalendar.getId());
            worker.setCalendar(null);
            resourceDAO.save(worker);
        } catch (InstanceNotFoundException e) {

        }
        assertTrue(!baseCalendarDAO.exists(resourceCalendar.getId()));
    }

}
