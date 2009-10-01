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
 * Represents an exceptional day that has a different number of hours. For
 * example, a bank holiday.
 *
 * It is used for the {@link BaseCalendar}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class ExceptionDay extends BaseEntity {

    public static ExceptionDay create(Date date, Integer hours) {
        ExceptionDay exceptionDay = new ExceptionDay(new LocalDate(date), hours);
        exceptionDay.setNewObject(true);
        return exceptionDay;
    }

    public static ExceptionDay create(LocalDate date, Integer hours) {
        ExceptionDay exceptionDay = new ExceptionDay(date, hours);
        exceptionDay.setNewObject(true);
        return exceptionDay;
    }

    private LocalDate date;

    private Integer hours;

    /**
     * Constructor for hibernate. Do not use!
     */
    public ExceptionDay() {

    }

    private ExceptionDay(LocalDate date, Integer hours) {
        this.date = date;
        this.hours = hours;
    }

    public LocalDate getDate() {
        return date;
    }

    public Integer getHours() {
        return hours;
    }

}
