/*
 * This file is part of ###PROJECT_NAME###
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
package org.navalplanner.web.subcontract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.navalplanner.business.advance.daos.IAdvanceAssignmentDAO;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for operations related with report advances.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportAdvancesModel implements IReportAdvancesModel {

    private static Log LOG = LogFactory.getLog(ReportAdvancesModel.class);

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IAdvanceAssignmentDAO advanceAssignmentDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersWithExternalCodeInAnyOrderElement() {
        List<OrderElement> orderElements = orderElementDAO
                .findOrderElementsWithExternalCode();

        Map<Long, Order> ordersMap = new HashMap<Long, Order>();
        for (OrderElement orderElement : orderElements) {
            Order order = orderElementDAO
                    .loadOrderAvoidingProxyFor(orderElement);
            if (ordersMap.get(order.getId()) == null) {
                ordersMap.put(order.getId(), order);
                forceLoadDirectAdvanceAssignments(order);
            }
        }

        return new ArrayList<Order>(ordersMap.values());
    }

    private void forceLoadDirectAdvanceAssignments(Order order) {
        order.getAllDirectAdvanceAssignments(
                PredefinedAdvancedTypes.SUBCONTRACTOR.getType()).size();
    }

    @Override
    @Transactional(readOnly = true)
    public AdvanceMeasurement getLastAdvanceMeasurement(
            Set<DirectAdvanceAssignment> allDirectAdvanceAssignments) {
        if (allDirectAdvanceAssignments.isEmpty()) {
            return null;
        }

        Iterator<DirectAdvanceAssignment> iterator = allDirectAdvanceAssignments
                .iterator();
        DirectAdvanceAssignment advanceAssignment = iterator.next();
        advanceAssignmentDAO.reattachUnmodifiedEntity(advanceAssignment);

        AdvanceMeasurement lastAdvanceMeasurement = advanceAssignment
                .getLastAdvanceMeasurement();
        while (iterator.hasNext()) {
            advanceAssignment = iterator.next();
            advanceAssignmentDAO.reattachUnmodifiedEntity(advanceAssignment);
            AdvanceMeasurement advanceMeasurement = advanceAssignment
                    .getLastAdvanceMeasurement();
            if (advanceMeasurement.getDate().compareTo(
                    lastAdvanceMeasurement.getDate()) > 0) {
                lastAdvanceMeasurement = advanceMeasurement;
            }
        }

        return lastAdvanceMeasurement;
    }

    @Override
    @Transactional(readOnly = true)
    public AdvanceMeasurement getLastAdvanceMeasurementReported(
            Set<DirectAdvanceAssignment> allDirectAdvanceAssignments) {
        if (allDirectAdvanceAssignments.isEmpty()) {
            return null;
        }

        AdvanceMeasurement lastAdvanceMeasurementReported = null;

        for (DirectAdvanceAssignment advanceAssignment : allDirectAdvanceAssignments) {
            advanceAssignmentDAO.reattachUnmodifiedEntity(advanceAssignment);

            for (AdvanceMeasurement advanceMeasurement : advanceAssignment.getAdvanceMeasurements()) {
                if (advanceMeasurement.getCommunicationDate() != null) {
                    if (lastAdvanceMeasurementReported == null) {
                        lastAdvanceMeasurementReported = advanceMeasurement;
                    } else {
                        if (advanceMeasurement.getCommunicationDate()
                                .compareTo(
                                        lastAdvanceMeasurementReported
                                                .getCommunicationDate()) > 0) {
                            lastAdvanceMeasurementReported = advanceMeasurement;
                        }
                    }
                }
            }
        }

        return lastAdvanceMeasurementReported;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAnyAdvanceMeasurementNotReported(
            Set<DirectAdvanceAssignment> allDirectAdvanceAssignments) {
        if (allDirectAdvanceAssignments.isEmpty()) {
            return false;
        }

        for (DirectAdvanceAssignment advanceAssignment : allDirectAdvanceAssignments) {
            advanceAssignmentDAO.reattachUnmodifiedEntity(advanceAssignment);

            for (AdvanceMeasurement advanceMeasurement : advanceAssignment.getAdvanceMeasurements()) {
                if (advanceMeasurement.getCommunicationDate() == null) {
                    return true;
                }
            }
        }

        return false;
    }

}
