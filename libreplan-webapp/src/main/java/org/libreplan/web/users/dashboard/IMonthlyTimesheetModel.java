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

/**
 * Interface for creation/edition of a monthly timesheet model
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IMonthlyTimesheetModel {

    /**
     * Edits the monthly timesheet for the specified <code>date</code> and
     * resource bound to current user or creates a new one if it doesn't exist
     * yet.
     */
    void initCreateOrEdit(LocalDate date);

    /**
     * Edits the monthly timesheet for the specified <code>date</code> and
     * resource bound to the {@link Worker} specified by the
     * <code>resource</code> or creates a new one if it doesn't exist yet.
     */
    void initCreateOrEdit(LocalDate date, Resource resource);

    /**
     * Returns the date of the monthly timesheet (only year and month should
     * take into account as the day is not important to define a monthly
     * timesheet).
     */
    LocalDate getDate();

    /**
     * Returns the first day of the month of the current monthly timesheet.
     */
    LocalDate getFirstDay();

    /**
     * Returns the last day of the month of the current monthly timesheet.
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
     * Returns the {@link EffortDuration} in the current monthly timesheet for
     * the specified <code>orderElement</code> and <code>date</code>.
     */
    EffortDuration getEffortDuration(OrderElement orderElement, LocalDate date);

    /**
     * Sets the {@link EffortDuration} in the current monthly timesheet for the
     * specified <code>orderElement</code> and <code>date</code>.<br />
     *
     * Marks the current monthly timesheet as modified.
     */
    void setEffortDuration(OrderElement orderElement, LocalDate date,
            EffortDuration effortDuration);

    /**
     * Save {@link WorkReport} for the monthly timesheet.
     */
    void save();

    /**
     * Cancel changes in {@link WorkReport} for the monthly timesheet.
     */
    void cancel();

    /**
     * Returns the {@link EffortDuration} in the current monthly timesheet for
     * the specified <code>orderElement</code>.
     */
    EffortDuration getEffortDuration(OrderElement orderElement);

    /**
     * Returns the {@link EffortDuration} for all the {@link OrderElement
     * OrderElements} in the current monthly timesheet in the specified
     * <code>date</code>.
     */
    EffortDuration getEffortDuration(LocalDate date);

    /**
     * Returns the total {@link EffortDuration} for the currently monthly
     * timesheet.
     */
    EffortDuration getTotalEffortDuration();

    /**
     * Returns the capacity of the current resource for the specified
     * <code>date</code>.
     */
    EffortDuration getResourceCapacity(LocalDate date);

    /**
     * Adds the <code>orderElement</code> to the current monthly timehseet.
     */
    void addOrderElement(OrderElement orderElement);

    /**
     * Returns the {@link Order} of the <code>orderElement</code> avoiding a
     * proxy.
     */
    Order getOrder(OrderElement orderElement);

    /**
     * Returns <code>true</code> if current monthly timesheet has been modified
     * by the user.
     */
    boolean isModified();

    /**
     * Checks if current monthly timesheet is the first month, that means the
     * first activation period of the resource.
     */
    boolean isFirstMonth();

    /**
     * Checks if current monthly timesheet is the last month, that means the
     * next month of current date.
     */
    boolean isLastMonth();

    /**
     * Returns true if the value for the specified <code>orderElement</code> in
     * a given <code>date</date> has been modified by the user.
     */
    boolean wasModified(OrderElement orderElement, LocalDate date);

    /**
     * Returns <code>true</code> or <code>false</code> depending on if it's
     * editing a monthly timesheet of the current user or not.<br />
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
     * Returns <code>true</code> if the resource of the current monthly
     * timesheet has any effort reported in other {@link WorkReport WorkReports}
     * in the month of the timesheet.
     */
    boolean hasOtherReports();

    /**
     * Returns the {@link EffortDuration} of the specified
     * <code>orderElement</code> from other {@link WorkReport WorkReports} for
     * the current resource in the month of the timesheet.<br />
     */
    EffortDuration getOtherEffortDuration(OrderElement orderElement);

    /**
     * Returns the {@link EffortDuration} in the specified <code>date</code>
     * from other {@link WorkReport WorkReports} for the current resource in the
     * month of the timesheet.
     */
    EffortDuration getOtherEffortDuration(LocalDate date);

    /**
     * Returns the total {@link EffortDuration} from other {@link WorkReport
     * WorkReports} for the current resource in the month of the timesheet.
     */
    EffortDuration getTotalOtherEffortDuration();

    /**
     * Returns the {@link PersonalTimesheetsPeriodicityEnum} from
     * {@link Configuration}.
     */
    PersonalTimesheetsPeriodicityEnum getPersonalTimesheetsPeriodicity();

}
