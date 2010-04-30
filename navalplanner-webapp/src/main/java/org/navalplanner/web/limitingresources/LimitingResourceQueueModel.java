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
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.ILimitingResourceQueueElementDAO;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
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
import org.zkoss.ganttz.data.resourceload.TimeLineRole;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

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
    private IResourceAllocationDAO resourceAllocationDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    private ILimitingResourceQueueElementDAO limitingResourceQueueElementDAO;

    private List<LimitingResourceQueue> limitingResourceQueues = new ArrayList<LimitingResourceQueue>();

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
            // Build interval
            // viewInterval =
            // LimitingResourceQueue.getIntervalFrom(limitingResourceQueues);
            viewInterval = new Interval(new Date(), plusFiveYears(new Date()));
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
        List<Resource> result = Resource.sortByName(resourcesDAO
                .getAllLimitingResources());
        for (Resource each : result) {
            limitingResourceQueues.add(each.getLimitingResourceQueue());
        }
        return result;
    }

    private TimeLineRole<BaseEntity> getCurrentTimeLineRole(BaseEntity entity) {
        return new TimeLineRole<BaseEntity>(entity);
    }

    private List<LimitingResourceQueue> groupsFor(List<Resource> allResources) {
        List<LimitingResourceQueue> result = new ArrayList<LimitingResourceQueue>();
        for (Resource resource : allResources) {
            LimitingResourceQueue group = resource.getLimitingResourceQueue();
            result.add(group);
        }
        return result;
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


    private List<GenericResourceAllocation> onlyGeneric(
            List<ResourceAllocation<?>> sortedByStartDate) {
        return ResourceAllocation.getOfType(GenericResourceAllocation.class,
                sortedByStartDate);
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

    @Override
    @Transactional(readOnly=true)
    public List<LimitingResourceQueueElement> getUnassignedLimitingResourceQueueElements() {
        List<LimitingResourceQueueElement> result = limitingResourceQueueElementDAO
                .getUnassigned();
        for (LimitingResourceQueueElement each : result) {
            limitingResourceQueueElementDAO.reattach(each);
            each.getResourceAllocation().getTask().getName();
        }
        return result;
    }

}
