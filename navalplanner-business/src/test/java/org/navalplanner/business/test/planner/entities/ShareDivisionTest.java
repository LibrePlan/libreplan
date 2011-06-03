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

package org.navalplanner.business.test.planner.entities;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.navalplanner.business.planner.entities.Share;
import org.navalplanner.business.planner.entities.ShareDivision;

public class ShareDivisionTest {

    private ShareDivision shareDivision;

    private void givenDivisionShare(Share... shares) {
        shareDivision = ShareDivision.create(Arrays.asList(shares));
    }

    @Test
    public void aCompoundShareIsCreatedFromSeveralShares() {
        Share share1 = createExampleShare();
        Share share2 = createExampleShare();
        ShareDivision shareDivision = ShareDivision.create(Arrays.asList(
                share1, share2));
        assertThat(shareDivision.getShares(), JUnitMatchers.hasItems(share1,
                share2));
    }

    private Share createExampleShare() {
        return new Share(10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sharesCannotBeNull() {
        ShareDivision.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void allSharesMustBeNotNull(){
        ShareDivision.create(Arrays.asList(createExampleShare(), null));
    }

    @Test
    public void aSharesCompoundMustHaveADescriptiveToString() {
        givenDivisionShare(new Share(10), new Share(5));
        assertTrue(shareDivision.toString().contains("[10, 5]"));
    }

    @Test
    public void remainderIsGivenToFirstShares() {
        givenDivisionShare(new Share(10), new Share(10), new Share(10));
        ShareDivision s = shareDivision.plus(8);
        assertThat(s, haveValues(13, 13, 12));
    }

    @Test
    public void theSharesWithLessAreGivenMore() {
        givenDivisionShare(new Share(10), new Share(5), new Share(10));
        ShareDivision s = shareDivision.plus(4);
        assertThat(s, haveValues(10, 9, 10));
    }

    @Test
    public void theIncrementIsEquallyDistributedToTheSharesWithLess() {
        givenDivisionShare(new Share(10), new Share(5), new Share(5),
                new Share(10));
        ShareDivision s = shareDivision.plus(4);
        assertThat(s, haveValues(10, 7, 7, 10));
    }

    @Test
    public void theIncrementIsEquallyDistributedToTheSharesWithLessUntilEqualTheOthers() {
        givenDivisionShare(new Share(10), new Share(5), new Share(5),
                new Share(10));
        assertThat(shareDivision.plus(2), haveValues(10, 6, 6, 10));
        assertThat(shareDivision.plus(10), haveValues(10, 10, 10, 10));
        assertThat(shareDivision.plus(11), haveValues(11, 10, 10, 10));
        assertThat(shareDivision.plus(12), haveValues(11, 11, 10, 10));
        assertThat(shareDivision.plus(14), haveValues(11, 11, 11, 11));
    }

    @Test
    public void areProgressivelyFilled() {
        givenDivisionShare(new Share(2), new Share(5), new Share(10));
        assertThat(shareDivision.plus(1), haveValues(3, 5, 10));
        assertThat(shareDivision.plus(3), haveValues(5, 5, 10));
        assertThat(shareDivision.plus(4), haveValues(6, 5, 10));
        assertThat(shareDivision.plus(5), haveValues(6, 6, 10));
        assertThat(shareDivision.plus(6), haveValues(7, 6, 10));
        assertThat(shareDivision.plus(13), haveValues(10, 10, 10));
    }

    @Test
    public void canDistributeWhenSomeAreNegative() {
        givenDivisionShare(new Share(2), new Share(-5), new Share(-3));
        assertThat(shareDivision.plus(2), haveValues(2, -3, -3));
        assertThat(shareDivision.plus(3), haveValues(2, -2, -3));
        assertThat(shareDivision.plus(8), haveValues(2, 0, 0));
        assertThat(shareDivision.plus(11), haveValues(2, 2, 1));
        assertThat(shareDivision.plus(12), haveValues(2, 2, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantKnowTheDifferenceBetweenTwoDivisionsOfDifferentNumberOfShares(){
        givenDivisionShare(new Share(2), new Share(-5), new Share(-3));
        shareDivision.to(ShareDivision.create(Arrays.asList(new Share(2),
                new Share(1))));
    }

    @Test
    public void canKnowTheDifferenceBetweenTwoDivisions() {
        givenDivisionShare(new Share(2), new Share(-5), new Share(-3));
        int[] difference = shareDivision.to(ShareDivision.create(Arrays.asList(
                new Share(1), new Share(1), new Share(-2))));
        assertTrue(Arrays.equals(difference, new int[] { -1, 6, 1 }));
    }

    @Test
    public void canHandleMaximumValueIntegers() {
        givenDivisionShare(new Share(2), new Share(0), new Share(
                Integer.MAX_VALUE), new Share(Integer.MAX_VALUE), new Share(
                Integer.MAX_VALUE));
        ShareDivision plus = shareDivision.plus(10);
        int[] difference = shareDivision.to(plus);
        assertArrayEquals(new int[] { 4, 6, 0, 0, 0 }, difference);
    }

    @Test
    public void canHandleAllMaximumValueIntegers() {
        givenDivisionShare(new Share(Integer.MAX_VALUE), new Share(
                Integer.MAX_VALUE), new Share(Integer.MAX_VALUE));
        ShareDivision plus = shareDivision.plus(2);
        int[] difference = shareDivision.to(plus);
        assertArrayEquals(new int[] { 1, 1, 0 }, difference);
    }

    @Test
    public void canHandleMaximumValueIntegersAndMinimumValue() {
        givenDivisionShare(new Share(Integer.MIN_VALUE), new Share(
                Integer.MAX_VALUE), new Share(Integer.MAX_VALUE), new Share(
                Integer.MIN_VALUE), new Share(Integer.MIN_VALUE), new Share(
                Integer.MAX_VALUE));
        ShareDivision plus = shareDivision.plus(9);
        int[] difference = shareDivision.to(plus);
        assertArrayEquals(new int[] { 3, 0, 0, 3, 3, 0 }, difference);
    }

    @Test
    @Ignore("TODO handling substractions")
    public void canDistributeSubstraction() {
        givenDivisionShare(new Share(2), new Share(5), new Share(10));
        assertThat(shareDivision.plus(-1), haveValues(2, 5, 9));
    }

    private Matcher<ShareDivision> haveValues(final int... shares) {
        final List<Integer> sharesList = asIntegersList(shares);
        return new BaseMatcher<ShareDivision>() {

            @Override
            public boolean matches(Object value) {
                if (value instanceof ShareDivision) {
                    ShareDivision compound = (ShareDivision) value;
                    return sharesList
                            .equals(asIntegerList(compound.getShares()));
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("must have this shares: "
                        + Arrays.toString(shares));
            }
        };
    }

    private static List<Integer> asIntegerList(List<Share> shares) {
        List<Integer> result = new ArrayList<Integer>();
        for (Share share : shares) {
            result.add(share.getHours());
        }
        return result;
    }

    private static List<Integer> asIntegersList(int[] shares) {
        List<Integer> result = new ArrayList<Integer>();
        for (int share : shares) {
            result.add(share);
        }
        return result;
    }

}
