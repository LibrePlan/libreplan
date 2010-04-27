/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.limitingresources;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.users.daos.IOrderAuthorizationDAO;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.navalplanner.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.data.limitingresource.LimitingResourceQueue;
import org.zkoss.ganttz.data.limitingresource.QueueTask;
import org.zkoss.ganttz.data.resourceload.TimeLineRole;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LimitingResourceQueueModel implements ILimitingResourceQueueModel {

    @Autowired
    private IResourceDAO resourcesDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    private List<LimitingResourceQueue> limitingResourceQueues;
    private Interval viewInterval;

    private Order filterBy;

    private boolean filterByResources = true;

    @Override
    @Transactional(readOnly = true)
    public void initGlobalView(boolean filterByResources) {
        filterBy = null;
        this.filterByResources = filterByResources;
        doGlobalView();
    }

    @Override
    @Transactional(readOnly = true)
    public void initGlobalView(Order filterBy, boolean filterByResources) {
        this.filterBy = orderDAO.findExistingEntity(filterBy.getId());
        this.filterByResources = filterByResources;
        doGlobalView();
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByTask(TaskElement task) {
        return orderElementDAO
                .loadOrderAvoidingProxyFor(task.getOrderElement());
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
        limitingResourceQueues = calculateLimitingResourceQueues();
        if (!limitingResourceQueues.isEmpty()) {
            viewInterval = LimitingResourceQueue
                    .getIntervalFrom(limitingResourceQueues);
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

    private List<LimitingResourceQueue> calculateLimitingResourceQueues() {
        List<LimitingResourceQueue> result = new ArrayList<LimitingResourceQueue>();
        result.addAll(groupsFor(resourcesToShow()));
        return result;
    }

    private Map<Criterion, List<GenericResourceAllocation>> genericAllocationsByCriterion() {
        if (filter()) {
            List<Criterion> criterions = new ArrayList<Criterion>();
            List<GenericResourceAllocation> generics = new ArrayList<GenericResourceAllocation>();
            List<Task> tasks = justTasks(filterBy
                    .getAllChildrenAssociatedTaskElements());
            for (Task task : tasks) {

                List<ResourceAllocation<?>> listAllocations = new ArrayList<ResourceAllocation<?>>(
                        task.getSatisfiedResourceAllocations());
                for (GenericResourceAllocation generic : (onlyGeneric(listAllocations))) {
                    criterions.addAll(generic.getCriterions());
                }
            }
            return resourceAllocationDAO
                    .findGenericAllocationsBySomeCriterion(criterions);
        } else {
            return resourceAllocationDAO.findGenericAllocationsByCriterion();
        }
    }

    private List<Resource> resourcesToShow() {
        if (filter()) {
            return resourcesForActiveTasks();
        } else {
            return allLimitingResources();
        }
    }

    private boolean filter() {
        return filterBy != null;
    }

    private List<Resource> resourcesForActiveTasks() {
        return Resource.sortByName(resourcesDAO
                .findResourcesRelatedTo(justTasks(filterBy
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

    private List<Resource> allLimitingResources() {
        return Resource.sortByName(resourcesDAO.getAllLimitingResources());
    }

    private TimeLineRole<BaseEntity> getCurrentTimeLineRole(BaseEntity entity) {
        return new TimeLineRole<BaseEntity>(entity);
    }

    private List<LimitingResourceQueue> groupsFor(List<Resource> allResources) {
        List<LimitingResourceQueue> result = new ArrayList<LimitingResourceQueue>();
        for (Resource resource : allResources) {
            LimitingResourceQueue group = buildGroup(resource);
            if (!group.isEmpty()) {
                result.add(group);
            }
        }
        return result;
    }

    private LimitingResourceQueue buildGroup(Resource resource) {
        List<ResourceAllocation<?>> sortedByStartDate = ResourceAllocation
                .sortedByStartDate(resourceAllocationDAO
                        .findAllocationsRelatedTo(resource));
        TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(resource);
        LimitingResourceQueue result = new LimitingResourceQueue(buildTimeLine(
                resource, resource.getShortDescription(), sortedByStartDate,
                "resource", role),
                buildSecondLevel(resource, sortedByStartDate));
        return result;
    }

    private LimitingResourceQueue buildTimeLine(Resource resource, String name,
            List<? extends ResourceAllocation<?>> sortedByStartDate,
            String type, TimeLineRole<BaseEntity> role) {
        return new LimitingResourceQueue(name, PeriodsBuilder.build(
                QueueTaskGenerator.onResource(resource), sortedByStartDate),
                type, role);
    }

    private List<LimitingResourceQueue> buildSecondLevel(Resource resource,
            List<ResourceAllocation<?>> sortedByStartDate) {
        List<LimitingResourceQueue> result = new ArrayList<LimitingResourceQueue>();
        Map<Order, List<ResourceAllocation<?>>> byOrder = byOrder(sortedByStartDate);

        if (filter()) {
            // build time lines for current order
            result.addAll(buildTimeLinesForOrder(resource, byOrder
                    .get(filterBy)));
            byOrder.remove(filterBy);
            // build time lines for other orders
            LimitingResourceQueue lineOthersOrders = buildTimeLinesForOtherOrders(
                    resource, byOrder);
            if (lineOthersOrders != null) {
                result.add(lineOthersOrders);
            }
        } else {
            // result.addAll(buildTimeLinesGroupForOrder(resource, byOrder));
        }
        return result;
    }

    private LimitingResourceQueue buildTimeLinesForOtherOrders(
            Resource resource, Map<Order, List<ResourceAllocation<?>>> byOrder) {
        // TODO Auto-generated method stub
        return null;
    }

    private List<LimitingResourceQueue> buildTimeLinesForEachTask(
            Criterion criterion,
            List<ResourceAllocation<?>> allocations) {
        Map<Task, List<ResourceAllocation<?>>> byTask = ResourceAllocation
                .byTask(allocations);

        List<LimitingResourceQueue> secondLevel = new ArrayList<LimitingResourceQueue>();
        for (Entry<Task, List<ResourceAllocation<?>>> entry : byTask.entrySet()) {
            Task task = entry.getKey();
            Set<Criterion> criterions = task.getCriterions();
            TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(task);

            /**
             * Each resource line has the same role than its allocated task, so
             * that link with the resource allocation screen
             */

        }
        return secondLevel;
    }

    private String getDescriptionResourceWithCriterions(Resource resource) {
        Set<CriterionSatisfaction> criterionSatisfactions = resource
                .getCriterionSatisfactions();
        return resource.getShortDescription()
                + getCriterionSatisfactionDescription(criterionSatisfactions);
    }

    private String getCriterionSatisfactionDescription(
            Set<CriterionSatisfaction> satisfactions) {
        if (satisfactions.isEmpty()) {
            return _("");
        }
        List<Criterion> criterions = new ArrayList<Criterion>();
        for (CriterionSatisfaction satisfaction : satisfactions) {
            criterions.add(satisfaction.getCriterion());
        }
        return " :: " + getName(criterions);
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

    @Transactional(readOnly = true)
    public Map<Order, List<ResourceAllocation<?>>> byOrder(
            Collection<ResourceAllocation<?>> allocations) {
        Map<Order, List<ResourceAllocation<?>>> result = new HashMap<Order, List<ResourceAllocation<?>>>();
        for (ResourceAllocation<?> resourceAllocation : allocations) {
            if ((resourceAllocation.isSatisfied())
                    && (resourceAllocation.getTask() != null)) {
                OrderElement orderElement = resourceAllocation.getTask()
                        .getOrderElement();
                Order order = orderElementDAO
                        .loadOrderAvoidingProxyFor(orderElement);
                initializeIfNeeded(result, order);
                result.get(order).add(resourceAllocation);
            }
        }
        return result;
    }

    private List<LimitingResourceQueue> buildTimeLinesForOrder(
            Resource resource,
            List<ResourceAllocation<?>> sortedByStartDate) {
        List<LimitingResourceQueue> result = new ArrayList<LimitingResourceQueue>();
        // Add all time lines
        // result.addAll(buildTimeLinesForEachTask(resource,
        // onlySpecific(sortedByStartDate)));
        // result.addAll(buildTimeLinesForEachCriterion(resource,
        // onlyGeneric(sortedByStartDate)));
        return result;
    }

    private List<GenericResourceAllocation> onlyGeneric(
            List<ResourceAllocation<?>> sortedByStartDate) {
        return ResourceAllocation.getOfType(GenericResourceAllocation.class,
                sortedByStartDate);
    }

    private List<SpecificResourceAllocation> onlySpecific(
            List<ResourceAllocation<?>> sortedByStartDate) {
        return ResourceAllocation.getOfType(SpecificResourceAllocation.class,
                sortedByStartDate);
    }

    private List<LimitingResourceQueue> buildTimeLinesForEachCriterion(
            Resource resource, List<GenericResourceAllocation> sortdByStartDate) {
        Map<Set<Criterion>, List<GenericResourceAllocation>> byCriterions = GenericResourceAllocation
                .byCriterions(sortdByStartDate);

        List<LimitingResourceQueue> result = new ArrayList<LimitingResourceQueue>();
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
            }
        }
        return result;
    }

    private List<LimitingResourceQueue> buildTimeLinesForEachTask(
            Resource resource,
            List<SpecificResourceAllocation> sortedByStartDate) {

        List<ResourceAllocation<?>> listOnlySpecific = new ArrayList<ResourceAllocation<?>>(
                sortedByStartDate);
        Map<Task, List<ResourceAllocation<?>>> byTask = ResourceAllocation
                .byTask(listOnlySpecific);

        List<LimitingResourceQueue> secondLevel = new ArrayList<LimitingResourceQueue>();
        for (Entry<Task, List<ResourceAllocation<?>>> entry : byTask.entrySet()) {
            Task task = entry.getKey();
            TimeLineRole<BaseEntity> role = getCurrentTimeLineRole(task);

        }
        return secondLevel;
    }

    public static String getName(Collection<? extends Criterion> criterions,
            Task task) {
        String prefix = task.getName();
        return (prefix + " :: " + getName(criterions));
    }

    public static String getName(Collection<? extends Criterion> criterions) {
        if (criterions.isEmpty()) {
            return _("[generic all workers]");
        }
        String[] names = new String[criterions.size()];
        int i = 0;
        for (Criterion criterion : criterions) {
            names[i++] = criterion.getName();
        }
        return (Arrays.toString(names));
    }


    @Override
    public List<LimitingResourceQueue> getLimitingResourceQueues() {
        return limitingResourceQueues;
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


}

class PeriodsBuilder {

    private final List<? extends ResourceAllocation<?>> sortedByStartDate;

    private final List<QueueTaskGenerator> loadPeriodsGenerators = new LinkedList<QueueTaskGenerator>();

    private final QueueTaskGeneratorFactory factory;

    private PeriodsBuilder(QueueTaskGeneratorFactory factory,
            List<? extends ResourceAllocation<?>> sortedByStartDate) {
        this.factory = factory;
        this.sortedByStartDate = sortedByStartDate;
    }

    public static List<QueueTask> build(QueueTaskGeneratorFactory factory,
            List<? extends ResourceAllocation<?>> sortedByStartDate) {
        return new PeriodsBuilder(factory, sortedByStartDate).buildPeriods();
    }

    private List<QueueTask> buildPeriods() {
        for (ResourceAllocation<?> resourceAllocation : sortedByStartDate) {
            loadPeriodsGenerators.add(factory.create(resourceAllocation));
        }
        joinPeriodGenerators();
        return toGenerators(loadPeriodsGenerators);
    }

    private List<QueueTask> toGenerators(List<QueueTaskGenerator> generators) {
        List<QueueTask> result = new ArrayList<QueueTask>();
        for (QueueTaskGenerator queueTaskGenerator : generators) {
            result.add(queueTaskGenerator.build());
        }
        return result;
    }

    private void joinPeriodGenerators() {
        ListIterator<QueueTaskGenerator> iterator = loadPeriodsGenerators
                .listIterator();
        while (iterator.hasNext()) {
            final QueueTaskGenerator current = findNextOneOverlapping(iterator);
            if (current != null) {
                rewind(iterator, current);
                iterator.remove();
                QueueTaskGenerator next = iterator.next();
                iterator.remove();
                List<QueueTaskGenerator> generated = current.join(next);
                final QueueTaskGenerator positionToComeBack = generated.get(0);
                final List<QueueTaskGenerator> remaining = loadPeriodsGenerators
                        .subList(iterator.nextIndex(), loadPeriodsGenerators
                                .size());
                List<QueueTaskGenerator> generatorsSortedByStartDate = mergeListsKeepingByStartSortOrder(
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

    private QueueTaskGenerator findNextOneOverlapping(
            ListIterator<QueueTaskGenerator> iterator) {
        while (iterator.hasNext()) {
            QueueTaskGenerator current = iterator.next();
            if (!iterator.hasNext()) {
                return null;
            }
            QueueTaskGenerator next = peekNext(iterator);
            if (current.overlaps(next)) {
                return current;
            }
        }
        return null;
    }

    private void addAtCurrentPosition(
            ListIterator<QueueTaskGenerator> iterator,
            List<QueueTaskGenerator> sortedByStartDate) {
        for (QueueTaskGenerator l : sortedByStartDate) {
            iterator.add(l);
        }
    }

    private void removeNextElements(ListIterator<QueueTaskGenerator> iterator,
            final int elementsNumber) {
        for (int i = 0; i < elementsNumber; i++) {
            iterator.next();
            iterator.remove();
        }
    }

    private void rewind(ListIterator<QueueTaskGenerator> iterator,
            QueueTaskGenerator nextOne) {
        while (peekNext(iterator) != nextOne) {
            iterator.previous();
        }
    }

    private List<QueueTaskGenerator> mergeListsKeepingByStartSortOrder(
            List<QueueTaskGenerator> joined, List<QueueTaskGenerator> remaining) {
        List<QueueTaskGenerator> result = new ArrayList<QueueTaskGenerator>();
        ListIterator<QueueTaskGenerator> joinedIterator = joined.listIterator();
        ListIterator<QueueTaskGenerator> remainingIterator = remaining
                .listIterator();
        while (joinedIterator.hasNext() && remainingIterator.hasNext()) {
            QueueTaskGenerator fromJoined = peekNext(joinedIterator);
            QueueTaskGenerator fromRemaining = peekNext(remainingIterator);
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

    private QueueTaskGenerator peekNext(
            ListIterator<QueueTaskGenerator> iterator) {
        if (!iterator.hasNext()) {
            return null;
        }
        QueueTaskGenerator result = iterator.next();
        iterator.previous();
        return result;
    }

}
