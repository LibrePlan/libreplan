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
     * <code>{@link Order}</code> and <code>connectorName</code>
     *
     * @param order
     *            the order to search for
     * @param connectorName
     *            the connector name
     *
     * @return Last synchronized info
     */
    OrderSyncInfo findLastSynchronizedInfoByOrderAndConnectorName(Order order,
            String connectorName);

    /**
     * Search last synchronized infos for the specified
     * <code>{@link Order}</code> and <code>connectorName</code>
     *
     * @param order
     *            the order to search for
     * @param connectorName
     *            the connector name
     * @return list of last synchronized infos
     */
    List<OrderSyncInfo> findLastSynchronizedInfosByOrderAndConnectorName(
            Order order, String connectorName);

    /**
     * Searches and returns <code>{@link OrderSyncInfo}</code> for the specified
     * <code>key</code> and <code>connectorName</code>
     *
     * @param key
     *            the unique key with in connector id
     * @param order
     *            the order
     * @param connectorName
     *            the connector name
     */
    OrderSyncInfo findByKeyOrderAndConnectorName(String key, Order order,
            String connectorName);

    /**
     * Finds the {@link OrderSyncInfo}s for the specified
     * <code>connectorName</code>
     *
     * @param connectorName
     *            the connector name
     * @return a list of OrderSyncInfo if found and null if not
     */
    List<OrderSyncInfo> findByConnectorName(String connectorName);

    /**
     * Returns true if there exists other {@link OrderSyncInfo} with the same
     * <code>{@link OrderSyncInfo#getKey()}</code>,
     * <code>{@link OrderSyncInfo#getOrder()}</code> and
     * <code>{@link OrderSyncInfo#getConnectorName()}</code>
     *
     * @param orderSyncInfo
     *            the <code>{@link OrderSyncInfo}</code>
     */
    boolean existsByKeyOrderAndConnectorNameAnotherTransaction(
            OrderSyncInfo orderSyncInfo);

    /**
     * Returns unique {@link OrderSyncInfo} for the specified <code>key</code>,
     * <code>order</code> and <code>connectorName</code>
     *
     * @param key
     *            the key
     * @param order
     *            an order
     * @param connectorName
     *            the name of the connector
     */
    OrderSyncInfo findUniqueByKeyOrderAndConnectorNameAnotherTransaction(
            String key, Order order, String connectorName);
}
