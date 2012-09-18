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

/**
 * Class that represents the information of the calendar
 * that is different from the default one
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 */
public class CalendarWeekDTO {

    /**
     * Start date of the working week.
     */
    public Date startDate;

    /**
     * End date of the working week.
     */
    public Date endDate;

    /**
     * List of hours per day of the working week.
     */
    public List<CalendarDayHoursDTO> hoursPerDays;
}
