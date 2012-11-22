/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.web.users.dashboard;

import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.common.entities.Configuration;
import org.libreplan.business.common.entities.PersonalTimesheetsPeriodicityEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;

/**
 * Interface for creation/edition of a personal timesheet model
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IPersonalTimesheetModel {

    /**
     * Edits the personal timesheet for the specified <code>date</code> and
     * resource bound to current user or creates a new one if it doesn't exist
     * yet.
     */
    void initCreateOrEdit(LocalDate date);

    /**
     * Edits the personal timesheet for the specified <code>date</code> and
     * resource bound to the {@link Worker} specified by the
     * <code>resource</code> or creates a new one if it doesn't exist yet.
     */
    void initCreateOrEdit(LocalDate date, Resource resource);

    /**
     * Returns the date of the personal timesheet.
     */
    LocalDate getDate();

    /**
     * Returns the first day of the current personal timesheet.
     */
    LocalDate getFirstDay();

    /**
     * Returns the last day of the current personal timesheet.
     */
    LocalDate getLastDate();

    /**
     * Returns resource bound to current user.
     */
    Worker getWorker();

    /**
     * At this moment returns the list of {@link OrderElement OrderElements}
     * where the resource bound to current user is assigned.
     */
    List<OrderElement> getOrderElements();

    /**
     * Returns the {@link EffortDuration} in the current personal timesheet for
     * the specified <code>orderElement</code> and <code>date</code>.
     */
    EffortDuration getEffortDuration(OrderElement orderElement, LocalDate date);

    /**
     * Sets the {@link EffortDuration} in the current personal timesheet for the
     * specified <code>orderElement</code> and <code>date</code>.<br />
     *
     * Marks the current personal timesheet as modified.
     */
    void setEffortDuration(OrderElement orderElement, LocalDate date,
            EffortDuration effortDuration);

    /**
     * Save {@link WorkReport} for the personal timesheet.
     */
    void save();

    /**
     * Cancel changes in {@link WorkReport} for the personal timesheet.
     */
    void cancel();

    /**
     * Returns the {@link EffortDuration} in the current personal timesheet for
     * the specified <code>orderElement</code>.
     */
    EffortDuration getEffortDuration(OrderElement orderElement);

    /**
     * Returns the {@link EffortDuration} for all the {@link OrderElement
     * OrderElements} in the current personal timesheet in the specified
     * <code>date</code>.
     */
    EffortDuration getEffortDuration(LocalDate date);

    /**
     * Returns the total {@link EffortDuration} for the currently personal
     * timesheet.
     */
    EffortDuration getTotalEffortDuration();

    /**
     * Returns the capacity of the current resource for the specified
     * <code>date</code>.
     */
    EffortDuration getResourceCapacity(LocalDate date);

    /**
     * Adds the <code>orderElement</code> to the current personal timehseet.
     */
    void addOrderElement(OrderElement orderElement);

    /**
     * Returns the {@link Order} of the <code>orderElement</code> avoiding a
     * proxy.
     */
    Order getOrder(OrderElement orderElement);

    /**
     * Returns <code>true</code> if current personal timesheet has been modified
     * by the user.
     */
    boolean isModified();

    /**
     * Checks if current personal timesheet is the first period, that means the
     * first activation period of the resource.
     */
    boolean isFirstPeriod();

    /**
     * Checks if current personal timesheet is the last period, that means the
     * next month of current date.
     */
    boolean isLastPeriod();

    /**
     * Returns true if the value for the specified <code>orderElement</code> in
     * a given <code>date</date> has been modified by the user.
     */
    boolean wasModified(OrderElement orderElement, LocalDate date);

    /**
     * Returns <code>true</code> or <code>false</code> depending on if it's
     * editing a personal timesheet of the current user or not.<br />
     *
     * That means if you entered via:
     * <ul>
     * <li>{@link #initCreateOrEdit(LocalDate)}: It returns <code>true</code>.</li>
     * <li>{@link #initCreateOrEdit(LocalDate, Resource)}: It returns
     * <code>false</code>.</li>
     * </ul>
     */
    boolean isCurrentUser();

    /**
     * Returns <code>true</code> if the resource of the current personal
     * timesheet has any effort reported in other {@link WorkReport WorkReports}
     * in the period of the timesheet.
     */
    boolean hasOtherReports();

    /**
     * Returns the {@link EffortDuration} of the specified
     * <code>orderElement</code> from other {@link WorkReport WorkReports} for
     * the current resource in the period of the timesheet.<br />
     */
    EffortDuration getOtherEffortDuration(OrderElement orderElement);

    /**
     * Returns the {@link EffortDuration} in the specified <code>date</code>
     * from other {@link WorkReport WorkReports} for the current resource in the
     * period of the timesheet.
     */
    EffortDuration getOtherEffortDuration(LocalDate date);

    /**
     * Returns the total {@link EffortDuration} from other {@link WorkReport
     * WorkReports} for the current resource in the period of the timesheet.
     */
    EffortDuration getTotalOtherEffortDuration();

    /**
     * Returns the {@link PersonalTimesheetsPeriodicityEnum} from
     * {@link Configuration}.
     */
    PersonalTimesheetsPeriodicityEnum getPersonalTimesheetsPeriodicity();

    /**
     * Returns the string that represents the personal timesheet depending on
     * the configured periodicity.
     */
    String getTimesheetString();

    /**
     * Returns the previous personal timesheet to the current one depending on
     * the configured periodicity.
     */
    LocalDate getPrevious();

    /**
     * Returns the next personal timesheet to the current one depending on the
     * configured periodicity.
     */
    LocalDate getNext();

    /**
     * Returns <code>true</code> (or <code>false</code>) if the specified
     * <code>orderElement</code> is marked as finished (or not) in the current
     * personal timesheet for the specified <code>date</code>.
     */
    Boolean isFinished(OrderElement orderElement, LocalDate date);

    /**
     * Mark the specified <code>orderElement</code> as finished in the current
     * personal timesheet for the specified <code>date</code>.<br />
     *
     * Marks the current personal timesheet as modified.
     */
    void setFinished(OrderElement orderElement, LocalDate textboxDate,
            Boolean finished);

    /**
     * Checks if the specified <code>orderElement</code> is marked or not as
     * finished in any {@link WorkReportLine}.
     */
    Boolean isFinished(OrderElement orderElement);

}
