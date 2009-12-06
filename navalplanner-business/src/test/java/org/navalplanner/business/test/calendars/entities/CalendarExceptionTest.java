/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

import java.util.Date;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.calendars.entities.CalendarException;

/**
 * Tests for {@link CalendarException}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarExceptionTest {

    @Test
    public void testCreateExceptionDayWithDate() {
        Date date = new Date();

        CalendarException day = CalendarException.create(date, 8);

        assertThat(day.getDate(), equalTo(new LocalDate(date)));
        assertThat(day.getHours(), equalTo(8));
    }

}
