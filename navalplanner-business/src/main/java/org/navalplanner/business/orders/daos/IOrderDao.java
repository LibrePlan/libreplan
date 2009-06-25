package org.navalplanner.business.orders.daos;

import org.navalplanner.business.common.daos.IGenericDao;
import org.navalplanner.business.orders.entities.Order;

/**
 * Contract for {@link OrderDao}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IOrderDao extends IGenericDao<Order, Long> {

}