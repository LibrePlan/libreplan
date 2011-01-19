/*
 * This file is part of NavalPlan
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
package org.navalplanner.business.common;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class ProportionalDistributorTest {

    @Test
    public void mustGiveTheSameDistributionForSameTotal() {
        ProportionalDistributor distributor = ProportionalDistributor.create(
                100, 200);
        assertThat(distributor.distribute(300), equalToDistribution(100, 200));
    }

    @Test
    public void exactDivisionsWorkOk() {
        ProportionalDistributor distributor = ProportionalDistributor.create(
                100, 100, 100);
        assertThat(distributor.distribute(600), equalToDistribution(200, 200,
                200));
    }

    @Test
    public void distributingZeroGivesZeroShares() {
        ProportionalDistributor distributor = ProportionalDistributor.create(
                100, 100, 100);
        assertThat(distributor.distribute(0), equalToDistribution(0, 0, 0));
    }

    @Test
    public void ifOneOfTheProportionsIsZeroAlwaysGivesZeros() {
        ProportionalDistributor distributor = ProportionalDistributor.create(
                100, 100, 0);
        assertThat(distributor.distribute(100), equalToDistribution(50, 50, 0));
    }

    @Test
    public void ifEmptySharesProvidedItDistributesEqually() {
        ProportionalDistributor distributor = ProportionalDistributor.create(0,
                0, 0, 0);
        assertThat(distributor.distribute(4), equalToDistribution(1, 1, 1, 1));
        assertThat(distributor.distribute(5), equalToDistribution(2, 1, 1, 1));
        assertThat(distributor.distribute(6), equalToDistribution(2, 2, 1, 1));
    }

    @Test
    public void noSharesProvidedImpliesItReturnsEmptyDistribution() {
        ProportionalDistributor distributor = ProportionalDistributor.create();
        assertThat(distributor.distribute(0).length, equalTo(0));
        assertThat(distributor.distribute(1).length, equalTo(0));

    }

    @Test
    public void disputedPartGoesToFirstIfEqualWeight() {
        ProportionalDistributor distributor = ProportionalDistributor.create(
                10, 10, 10);
        assertThat(distributor.distribute(10), equalToDistribution(4, 3, 3));
    }

    @Test
    public void distributionIsKept() {
        ProportionalDistributor distributor = ProportionalDistributor.create(2,
                3, 5);
        assertThat(distributor.distribute(1), equalToDistribution(0, 0, 1));
        assertThat(distributor.distribute(2), equalToDistribution(0, 1, 1));
        assertThat(distributor.distribute(3), equalToDistribution(1, 1, 1));
        assertThat(distributor.distribute(4), equalToDistribution(1, 1, 2));
        assertThat(distributor.distribute(5), equalToDistribution(1, 2, 2));
        assertThat(distributor.distribute(6), equalToDistribution(1, 2, 3));
        assertThat(distributor.distribute(10), equalToDistribution(2, 3, 5));
        assertThat(distributor.distribute(7), equalToDistribution(1, 2, 4));
    }

    @Test
    public void addingOneEachTime() {
        ProportionalDistributor distributor = ProportionalDistributor.create(
                99, 101, 800);
        assertThat(distributor.distribute(1), equalToDistribution(0, 0, 1));
        assertThat(distributor.distribute(3), equalToDistribution(0, 0, 3));
        assertThat(distributor.distribute(6), equalToDistribution(0, 1, 5));
        assertThat(distributor.distribute(7), equalToDistribution(1, 1, 5));
        assertThat(distributor.distribute(8), equalToDistribution(1, 1, 6));
        assertThat(distributor.distribute(9), equalToDistribution(1, 1, 7));
        assertThat(distributor.distribute(10), equalToDistribution(1, 1, 8));
        assertThat(distributor.distribute(11), equalToDistribution(1, 1, 9));
        assertThat(distributor.distribute(12), equalToDistribution(1, 1, 10));
        assertThat(distributor.distribute(13), equalToDistribution(1, 1, 11));
        assertThat(distributor.distribute(14), equalToDistribution(1, 2, 11));
        assertThat(distributor.distribute(15), equalToDistribution(1, 2, 12));
        assertThat(distributor.distribute(16), equalToDistribution(1, 2, 13));
        assertThat(distributor.distribute(17), equalToDistribution(2, 2, 13));
        assertThat(distributor.distribute(20), equalToDistribution(2, 2, 16));
    }

    private static Matcher<int[]> equalToDistribution(final int... distribution) {
        return new BaseMatcher<int[]>() {

            @Override
            public boolean matches(Object object) {
                if (object instanceof int[]) {
                    int[] arg = (int[]) object;
                    return Arrays.equals(arg, distribution);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("must equal "
                        + Arrays.toString(distribution));
            }
        };
    }

    @Test
    public void notThrowDivisionByZeroException() {
        ProportionalDistributor.create(0);
    }

    @Test
    public void notThrowDivisionByZeroExceptionAtDistributeMehtod() {
        ProportionalDistributor distributor = ProportionalDistributor
                .create(100);
        distributor.distribute(0);
    }

}
