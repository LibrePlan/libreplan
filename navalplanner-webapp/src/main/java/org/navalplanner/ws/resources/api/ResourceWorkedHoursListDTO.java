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

package org.navalplanner.ws.resources.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for a list of {@link ResourceWorkedHoursDTO}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@XmlRootElement(name = "resource-worked-hours-list")
public class ResourceWorkedHoursListDTO {

    @XmlElement(name = "resource-worked-hours")
    public List<ResourceWorkedHoursDTO> resourceWorkedHoursListDTOs = new ArrayList<ResourceWorkedHoursDTO>();

    @XmlAttribute(name = "start-date")
    public Date startDate;

    @XmlAttribute(name = "end-date")
    public Date endDate;

    public ResourceWorkedHoursListDTO() {
    }

    public ResourceWorkedHoursListDTO(
            List<ResourceWorkedHoursDTO> resourceWorkedHoursListDTOs,
            Date startDate, Date endDate) {
        this.resourceWorkedHoursListDTOs = resourceWorkedHoursListDTOs;
        this.startDate = startDate;
        this.endDate = endDate;
    }

}
