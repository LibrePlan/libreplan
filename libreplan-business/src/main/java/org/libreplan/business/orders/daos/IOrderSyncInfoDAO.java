/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.orders.daos;

import java.util.List;

import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderSyncInfo;

/**
 * Contract for {@link OrderSyncInfoDAO}
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface IOrderSyncInfoDAO extends IGenericDAO<OrderSyncInfo, Long> {

    /**
     * Search last synchronized info for the specified
     * <code>{@link Order}</code> and <code>connectorId</code>
     *
     * @param order
     *            the order to search for
     * @param connectorId
     *            the connector-id
     *
     * @return Last synchronized info
     */
    OrderSyncInfo findLastSynchronizedInfoByOrderAndConnectorId(Order order,
            String connectorId);

    /**
     * Search last synchronized infos for the specified
     * <code>{@link Order}</code> and <code>connectorId</code>
     *
     * @param order
     *            the order to search for
     * @param connectorId
     *            the connector-id
     * @return list of last synchronized infos
     */
    List<OrderSyncInfo> findLastSynchronizedInfosByOrderAndConnectorId(
            Order order, String connectorId);

}
