package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;

import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;

/**
 * The information required for creating a {@link GenericResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class GenericAllocationDTO extends AllocationDTO {

    public static GenericAllocationDTO createDefault() {
        GenericAllocationDTO result = new GenericAllocationDTO();
        result.setName(_("Generic"));
        result.setResourcesPerDay(ResourcesPerDay.amount(0));
        return result;
    }

    public static GenericAllocationDTO from(
            GenericResourceAllocation resourceAllocation) {
        GenericAllocationDTO result = createDefault();
        result.setResourcesPerDay(resourceAllocation.getResourcesPerDay());
        result.setOrigin(resourceAllocation);
        return result;
    }

    @Override
    public boolean isGeneric() {
        return true;
    }

    public static Collection<GenericAllocationDTO> toGenericAllocations(
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        ArrayList<GenericAllocationDTO> result = new ArrayList<GenericAllocationDTO>();
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                result.add(from((GenericResourceAllocation) resourceAllocation));
            }
        }
        return result;
    }
}
