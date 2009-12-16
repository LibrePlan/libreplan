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

package org.navalplanner.ws.orders.api;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.navalplanner.business.orders.entities.OrderElement;

/**
 * DTO for {@link OrderElement} entity.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@XmlRootElement(name = "order-element")
public class OrderElementDTO {

    @XmlAttribute
    public String name;

    @XmlAttribute
    public String code;

    @XmlAttribute(name = "init-date")
    public Date initDate;

    @XmlAttribute
    public Date deadline;

    @XmlAttribute
    public String description;

    public OrderElementDTO() {
    }

    public OrderElementDTO(String name, String code, Date initDate,
            Date deadline, String description) {
        this.name = name;
        this.code = code;
        this.initDate = initDate;
        this.deadline = deadline;
        this.description = description;
    }

}
