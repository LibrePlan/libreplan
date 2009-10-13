/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.orders;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.IOrderLineGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.TaskElement;

/**
 * Contract for {@link OrderModel}<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IOrderModel {

    List<Order> getOrders();

    void prepareEditFor(Order order);

    void prepareForCreate();

    void save() throws ValidationException;

    IOrderLineGroup getOrder();

    void remove(Order order);

    void prepareForRemove(Order order);

    OrderElementTreeModel getOrderElementTreeModel();

    IOrderElementModel getOrderElementModel(OrderElement orderElement);

    void prepareForSchedule(Order order);

    void schedule();

    boolean isAlreadyScheduled(Order order);

    void setOrder(Order order);

    TaskElement convertToInitialSchedule(OrderElement order);

    void convertToScheduleAndSave(Order order);

    void addLabelPredicate(Label label);
}
