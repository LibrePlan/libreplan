/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.test.workingday;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.workingday.EffortDuration.hours;
import static org.libreplan.business.workingday.EffortDuration.seconds;
import static org.libreplan.business.workingday.EffortDuration.zero;

import java.math.BigDecimal;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.EffortDuration.Granularity;
import org.libreplan.business.workingday.ResourcesPerDay;
import org.libreplan.business.workingday.ResourcesPerDay.ResourcesPerDayDistributor;

public class ResourcesPerDayTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotHaveANegativeNumberOfUnits() {
        ResourcesPerDay.amount(-1);
    }

    @Test
    public void theUnitsAmoutCanBeRetrieved() {
        ResourcesPerDay units = ResourcesPerDay.amount(2);
        assertThat(units, readsAs(2, 0));
    }

    private Matcher<ResourcesPerDay> readsAs(final int integerPart,
            final int decimalPart) {
        return new BaseMatcher<ResourcesPerDay>() {

            @Override
            public boolean matches(Object arg) {
                if (arg instanceof ResourcesPerDay) {
                    ResourcesPerDay r = (ResourcesPerDay) arg;
                    return r.getAmount().intValue() == integerPart
                            && getDecimalPart(r) == decimalPart;
                }
                return false;
            }

            private int getDecimalPart(ResourcesPerDay r) {
                BigDecimal onlyDecimal = r.getAmount().subtract(
                        new BigDecimal(r.getAmount().intValue()));
                BigDecimal decimalPartAsInt = onlyDecimal.movePointRight(4);
                int result = decimalPartAsInt.intValue();
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("must have an integer part of "
                        + integerPart + " and ");
                description.appendText("must have " + decimalPart
                        + " as decimal part");
            }
        };
    }

    @Test
    public void theUnitsAmountCanBeADecimalValue() {
        ResourcesPerDay resourcesPerDay = ResourcesPerDay
                .amount(new BigDecimal(2.2));
        assertThat(resourcesPerDay, readsAs(2, 2000));
    }

    @Test
    public void theAmountIsConvertedToABigDecimalOfScale4() {
        ResourcesPerDay resourcesPerDay = ResourcesPerDay
                .amount(new BigDecimal(2.2));
        assertThat(resourcesPerDay.getAmount().scale(), equalTo(4));
    }

    @Test
    public void ifTheAmountSpecifiedHasBiggerScaleThan4ItIsRoundedHalfUp() {
        BigDecimal[] examples = { new BigDecimal(2.11236),
                new BigDecimal(211235).movePointLeft(5),
                new BigDecimal(2.1124), new BigDecimal(2.112449) };
        for (BigDecimal example : examples) {
            ResourcesPerDay resourcesPerDay = ResourcesPerDay.amount(example);
            assertThat(resourcesPerDay.getAmount().scale(), equalTo(4));
            assertThat(resourcesPerDay, readsAs(2, 1124));
        }
    }

    @Test
    public void canBeConvertedToDurationsGivenTheWorkingDayInDifferentGranularities() {
        ResourcesPerDay units = ResourcesPerDay.amount(2);
        for (Granularity each : Granularity.values()) {
            assertThat(units.asDurationGivenWorkingDayOf(EffortDuration
                    .elapsing(8, each)), equalTo(EffortDuration.elapsing(16,
                    each)));
        }
    }

    @Test
    public void ifTheAmountIsDecimalTheSecondsAreMultiplied() {
        ResourcesPerDay resourcesPerDay = ResourcesPerDay
                .amount(new BigDecimal(2.4));
        assertThat(resourcesPerDay.asDurationGivenWorkingDayOf(hours(8)),
                equalTo(hours(19).and(12, Granularity.MINUTES)));
        assertThat(resourcesPerDay.asDurationGivenWorkingDayOf(hours(10)),
                equalTo(hours(24)));
        assertThat(resourcesPerDay.asDurationGivenWorkingDayOf(hours(2)),
                equalTo(hours(4).and(48, Granularity.MINUTES)));
    }

    @Test
    public void theSecondsAreRoundedHalfUpUnlessItIsMinusThanOneSecond() {
        ResourcesPerDay resourcesPerDay = ResourcesPerDay
                .amount(new BigDecimal(2.4));
        assertThat(resourcesPerDay.asDurationGivenWorkingDayOf(seconds(1)),
                equalTo(seconds(2)));
        assertThat(resourcesPerDay.asDurationGivenWorkingDayOf(seconds(2)),
                equalTo(seconds(5)));
    }

    @Test
    public void asSecondsMustReturnOneIfResultingAmountFromMultiplicationIsGreaterThanZero() {
        ResourcesPerDay resourcesPerDay = ResourcesPerDay
                .amount(new BigDecimal(0.1));
        assertThat(resourcesPerDay.asDurationGivenWorkingDayOf(seconds(1)),
                equalTo(seconds(1)));
    }

    @Test
    public void twoResourcesPerDayAreEqualsIfNormalizeToTheSameAmount() {
        ResourcesPerDay a = ResourcesPerDay.amount(new BigDecimal(2.00001));
        ResourcesPerDay b = ResourcesPerDay.amount(2);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, b);
    }

    @Test
    public void ifTheAmountIsZeroMustReturnZero() {
        ResourcesPerDay amount = ResourcesPerDay.amount(BigDecimal.ZERO);
        EffortDuration result = amount.asDurationGivenWorkingDayOf(hours(8));
        assertThat(result, equalTo(zero()));
    }

    @Test
    public void isZeroIfHaveZeroValue() {
        BigDecimal[] examples = { new BigDecimal(0.00001), new BigDecimal(0),
                new BigDecimal(00), new BigDecimal(0.00) };
        for (BigDecimal example : examples) {
            assertTrue(ResourcesPerDay.amount(example).isZero());
        }
    }

    @Test
    public void notZeroIfNoZeroValue() {
        BigDecimal[] examples = { new BigDecimal(0.0001),
                new BigDecimal(0.00009), new BigDecimal(1),
                new BigDecimal(0.1000) };
        for (BigDecimal example : examples) {
            assertFalse(ResourcesPerDay.amount(example).isZero());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void canCalculateTheResourcesPerDayFromTheWorkingEffortAndTheWorkableEffort() {
        Object[] periodicalNumber = {
                ResourcesPerDay.calculateFrom(seconds(10), seconds(3)),
                readsAs(3, 3333) };
        Object[][] examples = {
                { ResourcesPerDay.calculateFrom(seconds(1000), seconds(1000)),
                        readsAs(1, 0000) },
                { ResourcesPerDay.calculateFrom(seconds(2000), seconds(1000)),
                        readsAs(2, 0000) },
                { ResourcesPerDay.calculateFrom(seconds(500), seconds(1000)),
                        readsAs(0, 5000) },
                { ResourcesPerDay.calculateFrom(seconds(651), seconds(1000)),
                        readsAs(0, 6510) },
                { ResourcesPerDay.calculateFrom(seconds(1986), seconds(1000)),
                        readsAs(1, 9860) },
                periodicalNumber };
        for (Object[] pair : examples) {
            ResourcesPerDay first = (ResourcesPerDay) pair[0];
            Matcher<ResourcesPerDay> matcher = (Matcher<ResourcesPerDay>) pair[1];
            assertThat(first, matcher);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void canDistributeResourcesPerDay() {
        ResourcesPerDayDistributor distributor = ResourcesPerDay.distributor(
                ResourcesPerDay.amount(new BigDecimal(0.8)), ResourcesPerDay
                        .amount(new BigDecimal(0.2)));
        Object[][] examples = {
                { ResourcesPerDay.amount(10),
                    readsAs(8, 0), readsAs(2, 0) },
                { ResourcesPerDay.amount(1),
                    readsAs(0, 8000), readsAs(0, 2000) },
                { ResourcesPerDay.amount(new BigDecimal(0.5)),
                    readsAs(0, 4000),readsAs(0, 1000) } };
        for (Object[] eachExample : examples) {
            ResourcesPerDay toDistribute = (ResourcesPerDay) eachExample[0];
            Matcher<ResourcesPerDay> firstMatcher = (Matcher<ResourcesPerDay>) eachExample[1];
            Matcher<ResourcesPerDay> secondMatcher = (Matcher<ResourcesPerDay>) eachExample[2];
            ResourcesPerDay[] distribute = distributor.distribute(toDistribute);
            assertThat(distribute[0], firstMatcher);
            assertThat(distribute[1], secondMatcher);
        }
    }

}
