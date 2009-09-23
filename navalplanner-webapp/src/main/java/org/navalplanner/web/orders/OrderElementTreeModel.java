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
        List<OrderElement> orderElements = order.getOrderElements();
        MutableTreeModel<OrderElement> treeModel = MutableTreeModel
                .create(OrderElement.class);
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
            reattach(orderElement);
        }
    }

    private static void reattach(OrderElement orderElement) {
        orderElement.getHoursGroups().size();
    }

    private MutableTreeModel<OrderElement> tree;
    private final Order order;

    public OrderElementTreeModel(Order order) {
        this.order = order;
        tree = createTreeFrom(order);
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
        addOrderElementAtImpl(parent, createNewOrderElement());

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

    private void addOrderElementAtImpl(OrderElement parent,
            OrderElement orderElement) {
        IOrderLineGroup container = turnIntoContainerIfNeeded(parent);
        container.add(orderElement);
        addToTree(toNode(container), orderElement);
    }

    private void addOrderElementAtImpl(OrderElement destinationNode,
            OrderElement elementToAdd, int position) {
        IOrderLineGroup container = turnIntoContainerIfNeeded(destinationNode);
        container.add(position, elementToAdd);
        addToTree(toNode(container), position, elementToAdd);
    }

    private OrderElement toNode(IOrderLineGroup container) {
        if (container == order) {
            return tree.getRoot();
        }
        return (OrderElement) container;
    }

    private IOrderLineGroup turnIntoContainerIfNeeded(
            OrderElement selectedForTurningIntoContainer) {
        if (tree.isRoot(selectedForTurningIntoContainer)) {
            return order;
        }
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
        moveImpl(nodeToIndent, destination, getChildren(destination).size());
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
        moveImpl(nodeToUnindent, destination, getChildren(destination).indexOf(
                parent) + 1);
    }

    public void move(OrderElement toBeMoved, OrderElement destination) {
        moveImpl(toBeMoved, destination, getChildren(destination).size());
    }

    public void moveToRoot(OrderElement toBeMoved) {
        moveImpl(toBeMoved, tree.getRoot(), 0);
    }

    private void moveImpl(OrderElement toBeMoved, OrderElement destination,
            int position) {
        if (getChildren(destination).contains(toBeMoved)) {
            return;// it's already moved
        }
        removeNodeImpl(toBeMoved);
        addOrderElementAtImpl(destination, toBeMoved, position);
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

    private IOrderLineGroup asOrderLineGroup(OrderElement node) {
        if (tree.isRoot(node)) {
            return order;
        }
        return (IOrderLineGroup) node;
    }

    public void removeNode(OrderElement node) {
        removeNodeImpl(node);
    }

    private void removeNodeImpl(OrderElement orderElement) {
        if (orderElement == tree.getRoot()) {
            return;
        }
        IOrderLineGroup parent = asOrderLineGroup(tree.getParent(orderElement));
        parent.remove(orderElement);
        tree.remove(orderElement);
        // If removed node was the last one and its parent is not the root node
        if (parent != order && tree.getChildCount(parent) == 0) {
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
