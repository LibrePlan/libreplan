package org.navalplanner.business.orders.daos;

import org.navalplanner.business.common.daos.IGenericDao;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 * Contract for {@link OrderElementDao}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IOrderElementDao extends IGenericDao<OrderElement, Long> {

}
