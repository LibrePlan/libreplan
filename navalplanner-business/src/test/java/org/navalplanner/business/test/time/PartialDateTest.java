package org.navalplanner.business.test.time;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
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
import org.navalplanner.business.time.PartialDate;
import org.navalplanner.business.time.PartialDate.Granularity;

public class PartialDateTest {

    @Test
    public void defaultTimeUnitIsMilliseconds() throws Exception {
        Date date = new Date();
        PartialDate partialDate = PartialDate.from(date);
        assertThat(partialDate.getGranularity(), equalTo(Granularity.MILLISECONDS));
    }

    @Test
    public void isAReadablePartial() {
        assertTrue(PartialDate.from(new Date()) instanceof ReadablePartial);
    }

    @Test
    public void canBeCreatedFromJavaUtilDate() {
        Date now = new Date();
        PartialDate partial = PartialDate.from(now).with(Granularity.DAY);
        Granularity unit = partial.getGranularity();
        assertThat(unit, equalTo(Granularity.DAY));
    }

    @Test
    public void canBeCreatedFromJodaDateTime() {
        LocalDate date = new LocalDate(2000, 10, 12);
        DateTime dateTimeAtCurrentTime = date.toDateTimeAtCurrentTime();
        PartialDate partial = PartialDate.from(dateTimeAtCurrentTime).with(
                Granularity.DAY);
        Granularity unit = partial.getGranularity();
        assertThat(unit, equalTo(Granularity.DAY));
        assertThat(partial.get(DateTimeFieldType.dayOfMonth()), equalTo(12));
    }

    @Test
    public void canBeCreatedFromLocalDateAndPutsGranularityAsDay() {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partial = PartialDate.from(date);
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
            PartialDate partialDate = PartialDate.from(now).with(g);
            assertTrue(partialDate.canBeConvertedToLocalDate());
            LocalDate converted = PartialDate
                    .tryToConvertToLocalDate(partialDate);
            assertThat(converted, equalTo(today));
        }
        Granularity[] notSupportingConversionToLocalDate = Granularity.DAY
                .moreCoarseGrained();
        for (Granularity g : notSupportingConversionToLocalDate) {
            PartialDate partialDate = PartialDate.from(now).with(g);
            assertFalse(partialDate.canBeConvertedToLocalDate());
            try {
                LocalDate converted = PartialDate
                        .tryToConvertToLocalDate(partialDate);
                fail("the partial date cannot be converted to a LocalDate");
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }


    @Test
    public void fieldsDependOnTimeUnit() throws Exception {
        Date now = new Date();
        for (Granularity timeUnit : Granularity.values()) {
            PartialDate partial = PartialDate.from(now).with(timeUnit);
            DateTimeFieldType types[] = timeUnit.getDateTimeTypes();
            for (int i = 0; i < types.length; i++) {
                assertThat(partial.getFieldType(i), equalTo(types[i]));
            }
        }
    }

    @Test
    public void retainsValues() throws Exception {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partialDate = PartialDate.from(
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
        PartialDate partialDate = PartialDate.from(
                date.toDateMidnight().toDate()).with(Granularity.DAY);
        partialDate.get(DateTimeFieldType.minuteOfHour());
    }

    @Test
    public void canBeConvertedToMoreCoarseGrained() {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partialDate = PartialDate.from(
                date.toDateMidnight().toDate()).with(Granularity.DAY);
        PartialDate onlyUntilMonthSpecified = partialDate.with(Granularity.MONTH);
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
        PartialDate partialDate = PartialDate.from(
                date.toDateMidnight().toDate()).with(Granularity.DAY);
        Granularity[] moreCoarseGrained = { Granularity.YEAR, Granularity.MONTH,
                Granularity.DAY };
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
        PartialDate partialDate = PartialDate.from(
                date.toDateMidnight().toDate()).with(Granularity.DAY);
        partialDate.with(Granularity.HOUR);
    }

    @Test
    public void onlyPartialDatesOfTheSameGranuralityCanBeEqual() {
        LocalDate date = new LocalDate(2000, 10, 12);
        PartialDate partialDate = PartialDate.from(
                date.toDateMidnight()).with(
                Granularity.DAY);
        PartialDate otherOnSameInstantWithDifferentGranularity = PartialDate
                .from(date.toDateMidnight()).with(
                Granularity.MILLISECONDS);
        assertThat(partialDate,
                not(equalTo(otherOnSameInstantWithDifferentGranularity)));
    }

}