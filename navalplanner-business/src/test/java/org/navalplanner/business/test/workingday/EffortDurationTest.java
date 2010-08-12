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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.navalplanner.business.workingday.EffortDuration.elapsing;

import org.junit.Test;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.Granularity;

public class EffortDurationTest {

    @Test
    public void itCanExpressTheDurationAsHoursMinutesAndSeconds() {
        EffortDuration duration = EffortDuration.elapsing(4, Granularity.HOURS);
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
        EffortDuration.elapsing(0, Granularity.HOURS);
    }

    @Test
    public void minutesAreTranslatedToHours() {
        assertThat(elapsing(120, Granularity.MINUTES).getHours(),
                equalTo(2));
        assertThat(elapsing(119, Granularity.MINUTES).getHours(), equalTo(1));
        assertThat(elapsing(121, Granularity.MINUTES).getHours(), equalTo(2));
    }

    @Test
    public void canBeTranslatedToAnyGranularity(){
        assertThat(elapsing(3, Granularity.HOURS)
                .convertTo(Granularity.MINUTES),
                equalTo(180));
    }

    @Test
    public void canSpecifyADurationWithSeveralUnits() {
        assertThat(elapsing(2, Granularity.HOURS).and(30, Granularity.MINUTES)
                .convertTo(Granularity.MINUTES), equalTo(150));
    }

    private static final EffortDuration oneHourAndAHalf = elapsing(1,
            Granularity.HOURS).and(30, Granularity.MINUTES);
    private static final EffortDuration ninetyMinutes = elapsing(90,
            Granularity.MINUTES);

    @Test
    public void twoDurationWithTheSameSecondsAreEqual(){
        assertThat(oneHourAndAHalf, equalTo(ninetyMinutes));
    }

    @Test
    public void twoEqualDurationsHaveTheSameHashCode() {
        assertThat(oneHourAndAHalf.hashCode(),
                equalTo(ninetyMinutes.hashCode()));
    }

}
