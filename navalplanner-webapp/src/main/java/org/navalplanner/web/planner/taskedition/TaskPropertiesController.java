/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.planner.taskedition;
import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.ITaskLeafConstraint;
import org.navalplanner.business.planner.entities.StartConstraintType;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskStartConstraint;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.web.I18nHelper;
import org.navalplanner.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.TaskEditFormComposer;
import org.zkoss.ganttz.TaskEditFormComposer.TaskDTO;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Comboitem;
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

    // This is a workaround, because in business we don't have access to
    // I18nHelper
    private enum WebStartConstraintType {
        AS_SOON_AS_POSSIBLE(StartConstraintType.AS_SOON_AS_POSSIBLE) {
            @Override
            public String getDescription() {
                return _("as soon as possible");
            }

            @Override
            public String getName() {
                return _("AS_SOON_AS_POSSIBLE");
            }
        },
        START_NOT_EARLIER_THAN(StartConstraintType.START_NOT_EARLIER_THAN) {
            @Override
            public String getDescription() {
                return _("start not earlier than");
            }

            @Override
            public String getName() {
                return _("START_NOT_EARLIER_THAN");
            }
        },
        START_IN_FIXED_DATE(StartConstraintType.START_IN_FIXED_DATE) {
            @Override
            public String getDescription() {
                return _("start in fixed date");
            }

            @Override
            public String getName() {
                return _("START_IN_FIXED_DATE");
            }
        };

        public static void appendItems(Combobox combo) {
            for (WebStartConstraintType type : WebStartConstraintType.values()) {
                combo.appendChild(type.createCombo());
            }
        }

        private final StartConstraintType type;

        private WebStartConstraintType(StartConstraintType type) {
            this.type = type;
        }

        public abstract String getName();

        public abstract String getDescription();

        private Comboitem createCombo() {
            Comboitem result = new Comboitem();
            result.setValue(this);
            result.setLabel(this.getName());
            result.setDescription(this.getDescription());
            return result;
        }

        public static boolean representsType(Comboitem item,
                StartConstraintType type) {
            WebStartConstraintType webType = (WebStartConstraintType) item
                    .getValue();
            return webType.equivalentTo(type);
        }

        private boolean equivalentTo(StartConstraintType type) {
            return this.type == type;
        }

        public boolean isAssociatedDateRequired() {
            return type.isAssociatedDateRequired();
        }

        public StartConstraintType getType() {
            return type;
        }
    }

    /**
     * Controller from the Gantt to manage common fields on edit {@link Task}
     * popup.
     */

    @Autowired
    private IScenarioManager scenarioManager;

    private TaskEditFormComposer taskEditFormComposer = new TaskEditFormComposer();

    private EditTaskController editTaskController;

    private TaskElement currentTaskElement;

    private Tabpanel tabpanel;

    private Intbox hours;

    private Intbox duration;

    private Datebox endDateBox;

    private Combobox startConstraintTypes;

    private Datebox startConstraintDate;

    private Row startConstraint;

    private IContextWithPlannerTask<TaskElement> currentContext;

    private Row resourceAllocationType;

    private Listbox lbResourceAllocationType;

    private ResourceAllocationTypeEnum originalState;

    public void init(final EditTaskController editTaskController,
            IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement) {
        this.editTaskController = editTaskController;
        this.currentContext = context;
        this.currentTaskElement = taskElement;

        originalState = getResourceAllocationType(currentTaskElement);
        setOldState(originalState);

        final boolean disabled = currentTaskElement.isSubcontracted()
                || currentTaskElement.isLimitingAndHasDayAssignments();
        startConstraintTypes.setDisabled(disabled);
        startConstraintDate.setDisabled(disabled);
        lbResourceAllocationType.setDisabled(disabled);

        lbResourceAllocationType.addEventListener(Events.ON_SELECT,
                new EventListener() {
                    @Override
                    public void onEvent(Event event) throws Exception {

                        editTaskController
                                .selectAssignmentTab(lbResourceAllocationType
                                        .getSelectedIndex() + 1);
                    }
                });

        if (context != null) {
            taskEditFormComposer.init(context.getRelativeTo(), context.getTask());
        }
        updateComponentValuesForTask();
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
            if (currentTaskElement instanceof ITaskLeafConstraint) {
                showStartConstraintRow((ITaskLeafConstraint) currentTaskElement);
            } else {
                hideStartConstraintRow();
            }
            hideResourceAllocationTypeRow();
        }
        hours.setValue(currentTaskElement.getWorkHours());
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

    private void showStartConstraintRow(ITaskLeafConstraint task) {
        startConstraint.setVisible(true);
        StartConstraintType type = task.getStartConstraint()
                .getStartConstraintType();
        startConstraintTypes.setSelectedItemApi(findComboWithType(type));
        updateStartConstraint(type);
    }

    private Comboitem findComboWithType(StartConstraintType type) {
        for (Object component : startConstraintTypes.getChildren()) {
            if (component instanceof Comboitem) {
                Comboitem item = (Comboitem) component;
                if (WebStartConstraintType.representsType(item, type)) {
                    return item;
                }
            }
        }
        return null;
    }

    private void constraintTypeChoosen(WebStartConstraintType constraint) {
        startConstraintDate.setVisible(constraint.isAssociatedDateRequired());
        updateStartConstraint(constraint.getType());
    }

    private void updateStartConstraint(StartConstraintType type) {
        TaskStartConstraint taskStartConstraint = currentTaskElementAsTaskLeafConstraint()
                .getStartConstraint();
        startConstraintDate.setVisible(type.isAssociatedDateRequired());
        if (taskStartConstraint.getConstraintDateAsDate() != null) {
            startConstraintDate.setValue(taskStartConstraint
                    .getConstraintDateAsDate());
        }
    }

    private boolean saveConstraintChanges() {
        TaskStartConstraint taskConstraint = currentTaskElementAsTaskLeafConstraint()
                .getStartConstraint();
        WebStartConstraintType type = (WebStartConstraintType) startConstraintTypes
                .getSelectedItemApi().getValue();
        LocalDate inputDate = type.isAssociatedDateRequired() ? LocalDate
                .fromDateFields(startConstraintDate.getValue()) : null;
        if (taskConstraint.isValid(type.getType(), inputDate)) {
            taskConstraint.update(type.getType(), inputDate);
            if (currentContext != null) {
                currentContext.recalculatePosition(currentTaskElement);
            }
            return true;
        } else {
            return false;
        }
    }

    private ITaskLeafConstraint currentTaskElementAsTaskLeafConstraint() {
        return (ITaskLeafConstraint) currentTaskElement;
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
        WebStartConstraintType.appendItems(startConstraintTypes);
        startConstraintTypes.addEventListener(Events.ON_SELECT,
                new EventListener() {

                    @Override
                    public void onEvent(Event event) throws Exception {
                        WebStartConstraintType constraint = (WebStartConstraintType) startConstraintTypes
                                .getSelectedItemApi().getValue();
                        constraintTypeChoosen(constraint);
                    }
                });

        lbResourceAllocationType.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                SelectEvent se = (SelectEvent) event;

                final ResourceAllocationTypeEnum oldState = getOldState();
                ResourceAllocationTypeEnum newState = getSelectedValue(new ArrayList(se.getSelectedItems()));
                        if (thereIsTransition(newState)) {
                            if (isConsolidatedTask()) {
                                restoreOldState();
                                editTaskController
                                        .showNonPermitChangeResourceAllocationType();
                            } else {
                                changeResourceAllocationType(oldState, newState);
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
        if (currentTaskElement instanceof ITaskLeafConstraint) {
            ok = saveConstraintChanges();
        }
        if (ok) {
            taskEditFormComposer.accept();
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
        NON_LIMITING_RESOURCES(_("Non limiting resource assignation")),
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
}
