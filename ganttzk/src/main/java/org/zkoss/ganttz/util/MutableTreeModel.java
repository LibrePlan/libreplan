package org.zkoss.ganttz.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.event.TreeDataEvent;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class MutableTreeModel<T> extends AbstractTreeModel {

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

    private Map<T, Node<T>> nodesByDomainObject = new WeakHashMap<T, Node<T>>();

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
        if (type == null)
            throw new IllegalArgumentException("type cannot be null");
        nodesByDomainObject.put(unwrap(root), root);
        this.root = root;
    }

    @Override
    public int[] getPath(Object parent, Object last) {
        Node<T> parentNode = find(parent);
        Node<T> lastNode = find(last);
        if (parentNode == null || lastNode == null)
            return new int[0];
        List<Integer> path = lastNode.until(parentNode);
        return asIntArray(path);
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
        if (children.isEmpty())
            return;
        int indexFrom = position == null ? parent.children.size() : position;
        int indexTo = indexFrom + children.size() - 1;
        parent.addAll(position, children);
        addToNodesAndDomainMapping(children);
        fireEvent(unwrap(parent), indexFrom, indexTo,
                TreeDataEvent.INTERVAL_ADDED);
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

    public void add(T parent, int position, Collection<? extends T> children) {
        add(find(parent), position, wrap(children));
    }

    public void add(T parent, Collection<? extends T> children) {
        Node<T> parentNode = find(parent);
        add(parentNode, null, wrap(children));
    }

    public void remove(T node) {
        Node<T> found = find(node);
        if (found.isRoot())
            throw new IllegalArgumentException(node
                    + " is root. It can't be removed");
        int positionInParent = found.remove();
        nodesByDomainObject.remove(node);
        fireEvent(unwrap(found.parentNode), positionInParent, positionInParent,
                TreeDataEvent.INTERVAL_REMOVED);
    }

    public T getParent(T node) {
        Node<T> associatedNode = find(node);
        if (associatedNode.equals(root))
            throw new IllegalArgumentException(node + " is root");
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

    public void replace(T nodeToRemove, T nodeToAdd) {
        T parent = getParent(nodeToRemove);
        Node<T> parentNode = find(parent);
        final int insertionPosition = parentNode.getIndexOf(find(nodeToRemove));
        remove(nodeToRemove);
        List<T> toAdd = new ArrayList<T>();
        toAdd.add(nodeToAdd);
        add(parent, insertionPosition, toAdd);
    }

}
