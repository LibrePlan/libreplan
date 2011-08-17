/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
import java.util.List;

/**
 * 
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Emitter<T> {

    public interface IEmissionListener<T> {

        public void newEmission(T value);
    }

    private T lastValue;

    private List<IEmissionListener<? super T>> listeners = new ArrayList<IEmissionListener<? super T>>();

    public static <T> Emitter<T> withInitial(T initialValue) {
        return new Emitter<T>(initialValue);
    }

    private Emitter(T initialValue) {
        this.lastValue = initialValue;
    }

    public T getLastValue() {
        return lastValue;
    }

    public void emit(T value) {
        this.lastValue = value;
        fireListeners(value);
    }

    private void fireListeners(T value) {
        for (IEmissionListener<? super T> each : listeners) {
            each.newEmission(value);
        }
    }

    public void addListener(IEmissionListener<? super T> listener) {
        this.listeners.add(listener);
    }

    public void removeListener(IEmissionListener<? super T> listener) {
        this.listeners.remove(listener);
    }
}
