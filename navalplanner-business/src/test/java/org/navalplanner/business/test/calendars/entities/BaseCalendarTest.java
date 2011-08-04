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

package org.navalplanner.business.test.calendars.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.workingday.EffortDuration.hours;
import static org.navalplanner.business.workingday.EffortDuration.zero;
import static org.navalplanner.business.workingday.IntraDayDate.PartialDay.wholeDay;

import java.util.Set;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.BaseCalendar.DayType;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.business.calendars.entities.CalendarException;
import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.calendars.entities.CalendarExceptionTypeColor;
import org.navalplanner.business.calendars.entities.Capacity;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.navalplanner.business.workingday.ResourcesPerDay;

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

        Capacity eightHours = withNormalDuration(hours(8));
        calendar.setCapacityAt(Days.MONDAY, eightHours);
        calendar.setCapacityAt(Days.TUESDAY, eightHours);
        calendar.setCapacityAt(Days.WEDNESDAY, eightHours);
        calendar.setCapacityAt(Days.THURSDAY, eightHours);
        calendar.setCapacityAt(Days.FRIDAY, eightHours);
        calendar.setCapacityAt(Days.SATURDAY, Capacity.zero());
        calendar.setCapacityAt(Days.SUNDAY, Capacity.zero());

        return calendar;
    }

    /**
     * Creates a {@link Capacity} with normal {@link EffortDuration} and no
     * extra hours limit
     * @param effort
     * @return
     */
    private static Capacity withNormalDuration(EffortDuration effort) {
        return Capacity.create(effort).overAssignableWithoutLimit();
    }

    private BaseCalendar calendarFixture;

    private void givenUnitializedCalendar() {
        calendarFixture = BaseCalendar.create();
    }

    public static CalendarExceptionType createCalendarExceptionType() {
        CalendarExceptionType result = CalendarExceptionType.create("TEST",
                CalendarExceptionTypeColor.DEFAULT, true);
        return result;
    }

    public static void addChristmasAsExceptionDay(BaseCalendar calendar) {
        CalendarException christmasDay = CalendarException.create(
                CHRISTMAS_DAY_LOCAL_DATE, EffortDuration.zero(),
                createCalendarExceptionType());

        calendar.addExceptionDay(christmasDay);
    }

    @Test
    public void testOnlyGivesZeroHoursWhenThereIsNoParent() {
        BaseCalendar calendar = createBasicCalendar();
        assertFalse(calendar.onlyGivesZeroHours());
        initializeAllToZeroHours(calendar);
        assertTrue(calendar.onlyGivesZeroHours());
    }

    private void initializeAllToZeroHours(BaseCalendar calendar) {
        for (Days each : Days.values()) {
            calendar.setCapacityAt(each, Capacity.zero());
        }
    }

    @Test
    public void testOnlyGivesZeroHoursWhenThereIsParent() {
        BaseCalendar calendar = createBasicCalendar();
        initializeAllToZeroHours(calendar);
        BaseCalendar parent = createBasicCalendar();
        calendar.setParent(parent);
        assertTrue(calendar.onlyGivesZeroHours());
        calendar.setDefault(Days.MONDAY);
        assertFalse(calendar.onlyGivesZeroHours());
    }

    public static BaseCalendar createChristmasCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        addChristmasAsExceptionDay(calendar);
        return calendar;
    }

    @Test
    public void testCapacityOnBasic() {
        BaseCalendar calendar = createBasicCalendar();

        EffortDuration wednesdayHours = calendar
                .getCapacityOn(wholeDay(WEDNESDAY_LOCAL_DATE));
        assertThat(wednesdayHours, equalTo(hours(8)));

        EffortDuration sundayHours = calendar
                .getCapacityOn(wholeDay(SUNDAY_LOCAL_DATE));
        assertThat(sundayHours, equalTo(zero()));
    }

    @Test
    public void aBaseCalendarMustBeActive() {
        BaseCalendar calendar = createBasicCalendar();
        assertTrue(calendar.isActive(new LocalDate()));
    }

    @Test
    public void testCapacityOnChristmas() {
        BaseCalendar calendar = createChristmasCalendar();

        EffortDuration duration = calendar
                .getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE));
        assertThat(duration, equalTo(EffortDuration.zero()));
    }

    @Test
    public void testDeriveCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        BaseCalendar derivedCalendar = calendar.newDerivedCalendar();

        assertThat(derivedCalendar.getParent(), equalTo(calendar));
    }

    @Test
    public void testCapacityOnDerivedBasicCalendar() {
        BaseCalendar calendar = createBasicCalendar().newDerivedCalendar();

        EffortDuration wednesdayHours = calendar
                .getCapacityOn(wholeDay(WEDNESDAY_LOCAL_DATE));
        assertThat(wednesdayHours, equalTo(hours(8)));

        EffortDuration sundayHours = calendar
                .getCapacityOn(wholeDay(SUNDAY_LOCAL_DATE));
        assertThat(sundayHours, equalTo(zero()));
    }

    @Test
    public void testCapacityOnDerivedChristmasCalendar() {
        BaseCalendar calendar = createChristmasCalendar().newDerivedCalendar();

        EffortDuration hours = calendar
                .getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE));
        assertThat(hours, equalTo(EffortDuration.zero()));
    }

    @Test
    public void testCapacityOnDerivedBasicCalendarWithException() {
        BaseCalendar calendar = createBasicCalendar().newDerivedCalendar();

        CalendarException day = CalendarException.create(WEDNESDAY_LOCAL_DATE,
                hours(4), createCalendarExceptionType());
        calendar.addExceptionDay(day);

        EffortDuration mondayHours = calendar
                .getCapacityOn(wholeDay(MONDAY_LOCAL_DATE));
        assertThat(mondayHours, equalTo(hours(8)));

        EffortDuration wednesdayHours = calendar
                .getCapacityOn(wholeDay(WEDNESDAY_LOCAL_DATE));
        assertThat(wednesdayHours, equalTo(hours(4)));

        EffortDuration sundayHours = calendar
                .getCapacityOn(wholeDay(SUNDAY_LOCAL_DATE));
        assertThat(sundayHours, equalTo(zero()));
    }

    @Test
    public void testCapacityOnDerivedChristmasCalendarRedefiningExceptionDay() {
        BaseCalendar calendar = createChristmasCalendar().newDerivedCalendar();

        CalendarException day = CalendarException.create(
                CHRISTMAS_DAY_LOCAL_DATE, hours(4),
                createCalendarExceptionType());
        calendar.addExceptionDay(day);

        EffortDuration hours = calendar
                .getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE));
        assertThat(hours, equalTo(hours(4)));
    }

    @Test
    public void testCapacityOnInterval() {
        BaseCalendar calendar = createBasicCalendar();

        int mondayToWednesdayHours = calendar.getWorkableHours(
                MONDAY_LOCAL_DATE, WEDNESDAY_LOCAL_DATE);
        assertThat(mondayToWednesdayHours, equalTo(24));
    }

    @Test
    public void testCapacityOnPerWeek() {
        BaseCalendar calendar = createBasicCalendar();

        int weekHours = calendar.getWorkableHoursPerWeek(WEDNESDAY_LOCAL_DATE);
        assertThat(weekHours, equalTo(40));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTwoExceptionDaysInTheSameDate() {
        BaseCalendar calendar = createBasicCalendar();

        CalendarException day = CalendarException.create(MONDAY_LOCAL_DATE,
                hours(8), createCalendarExceptionType());
        calendar.addExceptionDay(day);

        CalendarException day2 = CalendarException.create(MONDAY_LOCAL_DATE,
                hours(4), createCalendarExceptionType());
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
    public void testCapacityOnNewVersion() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.newVersion(MONDAY_LOCAL_DATE);

        calendar.setCapacityAt(Days.WEDNESDAY, withNormalDuration(hours(4)));
        calendar.setCapacityAt(Days.SUNDAY, withNormalDuration(hours(4)));

        assertThat(calendar.getCapacityOn(wholeDay(WEDNESDAY_LOCAL_DATE)),
                equalTo(hours(4)));

        assertThat(calendar.getCapacityOn(wholeDay(WEDNESDAY_LOCAL_DATE
                .minusWeeks(1))), equalTo(hours(8)));

        assertThat(calendar.getCapacityOn(wholeDay(SUNDAY_LOCAL_DATE)),
                equalTo(hours(4)));

        assertThat(calendar.getCapacityOn(wholeDay(SUNDAY_LOCAL_DATE
                .minusWeeks(1))),
                equalTo(zero()));
    }

    @Test
    public void testCapacityOnNewVersionCheckingLimits() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.newVersion(MONDAY_LOCAL_DATE);

        calendar.setCapacityAt(Days.MONDAY, withNormalDuration(hours(1)));
        calendar.setCapacityAt(Days.SUNDAY, withNormalDuration(hours(2)));

        assertThat(calendar.getCapacityOn(wholeDay(MONDAY_LOCAL_DATE)),
                equalTo(hours(1)));

        assertThat(calendar.getCapacityOn(wholeDay(SUNDAY_LOCAL_DATE)),
                equalTo(hours(2)));

        assertThat(calendar.getCapacityOn(wholeDay(MONDAY_LOCAL_DATE
                .minusWeeks(1))),
                equalTo(hours(8)));

        assertThat(calendar.getCapacityOn(wholeDay(MONDAY_LOCAL_DATE
                .minusDays(1))),
                equalTo(zero()));
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
    public void testCapacityOnNewVersionFromChristmasCalendar() {
        BaseCalendar calendar = createChristmasCalendar();
        CalendarException day = CalendarException.create(
                CHRISTMAS_DAY_LOCAL_DATE.plusYears(1), EffortDuration.zero(),
                createCalendarExceptionType());
        calendar.addExceptionDay(day);

        calendar.newVersion(CHRISTMAS_DAY_LOCAL_DATE.plusDays(1));

        CalendarExceptionType type = createCalendarExceptionType();
        calendar.updateExceptionDay(CHRISTMAS_DAY_LOCAL_DATE.plusYears(1),
                Capacity.create(hours(8)).withAllowedExtraEffort(
                        type.getCapacity().getAllowedExtraEffort()), type);

        assertThat(calendar.getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE
                .plusYears(1))), equalTo(hours(8)));

        assertThat(calendar.getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE)),
                equalTo(zero()));
    }

    public static void setHoursForAllDays(BaseCalendar calendar, Integer hours) {
        Capacity capacity = withNormalDuration(hours(hours));
        for (Days each : Days.values()) {
            calendar.setCapacityAt(each, capacity);
        }
    }

    @Test
    public void testCapacityOnTwoNewVersions() {
        BaseCalendar calendar = createBasicCalendar();
        setHoursForAllDays(calendar, 8);

        calendar.newVersion(TUESDAY_LOCAL_DATE);
        setHoursForAllDays(calendar, 4);

        calendar.newVersion(FRIDAY_LOCAL_DATE);
        setHoursForAllDays(calendar, 2);

        assertThat(calendar.getCapacityOn(wholeDay(MONDAY_LOCAL_DATE)),
                equalTo(hours(8)));

        assertThat(calendar.getCapacityOn(wholeDay(WEDNESDAY_LOCAL_DATE)),
                equalTo(hours(4)));

        assertThat(calendar.getCapacityOn(wholeDay(FRIDAY_LOCAL_DATE)),
                equalTo(hours(2)));

    }

    @Test
    public void testCapacityOnDeriveAndNewVersion() {
        BaseCalendar baseCalendar = createChristmasCalendar();

        BaseCalendar calendar = baseCalendar.newDerivedCalendar();
        setHoursForAllDays(calendar, 4);

        calendar.newVersion(WEDNESDAY_LOCAL_DATE);
        setHoursForAllDays(calendar, 2);

        assertThat(baseCalendar.getCapacityOn(wholeDay(MONDAY_LOCAL_DATE)),
                equalTo(hours(8)));

        assertThat(calendar.getCapacityOn(wholeDay(MONDAY_LOCAL_DATE)),
                equalTo(hours(4)));

        assertThat(baseCalendar.getCapacityOn(wholeDay(FRIDAY_LOCAL_DATE)),
                equalTo(hours(8)));

        assertThat(calendar.getCapacityOn(wholeDay(FRIDAY_LOCAL_DATE)),
                equalTo(hours(2)));

        assertThat(calendar.getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE)),
                equalTo(zero()));
    }

    @Test
    public void testAddExceptionToNewVersionCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.newVersion(CHRISTMAS_DAY_LOCAL_DATE
                .plusDays(1));

        CalendarException day = CalendarException.create(
                CHRISTMAS_DAY_LOCAL_DATE, EffortDuration.zero(),
                createCalendarExceptionType());
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
            assertThat(calendarFixture.getCapacityOn(wholeDay(localDate)),
                    equalTo(zero()));
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
        assertThat(calendar.getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE)),
                equalTo(zero()));
    }

    @Test
    public void testNewCopy() {
        BaseCalendar calendar = createChristmasCalendar();
        BaseCalendar derived = calendar.newDerivedCalendar();
        BaseCalendar copy = derived.newCopy();

        assertThat(copy.getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE)),
                equalTo(zero()));
        assertThat(copy.getParent(), equalTo(calendar));
        assertThat(copy.getCalendarDataVersions().size(), equalTo(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetHoursInvalid() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.setCapacityAt(Days.MONDAY, withNormalDuration(hours(-5)));
    }

    @Test
    public void testCapacityOnNewVersionChangeParent() {
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

        assertThat(calendar.getCapacityOn(wholeDay(MONDAY_LOCAL_DATE)),
                equalTo(hours(8)));

        assertThat(calendar.getCapacityOn(wholeDay(FRIDAY_LOCAL_DATE)),
                equalTo(hours(4)));
    }

    @Test
    public void testExceptionsInDifferentVersions() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.newVersion(WEDNESDAY_LOCAL_DATE);

        calendar.addExceptionDay(CalendarException.create(MONDAY_LOCAL_DATE,
                zero(), createCalendarExceptionType()));
        calendar.addExceptionDay(CalendarException.create(FRIDAY_LOCAL_DATE,
                zero(), createCalendarExceptionType()));

        assertThat(calendar.getCapacityOn(wholeDay(MONDAY_LOCAL_DATE)),
                equalTo(zero()));

        assertThat(calendar.getCapacityOn(wholeDay(FRIDAY_LOCAL_DATE)),
                equalTo(zero()));

        assertThat(calendar.getOwnExceptions().size(), equalTo(2));
    }

    @Test
    public void testAllowCreateExceptionsInThePast() {
        BaseCalendar calendar = createBasicCalendar();

        LocalDate pastMonth = (new LocalDate()).minusMonths(1);
        CalendarException exceptionDay = CalendarException.create(pastMonth,
                zero(),
                createCalendarExceptionType());

        calendar.addExceptionDay(exceptionDay);
    }

    @Test
    public void testAllowRemoveExceptionsInThePast() {
        BaseCalendar calendar = createBasicCalendar();

        LocalDate pastMonth = (new LocalDate()).minusMonths(1);
        CalendarException exceptionDay = CalendarException.create(pastMonth,
                zero(), createCalendarExceptionType());

        calendar.addExceptionDay(exceptionDay);
        calendar.removeExceptionDay(pastMonth);
    }

    @Test
    public void testAllowSetExpiringDateInThePast() {
        BaseCalendar calendar = createBasicCalendar();

        calendar.newVersion((new LocalDate()).plusDays(1));

        LocalDate pastWeek = (new LocalDate()).minusWeeks(1);
        calendar.setExpiringDate(pastWeek);
    }

    @Test
    public void testSetExpiringDate() {
        BaseCalendar calendar = createBasicCalendar();

        LocalDate currentDate = new LocalDate();
        calendar.newVersion(currentDate.plusWeeks(4));

        assertThat(calendar.getExpiringDate(currentDate), equalTo(currentDate
                .plusWeeks(4)));
        assertThat(calendar.getExpiringDate(currentDate.plusWeeks(4)),
                nullValue());

        calendar.setExpiringDate(currentDate.plusWeeks(2), currentDate);

        assertThat(calendar.getExpiringDate(currentDate), equalTo(currentDate
                .plusWeeks(2)));
        assertThat(calendar.getExpiringDate(currentDate.plusWeeks(4)),
                nullValue());
    }

    @Test
    public void testAllowNewVersionOnCurrentDate() {
        BaseCalendar calendar = createBasicCalendar();

        calendar.newVersion(new LocalDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAllowSetExpiringDateIfNotNextCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        assertThat(calendar.getCalendarDataVersions().size(), equalTo(1));

        calendar.setExpiringDate(WEDNESDAY_LOCAL_DATE);
    }

    @Test
    public void testSetValidFrom() {
        BaseCalendar calendar = createBasicCalendar();

        LocalDate currentDate = new LocalDate();
        calendar.newVersion(currentDate.plusWeeks(4));

        assertThat(calendar.getValidFrom(currentDate), nullValue());
        assertThat(calendar.getValidFrom(currentDate.plusWeeks(4)),
                equalTo(currentDate.plusWeeks(4)));

        calendar.setValidFrom(currentDate.plusWeeks(2), currentDate
                .plusWeeks(4));

        assertThat(calendar.getValidFrom(currentDate), nullValue());
        assertThat(calendar.getValidFrom(currentDate.plusWeeks(4)),
                equalTo(currentDate.plusWeeks(2)));
    }

    @Test
    public void testAllowSetValidFromInThePast() {
        BaseCalendar calendar = createBasicCalendar();

        LocalDate currentDate = new LocalDate();
        calendar.newVersion(currentDate.plusDays(1));

        LocalDate pastWeek = currentDate.minusWeeks(1);

        calendar.setValidFrom(pastWeek, currentDate.plusDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAllowSetValidFromIfNotPreviousCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        assertThat(calendar.getCalendarDataVersions().size(), equalTo(1));

        LocalDate currentDate = new LocalDate();
        calendar.setValidFrom(currentDate, currentDate);
    }

    @Test
    public void testGetNonWorkableDays() {
        BaseCalendar calendar = createBasicCalendar();

        Set<LocalDate> nonWorkableDays = calendar.getNonWorkableDays(
                MONDAY_LOCAL_DATE, WEDNESDAY_LOCAL_DATE);
        assertTrue(nonWorkableDays.isEmpty());

        nonWorkableDays = calendar.getNonWorkableDays(MONDAY_LOCAL_DATE,
                SUNDAY_LOCAL_DATE);
        assertFalse(nonWorkableDays.isEmpty());
        assertTrue(nonWorkableDays.contains(SATURDAY_LOCAL_DATE));
        assertTrue(nonWorkableDays.contains(SUNDAY_LOCAL_DATE));
    }

    @Test
    public void aCalendarHasAMethodToConvertAnAmountOfResourcesPerDayToAEffortDuration() {
        BaseCalendar calendar = createBasicCalendar();
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(1)), equalTo(hours(8)));
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(2)), equalTo(hours(16)));
    }

    @Test
    public void asDurationOnRespectsTheOverAssignablePropertyOfCalendarData() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.setCapacityAt(Days.MONDAY, Capacity.create(hours(8))
                .overAssignableWithoutLimit());

        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(1)), equalTo(hours(8)));
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(2)), equalTo(hours(16)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCapacityWithOvertimeMustNotBeCalledWithANullDate() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.getCapacityWithOvertime(null);
    }

    @Test
    public void getCapacityWithOvertimeOnReturnsTheCapacityForThatDay() {
        BaseCalendar calendar = createBasicCalendar();
        Capacity capacitySet = Capacity.create(hours(8))
                .overAssignableWithoutLimit();

        calendar.setCapacityAt(Days.MONDAY, capacitySet);
        assertThat(calendar.getCapacityWithOvertime(MONDAY_LOCAL_DATE),
                equalTo(capacitySet));
    }

    @Test
    public void asDurationOnRespectsTheNotOverAssignablePropertyOfCalendarData() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.setCapacityAt(Days.MONDAY, Capacity.create(hours(8))
                .notOverAssignableWithoutLimit());

        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(1)), equalTo(hours(8)));
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(2)), equalTo(hours(8)));
    }

    @Test
    public void DurationOnRespectsTheExtraEffortPropertyOfCalendarData() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.setCapacityAt(Days.MONDAY, Capacity.create(hours(8))
                .withAllowedExtraEffort(hours(2)));

        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(1)), equalTo(hours(8)));
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(2)), equalTo(hours(10)));

    }

    private void addExceptionOn(BaseCalendar calendar, LocalDate onDate,
            Capacity capacity) {
        calendar.addExceptionDay(CalendarException.create(onDate, capacity,
                createCalendarExceptionType()));
    }

    @Test
    public void asDurationOnRespectsAnOverAssignableCalendarException() {
        BaseCalendar calendar = createBasicCalendar();
        addExceptionOn(calendar, MONDAY_LOCAL_DATE, Capacity.create(hours(1))
                .overAssignableWithoutLimit());

        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(1)), equalTo(hours(1)));
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(2)), equalTo(hours(2)));
    }

    @Test
    public void asDurationOnRespectsANotOverAssignableCalendarException() {
        BaseCalendar calendar = createBasicCalendar();
        addExceptionOn(calendar, MONDAY_LOCAL_DATE, Capacity.create(hours(1))
                .notOverAssignableWithoutLimit());
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(1)), equalTo(hours(1)));
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(2)), equalTo(hours(1)));
    }

    @Test
    public void asDurationOnRespectsCapacityExtraEffort() {
        BaseCalendar calendar = createBasicCalendar();
        addExceptionOn(calendar, MONDAY_LOCAL_DATE, Capacity.create(hours(2))
                .withAllowedExtraEffort(hours(3)));
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(1)), equalTo(hours(2)));
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(2)), equalTo(hours(4)));
        assertThat(calendar.asDurationOn(
                PartialDay.wholeDay(MONDAY_LOCAL_DATE),
                ResourcesPerDay.amount(3)), equalTo(hours(5)));
    }

    @Test
    public void canWorkOnRespectsTheCapacityOfTheException() {
        BaseCalendar calendar = createBasicCalendar();
        addExceptionOn(calendar, MONDAY_LOCAL_DATE, Capacity.create(hours(0))
                .withAllowedExtraEffort(hours(0)));

        assertFalse(calendar.canWorkOn(MONDAY_LOCAL_DATE));
    }

    @Test
    public void canWorkOnRespectsIsOverAssignable() {
        BaseCalendar calendar = createBasicCalendar();
        addExceptionOn(calendar, MONDAY_LOCAL_DATE, Capacity.create(hours(0))
                .overAssignableWithoutLimit());

        assertTrue(calendar.canWorkOn(MONDAY_LOCAL_DATE));
    }

    @Test
    public void canWorkOnRespectsCalendarData() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.setCapacityAt(Days.MONDAY, Capacity.create(hours(0))
                .overAssignableWithoutLimit());

        assertTrue(calendar.canWorkOn(MONDAY_LOCAL_DATE));

        calendar.setCapacityAt(Days.MONDAY, Capacity.create(hours(0))
                .notOverAssignableWithoutLimit());
        assertFalse(calendar.canWorkOn(MONDAY_LOCAL_DATE));
    }

    @Test
    public void theAvailabilityTimeLineTakesIntoAccountTheDaysItCannotWorkDueToCalendarData() {
        BaseCalendar calendar = createBasicCalendar();
        calendar.setCapacityAt(Days.MONDAY, Capacity.create(hours(0))
                .notOverAssignableWithoutLimit());

        AvailabilityTimeLine availability = calendar.getAvailability();
        assertFalse(availability.isValid(MONDAY_LOCAL_DATE));
    }
}
