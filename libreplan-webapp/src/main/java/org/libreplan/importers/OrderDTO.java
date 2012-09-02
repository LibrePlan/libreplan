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

package org.libreplan.importers;

import java.util.Date;
import java.util.List;

import org.libreplan.business.orders.entities.Order;

/**
 * Class that represents no persistent imported data.
 *
 * At these moment it only represent the tasks and its subtasks.
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 * @todo It last relationships. resources, calendars, hours, etc
 */
public class OrderDTO {
    /**
     * Name of the project that is going to be imported.
     */
    public String name;

    /**
     * Start date of the project
     */
    public Date startDate;

    /**
     * Deadline of the project
     */
    public Date deadline;

    /**
     * Order created with this data
     */
    public Order order;

    /**
     * List of {@link OrderElementDTO} of the project that is going to be imported.
     */
    public List<OrderElementDTO> tasks;

}
