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

package org.navalplanner.web.planner.order;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.daos.IDayAssignmentDAO;
import org.navalplanner.business.planner.daos.ISubcontractedTaskDataDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.DerivedDayAssignment;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.zul.Messagebox;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
/**
 * A command that saves the changes in the taskElements.
 * It can be considered the final step in the conversation <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@OnConcurrentModification(goToPage = "/planner/index.zul;company_scheduling")
public class SaveCommand implements ISaveCommand {

    private static final Log LOG = LogFactory.getLog(SaveCommand.class);

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Autowired
    private ISubcontractedTaskDataDAO subcontractedTaskDataDAO;

    private PlanningState state;

    private Order order;

    @Autowired
    private IAdHocTransactionService transactionService;

    private List<IAfterSaveListener> listeners = new ArrayList<IAfterSaveListener>();

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Override
    public void setState(PlanningState state) {
        this.state = state;
    }

    @Override
    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public void doAction(IContext<TaskElement> context) {
        if (state.getScenarioInfo().isUsingTheOwnerScenario()
                || userAcceptsCreateANewOrderVersion()) {
            transactionService.runOnTransaction(new IOnTransaction<Void>() {
                @Override
                public Void execute() {
                    doTheSaving();
                    return null;
                }
            });
            state.getScenarioInfo().afterCommit();
            fireAfterSave();
            notifyUserThatSavingIsDone();
        }
    }

    private void fireAfterSave() {
        for (IAfterSaveListener listener : listeners) {
            listener.onAfterSave();
        }
    }

    private void notifyUserThatSavingIsDone() {
        try {
            Messagebox.show(_("Scheduling saved"), _("Information"), Messagebox.OK,
                    Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void doTheSaving() {
        if (!state.getScenarioInfo().isUsingTheOwnerScenario()) {
            state.getScenarioInfo().saveVersioningInfo(orderDAO, scenarioDAO,
                    taskSourceDAO);
        }
        saveTasksToSave();
        removeTasksToRemove();
        subcontractedTaskDataDAO.removeOrphanedSubcontractedTaskData();
    }

    private void removeTasksToRemove() {
        for (TaskElement taskElement : state.getToRemove()) {
            if (taskElementDAO.exists(taskElement.getId())) {
                // it might have already been saved in a previous save action
                try {
                    taskElementDAO.remove(taskElement.getId());
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void saveTasksToSave() {
        for (TaskElement taskElement : state.getTasksToSave()) {
            removeDetachedDerivedDayAssignments(taskElement);
            taskElementDAO.save(taskElement);
            if (taskElement.getTaskSource() != null
                    && taskElement.getTaskSource().isNewObject()) {
                saveTaskSources(taskElement);
            }
            dontPoseAsTransient(taskElement);
        }
        if (!state.getTasksToSave().isEmpty()) {
            updateRootTaskPosition();
        }
    }

    private void saveTaskSources(TaskElement taskElement) {
        taskSourceDAO.save(taskElement.getTaskSource());
        taskElement.getTaskSource().dontPoseAsTransientObjectAnymore();
        if (taskElement.isLeaf()) {
            return;
        }
        for (TaskElement each : taskElement.getChildren()) {
            saveTaskSources(each);
        }
    }

    private void removeDetachedDerivedDayAssignments(TaskElement taskElement) {
        for (ResourceAllocation<?> each : taskElement.getSatisfiedResourceAllocations()) {
            for (DerivedAllocation eachDerived : each.getDerivedAllocations()) {
                removeAssigments(eachDerived.getDetached());
                eachDerived.clearDetached();
            }
        }
    }

    private void removeAssigments(Set<DerivedDayAssignment> detached) {
        List<DerivedDayAssignment> toRemove = new ArrayList<DerivedDayAssignment>();
        for (DerivedDayAssignment eachAssignment : detached) {
            if (!eachAssignment.isNewObject()) {
                toRemove.add(eachAssignment);
            }
        }
        dayAssignmentDAO.removeDerived(toRemove);
    }

    // newly added TaskElement such as milestones must be called
    // dontPoseAsTransientObjectAnymore
    private void dontPoseAsTransient(TaskElement taskElement) {
        if (taskElement.isNewObject()) {
            taskElement.dontPoseAsTransientObjectAnymore();
        }
        Set<ResourceAllocation<?>> resourceAllocations = taskElement.getSatisfiedResourceAllocations();
        dontPoseAsTransient(resourceAllocations);
        if (!taskElement.isLeaf()) {
            for (TaskElement each : taskElement.getChildren()) {
                dontPoseAsTransient(each);
            }
        }
    }

    private void dontPoseAsTransient(
            Set<ResourceAllocation<?>> resourceAllocations) {
        for (ResourceAllocation<?> each : resourceAllocations) {
            each.dontPoseAsTransientObjectAnymore();
            each.makeAssignmentsContainersDontPoseAsTransientAnyMore();
            for (DayAssignment eachAssignment : each.getAssignments()) {
                eachAssignment.dontPoseAsTransientObjectAnymore();
            }
            for (DerivedAllocation eachDerived : each.getDerivedAllocations()) {
                eachDerived.dontPoseAsTransientObjectAnymore();
                for (DerivedDayAssignment eachAssignment : eachDerived
                        .getAssignments()) {
                    eachAssignment.dontPoseAsTransientObjectAnymore();
                }
            }
        }
    }

    private void updateRootTaskPosition() {
        TaskGroup rootTask = state.getRootTask();
        final Date min = minDate(state.getTasksToSave());
        if (min != null) {
            rootTask.setStartDate(min);
        }
        final Date max = maxDate(state.getTasksToSave());
        if (max != null) {
            rootTask.setEndDate(max);
        }
        taskElementDAO.save(rootTask);
    }

    private Date maxDate(Collection<? extends TaskElement> tasksToSave) {
        List<Date> endDates = toEndDates(tasksToSave);
        return endDates.isEmpty() ? null : Collections.max(endDates);
    }

    private List<Date> toEndDates(Collection<? extends TaskElement> tasksToSave) {
        List<Date> result = new ArrayList<Date>();
        for (TaskElement taskElement : tasksToSave) {
            Date endDate = taskElement.getEndDate();
            if (endDate != null) {
                result.add(endDate);
            } else {
                LOG.warn("the task" + taskElement + " has null end date");
            }
        }
        return result;
    }

    private Date minDate(Collection<? extends TaskElement> tasksToSave) {
        List<Date> startDates = toStartDates(tasksToSave);
        return startDates.isEmpty() ? null : Collections.min(startDates);
    }

    private List<Date> toStartDates(
            Collection<? extends TaskElement> tasksToSave) {
        List<Date> result = new ArrayList<Date>();
        for (TaskElement taskElement : tasksToSave) {
            Date startDate = taskElement.getStartDate();
            if (startDate != null) {
                result.add(startDate);
            } else {
                LOG.warn("the task" + taskElement + " has null start date");
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return _("Save");
    }

    @Override
    public void addListener(IAfterSaveListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(IAfterSaveListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getImage() {
        return "/common/img/ico_save.png";
    }

    private boolean userAcceptsCreateANewOrderVersion() {
        try {
            int status = Messagebox
                    .show(
                            _("Confirm creating a new order version for this scenario and derived. Are you sure?"),
                            _("New order version"), Messagebox.OK
                                    | Messagebox.CANCEL, Messagebox.QUESTION);
            return (Messagebox.OK == status);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
