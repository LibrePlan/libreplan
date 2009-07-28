package org.navalplanner.business.orders.daos;

import org.navalplanner.business.common.daos.IGenericDao;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Contract for {@link OrderElementDao}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface IOrderElementDao extends IGenericDao<OrderElement, Long> {
    public OrderElement findByCode(String code);

    /**
     * Find an order element with the <code>code</code> passed as parameter
     * and which is a son of the <code>parent</code> {@link OrderElement}
     * @param parent Parent {@link OrderElement}
     * @param code code of the {@link OrderElement} to find
     * @return the {@link OrderElement} found
     */
    public OrderElement findByCode(OrderElement parent, String code);
}
