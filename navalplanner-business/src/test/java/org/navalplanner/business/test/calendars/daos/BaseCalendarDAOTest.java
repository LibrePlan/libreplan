package org.navalplanner.business.test.calendars.daos;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.calendars.daos.BaseCalendarDAO;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.test.calendars.entities.BaseCalendarTest;
import org.springframework.beans.factory.annotation.Autowired;
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
    IBaseCalendarDAO baseCalendarDAO;

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

}
