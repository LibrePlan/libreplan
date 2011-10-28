/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.libreplan.business.test.calendars.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.workingday.EffortDuration.hours;
import static org.libreplan.business.workingday.EffortDuration.zero;

import org.junit.Test;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.workingday.EffortDuration;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CapacityTest {

    @Test
    public void itHasStandardEffortAndAllowedExtraEffort() {
        Capacity capacity = Capacity.create(hours(8)).withAllowedExtraEffort(hours(2));
        assertThat(capacity.getStandardEffort(),
                equalTo(EffortDuration.hours(8)));
        assertThat(capacity.getAllowedExtraEffort(), equalTo(hours(2)));
    }

    @Test
    public void ifOnlyTheStandardCapacitySpecifiedIsOverAssignableWithoutLimit() {
        Capacity capacity = Capacity.create(hours(8));
        assertTrue(capacity.isOverAssignableWithoutLimit());
    }

    @Test
    public void ifOnlyTheStandardCapacitySpecifiedTheExtraHoursAreNull() {
        Capacity capacity = Capacity.create(hours(8));
        assertThat(capacity.getAllowedExtraEffort(), nullValue());
    }

    @Test
    public void ifHasAllowedExtraEffortItsNotOverassignableWithoutLimit() {
        Capacity capacity = Capacity.create(hours(8)).withAllowedExtraEffort(hours(0));
        assertFalse(capacity.isOverAssignableWithoutLimit());
    }

    @Test
    public void hasAnEqualsAndHashCodeBasedOnStandardEffortAndExtraHours() {
        Capacity a1 = Capacity.create(hours(8)).withAllowedExtraEffort(hours(2));
        Capacity a2 = Capacity.create(hours(8)).withAllowedExtraEffort(hours(2));

        Capacity b1 = Capacity.create(hours(8));

        assertThat(a1, equalTo(a2));
        assertThat(a1.hashCode(), equalTo(a2.hashCode()));
        assertThat(a1, not(equalTo(b1)));
    }

    @Test
    public void aZeroCapacityIsNotOverAssignable() {
        assertFalse(Capacity.zero().isOverAssignableWithoutLimit());
    }

    @Test
    public void albeitTheCapacityIsCreatedFromHibernateAndTheFieldsAreNullDontReturnANullExtraEffort() {
        Capacity capacity = new Capacity();
        assertThat(capacity.getStandardEffort(), equalTo(EffortDuration.zero()));
    }

    @Test
    public void aInitiallyZeroCapacityDoesNotAllowWorking() {
        Capacity zero = Capacity.zero();
        assertFalse(zero.allowsWorking());
    }

    @Test
    public void aNonZeroCapacityAllowsWorking() {
        Capacity capacity = Capacity.create(EffortDuration.minutes(1));
        assertTrue(capacity.allowsWorking());
    }

    @Test
    public void aCapacityWithExtraHoursAndZeroEffortAllowsWorking() {
        Capacity capacity = Capacity.create(EffortDuration.zero()).withAllowedExtraEffort(
                EffortDuration.minutes(1));
        assertTrue(capacity.allowsWorking());
    }

    @Test
    public void aCapacityWithZeroHoursAndOverAssignableWithoutLimitAllowsWorking() {
        Capacity capacity = Capacity.zero().overAssignableWithoutLimit();
        assertTrue(capacity.allowsWorking());
    }

    @Test(expected = IllegalArgumentException.class)
    public void aCapacityCannotBeMultipliedByANegativeNumber() {
        Capacity.create(hours(8)).multiplyBy(-1);
    }

    @Test
    public void aCapacityMultipliedByZero() {
        Capacity[] originals = {
                Capacity.create(hours(8)).overAssignableWithoutLimit(),
                Capacity.create(hours(8)).notOverAssignableWithoutLimit() };
        for (Capacity original : originals) {
            Capacity multipliedByZero = original.multiplyBy(0);
            assertThat(multipliedByZero.getStandardEffort(), equalTo(zero()));
            assertEquals(original.isOverAssignableWithoutLimit(),
                    multipliedByZero.isOverAssignableWithoutLimit());
        }
    }

    @Test
    public void multiplyingMultipliesTheStandardEffortAndTheOverTimeEffort() {
        Capacity capacity = Capacity.create(hours(8)).withAllowedExtraEffort(hours(2));
        Capacity multiplied = capacity.multiplyBy(2);
        assertThat(multiplied.getStandardEffort(), equalTo(hours(16)));
        assertThat(multiplied.getAllowedExtraEffort(), equalTo(hours(4)));
    }

    private Capacity a = Capacity.create(hours(8));

    private Capacity b = Capacity.create(hours(4));

    @Test
    public void theMinOfTwoCapacitiesReturnsTheMinimumStandardEffort() {
        Capacity min = Capacity.min(a, b);
        assertThat(min.getStandardEffort(), equalTo(b.getStandardEffort()));

        Capacity max = Capacity.max(a, b);
        assertThat(max.getStandardEffort(), equalTo(a.getStandardEffort()));
    }

    @Test
    public void theMaxOfTwoCapacitiesReturnsTheMaximumStandardEffort() {
        Capacity max = Capacity.max(a, b);
        assertThat(max.getStandardEffort(), equalTo(a.getStandardEffort()));
    }

    @Test
    public void theExtraEffortIsAlsoMinimized() {
        assertThat(
                Capacity.min(a.withAllowedExtraEffort(hours(2)),
                        b.overAssignableWithoutLimit())
                        .getAllowedExtraEffort(), equalTo(hours(2)));

        assertThat(
                Capacity.min(a.withAllowedExtraEffort(hours(2)),
                        a.withAllowedExtraEffort(hours(4)))
                        .getAllowedExtraEffort(), equalTo(hours(2)));
    }

    @Test
    public void theExtraEffortIsMaximized() {
        assertThat(
                Capacity.max(a.withAllowedExtraEffort(hours(2)),
                        b.overAssignableWithoutLimit())
                        .getAllowedExtraEffort(), nullValue());

        assertThat(
                Capacity.max(a.withAllowedExtraEffort(hours(2)),
                        a.withAllowedExtraEffort(hours(4)))
                        .getAllowedExtraEffort(), equalTo(hours(4)));
    }

    @Test
    public void testThereIsCapacityForMoreAllocations() {
        assertThat(Capacity.create(hours(8)).overAssignableWithoutLimit()
                .hasSpareSpaceForMoreAllocations(hours(10)), is(true));

        Capacity notOverassignable = Capacity.create(hours(8))
                .notOverAssignableWithoutLimit();

        assertFalse(notOverassignable
                .hasSpareSpaceForMoreAllocations(hours(10)));
        assertTrue(notOverassignable
                .hasSpareSpaceForMoreAllocations(hours(7)));
        assertFalse(notOverassignable
                .hasSpareSpaceForMoreAllocations(hours(8)));

        Capacity withSomeExtraHours = notOverassignable
                .withAllowedExtraEffort(hours(2));

        assertFalse(withSomeExtraHours
                .hasSpareSpaceForMoreAllocations(hours(10)));
        assertTrue(withSomeExtraHours
                .hasSpareSpaceForMoreAllocations(hours(7)));
        assertTrue(withSomeExtraHours
                .hasSpareSpaceForMoreAllocations(hours(8)));
    }

    @Test
    public void testMinusWithZeroExtraHours() {
        Capacity c = Capacity.create(hours(8)).notOverAssignableWithoutLimit();

        assertThat(c.minus(hours(6)).getStandardEffort(), equalTo(hours(2)));
        assertThat(c.minus(hours(8)).getStandardEffort(), equalTo(hours(0)));
        assertThat(c.minus(hours(10)).getStandardEffort(), equalTo(hours(0)));
        assertFalse(c.minus(hours(6)).isOverAssignableWithoutLimit());

    }

    @Test
    public void testMinusWithExtraHours() {
        Capacity c = Capacity.create(hours(8)).withAllowedExtraEffort(hours(2));

        assertThat(c.minus(hours(6)).getStandardEffort(), equalTo(hours(2)));
        assertThat(c.minus(hours(6)).getAllowedExtraEffort(), equalTo(hours(2)));
        assertThat(c.minus(hours(8)).getStandardEffort(), equalTo(hours(0)));
        assertThat(c.minus(hours(8)).getAllowedExtraEffort(), equalTo(hours(2)));
        assertThat(c.minus(hours(10)).getStandardEffort(), equalTo(hours(0)));
        assertThat(c.minus(hours(10)).getAllowedExtraEffort(),
                equalTo(hours(0)));
        assertThat(c.minus(hours(12)).getAllowedExtraEffort(),
                equalTo(hours(0)));
        assertFalse(c.minus(hours(10)).isOverAssignableWithoutLimit());
    }

    @Test
    public void testMinusWithUnlimitedExtraHours() {
        Capacity c = Capacity.create(hours(8)).overAssignableWithoutLimit();

        assertThat(c.minus(hours(6)).getStandardEffort(), equalTo(hours(2)));
        assertTrue(c.minus(hours(6)).isOverAssignableWithoutLimit());
        assertThat(c.minus(hours(8)).getStandardEffort(), equalTo(hours(0)));
        assertTrue(c.minus(hours(8)).isOverAssignableWithoutLimit());
        assertThat(c.minus(hours(10)).getStandardEffort(), equalTo(hours(0)));
        assertTrue(c.minus(hours(10)).isOverAssignableWithoutLimit());
    }

    @Test
    public void capacitiesCanBeSummed() {
        Capacity result = Capacity.sum(
                Capacity.create(hours(8)).withAllowedExtraEffort(hours(2)),
                Capacity.create(hours(8)).notOverAssignableWithoutLimit());

        assertThat(result.getStandardEffort(), equalTo(hours(16)));
        assertThat(result.getAllowedExtraEffort(), equalTo(hours(2)));
    }

    @Test
    public void ifSomeOfTheSumandsHasUnlimitedExtraHoursTheSumToo() {
        Capacity result = Capacity.sum(Capacity.create(hours(8))
                .withAllowedExtraEffort(hours(2)), Capacity.create(hours(8))
                .overAssignableWithoutLimit());

        assertThat(result.getStandardEffort(), equalTo(hours(16)));
        assertTrue(result.isOverAssignableWithoutLimit());
    }
}
