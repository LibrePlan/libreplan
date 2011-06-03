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

package org.navalplanner.ws.common.api;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.datatype.XMLGregorianCalendar;

import org.navalplanner.business.orders.entities.OrderElement;

/**
 * DTO for {@link OrderElement} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */

public class OrderElementDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "order-element";

    @XmlAttribute
    public String name;

    @XmlAttribute(name = "init-date")
    public XMLGregorianCalendar initDate;

    @XmlAttribute
    public XMLGregorianCalendar deadline;

    @XmlAttribute
    public String description;

    @XmlElementWrapper(name = "labels")
    @XmlElement(name = "label")
    public Set<LabelReferenceDTO> labels = new HashSet<LabelReferenceDTO>();

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

    public OrderElementDTO(String name, String code,
            XMLGregorianCalendar initDate, XMLGregorianCalendar deadline,
            String description, Set<LabelReferenceDTO> labels,
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

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}