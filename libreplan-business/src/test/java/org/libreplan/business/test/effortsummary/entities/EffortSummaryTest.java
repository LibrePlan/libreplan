/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.business.test.effortsummary.entities;

import static org.junit.Assert.assertEquals;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.business.workingday.EffortDuration.hours;
import static org.libreplan.business.workingday.IntraDayDate.PartialDay.wholeDay;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.CalendarData.Days;
import org.libreplan.business.calendars.entities.CalendarException;
import org.libreplan.business.calendars.entities.CalendarExceptionType;
import org.libreplan.business.calendars.entities.CalendarExceptionTypeColor;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.effortsummary.entities.EffortSummary;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class EffortSummaryTest {

    public static final LocalDate JUNE_NEXT_YEAR = new LocalDate(
            (new LocalDate()).getYear(), 6, 1).plusYears(1);

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

    public static final LocalDate CHRISTMAS_DAY_LOCAL_DATE = new LocalDate(
            JUNE_NEXT_YEAR.getYear(), 12, 25);

    public static BaseCalendar createBasicCalendar() {
        BaseCalendar calendar = BaseCalendar.create();

        calendar.setName("test-" + UUID.randomUUID());

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
     *
     * @param effort
     * @return
     */
    private static Capacity withNormalDuration(EffortDuration effort) {
        return Capacity.create(effort).overAssignableWithoutLimit();
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

    public static BaseCalendar createChristmasCalendar() {
        BaseCalendar calendar = createBasicCalendar();
        addChristmasAsExceptionDay(calendar);
        return calendar;
    }

    public static Worker generateValidWorker() {
        Worker worker = Worker.create();
        worker.setFirstName("First name");
        worker.setSurname("Surname");
        worker.setNif("NIF" + UUID.randomUUID().toString());
        return worker;
    }

    @Test
    public void testCreateFromResource() {
        Worker worker = generateValidWorker();
        worker.setCalendar(createChristmasCalendar()
                .newDerivedResourceCalendar());
        EffortSummary summary = EffortSummary.createFromNewResource(worker);

        assertEquals(
                summary.getAvailableEffortForDate(WEDNESDAY_LOCAL_DATE),
                worker.getCalendar().getCapacityOn(wholeDay(WEDNESDAY_LOCAL_DATE)));
        assertEquals(EffortDuration.hours(8),
                summary.getAvailableEffortForDate(WEDNESDAY_LOCAL_DATE));

        assertEquals(summary.getAvailableEffortForDate(SUNDAY_LOCAL_DATE),
                worker.getCalendar().getCapacityOn(wholeDay(SUNDAY_LOCAL_DATE)));
        assertEquals(EffortDuration.zero(),
                summary.getAvailableEffortForDate(SUNDAY_LOCAL_DATE));

        assertEquals(
                summary.getAvailableEffortForDate(CHRISTMAS_DAY_LOCAL_DATE),
                worker.getCalendar().getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE)));
        assertEquals(EffortDuration.zero(),
                summary.getAvailableEffortForDate(CHRISTMAS_DAY_LOCAL_DATE));
    }

    @Test
    public void testUpdateAvailability() {
        Worker worker = generateValidWorker();
        worker.setCalendar(createBasicCalendar().newDerivedResourceCalendar());
        EffortSummary summary = EffortSummary.createFromNewResource(worker);

        assertEquals(
                summary.getAvailableEffortForDate(CHRISTMAS_DAY_LOCAL_DATE),
                worker.getCalendar().getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE)));
        assertEquals(EffortDuration.hours(8),
                summary.getAvailableEffortForDate(CHRISTMAS_DAY_LOCAL_DATE));

        // change worker calendar to add exception on Christmas
        worker.setCalendar(createChristmasCalendar()
                .newDerivedResourceCalendar());
        summary.updateAvailabilityFromResource();

        assertEquals(EffortDuration.zero(),
                summary.getAvailableEffortForDate(CHRISTMAS_DAY_LOCAL_DATE));
    }

    @Test
    public void testSerializers() {
        Worker worker = generateValidWorker();
        worker.setCalendar(createChristmasCalendar()
                .newDerivedResourceCalendar());
        EffortSummary summary = EffortSummary.createFromNewResource(worker);

        String serializedArray = summary.getAvailableEffortString();
        summary.setAssignedEffortString(serializedArray);

        assertEquals(
                summary.getAssignedEffortForDate(WEDNESDAY_LOCAL_DATE),
                worker.getCalendar().getCapacityOn(wholeDay(WEDNESDAY_LOCAL_DATE)));

        assertEquals(summary.getAssignedEffortForDate(SUNDAY_LOCAL_DATE),
                worker.getCalendar().getCapacityOn(wholeDay(SUNDAY_LOCAL_DATE)));

        assertEquals(
                summary.getAssignedEffortForDate(CHRISTMAS_DAY_LOCAL_DATE),
                worker.getCalendar().getCapacityOn(wholeDay(CHRISTMAS_DAY_LOCAL_DATE)));
    }

}
