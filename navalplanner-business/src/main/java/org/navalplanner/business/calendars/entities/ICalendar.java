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

package org.navalplanner.business.calendars.entities;

import org.joda.time.LocalDate;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.navalplanner.business.workingday.ResourcesPerDay;

public interface ICalendar {

    /**
     * Translates the received amount into the corresponding duration at the
     * given date. It takes into account the partial capacity of the day.
     *
     * @param day
     * @param amount
     * @return
     */
    public EffortDuration asDurationOn(PartialDay day, ResourcesPerDay amount);

    /**
     * Calculates the capacity duration at a given day. It means all the time
     * that could be worked without having overtime. It considers the
     * {@link PartialDay} so if the day it's not complete the capacity is
     * reduced
     * @param date
     *            the date at which the capacity is calculated
     * @return the capacity at which the resource can work at the day specified
     */
    public EffortDuration getCapacityOn(PartialDay partialDay);

    /**
     * Calculates the capacity information for a given date. It contains
     * information about the normal effort and the extra effort, i.e., the
     * overtime effort.
     *
     * @param date
     *            a not null date
     * @return the capacity for the date provided
     */
    public Capacity getCapacityWithOvertime(LocalDate date);

    public AvailabilityTimeLine getAvailability();

    public boolean thereAreCapacityFor(AvailabilityTimeLine availability,
            ResourcesPerDay resourcesPerDay, EffortDuration durationToAllocate);
}
