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
        if (this.parent != null)
            orderElement.setParent(this.parent);
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
