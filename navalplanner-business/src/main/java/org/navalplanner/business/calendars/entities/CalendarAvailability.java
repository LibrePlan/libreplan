/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.calendars.entities;

import java.util.Date;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;

/**
 * Stores information about activating periods, that define the availability of
 * the resource.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarAvailability extends BaseEntity {

    public static CalendarAvailability craete(Date startDate, Date endDate) {
        return create(new CalendarAvailability(new LocalDate(startDate),
                new LocalDate(endDate)));
    }

    public static CalendarAvailability craete(LocalDate startDate,
            LocalDate endDate) {
        return create(new CalendarAvailability(startDate, endDate));
    }

    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Constructor for hibernate. Do not use!
     */
    public CalendarAvailability() {
    }

    private CalendarAvailability(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

}
