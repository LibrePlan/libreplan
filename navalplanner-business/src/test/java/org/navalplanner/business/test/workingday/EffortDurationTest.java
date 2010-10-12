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

package org.navalplanner.business.test.workingday;

import static java.util.Arrays.asList;
import static java.util.Collections.min;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.navalplanner.business.workingday.EffortDuration.hours;
import static org.navalplanner.business.workingday.EffortDuration.minutes;
import static org.navalplanner.business.workingday.EffortDuration.seconds;
import static org.navalplanner.business.workingday.EffortDuration.zero;

import java.math.BigDecimal;
import java.util.EnumMap;

import org.junit.Test;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.Granularity;

public class EffortDurationTest {

    @Test
    public void itCanExpressTheDurationAsHoursMinutesAndSeconds() {
        EffortDuration duration = EffortDuration.hours(4);
        assertThat(duration.getHours(), equalTo(4));
        assertThat(duration.getMinutes(), equalTo(240));
        assertThat(duration.getSeconds(), equalTo(240 * 60));
    }

    @Test
    public void unitsCannotBeNegative() {
        Granularity[] granularities = EffortDuration.Granularity.values();
        for (Granularity each : granularities) {
            try {
                EffortDuration.elapsing(-1, each);
                fail("it should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }

    @Test
    public void hoursCanBeZero() {
        EffortDuration.hours(0);
    }

    @Test
    public void minutesAreTranslatedToHours() {
        assertThat(EffortDuration.minutes(120).getHours(), equalTo(2));
        assertThat(minutes(119).getHours(), equalTo(1));
        assertThat(minutes(121).getHours(), equalTo(2));
    }

    @Test
    public void canBeTranslatedToAnyGranularity(){
        assertThat(hours(3).convertTo(Granularity.MINUTES), equalTo(180));
    }

    @Test
    public void canSpecifyADurationWithSeveralUnits() {
        assertThat(
                hours(2).and(30, Granularity.MINUTES)
                .convertTo(Granularity.MINUTES), equalTo(150));
    }

    private static final EffortDuration oneHourAndAHalf = hours(1).and(30,
            Granularity.MINUTES);

    private static final EffortDuration ninetyMinutes = minutes(90);

    @Test
    public void twoDurationWithTheSameSecondsAreEqual(){
        assertThat(oneHourAndAHalf, equalTo(ninetyMinutes));
    }

    @Test
    public void twoEqualDurationsHaveTheSameHashCode() {
        assertThat(oneHourAndAHalf.hashCode(),
                equalTo(ninetyMinutes.hashCode()));
    }

    @Test
    public void anEffortDurationCanBeDecomposedIntoElements() {
        EffortDuration duration = hours(1).and(90, Granularity.MINUTES);
        EnumMap<Granularity, Integer> values = duration.decompose();
        assertThat(values.get(Granularity.HOURS), equalTo(2));
        assertThat(values.get(Granularity.MINUTES), equalTo(30));
        assertThat(values.get(Granularity.SECONDS), equalTo(0));
    }

    @Test
    public void anZeroDurationHasZeroElements() {
        EffortDuration duration = EffortDuration.zero();
        EnumMap<Granularity, Integer> values = duration.decompose();
        assertThat(values.get(Granularity.HOURS), equalTo(0));
        assertThat(values.get(Granularity.MINUTES), equalTo(0));
        assertThat(values.get(Granularity.SECONDS), equalTo(0));
    }

    @Test
    public void aZeroDurationIsZero() {
        EffortDuration duration = EffortDuration.zero();
        assertTrue(duration.isZero());
    }

    @Test
    public void effortDurationImplementsComparable() {
        assertTrue(hours(3).compareTo(minutes(180)) == 0);
        assertTrue(hours(3).compareTo(minutes(178)) > 0);
        assertTrue(hours(3).compareTo(minutes(181)) < 0);
        assertTrue(hours(3).compareTo(minutes(190)) < 0);
        EffortDuration min = min(asList(hours(2), hours(3), seconds(10),
                seconds(5).and(1, Granularity.HOURS)));
        assertThat(min, equalTo(seconds(10)));
    }

    @Test
    public void effortDurationsCanBePlused() {
        EffortDuration a = EffortDuration.hours(1).and(30, Granularity.MINUTES);
        EffortDuration b = EffortDuration.minutes(30);
        assertThat(a.plus(b), equalTo(EffortDuration.hours(2)));
    }

    @Test
    public void effortDurationCanBeDivided() {
        assertThat(hours(4).divideBy(4), equalTo(hours(1)));
        assertThat(hours(3).divideBy(2),
                equalTo(hours(1).and(30, Granularity.MINUTES)));
        assertThat(hours(3).divideBy(4), equalTo(minutes(45)));
    }

    @Test
    public void effortDurationCanBeDividedByAnotherEffortDuration() {
        assertThat(hours(4).divideBy(hours(2)), equalTo(2));
        assertThat(hours(4).remainderFor(hours(2)), equalTo(zero()));
        assertThat(hours(5).divideBy(hours(2)), equalTo(2));
        assertThat(hours(5).remainderFor(hours(2)), equalTo(hours(1)));
        assertThat(hours(2).divideBy(hours(4)), equalTo(0));
        assertThat(hours(2).remainderFor(hours(4)), equalTo(hours(2)));
        assertThat(hours(1).and(30, Granularity.MINUTES).divideBy(minutes(30)),
                equalTo(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void effortDurationCannotBeSubstractedIfMinuedIsSmallerThanSubtrahend() {
        EffortDuration threeHours = hours(3);
        threeHours.minus(threeHours.and(1, Granularity.SECONDS));
    }

    @Test
    public void effortDurationCanBeSubstracted() {
        assertThat(hours(2).minus(minutes(120)), equalTo(EffortDuration.zero()));
        assertThat(hours(2).minus(minutes(60)), equalTo(hours(1)));
    }

    @Test
    public void canConvertToDecimalHours() {
        EffortDuration duration = hours(1).and(30, Granularity.MINUTES);
        assertThat(duration.toHoursAsDecimalWithScale(1), equalTo(new BigDecimal("1.5")));
    }

    @Test
    public void theScaleIsTheOneProvided() {
        EffortDuration duration = hours(1).and(30, Granularity.MINUTES);
        assertThat(duration.toHoursAsDecimalWithScale(1).scale(), equalTo(1));
        assertThat(duration.toHoursAsDecimalWithScale(2).scale(), equalTo(2));
        assertThat(duration.toHoursAsDecimalWithScale(3).scale(), equalTo(3));
    }

    @Test
    public void anHalfUpRoundIsDone() {
        // 3601/3600 = 1.000277778
        EffortDuration duration = hours(1).and(1, Granularity.SECONDS);
        assertThat(duration.toHoursAsDecimalWithScale(6), equalTo(new BigDecimal(
                "1.000278")));
        assertThat(duration.toHoursAsDecimalWithScale(5),
                equalTo(new BigDecimal("1.00028")));
        assertThat(duration.toHoursAsDecimalWithScale(4),
                equalTo(new BigDecimal("1.0003")));
    }

    @Test
    public void theDurationCanBeRoundedToIntegerHours() {
        assertThat(hours(0).roundToHours(), equalTo(0));
        assertThat(hours(1).roundToHours(), equalTo(1));
        assertThat(hours(2).roundToHours(), equalTo(2));
    }

    @Test
    public void theTypeOfRoundDoneIsHalfUpWhenTheHoursAreAtLeastOne() {
        assertThat(hours(1).and(20, Granularity.MINUTES).roundToHours(),
                equalTo(1));
        assertThat(hours(1).and(30, Granularity.MINUTES).roundToHours(),
                equalTo(2));
    }

    @Test
    public void ifDurationNotZeroIsAlwaysRoundedToAtLeastOne() {
        assertThat(seconds(1).roundToHours(), equalTo(1));
        assertThat(minutes(29).roundToHours(), equalTo(1));
        assertThat(minutes(30).roundToHours(), equalTo(1));
    }

}
