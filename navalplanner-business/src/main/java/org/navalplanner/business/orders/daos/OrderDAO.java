/*
 * This file is part of NavalPlan
 * 
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 * Desenvolvemento Tecnolóxico de Galicia
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.orders.daos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.daos.IntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.costcategories.daos.CostCategoryDAO;
import org.navalplanner.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.reports.dtos.OrderCostsPerResourceDTO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.users.daos.IOrderAuthorizationDAO;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.navalplanner.business.users.entities.OrderAuthorizationType;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
/**
 * Dao for {@link Order}
 * 
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderDAO extends IntegrationEntityDAO<Order> implements
        IOrderDAO {

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Override
    public List<Order> getOrders() {
        return list(Order.class);
    }

    @Override
    public void remove(Long id) throws InstanceNotFoundException {
        Order order = find(id);
        OrderElementDAO.removeTaskSourcesFor(taskSourceDAO, order);
        super.remove(id);
    }

    private boolean isOrderNameContained(String code, List<Order> orders) {
        for (Order each : orders) {
                if (each.getCode().equals(code)) {
                    return true;
                }
        }
        return false;
    }

    private boolean matchFilterCriterion(OrderElement orderElement,
            List<Criterion> criterions) {
        if ((criterions != null) && (!criterions.isEmpty())) {
            List<OrderElement> orderElements = new ArrayList<OrderElement>();
            orderElements.add(orderElement);
            List<Task> tasks = this.getFilteredTask(orderElements, criterions);
            return (!tasks.isEmpty());
        }
        return true;
    }

    @Transactional(readOnly = true)
    public List<OrderCostsPerResourceDTO> getOrderCostsPerResource(
            List<Order> orders, Date startingDate, Date endingDate,
            List<Criterion> criterions) {

        String orderStrQuery = "FROM Order order ";
        Query orderQuery = getSession().createQuery(orderStrQuery);
        List<Order> orderList = orderQuery.list();

        String strQuery = "SELECT new org.navalplanner.business.reports.dtos.OrderCostsPerResourceDTO(worker, wrl) "
                + "FROM Worker worker, WorkReportLine wrl "
                + "LEFT OUTER JOIN wrl.resource resource "
                + "WHERE resource.id = worker.id ";

        // Set date range
        if (startingDate != null && endingDate != null) {
            strQuery += "AND wrl.date BETWEEN :startingDate AND :endingDate ";
        }
        if (startingDate != null && endingDate == null) {
            strQuery += "AND wrl.date >= :startingDate ";
        }
        if (startingDate == null && endingDate != null) {
            strQuery += "AND wrl.date <= :endingDate ";
        }

        // Order by
        strQuery += "ORDER BY worker.id, wrl.date";

        Query query = getSession().createQuery(strQuery);

        if (startingDate != null) {
            query.setParameter("startingDate", startingDate);
        }
        if (endingDate != null) {
            query.setParameter("endingDate", endingDate);
        }

        List<OrderCostsPerResourceDTO> list = query.list();

        List<OrderCostsPerResourceDTO> filteredList = new ArrayList<OrderCostsPerResourceDTO>();
        for (OrderCostsPerResourceDTO each : list) {

            Order order = each.getOrderElement().getOrder();

            // Apply filtering
            if (matchFilterCriterion(each.getOrderElement(), criterions)
                    && (orders.isEmpty() || isOrderNameContained(order
                            .getCode(), orders))) {

                // Attach ordername value
                each.setOrderName(order.getName());
                // Attach calculated pricePerHour
                BigDecimal pricePerHour = CostCategoryDAO
                        .getPriceByResourceDateAndHourType(each.getWorker(),
                                new LocalDate(each.getDate()), each
                                        .getHoursTypeCode());
                if (pricePerHour == null) {
                    for (TypeOfWorkHours defaultprice : typeOfWorkHoursDAO
                            .list(TypeOfWorkHours.class)) {
                        if (defaultprice.getCode().equals(
                                each.getHoursTypeCode())) {
                            pricePerHour = defaultprice.getDefaultPrice();
                        }
                    }
                }

                each.setCostPerHour(pricePerHour);
                each.setCost(each.getCostPerHour().multiply(
                        new BigDecimal(each.getNumHours())));
                filteredList.add(each);
            }
        }
        return filteredList;
    }

    @Override
    public List<Order> getOrdersByReadAuthorization(User user) {
        if (user.isInRole(UserRole.ROLE_READ_ALL_ORDERS) ||
            user.isInRole(UserRole.ROLE_EDIT_ALL_ORDERS)) {
            return getOrders();
        }
        else {
            List<Order> orders = new ArrayList<Order>();
            List<OrderAuthorization> authorizations = orderAuthorizationDAO.listByUserAndItsProfiles(user);
            for(OrderAuthorization authorization : authorizations) {
                if (authorization.getAuthorizationType() == OrderAuthorizationType.READ_AUTHORIZATION ||
                    authorization.getAuthorizationType() == OrderAuthorizationType.WRITE_AUTHORIZATION) {

                    Order order = authorization.getOrder();
                    if(!orders.contains(order)) {
                        order.getName(); //this lines forces the load of the basic attributes of the order
                        orders.add(order);
                    }
                }
            }
            return orders;
        }
    }

    @Override
    public List<Order> getOrdersByWriteAuthorization(User user) {
        if (user.isInRole(UserRole.ROLE_EDIT_ALL_ORDERS)) {
            return getOrders();
        }
        else {
            List<Order> orders = new ArrayList<Order>();
            List<OrderAuthorization> authorizations = orderAuthorizationDAO.listByUserAndItsProfiles(user);
            for(OrderAuthorization authorization : authorizations) {
                if (authorization.getAuthorizationType() == OrderAuthorizationType.WRITE_AUTHORIZATION) {
                    Order order = authorization.getOrder();
                    if(!orders.contains(order)) {
                        order.getName(); //this lines forces the load of the basic attributes of the order
                        orders.add(order);
                    }
                }
            }
            return orders;
        }
    }

    @Override
    public List<Order> findAll() {
        return getSession().createCriteria(getEntityClass()).addOrder(
                org.hibernate.criterion.Order.asc("infoComponent.code")).list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Order findByCode(String code) throws InstanceNotFoundException {

        if (StringUtils.isBlank(code)) {
            throw new InstanceNotFoundException(null, getEntityClass()
                    .getName());
        }

        Order entity = (Order) getSession().createCriteria(getEntityClass())
                .add(
                        Restrictions.eq("infoComponent.code", code.trim())
                                .ignoreCase()).uniqueResult();

        if (entity == null) {
            throw new InstanceNotFoundException(code, getEntityClass()
                    .getName());
        } else {
            return entity;
        }

    }

    @Override
    public List<Order> getOrdersByReadAuthorizationByScenario(User user,
            Scenario scenario) {
        return existsInScenario(getOrdersByReadAuthorization(user), scenario);
    }

    private List<Order> existsInScenario(List<Order> orders, Scenario scenario) {
        List<Order> result = new ArrayList<Order>();
        for (Order each : orders) {
            if (scenario.contains(each)) {
                result.add(each);
            }
        }
        return result;
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Order findByNameAnotherTransaction(String name)
            throws InstanceNotFoundException {

        return findByName(name);

    }

    @SuppressWarnings("unchecked")
    private Order findByName(String name) throws InstanceNotFoundException {

        if (StringUtils.isBlank(name)) {
            throw new InstanceNotFoundException(null,
                getEntityClass().getName());
        }

        Order order = (Order) getSession().createCriteria(getEntityClass())
                .add(
                        Restrictions.ilike("infoComponent.name", name,
                                MatchMode.EXACT))
                .uniqueResult();

        if (order == null) {
            throw new InstanceNotFoundException(
                name, getEntityClass().getName());
        } else {
            return order;
        }

    }

    @Override
    public List<Order> getOrdersByScenario(Scenario scenario) {
        return existsInScenario(getOrders(), scenario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getFilteredTask(List<OrderElement> orderElements,
            List<Criterion> criterions) {


        String strQuery = "SELECT taskSource.task "
                + "FROM OrderElement orderElement, TaskSource taskSource, Task task "
                + "LEFT OUTER JOIN taskSource.schedulingData.orderElement taskSourceOrderElement "
                + "LEFT OUTER JOIN taskSource.task taskElement "
                + "WHERE taskSourceOrderElement.id = orderElement.id "
                + "AND taskElement.id = task.id ";

        if (orderElements != null && !orderElements.isEmpty()) {
            strQuery += " AND orderElement IN (:orderElements) ";
        } else {
            return new ArrayList<Task>();
        }

        // Set Criterions
        if (criterions != null && !criterions.isEmpty()) {
            strQuery += " AND (EXISTS (FROM task.resourceAllocations as allocation, GenericResourceAllocation as generic "
                    + " WHERE generic.id = allocation.id "
                    + " AND EXISTS( FROM generic.criterions criterion WHERE criterion IN (:criterions))))";
        }

        // Order by
        strQuery += "ORDER BY task.name";

        // Set parameters
        Query query = getSession().createQuery(strQuery);
        if (orderElements != null && !orderElements.isEmpty()) {
            query.setParameterList("orderElements", orderElements);
        }

        if (criterions != null && !criterions.isEmpty()) {
            query.setParameterList("criterions", criterions);
        }

        // Get result
        return query.list();
    }
}
