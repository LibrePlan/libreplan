package org.navalplanner.business.test.calendars.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.validator.InvalidStateException;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.calendars.daos.BaseCalendarDAO;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.test.calendars.entities.BaseCalendarTest;
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
    private SessionFactory session;

    @Test
    public void saveBasicCalendar() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);
        assertTrue(baseCalendarDAO.exists(calendar.getId()));
    }

    @Test
    public void saveBasicCalendarWithExceptionDay() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        BaseCalendarTest.addChristmasAsExceptionDay(calendar);

        baseCalendarDAO.save(calendar);
        assertTrue(baseCalendarDAO.exists(calendar.getId()));

        try {
            BaseCalendar savedCalendar = baseCalendarDAO.find(calendar.getId());
            assertThat(savedCalendar.getExceptions().size(), equalTo(1));
        } catch (InstanceNotFoundException e) {
            fail("It should not throw an exception");
        }
    }

    @Test
    public void saveDerivedCalendar() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);

        BaseCalendar derivedCalendar = calendar.newDerivedCalendar();
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

    @Test(expected = InvalidStateException.class)
    public void notAllowTwoCalendarsWithNullName() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        calendar.setName(null);

        baseCalendarDAO.save(calendar);
        baseCalendarDAO.flush();
    }

    @Test(expected = InvalidStateException.class)
    public void notAllowTwoCalendarsWithEmptyName() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        calendar.setName("");

        baseCalendarDAO.save(calendar);
        baseCalendarDAO.flush();
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void notAllowTwoCalendarsWithTheSameName() {
        BaseCalendar calendar1 = BaseCalendarTest.createBasicCalendar();
        calendar1.setName("Test");
        BaseCalendar calendar2 = BaseCalendarTest.createBasicCalendar();
        calendar2.setName("Test");

        assertThat(calendar2.getName(), equalTo(calendar1.getName()));
        baseCalendarDAO.save(calendar1);
        baseCalendarDAO.save(calendar2);
        baseCalendarDAO.flush();
    }

    @Test
    public void notAllowTwoCalendarsWithTheSameNameChangingCalendarName()
            throws InstanceNotFoundException {
        BaseCalendar calendar1 = BaseCalendarTest.createBasicCalendar();
        calendar1.setName("Test");
        BaseCalendar calendar2 = BaseCalendarTest.createBasicCalendar();
        calendar2.setName("Test2");

        baseCalendarDAO.save(calendar1);
        baseCalendarDAO.save(calendar2);
        baseCalendarDAO.flush();

        calendar2 = baseCalendarDAO.find(calendar2.getId());
        calendar2.setName("Test");

        assertThat(calendar2.getName(), equalTo(calendar1.getName()));

        try {
            baseCalendarDAO.save(calendar2);
            baseCalendarDAO.flush();
            fail("It should throw an exception");
        } catch (DataIntegrityViolationException e) {

        }
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

}
