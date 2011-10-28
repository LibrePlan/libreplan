/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import java.util.Set;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public class HoursGroupOrderLineHandler extends HoursGroupHandler<OrderLine> {

    private static final HoursGroupOrderLineHandler singleton =
        new HoursGroupOrderLineHandler();

    private HoursGroupOrderLineHandler() {

    }

    public static HoursGroupOrderLineHandler getInstance() {
        return singleton;
    }

    @Override
    protected HoursGroup createHoursGroup(OrderLine orderLine) {
        return HoursGroup.create(orderLine);
    }

    @Override
    protected Set<HoursGroup> getHoursGroup(OrderLine orderLine) {
        return orderLine.myHoursGroups();
    }

    @Override
    protected void setHoursGroups(OrderLine orderLine,
            Set<HoursGroup> hoursGroups) {
        orderLine.setHoursGroups(hoursGroups);
    }

    @Override
    protected void addHoursGroup(OrderLine orderLine, HoursGroup hoursGroup) {
        orderLine.doAddHoursGroup(hoursGroup);
    }

    @Override
    protected boolean hoursGroupsIsEmpty(OrderLine orderLine) {
        return orderLine.getHoursGroups().isEmpty();
    }

}
