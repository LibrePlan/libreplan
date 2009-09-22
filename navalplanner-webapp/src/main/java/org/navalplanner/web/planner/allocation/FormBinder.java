package org.navalplanner.web.planner.allocation;

import java.util.HashMap;
import java.util.Map;

import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.impl.api.InputElement;

class FormBinder {

    private Intbox assignedHoursComponent;

    private final ResourceAllocationsBeingEdited resourceAllocationsBeingEdited;
    private AggregateOfResourceAllocations aggregate;

    private Datebox taskStartDateBox;

    private Intbox taskElapsedDays;

    private Button applyButton;

    private Map<AllocationDTO, Decimalbox> resourcesPerDayInputsByAllocationDTO = new HashMap<AllocationDTO, Decimalbox>();

    private EventListener onChangeEnableApply = new EventListener() {

        @Override
        public void onEvent(Event event) throws Exception {
            Component target = event.getTarget();
            if (target instanceof InputElement) {
                InputElement inputElement = (InputElement) target;
                if (inputElement.isDisabled()) {
                    return;
                }
            }
            applyButton.setDisabled(false);
        }
    };

    public FormBinder(
            ResourceAllocationsBeingEdited resourceAllocationsBeingEdited) {
        this.resourceAllocationsBeingEdited = resourceAllocationsBeingEdited;
        this.aggregate = this.resourceAllocationsBeingEdited
                .getInitialAggregate();
    }

    public void setAssignedHoursComponent(Intbox assignedHoursComponent) {
        this.assignedHoursComponent = assignedHoursComponent;
        assignedHoursComponentDisabilityRule();
        loadValueForAssignedHoursComponent();
        onChangeEnableApply(assignedHoursComponent);
    }

    private void loadValueForAssignedHoursComponent() {
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
        applyButton.setDisabled(false);
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
        loadValueForTaskStartDateBox();
        onChangeEnableApply(taskStartDateBox);
    }

    private void loadValueForTaskStartDateBox() {
        this.taskStartDateBox.setValue(resourceAllocationsBeingEdited.getTask()
                .getStartDate());
    }

    private void onChangeEnableApply(InputElement inputElement) {
        inputElement.addEventListener(Events.ON_CHANGE, onChangeEnableApply);
    }

    public void setTaskElapsedDays(Intbox taskElapsedDays) {
        this.taskElapsedDays = taskElapsedDays;
        taskElapsedDaysDisabilityRule();
        loadValuesForElapsedDays();
        onChangeEnableApply(taskElapsedDays);
    }

    private void loadValuesForElapsedDays() {
        this.taskElapsedDays.setValue(resourceAllocationsBeingEdited
                .getDaysDuration());
    }

    private void taskElapsedDaysDisabilityRule() {
        this.taskElapsedDays.setDisabled(true);
    }

    private void doApply() {
        aggregate = resourceAllocationsBeingEdited.doAllocation();
        reloadValues();
    }

    private void reloadValues() {
        loadValueForAssignedHoursComponent();
        loadValueForTaskStartDateBox();
        loadValuesForElapsedDays();
    }

    public void setApplyButton(Button applyButton) {
        this.applyButton = applyButton;
        this.applyButton.setDisabled(true);
        this.applyButton.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                doApply();
                FormBinder.this.applyButton.setDisabled(true);
            }
        });
    }

    public void setResourcesPerDayBoxFor(AllocationDTO data,
            Decimalbox decimalbox) {
        resourcesPerDayInputsByAllocationDTO.put(data, decimalbox);
        onChangeEnableApply(decimalbox);
    }

    public int getAssignedHours() {
        return assignedHoursComponent.getValue();
    }

}
