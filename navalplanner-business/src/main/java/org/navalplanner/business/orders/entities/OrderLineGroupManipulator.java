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

package org.navalplanner.business.orders.entities;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link OrderElement}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class OrderLineGroupManipulator implements IOrderLineGroup {

    public static OrderLineGroupManipulator createManipulatorForOrder(
            List<OrderElement> orderElements) {
        return new OrderLineGroupManipulator(null, orderElements);
    }

    public static OrderLineGroupManipulator createManipulatorForOrderLineGroup(
            OrderLineGroup group, List<OrderElement> children) {
        return new OrderLineGroupManipulator(group, children);
    }

    private final List<OrderElement> orderElements;
    private final OrderLineGroup parent;

    private OrderLineGroupManipulator(OrderLineGroup parent,
            List<OrderElement> orderElements) {
        this.parent = parent;
        this.orderElements = orderElements;
    }

    @Override
    public void add(OrderElement orderElement) {
        setParentIfRequired(orderElement);
        orderElements.add(orderElement);
    }

    private void setParentIfRequired(OrderElement orderElement) {
        if (this.parent != null) {
            orderElement.setParent(this.parent);
        }
    }

    @Override
    public void remove(OrderElement orderElement) {
        orderElements.remove(orderElement);
    }

    @Override
    public void replace(OrderElement oldOrderElement, OrderElement orderElement) {
        setParentIfRequired(orderElement);
        Collections.replaceAll(orderElements, oldOrderElement, orderElement);
    }

    @Override
    public void up(OrderElement orderElement) {
        int position = orderElements.indexOf(orderElement);
        if (position < orderElements.size() - 1) {
            orderElements.remove(position);
            orderElements.add(position + 1, orderElement);
        }
    }

    @Override
    public void down(OrderElement orderElement) {
        int position = orderElements.indexOf(orderElement);
        if (position > 0) {
            orderElements.remove(position);
            orderElements.add(position - 1, orderElement);
        }
    }

    @Override
    public void add(int position, OrderElement orderElement) {
        setParentIfRequired(orderElement);
        orderElements.add(position, orderElement);
    }

}
