/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

import org.joda.time.LocalDate;

/**
 * This interface represents an object on which an allocation can be done
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IAllocatable extends IAllocateResourcesPerDay {

    public IAllocateResourcesPerDay resourcesPerDayUntil(LocalDate endExclusive);

    public IAllocateResourcesPerDay resourcesPerDayFromEndUntil(LocalDate start);

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
     * @param startInclusive
     * @param endExclusive
     * @return an object which can be used to allocate hours on the interval
     *         specified
     */
    public IAllocateHoursOnInterval onIntervalWithinTask(LocalDate startInclusive,
            LocalDate endExclusive);

    public IAllocateHoursOnInterval fromStartUntil(LocalDate endExclusive);

    public IAllocateHoursOnInterval fromEndUntil(LocalDate start);

}