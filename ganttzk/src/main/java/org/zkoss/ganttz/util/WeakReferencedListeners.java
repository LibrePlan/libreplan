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
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.Validate;

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

    public enum Mode {
        RECEIVE_PENDING, FROM_NOW_ON;
    }

    public void addListener(T listener) {
        this.addListener(listener, Mode.FROM_NOW_ON);
    }

    public synchronized void addListener(T listener, Mode mode) {
        Validate.notNull(listener);

        if (getActiveListeners().isEmpty() && mode == Mode.RECEIVE_PENDING) {
            notifyPendingOfNotificationTo(listener);
        }
        listeners.add(new WeakReference<T>(listener));
    }

    private List<IListenerNotification<? super T>> pendingOfNotification = new ArrayList<WeakReferencedListeners.IListenerNotification<? super T>>();

    private void notifyPendingOfNotificationTo(T listener) {
        for (IListenerNotification<? super T> each : pendingOfNotification) {
            each.doNotify(listener);
        }
        pendingOfNotification.clear();
    }

    public synchronized void fireEvent(
            IListenerNotification<? super T> notification) {
        List<T> active = getActiveListeners();

        for (T listener : active) {
            notification.doNotify(listener);
        }

        if (active.isEmpty()) {
            pendingOfNotification.add(notification);
        }
    }

    private List<T> getActiveListeners() {
        ListIterator<WeakReference<T>> listIterator = listeners.listIterator();
        List<T> result = new ArrayList<T>();
        while (listIterator.hasNext()) {
            T listener = listIterator.next().get();
            if (listener == null) {
                listIterator.remove();
            } else {
                result.add(listener);
            }
        }
        return result;
    }
}
