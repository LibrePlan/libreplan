package org.navalplanner.business.test.planner.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.navalplanner.business.planner.entities.Share;

public class ShareTest {

    private Share share;

    private void givenHours(int hours) {
        share = new Share(hours);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustNotHaveNegativeHours() {
        new Share(-1);
    }

    @Test
    public void canHaveZeroHours() {
        new Share(0);
    }

    @Test
    public void hasPropertyToRetrieveHours() {
        givenHours(4);
        assertThat(share.getHours(), equalTo(4));
    }

    @Test
    public void toStringContainsHours(){
        givenHours(133);
        assertTrue(share.toString().contains("133"));
    }

    @Test
    public void canAddHours() {
        givenHours(10);
        Share newShare = share.plus(5);
        assertThat(newShare.getHours(), equalTo(15));
    }

    @Test
    public void isImmutable(){
        givenHours(4);
        share.plus(3);
        assertThat(share.getHours(), equalTo(4));
    }

    @Test
    public void canSubstract() {
        givenHours(5);
        Share newShare = share.plus(-2);
        assertThat(newShare.getHours(), equalTo(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotSubstractABiggerNumber() {
        givenHours(5);
        share.plus(-6);
    }

    @Test
    public void twoSharesEqualIfHaveTheSameHours() {
        givenHours(10);
        assertThat(share, equalTo(new Share(10)));
        assertThat(share.hashCode(), equalTo(new Share(10).hashCode()));
    }

}
