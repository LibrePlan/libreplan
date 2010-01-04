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
package org.navalplanner.web.orders.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.trees.ITreeNode;
import org.navalplanner.business.trees.ITreeParentNode;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zul.TreeModel;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class EntitiesTree<T extends ITreeNode<T>> {

    protected static <T extends ITreeNode<T>> MutableTreeModel<T> createTreeFrom(
            Class<T> type, T tree) {
        List<T> children = tree.getChildren();
        return createTreeFrom(type, tree, children);
    }

    protected static <T extends ITreeNode<T>> MutableTreeModel<T> createTreeFrom(
            Class<T> type, T tree, List<T> children) {
        MutableTreeModel<T> treeModel = MutableTreeModel.create(type, tree);
        T parent = treeModel.getRoot();
        treeModel.add(parent, children);
        addChildren(treeModel, children);
        return treeModel;
    }

    private static <T extends ITreeNode<T>> void addChildren(
            MutableTreeModel<T> treeModel, List<T> children) {
        for (T each : children) {
            treeModel.add(each, each.getChildren());
            addChildren(treeModel, each.getChildren());
        }
    }

    private MutableTreeModel<T> tree;

    protected EntitiesTree(Class<T> type, T root) {
        tree = createTreeFrom(type, root);
    }

    protected EntitiesTree(Class<T> type, T root, List<T> rootChildren) {
        tree = createTreeFrom(type, root, rootChildren);
    }

    public TreeModel asTree() {
        return tree;
    }

    public void addElement() {
        addElementAtImpl(tree.getRoot());
    }

    protected abstract T createNewElement();

    public void addElementAt(T node) {
        addElementAtImpl(node);
    }

    private void addElementAtImpl(T parent) {
        addOrderElementAt(parent, createNewElement());

    }

    private void addToTree(ITreeNode<T> parentNode, ITreeNode<T> elementToAdd) {
        tree.add(parentNode.getThis(), elementToAdd.getThis());
        addChildren(tree, Collections.singletonList(elementToAdd.getThis()));
    }

    private void addToTree(ITreeNode<T> parentNode, int position,
            ITreeNode<T> elementToAdd) {
        List<T> children = Collections
                        .singletonList(elementToAdd.getThis());
        tree.add(parentNode.getThis(), position, children);
        addChildren(tree, children);
    }

    private void addOrderElementAt(ITreeNode<T> parent, ITreeNode<T> element) {
        ITreeParentNode<T> container = turnIntoContainerIfNeeded(parent);
        container.add(element.getThis());
        addToTree(container.getThis(), element);
        added(parent, element, container);
    }

    private void addOrderElementAt(ITreeNode<T> destinationNode,
            ITreeNode<T> elementToAdd, int position) {
        ITreeParentNode<T> container = turnIntoContainerIfNeeded(destinationNode);
        container.add(position, elementToAdd.getThis());
        addToTree(container, position, elementToAdd);
        added(destinationNode, elementToAdd, container);
    }

    protected abstract void added(ITreeNode<T> destination, ITreeNode<T> added,
            ITreeParentNode<T> turnedIntoContainer);

    private ITreeParentNode<T> turnIntoContainerIfNeeded(
            ITreeNode<T> selectedForTurningIntoContainer) {
        if (selectedForTurningIntoContainer instanceof ITreeParentNode) {
            return (ITreeParentNode<T>) selectedForTurningIntoContainer;
        }
        ITreeParentNode<T> parentContainer = getParent(selectedForTurningIntoContainer);
        ITreeParentNode<T> asContainer = selectedForTurningIntoContainer
                .toContainer();
        parentContainer.replace(selectedForTurningIntoContainer.getThis(),
                asContainer.getThis());
        tree.replace(selectedForTurningIntoContainer.getThis(), asContainer
                .getThis());
        addChildren(tree, Collections.singletonList(asContainer.getThis()));
        return asContainer;
    }

    private ITreeParentNode<T> getParent(ITreeNode<T> node) {
        return (ITreeParentNode<T>) tree.getParent(node.getThis());
    }

    public List<T> getParents(T node) {
        return tree.getParents(node);
    }

    public void indent(T nodeToIndent) {
        T parentOfSelected = tree.getParent(nodeToIndent);
        int position = getChildren(parentOfSelected).indexOf(nodeToIndent);
        if (position == 0) {
            return;
        }
        T destination = getChildren(parentOfSelected)
                .get(position - 1);
        move(nodeToIndent, destination, getChildren(destination).size());
    }

    private List<T> getChildren(T node) {
        List<T> result = new ArrayList<T>();
        final int childCount = tree.getChildCount(node);
        for (int i = 0; i < childCount; i++) {
            result.add(tree.getChild(node, i));
        }
        return result;
    }

    public void unindent(T nodeToUnindent) {
        T parent = tree.getParent(nodeToUnindent);
        if (tree.isRoot(parent)) {
            return;
        }
        T destination = tree.getParent(parent);
        move(nodeToUnindent, destination, getChildren(destination).indexOf(
                parent) + 1);
    }

    public void move(T toBeMoved, T destination) {
        move(toBeMoved, destination, getChildren(destination).size());
    }

    public void moveToRoot(T toBeMoved) {
        move(toBeMoved, tree.getRoot(), 0);
    }

    private void move(T toBeMoved, T destination,
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

    private boolean isGreatInHierarchy(T parent, T child) {
        return find(child, getChildren(parent));
    }

    private boolean find(T child, List<T> children) {
        if (children.indexOf(child) >= 0) {
            return true;
        }
        for (T each : children) {
            return find(child, getChildren(each));
        }
        return false;
    }

    public void up(T node) {
        ITreeParentNode<T> parent = getParent(node);
        parent.up(node);
        tree.up(node);
    }

    public void down(T node) {
        ITreeParentNode<T> parent = getParent(node);
        parent.down(node);
        tree.down(node);
    }

    public void removeNode(T element) {
        if (element == tree.getRoot()) {
            return;
        }
        ITreeParentNode<T> parent = getParent(element);
        parent.remove(element);
        tree.remove(element);
        // If removed node was the last one and its parent is not the root node
        if (!tree.isRoot(parent.getThis()) && tree.getChildCount(parent) == 0) {
            T asLeaf = parent.toLeaf();
            ITreeParentNode<T> parentContainer = getParent(parent.getThis());
            parentContainer.replace(parent.getThis(), asLeaf);
            tree.replace(parent.getThis(), asLeaf);
        }
    }

    public int[] getPath(OrderElement orderElement) {
        return tree.getPath(tree.getRoot(), orderElement);
    }

}
