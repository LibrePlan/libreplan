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
package org.navalplanner.business.planner.entities;

import java.util.List;

import org.navalplanner.business.workingday.EffortDuration;

public interface IAllocateEffortOnInterval {

    void allocateHours(int hours);

    void allocate(EffortDuration effortDuration);

    /**
     * <p>
     * It tries to allocate the specified durations on the originally specified
     * interval. It tries to fit them to the interval. If the specified list has
     * less days than the days required by the interval, the end of the list is
     * padded with zeroes. If the specified list has more days than the days
     * required the trailing days are discarded.
     * </p>
     * <p>
     * If the allocation is done within the bounds of the task, the durations
     * specified outside the task's bounds are discarded.
     * </p>
     * <p>
     * If for some day no allocation can't be done, i.e. the day is considered
     * unavailable, the real assignment will be zero.
     * </p>
     * @param durationsByDay
     */
    void allocate(List<EffortDuration> durationsByDay);
}