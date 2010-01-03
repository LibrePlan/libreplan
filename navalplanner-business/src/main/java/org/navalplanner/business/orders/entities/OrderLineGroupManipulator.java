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

import java.util.List;

import org.navalplanner.business.trees.TreeNodeOnList;

/**
 * Implementation of {@link IOrderLineGroup}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class OrderLineGroupManipulator extends
        TreeNodeOnList<OrderElement, OrderLineGroup> {

    public static OrderLineGroupManipulator createManipulatorForOrder(
            List<OrderElement> orderElements) {
        return new OrderLineGroupManipulator(null, orderElements);
    }

    public static OrderLineGroupManipulator createManipulatorForOrderLineGroup(
            OrderLineGroup group, List<OrderElement> children) {
        return new OrderLineGroupManipulator(group, children);
    }

    private OrderLineGroupManipulator(OrderLineGroup parent,
            List<OrderElement> orderElements) {
        super(parent, orderElements);
    }

    protected void setParentIfRequired(OrderElement orderElement) {
        if (getParent() != null) {
            orderElement.setParent(getParent());
        }
    }

    @Override
    protected void onChildAdded(OrderElement newChild) {
        addSchedulingStateToParent(newChild);
    }

    private void addSchedulingStateToParent(OrderElement orderElement) {
        final OrderLineGroup parent = getParent();
        if (parent != null) {
            SchedulingState schedulingState = orderElement.getSchedulingState();
            removeSchedulingStateFromParent(orderElement);
            parent.getSchedulingState().add(schedulingState);
        }
    }

    @Override
    protected void onChildRemoved(OrderElement previousChild) {
        removeSchedulingStateFromParent(previousChild);
    }

    private void removeSchedulingStateFromParent(OrderElement orderElement) {
        SchedulingState schedulingState = orderElement.getSchedulingState();
        if (!schedulingState.isRoot()) {
            schedulingState.getParent().removeChild(schedulingState);
        }
    }
}