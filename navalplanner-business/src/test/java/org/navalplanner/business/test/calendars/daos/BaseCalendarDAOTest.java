package org.navalplanner.business.test.calendars.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;

import org.hibernate.SessionFactory;
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

        BaseCalendar nextCalendar = calendar.newVersion();
        baseCalendarDAO.save(nextCalendar);

        try {

            BaseCalendar savedCalendar = baseCalendarDAO.find(calendar.getId());
            assertThat(savedCalendar.getPreviousCalendar(), nullValue());
            assertThat(savedCalendar.getNextCalendar(), notNullValue());
            assertThat(savedCalendar.getNextCalendar(), equalTo(nextCalendar));

            BaseCalendar savedNextCalendar = baseCalendarDAO
                    .find(nextCalendar
                    .getId());
            assertThat(savedNextCalendar.getPreviousCalendar(), notNullValue());
            assertThat(savedNextCalendar.getNextCalendar(), nullValue());
            assertThat(savedNextCalendar.getPreviousCalendar(),
                    equalTo(calendar));

        } catch (InstanceNotFoundException e) {
            fail("It should not throw an exception");
        }

    }

    @Test(expected = DataIntegrityViolationException.class)
    public void notAllowSaveCalendarWithChildren()
            throws InstanceNotFoundException {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);
        BaseCalendar derivedCalendar = calendar.newDerivedCalendar();
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
        BaseCalendar newCalendar = calendar.newVersion();
        baseCalendarDAO.save(newCalendar);

        baseCalendarDAO.flush();
        session.getCurrentSession().evict(calendar);
        session.getCurrentSession().evict(newCalendar);

        baseCalendarDAO.remove(calendar.getId());
        baseCalendarDAO.flush();

        baseCalendarDAO.find(newCalendar.getId());
    }

    @Test
    public void findChildrens() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);
        BaseCalendar derivedCalendar = calendar.newDerivedCalendar();
        baseCalendarDAO.save(derivedCalendar);
        BaseCalendar derivedCalendar2 = calendar.newDerivedCalendar();
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

    @Test
    public void findLastVersions() {
        BaseCalendar calendar = BaseCalendarTest.createBasicCalendar();
        baseCalendarDAO.save(calendar);
        baseCalendarDAO.flush();

        assertThat(baseCalendarDAO.findLastVersions().size(), equalTo(1));

        BaseCalendar newCalendar = calendar.newVersion();
        baseCalendarDAO.save(newCalendar);
        baseCalendarDAO.flush();

        assertThat(baseCalendarDAO.findLastVersions().size(), equalTo(1));
    }

}
