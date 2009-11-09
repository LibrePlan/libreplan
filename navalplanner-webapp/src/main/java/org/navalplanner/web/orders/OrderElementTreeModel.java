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

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.navalplanner.business.orders.entities.IOrderLineGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zul.TreeModel;

/**
 * Model for a the {@link OrderElement} tree for a {@link Order} <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public class OrderElementTreeModel {

    private static MutableTreeModel<OrderElement> createTreeFrom(Order order) {
        MutableTreeModel<OrderElement> treeModel = MutableTreeModel
                .create(
                OrderElement.class, order);
        OrderElement parent = treeModel.getRoot();
        List<OrderElement> orderElements = order.getOrderElements();
        treeModel.add(parent, orderElements);
        addChildren(treeModel, orderElements);
        return treeModel;
    }

    private static MutableTreeModel<OrderElement> createTreeFrom(Order order,
            List<OrderElement> orderElements) {
        MutableTreeModel<OrderElement> treeModel = MutableTreeModel.create(
                OrderElement.class, order);
        OrderElement parent = treeModel.getRoot();
        treeModel.add(parent, orderElements);
        addChildren(treeModel, orderElements);
        return treeModel;
    }

    private static void addChildren(MutableTreeModel<OrderElement> treeModel,
            List<OrderElement> orderElements) {
        for (OrderElement orderElement : orderElements) {
            treeModel.add(orderElement, orderElement.getChildren());
            addChildren(treeModel, orderElement.getChildren());
        }
    }

    private MutableTreeModel<OrderElement> tree;

    public OrderElementTreeModel(Order order) {
        tree = createTreeFrom(order);
    }

    public OrderElementTreeModel(Order order, List<OrderElement> orderElements) {
        tree = createTreeFrom(order, orderElements);
    }

    public TreeModel asTree() {
        return tree;
    }

    public void addOrderElement() {
        addOrderElementAtImpl(tree.getRoot());
    }

    private OrderElement createNewOrderElement() {
        OrderElement newOrderElement = OrderLine
                .createOrderLineWithUnfixedPercentage(0);
        newOrderElement.setName(_("New order element"));
        return newOrderElement;
    }

    public void addOrderElementAt(OrderElement node) {
        addOrderElementAtImpl(node);
    }

    private void addOrderElementAtImpl(OrderElement parent) {
        addOrderElementAt(parent, createNewOrderElement());

    }

    private void addToTree(OrderElement parentNode, OrderElement elementToAdd) {
        tree.add(parentNode, elementToAdd);
        addChildren(tree, Arrays.asList(elementToAdd));
    }

    private void addToTree(OrderElement parentNode, int position,
            OrderElement elementToAdd) {
        tree.add(parentNode, position, Arrays.asList(elementToAdd));
        addChildren(tree, Arrays.asList(elementToAdd));
    }

    private void addOrderElementAt(OrderElement parent,
            OrderElement orderElement) {
        IOrderLineGroup container = turnIntoContainerIfNeeded(parent);
        container.add(orderElement);
        addToTree(toNode(container), orderElement);
        updateCriterionRequirementsInHierarchy(parent, orderElement,
                (OrderElement) container);
    }

    private void addOrderElementAt(OrderElement destinationNode,
            OrderElement elementToAdd, int position) {
        IOrderLineGroup container = turnIntoContainerIfNeeded(destinationNode);
        container.add(position, elementToAdd);
        addToTree(toNode(container), position, elementToAdd);
        updateCriterionRequirementsInHierarchy(destinationNode, elementToAdd,
                (OrderElement) container);
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

    private OrderElement toNode(IOrderLineGroup container) {
        return (OrderElement) container;
    }

    private IOrderLineGroup turnIntoContainerIfNeeded(
            OrderElement selectedForTurningIntoContainer) {
        if (selectedForTurningIntoContainer instanceof IOrderLineGroup) {
            return (IOrderLineGroup) selectedForTurningIntoContainer;
        }
        IOrderLineGroup parentContainer = asOrderLineGroup(getParent(selectedForTurningIntoContainer));
        OrderLineGroup asContainer = selectedForTurningIntoContainer
                .toContainer();
        parentContainer.replace(selectedForTurningIntoContainer, asContainer);
        tree.replace(selectedForTurningIntoContainer, asContainer);
        addChildren(tree, Arrays.asList((OrderElement) asContainer));
        return asContainer;
    }

    private OrderElement getParent(OrderElement node) {
        return tree.getParent(node);
    }

    public List<OrderElement> getParents(OrderElement node) {
        return tree.getParents(node);
    }

    public void indent(OrderElement nodeToIndent) {
        OrderElement parentOfSelected = tree.getParent(nodeToIndent);
        int position = getChildren(parentOfSelected).indexOf(nodeToIndent);
        if (position == 0) {
            return;
        }
        OrderElement destination = (OrderElement) getChildren(parentOfSelected)
                .get(position - 1);
        move(nodeToIndent, destination, getChildren(destination).size());
    }

    private List<OrderElement> getChildren(OrderElement node) {
        List<OrderElement> result = new ArrayList<OrderElement>();
        final int childCount = tree.getChildCount(node);
        for (int i = 0; i < childCount; i++) {
            result.add(tree.getChild(node, i));
        }
        return result;
    }

    public void unindent(OrderElement nodeToUnindent) {
        OrderElement parent = tree.getParent(nodeToUnindent);
        if (tree.isRoot(parent)) {
            return;
        }
        OrderElement destination = tree.getParent(parent);
        move(nodeToUnindent, destination, getChildren(destination).indexOf(
                parent) + 1);
    }

    public void move(OrderElement toBeMoved, OrderElement destination) {
        move(toBeMoved, destination, getChildren(destination).size());
    }

    public void moveToRoot(OrderElement toBeMoved) {
        move(toBeMoved, tree.getRoot(), 0);
    }

    private void move(OrderElement toBeMoved, OrderElement destination,
            int position) {
        if (getChildren(destination).contains(toBeMoved)) {
            return;// it's already moved
        }
        if (isGreatInHierarchy(toBeMoved, destination)) {
            return;
        }
        removeNode(toBeMoved);
        addOrderElementAt(destination, toBeMoved, position);
    }

    private boolean isGreatInHierarchy(OrderElement parent, OrderElement child) {
        return find(child, getChildren(parent));
    }

    private boolean find(OrderElement child, List<OrderElement> children) {
        if (children.indexOf(child) >= 0) {
            return true;
        }
        for (OrderElement criterionDTO : children) {
            return find(child, getChildren(criterionDTO));
        }
        return false;
    }

    public void up(OrderElement node) {
        IOrderLineGroup orderLineGroup = asOrderLineGroup(tree.getParent(node));
        orderLineGroup.up(node);
        tree.up(node);
    }

    public void down(OrderElement node) {
        IOrderLineGroup orderLineGroup = asOrderLineGroup(tree.getParent(node));
        orderLineGroup.down(node);
        tree.down(node);
    }

    private OrderLineGroup asOrderLineGroup(OrderElement node) {
        return (OrderLineGroup) node;
    }

    public void removeNode(OrderElement orderElement) {
        if (orderElement == tree.getRoot()) {
            return;
        }
        OrderLineGroup parent = asOrderLineGroup(tree.getParent(orderElement));
        parent.remove(orderElement);
        tree.remove(orderElement);
        // If removed node was the last one and its parent is not the root node
        if (!tree.isRoot(parent) && tree.getChildCount(parent) == 0) {
            OrderElement asLeaf = ((OrderElement) parent).toLeaf();
            OrderElement parentContainer = getParent(toNode(parent));
            asOrderLineGroup(parentContainer).replace((OrderElement) parent,
                    asLeaf);
            tree.replace((OrderElement) parent, asLeaf);
        }
    }

    public int[] getPath(OrderElement orderElement) {
        return tree.getPath(tree.getRoot(), orderElement);
    }
}
