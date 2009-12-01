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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.planner.allocation.IResourceAllocationModel.IResourceAllocationContext;
import org.navalplanner.web.resourceload.ResourceLoadModel;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.impl.api.InputElement;

class FormBinder {

    private Intbox assignedHoursComponent;

    private final ResourceAllocationsBeingEdited resourceAllocationsBeingEdited;

    private AggregateOfResourceAllocations aggregate;

    private AllocationResult lastAllocation;

    private Datebox taskStartDateBox;

    private Datebox endDate;

    private Button applyButton;

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

    private IMessagesForUser messagesForUser;

    private final IResourceAllocationModel resourceAllocationModel;

    private List<AllocationRow> rows;

    public FormBinder(
            ResourceAllocationsBeingEdited resourceAllocationsBeingEdited,
            IResourceAllocationModel resourceAllocationModel) {
        this.resourceAllocationsBeingEdited = resourceAllocationsBeingEdited;
        this.resourceAllocationModel = resourceAllocationModel;
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
        loadValueForEndDate();
        applyButton.setDisabled(false);
    }

    private void applyDisabledRules() {
        assignedHoursComponentDisabilityRule();
        endDateDisabilityRule();
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

    public void setEndDate(Datebox endDate) {
        this.endDate = endDate;
        this.endDate.setConstraint(datePosteriorToStartDate());
        endDateDisabilityRule();
        loadValueForEndDate();
        onChangeEnableApply(endDate);
    }

    private Constraint datePosteriorToStartDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date date = (Date) value;
                Date startDate = resourceAllocationsBeingEdited.getStartDate();
                if (!date.after(startDate)) {
                    throw new WrongValueException(comp, _(
                            "{0} must be after {1}", date, startDate));
                }
            }
        };
    }

    private void loadValueForEndDate() {
        this.endDate.setValue(resourceAllocationsBeingEdited.getEnd());
    }

    private void endDateDisabilityRule() {
        this.endDate.setDisabled(resourceAllocationsBeingEdited
                .getCalculatedValue() == CalculatedValue.END_DATE);
    }

    public List<AllocationRow> getCurrentRows() {
        return rows = addListeners(resourceAllocationsBeingEdited
                .getCurrentRows());
    }

    private List<AllocationRow> addListeners(List<AllocationRow> list) {
        for (AllocationRow each : list) {
            each.addListenerForInputChange(onChangeEnableApply);
        }
        return list;
    }

    void doApply() {
        lastAllocation = resourceAllocationModel
                .onAllocationContext(new IResourceAllocationContext<AllocationResult>() {

                    @Override
                    public AllocationResult doInsideTransaction() {
                        return resourceAllocationsBeingEdited.doAllocation();
                    }
                });
        aggregate = lastAllocation.getAggregate();
        reloadValues();
    }

    private void reloadValues() {
        loadHoursValues();
        loadValueForAssignedHoursComponent();
        loadValueForTaskStartDateBox();
        loadValueForEndDate();
    }

    private void loadHoursValues() {
        for (AllocationRow each : rows) {
            each.loadHours();
        }
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

    public int getAssignedHours() {
        Integer result = assignedHoursComponent.getValue();
        if (result == null) {
            throw new RuntimeException("assignedHoursComponent returns null");
        }
        return result;
    }

    public LocalDate getAllocationEnd() {
        return new LocalDate(endDate.getValue());
    }

    public void setDeleteButtonFor(AllocationRow row,
            Button deleteButton) {
        deleteButton.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                applyButton.setDisabled(false);
            }
        });
    }

    public void newAllocationAdded() {
        applyButton.setDisabled(false);
    }

    public void markAssignedHoursMustBePositive() {
        throw new WrongValueException(assignedHoursComponent,
                _("it must be greater than zero"));
    }

    public void markRepeatedResources(List<Resource> resources) {
        messagesForUser.showMessage(Level.ERROR, _(
                "{0} already assigned to resource allocation list", StringUtils
                        .join(getResourcesDescriptions(resources), ", ")));
    }

    private List<String> getResourcesDescriptions(List<Resource> resources) {
        List<String> resourcesDescriptions = new ArrayList<String>();
        for (Resource each : resources) {
            resourcesDescriptions.add(each.getDescription());
        }
        return resourcesDescriptions;
    }

    public void markNoWorkersMatchedByCriterions(
            Collection<? extends Criterion> criterions) {
        messagesForUser
                .showMessage(
                        Level.ERROR,
                        _(
                        "there are no workers for required criteria: {0}. So the generic allocation can't be added",
                        ResourceLoadModel.getName(criterions)));
    }

    public void markThereisAlreadyAssignmentWith(Set<Criterion> criterions) {
        messagesForUser.showMessage(Level.ERROR,
                _("for criterions {0} already exists an allocation"));
    }

    public void markEndDateMustBeAfterStartDate() {
        DateTimeFormatter formatter = ISODateTimeFormat.basicDate().withLocale(
                Locales.getCurrent());
        LocalDate start = new LocalDate(resourceAllocationsBeingEdited
                .getStartDate());
        throw new WrongValueException(endDate, _(
                "end date: {0} must be after start date: {1}",
                getAllocationEnd().toString(formatter), start
                        .toString(formatter)));
    }

    public void setAllocationsList(Listbox allocationsList) {
        this.allocationsList = allocationsList;
    }

    public void setMessagesForUser(IMessagesForUser messages) {
        this.messagesForUser = messages;
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
