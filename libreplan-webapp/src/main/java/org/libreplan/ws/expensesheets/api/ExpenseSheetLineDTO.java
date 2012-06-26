/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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

package org.libreplan.ws.expensesheets.api;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for {@link ExpenseSheet} entity.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@XmlRootElement(name = "expense-sheet-line")
public class ExpenseSheetLineDTO extends IntegrationEntityDTO {

    public final static String ENTITY_TYPE = "expense-sheet-line";

    @XmlAttribute
    public String concept;

    @XmlAttribute
    public BigDecimal value;

    @XmlAttribute
    public String resource;

    @XmlAttribute(name = "task")
    public String orderElement;

    @XmlAttribute
    public XMLGregorianCalendar date;

    public ExpenseSheetLineDTO() {
    }

    public ExpenseSheetLineDTO(String code, String concept, BigDecimal value, String resource,
            String orderElement, XMLGregorianCalendar date) {
        super(code);
        this.concept = concept;
        this.value = value;
        this.resource = resource;
        this.orderElement = orderElement;
        this.date = date;
    }

    public ExpenseSheetLineDTO(String concept, BigDecimal value, String resource,
            String orderElement,
            XMLGregorianCalendar date) {
        this(generateCode(), concept, value, resource, orderElement, date);
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
