package org.navalplanner.web.planner.allocation;

import org.navalplanner.business.planner.entities.GenericResourceAllocation;

/**
 * The information required for creating a {@link GenericResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GenericAllocationDTO extends AllocationDTO {

    @Override
    public boolean isGeneric() {
        return true;
    }

}
