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
package org.navalplanner.web.reports;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CommonQueries implements ICommonQueries {

    @Autowired
    IOrderDAO orderDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Task> filteredTaskElements(Order order, List<Label> labels,
            List<Criterion> criterions) {
        List<OrderElement> orderElements = order.getAllChildren();
        // Filter by labels
        List<OrderElement> filteredOrderElements = filteredOrderElementsByLabels(
                orderElements, labels);
        return orderDAO.getFilteredTask(filteredOrderElements, criterions);
    }

    private List<OrderElement> filteredOrderElementsByLabels(
            List<OrderElement> orderElements, List<Label> labels) {
        if (labels != null && !labels.isEmpty()) {
            List<OrderElement> filteredOrderElements = new ArrayList<OrderElement>();
            for (OrderElement orderElement : orderElements) {
                List<Label> inheritedLabels = getInheritedLabels(orderElement);
                if (containsAny(labels, inheritedLabels)) {
                    filteredOrderElements.add(orderElement);
                }
            }
            return filteredOrderElements;
        } else {
            return orderElements;
        }
    }

    private boolean containsAny(List<Label> labelsA, List<Label> labelsB) {
        for (Label label : labelsB) {
            if (labelsA.contains(label)) {
                return true;
            }
        }
        return false;
    }

    private List<Label> getInheritedLabels(OrderElement orderElement) {
        List<Label> result = new ArrayList<Label>();
        OrderElement current = orderElement;
        while (current != null) {
            result.addAll(current.getLabels());
            current = current.getParent();
        }
        return result;
    }

}
