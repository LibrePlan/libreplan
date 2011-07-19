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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.Hibernate;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.IAssignmentsOnResourceCalculator;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.daos.IOrderVersionDAO;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.web.calendars.BaseCalendarModel;
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
            return new EmptyPlannigState(currentScenario, orderReloaded);
        }
        final List<Resource> allResources = resourceDAO.list(Resource.class);
        criterionDAO.list(Criterion.class);
        TaskGroup taskElement = orderReloaded.getAssociatedTaskElement();
        forceLoadOfChildren(Arrays.asList(taskElement));
        forceLoadDayAssignments(orderReloaded.getResources());
        switchAllocationsToScenario(currentScenario, taskElement);

        PlanningState result = new WithDataPlanningState(taskElement,
                orderReloaded.getAssociatedTasks(), allResources,
                buildScenarioInfo(orderReloaded));

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
            return new UsingOwnerScenario(currentScenario, currentScenario.getOrderVersion(orderReloaded));
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

        return new UsingNotOwnerScenario(new AvoidStaleAssignments(
                previousAssignments), orderReloaded, previousVersion,
                currentScenario, newVersion);
    }

    private static void switchAllocationsToScenario(Scenario scenario,
            TaskElement task) {
        for (ResourceAllocation<?> each : task.getAllResourceAllocations()) {
            each.switchToScenario(scenario);
        }
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

    public interface IScenarioInfo {

        public IAssignmentsOnResourceCalculator getAssignmentsCalculator();

        public Scenario getCurrentScenario();

        public boolean isUsingTheOwnerScenario();

        /**
         * @throws IllegalStateException
         *             if it's using the owner scenario
         */
        public void saveVersioningInfo() throws IllegalStateException;

        public void afterCommit();
    }

    private static class EmptySchedulingScenarioInfo implements IScenarioInfo {

        private final Scenario currentScenario;

        public EmptySchedulingScenarioInfo(Scenario currentScenario) {
            this.currentScenario = currentScenario;
        }

        @Override
        public void afterCommit() {
        }

        @Override
        public Scenario getCurrentScenario() {
            return currentScenario;
        }

        @Override
        public boolean isUsingTheOwnerScenario() {
            return true;
        }

        @Override
        public void saveVersioningInfo() throws IllegalStateException {
        }

        @Override
        public IAssignmentsOnResourceCalculator getAssignmentsCalculator() {
            return new Resource.AllResourceAssignments();
        }

    }

    private class UsingOwnerScenario implements IScenarioInfo {

        private final Scenario currentScenario;
        private final OrderVersion currentVersionForScenario;

        public UsingOwnerScenario(Scenario currentScenario,
                OrderVersion currentVersionForScenario) {
            Validate.notNull(currentScenario);
            Validate.notNull(currentVersionForScenario);
            this.currentScenario = currentScenario;
            this.currentVersionForScenario = currentVersionForScenario;
        }

        @Override
        public boolean isUsingTheOwnerScenario() {
            return true;
        }

        @Override
        public void saveVersioningInfo() throws IllegalStateException {
            currentVersionForScenario.savingThroughOwner();
            orderVersionDAO.save(currentVersionForScenario);
        }

        @Override
        public void afterCommit() {
            // do nothing
        }

        @Override
        public Scenario getCurrentScenario() {
            return currentScenario;
        }

        @Override
        public IAssignmentsOnResourceCalculator getAssignmentsCalculator() {
            return new Resource.AllResourceAssignments();
        }
    }

    private class UsingNotOwnerScenario implements IScenarioInfo {

        private final OrderVersion previousVersion;
        private final Scenario currentScenario;
        private final OrderVersion newVersion;
        private final Order order;
        private boolean versionSaved = false;

        private final IAssignmentsOnResourceCalculator assigmentsOnResourceCalculator;

        public UsingNotOwnerScenario(
                IAssignmentsOnResourceCalculator assigmentsOnResourceCalculator,
                Order order,
                OrderVersion previousVersion, Scenario currentScenario,
                OrderVersion newVersion) {
            Validate.notNull(assigmentsOnResourceCalculator);
            Validate.notNull(order);
            Validate.notNull(previousVersion);
            Validate.notNull(currentScenario);
            Validate.notNull(newVersion);
            this.assigmentsOnResourceCalculator = assigmentsOnResourceCalculator;
            this.previousVersion = previousVersion;
            this.currentScenario = currentScenario;
            this.newVersion = newVersion;
            this.order = order;
        }

        @Override
        public boolean isUsingTheOwnerScenario() {
            return versionSaved;
        }

        @Override
        public void saveVersioningInfo() throws IllegalStateException {
            if (versionSaved) {
                return;
            }
            orderDAO.save(order);
            TaskSource taskSource = order.getTaskSource();
            taskSourceDAO.save(taskSource);
            taskSource.dontPoseAsTransientObjectAnymore();
            taskSource.getTask().dontPoseAsTransientObjectAnymore();
            scenarioDAO.updateDerivedScenariosWithNewVersion(previousVersion,
                    order, currentScenario, newVersion);
        }

        @Override
        public void afterCommit() {
            versionSaved = true;
        }

        @Override
        public Scenario getCurrentScenario() {
            return currentScenario;
        }

        @Override
        public IAssignmentsOnResourceCalculator getAssignmentsCalculator() {
            return assigmentsOnResourceCalculator;
        }
    }

    public abstract class PlanningState {

        private final Order order;

        public PlanningState(Order order) {
            Validate.notNull(order);
            this.order = order;
        }

        public Order getOrder() {
            return order;
        }

        public abstract boolean isEmpty();

        /**
         * <p>
         * When the scenario was not owner, the previous {@link DayAssignment
         * day assingments} for the scenario must be avoided. Since the previous
         * scenario was not an owner, all tasks and related information are
         * copied, but the resource keeps pointing to the scenario's previous
         * assignments.
         * </p>
         * <p>
         * If the scenario is the owner, the assignments are returned directly.
         * </p>
         * @return the {@link IAssignmentsOnResourceCalculator} to use.
         * @see IAssignmentsOnResourceCalculator
         * @see AvoidStaleAssignments
         */
        public IAssignmentsOnResourceCalculator getAssignmentsCalculator() {
            return getScenarioInfo().getAssignmentsCalculator();
        }

        public abstract Collection<? extends TaskElement> getTasksToSave();

        public abstract List<TaskElement> getInitial();

        public abstract void reassociateResourcesWithSession();

        public abstract Collection<? extends TaskElement> getToRemove();

        public abstract void removed(TaskElement taskElement);

        public abstract void added(TaskElement taskElement);

        public abstract TaskGroup getRootTask();

        public abstract IScenarioInfo getScenarioInfo();

        public Scenario getCurrentScenario() {
            return getScenarioInfo().getCurrentScenario();
        }

    }

    private class EmptyPlannigState extends PlanningState {

        private final Scenario currentScenario;

        private EmptyPlannigState(Scenario currentScenario, Order order) {
            super(order);
            this.currentScenario = currentScenario;
        }

        @Override
        public void added(TaskElement taskElement) {
        }

        @Override
        public List<TaskElement> getInitial() {
            return Collections.emptyList();
        }

        @Override
        public TaskGroup getRootTask() {
            return null;
        }

        @Override
        public Collection<? extends TaskElement> getTasksToSave() {
            return Collections.emptyList();
        }

        @Override
        public Collection<? extends TaskElement> getToRemove() {
            return Collections.emptyList();
        }

        @Override
        public void reassociateResourcesWithSession() {
        }

        public void removed(TaskElement taskElement) {
        }

        @Override
        public IScenarioInfo getScenarioInfo() {
            return new EmptySchedulingScenarioInfo(currentScenario);
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

    }

    private class WithDataPlanningState extends PlanningState {

        private final ArrayList<TaskElement> initial;

        private final Set<TaskElement> toSave;

        private final Set<TaskElement> toRemove;

        private Set<Resource> resources = new HashSet<Resource>();

        private final TaskGroup rootTask;

        private final IScenarioInfo scenarioInfo;

        private WithDataPlanningState(TaskGroup rootTask,
                Collection<? extends TaskElement> initialState,
                Collection<? extends Resource> initialResources,
                IScenarioInfo scenarioInfo) {
            super((Order) rootTask.getOrderElement());
            this.rootTask = rootTask;
            this.scenarioInfo = scenarioInfo;
            this.initial = new ArrayList<TaskElement>(initialState);
            this.toSave = new HashSet<TaskElement>(initialState);
            this.toRemove = new HashSet<TaskElement>();
            this.resources = OrderPlanningModel
                    .loadRequiredDataFor(new HashSet<Resource>(initialResources));
            associateWithScenario(this.resources);
        }

        private void associateWithScenario(
                Collection<? extends Resource> resources) {
            Scenario currentScenario = getCurrentScenario();
            for (Resource each : resources) {
                each.useScenario(currentScenario);

            }
        }

        @Override
        public Collection<? extends TaskElement> getTasksToSave() {
            return Collections.unmodifiableCollection(toSave);
        }

        @Override
        public List<TaskElement> getInitial() {
            return new ArrayList<TaskElement>(initial);
        }

        @Override
        public void reassociateResourcesWithSession() {
            for (Resource resource : resources) {
                resourceDAO.reattach(resource);
            }
            // ensuring no repeated instances of criterions
            reattachCriterions(getExistentCriterions(resources));
            addingNewlyCreated(resourceDAO);
        }

        private void reattachCriterions(Set<Criterion> criterions) {
            for (Criterion each : criterions) {
                criterionDAO.reattachUnmodifiedEntity(each);
            }
        }

        private Set<Criterion> getExistentCriterions(Set<Resource> resources) {
            Set<Criterion> result = new HashSet<Criterion>();
            for (Resource resource : resources) {
                for (CriterionSatisfaction each : resource
                        .getCriterionSatisfactions()) {
                    result.add(each.getCriterion());
                }
            }
            return result;
        }

        private void addingNewlyCreated(IResourceDAO resourceDAO) {
            Set<Resource> newResources = getNewResources(resourceDAO);
            OrderPlanningModel.loadRequiredDataFor(newResources);
            associateWithScenario(newResources);
            resources.addAll(newResources);
        }

        private Set<Resource> getNewResources(IResourceDAO resourceDAO) {
            Set<Resource> result = new HashSet<Resource>(
                    resourceDAO.list(Resource.class));
            result.removeAll(resources);
            return result;
        }

        @Override
        public Collection<? extends TaskElement> getToRemove() {
            return Collections
                    .unmodifiableCollection(onlyNotTransient(toRemove));
        }

        private List<TaskElement> onlyNotTransient(
                Collection<? extends TaskElement> toRemove) {
            ArrayList<TaskElement> result = new ArrayList<TaskElement>();
            for (TaskElement taskElement : toRemove) {
                if (taskElement.getId() != null) {
                    result.add(taskElement);
                }
            }
            return result;
        }

        @Override
        public void removed(TaskElement taskElement) {
            taskElement.detach();
            if (!isTopLevel(taskElement)) {
                return;
            }
            toSave.remove(taskElement);
            toRemove.add(taskElement);
        }

        private boolean isTopLevel(TaskElement taskElement) {
            if (taskElement instanceof TaskMilestone) {
                return true;
            }
            return taskElement.getParent() == null;
        }

        @Override
        public void added(TaskElement taskElement) {
            if (!isTopLevel(taskElement)) {
                return;
            }
            toRemove.remove(taskElement);
            toSave.add(taskElement);
        }

        @Override
        public TaskGroup getRootTask() {
            return rootTask;
        }

        @Override
        public IScenarioInfo getScenarioInfo() {
            return scenarioInfo;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

    }

}
