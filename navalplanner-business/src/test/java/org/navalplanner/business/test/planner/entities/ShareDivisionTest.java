package org.navalplanner.business.test.planner.entities;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
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
        ShareDivision s = shareDivision.add(8);
        assertThat(s, haveValues(13, 13, 12));
    }

    @Test
    public void theSharesWithLessAreGivenMore() {
        givenDivisionShare(new Share(10), new Share(5), new Share(10));
        ShareDivision s = shareDivision.add(4);
        assertThat(s, haveValues(10, 9, 10));
    }

    @Test
    public void theIncrementIsEquallyDistributedToTheSharesWithLess() {
        givenDivisionShare(new Share(10), new Share(5), new Share(5),
                new Share(10));
        ShareDivision s = shareDivision.add(4);
        assertThat(s, haveValues(10, 7, 7, 10));
    }

    @Test
    public void theIncrementIsEquallyDistributedToTheSharesWithLessUntilEqualTheOthers() {
        givenDivisionShare(new Share(10), new Share(5), new Share(5),
                new Share(10));
        assertThat(shareDivision.add(2), haveValues(10, 6, 6, 10));
        assertThat(shareDivision.add(10), haveValues(10, 10, 10, 10));
        assertThat(shareDivision.add(11), haveValues(11, 10, 10, 10));
        assertThat(shareDivision.add(12), haveValues(11, 11, 10, 10));
        assertThat(shareDivision.add(14), haveValues(11, 11, 11, 11));
    }

    @Test
    public void areProgressivelyFilled() {
        givenDivisionShare(new Share(2), new Share(5), new Share(10));
        assertThat(shareDivision.add(1), haveValues(3, 5, 10));
        assertThat(shareDivision.add(3), haveValues(5, 5, 10));
        assertThat(shareDivision.add(4), haveValues(6, 5, 10));
        assertThat(shareDivision.add(5), haveValues(6, 6, 10));
        assertThat(shareDivision.add(6), haveValues(7, 6, 10));
        assertThat(shareDivision.add(13), haveValues(10, 10, 10));
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
