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

import java.util.List;

import org.libreplan.business.calendars.entities.CalendarException;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorException;

/**
 * Import Rosters from Tim SOAP server using {@link TimSoapClient} and updates
 * worker's Exception calendar accordingly
 *
 * It imports the Rosters between periods current-date and current-date plus
 * <code>NrDaysRosterFromTim</code> specified in Tim {@link Connector}.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface IImportRosterFromTim {

    /**
     * Import rosters from Tim and update workers {@link CalendarException}
     *
     * If worker calendar exception already exists it will be removed and added
     * new one, in other cases a new calendar exception will be created
     *
     * @return a list of {@link SynchronizationInfo}
     *
     * @throws ConnectorException
     *             if connector is not valid
     */
    List<SynchronizationInfo> importRosters() throws ConnectorException;

    /**
     * Returns synchronization info, success of fail info
     */
    SynchronizationInfo getSynchronizationInfo();
}
