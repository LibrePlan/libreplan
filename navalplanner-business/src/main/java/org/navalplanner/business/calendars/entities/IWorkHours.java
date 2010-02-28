/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.calendars.entities;

import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.ResourcesPerDay;

public interface IWorkHours {

    /**
     * Translates the received amount into the corresponding hours at the given
     * date
     * @param day
     * @param amount
     * @return
     */
    public Integer toHours(LocalDate day, ResourcesPerDay amount);

    /**
     * Calculates the capacity at a given date. It means all the hours that
     * could be worked without having overtime
     * @param date
     *            the date at which the capacity is calculated
     * @return the capacity at which the resource can work
     */
    public Integer getCapacityAt(LocalDate date);

    public boolean thereAreHoursOn(AvailabilityTimeLine availability,
            ResourcesPerDay resourcesPerDay, int hoursToAllocate);
}
