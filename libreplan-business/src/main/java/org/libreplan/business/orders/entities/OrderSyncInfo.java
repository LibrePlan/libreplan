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

package org.libreplan.business.orders.entities;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.libreplan.business.common.BaseEntity;

/**
 * OrderSyncInfo entity. This entity holds order synchronization info. Each time
 * that order synchronization is successfully completed, an instance of this
 * entity is created or updated and saved to DB to hold the synchronized info.
 * This info is then displayed in UI.
 *
 * This entity contains the following fields:
 * <ul>
 * <li>lastSyncDate: last date where synchronization took place</li>
 * <li>key: an identifier, which connector's key is last synchronized</li>
 * <li>connectorId: an identifier to distinguish which connector has running the
 * synchronization</li>
 * <li>order: order that is synchronized</li>
 * </ul>
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public class OrderSyncInfo extends BaseEntity {

    private Date lastSyncDate;
    private String key;
    private String connectorId;
    private Order order;

    public static OrderSyncInfo create(String key, Order order,
            String connectorId) {
        Validate.notEmpty(key);
        Validate.notNull(order);
        Validate.notEmpty(connectorId);
        return create(new OrderSyncInfo(key, order, connectorId));
    }

    /**
     * Constructor for Hibernate. Do not use!
     */
    protected OrderSyncInfo() {
    }

    private OrderSyncInfo(String key, Order order, String connectorId) {
        this.lastSyncDate = new Date();
        this.key = key;
        this.order = order;
        this.connectorId = connectorId;
    }

    @NotNull(message = "last synchronized date not specified")
    public Date getLastSyncDate() {
        return lastSyncDate;
    }

    public void setLastSyncDate(Date lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
    }

    @NotNull(message = "key not specified")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @NotNull(message = "connector id not specified")
    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
