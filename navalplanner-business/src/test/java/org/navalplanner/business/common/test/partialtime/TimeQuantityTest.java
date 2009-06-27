package org.navalplanner.business.common.test.partialtime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.navalplanner.business.common.partialtime.TimeQuantity;
import org.navalplanner.business.common.partialtime.PartialDate.Granularity;

public class TimeQuantityTest {

    @Test
    public void aEmptyTimeQuantityHasAValueOfZeroForAllGranularities() {
        TimeQuantity empty = TimeQuantity.empty();
        for (Granularity granularity : Granularity.values()) {
            assertThat(empty.valueFor(granularity), equalTo(0));
        }
    }

    @Test
    public void aTimeQuantityIsComposedOfIntengersAssociatedToSeveralGranularities() {
        TimeQuantity timeQuantity = TimeQuantity.empty().plus(2,
                Granularity.MONTH).plus(-1, Granularity.DAY);
        assertThat(timeQuantity.valueFor(Granularity.MONTH), equalTo(2));
        assertThat(timeQuantity.valueFor(Granularity.DAY), equalTo(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void theGranularityMustBeNotNull() {
        TimeQuantity.empty().plus(3, null);
    }

    @Test
    public void severalPlusOnTheSameGranularityAccumulateTheValue() {
        TimeQuantity timeQuantity = TimeQuantity.empty().plus(2,
                Granularity.MONTH).plus(4, Granularity.DAY).plus(3,
                Granularity.MONTH);
        assertThat(timeQuantity.valueFor(Granularity.MONTH), equalTo(5));
    }

    @Test
    public void isInmutable() {
        TimeQuantity initial = TimeQuantity.empty();
        TimeQuantity modified = initial.plus(3, Granularity.HOUR);
        assertNotSame(initial, modified);
    }

    @Test
    public void theAmountCanBeZero() {
        TimeQuantity.empty().plus(0, Granularity.MONTH);
    }

    @Test
    public void theAmountCanBeNegative() {
        TimeQuantity.empty().plus(-1, Granularity.MONTH);
    }

    @Test
    public void timeQuantitiesCanBeAdded() {
        TimeQuantity one = TimeQuantity.empty().plus(2, Granularity.MONTH)
                .plus(4, Granularity.DAY);
        TimeQuantity another = TimeQuantity.empty().plus(3, Granularity.YEAR)
                .plus(2, Granularity.DAY);
        TimeQuantity result = one.plus(another);
        assertThat(result.valueFor(Granularity.MONTH), equalTo(2));
        assertThat(result.valueFor(Granularity.DAY), equalTo(6));
        assertThat(result.valueFor(Granularity.YEAR), equalTo(3));
        assertThat(result.valueFor(Granularity.MINUTE), equalTo(0));
    }

    @Test
    public void canSubstractUsingNegativeIntegers() {
        TimeQuantity one = TimeQuantity.empty().plus(2, Granularity.MONTH)
                .plus(4, Granularity.DAY);
        TimeQuantity another = TimeQuantity.empty().plus(3, Granularity.YEAR)
                .plus(-5, Granularity.DAY);
        TimeQuantity result = one.plus(another);
        assertThat(result.valueFor(Granularity.MONTH), equalTo(2));
        assertThat(result.valueFor(Granularity.DAY), equalTo(-1));
        assertThat(result.valueFor(Granularity.YEAR), equalTo(3));
        assertThat(result.valueFor(Granularity.MINUTE), equalTo(0));
    }

    @Test
    public void timeQuantitiesDontOverflowIntoLargerGranularities() {
        TimeQuantity quantity = TimeQuantity.empty()
                .plus(13, Granularity.MONTH);
        assertThat(quantity.valueFor(Granularity.MONTH), equalTo(13));
        assertThat(quantity.valueFor(Granularity.YEAR), equalTo(0));
    }

    @Test
    public void twoTimeQuantitiesAreEqualsIfHaveTheSameValues() {
        assertEquals(TimeQuantity.empty(), TimeQuantity.empty());
        assertEquals(TimeQuantity.empty().plus(3, Granularity.HOUR),
                TimeQuantity.empty().plus(3, Granularity.HOUR));
        assertEquals(TimeQuantity.empty().plus(3, Granularity.HOUR),
                TimeQuantity.empty().plus(2, Granularity.HOUR).plus(1,
                        Granularity.HOUR));
        assertThat(TimeQuantity.empty().plus(2, Granularity.DAY),
                not(equalTo(TimeQuantity.empty().plus(1, Granularity.DAY))));
    }

    @Test
    public void equalsWorksWellWithZeroValues() {
        TimeQuantity quantity = TimeQuantity.empty().plus(2, Granularity.MONTH)
                .plus(-2, Granularity.MONTH);
        assertEquals(quantity.hashCode(), TimeQuantity.empty().hashCode());
        assertEquals(quantity, TimeQuantity.empty());
        assertEquals(quantity, TimeQuantity.empty().plus(0, Granularity.MONTH));
    }

    @Test
    public void theGreatestGranularitySpecifiedCanBeKnown() {
        TimeQuantity timeQuantity = TimeQuantity.empty().plus(2,
                Granularity.MONTH).plus(4, Granularity.DAY);
        assertThat(timeQuantity.getGreatestGranularitySpecified(),
                equalTo(Granularity.DAY));
    }

    @Test
    public void theGranularityOfAnEmptyTimeQuantityIsYear() {
        TimeQuantity empty = TimeQuantity.empty();
        assertThat(empty.getGreatestGranularitySpecified(),
                equalTo(Granularity.YEAR));
    }
}
