package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.business.planner.entities.GenericResourceAllocation;

/**
 * The information required for creating a {@link GenericResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GenericAllocationDTO extends AllocationDTO {

    public static GenericAllocationDTO createDefault() {
        GenericAllocationDTO result = new GenericAllocationDTO();
        result.setName(_("Generic"));
        return result;
    }

    public static GenericAllocationDTO from(
            GenericResourceAllocation resourceAllocation) {
        GenericAllocationDTO result = createDefault();
        result.setPercentage(resourceAllocation.getPercentage());
        return result;
    }

    @Override
    public boolean isGeneric() {
        return true;
    }
}
