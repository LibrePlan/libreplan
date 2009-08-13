package org.navalplanner.business.calendars.entities;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.IValidable;
import org.navalplanner.business.common.exceptions.ValidationException;

/**
 * Represents a calendar with some exception days. A calendar is valid till the
 * expiring date, when the next calendar starts to be valid.
 *
 * On the other hand, a calendar could be derived, and the derived calendar
 * could add or overwrite some exceptions of its parent calendar.
 *
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendar extends BaseEntity implements IValidable {

    public static BaseCalendar create() {
        BaseCalendar baseCalendar = new BaseCalendar();
        baseCalendar.setNewObject(true);
        return baseCalendar;
    }

    private String name;

    private Integer monday = -1;
    private Integer tuesday = -1;
    private Integer wednesday = -1;
    private Integer thursday = -1;
    private Integer friday = -1;
    private Integer saturday = -1;
    private Integer sunday = -1;

    private BaseCalendar parent;

    private BaseCalendar previousCalendar;

    private BaseCalendar nextCalendar;

    private LocalDate expiringDate;

    private Set<ExceptionDay> exceptions = new HashSet<ExceptionDay>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public BaseCalendar() {

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMonday(Integer monday) {
        this.monday = monday;
    }

    public Integer getMonday() {
        if ((monday == -1) && (parent != null)) {
            return parent.getMonday();
        } else {
            return monday;
        }
    }

    public void setTuesday(Integer tuesday) {
        this.tuesday = tuesday;
    }

    public Integer getTuesday() {
        if ((tuesday == -1) && (parent != null)) {
            return parent.getTuesday();
        } else {
            return tuesday;
        }
    }

    public void setWednesday(Integer wednesday) {
        this.wednesday = wednesday;
    }

    public Integer getWednesday() {
        if ((wednesday == -1) && (parent != null)) {
            return parent.getWednesday();
        } else {
            return wednesday;
        }
    }

    public void setThursday(Integer thursday) {
        this.thursday = thursday;
    }

    public Integer getThursday() {
        if ((thursday == -1) && (parent != null)) {
            return parent.getThursday();
        } else {
            return thursday;
        }
    }

    public void setFriday(Integer friday) {
        this.friday = friday;
    }

    public Integer getFriday() {
        if ((friday == -1) && (parent != null)) {
            return parent.getFriday();
        } else {
            return friday;
        }
    }

    public void setSaturday(Integer saturday) {
        this.saturday = saturday;
    }

    public Integer getSaturday() {
        if ((saturday == -1) && (parent != null)) {
            return parent.getSaturday();
        } else {
            return saturday;
        }
    }

    public void setSunday(Integer sunday) {
        this.sunday = sunday;
    }

    public Integer getSunday() {
        if ((sunday == -1) && (parent != null)) {
            return parent.getSunday();
        } else {
            return sunday;
        }
    }

    public BaseCalendar getParent() {
        return parent;
    }

    public BaseCalendar getPreviousCalendar() {
        return previousCalendar;
    }

    public BaseCalendar getNextCalendar() {
        return nextCalendar;
    }

    public LocalDate getExpiringDate() {
        return expiringDate;
    }

    public Set<ExceptionDay> getExceptions() {
        Set<ExceptionDay> exceptionDays = new HashSet<ExceptionDay>();
        exceptionDays.addAll(exceptions);

        if (parent != null) {
            for (ExceptionDay exceptionDay : parent.getExceptions()) {
                if (!isExceptionDayAlreadyInExceptions(exceptionDay)) {
                    exceptionDays.add(exceptionDay);
                }
            }
        }

        return Collections.unmodifiableSet(exceptionDays);
    }

    private boolean isExceptionDayAlreadyInExceptions(ExceptionDay exceptionDay) {
        for (ExceptionDay day : exceptions) {
            if (day.getDate().equals(exceptionDay.getDate())) {
                return true;
            }
        }

        return false;
    }

    public void addExceptionDay(ExceptionDay day)
            throws IllegalArgumentException {
        if (isExceptionDayAlreadyInExceptions(day)) {
            throw new IllegalArgumentException(
                    "This day is already in the exception days");
        }
        exceptions.add(day);
    }

    public void removeExceptionDay(Date date) throws IllegalArgumentException {
        removeExceptionDay(new LocalDate(date));
    }

    public void removeExceptionDay(LocalDate date)
            throws IllegalArgumentException {

        ExceptionDay day = getExceptionDay(date);
        if (day == null) {
            throw new IllegalArgumentException(
                    "There is not an exception day on that date");
        }

        exceptions.remove(day);
    }

    public void updateExceptionDay(LocalDate date, Integer hours)
            throws IllegalArgumentException {
        removeExceptionDay(date);
        ExceptionDay day = ExceptionDay.create(date, hours);
        addExceptionDay(day);
    }

    private ExceptionDay getExceptionDay(LocalDate date) {
        for (ExceptionDay exceptionDay : exceptions) {
            if (exceptionDay.getDate().equals(date)) {
                return exceptionDay;
            }
        }

        return null;
    }

    private boolean shouldUsePreviousCalendar(LocalDate date) {
        return ((previousCalendar != null) && (date.compareTo(previousCalendar
                .getExpiringDate()) < 0));
    }

    private boolean shouldUseNextCalendar(LocalDate date) {
        if ((getExpiringDate() != null)
                && (getExpiringDate().compareTo(date) <= 0)) {
            if (nextCalendar == null) {
                throw new RuntimeException("A next calendar should exist "
                        + "if current calendar has a expiring date fixed");
            }

            return true;
        }

        return false;
    }

    /**
     * Returns the number of workable hours for a specific date depending on the
     * calendar restrictions.
     */
    public Integer getWorkableHours(Date date) {
        return getWorkableHours(new LocalDate(date));
    }

    /**
     * Returns the number of workable hours for a specific date depending on the
     * calendar restrictions.
     */
    public Integer getWorkableHours(LocalDate date) {
        if (shouldUsePreviousCalendar(date)) {
            return previousCalendar.getWorkableHours(date);
        } else if (shouldUseNextCalendar(date)) {
            return nextCalendar.getWorkableHours(date);
        }

        for (ExceptionDay exceptionDay : getExceptions()) {
            if (exceptionDay.getDate().equals(date)) {
                return exceptionDay.getHours();
            }
        }

        switch (date.getDayOfWeek()) {
            case DateTimeConstants.MONDAY:
                return getMonday();

            case DateTimeConstants.TUESDAY:
                return getTuesday();

            case DateTimeConstants.WEDNESDAY:
                return getWednesday();

            case DateTimeConstants.THURSDAY:
                return getThursday();

            case DateTimeConstants.FRIDAY:
                return getFriday();

            case DateTimeConstants.SATURDAY:
                return getSaturday();

            case DateTimeConstants.SUNDAY:
                return getSunday();

            default:
                throw new RuntimeException("Day of week out of range!");
        }

    }

    /**
     * Returns the number of workable hours for a specific period depending on
     * the calendar restrictions.
     */
    public Integer getWorkableHours(Date initDate, Date endDate) {
        return getWorkableHours(new LocalDate(initDate), new LocalDate(endDate));
    }

    /**
     * Returns the number of workable hours for a specific period depending on
     * the calendar restrictions.
     */
    public Integer getWorkableHours(LocalDate init, LocalDate end) {
        int total = 0;

        while (init.compareTo(end) <= 0) {
            total += getWorkableHours(init);
            init = init.plusDays(1);
        }

        return total;
    }

    /**
     * Returns the number of workable hours for a specific week depending on the
     * calendar restrictions.
     */
    public Integer getWorkableHoursPerWeek(Date date) {
        DateTime week = new DateTime(date);
        DateTime init = week.dayOfWeek().withMinimumValue();
        DateTime end = week.dayOfWeek().withMaximumValue();

        return getWorkableHours(init.toDate(), end.toDate());
    }

    /**
     * Returns the number of workable hours for a specific week depending on the
     * calendar restrictions.
     */
    public Integer getWorkableHoursPerWeek(LocalDate date) {
        LocalDate init = date.dayOfWeek().withMinimumValue();
        LocalDate end = date.dayOfWeek().withMaximumValue();

        return getWorkableHours(init, end);
    }

    @Override
    public void checkValid() throws ValidationException {
        if (parent == null) {
            if ((monday == -1) || (thursday == -1) || (wednesday == -1)
                    || (tuesday == -1) || (friday == -1) || (saturday == -1)
                    || (sunday == -1)) {
                throw new ValidationException(
                        "Daily hours could not have the default value "
                                + "if the calendar is not derivated");
            }
        }

        if ((nextCalendar == null) && (expiringDate != null)) {
            throw new ValidationException("A next calendar should exist "
                    + "if current calendar has a expiring date fixed");
        }
        if ((nextCalendar != null) && (expiringDate == null)) {
            throw new ValidationException("A expiring date should be fixed"
                    + "if current calendar has a next calendar");
        }
    }

    /**
     * Creates a new {@link BaseCalendar} derived from the current calendar. The
     * new calendar will be the child of the current calendar.
     *
     * @return The derived calendar
     */
    public BaseCalendar newDerivedCalendar() {
        BaseCalendar derivedCalendar = create();
        derivedCalendar.parent = this;
        return derivedCalendar;
    }

    /**
     * Creates a new version this {@link BaseCalendar} from the current moment.
     * It makes that the current calendar expires in the current date. And the
     * new calendar will be used from now onwards.
     */
    public BaseCalendar newVersion() {
        return newVersion(new LocalDate());
    }

    /**
     * Creates a new version this {@link BaseCalendar} from the specific date.
     * It makes that the current calendar expires in the specific date. And the
     * new calendar will be used from that date onwards.
     */
    public BaseCalendar newVersion(Date date) {
        return newVersion(new LocalDate(date));
    }

    /**
     * Creates a new version this {@link BaseCalendar} from the specific date.
     * It makes that the current calendar expires in the specific date. And the
     * new calendar will be used from that date onwards.
     */
    public BaseCalendar newVersion(LocalDate date) {
        if (nextCalendar != null) {
            nextCalendar.newVersion(date);
        }

        BaseCalendar nextCalendar = copy();

        this.expiringDate = date;

        this.nextCalendar = nextCalendar;
        nextCalendar.previousCalendar = this;

        return nextCalendar;
    }

    private BaseCalendar copy() {
        BaseCalendar copy = create();

        copy.name = this.name;

        copy.monday = this.monday;
        copy.tuesday = this.tuesday;
        copy.wednesday = this.wednesday;
        copy.thursday = this.thursday;
        copy.friday = this.friday;
        copy.saturday = this.saturday;
        copy.sunday = this.sunday;

        copy.exceptions = new HashSet<ExceptionDay>(this.exceptions);

        copy.parent = this.parent;

        return copy;
    }

}
