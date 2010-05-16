/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.planner.entities;

import org.joda.time.LocalDate;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class DateAndHour implements Comparable<DateAndHour> {

    private LocalDate date;

    private Integer hour;

    public DateAndHour(LocalDate date, Integer hour) {
        this.date = date;
        this.hour = hour;
    }

    public LocalDate getDate() {
        return date;
    }

    public Integer getHour() {
        return hour;
    }

    @Override
    public int compareTo(DateAndHour dateAndTime) {
        int compareDate = date.compareTo(getDate(dateAndTime));
        return (compareDate != 0) ? compareDate : compareHour(dateAndTime
                .getHour());
    }

    private LocalDate getDate(DateAndHour dateAndHour) {
        return (dateAndHour != null) ? dateAndHour.getDate() : null;
    }

    private int compareHour(int hour) {
        int deltaHour = this.hour - hour;
        return (deltaHour != 0) ? deltaHour / Math.abs(deltaHour) : 0;
    }

    public String toString() {
        return date + "; " + hour;
    }

    public static DateAndHour Max(DateAndHour arg0, DateAndHour arg1) {
        if (arg0 == null) {
            return arg1;
        }
        if (arg1 == null) {
            return arg0;
        }
        return (arg0.compareTo(arg1) > 0) ? arg0 : arg1;
    }

    public boolean isBefore(DateAndHour dateAndHour) {
        return (this.compareTo(dateAndHour) < 0);
    }

}
