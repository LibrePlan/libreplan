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

import org.navalplanner.business.orders.entities.HoursGroup;

/**
 * DTO for {@link HoursGroup} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class HoursGroupDTO {

    @XmlAttribute(name = "code")
    public String code;

    @XmlAttribute(name = "resource-type")
    public ResourceEnumDTO resourceType;

    @XmlAttribute(name = "working-hours")
    public Integer workingHours;

    @XmlElementWrapper(name = "criterion-requirements")
    @XmlElements( {
            @XmlElement(name = "direct-criterion-requirement", type = DirectCriterionRequirementDTO.class),
            @XmlElement(name = "indirect-criterion-requirement", type = IndirectCriterionRequirementDTO.class) })
    public Set<CriterionRequirementDTO> criterionRequirements = new HashSet<CriterionRequirementDTO>();

    public HoursGroupDTO() {
    }

    public HoursGroupDTO(String name, ResourceEnumDTO resourceType,
            Integer workingHours,
            Set<CriterionRequirementDTO> criterionRequirements) {
        this.code = name;
        this.resourceType = resourceType;
        this.workingHours = workingHours;
        this.criterionRequirements = criterionRequirements;
    }

}
