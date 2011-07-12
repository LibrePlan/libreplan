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

package org.navalplanner.ws.typeofworkhours.api;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.navalplanner.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for {@link TypeOfWorkHours} entity.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@XmlRootElement(name = "type-work-hours")
public class TypeOfWorkHoursDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "type-work-hours";

    @XmlAttribute
    public String name;

    @XmlAttribute
    public BigDecimal defaultPrice;

    @XmlAttribute
    public Boolean enabled;

    public TypeOfWorkHoursDTO() {
    }

    public TypeOfWorkHoursDTO(String code, String name, Boolean enabled,
            BigDecimal defaultPrice) {

        super(code);
        this.name = name;
        this.enabled = enabled;
        this.defaultPrice = defaultPrice;
    }

    /**
     * This constructor automatically generates a unique code. It is intended to
     * facilitate the implementation of test cases that add new instances (such
     * instances will have a unique code).
     */
    public TypeOfWorkHoursDTO(String name, Boolean enabled,
            BigDecimal defaultPrice) {

        this(generateCode(), name, enabled, defaultPrice);

    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}