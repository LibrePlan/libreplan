/*
 * This file is part of LibrePlan
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
package org.libreplan.business.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Wraps a value with several arbitrary flags.
 *
 * @author Oscar Gonzalez Fernandez <ogonzalez@igalia.com>
 *
 * @param <T>
 *            the value that can have flags associated with it
 * @param <F>
 *            the type of the flags
 */
public class Flagged<T, F> {

    public static <T, F> Flagged<T, F> justValue(T value) {
        return new Flagged<T, F>(value, new HashSet<F>());
    }

    public static <T, F> Flagged<T, F> withFlags(T value, F... flags) {
        return new Flagged<T, F>(value, new HashSet<F>(Arrays.asList(flags)));
    }

    private final T value;

    private final Set<F> flags;

    private Flagged(T value, Set<F> flags) {
        this.value = value;
        this.flags = Collections.unmodifiableSet(flags);
    }

    public Flagged<T, F> withFlag(F flag) {
        Set<F> newFlags = new HashSet<F>(flags);
        newFlags.add(flag);
        return new Flagged<T, F>(value, newFlags);
    }

    public Flagged<T, F> withoutFlag(F flag) {
        Set<F> newFlags = new HashSet<F>(flags);
        newFlags.remove(flag);
        return new Flagged<T, F>(value, newFlags);
    }

    public T getValue() {
        return value;
    }

    public boolean isFlagged() {
        return !this.flags.isEmpty();
    }

    public Set<? extends F> getFlags() {
        return this.flags;
    }

    public boolean isFlaggedWith(F flag){
        return this.flags.contains(flag);
    }

    public boolean isFlaggedWithSomeOf(F... flags) {
        for (F each : flags) {
            if (this.isFlaggedWith(each)) {
                return true;
            }
        }
        return false;
    }

}
