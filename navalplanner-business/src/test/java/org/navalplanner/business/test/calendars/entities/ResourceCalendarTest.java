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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.workingday.IntraDayDate.PartialDay.wholeDay;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.CalendarAvailability;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.business.calendars.entities.Capacity;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.workingday.EffortDuration;

/**
 * Tests for {@link ResourceCalendar}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ResourceCalendarTest {

    public static ResourceCalendar createBasicResourceCalendar() {
        ResourceCalendar calendar = ResourceCalendar.create();
        calendar.setName("Test");
        for (Days each : Days.values()) {
            calendar.setCapacityAt(each,
                    Capacity.create(EffortDuration.hours(8))
                            .overAssignableWithoutLimit(true));
        }
        return calendar;
    }

    private static final LocalDate PAST = (new LocalDate()).minusMonths(1);
    private static final LocalDate FUTURE = (new LocalDate()).plusMonths(1);

    @Test
    public void testIsActive() {
        ResourceCalendar calendar = createBasicResourceCalendar();

        assertFalse(calendar.isActive(PAST));
        assertTrue(calendar.isActive(FUTURE));
    }

    @Test
    public void testGetWorkableHours() {
        ResourceCalendar calendar = createBasicResourceCalendar();

        assertThat(calendar.getCapacityOn(wholeDay(PAST)),
                equalTo(EffortDuration.zero()));
        assertThat(calendar.getCapacityOn(wholeDay(FUTURE)),
                equalTo(EffortDuration.hours(8)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAllowCreateCalendarAvailabilityInThePast() {
        ResourceCalendar calendar = createBasicResourceCalendar();

        CalendarAvailability calendarAvailability = CalendarAvailability
                .create(PAST, null);
        calendar.addNewCalendarAvailability(calendarAvailability);
    }

    @Test
    public void allowCreateCalendarAvailabilityInTheFuture() {
        ResourceCalendar calendar = createBasicResourceCalendar();

        CalendarAvailability calendarAvailability = CalendarAvailability
                .create(FUTURE, null);
        calendar.addNewCalendarAvailability(calendarAvailability);

        List<CalendarAvailability> calendarAvailabilities = calendar.getCalendarAvailabilities();
        assertThat(calendarAvailabilities.size(), equalTo(2));
        assertThat(calendarAvailabilities.get(0).getEndDate(), equalTo(FUTURE
                .minusDays(1)));
        assertThat(calendarAvailabilities.get(1).getStartDate(),
                equalTo(FUTURE));
        assertNull(calendarAvailabilities.get(1).getEndDate());
    }

}