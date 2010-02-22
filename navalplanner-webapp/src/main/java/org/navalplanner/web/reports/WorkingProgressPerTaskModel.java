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

package org.navalplanner.web.reports;

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
import org.navalplanner.business.reports.dtos.WorkingProgressPerTaskDTO;
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
public class WorkingProgressPerTaskModel implements IWorkingProgressPerTaskModel {

    @Autowired
    IOrderDAO orderDAO;

    @Autowired
    ITaskElementDAO taskDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        List<Order> result = orderDAO.getOrders();
        return result;
    }

    private void initializeOrderElements(List<OrderElement> orderElements) {
        for (OrderElement each: orderElements) {
            initializeOrderElement(each);
        }
    }

    private void initializeOrderElement(OrderElement orderElement) {
        orderElement.getName();
    }

    @Override
    @Transactional(readOnly = true)
    public JRDataSource getWorkingProgressPerTaskReport(Order order,
            Date referenceDate) {

        orderDAO.reattachUnmodifiedEntity(order);
        initializeOrderElements(order.getAllOrderElements());

        final List<WorkingProgressPerTaskDTO> workingHoursPerWorkerList =
            taskDAO.getWorkingProgressPerTaskReport(order, new LocalDate(referenceDate));

        if (workingHoursPerWorkerList != null && !workingHoursPerWorkerList.isEmpty()) {
            return new JRBeanCollectionDataSource(workingHoursPerWorkerList);
        } else {
            return new JREmptyDataSource();
        }
    }

}
