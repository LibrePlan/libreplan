package org.navalplanner.web.planner.allocation;

import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;

class FormBinder {

    private Intbox assignedHoursComponent;

    private final ResourceAllocationsBeingEdited resourceAllocationsBeingEdited;
    private AggregateOfResourceAllocations aggregate;

    private Datebox taskStartDateBox;

    private Intbox taskElapsedDays;

    public FormBinder(
            ResourceAllocationsBeingEdited resourceAllocationsBeingEdited) {
        this.resourceAllocationsBeingEdited = resourceAllocationsBeingEdited;
        this.aggregate = this.resourceAllocationsBeingEdited
                .getInitialAggregate();
    }

    public void setAssignedHoursComponent(Intbox assignedHoursComponent) {
        this.assignedHoursComponent = assignedHoursComponent;
        assignedHoursComponentDisabilityRule();
        this.assignedHoursComponent.setValue(aggregate.getTotalHours());
    }

    private void assignedHoursComponentDisabilityRule() {
        this.assignedHoursComponent.setDisabled(resourceAllocationsBeingEdited
                .getCalculatedValue() == CalculatedValue.NUMBER_OF_HOURS);
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
        applyDisabledRules();
    }

    private void applyDisabledRules() {
        assignedHoursComponentDisabilityRule();
        taskElapsedDaysDisabilityRule();
    }

    public CalculatedValue getCalculatedValue() {
        return resourceAllocationsBeingEdited.getCalculatedValue();
    }

    public void setTaskStartDateBox(Datebox taskStartDateBox) {
        this.taskStartDateBox = taskStartDateBox;
        this.taskStartDateBox.setDisabled(true);
        this.taskStartDateBox.setValue(resourceAllocationsBeingEdited.getTask()
                .getStartDate());
    }

    public void setTaskElapsedDays(Intbox taskElapsedDays) {
        this.taskElapsedDays = taskElapsedDays;
        taskElapsedDaysDisabilityRule();
        this.taskElapsedDays.setValue(resourceAllocationsBeingEdited.getTask()
                .getDaysDuration());
    }

    private void taskElapsedDaysDisabilityRule() {
        this.taskElapsedDays.setDisabled(true);
    }

}
