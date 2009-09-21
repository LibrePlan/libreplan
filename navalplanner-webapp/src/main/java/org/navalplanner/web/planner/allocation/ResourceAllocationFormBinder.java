package org.navalplanner.web.planner.allocation;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.planner.entities.CalculatedValue;

public class ResourceAllocationFormBinder {

    private CalculatedValue calculatedValue;
    private final IResourceAllocationModel resourceAllocationModel;

    public ResourceAllocationFormBinder(CalculatedValue calculatedValue,
            IResourceAllocationModel resourceAllocationModel) {
        Validate.notNull(resourceAllocationModel);
        Validate.notNull(resourceAllocationModel);
        this.calculatedValue = calculatedValue;
        this.resourceAllocationModel = resourceAllocationModel;
    }

}
