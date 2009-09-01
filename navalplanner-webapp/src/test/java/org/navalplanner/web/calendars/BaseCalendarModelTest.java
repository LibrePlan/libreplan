package org.navalplanner.web.calendars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
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
        baseCalendar.setName("Test");
        setHours(baseCalendar, 8);
        try {
            baseCalendarModel.confirmSave();

            assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(1));
            assertThat(baseCalendarModel.getBaseCalendars().get(0).getId(),
                    equalTo(baseCalendar.getId()));
            assertThat(baseCalendarModel.getBaseCalendars().get(0).getHours(
                    new Date(), Days.MONDAY), equalTo(8));
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
    public void testEditAndSave() throws ValidationException {
        assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(0));
        saveOneCalendar();

        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendars().get(0);
        baseCalendarModel.initEdit(baseCalendar);
        setHours(baseCalendarModel.getBaseCalendar(), 4);

        baseCalendarModel.confirmSave();

        assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(1));
        assertThat(baseCalendarModel.getBaseCalendars().get(0).getId(),
                equalTo(baseCalendar.getId()));
        assertThat(baseCalendarModel.getBaseCalendars().get(0).getHours(
                new Date(), Days.MONDAY), equalTo(4));
    }

    @Test
    public void testEditAndNewVersion() {
        assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(0));
        saveOneCalendar();

        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendars().get(0);
        baseCalendarModel.initEdit(baseCalendar);
        Date date = (new LocalDate()).plusWeeks(1)
                .toDateTimeAtStartOfDay().toDate();
        baseCalendarModel.createNewVersion(date);
        setHours(baseCalendarModel.getBaseCalendar(), 4);
        try {
            baseCalendarModel.confirmSave();

            assertThat(baseCalendarModel.getBaseCalendars().size(), equalTo(1));
            assertThat(baseCalendarModel.getBaseCalendars().get(0).getHours(
                    date, Days.MONDAY), equalTo(4));
            assertThat(baseCalendarModel.getBaseCalendars().get(0)
                    .getCalendarDataVersions().size(), equalTo(2));
        } catch (ValidationException e) {
            fail("It should not throw an exception");
        }
    }

    private void saveOneCalendar() {
        baseCalendarModel.initCreate();
        baseCalendarModel.getBaseCalendar().setName("Test");
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

    @Test
    public void testPossibleParentCalendars() throws ValidationException {
        baseCalendarModel.initCreate();
        baseCalendarModel.getBaseCalendar().setName("Test");
        setHours(baseCalendarModel.getBaseCalendar(), 8);
        BaseCalendar parent = baseCalendarModel.getBaseCalendar();
        baseCalendarModel.createNewVersion((new LocalDate()).plusMonths(1)
                .toDateTimeAtStartOfDay().toDate());
        BaseCalendar parentNewVersion = baseCalendarModel.getBaseCalendar();
        baseCalendarModel.confirmSave();

        baseCalendarModel.initCreateDerived(parent);
        BaseCalendar child = baseCalendarModel.getBaseCalendar();
        baseCalendarModel.getBaseCalendar().setName("Derived");
        baseCalendarModel.confirmSave();

        baseCalendarModel.initEdit(child);
        List<BaseCalendar> possibleParentCalendars = baseCalendarModel
                .getPossibleParentCalendars();

        assertThat(possibleParentCalendars.size(), equalTo(1));
        assertThat(possibleParentCalendars.get(0).getId(),
                equalTo(parentNewVersion.getId()));
        assertThat(
                possibleParentCalendars.get(0).getCalendarDataVersions()
                .size(), equalTo(2));
    }

}
