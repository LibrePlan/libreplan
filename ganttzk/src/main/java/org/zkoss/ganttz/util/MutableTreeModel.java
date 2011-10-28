/*
 * This file is part of LibrePlan
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

package org.zkoss.ganttz.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.event.TreeDataEvent;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class MutableTreeModel<T> extends AbstractTreeModel {

    public interface IChildrenExtractor<T> {

        public List<? extends T> getChildren(T parent);

    }

    private static class Node<T> {
        private T value;

        private List<Node<T>> children = new LinkedList<Node<T>>();

        private Node<T> parentNode;

        private Node(T value) {
            this.value = value;
        }

        public void addAll(Integer position, List<Node<T>> nodes) {
            for (Node<T> n : nodes) {
                n.parentNode = this;
            }
            if (position == null) {
                children.addAll(nodes);
            } else {
                children.addAll(position, nodes);
            }
        }

        public int[] down(Node<T> node) {
            ListIterator<Node<T>> listIterator = children.listIterator();
            while (listIterator.hasNext()) {
                Node<T> current = listIterator.next();
                if (current == node && listIterator.hasNext()) {
                    int nextIndex = listIterator.nextIndex();
                    listIterator.remove();
                    listIterator.next();
                    listIterator.add(node);
                    return new int[] { nextIndex - 1, nextIndex };
                }
            }
            return new int[] {};
        }

        public int[] up(Node<T> node) {
            ListIterator<Node<T>> listIterator = children.listIterator(children
                    .size());
            while (listIterator.hasPrevious()) {
                Node<T> current = listIterator.previous();
                if (current == node && listIterator.hasPrevious()) {
                    listIterator.remove();
                    int previousIndex = listIterator.previousIndex();
                    listIterator.previous();
                    listIterator.add(current);
                    return new int[] { previousIndex, previousIndex + 1 };
                }
            }
            return new int[] {};
        }

        private void until(LinkedList<Integer> result, Node<T> parent) {
            if (parent.equals(this)) {
                return;
            } else if (isRoot()) {
                // final reached, but parent not found
                result.clear();
            } else {
                result.add(0, this.parentNode.getIndexOf(this));
                this.parentNode.until(result, parent);
            }

        }

        private boolean isRoot() {
            return parentNode == null;
        }

        private int getIndexOf(Node<T> child) {
            return children.indexOf(child);
        }

        public LinkedList<Integer> until(Node<T> parent) {
            LinkedList<Integer> result = new LinkedList<Integer>();
            until(result, parent);
            return result;
        }

        public int remove() {
            int positionInParent = parentNode.getIndexOf(this);
            parentNode.children.remove(positionInParent);
            return positionInParent;
        }

        public Node<T> getParent() {
            return parentNode;
        }

    }

    private final Node<T> root;

    private transient Map<T, Node<T>> nodesByDomainObject = new WeakHashMap<T, Node<T>>();

    private static <T> Node<T> wrapOne(T object) {
        return new Node<T>(object);
    }

    private static <T> List<Node<T>> wrap(T... objects) {
        return wrap(Arrays.asList(objects));
    }

    private static <T> List<Node<T>> wrap(Collection<? extends T> objects) {
        List<Node<T>> result = new ArrayList<Node<T>>();
        for (T o : objects) {
            result.add(wrapOne(o));
        }
        return result;
    }

    private Node<T> find(Object domainObject) {
        return nodesByDomainObject.get(domainObject);
    }

    private static <T> T unwrap(Node<T> node) {
        return node == null ? null : node.value;
    }

    public static <T> MutableTreeModel<T> create(Class<T> type) {
        return new MutableTreeModel<T>(type, new Node<T>(null));
    }

    public static <T> MutableTreeModel<T> create(Class<T> type, T root) {
        return new MutableTreeModel<T>(type, wrapOne(root));
    }

    private MutableTreeModel(Class<T> type, Node<T> root) {
        super(root);
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        nodesByDomainObject.put(unwrap(root), root);
        this.root = root;
    }

    @Override
    public int[] getPath(Object parent, Object last) {
        Node<T> parentNode = find(parent);
        Node<T> lastNode = find(last);
        if (parentNode == null || lastNode == null) {
            return new int[0];
        }
        List<Integer> path = lastNode.until(parentNode);
        return asIntArray(path);
    }

    public int[] getPath(Object last) {
        return getPath(getRoot(), last);
    }

    public T findObjectAt(int... path) {
        T current = getRoot();
        for (int i = 0; i < path.length; i++) {
            int position = path[i];
            if (position >= getChildCount(current)) {
                throw new IllegalArgumentException(
                        "Failure acessing the path at: "
                                + stringRepresentationUntil(path, i));
            }
            current = getChild(current, position);
        }
        return current;
    }

    private static String stringRepresentationUntil(int[] path, int endExclusive) {
        String valid = Arrays.toString(Arrays
                .copyOfRange(path, 0, endExclusive));
        String invalid = Arrays.toString(Arrays.copyOfRange(path, endExclusive,
                path.length));
        return valid + "^" + invalid;
    }

    private int[] asIntArray(List<Integer> path) {
        int[] result = new int[path.size()];
        int i = 0;
        for (Integer integer : path) {
            result[i++] = integer;
        }
        return result;
    }

    @Override
    public T getRoot() {
        return unwrap(root);
    }

    @Override
    public T getChild(Object parent, int index) {
        Node<T> node = find(parent);
        return unwrap(node.children.get(index));
    }

    @Override
    public int getChildCount(Object parent) {
        Node<T> node = find(parent);
        return node.children.size();
    }

    @Override
    public boolean isLeaf(Object object) {
        Node<T> node = find(object);
        return node.children.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public void addToRoot(T child) {
        add(root, null, wrap(child));
    }

    private void add(Node<T> parent, Integer position, List<Node<T>> children) {
        add(parent, position, children, noChildrenExtractor());
    }

    private IChildrenExtractor<T> noChildrenExtractor() {
        return new IChildrenExtractor<T>() {

            @Override
            public List<? extends T> getChildren(T parent) {
                return Collections.emptyList();
            }
        };
    }

    private void add(Node<T> parent, Integer position, List<Node<T>> children,
            IChildrenExtractor<T> extractor) {
        if (children.isEmpty()) {
            return;
        }
        int indexFrom = position == null ? parent.children.size() : position;
        int indexTo = indexFrom + children.size() - 1;
        addWithoutSendingEvents(parent, position, children, extractor);
        fireEvent(unwrap(parent), indexFrom, indexTo,
                TreeDataEvent.INTERVAL_ADDED);
    }

    private void addWithoutSendingEvents(Node<T> parent, Integer position,
            List<Node<T>> children, IChildrenExtractor<T> extractor) {
        parent.addAll(position, children);
        addToNodesAndDomainMapping(children);
        for (Node<T> each : children) {
            T value = each.value;
            addWithoutSendingEvents(each, 0,
                    wrap(extractor.getChildren(value)), extractor);
        }
    }

    private void addToNodesAndDomainMapping(Collection<Node<T>> children) {
        for (Node<T> child : children) {
            nodesByDomainObject.put(unwrap(child), child);
        }
    }

    public void add(T parent, T child) {
        ArrayList<T> children = new ArrayList<T>();
        children.add(child);
        add(parent, children);
    }

    public void sendContentsChangedEventFor(T object) {
        Node<T> node = find(object);
        T parent = getParent(object);
        Node<T> parentNode = find(parent);
        int position = parentNode.getIndexOf(node);
        fireEvent(parent, position, position, TreeDataEvent.CONTENTS_CHANGED);
    }

    public void add(T parent, int position, Collection<? extends T> children) {
        add(find(parent), position, wrap(children));
    }

    public void add(T parent, Collection<? extends T> children) {
        Node<T> parentNode = find(parent);
        add(parentNode, null, wrap(children));
    }

    public void add(T parent, int position, Collection<? extends T> children,
            IChildrenExtractor<T> childrenExtractor) {
        add(find(parent), position, wrap(children), childrenExtractor);
    }

    public void add(T parent, Collection<? extends T> children,
            IChildrenExtractor<T> childrenExtractor) {
        add(find(parent), null, wrap(children), childrenExtractor);
    }

    public void remove(T node) {
        Node<T> found = find(node);
        if (found.isRoot()) {
            throw new IllegalArgumentException(node
                    + " is root. It can't be removed");
        }
        int positionInParent = found.remove();
        nodesByDomainObject.remove(node);
        fireEvent(unwrap(found.parentNode), positionInParent, positionInParent,
                TreeDataEvent.INTERVAL_REMOVED);
    }

    public T getParent(T node) {
        Node<T> associatedNode = find(node);
        if (associatedNode.equals(root)) {
            throw new IllegalArgumentException(node + " is root");
        }
        return unwrap(associatedNode.getParent());
    }

    public List<T> getParents(T node) {
        ArrayList<T> result = new ArrayList<T>();
        T current = node;
        while (!isRoot(current)) {
            current = getParent(current);
            result.add(current);
        }
        return result;
    }

    public boolean isRoot(T node) {
        Node<T> associatedNode = find(node);
        return associatedNode.isRoot();
    }

    public void replace(T nodeToRemove, T nodeToAdd,
            IChildrenExtractor<T> childrenExtractor) {
        T parent = getParent(nodeToRemove);
        Node<T> parentNode = find(parent);
        final int insertionPosition = parentNode.getIndexOf(find(nodeToRemove));
        remove(nodeToRemove);
        if (childrenExtractor != null) {
            add(parent, insertionPosition,
                    Collections.singletonList(nodeToAdd), childrenExtractor);
        } else {
            add(parent, insertionPosition, Collections.singletonList(nodeToAdd));
        }
    }

    public void replace(T nodeToRemove, T nodeToAdd) {
        replace(nodeToRemove, nodeToAdd, null);
    }

    public void down(T node) {
        T parent = getParent(node);
        Node<T> parentNode = find(parent);
        int[] changed = parentNode.down(find(node));
        if (changed.length != 0) {
            fireRecreationOfInterval(parentNode, changed[0], changed[1]);
        }
    }

    public void up(T node) {
        T parent = getParent(node);
        Node<T> parentNode = find(parent);
        int[] changed = parentNode.up(find(node));
        if (changed.length != 0) {
            fireRecreationOfInterval(parentNode, changed[0], changed[1]);
        }
    }

    private void fireRecreationOfInterval(Node<T> parentNode, int start,
            int endInclusive) {
        fireEvent(parentNode.value, start, endInclusive,
                TreeDataEvent.INTERVAL_REMOVED);
        fireEvent(parentNode.value, start, endInclusive,
                TreeDataEvent.INTERVAL_ADDED);
    }

    public boolean isEmpty() {
        return getChildCount(getRoot()) == 0;
    }

    public boolean hasChildren(T node) {
        return getChildCount(node) > 0;
    }

    public boolean contains(T object) {
        return find(object) != null;
    }

    public boolean contains(T parent, T child) {
        Node<T> parentNode = find(parent);
        Node<T> childNode = find(child);

        return parentNode != null && childNode != null
                && childNode.getParent() != null
                && childNode.getParent().equals(parentNode);
    }

    public List<T> asList() {
        List<T> result = new ArrayList<T>();
        asList(getRoot(), result);
        return result;
    }

    private void asList(T root, List<T> result) {
        List<T> list = new ArrayList<T>();
        for (int i = 0; i < getChildCount(root); i++) {
            final T child = getChild(root, i);
            list.add(child);
            result.add(child);
        }

        for (T each: list) {
            asList(each, result);
        }
    }
}
