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
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        listeners.add(new WeakReference<T>(listener));
    }

    public synchronized void fireEvent(
            IListenerNotification<? super T> notification) {
        ListIterator<WeakReference<T>> listIterator = listeners.listIterator();
        ArrayList<T> active = new ArrayList<T>();
        while (listIterator.hasNext()) {
            T listener = listIterator.next().get();
            if (listener == null) {
                listIterator.remove();
            } else {
                active.add(listener);
            }
        }
        for (T listener : active) {
            notification.doNotify(listener);
        }
    }
}
