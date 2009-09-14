package org.navalplanner.web.planner.allocation;

import java.math.BigDecimal;

import org.navalplanner.business.planner.entities.ResourceAllocation;

/**
 * The information that must be introduced to create a
 * {@link ResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class AllocationDTO {

    private String name;

    private BigDecimal percentage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public abstract boolean isGeneric();

}
