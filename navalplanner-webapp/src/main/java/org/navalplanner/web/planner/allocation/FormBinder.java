/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.navalplanner.business.common.ProportionalDistributor;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.ResourcesPerDay;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.NewAllocationSelectorCombo;
import org.navalplanner.web.common.components.ResourceAllocationBehaviour;
import org.navalplanner.web.planner.allocation.IResourceAllocationModel.IResourceAllocationContext;
import org.navalplanner.web.planner.taskedition.TaskPropertiesController;
import org.zkoss.util.Locales;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.SimpleConstraint;
import org.zkoss.zul.Tab;
import org.zkoss.zul.impl.api.InputElement;

public class FormBinder {

    private Intbox allHoursInput;

    private Label allOriginalHours;
    private Label allTotalHours;
    private Label allConsolidatedHours;

    private Label allTotalResourcesPerDay;
    private Label allConsolidatedResourcesPerDay;

    private final AllocationRowsHandler allocationRowsHandler;

    private AggregateOfResourceAllocations aggregate;

    private AllocationResult lastAllocation;

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
            Clients.closeErrorBox(allocationsGrid);
            applyButton.setDisabled(false);
        }
    };

    private ResourceAllocationBehaviour behaviour;

    private Grid allocationsGrid;

    private EventListener applyButtonListener;

    private List<InputElement> inputsAssociatedWithOnChangeEnableApply = new ArrayList<InputElement>();

    private IMessagesForUser messagesForUser;

    private final IResourceAllocationModel resourceAllocationModel;

    private List<AllocationRow> rows = Collections.emptyList();

    private WorkableDaysAndDatesBinder workableDaysAndDatesBinder;

    private Button btnRecommendedAllocation;

    private ProportionalDistributor hoursDistributorForRecommendedAllocation;

    private ResourcesPerDay.ResourcesPerDayDistributor resourcesPerDayDistributorForRecommendedAllocation;

    private EventListener hoursRowInputChange = new EventListener() {

        @Override
        public void onEvent(Event event) throws Exception {
            if (allHoursInput.isDisabled()) {
                allHoursInput.setValue(sumAllHoursFromHoursInputs());
            }
        }
    };

    private EventListener resourcesPerDayRowInputChange = new EventListener() {

        @Override
        public void onEvent(Event event) throws Exception {
            if (allResourcesPerDay.isDisabled()) {
                sumResourcesPerDayFromRowsAndAssignToAllResourcesPerDay();
            }
        }
    };

    private EventListener allHoursInputChange = new EventListener() {

        @Override
        public void onEvent(Event event) throws Exception {
            if (!allHoursInput.isDisabled()) {
                distributeHoursFromTotalToRows();
            }
        }
    };

    private EventListener allResourcesPerDayChange = new EventListener() {

        @Override
        public void onEvent(Event event) throws Exception {
            if (!allResourcesPerDay.isDisabled()) {
                distributeResourcesPerDayToRows();
            }
        }
    };

    private boolean recommendedAllocation = false;

    private NewAllocationSelectorCombo newAllocationSelectorCombo;

    private Tab workerSearchTab;

    private Decimalbox allResourcesPerDay;

    private Button advancedSearchButton;

    public FormBinder(Scenario currentScenario,
            AllocationRowsHandler allocationRowsHandler,
            IResourceAllocationModel resourceAllocationModel) {
        this.allocationRowsHandler = allocationRowsHandler;
        this.resourceAllocationModel = resourceAllocationModel;
        this.lastAllocation = this.allocationRowsHandler
                .getInitialAllocation(currentScenario);
        this.aggregate = this.lastAllocation.getAggregate();
    }

    public void setAssignedHoursComponent(Intbox assignedHoursComponent) {
        this.allHoursInput = assignedHoursComponent;
        this.allHoursInput.setConstraint(positiveValueRequired());
        allHoursInputComponentDisabilityRule();
        loadValueForAssignedHoursComponent();
        onChangeEnableApply(assignedHoursComponent);
    }

    private void loadValueForAssignedHoursComponent() {
        this.allHoursInput
                .setValue(aggregate.isEmpty() ? allocationRowsHandler
                        .getTask().getWorkHours() : aggregate.getTotalHours());
    }

    private void allHoursInputComponentDisabilityRule() {
        CalculatedValue c = allocationRowsHandler.getCalculatedValue();
        boolean disabled = rows.isEmpty()
                || (CalculatedValue.NUMBER_OF_HOURS == c)
                || (c == CalculatedValue.RESOURCES_PER_DAY && !recommendedAllocation);
        this.allHoursInput.setDisabled(disabled);
    }

    public AllocationResult getLastAllocation() {
        return lastAllocation;
    }

    public void setCalculatedValue(CalculatedValue newCalculatedValue) {
        CalculatedValue previousCalculatedValue = allocationRowsHandler
                .getCalculatedValue();
        if (newCalculatedValue == previousCalculatedValue) {
            return;
        }
        allocationRowsHandler.setCalculatedValue(newCalculatedValue);
        applyDisabledRules();
        sumResourcesPerDayFromRowsAndAssignToAllResourcesPerDay();
        workableDaysAndDatesBinder.switchFromTo(previousCalculatedValue,
                newCalculatedValue);
        applyButton.setDisabled(false);
    }

    private void applyDisabledRules() {
        allHoursInputComponentDisabilityRule();
        workableDaysAndDatesBinder.applyDisabledRules();
        allResourcesPerDayVisibilityRule();
        applyDisabledRulesOnRows();
        this.btnRecommendedAllocation.setDisabled(recommendedAllocation);
    }

    private void applyDisabledRulesOnRows() {
        for (AllocationRow each : rows) {
            each
                    .applyDisabledRules(getCalculatedValue(),
                            recommendedAllocation);
        }
    }

    private void bindTotalHoursToHoursInputs() {
        int sum = 0;
        for (AllocationRow each : rows) {
            each.addListenerForHoursInputChange(hoursRowInputChange);
            sum += each.getHoursFromInput();
        }
        allHoursInput.setValue(sum);
    }

    private int sumAllHoursFromHoursInputs() {
        int result = 0;
        for (AllocationRow each : rows) {
            result += each.getHoursFromInput();
        }
        return result;
    }

    private int sumAllOriginalHours() {
        int result = 0;
        for (AllocationRow each : rows) {
            result += each.getOriginalHours();
        }
        return result;
    }

    private int sumAllTotalHours() {
        int result = 0;
        for (AllocationRow each : rows) {
            result += each.getTotalHours();
        }
        return result;
    }

    private int sumAllConsolidatedHours() {
        int result = 0;
        for (AllocationRow each : rows) {
            result += each.getConsolidatedHours();
        }
        return result;
    }

    public CalculatedValue getCalculatedValue() {
        return allocationRowsHandler.getCalculatedValue();
    }

    private void onChangeEnableApply(InputElement inputElement) {
        Util.ensureUniqueListener(inputElement, Events.ON_CHANGE,
                onChangeEnableApply);
    }

    public void setWorkableDays(Intbox duration,
            final TaskPropertiesController taskPropertiesController,
            final Label labelTaskStart, final Label labelTaskEnd) {
        this.workableDaysAndDatesBinder = new WorkableDaysAndDatesBinder(
                duration, labelTaskStart, labelTaskEnd,
                taskPropertiesController);
    }

    class WorkableDaysAndDatesBinder {

        private Intbox taskWorkableDays;

        private Label labelTaskStart;

        private Label labelTaskEnd;

        WorkableDaysAndDatesBinder(final Intbox taskWorkableDays,
                final Label labelTaskStart, final Label labelTaskEnd,
                final TaskPropertiesController taskPropertiesController) {
            this.taskWorkableDays = taskWorkableDays;
            this.labelTaskStart = labelTaskStart;
            this.labelTaskEnd = labelTaskEnd;
            initializeDateAndDurationFieldsFromTaskOriginalValues();
            Util.ensureUniqueListener(taskWorkableDays, Events.ON_CHANGE,
                    new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    Task task = getTask();
                    Integer workableDays = taskWorkableDays.getValue();
                    if (allocationRowsHandler.isForwardsAllocation()) {
                        LocalDate newEndDate = task
                                .calculateEndGivenWorkableDays(workableDays);
                        taskPropertiesController
                                .updateTaskEndDate(newEndDate);
                        showValueOfDateOn(labelTaskEnd, newEndDate);
                    } else {
                        LocalDate newStart = task
                                .calculateStartGivenWorkableDays(workableDays);
                        taskPropertiesController
                                .updateTaskStartDate(newStart);
                        showValueOfDateOn(labelTaskStart, newStart);
                    }
                }
            });
            applyDisabledRules();
            onChangeEnableApply(taskWorkableDays);
        }

        void applyDisabledRules() {
            this.taskWorkableDays.setDisabled(allocationRowsHandler
                    .getCalculatedValue() == CalculatedValue.END_DATE);
        }

        private void initializeDateAndDurationFieldsFromTaskOriginalValues() {
            Task task = getTask();
            showValueOfDateOn(labelTaskStart, task.getStartAsLocalDate());
            showValueOfDateOn(labelTaskEnd, task.getIntraDayEndDate()
                    .asExclusiveEnd());

            taskWorkableDays.setConstraint(positiveValueRequired());
            taskWorkableDays.setValue(task.getWorkableDays());
        }

        Integer getValue() {
            return taskWorkableDays.getValue();
        }

        private Integer lastSpecifiedWorkableDays = null;

        void switchFromTo(CalculatedValue previousCalculatedValue,
                CalculatedValue newCalculatedValue) {
            if (newCalculatedValue == CalculatedValue.END_DATE) {
                clearDateAndDurationFields();
            } else if (previousCalculatedValue == CalculatedValue.END_DATE
                    && taskWorkableDays.getValue() == null) {
                initializeDateAndDurationFieldsFromLastValues();
            }
        }

        private void clearDateAndDurationFields() {
            (allocationRowsHandler.isForwardsAllocation() ? labelTaskEnd
                    : labelTaskStart).setValue("");
            taskWorkableDays.setConstraint((Constraint) null);
            lastSpecifiedWorkableDays = taskWorkableDays.getValue();
            taskWorkableDays.setValue(null);
        }

        private void initializeDateAndDurationFieldsFromLastValues() {
            if (lastSpecifiedWorkableDays == null) {
                initializeDateAndDurationFieldsFromTaskOriginalValues();
            } else {
                Task task = getTask();
                taskWorkableDays.setConstraint(positiveValueRequired());
                taskWorkableDays.setValue(lastSpecifiedWorkableDays);
                showValueOfDateOn(
                        labelTaskEnd,
                        task.calculateEndGivenWorkableDays(lastSpecifiedWorkableDays));
                lastSpecifiedWorkableDays = null;
            }
        }

        void afterApplicationReloadValues() {
            if (getCalculatedValue() != CalculatedValue.END_DATE
                    || aggregate.isEmpty()) {
                return;
            }
            LocalDate start = aggregate.getStart().getDate();
            LocalDate end = aggregate.getEnd().asExclusiveEnd();
            showValueOfDateOn(labelTaskStart, start);
            showValueOfDateOn(labelTaskEnd, end);
            taskWorkableDays
                    .setValue(getTask().getWorkableDaysFrom(start, end));
        }

        private void showValueOfDateOn(final Label label, LocalDate date) {
            DateTimeFormatter formatter = DateTimeFormat.forStyle("S-")
                    .withLocale(Locales.getCurrent());
            label.setValue(formatter.print(date));
        }

    }

    private static SimpleConstraint positiveValueRequired() {
        return new SimpleConstraint(SimpleConstraint.NO_EMPTY
                | SimpleConstraint.NO_NEGATIVE);
    }

    public LocalDate getAllocationEnd() {
        return getTask().calculateEndGivenWorkableDays(
                workableDaysAndDatesBinder.getValue());
    }

    public LocalDate getAllocationStart() {
        return getTask().calculateStartGivenWorkableDays(
                workableDaysAndDatesBinder.getValue());
    }

    private Task getTask() {
        return allocationRowsHandler.getTask();
    }

    public void setAllResourcesPerDay(Decimalbox allResourcesPerDay) {
        this.allResourcesPerDay = allResourcesPerDay;
        this.allResourcesPerDay.setConstraint(positiveValueRequired());
        allResourcesPerDayVisibilityRule();
        onChangeEnableApply(allResourcesPerDay);
    }

    private void allResourcesPerDayVisibilityRule() {
        CalculatedValue c = allocationRowsHandler
            .getCalculatedValue();
        this.allResourcesPerDay.setDisabled(rows.isEmpty()
                || c == CalculatedValue.RESOURCES_PER_DAY
                || !recommendedAllocation);
        this.allResourcesPerDay
                .setConstraint(constraintForAllResourcesPerDay());
    }

    private Constraint constraintForAllResourcesPerDay() {
        if (allResourcesPerDay.isDisabled()) {
            return null;
        }
        return AllocationRow.CONSTRAINT_FOR_RESOURCES_PER_DAY;
    }

    public List<AllocationRow> getCurrentRows() {
        List<AllocationRow> result = addListeners(allocationRowsHandler
                .getCurrentRows());
        rows = result;
        applyDisabledRulesOnRows();
        bindTotalHoursToHoursInputs();
        allHoursInputComponentDisabilityRule();
        bindAllResourcesPerDayToRows();
        allResourcesPerDayVisibilityRule();
        loadAggregatedCalculations();
        return result;
    }


    private void bindAllResourcesPerDayToRows() {
        sumResourcesPerDayFromRowsAndAssignToAllResourcesPerDay();
        for (AllocationRow each : rows) {
            each
                    .addListenerForResourcesPerDayInputChange(resourcesPerDayRowInputChange);
        }
    }

    private List<AllocationRow> addListeners(List<AllocationRow> list) {
        for (AllocationRow each : list) {
            each.addListenerForInputChange(onChangeEnableApply);
        }
        return list;
    }

    public void doApply() {
        lastAllocation = resourceAllocationModel
                .onAllocationContext(new IResourceAllocationContext<AllocationResult>() {

                    @Override
                    public AllocationResult doInsideTransaction() {
                        return allocationRowsHandler.doAllocation();
                    }
                });
        aggregate = lastAllocation.getAggregate();
        allResourcesPerDayVisibilityRule();
        sumResourcesPerDayFromRowsAndAssignToAllResourcesPerDay();
        reloadValues();
    }

    private void reloadValues() {
        loadResourcesPerDay();
        loadHoursValues();
        loadValueForAssignedHoursComponent();
        loadDerivedAllocations();
        loadSclassRowSatisfied();
        workableDaysAndDatesBinder.afterApplicationReloadValues();
        Util.reloadBindings(allocationsGrid);
    }

    @SuppressWarnings("unchecked")
    private void loadSclassRowSatisfied() {
        try {
            List<org.zkoss.zul.Row> rows = (List<org.zkoss.zul.Row>) allocationsGrid
                    .getRows().getChildren();
            for (org.zkoss.zul.Row row : rows) {
                if (row.getValue() instanceof AllocationRow) {
                    if (!((AllocationRow) row.getValue()).isSatisfied()) {
                        row.setSclass("allocation-not-satisfied");
                    } else {
                        row.setSclass("allocation-satisfied");
                    }
                }
            }
        } catch (ClassCastException e) {
            throw new RuntimeException();
        }
    }

    private void loadHoursValues() {
        for (AllocationRow each : rows) {
            each.loadHours();
        }
    }

    private void loadResourcesPerDay() {
        for (AllocationRow each : rows) {
            each.loadResourcesPerDay();
        }
    }

    private void loadDerivedAllocations() {
        for (AllocationRow each : rows) {
            each.reloadDerivedAllocationsGrid();
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
        Util.ensureUniqueListener(this.applyButton, Events.ON_CLICK,
                applyButtonListener);
    }

    public int getAssignedHours() {
        Integer result = allHoursInput.getValue();
        if (result == null) {
            throw new RuntimeException("assignedHoursComponent returns null");
        }
        return result;
    }

    public Integer getWorkableDays() {
        return workableDaysAndDatesBinder.getValue();
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
        throw new WrongValueException(allHoursInput,
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
            resourcesDescriptions.add(each.getShortDescription());
        }
        return resourcesDescriptions;
    }

    public void markNoResourcesMatchedByCriterions(ResourceEnum resourceType,
            Collection<? extends Criterion> criterions) {
        messagesForUser
                .showMessage(
                        Level.ERROR,
                        _(
                                "there are no resources for required criteria: {0}. So the generic allocation can't be added",
                                Criterion.getCaptionFor(resourceType,
                                        criterions)));
    }

    public void markThereisAlreadyAssignmentWith(ResourceEnum resourceType,
            Collection<? extends Criterion> criterions) {
        messagesForUser.showMessage(Level.ERROR, _(
                "already exists an allocation for criteria {0}",
                        Criterion.getCaptionFor(resourceType, criterions)));
    }

    public void markNoEmptyResourcesPerDay(List<AllocationRow> rows) {
        Validate.isTrue(!rows.isEmpty());
        final String message = _("resources per day must be not empty and bigger than zero");
        if (!recommendedAllocation) {
            AllocationRow first = rows.get(0);
            throw new WrongValueException(first.getResourcesPerDayInput(),
                    message);
        } else {
            throw new WrongValueException(allResourcesPerDay, message);
        }
    }

    public void setAllocationsGrid(Grid allocationsGrid) {
        this.allocationsGrid = allocationsGrid;
    }

    public void setMessagesForUser(IMessagesForUser messages) {
        this.messagesForUser = messages;
    }

    public void detach() {
        if (this.applyButton != null) {
            this.applyButton.removeEventListener(Events.ON_CLICK,
                    applyButtonListener);
        }
        for (InputElement inputElement : inputsAssociatedWithOnChangeEnableApply) {
            inputElement.removeEventListener(Events.ON_CHANGE,
                    onChangeEnableApply);
        }
    }

    public void setRecommendedAllocation(Button recommendedAllocation) {
        this.btnRecommendedAllocation = recommendedAllocation;
        this.btnRecommendedAllocation.setDisabled(false);
        Util.ensureUniqueListener(recommendedAllocation, Events.ON_CLICK,
                new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                activatingRecommendedAllocation();
            }
        });
    }

    public EventListener getRecommendedAllocationListener() {
        return new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                activatingRecommendedAllocation();
            }
        };
    }

    private void activatingRecommendedAllocation() {
        allocationRowsHandler.removeAll();
        ProportionalDistributor distributor = resourceAllocationModel
                .addDefaultAllocations();
        boolean recommendAllocationSuccessful = distributor != null;
        if (recommendAllocationSuccessful) {
            hoursDistributorForRecommendedAllocation = distributor;
            resourcesPerDayDistributorForRecommendedAllocation = ResourcesPerDay
                    .distributor(hoursDistributorForRecommendedAllocation);
            this.recommendedAllocation = true;
            disableIfNeededWorkerSearch();
            applyDisabledRules();
            allHoursInput.addEventListener(Events.ON_CHANGE,
                    allHoursInputChange);
            allResourcesPerDay.addEventListener(Events.ON_CHANGE,
                    allResourcesPerDayChange);
            sumResourcesPerDayOrSetToZero();
        }
        Util.reloadBindings(allocationsGrid);
    }

    private void sumResourcesPerDayOrSetToZero() {
        if (allResourcesPerDay.isDisabled()) {
            sumResourcesPerDayFromRowsAndAssignToAllResourcesPerDay();
        } else {
            allResourcesPerDay.setValue(BigDecimal.ZERO);
        }
    }

    private void distributeHoursFromTotalToRows() {
        Integer value = allHoursInput.getValue();
        value = value != null ? value : 0;
        int[] hours = hoursDistributorForRecommendedAllocation
                .distribute(value);
        AllocationRow.assignHours(rows, hours);
    }

    private void distributeResourcesPerDayToRows() {
        BigDecimal total = allResourcesPerDay.getValue();
        total = total != null ? total : BigDecimal.ZERO;
        ResourcesPerDay[] forRows = resourcesPerDayDistributorForRecommendedAllocation
                .distribute(ResourcesPerDay.amount(total));
        AllocationRow.assignResourcesPerDay(rows, forRows);
    }

    public void rowRemoved() {
        deactivatingRecommendedAllocation();
    }

    private void deactivatingRecommendedAllocation() {
        this.recommendedAllocation = false;
        allHoursInput
                .removeEventListener(Events.ON_CHANGE,
                allHoursInputChange);
        applyDisabledRules();
        disableIfNeededWorkerSearch();
    }

    private void disableIfNeededWorkerSearch() {
        workerSearchTab.setDisabled(this.recommendedAllocation);
        newAllocationSelectorCombo.setDisabled(this.recommendedAllocation);
        advancedSearchButton.setDisabled(this.recommendedAllocation);
    }

    public void setWorkerSearchTab(Tab workerSearchTab) {
        this.workerSearchTab = workerSearchTab;
        this.workerSearchTab.setDisabled(recommendedAllocation);
    }

    public void setAdvancedSearchButton(Button advancedSearchButton) {
        this.advancedSearchButton = advancedSearchButton;
        this.advancedSearchButton.setDisabled(recommendedAllocation);
    }

    public void setNewAllocationSelectorCombo(
            NewAllocationSelectorCombo newAllocationSelectorCombo) {
        this.newAllocationSelectorCombo = newAllocationSelectorCombo;
        this.newAllocationSelectorCombo.setDisabled(recommendedAllocation);
    }

    private void sumResourcesPerDayFromRowsAndAssignToAllResourcesPerDay() {
        if (allResourcesPerDay.isDisabled()) {
            allResourcesPerDay.setValue(sumResourcesPerDayFromInputs());
        }
    }

    private BigDecimal sumResourcesPerDayFromInputs() {
        BigDecimal sum = BigDecimal.ZERO;
        for (AllocationRow each : rows) {
            if (each.getResourcesPerDayInput().isValid()) {
                sum = sum.add(each.getResourcesPerDayFromInput().getAmount());
            }
        }
        return sum;
    }

    private BigDecimal sumAllTotalResourcesPerDay() {
        BigDecimal sum = BigDecimal.ZERO;
        for (AllocationRow each : rows) {
            sum = sum.add(each.getTotalResourcesPerDay().getAmount());
        }
        return sum;
    }

    private BigDecimal sumAllConsolidatedResourcesPerDay() {
        BigDecimal sum = BigDecimal.ZERO;
        for (AllocationRow each : rows) {
            sum = sum.add(each.getConsolidatedResourcesPerDay().getAmount());
        }
        return sum;
    }

    public void setAllOriginalHours(Label allOriginalHours) {
        this.allOriginalHours = allOriginalHours;
    }

    public Label getAllOriginalHours() {
        return allOriginalHours;
    }

    public void setAllTotalHours(Label allTotalHours) {
        this.allTotalHours = allTotalHours;
    }

    public Label getAllTotalHours() {
        return allTotalHours;
    }

    public void setAllConsolidatedHours(Label alCo1nsolidatedHours) {
        this.allConsolidatedHours = alCo1nsolidatedHours;
    }

    public Label getAllConsolidatedHours() {
        return allConsolidatedHours;
    }

    public void setAllTotalResourcesPerDay(Label allTotalResourcesPerDay) {
        this.allTotalResourcesPerDay = allTotalResourcesPerDay;
    }

    public Label getAllTotalResourcesPerDay() {
        return allTotalResourcesPerDay;
    }

    public void setAllConsolidatedResourcesPerDay(
            Label allConsolidatedResourcesPerDay) {
        this.allConsolidatedResourcesPerDay = allConsolidatedResourcesPerDay;
    }

    public Label getAllConsolidatedResourcesPerDay() {
        return allConsolidatedResourcesPerDay;
    }

    public void loadAggregatedCalculations() {
        // Calculate aggregated values
        if (behaviour.allowMultipleSelection()) {
            allOriginalHours.setValue(Integer.toString(sumAllOriginalHours()));
            allTotalHours.setValue(Integer.toString(sumAllTotalHours()));
            allConsolidatedHours.setValue(Integer
                    .toString(sumAllConsolidatedHours()));
            allTotalResourcesPerDay.setValue(sumAllTotalResourcesPerDay()
                    .toString());
            allConsolidatedResourcesPerDay
                    .setValue(sumAllConsolidatedResourcesPerDay().toString());
        }
    }

    public boolean allowMultipleSelection() {
        return behaviour.allowMultipleSelection();
    }

    public void cannotAllocateMoreThanOneResource(List<Resource> resources) {
        messagesForUser.showMessage(
                Level.ERROR,
                _("{0} could not be allocated. "
                        + "Cannot allocate more than one resource",
                        Resource.getCaptionFor(resources)));
    }

    public void setBehaviour(ResourceAllocationBehaviour behaviour) {
        this.behaviour = behaviour;
    }


}
