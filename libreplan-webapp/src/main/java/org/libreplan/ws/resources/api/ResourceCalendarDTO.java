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

package org.libreplan.ws.resources.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.ws.calendars.api.BaseCalendarDTO;
import org.libreplan.ws.calendars.api.CalendarDataDTO;
import org.libreplan.ws.calendars.api.CalendarExceptionDTO;

/**
 * DTO for {@link ResourceCalendar} entity.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class ResourceCalendarDTO extends BaseCalendarDTO {

    public final static String ENTITY_TYPE = "resource-calendar";

    @XmlAttribute
    public Integer capacity;

    @XmlElementWrapper(name = "calendar-availability-list")
    @XmlElement(name = "calendar-availability")
    public List<CalendarAvailabilityDTO> calendarAvailabilityDTOs = new ArrayList<CalendarAvailabilityDTO>();

    public ResourceCalendarDTO() {
    }

    public ResourceCalendarDTO(String code, String name, String parent,
            Integer capacity,
            List<CalendarExceptionDTO> calendarExceptions,
            List<CalendarDataDTO> calendarDatas,
            List<CalendarAvailabilityDTO> calendarAvailabilityDTOs) {
        super(code, name, parent, calendarExceptions, calendarDatas);
        this.capacity = capacity;
        this.calendarAvailabilityDTOs = calendarAvailabilityDTOs;
    }

    public ResourceCalendarDTO(String name, String parent, Integer capacity,
            List<CalendarExceptionDTO> calendarExceptions,
            List<CalendarDataDTO> calendarDatas,
            List<CalendarAvailabilityDTO> calendarAvailabilityDTOs) {
        this(generateCode(), name, parent, capacity, calendarExceptions,
                calendarDatas,
                calendarAvailabilityDTOs);
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}