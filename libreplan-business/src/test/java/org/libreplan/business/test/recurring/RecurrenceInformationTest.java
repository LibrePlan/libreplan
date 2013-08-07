package org.libreplan.business.test.recurring;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.libreplan.business.recurring.RecurrenceInformation;
import org.libreplan.business.recurring.RecurrencePeriodicity;

public class RecurrenceInformationTest {

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

}
