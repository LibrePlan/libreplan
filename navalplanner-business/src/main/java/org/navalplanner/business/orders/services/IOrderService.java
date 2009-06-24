package org.navalplanner.business.orders.services;

import java.util.List;

import org.navalplanner.business.common.OnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.Order;

/**
 * Management of {@link Order} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IOrderService {

    void save(Order order) throws ValidationException;

    boolean exists(Order HoursGroup);

    List<Order> getOrders();

    void remove(Order HoursGroup) throws InstanceNotFoundException;

    Order find(Long workerId) throws InstanceNotFoundException;

    public <T> T onTransaction(OnTransaction<T> onTransaction);

}
