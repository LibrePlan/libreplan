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
package org.navalplanner.business.test.workingday;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.Granularity;
import org.navalplanner.business.workingday.TaskDate;

/**
 * @author Óscar González Fernández
 *
 */
public class TaskDateTest {

    private LocalDate today = new LocalDate();
    private LocalDate tomorrow = today.plusDays(1);
    private EffortDuration oneHour = EffortDuration.elapsing(1,
            Granularity.HOURS);
    private EffortDuration halfHour = EffortDuration.elapsing(30,
            Granularity.MINUTES);

    @Test(expected = IllegalArgumentException.class)
    public void needsANotNullDuration() {
        TaskDate.create(new LocalDate(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void needsANotNullDate() {
        TaskDate.create(null, EffortDuration.elapsing(1, Granularity.HOURS));
    }

    @Test
    public void equalsAndHashCodeWorkOk() {
        LocalDate today = new LocalDate();
        LocalDate tomorrow = today.plusDays(1);
        EffortDuration oneHour = EffortDuration.elapsing(1, Granularity.HOURS);
        EffortDuration halfHour = EffortDuration.elapsing(30,
                Granularity.MINUTES);
        assertEquals(TaskDate.create(today, halfHour),
                TaskDate.create(tomorrow.minusDays(1), halfHour));
        assertEquals(TaskDate.create(today, halfHour).hashCode(), TaskDate
                .create(tomorrow.minusDays(1), halfHour).hashCode());
        assertThat(TaskDate.create(today, halfHour),
                not(equalTo(TaskDate.create(today, oneHour))));
    }

    @Test
    public void canKnowIfAreSameDay() {
        assertTrue(TaskDate.create(today, halfHour).areSameDay(today));
        assertTrue(TaskDate.create(today, oneHour).areSameDay(today));
        assertFalse(TaskDate.create(today, halfHour).areSameDay(tomorrow));
        assertFalse(TaskDate.create(tomorrow, halfHour).areSameDay(today));
    }

    @Test
    public void canGetDateTimeAtStartOfDay() {
        DateTime dateTime = TaskDate.create(today, halfHour)
                .toDateTimeAtStartOfDay();
        assertThat(dateTime, equalTo(today.toDateTimeAtStartOfDay()));
    }
}
