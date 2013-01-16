/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderSyncInfo;

/**
 * Export time sheets of an existing order to Tim SOAP server using
 * {@link TimSoapClient}.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface IExportTimesheetsToTim {

    /**
     * Exports time sheets of the specified <code>productCode</code> and
     * <code>{@link Order}</code>
     *
     * @param productCode
     *            the Tim's productCode
     * @param order
     *            an existing order
     */
    boolean exportTimesheets(String productCode, Order order);

    /**
     * Loops through all the time sheets of all {@link Order}s which has tim's
     * productcodes and export them to Tim SOAP server
     * <p>
     * This method is of importance for the scheduler service
     * </p>
     */
    void exportTimesheets();

    /**
     * Gets the most recent synchronized time sheet info
     *
     * @param order
     *            the order
     * @return recent synchronized time sheet info
     */
    OrderSyncInfo getOrderLastSyncInfo(Order order);

}
