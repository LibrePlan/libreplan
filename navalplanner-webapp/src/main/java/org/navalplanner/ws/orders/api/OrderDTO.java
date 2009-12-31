/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.ws.orders.api;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.navalplanner.business.orders.entities.Order;

/**
 * DTO for {@link Order} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@XmlRootElement(name = "order")
public class OrderDTO extends OrderLineGroupDTO {

    @XmlAttribute(name = "dependencies-constraints-have-priority")
    public Boolean dependenciesConstraintsHavePriority;

    @XmlAttribute(name = "calendar-name")
    public String calendarName;

    public OrderDTO() {
        super();
    }

    public OrderDTO(String name, String code, Date initDate, Date deadline,
            String description, Set<LabelDTO> labels,
            Set<MaterialAssignmentDTO> materialAssignments,
            Set<AdvanceMeasurementDTO> advanceMeasurements,
            Set<CriterionRequirementDTO> criterionRequirements,
            List<OrderElementDTO> children,
            Boolean dependenciesConstraintsHavePriority, String calendarName) {
        super(name, code, initDate, deadline, description, labels,
                materialAssignments, advanceMeasurements,
                criterionRequirements, children);
        this.dependenciesConstraintsHavePriority = dependenciesConstraintsHavePriority;
        this.calendarName = calendarName;
    }

}
