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

package org.navalplanner.business.planner.daos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.GenericDayAssignment;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.reports.dtos.CompletedEstimatedHoursPerTaskDTO;
import org.navalplanner.business.reports.dtos.WorkingProgressPerTaskDTO;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskElementDAO extends GenericDAOHibernate<TaskElement, Long>
        implements ITaskElementDAO {

    @SuppressWarnings("unchecked")
    private List<GenericDayAssignment> findOrphanedGenericDayAssignments() {
        return getSession().createCriteria(GenericDayAssignment.class).add(
                Restrictions.isNull("genericResourceAllocation")).list();
    }

    @SuppressWarnings("unchecked")
    private List<SpecificDayAssignment> findOrphanedSpecificDayAssignments() {
        return getSession().createCriteria(SpecificDayAssignment.class).add(
                Restrictions.isNull("specificResourceAllocation")).list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TaskElement> findChildrenOf(TaskGroup each) {
        return getSession().createCriteria(TaskElement.class).add(
                Restrictions.eq("parent", each)).list();
    }

    /**
     * Returns a list of dtos with calculations for Working progress per task report
     *
     * @param orders filter by orders
     * @param deadline deadline for task
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<WorkingProgressPerTaskDTO> getWorkingProgressPerTaskReport(
            Order order, LocalDate deadline) {

        List<WorkingProgressPerTaskDTO> result = new ArrayList<WorkingProgressPerTaskDTO>();

        final List<Task> tasks = getTasksByOrderAndDate(order, deadline);
        final List<Task> sortTasks = sortTasks(order, tasks);
        for (Task task : sortTasks) {
            result.add(new WorkingProgressPerTaskDTO(task, deadline));
        }
        return result;
    }

    private List<Task> sortTasks(Order order, List<Task> tasks) {
        List<Task> sortTasks = new ArrayList<Task>();
        final List<OrderElement> orderElements = order.getAllChildren();
        for (OrderElement orderElement : orderElements) {
            Task task = findOrderElementInTasks(orderElement, tasks);
            if (task != null) {
                sortTasks.add(task);
            }
        }
        return sortTasks;
    }

    private Task findOrderElementInTasks(OrderElement orderElement,
            List<Task> tasks) {
        for (Task task : tasks) {
            if (task.getOrderElement().getId().equals(orderElement.getId())) {
                return task;
            }
        }
        return null;
    }

    private List<Task> getTasksByOrderAndDate(Order order, LocalDate deadline) {

        final List<OrderElement> orders = (order != null) ? order
                .getAllOrderElements() : new ArrayList<OrderElement>();

        // Prepare query
        String strQuery =
            "SELECT task "
            + "FROM TaskSource taskSource "
            + "LEFT OUTER JOIN taskSource.task task "
            + "LEFT OUTER JOIN taskSource.orderElement orderElement "
            + "WHERE task IN (SELECT task FROM Task task) ";

        if (orders != null && !orders.isEmpty()) {
            strQuery += "AND orderElement IN (:orders) ";
        }

        // Execute query
        Query query = getSession().createQuery(strQuery);
        if (orders != null && !orders.isEmpty()) {
            query.setParameterList("orders", orders);
        }

        return query.list();
    }

    @Override
    public List<CompletedEstimatedHoursPerTaskDTO> getCompletedEstimatedHoursPerTaskReport(
            Order order, LocalDate deadline) {

        List<CompletedEstimatedHoursPerTaskDTO> result = new ArrayList<CompletedEstimatedHoursPerTaskDTO>();

        final List<Task> tasks = getTasksByOrderAndDate(order, deadline);
        for (Task task: tasks) {
            result.add(new CompletedEstimatedHoursPerTaskDTO(task, deadline));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TaskElement> listFilteredByDate(Date start, Date end) {
        Criteria criteria  = getSession().createCriteria(TaskElement.class);
        if(start != null) {
            criteria.add(Restrictions.ge("endDate", start));
        }
        if(end != null) {
            criteria.add(Restrictions.le("startDate", end));
        }
        return criteria.list();
    }
}
