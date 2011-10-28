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

package org.libreplan.ws.calendars.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.datatype.XMLGregorianCalendar;

import org.libreplan.business.calendars.entities.CalendarData;
import org.libreplan.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for {@link CalendarData} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarDataDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "calendar-data";

    @XmlElementWrapper(name = "hours-per-day-list")
    @XmlElement(name = "hours-per-day")
    public List<HoursPerDayDTO> hoursPerDays = new ArrayList<HoursPerDayDTO>();

    @XmlAttribute(name = "expiring-date")
    public XMLGregorianCalendar expiringDate;

    @XmlAttribute(name = "parent-calendar")
    public String parentCalendar;

    public CalendarDataDTO() {
    }

    public CalendarDataDTO(String code, List<HoursPerDayDTO> hoursPerDays,
            XMLGregorianCalendar expiringDate, String parentCalendar) {
        super(code);
        this.hoursPerDays = hoursPerDays;
        this.expiringDate = expiringDate;
        this.parentCalendar = parentCalendar;
    }

    public CalendarDataDTO(List<HoursPerDayDTO> hoursPerDays,
            XMLGregorianCalendar expiringDate, String parentCalendar) {
        this(generateCode(), hoursPerDays, expiringDate, parentCalendar);
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
