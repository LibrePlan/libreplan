package org.navalplanner.business.test.planner.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.navalplanner.business.planner.entities.ResourcePerDayUnit;

public class ResourcePerDayUnitTest {

    @Test(expected = IllegalArgumentException.class)
    public void cannotHaveANegativeNumberOfUnits() {
        ResourcePerDayUnit.amount(-1);
    }

    @Test
    public void theUnitsAmoutCanBeRetrieved() {
        ResourcePerDayUnit units = ResourcePerDayUnit.amount(2);
        assertThat(units.getAmount(), equalTo(2));
    }

}
