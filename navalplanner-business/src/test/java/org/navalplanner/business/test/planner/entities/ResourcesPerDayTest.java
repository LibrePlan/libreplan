package org.navalplanner.business.test.planner.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.navalplanner.business.planner.entities.ResourcesPerDay;

public class ResourcesPerDayTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotHaveANegativeNumberOfUnits() {
        ResourcesPerDay.amount(-1);
    }

    @Test
    public void theUnitsAmoutCanBeRetrieved() {
        ResourcesPerDay units = ResourcesPerDay.amount(2);
        assertThat(units.getAmount(), hasIntegerPart(2));
    }

    private Matcher<BigDecimal> hasIntegerPart(final int integer) {
        return new BaseMatcher<BigDecimal>() {

            @Override
            public boolean matches(Object arg) {
                if (arg instanceof BigDecimal) {
                    BigDecimal value = (BigDecimal) arg;
                    return value.intValue() == integer;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("must have an integer part of" + integer);
            }
        };
    }

    @Test
    public void theUnitsAmountCanBeADecimalValue() {
        ResourcesPerDay resourcesPerDay = ResourcesPerDay
                .amount(new BigDecimal(2.2));
        assertThat(resourcesPerDay.getAmount(), hasIntegerPart(2));
    }

    @Test
    public void theAmountIsConvertedToABigDecimalOfScale2() {
        ResourcesPerDay resourcesPerDay = ResourcesPerDay
                .amount(new BigDecimal(2.2));
        assertThat(resourcesPerDay.getAmount().scale(), equalTo(2));
    }

    @Test
    public void ifTheAmountSpecifiedHasBiggerScaleThan2ItIsRoundedHalfUp() {
        BigDecimal[] examples = { new BigDecimal(2.236),
                new BigDecimal(2235).movePointLeft(3), new BigDecimal(2.24),
                new BigDecimal(2.2449) };
        for (BigDecimal example : examples) {
            ResourcesPerDay resourcesPerDay = ResourcesPerDay.amount(example);
            assertThat(resourcesPerDay.getAmount().scale(), equalTo(2));
            assertThat(
                    resourcesPerDay.getAmount().movePointRight(2).intValue(),
                    equalTo(224));
        }
    }

    @Test
    public void canBeConvertedToHoursGivenTheWorkingDayHours() {
        ResourcesPerDay units = ResourcesPerDay.amount(2);
        assertThat(units.asHoursGivenResourceWorkingDayOf(8), equalTo(16));
    }

    @Test
    public void ifTheAmountIsDecimalTheRoundingIs() {
        ResourcesPerDay units = ResourcesPerDay.amount(new BigDecimal(2.4));
        assertThat(units.asHoursGivenResourceWorkingDayOf(8), equalTo(19));
        assertThat(units.asHoursGivenResourceWorkingDayOf(10), equalTo(24));
        assertThat(units.asHoursGivenResourceWorkingDayOf(2), equalTo(5));
    }

    @Test
    public void twoResourcesPerDayAreEqualsIfNormalizeToTheSameAmount() {
        ResourcesPerDay a = ResourcesPerDay.amount(new BigDecimal(2.001));
        ResourcesPerDay b = ResourcesPerDay.amount(2);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, b);
    }

}
