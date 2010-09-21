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
import org.navalplanner.business.workingday.IntraDayDate;

/**
 * @author Óscar González Fernández
 *
 */
public class IntraDayDateTest {

    private LocalDate today = new LocalDate();
    private LocalDate tomorrow = today.plusDays(1);
    private EffortDuration oneHour = EffortDuration.hours(1);
    private EffortDuration halfHour = EffortDuration.minutes(30);

    @Test(expected = IllegalArgumentException.class)
    public void needsANotNullDuration() {
        IntraDayDate.create(new LocalDate(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void needsANotNullDate() {
        IntraDayDate.create(null, EffortDuration.elapsing(1, Granularity.HOURS));
    }

    @Test
    public void equalsAndHashCodeWorkOk() {
        LocalDate today = new LocalDate();
        LocalDate tomorrow = today.plusDays(1);
        EffortDuration oneHour = EffortDuration.hours(1);
        EffortDuration halfHour = EffortDuration.minutes(30);
        assertEquals(IntraDayDate.create(today, halfHour),
                IntraDayDate.create(tomorrow.minusDays(1), halfHour));
        assertEquals(IntraDayDate.create(today, halfHour).hashCode(), IntraDayDate
                .create(tomorrow.minusDays(1), halfHour).hashCode());
        assertThat(IntraDayDate.create(today, halfHour),
                not(equalTo(IntraDayDate.create(today, oneHour))));
    }

    @Test
    public void canKnowIfAreSameDay() {
        assertTrue(IntraDayDate.create(today, halfHour).areSameDay(today));
        assertTrue(IntraDayDate.create(today, oneHour).areSameDay(today));
        assertFalse(IntraDayDate.create(today, halfHour).areSameDay(tomorrow));
        assertFalse(IntraDayDate.create(tomorrow, halfHour).areSameDay(today));
    }

    @Test
    public void canGetDateTimeAtStartOfDay() {
        DateTime dateTime = IntraDayDate.create(today, halfHour)
                .toDateTimeAtStartOfDay();
        assertThat(dateTime, equalTo(today.toDateTimeAtStartOfDay()));
    }

    @Test
    public void implementsComparable() {
        assertTrue(Comparable.class.isAssignableFrom(IntraDayDate.class));
        assertTrue(IntraDayDate.create(today, halfHour).compareTo(
                IntraDayDate.create(today, oneHour)) < 0);
        assertTrue(IntraDayDate.create(today, oneHour).compareTo(
                IntraDayDate.create(tomorrow, halfHour)) < 0);
    }

    @Test
    public void hasMaxAndMinMethods() {
        IntraDayDate a = IntraDayDate.create(today, halfHour);
        IntraDayDate b = IntraDayDate.create(today, oneHour);
        assertThat(IntraDayDate.min(a, b), equalTo(a));
        assertThat(IntraDayDate.max(a, b), equalTo(b));
    }
}
