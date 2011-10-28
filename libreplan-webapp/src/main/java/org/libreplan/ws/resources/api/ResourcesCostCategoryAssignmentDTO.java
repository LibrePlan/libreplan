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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.datatype.XMLGregorianCalendar;

import org.libreplan.ws.common.api.IntegrationEntityDTO;

/**
 * DTO for <code>ResourcesCostCategoryAssignment</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class ResourcesCostCategoryAssignmentDTO extends IntegrationEntityDTO {

     public final static String ENTITY_TYPE =
         "resources-cost-category-assignment";

    @XmlAttribute(name="cost-category-name")
    public String costCategoryName;

    @XmlAttribute(name="start-date")
    @XmlSchemaType(name="date")
    public XMLGregorianCalendar startDate;

    @XmlAttribute(name="end-date")
    @XmlSchemaType(name="date")
    public XMLGregorianCalendar endDate;

    public ResourcesCostCategoryAssignmentDTO() {}

    public ResourcesCostCategoryAssignmentDTO(String code,
        String costCategoryName, XMLGregorianCalendar startDate,
        XMLGregorianCalendar endDate) {

        super(code);
        this.costCategoryName = costCategoryName;
        this.startDate = startDate;
        this.endDate = endDate;

    }

    /**
     * This constructor automatically generates a unique code. It is intended
     * to facilitate the implementation of test cases that add new instances
     * (such instances will have a unique code).
     */
    public ResourcesCostCategoryAssignmentDTO(
        String costCategoryName, XMLGregorianCalendar startDate,
        XMLGregorianCalendar endDate) {

        this(generateCode(), costCategoryName, startDate, endDate);

    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
