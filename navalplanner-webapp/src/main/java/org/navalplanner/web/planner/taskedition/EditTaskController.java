/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.planner.taskedition;

import static org.navalplanner.web.I18nHelper._;

import java.util.Date;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.planner.allocation.AllocationResult;
import org.navalplanner.web.planner.allocation.FormBinder;
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.IAdvanceAllocationResultReceiver;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.Restriction;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.Restriction.IRestrictionSource;
import org.navalplanner.web.planner.limiting.allocation.LimitingResourceAllocationController;
import org.navalplanner.web.planner.order.PlanningState;
import org.navalplanner.web.planner.order.SubcontractController;
import org.navalplanner.web.planner.taskedition.TaskPropertiesController.ResourceAllocationTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.TaskComponent;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.Component;
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
        window = (Window) comp;
        taskPropertiesController.doAfterCompose(taskPropertiesTabpanel);
        resourceAllocationController.doAfterCompose(resourceAllocationTabpanel);
        subcontractController.doAfterCompose(subcontractTabpanel);
        limitingResourceAllocationController.doAfterCompose(limitingResourceAllocationTabpanel);
        messagesForUser = new MessagesForUser(messagesContainer);
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
        this.taskElement = taskElement;
        this.context = context;
        this.planningState = planningState;

        taskPropertiesController.init(this, context, taskElement);

        try {
            window.setTitle(_("Edit task: {0}", taskElement.getName()));
            window.setMode("modal");
            showSelectedTabPanel();
            Util.reloadBindings(window);
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
            subcontractController.init(asTask(taskElement), context);
            showSubcontractTab();
        } else if (ResourceAllocationTypeEnum.NON_LIMITING_RESOURCES
                .equals(resourceAllocationType)) {
            resourceAllocationController.init(context, asTask(taskElement), planningState, messagesForUser);
            showNonLimitingResourcesTab();
        } else if (ResourceAllocationTypeEnum.LIMITING_RESOURCES
                .equals(resourceAllocationType)) {
            limitingResourceAllocationController.init(asTask(taskElement),
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

    public void showEditFormResourceAllocation(
            IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement, PlanningState planningState) {

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
        showEditForm(context, taskElement, planningState);
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
                resourceAllocationController.accept();
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
            context.getTask().reloadResourcesText();
            context.reloadCharts();

            if (context.getRelativeTo() instanceof TaskComponent) {
                ((TaskComponent) context.getRelativeTo()).invalidate();
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

    public void goToAdvancedAllocation() {
        FormBinder formBinder = resourceAllocationController.getFormBinder();

        AllocationResult allocationResult = formBinder.getLastAllocation();
        if (allocationResult.getAggregate().isEmpty()) {
            formBinder.doApply();
            allocationResult = formBinder.getLastAllocation();
        }
        resourceAllocationController.getSwitcher().goToAdvancedAllocation(
                allocationResult, createResultReceiver(allocationResult));
        window.setVisible(false);
    }

    private IAdvanceAllocationResultReceiver createResultReceiver(
            final AllocationResult allocation) {
        return new AdvanceAllocationResultReceiver(allocation);
    }

    private final class AdvanceAllocationResultReceiver implements
            IAdvanceAllocationResultReceiver {

        private final AllocationResult allocation;
        private final IRestrictionSource restrictionSource;

        private AdvanceAllocationResultReceiver(AllocationResult allocation) {
            this.allocation = allocation;
            final int totalHours = allocation.getAggregate().getTotalHours();
            final LocalDate start = allocation.getStart();
            final LocalDate end = start.plusDays(allocation.getDaysDuration());
            final CalculatedValue calculatedValue = allocation
                    .getCalculatedValue();
            restrictionSource = new IRestrictionSource() {

                @Override
                public int getTotalHours() {
                    return totalHours;
                }

                @Override
                public LocalDate getStart() {
                    return start;
                }

                @Override
                public LocalDate getEnd() {
                    return end;
                }

                @Override
                public CalculatedValue getCalculatedValue() {
                    return calculatedValue;
                }
            };
        }

        @Override
        public void cancel() {
            showEditFormResourceAllocation(context, taskElement, planningState);
        }

        @Override
        public void accepted(AggregateOfResourceAllocations aggregate) {
            resourceAllocationController.accept(allocation);
        }

        @Override
        public Restriction createRestriction() {
            return Restriction.build(restrictionSource);
        }
    }

    public boolean isTask() {
        return isTask(taskElement);
    }

    public Date getStartConstraintDate() {
        if ((taskElement == null) || (!isTask())) {
            return null;
        }

        return ((Task) taskElement).getStartConstraint().getConstraintDate();
    }

    public void setStartConstraintDate(Date date) {
        if ((taskElement != null) && (isTask())) {
            resourceAllocationController.setStartDate(date);
        }
    }

    public void showNonPermitChangeResourceAllocationType() {
        String message = _("The task has got advance consolidations.It must delete all consolidations to change the resource allocation type ");
        try {
            Messagebox.show(message, _("Information"), Messagebox.OK,
                    Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            messagesForUser.showMessage(Level.INFO, message);
        }
    }

}
