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

package org.libreplan.web.planner.tabs;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.Registry;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.web.common.TemplateModel.DependencyWithVisibility;
import org.libreplan.web.common.TemplateModelAdapter;
import org.libreplan.web.planner.order.PlanningStateCreator;
import org.libreplan.web.planner.order.PlanningStateCreator.IActionsOnRetrieval;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.GanttDiagramGraph.IAdapter;
import org.zkoss.ganttz.data.criticalpath.CriticalPathCalculator;
import org.zkoss.zk.ui.Desktop;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Calculate critical path tasks in an order.
 *
 *         To calculate the tasks that are in the critical path is necessary to
 *         create an empty graph filled with the tasks and dependencies of this
 *         order
 */
public class CriticalPathBuilder {

    private PlanningStateCreator planningStateCreator;

    private IResourcesSearcher resourcesSearcher;

    public static CriticalPathBuilder create(
            PlanningStateCreator planningStateCreator,
            IResourcesSearcher resourcesSearcher) {
        return new CriticalPathBuilder(planningStateCreator, resourcesSearcher);
    }

    CriticalPathBuilder(PlanningStateCreator planningStateCreator,
            IResourcesSearcher resourcesSearcher) {
        this.planningStateCreator = planningStateCreator;
        this.resourcesSearcher = resourcesSearcher;
    }

    List<TaskElement> getCriticalPath(final Order order, final Desktop desktop) {
        IAdHocTransactionService transactionService = Registry
                .getTransactionService();
        return transactionService
                .runOnReadOnlyTransaction(new IOnTransaction<List<TaskElement>>() {
                    @Override
                    public List<TaskElement> execute() {
                        PlanningState state = retrieveOrCreate();
                        return criticalPathFor(state, resourcesSearcher);
                    }

                    private PlanningState retrieveOrCreate() {
                        return planningStateCreator.retrieveOrCreate(desktop,
                                order, new IActionsOnRetrieval() {

                                    @Override
                                    public void onRetrieval(
                                            PlanningState planningState) {
                                        planningState.reattach();
                                    }
                                });
                    }
                });
    }

    private List<TaskElement> criticalPathFor(PlanningState state,
            IResourcesSearcher resourcesSearcher) {
        final Order order = state.getOrder();
        final Scenario currentScenario = state.getCurrentScenario();

        IAdapter<TaskElement, DependencyWithVisibility> adapter = TemplateModelAdapter
                .create(currentScenario, asLocalDate(order.getInitDate()),
                        asLocalDate(order.getDeadline()), resourcesSearcher);
        GanttDiagramGraph<TaskElement, DependencyWithVisibility> graph = GanttDiagramBuilder
                .createForcingDependencies(order, adapter);
        CriticalPathCalculator<TaskElement, DependencyWithVisibility> criticalPathCalculator = CriticalPathCalculator
                .create(order.getDependenciesConstraintsHavePriority());
        return criticalPathCalculator.calculateCriticalPath(graph);
    }

    private LocalDate asLocalDate(Date date) {
        return date != null ? LocalDate.fromDateFields(date) : null;
    }

}