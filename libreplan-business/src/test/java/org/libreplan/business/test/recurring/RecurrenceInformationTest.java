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
                RecurrencePeriodicity.MONTHLY);
        RecurrenceInformation r2 = new RecurrenceInformation(2,
                RecurrencePeriodicity.MONTHLY);

        RecurrenceInformation other = new RecurrenceInformation(1,
                RecurrencePeriodicity.MONTHLY);

        assertThat(r1, equalTo(r2));
        assertThat(r1, not(equalTo(other)));

        assertThat(r1.hashCode(), equalTo(r2.hashCode()));
    }

    @Test
    public void withNoPeriodicityItHasZeroRepetitions() {
        RecurrenceInformation r = new RecurrenceInformation(10,
                RecurrencePeriodicity.NO_PERIODICTY);
        assertThat(r.getRepetitions(), equalTo(0));
    }

}
