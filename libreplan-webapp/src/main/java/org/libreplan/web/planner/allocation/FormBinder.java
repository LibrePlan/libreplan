/*
 * This file is part of LibrePlan
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

package org.libreplan.web.planner.allocation;

import static org.libreplan.business.workingday.EffortDuration.hours;
import static org.libreplan.business.workingday.EffortDuration.zero;
import static org.libreplan.web.I18nHelper._;
import static org.libreplan.web.planner.allocation.AllocationRow.assignEfforts;
import static org.libreplan.web.planner.allocation.AllocationRow.sumAllEffortFromInputs;
import static org.libreplan.web.planner.allocation.AllocationRow.sumAllOriginalEffort;
import static org.libreplan.web.planner.allocation.AllocationRow.sumAllTotalEffort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.libreplan.business.common.Flagged;
import org.libreplan.business.common.ProportionalDistributor;
import org.libreplan.business.planner.entities.AggregateOfResourceAllocations;
import org.libreplan.business.planner.entities.CalculatedValue;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workingday.ResourcesPerDay;
import org.libreplan.web.common.EffortDurationBox;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.NewAllocationSelectorCombo;
import org.libreplan.web.common.components.ResourceAllocationBehaviour;
import org.libreplan.web.planner.allocation.AllocationRowsHandler.Warnings;
import org.libreplan.web.planner.allocation.IResourceAllocationModel.IResourceAllocationContext;
import org.libreplan.web.planner.taskedition.TaskPropertiesController;
import org.zkoss.util.Locales;
import org.zkoss.zk.au.out.AuWrongValue;
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

    private EffortDurationBox effortInput;

    private Label allOriginalEffort;
    private Label allTotalEffort;
    private Label allConsolidatedEffort;

    private Label allTotalResourcesPerDay;
    private Label allConsolidatedResourcesPerDay;

    private final AllocationRowsHandler allocationRowsHandler;

    private AggregateOfResourceAllocations aggregate;

    private AllocationResult lastAllocation;

    private Button applyButton;

    private EventListener onChangeEnableApply = new EventListener() {

        @Override
        public void onEvent(Event event) {
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
        public void onEvent(Event event) {
            if (effortInput.isDisabled()) {
                effortInput.setValue(sumAllEffortFromInputs(rows));
            }
        }
    };

    private EventListener resourcesPerDayRowInputChange = new EventListener() {

        @Override
        public void onEvent(Event event) {
            if (allResourcesPerDay.isDisabled()) {
                sumResourcesPerDayFromRowsAndAssignToAllResourcesPerDay();
            }
        }
    };

    private EventListener allHoursInputChange = new EventListener() {

        @Override
        public void onEvent(Event event) {
            if (!effortInput.isDisabled()) {
                distributeHoursFromTotalToRows();
            }
        }
    };

    private EventListener allResourcesPerDayChange = new EventListener() {

        @Override
        public void onEvent(Event event) {
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

    public void setAssignedEffortComponent(
            EffortDurationBox assignedEffortComponent) {
        this.effortInput = assignedEffortComponent;
        this.effortInput.setConstraint(positiveValueRequired());
        allHoursInputComponentDisabilityRule();
        loadValueForEffortInput();
        onChangeEnableApply(assignedEffortComponent);
    }

    private void loadValueForEffortInput() {
        this.effortInput
                .setValue(aggregate.isEmpty() ? hours(allocationRowsHandler
                        .getTask().getWorkHours()) : aggregate.getTotalEffort());
    }

    private void allHoursInputComponentDisabilityRule() {
        CalculatedValue c = allocationRowsHandler.getCalculatedValue();
        boolean disabled = rows.isEmpty()
                || (CalculatedValue.NUMBER_OF_HOURS == c)
                || (c == CalculatedValue.RESOURCES_PER_DAY && !recommendedAllocation)
                || isAnyManual() || isTaskUpdatedFromTimesheets();
        this.effortInput.setDisabled(disabled);
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
        this.btnRecommendedAllocation.setDisabled(recommendedAllocation
                || isAnyManual() || isTaskUpdatedFromTimesheets());
    }

    private void applyDisabledRulesOnRows() {
        for (AllocationRow each : rows) {
            each.applyDisabledRules(getCalculatedValue(),
                    recommendedAllocation, isAnyManual()
                            || isTaskUpdatedFromTimesheets());
        }
    }

    private void bindTotalHoursToHoursInputs() {
        for (AllocationRow each : rows) {
            each.addListenerForHoursInputChange(hoursRowInputChange);
        }
        effortInput.setValue(sumAllEffortFromInputs(this.rows));
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

        private final TaskPropertiesController taskPropertiesController;

        WorkableDaysAndDatesBinder(final Intbox taskWorkableDays,
                final Label labelTaskStart, final Label labelTaskEnd,
                final TaskPropertiesController taskPropertiesController) {
            this.taskWorkableDays = taskWorkableDays;
            this.labelTaskStart = labelTaskStart;
            this.labelTaskEnd = labelTaskEnd;
            this.taskPropertiesController = taskPropertiesController;
            initializeDateAndDurationFieldsFromTaskOriginalValues();
            final LocalDate firstPossibleDay = getTask()
                    .getFirstDayNotConsolidated().nextDayAtStart()
                    .asExclusiveEnd();
            Util.ensureUniqueListeners(taskWorkableDays, Events.ON_CHANGE,
                    new EventListener() {

                        @Override
                        public void onEvent(Event event) {
                            Task task = getTask();
                            Integer workableDays = taskWorkableDays.getValue();
                            if (allocationRowsHandler.isForwardsAllocation()) {
                                IntraDayDate newEnd = ensureItIsAfterConsolidation(task
                                        .calculateEndGivenWorkableDays(workableDays));
                                updateWorkableDaysIfNecessary(workableDays,
                                        getTask().getIntraDayStartDate(),
                                        newEnd);
                                taskPropertiesController
                                        .updateTaskEndDate(newEnd.getDate());
                                showValueOfDateOn(labelTaskEnd,
                                        newEnd.getDate());
                            } else {
                                IntraDayDate newStart = ensureItIsAfterConsolidation(task
                                        .calculateStartGivenWorkableDays(workableDays));
                                updateWorkableDaysIfNecessary(workableDays,
                                        newStart, task.getIntraDayEndDate());
                                taskPropertiesController
                                        .updateTaskStartDate(newStart.getDate());
                                showValueOfDateOn(labelTaskStart,
                                        newStart.getDate());
                            }
                        }

                        private void updateWorkableDaysIfNecessary(
                                int specifiedWorkableDays,
                                IntraDayDate allocationStart,
                                IntraDayDate allocationEnd) {
                            Integer effectiveWorkableDays = getTask()
                                    .getWorkableDaysFrom(
                                            allocationStart.getDate(),
                                            allocationEnd.asExclusiveEnd());
                            if (effectiveWorkableDays < specifiedWorkableDays) {
                                Clients.response(new AuWrongValue(
                                        taskWorkableDays,
                                        _("The original workable days value {0} cannot be modified as it has consolidations",
                                                specifiedWorkableDays)));
                                taskWorkableDays
                                        .setValue(effectiveWorkableDays);
                            }
                        }

                        @SuppressWarnings("unchecked")
                        private IntraDayDate ensureItIsAfterConsolidation(
                                IntraDayDate newDate) {
                            if (getTask().hasConsolidations()) {
                                return Collections.max(Arrays.asList(newDate,
                                        IntraDayDate.startOfDay(firstPossibleDay)));
                            }
                            return newDate;
                        }

                    }, onChangeEnableApply);
            applyDisabledRules();
        }

        void applyDisabledRules() {
            this.taskWorkableDays.setDisabled(allocationRowsHandler
                    .getCalculatedValue() == CalculatedValue.END_DATE
                    || isAnyManual() || isTaskUpdatedFromTimesheets());
        }

        private void initializeDateAndDurationFieldsFromTaskOriginalValues() {
            Task task = getTask();
            showValueOfDateOn(labelTaskStart, task.getStartAsLocalDate());
            showValueOfDateOn(labelTaskEnd, task.getEndAsLocalDate());

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
                        task.calculateEndGivenWorkableDays(
                                lastSpecifiedWorkableDays).getDate());
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
            taskPropertiesController.updateTaskStartDate(start);
            taskPropertiesController.updateTaskEndDate(end);
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

    public IntraDayDate getAllocationEnd() {
        return getTask().calculateEndGivenWorkableDays(
                workableDaysAndDatesBinder.getValue());
    }

    public IntraDayDate getAllocationStart() {
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
        CalculatedValue c = allocationRowsHandler.getCalculatedValue();
        this.allResourcesPerDay.setDisabled(rows.isEmpty()
                || c == CalculatedValue.RESOURCES_PER_DAY
                || !recommendedAllocation || isAnyManual()
                || isTaskUpdatedFromTimesheets());
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
            each.addListenerForResourcesPerDayInputChange(resourcesPerDayRowInputChange);
        }
    }

    private List<AllocationRow> addListeners(List<AllocationRow> list) {
        for (AllocationRow each : list) {
            each.addListenerForInputChange(onChangeEnableApply);
        }
        return list;
    }

    public void doApply() {
        AllocationResult allocationResult = resourceAllocationModel
                .onAllocationContext(new IResourceAllocationContext<AllocationResult>() {

                    @Override
                    public AllocationResult doInsideTransaction() {
                        return allocationRowsHandler.doAllocation().getValue();
                    }
                });
        allocationProduced(allocationResult);
    }

    /**
     *
     * @return <code>true</code> if and only if operation completed and must
     *         exit the edition form
     */
    public boolean accept() {
        if (isTaskUpdatedFromTimesheets()) {
            return true;
        }

        Flagged<AllocationResult, Warnings> result = resourceAllocationModel
                .accept();

        // result can be null when editing milestones
        if (result != null && result.isFlagged()) {
            allocationProduced(result.getValue());
        }
        return result == null || !result.isFlagged();
    }

    private void allocationProduced(AllocationResult allocationResult) {
        lastAllocation = allocationResult;
        aggregate = lastAllocation.getAggregate();
        allResourcesPerDayVisibilityRule();
        sumResourcesPerDayFromRowsAndAssignToAllResourcesPerDay();
        reloadValues();
    }

    private void reloadValues() {
        loadResourcesPerDay();
        loadEffortValues();
        loadValueForEffortInput();
        loadDerivedAllocations();
        loadSclassRowSatisfied();
        loadAssignmentFunctionNames();
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

    private void loadAssignmentFunctionNames() {
        for (AllocationRow each : rows) {
            each.loadAssignmentFunctionName();
        }
    }

    private void loadEffortValues() {
        for (AllocationRow each : rows) {
            each.loadEffort();
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
            public void onEvent(Event event) {
                doApply();
                FormBinder.this.applyButton.setDisabled(true);
            }
        };
        Util.ensureUniqueListener(this.applyButton, Events.ON_CLICK,
                applyButtonListener);
    }

    public EffortDuration getAssignedEffort() {
        EffortDuration result = effortInput.getEffortDurationValue();
        if (result == null) {
            throw new RuntimeException("assignedHoursComponent returns null");
        }
        return result;
    }

    public Integer getWorkableDays() {
        return workableDaysAndDatesBinder.getValue();
    }

    public void setDeleteButtonFor(AllocationRow row, Button deleteButton) {
        deleteButton.addEventListener(Events.ON_CLICK, new EventListener() {

            @Override
            public void onEvent(Event event) {
                applyButton.setDisabled(false);
            }
        });
    }

    public void newAllocationAdded() {
        applyButton.setDisabled(false);
    }

    public void markAssignedHoursMustBePositive() {
        throw new WrongValueException(effortInput,
                _("it must be greater than zero"));
    }

    public void markRepeatedResources(List<Resource> resources) {
        messagesForUser.showMessage(
                Level.ERROR,
                _("{0} already assigned to resource allocation list",
                        StringUtils.join(getResourcesDescriptions(resources),
                                ", ")));
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
                        _("there are no resources for required criteria: {0}. So the generic allocation can't be added",
                                Criterion.getCaptionFor(resourceType,
                                        criterions)));
    }

    public void markThereisAlreadyAssignmentWith(ResourceEnum resourceType,
            Collection<? extends Criterion> criterions) {
        messagesForUser.showMessage(
                Level.ERROR,
                _("already exists an allocation for criteria {0}",
                        Criterion.getCaptionFor(resourceType, criterions)));
    }

    public void markNoEmptyResourcesPerDay(List<AllocationRow> rows) {
        Validate.isTrue(!rows.isEmpty());
        final String message = _("resources per day cannot be empty or less than zero");
        if (!recommendedAllocation) {
            AllocationRow first = rows.get(0);
            throw new WrongValueException(
                    first.getIntendedResourcesPerDayInput(), message);
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
        this.btnRecommendedAllocation.setDisabled(isAnyManual()
                || isTaskUpdatedFromTimesheets());
        Util.ensureUniqueListener(recommendedAllocation, Events.ON_CLICK,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        activatingRecommendedAllocation();
                    }
                });
    }

    public EventListener getRecommendedAllocationListener() {
        return new EventListener() {
            @Override
            public void onEvent(Event event) {
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
            effortInput.addEventListener(Events.ON_CHANGE, allHoursInputChange);
            allResourcesPerDay.addEventListener(Events.ON_CHANGE,
                    allResourcesPerDayChange);
            resetStateForResourcesPerDayInputsWhenDoingRecommendedAllocation();
        }
        Util.reloadBindings(allocationsGrid);
    }

    private void resetStateForResourcesPerDayInputsWhenDoingRecommendedAllocation() {
        if (allResourcesPerDay.isDisabled()) {
            allResourcesPerDay.setValue((BigDecimal) null);
            AllocationRow.unknownResourcesPerDay(rows);
        } else {
            allResourcesPerDay.setValue(BigDecimal.ONE);
            distributeResourcesPerDayToRows();
            allResourcesPerDay.focus();
        }
    }

    private void distributeHoursFromTotalToRows() {
        EffortDuration value = effortInput.getEffortDurationValue();
        value = value != null ? value : zero();
        int[] seconds = hoursDistributorForRecommendedAllocation
                .distribute(value.getSeconds());
        assignEfforts(rows, asEfforts(seconds));
    }

    private EffortDuration[] asEfforts(int[] seconds) {
        EffortDuration[] result = new EffortDuration[seconds.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = EffortDuration.seconds(seconds[i]);
        }
        return result;
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
        effortInput.removeEventListener(Events.ON_CHANGE, allHoursInputChange);
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
            if (each.getIntendedResourcesPerDayInput().isValid()) {
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

    public void setAllOriginalEffort(Label allOriginalEffort) {
        this.allOriginalEffort = allOriginalEffort;
    }

    public Label getAllOriginalEffort() {
        return allOriginalEffort;
    }

    public void setAllTotalEffort(Label allTotalHours) {
        this.allTotalEffort = allTotalHours;
    }

    public Label getAllTotalEffort() {
        return allTotalEffort;
    }

    public void setAllConsolidatedEffort(Label allConsolidatedEffort) {
        this.allConsolidatedEffort = allConsolidatedEffort;
    }

    public Label getAllConsolidatedEffort() {
        return allConsolidatedEffort;
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
            allOriginalEffort.setValue(sumAllOriginalEffort(this.rows)
                    .toFormattedString());
            allTotalEffort.setValue(sumAllTotalEffort(this.rows)
                    .toFormattedString());
            allConsolidatedEffort.setValue(AllocationRow
                    .sumAllConsolidatedEffort(this.rows).toFormattedString());
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

    public boolean isAnyNotFlat() {
        for (AllocationRow allocationRow : allocationRowsHandler
                .getCurrentRows()) {
            if (allocationRow.isAssignmentFunctionNotFlat()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyManual() {
        for (AllocationRow allocationRow : allocationRowsHandler
                .getCurrentRows()) {
            if (allocationRow.isAssignmentFunctionManual()) {
                return true;
            }
        }
        return false;
    }

    public boolean isTaskUpdatedFromTimesheets() {
        return allocationRowsHandler.isTaskUpdatedFromTimesheets();
    }

}
