/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.calendars.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine.IVetoer;
import org.libreplan.business.calendars.entities.CalendarData.Days;
import org.libreplan.business.common.IHumanIdentifiable;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.entities.EntitySequence;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.resources.entities.VirtualWorker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.EffortDuration.IEffortFrom;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workingday.ResourcesPerDay;

/**
 * Represents a calendar with some exception days. A calendar is valid till the
 * expiring date, when the next calendar starts to be valid. On the other hand,
 * a calendar could be derived, and the derived calendar could add or overwrite
 * some exceptions of its parent calendar.
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendar extends IntegrationEntity implements ICalendar,
        IHumanIdentifiable, Comparable<BaseCalendar> {

    private static final Capacity DEFAULT_VALUE = Capacity.zero()
            .overAssignableWithoutLimit();

    public static BaseCalendar create() {
        return create(new BaseCalendar(CalendarData.create()));
    }

    public static BaseCalendar create(String code) {
        return create(new BaseCalendar(CalendarData.create()), code);
    }

    public static BaseCalendar createBasicCalendar() {
        BaseCalendar calendar = create();
        resetDefaultCapacities(calendar);
        return calendar;
    }

    public static BaseCalendar createBasicCalendar(String code) {
        BaseCalendar calendar = create(code);
        resetDefaultCapacities(calendar);
        return calendar;
    }

    private static void resetDefaultCapacities(BaseCalendar calendar) {
        CalendarData calendarData = calendar.getLastCalendarData();
        if (calendarData != null) {
            CalendarData.resetDefaultCapacities(calendarData);
        }
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

    private String name;

    @Valid
    private Set<CalendarException> exceptions = new HashSet<CalendarException>();

    @Valid
    private List<CalendarData> calendarDataVersions = new ArrayList<CalendarData>();

    @Valid
    private List<CalendarAvailability> calendarAvailabilities = new ArrayList<CalendarAvailability>();

    private Integer lastSequenceCode = 0;

    /**
     * Constructor for hibernate. Do not use!
     */
    public BaseCalendar() {
    }

    protected BaseCalendar(CalendarData calendarData) {
        calendarDataVersions.add(calendarData);
        Collections.sort(calendarDataVersions,
                CalendarData.BY_EXPIRING_DATE_COMPARATOR);
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty(message = "name not specified")
    public String getName() {
        return name;
    }

    public BaseCalendar getParent() {
        return getLastCalendarData().getParent();
    }

    public BaseCalendar getParent(LocalDate date) {
        return getCalendarData(date).getParent();
    }

    public void setParent(BaseCalendar parent) {
        getLastCalendarData().setParent(parent);
    }

    public void setParent(BaseCalendar parent, LocalDate date) {
        getCalendarData(date).setParent(parent);
    }

    public boolean isDerived() {
        return (getParent() != null);
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

    public void removeExceptionDay(LocalDate date)
            throws IllegalArgumentException {
        CalendarException day = getOwnExceptionDay(date);
        if (day == null) {
            throw new IllegalArgumentException(
                    "There is not an exception day on that date");
        }

        exceptions.remove(day);
    }

    public void updateExceptionDay(LocalDate date, Capacity capacity,
            CalendarExceptionType type) throws IllegalArgumentException {
        removeExceptionDay(date);
        CalendarException day = CalendarException.create("", date, capacity,
                type);
        addExceptionDay(day);
    }

    public CalendarException getOwnExceptionDay(LocalDate date) {
        for (CalendarException exceptionDay : exceptions) {
            if (exceptionDay.getDate().equals(date)) {
                return exceptionDay;
            }
        }

        return null;
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
        return date.limitWorkingDay(getCapacityWithOvertime(date.getDate())
                .getStandardEffort());
    }

    @Override
    public Capacity getCapacityWithOvertime(LocalDate day) {
        Validate.notNull(day);
        return multiplyByCalendarUnits(findCapacityAt(day));
    }

    private Capacity findCapacityAt(LocalDate date) {
        if (!isActive(date)) {
            return Capacity.zero();
        }
        CalendarException exceptionDay = getExceptionDay(date);
        if (exceptionDay != null) {
            return exceptionDay.getCapacity();
        }
        return getCapacityConsideringCalendarDatasOn(date, getDayFrom(date));
    }

    private Days getDayFrom(LocalDate date) {
        return Days.values()[date.getDayOfWeek() - 1];
    }

    public Capacity getCapacityConsideringCalendarDatasOn(LocalDate date, Days day) {
        CalendarData calendarData = getCalendarData(date);

        Capacity capacity = calendarData.getCapacityOn(day);
        BaseCalendar parent = getParent(date);
        if (capacity == null && parent != null) {
            return parent.getCapacityConsideringCalendarDatasOn(date, day);
        }
        return valueIfNotNullElseDefaultValue(capacity);
    }

    private Capacity valueIfNotNullElseDefaultValue(Capacity capacity) {
        if (capacity == null) {
            return DEFAULT_VALUE;
        }
        return capacity;
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
    public EffortDuration getWorkableDuration(LocalDate init,
            LocalDate endInclusive) {
        Iterable<PartialDay> daysBetween = IntraDayDate.startOfDay(init)
                .daysUntil(
                        IntraDayDate.startOfDay(endInclusive).nextDayAtStart());

        return EffortDuration.sum(daysBetween, new IEffortFrom<PartialDay>() {

            @Override
            public EffortDuration from(PartialDay each) {
                return getCapacityOn(each);
            }

        });
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
    public void newVersion(LocalDate date) throws IllegalArgumentException {
        BaseCalendar lastParent = null;
        if (getLastCalendarData() != null) {
            lastParent = getLastCalendarData().getParent();
        }
        CalendarData newCalendarData = createLastVersion(date);
        newCalendarData.setParent(lastParent);
    }

    public CalendarData createNewVersionInsideIntersection(LocalDate startDate,
            LocalDate expiringDate) {
        for (CalendarData nextVersion : calendarDataVersions) {
            if ((nextVersion.getExpiringDate() == null)
                    || (expiringDate.compareTo(nextVersion.getExpiringDate()) <= 0)) {
                int index = calendarDataVersions.indexOf(nextVersion);
                if (index > 0) {
                    CalendarData prevVersion = calendarDataVersions
                            .get(index - 1);
                    if (newIntervalIncludeAnotherWorkWeek(startDate,
                            expiringDate, prevVersion, nextVersion)) {
                        throw new IllegalArgumentException(
                                "the new work week includes a whole work week already exists");
                    } else {
                        LocalDate prevExpiringDate = prevVersion
                                .getExpiringDate();
                        LocalDate nextExpiringDate = nextVersion
                                .getExpiringDate();
                        BaseCalendar oldParent = nextVersion.getParent();

                        if ((prevExpiringDate == null)
                                || (startDate.compareTo(prevExpiringDate) > 0)) {
                            CalendarData prevCalendarData = CalendarData
                                    .create();
                            prevCalendarData.setExpiringDate(startDate);
                            prevCalendarData.setParent(oldParent);
                            resetDefaultCapacities(prevCalendarData);
                            calendarDataVersions.add(prevCalendarData);
                        } else {
                            prevVersion.setExpiringDate(startDate);
                        }

                        CalendarData newCalendarData = CalendarData.create();
                        newCalendarData.setExpiringDate(expiringDate);
                        calendarDataVersions.add(newCalendarData);

                        if ((nextExpiringDate != null)
                                && (expiringDate.compareTo(nextExpiringDate) >= 0)) {
                            calendarDataVersions.remove(nextVersion);
                        }

                        Collections.sort(calendarDataVersions,
                                CalendarData.BY_EXPIRING_DATE_COMPARATOR);
                        return newCalendarData;
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Wrong start date : the new work week will be the first one, and the start date must be empty");
                }
            }
        }
        throw new IllegalArgumentException(
                "Wrong expiring date : the new work week will be the last one, and the expiring date must be empty");
    }

    public boolean newIntervalIncludeAnotherWorkWeek(LocalDate startDate,
            LocalDate expiringDate, CalendarData prevVersion,
            CalendarData nextVersion) {
        if ((startDate.compareTo(prevVersion.getExpiringDate()) <= 0)
                && (nextVersion.getExpiringDate() != null)
                && (expiringDate.compareTo(nextVersion.getExpiringDate()) >= 0)) {
            return true;
        }
        int indexPrevOfPrev = calendarDataVersions.indexOf(prevVersion);
        if (indexPrevOfPrev > 0) {
            CalendarData prevOfPrev = (CalendarData) calendarDataVersions
                    .get(indexPrevOfPrev - 1);
            if (startDate.compareTo(prevOfPrev.getExpiringDate()) <= 0) {
                return true;
            }
        }
        return false;
    }

    public CalendarData createLastVersion(LocalDate startDate)
            throws IllegalArgumentException {
        CalendarData calendarData = getCalendarDataBeforeTheLastIfAny();
        if ((calendarData.getExpiringDate() != null)
                && (startDate.compareTo(calendarData.getExpiringDate()) <= 0)) {
            throw new IllegalArgumentException(
                    "Wrong start date : the new work week includes a whole work week already exists");
        }

        getLastCalendarData().setExpiringDate(startDate);

        CalendarData newCalendarData = CalendarData.create();
        calendarDataVersions.add(newCalendarData);
        Collections.sort(calendarDataVersions,
                CalendarData.BY_EXPIRING_DATE_COMPARATOR);
        return newCalendarData;
    }

    public CalendarData createFirstVersion(LocalDate expiringDate)
            throws IllegalArgumentException {
        CalendarData firstVersion = getFirstCalendarData();
        if ((firstVersion.getExpiringDate() != null)
                && (expiringDate.compareTo(firstVersion.getExpiringDate()) >= 0)) {

            throw new IllegalArgumentException(
                    "Wrong expiring date : Work week expiring date must be lower than expiring date for "
                            + "all work weeks of this calendar");
        }

        CalendarData newCalendarData = CalendarData.create();
        newCalendarData.setExpiringDate(expiringDate);
        calendarDataVersions.add(newCalendarData);
        Collections.sort(calendarDataVersions,
                CalendarData.BY_EXPIRING_DATE_COMPARATOR);
        return newCalendarData;
    }

    public void newVersion(LocalDate startDate, LocalDate expiringDate,
            BaseCalendar parent) throws IllegalArgumentException {

        CalendarData newCalendarData;
        if (startDate != null && expiringDate != null) {
            if (startDate.compareTo(expiringDate) > 0) {
                throw new IllegalArgumentException(
                        "the start date must be lower than expiring date");
            }
            if (calendarDataVersions.size() == 1) {
                BaseCalendar lastParent = getLastCalendarData().getParent();
                newCalendarData = createLastVersion(startDate);
                CalendarData newLastVersion = createLastVersion(expiringDate);
                newLastVersion.setParent(lastParent);
                resetDefaultCapacities(newLastVersion);
            } else {
                newCalendarData = createNewVersionInsideIntersection(startDate,
                        expiringDate);
            }
        } else if (startDate != null) {
            newCalendarData = createLastVersion(startDate);
        } else if (expiringDate != null) {
            newCalendarData = createFirstVersion(expiringDate);
        } else {
            throw new IllegalArgumentException(
                    "At least the start date must be specified");
        }

        if (parent != null) {
            newCalendarData.setParent(parent);
        } else {
            newCalendarData.setParent(getLastCalendarData().getParent());
        }

        resetDefaultCapacities(newCalendarData);
    }

    private void resetDefaultCapacities(CalendarData version){
        if(version.getParent() == null){
            CalendarData.resetDefaultCapacities(version);
        }
    }

    public void addNewVersion(CalendarData version){
        if (version.getExpiringDate() == null) {
            if (getLastCalendarData().getExpiringDate() == null) {
                throw new IllegalArgumentException(
                        "the date is null and overlaps with the last work week.");
            }
            else{
                calendarDataVersions.add(version);
                Collections.sort(calendarDataVersions,
                        CalendarData.BY_EXPIRING_DATE_COMPARATOR);
                return;
            }
        }

        if (version.getExpiringDate().compareTo(new LocalDate()) <= 0) {

            throw new IllegalArgumentException(
                    "You can not add a work week with previous date than current date");
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
                            "the date is null and overlap with the other work week.");
                }
                calendarDataVersions.add(i, version);
                return;
            }
        }

        calendarDataVersions.add(version);
        Collections.sort(calendarDataVersions,
                CalendarData.BY_EXPIRING_DATE_COMPARATOR);

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

        throw new RuntimeException("Some work week should not be expired");
    }

    public CalendarData getLastCalendarData() {
        if (calendarDataVersions.isEmpty()) {
            return null;
        }
        return calendarDataVersions.get(calendarDataVersions.size() - 1);
    }

    public CalendarData getFirstCalendarData() {
        if (calendarDataVersions.isEmpty()) {
            return null;
        }
        return calendarDataVersions.get(0);
    }

    public void setCapacityAt(Days day, Capacity capacity) {
        CalendarData calendarData = getLastCalendarData();
        calendarData.setCapacityAt(day, capacity);
    }

    public void setCapacityAt(Days day, Capacity capacity, LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        calendarData.setCapacityAt(day, capacity);
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

    public boolean isDefault(Days day, LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        return calendarData.isDefault(day);
    }

    public void setDefault(Days day) {
        CalendarData calendarData = getLastCalendarData();
        calendarData.setDefault(day);
    }

    public void setDefault(Days day, LocalDate date) {
        CalendarData calendarData = getCalendarData(date);
        calendarData.setDefault(day);
    }

    public LocalDate getExpiringDate() {
        return getLastCalendarData().getExpiringDate();
    }

    public LocalDate getExpiringDate(LocalDate date) {
        return getCalendarData(date).getExpiringDate();
    }

    public void setExpiringDate(LocalDate expiringDate) {
        setExpiringDate(expiringDate, new LocalDate());
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
                    + "because of this is the last work week");
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
        return getPrevious(calendarData);
    }

    public CalendarData getPrevious(CalendarData calendarData) {
        Integer index = calendarDataVersions.indexOf(calendarData) - 1;
        if (index < 0) {
            return null;
        }
        return calendarDataVersions.get(index);
    }

    public LocalDate getValidFrom(LocalDate date) {
        CalendarData calendarData = getPreviousCalendarData(date);
        if (calendarData == null) {
            return null;
        }
        return calendarData.getExpiringDate();
    }

    public void setValidFrom(LocalDate validFromDate, LocalDate date)
            throws IllegalArgumentException {
        CalendarData calendarData = getPreviousCalendarData(date);
        if (calendarData == null) {
            throw new IllegalArgumentException(
                    "You can not set this date for the first work week");
        }
        setExpiringDate(calendarData, validFromDate);
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


    public CalendarExceptionType getExceptionType(LocalDate date) {
        CalendarException exceptionDay = getExceptionDay(date);
        if (exceptionDay == null) {
            return null;
        }

        return exceptionDay.getType();
    }

    public void removeCalendarData(CalendarData calendarData)
            throws IllegalArgumentException {
        if (this.getCalendarDataVersions().size() <= 1) {
            throw new IllegalArgumentException(
                    "You can not remove the last calendar data");
        }

        CalendarData lastCalendarData = getLastCalendarData();
        if (calendarData.equals(lastCalendarData)) {
            calendarDataVersions.remove(calendarData);
            getLastCalendarData().removeExpiringDate();
        } else {
            calendarDataVersions.remove(calendarData);
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

    public boolean canWorkOn(LocalDate date) {
        Capacity capacity = findCapacityAt(date);
        return capacity.allowsWorking();
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

    public boolean isLastCalendarAvailability(
            CalendarAvailability calendarAvailability) {
        if (getLastCalendarAvailability() == null
                || calendarAvailability == null) {
            return false;
        }
        if (getLastCalendarAvailability().getId() == null
                && calendarAvailability.getId() == null) {
            return getLastCalendarAvailability() == calendarAvailability;
        }
        return ObjectUtils.equals(getLastCalendarAvailability().getId(),
                calendarAvailability.getId());
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
        Capacity capacity = findCapacityAt(day.getDate());
        EffortDuration oneResourcePerDayWorkingDuration = day
                .limitWorkingDay(capacity.getStandardEffort());
        EffortDuration amountRequestedDuration = amount
                .asDurationGivenWorkingDayOf(oneResourcePerDayWorkingDuration);

        EffortDuration duration = multiplyByCalendarUnits(capacity)
                .limitDuration(amountRequestedDuration);
        return duration.atNearestMinute();
    }

    /**
     * <p>
     * Calendar units are the number of units this calendar is applied to. For
     * example a {@link VirtualWorker} composed of ten workers would multiply
     * the capacity by ten.
     * </p>
     * <p>
     * This method is intended to be overridden
     * </p>
     *
     */
    protected Capacity multiplyByCalendarUnits(Capacity capacity) {
        return capacity;
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

    public boolean onlyGivesZeroHours(Days day) {
        for (CalendarData each : calendarDataVersions) {
            if (!each.isEmptyFor(day)) {
                return false;
            }
        }
        return true;
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
        addInvaliditiesFromEmptyCalendarDatas(result);
        addInvaliditiesFromEmptyDaysInCalendarDatas(result);
    }

    private void addInvaliditiesFromEmptyDaysInCalendarDatas(
            AvailabilityTimeLine result) {
        result.setVetoer(new IVetoer() {

            @Override
            public boolean isValid(LocalDate date) {
                return canWorkOn(date);
            }
        });
    }

    private void addInvaliditiesFromEmptyCalendarDatas(
            AvailabilityTimeLine result) {
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
            if (!each.getCapacity().allowsWorking()) {
                timeLine.invalidAt(each.getDate());
            }
        }
    }

    @Override
    protected IBaseCalendarDAO getIntegrationEntityDAO() {
        return org.libreplan.business.common.Registry.getBaseCalendarDAO();
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "the work week: the dates should be corrects and sorted and could not overlap ")
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

    @AssertTrue(message = "calendars with zero hours are not allowed")
    public boolean checkConstraintZeroHours() {
        if ((calendarDataVersions != null) && (!calendarDataVersions.isEmpty())) {
            for (CalendarData each : calendarDataVersions) {
                if (!each.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getHumanId() {
        return name;
    }

    @Override
    public int compareTo(BaseCalendar calendar) {
        return this.getName().compareToIgnoreCase(calendar.getName());
    }

}
