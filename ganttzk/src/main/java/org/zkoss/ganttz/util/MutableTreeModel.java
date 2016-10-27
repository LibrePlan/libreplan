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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.event.TreeDataEvent;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 * @author Bogdan Bodnarjuk <b.bodnarjuk@libreplan-enterprise.com>
 */

public class MutableTreeModel<T> extends AbstractTreeModel {

    private static final Log LOG = LogFactory.getLog(MutableTreeModel.class);

    public interface IChildrenExtractor<T> {

        List<? extends T> getChildren(T parent);

    }

    public static class Node<T> {

        private T value;

        private List<Node<T>> children = new LinkedList<>();

        private Node<T> parentNode;

        public Node(T value) {
            this.value = value;
        }

        public void addAll(Integer position, List<Node<T>> nodes) {
            for (Node<T> n : nodes) {
                n.parentNode = this;
            }

            if ( position == null ) {
                children.addAll(nodes);
            } else {
                children.addAll(position, nodes);
            }
        }

        public int[] down(Node<T> node) {
            ListIterator<Node<T>> listIterator = children.listIterator();

            while (listIterator.hasNext()) {
                Node<T> current = listIterator.next();

                if ( current == node && listIterator.hasNext() ) {
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
            ListIterator<Node<T>> listIterator = children.listIterator(children.size());

            while ( listIterator.hasPrevious() ) {
                Node<T> current = listIterator.previous();

                if ( current == node && listIterator.hasPrevious() ) {
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
            if ( !parent.equals(this) ) {

                if ( isRoot() ) {

                    /* Final reached, but parent not found */
                    result.clear();

                } else {
                    result.add(0, this.parentNode.getIndexOf(this));
                    this.parentNode.until(result, parent);
                }
            }

        }

        private boolean isRoot() {
            return parentNode == null;
        }

        private int getIndexOf(Node<T> child) {
            return children.indexOf(child);
        }

        public LinkedList<Integer> until(Node<T> parent) {
            LinkedList<Integer> result = new LinkedList<>();
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

    private transient Map<T, Node<T>> nodesByDomainObject = new WeakHashMap<>();

    private static <T> Node<T> wrapOne(T object) {
        return new Node<>(object);
    }

    private static <T> List<Node<T>> wrap(T... objects) {
        return wrap(Arrays.asList(objects));
    }

    private static <T> List<Node<T>> wrap(Collection<? extends T> objects) {
        List<Node<T>> result = new ArrayList<>();

        for (T o : objects) {
            result.add(wrapOne(o));
        }

        return result;
    }

    private Node<T> find(Object domainObject) {
        for (Map.Entry<T, Node<T>> item : nodesByDomainObject.entrySet()) {
            if ( item.getKey() != null && item.getKey().equals(domainObject) ) {
                return item.getValue();
            }
        }

        return nodesByDomainObject.get(domainObject);
    }

    private static <T> T unwrap(Node<T> node) {
        return node == null ? null : node.value;
    }

    public static <T> MutableTreeModel<T> create(Class<T> type) {
        return new MutableTreeModel<>(type, new Node<>(null));
    }

    public static <T> MutableTreeModel<T> create(Class<T> type, T root) {
        return new MutableTreeModel<>(type, wrapOne(root));
    }

    private MutableTreeModel(Class<T> type, Node<T> root) {
        super(root);

        if ( type == null ) {
            throw new IllegalArgumentException("type cannot be null");
        }
        nodesByDomainObject.put(unwrap(root), root);
        this.root = root;
    }

    /**
     * Is some cases it was returning new int[0], but should return new path instead.
     * Reason of that: API changes. Before it was looking for child index manually.
     * Now it is not looking at all.
     * So I decided to return value by our own
     * {@link MutableTreeModel#shouldILookForLastValue(Object, Node), {@link #shouldILookForParentValue(Object, Node)}}
     * Not to use {@link AbstractTreeModel#getIndexOfChild(Object parent, Object child)}.
     */
    public int[] getPath(Object parent, Object last) {
        Node<T> parentNode = find(parent);
        Node<T> lastNode = find(last);

        if ( shouldILookForParentValue(parent, parentNode) ) {
            parentNode = find( ((Node) parent).value );
        }

        if ( shouldILookForLastValue(last, lastNode) ) {
            lastNode = find( ((Node) last).value );
        }

        if ( parentNode == null || lastNode == null)  {
            return new int[0];
        }
        List<Integer> path = lastNode.until(parentNode);

        return asIntArray(path);
    }

    private boolean shouldILookForParentValue(Object parent, Node<T> parentNode) {
        return parent != null &&
                parentNode == null &&
                parent.getClass().toString().contains("Node") &&
                ((Node) parent).value != null;
    }
    private boolean shouldILookForLastValue(Object last, Node<T> lastNode) {
        return last != null &&
                lastNode == null &&
                last.getClass().toString().contains("Node") &&
                ((Node) last).value != null;
    }

    @Override
    public int[] getPath(Object last) {
        return getPath(getRoot(), last);
    }

    public T findObjectAt(int... path) {
        T current = getRoot();
        for (int i = 0; i < path.length; i++) {
            int position = path[i];

            if ( position >= getChildCount(current) ) {

                throw new IllegalArgumentException(
                        "Failure acessing the path at: " + stringRepresentationUntil(path, i));
            }
            current = getChild(current, position);
        }

        return current;
    }

    private static String stringRepresentationUntil(int[] path, int endExclusive) {
        String valid = Arrays.toString(Arrays.copyOfRange(path, 0, endExclusive));
        String invalid = Arrays.toString(Arrays.copyOfRange(path, endExclusive, path.length));

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
    public T getChild(int[] path){
        T node = getRoot();

        for (int item : path) {
            if (item < 0 || item > _childCount(node))
                return null;

            node = getChild(node, item);
        }

        return node;
    }

    private int _childCount(T parent) {
        return isLeaf(parent) ? 0 : getChildCount(parent);
    }

    /**
     * Previously index was correct,
     * because ZK API was calling {@link AbstractTreeModel#getIndexOfChild(Object, Object)} method.
     * Now it is not calling that method and sometimes index could be incorrect.
     * So I decided to make --index if it will throw exception.
     */
    @Override
    public T getChild(Object parent, int index) {
        Node<T> node;

        if (parent instanceof MutableTreeModel.Node) {
            node = find(((Node) parent).value);
        } else {
            node = find(parent);
        }

        T nodeToReturn;

        try {
            nodeToReturn = unwrap(node.children.get(index));
        } catch (IndexOutOfBoundsException e) {
            if (parent != null) {
                nodeToReturn = unwrap(node.parentNode.children.get(index));
            } else if (index - 1 >= 0) {
                nodeToReturn = unwrap(node.children.get(index - 1));
            } else {
                throw new IndexOutOfBoundsException("Something wrong with indexes");
            }
        }

        return nodeToReturn;
    }

    @Override
    public int getChildCount(Object parent) {
        Node<T> node;

        if (parent instanceof MutableTreeModel.Node) {
            node = find(((Node) parent).value);
        } else {
            node = find(parent);
        }

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
        return parent -> Collections.emptyList();
    }

    private void add(Node<T> parent, Integer position, List<Node<T>> children, IChildrenExtractor<T> extractor) {
        if ( children.isEmpty() ) {
            return;
        }

        int indexFrom = position == null ? parent.children.size() : position;
        int indexTo = indexFrom + children.size() - 1;
        addWithoutSendingEvents(parent, position, children, extractor);
        fireEvent(TreeDataEvent.INTERVAL_ADDED, getPath(parent), indexFrom, indexTo);
    }

    private void addWithoutSendingEvents(Node<T> parent,
                                         Integer position,
                                         List<Node<T>> children,
                                         IChildrenExtractor<T> extractor) {

        parent.addAll(position, children);
        addToNodesAndDomainMapping(children);

        for (Node<T> each : children) {
            T value = each.value;
            addWithoutSendingEvents(each, 0, wrap(extractor.getChildren(value)), extractor);
        }
    }

    private void addToNodesAndDomainMapping(Collection<Node<T>> children) {
        for (Node<T> child : children) {
            nodesByDomainObject.put(unwrap(child), child);
        }
    }

    public void add(T parent, T child) {
        ArrayList<T> children = new ArrayList<>();
        children.add(child);
        add(parent, children);
    }

    public void sendContentsChangedEventFor(T object) {
        Node<T> node = find(object);
        T parent = getParent(object);
        Node<T> parentNode = find(parent);
        int position = parentNode.getIndexOf(node);
        fireEvent(TreeDataEvent.CONTENTS_CHANGED,getPath(parent), position, position);
    }

    public void add(T parent, int position, Collection<? extends T> children) {
        add(find(parent), position, wrap(children));
    }

    public void add(T parent, Collection<? extends T> children) {
        Node<T> parentNode = find(parent);
        add(parentNode, null, wrap(children));
    }

    public void add(T parent, int position, Collection<? extends T> children, IChildrenExtractor<T> childrenExtractor) {
        add(find(parent), position, wrap(children), childrenExtractor);
    }

    public void add(T parent, Collection<? extends T> children, IChildrenExtractor<T> childrenExtractor) {
        add(find(parent), null, wrap(children), childrenExtractor);
    }

    public void remove(T node) {
        Node<T> found = find(node);

        if ( found.isRoot() ) {
            throw new IllegalArgumentException(node + " is root. It can't be removed");
        }

        int positionInParent = found.remove();
        nodesByDomainObject.remove(node);
        fireEvent(TreeDataEvent.INTERVAL_REMOVED, getPath(found.parentNode), positionInParent, positionInParent);
    }

    public T getParent(T node) {
        Node<T> associatedNode = find(node);
        if ( associatedNode.equals(root) ) {
            throw new IllegalArgumentException(node + " is root");
        }

        return unwrap(associatedNode.getParent());
    }

    public List<T> getParents(T node) {
        ArrayList<T> result = new ArrayList<>();

        try {
            T current = node;

            while ( !isRoot(current) ) {
                current = getParent(current);
                result.add(current);
            }
        } catch (Exception e) {
            LOG.error("Trying to get the parent of a removed node", e);
        }

        return result;
    }

    public boolean isRoot(T node) {
        return find(node).isRoot();
    }

    public void replace(T nodeToRemove, T nodeToAdd, IChildrenExtractor<T> childrenExtractor) {
        T parent = getParent(nodeToRemove);
        Node<T> parentNode = find(parent);
        final int insertionPosition = parentNode.getIndexOf(find(nodeToRemove));
        remove(nodeToRemove);

        if ( childrenExtractor != null ) {
            add(parent, insertionPosition, Collections.singletonList(nodeToAdd), childrenExtractor);
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

        if ( changed.length != 0 ) {
            fireRecreationOfInterval(parentNode, changed[0], changed[1]);
        }
    }

    public void up(T node) {
        T parent = getParent(node);
        Node<T> parentNode = find(parent);
        int[] changed = parentNode.up(find(node));

        if ( changed.length != 0 ) {
            fireRecreationOfInterval(parentNode, changed[0], changed[1]);
        }
    }

    private void fireRecreationOfInterval(Node<T> parentNode, int start, int endInclusive) {
        fireEvent(TreeDataEvent.INTERVAL_REMOVED,getPath(parentNode.value), start, endInclusive);
        fireEvent(TreeDataEvent.INTERVAL_ADDED, getPath(parentNode.value), start, endInclusive);
    }

    public boolean isEmpty() {
        return getChildCount(getRoot()) == 0;
    }

    public boolean contains(T object) {
        return find(object) != null;
    }

    public boolean contains(T parent, T child) {
        Node<T> parentNode = find(parent);
        Node<T> childNode = find(child);

        return parentNode != null &&
                childNode != null &&
                childNode.getParent() != null &&
                childNode.getParent().equals(parentNode);
    }

    public List<T> asList() {
        List<T> result = new ArrayList<>();
        asList(getRoot(), result);

        return result;
    }

    private void asList(T root, List<T> result) {
        List<T> list = new ArrayList<>();

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
