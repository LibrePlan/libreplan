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

import org.libreplan.business.calendars.entities.CalendarAvailability;
import org.libreplan.business.common.entities.PersonalTimesheetsPeriodicityEnum;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.workreports.entities.WorkReport;

/**
 * Interface for "Personal timesheets" area model
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IPersonalTimesheetsAreaModel {

    /**
     * Returns the list of {@link PersonalTimesheetDTO PersonalTimesheetDTOs}
     * for the resource bound to current {@link User}.<br />
     *
     * There's no need that a {@link WorkReport} is saved in order to a
     * {@link PersonalTimesheetDTO} exists for a period.<br />
     *
     * The list of {@link PersonalTimesheetDTO PersonalTimesheetDTOs} will be
     * since the date the resource is activated in the system (checking
     * {@link CalendarAvailability} for the resource) to next month of current
     * date.
     */
    List<PersonalTimesheetDTO> getPersonalTimesheets();

    /**
     * Returns the number of different {@link OrderElement OrderElements} with
     * tracked time in the specified <code>workReport</code>.
     */
    int getNumberOfOrderElementsWithTrackedTime(WorkReport workReport);

    /**
     * Returns configured periodicity for personal timesheets.
     */
    PersonalTimesheetsPeriodicityEnum getPersonalTimesheetsPeriodicity();

}
