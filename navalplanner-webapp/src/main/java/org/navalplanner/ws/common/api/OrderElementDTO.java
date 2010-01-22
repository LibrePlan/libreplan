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

package org.navalplanner.ws.common.api;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.navalplanner.business.orders.entities.OrderElement;

/**
 * DTO for {@link OrderElement} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@XmlRootElement(name = "order-element")
public class OrderElementDTO {

    @XmlAttribute
    public String name;

    @XmlAttribute
    public String code;

    @XmlAttribute(name = "init-date")
    public Date initDate;

    @XmlAttribute
    public Date deadline;

    @XmlAttribute
    public String description;

    @XmlElementWrapper(name = "labels")
    @XmlElement(name = "label")
    public Set<LabelDTO> labels = new HashSet<LabelDTO>();

    @XmlElementWrapper(name = "material-assignments")
    @XmlElement(name = "material-assignment")
    public Set<MaterialAssignmentDTO> materialAssignments = new HashSet<MaterialAssignmentDTO>();

    @XmlElementWrapper(name = "advance-measurements")
    @XmlElement(name = "advance-measurement")
    public Set<AdvanceMeasurementDTO> advanceMeasurements = new HashSet<AdvanceMeasurementDTO>();

    @XmlElementWrapper(name = "criterion-requirements")
    @XmlElements( {
            @XmlElement(name = "direct-criterion-requirement", type = DirectCriterionRequirementDTO.class),
            @XmlElement(name = "indirect-criterion-requirement", type = IndirectCriterionRequirementDTO.class) })
    public Set<CriterionRequirementDTO> criterionRequirements = new HashSet<CriterionRequirementDTO>();

    public OrderElementDTO() {
    }

    public OrderElementDTO(String name, String code, Date initDate,
            Date deadline, String description, Set<LabelDTO> labels,
            Set<MaterialAssignmentDTO> materialAssignments,
            Set<AdvanceMeasurementDTO> advanceMeasurements,
            Set<CriterionRequirementDTO> criterionRequirements) {
        this.name = name;
        this.code = code;
        this.initDate = initDate;
        this.deadline = deadline;
        this.description = description;
        this.labels = labels;
        this.materialAssignments = materialAssignments;
        this.advanceMeasurements = advanceMeasurements;
        this.criterionRequirements = criterionRequirements;
    }

}