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

package org.navalplanner.web.resourceload;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.IDayAssignmentDAO;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.daos.IResourcesSearcher;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.users.daos.IOrderAuthorizationDAO;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.calendars.BaseCalendarModel;
import org.navalplanner.web.planner.order.PlanningStateCreator.PlanningState;
import org.navalplanner.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.data.resourceload.LoadPeriod;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.data.resourceload.TimeLineRole;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;

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

    private List<LoadTimeLine> loadTimeLines;
    private Interval viewInterval;

    private PlanningState filterBy;

    private boolean filterByResources = true;

    /**
     * Contains the resources to be shown when specified manually using
     * the Bandbox
     */
    private List<Resource> resourcesToShowList = new ArrayList<Resource>();

    /**
     * Contains the criteria to be shown when specified manually using
     * the Bandbox
     */
    private List<Criterion> criteriaToShowList = new ArrayList<Criterion>();

    private LocalDate initDateFilter;
    private LocalDate endDateFilter;

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    private int pageFilterPosition = 0;
    private int pageSize = 10;

    /**
     * Contains all the resources which have to be filtered page by page
     */
    private List<Resource> allResourcesList;

    /**
     * Contains all the resources which have to be filtered page by page
     */
    List<Criterion> allCriteriaList;

    @Override
    @Transactional(readOnly = true)
    public void initGlobalView(boolean filterByResources) {
        filterBy = null;
        this.filterByResources = filterByResources;
        doGlobalView();
    }

    @Override
    @Transactional(readOnly = true)
    public void initGlobalView(PlanningState filterBy, boolean filterByResources) {
        this.filterBy = filterBy;
        this.filterBy.reattach();
        this.filterBy.reassociateResourcesWithSession();
        this.filterByResources = filterByResources;
        doGlobalView();
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
        if (SecurityUtils.isUserInRole(UserRole.ROLE_READ_ALL_ORDERS)
                || SecurityUtils.isUserInRole(UserRole.ROLE_EDIT_ALL_ORDERS)) {
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

    private void doGlobalView() {
        loadTimeLines = calculateLoadTimeLines();
        if (!loadTimeLines.isEmpty()) {
            viewInterval = LoadTimeLine.getIntervalFrom(loadTimeLines);
        } else {
            viewInterval = new Interval(new Date(), plusFiveYears(new Date()));
        }
    }

    private Date plusFiveYears(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 5);
        return calendar.getTime();
    }

    private List<LoadTimeLine> calculateLoadTimeLines() {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        if (filterByResources) {
            result.addAll(groupsFor(resourcesToShow()));
        } else {
            allocationsByCriterion = allocationsByCriterion();
            result.addAll(groupsFor(allocationsByCriterion));
        }
        return result;
    }

    private Map<Criterion, List<ResourceAllocation<?>>> allocationsByCriterion;

    private Map<Criterion, List<ResourceAllocation<?>>> allocationsByCriterion() {
        if (!criteriaToShowList.isEmpty()) {
            reattachCriteriaToShow();
            // reattaching criterions so the query returns the same criteria as
            // keys
            allCriteriaList = new ArrayList<Criterion>(criteriaToShowList);
            return withAssociatedSpecific(findAllocationsGroupedByCriteria(criteriaToShowList));
        }
        Map<Criterion, List<GenericResourceAllocation>> result = findAllocationsByCriterion();
        allCriteriaList = Criterion.sortByInclusionTypeAndName(result.keySet());
        if (pageFilterPosition == -1) {
            return withAssociatedSpecific(result);
        }
        List<Criterion> criteriaReallyShown = allCriteriaList.subList(
                pageFilterPosition, getEndPositionForCriterionPageFilter());
        return withAssociatedSpecific(onlyForThePagesShown(criteriaReallyShown,
                result));
    }

    private Map<Criterion, List<GenericResourceAllocation>> findAllocationsByCriterion() {
        if (filter()) {
            List<Task> tasks = justTasks(filterBy.getOrder()
                    .getAllChildrenAssociatedTaskElements());
            return findAllocationsGroupedByCriteria(getCriterionsOn(tasks));
        } else {
            return findAllocationsGroupedByCriteria();
        }
    }

    private Map<Criterion, List<GenericResourceAllocation>> findAllocationsGroupedByCriteria(
            List<Criterion> relatedWith) {
        return resourceAllocationDAO
                .findGenericAllocationsBySomeCriterion(
                        relatedWith, asDate(initDateFilter), asDate(endDateFilter));
    }

    private Map<Criterion, List<GenericResourceAllocation>> findAllocationsGroupedByCriteria() {
        return resourceAllocationDAO
                .findGenericAllocationsByCriterion(
                asDate(initDateFilter), asDate(endDateFilter));
    }

    private Map<Criterion, List<ResourceAllocation<?>>> withAssociatedSpecific(
            Map<Criterion, List<GenericResourceAllocation>> genericAllocationsByCriterion) {
        Map<Criterion, List<ResourceAllocation<?>>> result = new HashMap<Criterion, List<ResourceAllocation<?>>>();
        for (Entry<Criterion, List<GenericResourceAllocation>> each : genericAllocationsByCriterion
                .entrySet()) {
            List<ResourceAllocation<?>> both = new ArrayList<ResourceAllocation<?>>();
            both.addAll(each.getValue());
            both.addAll(resourceAllocationDAO.findSpecificAllocationsRelatedTo(
                    each.getKey(), asDate(initDateFilter),
                    asDate(endDateFilter)));
            result.put(each.getKey(), both);
        }
        return result;
    }

    private <R extends ResourceAllocation<?>> Map<Criterion, List<R>> onlyForThePagesShown(
            List<Criterion> criteriaReallyShown,
            Map<Criterion, List<R>> allocationsByCriteria) {
        Map<Criterion, List<R>> result = new HashMap<Criterion, List<R>>();
        for (Criterion each : criteriaReallyShown) {
            if (allocationsByCriteria.get(each) != null) {
                result.put(each, allocationsByCriteria.get(each));
            }
        }
        return result;
    }

    public static Date asDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.toDateTimeAtStartOfDay().toDate();
    }

    private void reattachCriteriaToShow() {
        for (Criterion each : criteriaToShowList) {
            criterionDAO.reattachUnmodifiedEntity(each);
        }
    }

    private List<Criterion> getCriterionsOn(Collection<? extends Task> tasks) {
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

    private List<Resource> resourcesToShow() {
        if(!resourcesToShowList.isEmpty()) {
            return getResourcesToShowReattached();
        }
        // if we haven't manually specified some resources to show, we load them
        if (filter()) {
            allResourcesList = resourcesForActiveTasks();
        } else {
            allResourcesList = allResources();
        }
        return applyPagination(allResourcesList);
    }

    private List<Resource> applyPagination(List<Resource> allResourcesList) {
        if (pageFilterPosition == -1) {
            return allResourcesList;
        }
        return allResourcesList.subList(pageFilterPosition, Math.min(
                pageFilterPosition + pageSize, allResourcesList.size()));
    }

    private List<Criterion> criteriaToShow() {
        List<Criterion> criteriaList;
        if(!criteriaToShowList.isEmpty()) {
            criteriaList = criteriaToShowList;
        }
        else if(pageFilterPosition == -1) {
            criteriaList = allCriteriaList;
        }
        else {
            criteriaList = allCriteriaList.subList(
                    pageFilterPosition, getEndPositionForCriterionPageFilter());
        }
        return criteriaList;
    }

    @Override
    public List<Resource> getAllResourcesList() {
        return allResourcesList;
    }

    @Override
    public List<Criterion> getAllCriteriaList() {
        return allCriteriaList;
    }

    @Override
    public int getPageFilterPosition() {
        return pageFilterPosition;
    }

    @Override
    public void setPageFilterPosition(int pageFilterPosition) {
        this.pageFilterPosition = pageFilterPosition;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    private int getEndPositionForCriterionPageFilter() {
        return
            (pageFilterPosition + pageSize < allCriteriaList.size())?
                pageFilterPosition + pageSize :
                allCriteriaList.size();
    }

    private boolean filter() {
        return filterBy != null;
    }

    private List<Resource> resourcesForActiveTasks() {
        return Resource.sortByName(resourcesDAO
                .findResourcesRelatedTo(justTasks(filterBy.getOrder()
                        .getAllChildrenAssociatedTaskElements())));
    }

    private List<Task> justTasks(Collection<? extends TaskElement> tasks) {
        List<Task> result = new ArrayList<Task>();
        for (TaskElement taskElement : tasks) {
            if (taskElement instanceof Task) {
                result.add((Task) taskElement);
            }
        }
        return result;
    }

    private List<Resource> allResources() {
        return Resource.sortByName(resourcesDAO.list(Resource.class));
    }

    private TimeLineRole<BaseEntity> getCurrentTimeLineRole(BaseEntity entity) {
        return new TimeLineRole<BaseEntity>(entity);
    }

    /**
     * @param allocationsByCriterion
     * @return
     */
    private List<LoadTimeLine> groupsFor(
            Map<Criterion, List<ResourceAllocation<?>>> allocationsByCriterion) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        for(Criterion criterion : criteriaToShow()) {
            if (allocationsByCriterion.get(criterion) == null) {
                // no allocations found for criterion
                continue;
            }
            List<ResourceAllocation<?>> allocations = ResourceAllocation
                    .sortedByStartDate(allocationsByCriterion
                            .get(criterion));
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

    private List<LoadTimeLine> buildSecondaryLevels(Criterion criterion,
            List<? extends ResourceAllocation<?>> allocations) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        result.addAll(buildSubLevels(criterion, ResourceAllocation.getOfType(
                GenericResourceAllocation.class, allocations)));
        result.add(buildRelatedSpecificAllocations(criterion, allocations));
        Collections.sort(result, LoadTimeLine.byStartAndEndDate());
        return result;
    }

    private LoadTimeLine buildRelatedSpecificAllocations(Criterion criterion,
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

    private List<LoadTimeLine> buildSubLevels(Criterion criterion,
            List<? extends ResourceAllocation<?>> allocations) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        Map<Order, List<ResourceAllocation<?>>> byOrder = byOrder(new ArrayList<ResourceAllocation<?>>(
                allocations));

        // REVISAR ESTO ANTES DE ACABAR
        if (filter()) {
            // build time lines for current order
            if (byOrder.get(filterBy.getOrder()) != null) {
                result.addAll(buildTimeLinesForOrder(filterBy.getOrder(),
                        criterion, byOrder.get(filterBy.getOrder())));
            }
            byOrder.remove(filterBy.getOrder());
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

    private List<LoadTimeLine> buildTimeLinesForOrder(Order order,
            Criterion criterion,
            List<ResourceAllocation<?>> allocations) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        result.addAll(buildTimeLinesForEachTask(criterion,
                onlyGeneric(allocations)));
        result.addAll(buildTimeLinesForEachResource(criterion,
                onlySpecific(allocations), getCurrentTimeLineRole(order)));
        Collections.sort(result, LoadTimeLine.byStartAndEndDate());
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
                getCurrentTimeLineRole(null)),
                buildTimeLinesGroupForOrder(
                criterion, byOrder));
        return group;
    }

    private List<LoadTimeLine> buildTimeLinesGroupForOrder(Criterion criterion,
            Map<Order, List<ResourceAllocation<?>>> byOrder) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        for (Order order : byOrder.keySet()) {
            if (byOrder.get(order) == null) {
                // no allocations found for order
                continue;
            }
            TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(order);
            result.add(new LoadTimeLine(
                    buildTimeLine(criterion, order.getName(), "global-generic",
                            byOrder.get(order), role), buildTimeLinesForOrder(
                            order, criterion, byOrder.get(order))));
        }
        return result;
    }

    private List<LoadTimeLine> buildTimeLinesForEachTask(Criterion criterion,
            List<GenericResourceAllocation> allocations) {
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
                 * Each resource line has the same role than its allocated task,
                 * so that link with the resource allocation screen
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

    private Map<Set<Criterion>, List<GenericResourceAllocation>> getAllocationsWithSameCriteria(
            List<GenericResourceAllocation> genericAllocations) {
        return GenericResourceAllocation.byCriterions(genericAllocations);
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
            List<ResourceAllocation<?>> resourceAllocations = entry.getValue();
            String descriptionTimeLine = resource.getShortDescription();

            LoadTimeLine timeLine = buildTimeLine(resource,
                    descriptionTimeLine, resourceAllocations, "generic", role);
            if (!timeLine.isEmpty()) {
                secondLevel.add(timeLine);
            }

        }
        return secondLevel;
    }

    private LoadTimeLine createMain(Criterion criterion,
            List<? extends ResourceAllocation<?>> orderedAllocations,
            TimeLineRole<BaseEntity> role) {
        return new LoadTimeLine(criterion.getType().getName() + ": " + criterion.getName(),
                createPeriods(criterion, orderedAllocations), "global-generic", role);
    }

    private List<LoadPeriod> createPeriods(Criterion criterion,
            List<? extends ResourceAllocation<?>> value) {
        if (initDateFilter != null || endDateFilter != null) {
            return PeriodsBuilder.build(LoadPeriodGenerator.onCriterion(
                    criterion, resourcesSearchModel),
                    value, asDate(initDateFilter), asDate(endDateFilter));
        }
        return PeriodsBuilder.build(LoadPeriodGenerator.onCriterion(criterion,
                resourcesSearchModel), value);
    }

    private List<LoadTimeLine> groupsFor(List<Resource> allResources) {
        return buildGroupsFor(eachWithAllocations(allResources));
    }

    private Map<Resource, List<ResourceAllocation<?>>> eachWithAllocations(
            List<Resource> allResources) {
        Map<Resource, List<ResourceAllocation<?>>> map = new LinkedHashMap<Resource, List<ResourceAllocation<?>>>();
        for (Resource resource : allResources) {
            map.put(resource, ResourceAllocation
                    .sortedByStartDate(resourceAllocationDAO
                            .findAllocationsRelatedTo(resource, initDateFilter,
                                    endDateFilter)));
        }
        return map;
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

    private LoadTimeLine buildGroupFor(Resource resource,
            List<? extends ResourceAllocation<?>> sortedByStartDate) {
        TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(resource);
        LoadTimeLine result = new LoadTimeLine(buildTimeLine(resource, resource
                .getName(), sortedByStartDate, "resource", role),
                buildSecondLevel(resource, sortedByStartDate));
        return result;
    }

    private List<LoadTimeLine> buildSecondLevel(Resource resource,
            List<? extends ResourceAllocation<?>> sortedByStartDate) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        Map<Order, List<ResourceAllocation<?>>> byOrder = byOrder(sortedByStartDate);

        if (filter()) {
            if (byOrder.get(filterBy.getOrder()) != null) {
                // build time lines for current order
                result.addAll(buildTimeLinesForOrder(resource,
                        byOrder.get(filterBy.getOrder())));
            }
            byOrder.remove(filterBy.getOrder());
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

    private LoadTimeLine buildTimeLinesForOtherOrders(Resource resource,
            Map<Order, List<ResourceAllocation<?>>> byOrder) {
        List<ResourceAllocation<?>> resourceAllocations = getAllSortedValues(byOrder);
        if (resourceAllocations.isEmpty()) {
            return null;
        }
        TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(null);
        LoadTimeLine group = new LoadTimeLine(buildTimeLine(resource,
                _("Other projects"), resourceAllocations, "resource", role),
                buildTimeLinesGroupForOrder(resource, byOrder));
        return group;
    }

    private List<LoadTimeLine> buildTimeLinesGroupForOrder(Resource resource,
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

    private List<ResourceAllocation<?>> getAllSortedValues(
            Map<Order, List<ResourceAllocation<?>>> byOrder) {
        List<ResourceAllocation<?>> resourceAllocations = new ArrayList<ResourceAllocation<?>>();
        for (List<ResourceAllocation<?>> listAllocations : byOrder.values()) {
            resourceAllocations.addAll(listAllocations);
        }
        return ResourceAllocation.sortedByStartDate(resourceAllocations);
    }

    private void initializeIfNeeded(
            Map<Order, List<ResourceAllocation<?>>> result, Order order) {
        if (!result.containsKey(order)) {
            result.put(order, new ArrayList<ResourceAllocation<?>>());
        }
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

    private List<GenericResourceAllocation> onlyGeneric(
            Collection<? extends ResourceAllocation<?>> sortedByStartDate) {
        return ResourceAllocation.getOfType(GenericResourceAllocation.class,
                sortedByStartDate);
    }

    private List<SpecificResourceAllocation> onlySpecific(
            List<ResourceAllocation<?>> sortedByStartDate) {
        return ResourceAllocation.getOfType(SpecificResourceAllocation.class,
                sortedByStartDate);
    }

    private List<LoadTimeLine> buildTimeLinesForEachCriterion(
            Resource resource, List<GenericResourceAllocation> sortdByStartDate) {
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

    private List<LoadTimeLine> buildTimeLinesForEachTask(Resource resource,
            List<SpecificResourceAllocation> sortedByStartDate) {

        List<ResourceAllocation<?>> listOnlySpecific = new ArrayList<ResourceAllocation<?>>(
                sortedByStartDate);
        Map<Task, List<ResourceAllocation<?>>> byTask = ResourceAllocation
                .byTask(listOnlySpecific);

        List<LoadTimeLine> secondLevel = new ArrayList<LoadTimeLine>();
        for (Entry<Task, List<ResourceAllocation<?>>> entry : byTask.entrySet()) {
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

    public static String getName(Collection<? extends Criterion> criterions,
            Task task) {
        String prefix = task.getName();
        return (prefix + " :: " + Criterion.getCaptionFor(criterions));
    }

    private LoadTimeLine buildTimeLine(Resource resource, String name,
            List<? extends ResourceAllocation<?>> sortedByStartDate,
            String type,
            TimeLineRole<BaseEntity> role) {
        List<LoadPeriod> loadPeriods;
        if(initDateFilter != null || endDateFilter != null) {
            loadPeriods = PeriodsBuilder.build(
                    LoadPeriodGenerator.onResource(resource),
                    sortedByStartDate, asDate(initDateFilter),
                    asDate(endDateFilter));
        }
        else {
            loadPeriods = PeriodsBuilder
                .build(LoadPeriodGenerator.onResource(resource), sortedByStartDate);
        }
        return new LoadTimeLine(name, loadPeriods, type, role);
    }

    private LoadTimeLine buildTimeLine(Criterion criterion, String name,
            String type, List<ResourceAllocation<?>> allocations,
            TimeLineRole<BaseEntity> role) {
        List<GenericResourceAllocation> generics = onlyGeneric(allocations);
        return new LoadTimeLine(name, createPeriods(criterion, generics), type,
                role);
    }

    private LoadTimeLine buildTimeLine(Collection<Criterion> criterions,
            Task task, Criterion criterion, String type,
            List<ResourceAllocation<?>> allocations,
            TimeLineRole<BaseEntity> role) {
        return buildTimeLine(criterion, getName(criterions, task), type,
                allocations,
                role);
    }

    private LoadTimeLine buildTimeLine(Collection<Criterion> criterions,
            Task task, Resource resource, String type,
            List<GenericResourceAllocation> allocationsSortedByStartDate,
            TimeLineRole<BaseEntity> role) {
        LoadPeriodGeneratorFactory periodGeneratorFactory = LoadPeriodGenerator
                .onResourceSatisfying(resource, criterions);
        List<LoadPeriod> loadPeriods;
        if(initDateFilter != null || endDateFilter != null) {
            loadPeriods = PeriodsBuilder.build(periodGeneratorFactory,
                    allocationsSortedByStartDate, asDate(initDateFilter),
                    asDate(endDateFilter));
        }
        else {
            loadPeriods = PeriodsBuilder
                .build(periodGeneratorFactory, allocationsSortedByStartDate);
        }

        return new LoadTimeLine(getName(criterions, task), loadPeriods,
                type, role);
    }

    @Override
    public List<LoadTimeLine> getLoadTimeLines() {
        return loadTimeLines;
    }

    @Override
    public Interval getViewInterval() {
        return viewInterval;
    }

    public ZoomLevel calculateInitialZoomLevel() {
        Interval interval = getViewInterval();
        return ZoomLevel.getDefaultZoomByDates(new LocalDate(interval
                .getStart()), new LocalDate(interval.getFinish()));
    }

    @Override
    public void setResourcesToShow(List<Resource> resourcesList) {
        this.resourcesToShowList.clear();
        this.resourcesToShowList.addAll(Resource.sortByName(resourcesList));

    }

    @Override
    public void clearResourcesToShow() {
        resourcesToShowList.clear();
    }

    private List<Resource> getResourcesToShowReattached() {
        List<Resource> list = new ArrayList<Resource>();
        for(Resource worker : resourcesToShowList) {
            try {
                //for some reason, resourcesDAO.reattach(worker) doesn't work
                //and we have to retrieve them again with find
                list.add(resourcesDAO.find(worker.getId()));
            }
            catch(InstanceNotFoundException e) {
                //maybe it was removed by another transaction
                //we just ignore the exception to not show the Resource
            }
        }
        return list;
    }

    @Override
    public void clearCriteriaToShow() {
        criteriaToShowList.clear();
    }

    @Override
    public void setCriteriaToShow(List<Criterion> criteriaList) {
        criteriaToShowList.clear();
        criteriaToShowList.addAll(criteriaList);
    }

    @Override
    public void setEndDateFilter(LocalDate value) {
        endDateFilter = value;
    }

    @Override
    public void setInitDateFilter(LocalDate value) {
        initDateFilter = value;
    }

    @Override
    public LocalDate getEndDateFilter() {
        return endDateFilter;
    }

    @Override
    public LocalDate getInitDateFilter() {
        return initDateFilter;
    }

    @Transactional(readOnly = true)
    public List<DayAssignment> getDayAssignments() {
        if(filterByResources) {
            return dayAssignmentDAO.findByResources(scenarioManager.getCurrent(),
                    getResources());
        }
        else {
            List<DayAssignment> dayAssignments = new ArrayList<DayAssignment>();
            for (Entry<Criterion, List<ResourceAllocation<?>>> entry : allocationsByCriterion
                    .entrySet()) {

                for (ResourceAllocation<?> allocation : entry.getValue()) {
                    dayAssignments.addAll(allocation.getAssignments());
                }
            }

            return dayAssignments;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resource> getResources() {
        List<Resource> resources;
        if(filterByResources) {
            resources = resourcesToShow();
        }
        else {
            resources = Resource.sortByName(resourcesSearchModel.searchBoth()
                    .byCriteria(criteriaToShow()).execute());
        }
        for (Resource resource : resources) {
            resourcesDAO.reattach(resource);
            ResourceCalendar calendar = resource.getCalendar();
            baseCalendarDAO.reattach(calendar);
            BaseCalendarModel.forceLoadBaseCalendar(calendar);
            resource.getAssignments().size();
        }
        return resources;
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
        for(LoadPeriod loadPeriod : list) {
            LocalDate finalStartDate = loadPeriod.getStart();
            LocalDate finalEndDate = loadPeriod.getEnd();
            if(startDateFilter != null) {
                LocalDate startDateFilterLocalDate = new LocalDate(startDateFilter.getTime());
                if(finalStartDate.compareTo(startDateFilterLocalDate) < 0) {
                    finalStartDate = startDateFilterLocalDate;
                }
            }
            if(endDateFilter != null) {
                LocalDate endDateFilterLocalDate = new LocalDate(endDateFilter.getTime());
                if(loadPeriod.getEnd().compareTo(endDateFilterLocalDate) > 0) {
                    finalEndDate = endDateFilterLocalDate;
                }
            }
            if(finalStartDate.compareTo(finalEndDate) < 0) {
                toReturn.add(new LoadPeriod(finalStartDate, finalEndDate,
                        loadPeriod.getTotalResourceWorkHours(),
                        loadPeriod.getAssignedHours(), loadPeriod.getLoadLevel()));
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
