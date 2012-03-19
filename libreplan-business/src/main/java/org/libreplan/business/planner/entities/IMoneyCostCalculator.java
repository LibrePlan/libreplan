/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

import java.math.BigDecimal;

import org.libreplan.business.orders.entities.OrderElement;

/**
 * Interface to calculate the money cost of a {@link TaskElement}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IMoneyCostCalculator {

    /**
     * Returns the money cost of a {@link TaskElement}.<br />
     *
     * It uses the {@link OrderElement} (or OrderElements) associated to the
     * {@link TaskElement} in order to calculate the cost using the following
     * formula:<br />
     * <tt>Sum of all the hours devoted to a task multiplied by the cost of
     * each hour according to these parameters (type of hour, cost category of
     * the resource, date of the work report)</tt>
     *
     * @param The
     *            {@link TaskElement} to calculate the money cost
     * @return Money cost of the task
     */
    BigDecimal getMoneyCost(TaskElement taskElement);

}
