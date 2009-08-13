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
