/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.impl.api.InputElement;

class FormBinder {

    private Intbox assignedHoursComponent;

    private final ResourceAllocationsBeingEdited resourceAllocationsBeingEdited;

    private AggregateOfResourceAllocations aggregate;

    private AllocationResult lastAllocation;

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
            Clients.closeErrorBox(allocationsList);
            applyButton.setDisabled(false);
        }
    };

    private Listbox allocationsList;

    private EventListener applyButtonListener;

    private List<InputElement> inputsAssociatedWithOnChangeEnableApply = new ArrayList<InputElement>();

    public FormBinder(
            ResourceAllocationsBeingEdited resourceAllocationsBeingEdited) {
        this.resourceAllocationsBeingEdited = resourceAllocationsBeingEdited;
        this.lastAllocation = this.resourceAllocationsBeingEdited
            .getInitialAllocation();
        this.aggregate = this.lastAllocation.getAggregate();
    }

    public void setAssignedHoursComponent(Intbox assignedHoursComponent) {
        this.assignedHoursComponent = assignedHoursComponent;
        assignedHoursComponentDisabilityRule();
        loadValueForAssignedHoursComponent();
        onChangeEnableApply(assignedHoursComponent);
    }

    private void loadValueForAssignedHoursComponent() {
        this.assignedHoursComponent
                .setValue(aggregate.isEmpty() ? resourceAllocationsBeingEdited
                        .getTask().getWorkHours() : aggregate.getTotalHours());
    }

    private void assignedHoursComponentDisabilityRule() {
        this.assignedHoursComponent.setDisabled(resourceAllocationsBeingEdited
                .getCalculatedValue() == CalculatedValue.NUMBER_OF_HOURS);
    }

    public AllocationResult getLastAllocation() {
        return lastAllocation;
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
        loadValuesForElapsedDays();
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

    void doApply() {
        lastAllocation = resourceAllocationsBeingEdited.doAllocation();
        aggregate = lastAllocation.getAggregate();
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
        applyButtonListener = new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                doApply();
                FormBinder.this.applyButton.setDisabled(true);
            }
        };
        this.applyButton.addEventListener(Events.ON_CLICK, applyButtonListener);
    }

    public void setResourcesPerDayBoxFor(AllocationDTO data,
            Decimalbox decimalbox) {
        resourcesPerDayInputsByAllocationDTO.put(data, decimalbox);
        onChangeEnableApply(decimalbox);
    }

    public int getAssignedHours() {
        Integer result = assignedHoursComponent.getValue();
        if (result == null) {
            throw new RuntimeException("assignedHoursComponent returns null");
        }
        return result;
    }

    public void setDeleteButtonFor(SpecificAllocationDTO data,
            Button deleteButton) {
        deleteButton.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                applyButton.setDisabled(false);
            }
        });
    }

    public void newSpecificAllocation() {
        applyButton.setDisabled(false);
    }

    public void markAssignedHoursMustBePositive() {
        throw new WrongValueException(assignedHoursComponent,
                _("it must be greater than zero"));
    }

    public void markThereMustBeAtLeastOneNoEmptyAllocation() {
        throw new WrongValueException(allocationsList,
                _("at least one no empty allocation is needed"));
    }

    public void setAllocationsList(Listbox allocationsList) {
        this.allocationsList = allocationsList;
    }

    public void detach() {
        this.applyButton.removeEventListener(Events.ON_CLICK,
                applyButtonListener);
        for (InputElement inputElement : inputsAssociatedWithOnChangeEnableApply) {
            inputElement.removeEventListener(Events.ON_CHANGE,
                    onChangeEnableApply);
        }
    }

}
