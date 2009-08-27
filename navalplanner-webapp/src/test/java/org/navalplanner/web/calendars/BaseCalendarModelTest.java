package org.navalplanner.web.calendars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.BaseCalendar.Days;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link BaseCalendarModel}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE })
@Transactional
public class BaseCalendarModelTest {

    @Autowired
    private IBaseCalendarModel baseCalendarModel;

    @Test
    public void testCreateAndSave() {
        assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(0));
        baseCalendarModel.initCreate();
        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendar();
        setHours(baseCalendar, 8);
        try {
            baseCalendarModel.confirmSave();

            assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(1));
            assertThat(baseCalendarModel.getBaseCalendars().get(0).getId(),
                    equalTo(baseCalendar.getId()));
            assertThat(baseCalendarModel.getBaseCalendars().get(0).getHours(
                    Days.MONDAY), equalTo(8));
        } catch (ValidationException e) {
            fail("It should not throw an exception");
        }
    }

    private void setHours(BaseCalendar baseCalendar, Integer hours) {
        baseCalendar.setHours(Days.MONDAY, hours);
        baseCalendar.setHours(Days.TUESDAY, hours);
        baseCalendar.setHours(Days.WEDNESDAY, hours);
        baseCalendar.setHours(Days.THURSDAY, hours);
        baseCalendar.setHours(Days.FRIDAY, hours);
        baseCalendar.setHours(Days.SATURDAY, hours);
        baseCalendar.setHours(Days.SUNDAY, hours);
    }

    @Test
    public void testEditAndSave() {
        assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(0));
        saveOneCalendar();

        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendars().get(0);
        baseCalendarModel.initEdit(baseCalendar);
        setHours(baseCalendarModel.getBaseCalendar(), 4);
        try {
            baseCalendarModel.confirmSave();

            assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(1));
            assertThat(baseCalendarModel.getBaseCalendars().get(0).getId(),
                    equalTo(baseCalendar.getId()));
            assertThat(baseCalendarModel.getBaseCalendars().get(0).getHours(
                    Days.MONDAY), equalTo(4));
        } catch (ValidationException e) {
            fail("It should not throw an exception");
        }
    }

    @Test
    public void testEditAndNewVersion() {
        assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(0));
        saveOneCalendar();

        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendars().get(0);
        baseCalendarModel.initEdit(baseCalendar);
        baseCalendarModel.createNewVersion((new LocalDate()).plusWeeks(1)
                .toDateTimeAtStartOfDay().toDate());
        setHours(baseCalendarModel.getBaseCalendar(), 4);
        try {
            baseCalendarModel.confirmSave();

            assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(1));
            assertThat(baseCalendarModel.getBaseCalendars().get(0)
                    .getPreviousCalendar().getId(), equalTo(baseCalendar
                    .getId()));
            assertThat(baseCalendarModel.getBaseCalendars().get(0).getHours(
                    Days.MONDAY), equalTo(4));
        } catch (ValidationException e) {
            fail("It should not throw an exception");
        }
    }

    private void saveOneCalendar() {
        baseCalendarModel.initCreate();
        setHours(baseCalendarModel.getBaseCalendar(), 8);
        try {
            baseCalendarModel.confirmSave();
        } catch (ValidationException e) {
            fail("It should not throw an exception");
        }
    }

    @Test
    public void testRemove() {
        assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(0));
        saveOneCalendar();

        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendars().get(0);
        baseCalendarModel.initRemove(baseCalendar);
        baseCalendarModel.confirmRemove();
        assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(0));
    }

}
