package org.navalplanner.business.test.orders.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.HoursGroup.HoursPolicies;

/**
 * Tests for {@link OrderLine}. <br />
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class OrderLineTest {

    /**
     * An empty {@link OrderLine} without any {@link HoursGroup}.
     *
     * Trying to set work hours of {@link OrderLine} to 100h.
     *
     * Expected: {@link OrderLine} with 100h, with one {@link HoursGroup} with
     * 100h NO_FIXED.
     */
    @Test
    public void testSetWorkHoursHoursEmptyOrderLine() {

        OrderLine orderLine = new OrderLine();

        assertThat(orderLine.getWorkHours(), equalTo(0));

        try {
            orderLine.setWorkHours(100);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(100));
        assertThat(orderLine.getHoursGroups().size(), equalTo(1));
        HoursGroup hoursGroup = orderLine.getHoursGroups().get(0);
        assertThat(hoursGroup.getWorkingHours(), equalTo(100));
        assertThat(hoursGroup.getPercentage(), equalTo(new BigDecimal(1)
                .setScale(2)));
        assertThat(hoursGroup.getHoursPolicy(), equalTo(HoursPolicies.NO_FIXED));
    }

    /**
     * An empty {@link OrderLine} without any {@link HoursGroup}.
     *
     * Trying to set work hours of {@link OrderLine} to -100h.
     *
     * Expected: Exception.
     */
    @Test
    public void testSetWorkHoursHoursEmptyOrderLineIllegal() {

        OrderLine orderLine = new OrderLine();

        assertThat(orderLine.getWorkHours(), equalTo(0));

        try {
            orderLine.setWorkHours(-100);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {

        }

        assertThat(orderLine.getWorkHours(), equalTo(0));
        assertThat(orderLine.getHoursGroups().size(), equalTo(0));

    }

    /**
     * An {@link OrderLine} with just one {@link HoursGroup} of 100h NO_FIXED.
     *
     * Trying to set work hours of {@link OrderLine} to 120h.
     *
     * Expected: {@link OrderLine} with 120h. {@link HoursGroup} with 120h.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(120);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(120));
        assertThat(orderLine.getHoursGroups().size(), equalTo(1));
        assertThat(hoursGroup.getWorkingHours(), equalTo(120));
    }

    /**
     * An {@link OrderLine} with just one {@link HoursGroup} of 100h NO_FIXED.
     *
     * Trying to set work hours of {@link OrderLine} to 75h.
     *
     * Expected: {@link OrderLine} with 75h. {@link HoursGroup} with 75h.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(75);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(75));
        assertThat(orderLine.getHoursGroups().size(), equalTo(1));
        assertThat(hoursGroup.getWorkingHours(), equalTo(75));

    }

    /**
     * An {@link OrderLine} with just one {@link HoursGroup} of 100h
     * FIXED_HOURS.
     *
     * Trying to set work hours of {@link OrderLine} to 120h.
     *
     * Expected: {@link OrderLine} with 120h. {@link HoursGroup} with 100h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedHoursIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(120);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(120));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(100));

    }

    /**
     * An {@link OrderLine} with just one {@link HoursGroup} of 100h
     * FIXED_HOURS.
     *
     * Trying to set work hours of {@link OrderLine} to 75h.
     *
     * Expected: Exception expected. {@link OrderLine} with 100h.
     * {@link HoursGroup} with 100h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedHoursDecreaseValueIllegal() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(75);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // OK
        }

        assertThat(orderLine.getWorkHours(), equalTo(100));
        assertThat(orderLine.getHoursGroups().size(), equalTo(1));
        assertThat(hoursGroup.getWorkingHours(), equalTo(100));

    }

    /**
     * An {@link OrderLine} with just one {@link HoursGroup} of 100h 100%
     * FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 120h.
     *
     * Expected: {@link OrderLine} with 120h. {@link HoursGroup} with 120h 100%.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup.setWorkingHours(100);
        hoursGroup.setPercentage(new BigDecimal(1).setScale(2));
        orderLine.addHoursGroup(hoursGroup);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(120);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(120));
        assertThat(orderLine.getHoursGroups().size(), equalTo(1));
        assertThat(hoursGroup.getWorkingHours(), equalTo(120));
        assertThat(hoursGroup.getPercentage(), equalTo(new BigDecimal(1)
                .setScale(2)));

    }

    /**
     * An {@link OrderLine} with just one {@link HoursGroup} of 100h 100%
     * FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 75h.
     *
     * Expected: {@link OrderLine} with 100h. {@link HoursGroup} with 75h 100%.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup.setWorkingHours(100);
        hoursGroup.setPercentage(new BigDecimal(1).setScale(2));
        orderLine.addHoursGroup(hoursGroup);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(75);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(75));
        assertThat(orderLine.getHoursGroups().size(), equalTo(1));
        assertThat(hoursGroup.getWorkingHours(), equalTo(75));
        assertThat(hoursGroup.getPercentage(), equalTo(new BigDecimal(1)
                .setScale(2)));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 100h and 50h
     * NO_FIXED.
     *
     * Trying to set work hours of {@link OrderLine} to 200h.
     *
     * Expected: {@link OrderLine} with 200h. {@link HoursGroup} with 133h and
     * HoursGroup with 66h.
     */
    @Test
    public void testSetWorkHoursTwoHoursGroupNoFixedIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(100);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(50);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(150));

        try {
            orderLine.setWorkHours(200);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(200));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(133));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(66));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 100h and 50h
     * NO_FIXED.
     *
     * Trying to set work hours of {@link OrderLine} to 50h.
     *
     * Expected: {@link OrderLine} with 50h. {@link HoursGroup} with 33h and
     * {@link HoursGroup} with 16h.
     */
    @Test
    public void testSetWorkHoursTwoHoursGroupNoFixedDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(100);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(50);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(150));

        try {
            orderLine.setWorkHours(50);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(50));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(33));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(16));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 100h FIXED_HOURS and
     * 50h NO_FIXED.
     *
     * Trying to set work hours of {@link OrderLine} to 200h.
     *
     * Expected: {@link OrderLine} with 200h. {@link HoursGroup} with 100h and
     * HoursGroup with 100h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedHoursAndHoursGroupNoFixedIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup.setWorkingHours(100);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(50);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(150));

        try {
            orderLine.setWorkHours(200);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(200));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(100));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(100));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 100h FIXED_HOURS and
     * 50h NO_FIXED.
     *
     * Trying to set work hours of {@link OrderLine} to 125h.
     *
     * Expected: {@link OrderLine} with 125h. {@link HoursGroup} with 100h and
     * HoursGroup with 25h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedHoursAndHoursGroupNoFixedDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup.setWorkingHours(100);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(50);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(150));

        try {
            orderLine.setWorkHours(125);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(125));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(100));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(25));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 100h FIXED_HOURS and
     * 50h NO_FIXED.
     *
     * Trying to set work hours of {@link OrderLine} to 50h.
     *
     * Expected: Exception. {@link OrderLine} with 150h. {@link HoursGroup} with
     * 100h and {@link HoursGroup} with 50h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedHoursAndHoursGroupNoFixedDecreaseValueIllegal() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup.setWorkingHours(100);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(50);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(150));

        try {
            orderLine.setWorkHours(50);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // OK
        }

        assertThat(orderLine.getWorkHours(), equalTo(150));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(100));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(50));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 75h 75%
     * FIXED_PERCENTAGE and 25h NO_FIXED.
     *
     * Trying to set work hours of {@link OrderLine} to 200h.
     *
     * Expected: {@link OrderLine} with 200h. {@link HoursGroup} with 150h 75%
     * and {@link HoursGroup} with 50h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageAndHoursGroupNoFixedIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup.setWorkingHours(75);
        hoursGroup.setPercentage(new BigDecimal(0.75).setScale(2));
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(25);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(200);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(200));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(150));
        assertThat(hoursGroup.getPercentage(), equalTo(new BigDecimal(0.75)
                .setScale(2)));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(50));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 75h 75%
     * FIXED_PERCENTAGE and 25h NO_FIXED.
     *
     * Trying to set work hours of {@link OrderLine} to 50h.
     *
     * Expected: {@link OrderLine} with 50h. {@link HoursGroup} with 37h 75% and
     * HoursGroup with 13h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageAndHoursGroupNoFixedDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup.setWorkingHours(75);
        hoursGroup.setPercentage(new BigDecimal(0.75).setScale(2));
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(25);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(50);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(50));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(37));
        assertThat(hoursGroup.getPercentage(), equalTo(new BigDecimal(0.75)
                .setScale(2)));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(13));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 75h FIXED_HOURS and
     * 25h 25% FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 150h.
     *
     * Expected: {@link OrderLine} with 150h. {@link HoursGroup} with 75h and
     * HoursGroup with 37h 25%.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedHoursAndHoursGroupFixedPercentageIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup.setWorkingHours(75);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup2.setWorkingHours(25);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(150);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(150));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(75));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(37));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 75h FIXED_HOURS and
     * 25h 25% FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 75h.
     *
     * Expected: Exception. {@link OrderLine} with 75h. {@link HoursGroup} with
     * 75h and {@link HoursGroup} with 25h 25%.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedHoursAndHoursGroupFixedPercentageDecreaseValueIllegal() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup.setWorkingHours(75);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup2.setWorkingHours(25);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(75);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // OK
        }

        assertThat(orderLine.getWorkHours(), equalTo(100));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(75));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(25));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 75h and 25h
     * FIXED_HOURS.
     *
     * Trying to set work hours of {@link OrderLine} to 150h.
     *
     * Expected: {@link OrderLine} with 150h. {@link HoursGroup} with 75h and
     * {@link HoursGroup} with 25h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedHoursAndHoursGroupFixedHoursIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup.setWorkingHours(75);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup2.setWorkingHours(25);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(150);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(150));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(75));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(25));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 75h and 25h
     * FIXED_HOURS.
     *
     * Trying to set work hours of {@link OrderLine} to 50h.
     *
     * Expected: Exception. {@link OrderLine} with 100h. {@link HoursGroup} with
     * 75h and {@link HoursGroup} with 25h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedHoursAndHoursGroupFixedHoursDecreaseValueIllegal() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup.setWorkingHours(75);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup2.setWorkingHours(25);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(50);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // OK
        }

        assertThat(orderLine.getWorkHours(), equalTo(100));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(75));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(25));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 75h 75% and 25h 25%
     * FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 200h.
     *
     * Expected: {@link OrderLine} with 200h. {@link HoursGroup} with 150h 75%
     * and {@link HoursGroup} with 50h 25%.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageAndHoursGroupFixedPercentageIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup.setPercentage(new BigDecimal(0.75).setScale(2));
        hoursGroup.setWorkingHours(75);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup2.setWorkingHours(25);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(200);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(200));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(150));
        assertThat(hoursGroup.getPercentage(), equalTo(new BigDecimal(0.75)
                .setScale(2)));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup2.getPercentage(), equalTo(new BigDecimal(0.25)
                .setScale(2)));

    }

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 75h 75% and 25h 25%
     * FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 80h.
     *
     * Expected: {@link OrderLine} with 80h. {@link HoursGroup} with 60h 75% and
     * {@link HoursGroup} with 20h 25%.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageAndHoursGroupFixedPercentageDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup.setPercentage(new BigDecimal(0.75).setScale(2));
        hoursGroup.setWorkingHours(75);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup2.setWorkingHours(25);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(100));

        try {
            orderLine.setWorkHours(80);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(80));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(60));
        assertThat(hoursGroup.getPercentage(), equalTo(new BigDecimal(0.75)
                .setScale(2)));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(20));
        assertThat(hoursGroup2.getPercentage(), equalTo(new BigDecimal(0.25)
                .setScale(2)));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 40h, 60h NO_FIXED
     * and 100h FIXED_HOURS.
     *
     * Trying to set work hours of {@link OrderLine} to 300h.
     *
     * Expected: {@link OrderLine} with 300h. {@link HoursGroup} with 80h,
     * {@link HoursGroup} with 120h and {@link HoursGroup} with 100h.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupNoFixedAndHoursGroupFixedHoursIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(40);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(60);
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup3.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        try {
            orderLine.setWorkHours(300);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(300));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(80));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(120));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(100));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 50h, 50h NO_FIXED
     * and 50h FIXED_HOURS.
     *
     * Trying to set work hours of {@link OrderLine} to 70h.
     *
     * Expected: {@link OrderLine} with 70h. {@link HoursGroup} with 10h,
     * {@link HoursGroup} with 10h and {@link HoursGroup} with 50h.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupNoFixedAndHoursGroupFixedHoursDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup3.setWorkingHours(50);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(150));

        try {
            orderLine.setWorkHours(70);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(70));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(10));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(10));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(50));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 50h, 50h NO_FIXED
     * and 50h FIXED_HOURS.
     *
     * Trying to set work hours of {@link OrderLine} to 25h.
     *
     * Expected: Exception. {@link OrderLine} with 150h. {@link HoursGroup} with
     * 50h, {@link HoursGroup} with 50h and {@link HoursGroup} with 50h.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupNoFixedAndHoursGroupFixedHoursDecreaseValueIllegal() {
        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup3.setWorkingHours(50);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(150));

        try {
            orderLine.setWorkHours(25);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // OK
        }

        assertThat(orderLine.getWorkHours(), equalTo(150));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(50));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 50h, 50h NO_FIXED
     * and 100h 50% FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 300h.
     *
     * Expected: {@link OrderLine} with 300h. {@link HoursGroup} with 75h,
     * {@link HoursGroup} with 75h and {@link HoursGroup} with 150h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupNoFixedAndHoursGroupFixedPercentageIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup3.setWorkingHours(100);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        try {
            orderLine.setWorkHours(300);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(300));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(75));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(75));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(150));
        assertThat(hoursGroup3.getPercentage(), equalTo(new BigDecimal(0.50)
                .setScale(2)));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 40h, 60h NO_FIXED
     * and 100h 50% FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 100h.
     *
     * Expected: {@link OrderLine} with 100h. {@link HoursGroup} with 20h,
     * {@link HoursGroup} with 30h and {@link HoursGroup} with 50h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupNoFixedAndHoursGroupFixedPercentageDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(40);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup2.setWorkingHours(60);
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup3.setWorkingHours(100);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        try {
            orderLine.setWorkHours(100);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(100));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(20));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(30));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup3.getPercentage(), equalTo(new BigDecimal(0.50)
                .setScale(2)));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 50h NO_FIXED, 50h
     * FIXED_HOURS and 100h 50% FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 300h.
     *
     * Expected: {@link OrderLine} with 300h. {@link HoursGroup} with 100h,
     * {@link HoursGroup} with 50h and {@link HoursGroup} with 150h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupFixedHoursAndHoursGroupFixedPercentageIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup3.setWorkingHours(100);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        try {
            orderLine.setWorkHours(300);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(300));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(100));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(150));
        assertThat(hoursGroup3.getPercentage(), equalTo(new BigDecimal(0.50)
                .setScale(2)));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 50h NO_FIXED, 50h
     * FIXED_HOURS and 100h 50% FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 150h.
     *
     * Expected: {@link OrderLine} with 150h. {@link HoursGroup} with 25h,
     * {@link HoursGroup} with 50h and {@link HoursGroup} with 75h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupFixedHoursAndHoursGroupFixedPercentageDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup3.setWorkingHours(100);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        try {
            orderLine.setWorkHours(150);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(150));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(25));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(75));
        assertThat(hoursGroup3.getPercentage(), equalTo(new BigDecimal(0.50)
                .setScale(2)));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 50h NO_FIXED, 50h
     * FIXED_HOURS and 100h 50% FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 50h.
     *
     * Expected: Exception. {@link OrderLine} with 200h. {@link HoursGroup} with
     * 50h, {@link HoursGroup} with 50h and {@link HoursGroup} with 100h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupFixedHoursAndHoursGroupFixedPercentageDecreaseValueIllegal() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup3.setWorkingHours(100);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        try {
            orderLine.setWorkHours(50);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // OK
        }

        assertThat(orderLine.getWorkHours(), equalTo(200));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(100));
        assertThat(hoursGroup3.getPercentage(), equalTo(new BigDecimal(0.50)
                .setScale(2)));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 50h NO_FIXED, 50h
     * and 100h FIXED_HOURS.
     *
     * Trying to set work hours of {@link OrderLine} to 400h.
     *
     * Expected: {@link OrderLine} with 400h. {@link HoursGroup} with 250h,
     * {@link HoursGroup} with 50h and {@link HoursGroup} with 100h.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupFixedHoursAndHoursGroupFixedHoursIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup3.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        try {
            orderLine.setWorkHours(400);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(400));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(250));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(100));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 50h NO_FIXED, 50h
     * and 100h FIXED_HOURS.
     *
     * Trying to set work hours of {@link OrderLine} to 175h.
     *
     * Expected: {@link OrderLine} with 175h. {@link HoursGroup} with 25h,
     * {@link HoursGroup} with 50h and {@link HoursGroup} with 100h.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupFixedHoursAndHoursGroupFixedHoursDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_HOURS);
        hoursGroup3.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        try {
            orderLine.setWorkHours(175);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(175));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(25));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(100));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 50h NO_FIXED, 50h
     * 25% and 100h 50% FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 400h.
     *
     * Expected: {@link OrderLine} with 400h. {@link HoursGroup} with 100h,
     * {@link HoursGroup} with 100h 25% and {@link HoursGroup} with 200h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupFixedPercentageAndHoursGroupFixedPercentageIncreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup2.setWorkingHours(50);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup3.setWorkingHours(100);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        try {
            orderLine.setWorkHours(400);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(400));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(100));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(100));
        assertThat(hoursGroup2.getPercentage(), equalTo(new BigDecimal(0.25)
                .setScale(2)));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(200));
        assertThat(hoursGroup3.getPercentage(), equalTo(new BigDecimal(0.50)
                .setScale(2)));

    }

    /**
     * An {@link OrderLine} with three {@link HoursGroup} of 50h NO_FIXED, 50h
     * 25% and 100h 50% FIXED_PERCENTAGE.
     *
     * Trying to set work hours of {@link OrderLine} to 100h.
     *
     * Expected: {@link OrderLine} with 400h. {@link HoursGroup} with 25h,
     * {@link HoursGroup} with 25h 25% and {@link HoursGroup} with 50h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupFixedPercentageAndHoursGroupFixedPercentageDecreaseValue() {

        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();
        hoursGroup.setHoursPolicy(HoursPolicies.NO_FIXED);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = new HoursGroup();
        hoursGroup2.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup2.setWorkingHours(50);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));
        HoursGroup hoursGroup3 = new HoursGroup();
        hoursGroup3.setHoursPolicy(HoursPolicies.FIXED_PERCENTAGE);
        hoursGroup3.setWorkingHours(100);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        try {
            orderLine.setWorkHours(100);
        } catch (IllegalArgumentException e) {
            fail("It should not throw an exception");
        }

        assertThat(orderLine.getWorkHours(), equalTo(100));
        assertThat(orderLine.getHoursGroups().size(), equalTo(3));
        assertThat(hoursGroup.getWorkingHours(), equalTo(25));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(25));
        assertThat(hoursGroup2.getPercentage(), equalTo(new BigDecimal(0.25)
                .setScale(2)));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(50));
        assertThat(hoursGroup3.getPercentage(), equalTo(new BigDecimal(0.50)
                .setScale(2)));

    }

    @Test
    public void testAddNewEmptyHoursGroup() {
        OrderLine orderLine = new OrderLine();
        HoursGroup hoursGroup = new HoursGroup();

        orderLine.addHoursGroup(hoursGroup);

        assertThat(orderLine.getWorkHours(), equalTo(0));
        assertThat(orderLine.getHoursGroups().size(), equalTo(1));
    }

}
