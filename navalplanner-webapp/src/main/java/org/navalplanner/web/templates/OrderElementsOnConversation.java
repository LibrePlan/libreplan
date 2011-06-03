/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.templates.entities.OrderElementTemplate;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 */
public class OrderElementsOnConversation {

    private final IOrderElementDAO orderElementDAO;

    private final IOrderDAO orderDAO;

    private List<OrderElement> orderElements = new ArrayList<OrderElement>();


    public OrderElementsOnConversation(IOrderElementDAO orderElementDAO,
            IOrderDAO orderDAO) {
        Validate.notNull(orderElementDAO);
        Validate.notNull(orderDAO);
        this.orderElementDAO = orderElementDAO;
        this.orderDAO = orderDAO;
    }

    public List<OrderElement> getOrderElements() {
        return orderElements;
    }

    public void initialize(OrderElementTemplate template) {
        if ((template != null) && (!template.isNewObject())) {
            orderElements = new ArrayList<OrderElement>(orderElementDAO
                    .findByTemplate(template));
            initialize(orderElements);
        }
    }

    private void initialize(Collection<OrderElement> orderElements) {
        for (OrderElement each : orderElements) {
            initialize(each);
        }
    }

    private void initialize(OrderElement orderElement) {
        orderElement.getName();
        (orderDAO.loadOrderAvoidingProxyFor(orderElement)).getName();
    }

    public void reattach() {
        for (OrderElement each : orderElements) {
            orderElementDAO.reattach(each);
            (orderDAO.loadOrderAvoidingProxyFor(each)).getName();
        }
    }

}
