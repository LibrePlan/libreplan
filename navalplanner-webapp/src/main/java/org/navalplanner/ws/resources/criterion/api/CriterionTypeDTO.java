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

package org.navalplanner.ws.resources.criterion.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * DTO for <code>CriterionType</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class CriterionTypeDTO {

    @XmlAttribute
    public String name;

    @XmlAttribute
    public String description;

    @XmlAttribute(name="allow-hierarchy")
    public boolean allowHierarchy = true;

    @XmlAttribute(name="allow-simultaneous-criterions-per-resource")
    public boolean allowSimultaneousCriterionsPerResource = true;

    @XmlAttribute
    public boolean enabled = true;

    @XmlAttribute
    public ResourceEnumDTO resource = ResourceEnumDTO.RESOURCE;

    @XmlElementWrapper(name="criterion-list")
    @XmlElement(name="criterion")
    public List<CriterionDTO> criterions = new ArrayList<CriterionDTO>();

    public CriterionTypeDTO() {}

    public CriterionTypeDTO(String name, String description,
        boolean allowHierarchy, boolean allowSimultaneousCriterionsPerResource,
        boolean enabled, ResourceEnumDTO resource,
        List<CriterionDTO> criterions) {

        this.name = name;
        this.description = description;
        this.allowHierarchy = allowHierarchy;
        this.allowSimultaneousCriterionsPerResource =
            allowSimultaneousCriterionsPerResource;
        this.enabled = enabled;
        this.resource = resource;
        this.criterions = criterions;

    }

}
