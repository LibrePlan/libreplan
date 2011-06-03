/*
 * This file is part of NavalPlan
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
package org.navalplanner.business.templates.entities;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.Date;

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;

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

    @NotNull(message = "project calendar not specified")
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
        order.setInitDate(today());
        order.setCalendar(calendar);
        order.initializeTemplate(this);
        return setupGroupParts(setupSchedulingStateType(order));
    }

    private Date today() {
        return new LocalDate().toDateTimeAtStartOfDay().toDate();
    }

    @Override
    public String getType() {
        return _("Project");
    }

}
