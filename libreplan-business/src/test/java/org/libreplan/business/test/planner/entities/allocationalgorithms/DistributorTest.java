package org.libreplan.business.test.planner.entities.allocationalgorithms;

import static org.junit.Assert.assertThat;
import static org.libreplan.business.workingday.EffortDuration.hours;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.planner.entities.allocationalgorithms.Distributor;
import org.libreplan.business.workingday.EffortDuration;

public class DistributorTest {

    @Test
    public void theEffortIsDistributedEvenly() {
        Distributor distributor = Distributor.among(Capacity.create(hours(8)),
                Capacity.create(hours(8)));

        assertThat(distributor.distribute(hours(16)),
                hasEfforts(hours(8), hours(8)));
        assertThat(distributor.distribute(hours(8)),
                hasEfforts(hours(4), hours(4)));
    }

    @Test
    public void ifNoOverassignationAllowedNotAllIsDistributed() {
        Distributor distributor = Distributor.among(Capacity.create(hours(8))
                .notOverAssignableWithoutLimit(), Capacity.create(hours(8))
                .notOverAssignableWithoutLimit());

        assertThat(distributor.distribute(hours(18)),
                hasEfforts(hours(8), hours(8)));
    }

    @Test
    public void theOverAssignableCapacityGetsTheRest() {
        Distributor distributor = Distributor.among(Capacity.create(hours(8))
                .notOverAssignableWithoutLimit(), Capacity.create(hours(8))
                .overAssignableWithoutLimit());

        assertThat(distributor.distribute(hours(14)),
                hasEfforts(hours(7), hours(7)));
        assertThat(distributor.distribute(hours(16)),
                hasEfforts(hours(8), hours(8)));

        assertThat(distributor.distribute(hours(18)),
                hasEfforts(hours(8), hours(10)));
    }

    @Test
    public void mixingNotOverAssignableAndOverassignableToALimit() {
        Distributor distributor = Distributor.among(Capacity.create(hours(8))
                .withAllowedExtraEffort(hours(2)), Capacity.create(hours(8))
                .notOverAssignableWithoutLimit());

        assertThat(distributor.distribute(hours(16)),
                hasEfforts(hours(8), hours(8)));
        assertThat(distributor.distribute(hours(17)),
                hasEfforts(hours(9), hours(8)));
        assertThat(distributor.distribute(hours(18)),
                hasEfforts(hours(10), hours(8)));
        assertThat(distributor.distribute(hours(19)),
                hasEfforts(hours(10), hours(8)));
    }

    @Test
    public void ifNoCapacityItReturnsZeroHours() {
        Distributor distributor = Distributor.among(Capacity.create(hours(0))
                .notOverAssignableWithoutLimit());
        assertThat(distributor.distribute(hours(4)), hasEfforts(hours(0)));
    }

    private Matcher<List<EffortDuration>> hasEfforts(
            final EffortDuration... efforts) {
        return new BaseMatcher<List<EffortDuration>>() {

            @Override
            public boolean matches(Object arg) {
                return Arrays.equals(efforts, toArray(arg));
            }

            private EffortDuration[] toArray(Object value) {
                if (value instanceof EffortDuration[]) {
                    return (EffortDuration[]) value;
                }
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    return list.toArray(new EffortDuration[0]);
                }
                return null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(Arrays.toString(efforts));
            }
        };
    }

}
