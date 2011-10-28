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

package org.libreplan.ws.resources.criterion.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.libreplan.ws.common.api.IntegrationEntityDTO;
import org.libreplan.ws.common.api.ResourceEnumDTO;

/**
 * DTO for <code>CriterionType</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@XmlRootElement(name = "criterion-type")
public class CriterionTypeDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "criterion-type";

    @XmlAttribute
    public String name;

    @XmlAttribute
    public String description;

    @XmlAttribute(name="allow-hierarchy")
    public Boolean allowHierarchy;

    @XmlAttribute(name="allow-simultaneous-criterions-per-resource")
    public Boolean allowSimultaneousCriterionsPerResource;

    @XmlAttribute
    public Boolean enabled;

    @XmlAttribute
    public ResourceEnumDTO resource;

    @XmlElementWrapper(name="criterion-list")
    @XmlElement(name="criterion")
    public List<CriterionDTO> criterions = new ArrayList<CriterionDTO>();

    public CriterionTypeDTO() {}

    public CriterionTypeDTO(String code, String name, String description,
        Boolean allowHierarchy, Boolean allowSimultaneousCriterionsPerResource,
        Boolean enabled, ResourceEnumDTO resource,
        List<CriterionDTO> criterions) {

        super(code);
        this.name = name;
        this.description = description;
        this.allowHierarchy = allowHierarchy;
        this.allowSimultaneousCriterionsPerResource =
            allowSimultaneousCriterionsPerResource;
        this.enabled = enabled;
        this.resource = resource;
        this.criterions = criterions;

    }

    /**
     * This constructor automatically generates a unique code. It is intended
     * to facilitate the implementation of test cases that add new instances
     * (such instances will have a unique code).
     */
    public CriterionTypeDTO(String name, String description,
        Boolean allowHierarchy, Boolean allowSimultaneousCriterionsPerResource,
        Boolean enabled, ResourceEnumDTO resource,
        List<CriterionDTO> criterions) {

        this(generateCode(), name, description, allowHierarchy,
            allowSimultaneousCriterionsPerResource, enabled, resource,
            criterions);

    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
