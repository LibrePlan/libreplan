package org.navalplanner.business.orders.entities;

import java.math.BigDecimal;

public class HoursGroup {

    private Long id;

    public Long getId() {
        return id;
    }

    private Integer workingHours;

    private BigDecimal percentage;

    public enum HoursPolicies {
        NO_FIXED, FIXED_HOURS, FIXED_PERCENTAGE
    };

    private HoursPolicies hoursPolicy = HoursPolicies.NO_FIXED;

    public void setWorkingHours(Integer workingHours) {
        this.workingHours = workingHours;
    }

    public Integer getWorkingHours() {
        return workingHours;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setHoursPolicy(HoursPolicies hoursPolicy) {
        this.hoursPolicy = hoursPolicy;
    }

    public HoursPolicies getHoursPolicy() {
        return hoursPolicy;
    }

}
