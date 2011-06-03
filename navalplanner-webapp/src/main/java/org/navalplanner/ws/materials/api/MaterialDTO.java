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

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;

import org.navalplanner.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for <code>Material</code> entity.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class MaterialDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "material";

    @XmlAttribute
    public String description;

    @XmlAttribute(name = "price")
    public BigDecimal defaultPrice;

    @XmlAttribute(name = "unit-type")
    public String unitType;

    @XmlAttribute
    public Boolean disabled;

    public MaterialDTO() {
    }

    public MaterialDTO(String code, String description,
            BigDecimal defaultPrice, String unitType, Boolean disabled) {

        super(code);
        this.description = description;
        this.defaultPrice = defaultPrice;
        this.unitType = unitType;
        this.disabled = disabled;

    }

    /**
     * This constructor automatically generates a unique code. It is intended to
     * facilitate the implementation of test cases that add new instances (such
     * instances will have a unique code).
     */
    public MaterialDTO(String description, BigDecimal defaultPrice,
            String unitType, Boolean disabled) {

        this(generateCode(), description, defaultPrice, unitType, disabled);

    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
