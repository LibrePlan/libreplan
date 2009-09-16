package org.navalplanner.web.planner.allocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;

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

    private ResourceAllocation origin;

    private String name;

    private ResourcesPerDay resourcesPerDay;

    public boolean isCreating() {
        return origin == null;
    }

    public boolean isModifying() {
        return origin != null;
    }

    public ResourceAllocation getOrigin() {
        return origin;
    }

    protected void setOrigin(ResourceAllocation allocation) {
        this.origin = allocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourcesPerDay getResourcesPerDay() {
        return this.resourcesPerDay;
    }

    public void setResourcesPerDay(ResourcesPerDay resourcesPerDay) {
        this.resourcesPerDay = resourcesPerDay;
    }

    public abstract boolean isGeneric();

}
