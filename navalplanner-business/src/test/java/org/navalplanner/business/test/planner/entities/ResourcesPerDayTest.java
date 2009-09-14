package org.navalplanner.business.test.planner.entities;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
        assertThat(units.getAmount(), equalTo(2));
    }

    @Test
    public void canBeConvertedToHoursGivenTheWorkingDayHours() {
        ResourcesPerDay units = ResourcesPerDay.amount(2);
        assertThat(units.asHoursGivenResourceWorkingDayOf(8), equalTo(16));
    }

}
