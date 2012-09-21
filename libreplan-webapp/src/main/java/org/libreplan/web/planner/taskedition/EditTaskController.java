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

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.planner.entities.ITaskPositionConstrained;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.allocation.ResourceAllocationController;
import org.libreplan.web.planner.limiting.allocation.LimitingResourceAllocationController;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.libreplan.web.planner.order.SubcontractController;
import org.libreplan.web.planner.taskedition.TaskPropertiesController.ResourceAllocationTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.TaskComponent;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.api.Tab;
import org.zkoss.zul.api.Tabbox;
import org.zkoss.zul.api.Tabpanel;
import org.zkoss.zul.api.Window;

/**
 * Controller for edit a {@link Task}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component("editTaskController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EditTaskController extends GenericForwardComposer {

    @Autowired
    private TaskPropertiesController taskPropertiesController;

    @Autowired
    private ResourceAllocationController resourceAllocationController;

    @Autowired
    private LimitingResourceAllocationController limitingResourceAllocationController;

    @Autowired
    private SubcontractController subcontractController;

    private Window window;

    private Tabbox editTaskTabbox;

    private Tab taskPropertiesTab;
    private Tab resourceAllocationTab;
    private Tab limitingResourceAllocationTab;
    private Tab subcontractTab;

    private Tabpanel taskPropertiesTabpanel;
    private Tabpanel resourceAllocationTabpanel;
    private Tabpanel limitingResourceAllocationTabpanel;
    private Tabpanel subcontractTabpanel;

    private Component messagesContainer;

    private IMessagesForUser messagesForUser;

    private TaskElement taskElement;

    private IContextWithPlannerTask<TaskElement> context;

    private PlanningState planningState;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);

        window = (Window) comp;
        taskPropertiesController.doAfterCompose(taskPropertiesTabpanel);
        resourceAllocationController.doAfterCompose(resourceAllocationTabpanel);
        resourceAllocationController.setEditTaskController(this);
        subcontractController.doAfterCompose(subcontractTabpanel);
        initLimitingResourceAllocationController();
    }

    public void initLimitingResourceAllocationController() throws Exception {
        limitingResourceAllocationController.doAfterCompose(limitingResourceAllocationTabpanel);
        limitingResourceAllocationController.setEditTaskController(this);
    }

    public IMessagesForUser getMessagesForUser() {
        return messagesForUser;
    }

    public TaskPropertiesController getTaskPropertiesController() {
        return taskPropertiesController;
    }

    public ResourceAllocationController getResourceAllocationController() {
        return resourceAllocationController;
    }

    public LimitingResourceAllocationController getLimitingResourceAllocationController() {
        return limitingResourceAllocationController;
    }

    public SubcontractController getSubcontractController() {
        return subcontractController;
    }

    private void showEditForm(IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement, PlanningState planningState) {
        showEditForm(context, taskElement, planningState, false);
    }

    private void showEditForm(IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement, PlanningState planningState,
            boolean fromLimitingResourcesView) {
        this.taskElement = taskElement;
        this.context = context;
        this.planningState = planningState;

        taskPropertiesController.init(this, context, taskElement);

        try {
            window.setTitle(_("Edit task: {0}", taskElement.getName()));
            showSelectedTabPanel();
            Util.reloadBindings(window);
            if (fromLimitingResourcesView) {
                window.doModal();
            } else {
                window.setMode("modal");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void showSelectedTabPanel() {
        showTabPanel(taskPropertiesController
                .getResourceAllocationType(taskElement));
    }

    public void showTabPanel(
            ResourceAllocationTypeEnum resourceAllocationType) {
        subcontractTab.setVisible(false);
        resourceAllocationTab.setVisible(false);
        limitingResourceAllocationTab.setVisible(false);

        if (ResourceAllocationTypeEnum.SUBCONTRACT
                .equals(resourceAllocationType)) {

            subcontractController.init(asTask(taskElement), context,
                    taskPropertiesController.getTaskEditFormComposer());
            showSubcontractTab();
        } else if (ResourceAllocationTypeEnum.NON_LIMITING_RESOURCES
                .equals(resourceAllocationType)) {
            resourceAllocationController.init(context, asTask(taskElement), planningState, messagesForUser);
            showNonLimitingResourcesTab();
        } else if (ResourceAllocationTypeEnum.LIMITING_RESOURCES
                .equals(resourceAllocationType)) {
            limitingResourceAllocationController.init(context, asTask(taskElement),
                    planningState, messagesForUser);
            showLimitingResourcesTab();
        }
    }

    private void showSubcontractTab() {
        subcontractTab.setVisible(true);
    }

    private void showNonLimitingResourcesTab() {
        resourceAllocationController.clear();
        resourceAllocationTab.setVisible(true);
    }

    private void showLimitingResourcesTab() {
        limitingResourceAllocationController.clear();
        limitingResourceAllocationTab.setVisible(true);
    }

    public void showEditFormTaskProperties(
            IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement, PlanningState planningState) {
        editTaskTabbox.setSelectedPanelApi(taskPropertiesTabpanel);
        showEditForm(context, taskElement, planningState);
    }

    public void showEditFormResourceAllocationFromLimitingResources(TaskElement taskElement) {
        limitingResourceAllocationController.setDisableHours(false);
        taskPropertiesTab.setVisible(false);
        showEditFormResourceAllocation(null, taskElement, null, true);
    }

    public void showEditFormResourceAllocation(
            IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement, PlanningState planningState) {
        showEditFormResourceAllocation(context, taskElement, planningState,
                false);
    }

    public void showEditFormResourceAllocation(
            IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement, PlanningState planningState,
            boolean fromLimitingResourcesView) {

        if (isTask(taskElement)) {
            Task task = asTask(taskElement);
            if (task.isLimiting()) {
                editTaskTabbox.setSelectedPanelApi(limitingResourceAllocationTabpanel);
            } else {
                editTaskTabbox.setSelectedPanelApi(resourceAllocationTabpanel);
            }
        } else {
            editTaskTabbox.setSelectedPanelApi(taskPropertiesTabpanel);
        }
        showEditForm(context, taskElement, planningState,
                fromLimitingResourcesView);
    }

    public void selectAssignmentTab(int index) {
        editTaskTabbox.setSelectedIndex(index);
    }

    public void showEditFormSubcontract(
            IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement, PlanningState planningState) {
        if (isSubcontractedAndIsTask(taskElement)) {
            editTaskTabbox.setSelectedPanelApi(subcontractTabpanel);
        } else {
            editTaskTabbox.setSelectedPanelApi(taskPropertiesTabpanel);
        }
        showEditForm(context, taskElement, planningState);
    }

    public void accept() {
        try {
            if (taskPropertiesController.stateHasChanged()) {
                ResourceAllocationTypeEnum oldState = taskPropertiesController.getOriginalState();
                removeAssociatedData(oldState);
            }

            editTaskTabbox.setSelectedPanelApi(taskPropertiesTabpanel);
            taskPropertiesController.accept();

            ResourceAllocationTypeEnum currentState = taskPropertiesController.getCurrentState();
            if (ResourceAllocationTypeEnum.NON_LIMITING_RESOURCES.equals(currentState)) {
                editTaskTabbox.setSelectedPanelApi(resourceAllocationTabpanel);
                boolean mustNotExit = !resourceAllocationController.accept();
                if (mustNotExit) {
                    return;
                }
            } else if (ResourceAllocationTypeEnum.SUBCONTRACT.equals(currentState)) {
                editTaskTabbox.setSelectedPanelApi(subcontractTabpanel);
                subcontractController.accept();
            } else if (ResourceAllocationTypeEnum.LIMITING_RESOURCES.equals(currentState)) {
                editTaskTabbox.setSelectedPanelApi(limitingResourceAllocationTabpanel);
                limitingResourceAllocationController.accept();
            }

            askForReloads();

            taskElement = null;
            context = null;

            window.setVisible(false);
            setStatus(Messagebox.OK);
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    private void removeAssociatedData(ResourceAllocationTypeEnum state) {
        Task task = asTask(taskElement);

        if (state.equals(ResourceAllocationTypeEnum.SUBCONTRACT)) {
            task.removeSubcontractCommunicationDate();
            task.setSubcontractedTaskData(null);
            subcontractController.removeSubcontractedTaskData();
        }
    }

    public Task asTask(TaskElement taskElement) {
        return (Task) taskElement;
    }

    private void askForReloads() {
        if (context != null) {
            org.zkoss.ganttz.data.Task.reloadResourcesText(context);
            context.reloadCharts();
            if (context.getRelativeTo() instanceof TaskComponent) {
                TaskComponent taskComponent = (TaskComponent) context
                        .getRelativeTo();
                taskComponent.updateProperties();
                taskComponent.invalidate();
                taskComponent.recalculatePositionOfSiblings();
            }
        }
    }

    public void cancel() {
        taskPropertiesController.cancel();
        subcontractController.cancel();
        resourceAllocationController.cancel();

        taskElement = null;
        context = null;

        window.setVisible(false);
        setStatus(Messagebox.CANCEL);
    }

    public boolean isSubcontractedAndIsTask() {
        return isSubcontractedAndIsTask(taskElement);
    }

    private boolean isSubcontractedAndIsTask(TaskElement task) {
        return (isTask(task) && task.isSubcontracted());
    }

    private boolean isTask(TaskElement taskElement) {
        return (taskElement != null && taskElement instanceof Task);
    }

    public boolean isNotSubcontractedAndIsTask() {
        return isNotSubcontractedAndIsTask(taskElement);
    }

    private boolean isNotSubcontractedAndIsTask(TaskElement task) {
        return (isTask(task) && !task.isSubcontracted());
    }

    public boolean isTask() {
        return isTask(taskElement);
    }

    private boolean isTaskLeafConstraint() {
        return (taskElement != null && taskElement instanceof ITaskPositionConstrained);
    }

    public void showNonPermitChangeResourceAllocationType() {
        String message = _("The task has got progress consolidations. To change resource allocation type all consolidations must be removed before");
        try {
            Messagebox.show(message, _("Information"), Messagebox.OK,
                    Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            messagesForUser.showMessage(Level.INFO, message);
        }
    }

    public void close(Event event) {
        event.stopPropagation();
        self.setVisible(false);
        setStatus(Messagebox.CANCEL);
    }

    public void setStatus(Integer status) {
        self.setVariable("status", status, true);
    }

    public Integer getStatus() {
        return (Integer) self.getVariable("status", true);
    }
}
