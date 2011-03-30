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
import static org.navalplanner.web.planner.tabs.MultipleTabsPlannerController.getSchedulingLabel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.Hibernate;
import org.jfree.util.Log;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarAvailability;
import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.CalendarException;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.daos.IResourceDAO;
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

    private String ORDER_LIMITING_RESOURCES_VIEW = _("MonteCarlo Method");

    public static ITab create(Mode mode,
            MonteCarloController monteCarloController,
            OrderPlanningController orderPlanningController,
            Component breadcrumbs, IResourceDAO resourceDAO) {

        return new MonteCarloTabCreator(mode, monteCarloController,
                orderPlanningController, breadcrumbs, resourceDAO).build();
    }

    private final Mode mode;

    private final MonteCarloController monteCarloController;

    private final OrderPlanningController orderPlanningController;

    private final Component breadcrumbs;

    private final IResourceDAO resourceDAO;

    private MonteCarloTabCreator(Mode mode,
            MonteCarloController MonteCarloController,
            OrderPlanningController orderPlanningController,
            Component breadcrumbs, IResourceDAO resourceDAO) {
        Validate.notNull(resourceDAO);
        this.mode = mode;
        this.monteCarloController = MonteCarloController;
        this.orderPlanningController = orderPlanningController;
        this.breadcrumbs = breadcrumbs;
        this.resourceDAO = resourceDAO;
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
                    criticalPath = getCriticalPathFor(mode.getOrder().getId());
                }
                monteCarloController.setCriticalPath(criticalPath);

                breadcrumbs.getChildren().clear();
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(getSchedulingLabel()));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs
                        .appendChild(new Label(ORDER_LIMITING_RESOURCES_VIEW));
                breadcrumbs.appendChild(new Image(BREADCRUMBS_SEPARATOR));
                breadcrumbs.appendChild(new Label(mode.getOrder().getName()));
            }

        };
    }

    public List<TaskElement> getCriticalPathFor(final Long orderId) {

        IAdHocTransactionService transactionService = Registry.getTransactionService();

        return transactionService
                .runOnTransaction(new IOnTransaction<List<TaskElement>>() {

            /**
             * Retrieve Order from DB and initialize all data that will
             * be needed for creating MonteCarloTasks
             *
             * monteCarloController.setCriticalPath(criticalPath) won't
             * open a new Session for initializing proxy objects, that's
             * why is needed to initialize the proxies at this point
             *
             * @param orderId
             * @return
             */
            @Override
            public List<TaskElement> execute() {
                Order order = retrieveFromDB(orderId);
                initializeOrder(order);
                return getCriticalPathFor(order);
            }

            private Order retrieveFromDB(Long orderId) {
                try {
                    IOrderDAO orderDAO = Registry.getOrderDAO();
                    return orderDAO.find(orderId);
                } catch (InstanceNotFoundException e) {
                    Log.error(e.getMessage());
                }
                return null;
            }

            private void initializeOrder(Order order) {
                useSchedulingDataFor(order);
                initializeTasksInOrder(order);
            }

            private void useSchedulingDataFor(Order order) {
                order.useSchedulingDataFor(getCurrentScenario());
            }

            private Scenario getCurrentScenario() {
                IScenarioManager scenarioManager = Registry.getScenarioManager();
                return scenarioManager.getCurrent();
            }

            private void initializeTasksInOrder(Order root) {
                initializeTask(root);
                for (OrderElement each: root.getAllChildren()) {
                    Hibernate.initialize(each);
                    initializeTask(each);
                }
            }

            private void initializeTask(OrderElement orderElement) {
                TaskElement task = orderElement.getAssociatedTaskElement();
                if (task != null) {
                    ITaskElementDAO taskDAO = Registry.getTaskElementDAO();
                    taskDAO.reattach(task);
                    initializeCalendarFor(task);
                    initializeDependenciesFor(task);
                }
            }

            private void initializeCalendarFor(TaskElement task) {
                BaseCalendar calendar = task.getCalendar();
                Hibernate.initialize(calendar);
                initializeCalendarAvailabilities(calendar.getCalendarAvailabilities());
                initializeCalendarExceptions(calendar.getExceptions());
                initializeCalendarData(calendar.getCalendarDataVersions());
            }

            private void initializeCalendarAvailabilities(
                    List<CalendarAvailability> calendarAvailabilities) {
                for (CalendarAvailability each: calendarAvailabilities) {
                    Hibernate.initialize(each);
                }
            }

            private void initializeCalendarData(
                    List<CalendarData> calendarDataVersions) {
                for (CalendarData each: calendarDataVersions) {
                    Hibernate.initialize(each);
                    Hibernate.initialize(each.getHoursPerDay());
                }
            }

            private void initializeCalendarExceptions(
                    Set<CalendarException> exceptions) {
                for (CalendarException each: exceptions) {
                    Hibernate.initialize(each);
                }
            }

            private void initializeDependenciesFor(TaskElement task) {
                Set<Dependency> dependencies = task
                        .getDependenciesWithThisDestination();
                Hibernate.initialize(dependencies);
                for (Dependency each : dependencies) {
                    Hibernate.initialize(each.getOrigin());
                    Hibernate.initialize(each.getDestination());
                }
            }

            /**
             * Calculate critical path tasks in order
             *
             * To calculate the tasks that are in the critical path is
             * necesary to create an empy graph filled with the tasks
             * and dependencies of this order
             *
             * @param order
             * @return
             */
            public List<TaskElement> getCriticalPathFor(Order order) {
                CriticalPathCalculator<TaskElement, DependencyWithVisibility> criticalPathCalculator = CriticalPathCalculator
                        .create(order
                                .getDependenciesConstraintsHavePriority());
                IAdapter<TaskElement, DependencyWithVisibility> adapter = TemplateModelAdapter
                                .create(getCurrentScenario(),
                                        asLocalDate(order.getInitDate()),
                                        asLocalDate(order.getDeadline()),
                                        resourceDAO);
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
