package org.navalplanner.web.orders;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.IOrderLineGroup;
import org.navalplanner.business.orders.entities.Order;

/**
 * Contract for {@link OrderModel}<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IOrderModel {

    List<Order> getOrders();

    void prepareEditFor(Order order);

    void prepareForCreate();

    void save() throws ValidationException;

    IOrderLineGroup getOrder();

    void remove(Order order);

    void prepareForRemove(Order order);

    OrderElementModel getOrderElementTreeModel();

}
