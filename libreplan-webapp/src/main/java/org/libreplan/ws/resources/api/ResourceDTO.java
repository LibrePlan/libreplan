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

package org.libreplan.ws.resources.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.libreplan.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for <code>Resource</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public abstract class ResourceDTO extends IntegrationEntityDTO {

    @XmlElement(name = "calendar")
    public ResourceCalendarDTO calendar;

    @XmlElementWrapper(name="criterion-satisfaction-list")
    @XmlElement(name="criterion-satisfaction")
    public List<CriterionSatisfactionDTO> criterionSatisfactions =
        new ArrayList<CriterionSatisfactionDTO>();

    @XmlElementWrapper(name="resources-cost-category-assignment-list")
    @XmlElement(name="resources-cost-category-assignment")
    public List<ResourcesCostCategoryAssignmentDTO>
        resourcesCostCategoryAssignments =
            new ArrayList<ResourcesCostCategoryAssignmentDTO>();

    protected ResourceDTO() {}

    protected ResourceDTO(String code) {
        super(code);
    }

}
