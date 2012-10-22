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
import java.util.List;

import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;

/**
 * Contract for the {@link CalendarImporterMPXJ}.
 *
 * Has all the methods needed to successfully import the calendars of
 * some external project files into Libreplan.
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 */
public interface ICalendarImporter {

    /**
     * Makes a list of {@link CalendarDTO} from a InputStream.
     *
     * @param file
     *            InputStream to extract data from.
     * @return List<CalendarDTO> with the calendar data that we want to import.
     */
    public List<CalendarDTO> getCalendarDTOs(InputStream file, String filename);

    /**
     * Makes a list of {@link BaseCalendar} from a list of {@link CalendarDTO}.
     *
     * @param calendarDTOs
     *            List of CalendarDTO to extract data from.
     * @return List<BaseCalendar> with all the calendars that we want.
     * @throws InstanceNotFoundException
     */
    public List<BaseCalendar> getBaseCalendars(
List<CalendarDTO> calendarDTOs)
            throws InstanceNotFoundException;

    /**
     * Saves a list of {@link BaseCalendar} that has all the calendar data that
     * we want to store in the database.
     *
     * @param Order
     *            Order with the data.
     *
     * @param TaskGroup
     *            TaskGroup with the data.
     */
    public void storeBaseCalendars(List<BaseCalendar> baseCalendars);

    /**
     * Makes a {@link OrderDTO} from a InputStream.
     *
     * Uses the ProjectReader of the class. It must be created before.
     *
     * @param filename
     *            String with the name of the original file of the InputStream.
     * @return OrderDTO with the data that we want to import.
     */
    OrderDTO getOrderDTO(String filename);
}
