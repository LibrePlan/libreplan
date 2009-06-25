package org.navalplanner.business.test.partialtime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.navalplanner.business.partialtime.IntervalOfPartialDates;
import org.navalplanner.business.partialtime.PartialDate;
import org.navalplanner.business.partialtime.TimeQuantity;
import org.navalplanner.business.partialtime.PartialDate.Granularity;

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
