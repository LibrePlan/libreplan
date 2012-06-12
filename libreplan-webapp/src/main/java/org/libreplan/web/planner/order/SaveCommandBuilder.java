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

package org.libreplan.web.planner.order;
import static org.libreplan.business.planner.limiting.entities.LimitingResourceQueueDependency.toQueueDependencyType;
import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.InvalidValue;
import org.joda.time.LocalDate;
import org.libreplan.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.IEntitySequenceDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.ISumChargedEffortRecalculator;
import org.libreplan.business.orders.entities.ISumExpensesRecalculator;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLineGroup;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.planner.daos.IConsolidationDAO;
import org.libreplan.business.planner.daos.IDependencyDAO;
import org.libreplan.business.planner.daos.ISubcontractedTaskDataDAO;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.DerivedAllocation;
import org.libreplan.business.planner.entities.DerivedDayAssignment;
import org.libreplan.business.planner.entities.DerivedDayAssignmentsContainer;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.SubcontractorDeliverDate;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.consolidations.CalculatedConsolidatedValue;
import org.libreplan.business.planner.entities.consolidations.CalculatedConsolidation;
import org.libreplan.business.planner.entities.consolidations.ConsolidatedValue;
import org.libreplan.business.planner.entities.consolidations.Consolidation;
import org.libreplan.business.planner.entities.consolidations.NonCalculatedConsolidatedValue;
import org.libreplan.business.planner.entities.consolidations.NonCalculatedConsolidation;
import org.libreplan.business.planner.limiting.daos.ILimitingResourceQueueDependencyDAO;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.libreplan.business.scenarios.daos.IScenarioDAO;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.users.daos.IOrderAuthorizationDAO;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.web.common.concurrentdetection.ConcurrentModificationHandling;
import org.libreplan.web.planner.TaskElementAdapter;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.adapters.DomainDependency;
import org.zkoss.ganttz.adapters.IAdapterToTaskFundamentalProperties;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.ConstraintCalculator;
import org.zkoss.ganttz.data.DependencyType.Point;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.extensions.IContext;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

/**
 * Builds a command that saves the changes in the taskElements. It can be
 * considered the final step in the conversation <br />
 *
 * In the save operation it is also kept the consistency of the
 * LimitingResourceQueueDependencies with the Dependecies between the task of
 * the planning gantt.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SaveCommandBuilder {


    private static final Log LOG = LogFactory.getLog(SaveCommandBuilder.class);

    public ISaveCommand build(PlanningState planningState,
            PlannerConfiguration<TaskElement> plannerConfiguration) {
        SaveCommand result = new SaveCommand(planningState,
                plannerConfiguration);
        return ConcurrentModificationHandling.addHandling(
                "/planner/index.zul;company_scheduling", ISaveCommand.class,
                result);
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

    @Autowired
    private IConsolidationDAO consolidationDAO;

    @Autowired
    private IEntitySequenceDAO entitySequenceDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private ISubcontractedTaskDataDAO subcontractedTaskDataDAO;

    @Autowired
    private ILimitingResourceQueueDependencyDAO limitingResourceQueueDependencyDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    private IDependencyDAO dependencyDAO;

    @Autowired
    private ISumChargedEffortRecalculator sumChargedEffortRecalculator;

    @Autowired
    private ISumExpensesRecalculator sumExpensesRecalculator;

    private class SaveCommand implements ISaveCommand {

        private PlanningState state;

        private PlannerConfiguration<TaskElement> configuration;

        private ConstraintCalculator<TaskElement> constraintCalculator;

        private IAdapterToTaskFundamentalProperties<TaskElement> adapter;

        private final List<IAfterSaveListener> listenersAfter = new ArrayList<IAfterSaveListener>();

        private final List<IBeforeSaveListener> listenersBefore = new ArrayList<IBeforeSaveListener>();

        private boolean disabled = false;

        public SaveCommand(PlanningState planningState,
                PlannerConfiguration<TaskElement> configuration) {
            this.state = planningState;
            this.configuration = configuration;
            this.adapter = configuration.getAdapter();
            this.constraintCalculator = new ConstraintCalculator<TaskElement>(
                    configuration.isScheduleBackwards()) {

                @Override
                protected GanttDate getStartDate(TaskElement vertex) {
                    return TaskElementAdapter.toGantt(vertex
                            .getIntraDayStartDate());
                }

                @Override
                protected GanttDate getEndDate(TaskElement vertex) {
                    return TaskElementAdapter.toGantt(vertex
                            .getIntraDayEndDate());
                }
            };
        }

        @Override
        public void doAction(IContext<TaskElement> context) {
            if (disabled) {
                return;
            }

            save(null, new IAfterSaveActions() {

                @Override
                public void doActions() {
                    notifyUserThatSavingIsDone();
                }
            });
        }

        @Override
        public void save(IBeforeSaveActions beforeSaveActions) {
            save(beforeSaveActions, new IAfterSaveActions() {

                @Override
                public void doActions() {
                    notifyUserThatSavingIsDone();
                }
            });
        }

        @Override
        public void save(final IBeforeSaveActions beforeSaveActions,
                IAfterSaveActions afterSaveActions) {
            try {
                if (state.getScenarioInfo().isUsingTheOwnerScenario()
                        || userAcceptsCreateANewOrderVersion()) {
                    fireBeforeSave();
                    transactionService
                            .runOnTransaction(new IOnTransaction<Void>() {
                                @Override
                                public Void execute() {
                                    if (beforeSaveActions != null) {
                                        beforeSaveActions.doActions();
                                    }
                                    doTheSaving();
                                    return null;
                                }
                            });
                    dontPoseAsTransientObjectAnymore(state.getOrder());
                    dontPoseAsTransientObjectAnymore(state.getOrder()
                            .getEndDateCommunicationToCustomer());
                    state.getScenarioInfo().afterCommit();

                    if (state.getOrder()
                            .isNeededToRecalculateSumChargedEfforts()) {
                        sumChargedEffortRecalculator.recalculate(state
                                .getOrder().getId());
                    }

                    if (state.getOrder().isNeededToRecalculateSumExpenses()) {
                        sumExpensesRecalculator.recalculate(state.getOrder().getId());
                    }

                    fireAfterSave();
                    if (afterSaveActions != null) {
                        afterSaveActions.doActions();
                    }
                }
            } catch (ValidationException validationException) {
                if (Executions.getCurrent() == null) {
                    throw validationException;
                }

                try {
                    String message = validationException.getMessage();
                    for (InvalidValue invalidValue : validationException
                            .getInvalidValues()) {
                        message += "\n" + invalidValue.getPropertyName() + ": "
                                + invalidValue.getMessage();
                    }
                    Messagebox.show(
                            _("Error saving the project\n{0}", message),
                            _("Error"), Messagebox.OK, Messagebox.ERROR);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        private void fireAfterSave() {
            for (IAfterSaveListener listener : listenersAfter) {
                listener.onAfterSave();
            }
        }

        private void fireBeforeSave() {
            for (IBeforeSaveListener listener : listenersBefore) {
                listener.onBeforeSave();
            }
        }

        private void notifyUserThatSavingIsDone() {
            if (Executions.getCurrent() == null) {
                // test environment
                return;
            }
            try {
                Messagebox.show(_("Project saved"), _("Information"),
                        Messagebox.OK, Messagebox.INFORMATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void doTheSaving() {
            Order order = state.getOrder();
            generateOrderElementCodes(order);
            createAdvancePercentagesIfRequired(order);
            calculateAndSetTotalHours(order);
            checkConstraintOrderUniqueCode(order);
            checkConstraintHoursGroupUniqueCode(order);
            state.synchronizeTrees();

            TaskGroup rootTask = state.getRootTask();

            if (rootTask != null) {
                // This reattachment is needed to ensure that the root task in
                // the state is the one associated to the transaction's session.
                // Otherwise if some order element has been removed, when doing
                // the deletes on cascade a new root task is fetched causing a
                // NonUniqueObjectException later
                taskElementDAO.reattach(rootTask);
            }
            orderDAO.save(order);

            saveDerivedScenarios(order);
            deleteOrderElementWithoutParent(order);
            deleteUnboundedDependencies();

            updateTasksRelatedData();
            removeTasksToRemove();
            loadDataAccessedWithNotPosedAsTransientInOrder(state.getOrder());
            loadDataAccessedWithNotPosedAsTransient(state.getOrder());
            if (state.getRootTask() != null) {
                loadDependenciesCollectionsForTaskRoot(state.getRootTask());
            }
            subcontractedTaskDataDAO.removeOrphanedSubcontractedTaskData();

            saveOrderAuthorizations();

            removeTaskElementsWithTaskSourceNull();

            state.updateSavedOrderState();
        }

        private void removeTaskElementsWithTaskSourceNull() {
            List<TaskElement> toRemove = taskElementDAO
                    .getTaskElementsNoMilestonesWithoutTaskSource();
            List<TaskElement> parentsWithChangesToSave = new ArrayList<TaskElement>();
            for (TaskElement taskElement : toRemove) {
                try {
                    taskElementDAO.remove(taskElement.getId());

                    TaskGroup parent = taskElement.getParent();
                    if (parent != null && !toRemove.contains(parent)) {
                        parent.remove(taskElement);
                        parentsWithChangesToSave.add(parent);
                    }

                    LOG.info("TaskElement removed because of TaskSource was null. "
                            + taskElement);
                } catch (InstanceNotFoundException e) {
                    // Do nothing
                    // Maybe it was already removed before reaching this point
                    // so if it's not in the database there isn't any problem
                }
            }
            for (TaskElement taskElement : parentsWithChangesToSave) {
                taskElementDAO.save(taskElement);
            }
        }

        private void saveOrderAuthorizations() {
            for (OrderAuthorization each : state
                    .getOrderAuthorizationsAddition()) {
                orderAuthorizationDAO.save(each);
            }
            for (OrderAuthorization each : state
                    .getOrderAuthorizationsRemoval()) {
                try {
                    orderAuthorizationDAO.remove(each.getId());
                } catch (InstanceNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            state.cleanOrderAuthorizationsAdditionAndRemoval();
        }

        private void createAdvancePercentagesIfRequired(Order order) {
            List<OrderElement> allChildren = order.getAllChildren();
            for (OrderElement each : allChildren) {
                createAdvancePercentageIfRequired(each);
            }
        }

        private void createAdvancePercentageIfRequired(OrderElement orderElement) {
            DirectAdvanceAssignment advancePercentage = orderElement
                    .getDirectAdvanceAssignmentByType(PredefinedAdvancedTypes.PERCENTAGE
                            .getType());

            if ((orderElement.isSchedulingPoint())
                    && (orderElement.getReportGlobalAdvanceAssignment() == null)
                    && (advancePercentage == null)) {
                createAdvancePercentage(orderElement);
            }
        }

        private void createAdvancePercentage(OrderElement orderElement) {
            DirectAdvanceAssignment newAdvance = DirectAdvanceAssignment
                    .create();
            newAdvance.setOrderElement(orderElement);

            AdvanceType type = PredefinedAdvancedTypes.PERCENTAGE.getType();
            newAdvance.setAdvanceType(type);
            newAdvance.setMaxValue(type.getDefaultMaxValue());
            newAdvance.setReportGlobalAdvance(true);

            try {
                orderElement.addAdvanceAssignment(newAdvance);
            } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
                // This shouldn't happen
                throw new RuntimeException(e);
            } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
                // Do nothing.
                // This means that some parent has already defined an advance
                // percentage so we don't need to create it at this point
            }
        }

        private void generateOrderElementCodes(Order order) {
            order.generateOrderElementCodes(entitySequenceDAO
                    .getNumberOfDigitsCode(EntityNameEnum.ORDER));
        }

        private void calculateAndSetTotalHours(Order order) {
            int result = 0;
            for (OrderElement orderElement : order.getChildren()) {
                result = result + orderElement.getWorkHours();
            }
            order.setTotalHours(result);
        }

        private void checkConstraintOrderUniqueCode(OrderElement order) {
            OrderElement repeatedOrder;

            // Check no code is repeated in this order
            if (order instanceof OrderLineGroup) {
                repeatedOrder = ((OrderLineGroup) order)
                        .findRepeatedOrderCode();
                if (repeatedOrder != null) {
                    throw new ValidationException(_(
                            "Repeated Project code {0} in Project {1}",
                            repeatedOrder.getCode(), repeatedOrder.getName()));
                }
            }

            // Check no code is repeated within the DB
            repeatedOrder = Registry.getOrderElementDAO()
                    .findRepeatedOrderCodeInDB(order);
            if (repeatedOrder != null) {
                throw new ValidationException(_(
                        "Repeated Project code {0} in Project {1}",
                        repeatedOrder.getCode(), repeatedOrder.getName()));
            }
        }

        private void checkConstraintHoursGroupUniqueCode(Order order) {
            HoursGroup repeatedHoursGroup;

            if (order instanceof OrderLineGroup) {
                repeatedHoursGroup = ((OrderLineGroup) order)
                        .findRepeatedHoursGroupCode();
                if (repeatedHoursGroup != null) {
                    throw new ValidationException(_(
                            "Repeated Hours Group code {0} in Project {1}",
                            repeatedHoursGroup.getCode(), repeatedHoursGroup
                                    .getParentOrderLine().getName()));
                }
            }

            repeatedHoursGroup = Registry.getHoursGroupDAO()
                    .findRepeatedHoursGroupCodeInDB(order.getHoursGroups());
            if (repeatedHoursGroup != null) {
                throw new ValidationException(_(
                        "Repeated Hours Group code {0} in Project {1}",
                        repeatedHoursGroup.getCode(), repeatedHoursGroup
                                .getParentOrderLine().getName()));
            }
        }

        private void saveDerivedScenarios(Order order) {
            List<Scenario> derivedScenarios = scenarioDAO
                    .getDerivedScenarios(state.getCurrentScenario());
            for (Scenario scenario : derivedScenarios) {
                scenario.addOrder(order, order.getCurrentOrderVersion());
            }
        }

        private void deleteOrderElementWithoutParent(Order order)
                throws ValidationException {
            List<OrderElement> listToBeRemoved = orderElementDAO
                    .findWithoutParent();
            for (OrderElement orderElement : listToBeRemoved) {
                if (!(orderElement instanceof Order)) {
                    tryToRemove(orderElement);
                }
            }
        }

        private void deleteUnboundedDependencies() {
            try {
                dependencyDAO.deleteUnattachedDependencies();
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        private void tryToRemove(OrderElement orderElement) {
            // checking no work reports for that orderElement
            if (orderElementDAO
                    .isAlreadyInUseThisOrAnyOfItsChildren(orderElement)) {
                return;
            }
            try {
                orderElementDAO.remove(orderElement.getId());
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        private void removeTasksToRemove() {
            for (TaskElement taskElement : state.getToRemove()) {
                if (taskElementDAO.exists(taskElement.getId())) {
                    // it might have already been saved in a previous save
                    // action
                    try {
                        taskElementDAO.remove(taskElement.getId());
                    } catch (InstanceNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        private void updateTasksRelatedData() {
            TaskGroup rootTask = state.getRootTask();
            if (rootTask == null) {
                return;
            }
            for (TaskElement taskElement : rootTask.getChildren()) {
                removeEmptyConsolidation(taskElement);
                updateLimitingResourceQueueElementDates(taskElement);
                if (taskElement.getTaskSource() != null
                        && taskElement.getTaskSource().isNewObject()) {
                    saveTaskSources(taskElement);
                }
                updateLimitingQueueDependencies(taskElement);
            }
            saveRootTask();
        }

        private void saveRootTask() {
            TaskGroup rootTask = state.getRootTask();
            updateRootTaskPosition(rootTask);
            taskElementDAO.save(rootTask);
        }

        private void updateRootTaskPosition(TaskGroup rootTask) {
            final IntraDayDate min = minDate(rootTask.getChildren());
            if (min != null) {
                rootTask.setIntraDayStartDate(min);
            }
            final IntraDayDate max = maxDate(rootTask.getChildren());
            if (max != null) {
                rootTask.setIntraDayEndDate(max);
            }
        }

        private void saveTaskSources(TaskElement taskElement) {
            TaskSource taskSource = taskElement.getTaskSource();
            if (taskSource != null) {
                taskSourceDAO.save(taskSource);
            }
            if (taskElement.isLeaf()) {
                return;
            }
            for (TaskElement each : taskElement.getChildren()) {
                saveTaskSources(each);
            }
        }

        private void updateLimitingResourceQueueElementDates(
                TaskElement taskElement) {
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
            try {
                LimitingResourceQueueElement limiting = task
                        .getAssociatedLimitingResourceQueueElementIfAny();

                GanttDate earliestStart = resolveConstraints(task, Point.START);
                GanttDate earliestEnd = resolveConstraints(task, Point.END);

                limiting.updateDates(
                        TaskElementAdapter.toIntraDay(earliestStart),
                        TaskElementAdapter.toIntraDay(earliestEnd));
            } catch (Exception e) {
                // if this fails all the saving shouldn't fail
                LOG.error(
                        "error updating associated LimitingResourceQueueElement for task: "
                                + task, e);
            }
        }

        private GanttDate resolveConstraints(Task task, Point point) {
            List<Constraint<GanttDate>> dependencyConstraints = toConstraints(
                    adapter.getIncomingDependencies(task), point);
            List<Constraint<GanttDate>> taskConstraints = getTaskConstraints(task);

            boolean dependenciesHavePriority = configuration
                    .isDependenciesConstraintsHavePriority();
            if (dependenciesHavePriority) {
                return Constraint
                        .<GanttDate> initialValue(
                                TaskElementAdapter.toGantt(getOrderInitDate()))
                        .withConstraints(taskConstraints)
                        .withConstraints(dependencyConstraints)
                        .applyWithoutFinalCheck();
            } else {
                return Constraint
                        .<GanttDate> initialValue(
                                TaskElementAdapter.toGantt(getOrderInitDate()))
                        .withConstraints(dependencyConstraints)
                        .withConstraints(taskConstraints)
                        .applyWithoutFinalCheck();
            }
        }

        private List<Constraint<GanttDate>> getTaskConstraints(Task task) {
            return TaskElementAdapter.getStartConstraintsFor(task,
                    getOrderInitDate());
        }

        private LocalDate getOrderInitDate() {
            return LocalDate.fromDateFields(state.getRootTask()
                    .getOrderElement().getInitDate());
        }

        private List<Constraint<GanttDate>> toConstraints(
                List<DomainDependency<TaskElement>> incomingDependencies,
                Point point) {
            List<Constraint<GanttDate>> result = new ArrayList<Constraint<GanttDate>>();
            for (DomainDependency<TaskElement> each : incomingDependencies) {
                result.addAll(constraintCalculator.getConstraints(each, point));
            }
            return result;
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

        private void updateLimitingQueueDependencies(TaskElement t) {
            for (Dependency each : t.getDependenciesWithThisOrigin()) {
                addLimitingDependencyIfNeeded(each);
                removeLimitingDependencyIfNeeded(each);
            }
            for (TaskElement each : t.getChildren()) {
                updateLimitingQueueDependencies(each);
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

        private void loadDataAccessedWithNotPosedAsTransientInOrder(Order order) {
            order.getEndDateCommunicationToCustomer().size();
        }

        private void loadDataAccessedWithNotPosedAsTransient(
                OrderElement orderElement) {
            orderElement.getDirectAdvanceAssignments().size();
            getAllMeasurements(orderElement.getDirectAdvanceAssignments());
            orderElement.getIndirectAdvanceAssignments().size();
            orderElement.getCriterionRequirements().size();
            orderElement.getLabels().size();
            orderElement.getTaskQualityForms().size();
            orderElement.getAllMaterialAssignments().size();
            for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
                dontPoseAsTransientObjectAnymore(hoursGroup
                        .getCriterionRequirements());
            }

            for (OrderElement each : orderElement.getChildren()) {
                loadDataAccessedWithNotPosedAsTransient(each);
            }
        }

        // avoid LazyInitializationException when forcing the don't pose as
        // transient
        private void loadDependenciesCollectionsForTaskRoot(
                TaskElement taskElement) {
            taskElement.getDependenciesWithThisOrigin().size();
            taskElement.getDependenciesWithThisDestination().size();
        }

        private IntraDayDate maxDate(Collection<? extends TaskElement> tasksToSave) {
            List<IntraDayDate> endDates = toEndDates(tasksToSave);
            return endDates.isEmpty() ? null : Collections.max(endDates);
        }

        private List<IntraDayDate> toEndDates(
                Collection<? extends TaskElement> tasksToSave) {
            List<IntraDayDate> result = new ArrayList<IntraDayDate>();
            for (TaskElement taskElement : tasksToSave) {
                IntraDayDate endDate = taskElement.getIntraDayEndDate();
                if (endDate != null) {
                    result.add(endDate);
                } else {
                    LOG.warn("the task" + taskElement + " has null end date");
                }
            }
            return result;
        }

        private IntraDayDate minDate(Collection<? extends TaskElement> tasksToSave) {
            List<IntraDayDate> startDates = toStartDates(tasksToSave);
            return startDates.isEmpty() ? null : Collections.min(startDates);
        }

        private List<IntraDayDate> toStartDates(
                Collection<? extends TaskElement> tasksToSave) {
            List<IntraDayDate> result = new ArrayList<IntraDayDate>();
            for (TaskElement taskElement : tasksToSave) {
                IntraDayDate startDate = taskElement.getIntraDayStartDate();
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
            listenersAfter.add(listener);
        }

        @Override
        public void removeListener(IAfterSaveListener listener) {
            listenersAfter.remove(listener);
        }

        @Override
        public void addListener(IBeforeSaveListener listener) {
            listenersBefore.add(listener);
        }

        @Override
        public void removeListener(IBeforeSaveListener listener) {
            listenersBefore.remove(listener);
        }

        @Override
        public String getImage() {
            return "/common/img/ico_save.png";
        }

        private boolean userAcceptsCreateANewOrderVersion() {
            try {
                int status = Messagebox
                        .show(_("Confirm creating a new project version for this scenario and derived. Are you sure?"),
                                _("New project version"), Messagebox.OK
                                        | Messagebox.CANCEL,
                                Messagebox.QUESTION);
                return (Messagebox.OK == status);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void dontPoseAsTransientObjectAnymore(OrderElement orderElement) {
            orderElement.dontPoseAsTransientObjectAnymore();
            dontPoseAsTransientObjectAnymore(orderElement.getOrderVersions());
            dontPoseAsTransientObjectAnymore(orderElement
                    .getTaskSourcesFromBottomToTop());
            dontPoseAsTransientObjectAnymore(orderElement
                    .getSchedulingDatasForVersionFromBottomToTop());

            dontPoseAsTransientObjectAnymore(orderElement
                    .getDirectAdvanceAssignments());
            dontPoseAsTransientObjectAnymore(getAllMeasurements(orderElement
                    .getDirectAdvanceAssignments()));

            dontPoseAsTransientObjectAnymore(orderElement
                    .getIndirectAdvanceAssignments());
            dontPoseAsTransientObjectAnymore(orderElement
                    .getCriterionRequirements());
            dontPoseAsTransientObjectAnymore(orderElement.getLabels());
            dontPoseAsTransientObjectAnymoreTasks(orderElement
                    .getTaskElements());
            dontPoseAsTransientObjectAnymore(orderElement.getHoursGroups());
            dontPoseAsTransientObjectAnymore(orderElement.getTaskQualityForms());
            dontPoseAsTransientObjectAnymore(orderElement
                    .getAllMaterialAssignments());

            for (HoursGroup hoursGroup : orderElement.getHoursGroups()) {
                dontPoseAsTransientObjectAnymore(hoursGroup
                        .getCriterionRequirements());
            }

            for (OrderElement child : orderElement.getAllChildren()) {
                child.dontPoseAsTransientObjectAnymore();
                dontPoseAsTransientObjectAnymore(child);
            }
        }

        private void dontPoseAsTransientObjectAnymore(
                Collection<? extends BaseEntity> collection) {
            for (BaseEntity entity : collection) {
                entity.dontPoseAsTransientObjectAnymore();
            }
        }

        private List<AdvanceMeasurement> getAllMeasurements(
                Collection<? extends DirectAdvanceAssignment> assignments) {
            List<AdvanceMeasurement> result = new ArrayList<AdvanceMeasurement>();
            for (DirectAdvanceAssignment each : assignments) {
                result.addAll(each.getAdvanceMeasurements());
            }
            return result;
        }

        private void dontPoseAsTransientObjectAnymoreTasks(
                Collection<? extends TaskElement> taskElements) {
            for (TaskElement each : taskElements) {
                dontPoseAsTransient(each);
            }
        }

        private void dontPoseAsTransient(TaskElement taskElement) {
            if (taskElement.isNewObject()) {
                taskElement.dontPoseAsTransientObjectAnymore();
            }
            dontPoseAsTransient(taskElement.getDependenciesWithThisOrigin());
            dontPoseAsTransient(taskElement
                    .getDependenciesWithThisDestination());
            Set<ResourceAllocation<?>> resourceAllocations = taskElement
                    .getAllResourceAllocations();
            dontPoseAsTransientAndChildrenObjects(resourceAllocations);
            if (!taskElement.isLeaf()) {
                for (TaskElement each : taskElement.getChildren()) {
                    dontPoseAsTransient(each);
                }
            }
            if (taskElement instanceof Task) {
                dontPoseAsTransient(((Task) taskElement).getConsolidation());
                dontPoseAsTransient(((Task) taskElement).getSubcontractedTaskData());
            }
            if (taskElement instanceof TaskGroup) {
                ((TaskGroup) taskElement).dontPoseAsTransientPlanningData();
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

        private void dontPoseAsTransient(SubcontractedTaskData subcontractedTaskData) {
            if (subcontractedTaskData != null) {
                //dontPoseAsTransient - subcontratedTaskData
                subcontractedTaskData.dontPoseAsTransientObjectAnymore();

                for (SubcontractorDeliverDate subDeliverDate : subcontractedTaskData
                        .getRequiredDeliveringDates()) {
                    //dontPoseAsTransient - DeliverDate
                    subDeliverDate.dontPoseAsTransientObjectAnymore();
                }
            }
        }

        private void dontPoseAsTransient(
                SortedSet<? extends ConsolidatedValue> values) {
            for (ConsolidatedValue value : values) {
                value.dontPoseAsTransientObjectAnymore();
            }
        }

        private void dontPoseAsTransient(
                Collection<? extends Dependency> dependencies) {
            for (Dependency each : dependencies) {
                each.dontPoseAsTransientObjectAnymore();
                if (each.hasLimitedQueueDependencyAssociated()) {
                    each.getQueueDependency()
                            .dontPoseAsTransientObjectAnymore();
                }
            }
        }

        @Override
        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        @Override
        public boolean isDisabled() {
            return disabled;
        }
    }
}
