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

package org.navalplanner.business.calendars.entities;

import static org.navalplanner.business.workingday.EffortDuration.zero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.entities.EntitySequence;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.ResourcesPerDay;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;

/**
 * Represents a calendar with some exception days. A calendar is valid till the
 * expiring date, when the next calendar starts to be valid. On the other hand,
 * a calendar could be derived, and the derived calendar could add or overwrite
 * some exceptions of its parent calendar.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendar extends IntegrationEntity implements ICalendar {

    private static final EffortDuration DEFAULT_VALUE = EffortDuration.zero();

    public static BaseCalendar create() {
        return create(new BaseCalendar(CalendarData.create()));
    }

    public static BaseCalendar create(String code) {
        return create(new BaseCalendar(CalendarData.create()), code);
    }

    public static BaseCalendar createUnvalidated(String code, String name,
            BaseCalendar parent, Set<CalendarException> exceptions,
            List<CalendarData> calendarDataVersions)
            throws IllegalArgumentException {

        BaseCalendar baseCalendar = create(new BaseCalendar(CalendarData
                .create()), code);
        baseCalendar.name = name;

        if ((exceptions != null) && (!exceptions.isEmpty())) {
            for (CalendarException exception : exceptions) {
                baseCalendar.addExceptionDay(exception);
            }
        }

        if ((calendarDataVersions != null) && (!calendarDataVersions.isEmpty())) {
            baseCalendar.calendarDataVersions = calendarDataVersions;
        }

        if (parent != null) {
            baseCalendar.setParent(parent);
        }

        return baseCalendar;

    }

    public void updateUnvalidated(String name, BaseCalendar parent) {

        if (!StringUtils.isBlank(name)) {
            this.name = name;
        }

        if (parent != null) {
            setParent(parent);
        }

    }

    @NotEmpty
    private String name;

    @Valid
    private Set<CalendarException> exceptions = new HashSet<CalendarException>();

    @Valid
    private List<CalendarData> calendarDataVersions = new ArrayList<CalendarData>();

    @Valid
    private List<CalendarAvailability> calendarAvailabilities = new ArrayList<CalendarAvailability>();

    private Integer lastSequenceCode = 0;

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

    public Set<CalendarException> getOwnExceptions() {
        return Collections.unmodifiableSet(exceptions);
    }

    public Set<CalendarException> getExceptions() {
        Set<CalendarException> exceptionDays = new HashSet<CalendarException>();
        exceptionDays.addAll(exceptions);

        if (getParent() != null) {
            for (CalendarException exceptionDay : getParent().getExceptions()) {
                if (!isExceptionDayAlreadyInExceptions(exceptionDay)) {
                    exceptionDays.add(exceptionDay);
                }
            }
        }

        return Collections.unmodifiableSet(exceptionDays);
    }

    public Set<CalendarException> getExceptions(Date date) {
        return getExceptions(new LocalDate(date));
    }

    public Set<CalendarException> getExceptions(LocalDate date) {
        Set<CalendarException> exceptionDays = new HashSet<CalendarException>();
        exceptionDays.addAll(exceptions);

        if (getParent(date) != null) {
            for (CalendarException exceptionDay : getParent(date)
                    .getExceptions()) {
                if (!isExceptionDayAlreadyInExceptions(exceptionDay)) {
                    exceptionDays.add(exceptionDay);
                }
            }
        }

        return Collections.unmodifiableSet(exceptionDays);
    }

    private boolean isExceptionDayAlreadyInExceptions(
            CalendarException exceptionDay) {
        for (CalendarException day : exceptions) {
            if (day.getDate().equals(exceptionDay.getDate())) {
                return true;
            }
        }

        return false;
    }

    public void addExceptionDay(CalendarException day)
            throws IllegalArgumentException {

        if (day.getDate() == null) {
            throw new IllegalArgumentException(
                    "This exception day has a incorrect date");
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
        CalendarException day = getOwnExceptionDay(date);
        if (day == null) {
            throw new IllegalArgumentException(
                    "There is not an exception day on that date");
        }

        exceptions.remove(day);
    }

    public void updateExceptionDay(Date date, EffortDuration duration,
            CalendarExceptionType type) throws IllegalArgumentException {
        updateExceptionDay(new LocalDate(date), duration, type);
    }

    public void updateExceptionDay(LocalDate date, EffortDuration duration,
            CalendarExceptionType type) throws IllegalArgumentException {
        removeExceptionDay(date);
        CalendarException day = CalendarException.create(date, duration, type);
        addExceptionDay(day);
    }

    public CalendarException getOwnExceptionDay(Date date) {
        return getOwnExceptionDay(new LocalDate(date));
    }

    public CalendarException getOwnExceptionDay(LocalDate date) {
        for (CalendarException exceptionDay : exceptions) {
            if (exceptionDay.getDate().equals(date)) {
                return exceptionDay;
            }
        }

        return null;
    }

    public CalendarException getExceptionDay(Date date) {
        return getExceptionDay(new LocalDate(date));
    }

    public CalendarException getExceptionDay(LocalDate date) {
        for (CalendarException exceptionDay : getExceptions(date)) {
            if (exceptionDay.getDate().equals(date)) {
                return exceptionDay;
            }
        }

        return null;
    }

    public EffortDuration getCapacityOn(PartialDay date) {
        return date.limitDuration(getWorkableTimeAt(date.getDate()));
    }

    public EffortDuration getWorkableTimeAt(LocalDate date) {
        if (!isActive(date)) {
            return EffortDuration.zero();
        }
        CalendarException exceptionDay = getExceptionDay(date);
        if (exceptionDay != null) {
            return exceptionDay.getDuration();
        }

        switch (date.getDayOfWeek()) {
        case DateTimeConstants.MONDAY:
            return getDurationAt(date, Days.MONDAY);

        case DateTimeConstants.TUESDAY:
            return getDurationAt(date, Days.TUESDAY);

        case DateTimeConstants.WEDNESDAY:
            return getDurationAt(date, Days.WEDNESDAY);

        case DateTimeConstants.THURSDAY:
            return getDurationAt(date, Days.THURSDAY);

        case DateTimeConstants.FRIDAY:
            return getDurationAt(date, Days.FRIDAY);

        case DateTimeConstants.SATURDAY:
            return getDurationAt(date, Days.SATURDAY);

        case DateTimeConstants.SUNDAY:
            return getDurationAt(date, Days.SUNDAY);

        default:
            throw new RuntimeException("Day of week out of range!");
        }
    }

    private boolean isOverAssignable(LocalDate localDate) {
        CalendarException exceptionDay = getExceptionDay(localDate);
        if (exceptionDay != null) {
            return exceptionDay.getType().isOverAssignable();
        }
        return true;
    }

    public EffortDuration getDurationAt(Date date, Days day) {
        return getDurationAt(new LocalDate(date), day);
    }

    public EffortDuration getDurationAt(LocalDate date, Days day) {
        CalendarData calendarData = getCalendarData(date);

        EffortDuration duration = calendarData.getDurationAt(day);
        BaseCalendar parent = getParent(date);
        if (duration == null && parent != null) {
            return parent.getDurationAt(date, day);
        }
        return valueIfNotNullElseDefaultValue(duration);
    }

    private EffortDuration valueIfNotNullElseDefaultValue(
            EffortDuration duration) {
        if (duration == null) {
            return DEFAULT_VALUE;
        }
        return duration;
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
        return getWorkableDuration(init, end).roundToHours();
    }

    /**
     * Returns the workable duration for a specific period depending on the
     * calendar restrictions.
     */
    public EffortDuration getWorkableDuration(LocalDate init, LocalDate end) {
        EffortDuration result = zero();
        for (LocalDate current = init; current.compareTo(end) <= 0; current = current
                .plusDays(1)) {
            result = result.plus(getCapacityOn(PartialDay.wholeDay(current)));
            init = init.plusDays(1);
        }
        return result;
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
     * @return The derived calendar
     */
    public BaseCalendar newDerivedCalendar() {
        BaseCalendar derivedCalendar = create();
        derivedCalendar.setParent(this);
        return derivedCalendar;
    }

    public ResourceCalendar newDerivedResourceCalendar() {
        ResourceCalendar derivedCalendar = ResourceCalendar.create();
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
    public void newVersion(LocalDate date) throws IllegalArgumentException {
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

    public void addNewVersion(CalendarData version){
        if (version.getExpiringDate() == null) {
            if (getLastCalendarData().getExpiringDate() == null) {
                throw new IllegalArgumentException(
                        "the date is null and overlaps with the last version.");
            }
            else{
                calendarDataVersions.add(version);
                return;
            }
        }

        if (version.getExpiringDate().toDateTimeAtStartOfDay().toDate()
                .compareTo(new Date()) <= 0) {

            throw new IllegalArgumentException(
                    "You can not add a version with previous date than current date");
        }
        for (int i = 0; i < calendarDataVersions.size(); i++) {
            if ((calendarDataVersions.get(i).getExpiringDate() == null)
                    || (calendarDataVersions.get(i).getExpiringDate()
                            .compareTo(version.getExpiringDate()) > 0)) {
                if ((i - 1 >= 0)
                        && (calendarDataVersions.get(i - 1).getExpiringDate() != null)
                        && (calendarDataVersions.get(i - 1).getExpiringDate()
                                .compareTo(version.getExpiringDate()) >= 0)) {
                    throw new IllegalArgumentException(
                            "the date is null and overlap with the other version.");
                }
                calendarDataVersions.add(i, version);
                return;
            }
        }

        calendarDataVersions.add(version);

    }

    public BaseCalendar newCopy() {
        BaseCalendar copy = create();
        copyFields(copy);
        return copy;
    }

    private void copyFields(BaseCalendar copy) {
        copy.name = this.name;
        copy.setCodeAutogenerated(this.isCodeAutogenerated());
        copy.calendarDataVersions = new ArrayList<CalendarData>();
        for (CalendarData calendarData : this.calendarDataVersions) {
            copy.calendarDataVersions.add(calendarData.copy());
        }
        copy.exceptions = new HashSet<CalendarException>(this.exceptions);
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
        CalendarException exceptionDay = getExceptionDay(date);
        if (exceptionDay != null) {
            if (getOwnExceptionDay(date) != null) {
                return DayType.OWN_EXCEPTION;
            }
            return DayType.ANCESTOR_EXCEPTION;
        }
        if (getCapacityOn(PartialDay.wholeDay(date)).isZero()) {
            return DayType.ZERO_HOURS;
        }
        return DayType.NORMAL;
    }

    public List<CalendarData> getCalendarDataVersions() {
        return Collections.unmodifiableList(calendarDataVersions);
    }

    public CalendarData getCalendarData(LocalDate date) {
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

    public CalendarData getLastCalendarData() {
        if (calendarDataVersions.isEmpty()) {
            return null;
        }
        return calendarDataVersions.get(calendarDataVersions.size() - 1);
    }

    public void setDurationAt(Days day, EffortDuration duration) {
        CalendarData calendarData = getLastCalendarData();
        calendarData.setDurationAt(day, duration);
    }

    public void setDurationAt(Days day, EffortDuration effort, Date date) {
        setDurationAt(day, effort, LocalDate.fromDateFields(date));
    }

    public void setDurationAt(Days day, EffortDuration effort, LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        calendarData.setDurationAt(day, effort);
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

        Integer index = calendarDataVersions.indexOf(calendarData);
        if (index > 0) {
            CalendarData preivousCalendarData = calendarDataVersions
                    .get(index - 1);
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

    public boolean isFirstVersion(Date date) {
        return isFirstVersion(new LocalDate(date));
    }

    public boolean isLastVersion(LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        Integer index = calendarDataVersions.indexOf(calendarData);
        return (index == (calendarDataVersions.size() - 1));
    }

    public boolean isFirstVersion(LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        Integer index = calendarDataVersions.indexOf(calendarData);
        return (index == 0);
    }

    /**
     * Returns a set of non workable days (0 hours) for a specific period
     * depending on the calendar restrictions.
     */
    public Set<LocalDate> getNonWorkableDays(Date initDate, Date endDate) {
        return getNonWorkableDays(new LocalDate(initDate), new LocalDate(
                endDate));
    }

    /**
     * Returns a set of non workable days (0 hours) for a specific period
     * depending on the calendar restrictions.
     */
    public Set<LocalDate> getNonWorkableDays(LocalDate init, LocalDate end) {
        Set<LocalDate> result = new HashSet<LocalDate>();
        for (LocalDate current = init; current.compareTo(end) <= 0; current = current
                .plusDays(1)) {
            if (getCapacityOn(PartialDay.wholeDay(current)).isZero()) {
                result.add(current);
            }
        }
        return result;
    }

    public CalendarExceptionType getExceptionType(Date date) {
        return getExceptionType(new LocalDate(date));
    }

    public CalendarExceptionType getExceptionType(LocalDate date) {
        CalendarException exceptionDay = getExceptionDay(date);
        if (exceptionDay == null) {
            return null;
        }

        return exceptionDay.getType();
    }

    public void removeCalendarData(CalendarData calendarData)
            throws IllegalArgumentException {
        CalendarData lastCalendarData = getLastCalendarData();
        if (calendarData.equals(lastCalendarData)) {
            LocalDate validFrom = getValidFrom(calendarData);
            if (validFrom == null) {
                throw new IllegalArgumentException(
                        "You can not remove the current calendar data");
            }
            calendarDataVersions.remove(lastCalendarData);
            getLastCalendarData().removeExpiringDate();
        } else {
            throw new IllegalArgumentException(
                    "You just can remove the last calendar data");
        }
    }

    public LocalDate getValidFrom(CalendarData calendarData) {
        Integer index = calendarDataVersions.indexOf(calendarData);
        if (index > 0) {
            return calendarDataVersions.get(index - 1).getExpiringDate();
        }
        return null;
    }

    public List<CalendarAvailability> getCalendarAvailabilities() {
        return Collections.unmodifiableList(calendarAvailabilities);
    }

    /**
     * Returns a a copy of calendar availabilities sorted by start date.
     * calendarAvailabilities should already be sorted by start date, this
     * method is just for extra safety
     */
    private List<CalendarAvailability> getCalendarAvailabilitiesSortedByStartDate() {
        List<CalendarAvailability> result = new ArrayList<CalendarAvailability>(
                calendarAvailabilities);
        Collections.sort(result, CalendarAvailability.BY_START_DATE_COMPARATOR);
        return result;
    }

    public void addNewCalendarAvailability(
            CalendarAvailability calendarAvailability)
            throws IllegalArgumentException {
        if (this instanceof ResourceCalendar) {
            if (!calendarAvailabilities.isEmpty()) {
                CalendarAvailability lastCalendarAvailability = getLastCalendarAvailability();
                if (lastCalendarAvailability != null) {
                    if (lastCalendarAvailability.getEndDate() == null) {
                        if (lastCalendarAvailability.getStartDate().compareTo(
                                calendarAvailability.getStartDate()) >= 0) {
                            throw new IllegalArgumentException(
                                    "New calendar availability should start after the last calendar availability");
                        }
                    } else {
                        if (lastCalendarAvailability.getEndDate().compareTo(
                                calendarAvailability.getStartDate()) >= 0) {
                            throw new IllegalArgumentException(
                                    "New calendar availability should start after the last calendar availability");
                        }
                    }
                    lastCalendarAvailability.setEndDate(calendarAvailability
                            .getStartDate().minusDays(1));
                }
            }
            calendarAvailabilities.add(calendarAvailability);
        }
    }

    public void removeCalendarAvailability(
            CalendarAvailability calendarAvailability)
            throws IllegalArgumentException {
        calendarAvailabilities.remove(calendarAvailability);
    }

    public boolean isActive(Date date) {
        return isActive(new LocalDate(date));
    }

    public boolean isActive(LocalDate date) {
        if (getCalendarAvailabilities().isEmpty()) {
            return true;
        }
        for (CalendarAvailability calendarAvailability : getCalendarAvailabilities()) {
            if (calendarAvailability.isActive(date)) {
                return true;
            }
        }
        return false;
    }

    public boolean canWork(LocalDate date) {
        return isActive(date) && canWorkConsideringOnlyException(date);
    }


    private boolean canWorkConsideringOnlyException(LocalDate date) {
        CalendarException exceptionDay = getExceptionDay(date);
        return exceptionDay == null || canWorkAt(exceptionDay);
    }
    public CalendarAvailability getLastCalendarAvailability() {
        if (calendarAvailabilities.isEmpty()) {
            return null;
        }
        // Sorting for ensuring the last one is picked. In theory sorting would
        // not be necessary, doing it for safety
        List<CalendarAvailability> sorted = getCalendarAvailabilitiesSortedByStartDate();
        return sorted.get(sorted.size() - 1);
    }

    public void setStartDate(CalendarAvailability calendarAvailability,
            LocalDate startDate) throws IllegalArgumentException {
        int index = calendarAvailabilities.indexOf(calendarAvailability);
        if (index > 0) {
            if (calendarAvailabilities.get(index - 1).getEndDate().compareTo(
                    startDate) >= 0) {
                throw new IllegalArgumentException(
                        "Start date could not overlap previous calendar availability");
            }
        }
        calendarAvailability.setStartDate(startDate);
    }

    public void setEndDate(CalendarAvailability calendarAvailability,
            LocalDate endDate) throws IllegalArgumentException {
        int index = calendarAvailabilities.indexOf(calendarAvailability);
        if (index < (calendarAvailabilities.size() - 1)) {
            if (calendarAvailabilities.get(index + 1).getStartDate().compareTo(
                    endDate) <= 0) {
                throw new IllegalArgumentException(
                        "End date could not overlap next calendar availability");
            }
        }
        calendarAvailability.setEndDate(endDate);
    }

    @Override
    public EffortDuration asDurationOn(PartialDay day, ResourcesPerDay amount) {
        EffortDuration workableDuration = day
                .limitDuration(getWorkableTimeAt(day.getDate()));
        EffortDuration asDuration = amount
                .asDurationGivenWorkingDayOf(workableDuration);
        return limitOverAssignability(day.getDate(), asDuration,
                workableDuration);
    }

    private EffortDuration limitOverAssignability(LocalDate day,
            EffortDuration effortInitiallyCalculated,
            EffortDuration workableHoursAtDay) {
        boolean overAssignable = isOverAssignable(day);
        if (overAssignable) {
            return effortInitiallyCalculated;
        } else {
            return EffortDuration.min(effortInitiallyCalculated,
                    multiplyByCapacity(workableHoursAtDay));
        }
    }

    /**
     * This method is intended to be overriden
     */
    protected EffortDuration multiplyByCapacity(EffortDuration duration) {
        return duration;
    }

    @Override
    public boolean thereAreCapacityFor(AvailabilityTimeLine availability,
            ResourcesPerDay resourcesPerDay, EffortDuration durationToAllocate) {
        return ThereAreHoursOnWorkHoursCalculator.thereIsAvailableCapacityFor(
                this, availability, resourcesPerDay, durationToAllocate)
                .thereIsCapacityAvailable();
    }

    public boolean onlyGivesZeroHours() {
        return lastDataDoesntGiveOnlyZeros();
    }

    public boolean lastDataDoesntGiveOnlyZeros() {
        CalendarData last = lastCalendarData();
        return last.isEmpty();
    }

    private CalendarData lastCalendarData() {
        return calendarDataVersions.get(calendarDataVersions.size() - 1);
    }

    public boolean onlyGivesZeroHours(Days each) {
        CalendarData last = lastCalendarData();
        return last.isEmptyFor(each);
    }

    @Override
    public AvailabilityTimeLine getAvailability() {
        AvailabilityTimeLine result = AvailabilityTimeLine.allValid();
        addInvaliditiesDerivedFromCalendar(result);
        return result;
    }

    private void addInvaliditiesDerivedFromCalendar(AvailabilityTimeLine result) {
        addInvaliditiesFromAvailabilities(result);
        addInvaliditiesFromExceptions(result);
        addInvaliditiesFromCalendarDatas(result);
    }

    private void addInvaliditiesFromCalendarDatas(AvailabilityTimeLine result) {
        LocalDate previous = null;
        for (CalendarData each : calendarDataVersions) {
            addInvalidityIfDataEmpty(result, previous, each);
            previous = each.getExpiringDate();
        }
    }

    private void addInvalidityIfDataEmpty(AvailabilityTimeLine result,
            LocalDate previous, CalendarData each) {
        if (!each.isEmpty()) {
            return;
        }
        final boolean hasExpiringDate = each.getExpiringDate() != null;
        if (previous == null && hasExpiringDate) {
            result.invalidUntil(each.getExpiringDate());
        } else if (previous == null && !hasExpiringDate) {
            result.allInvalid();
        } else if (hasExpiringDate) {
            result.invalidAt(previous, each.getExpiringDate());
        } else {
            result.invalidFrom(previous);
        }
    }

    private void addInvaliditiesFromAvailabilities(AvailabilityTimeLine timeLine) {
        if (calendarAvailabilities.isEmpty()) {
            return;
        }
        List<CalendarAvailability> availabilities = getCalendarAvailabilitiesSortedByStartDate();
        CalendarAvailability previous = null;
        for (CalendarAvailability each : availabilities) {
            final boolean isFirstOne = previous == null;
            if (isFirstOne) {
                timeLine.invalidUntil(each.getStartDate());
            } else {
                // CalendarAvailability's end is inclusive
                LocalDate startOfInvalidPeriod = previous.getEndDate()
                        .plusDays(1);
                timeLine.invalidAt(startOfInvalidPeriod, each.getStartDate());
            }
            previous = each;
        }
        final CalendarAvailability last = previous;
        if (last.getEndDate() != null) {
            // CalendarAvailability's end is inclusive
            timeLine.invalidFrom(last.getEndDate().plusDays(1));
        }
    }

    private void addInvaliditiesFromExceptions(AvailabilityTimeLine timeLine) {
        for (CalendarException each : getExceptions()) {
            if (!canWorkAt(each)) {
                timeLine.invalidAt(each.getDate());
            }
        }
    }

    private boolean canWorkAt(CalendarException each) {
        return !each.getDuration().isZero()
                || each.getType().isOverAssignable();
    }

    @Override
    protected IBaseCalendarDAO getIntegrationEntityDAO() {
        return org.navalplanner.business.common.Registry.getBaseCalendarDAO();
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "the versions: the dates should be corrects and sorted and could not overlap ")
    public boolean checkConstraintDateCouldNotOverlap() {

        if (calendarDataVersions == null || calendarDataVersions.isEmpty()) {
            return true;
        }

        if (this.getLastCalendarData().getExpiringDate() != null) {
            return false;
        }

        for (int i = 0; i < calendarDataVersions.size() - 2; i++) {
            LocalDate date1 = calendarDataVersions.get(i).getExpiringDate();
            LocalDate date2 = calendarDataVersions.get(i + 1).getExpiringDate();
            if ((date1 == null) || (date2 == null)
                    || (date1.compareTo(date2) >= 0)) {
                return false;
            }
        }

        return true;
    }

    public CalendarException getCalendarExceptionByCode(String code)
            throws InstanceNotFoundException {

        if (StringUtils.isBlank(code)) {
            throw new InstanceNotFoundException(code, CalendarException.class
                    .getName());
        }

        for (CalendarException e : this.exceptions) {
            if (e.getCode().equalsIgnoreCase(StringUtils.trim(code))) {
                return e;
            }
        }

        throw new InstanceNotFoundException(code, CalendarException.class
                .getName());

    }

    public CalendarData getCalendarDataByCode(String code)
            throws InstanceNotFoundException {

        if (StringUtils.isBlank(code)) {
            throw new InstanceNotFoundException(code, CalendarData.class
                    .getName());
        }

        for (CalendarData e : this.calendarDataVersions) {
            if (e.getCode().equalsIgnoreCase(StringUtils.trim(code))) {
                return e;
            }
        }

        throw new InstanceNotFoundException(code, CalendarData.class.getName());

    }

    public void generateCalendarExceptionCodes(int numberOfDigits) {
        for (CalendarException exception : this.getExceptions()) {
            if ((exception.getCode() == null)
                    || (exception.getCode().isEmpty())
                    || (!exception.getCode().startsWith(this.getCode()))) {
                this.incrementLastSequenceCode();
                String exceptionCode = EntitySequence.formatValue(
                        numberOfDigits, this.getLastSequenceCode());
                exception.setCode(this.getCode()
                        + EntitySequence.CODE_SEPARATOR_CHILDREN
                        + exceptionCode);
            }
        }

        for (CalendarData data : this.getCalendarDataVersions()) {
            if ((data.getCode() == null) || (data.getCode().isEmpty())
                    || (!data.getCode().startsWith(this.getCode()))) {
                this.incrementLastSequenceCode();
                String dataCode = EntitySequence.formatValue(numberOfDigits,
                        this.getLastSequenceCode());
                data.setCode(this.getCode()
                        + EntitySequence.CODE_SEPARATOR_CHILDREN + dataCode);
            }
        }

        for (CalendarAvailability availability : this
                .getCalendarAvailabilities()) {
            if ((availability.getCode() == null)
                    || (availability.getCode().isEmpty())
                    || (!availability.getCode().startsWith(this.getCode()))) {
                this.incrementLastSequenceCode();
                String availabilityCode = EntitySequence.formatValue(
                        numberOfDigits, this.getLastSequenceCode());
                availability.setCode(this.getCode()
                        + EntitySequence.CODE_SEPARATOR_CHILDREN
                        + availabilityCode);
            }
        }
    }

    public void incrementLastSequenceCode() {
        if (lastSequenceCode == null) {
            lastSequenceCode = 0;
        }
        lastSequenceCode++;
    }

    @NotNull(message = "last sequence code not specified")
    public Integer getLastSequenceCode() {
        return lastSequenceCode;
    }
}
