/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.planner.taskedition;
import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.entities.ITaskPositionConstrained;
import org.libreplan.business.planner.entities.PositionConstraintType;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskPositionConstraint;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.web.I18nHelper;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.TaskEditFormComposer;
import org.zkoss.ganttz.TaskEditFormComposer.TaskDTO;
import org.zkoss.ganttz.data.TaskContainer;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.api.Combobox;
import org.zkoss.zul.api.Datebox;
import org.zkoss.zul.api.Row;
import org.zkoss.zul.api.Tabpanel;

/**
 * Controller for edit {@link Task} popup.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component("taskPropertiesController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TaskPropertiesController extends GenericForwardComposer {

    @Autowired
    private IScenarioManager scenarioManager;

    private TaskEditFormComposer taskEditFormComposer = new TaskEditFormComposer();

    private EditTaskController editTaskController;

    private TaskElement currentTaskElement;

    private Tabpanel tabpanel;

    private Intbox hours;

    private Intbox duration;

    private Decimalbox budget;

    private Datebox startDateBox;

    private Datebox endDateBox;

    private Combobox startConstraintTypes;

    private Datebox startConstraintDate;

    private Row startConstraint;

    private IContextWithPlannerTask<TaskElement> currentContext;

    private Row resourceAllocationType;

    private Listbox lbResourceAllocationType;

    private ResourceAllocationTypeEnum originalState;

    private boolean disabledConstraintsAndAllocations = false;

    public void init(final EditTaskController editTaskController,
            IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement) {
        this.editTaskController = editTaskController;
        this.currentContext = context;
        this.currentTaskElement = taskElement;

        Order order = null;
        if (context != null) {
            order = findOrderIn(context);
        } else {
            order = taskElement.getOrderElement().getOrder();
        }

        // WebStartConstraintType.setItems(startConstraintTypes, order);
        setItemsStartConstraintTypesCombo(order);
        originalState = getResourceAllocationType(currentTaskElement);
        setOldState(originalState);

        disabledConstraintsAndAllocations = currentTaskElement
                .isSubcontractedAndWasAlreadySent()
                || currentTaskElement.isLimitingAndHasDayAssignments();
        if (!disabledConstraintsAndAllocations && (currentTaskElement.isTask())) {
            disabledConstraintsAndAllocations = ((Task) currentTaskElement)
                    .isManualAnyAllocation();
        }
        startConstraintTypes.setDisabled(disabledConstraintsAndAllocations);
        startConstraintDate.setDisabled(disabledConstraintsAndAllocations);
        lbResourceAllocationType.setDisabled(disabledConstraintsAndAllocations);

        if (context != null) {
            taskEditFormComposer.init(context.getRelativeTo(), context.getTask());
        }
        updateComponentValuesForTask();
    }

    private void setItemsStartConstraintTypesCombo(Order order) {
        startConstraintTypes.getChildren().clear();
        for (PositionConstraintType type : PositionConstraintType.values()) {
            if ((type != PositionConstraintType.AS_LATE_AS_POSSIBLE &&
                        type != PositionConstraintType.AS_SOON_AS_POSSIBLE) ||
                    (type == PositionConstraintType.AS_LATE_AS_POSSIBLE &&
                        order.getDeadline() != null) ||
                    (type == PositionConstraintType.AS_SOON_AS_POSSIBLE &&
                        order.getInitDate() != null)) {
                Comboitem comboitem = new Comboitem(_(type.getName()));
                comboitem.setValue(type);
                startConstraintTypes.appendChild(comboitem);
            }
        }
    }

    private Order findOrderIn(IContextWithPlannerTask<TaskElement> context) {
        TaskElement topTask = context.getMapper().findAssociatedDomainObject(
                findTopMostTask(context));
        return topTask.getParent().getOrderElement().getOrder();
    }

    private OrderElement findOrderElementIn(IContextWithPlannerTask<TaskElement> context) {
        TaskElement topTask = context.getMapper().findAssociatedDomainObject(
                findTopMostTask(context));
        return topTask.getOrderElement();
    }

    private org.zkoss.ganttz.data.Task findTopMostTask(
            IContextWithPlannerTask<TaskElement> context) {
        List<? extends TaskContainer> parents = context.getMapper().getParents(
                context.getTask());
        return parents.isEmpty() ? context.getTask() : parents.get(parents
                .size() - 1);
    }

    private void setOldState(ResourceAllocationTypeEnum state) {
        lbResourceAllocationType.setVariable("oldState", state, true);
    }

    private ResourceAllocationTypeEnum getOldState() {
        return (ResourceAllocationTypeEnum) lbResourceAllocationType
                .getVariable("oldState", true);
    }

    private void setResourceAllocationType(Listbox listbox, ResourceAllocationTypeEnum value) {
        setResourceAllocationType(listbox, value.toString());
    }

    private void setResourceAllocationType(Listbox listbox, String label) {
        for (Iterator i = listbox.getChildren().iterator(); i.hasNext(); ) {
            Listitem item = (Listitem) i.next();
            Listcell cell = (Listcell) item.getFirstChild();
            if (cell.getLabel() != null && cell.getLabel().equals(label)) {
                item.setSelected(true);
            }
        }
    }

    private void updateComponentValuesForTask() {
        if (currentTaskElement instanceof Task) {
            Task task = (Task) currentTaskElement;
            showDurationRow(task);
            showStartConstraintRow(task);
            showResourceAllocationTypeRow(task);
        } else {
            hideDurationRow();
            if (currentTaskElement instanceof ITaskPositionConstrained) {
                showStartConstraintRow((ITaskPositionConstrained) currentTaskElement);
            } else {
                hideStartConstraintRow();
            }
            hideResourceAllocationTypeRow();
        }
        hours.setValue(currentTaskElement.getWorkHours());
        budget.setValue(currentTaskElement.getBudget());
        Util.reloadBindings(tabpanel);
    }

    private void hideResourceAllocationTypeRow() {
        resourceAllocationType.setVisible(false);
    }

    private void showResourceAllocationTypeRow(Task task) {
        resourceAllocationType.setVisible(true);
    }

    private void hideStartConstraintRow() {
        startConstraint.setVisible(false);
    }

    private void showStartConstraintRow(ITaskPositionConstrained task) {
        startConstraint.setVisible(true);
        PositionConstraintType type = task.getPositionConstraint()
                .getConstraintType();
        startConstraintTypes.setSelectedItemApi(findComboWithType(type));
        updateStartConstraint(type);
    }

    private Comboitem findComboWithType(PositionConstraintType type) {
        for (Object component : startConstraintTypes.getChildren()) {
            if (component instanceof Comboitem) {
                Comboitem item = (Comboitem) component;
                if (((PositionConstraintType) item.getValue()) == type) {
                    return item;
                }
            }
        }
        return null;
    }

    private void constraintTypeChoosen(PositionConstraintType constraint) {
        startConstraintDate.setVisible(constraint.isAssociatedDateRequired());
        updateStartConstraint(constraint);
    }

    private void updateStartConstraint(PositionConstraintType type) {
        TaskPositionConstraint taskStartConstraint = currentTaskElementAsTaskLeafConstraint()
                .getPositionConstraint();
        startConstraintDate.setVisible(type.isAssociatedDateRequired());
        if (taskStartConstraint.getConstraintDateAsDate() != null) {
            startConstraintDate.setValue(taskStartConstraint
                    .getConstraintDateAsDate());
        }
    }

    private boolean saveConstraintChanges() {
        TaskPositionConstraint taskConstraint = currentTaskElementAsTaskLeafConstraint()
                .getPositionConstraint();
        PositionConstraintType type = (PositionConstraintType) startConstraintTypes
                .getSelectedItemApi().getValue();
        IntraDayDate inputDate = type.isAssociatedDateRequired() ? IntraDayDate
                .startOfDay(LocalDate.fromDateFields(startConstraintDate
                        .getValue())) : null;
        if (taskConstraint.isValid(type, inputDate)) {
            taskConstraint.update(type, inputDate);
            //at this point we could call currentContext.recalculatePosition(currentTaskElement)
            //to trigger the scheduling algorithm, but we don't do it because
            //the ResourceAllocationController, which is attached to the other
            //tab of the same window, will do it anyway.
            return true;
        } else {
            return false;
        }
    }

    private ITaskPositionConstrained currentTaskElementAsTaskLeafConstraint() {
        return (ITaskPositionConstrained) currentTaskElement;
    }

    private void hideDurationRow() {
        hours.getFellow("durationRow").setVisible(false);
    }

    private void showDurationRow(Task task) {
        hours.getFellow("durationRow").setVisible(true);
        duration.setValue(task.getWorkableDays());
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        tabpanel = (Tabpanel) comp;
        taskEditFormComposer.doAfterCompose(comp);
        startConstraintTypes.addEventListener(Events.ON_SELECT,
                new EventListener() {

                    @Override
                    public void onEvent(Event event) {
                        PositionConstraintType constraint = (PositionConstraintType) startConstraintTypes
                                .getSelectedItemApi().getValue();
                        constraintTypeChoosen(constraint);
                    }
                });

        lbResourceAllocationType.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) {
                SelectEvent se = (SelectEvent) event;

                final ResourceAllocationTypeEnum oldState = getOldState();
                ResourceAllocationTypeEnum newState = getSelectedValue(new ArrayList(se.getSelectedItems()));
                if (thereIsTransition(newState)) {
                    if (isConsolidatedTask()) {
                        restoreOldState();
                        editTaskController.showNonPermitChangeResourceAllocationType();
                    } else {
                        if(newState.equals(ResourceAllocationTypeEnum.SUBCONTRACT) && checkCompatibleAllocation()){
                            changeResourceAllocationType(oldState, newState);
                            editTaskController.selectAssignmentTab(lbResourceAllocationType
                                .getSelectedIndex() + 1);
                        }else{
                            try {
                                restoreOldState();
                                Messagebox.show(_("This resource allocation type is incompatible. The task has an associated order element which has a progress that is of type subcontractor. "),
                                        _("Error"), Messagebox.OK , Messagebox.ERROR);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (oldState == null) {
                    setOldState(newState);
                }
            }

            private ResourceAllocationTypeEnum getSelectedValue(List<Listitem> selectedItems) {
                final Listitem item = (Listitem) selectedItems.get(0);
                final Listcell cell = (Listcell) item.getChildren().get(0);
                return ResourceAllocationTypeEnum.asEnum(cell.getLabel());
            }

                    private void restoreOldState() {
                        Util.reloadBindings(lbResourceAllocationType);
                    }

        });

    }

    private boolean checkCompatibleAllocation(){
        OrderElement orderElement = null;
        AdvanceType advanceType = PredefinedAdvancedTypes.SUBCONTRACTOR.getType();

        if (this.currentContext  != null) {
            orderElement = findOrderElementIn(this.currentContext );
        } else {
            orderElement = this.currentTaskElement.getOrderElement();
        }
        if(orderElement.getAdvanceAssignmentByType(advanceType) != null){
                return false;
        }
        try {
            DirectAdvanceAssignment newAdvanceAssignment = DirectAdvanceAssignment
                    .create();
            newAdvanceAssignment.setAdvanceType(advanceType);
            orderElement.checkAncestorsNoOtherAssignmentWithSameAdvanceType(
                    orderElement.getParent(), newAdvanceAssignment);
        } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
            return false;
        }
        return true;
    }

    private boolean thereIsTransition(ResourceAllocationTypeEnum newState) {
        return getOldState() != null && !getOldState().equals(newState);
    }

    public TaskDTO getGanttTaskDTO() {
        if (taskEditFormComposer == null) {
            return null;
        }
        return taskEditFormComposer.getTaskDTO();
    }

    public void accept() {
        boolean ok = true;
        if (currentTaskElement instanceof ITaskPositionConstrained) {
            ok = saveConstraintChanges();
        }
        if (ok) {
            if (disabledConstraintsAndAllocations) {
                taskEditFormComposer.accept();
            } else {
                taskEditFormComposer.acceptWithoutCopyingDates();
            }
        }
    }

    public void cancel() {
        taskEditFormComposer.cancel();
    }

    /**
     * Enum for showing type of resource assignation option list
     *
     * @author Diego Pino Garcia <dpino@igalia.com>
     *
     */
    public enum ResourceAllocationTypeEnum {
        NON_LIMITING_RESOURCES(_("Non limiting resource assignment")),
        LIMITING_RESOURCES(_("Limiting resource assignation")),
        SUBCONTRACT(_("Subcontract"));

        /**
         * Forces to mark the string as needing translation
         */
        private static String _(String string) {
            return string;
        }

        private String option;

        private static final List<ResourceAllocationTypeEnum> nonMasterOptionList = new ArrayList<ResourceAllocationTypeEnum>() {
            {
                add(NON_LIMITING_RESOURCES);
                add(SUBCONTRACT);
            }
        };

        private ResourceAllocationTypeEnum(String option) {
            this.option = option;
        }

        public String toString() {
            return I18nHelper._(option);
        }

        public static List<ResourceAllocationTypeEnum> getOptionList() {
            return Arrays.asList(values());
        }

        public static List<ResourceAllocationTypeEnum> getOptionListForNonMasterBranch() {
            return nonMasterOptionList;
        }

        public static ResourceAllocationTypeEnum getDefault() {
            return NON_LIMITING_RESOURCES;
        }

        public static ResourceAllocationTypeEnum asEnum(String label) {
            if (NON_LIMITING_RESOURCES.toString().equals(label)) {
                return NON_LIMITING_RESOURCES;
            } else if (LIMITING_RESOURCES.toString().equals(label)) {
                return LIMITING_RESOURCES;
            } else if (SUBCONTRACT.toString().equals(label)) {
                return SUBCONTRACT;
            }
            return getDefault();
        }

    }

    public List<ResourceAllocationTypeEnum> getResourceAllocationTypeOptionList() {
        if (scenarioManager.getCurrent().isMaster()) {
            return ResourceAllocationTypeEnum.getOptionList();
        } else {
            return ResourceAllocationTypeEnum.getOptionListForNonMasterBranch();
        }
    }

    public ResourceAllocationTypeEnum getResourceAllocationType() {
        return getResourceAllocationType(currentTaskElement);
    }

    /**
     * Does nothing, but it must exist for receiving selected value from listbox
     *
     * @param resourceAllocation
     */
    public void setResourceAllocationType(ResourceAllocationTypeEnum resourceAllocation) {

    }

    public ResourceAllocationTypeEnum getResourceAllocationType(TaskElement taskElement) {
        if (taskElement == null || !isTask(taskElement)) {
            return null;
        }
        return getResourceAllocationType(asTask(currentTaskElement));
    }

    /**
     * Returns type of resource allocation depending on state of task
     *
     * If task is subcontracted, return a SUBCONTRACT state
     * If task has at least one limiting resource, returns a LIMITING RESOURCE state
     * Otherwise, return default state (NON-LIMITING RESOURCE)
     *
     * @return
     */
    public ResourceAllocationTypeEnum getResourceAllocationType(Task task) {
        ResourceAllocationTypeEnum result = ResourceAllocationTypeEnum.NON_LIMITING_RESOURCES;

        if (task.isSubcontracted()) {
            result = ResourceAllocationTypeEnum.SUBCONTRACT;
        }
        if (task.isLimiting()) {
            result = ResourceAllocationTypeEnum.LIMITING_RESOURCES;
        }
        return result;
    }

    private boolean isTask(TaskElement taskElement) {
        return taskElement instanceof Task;
    }

    private Task asTask(TaskElement taskElement) {
        return (Task) taskElement;
    }

    private void changeResourceAllocationType(ResourceAllocationTypeEnum from, ResourceAllocationTypeEnum to) {
        if (from.equals(ResourceAllocationTypeEnum.NON_LIMITING_RESOURCES)) {
            fromNonLimitingResource(to);
        } else if (from.equals(ResourceAllocationTypeEnum.LIMITING_RESOURCES)) {
            fromLimitingResource(to);
        } else if (from.equals(ResourceAllocationTypeEnum.SUBCONTRACT)) {
            fromSubcontract(to);
        }
    }

    /**
     * Change state from NonLimitingResource assignation type to a new state (limiting, subcontract)
     *
     * @param newState
     */
    private void fromNonLimitingResource(ResourceAllocationTypeEnum newState) {
        if (!isTask(currentTaskElement)) {
            return;
        }

        Task task = asTask(currentTaskElement);
        if (task.hasResourceAllocations()) {
            try {
                if (Messagebox.show(_("Assigned resources for this task will be deleted. Are you sure?"),
                        _("Warning"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION) == Messagebox.OK) {
                    task.removeAllResourceAllocations();
                    setStateTo(newState);
                } else {
                    resetStateTo(ResourceAllocationTypeEnum.NON_LIMITING_RESOURCES);
                }
                return;
            } catch (InterruptedException e) {

            }
        }
        setStateTo(newState);
    }

    private void setStateTo(ResourceAllocationTypeEnum state) {
        setOldState(state);
        editTaskController.showTabPanel(state);
    }

    private void resetStateTo(ResourceAllocationTypeEnum state) {
        setResourceAllocationType(lbResourceAllocationType, state);
        setOldState(state);
    }

    /**
     * Change state from LimitingResource assignation type to a new state (non-limiting, subcontract)
     *
     * @param newState
     */
    private void fromLimitingResource(ResourceAllocationTypeEnum newState) {
        if (!isTask(currentTaskElement)) {
            return;
        }

        Task task = asTask(currentTaskElement);
        if (task.hasResourceAllocations()) {
            try {
                if (Messagebox.show(_("Assigned resources for this task will be deleted. Are you sure?"),
                        _("Warning"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION) == Messagebox.OK) {
                    task.removeAllResourceAllocations();
                    setStateTo(newState);
                } else {
                    resetStateTo(ResourceAllocationTypeEnum.LIMITING_RESOURCES);
                }
                return;
            } catch (InterruptedException e) {

            }
        }
        setStateTo(newState);
    }

    /**
     * Change state from Subcontract assignation type to a new state (non-limiting, limiting)
     *
     * @param newState
     */
    private void fromSubcontract(ResourceAllocationTypeEnum newState) {
        Task task = asTask(currentTaskElement);

        if (task.isSubcontracted()) {
            final Date communicationDate = (task.getSubcontractedTaskData() != null) ?
                    task.getSubcontractedTaskData().getSubcontractCommunicationDate()
                    : null;

            // Notification has been sent
            if (communicationDate != null) {
                try {
                    if (Messagebox.show(_("IMPORTANT: Don't forget to communicate to subcontractor that his contract has been cancelled"),
                            _("Warning"), Messagebox.OK, Messagebox.EXCLAMATION) == Messagebox.OK) {
                        setStateTo(newState);
                    } else {
                        resetStateTo(ResourceAllocationTypeEnum.SUBCONTRACT);
                    }
                    return;
                } catch (InterruptedException e) {

                }
            }
        }
        setStateTo(newState);
    }

    public boolean stateHasChanged() {
        final ResourceAllocationTypeEnum currentState = getCurrentState();
        return currentState != null && !currentState.equals(getOriginalState());
    }

    public ResourceAllocationTypeEnum getOriginalState() {
        return originalState;
    }

    public ResourceAllocationTypeEnum getCurrentState() {
        return getSelectedResourceAllocationType();
    }

    private ResourceAllocationTypeEnum getSelectedResourceAllocationType() {
        final Listitem item = lbResourceAllocationType.getSelectedItem();
        if (item == null) {
            return null;
        }

        final Listcell cell = (Listcell) item.getChildren().get(0);
        return ResourceAllocationTypeEnum.asEnum(cell.getLabel());
    }

    public boolean isConsolidatedTask() {
        Task task = asTask(currentTaskElement);
        if (task != null) {
            return task.hasConsolidations();
        }
        return false;
    }

    public void updateTaskEndDate(LocalDate endDate) {
        getGanttTaskDTO().endDate = endDate.toDateTimeAtStartOfDay().toDate();
        Util.reloadBindings(endDateBox);
    }

    public void updateTaskStartDate(LocalDate newStart) {
        getGanttTaskDTO().beginDate = newStart.toDateTimeAtStartOfDay()
                .toDate();
        Util.reloadBindings(startDateBox);
    }

    public TaskEditFormComposer getTaskEditFormComposer() {
        return taskEditFormComposer;
    }

    public void refreshTaskEndDate() {
        Util.reloadBindings(endDateBox);
    }

    public String getMoneyFormat() {
        return Util.getMoneyFormat();
    }

}
