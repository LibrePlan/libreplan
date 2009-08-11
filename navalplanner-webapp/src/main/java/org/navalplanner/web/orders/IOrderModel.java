package org.navalplanner.web.orders;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.IOrderLineGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.TaskElement;

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

    OrderElementTreeModel getOrderElementTreeModel();

    IOrderElementModel getOrderElementModel(OrderElement orderElement);

    void prepareForSchedule(Order order);

    void schedule();

    boolean isAlreadyScheduled(Order order);

    void setOrder(Order order);

    TaskElement convertToInitialSchedule(OrderElement order);

    void convertToScheduleAndSave(Order order);

}
