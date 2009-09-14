package org.navalplanner.web.planner.allocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.navalplanner.business.planner.entities.ResourceAllocation;

/**
 * The information that must be introduced to create a
 * {@link ResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class AllocationDTO {

    public static List<GenericAllocationDTO> getGeneric(
            Collection<? extends AllocationDTO> all) {
        List<GenericAllocationDTO> result = new ArrayList<GenericAllocationDTO>();
        for (AllocationDTO dto : all) {
            if (dto.isGeneric()) {
                result.add((GenericAllocationDTO) dto);
            }
        }
        return result;
    }

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
