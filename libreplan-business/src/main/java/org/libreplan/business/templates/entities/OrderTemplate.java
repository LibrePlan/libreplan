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
package org.libreplan.business.templates.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import org.hibernate.validator.NotNull;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLineGroup;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class OrderTemplate extends OrderLineGroupTemplate {

    public static OrderTemplate create(Order order) {
        OrderTemplate beingBuilt = new OrderTemplate();
        beingBuilt.calendar = order.getCalendar();
        return create(beingBuilt, order);
    }

    private BaseCalendar calendar;

    @Override
    public OrderElement createElement(OrderLineGroup parent) {
        throw new UnsupportedOperationException();
    }

    public Order createOrder(Scenario currentScenario) {
        Order order = Order.create();
        order.setVersionForScenario(currentScenario, OrderVersion
                .createInitialVersion(currentScenario));
        order.useSchedulingDataFor(currentScenario);
        order.setCalendar(calendar);
        order.initializeTemplate(this);
        return setupGroupParts(setupSchedulingStateType(order));
    }

    @Override
    public String getType() {
        return _("Project");
    }

    public void setCalendar(BaseCalendar calendar) {
        this.calendar = calendar;
    }

    @NotNull(message = "template calendar not specified")
    public BaseCalendar getCalendar() {
        return calendar;
    }

    @Override
    public boolean isOrderTemplate() {
        return true;
    }

}
