/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.calendars.entities;

import java.util.Comparator;
import java.util.Date;

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.daos.ICalendarAvailabilityDAO;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;

/**
 * Stores information about activating periods, that define the availability of
 * the resource.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarAvailability extends IntegrationEntity {

    public static CalendarAvailability craete() {
        return create(new CalendarAvailability(new LocalDate(), null));
    }

    public static CalendarAvailability craete(Date startDate, Date endDate) {
        return create(new CalendarAvailability(new LocalDate(startDate),
                new LocalDate(endDate)));
    }

    public static CalendarAvailability create(LocalDate startDate,
            LocalDate endDate) {
        return create(new CalendarAvailability(startDate, endDate));
    }

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    public static final Comparator<CalendarAvailability> BY_START_DATE_COMPARATOR = new Comparator<CalendarAvailability>() {

        @Override
        public int compare(CalendarAvailability o1, CalendarAvailability o2) {
            return o1.getStartDate().compareTo(o2.getStartDate());
        }
    };

    /**
     * Constructor for hibernate. Do not use!
     */
    public CalendarAvailability() {
    }

    private CalendarAvailability(LocalDate startDate, LocalDate endDate)
            throws IllegalArgumentException {
        setStartDate(startDate);
        setEndDate(endDate);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate)
            throws IllegalArgumentException {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date must not be null");
        }
        if (endDate != null) {
            if (startDate.compareTo(endDate) > 0) {
                throw new IllegalArgumentException(
                        "End date must be greater or equal than start date");
            }
        }
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) throws IllegalArgumentException {
        if (endDate != null) {
            if (startDate.compareTo(endDate) > 0) {
                throw new IllegalArgumentException(
                        "End date must be greater or equal than start date");
            }
        }
        this.endDate = endDate;
    }

    public boolean isActive(LocalDate date) {
        if (startDate.compareTo(date) > 0) {
            return false;
        }

        if ((endDate != null) && (endDate.compareTo(date) < 0)) {
            return false;
        }

        return true;
    }

    @Override
    protected ICalendarAvailabilityDAO getIntegrationEntityDAO() {
        return Registry.getCalendarAvailabilityDAO();
    }

}
