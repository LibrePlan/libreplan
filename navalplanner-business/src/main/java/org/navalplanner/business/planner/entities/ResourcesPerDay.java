package org.navalplanner.business.planner.entities;

import org.apache.commons.lang.Validate;

public class ResourcesPerDay {

    private final int amount;

    public static ResourcesPerDay amount(int amount) {
        return new ResourcesPerDay(amount);
    }

    private ResourcesPerDay(int amount) {
        Validate.isTrue(amount >= 0);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public int asHoursGivenResourceWorkingDayOf(
            Integer resourceWorkingDayHours) {
        return getAmount() * resourceWorkingDayHours;
    }
}
