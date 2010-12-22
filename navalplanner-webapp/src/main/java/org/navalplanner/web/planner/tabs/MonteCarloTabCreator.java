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
package org.navalplanner.web.planner.tabs;

import static org.navalplanner.web.I18nHelper._;
import static org.navalplanner.web.planner.tabs.MultipleTabsPlannerController.BREADCRUMBS_SEPARATOR;
import static org.navalplanner.web.planner.tabs.MultipleTabsPlannerController.PLANNIFICATION;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.web.common.TemplateModel.DependencyWithVisibility;
import org.navalplanner.web.common.TemplateModelAdapter;
import org.navalplanner.web.montecarlo.MonteCarloController;
import org.navalplanner.web.planner.order.OrderPlanningController;
import org.navalplanner.web.planner.tabs.CreatedOnDemandTab.IComponentCreator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.GanttDiagramGraph.IAdapter;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.data.criticalpath.CriticalPathCalculator;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class MonteCarloTabCreator {

    private static String ORDER_LIMITING_RESOURCES_VIEW = _("MonteCarlo Method");

    public static ITab create(Mode mode,
            MonteCarloController monteCarloController,
            OrderPlanningController orderPlanningController,
            Component breadcrumbs) {

        return new MonteCarloTabCreator(mode, monteCarloController,
                orderPlanningController, breadcrumbs).build();
    }

    private final Mode mode;

    private final MonteCarloController monteCarloController;

    private final OrderPlanningController orderPlanningController;

    private final Component breadcrumbs;

    private MonteCarloTabCreator(Mode mode,
            MonteCarloController MonteCarloController,
            OrderPlanningController orderPlanningController,
            Component breadcrumbs) {
        this.mode = mode;
        this.monteCarloController = MonteCarloController;
        this.orderPlanningController = orderPlanningController;
        this.breadcrumbs = breadcrumbs;
    }

    private ITab build() {
        return TabOnModeType.forMode(mode)
                .forType(ModeType.GLOBAL, createGlobalMonteCarloTab())
                .forType(ModeType.ORDER, createOrderMonteCarloTab())
                .create();
    }

    private ITab createOrderMonteCarloTab() {

        IComponentCreator componentCreator = new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {

                Map<String, Object> arguments = new HashMap<String, Object>();
                arguments.put("monteCarloController",
                        monteCarloController);
                return Executions.createComponents(
                        "/montecarlo/_montecarlo.zul", parent, arguments);
            }

        };

        return new CreatedOnDemandTab(ORDER_LIMITING_RESOURCES_VIEW,
                "order-limiting-resources", componentCreator) {

            @Override
            protected void afterShowAction() {
                List<TaskElement> criticalPath = orderPlanningController.getCriticalPath();
                if (criticalPath == null) {
                    criticalPath = getCriticalPathFor(mode.getOrder());
                }
                monteCarloController.setCriticalPath(criticalPath);

                breadcrumbs.getChildren().clear();
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(PLANNIFICATION));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs
                        .appendChild(new Label(ORDER_LIMITING_RESOURCES_VIEW));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(mode.getOrder().getName()));
            }
        };
    }

    public List<TaskElement> getCriticalPathFor(final Order order) {

        IAdHocTransactionService transactionService = Registry.getTransactionService();

        return transactionService.runOnTransaction(new IOnTransaction<List<TaskElement>>() {
            @Override
            public List<TaskElement> execute() {
                initializeOrder(order);
                return getCriticalPathFor(order);
            }

            private void initializeOrder(Order order) {
                IOrderDAO orderDAO = Registry.getOrderDAO();
                orderDAO.reattach(order);
                useSchedulingDataFor(order);
                for (TaskElement each: order.getTaskElements()) {
                    each.getName();
                }
                order.getTaskSource().getTask().getName();
            }

            private void useSchedulingDataFor(Order order) {
                IScenarioManager scenarioManager = Registry.getScenarioManager();
                order.useSchedulingDataFor(scenarioManager.getCurrent());
            }

            /**
             * Calculate critical path tasks in order
             *
             * @param order
             * @return
             */
            public List<TaskElement> getCriticalPathFor(Order order) {
                CriticalPathCalculator<TaskElement, DependencyWithVisibility> criticalPathCalculator = CriticalPathCalculator
                        .create();
                IAdapter<TaskElement, DependencyWithVisibility> adapter = TemplateModelAdapter
                                .create(getCurrentScenario(),
                                        asLocalDate(order.getInitDate()),
                                        asLocalDate(order.getDeadline()));
                GanttDiagramGraph<TaskElement, DependencyWithVisibility> graph = createFor(
                        order, adapter);
                graph.addTasks(order.getAllChildrenAssociatedTaskElements());
                addDependencies(graph, order);
                return criticalPathCalculator.calculateCriticalPath(graph);
            }

            private LocalDate asLocalDate(Date date) {
                return date != null ? LocalDate.fromDateFields(date)
                        : null;
            }

            private Scenario getCurrentScenario() {
                IScenarioManager scenarioManager = Registry.getScenarioManager();
                return scenarioManager.getCurrent();
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
                GanttDiagramGraph<TaskElement, DependencyWithVisibility> result = GanttDiagramGraph.create(
                        order.isScheduleBackwards(), adapter,
                        startConstraints, endConstraints,
                        order.getDependenciesConstraintsHavePriority());
                return result;
            }

        });
    }

    private ITab createGlobalMonteCarloTab() {

        final IComponentCreator componentCreator = new IComponentCreator() {

            @Override
            public org.zkoss.zk.ui.Component create(
                    org.zkoss.zk.ui.Component parent) {
                // do nothing
                return null;
            }

        };
        return new CreatedOnDemandTab(_("MonteCarlo Method"),
                "montecarlo-simulation", componentCreator) {
            @Override
            protected void afterShowAction() {
                // do nothing
            }
        };
    }

}
