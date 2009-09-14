package org.navalplanner.business.planner.entities;

import org.apache.commons.lang.Validate;

public class ResourcePerDayUnit {

    private final int amount;

    public static ResourcePerDayUnit amount(int amount) {
        return new ResourcePerDayUnit(amount);
    }

    private ResourcePerDayUnit(int amount) {
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
