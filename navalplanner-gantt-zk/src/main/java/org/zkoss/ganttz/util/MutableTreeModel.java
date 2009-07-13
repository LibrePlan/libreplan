package org.zkoss.ganttz.util;

import java.util.ArrayList;
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

        public void add(Node<T> node) {
            node.parentNode = this;
            children.add(node);
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

    private final Class<T> type;

    private final Node<T> root;

    private Map<T, Node<T>> nodesByDomainObject = new WeakHashMap<T, Node<T>>();

    private static <T> Node<T> wrap(T object) {
        return new Node<T>(object);
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
        return new MutableTreeModel<T>(type, wrap(root));
    }

    private MutableTreeModel(Class<T> type, Node<T> root) {
        super(root);
        if (type == null)
            throw new IllegalArgumentException("type cannot be null");
        nodesByDomainObject.put(unwrap(root), root);
        this.type = type;
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

    public void addToRoot(T child) {
        add(root, wrap(child));
    }

    private void add(Node<T> parent, Node<T> child) {
        parent.add(child);
        nodesByDomainObject.put(unwrap(child), child);
        final int position = parent.children.size() - 1;
        fireEvent(unwrap(parent), position, position,
                TreeDataEvent.INTERVAL_ADDED);
    }

    public void add(T parent, T child) {
        add(find(parent), wrap(child));
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

}
