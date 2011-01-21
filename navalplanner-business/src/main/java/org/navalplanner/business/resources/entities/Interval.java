/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.resources.entities;


import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

/**
 * Represents a time interval <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class Interval {
    protected final LocalDate start;

    protected final LocalDate end;

    public static Interval from(LocalDate start) {
        return new OpenEndedInterval(start);
    }

    public static Interval point(LocalDate date) {
        return new Point(date);
    }

    public static Interval range(LocalDate start, LocalDate end) {
        Validate.notNull(start, "start date must be not null");
        if (end == null) {
            return from(start);
        }
        if (start.equals(end)) {
            return point(start);
        }
        return new Range(start, end);
    }

    protected Interval(LocalDate start, LocalDate end) {
        Validate.notNull(start, "start date must be not null");
        if (end != null) {
            Validate.isTrue(start.compareTo(end) <= 0,
                    "start date must be equal or before than end date");
        }
        this.start = start;
        this.end = end;
    }

    public abstract boolean contains(LocalDate date);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Interval) {
            Interval interval = (Interval) obj;
            return dateEquals(start, interval.start)
                    && dateEquals(end, interval.end);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(start).append(end).toHashCode();
    }

    private boolean dateEquals(LocalDate date1, LocalDate date2) {
        return date1 == date2
                || (date1 != null && date2 != null && date1.equals(date2));
    }

    public abstract boolean includes(Interval included);

    public abstract boolean overlapsWith(Interval interval);

    public boolean before(LocalDate date) {
        return start.isBefore(date);
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

}

class Range extends Interval {

    Range(LocalDate start, LocalDate end) {
        super(start, end);
        Validate.notNull(start);
        Validate.notNull(end);
    }

    @Override
    public boolean contains(LocalDate date) {
        return date.compareTo(start) >= 0 && date.compareTo(end) < 0;
    }

    @Override
    public boolean includes(Interval included) {
        if (included instanceof Point) {
            Point point = (Point) included;
            return point.overlapsWith(this);
        }
        return start.compareTo(included.start) <= 0 && included.end != null
                && end.isAfter(included.end);
    }

    @Override
    public boolean overlapsWith(Interval interval) {
        if (interval instanceof Point) {
            Point point = (Point) interval;
            return point.overlapsWith(this);
        }
        if (interval instanceof OpenEndedInterval) {
            return interval.overlapsWith(this);
        }
        return interval.start.compareTo(this.end) < 0
                && this.start.compareTo(interval.end) < 0;
    }

    @Override
    public String toString() {
        return new StringBuilder("[").append(start).append(", ").append(end)
                .append(")").toString();
    }

}

class OpenEndedInterval extends Interval {
    OpenEndedInterval(LocalDate start) {
        super(start, null);
    }

    @Override
    public boolean contains(LocalDate date) {
        return date.compareTo(start) >= 0;
    }

    @Override
    public boolean includes(Interval included) {
        return start.compareTo(included.start) <= 0;
    }

    @Override
    public boolean overlapsWith(Interval interval) {
        return start.isBefore(interval.start) || interval.end == null
                || start.isBefore(interval.end);
    }

    @Override
    public String toString() {
        return new StringBuilder("[").append(start).append(",...)").toString();
    }
}

class Point extends Interval {

    Point(LocalDate date) {
        super(date, date);
    }

    @Override
    public boolean contains(LocalDate date) {
        return start.equals(date);
    }

    @Override
    public boolean includes(Interval included) {
        return equals(included);
    }

    @Override
    public boolean overlapsWith(Interval interval) {
        return interval.contains(end) && !interval.start.equals(end);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[").append(start).append(")")
                .toString();
    }

}