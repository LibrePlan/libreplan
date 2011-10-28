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

package org.libreplan.business.orders.entities;

import java.util.Set;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IHoursGroupHandler<T> {

    /**
     * Calculates the total number of working hours in a set of
     * {@link HoursGroup}.
     * @param hoursGroups
     *            A {@link HoursGroup} set
     * @return The sum of working hours
     */
    Integer calculateTotalHours(Set<HoursGroup> hoursGroups);

    /**
     * Checks if the percentage is or not valid. That means, if the pertentage
     * of all {@link HoursGroup} with FIXED_PERCENTAGE isn't more than 100%.
     * This method is called from setPercentage at {@link HoursGroup} class.
     * @return true if the percentage is valid
     */
    boolean isPercentageValid(final Set<HoursGroup> hoursGroups);

    /**
     * Checks if the desired total number of hours is valid taking into account
     * {@link HoursGroup} policy restrictions.
     * @param total
     *            The desired value
     * @return true if the value is valid
     */
    boolean isTotalHoursValid(Integer total, final Set<HoursGroup> hoursGroups);

    /**
     * Re-calculates the working hours and percentages in the {@link HoursGroup}
     * set of the current {@link OrderLine}, taking into account the policy of
     * each {@link HoursGroup}.
     */
    void recalculateHoursGroups(T orderLine);

    /**
     * Set the total working hours of the {@link OrderLine} taking into account
     * the {@link HoursGroup} policies.
     * @param workHours
     *            The desired value to set as total working hours
     * @throws IllegalArgumentException
     *             If parameter is less than 0 or if it's not possible to set
     *             this value taking into account {@link HoursGroup} policies.
     */
    void setWorkHours(T orderLine, Integer workHours) throws IllegalArgumentException;

}
