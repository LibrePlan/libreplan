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

package org.libreplan.ws.boundusers.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.ws.boundusers.api.PersonalTimesheetEntryDTO;
import org.libreplan.ws.boundusers.api.PersonalTimesheetEntryListDTO;
import org.libreplan.ws.common.impl.DateConverter;

/**
 * Converter from/to {@link WorkReportLine} to/from DTOs.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public final class PersonalTimesheetEntryConverter {

    private PersonalTimesheetEntryConverter() {
    }

    public final static PersonalTimesheetEntryDTO toDTO(
            WorkReportLine workReportLine) {
        return new PersonalTimesheetEntryDTO(workReportLine.getOrderElement()
                .getCode(), DateConverter.toXMLGregorianCalendar(workReportLine
                .getDate()), workReportLine.getEffort().toFormattedString());
    }

    public final static PersonalTimesheetEntryListDTO toDTO(
            Collection<WorkReportLine> workReportLines) {
        List<PersonalTimesheetEntryDTO> dtos = new ArrayList<PersonalTimesheetEntryDTO>();
        for (WorkReportLine line : workReportLines) {
            dtos.add(toDTO(line));
        }
        return new PersonalTimesheetEntryListDTO(dtos);
    }

}
