package org.navalplanner.business.planner.entities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

/**
 * Computes aggregate values on a set{@link ResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AggregateOfResourceAllocations {

    private Set<ResourceAllocation<?>> resourceAllocations;

    public AggregateOfResourceAllocations(
            Collection<? extends ResourceAllocation<?>> allocations) {
        Validate.notNull(allocations);
        Validate.noNullElements(allocations);
        this.resourceAllocations = new HashSet<ResourceAllocation<?>>(
                allocations);
    }

    public int getTotalHours() {
        int sum = 0;
        for (ResourceAllocation<?> resourceAllocation : resourceAllocations) {
            sum += resourceAllocation.getAssignedHours();
        }
        return sum;
    }

}
