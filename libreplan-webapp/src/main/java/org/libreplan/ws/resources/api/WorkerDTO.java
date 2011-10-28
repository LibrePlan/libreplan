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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for <code>Worker</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@XmlRootElement(name = "worker")
public class WorkerDTO extends ResourceDTO {

    public final static String ENTITY_TYPE = "worker";

    @XmlAttribute(name="first-name")
    public String firstName;

    @XmlAttribute
    public String surname;

    @XmlAttribute
    public String nif;

    public WorkerDTO() {}

    public WorkerDTO(String code, String firstName, String surname,
        String nif) {

        super(code);
        this.firstName = firstName;
        this.surname = surname;
        this.nif = nif;

    }

    /**
     * This constructor automatically generates a unique code. It is intended
     * to facilitate the implementation of test cases that add new instances
     * (such instances will have a unique code).
     */
    public WorkerDTO(String firstName, String surname, String nif) {
        this(generateCode(), firstName, surname, nif);
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

}
