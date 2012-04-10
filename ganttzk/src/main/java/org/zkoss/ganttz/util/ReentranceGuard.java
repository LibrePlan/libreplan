/*
 * This file is part of LibrePlan
 *
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

/**
 * <p>
 * It marks the start and the end part of a potentially reentering execution
 * using a {@link ThreadLocal} variable. For example, some method execution can
 * eventually be called again. When that methods is called we want to know if
 * it's called within the execution of itself or from the outside. I.e., it's
 * useful to do different things depending if the execution is already being
 * done or entering in it.
 * </p>
 *
 * <p>
 * It can detect if it's already executing or not. If it is,
 * {@link IReentranceCases#ifAlreadyInside()} is called, otherwise
 * {@link IReentranceCases#ifNewEntrance()} is called.
 * </p>
 *
 * @author Óscar González Fernández <ogfernandez@gmail.com>
 */
public class ReentranceGuard {

    public interface IReentranceCases {
        public void ifNewEntrance();

        public void ifAlreadyInside();
    }

    private final ThreadLocal<Boolean> inside = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        };
    };

    public void entranceRequested(IReentranceCases reentranceCases) {
        if (inside.get()) {
            reentranceCases.ifAlreadyInside();
            return;
        }
        inside.set(true);
        try {
            reentranceCases.ifNewEntrance();
        } finally {
            inside.set(false);
        }
    }
}