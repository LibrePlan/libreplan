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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.NotEmpty;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.business.common.BaseEntity;

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
public class BaseCalendar extends BaseEntity implements IWorkHours {

    private static final Integer DEFAULT_VALUE = 0;

    public static BaseCalendar create() {
        BaseCalendar baseCalendar = new BaseCalendar(CalendarData.create());
        baseCalendar.setNewObject(true);
        return baseCalendar;
    }

    @NotEmpty
    private String name;

    private Set<ExceptionDay> exceptions = new HashSet<ExceptionDay>();

    private List<CalendarData> calendarDataVersions = new ArrayList<CalendarData>();

    public enum DayType {
        NORMAL, ZERO_HOURS, OWN_EXCEPTION, ANCESTOR_EXCEPTION
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    public BaseCalendar() {
    }

    protected BaseCalendar(CalendarData calendarData) {
        calendarDataVersions.add(calendarData);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public BaseCalendar getParent() {
        return getLastCalendarData().getParent();
    }

    public BaseCalendar getParent(Date date) {
        return getParent(new LocalDate(date));
    }

    public BaseCalendar getParent(LocalDate date) {
        return getCalendarData(date).getParent();
    }

    public void setParent(BaseCalendar parent) {
        getLastCalendarData().setParent(parent);
    }

    public void setParent(BaseCalendar parent, Date date) {
        setParent(parent, new LocalDate(date));
    }

    public void setParent(BaseCalendar parent, LocalDate date) {
        getCalendarData(date).setParent(parent);
    }

    public boolean isDerived() {
        return (getParent() != null);
    }

    public boolean isDerived(Date date) {
        return isDerived(new LocalDate(date));
    }

    public boolean isDerived(LocalDate date) {
        return (getParent(date) != null);
    }

    public Set<ExceptionDay> getOwnExceptions() {
        return Collections.unmodifiableSet(exceptions);
    }

    public Set<ExceptionDay> getExceptions() {
        Set<ExceptionDay> exceptionDays = new HashSet<ExceptionDay>();
        exceptionDays.addAll(exceptions);

        if (getParent() != null) {
            for (ExceptionDay exceptionDay : getParent().getExceptions()) {
                if (!isExceptionDayAlreadyInExceptions(exceptionDay)) {
                    exceptionDays.add(exceptionDay);
                }
            }
        }

        return Collections.unmodifiableSet(exceptionDays);
    }

    public Set<ExceptionDay> getExceptions(Date date) {
        return getExceptions(date);
    }

    public Set<ExceptionDay> getExceptions(LocalDate date) {
        Set<ExceptionDay> exceptionDays = new HashSet<ExceptionDay>();
        exceptionDays.addAll(exceptions);

        if (getParent(date) != null) {
            for (ExceptionDay exceptionDay : getParent(date).getExceptions()) {
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
        if (date.compareTo(new LocalDate()) <= 0) {
            throw new IllegalArgumentException(
                    "You can not modify the past removing an exception day");
        }

        ExceptionDay day = getOwnExceptionDay(date);
        if (day == null) {
            throw new IllegalArgumentException(
                    "There is not an exception day on that date");
        }

        exceptions.remove(day);
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
        for (ExceptionDay exceptionDay : getExceptions(date)) {
            if (exceptionDay.getDate().equals(date)) {
                return exceptionDay;
            }
        }

        return null;
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
        ExceptionDay exceptionDay = getExceptionDay(date);
        if (exceptionDay != null) {
            return exceptionDay.getHours();
        }

        switch (date.getDayOfWeek()) {
        case DateTimeConstants.MONDAY:
            return getHours(date, Days.MONDAY);

        case DateTimeConstants.TUESDAY:
            return getHours(date, Days.TUESDAY);

        case DateTimeConstants.WEDNESDAY:
            return getHours(date, Days.WEDNESDAY);

        case DateTimeConstants.THURSDAY:
            return getHours(date, Days.THURSDAY);

        case DateTimeConstants.FRIDAY:
            return getHours(date, Days.FRIDAY);

        case DateTimeConstants.SATURDAY:
            return getHours(date, Days.SATURDAY);

        case DateTimeConstants.SUNDAY:
            return getHours(date, Days.SUNDAY);

        default:
            throw new RuntimeException("Day of week out of range!");
        }
    }

    public Integer getHours(Date date, Days day) {
        return getHours(new LocalDate(date), day);
    }

    public Integer getHours(LocalDate date, Days day) {
        CalendarData calendarData = getCalendarData(date);

        Integer hours = calendarData.getHours(day);
        BaseCalendar parent = getParent(date);
        if ((hours == null) && (parent != null)) {
            return parent.getHours(date, day);
        }

        return valueIfNotNullElseDefaultValue(hours);
    }

    private Integer valueIfNotNullElseDefaultValue(Integer hours) {
        if (hours == null) {
            return DEFAULT_VALUE;
        }
        return hours;
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

    /**
     * Creates a new {@link BaseCalendar} derived from the current calendar. The
     * new calendar will be the child of the current calendar.
     *
     * @return The derived calendar
     */
    public BaseCalendar newDerivedCalendar() {
        BaseCalendar derivedCalendar = create();
        derivedCalendar.setParent(this);
        return derivedCalendar;
    }

    public BaseCalendar newDerivedResourceCalendar() {
        BaseCalendar derivedCalendar = ResourceCalendar.create();
        derivedCalendar.setParent(this);
        return derivedCalendar;
    }

    /**
     * Creates a new version this {@link BaseCalendar} from the specific date.
     * It makes that the current calendar expires in the specific date. And the
     * new calendar will be used from that date onwards.
     */
    public void newVersion(Date date) throws IllegalArgumentException {
        newVersion(new LocalDate(date));
    }

    /**
     * Creates a new version this {@link BaseCalendar} from the specific date.
     * It makes that the current calendar expires in the specific date. And the
     * new calendar will be used from that date onwards.
     */
    public void newVersion(LocalDate date)
            throws IllegalArgumentException {
        if (date.compareTo(new LocalDate()) <= 0) {
            throw new IllegalArgumentException(
                    "Date for new version must be greater than current date");
        }

        CalendarData calendarData = getCalendarDataBeforeTheLastIfAny();
        if ((calendarData.getExpiringDate() != null)
                && (date.compareTo(calendarData.getExpiringDate()) <= 0)) {
            throw new IllegalArgumentException(
                    "Version date must be greater than expiring date of "
                            + "all versions of this calendar");
        }

        getLastCalendarData().setExpiringDate(date);

        CalendarData newCalendarData = CalendarData.create();
        newCalendarData.setParent(getLastCalendarData().getParent());
        calendarDataVersions.add(newCalendarData);
    }

    public BaseCalendar newCopy() {
        BaseCalendar copy = create();
        copyFields(copy);
        return copy;
    }

    private void copyFields(BaseCalendar copy) {
        copy.name = this.name;
        copy.calendarDataVersions = new ArrayList<CalendarData>();
        for (CalendarData calendarData : this.calendarDataVersions) {
            copy.calendarDataVersions.add(calendarData.copy());
        }
        copy.exceptions = new HashSet<ExceptionDay>(this.exceptions);
    }

    public BaseCalendar newCopyResourceCalendar() {
        BaseCalendar copy = ResourceCalendar.create();
        copyFields(copy);
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

    public List<CalendarData> getCalendarDataVersions() {
        return Collections.unmodifiableList(calendarDataVersions);
    }

    private CalendarData getCalendarData(LocalDate date) {
        for (CalendarData calendarData : calendarDataVersions) {
            if (calendarData.getExpiringDate() == null) {
                return calendarData;
            } else {
                if (date.compareTo(calendarData.getExpiringDate()) < 0) {
                    return calendarData;
                }
            }
        }

        throw new RuntimeException("Some version should not be expired");
    }

    private CalendarData getLastCalendarData() {
        if (calendarDataVersions.isEmpty()) {
            return null;
        }
        return calendarDataVersions.get(calendarDataVersions.size() - 1);
    }

    public void setHours(Days day, Integer hours) {
        CalendarData calendarData = getLastCalendarData();
        calendarData.setHours(day, hours);
    }

    public void setHours(Days day, Integer hours, Date date) {
        setHours(day, hours, new LocalDate(date));
    }

    public void setHours(Days day, Integer hours, LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        calendarData.setHours(day, hours);
    }

    private CalendarData getCalendarDataBeforeTheLastIfAny() {
        if (calendarDataVersions.size() <= 1) {
            return getLastCalendarData();
        }
        return calendarDataVersions.get(calendarDataVersions.size() - 2);
    }

    public boolean isDefault(Days day) {
        CalendarData calendarData = getLastCalendarData();
        return calendarData.isDefault(day);
    }

    public boolean isDefault(Days day, Date date) {
        return isDefault(day, new LocalDate(date));
    }

    public boolean isDefault(Days day, LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        return calendarData.isDefault(day);
    }

    public void setDefault(Days day) {
        CalendarData calendarData = getLastCalendarData();
        calendarData.setDefault(day);
    }

    public void setDefault(Days day, Date date) {
        setDefault(day, new LocalDate(date));
    }

    public void setDefault(Days day, LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        calendarData.setDefault(day);
    }

    public LocalDate getExpiringDate() {
        return getLastCalendarData().getExpiringDate();
    }

    public LocalDate getExpiringDate(Date date) {
        return getExpiringDate(new LocalDate(date));
    }

    public LocalDate getExpiringDate(LocalDate date) {
        return getCalendarData(date).getExpiringDate();
    }

    public void setExpiringDate(Date expiringDate) {
        setExpiringDate(new LocalDate(expiringDate));
    }

    public void setExpiringDate(LocalDate expiringDate) {
        setExpiringDate(expiringDate, new LocalDate());
    }

    public void setExpiringDate(Date expiringDate, Date date)
            throws IllegalArgumentException {
        setExpiringDate(new LocalDate(expiringDate), new LocalDate(date));
    }

    public void setExpiringDate(LocalDate expiringDate, LocalDate date)
            throws IllegalArgumentException {
        CalendarData calendarData = getCalendarData(date);
        setExpiringDate(calendarData, expiringDate);
    }

    private void setExpiringDate(CalendarData calendarData,
            LocalDate expiringDate) throws IllegalArgumentException {
        if (calendarData.getExpiringDate() == null) {
            throw new IllegalArgumentException("Can not set the expiring date "
                    + "because of this is the last version");
        }

        if (expiringDate.compareTo(new LocalDate()) <= 0) {
            throw new IllegalArgumentException(
                    "This date must be greater than current date");
        }

        Integer index = calendarDataVersions.indexOf(calendarData);
        if (index > 0) {
            CalendarData preivousCalendarData = calendarDataVersions.get(index - 1);
            if (expiringDate.compareTo(preivousCalendarData.getExpiringDate()) <= 0) {
                throw new IllegalArgumentException(
                        "This date must be greater than expiring date of previous calendars");
            }
        }

        calendarData.setExpiringDate(expiringDate);
    }

    private CalendarData getPreviousCalendarData(LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        Integer index = calendarDataVersions.indexOf(calendarData) - 1;
        if (index < 0) {
            return null;
        }
        return calendarDataVersions.get(index);
    }

    public LocalDate getValidFrom(Date date) {
        return getValidFrom(new LocalDate(date));
    }

    public LocalDate getValidFrom(LocalDate date) {
        CalendarData calendarData = getPreviousCalendarData(date);
        if (calendarData == null) {
            return null;
        }
        return calendarData.getExpiringDate();
    }

    public void setValidFrom(Date validFromDate, Date date) {
        setValidFrom(new LocalDate(validFromDate), new LocalDate(date));
    }

    public void setValidFrom(LocalDate validFromDate, LocalDate date)
            throws IllegalArgumentException {
        CalendarData calendarData = getPreviousCalendarData(date);
        if (calendarData == null) {
            throw new IllegalArgumentException(
                    "You can not set this date for the first version");
        }
        setExpiringDate(calendarData, validFromDate);
    }

    public boolean isLastVersion(Date date) {
        return isLastVersion(new LocalDate(date));
    }

    public boolean isLastVersion(LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        Integer index = calendarDataVersions.indexOf(calendarData);
        return (index == (calendarDataVersions.size() - 1));
    }

}
