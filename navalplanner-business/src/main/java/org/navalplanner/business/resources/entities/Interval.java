package org.navalplanner.business.resources.entities;

import java.util.Date;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a time interval <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class Interval {
    protected final Date start;
    protected final Date end;

    public static Interval from(Date start) {
        return new OpenEndedInterval(start);
    }

    public static Interval point(Date date) {
        return new Point(date);
    }

    public static Interval range(Date start, Date end) {
        Validate.notNull(start, "start date must be not null");
        if (end == null)
            return from(start);
        if (start.equals(end))
            return point(start);
        return new Range(start, end);
    }

    protected Interval(Date start, Date end) {
        Validate.notNull(start, "start date must be not null");
        if (end != null) {
            Validate.isTrue(start.compareTo(end) <= 0,
                    "start date must be equal or before than end date");
        }
        this.start = start;
        this.end = end;
    }

    public abstract boolean contains(Date date);

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

    private boolean dateEquals(Date date1, Date date2) {
        return date1 == date2
                || (date1 != null && date2 != null && date1.equals(date2));
    }

    public abstract boolean includes(Interval included);

    public abstract boolean overlapsWith(Interval interval);

    public boolean before(Date date) {
        return start.before(date);
    }

    public Date getStart() {
        return new Date(start.getTime());
    }

    public Date getEnd() {
        return end != null ? new Date(end.getTime()) : null;
    }

}

class Range extends Interval {

    Range(Date start, Date end) {
        super(start, end);
        Validate.notNull(start);
        Validate.notNull(end);
    }

    @Override
    public boolean contains(Date date) {
        return date.compareTo(start) >= 0 && date.compareTo(end) < 0;
    }

    @Override
    public boolean includes(Interval included) {
        if (included instanceof Point) {
            Point point = (Point) included;
            return point.overlapsWith(this);
        }
        return start.compareTo(included.start) <= 0 && included.end != null
                && end.after(included.end);
    }

    @Override
    public boolean overlapsWith(Interval interval) {
        if (interval instanceof Point) {
            Point point = (Point) interval;
            return point.overlapsWith(this);
        }
        return contains(interval.start)
                || (interval.end != null ? contains(interval.end)
                        && !interval.end.equals(start) : end
                        .compareTo(interval.start) > 0);
    }

    @Override
    public String toString() {
        return new StringBuilder("[").append(start).append(", ").append(end)
                .append(")").toString();
    }

}

class OpenEndedInterval extends Interval {
    OpenEndedInterval(Date start) {
        super(start, null);
    }

    @Override
    public boolean contains(Date date) {
        return date.compareTo(start) >= 0;
    }

    @Override
    public boolean includes(Interval included) {
        return start.compareTo(included.start) <= 0;
    }

    @Override
    public boolean overlapsWith(Interval interval) {
        return start.before(interval.start) || interval.end == null
                || start.before(interval.end);
    }

    @Override
    public String toString() {
        return new StringBuilder("[").append(start).append(",...)").toString();
    }
}

class Point extends Interval {

    Point(Date date) {
        super(date, date);
    }

    @Override
    public boolean contains(Date date) {
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