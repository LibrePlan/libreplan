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

import java.io.InputStream;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskGroup;

/**
 * Contract for the {@link OrderImporterMPXJ}.
 *
 * Has all the methods needed to successfully import some external project files
 * into Libreplan.
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 */
public interface IOrderImporter {

    /**
     * Makes a {@link OrderDTO} from a InputStream.
     *
     * @param file
     *            InputStream to extract data from.
     * @param filename
     *            String with the name of the original file of the InputStream.
     * @return ImportData with the data that we want to import.
     */
    public OrderDTO getImportData(InputStream file, String filename);

    /**
     * Makes a {@link Order} from a {@link OrderDTO}.
     *
     * @param project
     *            ImportData to extract data from.
     * @return Order with all the data that we want.
     */
    public Order convertImportDataToOrder(OrderDTO project);

    /**
     * Makes a {@link TaskGroup} from a {@link ImportData}.
     *
     * @param project
     *            ImportData to extract data from.
     * @return TaskGroup with the data that we want.
     */
    public TaskGroup createTask(OrderDTO project);

    /**
     * Saves a {@link Order} and a {@link TaskGroup} which has all the data that
     * we want to store in the database.
     *
     * @param Order
     *            Order with the data.
     *
     * @param TaskGroup
     *            TaskGroup with the data.
     */
    public void storeOrder(Order order, TaskGroup taskGroup);

}
