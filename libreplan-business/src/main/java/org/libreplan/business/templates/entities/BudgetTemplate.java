/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

import org.libreplan.business.common.Registry;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;

/**
 * Marker class intended to differentiate two different kinds of OrderTemplate:
 * BudgetTemplate and Budget.
 * @author jaragunde
 *
 */
public class BudgetTemplate extends OrderTemplate {

    public static BudgetTemplate create() {
        BudgetTemplate beingBuilt = new BudgetTemplate();
        beingBuilt.calendar =
                Registry.getConfigurationDAO().getConfiguration().getDefaultCalendar();
        beingBuilt.setCode("default-code-for-budget-template");
        return create(beingBuilt);
    }

    public Order createOrder(Scenario currentScenario) {
        Order order = Order.create();
        order.setVersionForScenario(currentScenario,
                OrderVersion.createInitialVersion(currentScenario));
        order.useSchedulingDataFor(currentScenario);
        order.setCalendar(calendar);
        order.initializeTemplate(this);

        Budget budget = Budget.createFromTemplate(this);
        budget.setAssociatedOrder(order);
        order.setAssociatedBudgetObject(budget);

        return order;
    }

}
