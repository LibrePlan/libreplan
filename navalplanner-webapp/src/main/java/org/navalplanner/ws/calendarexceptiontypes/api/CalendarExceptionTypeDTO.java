/*
 * This file is part of NavalPlan
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

package org.navalplanner.ws.calendarexceptiontypes.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for {@link CalendarExceptionType} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@XmlRootElement(name = "calendar-exception-type")
public class CalendarExceptionTypeDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "calendar-exception-type";

    @XmlAttribute
    public String name;

    @XmlAttribute
    public CalendarExceptionTypeColorDTO color;

    @XmlAttribute(name = "over-assignable")
    public boolean overAssignable;

    @XmlAttribute
    public int duration;

    public CalendarExceptionTypeDTO() {
    }

    public CalendarExceptionTypeDTO(String code, String name,
            CalendarExceptionTypeColorDTO color,
            boolean overAssignable, int duration) {
        super(code);
        this.name = name;
        this.color = color;
        this.overAssignable = overAssignable;
        this.duration = duration;
    }

    public CalendarExceptionTypeDTO(String name,
            CalendarExceptionTypeColorDTO color,
            boolean overAssignable, int duration) {
        this(generateCode(), name, color, overAssignable, duration);
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}