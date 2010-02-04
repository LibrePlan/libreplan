/*
 * This file is part of NavalPlan
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

package org.navalplanner.ws.resources.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * DTO for <code>Resource</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public abstract class ResourceDTO {

    @XmlAttribute(name="calendar-name")
    public String calendarName;

    @XmlElementWrapper(name="criterion-satisfaction-list")
    @XmlElement(name="criterion-satisfaction")
    public List<CriterionSatisfactionDTO> criterionSatisfactions =
        new ArrayList<CriterionSatisfactionDTO>();

    @XmlElementWrapper(name="resources-cost-category-assignment-list")
    @XmlElement(name="resources-cost-category-assignment")
    public List<ResourcesCostCategoryAssignmentDTO>
        resourcesCostCategoryAssignments =
            new ArrayList<ResourcesCostCategoryAssignmentDTO>();

}
