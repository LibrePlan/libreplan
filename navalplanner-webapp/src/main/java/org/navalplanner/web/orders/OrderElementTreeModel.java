/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.List;

import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.trees.ITreeNode;
import org.navalplanner.business.trees.ITreeParentNode;
import org.navalplanner.web.tree.EntitiesTree;

/**
 * Model for a the {@link OrderElement} tree for a {@link Order} <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public class OrderElementTreeModel extends EntitiesTree<OrderElement> {

    public OrderElementTreeModel(OrderElement root,
            List<OrderElement> orderElements) {
        super(OrderElement.class, root, orderElements);
    }

    public OrderElementTreeModel(OrderElement root) {
        super(OrderElement.class, root);
    }

    private void updateCriterionRequirementsInHierarchy(
            OrderElement destination, OrderElement origin,
            OrderElement container) {
        if (destination instanceof OrderLine) {
            container.updateCriterionRequirements();
        } else {
            origin.updateCriterionRequirements();
        }
    }

    @Override
    protected void added(ITreeNode<OrderElement> destination,
            ITreeNode<OrderElement> added,
            ITreeParentNode<OrderElement> turnedIntoContainer) {
        updateCriterionRequirementsInHierarchy(destination.getThis(), added
                .getThis(), turnedIntoContainer.getThis());
    }

    @Override
    protected OrderElement createNewElement() {
        OrderElement newOrderElement = OrderLine
                .createOrderLineWithUnfixedPercentage(0);
        newOrderElement.setName(_("New task"));
        return newOrderElement;
    }

    @Override
    protected OrderElement createNewElement(String name, int hours) {
        OrderLine newOrderElement = OrderLine
                .createOrderLineWithUnfixedPercentage(hours);
        newOrderElement.setName(name);
        return newOrderElement;
    }

}
