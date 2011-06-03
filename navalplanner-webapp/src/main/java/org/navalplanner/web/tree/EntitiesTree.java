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
package org.navalplanner.web.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.navalplanner.business.trees.ITreeNode;
import org.navalplanner.business.trees.ITreeParentNode;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.ganttz.util.MutableTreeModel.IChildrenExtractor;
import org.zkoss.zul.TreeModel;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
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
        treeModel.add(parent, children, EntitiesTree
                .<T> createChildrenExtractor());
        return treeModel;
    }

    private static <T extends ITreeNode<T>> IChildrenExtractor<T> createChildrenExtractor() {
        return new IChildrenExtractor<T>() {

            @Override
            public List<? extends T> getChildren(T parent) {
                return parent.getChildren();
            }
        };
    }

    private static <T extends ITreeNode<T>> MutableTreeModel<T> createFilteredTreeFrom(
            Class<T> type, T tree, List<T> elements) {
        MutableTreeModel<T> treeModel = MutableTreeModel.create(type, tree);
        T parent = treeModel.getRoot();
        addFilteredChildren(treeModel, parent, elements);
        return treeModel;
    }

    private static <T extends ITreeNode<T>> void addFilteredChildren(
            MutableTreeModel<T> treeModel, T parent, List<T> children) {
        for (T each : children) {
            if ((each.getParent() != null) && (each.getParent().equals(parent))) {
                treeModel.add(parent, each);
                addFilteredChildren(treeModel, each, children);
            }
        }
    }

    private MutableTreeModel<T> tree;

    protected EntitiesTree(Class<T> type, T root) {
        tree = createTreeFrom(type, root);
    }

    protected EntitiesTree(Class<T> type, T root, List<T> elements) {
        tree = createFilteredTreeFrom(type, root, elements);
    }

    public TreeModel asTree() {
        return tree;
    }

    public T getRoot() {
        return tree.getRoot();
    }

    public void addElement() {
        addElementAtImpl(tree.getRoot());
    }

    public void addElement(String name, int hours) {
        addElementAtImpl(tree.getRoot(), name, hours);
    }

    public void addElementAt(T node) {
        addElementAtImpl(node);
    }

    public void addElementAt(T node, String name, int hours) {
        addElementAtImpl(node, name, hours);
    }

    protected abstract T createNewElement();

    protected abstract T createNewElement(String name, int hours);

    private void addElementAtImpl(T parent) {
        addOrderElementAt(parent, createNewElement());
    }

    private void addElementAtImpl(T parent, String name, int hours) {
        addOrderElementAt(parent, createNewElement(name, hours));
    }

    private void addToTree(ITreeNode<T> parentNode, ITreeNode<T> elementToAdd) {
        tree.add(parentNode.getThis(), Collections.singletonList(elementToAdd
                .getThis()),
                childrenExtractor());
    }

    private void addToTree(ITreeNode<T> parentNode, int position,
            ITreeNode<T> elementToAdd) {
        List<T> children = Collections.singletonList(elementToAdd.getThis());
        tree.add(parentNode.getThis(), position, children, childrenExtractor());
    }

    private void addOrderElementAt(ITreeNode<T> parent, ITreeNode<T> element) {
        ITreeParentNode<T> container = turnIntoContainerIfNeeded(parent);
        container.add(element.getThis());
        addToTree(container.getThis(), element);
    }

    private void addOrderElementAt(ITreeNode<T> destinationNode,
            ITreeNode<T> elementToAdd, int position) {
        ITreeParentNode<T> container = turnIntoContainerIfNeeded(destinationNode);
        container.add(position, elementToAdd.getThis());
        addToTree(container, position, elementToAdd);
        if (!tree.isRoot(container.getThis())) {
            // the destination node might have data that depends on its
            // children, so it should be redrawn
            tree.sendContentsChangedEventFor(container.getThis());
        }
    }

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
        asContainer.add(selectedForTurningIntoContainer.getThis());
        tree.replace(selectedForTurningIntoContainer.getThis(), asContainer
                .getThis(), childrenExtractor());
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
        T destination = getChildren(parentOfSelected).get(position - 1);
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
        // if the last child of parent in unindented parent is replaced by its
        // representation as leaf so no longer would be found. Keeping track of
        // the position
        int[] parentNodePath = tree.getPath(getRoot(), parent);
        T destination = tree.getParent(parent);
        move(nodeToUnindent, destination, getChildren(destination).indexOf(
                parent) + 1);

        if (!tree.contains(parent)) {
            parent = tree.findObjectAt(parentNodePath);
        }
        if (!tree.isRoot(parent)) {
            tree.sendContentsChangedEventFor(parent);
        }
    }

    private class WithPosition {
        int position;
        T element;

        private WithPosition(int position, T element) {
            this.position = position;
            this.element = element;
        }
    }

    public void addNewlyAddedChildrenOf(ITreeParentNode<T> parent) {
        List<T> treeChildren = getTreeChildren(parent);
        List<T> currentChildren = parent.getChildren();
        if (!currentChildren.containsAll(treeChildren)) {
            throw new IllegalStateException(
                    "some children were removed. Can't add new tree children");
        }
        int i = 0;
        List<WithPosition> addings = new ArrayList<WithPosition>();
        for (T each : currentChildren) {
            if (!treeChildren.contains(each)) {
                addings.add(new WithPosition(i, each));
            }
            i++;
        }
        for (WithPosition each : addings) {
            tree.add(parent.getThis(), each.position, Collections
                    .singletonList(each.element), childrenExtractor());
        }
    }

    private IChildrenExtractor<T> childrenExtractor() {
        return EntitiesTree.<T> createChildrenExtractor();
    }

    private List<T> getTreeChildren(ITreeParentNode<T> parent) {
        List<T> result = new ArrayList<T>();
        int childCount = tree.getChildCount(parent);
        for (int i = 0; i < childCount; i++) {
            result.add(tree.getChild(parent, i));
        }
        return result;
    }

    public void move(T toBeMoved, T destination) {
        move(toBeMoved, destination, getChildren(destination).size());
    }

    public void moveToRoot(T toBeMoved) {
        move(toBeMoved, tree.getRoot(), 0);
    }

    private void move(T toBeMoved, T destination, int position) {
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

    public int[] getPath(T element) {
        return tree.getPath(tree.getRoot(), element);
    }

}
