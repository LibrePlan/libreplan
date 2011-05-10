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
package org.navalplanner.web.planner.tabs;

import static org.navalplanner.web.I18nHelper._;
import static org.navalplanner.web.planner.tabs.MultipleTabsPlannerController.BREADCRUMBS_SEPARATOR;
import static org.navalplanner.web.planner.tabs.MultipleTabsPlannerController.getSchedulingLabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.AggregateOfResourceAllocations;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.consolidations.Consolidation;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.web.calendars.BaseCalendarModel;
import org.navalplanner.web.common.concurrentdetection.ConcurrentModificationHandling;
import org.navalplanner.web.common.entrypoints.EntryPointsHandler;
import org.navalplanner.web.common.entrypoints.EntryPointsHandler.ICapture;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.AllocationInput;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.IAdvanceAllocationResultReceiver;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.IBack;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.Restriction;
import org.navalplanner.web.planner.allocation.AdvancedAllocationController.Restriction.IRestrictionSource;
import org.navalplanner.web.planner.allocation.AllocationResult;
import org.navalplanner.web.planner.order.OrderPlanningModel;
import org.navalplanner.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class AdvancedAllocationTabCreator {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(AdvancedAllocationTabCreator.class);

    private final class ResultReceiver implements
            IAdvanceAllocationResultReceiver {

        private final CalculatedValue calculatedValue;
        private final AggregateOfResourceAllocations aggregate;
        private AllocationResult allocationResult;
        private final Task task;
        private Set<Resource> associatedResources;
        private final Scenario currentScenario;

        private final String retryPage;

        public ResultReceiver(Scenario currentScenario, final Order order,
                Task task) {
            this.currentScenario = currentScenario;
            this.calculatedValue = task.getCalculatedValue();
            this.allocationResult = AllocationResult.createCurrent(currentScenario, task);
            this.aggregate = this.allocationResult.getAggregate();
            this.task = task;
            this.associatedResources = getAssociatedResources(task);
            this.retryPage = EntryPointsHandler.capturePath(new ICapture() {
                @Override
                public void capture() {
                    globalViewEntryPoints.goToAdvancedAllocation(order);
                }
            });
            reattachResources();
            loadNeededDataOfTask();
        }

        private void loadNeededDataOfTask() {
            BaseCalendarModel.forceLoadBaseCalendar(task.getCalendar());
            loadConsolidationRelatedData(task);
        }

        private void loadConsolidationRelatedData(Task task) {
            Consolidation consolidation = task.getConsolidation();
            if (consolidation != null) {
                consolidation.getConsolidatedValues().size();
            }
        }

        private Set<Resource> getAssociatedResources(Task task) {
            Set<Resource> result = new HashSet<Resource>();
            for (ResourceAllocation<?> resourceAllocation : task
                    .getSatisfiedResourceAllocations()) {
                result.addAll(resourceAllocation.getAssociatedResources());
            }
            return result;
        }

        @Override
        public Restriction createRestriction() {
            return Restriction.build(createRestrictionSource());
        }

        private IRestrictionSource createRestrictionSource() {
            return new IRestrictionSource() {

                @Override
                public EffortDuration getTotalEffort() {
                    return aggregate.getTotalEffort();
                }

                @Override
                public LocalDate getStart() {
                    if (aggregate.isEmpty()) {
                        // FIXME Review Bug #906
                        LOG.info("the aggregate for task " + task.getName()
                                + " is empty");
                        return task.getStartAsLocalDate();
                    }
                    return aggregate.getStart().getDate();
                }

                @Override
                public LocalDate getEnd() {
                    if (aggregate.isEmpty()) {
                        // FIXME Review Bug #906
                        LOG.info("the aggregate for task " + task.getName()
                                + " is empty");
                        return task.getEndAsLocalDate();
                    }
                    return aggregate.getEnd().asExclusiveEnd();
                }

                @Override
                public CalculatedValue getCalculatedValue() {
                    return calculatedValue;
                }
            };
        }

        @Override
        public void cancel() {
            // do nothing
        }

        @Override
        public void accepted(AggregateOfResourceAllocations modifiedAllocations) {
            Validate
                    .isTrue(allocationResult.getAggregate() == modifiedAllocations);
            IAdHocTransactionService withConcurrencyHandling = ConcurrentModificationHandling
                    .addHandling(retryPage,
                    IAdHocTransactionService.class, adHocTransactionService);

            withConcurrencyHandling
                    .runOnTransaction(new IOnTransaction<Void>() {

                        @Override
                        public Void execute() {
                            reattachResources();
                            applyChanges();
                            return null;
                        }
                    });
        }

        public AggregateOfResourceAllocations getAggregate() {
            return aggregate;
        }

        private void reattachResources() {
            for (Resource each : associatedResources) {
                resourceDAO.reattach(each);
            }
            OrderPlanningModel.loadRequiredDataFor(associatedResources);
            for (Resource each : associatedResources) {
                loadDayAssignments(each.getAssignments());
            }
        }

        private void loadDayAssignments(List<DayAssignment> assignments) {
            for (DayAssignment each : assignments) {
                Hibernate.initialize(each);
            }
        }

        private void applyChanges() {
            taskElementDAO.reattach(task);
            allocationResult.applyTo(currentScenario, task);
            taskElementDAO.save(task);
            makeNewAssignmentsDontPoseAsTransient(task);
            updateParentsPositions(task);
        }

        private void makeNewAssignmentsDontPoseAsTransient(TaskElement task) {
            for (DayAssignment each : task.getDayAssignments()) {
                each.dontPoseAsTransientObjectAnymore();
            }
        }

        private void updateParentsPositions(TaskElement task) {
            TaskGroup current = task.getParent();
            while (current != null) {
                current.fitStartAndEndDatesToChildren();
                taskElementDAO.save(current);
                current = current.getParent();
            }
        }
    }

    private final String ADVANCED_ALLOCATION_VIEW = _("Advanced Allocation");
    private final Mode mode;
    private final IAdHocTransactionService adHocTransactionService;
    private final IOrderDAO orderDAO;
    private AdvancedAllocationController advancedAllocationController;
    private final IBack onBack;
    private final ITaskElementDAO taskElementDAO;

    private final IResourceDAO resourceDAO;
    private final Scenario currentScenario;
    private final Component breadcrumbs;
    private final IGlobalViewEntryPoints globalViewEntryPoints;

    public static ITab create(final Mode mode,
            IAdHocTransactionService adHocTransactionService,
            IOrderDAO orderDAO, ITaskElementDAO taskElementDAO,
            IResourceDAO resourceDAO, Scenario currentScenario, IBack onBack,
            Component breadcrumbs, IGlobalViewEntryPoints globalViewEntryPoints) {
        return new AdvancedAllocationTabCreator(mode, adHocTransactionService,
                orderDAO, taskElementDAO, resourceDAO, currentScenario, onBack,
                breadcrumbs, globalViewEntryPoints).build();
    }

    private AdvancedAllocationTabCreator(Mode mode,
            IAdHocTransactionService adHocTransactionService,
            IOrderDAO orderDAO, ITaskElementDAO taskElementDAO,
            IResourceDAO resourceDAO, Scenario currentScenario, IBack onBack,
            Component breadcrumbs, IGlobalViewEntryPoints globalViewEntryPoints) {
        Validate.notNull(mode);
        Validate.notNull(adHocTransactionService);
        Validate.notNull(orderDAO);
        Validate.notNull(resourceDAO);
        Validate.notNull(onBack);
        Validate.notNull(currentScenario);
        Validate.notNull(breadcrumbs);
        Validate.notNull(globalViewEntryPoints);
        this.adHocTransactionService = adHocTransactionService;
        this.orderDAO = orderDAO;
        this.mode = mode;
        this.onBack = onBack;
        this.taskElementDAO = taskElementDAO;
        this.resourceDAO = resourceDAO;
        this.currentScenario = currentScenario;
        this.breadcrumbs = breadcrumbs;
        this.globalViewEntryPoints = globalViewEntryPoints;
    }

    private ITab build() {
        IComponentCreator advanceAllocationComponentCreator = new IComponentCreator() {
            @Override
            public Component create(final Component parent) {
                return adHocTransactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Component>() {
                            @Override
                            public Component execute() {
                                return createComponent(parent);
                            }

                        });
            }

        };
        return new CreatedOnDemandTab(ADVANCED_ALLOCATION_VIEW,
                "advanced-allocation",
                advanceAllocationComponentCreator) {
            private boolean firstTime = true;

            @Override
            protected void afterShowAction() {
                breadcrumbs.getChildren().clear();
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(getSchedulingLabel()));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(_("Advanced Allocation")));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(mode.getOrder().getName()));

                if (firstTime) {
                    firstTime = false;
                    return;
                }
                adHocTransactionService
                        .runOnReadOnlyTransaction(new IOnTransaction<Void>() {

                            @Override
                            public Void execute() {
                                resetController();
                                return null;
                            }
                        });
            }
        };
    }

    private Component createComponent(final Component parent) {
        Order order = mode.getOrder();
        return Executions.createComponents("advance_allocation.zul", parent,
                argsWithController(order));
    }

    private Map<String, Object> argsWithController(Order order) {
        Map<String, Object> result = new HashMap<String, Object>();
        advancedAllocationController = new AdvancedAllocationController(onBack,
                createAllocationInputsFor(order));
        result
                .put("advancedAllocationController",
                        advancedAllocationController);
        return result;
    }

    private Order reload(Order order) {
        try {
            return orderDAO.find(order.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<AllocationInput> createAllocationInputsFor(Order order) {
        Order orderReloaded = reload(order);
        orderReloaded.useSchedulingDataFor(currentScenario);
        return createAllocationsWithOrderReloaded(orderReloaded);
    }

    private List<AllocationInput> createAllocationsWithOrderReloaded(
            Order orderReloaded) {
        List<AllocationInput> result = new ArrayList<AllocationInput>();
        for (TaskElement taskElement : orderReloaded.getTaskElements()) {
            addAllocations(orderReloaded, result, taskElement);
            if (taskElement instanceof Task) {
                Task t = (Task) taskElement;
                result.add(createAllocationInputFor(orderReloaded, t));
            }
        }
        return result;
    }

    private void addAllocations(Order order,
            List<AllocationInput> result, TaskElement taskElement) {
        if (taskElement instanceof Task
                && ((Task) taskElement).hasSomeSatisfiedAllocation()) {
            result.add(createAllocationInputFor(order, (Task) taskElement));
        }
        if (!taskElement.isLeaf()) {
            for (TaskElement each : taskElement.getChildren()) {
                addAllocations(order, result, each);
            }
        }
    }

    private AllocationInput createAllocationInputFor(Order order, Task task) {
        Scenario currentScenario = Registry.getScenarioManager().getCurrent();
        ResultReceiver resultReceiver = new ResultReceiver(currentScenario,
                order, task);
        return new AllocationInput(resultReceiver.getAggregate(), task,
                resultReceiver);
    }

    private void resetController() {
        Order order = mode.getOrder();
        advancedAllocationController.reset(onBack,
                createAllocationInputsFor(order));
    }

}
