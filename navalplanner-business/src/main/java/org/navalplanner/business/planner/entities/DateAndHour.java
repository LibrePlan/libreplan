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
        return (deltaHour != 0) ? Math.abs(deltaHour) : 0;
    }

    public String toString() {
        return date + "; " + hour;
    }

}
