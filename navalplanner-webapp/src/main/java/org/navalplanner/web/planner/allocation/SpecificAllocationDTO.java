package org.navalplanner.web.planner.allocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * The information required for creating a {@link SpecificResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class SpecificAllocationDTO extends AllocationDTO {

    public static List<SpecificAllocationDTO> withResource(
            List<SpecificAllocationDTO> specific, Worker worker) {
        List<SpecificAllocationDTO> result = new ArrayList<SpecificAllocationDTO>();
        for (SpecificAllocationDTO specificAllocationDTO : specific) {
            if (areEquals(specificAllocationDTO.getResource(), worker)) {
                result.add(specificAllocationDTO);
            }
        }
        return result;
    }

    private static boolean areEquals(Resource one, Resource other) {
        if (one == other)
            return true;
        if (one == null || other == null)
            return false;
        return one.equals(other);
    }

    public static List<SpecificAllocationDTO> getSpecific(
            Collection<? extends AllocationDTO> currentAllocations) {
        List<SpecificAllocationDTO> result = new ArrayList<SpecificAllocationDTO>();
        for (AllocationDTO allocationDTO : currentAllocations) {
            if (allocationDTO instanceof SpecificAllocationDTO) {
                result.add((SpecificAllocationDTO) allocationDTO);
            }
        }
        return result;
    }

    public static SpecificAllocationDTO from(SpecificResourceAllocation specific) {
        SpecificAllocationDTO result = forResource(specific.getWorker());
        result.setResourcesPerDay(specific.getResourcesPerDay());
        return result;
    }

    public static SpecificAllocationDTO forResource(Worker worker) {
        SpecificAllocationDTO result = new SpecificAllocationDTO();
        result.setName(worker.getName());
        result.setResource(worker);
        result.setResourcesPerDay(ResourcesPerDay.amount(1));
        return result;
    }

    private Resource resource;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

}
