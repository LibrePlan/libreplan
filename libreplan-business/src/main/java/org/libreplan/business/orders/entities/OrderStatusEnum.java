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

import java.util.EnumSet;


/**
 * @author Susana Montes Pedreiera <smotnes@wirelessgalicia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public enum OrderStatusEnum {

    PRE_SALES(_("PRE-SALES"), 0),
    OFFERED(_("OFFERED"), 1),
    OUTSOURCED(_("OUTSOURCED"), 2),
    ACCEPTED(_("ACCEPTED"), 3),
    STARTED(_("STARTED"), 4),
    ON_HOLD(_("ON HOLD"), 5),
    FINISHED(_("FINISHED"), 6),
    CANCELLED(_("CANCELLED"), 7),
    STORED(_("ARCHIVED"), 8);

    private String description;

    /**
     * For {@link DashboardControllerGlobal}.
     * When I am building Global Dashboard page I need to know order of enums in {@link Grid}.
     */
    private final int index;

    OrderStatusEnum(String description, int index) {
        this.description = description;
        this.index = index;
    }

    @Override
    public String toString() {
        return this.description;
    }

    public int getIndex() {
        return this.index;
    }

    public static OrderStatusEnum getDefault() {
        return PRE_SALES;
    }

    public static EnumSet<OrderStatusEnum> getVisibleStatus() {
        return EnumSet.of(
                OrderStatusEnum.PRE_SALES, OrderStatusEnum.OFFERED,
                OrderStatusEnum.OUTSOURCED, OrderStatusEnum.ACCEPTED,
                OrderStatusEnum.STARTED, OrderStatusEnum.ON_HOLD,
                OrderStatusEnum.FINISHED);
    }

}
