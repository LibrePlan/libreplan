/*
 * This file is part of NavalPlan
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
