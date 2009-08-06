package org.navalplanner.business.orders.daos;

import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.orders.entities.Order;

/**
 * Contract for {@link OrderDAO}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IOrderDAO extends IGenericDAO<Order, Long> {

    /**
     * Gets all the orders.
     *
     * @return A {@link List} of {@link Order} objects
     */
    List<Order> getOrders();

}
