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
package org.libreplan.web.planner.tabs;

import static org.libreplan.web.I18nHelper._;
import static org.libreplan.web.planner.tabs.MultipleTabsPlannerController.BREADCRUMBS_SEPARATOR;
import static org.libreplan.web.planner.tabs.MultipleTabsPlannerController.getSchedulingLabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.AggregateOfResourceAllocations;
import org.libreplan.business.planner.entities.CalculatedValue;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.web.planner.allocation.AdvancedAllocationController;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.AllocationInput;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.IAdvanceAllocationResultReceiver;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.IBack;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.Restriction;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.Restriction.IRestrictionSource;
import org.libreplan.web.planner.allocation.AllocationResult;
import org.libreplan.web.planner.order.PlanningStateCreator;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.libreplan.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
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
        private final PlanningState planningState;

        public ResultReceiver(PlanningState planningState,
                Task task) {
            this.planningState = planningState;
            this.calculatedValue = task.getCalculatedValue();
            this.allocationResult = AllocationResult.createCurrent(
                    planningState.getCurrentScenario(), task);
            this.aggregate = this.allocationResult.getAggregate();
            this.task = task;
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
                        LOG.info("the aggregate for task " + task.getName()
                                + " is empty");
                        return task.getStartAsLocalDate();
                    }
                    return aggregate.getStart().getDate();
                }

                @Override
                public LocalDate getEnd() {
                    if (aggregate.isEmpty()) {
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
            allocationResult.applyTo(planningState.getCurrentScenario(), task);
            if (task.isManualAnyAllocation()) {
                Task.convertOnStartInFixedDate(task);
            }
            updateParentsPositions(task);
        }

        private void updateParentsPositions(TaskElement task) {
            TaskGroup current = task.getParent();
            while (current != null) {
                current.fitStartAndEndDatesToChildren();
                current = current.getParent();
            }
        }

        public AggregateOfResourceAllocations getAggregate() {
            return aggregate;
        }
    }

    private final String ADVANCED_ALLOCATION_VIEW = _("Advanced Allocation");
    private final Mode mode;
    private final IAdHocTransactionService adHocTransactionService;
    private AdvancedAllocationController advancedAllocationController;
    private final IBack onBack;
    private final PlanningStateCreator planningStateCreator;
    private final Component breadcrumbs;

    public static ITab create(final Mode mode,
            IAdHocTransactionService adHocTransactionService,
            PlanningStateCreator planningStateCreator, IBack onBack,
            Component breadcrumbs) {
        return new AdvancedAllocationTabCreator(mode, adHocTransactionService,
                planningStateCreator, onBack, breadcrumbs).build();
    }

    private AdvancedAllocationTabCreator(Mode mode,
            IAdHocTransactionService adHocTransactionService,
            PlanningStateCreator planningStateCreator, IBack onBack,
            Component breadcrumbs) {
        Validate.notNull(mode);
        Validate.notNull(adHocTransactionService);
        Validate.notNull(onBack);
        Validate.notNull(planningStateCreator);
        Validate.notNull(breadcrumbs);
        this.adHocTransactionService = adHocTransactionService;
        this.mode = mode;
        this.onBack = onBack;
        this.planningStateCreator = planningStateCreator;
        this.breadcrumbs = breadcrumbs;
    }

    private class AdvanceAssignmentCreator implements IComponentCreator {

        private PlanningState planningState;

        @Override
        public Component create(final Component parent) {
            return adHocTransactionService
                    .runOnReadOnlyTransaction(new IOnTransaction<Component>() {
                        @Override
                        public Component execute() {
                            planningState = createPlanningState(parent,
                                    mode.getOrder());
                            return createComponent(parent, planningState);
                        }

                        private PlanningState createPlanningState(
                                final Component parent, Order order) {
                            return planningStateCreator.retrieveOrCreate(
                                    parent.getDesktop(), order);
                        }

                    });
        }

        public PlanningState getState() {
            if (planningState == null) {
                throw new IllegalStateException(
                        "the planningState has not been created yet");
            }
            return planningState;
        }

    }

    private ITab build() {
        final AdvanceAssignmentCreator advanceAllocationComponentCreator = new AdvanceAssignmentCreator();

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
                                resetController(advanceAllocationComponentCreator
                                        .getState());
                                return null;
                            }
                        });
            }
        };
    }

    private Component createComponent(final Component parent,
            PlanningState planningState) {
        return Executions.createComponents("advance_allocation.zul", parent,
                argsWithController(planningState));
    }


    private Map<String, Object> argsWithController(PlanningState planningState) {
        Map<String, Object> result = new HashMap<String, Object>();
        advancedAllocationController = new AdvancedAllocationController(onBack,
                createAllocationInputsFor(planningState));
        result.put("advancedAllocationController", advancedAllocationController);
        return result;
    }

    private List<AllocationInput> createAllocationInputsFor(
            PlanningState planningState) {
        planningState.reattach();
        planningState.reassociateResourcesWithSession();
        return createAllocationsWithOrderReloaded(planningState);
    }

    private List<AllocationInput> createAllocationsWithOrderReloaded(
            PlanningState planningState) {
        List<Task> allTasks = planningState.getAllTasks();
        List<AllocationInput> result = new ArrayList<AllocationInput>();
        for (Task each : allTasks) {
            if (each.hasSomeSatisfiedAllocation()) {
                result.add(createAllocationInputFor(planningState, each));
            }
        }
        return result;
    }

    private AllocationInput createAllocationInputFor(
            PlanningState planningState, Task task) {
        ResultReceiver resultReceiver = new ResultReceiver(planningState, task);
        return new AllocationInput(resultReceiver.getAggregate(), task,
                resultReceiver);
    }

    private void resetController(PlanningState planningState) {
        advancedAllocationController.reset(onBack,
                createAllocationInputsFor(planningState));
    }

}
