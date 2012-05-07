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
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public interface IMoneyCostCalculator {

    /**
     * Returns the money cost of a {@link OrderElement} taking into account all
     * its children.<br />
     *
     * It uses the {@link OrderElement} in order to calculate the cost using the
     * following formula:<br />
     * <tt>Sum of all the hours devoted to a task multiplied by the cost of
     * each hour according to these parameters (type of hour, cost category of
     * the resource, date of the work report)</tt><br />
     *
     * If there is not relationship between resource and type of hour through
     * the cost categories, the price used is the default one for the type of
     * hour.
     *
     * In addition, it includes the costs because of expenses.
     *
     * @param The
     *            {@link OrderElement} to calculate the money cost
     * @return Money cost of the order element and all its children, including the
     * costs because of hours and because of expenses.
     */
    BigDecimal getTotalMoneyCost(OrderElement orderElement);

    /**
     * Resets the map used to save cached values of money cost for each
     * {@link OrderElement}
     */
    void resetMoneyCostMap();

    /**
     * Returns the money cost of a {@link OrderElement} taking into account all
     * its children.<br />
     *
     * It uses the {@link OrderElement} in order to calculate the cost using the
     * following formula:<br />
     * <tt>Sum of all the hours devoted to a task multiplied by the cost of
     * each hour according to these parameters (type of hour, cost category of
     * the resource, date of the work report)</tt><br />
     *
     * If there is not relationship between resource and type of hour through
     * the cost categories, the price used is the default one for the type of
     * hour.
     *
     * @param The
     *            {@link OrderElement} to calculate the money cost according
     *            to the hours
     * @return Money cost of the order element and all its children
     */
    BigDecimal getHoursMoneyCost(OrderElement orderElement);

    /**
     * Returns the money cost of a {@link OrderElement} taking into account all
     * its children.<br />
     *
     * It uses the {@link OrderElement} in order to calculate the cost using the
     * following formula:<br />
     * <tt>Sum of its direct expenses and its indirect expenses according to its
     *  relationship with @{SumExpenses}
     *
     * If there is not relationship between orderElement and any SumExpenses
     * the cost will be zero.
     *
     * @param The
     *            {@link OrderElement} to calculate the money cost according to
     *            the expenses.
     * @return Money cost of the order element and all its children
     */
    BigDecimal getExpensesMoneyCost(OrderElement orderElement);

}
