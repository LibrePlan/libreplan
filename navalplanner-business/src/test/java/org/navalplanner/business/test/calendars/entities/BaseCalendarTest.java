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
import org.navalplanner.business.calendars.entities.CalendarData.Days;

/**
 * Tests for {@link BaseCalendar}.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendarTest {

    public static final LocalDate JUNE_NEXT_YEAR = new LocalDate(
            (new LocalDate())
            .getYear(), 6, 1).plusYears(1);

    public static final LocalDate MONDAY_LOCAL_DATE = JUNE_NEXT_YEAR
            .dayOfWeek().withMinimumValue();
    public static final LocalDate TUESDAY_LOCAL_DATE = MONDAY_LOCAL_DATE
            .plusDays(1);
    public static final LocalDate WEDNESDAY_LOCAL_DATE = MONDAY_LOCAL_DATE
            .plusDays(2);
    public static final LocalDate THURSDAY_LOCAL_DATE = MONDAY_LOCAL_DATE
            .plusDays(3);
    public static final LocalDate FRIDAY_LOCAL_DATE = MONDAY_LOCAL_DATE
            .plusDays(4);
    public static final LocalDate SATURDAY_LOCAL_DATE = MONDAY_LOCAL_DATE
            .plusDays(5);
    public static final LocalDate SUNDAY_LOCAL_DATE = MONDAY_LOCAL_DATE
            .plusDays(6);

    private static final LocalDate[] DAYS_OF_A_WEEK_EXAMPLE = {
            MONDAY_LOCAL_DATE, TUESDAY_LOCAL_DATE, WEDNESDAY_LOCAL_DATE,
            THURSDAY_LOCAL_DATE, FRIDAY_LOCAL_DATE, SATURDAY_LOCAL_DATE,
            SUNDAY_LOCAL_DATE };

    public static final LocalDate CHRISTMAS_DAY_LOCAL_DATE = new LocalDate(
            JUNE_NEXT_YEAR.getYear(), 12, 25);

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
        calendar.newVersion((new LocalDate()).plusDays(1));

        assertThat(calendar.getCalendarDataVersions().size(), equalTo(2));
    }

    @Test
    public void testCreateNewVersionPreservesName() {
        BaseCalendar calendar = createBasicCalendar();
        String name = calendar.getName();
        calendar.newVersion((new LocalDate()).plusDays(1));

        assertThat(calendar.getName(), equalTo(name));
    }

    @Test
    public void testChangeNameForAllVersions() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.setName("Test");
        calendar.newVersion((new LocalDate()).plusDays(1));

        String name = "Name";
        calendar.setName(name);

        assertThat(calendar.getName(), equalTo(name));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateInvalidNewVersion() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.newVersion(FRIDAY_LOCAL_DATE);
        calendar.newVersion(MONDAY_LOCAL_DATE);
    }

    @Test
    public void testGettWorkableHoursNewVersion() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.newVersion(MONDAY_LOCAL_DATE);

        calendar.setHours(Days.WEDNESDAY, 4);
        calendar.setHours(Days.SUNDAY, 4);

        assertThat(calendar.getWorkableHours(WEDNESDAY_LOCAL_DATE), equalTo(4));

        assertThat(calendar
                .getWorkableHours(WEDNESDAY_LOCAL_DATE.minusWeeks(1)),
                equalTo(8));

        assertThat(calendar.getWorkableHours(SUNDAY_LOCAL_DATE), equalTo(4));

        assertThat(calendar.getWorkableHours(SUNDAY_LOCAL_DATE.minusWeeks(1)),
                equalTo(0));
    }

    @Test
    public void testGettWorkableHoursNewVersionCheckingLimits() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.newVersion(MONDAY_LOCAL_DATE);

        calendar.setHours(Days.MONDAY, 1);
        calendar.setHours(Days.SUNDAY, 2);

        assertThat(calendar.getWorkableHours(MONDAY_LOCAL_DATE), equalTo(1));

        assertThat(calendar.getWorkableHours(SUNDAY_LOCAL_DATE), equalTo(2));

        assertThat(calendar
                .getWorkableHours(MONDAY_LOCAL_DATE.minusWeeks(1)), equalTo(8));

        assertThat(calendar.getWorkableHours(MONDAY_LOCAL_DATE
                .minusDays(1)), equalTo(0));
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
        BaseCalendar calendar = createChristmasCalendar();
        calendar.newVersion(MONDAY_LOCAL_DATE);

        calendar.removeExceptionDay(CHRISTMAS_DAY_LOCAL_DATE);

        assertThat(calendar.getExceptions().size(), equalTo(0));
    }

    @Test
    public void testGettWorkableHoursNewVersionFromChristmasCalendar() {
        BaseCalendar calendar = createChristmasCalendar();
        ExceptionDay day = ExceptionDay.create(CHRISTMAS_DAY_LOCAL_DATE
                .plusYears(1), 0);
        calendar.addExceptionDay(day);

        calendar.newVersion(CHRISTMAS_DAY_LOCAL_DATE.plusDays(1));

        calendar
                .updateExceptionDay(CHRISTMAS_DAY_LOCAL_DATE.plusYears(1), 8);

        assertThat(calendar
                .getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE.plusYears(1)), equalTo(8));

        assertThat(calendar
                .getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE), equalTo(0));
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

        calendar.newVersion(TUESDAY_LOCAL_DATE);
        setHoursForAllDays(calendar, 4);

        calendar.newVersion(FRIDAY_LOCAL_DATE);
        setHoursForAllDays(calendar, 2);

        assertThat(calendar.getWorkableHours(MONDAY_LOCAL_DATE), equalTo(8));

        assertThat(calendar.getWorkableHours(WEDNESDAY_LOCAL_DATE), equalTo(4));

        assertThat(calendar.getWorkableHours(FRIDAY_LOCAL_DATE), equalTo(2));

    }

    @Test
    public void testGettWorkableHoursDeriveAndNewVersion() {
        BaseCalendar baseCalendar = createChristmasCalendar();

        BaseCalendar calendar = baseCalendar.newDerivedCalendar();
        setHoursForAllDays(calendar, 4);

        calendar.newVersion(WEDNESDAY_LOCAL_DATE);
        setHoursForAllDays(calendar, 2);

        assertThat(baseCalendar.getWorkableHours(MONDAY_LOCAL_DATE), equalTo(8));

        assertThat(calendar.getWorkableHours(MONDAY_LOCAL_DATE), equalTo(4));

        assertThat(baseCalendar.getWorkableHours(FRIDAY_LOCAL_DATE), equalTo(8));

        assertThat(calendar.getWorkableHours(FRIDAY_LOCAL_DATE), equalTo(2));

        assertThat(calendar.getWorkableHours(CHRISTMAS_DAY_LOCAL_DATE),
                equalTo(0));
    }

    @Test
    public void testAddExceptionToNewVersionCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.newVersion(CHRISTMAS_DAY_LOCAL_DATE
                .plusDays(1));

        ExceptionDay day = ExceptionDay.create(CHRISTMAS_DAY_LOCAL_DATE, 0);
        calendar.addExceptionDay(day);

        assertThat(calendar.getExceptions().size(), equalTo(1));
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
        assertThat(copy.getCalendarDataVersions().size(), equalTo(1));
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

        calendar.newVersion(WEDNESDAY_LOCAL_DATE);
        calendar.setParent(parent2);

        assertThat(calendar.getParent(), equalTo(parent2));
        assertThat(calendar.getParent(MONDAY_LOCAL_DATE),
                equalTo(parent1));

        assertThat(calendar.getWorkableHours(MONDAY_LOCAL_DATE),
                equalTo(8));

        assertThat(calendar.getWorkableHours(FRIDAY_LOCAL_DATE),
                equalTo(4));
    }

    @Test
    public void testExceptionsInDifferentVersions() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.newVersion(WEDNESDAY_LOCAL_DATE);

        calendar.addExceptionDay(ExceptionDay.create(MONDAY_LOCAL_DATE, 0));
        calendar.addExceptionDay(ExceptionDay.create(FRIDAY_LOCAL_DATE, 0));

        assertThat(calendar.getWorkableHours(MONDAY_LOCAL_DATE),
                equalTo(0));

        assertThat(calendar.getWorkableHours(FRIDAY_LOCAL_DATE),
                equalTo(0));

        assertThat(calendar.getOwnExceptions().size(), equalTo(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAllowCreateExceptionsInThePast() {
        BaseCalendar calendar = createBasicCalendar();

        LocalDate pastMonth = (new LocalDate()).minusMonths(1);
        ExceptionDay exceptionDay = ExceptionDay.create(pastMonth, 0);

        calendar.addExceptionDay(exceptionDay);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAllowRemoveExceptionsInThePast() {
        BaseCalendar calendar = createBasicCalendar();

        LocalDate pastMonth = (new LocalDate()).minusMonths(1);

        calendar.removeExceptionDay(pastMonth);
    }

    @Test
    public void testNotAllowSetExpiringDateInThePast() {
        BaseCalendar calendar = createBasicCalendar();

        calendar.newVersion((new LocalDate()).plusDays(1));

        LocalDate pastWeek = (new LocalDate()).minusWeeks(1);
        try {
            calendar.setExpiringDate(pastWeek);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {

        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAllowNewVersionOnCurrentDate() {
        BaseCalendar calendar = createBasicCalendar();

        calendar.newVersion(new LocalDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAllowSetExpiringDateIfNotNextCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        assertThat(calendar.getCalendarDataVersions().size(), equalTo(1));

        calendar.setExpiringDate(WEDNESDAY_LOCAL_DATE);
    }

}
