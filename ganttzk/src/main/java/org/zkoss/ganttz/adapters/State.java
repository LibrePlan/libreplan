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
package org.zkoss.ganttz.adapters;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a value that can be changed. It notifies the interested
 * {@link IValueChangeListener listeners} when the value is changed
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class State<T> {

    public interface IValueChangeListener<T> {
        public void hasChanged(State<T> state);
    }

    public static <T> State<T> create(T value) {
        return new State<T>(value);
    }

    public static State<Void> create() {
        return new State<Void>();
    }

    private List<IValueChangeListener<T>> listeners = new ArrayList<IValueChangeListener<T>>();
    private T value;
    private boolean isVoid = false;

    private State(T value) {
        this.value = value;
    }

    private State() {
        this.value = null;
        isVoid = true;
    }

    public T getValue() {
        return value;
    }

    public void changeValueTo(T value) {
        if (!isVoid && this.value == value) {
            return;
        }
        this.value = value;
        fireChange();
    }

    private void fireChange() {
        for (IValueChangeListener<T> listener : listeners) {
            listener.hasChanged(this);
        }
    }

    public void addListener(IValueChangeListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(IValueChangeListener<?> listener) {
        listeners.remove(listener);
    }

}
