/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011-2012 Igalia, S.L.
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

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.planner.entities.AggregateOfExpensesLines;
import org.libreplan.business.planner.entities.AggregateOfResourceAllocations;
import org.libreplan.business.planner.entities.CalculatedValue;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.web.common.ViewSwitcher;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.IAdvanceAllocationResultReceiver;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.Restriction;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.Restriction.IRestrictionSource;
import org.libreplan.web.planner.allocation.AllocationResult;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Messagebox;

/**
 * Controller for advanced allocation of a {@link Task}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@org.springframework.stereotype.Component("advancedAllocationTaskController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AdvancedAllocationTaskController extends GenericForwardComposer {

    private Task task;

    private IContextWithPlannerTask<TaskElement> context;

    private PlanningState planningState;

    private ViewSwitcher switcher;

    public void showAdvancedAllocation(Task task,
            IContextWithPlannerTask<TaskElement> context,
            PlanningState planningState) {
        this.task = task;
        this.context = context;
        this.planningState = planningState;

        AllocationResult allocationResult = AllocationResult.createCurrent(
                planningState.getCurrentScenario(), task);

        if (allocationResult.getAggregate().isEmpty()) {
            try {
                Messagebox.show(_("Some allocations needed"), _("Warning"),
                        Messagebox.OK, Messagebox.EXCLAMATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        AggregateOfExpensesLines aggregateExpenses = planningState
                .createAggregateExpenses(task);
        getSwitcher().goToAdvancedAllocation(allocationResult,
                createResultReceiver(allocationResult), aggregateExpenses,
                planningState);
    }


    public ViewSwitcher getSwitcher() {
        return switcher;
    }

    public void setSwitcher(ViewSwitcher switcher) {
        this.switcher = switcher;
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
            Validate.isTrue(!allocation.getAggregate().isEmpty());
            this.allocation = allocation;
            final EffortDuration totalEffort = allocation.getAggregate()
                    .getTotalEffort();
            final IntraDayDate start = allocation.getIntraDayStart();
            final IntraDayDate end = allocation.getIntraDayEnd();
            final CalculatedValue calculatedValue = allocation
                    .getCalculatedValue();
            restrictionSource = new IRestrictionSource() {

                @Override
                public EffortDuration getTotalEffort() {
                    return totalEffort;
                }

                @Override
                public LocalDate getStart() {
                    return start.getDate();
                }

                @Override
                public LocalDate getEnd() {
                    return end.asExclusiveEnd();
                }

                @Override
                public CalculatedValue getCalculatedValue() {
                    return calculatedValue;
                }
            };
        }

        @Override
        public void cancel() {
            // Do nothing
        }

        @Override
        public void accepted(AggregateOfResourceAllocations aggregate) {
            allocation.applyTo(planningState.getCurrentScenario(), task);
            if (task.isManualAnyAllocation()) {
                Task.convertOnStartInFixedDate(task);
            }
            askForReloads();
        }

        @Override
        public Restriction createRestriction() {
            return Restriction.build(restrictionSource);
        }
    }


    private void askForReloads() {
        if (context != null) {
            context.reloadCharts();
        }
    }

}
