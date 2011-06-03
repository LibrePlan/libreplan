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

package org.navalplanner.business.test.orders.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.test.planner.entities.TaskTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link OrderLine}. <br />
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class OrderLineTest {

    @Resource
    private IDataBootstrap defaultAdvanceTypesBootstrapListener;

    @Before
    public void loadRequiredaData() {
        defaultAdvanceTypesBootstrapListener.loadRequiredData();
    }

    @Test
    public void parentPropertyMustBeSetWhenAddingOrderLineToContainer() {
        OrderLineGroup orderLineGroup = OrderLineGroup.create();
        orderLineGroup.useSchedulingDataFor(TaskTest.mockOrderVersion());
        OrderLine orderLine = OrderLine.create();
        orderLineGroup.add(orderLine);
        assertThat(orderLine.getParent(), equalTo(orderLineGroup));
    }

    /**
     * An empty {@link OrderLine} without any {@link HoursGroup}. Trying to set
     * work hours of {@link OrderLine} to 100h. Expected: {@link OrderLine} with
     * 100h, with one {@link HoursGroup} with 100h NO_FIXED.
     */
    @Test
    public void testSetWorkHoursHoursEmptyOrderLine() {

        OrderLine orderLine = OrderLine.create();

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
        assertFalse(hoursGroup.isFixedPercentage());
    }

    /**
     * An empty {@link OrderLine} without any {@link HoursGroup}. Trying to set
     * work hours of {@link OrderLine} to -100h. Expected: Exception.
     */
    @Test
    public void testSetWorkHoursHoursEmptyOrderLineIllegal() {

        OrderLine orderLine = OrderLine.create();

        assertThat(orderLine.getWorkHours(), equalTo(0));

        try {
            orderLine.setWorkHours(-100);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // Ok
        }

        assertThat(orderLine.getWorkHours(), equalTo(0));
        assertThat(orderLine.getHoursGroups().size(), equalTo(0));

    }

    /**
     * An {@link OrderLine} with just one {@link HoursGroup} of 100h NO_FIXED.
     * Trying to set work hours of {@link OrderLine} to 120h. Expected:
     * {@link OrderLine} with 120h. {@link HoursGroup} with 120h.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedIncreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
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
     * Trying to set work hours of {@link OrderLine} to 75h. Expected:
     * {@link OrderLine} with 75h. {@link HoursGroup} with 75h.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedDecreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
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
     * An {@link OrderLine} with just one {@link HoursGroup} of 100h 100%
     * FIXED_PERCENTAGE. Trying to set work hours of {@link OrderLine} to 120h.
     * Expected: {@link OrderLine} with 120h. {@link HoursGroup} with 120h 100%.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageIncreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);

        hoursGroup.setFixedPercentage(true);
        hoursGroup.setPercentage(new BigDecimal(1).setScale(2));

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
     * FIXED_PERCENTAGE. Trying to set work hours of {@link OrderLine} to 75h.
     * Expected: {@link OrderLine} with 100h. {@link HoursGroup} with 75h 100%.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageDecreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);

        hoursGroup.setFixedPercentage(true);
        hoursGroup.setPercentage(new BigDecimal(1).setScale(2));

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
     * NO_FIXED. Trying to set work hours of {@link OrderLine} to 200h.
     * Expected: {@link OrderLine} with 200h. {@link HoursGroup} with 133h and
     * HoursGroup with 66h.
     */
    @Test
    public void testSetWorkHoursTwoHoursGroupNoFixedIncreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(100);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
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
     * NO_FIXED. Trying to set work hours of {@link OrderLine} to 50h. Expected:
     * {@link OrderLine} with 50h. {@link HoursGroup} with 33h and
     * {@link HoursGroup} with 16h.
     */
    @Test
    public void testSetWorkHoursTwoHoursGroupNoFixedDecreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(100);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
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
     * An {@link OrderLine} with two {@link HoursGroup} of 75h 75%
     * FIXED_PERCENTAGE and 25h NO_FIXED. Trying to set work hours of
     * {@link OrderLine} to 200h. Expected: {@link OrderLine} with 200h.
     * {@link HoursGroup} with 150h 75% and {@link HoursGroup} with 50h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageAndHoursGroupNoFixedIncreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(75);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(25);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        hoursGroup.setFixedPercentage(true);
        hoursGroup.setPercentage(new BigDecimal(0.75).setScale(2));

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
     * FIXED_PERCENTAGE and 25h NO_FIXED. Trying to set work hours of
     * {@link OrderLine} to 50h. Expected: {@link OrderLine} with 50h.
     * {@link HoursGroup} with 37h 75% and HoursGroup with 13h.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageAndHoursGroupNoFixedDecreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(75);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(25);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        hoursGroup.setFixedPercentage(true);
        hoursGroup.setPercentage(new BigDecimal(0.75).setScale(2));

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
     * An {@link OrderLine} with two {@link HoursGroup} of 75h 75% and 25h 25%
     * FIXED_PERCENTAGE. Trying to set work hours of {@link OrderLine} to 200h.
     * Expected: {@link OrderLine} with 200h. {@link HoursGroup} with 150h 75%
     * and {@link HoursGroup} with 50h 25%.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageAndHoursGroupFixedPercentageIncreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(75);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(25);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        hoursGroup.setFixedPercentage(true);
        hoursGroup2.setFixedPercentage(true);
        hoursGroup.setPercentage(new BigDecimal(0.75).setScale(2));
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));

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
     * FIXED_PERCENTAGE. Trying to set work hours of {@link OrderLine} to 80h.
     * Expected: {@link OrderLine} with 80h. {@link HoursGroup} with 60h 75% and
     * {@link HoursGroup} with 20h 25%.
     */
    @Test
    public void testSetWorkHoursHoursGroupFixedPercentageAndHoursGroupFixedPercentageDecreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(75);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(25);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        hoursGroup.setFixedPercentage(true);
        hoursGroup.setPercentage(new BigDecimal(0.75).setScale(2));
        hoursGroup2.setFixedPercentage(true);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));

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
     * An {@link OrderLine} with three {@link HoursGroup} of 50h, 50h NO_FIXED
     * and 100h 50% FIXED_PERCENTAGE. Trying to set work hours of
     * {@link OrderLine} to 300h. Expected: {@link OrderLine} with 300h.
     * {@link HoursGroup} with 75h, {@link HoursGroup} with 75h and
     * {@link HoursGroup} with 150h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupNoFixedAndHoursGroupFixedPercentageIncreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = HoursGroup.create(orderLine);
        hoursGroup3.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        hoursGroup3.setFixedPercentage(true);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));

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
     * and 100h 50% FIXED_PERCENTAGE. Trying to set work hours of
     * {@link OrderLine} to 100h. Expected: {@link OrderLine} with 100h.
     * {@link HoursGroup} with 20h, {@link HoursGroup} with 30h and
     * {@link HoursGroup} with 50h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupNoFixedAndHoursGroupFixedPercentageDecreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(40);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(60);
        HoursGroup hoursGroup3 = HoursGroup.create(orderLine);
        hoursGroup3.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        hoursGroup3.setFixedPercentage(true);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));

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
     * 25% and 100h 50% FIXED_PERCENTAGE. Trying to set work hours of
     * {@link OrderLine} to 400h. Expected: {@link OrderLine} with 400h.
     * {@link HoursGroup} with 100h, {@link HoursGroup} with 100h 25% and
     * {@link HoursGroup} with 200h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupFixedPercentageAndHoursGroupFixedPercentageIncreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = HoursGroup.create(orderLine);
        hoursGroup3.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        hoursGroup2.setFixedPercentage(true);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));
        hoursGroup3.setFixedPercentage(true);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));

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
     * 25% and 100h 50% FIXED_PERCENTAGE. Trying to set work hours of
     * {@link OrderLine} to 100h. Expected: {@link OrderLine} with 400h.
     * {@link HoursGroup} with 25h, {@link HoursGroup} with 25h 25% and
     * {@link HoursGroup} with 50h 50%.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedAndHoursGroupFixedPercentageAndHoursGroupFixedPercentageDecreaseValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(50);
        HoursGroup hoursGroup3 = HoursGroup.create(orderLine);
        hoursGroup3.setWorkingHours(100);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        hoursGroup2.setFixedPercentage(true);
        hoursGroup2.setPercentage(new BigDecimal(0.25).setScale(2));
        hoursGroup3.setFixedPercentage(true);
        hoursGroup3.setPercentage(new BigDecimal(0.50).setScale(2));

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

    /**
     * An {@link OrderLine} with two {@link HoursGroup} of 0h NO_FIXED. Trying
     * to set work hours of {@link OrderLine} to 200h. Expected:
     * {@link OrderLine} with 200h. {@link HoursGroup} with 100h and
     * {@link HoursGroup} with 100h.
     */
    @Test
    public void testSetWorkHoursHoursGroupNoFixedZeroValue() {

        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(0);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(0);
        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(0));

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

    @Test
    public void testAddNewEmptyHoursGroup() {
        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);

        orderLine.addHoursGroup(hoursGroup);

        assertThat(orderLine.getWorkHours(), equalTo(0));
        assertThat(orderLine.getHoursGroups().size(), equalTo(1));
    }

    @Test
    public void testSetWorkingHoursIllegal() {
        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);

        try {
            hoursGroup.setWorkingHours(-50);
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // Ok
        }

    }

    @Test
    public void testSetPercentageIllegal() {
        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(150);

        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        hoursGroup.setFixedPercentage(true);
        hoursGroup.setPercentage(new BigDecimal(0.5).setScale(2));

        orderLine.recalculateHoursGroups();

        hoursGroup2.setFixedPercentage(true);
        try {
            hoursGroup2.setPercentage(new BigDecimal(0.75).setScale(2));
            fail("It should throw an exception");
        } catch (IllegalArgumentException e) {
            // Ok
        }

    }

    @Test
    public void testSetPercentageTwoHoursGroupIncrease() {
        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(150);

        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(200));

        hoursGroup.setFixedPercentage(true);
        hoursGroup.setPercentage(new BigDecimal(0.5).setScale(2));

        orderLine.recalculateHoursGroups();

        assertThat(orderLine.getWorkHours(), equalTo(200));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(100));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(100));
    }

    @Test
    public void testSetPercentageTwoHoursGroupDecrease() {
        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(100);

        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);

        assertThat(orderLine.getWorkHours(), equalTo(150));

        hoursGroup.setFixedPercentage(true);
        hoursGroup.setPercentage(new BigDecimal(0.25).setScale(2));

        orderLine.recalculateHoursGroups();

        assertThat(orderLine.getWorkHours(), equalTo(150));
        assertThat(orderLine.getHoursGroups().size(), equalTo(2));
        assertThat(hoursGroup.getWorkingHours(), equalTo(37));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(113));
    }

    @Test
    public void testSetPercentageThreeHoursGroupIncrease() {
        OrderLine orderLine = OrderLine.create();
        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(50);
        HoursGroup hoursGroup2 = HoursGroup.create(orderLine);
        hoursGroup2.setWorkingHours(150);
        HoursGroup hoursGroup3 = HoursGroup.create(orderLine);
        hoursGroup3.setWorkingHours(200);

        orderLine.addHoursGroup(hoursGroup);
        orderLine.addHoursGroup(hoursGroup2);
        orderLine.addHoursGroup(hoursGroup3);

        assertThat(orderLine.getWorkHours(), equalTo(400));

        hoursGroup.setFixedPercentage(true);
        hoursGroup.setPercentage(new BigDecimal(0.5).setScale(2));

        orderLine.recalculateHoursGroups();

        assertThat(orderLine.getWorkHours(), equalTo(400));
        assertThat(orderLine.getHoursGroups().size(), equalTo(4));
        assertThat(hoursGroup.getWorkingHours(), equalTo(200));
        assertThat(hoursGroup2.getWorkingHours(), equalTo(85));
        assertThat(hoursGroup3.getWorkingHours(), equalTo(114));
    }

    @Test
    public void createOrderLineWithAnHoursGroupTakingAll() {
        int[] hoursValues = { 0, 100, 10, 30 };
        for (int hours : hoursValues) {
            OrderLine orderLine = OrderLine
                    .createOrderLineWithUnfixedPercentage(hours);
            assertThat(orderLine.getWorkHours(), equalTo(hours));
            assertThat(orderLine.getHoursGroups().size(), equalTo(1));
            orderLine.setWorkHours(20);
            assertThat(orderLine.getWorkHours(), equalTo(20));
            assertThat(orderLine.getHoursGroups().size(), equalTo(1));
        }
    }

}
