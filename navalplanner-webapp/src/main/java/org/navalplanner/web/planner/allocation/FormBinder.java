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
import java.util.EnumSet;
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
import org.navalplanner.web.common.Util;
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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.impl.api.InputElement;

class FormBinder {

    private Intbox assignedHoursComponent;

    private final AllocationRowsHandler allocationRowsHandler;

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

    private Checkbox recommendedAllocationCheckbox;

    private EventListener recommendedCheckboxListener;

    public FormBinder(
            AllocationRowsHandler allocationRowsHandler,
            IResourceAllocationModel resourceAllocationModel) {
        this.allocationRowsHandler = allocationRowsHandler;
        this.resourceAllocationModel = resourceAllocationModel;
        this.lastAllocation = this.allocationRowsHandler
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
                .setValue(aggregate.isEmpty() ? allocationRowsHandler
                        .getTask().getWorkHours() : aggregate.getTotalHours());
    }

    private void assignedHoursComponentDisabilityRule() {
        EnumSet<CalculatedValue> set = EnumSet.of(
                CalculatedValue.NUMBER_OF_HOURS,
                CalculatedValue.RESOURCES_PER_DAY);
        this.assignedHoursComponent.setDisabled(set
                .contains(allocationRowsHandler.getCalculatedValue()));
    }

    public AllocationResult getLastAllocation() {
        return lastAllocation;
    }

    public void setCalculatedValue(CalculatedValue calculatedValue) {
        if (calculatedValue == allocationRowsHandler
                .getCalculatedValue()) {
            return;
        }
        allocationRowsHandler.setCalculatedValue(calculatedValue);
        applyDisabledRules();
        loadValueForEndDate();
        applyButton.setDisabled(false);
    }

    private void applyDisabledRules() {
        assignedHoursComponentDisabilityRule();
        endDateDisabilityRule();
        applyDisabledRulesOnRows();
    }

    private void applyDisabledRulesOnRows() {
        for (AllocationRow each : rows) {
            each.applyDisabledRules(getCalculatedValue());
        }
    }

    public CalculatedValue getCalculatedValue() {
        return allocationRowsHandler.getCalculatedValue();
    }

    public void setTaskStartDateBox(Datebox taskStartDateBox) {
        this.taskStartDateBox = taskStartDateBox;
        this.taskStartDateBox.setDisabled(true);
        loadValueForTaskStartDateBox();
        onChangeEnableApply(taskStartDateBox);
    }

    private void loadValueForTaskStartDateBox() {
        this.taskStartDateBox.setValue(allocationRowsHandler.getTask()
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
                Date startDate = allocationRowsHandler.getStartDate();
                if (!date.after(startDate)) {
                    throw new WrongValueException(comp, _(
                            "{0} must be after {1}", date, startDate));
                }
            }
        };
    }

    private void loadValueForEndDate() {
        this.endDate.setValue(allocationRowsHandler.getEnd());
    }

    private void endDateDisabilityRule() {
        this.endDate.setDisabled(allocationRowsHandler
                .getCalculatedValue() == CalculatedValue.END_DATE);
    }

    public List<AllocationRow> getCurrentRows() {
        List<AllocationRow> result = addListeners(allocationRowsHandler
                .getCurrentRows());
        rows = result;
        applyDisabledRulesOnRows();
        return result;
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
                        return allocationRowsHandler.doAllocation();
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
        messagesForUser.showMessage(Level.ERROR, _(
                "for criterions {0} already exists an allocation",
                ResourceLoadModel.getName(criterions)));
    }

    public void markEndDateMustBeAfterStartDate() {
        DateTimeFormatter formatter = ISODateTimeFormat.basicDate().withLocale(
                Locales.getCurrent());
        LocalDate start = new LocalDate(allocationRowsHandler
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
        this.recommendedAllocationCheckbox.removeEventListener(Events.ON_CHECK,
                recommendedCheckboxListener);
        for (InputElement inputElement : inputsAssociatedWithOnChangeEnableApply) {
            inputElement.removeEventListener(Events.ON_CHANGE,
                    onChangeEnableApply);
        }
    }

    public void setCheckbox(Checkbox recommendedAllocation) {
        this.recommendedAllocationCheckbox = recommendedAllocation;
        recommendedCheckboxListener = new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                if (recommendedAllocationCheckbox.isChecked()) {
                    activatingRecommendedAllocation();
                } else {
                    deactivatingRecommendedAllocation();
                }
            }

        };
        this.recommendedAllocationCheckbox.addEventListener(Events.ON_CHECK,
                recommendedCheckboxListener);
    }

    private void activatingRecommendedAllocation() {
        allocationRowsHandler.removeAll();
        resourceAllocationModel.addDefaultAllocations();
        Util.reloadBindings(allocationsList);
    }

    private void deactivatingRecommendedAllocation() {
    }

}
