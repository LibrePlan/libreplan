package org.navalplanner.business.calendars.entities;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    private static final Integer DEFAULT_VALUE = 0;

    public static BaseCalendar create() {
        BaseCalendar baseCalendar = new BaseCalendar();
        baseCalendar.setNewObject(true);
        return baseCalendar;
    }

    private String name;

    private Map<Integer, Integer> hoursPerDay;

    private BaseCalendar parent;

    private BaseCalendar previousCalendar;

    private BaseCalendar nextCalendar;

    private LocalDate expiringDate;

    private Set<ExceptionDay> exceptions = new HashSet<ExceptionDay>();

    public enum Days {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    public enum DayType {
        NORMAL, ZERO_HOURS, OWN_EXCEPTION, ANCESTOR_EXCEPTION
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public BaseCalendar() {
        hoursPerDay = new HashMap<Integer, Integer>();
        setHoursForDay(Days.MONDAY, null);
        setHoursForDay(Days.TUESDAY, null);
        setHoursForDay(Days.WEDNESDAY, null);
        setHoursForDay(Days.THURSDAY, null);
        setHoursForDay(Days.FRIDAY, null);
        setHoursForDay(Days.SATURDAY, null);
        setHoursForDay(Days.SUNDAY, null);
    }

    public void setName(String name) {
        if (nextCalendar != null) {
            nextCalendar.setName(name);
        } else {
            this.name = name;
        }
    }

    public String getName() {
        if (nextCalendar != null) {
            return nextCalendar.getName();
        }

        return name;
    }

    public Map<Integer, Integer> getHoursPerDay() {
        return hoursPerDay;
    }

    public Integer getHours(Days day) {
        if ((getHoursForDay(day) == null) && (parent != null)) {
            return parent.getHours(day);
        } else {
            return valueIfNotNullElseDefaultValue(getHoursForDay(day));
        }
    }

    private Integer valueIfNotNullElseDefaultValue(Integer hours) {
        if (hours == null) {
            return DEFAULT_VALUE;
        }
        return hours;
    }

    public void setHours(Days day, Integer hours)
            throws IllegalArgumentException {
        setHoursForDay(day, hours);
    }

    private void setHoursForDay(Days day, Integer hours)
            throws IllegalArgumentException {
        if ((hours != null) && (hours < 0)) {
            throw new IllegalArgumentException(
                    "The number of hours for a day can not be negative");
        }
        hoursPerDay.put(day.ordinal(), hours);
    }

    private Integer getHoursForDay(Days day) {
        return hoursPerDay.get(day.ordinal());
    }

    public boolean isDefault(Days day) {
        return (getHoursForDay(day) == null);
    }

    public void setDefault(Days day) {
        setHoursForDay(day, null);
    }

    public BaseCalendar getParent() {
        return parent;
    }

    public boolean isDerived() {
        return (parent != null);
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

    public void setExpiringDate(Date expiringDate)
            throws UnsupportedOperationException, IllegalArgumentException {
        setExpiringDate(new LocalDate(expiringDate));
    }

    public void setExpiringDate(LocalDate expiringDate)
            throws UnsupportedOperationException, IllegalArgumentException {
        if (nextCalendar == null) {
            throw new UnsupportedOperationException(
                    "Can not set the expiring date "
                            + "because of it does not have a next calendar");
        }
        if (expiringDate.compareTo(new LocalDate()) <= 0) {
            throw new IllegalArgumentException(
                    "Expering date must be greater than current date");
        }
        if (previousCalendar != null) {
            if (expiringDate.compareTo(previousCalendar.getExpiringDate()) <= 0) {
                throw new IllegalArgumentException(
                        "Expering date must be greater than expiring date of previous calendars");
            }
        }
        this.expiringDate = expiringDate;
    }

    public Set<ExceptionDay> getOwnExceptions() {
        return Collections.unmodifiableSet(exceptions);
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
        if (day.getDate().compareTo(new LocalDate()) <= 0) {
            throw new IllegalArgumentException(
                    "You can not modify the past adding a new exception day");
        }

        if (shouldUsePreviousCalendar(day.getDate())) {
            previousCalendar.addExceptionDay(day);
        } else if (shouldUseNextCalendar(day.getDate())) {
            nextCalendar.addExceptionDay(day);
        } else {
            if (isExceptionDayAlreadyInExceptions(day)) {
                throw new IllegalArgumentException(
                        "This day is already in the exception days");
            }

            exceptions.add(day);
        }
    }

    public void removeExceptionDay(Date date) throws IllegalArgumentException {
        removeExceptionDay(new LocalDate(date));
    }

    public void removeExceptionDay(LocalDate date)
            throws IllegalArgumentException {
        if (date.compareTo(new LocalDate()) <= 0) {
            throw new IllegalArgumentException(
                    "You can not modify the past removing an exception day");
        }

        if (shouldUsePreviousCalendar(date)) {
            previousCalendar.removeExceptionDay(date);
        } else if (shouldUseNextCalendar(date)) {
            nextCalendar.removeExceptionDay(date);
        } else {
            ExceptionDay day = getOwnExceptionDay(date);
            if (day == null) {
                throw new IllegalArgumentException(
                        "There is not an exception day on that date");
            }

            exceptions.remove(day);
        }
    }

    public void updateExceptionDay(Date date, Integer hours)
            throws IllegalArgumentException {
        updateExceptionDay(new LocalDate(date), hours);
    }

    public void updateExceptionDay(LocalDate date, Integer hours)
            throws IllegalArgumentException {
        removeExceptionDay(date);
        ExceptionDay day = ExceptionDay.create(date, hours);
        addExceptionDay(day);
    }

    public ExceptionDay getOwnExceptionDay(Date date) {
        return getOwnExceptionDay(new LocalDate(date));
    }

    public ExceptionDay getOwnExceptionDay(LocalDate date) {
        if (shouldUsePreviousCalendar(date)) {
            return previousCalendar.getOwnExceptionDay(date);
        } else if (shouldUseNextCalendar(date)) {
            return nextCalendar.getOwnExceptionDay(date);
        }

        for (ExceptionDay exceptionDay : exceptions) {
            if (exceptionDay.getDate().equals(date)) {
                return exceptionDay;
            }
        }

        return null;
    }

    public ExceptionDay getExceptionDay(Date date) {
        return getExceptionDay(new LocalDate(date));
    }

    public ExceptionDay getExceptionDay(LocalDate date) {
        if (shouldUsePreviousCalendar(date)) {
            return previousCalendar.getExceptionDay(date);
        } else if (shouldUseNextCalendar(date)) {
            return nextCalendar.getExceptionDay(date);
        }

        for (ExceptionDay exceptionDay : getExceptions()) {
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
            return getHours(Days.MONDAY);

        case DateTimeConstants.TUESDAY:
            return getHours(Days.TUESDAY);

        case DateTimeConstants.WEDNESDAY:
            return getHours(Days.WEDNESDAY);

        case DateTimeConstants.THURSDAY:
            return getHours(Days.THURSDAY);

        case DateTimeConstants.FRIDAY:
            return getHours(Days.FRIDAY);

        case DateTimeConstants.SATURDAY:
            return getHours(Days.SATURDAY);

        case DateTimeConstants.SUNDAY:
            return getHours(Days.SUNDAY);

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
        return getWorkableHoursPerWeek(new LocalDate(date));
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
     * Creates a new version this {@link BaseCalendar} from the specific date.
     * It makes that the current calendar expires in the specific date. And the
     * new calendar will be used from that date onwards.
     */
    public BaseCalendar newVersion(Date date) throws IllegalArgumentException {
        return newVersion(new LocalDate(date));
    }

    /**
     * Creates a new version this {@link BaseCalendar} from the specific date.
     * It makes that the current calendar expires in the specific date. And the
     * new calendar will be used from that date onwards.
     */
    public BaseCalendar newVersion(LocalDate date)
            throws IllegalArgumentException {
        if (date.compareTo(new LocalDate()) <= 0) {
            throw new IllegalArgumentException(
                    "Date for new version must be greater than current date");
        }

        if (nextCalendar != null) {
            return nextCalendar.newVersion(date);
        }

        if (previousCalendar != null) {
            if (date.compareTo(previousCalendar.getExpiringDate()) <= 0) {
                throw new IllegalArgumentException(
                        "Version date must be greater than expiring date of " +
                        "all versions of this calendar");
            }
        }

        BaseCalendar nextCalendar = newCopy();

        this.expiringDate = date;

        this.nextCalendar = nextCalendar;
        nextCalendar.previousCalendar = this;

        nextCalendar.name = this.name;

        return nextCalendar;
    }

    public BaseCalendar newCopy() {
        if (nextCalendar != null) {
            return nextCalendar.newCopy();
        }

        BaseCalendar copy = create();

        copy.name = this.name;
        copy.hoursPerDay = new HashMap<Integer, Integer>(this.hoursPerDay);
        copy.exceptions = new HashSet<ExceptionDay>(this.exceptions);
        copy.parent = this.parent;

        return copy;
    }

    public DayType getType(Date date) {
        return getType(new LocalDate(date));
    }

    public DayType getType(LocalDate date) {
        ExceptionDay exceptionDay = getExceptionDay(date);
        if (exceptionDay != null) {
            if (getOwnExceptionDay(date) != null) {
                return DayType.OWN_EXCEPTION;
            }
            return DayType.ANCESTOR_EXCEPTION;
        }

        if (getWorkableHours(date) == 0) {
            return DayType.ZERO_HOURS;
        }

        return DayType.NORMAL;
    }

    public void setParent(BaseCalendar parent)
            throws IllegalArgumentException {
        this.parent = parent;
    }

    public BaseCalendar getCalendarVersion(Date date) {
        return getCalendarVersion(new LocalDate(date));
    }

    public BaseCalendar getCalendarVersion(LocalDate date) {
        if (shouldUsePreviousCalendar(date)) {
            return previousCalendar.getCalendarVersion(date);
        } else if (shouldUseNextCalendar(date)) {
            return nextCalendar.getCalendarVersion(date);
        } else {
            return this;
        }
    }

}
