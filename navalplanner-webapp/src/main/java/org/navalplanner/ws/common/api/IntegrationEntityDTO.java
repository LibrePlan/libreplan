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

package org.navalplanner.ws.common.api;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * DTO for <code>IntegrationEntity</code>. All DTOs corresponding to entities
 * to be used in application integration must extend from this DTO.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public abstract class IntegrationEntityDTO {

    public final static String CODE_ATTRIBUTE_NAME = "code";

    @XmlAttribute(name=CODE_ATTRIBUTE_NAME)
    public String code;

    public IntegrationEntityDTO() {}

    public IntegrationEntityDTO(String code) {
        this.code = code;
    }

    /**
     * It returns the String to use in
     * <code>InstanceConstraintViolationsDTOId.entityType</code>.
     */
    public abstract String getEntityType();

    /**
     * This method is useful to implement constructors (in subclasses) that
     * automatically generate a unique code. Such constructors are useful for
     * the implementation of test cases that add new instances (such instances
     * will have a unique code).
     */
    protected static String generateCode() {
        return UUID.randomUUID().toString();
    }

}
