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

package org.navalplanner.web.common;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.Dependency.Type;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.scenarios.daos.IOrderVersionDAO;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.web.users.services.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.ganttz.data.DependencyType;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.GanttDiagramGraph;
import org.zkoss.ganttz.data.IDependency;
import org.zkoss.ganttz.data.GanttDiagramGraph.IAdapter;
import org.zkoss.ganttz.data.GanttDiagramGraph.PointType;
import org.zkoss.ganttz.data.constraint.Constraint;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.LongOperationFeedback.IBackGroundOperation;
import org.zkoss.ganttz.util.LongOperationFeedback.IDesktopUpdate;
import org.zkoss.ganttz.util.LongOperationFeedback.IDesktopUpdatesEmitter;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;

/**
 * Model to manage UI operations from main template.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TemplateModel implements ITemplateModel {

    public static class DependencyWithVisibility implements
            IDependency<TaskElement> {

        public static DependencyWithVisibility createInvisible(
                TaskElement source, TaskElement destination, DependencyType type) {
            return new DependencyWithVisibility(source, destination, type,
                    false);
        }

        public static DependencyWithVisibility existent(Dependency each) {
            return new DependencyWithVisibility(each.getOrigin(), each
                    .getDestination(), toGraphicalType(each.getType()), true);
        }

        public static List<Constraint<GanttDate>> getStartConstraintsGiven(
                TemplateModelAdapter adapter, Set<DependencyWithVisibility> withDependencies) {
            List<Constraint<GanttDate>> result = new ArrayList<Constraint<GanttDate>>();
            for (DependencyWithVisibility each : withDependencies) {
                TaskElement source = each.getSource();
                DependencyType type = each.getType();
                result.addAll(type.getStartConstraints(source, adapter));
            }
            return result;
        }

        public static List<Constraint<GanttDate>> getEndConstraintsGiven(
                TemplateModelAdapter adapter,
                Set<DependencyWithVisibility> withDependencies) {
            List<Constraint<GanttDate>> result = new ArrayList<Constraint<GanttDate>>();
            for (DependencyWithVisibility each : withDependencies) {
                TaskElement source = each.getSource();
                DependencyType type = each.getType();
                result.addAll(type.getEndConstraints(source, adapter));
            }
            return result;
        }

        private final TaskElement source;

        private final TaskElement destination;

        private final DependencyType type;

        private final boolean visible;

        private DependencyWithVisibility(TaskElement source,
                TaskElement destination, DependencyType type, boolean visible) {
            Validate.notNull(source);
            Validate.notNull(destination);
            Validate.notNull(type);
            this.source = source;
            this.destination = destination;
            this.type = type;
            this.visible = visible;
        }

        public boolean isVisible() {
            return visible;
        }

        public TaskElement getSource() {
            return source;
        }

        public TaskElement getDestination() {
            return destination;
        }

        public DependencyType getType() {
            return type;
        }

        private static DependencyType toGraphicalType(Type domainDependencyType) {
            switch (domainDependencyType) {
            case END_START:
                return DependencyType.END_START;
            case START_START:
                return DependencyType.START_START;
            case END_END:
                return DependencyType.END_END;
            case START_END:
                throw new RuntimeException(Dependency.Type.START_END
                        + " graphically it's not supported");
            default:
                throw new RuntimeException("can't handle "
                        + domainDependencyType);
            }
        }

        public PointType getPointType() {
            return getType().getPointModified();
        }

    }

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Scenario> getScenarios() {
        return scenarioDAO.getAll();
    }

    @Override
    @Transactional(readOnly = true)
    public String getCompanyLogoURL() {
        return configurationDAO.getConfiguration().getCompanyLogoURL();
    }

    @Override
    @Transactional(readOnly = true)
    public Scenario getScenarioByName(String name) {
        try {
            return scenarioDAO.findByName(name);
        } catch (InstanceNotFoundException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void setScenario(String loginName, Scenario scenario,
            IOnFinished onFinish) {
        Scenario scenarioReloaded = reloadScenario(scenario);
        associateToUser(scenarioReloaded, findUserByLoginName(loginName));
        doReassignations(scenarioReloaded, onFinish);
    }

    private Scenario reloadScenario(Scenario scenario) {
        return scenarioDAO.findExistingEntity(scenario
                .getId());
    }

    private User findUserByLoginName(String loginName) {
        try {
            return userDAO.findByLoginName(loginName);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void associateToUser(Scenario scenario, User user) {
        user.setLastConnectedScenario(scenario);
        userDAO.save(user);
        CustomUser customUser = (CustomUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        customUser.setScenario(scenario);
    }

    private void doReassignations(final Scenario scenario,
            IOnFinished onFinish) {
        if (isOnZKExecution()) {
            doReassignationsWithFeedback(getDesktop(), scenario, onFinish);
        } else {
            doReassignations(scenario, LongOperationFeedback
                    .<IDesktopUpdate> doNothingEmitter());
            onFinish.onWithoutErrorFinish();
        }
    }

    private boolean isOnZKExecution() {
        Execution current = Executions.getCurrent();
        return current != null && current.getDesktop() != null;
    }

    private Desktop getDesktop() {
        Execution current = Executions.getCurrent();
        return current.getDesktop();
    }

    private void doReassignationsWithFeedback(Desktop desktop,
            final Scenario scenario,
            final IOnFinished onFinish) {
        IBackGroundOperation<IDesktopUpdate> reassignations = new IBackGroundOperation<IDesktopUpdate>() {
            @Override
            public void doOperation(
                    final IDesktopUpdatesEmitter<IDesktopUpdate> desktopUpdateEmitter) {
                Exception exceptionHappened = null;
                try {
                    transactionService
                            .runOnTransaction(new IOnTransaction<Void>() {
                                @Override
                                public Void execute() {
                                    doReassignations(reloadScenario(scenario),
                                            desktopUpdateEmitter);
                                    return null;
                                }
                            });
                } catch (Exception e) {
                    exceptionHappened = e;
                } finally {
                    desktopUpdateEmitter.doUpdate(showEnd());
                }
                if (exceptionHappened == null) {
                    desktopUpdateEmitter.doUpdate(notifySuccess(onFinish));
                } else {
                    desktopUpdateEmitter.doUpdate(notifyException(onFinish,
                            exceptionHappened));
                }
            }

        };
        LongOperationFeedback.progressive(desktop, LongOperationFeedback
                .withAsyncUpates(reassignations));
    }

    private IDesktopUpdate notifySuccess(
            final IOnFinished onFinish) {
        return new IDesktopUpdate() {

            @Override
            public void doUpdate() {
                onFinish.onWithoutErrorFinish();
            }
        };
    }

    private IDesktopUpdate notifyException(final IOnFinished onFinish,
            final Exception exceptionHappened) {
        return new IDesktopUpdate() {

            @Override
            public void doUpdate() {
                onFinish.errorHappened(exceptionHappened);
            }
        };
    }

    private void doReassignations(Scenario scenario,
            IDesktopUpdatesEmitter<IDesktopUpdate> emitter) {
        List<Entry<Order, OrderVersion>> needingReassignation = scenario
                .getOrderVersionsNeedingReassignation();
        final int total = needingReassignation.size();
        if (!needingReassignation.isEmpty()) {
            emitter.doUpdate(showStart(total));
        }
        int i = 1;
        for (Entry<Order, OrderVersion> each : needingReassignation) {
            OrderVersion orderVersion = each.getValue();
            Order order = each.getKey();
            order.useSchedulingDataFor(scenario);
            if (order.isScheduled()) {
                doReassignationsOn(order, orderVersion.getOwnerScenario(),
                        scenario);
                orderVersion.savingThroughOwner();
                orderVersionDAO.save(orderVersion);
            }
            emitter.doUpdate(showProgress(total - i));
        }

    }

    private IDesktopUpdate showStart(final int ordersNumber) {
        return sendMessage(_("Reassigning {0} orders", ordersNumber));
    }

    private IDesktopUpdate showProgress(int remaining) {
        return sendMessage(_("{0} orders reassignation remaining", remaining));
    }

    private IDesktopUpdate sendMessage(final String message) {
        return new IDesktopUpdate() {
            @Override
            public void doUpdate() {
                Clients.showBusy(message, true);
            }
        };
    }

    private IDesktopUpdate showEnd() {
        return new IDesktopUpdate() {

            @Override
            public void doUpdate() {
                Clients.showBusy(null, false);
            }
        };
    }

    private void doReassignationsOn(Order order, Scenario from, Scenario to) {
        copyAssignments(order, from, to);
        installDependenciesEnforcer(order,
                TemplateModelAdapter.create(to, LocalDate.fromDateFields(order.getDeadline())));
        doReassignations(order, to);
        doTheSaving(order);
    }

    private void copyAssignments(Order order, Scenario from, Scenario to) {
        for (Task each : getTasksFrom(order)) {
            each.copyAssignmentsFromOneScenarioToAnother(from, to);
        }
    }

    private void installDependenciesEnforcer(Order order, TemplateModelAdapter adapter) {
        GanttDiagramGraph<TaskElement, DependencyWithVisibility> graph = createFor(order, adapter);
        TaskSource taskSource = order.getTaskSource();
        graph.addTopLevel(taskSource.getTask());
        for (Dependency each : getAllDependencies(order)) {
            graph.addWithoutEnforcingConstraints(DependencyWithVisibility
                    .existent(each));
        }
    }

    private GanttDiagramGraph<TaskElement, DependencyWithVisibility> createFor(
            Order order, IAdapter<TaskElement, DependencyWithVisibility> adapter) {
        List<Constraint<GanttDate>> startConstraints = PlannerConfiguration
                .getStartConstraintsGiven((GanttDate) GanttDate.createFrom(order.getInitDate()));
        List<Constraint<GanttDate>> endConstraints = PlannerConfiguration
                .getEndConstraintsGiven((GanttDate) GanttDate.createFrom(order.getDeadline()));
        GanttDiagramGraph<TaskElement, DependencyWithVisibility> result = GanttDiagramGraph
                .create(order.isScheduleBackwards(), adapter, startConstraints,
                        endConstraints,
                        order.getDependenciesConstraintsHavePriority());
        return result;
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

    private void doReassignations(Order order, Scenario scenario) {
        for (Task each : getTasksFrom(order)) {
            each.reassignAllocationsWithNewResources(scenario, resourceDAO);
        }
    }

    private void doTheSaving(Order order) {
        for (TaskSource each : order.getTaskSourcesFromBottomToTop()) {
            taskSourceDAO.save(each);
        }
    }

    private List<Task> getTasksFrom(Order order) {
        List<Task> result = new ArrayList<Task>();
        for (TaskElement each : getTaskElementsFrom(order)) {
            if (each instanceof Task) {
                result.add((Task) each);
            }
        }
        return result;
    }

    private List<TaskElement> getTaskElementsFrom(Order order) {
        List<TaskElement> result = new ArrayList<TaskElement>();
        for (TaskSource each : order.getTaskSourcesFromBottomToTop()) {
            result.add(each.getTask());
        }
        return result;
    }

}
