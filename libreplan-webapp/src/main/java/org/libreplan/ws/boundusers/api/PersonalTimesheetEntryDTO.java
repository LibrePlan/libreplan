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

package org.libreplan.ws.boundusers.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * DTO for an entry in a personal timesheet.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@XmlRootElement(name = "personal-timesheet-entry")
public class PersonalTimesheetEntryDTO {

    @XmlAttribute
    public String task;

    @XmlAttribute(name = "date")
    public XMLGregorianCalendar date;

    @XmlAttribute
    public String effort;

    public PersonalTimesheetEntryDTO() {}

    public PersonalTimesheetEntryDTO(String task, XMLGregorianCalendar date,
            String effort) {
        this.task = task;
        this.date = date;
        this.effort = effort;
    }

}
