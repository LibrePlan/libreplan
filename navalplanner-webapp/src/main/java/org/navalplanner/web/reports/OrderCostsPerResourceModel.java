/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.reports.dtos.OrderCostsPerResourceDTO;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lorenzo Tilve Álvaro <ltilve@igalia.com>
 *
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderCostsPerResourceModel implements IOrderCostsPerResourceModel {

    @Autowired
    private IOrderDAO orderDAO;

    @Transactional(readOnly = true)
    public JRDataSource getOrderReport(List<Order> orders, Date startingDate,
            Date endingDate) {
        List<OrderCostsPerResourceDTO> workingHoursPerWorkerList = orderDAO
                .getOrderCostsPerResource(orders, startingDate, endingDate);

        Collections.sort(workingHoursPerWorkerList);
        if (workingHoursPerWorkerList.isEmpty()) {
            Worker emptyWorker = createFictitiousWorker();
            WorkReportLine wrl = createEmptyWorkReportLine(emptyWorker);

            if (orders.isEmpty()) {
                Order order = Order.create();
                order.setName(_("All orders"));
                orders.add(order);
            }

            for (Order order : orders) {
                OrderCostsPerResourceDTO emptyDTO = new OrderCostsPerResourceDTO(
                        emptyWorker, wrl);
                emptyDTO.setOrderName(order.getName());
                emptyDTO.setCost(new BigDecimal(0));
                workingHoursPerWorkerList.add(emptyDTO);
            }
        }

        if (workingHoursPerWorkerList != null && !workingHoursPerWorkerList.isEmpty()) {
            return new JRBeanCollectionDataSource(workingHoursPerWorkerList);
        } else {
            return new JREmptyDataSource();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        return orderDAO.getOrders();
    }

    private WorkReportLine createEmptyWorkReportLine(Worker worker) {
        OrderLine leaf = OrderLine.create();
        leaf.setCode(_("All order tasks"));

        TypeOfWorkHours w = TypeOfWorkHours.create();
        w.setDefaultPrice(new BigDecimal(0));

        WorkReportLine wrl = new WorkReportLine();
        wrl.setNumHours(0);
        wrl.setTypeOfWorkHours(w);
        wrl.setResource(worker);
        wrl.setOrderElement(leaf);
        return wrl;
    }

    private Worker createFictitiousWorker() {
        Worker unnasigned = new Worker();
        unnasigned.setFirstName(_("Total dedication"));
        unnasigned.setSurname(" ");
        return unnasigned;
    }

}
