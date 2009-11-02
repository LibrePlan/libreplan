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
import java.util.Map;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.IOrderLineGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;

/**
 * Contract for {@link OrderModel}<br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface IOrderModel {

    /**
     * Adds {@link Label} to list of labels
     *
     * @param label
     */
    void addLabel(Label label);

    TaskElement convertToInitialSchedule(OrderElement order);

    void convertToScheduleAndSave(Order order);

    /**
     * Returns a list of {@link Label}
     *
     * @return
     */
    List<Criterion> getCriterionsFor(CriterionType criterionType);

    Map<CriterionType, List<Criterion>> getMapCriterions();

    List<Label> getLabels();

    IOrderLineGroup getOrder();

    IOrderElementModel getOrderElementModel(OrderElement orderElement);

    /**
     * Iterates through order.orderElements, and checks if orderElement holds
     * predicate. In case it's true, add orderElement and all its children to
     * filtered orderElements list
     *
     * @return
     */
    OrderElementTreeModel getOrderElementsFilteredByPredicate(IPredicate predicate);

    OrderElementTreeModel getOrderElementTreeModel();

    List<Order> getOrders();

    boolean isAlreadyScheduled(Order order);

    void initEdit(Order order);

    void prepareForCreate();

    void remove(Order order);

    void save() throws ValidationException;

    void schedule(Order order);

    void setOrder(Order order);

}
