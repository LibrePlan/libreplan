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

package org.libreplan.ws.common.api;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.libreplan.business.orders.entities.OrderLine;

/**
 * DTO for {@link OrderLine} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@XmlRootElement(name = "order-line")
public class OrderLineDTO extends OrderElementDTO {

    @XmlElementWrapper(name = "hours-groups")
    @XmlElement(name = "hours-group")
    public Set<HoursGroupDTO> hoursGroups = new HashSet<HoursGroupDTO>();

    public OrderLineDTO() {
        super();
    }

    public OrderLineDTO(String name, String code,
            XMLGregorianCalendar initDate, XMLGregorianCalendar deadline,
            String description, Set<LabelReferenceDTO> labels,
            Set<MaterialAssignmentDTO> materialAssignments,
            Set<AdvanceMeasurementDTO> advanceMeasurements,
            Set<CriterionRequirementDTO> criterionRequirements,
            Set<HoursGroupDTO> hoursGroups) {
        super(name, code, initDate, deadline, description, labels,
                materialAssignments, advanceMeasurements, criterionRequirements);
        this.hoursGroups = hoursGroups;
    }

}
