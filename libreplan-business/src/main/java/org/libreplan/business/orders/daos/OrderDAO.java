/*
 * This file is part of LibrePlan
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

package org.libreplan.business.orders.daos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.daos.CostCategoryDAO;
import org.libreplan.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderStatusEnum;
import org.libreplan.business.orders.entities.SchedulingState;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.reports.dtos.CostExpenseSheetDTO;
import org.libreplan.business.reports.dtos.OrderCostsPerResourceDTO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.users.daos.IOrderAuthorizationDAO;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.OrderAuthorization;
import org.libreplan.business.users.entities.OrderAuthorizationType;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link Order}.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderDAO extends IntegrationEntityDAO<Order> implements IOrderDAO {

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    private IOrderAuthorizationDAO orderAuthorizationDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private String STATE_PARAMETER = "state";

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

    private boolean isOrderContained(Order order, List<Order> orders) {
        for (Order each : orders) {
            if (each.getId().equals(order.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchFilterCriterion(OrderElement orderElement, List<Criterion> criterions) {
        if ((criterions != null) && (!criterions.isEmpty())) {

            List<OrderElement> orderElements = new ArrayList<>();
            orderElements.add(orderElement);
            List<Task> tasks = this.getFilteredTask(orderElements, criterions);

            return !tasks.isEmpty();
        }
        return true;
    }

    @Transactional(readOnly = true)
    public List<OrderCostsPerResourceDTO> getOrderCostsPerResource(
            List<Order> orders,
            Date startingDate,
            Date endingDate,
            List<Criterion> criterions) {

        String strQuery = "SELECT new org.libreplan.business.reports.dtos.OrderCostsPerResourceDTO(worker, wrl) " +
                "FROM Worker worker, WorkReportLine wrl " +
                "LEFT OUTER JOIN wrl.resource resource " +
                "WHERE resource.id = worker.id ";

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

        List<OrderCostsPerResourceDTO> filteredList = new ArrayList<>();
        for (OrderCostsPerResourceDTO each : list) {

            Order order = loadOrderAvoidingProxyFor(each.getOrderElement());

            // Apply filtering
            if (matchFilterCriterion(each.getOrderElement(), criterions) && isOrderContained(order, orders)) {

                // Attach orderName value
                each.setOrderName(order.getName());
                each.setOrderCode(order.getCode());

                // Attach calculated pricePerHour
                BigDecimal pricePerHour = CostCategoryDAO.getPriceByResourceDateAndHourType(
                        each.getWorker(), new LocalDate(each.getDate()), each.getHoursTypeCode());

                if (pricePerHour == null) {
                    for (TypeOfWorkHours defaultprice : typeOfWorkHoursDAO.list(TypeOfWorkHours.class)) {
                        if (defaultprice.getCode().equals(each.getHoursTypeCode())) {
                            pricePerHour = defaultprice.getDefaultPrice();
                        }
                    }
                }

                each.setCostPerHour(pricePerHour);
                each.setCost(each.getCostPerHour().multiply(each.getNumHours()));
                filteredList.add(each);
            }
        }
        return filteredList;
    }

    @Override
    public List<Order> getOrdersByReadAuthorization(User user) {
        if (user.isInRole(UserRole.ROLE_SUPERUSER) ||
                user.isInRole(UserRole.ROLE_READ_ALL_PROJECTS) ||
                user.isInRole(UserRole.ROLE_EDIT_ALL_PROJECTS)) {

            return getOrders();
        }
        else {
            List<Order> orders = new ArrayList<>();
            List<OrderAuthorization> authorizations = orderAuthorizationDAO.listByUserAndItsProfiles(user);
            for(OrderAuthorization authorization : authorizations) {

                if (authorization.getAuthorizationType() == OrderAuthorizationType.READ_AUTHORIZATION ||
                        authorization.getAuthorizationType() == OrderAuthorizationType.WRITE_AUTHORIZATION) {

                    Order order = authorization.getOrder();
                    if (!orders.contains(order)) {
                        // These lines forces the load of the basic attributes of the order
                        order.getName();
                        orders.add(order);
                    }
                }
            }
            return orders;
        }
    }

    private List<Order> getOrdersByReadAuthorizationBetweenDatesByLabelsCriteriaCustomerAndState(
            User user,
            Date startDate,
            Date endDate,
            List<Label> labels,
            List<Criterion> criteria,
            ExternalCompany customer,
            OrderStatusEnum state,
            Boolean excludeFinishedProject) {

        List<Long> ordersIdsFiltered = getOrdersIdsFiltered(user, labels, criteria, customer, state, excludeFinishedProject);
        if (ordersIdsFiltered != null && ordersIdsFiltered.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ordersIdsByDates = getOrdersIdsByDates(startDate, endDate);
        if (ordersIdsByDates != null && ordersIdsByDates.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ordersIdsUnscheduled = getOrdersIdsUnscheduled(startDate, endDate);

        Criteria c = getSession().createCriteria(Order.class);

        if (ordersIdsFiltered != null && ordersIdsByDates != null) {

            org.hibernate.criterion.Criterion and = Restrictions.and(
                    Restrictions.in("id", ordersIdsFiltered),
                    Restrictions.in("id", ordersIdsByDates));

            c.add(and);
        } else {
            if (ordersIdsFiltered != null) {
                c.add(Restrictions.in("id", ordersIdsFiltered));
            }

            if (ordersIdsByDates != null) {
                if (ordersIdsUnscheduled.isEmpty()) {
                    c.add(Restrictions.in("id", ordersIdsByDates));
                } else {
                    c.add(Restrictions.or(
                            Restrictions.in("id", ordersIdsByDates),
                            Restrictions.in("id", ordersIdsUnscheduled)));
                }
            }
        }

        c.addOrder(org.hibernate.criterion.Order.desc("initDate"));
        c.addOrder(org.hibernate.criterion.Order.asc("infoComponent.name"));

        return c.list();
    }

    private List<Long> getOrdersIdsUnscheduled(Date startDate, Date endDate) {
        String strQuery = "SELECT s.orderElement.id " +
                "FROM SchedulingDataForVersion s " +
                "WHERE s.schedulingStateType = :type";

        Query query = getSession().createQuery(strQuery);
        query.setParameter("type", SchedulingState.Type.NO_SCHEDULED);

        List<Long> ordersIdsUnscheduled = query.list();
        if (ordersIdsUnscheduled.isEmpty()) {
            return Collections.emptyList();
        }

        String strQueryDates = "SELECT o.id " + "FROM Order o " + "WHERE o.id IN (:ids) ";

        if (startDate != null) {
            strQueryDates += "AND o.initDate >= :startDate ";
        }
        if (endDate != null) {
            strQueryDates += "AND o.initDate <= :endDate ";
        }

        Query queryDates = getSession().createQuery(strQueryDates);
        if (startDate != null) {
            queryDates.setParameter("startDate", startDate);
        }
        if (endDate != null) {
            queryDates.setParameter("endDate", endDate);
        }
        queryDates.setParameterList("ids", ordersIdsUnscheduled);

        return queryDates.list();
    }

    /**
     * If both params are <code>null</code> it returns <code>null</code>.
     * Otherwise it filters the list of tasks to return the ones without parent between the dates.
     */
    private List<Long> getOrdersIdsByDates(Date startDate, Date endDate) {
        if (startDate == null && endDate == null) {
            /* Don't replace null with Collections.emptyList(), as the prompt says (sometimes), because it breaks logic */
            return null;
        }

        String strQuery = "SELECT t.taskSource.schedulingData.orderElement.id "
                + "FROM TaskElement t "
                + "WHERE t.parent IS NULL ";

        if (endDate != null) {
            strQuery += "AND t.startDate.date <= :endDate ";
        }

        if (startDate != null) {
            strQuery += "AND t.endDate.date >= :startDate ";
        }

        Query query = getSession().createQuery(strQuery);

        if (startDate != null) {
            query.setParameter("startDate", LocalDate.fromDateFields(startDate));
        }

        if (endDate != null) {
            query.setParameter("endDate", LocalDate.fromDateFields(endDate));
        }

        return query.list();
    }

    /**
     * If user has permissions over all orders and not filters are passed it returns <code>null</code>.
     * Otherwise, it returns the list of orders
     * identifiers for which the user has read permissions and the filters pass.
     */
    private List<Long> getOrdersIdsFiltered(User user,
                                            List<Label> labels,
                                            List<Criterion> criteria,
                                            ExternalCompany customer,
                                            OrderStatusEnum state,
                                            Boolean excludeFinishedProject) {

        List<Long> ordersIdsByReadAuthorization = getOrdersIdsByReadAuthorization(user);

        String strQuery = "SELECT o.id ";
        strQuery += "FROM Order o ";

        String where = "";
        String whereFinal = "";
        if (labels != null && !labels.isEmpty()) {
            for (int i = 0; i < labels.size(); i++) {
                if (where.isEmpty()) {
                    where += "WHERE ";
                } else {
                    where += "AND ";
                }
                where += ":label" + i + " IN elements(o.labels) ";
            }
        }

        if (criteria != null && !criteria.isEmpty()) {
            strQuery += "JOIN o.criterionRequirements cr ";
            if (where.isEmpty()) {
                where += "WHERE ";
            } else {
                where += "AND ";
            }
            where += "cr.criterion IN (:criteria) ";
            where += "AND cr.class = DirectCriterionRequirement ";
            whereFinal += "GROUP BY o.id ";
            whereFinal += "HAVING count(o.id) = :criteriaSize ";
        }

        if (customer != null) {
            if (where.isEmpty()) {
                where += "WHERE ";
            } else {
                where += "AND ";
            }
            where += "o.customer = :customer ";
        }

        if (state != null) {
            if (where.isEmpty()) {
                where += "WHERE ";
            } else {
                where += "AND ";
            }
            where += "o.state = :state ";
        }

        if (excludeFinishedProject != null && excludeFinishedProject == true) {
            if (where.isEmpty()) {
                where += "WHERE ";
            } else {
                where += "AND ";
            }
            where += "o.state <> '" + OrderStatusEnum.FINISHED.getIndex() + "'";
        }

        // If not restrictions by labels, criteria, customer or state
        if (where.isEmpty()) {
            return ordersIdsByReadAuthorization;
        }

        if (ordersIdsByReadAuthorization != null && !ordersIdsByReadAuthorization.isEmpty()) {
            if (where.isEmpty()) {
                where += "WHERE ";
            } else {
                where += "AND ";
            }
            where += "o.id IN (:ids) ";
        }

        strQuery += where + whereFinal;
        Query query = getSession().createQuery(strQuery);

        if (labels != null && !labels.isEmpty()) {
            int i = 0;
            for (Label label : labels) {
                query.setParameter("label" + i, label);
                i++;
            }
        }

        if (criteria != null && !criteria.isEmpty()) {
            query.setParameterList("criteria", criteria);
            query.setParameter("criteriaSize", (long) criteria.size());
        }

        if (customer != null) {
            query.setParameter("customer", customer);
        }

        if (state != null) {
            query.setParameter(STATE_PARAMETER, state);
        }

        if (ordersIdsByReadAuthorization != null && !ordersIdsByReadAuthorization.isEmpty()) {
            query.setParameterList("ids", ordersIdsByReadAuthorization);
        }

        return query.list();
    }

    /**
     * If user has permissions over all orders it returns <code>null</code>.
     * Otherwise, it returns the list of orders identifiers for which the user has read permissions.
     */
    private List<Long> getOrdersIdsByReadAuthorization(User user) {
        if (user.isInRole(UserRole.ROLE_SUPERUSER) ||
                user.isInRole(UserRole.ROLE_READ_ALL_PROJECTS) ||
                user.isInRole(UserRole.ROLE_EDIT_ALL_PROJECTS)) {

            return null;
        } else {
            String strQuery = "SELECT oa.order.id " +
                    "FROM OrderAuthorization oa " +
                    "WHERE oa.user = :user ";

            if (!user.getProfiles().isEmpty()) {
                strQuery += "OR oa.profile IN (:profiles) ";
            }

            Query query = getSession().createQuery(strQuery);
            query.setParameter("user", user);
            if (!user.getProfiles().isEmpty()) {
                query.setParameterList("profiles", user.getProfiles());
            }

            return query.list();
        }
    }

    @Override
    public List<Order> getOrdersByWriteAuthorization(User user) {
        if (user.isInRole(UserRole.ROLE_SUPERUSER) || user.isInRole(UserRole.ROLE_EDIT_ALL_PROJECTS)) {
            return getOrders();
        }
        else {
            List<Order> orders = new ArrayList<>();
            List<OrderAuthorization> authorizations = orderAuthorizationDAO.listByUserAndItsProfiles(user);
            for(OrderAuthorization authorization : authorizations) {
                if (authorization.getAuthorizationType() == OrderAuthorizationType.WRITE_AUTHORIZATION) {
                    Order order = authorization.getOrder();
                    if (!orders.contains(order)) {
                        order.getName(); // this lines forces the load of the basic attributes of the order
                        orders.add(order);
                    }
                }
            }
            return orders;
        }
    }

    @Override
    public List<Order> findAll() {
        return getSession()
                .createCriteria(getEntityClass())
                .addOrder(org.hibernate.criterion.Order.asc("infoComponent.code"))
                .list();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public Order findByCode(String code) throws InstanceNotFoundException {

        if (StringUtils.isBlank(code)) {
            throw new InstanceNotFoundException(null, getEntityClass().getName());
        }

        Order entity = (Order) getSession()
                .createCriteria(getEntityClass())
                .add(Restrictions.eq("infoComponent.code", code.trim()).ignoreCase())
                .uniqueResult();

        if (entity == null) {
            throw new InstanceNotFoundException(code, getEntityClass().getName());
        } else {
            return entity;
        }

    }

    @Override
    public List<Order> getOrdersByReadAuthorizationByScenario(String username, Scenario scenario) {
        User user;
        try {
            user = userDAO.findByLoginName(username);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        return existsInScenario(getOrdersByReadAuthorization(user), scenario);
    }

    @Override
    public List<Order> getOrdersByReadAuthorizationBetweenDatesByLabelsCriteriaCustomerAndState(
            String username,
            Scenario scenario,
            Date startDate,
            Date endDate,
            List<Label> labels,
            List<Criterion> criteria,
            ExternalCompany customer,
            OrderStatusEnum state,
            Boolean excludeFinishedProject) {

        User user;
        try {
            user = userDAO.findByLoginName(username);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        return existsInScenario(getOrdersByReadAuthorizationBetweenDatesByLabelsCriteriaCustomerAndState(
                user, startDate, endDate, labels, criteria, customer, state, excludeFinishedProject), scenario);
    }

    private List<Order> existsInScenario(List<Order> orders, Scenario scenario) {
        List<Order> result = new ArrayList<>();
        for (Order each : orders) {
            if (scenario.contains(each)) {
                result.add(each);
            }
        }
        return result;
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Order findByNameAnotherTransaction(String name) throws InstanceNotFoundException {
        return findByName(name);
    }

    @SuppressWarnings("unchecked")
    private Order findByName(String name) throws InstanceNotFoundException {

        if (StringUtils.isBlank(name)) {
            throw new InstanceNotFoundException(null, getEntityClass().getName());
        }

        Order order = (Order) getSession()
                .createCriteria(getEntityClass())
                .add(Restrictions.eq("infoComponent.name", name).ignoreCase())
                .uniqueResult();

        if (order == null) {
            throw new InstanceNotFoundException(name, getEntityClass().getName());
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
    public List<Task> getFilteredTask(List<OrderElement> orderElements, List<Criterion> criterions) {

        if (orderElements == null || orderElements.isEmpty()) {
            return new ArrayList<>();
        }


        String strQuery = "SELECT taskSource.task " +
                "FROM OrderElement orderElement, TaskSource taskSource, Task task " +
                "LEFT OUTER JOIN taskSource.schedulingData.orderElement taskSourceOrderElement " +
                "LEFT OUTER JOIN taskSource.task taskElement " +
                "WHERE taskSourceOrderElement.id = orderElement.id " +
                "AND taskElement.id = task.id  AND orderElement IN (:orderElements) ";

        // Set Criterions
        if (criterions != null && !criterions.isEmpty()) {
            strQuery += " AND (EXISTS (FROM task.resourceAllocations as allocation, GenericResourceAllocation as generic " +
                    " WHERE generic.id = allocation.id " +
                    " AND EXISTS( FROM generic.criterions criterion WHERE criterion IN (:criterions))))";
        }

        // Order by
        strQuery += "ORDER BY task.name";

        // Set parameters
        Query query = getSession().createQuery(strQuery);
        query.setParameterList("orderElements", orderElements);

        if (criterions != null && !criterions.isEmpty()) {
            query.setParameterList("criterions", Criterion.withAllDescendants(criterions));
        }

        return query.list();
    }

    @Override
    public Order loadOrderAvoidingProxyFor(final OrderElement orderElement) {
        return loadOrdersAvoidingProxyFor(Collections.singletonList(orderElement)).get(0);
    }

    @Override
    public List<Order> loadOrdersAvoidingProxyFor(final List<OrderElement> orderElements) {
        List<OrderElement> orders =
                transactionService.runOnAnotherTransaction(new IOnTransaction<List<OrderElement>>() {
                    @Override
                    public List<OrderElement> execute() {
                        List<OrderElement> result = new ArrayList<>();
                        for (OrderElement each : orderElements) {
                            if (each.isNewObject()) {
                                result.add(each.getOrder());
                            } else {
                                result.add(orderFrom(each));
                            }
                        }
                        return result;
                    }

                    private OrderElement orderFrom(OrderElement initial) {
                        OrderElement current = initial;
                        OrderElement result = current;

                        while (current != null) {
                            result = current;
                            current = findParent(current);
                        }
                        return result;
                    }

                    private OrderElement findParent(OrderElement orderElement) {
                        Query query = getSession()
                                .createQuery("select e.parent from OrderElement e where e.id = :id")
                                .setParameter("id", orderElement.getId());

                        return (OrderElement) query.uniqueResult();
                    }
                });

        List<Order> result = new ArrayList<>();
        for (OrderElement each : orders) {
            if (each != null) {
                result.add(findExistingEntity(each.getId()));
            } else {
                result.add(null);
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByNameAnotherTransaction(String name) {
        try {
            return findByName(name)!= null;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Order> getActiveOrders() {
        Criteria criteria = getSession().createCriteria(getEntityClass());
        criteria.add(Restrictions.not(Restrictions.eq(STATE_PARAMETER, OrderStatusEnum.CANCELLED)));
        criteria.add(Restrictions.not(Restrictions.eq(STATE_PARAMETER, OrderStatusEnum.STORED)));

        return criteria.list();
    }

    @Override
    public List<CostExpenseSheetDTO> getCostExpenseSheet(List<Order> orders,
                                                         Date startingDate,
                                                         Date endingDate,
                                                         List<Criterion> criterions) {

        String strQuery = "SELECT new org.libreplan.business.reports.dtos.CostExpenseSheetDTO(expense) " +
                "FROM OrderElement orderElement, ExpenseSheetLine expense " +
                "LEFT OUTER JOIN expense.orderElement exp_ord " +
                "WHERE orderElement.id = exp_ord.id ";

        if (startingDate != null && endingDate != null) {
            strQuery += "AND expense.date BETWEEN :startingDate AND :endingDate ";
        }

        if (startingDate != null && endingDate == null) {
            strQuery += "AND expense.date >= :startingDate ";
        }

        if (startingDate == null && endingDate != null) {
            strQuery += "AND expense.date <= :endingDate ";
        }

        // Order by date
        strQuery += "ORDER BY expense.date";

        Query query = getSession().createQuery(strQuery);

        // Set parameters
        if (startingDate != null) {
            query.setParameter("startingDate", new LocalDate(startingDate));
        }

        if (endingDate != null) {
            query.setParameter("endingDate", new LocalDate(endingDate));
        }

        List<CostExpenseSheetDTO> list = query.list();

        List<CostExpenseSheetDTO> filteredList = new ArrayList<>();
        for (CostExpenseSheetDTO each : list) {
            Order order = loadOrderAvoidingProxyFor(each.getOrderElement());

            // Apply filtering
            if (matchFilterCriterion(each.getOrderElement(), criterions) && isOrderContained(order, orders)) {
                each.setOrder(order);
                filteredList.add(each);
            }
        }
        return filteredList;
    }

    @Override
    public List<Order> getOrdersWithNotEmptyCustomersReferences() {
        return getSession()
                .createCriteria(Order.class)
                .add(Restrictions.isNotNull("customerReference"))
                .add(Restrictions.ne("customerReference", ""))
                .list();
    }

}
