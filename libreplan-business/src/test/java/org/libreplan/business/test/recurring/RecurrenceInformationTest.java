package org.libreplan.business.test.recurring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.EnumSet;
import java.util.List;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePeriod;
import org.junit.Test;
import org.libreplan.business.planner.entities.ResourceAllocation.Direction;
import org.libreplan.business.recurring.RecurrenceInformation;
import org.libreplan.business.recurring.RecurrencePeriodicity;

public class RecurrenceInformationTest {

    private final LocalDate start = new LocalDate(2013, 8, 8);

    private final EnumSet<RecurrencePeriodicity> recurrentPeriods;

    public RecurrenceInformationTest() {
        recurrentPeriods = EnumSet.allOf(RecurrencePeriodicity.class);
        recurrentPeriods.remove(RecurrencePeriodicity.NO_PERIODICTY);
    }

    @Test
    public void equalsAndHashCodeBasedOnContentsNotIdentity() {
        RecurrenceInformation r1 = new RecurrenceInformation(2,
                RecurrencePeriodicity.MONTHLY, 1);
        RecurrenceInformation r2 = new RecurrenceInformation(2,
                RecurrencePeriodicity.MONTHLY, 1);

        RecurrenceInformation other = new RecurrenceInformation(1,
                RecurrencePeriodicity.MONTHLY, 1);
        RecurrenceInformation other2 = new RecurrenceInformation(2,
                RecurrencePeriodicity.MONTHLY, 2);

        assertThat(r1, equalTo(r2));
        assertThat(r1, not(equalTo(other)));
        assertThat(r1, not(equalTo(other2)));

        assertThat(r1.hashCode(), equalTo(r2.hashCode()));
    }

    @Test
    public void withNoPeriodicityItHasZeroRepetitions() {
        RecurrenceInformation r = new RecurrenceInformation(10,
                RecurrencePeriodicity.NO_PERIODICTY, 2);
        assertThat(r.getRepetitions(), equalTo(0));
        assertThat(r.getAmountOfPeriodsPerRepetition(), equalTo(0));
    }


    @Test
    public void ifNoPeriodicityNoDates() {
        RecurrenceInformation r = RecurrenceInformation.noRecurrence();
        assertTrue(r.getRecurrences(Direction.FORWARD, start).isEmpty());
    }

    @Test
    public void withOneRepetitionMovingForward() {
        int[] amounts = { 1, 2, 3 };
        for (int amount : amounts) {
            for (RecurrencePeriodicity each : recurrentPeriods) {
                RecurrenceInformation r = new RecurrenceInformation(1, each,
                        amount);
                List<LocalDate> recurrences = r.getRecurrences(
                        Direction.FORWARD, start);
                assertThat(recurrences.size(), equalTo(1));
                ReadablePeriod p = each.getPeriod();
                assertThat(recurrences.get(0),
                        equalTo(plusTimes(start, p, amount)));
            }
        }
    }

    private LocalDate plusTimes(LocalDate date, ReadablePeriod p, int amount) {
        LocalDate result = date;
        for (int i = 0; i < amount; i++) {
            result = result.plus(p);
        }
        return result;
    }

    @Test
    public void withOneRepetitionMovingBackward() {
        int[] amounts = { 1, 2, 3 };
        for (int amount : amounts) {
            for (RecurrencePeriodicity each : recurrentPeriods) {
                RecurrenceInformation r = new RecurrenceInformation(1, each,
                        amount);
                List<LocalDate> recurrences = r.getRecurrences(
                        Direction.BACKWARD, start);
                assertThat(recurrences.size(), equalTo(1));
                ReadablePeriod p = each.getPeriod();
                assertThat(recurrences.get(0),
                        equalTo(minusTimes(start, p, amount)));
            }
        }
    }

    private LocalDate minusTimes(LocalDate date, ReadablePeriod p, int amount) {
        LocalDate result = date;
        for (int i = 0; i < amount; i++) {
            result = result.minus(p);
        }
        return result;
    }

    @Test
    public void testSeveralRepetitions() {
        int[] repetitions = { 2, 3, 4 };
        for (int repetition : repetitions) {
            for (RecurrencePeriodicity each : recurrentPeriods) {
                RecurrenceInformation r = new RecurrenceInformation(repetition,
                        each, 1);
                List<LocalDate> recurrences = r.getRecurrences(
                        Direction.FORWARD, start);
                assertThat(recurrences.size(), equalTo(repetition));

                int i = 1;
                for (LocalDate d : recurrences) {
                    assertThat(d,
                            equalTo(plusTimes(start, each.getPeriod(), i)));
                    i++;
                }
            }
        }
    }

    @Test
    public void weeklyOrMonthlyRecurrenceCanHaveRepeatOnDayInformation() {
        EnumSet<RecurrencePeriodicity> supportRepeatOnDay = EnumSet.of(
                RecurrencePeriodicity.WEEKLY, RecurrencePeriodicity.MONTHLY);

        for (RecurrencePeriodicity p : recurrentPeriods) {
            RecurrenceInformation r = new RecurrenceInformation(1, p, 1);
            boolean supported = supportRepeatOnDay.contains(p);
            RecurrenceInformation other;
            try {
                other = r.repeatOnDay(2);
                if (!supported) {
                    fail("it should send exception");
                }
            } catch (IllegalArgumentException e) {
                if (supported) {
                    fail("it shouldn't send exception when supported");
                }
                continue;
            }
            assertTrue(other != r);
            assertThat(other, not(equalTo(r)));
            assertThat(other.hashCode(), not(equalTo(r.hashCode())));
        }

    }

    @Test
    public void monthlySupportsRepeatsOnDaysFrom1To31() {
        RecurrenceInformation r = new RecurrenceInformation(1,
                RecurrencePeriodicity.MONTHLY, 1);
        for (int i = 1; i <= 31; i++) {
            RecurrenceInformation other = r.repeatOnDay(i);
            assertThat(other, not(equalTo(r)));
        }
    }

    @Test
    public void monthlySupportDoesntSupportMoreThan31AndLessThanOne() {
        RecurrenceInformation r = new RecurrenceInformation(1,
                RecurrencePeriodicity.MONTHLY, 1);
        int[] days = { -2, -1, 0, 32, 33, 1000 };
        for (int day : days) {
            try {
                r.repeatOnDay(day);
                fail("It should fail");
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }

    @Test
    public void weeklySupportDoesntSupportMoreThan7AndLessThanOne() {
        RecurrenceInformation r = new RecurrenceInformation(1,
                RecurrencePeriodicity.WEEKLY, 1);
        int[] days = { -2, -1, 0, 8, 9, 1000 };
        for (int day : days) {
            try {
                r.repeatOnDay(day);
                fail("It should fail");
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }


    @Test
    public void itRespectsOnDayForWeeks() {
        LocalDate wednesday = new LocalDate(2013, 9, 11);
        int dayOfWeek = wednesday.getDayOfWeek();
        assertThat(dayOfWeek, equalTo(DateTimeConstants.WEDNESDAY));

        RecurrenceInformation r = new RecurrenceInformation(2,
                RecurrencePeriodicity.WEEKLY, 1);

        // repeat on monday
        r = r.repeatOnDay(1);

        List<LocalDate> recurrences = r.getRecurrences(
                Direction.FORWARD, wednesday);
        assertThat(recurrences.size(), equalTo(2));
        assertThat(recurrences.get(0),
                equalTo(wednesday.dayOfMonth().setCopy(16)));
        assertThat(recurrences.get(0).getDayOfWeek(), equalTo(1));

        assertThat(recurrences.get(1),
                equalTo(wednesday.dayOfMonth().setCopy(23)));
        assertThat(recurrences.get(1).getDayOfWeek(), equalTo(1));
    }

    @Test
    public void itRespectsOnDayForMonths() {
        LocalDate start = new LocalDate(2013, 9, 11);

        RecurrenceInformation r = new RecurrenceInformation(2,
                RecurrencePeriodicity.MONTHLY, 1);

        final int day = 20;
        r = r.repeatOnDay(day);
        List<LocalDate> recurrences = r
                .getRecurrences(Direction.FORWARD, start);

        assertThat(recurrences.size(), equalTo(2));
        assertThat(recurrences.get(0), equalTo(new LocalDate(2013, 10, day)));
        assertThat(recurrences.get(1), equalTo(new LocalDate(2013, 11, day)));
    }

    @Test
    public void ifTheMonthDoesntHaveThatDayItGoesToTheTopmostOne() {
        LocalDate start = new LocalDate(2013, 1, 11);

        final int numberOfRecurrences = 3;
        RecurrenceInformation r = new RecurrenceInformation(
                numberOfRecurrences,
                RecurrencePeriodicity.MONTHLY, 1);

        final int day = 31;
        r = r.repeatOnDay(day);
        List<LocalDate> recurrences = r
                .getRecurrences(Direction.FORWARD, start);

        assertThat(recurrences.size(), equalTo(numberOfRecurrences));
        assertThat(recurrences.get(0), equalTo(new LocalDate(2013, 2, 28)));
        assertThat(recurrences.get(1), equalTo(new LocalDate(2013, 3, 31)));
        assertThat(recurrences.get(2), equalTo(new LocalDate(2013, 4, 30)));
    }

}
