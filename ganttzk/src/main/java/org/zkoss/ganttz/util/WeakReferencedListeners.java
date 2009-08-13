package org.zkoss.ganttz.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class WeakReferencedListeners<T> {

    public interface IListenerNotification<T> {

        void doNotify(T listener);

    }

    public static <T> WeakReferencedListeners<T> create() {
        return new WeakReferencedListeners<T>();
    }

    private LinkedList<WeakReference<T>> listeners = new LinkedList<WeakReference<T>>();

    private WeakReferencedListeners() {

    }

    public synchronized void addListener(T listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener cannot be null");
        listeners.add(new WeakReference<T>(listener));
    }

    public synchronized void fireEvent(
            IListenerNotification<? super T> notification) {
        ListIterator<WeakReference<T>> listIterator = listeners.listIterator();
        ArrayList<T> active = new ArrayList<T>();
        while (listIterator.hasNext()) {
            T listener = listIterator.next().get();
            if (listener == null)
                listIterator.remove();
            else {
                active.add(listener);
            }
        }
        for (T listener : active) {
            notification.doNotify(listener);
        }
    }
}
