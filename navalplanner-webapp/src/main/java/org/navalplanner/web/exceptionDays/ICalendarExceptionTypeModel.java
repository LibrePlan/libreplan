/*
 * This file is part of NavalPlan
 *
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
package org.navalplanner.web.exceptionDays;

import java.util.List;

import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.web.common.IIntegrationEntityModel;
import org.zkoss.util.InvalidValueException;

/**
 *
 * @author Diego Pino <dpino@igalia.com>
 *
 */
public interface ICalendarExceptionTypeModel extends IIntegrationEntityModel {

    void initCreate();

    void initEdit(CalendarExceptionType exceptionType);

    List<CalendarExceptionType> getExceptionDayTypes();

    CalendarExceptionType getExceptionDayType();

    void confirmSave();

    void confirmDelete(CalendarExceptionType exceptionType)
            throws InstanceNotFoundException, InvalidValueException;

}
