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

package org.libreplan.business.orders.entities;


/**
 * Interface to recalculate {@link SumChargedEffort} for an {@link Order}.<br />
 *
 * This is needed to be called when some elements are moved in the {@link Order}
 * .
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public interface ISumChargedEffortRecalculator {

    /**
     * Mark {@link Order} to recalculate {@link SumChargedEffort}.<br />
     *
     * @param orderId
     */
    void recalculate(Long orderId);

}
