package org.navalplanner.business.orders.entities;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link OrderElement}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class OrderLineGroupManipulator implements IOrderLineGroup {

    private final List<OrderElement> orderElements;

    public OrderLineGroupManipulator(List<OrderElement> orderElements) {
        this.orderElements = orderElements;

    }

    @Override
    public void add(OrderElement orderElement) {
        orderElements.add(orderElement);
    }

    @Override
    public void remove(OrderElement orderElement) {
        orderElements.remove(orderElement);
    }

    @Override
    public void replace(OrderElement oldOrderElement, OrderElement orderElement) {
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
        orderElements.add(position, orderElement);
    }

}
