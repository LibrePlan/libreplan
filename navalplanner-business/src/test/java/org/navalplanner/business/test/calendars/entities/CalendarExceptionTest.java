/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.CalendarException;
import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.workingday.EffortDuration;

/**
 * Tests for {@link CalendarException}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarExceptionTest {

    @Test
    public void testCreateExceptionDayWithDate() {
        LocalDate date = new LocalDate();
        CalendarExceptionType type = BaseCalendarTest
                .createCalendarExceptionType();

        CalendarException day = CalendarException.create(date,
                EffortDuration.hours(8), type);

        assertThat(day.getDate(), equalTo(new LocalDate(date)));
        assertThat(day.getDuration(), equalTo(EffortDuration.hours(8)));
        assertThat(day.getType(), equalTo(type));
    }

}
