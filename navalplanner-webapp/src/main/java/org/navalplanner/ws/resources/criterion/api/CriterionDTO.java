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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for <code>Criterion</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@XmlRootElement(name="criterion")
public class CriterionDTO {

    @XmlAttribute
    public String name;

    @XmlAttribute
    public boolean active = true;

    @XmlElementWrapper(name="children")
    @XmlElement(name="criterion")
    public List<CriterionDTO> children = new ArrayList<CriterionDTO>();

    public CriterionDTO() {}

    public CriterionDTO(String name, boolean active,
        List<CriterionDTO> children) {

        this.name = name;
        this.active = active;
        this.children = children;

    }

}
