/*
 * This file is part of LibrePlan
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

package org.libreplan.web.templates.historicalAssignment;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;


/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class OrderElementHistoricAssignmentDTO {

    public final String orderCode;
    public final String orderElementCode;
    private final String name;
    public final String orderElementName;
    public final String estimatedHours;
    public final String workedHours;
    public final OrderElement orderElement;
    public final Order order;

    OrderElementHistoricAssignmentDTO(OrderElement orderElement,
 Order order,
            String estimatedHours, String workedHours) {
        this.orderElement = orderElement;
        this.order = order;
        this.orderCode = order.getCode();
        this.orderElementCode = orderElement.getCode();
        this.name = order.getName();
        this.orderElementName = orderElement.getName();
        this.estimatedHours = estimatedHours;
        this.workedHours = workedHours;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public String getOrderElementCode() {
        return orderElementCode;
    }

    public String getEstimatedHours() {
        return estimatedHours;
    }

    public String getWorkedHours() {
        return workedHours;
    }

    public OrderElement getOrderElement() {
        return this.orderElement;
    }

    public Order getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public String getOrderElementName() {
        return orderElementName;
    }

}
