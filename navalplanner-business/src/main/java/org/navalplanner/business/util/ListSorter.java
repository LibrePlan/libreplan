package org.navalplanner.business.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.Validate;

/**
 * This class wraps a list that is kept sorted if and only if modify method is
 * called as modifications are done. There cannot be elements a and b at
 * different positions such as a == b.
 * <p>
 * No null elements allowed
 * <p>
 * This class it NOT THREAD SAFE
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ListSorter<T> {

    private final List<T> list;
    private final Comparator<T> comparator;

    public static <T extends Comparable<T>> ListSorter<T> create(
            Collection<T> elements) {
        return create(elements, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }
        });
    }

    public static <T> ListSorter<T> create(Collection<T> elements,
            Comparator<T> comparator) {
        Validate.notNull(elements);
        Validate.notNull(comparator);
        Validate.noNullElements(elements);
        return new ListSorter<T>(elements, comparator);
    }

    private ListSorter(Collection<? extends T> elements,
            Comparator<T> comparator) {
        this.comparator = comparator;
        this.list = new ArrayList<T>(elements);
        Collections.sort(this.list, this.comparator);
    }

    /**
     * Called to retrieve a view to a list that will be kept sorted and updated
     * with modifications
     * @return an unmodifiable view of the list. It's sorted
     */
    public List<T> toListView() {
        return Collections.unmodifiableList(list);
    }

    /**
     * Repositions the element since element could have been changed
     * @param element
     *            the element to be repositioned
     * @throws NoSuchElementException
     *             if there is no element e in list such e == element
     */
    public void modified(T element) throws NoSuchElementException {
        int index = indexOfElement(element);
        if ((index == list.size()))
            throw new NoSuchElementException("not found: " + element);
        list.remove(index);
        insert(element);
    }

    private void insert(T element) {
        int position = Collections.binarySearch(list, element, comparator);
        if (position < 0) {
            position = (-position) - 1;
        }
        list.add(position, element);
    }

    private int indexOfElement(T element) {
        int index = 0;
        for (T t : list) {
            if (t == element)
                break;
            index++;
        }
        return index;
    }

    /**
     * Adds the element at the right position keeping the order.
     * @throws IllegalArgumentException
     *             if the element already existed
     * @param element
     */
    public void add(T element) {
        Validate.notNull(element);
        if (exists(element))
            throw new IllegalArgumentException(element
                    + " already exists. Duplicateds not allowed");
        insert(element);
    }

    /**
     * @param element
     * @throws IllegalArgumentException
     *             it the element doesn't exist
     */
    public void remove(T element) {
        int position = indexOfElement(element);
        if (position == list.size()) {
            throw new NoSuchElementException(element + " doesn't exist");
        }
        list.remove(position);
    }

    private boolean exists(T element) {
        int index = indexOfElement(element);
        return index < list.size();
    }

}
