/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

import static org.libreplan.business.i18n.I18nHelper._;


/**
 * @author Susana Montes Pedreiera <smotnes@wirelessgalicia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum OrderStatusEnum {

    PRE_SALES(_("PRE-SALES")),
    OFFERED(_("OFFERED")),
    SUBCONTRACTED_PENDING_ORDER(_("SUBCONTRACTED PENDING ORDER")),
    ACCEPTED(_("ACCEPTED")),
    STARTED(_("STARTED")),
    ON_HOLD(_("ON HOLD")),
    FINISHED(_("FINISHED")),
    CANCELLED(_("CANCELLED")),
    STORED(_("STORED"));

    private String description;

    private OrderStatusEnum(String description) {
        this.description = description;
    }

    public String toString() {
        return this.description;
    }

    public static OrderStatusEnum getDefault() {
        return OFFERED;
    }
}
