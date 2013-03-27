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

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.libreplan.business.planner.entities.Task;

/**
 * DTO for a {@link Task} entity.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@XmlRootElement(name = "task")
public class TaskDTO {

    @XmlAttribute
    public String name;

    @XmlAttribute
    public String code;

    @XmlAttribute(name = "project-code")
    public String projectCode;

    @XmlAttribute(name = "project-name")
    public String projectName;

    @XmlAttribute(name = "start-date")
    public XMLGregorianCalendar startDate;

    @XmlAttribute(name = "end-date")
    public XMLGregorianCalendar endDate;

    @XmlAttribute(name = "progress-value")
    public BigDecimal progressValue;

    @XmlAttribute(name = "progress-date")
    public XMLGregorianCalendar progressDate;

    @XmlAttribute
    public String effort;

    public TaskDTO() {}

    public TaskDTO(String name, String code, String projectCode,
            String projectName, XMLGregorianCalendar startDate,
            XMLGregorianCalendar endDate, BigDecimal progressValue,
            XMLGregorianCalendar progressDate, String effort) {
        this.name = name;
        this.code = code;
        this.projectCode = projectCode;
        this.projectName = projectName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progressValue = progressValue;
        this.progressDate = progressDate;
        this.effort = effort;
    }

}
