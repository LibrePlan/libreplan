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
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.planner.allocation.AllocationResult;
import org.navalplanner.web.planner.allocation.FormBinder;
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.IAdvanceAllocationResultReceiver;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.Restriction;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.Restriction.IRestrictionSource;
import org.navalplanner.web.planner.order.PlanningState;
import org.navalplanner.web.planner.order.SubcontractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
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
    private SubcontractController subcontractController;

    private Window window;

    private Tabbox editTaskTabbox;
    private Tab resourceAllocationTab;
    private Tab subcontractTab;
    private Tabpanel taskPropertiesTabpanel;
    private Tabpanel resourceAllocationTabpanel;
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
        messagesForUser = new MessagesForUser(messagesContainer);
    }

    public TaskPropertiesController getTaskPropertiesController() {
        return taskPropertiesController;
    }

    public ResourceAllocationController getResourceAllocationController() {
        return resourceAllocationController;
    }

    public SubcontractController getSubcontractController() {
        return subcontractController;
    }

    private void showEditForm(IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement, PlanningState planningState) {
        this.taskElement = taskElement;
        this.context = context;
        this.planningState = planningState;

        taskPropertiesController.init(context, taskElement);
        if (taskElement instanceof Task) {
            resourceAllocationController.init(context, (Task) taskElement,
                    planningState, messagesForUser);
            if (taskElement.isSubcontracted()) {
                subcontractController.init((Task) taskElement, context);
            }
        }

        try {
            Util.reloadBindings(window);
            window.setTitle(_("Edit task: {0}", taskElement.getName()));
            window.setMode("modal");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
        if (isNotSubcontractedAndIsTask(taskElement)) {
            editTaskTabbox.setSelectedPanelApi(resourceAllocationTabpanel);
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
            editTaskTabbox.setSelectedPanelApi(taskPropertiesTabpanel);
            taskPropertiesController.accept();

            editTaskTabbox.setSelectedPanelApi(resourceAllocationTabpanel);
            resourceAllocationController.accept();

            editTaskTabbox.setSelectedPanelApi(subcontractTabpanel);
            subcontractController.accept();

            askForReloads();

            taskElement = null;
            context = null;

            window.setVisible(false);
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    private void askForReloads() {
        if (context != null) {
            context.getTask().reloadResourcesText();
            context.reloadCharts();
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

    public void subcontract(boolean subcontract) {
        if (taskElement instanceof Task) {
            if (subcontract) {
                resourceAllocationTab.setVisible(false);
                subcontractTab.setVisible(true);
                subcontractController.init((Task) taskElement, context);
            } else {
                subcontractTab.setVisible(false);
                resourceAllocationTab.setVisible(true);
                subcontractController.removeSubcontractedTaskData();
            }
        }
    }

    public boolean isSubcontractedAndIsTask() {
        if (taskElement == null) {
            return false;
        }
        if (!isTask()) {
            return false;
        }
        return taskElement.isSubcontracted();
    }

    private boolean isSubcontractedAndIsTask(TaskElement task) {
        if (task == null) {
            return false;
        }
        if (!(task instanceof Task)) {
            return false;
        }
        return task.isSubcontracted();
    }

    public boolean isNotSubcontractedAndIsTask() {
        if (taskElement == null) {
            return false;
        }
        if (!isTask()) {
            return false;
        }
        return !taskElement.isSubcontracted();
    }

    private boolean isNotSubcontractedAndIsTask(TaskElement task) {
        if (task == null) {
            return false;
        }
        if (!(task instanceof Task)) {
            return false;
        }
        return !task.isSubcontracted();
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

        private AdvanceAllocationResultReceiver(AllocationResult allocation) {
            this.allocation = allocation;
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
            return Restriction.build(new IRestrictionSource() {

                @Override
                public int getTotalHours() {
                    return allocation.getAggregate().getTotalHours();
                }

                @Override
                public LocalDate getStart() {
                    return allocation.getStart();
                }

                @Override
                public LocalDate getEnd() {
                    return getStart().plusDays(allocation.getDaysDuration());
                }

                @Override
                public CalculatedValue getCalculatedValue() {
                    return allocation.getCalculatedValue();
                }
            });
        }
    }

    public boolean isTask() {
        return (taskElement instanceof Task);
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

}