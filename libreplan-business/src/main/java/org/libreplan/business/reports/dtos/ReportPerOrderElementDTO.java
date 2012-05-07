/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalcia S.L.
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

package org.libreplan.business.reports.dtos;

import org.libreplan.business.orders.entities.OrderElement;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public abstract class ReportPerOrderElementDTO {

    private OrderElement orderElement;

    public ReportPerOrderElementDTO() {
    }

    public ReportPerOrderElementDTO(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    public OrderElement getOrderElement() {
        return this.orderElement;
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }
}
