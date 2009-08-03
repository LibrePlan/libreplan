package org.navalplanner.business.orders.daos;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.orders.entities.Order;

/**
 * Contract for {@link OrderDAO}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IOrderDAO extends IGenericDAO<Order, Long> {

}