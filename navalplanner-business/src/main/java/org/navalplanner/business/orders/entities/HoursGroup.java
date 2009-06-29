package org.navalplanner.business.orders.entities;

import java.math.BigDecimal;

import org.hibernate.validator.NotNull;

public class HoursGroup implements Cloneable {

    private Long id;

    public Long getId() {
        return id;
    }

    @NotNull
    private Integer workingHours = 0;

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
