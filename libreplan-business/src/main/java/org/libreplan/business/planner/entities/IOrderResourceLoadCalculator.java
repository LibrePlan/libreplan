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

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.chart.ContiguousDaysLine;
import org.libreplan.business.resources.entities.IAssignmentsOnResourceCalculator;
import org.libreplan.business.workingday.EffortDuration;


/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Utility class for calculating 'Resource Load' values from an Order
 */
public interface IOrderResourceLoadCalculator {

    void setOrder(Order order,
            IAssignmentsOnResourceCalculator assignmentsOnResourceCalculator);

    ContiguousDaysLine<EffortDuration> getMaxCapacityOnResources();

    ContiguousDaysLine<EffortDuration> getOrderLoad();

    ContiguousDaysLine<EffortDuration> getAllLoad();

    ContiguousDaysLine<EffortDuration> getOrderOverload();

    ContiguousDaysLine<EffortDuration> getAllOverload();

}
