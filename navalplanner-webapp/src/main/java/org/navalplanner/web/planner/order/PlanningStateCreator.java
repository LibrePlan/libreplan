/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.IAssignmentsOnResourceCalculator;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.daos.IOrderVersionDAO;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.web.calendars.BaseCalendarModel;
import org.navalplanner.web.planner.order.PlanningState.IScenarioInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.Desktop;

/**
 * It retrieves the PlaningState from a ZK {@link Desktop}. If it doesn't exist
 * yet, it creates and initializes a new PlanningState.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class PlanningStateCreator {

    private static final String ATTRIBUTE_NAME = PlanningState.class.getName();

    /**
     * When the scenario is not the owner, all the tasks are copied, creating
     * new assignments. But the previous assignments keep on being referenced by
     * the resource and must be discarded.
     */
    private static final class AvoidStaleAssignments implements
            IAssignmentsOnResourceCalculator {

        private Set<DayAssignment> previousAssignmentsSet;

        public AvoidStaleAssignments(List<DayAssignment> previousAssignments) {
            this.previousAssignmentsSet = new HashSet<DayAssignment>(
                    previousAssignments);
        }

        @Override
        public List<DayAssignment> getAssignments(Resource resource) {
            List<DayAssignment> result = new ArrayList<DayAssignment>();
            for (DayAssignment each : resource.getAssignments()) {
                if (!previousAssignmentsSet.contains(each)) {
                    result.add(each);
                }
            }
            return result;
        }

    }

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ITaskElementDAO taskDAO;

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    public PlanningState retrieveOrCreate(Desktop desktop, Order order) {
        Object existent = desktop.getAttribute(ATTRIBUTE_NAME);
        if (existent instanceof PlanningState) {
            return (PlanningState) existent;
        }
        PlanningState result = createInitialPlanning(reload(order));
        desktop.setAttribute(ATTRIBUTE_NAME, result);
        return result;
    }

    private Order reload(Order order) {
        Order result = orderDAO.findExistingEntity(order.getId());
        result.useSchedulingDataFor(scenarioManager.getCurrent());
        return result;
    }

    private PlanningState createInitialPlanning(Order orderReloaded) {
        Scenario currentScenario = scenarioManager.getCurrent();
        if (!orderReloaded.isSomeTaskElementScheduled()) {
            return PlanningState.createEmpty(currentScenario, orderReloaded);
        }
        final List<Resource> allResources = resourceDAO.list(Resource.class);
        criterionDAO.list(Criterion.class);
        TaskGroup taskElement = orderReloaded.getAssociatedTaskElement();
        forceLoadOfChildren(Arrays.asList(taskElement));
        forceLoadDayAssignments(orderReloaded.getResources());
        switchAllocationsToScenario(currentScenario, taskElement);

        PlanningState result = PlanningState.create(taskElement,
                orderReloaded.getAssociatedTasks(), allResources, criterionDAO,
                resourceDAO, buildScenarioInfo(orderReloaded));

        forceLoadOfDependenciesCollections(result.getInitial());
        forceLoadOfWorkingHours(result.getInitial());
        forceLoadOfLabels(result.getInitial());
        return result;
    }

    private void forceLoadDayAssignments(Set<Resource> resources) {
        for (Resource resource : resources) {
            resource.getAssignments().size();
        }
    }

    private void forceLoadOfChildren(Collection<? extends TaskElement> initial) {
        for (TaskElement each : initial) {
            forceLoadOfDataAssociatedTo(each);
            if (each instanceof TaskGroup) {
                findChildrenWithQueryToAvoidProxies((TaskGroup) each);
                List<TaskElement> children = each.getChildren();
                forceLoadOfChildren(children);
            }
        }
    }

    private static void forceLoadOfDataAssociatedTo(TaskElement each) {
        forceLoadOfResourceAllocationsResources(each);
        forceLoadOfCriterions(each);
        if (each.getCalendar() != null) {
            BaseCalendarModel.forceLoadBaseCalendar(each.getCalendar());
        }
        each.hasConsolidations();
    }

    /**
     * Forcing the load of all resources so the resources at planning state and
     * at allocations are the same
     */
    private static void forceLoadOfResourceAllocationsResources(
            TaskElement taskElement) {
        Set<ResourceAllocation<?>> resourceAllocations = taskElement
                .getAllResourceAllocations();
        for (ResourceAllocation<?> each : resourceAllocations) {
            each.getAssociatedResources();
            for (DerivedAllocation eachDerived : each.getDerivedAllocations()) {
                eachDerived.getResources();
            }
        }
    }

    /**
     * Forcing the load of all criterions so there are no different criterion
     * instances for the same criteiron at database
     */
    private static void forceLoadOfCriterions(TaskElement taskElement) {
        List<GenericResourceAllocation> generic = ResourceAllocation.getOfType(
                GenericResourceAllocation.class,
                taskElement.getSatisfiedResourceAllocations());
        for (GenericResourceAllocation each : generic) {
            for (Criterion eachCriterion : each.getCriterions()) {
                eachCriterion.getName();
            }
        }
    }

    private void findChildrenWithQueryToAvoidProxies(TaskGroup group) {
        for (TaskElement eachTask : taskDAO.findChildrenOf(group)) {
            Hibernate.initialize(eachTask);
        }
    }

    private IScenarioInfo buildScenarioInfo(Order orderReloaded) {
        Scenario currentScenario = scenarioManager.getCurrent();
        if (orderReloaded.isUsingTheOwnerScenario()) {
            return createOwnerScenarioInfoFor(orderReloaded, currentScenario);
        }
        final List<DayAssignment> previousAssignments = orderReloaded
                .getDayAssignments();
        OrderVersion previousVersion = currentScenario
                .getOrderVersion(orderReloaded);
        OrderVersion newVersion = OrderVersion
                .createInitialVersion(currentScenario);
        orderReloaded.writeSchedulingDataChangesTo(currentScenario, newVersion);
        switchAllocationsToScenario(currentScenario,
                orderReloaded.getAssociatedTaskElement());
        return createScenarioInfoForNotOwnerScenario(new AvoidStaleAssignments(
                previousAssignments), orderReloaded, previousVersion,
                newVersion, currentScenario);
    }

    private IScenarioInfo createOwnerScenarioInfoFor(Order orderReloaded, Scenario currentScenario) {
        return PlanningState
                .ownerScenarioInfo(orderVersionDAO, currentScenario,
                        currentScenario.getOrderVersion(orderReloaded));
    }

    private static void switchAllocationsToScenario(Scenario scenario,
            TaskElement task) {
        for (ResourceAllocation<?> each : task.getAllResourceAllocations()) {
            each.switchToScenario(scenario);
        }
    }

    private IScenarioInfo createScenarioInfoForNotOwnerScenario(
            IAssignmentsOnResourceCalculator returningNewAssignments,
            Order orderReloaded, OrderVersion previousVersion,
            OrderVersion newVersion, Scenario currentScenario) {
        return PlanningState.forNotOwnerScenario(returningNewAssignments,
                orderDAO, scenarioDAO, taskSourceDAO, orderReloaded,
                previousVersion, currentScenario, newVersion);
    }

    private void forceLoadOfDependenciesCollections(
            Collection<? extends TaskElement> elements) {
        for (TaskElement task : elements) {
            forceLoadOfDepedenciesCollections(task);
            if (!task.isLeaf()) {
                forceLoadOfDependenciesCollections(task.getChildren());
            }
        }
    }

    private void forceLoadOfDepedenciesCollections(TaskElement task) {
        task.getDependenciesWithThisOrigin().size();
        task.getDependenciesWithThisDestination().size();
    }

    private void forceLoadOfWorkingHours(List<TaskElement> initial) {
        for (TaskElement taskElement : initial) {
            if (taskElement.getTaskSource() != null) {
                taskElement.getTaskSource().getTotalHours();
                OrderElement orderElement = taskElement.getOrderElement();
                if (orderElement != null) {
                    orderElement.getWorkHours();
                }
                if (!taskElement.isLeaf()) {
                    forceLoadOfWorkingHours(taskElement.getChildren());
                }
            }
        }
    }

    private void forceLoadOfLabels(List<TaskElement> initial) {
        for (TaskElement taskElement : initial) {
            if (taskElement.isLeaf()) {
                OrderElement orderElement = taskElement.getOrderElement();
                if (orderElement != null) {
                    orderElement.getLabels().size();
                }
            } else {
                forceLoadOfLabels(taskElement.getChildren());
            }
        }
    }

}
