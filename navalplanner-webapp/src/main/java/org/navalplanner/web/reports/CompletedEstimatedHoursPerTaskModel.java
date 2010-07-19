/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
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

package org.navalplanner.web.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.joda.time.LocalDate;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.reports.dtos.CompletedEstimatedHoursPerTaskDTO;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Diego Pino Garcia <dpino@igalia.com>
 *
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CompletedEstimatedHoursPerTaskModel implements ICompletedEstimatedHoursPerTaskModel {

    @Autowired
    IOrderDAO orderDAO;

    @Autowired
    ITaskElementDAO taskDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        return orderDAO.getOrdersByScenario(scenarioManager.getCurrent());
    }

    private void initializeOrderElements(List<OrderElement> orderElements) {
        for (OrderElement each: orderElements) {
            initializeOrderElement(each);
        }
    }

    private void initializeOrderElement(OrderElement orderElement) {
        orderElement.getName();
    }

    private void reattachmentOrder(Order order) {
        orderDAO.reattachUnmodifiedEntity(order);
        initializeOrderElements(order.getAllOrderElements());
    }

    @Override
    @Transactional(readOnly = true)
    public JRDataSource getCompletedEstimatedHoursReportPerTask(
            Order order, Date deadline) {
        reattachmentOrder(order);
        order.useSchedulingDataFor(scenarioManager.getCurrent());
        LocalDate deadlineLocalDate = new LocalDate(deadline);

        final List<TaskElement> tasks = order.getAllChildrenAssociatedTaskElements();
        final List<CompletedEstimatedHoursPerTaskDTO> completedEstimatedHoursPerTaskList =
            new ArrayList<CompletedEstimatedHoursPerTaskDTO>();
        for (TaskElement task: tasks) {
            if(task instanceof Task) {
                completedEstimatedHoursPerTaskList.add(
                        new CompletedEstimatedHoursPerTaskDTO((Task)task, deadlineLocalDate));
            }
        }
        if (!completedEstimatedHoursPerTaskList.isEmpty()) {
            return new JRBeanCollectionDataSource(completedEstimatedHoursPerTaskList);
        } else {
            return new JREmptyDataSource();
        }
    }

}
