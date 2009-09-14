package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang.Validate;

public class ResourcesPerDay {

    private final BigDecimal amount;

    public static ResourcesPerDay amount(int amount) {
        return new ResourcesPerDay(new BigDecimal(amount));
    }

    public static ResourcesPerDay amount(BigDecimal decimal) {
        return new ResourcesPerDay(decimal);
    }

    private ResourcesPerDay(BigDecimal amount) {
        Validate.isTrue(amount.intValue() >= 0);
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int asHoursGivenResourceWorkingDayOf(
            Integer resourceWorkingDayHours) {
        return getAmount().multiply(new BigDecimal(resourceWorkingDayHours))
                .setScale(0, RoundingMode.HALF_UP).intValue();
    }

    @Override
    public int hashCode() {
        return amount.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof ResourcesPerDay) {
            ResourcesPerDay other = (ResourcesPerDay) obj;
            return amount.equals(other.getAmount());
        }
        return false;
    }

}
