package org.navalplanner.web.planner.allocation;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.zkoss.zul.Intbox;

public class ResourceAllocationFormBinder {

    private CalculatedValue calculatedValue;
    private final IResourceAllocationModel resourceAllocationModel;
    private Intbox assignedHoursComponent;

    public ResourceAllocationFormBinder(CalculatedValue calculatedValue,
            IResourceAllocationModel resourceAllocationModel) {
        Validate.notNull(resourceAllocationModel);
        Validate.notNull(resourceAllocationModel);
        this.calculatedValue = calculatedValue;
        this.resourceAllocationModel = resourceAllocationModel;
    }

    public void setAssignedHoursComponent(Intbox assignedHoursComponent) {
        this.assignedHoursComponent = assignedHoursComponent;
        this.assignedHoursComponent
                .setDisabled(calculatedValue == CalculatedValue.NUMBER_OF_HOURS);
        this.assignedHoursComponent.setValue(resourceAllocationModel.getTask()
                .getAssignedHours());
    }

}
