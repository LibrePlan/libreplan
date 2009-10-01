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

package org.navalplanner.business.common.test.partialtime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.common.partialtime.IntervalOfPartialDates;
import org.navalplanner.business.common.partialtime.PartialDate;
import org.navalplanner.business.common.partialtime.TimeQuantity;
import org.navalplanner.business.common.partialtime.PartialDate.Granularity;

public class IntervalOfPartialDatesTest {

    private PartialDate start = PartialDate.createFrom(new LocalDate(2006, 10, 2));
    private PartialDate end = PartialDate.createFrom(new LocalDate(2006, 11, 5));
    private IntervalOfPartialDates interval = new IntervalOfPartialDates(start,
            end);

    @Test
    public void isComposedFromTwoPartialDates() {
        assertThat(interval.getStart(), equalTo(start));
        assertThat(interval.getEnd(), equalTo(end));
    }

    @Test(expected = IllegalArgumentException.class)
    public void thePartialDatesMustBeOfTheSameGranularity() {
        new IntervalOfPartialDates(start, end.with(Granularity.MONTH));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theFromMustBeBeforeTo() {
        new IntervalOfPartialDates(end, start);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateEmptyInterval() {
        PartialDate.createFrom(new LocalDate(2006, 11, 2)).to(
                PartialDate.createFrom(new LocalDate(2006, 11, 2)));
    }

    @Test
    public void canBeCreatedFromPartialDate() {
        PartialDate.createFrom(new LocalDate(2006, 10, 2)).to(
                PartialDate.createFrom(new LocalDate(2006, 11, 2)));
    }

    @Test
    public void canGetTheDurationOfTheInterval() {
        TimeQuantity duration = interval.getDuration();
        assertThat(duration.valueFor(Granularity.MONTH), equalTo(1));
        assertThat(duration.valueFor(Granularity.DAY), equalTo(3));
    }

    @Test
    public void twoIntervalsAreEqualsIfTheirPartialDatesAreEquals() {
        PartialDate from = PartialDate.createFrom(new LocalDate(2006, 10, 2)
                .toDateTimeAtStartOfDay());
        IntervalOfPartialDates oneInterval = from.to(PartialDate
                .createFrom(new LocalDate(2006, 10, 3).toDateTimeAtStartOfDay()));
        IntervalOfPartialDates otherInterval = PartialDate.createFrom(
                new LocalDate(2006, 10, 2).toDateTimeAtStartOfDay()).to(
                PartialDate.createFrom(new LocalDate(2006, 10, 3)
                        .toDateTimeAtStartOfDay()));
        IntervalOfPartialDates differentInterval = from.to(PartialDate
                .createFrom(new LocalDate(2006, 10, 4).toDateTimeAtStartOfDay()));
        assertEquals(oneInterval, oneInterval);

        assertEquals(oneInterval, otherInterval);
        assertEquals(otherInterval, oneInterval);
        assertEquals(oneInterval.hashCode(), otherInterval.hashCode());

        assertFalse(oneInterval.equals(differentInterval));
        assertFalse(differentInterval.equals(oneInterval));
        assertFalse(oneInterval.hashCode() == differentInterval.hashCode());
    }

    @Test
    public void canGetTheDurationInMilliseconds() {
        DateTime startOfDay = new LocalDate(2009, 3, 8)
                .toDateTimeAtStartOfDay();
        PartialDate startOfDayPartial = PartialDate.createFrom(startOfDay);
        long millis = startOfDay.getMillis();
        PartialDate after = PartialDate.createFrom(new DateTime(millis + 3));
        PartialDate before = PartialDate.createFrom(new DateTime(millis - 3));
        IntervalOfPartialDates[] intervalsWithTheSameDuration = {
                before.to(startOfDayPartial), startOfDayPartial.to(after) };
        for (IntervalOfPartialDates interval : intervalsWithTheSameDuration) {
            TimeQuantity duration = interval.getDuration();
            PartialDate start2 = interval.getStart();
            PartialDate endCalculated = start2.plus(duration);
            PartialDate originalEnd = interval.getEnd();
            assertThat(endCalculated, equalTo(originalEnd));
        }
    }
}
