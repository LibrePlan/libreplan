package org.navalplanner.web.planner.allocation;

import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.zkoss.zul.Intbox;

class FormBinder {

    private Intbox assignedHoursComponent;

    private final ResourceAllocationsBeingEdited resourceAllocationsBeingEdited;
    private AggregateOfResourceAllocations aggregate;

    public FormBinder(
            ResourceAllocationsBeingEdited resourceAllocationsBeingEdited) {
        this.resourceAllocationsBeingEdited = resourceAllocationsBeingEdited;
        this.aggregate = this.resourceAllocationsBeingEdited
                .getInitialAggregate();
    }

    public void setAssignedHoursComponent(Intbox assignedHoursComponent) {
        this.assignedHoursComponent = assignedHoursComponent;
        this.assignedHoursComponent.setDisabled(resourceAllocationsBeingEdited
                .getCalculatedValue() == CalculatedValue.NUMBER_OF_HOURS);
        this.assignedHoursComponent.setValue(aggregate.getTotalHours());
    }

    public void setCalculatedValue(CalculatedValue calculatedValue) {
        if (calculatedValue == CalculatedValue.RESOURCES_PER_DAY) {
            throw new RuntimeException(CalculatedValue.RESOURCES_PER_DAY
                    + " not implemented yet");
        }
        if (calculatedValue == resourceAllocationsBeingEdited
                .getCalculatedValue()) {
            return;
        }
        resourceAllocationsBeingEdited.setCalculatedValue(calculatedValue);
    }

    public CalculatedValue getCalculatedValue() {
        return resourceAllocationsBeingEdited.getCalculatedValue();
    }

}
