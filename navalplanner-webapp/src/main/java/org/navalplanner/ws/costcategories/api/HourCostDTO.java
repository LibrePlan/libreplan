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

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.datatype.XMLGregorianCalendar;

import org.navalplanner.business.costcategories.entities.HourCost;
import org.navalplanner.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for {@link HourCost} entity.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class HourCostDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "hour-cost";

    @XmlAttribute
    public BigDecimal priceCost;

    @XmlAttribute
    public XMLGregorianCalendar initDate;

    @XmlAttribute
    public XMLGregorianCalendar endDate;

    @XmlAttribute(name = "work-hours-type")
    public String type;

    public HourCostDTO() {
    }

    public HourCostDTO(String code, BigDecimal priceCost,
            XMLGregorianCalendar initDate,
 XMLGregorianCalendar endDate,
            String type) {

        super(code);
        this.initDate = initDate;
        this.endDate = endDate;
        this.priceCost = priceCost;
        this.type = type;
    }

    /**
     * This constructor automatically generates a unique code. It is intended to
     * facilitate the implementation of test cases that add new instances (such
     * instances will have a unique code).
     */
    public HourCostDTO(BigDecimal priceCost, XMLGregorianCalendar initDate,
            XMLGregorianCalendar endDate,
            String type) {

        this(generateCode(), priceCost, initDate, endDate, type);

    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
