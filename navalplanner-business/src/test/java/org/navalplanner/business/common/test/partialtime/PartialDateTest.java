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

package org.navalplanner.business.common.test.partialtime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePartial;
import org.junit.Test;
import org.navalplanner.business.common.partialtime.IntervalOfPartialDates;
import org.navalplanner.business.common.partialtime.PartialDate;
import org.navalplanner.business.common.partialtime.TimeQuantity;
import org.navalplanner.business.common.partialtime.PartialDate.Granularity;

public class PartialDateTest {

    @Test
    public void defaultTimeUnitIsMilliseconds() throws Exception {
        Date date = new Date();
        PartialDate partialDate = PartialDate.createFrom(date);
        assertThat(partialDate.getGranularity(),
                equalTo(Granularity.MILLISECONDS));
    }

    @Test
    public void isAReadablePartial() {
        assertTrue(PartialDate.createFrom(new Date()) instanceof ReadablePartial);
    }

    @Test
    public void canBeCreatedFromJavaUtilDate() {
        Date now = new Date();
        PartialDate partial = PartialDate.createFrom(now).with(Granularity.DAY);
        Granularity unit = partial.getGranularity();
        assertThat(unit, equalTo(Granularity.DAY));
    }

    @Test
    public void canBeCreatedFromJodaDateTime() {
        LocalDate date = new LocalDate(2000, 10, 12);
        DateTime dateTimeAtCurrentTime = date.toDateTimeAtCurrentTime();
        PartialDate partial = PartialDate.createFrom(dateTimeAtCurrentTime)
                .with(Granularity.DAY);
        Granularity unit = partial.getGranularity();
        assertThat(unit, equalTo(Granularity.DAY));
        assertThat(partial.get(DateTimeFieldType.dayOfMonth()), equalTo(12));
    }

    @Test
    public void canBeCreatedFromLocalDateAndPutsGranularityAsDay() {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partial = PartialDate.createFrom(date);
        assertThat(partial.getGranularity(), equalTo(Granularity.DAY));
        check(partial, 2000, 10, 12);
    }

    @Test
    public void granularityOfDayAndMoreFineGrainedCanBeConvertedToLocalDate() {
        Instant now = new Instant();
        LocalDate today = new LocalDate();
        Granularity[] supportingConversionToLocalDate = Granularity.DAY
                .thisAndMoreFineGrained();
        for (Granularity g : supportingConversionToLocalDate) {
            PartialDate partialDate = PartialDate.createFrom(now).with(g);
            assertTrue(partialDate.canBeConvertedToLocalDate());
            LocalDate converted = partialDate.tryToConvertToLocalDate();
            assertThat(converted, equalTo(today));
        }
        Granularity[] notSupportingConversionToLocalDate = Granularity.DAY
                .moreCoarseGrained();
        for (Granularity g : notSupportingConversionToLocalDate) {
            PartialDate partialDate = PartialDate.createFrom(now).with(g);
            assertFalse(partialDate.canBeConvertedToLocalDate());
            try {
                partialDate.tryToConvertToLocalDate();
                fail("the partial date cannot be converted to a LocalDate");
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }

    @Test
    public void availableFieldsDependOnTimeUnit() throws Exception {
        Date now = new Date();
        for (Granularity timeUnit : Granularity.values()) {
            PartialDate partial = PartialDate.createFrom(now).with(timeUnit);
            DateTimeFieldType types[] = timeUnit.getDateTimeTypes();
            for (int i = 0; i < types.length; i++) {
                assertThat(partial.getFieldType(i), equalTo(types[i]));
            }
        }
    }

    @Test
    public void canAccessFieldsUsingGranularity() {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partialDate = PartialDate.createFrom(date.toDateMidnight()
                .toDate());
        assertThat(partialDate.valueFor(Granularity.MINUTE), equalTo(0));
        assertThat(partialDate.valueFor(Granularity.YEAR), equalTo(2000));
        assertThat(partialDate.valueFor(Granularity.DAY), equalTo(12));
    }

    @Test
    public void cannotAccessFieldsOfMoreSpecificGranularity() {
        PartialDate partialDate = PartialDate.createFrom(
                new LocalDate(2000, 10, 12)).with(Granularity.MONTH);
        Granularity[] moreSpecific = Granularity.MONTH.thisAndMoreFineGrained();
        for (int i = 1; i < moreSpecific.length; i++) {
            try {
                partialDate.valueFor(moreSpecific[i]);
                fail("must send IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }

    @Test
    public void retainsValues() throws Exception {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partialDate = PartialDate.createFrom(
                date.toDateMidnight().toDate()).with(Granularity.DAY);
        check(partialDate, 2000, 10, 12);
    }

    private void check(PartialDate partialDate, int year, int month, int day) {
        assertThat(partialDate.get(DateTimeFieldType.year()), equalTo(year));
        assertThat(partialDate.get(DateTimeFieldType.monthOfYear()),
                equalTo(month));
        assertThat(partialDate.get(DateTimeFieldType.dayOfMonth()),
                equalTo(day));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dontSupportAllTypes() {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partialDate = PartialDate.createFrom(
                date.toDateMidnight().toDate()).with(Granularity.DAY);
        partialDate.get(DateTimeFieldType.minuteOfHour());
    }

    @Test
    public void canBeConvertedToMoreCoarseGrained() {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partialDate = PartialDate.createFrom(
                date.toDateMidnight().toDate()).with(Granularity.DAY);
        PartialDate onlyUntilMonthSpecified = partialDate
                .with(Granularity.MONTH);
        assertThat(
                onlyUntilMonthSpecified.get(DateTimeFieldType.monthOfYear()),
                equalTo(10));
        try {
            onlyUntilMonthSpecified.get(DateTimeFieldType.dayOfMonth());
            fail("must send exception since the day is lost in the conversion");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    @Test
    public void thereIsQueryMethodToKnowIfItCanBeConverted() {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partialDate = PartialDate.createFrom(
                date.toDateMidnight().toDate()).with(Granularity.DAY);
        Granularity[] moreCoarseGrained = { Granularity.YEAR,
                Granularity.MONTH, Granularity.DAY };
        for (Granularity t : moreCoarseGrained) {
            assertTrue(partialDate.canBeConvertedTo(t));
        }
        Granularity[] moreFineGrained = { Granularity.HOUR, Granularity.MINUTE,
                Granularity.SECOND, Granularity.MILLISECONDS };
        for (Granularity t : moreFineGrained) {
            assertFalse(partialDate.canBeConvertedTo(t));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotBeConvertedToMoreFineGrained() {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partialDate = PartialDate.createFrom(
                date.toDateMidnight().toDate()).with(Granularity.DAY);
        partialDate.with(Granularity.HOUR);
    }

    @Test
    public void onlyPartialDatesOfTheSameGranuralityCanBeEqual() {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partialDate = PartialDate.createFrom(
                date.toDateTimeAtStartOfDay()).with(Granularity.DAY);
        PartialDate otherOnSameInstantWithDifferentGranularity = PartialDate
                .createFrom(date.toDateTimeAtStartOfDay()).with(
                        Granularity.MILLISECONDS);
        assertThat(partialDate,
                not(equalTo(otherOnSameInstantWithDifferentGranularity)));
    }

    @Test
    public void equalsAndHashCodeBasedOnTheValuesForCurrentGranularity() {
        LocalDate localDate = new LocalDate(2000, 10, 12);
        DateTime dateTime = localDate.toDateTimeAtStartOfDay();
        PartialDate partial = PartialDate.createFrom(localDate).with(
                Granularity.DAY);
        PartialDate other = PartialDate.createFrom(dateTime).with(
                Granularity.DAY);
        int partialHashCode = partial.hashCode();
        int otherHashCode = other.hashCode();
        assertThat(partialHashCode, equalTo(otherHashCode));
        assertThat(partial, equalTo(other));
    }

    @Test
    public void forTwoEqualsPartialsChangingGranularityTheSameWayKeepsEquality() {
        DateTime dateTime = new LocalDate(2000, 10, 12)
                .toDateTimeAtStartOfDay();
        PartialDate partial1 = PartialDate.createFrom(dateTime).with(
                Granularity.MILLISECONDS);
        PartialDate partial2 = PartialDate.createFrom(dateTime).with(
                Granularity.MILLISECONDS);
        assertEquals(partial1, partial2);
        Granularity[] granularities = Granularity.values();
        for (int i = granularities.length - 1; i >= 0; i--) {
            partial1 = partial1.with(granularities[i]);
            partial2 = partial2.with(granularities[i]);
            assertEquals(partial1, partial2);
        }
    }

    @Test
    public void hasBeforeAndAfterMethod() {
        PartialDate beforePartialDate = PartialDate.createFrom(new LocalDate(
                2000, 9, 12));
        PartialDate posteriorPartialDate = PartialDate
                .createFrom(new LocalDate(2000, 10, 1));
        assertTrue(beforePartialDate.before(posteriorPartialDate));
        assertFalse(posteriorPartialDate.before(beforePartialDate));
        assertFalse(posteriorPartialDate.before(posteriorPartialDate));
        assertFalse(beforePartialDate.before(beforePartialDate));
        assertTrue(posteriorPartialDate.after(beforePartialDate));
        assertFalse(beforePartialDate.after(posteriorPartialDate));
        assertFalse(posteriorPartialDate.after(posteriorPartialDate));
        assertFalse(beforePartialDate.after(beforePartialDate));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantUseBeforeWithPartialsOfDifferentGranularity() {
        PartialDate p = PartialDate.createFrom(new LocalDate(2000, 9, 12));
        PartialDate withDifferentGranularity = p.with(Granularity.MONTH);
        p.before(withDifferentGranularity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantUseAfterWithPartialsOfDifferentGranularity() {
        PartialDate p = PartialDate.createFrom(new LocalDate(2000, 9, 12));
        PartialDate withDifferentGranularity = p.with(Granularity.MONTH);
        p.after(withDifferentGranularity);
    }

    @Test
    public void canAddATimeQuantity() {
        PartialDate partialDate = PartialDate.createFrom(new LocalDate(2009, 8,
                4));
        assertThat(partialDate.plus(
                TimeQuantity.empty().plus(3, Granularity.MONTH))
                .tryToConvertToLocalDate().getMonthOfYear(), equalTo(11));
        assertThat(partialDate.plus(
                TimeQuantity.empty().plus(3, Granularity.DAY))
                .tryToConvertToLocalDate().getDayOfMonth(), equalTo(7));
        assertThat(partialDate.plus(
                TimeQuantity.empty().plus(4, Granularity.MONTH))
                .tryToConvertToLocalDate().getMonthOfYear(), equalTo(12));
        PartialDate overflowedToYear = partialDate.plus(TimeQuantity.empty()
                .plus(5, Granularity.MONTH));
        assertThat(overflowedToYear.tryToConvertToLocalDate().getMonthOfYear(),
                equalTo(1));
        assertThat(overflowedToYear.tryToConvertToLocalDate().getYear(),
                equalTo(2010));
        assertThat(partialDate.plus(
                TimeQuantity.empty().plus(-1, Granularity.DAY))
                .tryToConvertToLocalDate().getDayOfMonth(), equalTo(3));
    }

    @Test
    public void canAddTimeQuantityWithMillisecondsGranularity() {
        PartialDate partialDate = PartialDate.createFrom(new LocalDate(2009, 8,
                4).toDateTimeAtStartOfDay());
        assertThat(partialDate.valueFor(Granularity.MILLISECONDS), equalTo(0));
        PartialDate sum = partialDate.plus(TimeQuantity.empty().plus(4,
                Granularity.MILLISECONDS));
        assertThat(sum.valueFor(Granularity.MILLISECONDS), equalTo(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theTimeQuantityMustNotHaveGreaterGranularityThanThePartialDate() {
        PartialDate partialDate = PartialDate.createFrom(new LocalDate(2009, 8,
                4));
        partialDate.plus(TimeQuantity.empty().plus(3, Granularity.HOUR));
    }

    @Test
    public void canCreateIntervalsUsingQuantities() {
        PartialDate start = PartialDate.createFrom(new LocalDate(2009, 8, 4));
        IntervalOfPartialDates interval = start.to(TimeQuantity.empty().plus(3,
                Granularity.MONTH));
        assertThat(
                interval.getEnd().tryToConvertToLocalDate().getMonthOfYear(),
                equalTo(11));
    }

}
