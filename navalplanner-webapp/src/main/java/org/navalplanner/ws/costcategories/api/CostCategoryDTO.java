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

package org.navalplanner.ws.costcategories.api;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for {@link CostCategory} entity.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@XmlRootElement(name = "cost-category")
public class CostCategoryDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "cost-category";

    @XmlAttribute
    public String name;

    @XmlAttribute
    public Boolean enabled;

    @XmlElementWrapper(name = "hour-cost-list")
    @XmlElement(name = "hour-cost")
    public Set<HourCostDTO> hourCostDTOs = new HashSet<HourCostDTO>();

    public CostCategoryDTO() {
    }

    public CostCategoryDTO(String code, String name, Boolean enabled,
            Set<HourCostDTO> hourCostDTOs) {

        super(code);
        this.name = name;
        this.enabled = enabled;
        this.hourCostDTOs = hourCostDTOs;
    }

    /**
     * This constructor automatically generates a unique code. It is intended to
     * facilitate the implementation of test cases that add new instances (such
     * instances will have a unique code).
     */
    public CostCategoryDTO(String name, Boolean enabled,
            Set<HourCostDTO> hourCostDTOs) {

        this(generateCode(), name, enabled, hourCostDTOs);

    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}