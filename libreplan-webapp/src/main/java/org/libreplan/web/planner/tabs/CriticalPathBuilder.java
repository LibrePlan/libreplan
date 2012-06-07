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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.Registry;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.web.common.TemplateModel.DependencyWithVisibility;
import org.libreplan.web.common.TemplateModelAdapter;
import org.libreplan.web.planner.order.PlanningStateCreator;
import org.libreplan.web.planner.order.PlanningStateCreator.IActionsOnRetrieval;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.GanttDiagramGraph.IAdapter;
import org.zkoss.ganttz.data.constraint.Constraint;
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
                .runOnTransaction(new IOnTransaction<List<TaskElement>>() {
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

        CriticalPathCalculator<TaskElement, DependencyWithVisibility> criticalPathCalculator = CriticalPathCalculator
                .create(order.getDependenciesConstraintsHavePriority());
        IAdapter<TaskElement, DependencyWithVisibility> adapter = TemplateModelAdapter
                .create(currentScenario, asLocalDate(order.getInitDate()),
                        asLocalDate(order.getDeadline()), resourcesSearcher);
        GanttDiagramGraph<TaskElement, DependencyWithVisibility> graph = createFor(
                order, adapter);
        graph.addTask(order.getAssociatedTaskElement());
        addDependencies(graph, order);
        return criticalPathCalculator.calculateCriticalPath(graph);
    }

    private LocalDate asLocalDate(Date date) {
        return date != null ? LocalDate.fromDateFields(date) : null;
    }

    private void addDependencies(
            GanttDiagramGraph<TaskElement, DependencyWithVisibility> graph,
            Order order) {
        for (Dependency each : getAllDependencies(order)) {
            graph.addWithoutEnforcingConstraints(DependencyWithVisibility
                    .existent(each));
        }
    }

    private Set<Dependency> getAllDependencies(Order order) {
        Set<Dependency> dependencies = new HashSet<Dependency>();
        for (TaskElement each : getTaskElementsFrom(order)) {
            Set<Dependency> dependenciesWithThisOrigin = each
                    .getDependenciesWithThisOrigin();
            dependencies.addAll(dependenciesWithThisOrigin);
        }
        return dependencies;
    }

    private List<TaskElement> getTaskElementsFrom(Order order) {
        List<TaskElement> result = new ArrayList<TaskElement>();
        for (TaskSource each : order.getTaskSourcesFromBottomToTop()) {
            result.add(each.getTask());
        }
        return result;
    }

    private GanttDiagramGraph<TaskElement, DependencyWithVisibility> createFor(
            Order order, IAdapter<TaskElement, DependencyWithVisibility> adapter) {
        GanttDate orderStart = GanttDate.createFrom(order.getInitDate());
        List<Constraint<GanttDate>> startConstraints = PlannerConfiguration
                .getStartConstraintsGiven(orderStart);
        GanttDate deadline = GanttDate.createFrom(order.getDeadline());
        List<Constraint<GanttDate>> endConstraints = PlannerConfiguration
                .getEndConstraintsGiven(deadline);
        GanttDiagramGraph<TaskElement, DependencyWithVisibility> result = GanttDiagramGraph
                .create(order.isScheduleBackwards(), adapter, startConstraints,
                        endConstraints,
                        order.getDependenciesConstraintsHavePriority());
        return result;
    }

}