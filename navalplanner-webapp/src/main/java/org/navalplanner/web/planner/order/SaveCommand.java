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

package org.navalplanner.web.planner.order;

import static org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueDependency.toQueueDependencyType;
import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.IConsolidationDAO;
import org.navalplanner.business.planner.daos.ISubcontractedTaskDataDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.PlanningData;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.DerivedDayAssignment;
import org.navalplanner.business.planner.entities.DerivedDayAssignmentsContainer;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.consolidations.CalculatedConsolidatedValue;
import org.navalplanner.business.planner.entities.consolidations.CalculatedConsolidation;
import org.navalplanner.business.planner.entities.consolidations.ConsolidatedValue;
import org.navalplanner.business.planner.entities.consolidations.Consolidation;
import org.navalplanner.business.planner.entities.consolidations.NonCalculatedConsolidatedValue;
import org.navalplanner.business.planner.entities.consolidations.NonCalculatedConsolidation;
import org.navalplanner.business.planner.limiting.daos.ILimitingResourceQueueDependencyDAO;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.zul.Messagebox;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
/**
 * A command that saves the changes in the taskElements.
 * It can be considered the final step in the conversation <br />
 *
 * In the save operation it is also kept the consistency of the
 * LimitingResourceQueueDependencies with the Dependecies between
 * the task of the planning gantt.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
@OnConcurrentModification(goToPage = "/planner/index.zul;company_scheduling")
public class SaveCommand implements ISaveCommand {

    private static final Log LOG = LogFactory.getLog(SaveCommand.class);

    @Autowired
    private IConsolidationDAO consolidationDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private ISubcontractedTaskDataDAO subcontractedTaskDataDAO;

    @Autowired
    private ILimitingResourceQueueDependencyDAO limitingResourceQueueDependencyDAO;

    private PlanningState state;

    @Autowired
    private IAdHocTransactionService transactionService;

    private List<IAfterSaveListener> listeners = new ArrayList<IAfterSaveListener>();

    @Override
    public void setState(PlanningState state) {
        this.state = state;
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
            Messagebox.show(_("Scheduling saved"), _("Information"),
                    Messagebox.OK, Messagebox.INFORMATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void doTheSaving() {
        state.getScenarioInfo().saveVersioningInfo();
        saveTasksToSave();
        removeTasksToRemove();
        saveAndDontPoseAsTransientOrderElements();
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
            removeEmptyConsolidation(taskElement);
            updateLimitingResourceQueueElementDates(taskElement);
            taskElementDAO.save(taskElement);
            if (taskElement.getTaskSource() != null
                    && taskElement.getTaskSource().isNewObject()) {
                saveTaskSources(taskElement);
            }
            // Recursive iteration to put all the tasks of the
            // gantt as transiet
            dontPoseAsTransient(taskElement);
        }
        saveRootTaskIfNecessary();
    }

    private void saveRootTaskIfNecessary() {
        if (!state.getTasksToSave().isEmpty()) {
            TaskGroup rootTask = state.getRootTask();

            updateRootTaskPosition(rootTask);
            updateCriticalPathProgress(rootTask);
            taskElementDAO.save(rootTask);
        }
    }

    private void updateCriticalPathProgress(TaskGroup rootTask) {
        final List<Task> criticalPath = state.getPlanner().getCriticalPath();
        rootTask.updateCriticalPathProgress(criticalPath);
    }

    private void updateRootTaskPosition(TaskGroup rootTask) {
        final Date min = minDate(state.getTasksToSave());
        if (min != null) {
            rootTask.setStartDate(min);
        }
        final Date max = maxDate(state.getTasksToSave());
        if (max != null) {
            rootTask.setEndDate(max);
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

    private void updateLimitingResourceQueueElementDates(TaskElement taskElement) {
        if (taskElement.isLimiting()) {
            Task task = (Task) taskElement;
            updateLimitingResourceQueueElementDates(task);
        } else if (!taskElement.isLeaf()) {
            for (TaskElement each : taskElement.getChildren()) {
                updateLimitingResourceQueueElementDates(each);
            }
        }
    }

    private void updateLimitingResourceQueueElementDates(Task task) {
        LimitingResourceQueueElement limiting = task
                .getAssociatedLimitingResourceQueueElementIfAny();
        Date initDate = state.getRootTask().getOrderElement().getInitDate();
        limiting.updateDates(initDate,
                task.getDependenciesWithThisDestination());
    }

    private void removeEmptyConsolidation(TaskElement taskElement) {
        if ((taskElement.isLeaf()) && (!taskElement.isMilestone())) {
            Consolidation consolidation = ((Task) taskElement)
                    .getConsolidation();
            if ((consolidation != null)
                    && (isEmptyConsolidation(consolidation))) {
                if (!consolidation.isNewObject()) {
                    try {
                        consolidationDAO.remove(consolidation.getId());
                    } catch (InstanceNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                ((Task) taskElement).setConsolidation(null);
            }
        }
    }

    private boolean isEmptyConsolidation(final Consolidation consolidation) {
        return transactionService
                .runOnTransaction(new IOnTransaction<Boolean>() {
                    @Override
                    public Boolean execute() {

                        consolidationDAO.reattach(consolidation);
                        if (consolidation instanceof CalculatedConsolidation) {
                            SortedSet<CalculatedConsolidatedValue> consolidatedValues = ((CalculatedConsolidation) consolidation)
                                    .getCalculatedConsolidatedValues();
                            return consolidatedValues.isEmpty();
                        }
                        if (consolidation instanceof NonCalculatedConsolidation) {
                            SortedSet<NonCalculatedConsolidatedValue> consolidatedValues = ((NonCalculatedConsolidation) consolidation)
                                    .getNonCalculatedConsolidatedValues();
                            return consolidatedValues.isEmpty();
                        }
                        return false;

                    }
                });
    }

    // newly added TaskElement such as milestones must be called
    // dontPoseAsTransientObjectAnymore
    private void dontPoseAsTransient(TaskElement taskElement) {
        if (taskElement.isNewObject()) {
            taskElement.dontPoseAsTransientObjectAnymore();
        }
        dontPoseAsTransient(taskElement.getDependenciesWithThisOrigin());
        dontPoseAsTransient(taskElement.getDependenciesWithThisDestination());
        Set<ResourceAllocation<?>> resourceAllocations = taskElement
                .getSatisfiedResourceAllocations();
        dontPoseAsTransientAndChildrenObjects(resourceAllocations);
        if (!taskElement.isLeaf()) {
            for (TaskElement each : taskElement.getChildren()) {
                dontPoseAsTransient(each);
            }
        }
        if (taskElement instanceof Task) {
            updateLimitingQueueDependencies((Task) taskElement);
            dontPoseAsTransient(((Task) taskElement).getConsolidation());
        }
    }

    private void dontPoseAsTransient(
            Collection<? extends Dependency> dependencies) {
        for (Dependency each : dependencies) {
            each.dontPoseAsTransientObjectAnymore();
        }
    }

    private void updateLimitingQueueDependencies(Task t) {

        for (Dependency each : t.getDependenciesWithThisOrigin()) {
            addLimitingDependencyIfNeeded(each);
            removeLimitingDependencyIfNeeded(each);
        }
    }

    private void addLimitingDependencyIfNeeded(Dependency d) {
        if (d.isDependencyBetweenLimitedAllocatedTasks()
                && !d.hasLimitedQueueDependencyAssociated()) {
            LimitingResourceQueueElement origin = calculateQueueElementFromDependency((Task) d
                    .getOrigin());
            LimitingResourceQueueElement destiny = calculateQueueElementFromDependency((Task) d
                    .getDestination());

            LimitingResourceQueueDependency queueDependency = LimitingResourceQueueDependency
                    .create(origin, destiny, d,
                            toQueueDependencyType(d.getType()));
            d.setQueueDependency(queueDependency);
            limitingResourceQueueDependencyDAO.save(queueDependency);
            queueDependency.dontPoseAsTransientObjectAnymore();
        }
    }

    private LimitingResourceQueueElement calculateQueueElementFromDependency(
            Task t) {

        LimitingResourceQueueElement result = null;
        // TODO: Improve this method: One Task can only have one
        // limiting resource allocation
        Set<ResourceAllocation<?>> allocations = t
                .getLimitingResourceAllocations();

        if (allocations.isEmpty() || allocations.size() != 1) {
            throw new ValidationException("Incorrect limiting resource "
                    + "allocation configuration");
        }

        for (ResourceAllocation<?> r : allocations) {
            result = r.getLimitingResourceQueueElement();
        }

        return result;
    }

    private void removeLimitingDependencyIfNeeded(Dependency d) {
        if (!d.isDependencyBetweenLimitedAllocatedTasks()
                && (d.hasLimitedQueueDependencyAssociated())) {
            LimitingResourceQueueDependency queueDependency = d
                    .getQueueDependency();
            queueDependency.getHasAsOrigin().remove(queueDependency);
            queueDependency.getHasAsDestiny().remove(queueDependency);
            d.setQueueDependency(null);
            try {
                limitingResourceQueueDependencyDAO.remove(queueDependency
                        .getId());
            } catch (InstanceNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Trying to delete instance "
                        + " does not exist");
            }
        }
    }

    private void dontPoseAsTransient(OrderElement orderElement) {
        OrderElement order = (OrderElement) orderElementDAO
                .loadOrderAvoidingProxyFor(orderElement);
        order.dontPoseAsTransientObjectAnymore();
        dontPoseAsTransientAdvances(order.getDirectAdvanceAssignments());
        dontPoseAsTransientAdvances(order.getIndirectAdvanceAssignments());

        for (OrderElement child : order.getAllChildren()) {
            child.dontPoseAsTransientObjectAnymore();
            dontPoseAsTransientAdvances(child.getDirectAdvanceAssignments());
            dontPoseAsTransientAdvances(child.getIndirectAdvanceAssignments());
        }
    }

    private void dontPoseAsTransientAdvances(
            Set<? extends AdvanceAssignment> advances) {
        for (AdvanceAssignment advance : advances) {
            advance.dontPoseAsTransientObjectAnymore();
            if (advance instanceof DirectAdvanceAssignment) {
                dontPoseAsTransientMeasure(((DirectAdvanceAssignment) advance)
                        .getAdvanceMeasurements());
            }
        }
    }

    private void dontPoseAsTransientMeasure(SortedSet<AdvanceMeasurement> list) {
        for (AdvanceMeasurement measure : list) {
            measure.dontPoseAsTransientObjectAnymore();
        }
    }

    private void dontPoseAsTransient(Consolidation consolidation) {
        if (consolidation != null) {
            consolidation.dontPoseAsTransientObjectAnymore();
            if (consolidation.isCalculated()) {
                dontPoseAsTransient(((CalculatedConsolidation) consolidation)
                        .getCalculatedConsolidatedValues());
            } else {
                dontPoseAsTransient(((NonCalculatedConsolidation) consolidation)
                        .getNonCalculatedConsolidatedValues());
            }
        }
    }

    private void saveAndDontPoseAsTransientOrderElements() {
        for (TaskElement taskElement : state.getTasksToSave()) {
            if (taskElement.getOrderElement() != null) {
                orderElementDAO.save(taskElement.getOrderElement());
                dontPoseAsTransient(taskElement.getOrderElement());
            }
        }
    }

    private void dontPoseAsTransient(
            SortedSet<? extends ConsolidatedValue> values) {
        for (ConsolidatedValue value : values) {
            value.dontPoseAsTransientObjectAnymore();
        }
    }

    public static void dontPoseAsTransientAndChildrenObjects(
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        for (ResourceAllocation<?> each : resourceAllocations) {
            each.dontPoseAsTransientObjectAnymore();
            each.makeAssignmentsContainersDontPoseAsTransientAnyMore();
            for (DayAssignment eachAssignment : each.getAssignments()) {
                eachAssignment.dontPoseAsTransientObjectAnymore();
            }
            for (DerivedAllocation eachDerived : each.getDerivedAllocations()) {
                eachDerived.dontPoseAsTransientObjectAnymore();
                Collection<DerivedDayAssignmentsContainer> containers = eachDerived
                        .getContainers();
                for (DerivedDayAssignmentsContainer eachContainer : containers) {
                    eachContainer.dontPoseAsTransientObjectAnymore();
                }
                for (DerivedDayAssignment eachAssignment : eachDerived
                        .getAssignments()) {
                    eachAssignment.dontPoseAsTransientObjectAnymore();
                }
            }
            dontPoseAsTransient(each.getLimitingResourceQueueElement());
        }
    }

    private static void dontPoseAsTransient(LimitingResourceQueueElement element) {
        if (element != null) {
            for (LimitingResourceQueueDependency d : element
                    .getDependenciesAsOrigin()) {
                d.dontPoseAsTransientObjectAnymore();
            }
            for (LimitingResourceQueueDependency d : element
                    .getDependenciesAsDestiny()) {
                d.dontPoseAsTransientObjectAnymore();
            }
            element.dontPoseAsTransientObjectAnymore();
        }
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
                    .show(_("Confirm creating a new order version for this scenario and derived. Are you sure?"),
                            _("New order version"), Messagebox.OK
                                    | Messagebox.CANCEL, Messagebox.QUESTION);
            return (Messagebox.OK == status);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
