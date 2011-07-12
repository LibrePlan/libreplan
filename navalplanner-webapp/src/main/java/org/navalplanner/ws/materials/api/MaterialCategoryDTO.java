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

package org.navalplanner.ws.materials.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.navalplanner.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for <code>MaterialCategory</code> entity.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@XmlRootElement(name = "material-category")
public class MaterialCategoryDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "material-category";

    @XmlAttribute
    public String name;

    @XmlAttribute
    public String parent;

    @XmlElement(name = "children")
    public MaterialCategoryListDTO subcategories;

    @XmlElementWrapper(name = "material-list")
    @XmlElement(name = "material")
    public List<MaterialDTO> materials = new ArrayList<MaterialDTO>();

    public MaterialCategoryDTO() {
    }

    public MaterialCategoryDTO(String code, String name, String parent,
            MaterialCategoryListDTO subcategories, List<MaterialDTO> materials) {

        super(code);
        this.name = name;
        this.parent = parent;
        this.subcategories = subcategories;
        this.materials = materials;

    }

    /**
     * This constructor automatically generates a unique code. It is intended to
     * facilitate the implementation of test cases that add new instances (such
     * instances will have a unique code).
     */
    public MaterialCategoryDTO(String name, String parent,
            MaterialCategoryListDTO subcategories, List<MaterialDTO> materials) {

        this(generateCode(), name, parent, subcategories, materials);

    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
