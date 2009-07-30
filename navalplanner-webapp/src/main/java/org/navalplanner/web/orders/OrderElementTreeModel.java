package org.navalplanner.web.orders;

import java.util.ArrayList;
import java.util.List;

import org.navalplanner.business.orders.entities.IOrderLineGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;

/**
 * Model for a the {@link OrderElement} tree for a {@link Order} <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public class OrderElementTreeModel extends SimpleTreeModel {

    private static List<SimpleTreeNode> asNodes(List<OrderElement> orderElements) {
        ArrayList<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
        for (OrderElement orderElement : orderElements) {
            result.add(asNode(orderElement));
        }
        return result;
    }

    private static SimpleTreeNode asNode(OrderElement orderElement) {
        orderElement.getHoursGroups().size();
        return new SimpleTreeNode(orderElement, asNodes(orderElement
                .getChildren()));
    }

    private static SimpleTreeNode createRootNodeAndDescendants(Order order) {
        return new SimpleTreeNode(order, asNodes(order.getOrderElements()));
    }

    public OrderElementTreeModel(Order order) {
        super(createRootNodeAndDescendants(order));
    }

    public void reloadFromOrder() {
        Order root = getRootAsOrder();
        SimpleTreeNode rootAsNode = getRootAsNode();
        rootAsNode.getChildren().clear();
        rootAsNode.getChildren().addAll(asNodes(root.getOrderElements()));
    }

    public void addOrderElement() {
        addOrderElementAtImpl(getRootAsNode());
        reloadFromOrder();
    }

    private OrderElement createNewOrderElement() {
        OrderElement newOrderElement = OrderLine
                .createOrderLineWithUnfixedPercentage(0);
        newOrderElement.setName("New Order Element");
        return newOrderElement;
    }

    public void addOrderElementAt(SimpleTreeNode node) {
        addOrderElementAtImpl(node);
        reloadFromOrder();
    }

    private void addOrderElementAtImpl(SimpleTreeNode node) {
        addOrderElementAtImpl(node, createNewOrderElement());
    }

    private void addOrderElementAtImpl(SimpleTreeNode node, OrderElement orderElement) {
        IOrderLineGroup container = turnIntoContainerIfNeeded(node);
        container.add(orderElement);
    }

    private void addOrderElementAtImpl(SimpleTreeNode destinationNode, OrderElement orderElement,
            int position) {
        IOrderLineGroup container = turnIntoContainerIfNeeded(destinationNode);
        container.add(position, orderElement);
    }

    private IOrderLineGroup turnIntoContainerIfNeeded(
            SimpleTreeNode selectedForTurningIntoContainer) {
        IOrderLineGroup parentContainer = asOrderLineGroup(getParent(selectedForTurningIntoContainer));
        if (selectedForTurningIntoContainer.getData() instanceof IOrderLineGroup)
            return (IOrderLineGroup) selectedForTurningIntoContainer.getData();
        OrderElement toBeTurned = asOrderLine(selectedForTurningIntoContainer);
        OrderLineGroup asContainer = toBeTurned.toContainer();
        parentContainer.replace(toBeTurned, asContainer);
        return asContainer;
    }

    private SimpleTreeNode getParent(SimpleTreeNode node) {
        int[] position = getPath(node);
        SimpleTreeNode current = getRootAsNode();
        SimpleTreeNode[] path = new SimpleTreeNode[position.length];
        for (int i = 0; i < position.length; i++) {
            path[i] = (SimpleTreeNode) current.getChildAt(position[i]);
            current = path[i];
        }
        int parentOfLast = path.length - 2;
        if (parentOfLast >= 0)
            return path[parentOfLast];
        else
            return getRootAsNode();
    }

    public List<SimpleTreeNode> getParents(SimpleTreeNode node) {
        List<SimpleTreeNode> parents = new ArrayList<SimpleTreeNode>();
        SimpleTreeNode current = node;

        while (!current.equals(getRootAsNode())) {
            current = getParent(current);
            parents.add(current);
        }

        return parents;
    }

    public void indent(SimpleTreeNode nodeToIndent) {
        SimpleTreeNode parentOfSelected = getParent(nodeToIndent);
        int position = parentOfSelected.getChildren().indexOf(nodeToIndent);
        if (position == 0) {
            return;
        }
        SimpleTreeNode destination = (SimpleTreeNode) parentOfSelected
                .getChildren().get(position - 1);
        moveImpl(nodeToIndent, destination, destination.getChildCount());
        reloadFromOrder();
    }

    public void unindent(SimpleTreeNode nodeToUnindent) {
        SimpleTreeNode parent = getParent(nodeToUnindent);
        if (getRootAsNode() == parent) {
            return;
        }
        SimpleTreeNode destination = getParent(parent);
        moveImpl(nodeToUnindent, destination, destination.getChildren()
                .indexOf(parent) + 1);
        reloadFromOrder();
    }

    public void move(SimpleTreeNode toBeMoved, SimpleTreeNode destination) {
        moveImpl(toBeMoved, destination, destination.getChildCount());
        reloadFromOrder();
    }

    public void moveToRoot(SimpleTreeNode toBeMoved) {
        moveImpl(toBeMoved, getRootAsNode(), 0);
        reloadFromOrder();
    }

    private void moveImpl(SimpleTreeNode toBeMoved, SimpleTreeNode destination,
            int position) {
        if (destination.getChildren().contains(toBeMoved)) {
            return;// it's already moved
        }
        removeNodeImpl(toBeMoved);
        addOrderElementAtImpl(destination, asOrderLine(toBeMoved), position);
    }

    public int[] getPath(SimpleTreeNode destination) {
        int[] path = getPath(getRootAsNode(), destination);
        return path;
    }

    public void up(SimpleTreeNode node) {
        IOrderLineGroup orderLineGroup = asOrderLineGroup(getParent(node));
        orderLineGroup.up(asOrderLine(node));
        reloadFromOrder();
    }

    public void down(SimpleTreeNode node) {
        IOrderLineGroup orderLineGroup = asOrderLineGroup(getParent(node));
        orderLineGroup.down(asOrderLine(node));
        reloadFromOrder();
    }

    private Order getRootAsOrder() {
        return (Order) getRootAsNode().getData();
    }

    private static OrderElement asOrderLine(SimpleTreeNode node) {
        return (OrderElement) node.getData();
    }

    private static IOrderLineGroup asOrderLineGroup(SimpleTreeNode node) {
        return (IOrderLineGroup) node.getData();
    }

    private SimpleTreeNode getRootAsNode() {
        return (SimpleTreeNode) getRoot();
    }

    public void removeNode(SimpleTreeNode value) {
        removeNodeImpl(value);
        reloadFromOrder();
    }

    private void removeNodeImpl(SimpleTreeNode value) {
        if (value == getRootAsNode())
            return;
        SimpleTreeNode parent = getParent(value);
        IOrderLineGroup orderLineGroup = asOrderLineGroup(parent);
        orderLineGroup.remove(asOrderLine(value));

        // If removed node was the last one and its parent is not the root node
        if (!getRootAsNode().equals(parent)
                && parent.getChildCount() == 1) {
            // Convert parent node (container) to an orderline (leaf)
            IOrderLineGroup parentContainer = asOrderLineGroup(getParent(parent));
            OrderElement asOrderLine = ((OrderElement) orderLineGroup).toLeaf();
            parentContainer.replace((OrderElement)orderLineGroup, asOrderLine);
        }
    }
}
