package org.navalplanner.business.test.calendars.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ExceptionDay;
import org.navalplanner.business.calendars.entities.BaseCalendar.DayType;
import org.navalplanner.business.calendars.entities.BaseCalendar.Days;
import org.navalplanner.business.common.exceptions.ValidationException;

/**
 * Tests for {@link BaseCalendar}.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendarTest {

    public static final LocalDate MONDAY_LOCAL_DATE = new LocalDate(2009, 8, 10);
    public static final LocalDate TUESDAY_LOCAL_DATE = new LocalDate(2009, 8,
            11);
    public static final LocalDate WEDNESDAY_LOCAL_DATE = new LocalDate(2009, 8,
            12);
    public static final LocalDate THURSDAY_LOCAL_DATE = new LocalDate(2009, 8,
            13);
    public static final LocalDate FRIDAY_LOCAL_DATE = new LocalDate(2009, 8, 14);
    public static final LocalDate SATURDAY_LOCAL_DATE = new LocalDate(2009, 8,
            15);
    public static final LocalDate SUNDAY_LOCAL_DATE = new LocalDate(2009, 8, 16);

    private static final LocalDate[] DAYS_OF_A_WEEK_EXAMPLE = {
            MONDAY_LOCAL_DATE, TUESDAY_LOCAL_DATE, WEDNESDAY_LOCAL_DATE,
            THURSDAY_LOCAL_DATE, FRIDAY_LOCAL_DATE, SATURDAY_LOCAL_DATE,
            SUNDAY_LOCAL_DATE };

    public static final LocalDate CHRISTMAS_DAY_LOCAL_DATE = new LocalDate(
            2009, 12, 25);

    public static BaseCalendar createBasicCalendar() {
        BaseCalendar calendar = BaseCalendar.create();

        calendar.setName("Test");

        calendar.setHours(Days.MONDAY, 8);
        calendar.setHours(Days.TUESDAY, 8);
        calendar.setHours(Days.WEDNESDAY, 8);
        calendar.setHours(Days.THURSDAY, 8);
        calendar.setHours(Days.FRIDAY, 8);
        calendar.setHours(Days.SATURDAY, 0);
        calendar.setHours(Days.SUNDAY, 0);

        return calendar;
    }

    private BaseCalendar calendarFixture;

    private void givenUnitializedCalendar() {
        calendarFixture = BaseCalendar.create();
    }

    public static void addChristmasAsExceptionDay(BaseCalendar calendar) {
        ExceptionDay christmasDay = ExceptionDay.create(
                CHRISTMAS_DAY_LOCAL_DATE, 0);

        calendar.addExceptionDay(christmasDay);
    }

    public static BaseCalendar createChristmasCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        addChristmasAsExceptionDay(calendar);
        return calendar;
    }

    @Test
    public void testValidCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        try {
            calendar.checkValid();
        } catch (ValidationException e) {
            fail("It should not throw an exception");
        }
    }

    @Test
    public void testGetWorkableHoursBasic() {
        BaseCalendar calendar = createBasicCalendar();

        int wednesdayHours = calendar.getWorkableHours(WEDNESDAY_LOCAL_DATE);
        assertThat(wednesdayHours, equalTo(8));

        int sundayHours = calendar.getWorkableHours(SUNDAY_LOCAL_DATE);
        assertThat(sundayHours, equalTo(0));
    }

    @Test
    public void testGetWorkableHoursChristmas() {
        BaseCalendar calendar = createChristmasCalendar();

        int hours = calendar.getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE);
        assertThat(hours, equalTo(0));
    }

    @Test
    public void testDeriveCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        BaseCalendar derivedCalendar = calendar.newDerivedCalendar();

        assertThat(derivedCalendar.getParent(), equalTo(calendar));
    }

    @Test
    public void testGetWorkableHoursDerivedBasicCalendar() {
        BaseCalendar calendar = createBasicCalendar().newDerivedCalendar();

        int wednesdayHours = calendar.getWorkableHours(WEDNESDAY_LOCAL_DATE);
        assertThat(wednesdayHours, equalTo(8));

        int sundayHours = calendar.getWorkableHours(SUNDAY_LOCAL_DATE);
        assertThat(sundayHours, equalTo(0));
    }

    @Test
    public void testGetWorkableHoursDerivedChristmasCalendar() {
        BaseCalendar calendar = createChristmasCalendar().newDerivedCalendar();

        int hours = calendar.getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE);
        assertThat(hours, equalTo(0));
    }

    @Test
    public void testGetWorkableHoursDerivedBasicCalendarWithException() {
        BaseCalendar calendar = createBasicCalendar().newDerivedCalendar();

        ExceptionDay day = ExceptionDay.create(WEDNESDAY_LOCAL_DATE, 4);
        calendar.addExceptionDay(day);

        int mondayHours = calendar.getWorkableHours(MONDAY_LOCAL_DATE);
        assertThat(mondayHours, equalTo(8));

        int wednesdayHours = calendar.getWorkableHours(WEDNESDAY_LOCAL_DATE);
        assertThat(wednesdayHours, equalTo(4));

        int sundayHours = calendar.getWorkableHours(SUNDAY_LOCAL_DATE);
        assertThat(sundayHours, equalTo(0));
    }

    @Test
    public void testGetWorkableHoursDerivedChristmasCalendarRedefiningExceptionDay() {
        BaseCalendar calendar = createChristmasCalendar().newDerivedCalendar();

        ExceptionDay day = ExceptionDay.create(CHRISTMAS_DAY_LOCAL_DATE, 4);
        calendar.addExceptionDay(day);

        int hours = calendar.getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE);
        assertThat(hours, equalTo(4));
    }

    @Test
    public void testGettWorkableHoursInterval() {
        BaseCalendar calendar = createBasicCalendar();

        int mondayToWednesdayHours = calendar.getWorkableHours(
                MONDAY_LOCAL_DATE, WEDNESDAY_LOCAL_DATE);
        assertThat(mondayToWednesdayHours, equalTo(24));
    }

    @Test
    public void testGettWorkableHoursPerWeek() {
        BaseCalendar calendar = createBasicCalendar();

        int weekHours = calendar.getWorkableHoursPerWeek(WEDNESDAY_LOCAL_DATE);
        assertThat(weekHours, equalTo(40));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTwoExceptionDaysInTheSameDate() {
        BaseCalendar calendar = createBasicCalendar();

        ExceptionDay day = ExceptionDay.create(MONDAY_LOCAL_DATE, 8);
        calendar.addExceptionDay(day);

        ExceptionDay day2 = ExceptionDay.create(MONDAY_LOCAL_DATE, 4);
        calendar.addExceptionDay(day2);
    }

    @Test
    public void testCreateNewVersion() {
        BaseCalendar calendar = createBasicCalendar();
        BaseCalendar nextCalendar = calendar.newVersion();

        assertThat(calendar, equalTo(nextCalendar.getPreviousCalendar()));
        assertThat(nextCalendar, equalTo(calendar.getNextCalendar()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInvalidNewVersion() {
        BaseCalendar nextCalendar = createBasicCalendar().newVersion(
                FRIDAY_LOCAL_DATE);

        nextCalendar.newVersion(MONDAY_LOCAL_DATE);
    }

    @Test
    public void testGettWorkableHoursNewVersion() {
        BaseCalendar origCalendar = createBasicCalendar();
        BaseCalendar newCalendar = origCalendar.newVersion(MONDAY_LOCAL_DATE);

        newCalendar.setHours(Days.WEDNESDAY, 4);
        newCalendar.setHours(Days.SUNDAY, 4);

        int wednesdayHours = newCalendar.getWorkableHours(WEDNESDAY_LOCAL_DATE);
        assertThat(wednesdayHours, equalTo(4));
        assertThat(wednesdayHours, equalTo(origCalendar
                .getWorkableHours(WEDNESDAY_LOCAL_DATE)));

        int wednesdayHoursPastWeek = newCalendar
                .getWorkableHours(WEDNESDAY_LOCAL_DATE.minusWeeks(1));
        assertThat(wednesdayHoursPastWeek, equalTo(8));
        assertThat(wednesdayHoursPastWeek, equalTo(origCalendar
                .getWorkableHours(WEDNESDAY_LOCAL_DATE.minusWeeks(1))));

        int sundayHours = newCalendar.getWorkableHours(SUNDAY_LOCAL_DATE);
        assertThat(sundayHours, equalTo(4));
        assertThat(sundayHours, equalTo(origCalendar
                .getWorkableHours(SUNDAY_LOCAL_DATE)));

        int sundayHoursPastWeek = newCalendar
                .getWorkableHours(SUNDAY_LOCAL_DATE.minusWeeks(1));
        assertThat(sundayHoursPastWeek, equalTo(0));
        assertThat(sundayHoursPastWeek, equalTo(origCalendar
                .getWorkableHours(SUNDAY_LOCAL_DATE.minusWeeks(1))));
    }

    @Test
    public void testGettWorkableHoursNewVersionCheckingLimits() {
        BaseCalendar origCalendar = createBasicCalendar();
        BaseCalendar newCalendar = origCalendar.newVersion(MONDAY_LOCAL_DATE);

        newCalendar.setHours(Days.MONDAY, 1);
        newCalendar.setHours(Days.SUNDAY, 2);

        int mondayHours = newCalendar.getWorkableHours(MONDAY_LOCAL_DATE);
        assertThat(mondayHours, equalTo(1));
        assertThat(mondayHours, equalTo(origCalendar
                .getWorkableHours(MONDAY_LOCAL_DATE)));

        int sundayHours = newCalendar.getWorkableHours(SUNDAY_LOCAL_DATE);
        assertThat(sundayHours, equalTo(2));
        assertThat(sundayHours, equalTo(origCalendar
                .getWorkableHours(SUNDAY_LOCAL_DATE)));

        int mondayHoursPastWeek = newCalendar
                .getWorkableHours(MONDAY_LOCAL_DATE.minusWeeks(1));
        assertThat(mondayHoursPastWeek, equalTo(8));
        assertThat(mondayHoursPastWeek, equalTo(origCalendar
                .getWorkableHours(MONDAY_LOCAL_DATE.minusWeeks(1))));

        int pastSundayHours = newCalendar.getWorkableHours(MONDAY_LOCAL_DATE
                .minusDays(1));
        assertThat(pastSundayHours, equalTo(0));
        assertThat(pastSundayHours, equalTo(origCalendar
                .getWorkableHours(MONDAY_LOCAL_DATE.minusDays(1))));
    }

    @Test
    public void testRemoveExceptionDay() {
        BaseCalendar calendar = createChristmasCalendar();

        calendar.removeExceptionDay(CHRISTMAS_DAY_LOCAL_DATE);

        assertThat(calendar.getExceptions().size(), equalTo(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveExceptionDayDerivedCalendar() {
        BaseCalendar calendar = createChristmasCalendar().newDerivedCalendar();

        calendar.removeExceptionDay(CHRISTMAS_DAY_LOCAL_DATE);
    }

    @Test
    public void testRemoveExceptionDayNewVersionCalendar() {
        BaseCalendar origCalendar = createChristmasCalendar();
        BaseCalendar newCalendar = origCalendar.newVersion(MONDAY_LOCAL_DATE);

        newCalendar.removeExceptionDay(CHRISTMAS_DAY_LOCAL_DATE);

        assertThat(origCalendar.getExceptions().size(), equalTo(1));
        assertThat(newCalendar.getExceptions().size(), equalTo(0));
    }

    @Test
    public void testGettWorkableHoursNewVersionFromChristmasCalendar() {
        BaseCalendar origCalendar = createChristmasCalendar();
        ExceptionDay day = ExceptionDay.create(CHRISTMAS_DAY_LOCAL_DATE
                .plusYears(1), 0);
        origCalendar.addExceptionDay(day);

        BaseCalendar newCalendar = origCalendar
                .newVersion(CHRISTMAS_DAY_LOCAL_DATE.plusDays(1));

        newCalendar
                .updateExceptionDay(CHRISTMAS_DAY_LOCAL_DATE.plusYears(1), 8);

        int christmasHours = newCalendar
                .getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE.plusYears(1));
        assertThat(christmasHours, equalTo(8));

        int christmasHoursPastYear = newCalendar
                .getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE);
        assertThat(christmasHoursPastYear, equalTo(0));
    }

    public static void setHoursForAllDays(BaseCalendar calendar, Integer hours) {
        calendar.setHours(Days.MONDAY, hours);
        calendar.setHours(Days.TUESDAY, hours);
        calendar.setHours(Days.WEDNESDAY, hours);
        calendar.setHours(Days.THURSDAY, hours);
        calendar.setHours(Days.FRIDAY, hours);
        calendar.setHours(Days.SATURDAY, hours);
        calendar.setHours(Days.SUNDAY, hours);
    }

    @Test
    public void testGettWorkableHoursTwoNewVersions() {
        BaseCalendar calendar = createBasicCalendar();
        setHoursForAllDays(calendar, 8);

        BaseCalendar calendar2 = calendar.newVersion(TUESDAY_LOCAL_DATE);
        setHoursForAllDays(calendar2, 4);

        BaseCalendar calendar3 = calendar2.newVersion(FRIDAY_LOCAL_DATE);
        setHoursForAllDays(calendar3, 2);

        int hoursMonday = calendar.getWorkableHours(MONDAY_LOCAL_DATE);
        assertThat(hoursMonday, equalTo(8));
        assertThat(calendar2.getWorkableHours(MONDAY_LOCAL_DATE),
                equalTo(hoursMonday));
        assertThat(calendar3.getWorkableHours(MONDAY_LOCAL_DATE),
                equalTo(hoursMonday));

        int hoursWednesday = calendar.getWorkableHours(WEDNESDAY_LOCAL_DATE);
        assertThat(hoursWednesday, equalTo(4));
        assertThat(calendar2.getWorkableHours(WEDNESDAY_LOCAL_DATE),
                equalTo(hoursWednesday));
        assertThat(calendar3.getWorkableHours(WEDNESDAY_LOCAL_DATE),
                equalTo(hoursWednesday));

        int hoursFriday = calendar.getWorkableHours(FRIDAY_LOCAL_DATE);
        assertThat(hoursFriday, equalTo(2));
        assertThat(calendar2.getWorkableHours(FRIDAY_LOCAL_DATE),
                equalTo(hoursFriday));
        assertThat(calendar3.getWorkableHours(FRIDAY_LOCAL_DATE),
                equalTo(hoursFriday));

    }

    @Test
    public void testGettWorkableHoursDeriveAndNewVersion() {
        BaseCalendar baseCalendar = createChristmasCalendar();

        BaseCalendar calendar = baseCalendar.newDerivedCalendar();
        setHoursForAllDays(calendar, 4);

        BaseCalendar newCalendar = calendar.newVersion(WEDNESDAY_LOCAL_DATE);
        setHoursForAllDays(newCalendar, 2);

        int hoursMonday = baseCalendar.getWorkableHours(MONDAY_LOCAL_DATE);
        assertThat(hoursMonday, equalTo(8));

        hoursMonday = calendar.getWorkableHours(MONDAY_LOCAL_DATE);
        assertThat(hoursMonday, equalTo(4));
        assertThat(hoursMonday, equalTo(newCalendar
                .getWorkableHours(MONDAY_LOCAL_DATE)));

        int hoursFriday = baseCalendar.getWorkableHours(FRIDAY_LOCAL_DATE);
        assertThat(hoursFriday, equalTo(8));

        hoursFriday = calendar.getWorkableHours(FRIDAY_LOCAL_DATE);
        assertThat(hoursFriday, equalTo(2));
        assertThat(hoursFriday, equalTo(newCalendar
                .getWorkableHours(FRIDAY_LOCAL_DATE)));

        int christmasHours = newCalendar
                .getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE);
        assertThat(christmasHours, equalTo(0));
        assertThat(christmasHours, equalTo(calendar
                .getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE)));
    }

    @Test
    public void testAddExceptionToNewVersionCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        BaseCalendar newVersion = calendar.newVersion(CHRISTMAS_DAY_LOCAL_DATE
                .plusDays(1));

        ExceptionDay day = ExceptionDay.create(CHRISTMAS_DAY_LOCAL_DATE, 0);
        newVersion.addExceptionDay(day);

        assertThat(calendar.getExceptions().size(), equalTo(1));
        assertThat(newVersion.getExceptions().size(), equalTo(0));
        assertThat(calendar.getExceptions().iterator().next().getDate(),
                equalTo(CHRISTMAS_DAY_LOCAL_DATE));
    }

    @Test
    public void anUnitializedCalendarShouldReturnZeroHours() {
        givenUnitializedCalendar();
        thenForAllDaysReturnsZero();
    }

    private void thenForAllDaysReturnsZero() {
        for (LocalDate localDate : DAYS_OF_A_WEEK_EXAMPLE) {
            assertThat(calendarFixture.getWorkableHours(localDate), equalTo(0));
        }
    }

    @Test
    public void anUnitializedCalendarShouldHaveDefaultValues() {
        givenUnitializedCalendar();
        thenForAllDaysValueByDefault();
    }

    private void thenForAllDaysValueByDefault() {
        assertTrue(calendarFixture.isDefault(Days.MONDAY));
        assertTrue(calendarFixture.isDefault(Days.TUESDAY));
        assertTrue(calendarFixture.isDefault(Days.WEDNESDAY));
        assertTrue(calendarFixture.isDefault(Days.THURSDAY));
        assertTrue(calendarFixture.isDefault(Days.FRIDAY));
        assertTrue(calendarFixture.isDefault(Days.SATURDAY));
        assertTrue(calendarFixture.isDefault(Days.SUNDAY));
    }

    @Test
    public void testDefaultValues() {
        BaseCalendar calendar = createBasicCalendar();

        assertFalse(calendar.isDefault(Days.MONDAY));

        calendar.setDefault(Days.MONDAY);
        assertTrue(calendar.isDefault(Days.MONDAY));
    }

    @Test
    public void testIsDerivedCalendar() {
        BaseCalendar calendar = BaseCalendar.create();
        BaseCalendar derivedCalendar = calendar.newDerivedCalendar();

        assertFalse(calendar.isDerived());
        assertTrue(derivedCalendar.isDerived());
    }

    @Test
    public void testGetExceptionDay() {
        BaseCalendar calendar = createChristmasCalendar();
        BaseCalendar derived = calendar.newDerivedCalendar();

        assertThat(calendar.getExceptionDay(CHRISTMAS_DAY_LOCAL_DATE),
                notNullValue());
        assertThat(derived.getExceptionDay(CHRISTMAS_DAY_LOCAL_DATE),
                notNullValue());

        assertThat(calendar.getOwnExceptionDay(CHRISTMAS_DAY_LOCAL_DATE),
                notNullValue());
        assertThat(derived.getOwnExceptionDay(CHRISTMAS_DAY_LOCAL_DATE),
                nullValue());
    }

    @Test
    public void testGetType() {
        BaseCalendar calendar = createChristmasCalendar();

        assertThat(calendar.getType(MONDAY_LOCAL_DATE), equalTo(DayType.NORMAL));
        assertThat(calendar.getType(SUNDAY_LOCAL_DATE),
                equalTo(DayType.ZERO_HOURS));
        assertThat(calendar.getType(CHRISTMAS_DAY_LOCAL_DATE),
                equalTo(DayType.OWN_EXCEPTION));
    }

    @Test
    public void testGetTypeDerivedCalendar() {
        BaseCalendar calendar = createChristmasCalendar();
        BaseCalendar derived = calendar.newDerivedCalendar();

        assertThat(derived.getType(MONDAY_LOCAL_DATE), equalTo(DayType.NORMAL));
        assertThat(derived.getType(SUNDAY_LOCAL_DATE), equalTo(DayType.ZERO_HOURS));
        assertThat(derived.getType(CHRISTMAS_DAY_LOCAL_DATE),
                equalTo(DayType.ANCESTOR_EXCEPTION));

        assertThat(calendar.getType(CHRISTMAS_DAY_LOCAL_DATE),
                equalTo(DayType.OWN_EXCEPTION));
    }

    @Test
    public void testSetParent() {
        BaseCalendar calendar = createBasicCalendar();
        BaseCalendar calendar2 = createBasicCalendar();
        BaseCalendar derived = calendar.newDerivedCalendar();

        derived.setParent(calendar2);

        assertThat(derived.getParent(), equalTo(calendar2));
    }

    @Test
    public void testSetParentInACalendarWithoutParent() {
        BaseCalendar calendar = createBasicCalendar();
        BaseCalendar parent = createChristmasCalendar();

        calendar.setParent(parent);

        assertThat(calendar.getParent(), equalTo(parent));
        assertThat(calendar.getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE),
                equalTo(0));
    }

    @Test
    public void testNewCopy() {
        BaseCalendar calendar = createChristmasCalendar();
        BaseCalendar derived = calendar.newDerivedCalendar();
        BaseCalendar copy = derived.newCopy();

        assertThat(copy.getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE), equalTo(0));
        assertThat(copy.getParent(), equalTo(calendar));
        assertThat(copy.getNextCalendar(), nullValue());
        assertThat(copy.getPreviousCalendar(), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHoursInvalid() {
        BaseCalendar calendar = createBasicCalendar();

        calendar.setHours(Days.MONDAY, -5);
    }

    @Test
    public void testGettWorkableHoursNewVersionChangeParent() {
        BaseCalendar parent1 = createBasicCalendar();
        setHoursForAllDays(parent1, 8);
        BaseCalendar parent2 = createBasicCalendar();
        setHoursForAllDays(parent2, 4);

        BaseCalendar calendar = parent1.newDerivedCalendar();

        BaseCalendar newVersion = calendar.newVersion(WEDNESDAY_LOCAL_DATE);
        newVersion.setParent(parent2);

        assertThat(newVersion.getParent(), equalTo(parent2));
        assertThat(newVersion.getPreviousCalendar().getParent(),
                equalTo(parent1));

        int mondayHours = newVersion.getWorkableHours(MONDAY_LOCAL_DATE);
        assertThat(mondayHours, equalTo(8));
        assertThat(calendar.getWorkableHours(MONDAY_LOCAL_DATE),
                equalTo(mondayHours));

        int fridayHours = newVersion.getWorkableHours(FRIDAY_LOCAL_DATE);
        assertThat(fridayHours, equalTo(4));
        assertThat(calendar.getWorkableHours(FRIDAY_LOCAL_DATE),
                equalTo(fridayHours));
    }

    @Test
    public void testExceptionsInDifferentVersions() {
        BaseCalendar calendar = createBasicCalendar();
        BaseCalendar newVersion = calendar.newVersion(WEDNESDAY_LOCAL_DATE);

        newVersion.addExceptionDay(ExceptionDay.create(MONDAY_LOCAL_DATE, 0));
        newVersion.addExceptionDay(ExceptionDay.create(FRIDAY_LOCAL_DATE, 0));

        Integer mondayHours = newVersion.getWorkableHours(MONDAY_LOCAL_DATE);
        assertThat(mondayHours, equalTo(0));
        assertThat(calendar.getWorkableHours(MONDAY_LOCAL_DATE),
                equalTo(mondayHours));

        Integer fridayHours = newVersion.getWorkableHours(FRIDAY_LOCAL_DATE);
        assertThat(fridayHours, equalTo(0));
        assertThat(calendar.getWorkableHours(FRIDAY_LOCAL_DATE),
                equalTo(fridayHours));

        assertThat(calendar.getOwnExceptions().size(), equalTo(1));
        assertThat(newVersion.getOwnExceptions().size(), equalTo(1));
    }

    @Test
    public void testGetVersion() {
        BaseCalendar calendar = createBasicCalendar();
        BaseCalendar newVersion = calendar.newVersion(WEDNESDAY_LOCAL_DATE);
        BaseCalendar lastVersion = newVersion.newVersion(SATURDAY_LOCAL_DATE);

        assertThat(lastVersion.getCalendarVersion(MONDAY_LOCAL_DATE),
                equalTo(calendar));
        assertThat(lastVersion.getCalendarVersion(THURSDAY_LOCAL_DATE),
                equalTo(newVersion));
        assertThat(lastVersion.getCalendarVersion(SUNDAY_LOCAL_DATE),
                equalTo(lastVersion));
    }

}
