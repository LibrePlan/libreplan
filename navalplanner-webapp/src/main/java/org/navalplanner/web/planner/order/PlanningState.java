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

package org.navalplanner.web.planner.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.planner.entities.TaskMilestone;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.daos.IOrderVersionDAO;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.zkoss.ganttz.Planner;

public abstract class PlanningState {

    public static IScenarioInfo ownerScenarioInfo(
            IOrderVersionDAO orderVersionDAO, Scenario scenario,
            OrderVersion currentVersionForScenario) {
        return new UsingOwnerScenario(orderVersionDAO, scenario,
                currentVersionForScenario);
    }

    public static IScenarioInfo forNotOwnerScenario(IOrderDAO orderDAO,
            IScenarioDAO scenarioDAO, ITaskSourceDAO taskSourceDAO,
            Order order, OrderVersion previousVersion,
            Scenario currentScenario, OrderVersion newVersion) {
        return new UsingNotOwnerScenario(orderDAO, scenarioDAO, taskSourceDAO,
                order, previousVersion,
                currentScenario, newVersion);
    }

    public interface IScenarioInfo {

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
        public void saveVersioningInfo()
                throws IllegalStateException {
        }

    }

    private static class UsingOwnerScenario implements IScenarioInfo {

        private final Scenario currentScenario;
        private final OrderVersion currentVersionForScenario;
        private final IOrderVersionDAO orderVersionDAO;

        public UsingOwnerScenario(IOrderVersionDAO orderVersionDAO,
                Scenario currentScenario,
                OrderVersion currentVersionForScenario) {
            Validate.notNull(orderVersionDAO);
            Validate.notNull(currentScenario);
            Validate.notNull(currentVersionForScenario);
            this.orderVersionDAO = orderVersionDAO;
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
    }

    private static class UsingNotOwnerScenario implements IScenarioInfo {

        private final IOrderDAO orderDAO;
        private final IScenarioDAO scenarioDAO;
        private final ITaskSourceDAO taskSourceDAO;

        private final OrderVersion previousVersion;
        private final Scenario currentScenario;
        private final OrderVersion newVersion;
        private final Order order;
        private boolean versionSaved = false;

        public UsingNotOwnerScenario(IOrderDAO orderDAO,
                IScenarioDAO scenarioDAO, ITaskSourceDAO taskSourceDAO,
                Order order, OrderVersion previousVersion,
                Scenario currentScenario,
                OrderVersion newVersion) {
            Validate.notNull(order);
            Validate.notNull(previousVersion);
            Validate.notNull(currentScenario);
            Validate.notNull(newVersion);
            Validate.notNull(orderDAO);
            Validate.notNull(scenarioDAO);
            Validate.notNull(taskSourceDAO);
            this.orderDAO = orderDAO;
            this.scenarioDAO = scenarioDAO;
            this.taskSourceDAO = taskSourceDAO;
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
    }

    public static PlanningState create(Planner planner, TaskGroup rootTask,
            Collection<? extends TaskElement> initialState,
            Collection<? extends Resource> initialResources,
            ICriterionDAO criterionDAO, IResourceDAO resourceDAO,
            IScenarioInfo scenarioInfo) {
        return new WithDataPlanningState(planner, rootTask, initialState,
                initialResources, criterionDAO, resourceDAO, scenarioInfo);
    }

    public static PlanningState createEmpty(Scenario currentScenario) {
        return new EmptyPlannigState(currentScenario);
    }

    public abstract boolean isEmpty();

    public abstract Collection<? extends TaskElement> getTasksToSave();

    public abstract List<TaskElement> getInitial();

    public abstract void reassociateResourcesWithSession();

    public abstract Collection<? extends TaskElement> getToRemove();

    public abstract void removed(TaskElement taskElement);

    public abstract void added(TaskElement taskElement);

    public abstract TaskGroup getRootTask();

    public abstract IScenarioInfo getScenarioInfo();

    public abstract Planner getPlanner();

    public Scenario getCurrentScenario() {
        return getScenarioInfo().getCurrentScenario();
    }

    static class WithDataPlanningState extends PlanningState {

        private final ArrayList<TaskElement> initial;

        private final Set<TaskElement> toSave;

        private final Set<TaskElement> toRemove;

        private Set<Resource> resources = new HashSet<Resource>();

        private final TaskGroup rootTask;

        private final ICriterionDAO criterionDAO;

        private final IResourceDAO resourceDAO;

        private final IScenarioInfo scenarioInfo;

        private final Planner planner;

        private WithDataPlanningState(Planner planner, TaskGroup rootTask,
                Collection<? extends TaskElement> initialState,
                Collection<? extends Resource> initialResources,
                ICriterionDAO criterionDAO, IResourceDAO resourceDAO,
                IScenarioInfo scenarioInfo) {
            this.planner = planner;
            this.rootTask = rootTask;
            this.criterionDAO = criterionDAO;
            this.resourceDAO = resourceDAO;
            this.scenarioInfo = scenarioInfo;
            this.initial = new ArrayList<TaskElement>(initialState);
            this.toSave = new HashSet<TaskElement>(initialState);
            this.toRemove = new HashSet<TaskElement>();
            this.resources = OrderPlanningModel
                    .loadRequiredDataFor(new HashSet<Resource>(
                    initialResources));
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
            Set<Resource> result = new HashSet<Resource>(resourceDAO
                    .list(Resource.class));
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
        public Planner getPlanner() {
            return planner;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

    }

    private static class EmptyPlannigState extends PlanningState {

        private final Scenario currentScenario;

        private EmptyPlannigState(Scenario currentScenario) {
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
        public Planner getPlanner() {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

    }
}