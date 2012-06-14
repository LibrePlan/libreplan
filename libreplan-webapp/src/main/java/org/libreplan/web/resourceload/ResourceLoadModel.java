/*
 * This file is part of LibrePlan
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

package org.libreplan.web.resourceload;

import static org.libreplan.business.planner.entities.TaskElement.justTasks;
import static org.libreplan.web.I18nHelper._;
import static org.libreplan.web.planner.order.PlanningStateCreator.and;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.joda.time.LocalDate;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.daos.IResourceAllocationDAO;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.GenericResourceAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.users.daos.IOrderAuthorizationDAO;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.OrderAuthorizationType;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.calendars.BaseCalendarModel;
import org.libreplan.web.planner.order.PlanningStateCreator.IAllocationCriteria;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.libreplan.web.planner.order.PlanningStateCreator.RelatedWith;
import org.libreplan.web.planner.order.PlanningStateCreator.RelatedWithResource;
import org.libreplan.web.planner.order.PlanningStateCreator.SpecificRelatedWithCriterionOnInterval;
import org.libreplan.web.planner.order.PlanningStateCreator.TaskOnInterval;
import org.libreplan.web.resourceload.ResourceLoadParameters.IReattacher;
import org.libreplan.web.resourceload.ResourceLoadParameters.Paginator;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.data.resourceload.LoadPeriod;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.data.resourceload.TimeLineRole;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceLoadModel implements IResourceLoadModel {

    @Autowired
    private IResourceDAO resourcesDAO;

    @Autowired
    private IResourcesSearcher resourcesSearchModel;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Override
    @Transactional(readOnly = true)
    public ResourceLoadDisplayData calculateDataToDisplay(
            ResourceLoadParameters parameters) {

        PlanningState planningState = parameters.getPlanningState();
        if (planningState != null) {
            planningState.reattach();
            planningState.reassociateResourcesWithSession();
        }
        ResourceAllocationsFinder<?> allocationsFinder = create(parameters);
        List<LoadTimeLine> loadTimeLines = allocationsFinder.buildTimeLines();
        return new ResourceLoadDisplayData(loadTimeLines,
                parameters.getInitDateFilter(),
                parameters.getEndDateFilter(), allocationsFinder.getPaginator(),
                allocationsFinder.lazilyGetResourcesIncluded(),
                allocationsFinder.lazilyGetAssignmentsShown());
    }


    @Override
    @Transactional(readOnly = true)
    public Order getOrderByTask(TaskElement task) {
        Order result = orderDAO.loadOrderAvoidingProxyFor(task
                .getOrderElement());
        result.useSchedulingDataFor(scenarioManager.getCurrent());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userCanRead(Order order, String loginName) {
        if (SecurityUtils.isUserInRole(UserRole.ROLE_READ_ALL_PROJECTS)
                || SecurityUtils.isUserInRole(UserRole.ROLE_EDIT_ALL_PROJECTS)) {
            return true;
        }
        try {
            User user = userDAO.findByLoginName(loginName);
            for (OrderAuthorization authorization : orderAuthorizationDAO
                    .listByOrderUserAndItsProfiles(order, user)) {
                if (authorization.getAuthorizationType() == OrderAuthorizationType.READ_AUTHORIZATION
                        || authorization.getAuthorizationType() == OrderAuthorizationType.WRITE_AUTHORIZATION) {
                    return true;
                }
            }
        } catch (InstanceNotFoundException e) {
            // this case shouldn't happen, because it would mean that there
            // isn't a logged user
            // anyway, if it happenned we don't allow the user to pass
        }
        return false;
    }

    public ResourceAllocationsFinder<?> create(ResourceLoadParameters parameters) {
        return parameters.isFilterByResources() ? new ByResourceFinder(
                parameters) : new ByCriterionFinder(parameters);
    }

    private abstract class ResourceAllocationsFinder<T extends BaseEntity> {

        protected final ResourceLoadParameters parameters;

        private ResourceAllocationsFinder(ResourceLoadParameters parameters) {
            this.parameters = parameters;
        }

        public Callable<List<Resource>> lazilyGetResourcesIncluded() {
            return new Callable<List<Resource>>() {

                @Override
                public List<Resource> call() throws Exception {
                    return reattach(getResourcesIncluded());
                }

                private List<Resource> reattach(List<Resource> resources) {
                    for (Resource resource : resources) {
                        ResourceCalendar calendar = resource.getCalendar();
                        BaseCalendarModel.forceLoadBaseCalendar(calendar);
                        resource.getAssignments().size();
                    }
                    return resources;
                }

            };
        }

        abstract List<Resource> getResourcesIncluded();

        public Callable<List<DayAssignment>> lazilyGetAssignmentsShown() {
            return new Callable<List<DayAssignment>>() {

                @Override
                public List<DayAssignment> call() throws Exception {
                    return getAssignmentsShown();
                }

            };
        }

        private List<DayAssignment> getAssignmentsShown() {
            Set<DayAssignment> result = new HashSet<DayAssignment>();
            Map<T, List<ResourceAllocation<?>>> foundAllocations = getFoundAllocations();
            for (Entry<T, List<ResourceAllocation<?>>> each : foundAllocations
                    .entrySet()) {
                for (ResourceAllocation<?> eachAllocation : each.getValue()) {
                    result.addAll(eachAllocation.getAssignments());
                }
            }
            return new ArrayList<DayAssignment>(result);
        }

        abstract List<LoadTimeLine> buildTimeLines();

        abstract Map<T, List<ResourceAllocation<?>>> getFoundAllocations();

        abstract Paginator<T> getPaginator();

        Scenario getCurrentScenario() {
            PlanningState state = parameters.getPlanningState();
            if (state != null) {
                return state.getCurrentScenario();
            }
            return scenarioManager.getCurrent();
        }

        Collection<? extends ResourceAllocation<?>> doReplacementsIfNeeded(
                Collection<? extends ResourceAllocation<?>> allocations,
                IAllocationCriteria criteria) {
            if (parameters.getPlanningState() == null) {
                return allocations;
            }
            return parameters.getPlanningState().replaceByCurrentOnes(
                    allocations, criteria);
        }

        protected IAllocationCriteria onInterval() {
            return new TaskOnInterval(parameters.getInitDateFilter(),
                    parameters.getEndDateFilter());
        }

    }

    private class ByResourceFinder extends ResourceAllocationsFinder<Resource> {

        private final Map<Resource, List<ResourceAllocation<?>>> allocationsByResource;
        private Paginator<Resource> resources;

        public ByResourceFinder(ResourceLoadParameters parameters) {
            super(parameters);
            this.resources = resourcesToShow();
            this.allocationsByResource = eachWithAllocations(this.resources
                    .getForCurrentPage());
        }

        @Override
        Paginator<Resource> getPaginator() {
            return resources;
        }

        @Override
        List<Resource> getResourcesIncluded() {
            return resources.getForCurrentPage();
        }

        @Override
        Map<Resource, List<ResourceAllocation<?>>> getFoundAllocations() {
            return allocationsByResource;
        }

        @Override
        List<LoadTimeLine> buildTimeLines() {
            return new ByResourceLoadTimesLinesBuilder(parameters)
                    .buildGroupsByResource(getFoundAllocations());
        }

        private Paginator<Resource> resourcesToShow() {
            return parameters.getEntities(Resource.class,
                    new Callable<List<Resource>>() {

                        @Override
                        public List<Resource> call() throws Exception {
                            if (parameters.thereIsCurrentOrder()) {
                                return resourcesForActiveTasks();
                            } else {
                                return allResources();
                            }
                        }

                        private List<Resource> resourcesForActiveTasks() {
                            return Resource.sortByName(parameters
                                    .getPlanningState()
                                    .getResourcesRelatedWithAllocations());
                        }

                        private List<Resource> allResources() {
                            return Resource.sortByName(resourcesDAO
                                    .list(Resource.class));
                        }
                    }, new IReattacher<Resource>() {

                        @Override
                        public Resource reattach(Resource entity) {
                            return resourcesDAO.findExistingEntity(entity
                                    .getId());
                        }
                    });
        }

        private Map<Resource, List<ResourceAllocation<?>>> eachWithAllocations(
                List<Resource> allResources) {
            Map<Resource, List<ResourceAllocation<?>>> result = new LinkedHashMap<Resource, List<ResourceAllocation<?>>>();
            for (Resource resource : allResources) {
                IAllocationCriteria criteria = and(onInterval(),
                        relatedToResource(resource));
                result.put(resource, ResourceAllocation
                        .sortedByStartDate(doReplacementsIfNeeded(
                                resourceAllocationDAO.findAllocationsRelatedTo(
                                        getCurrentScenario(), resource,
                                        parameters.getInitDateFilter(),
                                        parameters.getEndDateFilter()),
                                criteria)));
            }
            return result;
        }

        private IAllocationCriteria relatedToResource(Resource resource) {
            return new RelatedWithResource(resource);
        }

    }

    private class ByCriterionFinder extends
            ResourceAllocationsFinder<Criterion> {

        private final Map<Criterion, List<ResourceAllocation<?>>> allocationsByCriterion;
        private final Paginator<Criterion> criterions;

        public ByCriterionFinder(ResourceLoadParameters parameters) {
            super(parameters);
            this.criterions = findCriterions();
            this.allocationsByCriterion = allocationsByCriterion(this.criterions
                    .getForCurrentPage());
        }

        @Override
        Paginator<Criterion> getPaginator() {
            return criterions;
        }

        @Override
        List<Resource> getResourcesIncluded() {
            Set<Resource> result = new HashSet<Resource>();
            for (List<ResourceAllocation<?>> each : getFoundAllocations()
                    .values()) {
                for (ResourceAllocation<?> eachAllocation : each) {
                    result.addAll(eachAllocation.getAssociatedResources());
                }
            }
            return new ArrayList<Resource>(result);
        }

        @Override
        protected Map<Criterion, List<ResourceAllocation<?>>> getFoundAllocations() {
            return allocationsByCriterion;
        }

        @Override
        List<LoadTimeLine> buildTimeLines() {
            return new ByCriterionLoadTimesLinesBuilder(parameters)
                    .buildGroupsByCriterion(getFoundAllocations());
        }

        private Paginator<Criterion> findCriterions() {
            return parameters.getEntities(Criterion.class,
                    criterionRetriever(), new IReattacher<Criterion>() {

                        @Override
                        public Criterion reattach(Criterion entity) {
                            criterionDAO.reattachUnmodifiedEntity(entity);
                            return entity;
                        }
                    });
        }

        Callable<List<Criterion>> criterionRetriever() {
            return new Callable<List<Criterion>>() {

                @Override
                public List<Criterion> call() throws Exception {
                    return Criterion
                            .sortByInclusionTypeAndName(findCriterions());
                }

                private Collection<Criterion> findCriterions() {
                    if (parameters.thereIsCurrentOrder()) {
                        List<Task> tasks = justTasks(parameters
                                .getCurrentOrder()
                                .getAllChildrenAssociatedTaskElements());
                        return getCriterionsOn(tasks);
                    } else {
                        return findAllocationsGroupedByCriteria().keySet();
                    }
                }

                private List<Criterion> getCriterionsOn(
                        Collection<? extends Task> tasks) {
                    Set<Criterion> result = new LinkedHashSet<Criterion>();
                    for (Task eachTask : tasks) {
                        result.addAll(getCriterionsOn(eachTask));
                    }
                    return new ArrayList<Criterion>(result);
                }

                private Set<Criterion> getCriterionsOn(Task task) {
                    Set<Criterion> result = new LinkedHashSet<Criterion>();
                    for (GenericResourceAllocation eachAllocation : onlyGeneric(task
                            .getSatisfiedResourceAllocations())) {
                        result.addAll(eachAllocation.getCriterions());
                    }
                    return result;
                }
            };
        }

        private Map<Criterion, List<ResourceAllocation<?>>> allocationsByCriterion(
                List<Criterion> criterions) {
            return withAssociatedSpecific(findAllocationsGroupedByCriteria(
                    scenarioManager.getCurrent(), criterions));
        }

        private Map<Criterion, List<GenericResourceAllocation>> findAllocationsGroupedByCriteria(
                Scenario onScenario, List<Criterion> relatedWith) {
            Map<Criterion, List<GenericResourceAllocation>> result = resourceAllocationDAO
                    .findGenericAllocationsBySomeCriterion(onScenario,
                            relatedWith,
                            asDate(parameters.getInitDateFilter()),
                            asDate(parameters.getEndDateFilter()));
            return doReplacementsIfNeeded(result);
        }

        private Map<Criterion, List<ResourceAllocation<?>>> withAssociatedSpecific(
                Map<Criterion, List<GenericResourceAllocation>> genericAllocationsByCriterion) {
            Map<Criterion, List<ResourceAllocation<?>>> result = new HashMap<Criterion, List<ResourceAllocation<?>>>();
            for (Entry<Criterion, List<GenericResourceAllocation>> each : genericAllocationsByCriterion
                    .entrySet()) {
                List<ResourceAllocation<?>> both = new ArrayList<ResourceAllocation<?>>();
                both.addAll(each.getValue());
                both.addAll(doReplacementsIfNeeded(resourceAllocationDAO
                        .findSpecificAllocationsRelatedTo(getCurrentScenario(),
                                each.getKey(),
                                asDate(parameters.getInitDateFilter()),
                                asDate(parameters.getEndDateFilter())),
                        and(onInterval(), specificRelatedTo(each.getKey()))));
                result.put(each.getKey(), both);
            }
            return result;
        }

        private IAllocationCriteria specificRelatedTo(Criterion key) {
            return new SpecificRelatedWithCriterionOnInterval(key,
                    parameters.getInitDateFilter(),
                    parameters.getEndDateFilter());
        }

        private Map<Criterion, List<GenericResourceAllocation>> findAllocationsGroupedByCriteria() {
            return doReplacementsIfNeeded(
                    resourceAllocationDAO.findGenericAllocationsByCriterion(
                            getCurrentScenario(),
                            asDate(parameters.getInitDateFilter()),
                            asDate(parameters.getEndDateFilter())));
        }

        private Map<Criterion, List<GenericResourceAllocation>> doReplacementsIfNeeded(
                Map<Criterion, List<GenericResourceAllocation>> map) {
            if (!parameters.thereIsCurrentOrder()) {
                return map;
            }
            Map<Criterion, List<GenericResourceAllocation>> result = new HashMap<Criterion, List<GenericResourceAllocation>>();
            for (Entry<Criterion, List<GenericResourceAllocation>> each : map
                    .entrySet()) {
                IAllocationCriteria criteria = and(onInterval(),
                        new RelatedWith(each.getKey()));
                List<ResourceAllocation<?>> replaced = parameters
                        .getPlanningState().replaceByCurrentOnes(
                                each.getValue(), criteria);
                if (!replaced.isEmpty()) {
                    result.put(each.getKey(), ResourceAllocation.getOfType(
                            GenericResourceAllocation.class, replaced));
                }
            }
            return result;
        }

    }

    class LoadTimeLinesBuilder {

        final PeriodBuilderFactory periodBuilderFactory;
        private final ResourceLoadParameters parameters;

        public LoadTimeLinesBuilder(ResourceLoadParameters parameters) {
            this.parameters = parameters;
            this.periodBuilderFactory = new PeriodBuilderFactory(
                    parameters.getInitDateFilter(),
                    parameters.getEndDateFilter());
        }

        TimeLineRole<BaseEntity> getCurrentTimeLineRole(BaseEntity entity) {
            return new TimeLineRole<BaseEntity>(entity);
        }

        LoadTimeLine buildGroupFor(Resource resource,
                List<? extends ResourceAllocation<?>> sortedByStartDate) {
            TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(resource);
            LoadTimeLine result = new LoadTimeLine(buildTimeLine(resource,
                    resource.getName(), sortedByStartDate, "resource", role),
                    buildSecondLevel(resource, sortedByStartDate));
            return result;
        }

        private List<LoadTimeLine> buildSecondLevel(Resource resource,
                List<? extends ResourceAllocation<?>> sortedByStartDate) {
            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            Map<Order, List<ResourceAllocation<?>>> byOrder = byOrder(sortedByStartDate);

            if (thereIsCurrentOrder()) {
                List<ResourceAllocation<?>> forCurrentOrder = byOrder
                        .get(getCurrentOrder());
                if (forCurrentOrder != null) {
                    result.addAll(buildTimeLinesForOrder(resource,
                            forCurrentOrder));
                }
                byOrder.remove(getCurrentOrder());
                // build time lines for other orders
                LoadTimeLine lineOthersOrders = buildTimeLinesForOtherOrders(
                        resource, byOrder);
                if (lineOthersOrders != null) {
                    result.add(lineOthersOrders);
                }
            } else {
                result.addAll(buildTimeLinesGroupForOrder(resource, byOrder));
            }
            return result;
        }

        Order getCurrentOrder() {
            return parameters.getCurrentOrder();
        }

        boolean thereIsCurrentOrder() {
            return parameters.thereIsCurrentOrder();
        }

        private List<LoadTimeLine> buildTimeLinesForOrder(Resource resource,
                List<ResourceAllocation<?>> sortedByStartDate) {
            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            result.addAll(buildTimeLinesForEachTask(resource,
                    onlySpecific(sortedByStartDate)));
            result.addAll(buildTimeLinesForEachCriterion(resource,
                    onlyGeneric(sortedByStartDate)));
            Collections.sort(result, LoadTimeLine.byStartAndEndDate());
            return result;
        }

        private List<LoadTimeLine> buildTimeLinesForEachTask(Resource resource,
                List<SpecificResourceAllocation> sortedByStartDate) {

            List<ResourceAllocation<?>> listOnlySpecific = new ArrayList<ResourceAllocation<?>>(
                    sortedByStartDate);
            Map<Task, List<ResourceAllocation<?>>> byTask = ResourceAllocation
                    .byTask(listOnlySpecific);

            List<LoadTimeLine> secondLevel = new ArrayList<LoadTimeLine>();
            for (Entry<Task, List<ResourceAllocation<?>>> entry : byTask
                    .entrySet()) {
                Task task = entry.getKey();
                TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(task);
                LoadTimeLine timeLine = buildTimeLine(resource, task.getName(),
                        entry.getValue(), "specific", role);
                if (!timeLine.isEmpty()) {
                    secondLevel.add(timeLine);
                }

            }
            return secondLevel;
        }

        private List<LoadTimeLine> buildTimeLinesForEachCriterion(
                Resource resource,
                List<GenericResourceAllocation> sortdByStartDate) {
            Map<Set<Criterion>, List<GenericResourceAllocation>> byCriterions = GenericResourceAllocation
                    .byCriterions(sortdByStartDate);

            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            for (Entry<Set<Criterion>, List<GenericResourceAllocation>> entry : byCriterions
                    .entrySet()) {

                Map<Task, List<ResourceAllocation<?>>> byTask = ResourceAllocation
                        .byTask(new ArrayList<ResourceAllocation<?>>(entry
                                .getValue()));

                for (Entry<Task, List<ResourceAllocation<?>>> entryTask : byTask
                        .entrySet()) {

                    Task task = entryTask.getKey();
                    List<GenericResourceAllocation> resouceAllocations = onlyGeneric(entryTask
                            .getValue());
                    TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(task);
                    LoadTimeLine timeLine = buildTimeLine(entry.getKey(), task,
                            resource, "generic", resouceAllocations, role);
                    if (!timeLine.isEmpty()) {
                        result.add(timeLine);
                    }

                }
            }
            return result;
        }

        private LoadTimeLine buildTimeLine(Collection<Criterion> criterions,
                Task task, Resource resource, String type,
                List<GenericResourceAllocation> allocationsSortedByStartDate,
                TimeLineRole<BaseEntity> role) {
            LoadPeriodGeneratorFactory periodGeneratorFactory = LoadPeriodGenerator
                    .onResourceSatisfying(resource, criterions);
            List<LoadPeriod> loadPeriods = periodBuilderFactory.build(
                    periodGeneratorFactory, allocationsSortedByStartDate);
            return new LoadTimeLine(getName(criterions, task), loadPeriods,
                    type, role);
        }

        String getName(Collection<? extends Criterion> criterions, Task task) {
            String prefix = task.getName();
            return (prefix + " :: " + Criterion.getCaptionFor(criterions));
        }

        private LoadTimeLine buildTimeLinesForOtherOrders(Resource resource,
                Map<Order, List<ResourceAllocation<?>>> byOrder) {
            List<ResourceAllocation<?>> resourceAllocations = getAllSortedValues(byOrder);
            if (resourceAllocations.isEmpty()) {
                return null;
            }
            TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(null);
            LoadTimeLine group = new LoadTimeLine(
                    buildTimeLine(resource, _("Other projects"),
                            resourceAllocations, "resource", role),
                    buildTimeLinesGroupForOrder(resource, byOrder));
            return group;
        }

        List<ResourceAllocation<?>> getAllSortedValues(
                Map<Order, List<ResourceAllocation<?>>> byOrder) {
            List<ResourceAllocation<?>> resourceAllocations = new ArrayList<ResourceAllocation<?>>();
            for (List<ResourceAllocation<?>> listAllocations : byOrder.values()) {
                resourceAllocations.addAll(listAllocations);
            }
            return ResourceAllocation.sortedByStartDate(resourceAllocations);
        }

        private List<LoadTimeLine> buildTimeLinesGroupForOrder(
                Resource resource,
                Map<Order, List<ResourceAllocation<?>>> byOrder) {
            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            for (Order order : byOrder.keySet()) {
                TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(order);
                result.add(new LoadTimeLine(buildTimeLine(resource,
                        order.getName(), byOrder.get(order), "resource", role),
                        buildTimeLinesForOrder(resource, byOrder.get(order))));
            }
            Collections.sort(result, LoadTimeLine.byStartAndEndDate());
            return result;
        }

        LoadTimeLine buildTimeLine(Resource resource, String name,
                List<? extends ResourceAllocation<?>> sortedByStartDate,
                String type, TimeLineRole<BaseEntity> role) {

            List<LoadPeriod> loadPeriods = periodBuilderFactory
                    .build(LoadPeriodGenerator.onResource(resource),
                            sortedByStartDate);
            return new LoadTimeLine(name, loadPeriods, type, role);
        }

    }

    class ByResourceLoadTimesLinesBuilder extends LoadTimeLinesBuilder {

        public ByResourceLoadTimesLinesBuilder(ResourceLoadParameters parameters) {
            super(parameters);
        }

        List<LoadTimeLine> buildGroupsByResource(
                Map<Resource, List<ResourceAllocation<?>>> map) {
            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            for (Entry<Resource, List<ResourceAllocation<?>>> each : map
                    .entrySet()) {
                LoadTimeLine l = buildGroupFor(each.getKey(), each.getValue());
                result.add(l);
            }
            return result;
        }

    }

    class ByCriterionLoadTimesLinesBuilder extends LoadTimeLinesBuilder {

        public ByCriterionLoadTimesLinesBuilder(
                ResourceLoadParameters parameters) {
            super(parameters);
        }

        List<LoadTimeLine> buildGroupsByCriterion(
                Map<Criterion, List<ResourceAllocation<?>>> map) {
            return groupsFor(map);
        }

        private List<LoadTimeLine> groupsFor(
                Map<Criterion, List<ResourceAllocation<?>>> allocationsByCriterion) {
            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            for (Entry<Criterion, List<ResourceAllocation<?>>> each : allocationsByCriterion
                    .entrySet()) {
                Criterion criterion = each.getKey();
                List<ResourceAllocation<?>> allocations = ResourceAllocation
                        .sortedByStartDate(each.getValue());
                if (allocations == null) {
                    continue;
                }
                TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(criterion);
                LoadTimeLine group = new LoadTimeLine(createMain(criterion,
                        allocations, role), buildSecondaryLevels(criterion,
                        allocations));
                if (!group.isEmpty()) {
                    result.add(group);
                }
            }
            return result;
        }

        private LoadTimeLine createMain(Criterion criterion,
                List<? extends ResourceAllocation<?>> orderedAllocations,
                TimeLineRole<BaseEntity> role) {
            return new LoadTimeLine(criterion.getType().getName() + ": "
                    + criterion.getName(), createPeriods(criterion,
                    orderedAllocations), "global-generic", role);
        }

        private List<LoadPeriod> createPeriods(Criterion criterion,
                List<? extends ResourceAllocation<?>> value) {
            return periodBuilderFactory.build(LoadPeriodGenerator.onCriterion(
                    criterion, resourcesSearchModel), value);
        }

        private List<LoadTimeLine> buildSecondaryLevels(Criterion criterion,
                List<? extends ResourceAllocation<?>> allocations) {
            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            result.addAll(buildSubLevels(criterion, ResourceAllocation
                    .getOfType(GenericResourceAllocation.class, allocations)));
            result.add(buildRelatedSpecificAllocations(criterion, allocations));
            Collections.sort(result, LoadTimeLine.byStartAndEndDate());
            return result;
        }

        private List<LoadTimeLine> buildSubLevels(Criterion criterion,
                List<? extends ResourceAllocation<?>> allocations) {
            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            Map<Order, List<ResourceAllocation<?>>> byOrder = byOrder(new ArrayList<ResourceAllocation<?>>(
                    allocations));

            if (thereIsCurrentOrder()) {
                List<ResourceAllocation<?>> allocationsForCurrent = byOrder
                        .get(getCurrentOrder());
                if (allocationsForCurrent != null) {
                    result.addAll(buildTimeLinesForOrder(getCurrentOrder(),
                            criterion, allocationsForCurrent));
                }
                byOrder.remove(getCurrentOrder());
                // build time lines for other orders
                LoadTimeLine lineOthersOrders = buildTimeLinesForOtherOrders(
                        criterion, byOrder);
                if (lineOthersOrders != null) {
                    result.add(lineOthersOrders);
                }
            } else {
                result.addAll(buildTimeLinesGroupForOrder(criterion, byOrder));
            }
            return result;
        }

        private LoadTimeLine buildTimeLinesForOtherOrders(Criterion criterion,
                Map<Order, List<ResourceAllocation<?>>> byOrder) {
            List<ResourceAllocation<?>> allocations = getAllSortedValues(byOrder);
            if (allocations.isEmpty()) {
                return null;
            }

            LoadTimeLine group = new LoadTimeLine(buildTimeLine(criterion,
                    "Other projects", "global-generic", allocations,
                    getCurrentTimeLineRole(null)), buildTimeLinesGroupForOrder(
                    criterion, byOrder));
            return group;
        }

        private List<LoadTimeLine> buildTimeLinesGroupForOrder(
                Criterion criterion,
                Map<Order, List<ResourceAllocation<?>>> byOrder) {
            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            for (Order order : byOrder.keySet()) {
                if (byOrder.get(order) == null) {
                    // no allocations found for order
                    continue;
                }
                TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(order);
                result.add(new LoadTimeLine(buildTimeLine(criterion,
                        order.getName(), "global-generic", byOrder.get(order),
                        role), buildTimeLinesForOrder(order, criterion,
                        byOrder.get(order))));
            }
            return result;
        }

        LoadTimeLine buildTimeLine(Criterion criterion, String name,
                String type, List<ResourceAllocation<?>> allocations,
                TimeLineRole<BaseEntity> role) {
            List<GenericResourceAllocation> generics = onlyGeneric(allocations);
            return new LoadTimeLine(name, createPeriods(criterion, generics),
                    type, role);
        }

        private List<LoadTimeLine> buildTimeLinesForOrder(Order order,
                Criterion criterion, List<ResourceAllocation<?>> allocations) {
            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            result.addAll(buildTimeLinesForEachTask(criterion,
                    onlyGeneric(allocations)));
            result.addAll(buildTimeLinesForEachResource(criterion,
                    onlySpecific(allocations), getCurrentTimeLineRole(order)));
            Collections.sort(result, LoadTimeLine.byStartAndEndDate());
            return result;
        }

        private List<LoadTimeLine> buildTimeLinesForEachTask(
                Criterion criterion, List<GenericResourceAllocation> allocations) {
            Map<Task, List<GenericResourceAllocation>> byTask = ResourceAllocation
                    .byTask(allocations);

            List<LoadTimeLine> secondLevel = new ArrayList<LoadTimeLine>();
            for (Entry<Task, List<GenericResourceAllocation>> entry : byTask
                    .entrySet()) {
                Task task = entry.getKey();

                Map<Set<Criterion>, List<GenericResourceAllocation>> mapSameCriteria = getAllocationsWithSameCriteria((entry
                        .getValue()));
                for (Entry<Set<Criterion>, List<GenericResourceAllocation>> entrySameCriteria : mapSameCriteria
                        .entrySet()) {
                    Set<Criterion> criterions = entrySameCriteria.getKey();
                    List<GenericResourceAllocation> genericAllocations = entrySameCriteria
                            .getValue();
                    List<ResourceAllocation<?>> resourceAllocations = new ArrayList<ResourceAllocation<?>>(
                            genericAllocations);
                    TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(task);

                    /**
                     * Each resource line has the same role than its allocated
                     * task, so that link with the resource allocation screen
                     */
                    LoadTimeLine timeLine = new LoadTimeLine(buildTimeLine(
                            criterions, task, criterion, "global-generic",
                            resourceAllocations, role),
                            buildTimeLinesForEachResource(criterion,
                                    genericAllocations, role));
                    if (!timeLine.isEmpty()) {
                        secondLevel.add(timeLine);
                    }
                }
            }
            return secondLevel;
        }

        private List<LoadTimeLine> buildTimeLinesForEachResource(
                Criterion criterion,
                List<? extends ResourceAllocation<?>> allocations,
                TimeLineRole<BaseEntity> role) {
            Map<Resource, List<ResourceAllocation<?>>> byResource = ResourceAllocation
                    .byResource(allocations);

            List<LoadTimeLine> secondLevel = new ArrayList<LoadTimeLine>();
            for (Entry<Resource, List<ResourceAllocation<?>>> entry : byResource
                    .entrySet()) {
                Resource resource = entry.getKey();
                List<ResourceAllocation<?>> resourceAllocations = entry
                        .getValue();
                String descriptionTimeLine = resource.getShortDescription();

                LoadTimeLine timeLine = buildTimeLine(resource,
                        descriptionTimeLine, resourceAllocations, "generic",
                        role);
                if (!timeLine.isEmpty()) {
                    secondLevel.add(timeLine);
                }

            }
            return secondLevel;
        }

        private LoadTimeLine buildTimeLine(Collection<Criterion> criterions,
                Task task, Criterion criterion, String type,
                List<ResourceAllocation<?>> allocations,
                TimeLineRole<BaseEntity> role) {
            return buildTimeLine(criterion, getName(criterions, task), type,
                    allocations, role);
        }

        private LoadTimeLine buildRelatedSpecificAllocations(
                Criterion criterion,
                List<? extends ResourceAllocation<?>> allocations) {
            List<SpecificResourceAllocation> specific = ResourceAllocation
                    .getOfType(SpecificResourceAllocation.class, allocations);

            LoadTimeLine main = new LoadTimeLine(_("Specific Allocations"),
                    createPeriods(criterion, specific), "related-specific",
                    getCurrentTimeLineRole(criterion));
            List<LoadTimeLine> children = buildGroupsFor(ResourceAllocation
                    .byResource(new ArrayList<ResourceAllocation<?>>(specific)));
            return new LoadTimeLine(main, children);
        }

        private List<LoadTimeLine> buildGroupsFor(
                Map<Resource, List<ResourceAllocation<?>>> map) {
            List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
            for (Entry<Resource, List<ResourceAllocation<?>>> each : map
                    .entrySet()) {
                LoadTimeLine l = buildGroupFor(each.getKey(), each.getValue());
                if (!l.isEmpty()) {
                    result.add(l);
                }
            }
            return result;
        }

    }


    public static Date asDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.toDateTimeAtStartOfDay().toDate();
    }

    public static LocalDate toLocal(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDate.fromDateFields(date);
    }

    private Map<Set<Criterion>, List<GenericResourceAllocation>> getAllocationsWithSameCriteria(
            List<GenericResourceAllocation> genericAllocations) {
        return GenericResourceAllocation.byCriterions(genericAllocations);
    }

    private Map<Order, List<ResourceAllocation<?>>> byOrder(
            Collection<? extends ResourceAllocation<?>> allocations) {
        Map<Order, List<ResourceAllocation<?>>> result = new HashMap<Order, List<ResourceAllocation<?>>>();
        for (ResourceAllocation<?> resourceAllocation : allocations) {
            if ((resourceAllocation.isSatisfied())
                    && (resourceAllocation.getTask() != null)) {
                OrderElement orderElement = resourceAllocation.getTask()
                        .getOrderElement();
                Order order = orderDAO.loadOrderAvoidingProxyFor(orderElement);
                initializeIfNeeded(result, order);
                result.get(order).add(resourceAllocation);
            }
        }
        return result;
    }

    private void initializeIfNeeded(
            Map<Order, List<ResourceAllocation<?>>> result, Order order) {
        if (!result.containsKey(order)) {
            result.put(order, new ArrayList<ResourceAllocation<?>>());
        }
    }

    private static List<GenericResourceAllocation> onlyGeneric(
            Collection<? extends ResourceAllocation<?>> sortedByStartDate) {
        return ResourceAllocation.getOfType(GenericResourceAllocation.class,
                sortedByStartDate);
    }

    private static List<SpecificResourceAllocation> onlySpecific(
            List<ResourceAllocation<?>> sortedByStartDate) {
        return ResourceAllocation.getOfType(SpecificResourceAllocation.class,
                sortedByStartDate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExpandResourceLoadViewCharts() {

        User user;
        try {
            user = this.userDAO.findByLoginName(SecurityUtils
                    .getSessionUserLoginName());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        return user.isExpandResourceLoadViewCharts();
    }

}

class PeriodBuilderFactory {
    private final LocalDate initDateFilter;
    private final LocalDate endDateFilter;

    public PeriodBuilderFactory(LocalDate initDateFilter, LocalDate endDateFilter) {
        this.initDateFilter = initDateFilter;
        this.endDateFilter = endDateFilter;
    }

    public List<LoadPeriod> build(LoadPeriodGeneratorFactory factory, List<? extends ResourceAllocation<?>> sortedByStartDate){
        if (initDateFilter == null && endDateFilter == null) {
            return PeriodsBuilder.build(factory, sortedByStartDate);
        } else {
            return PeriodsBuilder.build(factory, sortedByStartDate, asDate(initDateFilter), asDate(endDateFilter));
        }
    }

    private Date asDate(LocalDate date) {
        return ResourceLoadModel.asDate(date);
    }

}

class PeriodsBuilder {

    private final List<? extends ResourceAllocation<?>> sortedByStartDate;

    private final List<LoadPeriodGenerator> loadPeriodsGenerators = new LinkedList<LoadPeriodGenerator>();

    private final LoadPeriodGeneratorFactory factory;

    private PeriodsBuilder(LoadPeriodGeneratorFactory factory,
            List<? extends ResourceAllocation<?>> sortedByStartDate) {
        this.factory = factory;
        this.sortedByStartDate = sortedByStartDate;
    }

    public static List<LoadPeriod> build(LoadPeriodGeneratorFactory factory,
            List<? extends ResourceAllocation<?>> sortedByStartDate) {
        return new PeriodsBuilder(factory, sortedByStartDate).buildPeriods();
    }

    public static List<LoadPeriod> build(LoadPeriodGeneratorFactory factory,
            List<? extends ResourceAllocation<?>> sortedByStartDate,
            Date startDateFilter, Date endDateFilter) {
        List<LoadPeriod> list = new PeriodsBuilder(factory, sortedByStartDate).buildPeriods();
        List<LoadPeriod> toReturn = new ArrayList<LoadPeriod>();
        for (LoadPeriod loadPeriod : list) {

            final GanttDate finalStartDate;
            if (startDateFilter != null) {
                finalStartDate = GanttDate.max(GanttDate
                        .createFrom(new LocalDate(startDateFilter.getTime())),
                        loadPeriod.getStart());
            } else {
                finalStartDate = loadPeriod.getStart();
            }

            final GanttDate finalEndDate;
            if (endDateFilter != null) {
                finalEndDate = GanttDate.min(loadPeriod.getEnd(), GanttDate
                        .createFrom(new LocalDate(endDateFilter.getTime())));
            } else {
                finalEndDate = loadPeriod.getEnd();
            }
            if (finalStartDate.compareTo(finalEndDate) < 0) {
                toReturn.add(new LoadPeriod(finalStartDate, finalEndDate,
                        loadPeriod.getAvailableEffort(),
                        loadPeriod.getAssignedEffort(), loadPeriod.getLoadLevel()));
            }
        }
        return toReturn;
    }

    private List<LoadPeriod> buildPeriods() {
        for (ResourceAllocation<?> resourceAllocation : sortedByStartDate) {
            loadPeriodsGenerators.add(factory.create(resourceAllocation));
        }
        joinPeriodGenerators();
        return toGenerators(loadPeriodsGenerators);
    }

    private List<LoadPeriod> toGenerators(List<LoadPeriodGenerator> generators) {
        List<LoadPeriod> result = new ArrayList<LoadPeriod>();
        for (LoadPeriodGenerator loadPeriodGenerator : generators) {
            LoadPeriod period = loadPeriodGenerator.build();
            if (period != null) {
                result.add(period);
            }
        }
        return result;
    }

    private void joinPeriodGenerators() {
        ListIterator<LoadPeriodGenerator> iterator = loadPeriodsGenerators
                .listIterator();
        while (iterator.hasNext()) {
            final LoadPeriodGenerator current = findNextOneOverlapping(iterator);
            if (current != null) {
                rewind(iterator, current);
                iterator.remove();
                LoadPeriodGenerator next = iterator.next();
                iterator.remove();
                List<LoadPeriodGenerator> generated = current.join(next);
                final LoadPeriodGenerator positionToComeBack = generated.get(0);
                final List<LoadPeriodGenerator> remaining = loadPeriodsGenerators
                        .subList(iterator.nextIndex(), loadPeriodsGenerators
                                .size());
                List<LoadPeriodGenerator> generatorsSortedByStartDate = mergeListsKeepingByStartSortOrder(
                        generated, remaining);
                final int takenFromRemaining = generatorsSortedByStartDate
                        .size()
                        - generated.size();
                removeNextElements(iterator, takenFromRemaining);
                addAtCurrentPosition(iterator, generatorsSortedByStartDate);
                rewind(iterator, positionToComeBack);
            }
        }
    }

    private LoadPeriodGenerator findNextOneOverlapping(
            ListIterator<LoadPeriodGenerator> iterator) {
        while (iterator.hasNext()) {
            LoadPeriodGenerator current = iterator.next();
            if (!iterator.hasNext()) {
                return null;
            }
            LoadPeriodGenerator next = peekNext(iterator);
            if (current.overlaps(next)) {
                return current;
            }
        }
        return null;
    }

    private void addAtCurrentPosition(
            ListIterator<LoadPeriodGenerator> iterator,
            List<LoadPeriodGenerator> sortedByStartDate) {
        for (LoadPeriodGenerator l : sortedByStartDate) {
            iterator.add(l);
        }
    }

    private void removeNextElements(ListIterator<LoadPeriodGenerator> iterator,
            final int elementsNumber) {
        for (int i = 0; i < elementsNumber; i++) {
            iterator.next();
            iterator.remove();
        }
    }

    private void rewind(ListIterator<LoadPeriodGenerator> iterator,
            LoadPeriodGenerator nextOne) {
        while (peekNext(iterator) != nextOne) {
            iterator.previous();
        }
    }

    private List<LoadPeriodGenerator> mergeListsKeepingByStartSortOrder(
            List<LoadPeriodGenerator> joined,
            List<LoadPeriodGenerator> remaining) {
        List<LoadPeriodGenerator> result = new ArrayList<LoadPeriodGenerator>();
        ListIterator<LoadPeriodGenerator> joinedIterator = joined
                .listIterator();
        ListIterator<LoadPeriodGenerator> remainingIterator = remaining
                .listIterator();
        while (joinedIterator.hasNext() && remainingIterator.hasNext()) {
            LoadPeriodGenerator fromJoined = peekNext(joinedIterator);
            LoadPeriodGenerator fromRemaining = peekNext(remainingIterator);
            if (fromJoined.getStart().compareTo(fromRemaining.getStart()) <= 0) {
                result.add(fromJoined);
                joinedIterator.next();
            } else {
                result.add(fromRemaining);
                remainingIterator.next();
            }
        }
        if (joinedIterator.hasNext()) {
            result.addAll(joined.subList(joinedIterator.nextIndex(), joined
                    .size()));
        }
        return result;
    }

    private LoadPeriodGenerator peekNext(
            ListIterator<LoadPeriodGenerator> iterator) {
        if (!iterator.hasNext()) {
            return null;
        }
        LoadPeriodGenerator result = iterator.next();
        iterator.previous();
        return result;
    }

}
