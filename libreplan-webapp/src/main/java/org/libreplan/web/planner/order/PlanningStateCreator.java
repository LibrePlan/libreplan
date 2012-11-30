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
package org.libreplan.web.planner.order;

import static org.libreplan.business.planner.entities.TaskElement.justTasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.Hibernate;
import org.joda.time.LocalDate;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.entities.IndirectAdvanceAssignment;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IEntitySequenceDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderStatusEnum;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.orders.entities.TaskSource.IOptionalPersistence;
import org.libreplan.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.AssignmentFunction;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.DayAssignment.FilterType;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.DerivedAllocation;
import org.libreplan.business.planner.entities.GenericResourceAllocation;
import org.libreplan.business.planner.entities.IMoneyCostCalculator;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation.IVisitor;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.StretchesFunction;
import org.libreplan.business.planner.entities.SubcontractorDeliverDate;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.TaskMilestone;
import org.libreplan.business.planner.entities.consolidations.CalculatedConsolidation;
import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.IAssignmentsOnResourceCalculator;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.daos.IOrderVersionDAO;
import org.libreplan.business.scenarios.daos.IScenarioDAO;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.users.daos.IOrderAuthorizationDAO;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.ProfileOrderAuthorization;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserOrderAuthorization;
import org.libreplan.web.UserUtil;
import org.libreplan.web.calendars.BaseCalendarModel;
import org.libreplan.web.planner.TaskElementAdapter;
import org.libreplan.web.planner.tabs.CriticalPathBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.adapters.IAdapterToTaskFundamentalProperties;
import org.zkoss.ganttz.adapters.IStructureNavigator;
import org.zkoss.ganttz.adapters.PlannerConfiguration;
import org.zkoss.zk.ui.Desktop;

/**
 * It retrieves the PlaningState from a ZK {@link Desktop}. If it doesn't exist
 * yet, it creates and initializes a new PlanningState.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
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
    private IAdHocTransactionService transactionService;

    @Autowired
    private IOrderVersionDAO orderVersionDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ITaskElementDAO taskDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private IEntitySequenceDAO entitySequenceDAO;

    @Autowired
    private TaskElementAdapter taskElementAdapterCreator;

    @Autowired
    private SaveCommandBuilder saveCommandBuilder;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    private IMoneyCostCalculator moneyCostCalculator;

    @Autowired
    private IResourcesSearcher resourcesSearcher;

    void synchronizeWithSchedule(Order order, IOptionalPersistence persistence) {
        List<TaskSourceSynchronization> synchronizationsNeeded = order
                .calculateSynchronizationsNeeded();
        for (TaskSourceSynchronization each : synchronizationsNeeded) {
            each.apply(persistence);
        }
    }

    public interface IActionsOnRetrieval {

        public void onRetrieval(PlanningState planningState);
    }

    public PlanningState createOn(Desktop desktop, Order order) {
        Validate.notNull(desktop);
        Validate.notNull(order);
        setupScenario(order);
        PlanningState result = createPlanning(order);
        desktop.setAttribute(ATTRIBUTE_NAME, result);
        return result;
    }

    void setupScenario(Order order) {
        if (!order.hasNoVersions()) {
            return;
        }
        Scenario currentScenario = scenarioManager.getCurrent();
        OrderVersion orderVersion = currentScenario.addOrder(order);
        order.setVersionForScenario(currentScenario, orderVersion);
        order.useSchedulingDataFor(currentScenario);
    }

    public PlanningState retrieveOrCreate(Desktop desktop, Order order) {
        return retrieveOrCreate(desktop, order, null);
    }

    public PlanningState retrieveOrCreate(Desktop desktop, Order order,
            IActionsOnRetrieval onRetrieval) {
        Object existent = null;
        if (desktop != null) {
            existent = desktop.getAttribute(ATTRIBUTE_NAME);
        }
        if (existent instanceof PlanningState) {
            PlanningState result = (PlanningState) existent;
            if (ObjectUtils.equals(order.getId(), result.getOrder().getId())) {
                result.onRetrieval();
                if (onRetrieval != null) {
                    onRetrieval.onRetrieval(result);
                }
                return result;
            }
        }
        PlanningState result = createPlanning(reload(order));
        result.onRetrieval();
        if (desktop != null) {
            desktop.setAttribute(ATTRIBUTE_NAME, result);
        }
        return result;
    }

    private Order reload(Order order) {
        ensureOrderVersionsAreNotProxies();
        Order result = orderDAO.findExistingEntity(order.getId());
        Scenario current = scenarioManager.getCurrent();
        result.useSchedulingDataFor(current);
        return result;
    }

    private void ensureOrderVersionsAreNotProxies() {
        List<Long> orderVersionIds = transactionService
                .runOnAnotherReadOnlyTransaction(new IOnTransaction<List<Long>>() {

                    @Override
                    public List<Long> execute() {
                        List<Long> result = new ArrayList<Long>();
                        Scenario scenario = scenarioManager.getCurrent();
                        for (OrderVersion each : scenario.getOrders().values()) {
                            result.add(each.getId());
                        }
                        return result;
                    }
                });
        for (Long each : orderVersionIds) {
            orderVersionDAO.findExistingEntity(each);
        }
    }

    private PlanningState createPlanning(Order orderReloaded) {
        Scenario currentScenario = scenarioManager.getCurrent();
        final List<Resource> allResources = resourceDAO.list(Resource.class);
        criterionDAO.list(Criterion.class);
        forceLoadOfOrderAssociatedData(orderReloaded);
        TaskGroup rootTask = orderReloaded.getAssociatedTaskElement();
        if (rootTask != null) {
            forceLoadOf(rootTask);
            forceLoadDayAssignments(orderReloaded
                    .getResources(FilterType.KEEP_ALL));
            forceLoadOfDepedenciesCollections(rootTask);
            forceLoadOfLabels(Arrays.asList((TaskElement) rootTask));
        }

        if (orderReloaded.getCalendar() != null) {
            BaseCalendarModel
                    .forceLoadBaseCalendar(orderReloaded.getCalendar());
        }

        PlanningState result = new PlanningState(orderReloaded, allResources,
                currentScenario);

        forceLoadOfWorkingHours(result.getInitial());

        moneyCostCalculator.resetMoneyCostMap();

        return result;
    }

    private void forceLoadOfOrderAssociatedData(Order order) {
        List<OrderElement> all = new ArrayList<OrderElement>();
        all.add(order);
        all.addAll(order.getAllChildren());
        for (OrderElement each : all) {
            for (DirectAdvanceAssignment direct : each.getDirectAdvanceAssignments()) {
                direct.getAdvanceMeasurements().size();
                direct.getAdvanceType().getHumanId();
                direct.getAdvanceType().getUnitName();
            }
            for (IndirectAdvanceAssignment indirect : each
                    .getIndirectAdvanceAssignments()) {
                indirect.getAdvanceType().getUnitName();
                Set<CalculatedConsolidation> consolidation = indirect
                        .getCalculatedConsolidation();
                for (CalculatedConsolidation c : consolidation) {
                    c.getCalculatedConsolidatedValues().size();
                }
            }
            for (HoursGroup hours : each.getHoursGroups()) {
                for (CriterionRequirement requirement : hours
                        .getCriterionRequirements()) {
                    requirement.ensureDataLoaded();
                }
                hours.getValidCriterions().size();
            }
        }
    }

    private void forceLoadDayAssignments(Set<Resource> resources) {
        for (Resource resource : resources) {
            resource.getAssignments().size();
        }
    }

    private void forceLoadOf(TaskElement taskElement) {
        forceLoadOfDataAssociatedTo(taskElement);
        if (taskElement instanceof TaskGroup) {
            findChildrenWithQueryToAvoidProxies((TaskGroup) taskElement);
            for (TaskElement each : taskElement.getChildren()) {
                forceLoadOf(each);
            }
        }
    }

    private void forceLoadOfDataAssociatedTo(TaskElement each) {
        forceLoadOfResourceAllocationsResourcesAndAssignmentFunction(each);
        forceLoadOfCriterions(each);
        forceLoadOfSubcontractedTaskData(each);

        BaseCalendar calendar = each.getOwnCalendar();
        if (calendar == null) {
            if (each.getOrderElement() != null) {
                calendar = orderDAO.loadOrderAvoidingProxyFor(
                        each.getOrderElement()).getCalendar();
            }
        }
        if (calendar != null) {
            BaseCalendarModel.forceLoadBaseCalendar(calendar);
        }
        each.hasConsolidations();
    }

    /**
     * Forcing the load of all resources so the resources at planning state and
     * at allocations are the same. It loads the assignment function too
     */
    private static void forceLoadOfResourceAllocationsResourcesAndAssignmentFunction(
            TaskElement taskElement) {
        Set<ResourceAllocation<?>> resourceAllocations = taskElement
                .getAllResourceAllocations();
        for (ResourceAllocation<?> each : resourceAllocations) {
            each.getAssociatedResources();
            for (DerivedAllocation eachDerived : each.getDerivedAllocations()) {
                forceLoadOfDerivedAllocation(eachDerived);
            }
            forceLoadOfAssignmentFunction(each);
        }
    }

    private static void forceLoadOfDerivedAllocation(
            DerivedAllocation derivedAllocation) {
        derivedAllocation.getResources();
        derivedAllocation.getConfigurationUnit().getWorkerAssignments().size();
    }

    private static void forceLoadOfAssignmentFunction(ResourceAllocation<?> each) {
        AssignmentFunction function = each.getAssignmentFunction();
        if ((function != null) && (function instanceof StretchesFunction)) {
            ((StretchesFunction) function).getStretches().size();
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

    private static void forceLoadOfSubcontractedTaskData(TaskElement taskElement){
        if(taskElement.isTask()){
            if(((Task)taskElement).getSubcontractedTaskData() != null){
                for (SubcontractorDeliverDate subDeliverDate : ((Task) taskElement)
                        .getSubcontractedTaskData()
                        .getRequiredDeliveringDates()) {
                    subDeliverDate.getSaveDate();
                }
            }
        }
    }

    private void findChildrenWithQueryToAvoidProxies(TaskGroup group) {
        for (TaskElement eachTask : taskDAO.findChildrenOf(group)) {
            Hibernate.initialize(eachTask);
            eachTask.getParent().getName();
        }
    }

    private IScenarioInfo buildScenarioInfo(Order orderReloaded) {
        Scenario currentScenario = scenarioManager.getCurrent();
        if (orderReloaded.isUsingTheOwnerScenario()) {
            switchAllocationsToScenario(currentScenario,
                    orderReloaded.getAssociatedTaskElement());
            return new UsingOwnerScenario(currentScenario, orderReloaded);
        }
        final List<DayAssignment> previousAssignments = orderReloaded
                .getDayAssignments(DayAssignment.FilterType.KEEP_ALL);

        OrderVersion newVersion = OrderVersion
                .createInitialVersion(currentScenario);

        orderReloaded.writeSchedulingDataChangesTo(currentScenario, newVersion);
        switchAllocationsToScenario(currentScenario,
                orderReloaded.getAssociatedTaskElement());

        return new UsingNotOwnerScenario(new AvoidStaleAssignments(
                previousAssignments), orderReloaded, currentScenario,
                newVersion);
    }

    private static void switchAllocationsToScenario(Scenario scenario,
            TaskElement task) {
        if (task == null) {
            return;
        }
        for (ResourceAllocation<?> each : task.getAllResourceAllocations()) {
            each.switchToScenario(scenario);
        }
    }

    private void forceLoadOfDepedenciesCollections(TaskElement task) {
        loadDependencies(task.getDependenciesWithThisOrigin());
        loadDependencies(task.getDependenciesWithThisDestination());
        for (TaskElement each : task.getChildren()) {
            forceLoadOfDepedenciesCollections(each);
        }
    }

    private void loadDependencies(Set<Dependency> dependenciesWithThisOrigin) {
        for (Dependency each : dependenciesWithThisOrigin) {
            each.getOrigin().getName();
            each.getDestination().getName();
        }
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
            OrderElement orderElement = taskElement.getOrderElement();
            if (orderElement != null) {
                Set<Label> labels = orderElement.getLabels();
                for (Label each : labels) {
                    each.getType().getName();
                }
            }
            forceLoadOfLabels(taskElement.getChildren());
        }
    }

    private static final class TaskElementNavigator implements
            IStructureNavigator<TaskElement> {

        @Override
        public List<TaskElement> getChildren(TaskElement object) {
            return object.getChildren();
        }

        @Override
        public boolean isLeaf(TaskElement object) {
            return object.isLeaf();
        }

        @Override
        public boolean isMilestone(TaskElement object) {
            if (object != null) {
                return object instanceof TaskMilestone;
            }
            return false;
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

    private class ChangeScenarioInfoOnSave implements IScenarioInfo {

        private IScenarioInfo current;
        private final Order order;

        public ChangeScenarioInfoOnSave(IScenarioInfo initial, Order order) {
            Validate.notNull(initial);
            Validate.notNull(order);
            this.current = initial;
            this.order = order;
        }

        public IAssignmentsOnResourceCalculator getAssignmentsCalculator() {
            return current.getAssignmentsCalculator();
        }

        public Scenario getCurrentScenario() {
            return current.getCurrentScenario();
        }

        public boolean isUsingTheOwnerScenario() {
            return current.isUsingTheOwnerScenario();
        }

        public void saveVersioningInfo() throws IllegalStateException {
            current.saveVersioningInfo();

        }
        public void afterCommit() {
            if (current instanceof ChangeScenarioInfoOnSave) {
                current = new UsingOwnerScenario(current.getCurrentScenario(),
                        order, current.getAssignmentsCalculator());
            }
        }

    }

    private class UsingOwnerScenario implements IScenarioInfo {

        private final Scenario currentScenario;
        private final Order order;
        private final IAssignmentsOnResourceCalculator calculator;

        public UsingOwnerScenario(Scenario currentScenario, Order order) {
            this(currentScenario, order, new Resource.AllResourceAssignments());
        }

        public UsingOwnerScenario(Scenario currentScenario, Order order, IAssignmentsOnResourceCalculator calculator) {
            Validate.notNull(currentScenario);
            Validate.notNull(order);
            this.currentScenario = currentScenario;
            this.order = order;
            this.calculator = calculator;
        }

        @Override
        public boolean isUsingTheOwnerScenario() {
            return true;
        }

        @Override
        public void saveVersioningInfo() {
            OrderVersion orderVersion = order.getCurrentVersionInfo()
                    .getOrderVersion();
            if (order.isNewObject()) {
                scenarioDAO.updateDerivedScenariosWithNewVersion(null, order,
                        currentScenario, orderVersion);
            }
            orderVersion.savingThroughOwner();
            synchronizeWithSchedule(order, getPersistence());
            order.writeSchedulingDataChanges();
        }

        IOptionalPersistence getPersistence() {
            if (order.isNewObject()) {
                return TaskSource
                        .persistButDontRemoveTaskSources(taskSourceDAO);
            } else {
                return TaskSource.persistTaskSources(taskSourceDAO);
            }
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
            return calculator;
        }
    }

    private class UsingNotOwnerScenario implements IScenarioInfo {

        private final Scenario currentScenario;
        private final OrderVersion newVersion;
        private final Order order;

        private final IAssignmentsOnResourceCalculator assigmentsOnResourceCalculator;

        public UsingNotOwnerScenario(
                IAssignmentsOnResourceCalculator assigmentsOnResourceCalculator,
                Order order, Scenario currentScenario,
                OrderVersion newVersion) {
            Validate.notNull(assigmentsOnResourceCalculator);
            Validate.notNull(order);
            Validate.notNull(currentScenario);
            Validate.notNull(newVersion);
            this.assigmentsOnResourceCalculator = assigmentsOnResourceCalculator;
            this.currentScenario = currentScenario;
            this.newVersion = newVersion;
            this.order = order;
        }

        @Override
        public boolean isUsingTheOwnerScenario() {
            return false;
        }

        @Override
        public void saveVersioningInfo() throws IllegalStateException {
            reattachAllTaskSources();
            createAndSaveNewOrderVersion(scenarioManager.getCurrent(),
                    newVersion);
            synchronizeWithSchedule(order,
                    TaskSource.persistButDontRemoveTaskSources(taskSourceDAO));
            order.writeSchedulingDataChanges();
        }

        private void createAndSaveNewOrderVersion(Scenario currentScenario,
                OrderVersion newOrderVersion) {
            OrderVersion previousOrderVersion = currentScenario
                    .getOrderVersion(order);
            currentScenario.setOrderVersion(order, newOrderVersion);
            scenarioDAO.updateDerivedScenariosWithNewVersion(
                    previousOrderVersion, order, currentScenario,
                    newOrderVersion);
        }

        private void reattachAllTaskSources() {
            // avoid LazyInitializationException for when doing
            // removePredecessorsDayAssignmentsFor
            for (TaskSource each : order
                    .getAllScenariosTaskSourcesFromBottomToTop()) {
                taskSourceDAO.reattach(each);
            }
        }

        @Override
        public void afterCommit() {
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

    public class PlanningState {

        private final Order order;

        private ArrayList<TaskElement> initial;

        private Set<TaskElement> toRemove = new HashSet<TaskElement>();

        private Set<Resource> resources = new HashSet<Resource>();

        private final IScenarioInfo scenarioInfo;

        private List<OrderAuthorization> orderAuthorizations;
        private List<OrderAuthorization> orderAuthorizationsAddition = new ArrayList<OrderAuthorization>();
        private List<OrderAuthorization> orderAuthorizationsRemoval = new ArrayList<OrderAuthorization>();

        private OrderStatusEnum savedOrderState;

        public PlanningState(Order order,
                Collection<? extends Resource> initialResources,
                Scenario currentScenario) {
            Validate.notNull(order);
            this.order = order;
            rebuildTasksState();
            this.scenarioInfo = new ChangeScenarioInfoOnSave(
                    buildScenarioInfo(order), order);
            this.resources = OrderPlanningModel
                    .loadRequiredDataFor(new HashSet<Resource>(initialResources));
            associateWithScenario(this.resources);
            this.orderAuthorizations = loadOrderAuthorizations();
            this.savedOrderState = order.getState();
        }

        private List<OrderAuthorization> loadOrderAuthorizations() {
            if (order.isNewObject()) {
                return new ArrayList<OrderAuthorization>();
            }
            List<OrderAuthorization> orderAuthorizations = orderAuthorizationDAO
                    .listByOrder(order);
            for (OrderAuthorization each : orderAuthorizations) {
                if (each instanceof UserOrderAuthorization) {
                    ((UserOrderAuthorization) each).getUser().getLoginName();
                }
                if (each instanceof ProfileOrderAuthorization) {
                    ((ProfileOrderAuthorization) each).getProfile()
                            .getProfileName();
                }

            }
            return orderAuthorizations;
        }

        void onRetrieval() {
            cachedConfiguration = null;
            cachedCommand = null;
            synchronizeScheduling();
            generateOrderElementCodes();
            rebuildTasksState();
        }

        void synchronizeScheduling() {
            synchronizeWithSchedule(order, TaskSource.dontPersist());
        }

        private void generateOrderElementCodes() {
            order.generateOrderElementCodes(entitySequenceDAO
                    .getNumberOfDigitsCode(EntityNameEnum.ORDER));
        }

        private void rebuildTasksState() {
            TaskGroup rootTask = getRootTask();
            if (rootTask == null) {
                this.initial = new ArrayList<TaskElement>();
            } else {
                this.initial = new ArrayList<TaskElement>(
                        rootTask.getChildren());
            }
        }

        private void associateWithScenario(
                Collection<? extends Resource> resources) {
            Scenario currentScenario = getCurrentScenario();
            for (Resource each : resources) {
                each.useScenario(currentScenario);
            }
        }

        public Order getOrder() {
            return order;
        }

        public boolean isEmpty() {
            return getRootTask() == null;
        }

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


        private PlannerConfiguration<TaskElement> cachedConfiguration;

        public PlannerConfiguration<TaskElement> getConfiguration() {
            if (cachedConfiguration != null) {
                return cachedConfiguration;
            }
            IAdapterToTaskFundamentalProperties<TaskElement> adapter;
            adapter = taskElementAdapterCreator.createForOrder(
                    getScenarioInfo().getCurrentScenario(), order, this);

            PlannerConfiguration<TaskElement> result = new PlannerConfiguration<TaskElement>(
                    adapter, new TaskElementNavigator(), getInitial());

            result.setNotBeforeThan(order.getInitDate());
            result.setNotAfterThan(order.getDeadline());
            result.setDependenciesConstraintsHavePriority(order
                    .getDependenciesConstraintsHavePriority());
            result.setScheduleBackwards(order.isScheduleBackwards());
            return cachedConfiguration = result;
        }

        private ISaveCommand cachedCommand;

        public ISaveCommand getSaveCommand() {
            if (cachedCommand != null) {
                return cachedCommand;
            }
            return cachedCommand = saveCommandBuilder.build(this,
                    getConfiguration());
        }

        public List<TaskElement> getInitial() {
            return new ArrayList<TaskElement>(initial);
        }

        public List<Task> getAllTasks() {
            List<Task> result = new ArrayList<Task>();
            if (getRootTask() != null) {
                findTasks(getRootTask(), result);
            }
            return result;
        }

        private void findTasks(TaskElement taskElement, List<Task> result) {
            if (taskElement instanceof Task) {
                Task t = (Task) taskElement;
                result.add(t);
            }
            for (TaskElement each : taskElement.getChildren()) {
                findTasks(each, result);
            }
        }

        public void reassociateResourcesWithSession() {
            User user = UserUtil.getUserFromSession();
            boolean isBoundUser = (user != null) && user.isBound();

            for (Resource resource : resources) {
                if (isBoundUser
                        && user.getWorker().getId().equals(resource.getId())) {
                    // Resource bound to current user is already associated with
                    // session
                    continue;
                }
                resourceDAO.reattach(resource);
            }
            // ensuring no repeated instances of criterions
            reattachCriterions(getExistentCriterions(resources));
            addingNewlyCreated(resourceDAO);
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

        private void reattachCriterions(Set<Criterion> criterions) {
            for (Criterion each : criterions) {
                criterionDAO.reattachUnmodifiedEntity(each);
            }
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

        public void removed(TaskElement taskElement) {
            taskElement.detach();
            if (!isTopLevel(taskElement)) {
                return;
            }
            toRemove.add(taskElement);
        }

        private boolean isTopLevel(TaskElement taskElement) {
            if (taskElement instanceof TaskMilestone) {
                return true;
            }
            return taskElement.getParent() == getRootTask();
        }

        public TaskGroup getRootTask() {
            return order.getAssociatedTaskElement();
        }

        public IScenarioInfo getScenarioInfo() {
            return scenarioInfo;
        }

        public Scenario getCurrentScenario() {
            return getScenarioInfo().getCurrentScenario();
        }

        public void reattach() {
            orderDAO.reattach(order);
            if (getRootTask() != null) {
                taskDAO.reattach(getRootTask());
            }
        }

        public void synchronizeTrees() {
            scenarioInfo.saveVersioningInfo();
        }

        public List<Resource> getResourcesRelatedWithAllocations() {
            Set<Resource> result = new HashSet<Resource>();
            for (Task each : justTasks(order
                    .getAllChildrenAssociatedTaskElements())) {
                result.addAll(resourcesRelatedWith(each));
            }
            return new ArrayList<Resource>(result);
        }

        private Set<Resource> resourcesRelatedWith(Task task) {
            Set<Resource> result = new HashSet<Resource>();
            for (ResourceAllocation<?> each : task
                    .getSatisfiedResourceAllocations()) {
                result.addAll(resourcesRelatedWith(each));
            }
            return result;
        }

        private <T> Collection<Resource> resourcesRelatedWith(
                ResourceAllocation<?> allocation) {
            return ResourceAllocation.visit(allocation,
                    new IVisitor<Collection<Resource>>() {

                        @Override
                        public Collection<Resource> on(
                                SpecificResourceAllocation specificAllocation) {
                            return Collections.singletonList(specificAllocation
                                    .getResource());
                        }

                        @Override
                        public Collection<Resource> on(
                                GenericResourceAllocation genericAllocation) {
                            return DayAssignment.byResource(
                                    genericAllocation.getAssignments())
                                    .keySet();
                        }
                    });
        }

        public List<ResourceAllocation<?>> replaceByCurrentOnes(
                Collection<? extends ResourceAllocation<?>> allocationsReturnedByQuery,
                IAllocationCriteria allocationCriteria) {
            Set<Long> orderElements = getIds(order.getAllChildren());
            List<ResourceAllocation<?>> result = allocationsNotInOrder(
                    allocationsReturnedByQuery, orderElements);
            result.addAll(allocationsInOrderSatisfyingCriteria(
                    order.getAllChildrenAssociatedTaskElements(),
                    allocationCriteria));
            return result;
        }

        private Set<Long> getIds(List<OrderElement> allChildren) {
            Set<Long> result = new HashSet<Long>();
            for (OrderElement each : allChildren) {
                result.add(each.getId());
            }
            return result;
        }

        private List<ResourceAllocation<?>> allocationsNotInOrder(
                Collection<? extends ResourceAllocation<?>> allocationsReturnedByQuery,
                Set<Long> orderElementsIds) {
            List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
            for (ResourceAllocation<?> each : allocationsReturnedByQuery) {
                Task task = each.getTask();
                if (!isIncluded(orderElementsIds, task)) {
                    result.add(each);
                }
            }
            return result;
        }

        private boolean isIncluded(Set<Long> orderElementsIds, Task task) {
            return orderElementsIds.contains(task.getOrderElement().getId());
        }

        private List<ResourceAllocation<?>> allocationsInOrderSatisfyingCriteria(
                Collection<? extends TaskElement> tasks,
                IAllocationCriteria allocationCriteria) {
            List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
            for (Task each : justTasks(tasks)) {
                result.addAll(satisfying(allocationCriteria,
                        each.getSatisfiedResourceAllocations()));
            }
            return result;
        }

        private List<ResourceAllocation<?>> satisfying(
                IAllocationCriteria criteria,
                Collection<ResourceAllocation<?>> allocations) {
            List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
            for (ResourceAllocation<?> each : allocations) {
                if (criteria.isSatisfiedBy(each)) {
                    result.add(each);
                }
            }
            return result;
        }

        public List<OrderAuthorization> getOrderAuthorizations() {
            return Collections.unmodifiableList(orderAuthorizations);
        }

        public List<OrderAuthorization> getOrderAuthorizationsAddition() {
            return Collections.unmodifiableList(orderAuthorizationsAddition);
        }

        public List<OrderAuthorization> getOrderAuthorizationsRemoval() {
            return Collections.unmodifiableList(orderAuthorizationsRemoval);
        }

        public void addOrderAuthorization(OrderAuthorization orderAuthorization) {
            orderAuthorizations.add(orderAuthorization);
            orderAuthorizationsAddition.add(orderAuthorization);
        }

        public void removeOrderAuthorization(
                OrderAuthorization orderAuthorization) {
            orderAuthorizations.remove(orderAuthorization);
            orderAuthorizationsAddition.remove(orderAuthorization);
            if (!orderAuthorization.isNewObject()) {
                orderAuthorizationsRemoval.add(orderAuthorization);
            }
        }

        public void cleanOrderAuthorizationsAdditionAndRemoval() {
            orderAuthorizationsAddition.clear();
            orderAuthorizationsRemoval.clear();
        }

        public OrderStatusEnum getSavedOrderState() {
            return savedOrderState;
        }

        public void updateSavedOrderState() {
            savedOrderState = order.getState();
        }

        public List<TaskElement> getCriticalPath() {
            return CriticalPathBuilder.criticalPathFor(this, resourcesSearcher);
        }

    }

    public interface IAllocationCriteria {

        boolean isSatisfiedBy(ResourceAllocation<?> resourceAllocation);

    }

    public static IAllocationCriteria and(final IAllocationCriteria... criteria) {
        return new IAllocationCriteria() {

            @Override
            public boolean isSatisfiedBy(
                    ResourceAllocation<?> resourceAllocation) {
                for (IAllocationCriteria each : criteria) {
                    if (!each.isSatisfiedBy(resourceAllocation)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static class TaskOnInterval implements IAllocationCriteria {

        private final LocalDate startInclusive;

        private final LocalDate endInclusive;

        public TaskOnInterval(LocalDate startInclusive, LocalDate endInclusive) {
            this.startInclusive = startInclusive;
            this.endInclusive = endInclusive;
        }

        @Override
        public boolean isSatisfiedBy(ResourceAllocation<?> resourceAllocation) {
            if (startInclusive != null
                    && resourceAllocation.getEndDate()
                            .compareTo(startInclusive) < 0) {
                return false;
            }
            if (endInclusive != null
                    && resourceAllocation.getStartDate()
                            .compareTo(endInclusive) > 0) {
                return false;
            }
            return true;
        }
    }

    public static class RelatedWith implements
            IAllocationCriteria {

        private final Criterion criterion;

        public RelatedWith(Criterion criterion) {
            this.criterion = criterion;
        }

        @Override
        public boolean isSatisfiedBy(ResourceAllocation<?> resourceAllocation) {
            if (resourceAllocation instanceof GenericResourceAllocation) {
                GenericResourceAllocation g = (GenericResourceAllocation) resourceAllocation;
                Set<Criterion> allocationCriterions = g.getCriterions();
                return someCriterionIn(allocationCriterions);
            }
            return false;
        }

        private boolean someCriterionIn(
                Collection<? extends Criterion> allocationCriterions) {
            for (Criterion each : allocationCriterions) {
                if (criterion.equals(each)) {
                    return true;
                }
            }
            return false;
        }

    }

    public static class SpecificRelatedWithCriterionOnInterval implements
            IAllocationCriteria {

        private final LocalDate startInclusive;

        private final LocalDate endExclusive;

        private final Criterion criterion;

        public SpecificRelatedWithCriterionOnInterval(
                Criterion criterion, LocalDate startInclusive,
                LocalDate endInclusive) {
            Validate.notNull(criterion);
            this.startInclusive = startInclusive;
            this.endExclusive = endInclusive != null ? endInclusive.plusDays(1)
                    : null;
            this.criterion = criterion;
        }

        @Override
        public boolean isSatisfiedBy(ResourceAllocation<?> resourceAllocation) {
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                SpecificResourceAllocation s = (SpecificResourceAllocation) resourceAllocation;
                return s.interferesWith(criterion, startInclusive, endExclusive);
            }
            return false;
        }

    }

    public static class RelatedWithResource implements IAllocationCriteria {
        private final Resource resource;

        public RelatedWithResource(Resource resource) {
            Validate.notNull(resource);
            this.resource = resource;
        }

        @Override
        public boolean isSatisfiedBy(ResourceAllocation<?> resourceAllocation) {
            return ResourceAllocation.visit(resourceAllocation,
                    new IVisitor<Boolean>() {

                        @Override
                        public Boolean on(
                                SpecificResourceAllocation specificAllocation) {
                            return specificAllocation.getResource().equals(
                                    resource);
                        }

                        @Override
                        public Boolean on(
                                GenericResourceAllocation genericAllocation) {
                            return DayAssignment.byResource(
                                    genericAllocation.getAssignments())
                                    .containsKey(resource);
                        }
                    });
        }

    }

}
