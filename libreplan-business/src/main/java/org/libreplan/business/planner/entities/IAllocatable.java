/*
 * This file is part of LibrePlan
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

package org.libreplan.business.planner.entities;

import org.joda.time.LocalDate;
import org.libreplan.business.workingday.IntraDayDate;

/**
 * This interface represents an object on which an allocation can be done
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IAllocatable extends IAllocateResourcesPerDay {

    public IAllocateResourcesPerDay resourcesPerDayUntil(LocalDate endExclusive);

    public IAllocateResourcesPerDay resourcesPerDayFromEndUntil(LocalDate start);

    /**
     * @see IAllocatable#onIntervalWithinTask(IntraDayDate, IntraDayDate)
     */
    public IAllocateEffortOnInterval onIntervalWithinTask(
            LocalDate startInclusive, LocalDate endExclusive);

    /**
     * <p>
     * It does the allocation in the intersection of the underlying task's
     * bounds and the interval specified. This ensures it can't modify the start
     * and end of the task. The start and end of the allocation can grow, but
     * they can't be shrunk.
     * </p>
     * <p>
     * Putting it in another way: This method can't be used to expand an
     * allocation beyond the task's bounds.
     * </p>
     *
     * @param start
     * @param end
     * @return an object which can be used to allocate hours on the interval
     *         specified with the considerations noted above
     */
    public IAllocateEffortOnInterval onIntervalWithinTask(IntraDayDate start,
            IntraDayDate end);

    /**
     * @see IAllocatable#onInterval(IntraDayDate, IntraDayDate)
     */
    public IAllocateEffortOnInterval onInterval(LocalDate startInclusive,
            LocalDate endExclusive);

    /**
     * It does the allocation in the interval specified with one consideration:
     * the consolidated part of the allocation is never modified.
     *
     * @param startInclusive
     * @param endExclusive
     * @return an object which can be used to allocate hours on the interval
     *         specified with the considerations noted above
     */
    public IAllocateEffortOnInterval onInterval(IntraDayDate start,
            IntraDayDate end);

    /**
     * It allocates the effort specified on the interval from the start, i.e.
     * first day not consolidated to the specified end. All previous assignments
     * are removed, but the consolidated ones.
     *
     * @param endExclusive
     * @return
     */
    public IAllocateEffortOnInterval fromStartUntil(LocalDate endExclusive);

    /**
     * It allocates the effort specified on the interval from the end until the
     * start. Being the start the maximum of the provided start and the first
     * not consolidated day. All previous assignments are removed, but the
     * consolidated ones.
     *
     * @param endExclusive
     * @return
     */
    public IAllocateEffortOnInterval fromEndUntil(LocalDate start);

}